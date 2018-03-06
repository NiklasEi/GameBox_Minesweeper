package me.nikl.gamebox.games.minesweeper;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.game.GameSettings;
import me.nikl.gamebox.games.MinesweeperPlugin;

/**
 * @author Niklas Eicker
 */
public class Minesweeper extends me.nikl.gamebox.game.Game {
    public Minesweeper(GameBox gameBox) {
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
        gameSettings.setGameType(GameSettings.GameType.SINGLE_PLAYER);
        gameSettings.setHandleClicksOnHotbar(false);
        gameSettings.setGameGuiSize(54);
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
