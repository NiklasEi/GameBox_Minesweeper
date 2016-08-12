package me.nikl.minesweeper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import net.md_5.bungee.api.ChatColor;

public class TopCommand implements CommandExecutor {
	
	private Main plugin;
	private Language lang;
	private FileConfiguration stats;
	private String structure;
	
	public TopCommand(Main plugin){
		this.plugin = plugin;
		this.lang = plugin.lang;
		this.stats = plugin.getStatistics();
		this.structure = lang.CMD_TOP_STRUCTURE;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args) {
		if(!sender.hasPermission("minesweeper.top")){
			sender.sendMessage(plugin.chatColor(lang.PREFIX + lang.CMD_NO_PERM));
			return true;	
		}
		int bombsNum = 0;
		if(plugin.getConfig().isInt("mines")){
			bombsNum = plugin.getConfig().getInt("mines");
		}
		if(bombsNum < 1 || bombsNum > 30){
			bombsNum = 8;
		}
		Map<UUID, Integer> times = new HashMap<UUID, Integer>();
		for(String uuid : stats.getKeys(false)){
			if(stats.isSet(uuid+ "." + bombsNum)){
				times.put(UUID.fromString(uuid), stats.getInt(uuid+ "." + bombsNum));
			}
		}
		if(times.size() == 0){
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.CMD_NO_TOP_LIST));
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
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.CMD_TOP_HEAD));
		for(String message : messages){
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		}
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.CMD_TOP_TAIL));
		return true;
	}
}
