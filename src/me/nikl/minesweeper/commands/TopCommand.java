package me.nikl.minesweeper.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import me.nikl.minesweeper.Language;
import me.nikl.minesweeper.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;


public class TopCommand implements CommandExecutor {
	
	private Main plugin;
	private Language lang;
	private FileConfiguration stats;
	private String structure;

	private int easyBombs, normalBombs, hardBombs;
	
	public TopCommand(Main plugin){
		this.plugin = plugin;
		this.lang = plugin.lang;
		this.stats = plugin.getStatistics();
		this.structure = lang.CMD_TOP_STRUCTURE;

		if(plugin.getConfig().isConfigurationSection("mines")) {
            this.easyBombs = plugin.getConfig().getInt("mines.easy", 5);
            this.normalBombs = plugin.getConfig().getInt("mines.normal", 8);
            this.hardBombs = plugin.getConfig().getInt("mines.hard", 11);
        } else {
		    Bukkit.getLogger().log(Level.WARNING, "Please update your configuration file!");
            Bukkit.getLogger().log(Level.WARNING, "You have to provide three numbers of mines like:");
            Bukkit.getLogger().log(Level.WARNING, "     mines:");
            Bukkit.getLogger().log(Level.WARNING, "       easy: 5");
            Bukkit.getLogger().log(Level.WARNING, "       normal: 8");
            Bukkit.getLogger().log(Level.WARNING, "       hard: 11");
            Bukkit.getLogger().log(Level.WARNING, "Continuing with default values: 5, 8, 11");

            easyBombs = 5;
            normalBombs = 8;
            hardBombs = 11;
        }

		if(easyBombs < 1 || normalBombs < 1  || hardBombs < 1 ){
			if(easyBombs<1){
				this.easyBombs = 5;
			}
			if(normalBombs<1){
				this.normalBombs = 8;
			}
			if(hardBombs<1){
				this.hardBombs = 11;
			}
		}
		if(easyBombs > 30 || normalBombs > 30  || hardBombs > 30  ){
			if(easyBombs> 30 ){
				this.easyBombs = 5;
			}
			if(normalBombs> 30 ){
				this.normalBombs = 8;
			}
			if(hardBombs> 30 ){
				this.hardBombs = 11;
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("minesweeper.top")){
			sender.sendMessage(plugin.chatColor(lang.PREFIX + lang.CMD_NO_PERM));
			return true;	
		}
		String mode = "normal";
		if(args.length == 0 || args[0].equalsIgnoreCase("normal") || args[0].equalsIgnoreCase("easy") || args[0].equalsIgnoreCase("hard") || args[0].equalsIgnoreCase("e") || args[0].equalsIgnoreCase("n") || args[0].equalsIgnoreCase("h")){
			if(!(args.length == 0)) mode = args[0].toLowerCase();
		} else {
            for(String message :  lang.CMD_TOP_HELP)
                sender.sendMessage(plugin.chatColor(lang.PREFIX + message));
            return true;
		}
		int bombsNum = 0;
		switch (mode){
            case "e":
                mode = "easy";
			case "easy":
				bombsNum = easyBombs;
				break;
            case "n":
                mode = "normal";
			case "normal":
				bombsNum = normalBombs;
				break;
            case "h":
                mode = "hard";
			case "hard":
				bombsNum = hardBombs;
				break;
			default:
				break;
		}
		Map<UUID, Integer> times = new HashMap<UUID, Integer>();
		for(String uuid : stats.getKeys(false)){
			if(stats.isSet(uuid+ "." + bombsNum)){
				times.put(UUID.fromString(uuid), stats.getInt(uuid+ "." + bombsNum));
			}
		}
		if(times.size() == 0){
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.PREFIX + lang.CMD_NO_TOP_LIST.replace("%mode%", mode)));
			return true;
		}
		int length = (times.size() > 10? 10 : times.size());
		String[] messages = new String[length];
		UUID bestRecord = null;
		int record = 0;
		for(int i = 0; i<length;i++){
			record = 0;
			for(UUID current : times.keySet()){
				if(record == 0){
					record = times.get(current);
					bestRecord = current;
					continue;
				}
				if(times.get(current) < record){
					record = times.get(current);
					bestRecord = current;
					continue;
				}
			}
			String minutes = (record/60) + "";
			if(minutes.length()<2) minutes = "0" + minutes;
			String seconds = (record%60) + "";
			if(seconds.length()<2) seconds = "0" + seconds; 
			times.remove(bestRecord);
			String name;
			if(bestRecord == null){
				name = "PlayerNotFound";
			} else {
				name = (Bukkit.getOfflinePlayer(bestRecord) == null ? "PlayerNotFound" : (Bukkit.getOfflinePlayer(bestRecord).getName() == null ? "PlayerNotFound" : Bukkit.getOfflinePlayer(bestRecord).getName()));
			}
			messages[i] = structure.replaceAll("%rank%", (i+1)+"").replaceAll("%name%", name).replaceAll("%time%", minutes + ":" + seconds);
		}
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.CMD_TOP_HEAD.replace("%mode%", mode)));
		for(String message : messages){
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		}
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.CMD_TOP_TAIL));
		return true;
	}
}
