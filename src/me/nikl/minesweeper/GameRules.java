package me.nikl.minesweeper;

/**
 * Created by Niklas on 16.02.2017.
 */
public class GameRules {

    private int numberOfBombs;
    private double cost, reward;

    public GameRules(int bombsNum, double cost, double reward){
        this.numberOfBombs = bombsNum;
        this.cost = cost;
        this.reward = reward;
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
}
