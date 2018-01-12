package ecgberht;

public class EnemyInfo {
	String opponent = "";
	public int wins = 0;
	public int losses = 0;
	public boolean naughty = false;
	
	public EnemyInfo(String opponent) {
		this.opponent = opponent;
	}
	public EnemyInfo(String wins, String losses, String naughty) {
		this.wins = Integer.parseInt(wins);
		this.losses = Integer.parseInt(losses);
		if(naughty == "true") {
			this.naughty = true;
		}
	}
}
