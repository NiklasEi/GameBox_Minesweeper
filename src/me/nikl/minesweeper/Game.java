package me.nikl.minesweeper;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;

import me.nikl.minesweeper.nms.Update;

public class Game{
	private ItemStack empty, flagged, mine, covered, number;
	private Inventory inv;
	private int num, bombsNum, flags;
	private String[] positions;
	private Boolean[] cov; //Array with covered/not covered info
	private boolean changingInv;
	private String displayFlags, displayTime, currentState;
	private UUID player;
	private Language lang;
	private boolean started;
	private Update updater;
	
	private Main plugin;
	private GameTimer timer;
	
	public Game(Main plugin, UUID player){
		this.updater = plugin.getUpdater();
		this.setStarted(false);
		this.player = player;
		this.setChangingInv(false);
		this.num = 54;
		this.plugin = plugin;
		this.lang = plugin.lang;
		this.bombsNum = 0;
		this.displayTime = "00:00";
		if(plugin.getConfig() == null){
			Bukkit.getConsoleSender().sendMessage(plugin.chatColor(lang.PREFIX + " Failed to load config!"));
			Bukkit.getPluginManager().disablePlugin(plugin);
		}
		if(plugin.getConfig().isInt("mines")){
			this.bombsNum = plugin.getConfig().getInt("mines");
		}
		if(bombsNum < 1 || bombsNum > 30){
			Bukkit.getConsoleSender().sendMessage("[Minesweeper] Check the config, a not valid number of mines was set.");
			this.bombsNum = 8;
		}
		if(!getMaterials()){
			Bukkit.getConsoleSender().sendMessage(plugin.chatColor(lang.PREFIX+" &4Failed to load materials from config"));
			Bukkit.getConsoleSender().sendMessage(plugin.chatColor(lang.PREFIX+" &4Using default materials"));
			this.flagged = new ItemStack(Material.SIGN);
			ItemMeta metaFlagged = flagged.getItemMeta();
			metaFlagged.setDisplayName("Flag");
			flagged.setItemMeta(metaFlagged);
			flagged.setAmount(1);
			this.covered = new ItemStack(Material.STAINED_GLASS_PANE);
			covered.setDurability((short) 8);
			ItemMeta metaCovered = covered.getItemMeta();
			metaCovered.setDisplayName("Cover");
			covered.setItemMeta(metaCovered);
			covered.setAmount(1);
			this.mine = new ItemStack(Material.TNT);
			ItemMeta metaMine = mine.getItemMeta();
			metaMine.setDisplayName("Boooom");
			mine.setItemMeta(metaMine);
			this.number = new Wool(DyeColor.ORANGE).toItemStack();
			ItemMeta metaNumber = number.getItemMeta();
			metaNumber.setDisplayName("Warning");
			number.setItemMeta(metaNumber);
		}
		this.flags=0;
		this.positions = new String[num];
		this.cov = new Boolean[num];
		for(int i = 0 ;i<num;i++){
			positions[i] = "0";
			cov[i]=true;
		}
		this.inv = Bukkit.getServer().createInventory(null, num, ChatColor.translateAlternateColorCodes('&', lang.TITLE_BEGINNING));
		createGame();
	}
	
