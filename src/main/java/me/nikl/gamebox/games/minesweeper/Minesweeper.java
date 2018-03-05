package me.nikl.gamebox.games.minesweeper;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.games.MinesweeperPlugin;

/**
 * @author Niklas Eicker
 */
public class Minesweeper extends me.nikl.gamebox.game.Game {
    protected Minesweeper(GameBox gameBox) {
        super(gameBox, MinesweeperPlugin.MINESWEEPER);
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void init() {

    }

    @Override
    public void loadSettings() {

    }

    @Override
    public void loadLanguage() {
        gameLang = new Language(this);
    }

    @Override
    public void loadGameManager() {
        gameManager = new GameManager(this);
    }
}
