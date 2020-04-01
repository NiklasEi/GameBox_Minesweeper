package me.nikl.gamebox.games;

import me.nikl.gamebox.games.minesweeper.Minesweeper;
import me.nikl.gamebox.module.GameBoxModule;

/**
 * @author Niklas Eicker
 */
public class MinesweeperPlugin extends GameBoxModule {
    public static final String MINESWEEPER = "minesweeper";

    @Override
    public void onEnable() {
        registerGame(MINESWEEPER, Minesweeper.class, "ms");
    }

    @Override
    public void onDisable() {

    }
}
