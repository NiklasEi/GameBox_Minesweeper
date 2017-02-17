package me.nikl.minesweeper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.logging.Level;

import me.nikl.gamebox.ClickAction;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import me.nikl.gamebox.guis.gui.game.GameGui;
import me.nikl.minesweeper.commands.TopCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
	private String gameID = "minesweeper";
	GameBox gameBox;

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

		hook();
		if(disabled) return;

        //this.getCommand("minesweeperTop").setExecutor(new TopCommand(this));
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

	private void hook() {
		if(Bukkit.getPluginManager().getPlugin("GameBox") == null){
			Bukkit.getLogger().log(Level.SEVERE, " GameBox not found");
			Bukkit.getPluginManager().disablePlugin(this);
			disabled = true;
			return;
		}





		gameBox = (me.nikl.gamebox.GameBox)Bukkit.getPluginManager().getPlugin("GameBox");

		// disable economy if it is disabled for either one of the plugins
		this.econEnabled = this.econEnabled && gameBox.getEconEnabled();
		playSounds = playSounds && GameBox.playSounds;

		GUIManager guiManager = gameBox.getPluginManager().getGuiManager();

		this.manager = new GameManager(this);

		gameBox.getPluginManager().registerGame(manager, gameID, lang.NAME);

		GameGui gameGui = new GameGui(gameBox, guiManager, 54, gameID, "main");



		Map<String, GameRules> gameTypes = new HashMap<>();

		if(config.isConfigurationSection("gameBox.gameButtons")){
			ConfigurationSection gameButtons = config.getConfigurationSection("gameBox.gameButtons");
			ConfigurationSection buttonSec;
			int bombsNum;
			double cost, reward;

			String displayName;
			ArrayList<String> lore;

			GameRules rules;

			for(String buttonID : gameButtons.getKeys(false)){
				buttonSec = gameButtons.getConfigurationSection(buttonID);


				if(!buttonSec.isString("materialData")){
					Bukkit.getLogger().log(Level.WARNING, " missing material data under: gameBox.gameButtons." + buttonID + "        can not load the button");
					continue;
				}

				ItemStack mat = getItemStack(buttonSec.getString("materialData"));
				if(mat == null){
					Bukkit.getLogger().log(Level.WARNING, " error loading: gameBox.gameButtons." + buttonID);
					Bukkit.getLogger().log(Level.WARNING, "     invalid material data");
					continue;
				}


				AButton button =  new AButton(mat.getData(), 1);
				ItemMeta meta = button.getItemMeta();

				if(buttonSec.isString("displayName")){
					displayName = chatColor(buttonSec.getString("displayName"));
					meta.setDisplayName(displayName);
				}

				if(buttonSec.isList("lore")){
					lore = new ArrayList<>(buttonSec.getStringList("lore"));
					for(int i = 0; i < lore.size();i++){
						lore.set(i, chatColor(lore.get(i)));
					}
					meta.setLore(lore);
				}

				button.setItemMeta(meta);
				button.setAction(ClickAction.START_GAME);
				button.setArgs(gameID, buttonID);

				if(!buttonSec.isInt("mines")){
					Bukkit.getLogger().log(Level.WARNING, "[Minesweeper] not specified number of mines for gameBox.gameButtons." + buttonID);
				}
				bombsNum = buttonSec.getInt("mines", 8);
				cost = buttonSec.getDouble("cost", 0.);
				reward = buttonSec.getDouble("reward", 0.);


				rules = new GameRules(bombsNum, cost, reward);

				if(buttonSec.isInt("slot")){
					gameGui.setButton(button, buttonSec.getInt("slot"));
				} else {
					gameGui.setButton(button);
				}

				gameTypes.put(buttonID, rules);
			}
		}


		this.manager.setGameTypes(gameTypes);


		getMainButton:
		if(config.isConfigurationSection("gameBox.mainButton")){
			ConfigurationSection mainButtonSec = config.getConfigurationSection("gameBox.mainButton");
			if(!mainButtonSec.isString("materialData")) break getMainButton;

			ItemStack gameButton = getItemStack(mainButtonSec.getString("materialData"));
			if(gameButton == null){
				gameButton = (new ItemStack(Material.TNT));
			}
			ItemMeta meta = gameButton.getItemMeta();
			meta.setDisplayName(chatColor(mainButtonSec.getString("displayName","&3Minesweeper")));
			if(mainButtonSec.isList("lore")){
				ArrayList<String> lore = new ArrayList<>(mainButtonSec.getStringList("lore"));
				for(int i = 0; i < lore.size();i++){
					lore.set(i, chatColor(lore.get(i)));
				}
				meta.setLore(lore);
			}
			gameButton.setItemMeta(meta);
			guiManager.registerGameGUI(gameID, "main", gameGui, gameButton, "minesweeper", "ms");
		} else {
			Bukkit.getLogger().log(Level.WARNING, " Missing or wrong configured main button in the configuration file!");
		}

	}




	private ItemStack getItemStack(String itemPath){
		Material mat; short data;
		String[] obj = itemPath.split(":");

		if (obj.length == 2) {
			try {
				mat = Material.matchMaterial(obj[0]);
			} catch (Exception e) {
				return null; // material name doesn't exist
			}

			try {
				data = Short.valueOf(obj[1]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return null; // data not a number
			}

			//noinspection deprecation
			if(mat == null) return null;
			ItemStack stack = new ItemStack(mat);
			stack.setDurability(data);
			return stack;
		} else {
			try {
				mat = Material.matchMaterial(obj[0]);
			} catch (Exception e) {
				return null; // material name doesn't exist
			}
			//noinspection deprecation
			return (mat == null ? null : new ItemStack(mat));
		}
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

	public String getGameID(){
    	return this.gameID;
	}
}
