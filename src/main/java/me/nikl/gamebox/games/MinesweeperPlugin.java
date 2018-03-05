package me.nikl.gamebox.games;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Module;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Niklas Eicker
 */
public class MinesweeperPlugin extends JavaPlugin {
    private GameBox gameBox;
    public static final String MINESWEEPER = "minesweeper";

    @Override
    public void onEnable() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("GameBox");
        if(plugin == null || !plugin.isEnabled()){
            getLogger().warning(" GameBox was not found! Disabling LogicPuzzles...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        gameBox = (GameBox) plugin;
        new Module(gameBox, MINESWEEPER
                , "me.nikl.gamebox.games.minesweeper.Minesweeper"
                , this, MINESWEEPER, "ms");
    }
}