	private Boolean getMaterials() {
		Boolean worked = true;

	    Material mat = null;
	    int data = 0;
	    for(String key : Arrays.asList("cover", "warning", "mine", "flag")){
		    if(!plugin.getConfig().isSet("materials." + key)) return false;
	    	String value = plugin.getConfig().getString("materials." + key);
		    String[] obj = value.split(":");
	
		    if (obj.length == 2) {
		        try {
		            mat = Material.matchMaterial(obj[0]);
		        } catch (Exception e) {
		            worked = false; // material name doesn't exist
		        }
	
		        try {
		            data = Integer.valueOf(obj[1]);
		        } catch (NumberFormatException e) {
		        	worked = false; // data not a number
		        }
		    } else {
		        try {
		            mat = Material.matchMaterial(value);
		        } catch (Exception e) {
		            worked = false; // material name doesn't exist
		        }
		    }
		    if(mat == null) return false;
		    if(key.equals("cover")){
				this.covered = new ItemStack(mat, 1);
				if (obj.length == 2) covered.setDurability((short) data);
				ItemMeta metaCovered = covered.getItemMeta();
				metaCovered.setDisplayName("Cover");
				covered.setItemMeta(metaCovered);
				covered.setAmount(1);
		    	
		    } else if(key.equals("warning")){
				this.number = new ItemStack(mat, 1);
				if (obj.length == 2) number.setDurability((short) data);
				ItemMeta metaNumber = number.getItemMeta();
				metaNumber.setDisplayName("Warning");
				number.setItemMeta(metaNumber);
		    	
		    } else if(key.equals("mine")){
				this.mine = new ItemStack(mat, 1);
				if (obj.length == 2) mine.setDurability((short) data);
				ItemMeta metaMine = mine.getItemMeta();
				metaMine.setDisplayName("Boooom");
				mine.setItemMeta(metaMine);
		    	
		    } else if(key.equals("flag")){
				this.flagged = new ItemStack(mat, 1);
				if (obj.length == 2) flagged.setDurability((short) data);
				ItemMeta metaFlagged = flagged.getItemMeta();
				metaFlagged.setDisplayName("Flag");
				flagged.setItemMeta(metaFlagged);
				flagged.setAmount(1);		    	
		    }
	    }

		this.empty = new ItemStack(Material.AIR);
		return worked;
	}

	private void createGame(){		
		Random r = new Random();
		int rand = r.nextInt(num);
		int count = 0;
		while(count < bombsNum){
			if(positions[rand].equals("mine")){
				rand = r.nextInt(num);
				continue;
			}
			positions[rand] = "mine";
			count++;
			rand = r.nextInt(num);
		}
		for(int i=0;i<num;i++){
			if(positions[i].equals("mine")){
				continue;
			}
			positions[i] = getNextMines(i);
		}
		
		for(int i=0;i<num;i++){
			this.inv.setItem(i, covered);
		}
		
		
		
		
		
	}
	
	private String getNextMines(int i) {
		int count = 0;
		int[] add;
		if(i == 0){// corner left top
			add = new int[3];
			add[0] = 1; add[1] = 9; add[2] = 10;
		} else if (i == 8){// corner top right
			add = new int[3];
			add[0] = -1; add[1] = 8; add[2] = 9;
		} else if (i == 45){// corner bottom left
			add = new int[3];
			add[0] = -9; add[1] = -8; add[2] = 1;
		} else if (i == 53){// corner bottom right
			add = new int[3];
			add[0] = -10; add[1] = -9; add[2] = -1;
		} else if(i>0 && i<8){// edge top
			add = new int[5];
			add[0] = -1; add[1] = 1; add[2] = 8; add[3] = 9; add[4] = 10;
		} else if(i == 17 || i == 26 || i == 35 || i == 44){// edge right
			add = new int[5];
			add[0] = -10; add[1] = -9; add[2] = -1; add[3] = 8; add[4] = 9;
		} else if(i>45 && i<53){// edge bottom
			add = new int[5];
			add[0] = -1; add[1] = -10; add[2] = -9; add[3] = -8; add[4] = 1;
		} else if(i == 9 || i == 18 || i == 27 || i == 36){// edge left
			add = new int[5];
			add[0] = -9; add[1] = -8; add[2] = 1; add[3] = 9; add[4] = 10;
		} else {
			add = new int[8];
			add[0] = -10; add[1] = -9; add[2] = -8; add[3] = -1; add[4] = 1; add[5] = 8; add[6] = 9; add[7] = 10;
		}
		for (int a : add){
			if(!(i+a >= 0 && i+a < num)){
				Bukkit.getConsoleSender().sendMessage("[Minesweeper] That should not happen. Something went wrong while building a game.");
				continue;
			}
			if(positions[i+a].equals("mine")){
				count++;
			}
		}
		return String.valueOf(count);
	}

