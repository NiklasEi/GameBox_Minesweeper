package me.nikl.gamebox.games.minesweeper;

import me.nikl.gamebox.data.database.DataBase;
import me.nikl.gamebox.data.toplist.SaveType;
import me.nikl.gamebox.game.exceptions.GameStartException;
import me.nikl.gamebox.game.manager.EasyManager;
import me.nikl.gamebox.game.rules.GameRule;
import me.nikl.gamebox.games.MinesweeperPlugin;
import me.nikl.gamebox.utility.Permission;
import me.nikl.gamebox.utility.Sound;
import me.nikl.gamebox.utility.StringUtility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class GameManager extends EasyManager{
	private Minesweeper plugin;
	private Map<UUID, Game> games;
	private Language lang;
	private ItemStack covered, flagged, mine, number;
	private ItemStack[] items;

	private DataBase statistics;

	private Map<String,GameRules> gameTypes = new HashMap<>();

	private float volume = 0.5f, pitch = 1f;

	public GameManager(Minesweeper plugin){
		this.games = new HashMap<>();
		this.plugin = plugin;
		this.statistics = plugin.getGameBox().getDataBase();
		this.lang = (Language) plugin.getGameLang();

		if(!getMaterials()){
			plugin.warn(" Failed to load materials from config");
			plugin.warn(" Using default materials");
			this.flagged = new ItemStack(Material.SIGN);
			ItemMeta metaFlagged = flagged.getItemMeta();
			metaFlagged.setDisplayName(ChatColor.translateAlternateColorCodes('&',"&aFlag"));
			flagged.setItemMeta(metaFlagged);
			flagged.setAmount(1);
			this.covered = new ItemStack(Material.STAINED_GLASS_PANE);
			covered.setDurability((short) 8);
			ItemMeta metaCovered = covered.getItemMeta();
			metaCovered.setDisplayName(ChatColor.translateAlternateColorCodes('&',"&1Cover"));
			covered.setItemMeta(metaCovered);
			covered.setAmount(1);
			this.mine = new ItemStack(Material.TNT);
			ItemMeta metaMine = mine.getItemMeta();
			metaMine.setDisplayName(ChatColor.translateAlternateColorCodes('&',"&4Mine"));
			mine.setItemMeta(metaMine);
			this.number = new Wool(DyeColor.ORANGE).toItemStack();
			ItemMeta metaNumber = number.getItemMeta();
			metaNumber.setDisplayName(ChatColor.translateAlternateColorCodes('&',"&6Warning"));
			number.setItemMeta(metaNumber);
		}
		this.items = new ItemStack[]{covered,flagged,number,mine};
	}


	private Boolean getMaterials() {
		Boolean worked = true;
		Material mat = null;
		int data = 0;
		for(String key : Arrays.asList("cover", "warning", "mine", "flag")){
			if(!plugin.getConfig().isSet("materials." + key)) return false;
			String value = plugin.getConfig().getString("materials." + key);
			String[] obj = value.split(":");
			String name = "default";
			boolean named = false;
			if(plugin.getConfig().isSet("displaynames." + key) && plugin.getConfig().isString("displaynames." + key)){
				name = plugin.getConfig().getString("displaynames." + key);
				named = true;
			}
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
				if(named)
					metaCovered.setDisplayName(StringUtility.color(name));
				covered.setItemMeta(metaCovered);
				covered.setAmount(1);

			} else if(key.equals("warning")){
				this.number = new ItemStack(mat, 1);
				if (obj.length == 2) number.setDurability((short) data);
				ItemMeta metaNumber = number.getItemMeta();
				metaNumber.setDisplayName("Warning");
				if(named)
					metaNumber.setDisplayName(StringUtility.color(name));
				number.setItemMeta(metaNumber);

			} else if(key.equals("mine")){
				this.mine = new ItemStack(mat, 1);
				if (obj.length == 2) mine.setDurability((short) data);
				ItemMeta metaMine = mine.getItemMeta();
				metaMine.setDisplayName("&4Mine");
				if(named)
					metaMine.setDisplayName(StringUtility.color(name));
				mine.setItemMeta(metaMine);

			} else if(key.equals("flag")){
				this.flagged = new ItemStack(mat, 1);
				if (obj.length == 2) flagged.setDurability((short) data);
				ItemMeta metaFlagged = flagged.getItemMeta();
				metaFlagged.setDisplayName("Flag");
				if(named)
					metaFlagged.setDisplayName(StringUtility.color(name));
				flagged.setItemMeta(metaFlagged);
				flagged.setAmount(1);
			}
		}

		return worked;
	}


	@Override
	public void onInventoryClick(InventoryClickEvent event) {
		if(!isInGame(event.getWhoClicked().getUniqueId()) || event.getInventory() == null){
			return;
		}
		int slot = event.getSlot();
		Game game = games.get(event.getWhoClicked().getUniqueId());
		if(!game.isStarted()){
			game.start();
		}
		Player player = (Player) event.getWhoClicked();
		if(game.isCovered(slot)){
			if(event.getAction().equals(InventoryAction.PICKUP_HALF)){
				game.setFlagged(slot);
				if(game.isPlaySounds())player.playSound(player.getLocation(), Sound.CLICK.bukkitSound(), volume, pitch);
			} else if (event.getAction().equals(InventoryAction.PICKUP_ALL)){
				game.uncover(slot);
				if(game.isWon()){
					int finalTime = game.getTimeInSeconds();
					game.cancelTimer();
					game.reveal();
					game.setState(lang.TITLE_END.replaceAll("%timer%", game.getDisplayTime()+""));
					GameRules gameType = game.getRule();
					if(game.isPlaySounds())player.playSound(player.getLocation(), Sound.LEVEL_UP.bukkitSound(), volume, pitch);
					if(plugin.getSettings().isEconEnabled() && !Permission.BYPASS_GAME.hasPermission(event.getWhoClicked(), MinesweeperPlugin.MINESWEEPER) && game.getRule().getMoneyToWin() > 0.0){
						player.sendMessage(lang.PREFIX + lang.GAME_WON_MONEY.replaceAll("%reward%", gameType.getMoneyToWin()+""));
					}
					plugin.onGameWon(player, gameType, (double)finalTime);
				} else {
					if(game.isPlaySounds())player.playSound(player.getLocation(), Sound.CLICK.bukkitSound(), volume, pitch);
				}
			}
		} else if(game.isFlagged(slot) && event.getAction().equals(InventoryAction.PICKUP_HALF)){
			game.deFlag(slot);
			if(game.isPlaySounds())player.playSound(player.getLocation(), Sound.CLICK.bukkitSound(), volume, pitch);
		}
	}

	@Override
	public void onInventoryClose(InventoryCloseEvent inventoryCloseEvent) {
		if(!isInGame(inventoryCloseEvent.getPlayer().getUniqueId()))
			return;
		games.get(inventoryCloseEvent.getPlayer().getUniqueId()).cancelTimer();
		games.remove(inventoryCloseEvent.getPlayer().getUniqueId());
	}

	@Override
	public boolean isInGame(UUID uuid) {
		return games.keySet().contains(uuid);
	}

	@Override
	public void startGame(Player[] players, boolean playSounds, String... strings) throws GameStartException {
		// first and only argument atm is the number of bombs
		if(strings.length != 1){
			Bukkit.getLogger().log(Level.WARNING, " unknown number of arguments to start a game: " + Arrays.asList(strings));
			throw new GameStartException(GameStartException.Reason.ERROR);
		}
		GameRules rule = gameTypes.get(strings[0]);
		if(rule == null){
			Bukkit.getLogger().log(Level.WARNING, " unknown argument to start a game: " + Arrays.asList(strings));
			throw new GameStartException(GameStartException.Reason.ERROR);
		}
		if(!pay(players, rule.getCost())){
			throw new GameStartException(GameStartException.Reason.NOT_ENOUGH_MONEY);
		}
		games.put(players[0].getUniqueId(), new Game(plugin, players[0].getUniqueId(), rule.getNumberOfBombs(), items, (plugin.getSettings().isPlaySounds() && playSounds), rule));
	}

	@Override
	public void removeFromGame(UUID uuid) {
		games.get(uuid).cancelTimer();
		games.remove(uuid);
	}

	@Override
	public void loadGameRules(ConfigurationSection buttonSec, String buttonID) {
		int bombsNum = buttonSec.getInt("mines", 8);
		double cost = buttonSec.getDouble("cost", 0.);
		double reward = buttonSec.getDouble("reward", 0.);
		int tokens = buttonSec.getInt("tokens", 0);
		boolean bigGrid  = buttonSec.getBoolean("big", false);
		boolean saveStats = buttonSec.getBoolean("saveStats", false);
		boolean automaticRevealing = buttonSec.getBoolean("automaticRevealing", true);
		gameTypes.put(buttonID, new GameRules(buttonID, bombsNum, cost, reward, tokens, bigGrid, saveStats, automaticRevealing));
	}

	@Override
	public Map<String, ? extends GameRule> getGameRules() {
		return gameTypes;
	}

	private boolean pay(Player[] player, double cost) {
		return plugin.payIfNecessary(player[0], cost);
	}
}
