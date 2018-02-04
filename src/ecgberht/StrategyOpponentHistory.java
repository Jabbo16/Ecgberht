package ecgberht;
//import java.util.Calendar;
//import java.text.SimpleDateFormat;

public class StrategyOpponentHistory {
	
	//String date = getDate();
	String strategyName = "";	
	int wins = 0;
	int losses = 0;
	int mapSize = 0;
	public StrategyOpponentHistory(String strategyName, int mapSize, boolean win) {
		this.strategyName = strategyName;
		this.mapSize = mapSize;
		if(win) {
			this.wins++;
		}
		else {
			this.losses++;
		}
	}
//	public String getDate() {
//		String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
//		Calendar cal = Calendar.getInstance();
//		return new SimpleDateFormat(DATE_FORMAT_NOW).format(cal.getTime());
//	}
}



