package ecgberht;

import java.awt.Point;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.openbw.bwapi4j.BW;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.util.Pair;

public class InfluenceMap {

	public double [][] map;
	public final int bio = 1;
	public final int defensive = 5;
	public final int flying = 6;
	public final int mech = 3;
	public final int neutral = 3;
	public final int ofensive = 4;
	public final int propagation = 2;
	public final int umbral = 3;
	public BW bw;
	public int height;
	public int width;
	public Player self;

	public InfluenceMap(BW bw, Player self, int alto, int ancho) {
		this.bw = bw;
		this.self = self;
		this.height = alto;
		this.width = ancho;
		map = new double[alto][ancho];
	}

	public void updateMap(Unit arg0,boolean Destroyed) {
		int influence = 0;
		UnitType type = Util.getType((PlayerUnit)arg0);
		TilePosition tile = arg0.getTilePosition();
		if(type.isBuilding()) {
			if(type.canAttack() || type.equals(UnitType.Terran_Bunker)) {
				influence = defensive;
			} else {
				if(type.canProduce()) {
					influence = ofensive;
				} else {
					influence = neutral;
				}
			}
		} else {
			if(type.isFlyer()) {
				influence = flying;
			} else if(type.isMechanical()) {
				influence = mech;
			} else {
				influence = bio;
			}
		}
		if(Destroyed) {
			influence *= -1;
		}
		if(((PlayerUnit)arg0).getPlayer().isEnemy(self)) {
			influence *= -1;
		}
		updateCellInfluence(new Pair<Point,Integer>(new Point(tile.getY(),tile.getX()),influence),type.isBuilding());
	}

	public void updateCellInfluence(Pair<Point,Integer> celda,boolean building) {
		map[celda.first.x][celda.first.y] += celda.second;
		if(!building) {
			int init_i = 0;
			if(celda.first.y-propagation > init_i) {
				init_i = celda.first.y-propagation;
			}
			int fin_i = width-1;
			if(celda.first.y+propagation < fin_i) {
				fin_i = celda.first.y+propagation;
			}
			int init_j = 0;
			if(celda.first.x-propagation > init_j) {
				init_j = celda.first.x-propagation;
			}
			int fin_j = height-1;
			if(celda.first.x+propagation < fin_j) {
				fin_j = celda.first.x+propagation;
			}
			for(int ii = init_i; ii <= fin_i; ii++) {
				for(int jj = init_j; jj <= fin_j; jj++) {
					if(!(jj == celda.first.x && ii == celda.first.y))
						map[jj][ii] += Math.round(celda.second / Math.pow(1 + Math.sqrt(Math.pow(ii-celda.first.y, 2) + Math.pow(jj-celda.first.x, 2)), 2));
				}
			}
		}
	}

	public void updateCellsInfluence(List<Pair<Point,Integer> > celdas) {
		for(Pair<Point,Integer> p : celdas) {
			updateCellInfluence(p,true);
		}
	}

	public double getInfluence(Point celda) {
		return map[celda.x][celda.y];
	}

	public int getMyInfluenceLevel() {
		int myInfluence = 0;
		for(int x = 0; x < height; x++) {
			for(int y = 0; y < width; y++) {
				if(map[x][y] > 0) {
					myInfluence += map[x][y];
				}
			}
		}
		return myInfluence;
	}

	public int getEnemyInfluenceLevel() {
		int enemyInfluence = 0;
		for(int x = 0; x < height; x++) {
			for(int y = 0; y < width; y++) {
				if(map[x][y] < 0) {
					enemyInfluence += map[x][y];
				}
			}
		}
		return enemyInfluence;
	}

	public int getMyInfluenceArea() {
		int count = 0;
		for(int x = 0; x < height; x++) {
			for(int y = 0; y < width; y++) {
				if(map[x][y] > 0) {
					count++;
				}
			}
		}
		return count;
	}

	public double getEnemyInfluenceArea() {
		int count = 0;
		for(int x = 0; x < height; x++) {
			for(int y = 0; y < width; y++) {
				if(map[x][y] < 0) {
					count++;
				}
			}
		}
		return count;
	}

	public Pair<Integer,Integer> getPosition(TilePosition start, boolean attack) {
		double count = 0;
		int sX = start.getX();
		int sY = start.getY();
		Pair<Integer,Integer> p = new Pair<>(0, 0);
		for(int x = 0; x < height; x++) {
			for(int y = 0; y < width; y++) {
				if(map[x][y] < count) {
					if(attack) {
						if(fixMapa(x,y)) {
							continue;
						}
					}
					count = map[x][y] / Math.pow(1 + Math.sqrt(Math.pow(x-sY, 2) + Math.pow(y-sX, 2)), 2);
					p.first = x;
					p.second = y;
				}
			}
		}
		return p;
	}

	public boolean fixMapa(int x, int y) {
		TilePosition pos = new TilePosition(y, x);
		if(bw.getBWMap().isVisible(pos)) {
			for(Unit u : bw.getAllUnits()) {
				if(u.getTilePosition().equals(pos)) {
					if(u instanceof Building) {
						return false;
					}
				}
			}
			updateCellInfluence(new Pair<Point,Integer>(new Point(x,y),(int) map[x][y] * (-1)),true);
			return true;
		}
		return false;
	}

	// Writes Influence Map to a file
	public void writeMapa(String fileName){
		FileWriter sw = null;
		try {
			sw = new FileWriter(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(int ii=0; ii<height; ii++) {
			for(int jj=0; jj<width; jj++) {
				try {
					sw.write(Integer.toString((int)map[ii][jj]));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(ii!=height-1) {
				try {
					sw.write("\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			sw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
