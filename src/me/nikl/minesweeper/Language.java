package me.nikl.minesweeper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;


public class Language {
	private Main plugin;
	private FileConfiguration langFile;
	
	public String GAME_PAYED, GAME_NOT_ENOUGH_MONEY, GAME_WON_MONEY, CMD_NO_PERM, CMD_ONLY_AS_PLAYER, CMD_RELOADED;
	public String TITLE_BEGINNING, TITLE_INGAME, TITLE_END, TITLE_LOST;
	public List<String> CMD_HELP;
	
	public Language(Main plugin){
		this.plugin = plugin;
		if(!getLangFile()){
			plugin.disabled = true;
			return;
		}
		getCommandMessages();
		getGameMessages();
	}
	
	private void getGameMessages() {
		GAME_PAYED = getString("game.econ.payed");
		GAME_NOT_ENOUGH_MONEY = getString("game.econ.notEnoughMoney");
		GAME_WON_MONEY = getString("game.econ.wonMoney");
		
		TITLE_BEGINNING = getString("game.inventoryTitles.beginning");
		TITLE_INGAME = getString("game.inventoryTitles.ingame");
		TITLE_END = getString("game.inventoryTitles.won");
		TITLE_LOST = getString("game.inventoryTitles.lost");
	}

	private void getCommandMessages() {
		CMD_NO_PERM = getString("commandMessages.noPermission");
		CMD_ONLY_AS_PLAYER = getString("commandMessages.onlyAsPlayer");
		CMD_RELOADED = getString("commandMessages.pluginReloaded");		

		this.CMD_HELP = getStringList("commandMessages.help");		
	}

	private List<String> getStringList(String path) {
		if(!langFile.isList(path)){
			return Arrays.asList(" &4StringList missing in Language file (" + path +")");
		}
		return langFile.getStringList(path);
	}

	private String getString(String path) {
		if(!langFile.isString(path)){
			return " &4String missing in language file! (" + path + ")";
		}
		return langFile.getString(path);
	}

	private boolean getLangFile() {
		File defaultEn = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + "lang_en.yml");
		if(!defaultEn.exists()){
			plugin.saveResource("language" + File.separatorChar + "lang_en.yml", false);
		}
		if(!plugin.getConfig().isString("langFile")){
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Language file is missing in the config!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Add the following to your config:"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " langFile: 'lang_en.yml'"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getServer().getPluginManager().disablePlugin(plugin);
			return false;			
		}
		File languageFile = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + plugin.getConfig().getString("langFile"));
		if(!plugin.getConfig().isString("langFile")){
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Language file is missing in the config!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getServer().getPluginManager().disablePlugin(plugin);
			return false;			
		}
		if(!languageFile.exists()){
			languageFile.mkdir();
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Language file not found! Disabling plugin!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getServer().getPluginManager().disablePlugin(plugin);
			return false;
		}
		try { 
			this.langFile = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(languageFile), "UTF-8")); 
		} catch (UnsupportedEncodingException e) { 
			e.printStackTrace(); 
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Error in language file! Disabling plugin!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getServer().getPluginManager().disablePlugin(plugin);
			return false;
		} catch (FileNotFoundException e) { 
			e.printStackTrace(); 
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Error in language file! Disabling plugin!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getServer().getPluginManager().disablePlugin(plugin);
			return false;
		} 
		return true;
		
	}
	
}

