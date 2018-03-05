package me.nikl.gamebox.games.minesweeper;

import org.bukkit.scheduler.BukkitRunnable;

public class GameTimer extends BukkitRunnable{
	private Game game;
	private int time;
	
	GameTimer(Game game){
		this.game = game;
		this.time = 0;
		
		this.runTaskTimer(game.getMinesweeper().getGameBox(), 20, 20);
	}

	@Override
	public void run() {
		time++;

		String minutes = (time/60) + "";
		if(minutes.length()<2) minutes = "0" + minutes;
		String seconds = (time%60) + "";
		if(seconds.length()<2) seconds = "0" + seconds;
		
		game.setTime(minutes + ":" + seconds);		
	}

	public int getTime(){
		return this.time;
	}
}
