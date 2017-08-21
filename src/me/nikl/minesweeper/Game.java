package me.nikl.minesweeper;

import io.netty.util.internal.ConcurrentSet;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.Sounds;
import me.nikl.gamebox.nms.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Random;
import java.util.Set;
import java.util.UUID;


public class Game{
	private ItemStack flagged, mine, covered, number;
	private Inventory inv;
	private int num, bombsNum, flags;
	private String[] positions;
	private Boolean[] cov, flagsGrid; //Array with covered/not covered info
	private String displayFlags, displayTime, currentState;
	private UUID player;
	private Language lang;

	// wait for the first click to start the timer
	private boolean started = false;
	private NMSUtil updater;
	
	private Main plugin;
	private GameTimer timer;

	private String rule;

	private boolean playSounds;
	private float volume = 0.5f, pitch= 1f;
	
	public Game(Main plugin, UUID player, int bombsNum, ItemStack[] items, boolean playSounds, GameRules rules){
		this.updater = plugin.getUpdater();
		this.player = player;
		this.num = 54 + (rules.isBigGrid()?27:0);

		this.playSounds = playSounds;
		this.plugin = plugin;
		this.rule = rules.getKey();
		this.lang = plugin.lang;
		this.bombsNum = bombsNum;
		this.displayTime = "00:00";
		this.covered = items[0];
		this.flagged = items[1];
		this.number = items[2];
		this.mine = items[3];
		if(plugin.getConfig() == null){
			Bukkit.getConsoleSender().sendMessage(GameBox.chatColor(lang.PREFIX + " &cFailed to load config!"));
			Bukkit.getPluginManager().disablePlugin(plugin);
		}

		this.flags=0;
		this.positions = new String[num];
		this.cov = new Boolean[num];
		this.flagsGrid = new Boolean[num];
		for(int i = 0 ;i<num;i++){
			positions[i] = "0";
			cov[i]=true;
			flagsGrid[i] = false;
		}
		String title = lang.TITLE_BEGINNING;
		if(GameBoxSettings.checkInventoryLength && title.length() > 32){
			title = "Title is too long!";
		}
		this.inv = Bukkit.getServer().createInventory(null, num, title);
		createGame();
		Player myPlayer = Bukkit.getPlayer(player);
		if(myPlayer == null) return;
		myPlayer.openInventory(inv);
	}

