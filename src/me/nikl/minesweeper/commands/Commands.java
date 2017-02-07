package me.nikl.minesweeper.commands;

import me.nikl.minesweeper.Language;
import me.nikl.minesweeper.Main;
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
				sender.sendMessage(plugin.chatColor(lang.PREFIX + lang.CMD_ONLY_AS_PLAYER));
				return true;
			}
			Player player = (Player) sender;
			if(!player.hasPermission("minesweeper.play")){
				sender.sendMessage(plugin.chatColor(lang.PREFIX + lang.CMD_NO_PERM));
				return true;				
			}


			if(plugin.getEconEnabled() && !player.hasPermission("minesweeper.bypass")){
				if(Main.econ.getBalance(player) >= plugin.getPrice()){
					Main.econ.withdrawPlayer(player, plugin.getPrice());
					sender.sendMessage(plugin.chatColor(lang.PREFIX + lang.GAME_PAYED.replaceAll("%cost%", plugin.getPrice()+"")));
					plugin.getManager().startGame((Player) sender);
					return true;					
				} else {
					player.sendMessage(plugin.chatColor(lang.PREFIX + lang.GAME_NOT_ENOUGH_MONEY));
					return true;
				}
			} else {
				plugin.getManager().startGame((Player) sender);
				return true;
			}


		} else if(args.length == 1){
			if(args[0].equalsIgnoreCase("reload")) {
				if (sender.hasPermission("minesweeper.reload")) {
					plugin.reload();
					sender.sendMessage(plugin.chatColor(lang.PREFIX + lang.CMD_RELOADED));
					return true;
				} else {
					sender.sendMessage(plugin.chatColor(lang.PREFIX + lang.CMD_NO_PERM));
					return true;
				}
			}

			// open game with specified mode
			if(args[0].equalsIgnoreCase("easy") || args[0].equalsIgnoreCase("normal") || args[0].equalsIgnoreCase("hard") || args[0].equalsIgnoreCase("e") || args[0].equalsIgnoreCase("n") || args[0].equalsIgnoreCase("h")) {
				if(!(sender instanceof Player)){
					sender.sendMessage(plugin.chatColor(lang.PREFIX + lang.CMD_ONLY_AS_PLAYER));
					return true;
				}
				Player player = (Player) sender;
				if(!player.hasPermission("minesweeper.play")){
					sender.sendMessage(plugin.chatColor(lang.PREFIX + lang.CMD_NO_PERM));
					return true;
				}

				switch (args[0].toLowerCase()){
					case "e":
						args[0] = "easy";
						break;
					case "n":
						args[0] = "normal";
						break;
					case "h":
						args[0] = "hard";
						break;
				}

				if(plugin.getEconEnabled() && !player.hasPermission("minesweeper.bypass")){
					if(Main.econ.getBalance(player) >= plugin.getPrice()){
						Main.econ.withdrawPlayer(player, plugin.getPrice());
						sender.sendMessage(plugin.chatColor(lang.PREFIX + lang.GAME_PAYED.replaceAll("%cost%", plugin.getPrice()+"")));
						plugin.getManager().startGame((Player) sender, args[0].toLowerCase());
						return true;
					} else {
						player.sendMessage(plugin.chatColor(lang.PREFIX + lang.GAME_NOT_ENOUGH_MONEY));
						return true;
					}
				} else {
					plugin.getManager().startGame((Player) sender, args[0].toLowerCase());
					return true;
				}

			}
		}
		for(String message :  lang.CMD_HELP)
			sender.sendMessage(plugin.chatColor(lang.PREFIX + message));
		return true;
	}

}
