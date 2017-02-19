package me.nikl.minesweeper;

/**
 * Created by Niklas on 16.02.2017.
 */
public class GameRules {

    private int numberOfBombs;
    private double cost, reward;
    private boolean saveStats;
    private String key;

    public GameRules(String key, int bombsNum, double cost, double reward, boolean saveStats){
        this.numberOfBombs = bombsNum;
        this.cost = cost;
        this.reward = reward;
        this.saveStats = saveStats;
        this.key = key;
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
}