package me.nikl.minesweeper;

/**
 * Created by Niklas on 16.02.2017.
 */
public class GameRules {

    private int numberOfBombs, tokens;
    private double cost, reward;
    private boolean saveStats, bigGrid;
    private String key;

    public GameRules(String key, int bombsNum, double cost, double reward, int tokens, boolean bigGrid, boolean saveStats){
        this.numberOfBombs = bombsNum;
        this.cost = cost;
        this.reward = reward;
        this.saveStats = saveStats;
        this.key = key;
        this.tokens = tokens;
        this.bigGrid = bigGrid;
    }

    public int getNumberOfBombs() {
        return numberOfBombs;
    }

    public double getCost() {
        return cost;
    }

    public double getReward() {
        return reward;
    }

    public boolean isSaveStats() {
        return saveStats;
    }

    public String getKey() {
        return key;
    }

    public int getTokens() {
        return tokens;
    }

    public boolean isBigGrid() {
        return bigGrid;
    }
}