	/**
	 * Build the grid with mines at random positions.
	 * Then cover the inventory
	 */
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
		int[] add = getSurroundings(i);
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
					e.printStackTrace();
				}
				if(amount == 0){
					this.inv.setItem(i, null);
					continue;
				}
				number.setAmount(amount);
				this.inv.setItem(i, number);
			}
		}
	}

	public Boolean isCovered(int slot){
		return (this.cov[slot] && !this.flagsGrid[slot]);
	}
	
	public Boolean isFlagged(int slot){
		return this.flagsGrid[slot];
	}

	public void setFlagged(int slot) {
		this.inv.setItem(slot, flagged);
		flags++;
		this.flagsGrid[slot] = true;
		this.displayFlags = "   &2"+flags+"&r/&4"+bombsNum;
		currentState = lang.TITLE_INGAME.replaceAll("%state%", displayFlags).replaceAll("%timer%", displayTime);
		setState(currentState);
	}

	public void deFlag(int slot) {
		this.inv.setItem(slot, covered);	
		flags--;
		this.flagsGrid[slot] = false;
		this.displayFlags = "   &2"+flags+"&r/&4"+bombsNum;
		currentState = lang.TITLE_INGAME.replaceAll("%state%", displayFlags).replaceAll("%timer%", displayTime);
		setState(currentState);
	}
	
	public void uncover(int slot){
		if(positions[slot].equals("mine")){
			cancelTimer();
			reveal();
			Player realPlayer = Bukkit.getPlayer(player);
			if(playSounds)realPlayer.playSound(realPlayer.getLocation(), Sounds.EXPLODE.bukkitSound(), volume, pitch);
			setState(lang.TITLE_LOST);
		} else {
			int amount = 0;
			try {
				amount = Integer.parseInt(positions[slot]);
			} catch (NumberFormatException e) {
				Bukkit.getLogger().severe("Something went wrong while building the game");
			}
			if(amount == 0){
				this.inv.setItem(slot, null);
				if(plugin.automaticReveal) {
					uncoverEmpty(slot);
				}
			} else {
				number.setAmount(amount);
				this.inv.setItem(slot, number);
			}
			cov[slot] = false;
		}		
	}
	
	private void uncoverEmpty(int slot) {
		Set<Integer> uncover = new ConcurrentSet<>();
		Set<Integer> newUncover = new ConcurrentSet<>();
		
		uncover.add(slot);
		int currentSlot = slot;
		
		int[] add = getSurroundings(slot);
		for(int i = 0; i < add.length; i++){
			add[i] = add[i] + currentSlot;
			if(!uncover.contains(add[i])){
				newUncover.add(add[i]);
			}
		}
		while (!newUncover.isEmpty()){
			for(int checkSlot : newUncover){
				if (!uncover.contains(checkSlot)){
					uncover.add(checkSlot);
					newUncover.remove(checkSlot);
					if(positions[checkSlot].equalsIgnoreCase("0")){
						int[] newAdd = getSurroundings(checkSlot);
						for(int i = 0; i < newAdd.length; i++){
							newAdd[i] = newAdd[i] + checkSlot;
							if(!uncover.contains(newAdd[i]) && !newUncover.contains(newAdd[i])){
								newUncover.add(newAdd[i]);
							}
						}
					}
				}
			}
		}
		for(int uncoverSlot : uncover){
			int amount = 0;
			try {
				amount = Integer.parseInt(positions[uncoverSlot]);
			} catch (NumberFormatException e) {
				Bukkit.getLogger().severe("Something went wrong while building the game");
				e.printStackTrace();
			}
			if(amount == 0){
				this.inv.setItem(uncoverSlot, null);
			} else {
				number.setAmount(amount);
				this.inv.setItem(uncoverSlot, number);
			}
			cov[uncoverSlot] = false;
		}
	}
	
	public int[] getSurroundings(int slot){
		int[] add;
		if(slot == 0){// corner left top
			add = new int[3];
			add[0] = 1; add[1] = 9; add[2] = 10;
		} else if (slot == 8){// corner top right
			add = new int[3];
			add[0] = -1; add[1] = 8; add[2] = 9;
		} else if (slot == num - 9){// corner bottom left
			add = new int[3];
			add[0] = -9; add[1] = -8; add[2] = 1;
		} else if (slot == num - 1){// corner bottom right
			add = new int[3];
			add[0] = -10; add[1] = -9; add[2] = -1;
		} else if(slot>0 && slot<8){// edge top
			add = new int[5];
			add[0] = -1; add[1] = 1; add[2] = 8; add[3] = 9; add[4] = 10;
		} else if(slot % 9 == 8){// edge right
			add = new int[5];
			add[0] = -10; add[1] = -9; add[2] = -1; add[3] = 8; add[4] = 9;
		} else if(slot> num - 9 && slot< num - 1){// edge bottom
			add = new int[5];
			add[0] = -1; add[1] = -10; add[2] = -9; add[3] = -8; add[4] = 1;
		} else if(slot % 9 == 0){// edge left
			add = new int[5];
			add[0] = -9; add[1] = -8; add[2] = 1; add[3] = 9; add[4] = 10;
		} else {
			add = new int[8];
			add[0] = -10; add[1] = -9; add[2] = -8; add[3] = -1; add[4] = 1; add[5] = 8; add[6] = 9; add[7] = 10;
		}
		return add;
	}
	
	
	public boolean isWon() {
		int count = 0;
		for(int i=0;i<num;i++){
			if(cov[i]){
				count++;
			}
		}
		if(count == bombsNum){
			return true;
		}
		return false;
	}

	public void setState(String state){
		Player playerP = Bukkit.getPlayer(player);
		if(playerP == null){
			plugin.getManager().removeFromGame(player);
		}
		updater.updateInventoryTitle(Bukkit.getPlayer(player), GameBox.chatColor(state));
	}

	public void setState() {
		this.displayFlags = "   &2"+flags+"&r/&4"+bombsNum;	
		currentState = lang.TITLE_INGAME.replaceAll("%state%", displayFlags).replaceAll("%timer%", displayTime);
		setState(currentState);
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

	public String getRule() {
		return rule;
	}

	public boolean isPlaySounds() {
		return playSounds;
	}

	public int getTimeInSeconds(){
		if(timer == null) return -1;
		return timer.getTime();
	}
}
