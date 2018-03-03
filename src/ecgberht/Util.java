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

	public static Pair<Double,Double> sumPosition(List<Pair<Double, Double>> vectors) {
		Pair<Double,Double> sum = new Pair<>(0.0,0.0);
		for(Pair<Double, Double> p : vectors) {
			sum.first += p.first;
			sum.second += p.second;
		}
		return sum;
	}
}
