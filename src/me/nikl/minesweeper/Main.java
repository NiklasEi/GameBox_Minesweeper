package me.nikl.minesweeper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;


public class Main extends JavaPlugin {

	private GameManager manager;
	private FileConfiguration config, statistics;
	private File con, sta;
	public static Economy econ = null;
	public String prefix;
	public Boolean econEnabled, wonCommandsEnabled;
	public List<String> wonCommands;
	public Double reward, price;
	public Language lang;
	public boolean disabled;
	
	@Override
	public void onEnable(){
		this.con = new File(this.getDataFolder().toString() + File.separatorChar + "config.yml");
		this.sta = new File(this.getDataFolder().toString() + File.separatorChar + "stats.yml");

		reload();
		if(disabled) return;

		this.setManager(new GameManager(this));
        this.getCommand("minesweeper").setExecutor(new Commands(this));
	}
	
	@Override
	public void onDisable(){
		try {
			this.statistics.save(sta);
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Could not save statistics", e);
		}
	}
	
    private boolean setupEconomy(){
    	if (getServer().getPluginManager().getPlugin("Vault") == null) {
    		return false;
    	}
    	RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
    	if (rsp == null) {
    		return false;
    	}
    	econ = (Economy)rsp.getProvider();
    	return econ != null;
    }
	
	public void reloadConfig(){
		try { 
			this.config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(this.con), "UTF-8")); 
		} catch (UnsupportedEncodingException e) { 
			e.printStackTrace(); 
		} catch (FileNotFoundException e) { 
			e.printStackTrace(); 
		} 
 
		InputStream defConfigStream = this.getResource("config.yml"); 
		if (defConfigStream != null){		
			@SuppressWarnings("deprecation") 
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream); 
			this.config.setDefaults(defConfig); 
		} 
	} 
	
	public GameManager getManager() {
		return manager;
	}
	
	public void reload(){
		if(!con.exists()){
			this.saveResource("config.yml", false);
		}
		if(!sta.exists()){
			try {
				sta.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		reloadConfig();
		
		// load statsfile
		try {
			this.statistics = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(this.sta), "UTF-8"));
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		} 
		
		this.lang = new Language(this);
		
		this.wonCommandsEnabled = false;
		this.wonCommands = new ArrayList<String>();
		if(this.config.isBoolean("wonCommands.enabled")){
			this.wonCommandsEnabled = config.getBoolean("wonCommands.enabled");
			this.wonCommands = config.getStringList("wonCommands.commands");
		}
		
		this.econEnabled = false;
		if(getConfig().getBoolean("economy.enabled")){
			this.econEnabled = true;
			if (!setupEconomy()){
				Bukkit.getConsoleSender().sendMessage(chatColor(prefix + " &4No economy found!"));
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
			this.price = getConfig().getDouble("economy.cost");
			this.reward = getConfig().getDouble("economy.reward");
			if(price == null || reward == null || price < 0. || reward < 0.){
				Bukkit.getConsoleSender().sendMessage(chatColor(prefix + " &4Wrong configuration in section economy!"));
				getServer().getPluginManager().disablePlugin(this);
			}
		}
	}

	public void setManager(GameManager manager) {
		this.manager = manager;
	}

	public FileConfiguration getConfig() {
		return config;
	}

	public void setConfig(FileConfiguration config) {
		this.config = config;
	}
	
    public String chatColor(String message){
    	return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public Boolean getEconEnabled(){
    	return this.econEnabled;
    }
    
    public Double getReward(){
    	return this.reward;
    }
    
    public Double getPrice(){
    	return this.price;
    }

	public void setStatistics(UUID player, String displayTime, int bombsNum) {
		if(this.statistics == null) return;
		if(!statistics.isSet(player.toString() + "." + bombsNum)){
			statistics.set(player.toString() + "." + bombsNum, displayTime);
			return;
		}
		boolean newRecord = false;
		String[] newTime = displayTime.split(":");
		String[] oldTime = statistics.getString(player.toString() + "." + bombsNum).split(":");
		if(Integer.parseInt(newTime[0]) < Integer.parseInt(oldTime[0])){
			newRecord = true;
		} else if(Integer.parseInt(newTime[1]) < Integer.parseInt(oldTime[1])){
			newRecord = true;
		}
		if(!newRecord) return;
		this.statistics.set(player.toString() + "." + bombsNum, displayTime);
	}
}
