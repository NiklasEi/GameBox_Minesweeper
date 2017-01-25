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

import me.nikl.minesweeper.commands.Commands;
import me.nikl.minesweeper.commands.TopCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.nikl.minesweeper.nms.*;
import net.milkbowl.vault.economy.Economy;


public class Main extends JavaPlugin {

	private GameManager manager;
	private FileConfiguration config, statistics;
	private File con, sta;
	public static Economy econ = null;
	public static boolean playSounds = true;
	public boolean econEnabled, wonCommandsEnabled;
	public List<String> wonCommands;
	public Double reward, price;
	public Language lang;
	public boolean disabled;
	private Update updater;
	
	public boolean automaticReveal;
	
	@Override
	public void onEnable(){

        if (!setupUpdater()) {
            getLogger().severe("Your server version is not compatible with this plugin!");

            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
		this.con = new File(this.getDataFolder().toString() + File.separatorChar + "config.yml");
		this.sta = new File(this.getDataFolder().toString() + File.separatorChar + "stats.yml");

		reload();
		if(disabled) return;

		this.setManager(new GameManager(this));
        this.getCommand("minesweeper").setExecutor(new Commands(this));
        this.getCommand("minesweeperTop").setExecutor(new TopCommand(this));
	}
	
	private boolean setupUpdater() {
		String version;
	
		try {
			version = Bukkit.getServer().getClass().getPackage().getName().replace(".",  ",").split(",")[3];
		} catch (ArrayIndexOutOfBoundsException whatVersionAreYouUsingException) {
			return false;
		}
	
		//getLogger().info("Your server is running version " + version);
	
		if (version.equals("v1_10_R1")) {
			updater = new Update_1_10_R1();
			
		} else if (version.equals("v1_9_R2")) {
			updater = new Update_1_9_R2();
			
		} else if (version.equals("v1_9_R1")) {
			updater = new Update_1_9_R1();
			
		} else if (version.equals("v1_8_R3")) {
			updater = new Update_1_8_R3();
			
		} else if (version.equals("v1_8_R2")) {
			updater = new Update_1_8_R2();
			
		} else if (version.equals("v1_8_R1")) {
			updater = new Update_1_8_R1();
			
		} else if (version.equals("v1_11_R1")) {
			updater = new Update_1_11_R1();
		}
		return updater != null;
	}

	public Update getUpdater(){
		return this.updater;
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
		
		this.lang = new Language(this);
		
		// load stats file
		try {
			this.statistics = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(this.sta), "UTF-8"));
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		}
		
		this.wonCommandsEnabled = false;
		this.wonCommands = new ArrayList<>();
		if(this.config.isBoolean("wonCommands.enabled")){
			this.wonCommandsEnabled = config.getBoolean("wonCommands.enabled");
			this.wonCommands = config.getStringList("wonCommands.commands");
		}
		
		this.automaticReveal = true;
		if(config.getBoolean("rules.turnOffAutomaticRevealing")){
			automaticReveal = false;
		}
		
		
		if(config.isBoolean("rules.playSounds")){
			playSounds = config.getBoolean("rules.playSounds");
		}
		
		this.econEnabled = false;
		if(getConfig().getBoolean("economy.enabled")){
			this.econEnabled = true;
			if (!setupEconomy()){
				Bukkit.getConsoleSender().sendMessage(chatColor(lang.PREFIX + " &4No economy found!"));
				getServer().getPluginManager().disablePlugin(this);
				disabled = true;
				return;
			}
			
			this.price = getConfig().getDouble("economy.cost");
			this.reward = getConfig().getDouble("economy.reward");
			if(price == null || reward == null || price < 0. || reward < 0.){
				Bukkit.getConsoleSender().sendMessage(chatColor(lang.PREFIX + " &4Wrong configuration in section economy!"));
				getServer().getPluginManager().disablePlugin(this);
			}
		}
	}
	
	public FileConfiguration getStatistics(){
		return this.statistics;
	}

	public void setManager(GameManager manager) {
		this.manager = manager;
	}

	public FileConfiguration getConfig() {
		return config;
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
		String[] timeSplit = displayTime.split(":");
		int newTime = Integer.parseInt(timeSplit[0])*60 + Integer.parseInt(timeSplit[1]);
		if(!statistics.isInt(player.toString() + "." + bombsNum)){
			statistics.set(player.toString() + "." + bombsNum, newTime);
			return;
		}
		boolean newRecord = false;
		int oldTime = statistics.getInt(player.toString() + "." + bombsNum);
		if(newTime < oldTime){
			newRecord = true;
		}
		if(!newRecord) return;
		this.statistics.set(player.toString() + "." + bombsNum, newTime);
	}
}
