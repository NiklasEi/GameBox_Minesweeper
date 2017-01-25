package me.nikl.minesweeper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
	
	public GameManager(Main plugin){
		this.games = new HashMap<>();
		this.plugin = plugin;
		this.lang = plugin.lang;
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
		games.put(player.getUniqueId(), new Game(plugin, player.getUniqueId()));
		games.get(player.getUniqueId()).showGame(player);
	}
}
