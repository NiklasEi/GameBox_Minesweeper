package me.nikl.minesweeper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class GameManager implements Listener{

	private Main plugin;
	private Map<UUID, Game> games;
	private Language lang;
	private int easyBombs, normalBombs, hardBombs;
	
	public GameManager(Main plugin){
		this.games = new HashMap<>();
		this.plugin = plugin;
		this.lang = plugin.lang;

		this.easyBombs = plugin.getConfig().getInt("mines.easy", 5);
		this.normalBombs = plugin.getConfig().getInt("mines.normal", 8);
		this.hardBombs = plugin.getConfig().getInt("mines.hard", 11);

		if(easyBombs < 1 || normalBombs < 1  || hardBombs < 1 ){
			Bukkit.getLogger().log(Level.WARNING, " Check the config, a too low number of mines was set.");
			if(easyBombs<1){
				this.easyBombs = 5;
			}
			if(normalBombs<1){
				this.normalBombs = 8;
			}
			if(hardBombs<1){
				this.hardBombs = 11;
			}
		}
		if(easyBombs > 30 || normalBombs > 30  || hardBombs > 30  ){
			Bukkit.getLogger().log(Level.WARNING, " Check the config, a too high number of mines was set.");
			if(easyBombs> 30 ){
				this.easyBombs = 5;
			}
			if(normalBombs> 30 ){
				this.normalBombs = 8;
			}
			if(hardBombs> 30 ){
				this.hardBombs = 11;
			}
		}

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onInvClick(InventoryClickEvent e){
		if(games.get(e.getWhoClicked().getUniqueId()) == null || e.getInventory() == null){
			return;
		}
		int slot = e.getSlot();
		e.setCancelled(true);
		if(slot != e.getRawSlot()) return;
		if(slot >= e.getInventory().getSize() || slot < 0) return;
		Game game = games.get(e.getWhoClicked().getUniqueId());
		if(!game.isStarted()){
			game.start();
		}
		if (game.isEmpty(slot)){
			return;
		}
		Player player = (Player) e.getWhoClicked();
		if(game.isCovered(slot)){
			if(e.getAction().equals(InventoryAction.PICKUP_HALF)){
				game.setFlagged(slot);
				if(Main.playSounds)player.playSound(player.getLocation(), Sounds.CLICK.bukkitSound(), 10f, 1f);
			} else if (e.getAction().equals(InventoryAction.PICKUP_ALL)){
				game.uncover(slot);
				if(game.isWon()){
					game.cancelTimer();
					game.reveal();
					game.setState(lang.TITLE_END.replaceAll("%timer%", game.getDisplayTime()+""));
					if(Main.playSounds)player.playSound(player.getLocation(), Sounds.LEVEL_UP.bukkitSound(), 10f, 1f);
					if(plugin.econEnabled && !e.getWhoClicked().hasPermission("minesweeper.bypass")){
						Main.econ.depositPlayer(player, plugin.getConfig().getDouble("economy.reward"));
						player.sendMessage(plugin.chatColor(lang.PREFIX + lang.GAME_WON_MONEY.replaceAll("%reward%", plugin.getReward()+"")));
						
					}
					if(plugin.wonCommandsEnabled && !e.getWhoClicked().hasPermission("minesweeper.bypass")){
						if(plugin.wonCommands != null && !plugin.wonCommands.isEmpty()) {
							for (String cmd : plugin.wonCommands) {
								Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
							}
						}
					}
				} else {
					if(Main.playSounds)player.playSound(player.getLocation(), Sounds.CLICK.bukkitSound(), 10f, 1f);
				}
			}
		} else if(game.isFlagged(slot) && e.getAction().equals(InventoryAction.PICKUP_HALF)){
			game.deFlag(slot);
			if(Main.playSounds)player.playSound(player.getLocation(), Sounds.CLICK.bukkitSound(), 10f, 1f);
		}
	}
	

	@EventHandler
	public void onInvClose(InventoryCloseEvent e){
		if(games.get(e.getPlayer().getUniqueId()) == null || !e.getInventory().equals(games.get(e.getPlayer().getUniqueId()).getInv()))
			return;
		if(games.get(e.getPlayer().getUniqueId()).isChangingInv()) return;
		games.get(e.getPlayer().getUniqueId()).cancelTimer();
		games.remove(e.getPlayer().getUniqueId());
	}
	
	public void removeGame(UUID player){
		games.remove(player);
	}
	

	public void startGame(Player player){
		games.put(player.getUniqueId(), new Game(plugin, player.getUniqueId(), normalBombs));
		games.get(player.getUniqueId()).showGame(player);
	}

	public void startGame(Player player, String mode){
		switch (mode){
			case "easy":
				games.put(player.getUniqueId(), new Game(plugin, player.getUniqueId(), easyBombs));
				games.get(player.getUniqueId()).showGame(player);
				break;
			case "hard":
				games.put(player.getUniqueId(), new Game(plugin, player.getUniqueId(), hardBombs));
				games.get(player.getUniqueId()).showGame(player);
				break;
			default:
			case "normal":
				games.put(player.getUniqueId(), new Game(plugin, player.getUniqueId(), normalBombs));
				games.get(player.getUniqueId()).showGame(player);
				break;
		}
	}

	public void startGame(Player player, int bombsNum){
		games.put(player.getUniqueId(), new Game(plugin, player.getUniqueId(), bombsNum));
		games.get(player.getUniqueId()).showGame(player);
	}
}
