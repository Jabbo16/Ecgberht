package ecgberht;

import java.util.List;

import bwapi.Pair;
import bwapi.Position;
import bwapi.TilePosition;

public class Util {
	public static Position sumPosition(Position... positions) {
		Position sum = new Position(0,0);
		for(Position p : positions) {
			sum = new Position(sum.getX() + p.getX(), sum.getY() + p.getY());
		}
		return sum;
	}

	public static TilePosition sumTilePosition(TilePosition... tilepositions) {
		TilePosition sum = new TilePosition(0,0);
		for(TilePosition p : tilepositions) {
			sum = new TilePosition(sum.getX() + p.getX(), sum.getY() + p.getY());
		}
		return sum;
	}

	public static Position sumPosition(List<Pair<Double, Double>> vectors) {
		Position sum = new Position(0,0);
		for(Pair<Double, Double> p : vectors) {
			sum = new Position((int) (sum.getX() + p.first), (int) (sum.getY() + p.second));
		}
		return sum;
	}
}
