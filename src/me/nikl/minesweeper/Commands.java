package me.nikl.minesweeper;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
	
	private Main plugin;
	private Language lang;
	
	public Commands(Main plugin){
		this.plugin = plugin;
		this.lang = plugin.lang;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args) {
		if(args.length == 0){
			if(!(sender instanceof Player)){
				sender.sendMessage(plugin.chatColor(Main.prefix + lang.CMD_ONLY_AS_PLAYER));
				return true;
			}
			Player player = (Player) sender;
			if(!player.hasPermission("minesweeper.play")){
				sender.sendMessage(plugin.chatColor(Main.prefix + lang.CMD_NO_PERM));
				return true;				
			}
			if(plugin.getEconEnabled() && !player.hasPermission("minesweeper.bypass")){
				if(Main.econ.getBalance(player) >= plugin.getPrice()){
					Main.econ.withdrawPlayer(player, plugin.getPrice());
					sender.sendMessage(plugin.chatColor(Main.prefix + lang.GAME_PAYED.replaceAll("%cost%", plugin.getPrice()+"")));
					plugin.getManager().startGame((Player) sender);
					return true;					
				} else {
					player.sendMessage(plugin.chatColor(Main.prefix + lang.GAME_NOT_ENOUGH_MONEY));
					return true;
				}
			} else {
				plugin.getManager().startGame((Player) sender);
				return true;
			}
		} else if(args.length == 1 && args[0].equalsIgnoreCase("reload")){
			if(sender.hasPermission("minesweeper.reload")){
				plugin.reload();
				sender.sendMessage(plugin.chatColor(Main.prefix + lang.CMD_RELOADED));
				return true;
			} else {
				sender.sendMessage(plugin.chatColor(Main.prefix + lang.CMD_NO_PERM));
				return true;
			}
		}
		for(String message :  lang.CMD_HELP)
			sender.sendMessage(plugin.chatColor(Main.prefix + message));
		return true;
	}

}
