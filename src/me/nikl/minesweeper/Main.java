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
import me.nikl.gamebox.data.SaveType;
import me.nikl.gamebox.guis.GUIManager;
import me.nikl.gamebox.guis.button.AButton;
import me.nikl.gamebox.guis.gui.game.GameGui;
import me.nikl.gamebox.guis.gui.game.TopListPage;
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
	private FileConfiguration config;
	private File con;
	public static Economy econ = null;
	public static boolean playSounds = true;
	public boolean econEnabled;
	public Language lang;
	public boolean disabled;
	private Update updater;
	
	public boolean automaticReveal;
	public static String gameID = "minesweeper";
	GameBox gameBox;

	private final String[][] depends =  new String[][]{
			new String[]{"Vault", "1.5"},
			new String[]{"GameBox", "1.0.1"}
	};


	@Override
	public void onEnable(){

        if (!setupUpdater()) {
            getLogger().severe("Your server version is not compatible with this plugin!");

            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
		this.con = new File(this.getDataFolder().toString() + File.separatorChar + "config.yml");

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
		if(Bukkit.getPluginManager().getPlugin("GameBox") == null || !Bukkit.getPluginManager().getPlugin("GameBox").isEnabled()){
			Bukkit.getLogger().log(Level.SEVERE, " GameBox not found");
			Bukkit.getPluginManager().disablePlugin(this);
			disabled = true;
			return;
		}


		gameBox = (me.nikl.gamebox.GameBox)Bukkit.getPluginManager().getPlugin("GameBox");

		String[] versionString = gameBox.getDescription().getVersion().split("\\.");
		String[] minVersionString = depends[1][1].split("\\.");
		Integer[] version = new Integer[versionString.length];
		Integer[] minVersion = new Integer[minVersionString.length];

		for(int i = 0; i < minVersionString.length; i++){
			try {
				minVersion[i] = Integer.valueOf(minVersionString[i]);
				version[i] = Integer.valueOf(versionString[i]);
			} catch (NumberFormatException exception){
				exception.printStackTrace();
			}
		}

		for(int i = 0; i < minVersion.length; i++){
			if(minVersion[i] < version[i]) break;
			if(minVersion[i].equals(version[i])) continue;

			Bukkit.getLogger().log(Level.WARNING, " Your GameBox is outdated!");
			Bukkit.getLogger().log(Level.WARNING, " Get the latest version here: https://www.spigotmc.org/resources/gamebox.37273/");
			Bukkit.getLogger().log(Level.WARNING, " You need at least version " + depends[1][1]);
			Bukkit.getPluginManager().disablePlugin(this);
			disabled = true;
			return;
		}


		// disable economy if it is disabled for either one of the plugins
		this.econEnabled = this.econEnabled && gameBox.getEconEnabled();
		playSounds = playSounds && GameBox.playSounds;

		GUIManager guiManager = gameBox.getPluginManager().getGuiManager();

		this.manager = new GameManager(this);

		gameBox.getPluginManager().registerGame(manager, gameID, lang.NAME, 1);

		int gameGuiSlots = 54;
		GameGui gameGui = new GameGui(gameBox, guiManager, gameGuiSlots, gameID, "main");
		gameGui.setHelpButton(lang.GAME_HELP);


		Map<String, GameRules> gameTypes = new HashMap<>();

		if(config.isConfigurationSection("gameBox.gameButtons")){
			ConfigurationSection gameButtons = config.getConfigurationSection("gameBox.gameButtons");
			ConfigurationSection buttonSec;
			int bombsNum, tokens;
			double cost, reward;
			boolean saveStats, bigGrid;

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
				tokens = buttonSec.getInt("tokens", 0);
				bigGrid  = buttonSec.getBoolean("big", false);

				// support older node for token
				if(tokens == 0) tokens = buttonSec.getInt("token", 0);
				saveStats = buttonSec.getBoolean("saveStats", false);


				rules = new GameRules(buttonID, bombsNum, cost, reward, tokens, bigGrid, saveStats);

				setTheButton:
				if(buttonSec.isInt("slot")){
					int slot = buttonSec.getInt("slot");
					if(slot < 0 || slot >= gameGuiSlots){
						Bukkit.getLogger().log(Level.WARNING, "the slot of gameBox.gameButtons." + buttonID + " is out of the inventory range (0 - 53)");
						gameGui.setButton(button);
						break setTheButton;
					}
					gameGui.setButton(button, slot);
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
			meta.setDisplayName(chatColor(mainButtonSec.getString("displayName",lang.NAME)));
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


		// get top list buttons
		if(config.isConfigurationSection("gameBox.topListButtons")){
			ConfigurationSection topListButtons = config.getConfigurationSection("gameBox.topListButtons");
			ConfigurationSection buttonSec;

			ArrayList<String> lore;


			for(String buttonID : topListButtons.getKeys(false)){
				buttonSec = topListButtons.getConfigurationSection(buttonID);

				if(!gameTypes.keySet().contains(buttonID)){
					Bukkit.getLogger().log(Level.WARNING, " the top list button 'gameBox.topListButtons." + buttonID + "' does not have a corresponding game button");
					continue;
				}


				if(!gameTypes.get(buttonID).isSaveStats()){
					Bukkit.getLogger().log(Level.WARNING, " the top list buttons 'gameBox.topListButtons." + buttonID + "' corresponding game button has statistics turned off!");
					Bukkit.getLogger().log(Level.WARNING, " With these settings there is no toplist to display");
					continue;
				}

				if(!buttonSec.isString("materialData")){
					Bukkit.getLogger().log(Level.WARNING, " missing material data under: gameBox.topListButtons." + buttonID + "        can not load the button");
					continue;
				}

				ItemStack mat = getItemStack(buttonSec.getString("materialData"));
				if(mat == null){
					Bukkit.getLogger().log(Level.WARNING, " error loading: gameBox.topListButtons." + buttonID);
					Bukkit.getLogger().log(Level.WARNING, "     invalid material data");
					continue;
				}


				AButton button =  new AButton(mat.getData(), 1);
				ItemMeta meta = button.getItemMeta();

				if(buttonSec.isString("displayName")){
					meta.setDisplayName(chatColor(buttonSec.getString("displayName")));
				}


				if(buttonSec.isList("lore")){
					lore = new ArrayList<>(buttonSec.getStringList("lore"));
					for(int i = 0; i < lore.size();i++){
						lore.set(i, chatColor(lore.get(i)));
					}
					meta.setLore(lore);
				}

				button.setItemMeta(meta);
				button.setAction(ClickAction.SHOW_TOP_LIST);
				button.setArgs(gameID, buttonID + GUIManager.TOP_LIST_KEY_ADDON);



				setTheButton:
				if(buttonSec.isInt("slot")){
					int slot = buttonSec.getInt("slot");
					if(slot < 0 || slot >= gameGuiSlots){
						Bukkit.getLogger().log(Level.WARNING, "the slot of gameBox.topListButtons." + buttonID + " is out of the inventory range (0 - 53)");
						gameGui.setButton(button);
						break setTheButton;
					}
					gameGui.setButton(button, slot);
				} else {
					gameGui.setButton(button);
				}

				// get skull lore and pass on to the top list page
				if(buttonSec.isList("skullLore")){
					lore = new ArrayList<>(buttonSec.getStringList("skullLore"));
					for(int i = 0; i < lore.size();i++){
						lore.set(i, chatColor(lore.get(i)));
					}
				} else {
					lore = new ArrayList<>(Arrays.asList("", "No lore specified in the config!"));
				}

				TopListPage topListPage = new TopListPage(gameBox, guiManager, 54, gameID, buttonID + GUIManager.TOP_LIST_KEY_ADDON, buttonSec.isString("inventoryTitle")? ChatColor.translateAlternateColorCodes('&',buttonSec.getString("inventoryTitle")):"Title missing in config", SaveType.TIME_LOW, lore);

				guiManager.registerTopList(gameID, buttonID, topListPage);
			}
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
			disabled = true;
		} catch (FileNotFoundException e) { 
			e.printStackTrace();
			disabled = true;
		}
	} 
	
	public GameManager getManager() {
		return manager;
	}
	
	public void reload(){
		if(!con.exists()){
			this.saveResource("config.yml", false);
		}
		reloadConfig();
		
		this.lang = new Language(this);
		
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
		}
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

	public String getGameID(){
    	return this.gameID;
	}

	public GameBox getGameBox(){
		return  this.gameBox;
	}
}
