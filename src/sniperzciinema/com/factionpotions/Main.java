
package sniperzciinema.com.factionpotions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.FactionColls;
import com.massivecraft.factions.entity.UPlayer;
import com.massivecraft.factions.event.FactionsEventMembershipChange;
import com.massivecraft.factions.event.FactionsEventMembershipChange.MembershipChangeReason;


public class Main extends JavaPlugin implements Listener {
	
	public void onEnable() {
		
		// Register the event listener
		getServer().getPluginManager().registerEvents(this, this);
		// Create the default config.yml
		getConfig().options().copyDefaults();
		saveConfig();
	}
	
	public void onDisable() {
		
	}
	
	public void update(Player player, Faction faction) {
		removeAllPotions(player);
		addPotions(player, faction);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (cmd.getName().equalsIgnoreCase("FactionPotions"))
		{
			
			if (args.length == 4 && args[0].equalsIgnoreCase("Set"))
			{
				PotionEffect pe = getPotion(args[2], Integer.valueOf(args[3]));
				try
				{
					UPlayer up = UPlayer.get((Player) sender);
					FactionColl fc = FactionColls.get().getForUniverse(up.getUniverse());
					Faction f = fc.getByName(args[1]);
					getConfig().set(f.getId(), pe);
					for (Player player : f.getOnlinePlayers())
						update(player, f);
				}
				catch (Exception e)
				{
					sender.sendMessage("Unable to find the faction...");
				}
				saveConfig();
				sender.sendMessage("Gave " + args[1] + ": " + pe.getType());
			}
			else if (args.length == 2 && args[0].equalsIgnoreCase("Reset"))
			{
				getConfig().set(args[1].toLowerCase(), null);
				saveConfig();
				sender.sendMessage("Reset " + args[1] + "'s potion effect");
			}
			else
			{
				sender.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "========== " + ChatColor.GRAY + ChatColor.UNDERLINE + "Faction Potions" + ChatColor.GOLD + ChatColor.BOLD + " ==========");
				sender.sendMessage("\n" + ChatColor.GREEN + ChatColor.BOLD + "/FactionPotions " + ChatColor.DARK_AQUA + "Set " + ChatColor.YELLOW + "<FactionName> " + ChatColor.LIGHT_PURPLE + "<PotionType> <Level>");
				sender.sendMessage("" + ChatColor.GREEN + ChatColor.BOLD + "/FactionPotions " + ChatColor.DARK_AQUA + "Reset " + ChatColor.YELLOW + "<FactionName>");
			}
			
		}
		return true;
	}
	
	public void removeAllPotions(Player player) {
		for (PotionEffect pe : player.getActivePotionEffects())
			player.removePotionEffect(pe.getType());
	}
	
	public void removePotions(Player player, Faction faction) {
		if (getConfig().get(faction.getId()) != null)
			player.removePotionEffect(((PotionEffect) getConfig().get(faction.getId())).getType());
	}
	
	public void addPotions(Player player, Faction faction) {
		if (getConfig().get(faction.getId()) != null)
			player.addPotionEffect((PotionEffect) getConfig().get(faction.getId()));
	}
	
	public static PotionEffect getPotion(String type, int level) {
		Integer time = 0;
		time = Integer.MAX_VALUE;
		return new PotionEffect(PotionEffectType.getByName(type), time, level - 1);
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		
		if (args.length == 2 && args[0].equalsIgnoreCase("Set"))
		{
			List<String> factions = new ArrayList<String>();
			
			for (Faction f : FactionColls.get().getForUniverse(UPlayer.get((Player) sender).getUniverse()).getAll())
				factions.add(f.getName());
			
			return TabCompletionHelper.getPossibleCompletionsForGivenArgs(args, factions);
		}
		else if (args.length == 3 && args[0].equalsIgnoreCase("Set"))
		{
			List<String> potions = new ArrayList<String>();
			
			for (PotionEffectType pe : PotionEffectType.values())
				if (pe != null)
					potions.add(pe.getName());
			
			return TabCompletionHelper.getPossibleCompletionsForGivenArgs(args, potions);
		}
		
		else if (args.length == 4 && args[0].equalsIgnoreCase("Set"))
		{
			List<String> numbers = new ArrayList<String>();
			for (int i = 1; i != 10; i++)
				numbers.add(String.valueOf(i));
			
			return TabCompletionHelper.getPossibleCompletionsForGivenArgs(args, numbers);
		}
		
		return null;
	}
	
	@EventHandler
	public void playerJoinEvent(PlayerJoinEvent e) {
		update(e.getPlayer(), UPlayer.get(e.getPlayer()).getFaction());
	}
	
	@EventHandler
	public void factionLeave(FactionsEventMembershipChange e) {
		if (e.getReason() == MembershipChangeReason.JOIN)
			addPotions(e.getUSender().getPlayer(), e.getUPlayer().getFaction());
		if (e.getReason() == MembershipChangeReason.LEAVE)
			removePotions(e.getUSender().getPlayer(), e.getUPlayer().getFaction());
		if (e.getReason() == MembershipChangeReason.DISBAND)
			for (Player player : e.getUPlayer().getFaction().getOnlinePlayers())
				removePotions(player, e.getUPlayer().getFaction());
	}
	
}
