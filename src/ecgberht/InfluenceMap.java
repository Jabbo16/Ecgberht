package ecgberht;

import java.awt.Point;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import bwapi.Game;
import bwapi.Pair;
import bwapi.Player;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

public class InfluenceMap {

	public Game game;
	public Player self;
	public int alto;
	public int ancho;
	public double [][] mapa;
	public final int neutro = 3;
	public final int ofensivo = 4;
	public final int defensivo = 5;

	public final int biologica = 1;
	public final int maquina = 3;
	public final int volador = 6;

	public final int umbral = 3;
	public final int propagacion = 2;

	public InfluenceMap(Game game, Player self, int alto, int ancho) {
		this.game = game;
		this.self = self;
		this.alto = alto;
		this.ancho = ancho;
		mapa = new double[alto][ancho];
	}

	public void updateMap(Unit arg0,boolean Destroyed) {
		int influence = 0;
		UnitType type = arg0.getType();
		TilePosition tile = arg0.getTilePosition().makeValid();
		if(type.isBuilding()) {
			if(type.canAttack() || type.equals(UnitType.Terran_Bunker)) {
				influence = defensivo;
			} else {
				if(type.canProduce()) {
					influence = ofensivo;
				} else {
					influence = neutro;
				}
			}
		} else {
			if(type.isFlyer()) {
				influence = volador;
			} else if(type.isMechanical()) {
				influence = maquina;
			} else {
				influence = biologica;
			}
		}
		if(Destroyed) {
			influence *= -1;
		}
		if(arg0.getPlayer().getID() != self.getID()) {
			influence *= -1;
		}
		updateCellInfluence(new Pair<Point,Integer>(new Point(tile.getY(),tile.getX()),influence),type.isBuilding());
	}

	public void updateCellInfluence(Pair<Point,Integer> celda,boolean building) {
		mapa[celda.first.x][celda.first.y] += celda.second;
		if(!building) {
			int init_i = 0;
			if(celda.first.y-propagacion > init_i) {
				init_i = celda.first.y-propagacion;
			}
			int fin_i = ancho-1;
			if(celda.first.y+propagacion < fin_i) {
				fin_i = celda.first.y+propagacion;
			}
			int init_j = 0;
			if(celda.first.x-propagacion > init_j) {
				init_j = celda.first.x-propagacion;
			}
			int fin_j = alto-1;
			if(celda.first.x+propagacion < fin_j) {
				fin_j = celda.first.x+propagacion;
			}
			for(int ii = init_i; ii <= fin_i; ii++) {
				for(int jj = init_j; jj <= fin_j; jj++) {
					if(!(jj == celda.first.x && ii == celda.first.y))
						mapa[jj][ii] += Math.round(celda.second / Math.pow(1 + Math.sqrt(Math.pow((double)(ii-celda.first.y), 2) + Math.pow((double)(jj-celda.first.x), 2)), 2));
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
		return mapa[celda.x][celda.y];
	}

	public int getMyInfluenceLevel() {
		int my_influence = 0;
		for(int x = 0; x < alto; x++) {
			for(int y = 0; y < ancho; y++) {
				if(mapa[x][y] > 0) {
					my_influence += mapa[x][y];
				}
			}
		}
		return my_influence;
	}

	public int getEnemyInfluenceLevel() {
		int enemy_influence = 0;
		for(int x = 0; x < alto; x++) {
			for(int y = 0; y < ancho; y++) {
				if(mapa[x][y] < 0) {
					enemy_influence += mapa[x][y];
				}
			}
		}
		return enemy_influence;
	}

	public int getMyInfluenceArea() {
		int count = 0;
		for(int x = 0; x < alto; x++) {
			for(int y = 0; y < ancho; y++) {
				if(mapa[x][y] > 0) {
					count++;
				}
			}
		}
		return count;
	}

	public double getEnemyInfluenceArea() {
		int count = 0;
		for(int x = 0; x < alto; x++) {
			for(int y = 0; y < ancho; y++) {
				if(mapa[x][y] < 0) {
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
		Pair<Integer,Integer> p = new Pair<Integer,Integer>();
		for(int x = 0; x < alto; x++) {
			for(int y = 0; y < ancho; y++) {
				if(mapa[x][y] < count) {
					if(attack) {
						if(fixMapa(x,y)) {
							continue;
						}
					}
					count = mapa[x][y] / Math.pow(1 + Math.sqrt(Math.pow((double)(x-sY), 2) + Math.pow((double)(y-sX), 2)), 2);
					p.first = x;
					p.second = y;
				}
			}
		}
		return p;
	}

	public boolean fixMapa(int x, int y) {
		TilePosition pos = new TilePosition(y, x);
		if(game.isVisible(pos)) {
			for(Unit u:game.getUnitsOnTile(pos)) {
				if(u.getType().isBuilding()) {
					return false;
				}
			}
			updateCellInfluence(new Pair<Point,Integer>(new Point(x,y),(int) mapa[x][y] * (-1)),true);
			return true;
		}
		return false;
	}

	//Metodo que escribe el mapa en un fichero
	public void writeMapa(String file_name){
		FileWriter sw = null;
		try {
			sw = new FileWriter(file_name);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(int ii=0; ii<alto; ii++) {
			for(int jj=0; jj<ancho; jj++) {
				try {
					sw.write(Integer.toString((int)mapa[ii][jj]));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(ii!=alto-1) {
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
