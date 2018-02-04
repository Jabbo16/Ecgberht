package ecgberht;

import java.util.ArrayList;
import java.util.List;

public class EnemyInfo {
	String opponent = "";
	public int wins = 0;
	public int losses = 0;
	public boolean naughty = false;
	public List<StrategyOpponentHistory> history = new ArrayList<StrategyOpponentHistory>(); 
	public EnemyInfo(String opponent) {
		this.opponent = opponent;
	}
	public EnemyInfo(String wins, String losses, List<StrategyOpponentHistory> history, String naughty) {
		this.wins = Integer.parseInt(wins);
		this.history = history;
		this.losses = Integer.parseInt(losses);
		if(naughty == "true") {
			this.naughty = true;
		}
	}
	
	public void updateStrategyOpponentHistory(String strategyName, int mapSize, boolean win) {
		if(history.isEmpty()) {
			StrategyOpponentHistory newData = new StrategyOpponentHistory(strategyName, mapSize , win);
			history.add(newData);
			return;
		}
		for(StrategyOpponentHistory data : history) {
			if(data.mapSize == mapSize && data.strategyName.equals(strategyName)) {
				if(win) {
					data.wins++;
				}
				else {
					data.losses++;
				}
				return;
			}
		}
		StrategyOpponentHistory newData = new StrategyOpponentHistory(strategyName, mapSize, win);
		history.add(newData);
	}
}
