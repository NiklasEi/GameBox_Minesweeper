package me.nikl.gamebox.games.minesweeper;

import me.nikl.gamebox.data.toplist.SaveType;
import me.nikl.gamebox.game.rules.GameRuleRewards;

/**
 * Created by Niklas on 16.02.2017.
 */
public class GameRules extends GameRuleRewards {
    private int numberOfBombs;
    private boolean automaticRevealing;
    private boolean firstClickEmptyField;

    public GameRules(String key, int bombsNum, double cost, double reward, int tokens, boolean saveStats, boolean automaticRevealing, boolean firstClickEmptyField){
        super(key, saveStats, SaveType.TIME_LOW, cost, reward, tokens);
        this.numberOfBombs = bombsNum;
        this.automaticRevealing = automaticRevealing;
        this.firstClickEmptyField = firstClickEmptyField;
    }

    public int getNumberOfBombs() {
        return numberOfBombs;
    }

    public boolean isAutomaticRevealing() {
        return automaticRevealing;
    }

    public boolean isFirstClickEmptyField() {
        return firstClickEmptyField;
    }
}
