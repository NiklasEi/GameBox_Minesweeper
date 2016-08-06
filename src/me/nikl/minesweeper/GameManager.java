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
		this.games = new HashMap<UUID, Game>();
		this.plugin = plugin;
		this.lang = plugin.lang;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onInvClick(InventoryClickEvent e){
		if(games.get(e.getWhoClicked().getUniqueId()) == null || e.getClickedInventory() == null){
			return;
		}
		if(!e.getClickedInventory().equals(games.get(e.getWhoClicked().getUniqueId()).getInv())){
			if(e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)){
				e.setCancelled(true);
			}
			return;
		}
		Game game = games.get(e.getWhoClicked().getUniqueId());
		int slot = e.getSlot();
		if(!game.isStarted()){
			game.start();
		}
		if (game.isEmpty(slot)){
			e.setCancelled(true);
			return;
		}
		if(game.isCovered(game.getInv().getItem(slot))){
			if(e.getAction().equals(InventoryAction.PICKUP_HALF)){
				game.setFlagged(slot);
			} else if (e.getAction().equals(InventoryAction.PICKUP_ALL)){
				game.uncover(slot);
				if(game.isWon()){
					game.cancelTimer();
					game.reveal();
					game.setState(lang.TITLE_END.replaceAll("%timer%", game.getDisplayTime()+""));
					if(plugin.econEnabled && !e.getWhoClicked().hasPermission("minesweeper.bypass")){
						Player player = (Player) e.getWhoClicked();
						Main.econ.depositPlayer(player, plugin.getConfig().getDouble("economy.reward"));
						player.sendMessage(plugin.chatColor(Main.prefix + lang.GAME_WON_MONEY.replaceAll("%reward%", plugin.getReward()+"")));
						
					}
					if(plugin.wonCommandsEnabled && !e.getWhoClicked().hasPermission("minesweeper.bypass")){
						Player player = (Player) e.getWhoClicked();
						for(String cmd : plugin.wonCommands){
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
						}
					}
				}
			}
		} else if(game.isFlaged(game.getInv().getItem(slot)) && e.getAction().equals(InventoryAction.PICKUP_HALF)){
			game.deFlag(slot);			
		}
		e.setCancelled(true);
		game.showGame((Player) e.getWhoClicked());
	}
	

	@EventHandler
	public void onInvClose(InventoryCloseEvent e){
		if(games.get(e.getPlayer().getUniqueId()) == null || !e.getInventory().equals(games.get(e.getPlayer().getUniqueId()).getInv()))
			return;
		if(games.get(e.getPlayer().getUniqueId()).isChangingInv()) return;
		games.get(e.getPlayer().getUniqueId()).cancelTimer();
		games.remove(e.getPlayer().getUniqueId());
	}
	

	public void startGame(Player player){
		games.put(player.getUniqueId(), new Game(plugin, player.getUniqueId()));
		games.get(player.getUniqueId()).showGame(player);
	}
}
