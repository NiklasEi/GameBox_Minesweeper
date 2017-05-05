package me.nikl.minesweeper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;



public class Language {
	private Main plugin;
	private FileConfiguration langFile;
	
	public String GAME_PAYED, GAME_NOT_ENOUGH_MONEY, GAME_WON_MONEY;
	public String TITLE_BEGINNING, TITLE_INGAME, TITLE_END, TITLE_LOST;
	public String PREFIX = "[Minesweeper]", NAME = "&1Minesweeper&r";
	public List<String> GAME_HELP;
	private YamlConfiguration defaultLang;
	
	public Language(Main plugin){
		this.plugin = plugin;
		getLangFile();
		PREFIX = getString("prefix");
		NAME = getString("name");
		getGameMessages();
		// check for 1.8.x version
		// if this is the version of the server check the length of the titles and shorten them if necessary
		if(Bukkit.getBukkitVersion().split("\\.|-")[0].equals("1") && Bukkit.getBukkitVersion().split("\\.|-")[1].equals("8")){
			boolean set = false; 
			if(TITLE_BEGINNING.length() > 32){
				set = true;
				TITLE_BEGINNING = "Title in lang file too long!";
			}
			if((TITLE_INGAME.replaceAll("%state%", "55555").replaceAll("%timer%", "55555")).length() > 32){
				set = true;
				TITLE_INGAME = "Title in lang file too long!";
			}
			if((TITLE_END.replaceAll("%timer%", "55555")).length() > 32){
				set = true;
				TITLE_END = "Title in lang file too long!";
			}
			if(TITLE_LOST.length() > 32){
				set = true;
				TITLE_LOST = "Title in lang file too long!";
			}
			if(set){
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', this.PREFIX + " &4+-+-+-+-+-+-+-+-+-+ Warning +-+-+-+-+-+-+-+-+-+"));
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', this.PREFIX + " &4In 1.8.x inventory titles longer than 32 characters will lead to an error"));
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', this.PREFIX + " &4Since this does not happen in 1.9.x and above the default titles are longer"));
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', this.PREFIX + " &4Please shorten the inventory titles in the language file you use (set in the config)"));
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', this.PREFIX + " &4Until you do this the titles will be shortend by the plugin to prevent the error!"));
			}
		}
	}
	
	private void getGameMessages() {
		GAME_PAYED = getString("game.econ.payed");
		GAME_NOT_ENOUGH_MONEY = getString("game.econ.notEnoughMoney");
		GAME_WON_MONEY = getString("game.econ.wonMoney");

		this.GAME_HELP = getStringList("gameHelp");
		
		TITLE_BEGINNING = getString("game.inventoryTitles.beginning");
		TITLE_INGAME = getString("game.inventoryTitles.ingame");
		TITLE_END = getString("game.inventoryTitles.won");
		TITLE_LOST = getString("game.inventoryTitles.lost");
	}


	private List<String> getStringList(String path) {
		List<String> toReturn;
		if(!langFile.isList(path)){
			toReturn = defaultLang.getStringList(path);
			for(int i = 0; i<toReturn.size(); i++){
				toReturn.set(i, ChatColor.translateAlternateColorCodes('&',toReturn.get(i)));
			}
			return toReturn;
		}
		toReturn = langFile.getStringList(path);
		for(int i = 0; i<toReturn.size(); i++){
			toReturn.set(i, ChatColor.translateAlternateColorCodes('&',toReturn.get(i)));
		}
		return toReturn;
	}

	private String getString(String path) {
		if(!langFile.isString(path)){
			return ChatColor.translateAlternateColorCodes('&',defaultLang.getString(path));
		}
		return ChatColor.translateAlternateColorCodes('&',langFile.getString(path));
	}

	private void getLangFile() {
		try {
			String fileName = "language/lang_en.yml";
			this.defaultLang =  YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(fileName), "UTF-8"));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}
		
		File defaultEn = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + "lang_en.yml");
		if(!defaultEn.exists()){
			defaultEn.getParentFile().mkdirs();
			plugin.saveResource("language" + File.separatorChar + "lang_en.yml", false);
		}
		File defaultEs = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + "lang_es.yml");
		if(!defaultEs.exists()){
			plugin.saveResource("language" + File.separatorChar + "lang_es.yml", false);
		}
		File defaultCn = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + "lang_zh-cn.yml");
		if(!defaultCn.exists()){
			plugin.saveResource("language" + File.separatorChar + "lang_zh-cn.yml", false);
		}
		File defaultDe = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + "lang_de.yml");
		if(!defaultDe.exists()){
			plugin.saveResource("language" + File.separatorChar + "lang_de.yml", false);
		}
		if(!plugin.getConfig().isString("langFile")){
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Language file is missing in the config!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Add the following to your config:"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " langFile: 'lang_en.yml'"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Using default language file"));
			this.langFile = defaultLang;
			return;
		}
		String fileName = plugin.getConfig().getString("langFile");
		if(fileName.equalsIgnoreCase("default") || fileName.equalsIgnoreCase("default.yml")){
			this.langFile = defaultLang;
			return;
		}
		File languageFile = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + plugin.getConfig().getString("langFile"));
		if(!languageFile.exists()){
			languageFile.mkdir();
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Language file not found!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Using default language file"));
			this.langFile = defaultLang;
			return;
		}
		try { 
			this.langFile = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(languageFile), "UTF-8")); 
		} catch (UnsupportedEncodingException e) { 
			e.printStackTrace(); 
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Error in language file!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Using default language file"));
			this.langFile = defaultLang;
			return;
		} catch (FileNotFoundException e) { 
			e.printStackTrace(); 
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Error in language file!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Using default language file"));
			this.langFile = defaultLang;
			return;
		} 
		int count = 0;
		for(String key : defaultLang.getKeys(true)){
			if(defaultLang.isString(key)){
				if(!this.langFile.isString(key)){// there is a message missing
					if(count == 0){
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*"));
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Missing message(s) in your language file!"));
					}
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " " + key));
					count++;
				}
			} else if (defaultLang.isList(key)){
				if(!this.langFile.isList(key)){// there is a message missing
					if(count == 0){
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*"));
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Missing message(s) in your language file!"));
					}
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " " + key + "     (StringList!)"));
					count++;
				}
			}
		}
		if(count > 0){
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + ""));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Game will use default messages for these paths"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + ""));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Please get an updated language file"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Or add the listed paths by hand"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*"));
		}
		return;
		
	}
	
}

