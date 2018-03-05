package me.nikl.gamebox.games.minesweeper;

import me.nikl.gamebox.game.GameLanguage;

public class Language extends GameLanguage {
	public String GAME_WON_MONEY;
	public String TITLE_BEGINNING, TITLE_INGAME, TITLE_END, TITLE_LOST;
	
	public Language(Minesweeper game){
		super(game);
	}

	@Override
	protected void loadMessages() {
		getGameMessages();
	}
	
	private void getGameMessages() {
		GAME_WON_MONEY = getString("game.econ.wonMoney");
		TITLE_BEGINNING = getString("game.inventoryTitles.beginning");
		TITLE_INGAME = getString("game.inventoryTitles.ingame");
		TITLE_END = getString("game.inventoryTitles.won");
		TITLE_LOST = getString("game.inventoryTitles.lost");
	}
}