	public void reveal(){
		for(int i=0;i<num;i++){
			cov[i] = false;
			if(positions[i].equals("mine")){
				this.inv.setItem(i, mine);
			} else {
				int amount = 0;
				try {
					amount = Integer.parseInt(positions[i]);
				} catch (NumberFormatException e) {
					Bukkit.getLogger().severe("Something went wrong while building the game");
				}
				if(amount == 0){
					this.inv.setItem(i, empty);
					continue;
				}
				number.setAmount(amount);
				this.inv.setItem(i, number);
			}
		}
	}
	
	public void showGame(Player player){
		this.setChangingInv(true);
		player.openInventory(inv);
		this.setChangingInv(false);
	}
	
	public Inventory getInv(){
		return this.inv;
	}
	
	public Boolean isCovered(ItemStack itemS){
		return covered.getType().equals(itemS.getType()) && covered.getData().equals(itemS.getData());
	}
	
	public Boolean isFlaged(ItemStack itemS){
		return flagged.getType().equals(itemS.getType()) && flagged.getData().equals(itemS.getData());
	}
	
	public Boolean isEmpty(int slot){
		return inv.getItem(slot) == null;		
	}

	public void setFlagged(int slot) {
		this.inv.setItem(slot, flagged);
		flags++;
		this.displayFlags = "   &2"+flags+"&r/&4"+bombsNum;
		currentState = lang.TITLE_INGAME.replaceAll("%state%", displayFlags).replaceAll("%timer%", displayTime);
		setState(currentState);
	}

	public void deFlag(int slot) {
		this.inv.setItem(slot, covered);	
		flags--;
		this.displayFlags = "   &2"+flags+"&r/&4"+bombsNum;
		currentState = lang.TITLE_INGAME.replaceAll("%state%", displayFlags).replaceAll("%timer%", displayTime);
		setState(currentState);
	}
	
	public void uncover(int slot){
		if(positions[slot].equals("mine")){
			cancelTimer();
			reveal();
			setState(lang.TITLE_LOST);
		} else {
			int amount = 0;
			try {
				amount = Integer.parseInt(positions[slot]);
			} catch (NumberFormatException e) {
				Bukkit.getLogger().severe("Something went wrong while building the game");
			}
			if(amount == 0){
				this.inv.setItem(slot, empty);
			} else {
				number.setAmount(amount);
				this.inv.setItem(slot, number);
			}
			cov[slot] = false;
		}		
	}
	
	public boolean isWon() {
		int count = 0;
		for(int i=0;i<num;i++){
			if(cov[i]){
				count++;
			}
		}
		if(count == bombsNum){
			plugin.setStatistics(this.player, this.displayTime, bombsNum);
			return true;
		}
		return false;
	}

	public void setState(String state){
		Player playerP = Bukkit.getPlayer(player);
		if(playerP == null){
			plugin.getManager().removeGame(player);
		}
		updater.updateTitle(Bukkit.getPlayer(player), ChatColor.translateAlternateColorCodes('&',state));
		/*Inventory newInv = Bukkit.getServer().createInventory(null, num, ChatColor.translateAlternateColorCodes('&', state));
		newInv.setContents(this.inv.getContents());
		this.inv = newInv;*/
	}
	
	public String getState(){
		return inv.getName();
	}

	public void setState() {
		this.displayFlags = "   &2"+flags+"&r/&4"+bombsNum;	
		currentState = lang.TITLE_INGAME.replaceAll("%state%", displayFlags).replaceAll("%timer%", displayTime);
		setState(currentState);
	}

	public boolean isChangingInv() {
		return changingInv;
	}

	public void setChangingInv(boolean changingInv) {
		this.changingInv = changingInv;
	}

	public void setTime(String string) {
		this.displayTime = string;
		currentState = lang.TITLE_INGAME.replaceAll("%state%", displayFlags).replaceAll("%timer%", displayTime);
		setState(currentState);
		//showGame(Bukkit.getPlayer(player));
	}

	public void startTimer() {
		this.timer = new GameTimer(this);
	}

	public String getDisplayTime() {
		return this.displayTime;
	}

	public void cancelTimer() {
		if(this.timer != null){
			this.timer.cancel();
		}
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public void start() {
		setStarted(true);
		startTimer();
		setState();		
	}
}
