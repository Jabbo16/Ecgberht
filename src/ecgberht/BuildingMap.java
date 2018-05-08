package ecgberht;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ecgberht.Ecgberht.getGs;
import bwapi.Game;
import bwapi.Pair;
import bwapi.Player;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;

public class BuildingMap {

	private Game game;
	private int height;
	private int width;
	private Player self;
	private String map[][];

	private enum Side{
		TOP, LEFT, RIGHT, BOTTOM
	}
	public BuildingMap(Game game, Player self) {
		this.game = game;
		this.self = self;
		this.height = game.mapHeight();
		this.width = game.mapWidth();
		this.map = new String[this.height][this.width];
	}

	public BuildingMap(Game game, Player self, int height, int width, String[][] map) {
		this.game = game;
		this.self = self;
		this.height = height;
		this.width = width;
		this.map = map.clone();
	}

	public String[][] getMap(){
		return this.map;
	}

	@Override
	public BuildingMap clone() {
		BuildingMap map2 = new BuildingMap(game,self,height,width,map);
		return map2;
	}

	//Generates an initial building map
	public void initMap() {
		//Find valid and no valid positions for building
		for(int jj=0; jj<height; jj++) {
			for(int ii=0; ii<width; ii++) {
				TilePosition x = new TilePosition(ii, jj);
				if(game.isBuildable(x)) {
					map[jj][ii] = "6";
				} else {
					map[jj][ii] = "0";
				}
			}
		}
		//Finds minerals and geysers
		for(Unit resource : game.getStaticNeutralUnits()) {
			TilePosition resourceTile = resource.getTilePosition();
			if(resource.getType().isBuilding()) {
				if(resource.getType().isMineralField()) {
					TilePosition resourceSize = resource.getType().tileSize();
					for(int i = resourceTile.getY(); i < resourceTile.getY() + resourceSize.getY(); i++) {
						for(int j = resourceTile.getX(); j < resourceTile.getX() + resourceSize.getX(); j++) {
							if(i < 0 || i >= height || j < 0 || j >= width) {
								continue;
							}
							if(map[i][j] != "V") {
								map[i][j] = "M";
							}
						}
					}
				} else if(resource.getType() == UnitType.Resource_Vespene_Geyser) {
					TilePosition resourceSize = resource.getType().tileSize();
					for(int i = resourceTile.getY(); i < resourceTile.getY() + resourceSize.getY(); i++) {
						for(int j = resourceTile.getX(); j < resourceTile.getX() + resourceSize.getX(); j++) {
							if(i < 0 || i >= height || j < 0 || j >= width) {
								continue;
							}
							map[i][j] = "V";
						}
					}
				} else {
					TilePosition resourceSize = resource.getType().tileSize();
					for(int i = resourceTile.getY(); i < resourceTile.getY() + resourceSize.getY(); i++) {
						for(int j = resourceTile.getX(); j < resourceTile.getX() + resourceSize.getX(); j++) {
							if(i < 0 || i >= height || j < 0 || j >= width) {
								continue;
							}
							map[i][j] = "E";
						}
					}
				}
			}
		}
		for(BaseLocation b : BWTA.getBaseLocations()) {
			TilePosition starting = b.getTilePosition();
			for(int i = starting.getY(); i < starting.getY() + UnitType.Terran_Command_Center.tileHeight(); i++) {
				for(int j = starting.getX(); j < starting.getX() + UnitType.Terran_Command_Center.tileWidth(); j++) {
					if(i < 0 || i >= height || j < 0 || j >= width) {
						continue;
					}
					map[i][j] = "E";
				}
			}
			map[starting.getY() + 1][starting.getX() + UnitType.Terran_Command_Center.tileWidth()] = "E";
			map[starting.getY() + 2][starting.getX() + UnitType.Terran_Command_Center.tileWidth()] = "E";
			map[starting.getY() + 1][starting.getX() + UnitType.Terran_Command_Center.tileWidth() + 1] = "E";
			map[starting.getY() + 2][starting.getX() + UnitType.Terran_Command_Center.tileWidth() + 1] = "E";
		}
		map = fillMap(map);
	}

	//Fills the map with the correct values for each tile
	public String[][] fillMap(String[][] map){
		int height = map.length;
		int width = map[0].length;
		for(int jj = height-1; jj>=0; jj--) {
			if(map[jj][width-1] != "M" && map[jj][width-1] != "V" && map[jj][width-1] != "0" && map[jj][width-1] != "E" && map[jj][width-1] != "B") {
				if(jj == this.height-1 || width-1 == this.width-1) {
					map[jj][width-1] = "1";
				}
			}
		}
		for(int ii=width-1; ii>=0; ii--) {
			if(map[height-1][ii] != "M" && map[height-1][ii]  != "V" && map[height-1][ii]  != "0" && map[height-1][ii]  != "E" && map[height-1][ii]  != "B") {
				if(height-1 == this.height-1 || ii == this.width-1) {
					map[height-1][ii] = "1";
				}
			}
		}
		//Se ponen a "B" las casillas adyacentes a 0,M,V por izquierda y arriba y sean 6
		for(int jj=height-1; jj>=0; jj--) {
			for(int ii=width-1; ii>=0; ii--) {
				if(map[jj][ii] == "E" || map[jj][ii] == "M" || map[jj][ii] == "V") {
					if(jj-1>=0) {
						if(map[jj-1][ii] == "6") {
							map[jj-1][ii] = "B";
						}
					}
					if(ii-1>=0) {
						if(map[jj][ii-1] == "6") {
							map[jj][ii-1] = "B";
						}
					}
					if(jj-1>=0 && ii-1>=0) {
						if(map[jj-1][ii-1] == "6") {
							map[jj-1][ii-1] = "B";
						}
					}
					if(jj+1<height) {
						if(map[jj+1][ii] == "6") {
							map[jj+1][ii] = "B";
						}
					}
					if(ii+1<width) {
						if(map[jj][ii+1] == "6") {
							map[jj][ii+1] = "B";
						}
					}
					if(jj+1<height && ii+1<width) {
						if(map[jj+1][ii+1] == "6") {
							map[jj+1][ii+1] = "B";
						}
					}
					if(jj-1>=0 && ii+1<width) {
						if(map[jj-1][ii+1] == "6") {
							map[jj-1][ii+1] = "B";
						}
					}
					if(jj+1<height && ii-1>=0) {
						if(map[jj+1][ii-1] == "6") {
							map[jj+1][ii-1] = "B";
						}
					}
				}
			}
		}
		for(int jj=height-1; jj>0; jj--) {
			for(int ii=width-1; ii>0; ii--) {
				if(map[jj][ii] == "B" || map[jj][ii] == "0" || map[jj][ii] == "M" || map[jj][ii] == "V" || map[jj][ii] == "E") {
					if(map[jj-1][ii] == "6") {
						map[jj-1][ii] = "1";
					}
					if(map[jj][ii-1] == "6") {
						map[jj][ii-1] = "1";
					}
					if(map[jj-1][ii-1] == "6") {
						map[jj-1][ii-1] = "1";
					}
				}
			}
		}
		for(int jj=height-1; jj>0; jj--) {
			for(int ii=width-1; ii>0; ii--) {
				if(map[jj][ii] == "1") {
					if(map[jj-1][ii] == "6") {
						map[jj-1][ii] = "2";
					}
					if(map[jj][ii-1] == "6") {
						map[jj][ii-1] = "2";
					}
					if(map[jj-1][ii-1] == "6") {
						map[jj-1][ii-1] = "2";
					}
				}
			}
		}
		for(int jj=height-1; jj>0; jj--) {
			for(int ii=width-1; ii>0; ii--) {
				if(map[jj][ii] == "2") {
					if(map[jj-1][ii] == "6") {
						map[jj-1][ii] = "3";
					}
					if(map[jj][ii-1] == "6") {
						map[jj][ii-1] = "3";
					}
					if(map[jj-1][ii-1] == "6") {
						map[jj-1][ii-1] = "3";
					}
				}
			}
		}
		for(int jj=height-1; jj>0; jj--) {
			for(int ii=width-1; ii>0; ii--) {
				if(map[jj][ii] == "3") {
					if(map[jj-1][ii] == "6") {
						map[jj-1][ii] = "4";
					}
					if(map[jj][ii-1] == "6") {
						map[jj][ii-1] = "4";
					}
					if(map[jj-1][ii-1] == "6") {
						map[jj-1][ii-1] = "4";
					}
				}
			}
		}
		for(int jj=height-1; jj>0; jj--) {
			for(int ii=width-1; ii>0; ii--) {
				if(map[jj][ii] == "4") {
					if(map[jj-1][ii] == "6") {
						map[jj-1][ii] = "5";
					}
					if(map[jj][ii-1] == "6") {
						map[jj][ii-1] = "5";
					}
					if(map[jj-1][ii-1] == "6") {
						map[jj-1][ii-1] = "5";
					}
				}
			}
		}
		return map;
	}

	//Updates a portion of the map around the building
	public void updateMap(TilePosition position,UnitType building,boolean destroyed){
		TilePosition buildingSize = building.tileSize();
		int tamY = buildingSize.getY();
		int tamX = buildingSize.getX();
		//Updates the map with the next building to be built
		for(int i = position.getY()-1; i < position.getY()+tamY+1; i++) {
			for(int j = position.getX()-1; j < position.getX()+tamX+1; j++) {
				if(i < 0 || i >= height || j < 0 || j >= width) {
					continue;
				}
				if(destroyed) {
					if(i == position.getY()-1 || i == position.getY()+tamY || j == position.getX()-1 || j == position.getX()+tamX) {
						if(map[i][j] != "0") {
							map[i][j] = "6";
						}
					} else {
						if(map[i][j] != "V") {
							map[i][j] = "6";
						}
					}
				} else {
					if(i != position.getY()-1 && i != position.getY()+tamY && j != position.getX()-1 && j != position.getX()+tamX) {
						if(map[i][j] != "M" && map[i][j] != "V" && map[i][j] != "0" && map[i][j] != "E" && map[i][j] != "B") {
							if(building == UnitType.Terran_Bunker) {
								map[i][j] = "0";
							} else {
								map[i][j] = "E";
							}
						}
					}
				}
			}
		}
		if(building.canBuildAddon()) {
			for(int i = position.getY()+tamY; i > position.getY()+tamY-4; i--) {
				for(int j = position.getX()+tamX-1; j < position.getX()+tamX+3; j++) {
					if(i < 0 || i >= height || j < 0 || j >= width) {
						continue;
					}
					if(destroyed) {
						if(i == position.getY()+tamY-3 || i == position.getY()+tamY || j == position.getX()+tamX+2 || j == position.getX()+tamX-1) {
							if(map[i][j] != "0") {
								map[i][j] = "6";
							}
						} else {
							if(map[i][j] != "V") {
								map[i][j] = "6";
							}
						}
					} else {
						if(i != position.getY()+tamY-3 && i != position.getY()+tamY && j != position.getX()+tamX+2 && j != position.getX()+tamX-1) {
							if(map[i][j] != "M" && map[i][j] != "V" && map[i][j] != "0" && map[i][j] != "E" && map[i][j] != "B") {
								map[i][j] = "E";
							}
						}
					}
				}
			}
		}
		//Finds the corners around the building
		int init_i = 0;
		if(position.getY() - height > 0) {
			init_i = position.getY() - height;
		}
		int fin_i = height;
		if(position.getY() + tamY + height < height) {
			fin_i = position.getY() + tamY + height;
		}
		int init_j = 0;
		if(position.getX() - width > 0) {
			init_j = position.getX() - width;
		}
		int fin_j = width;
		if(position.getX() + tamX + width < width) {
			fin_j = position.getX() + tamX + width;
		}
		//Generates a submatrix as a portion of the map delimited by the corners and resets the 1,2,3 values for 4
		String[][] submapa = new String[fin_i-init_i][fin_j-init_j];
		int i = 0;
		int j = 0;
		for(int ii = init_i; ii < fin_i; ii++) {
			j = 0;
			for(int jj = init_j; jj < fin_j; jj++) {
				if(map[ii][jj]=="M" || map[ii][jj]=="V" || map[ii][jj]=="0" || map[ii][jj]=="E" || map[ii][jj]=="B") {
					submapa[i][j] = map[ii][jj];
				} else {
					submapa[i][j] = "6";
				}
				j++;
			}
			i++;
		}
		submapa = fillMap(submapa);
		//Updates the map using the submatrix
		i = 0;
		j = 0;
		for(int ii = init_i; ii < fin_i; ii++) {
			j = 0;
			for(int jj = init_j; jj < fin_j; jj++) {
				map[ii][jj] = submapa[i][j];
				j++;
			}
			i++;
		}
	}

	public boolean checkUnitsChosenBuildingGrid(TilePosition BL,UnitType type) {
		try {
			Position topLeft = new Position(BL.getX() * TilePosition.SIZE_IN_PIXELS, BL.getY() * TilePosition.SIZE_IN_PIXELS);
			Position bottomRight = new Position(topLeft.getX() + type.tileWidth() * TilePosition.SIZE_IN_PIXELS, topLeft.getY() + type.tileHeight() * TilePosition.SIZE_IN_PIXELS);
			List<Unit> blockers = game.getUnitsInRectangle(topLeft, bottomRight);

			if(blockers.isEmpty() && !getGs().getGame().canBuildHere(BL, type)) {
				return true;
			}

			if(blockers.isEmpty()) {
				return false;
			}
			if(blockers.size() > 1) {
				return true;
			}
			else {
				Unit blocker = blockers.get(0);
				if(blocker.getPlayer().getID() == self.getID() && blocker.getType().isWorker() && blocker.getBuildType() == type) {
					return false;
				}
			}
			return true;
		} catch(Exception e) {
			System.err.println(e);
		}
		return true;

	}

	//Finds a valid position in the map for a specific building type starting with a given tileposition
	public TilePosition findPosition(UnitType buildingType, TilePosition starting){
		TilePosition buildingSize = buildingType.tileSize();
		int size = Math.max(buildingSize.getY(), buildingSize.getX());
		if(buildingType.canBuildAddon()) {
			size = Math.max(buildingSize.getY(), buildingSize.getX() + 2);
		}
		int x = starting.getY();
		int y = starting.getX();
		int[] coord = new int[2];
		int i = 2;
		int j = 2;
		boolean control = false;

		//Finds the first valid tileposition starting around the given tileposition
		while(!control) {
			for(int ii = (x - i); ii <= (x + i); ii++) {
				for(int jj = (y - j); jj <= (y + j); jj++) {
					if((ii >= 0 && ii < height) && (jj >= 0 && jj < width)) {
						if((map[ii][jj] != "M" && map[ii][jj] != "V" && map[ii][jj] != "E" && map[ii][jj] != "B") && Integer.parseInt(map[ii][jj]) >= size) {
							if(buildingType == UnitType.Terran_Bunker) {
								if(!BWTA.getRegion(new TilePosition(jj, ii)).getCenter().equals(BWTA.getRegion(self.getStartLocation()).getCenter())) {
									continue;
								}
							}
							if(!checkUnitsChosenBuildingGrid( new TilePosition(jj, ii), buildingType)) {
								coord[0] = ii; coord[1] = jj; control = true; break;
							}
						}
					}
				}
				if(control) {
					break;
				}
			}
			i++;
			j++;
		}
		TilePosition position = new TilePosition(coord[1], coord[0]);
		return position;
	}

	public TilePosition findBunkerPosition(Chokepoint choke){
		TilePosition buildingSize = UnitType.Terran_Bunker.tileSize();
		int size = Math.max(buildingSize.getY(), buildingSize.getX());
		Position starting = choke.getCenter();
		Pair<Position,Position> sides = choke.getSides();
		Position closestSide = null;
		if(getGs().broodWarDistance(self.getStartLocation().toPosition(), sides.first) < getGs().broodWarDistance(self.getStartLocation().toPosition(), sides.second)){
			closestSide = sides.first;
		} else {
			closestSide = sides.second;
		}
		int x = starting.toTilePosition().getY();
		int y = starting.toTilePosition().getX();

		int i = 15;
		int j = 15;
		//Finds the first valid tileposition starting around the given tileposition
		TilePosition position = null;
		double dist = Double.MAX_VALUE;
		for(int ii = (x - i); ii <= (x + i); ii++) {
			for(int jj = (y - j); jj <= (y + j); jj++) {
				if((ii >= 0 && ii < height) && (jj >= 0 && jj < width)) {
					if((map[ii][jj] != "M" && map[ii][jj] != "V" && map[ii][jj] != "E" && map[ii][jj] != "B") && Integer.parseInt(map[ii][jj]) >= size) {
						if(BWTA.getRegion(new TilePosition(jj, ii)).getCenter().equals(getGs().naturalRegion.getCenter())) {
							continue;
						}
						if(!checkUnitsChosenBuildingGrid(new TilePosition(jj, ii), UnitType.Terran_Bunker)) {
							TilePosition newPosition = new TilePosition(jj, ii);
							double newDist = getGs().broodWarDistance(getGs().getCenterFromBuilding(newPosition.toPosition(), UnitType.Terran_Bunker), closestSide);
							if(position == null || newDist < dist) {
								position = newPosition;
								dist = newDist;
							}
						}
					}
				}
			}
		}
		return position;
	}

	public TilePosition findBunkerPositionAntiPool(){ // TODO Finish it
		TilePosition bunkerPlace = TilePosition.None;
		Chokepoint choke = getGs().closestChoke;
		Position chokePos = choke.getCenter();
		Set<Position> posToDefend = new HashSet<>();
		int marineRange = UnitType.Terran_Marine.groundWeapon().maxRange();
		Side side = getSide(getGs().MainCC.getPosition(), choke.getCenter());

		Position top = null;
		Position bottom = null;
		double distTop = Double.MAX_VALUE;
		double distBottom = Double.MIN_VALUE;
		for(Unit m : getGs().mineralsAssigned.keySet()) {
			Position mPos = m.getPosition();
			if(top == null && bottom == null) {
				top = mPos;
				bottom = mPos;
				continue;
			}
			double dist = getGs().broodWarDistance(mPos, chokePos);
			if(dist < distTop) {
				top = mPos;
				distTop = dist;
			}
			if(dist > distBottom) {
				bottom = mPos;
				distBottom = dist;
			}

		}
		System.out.println(top.toTilePosition());
		System.out.println(bottom.toTilePosition());
		// Old method just in case something goes wrong
		if(bunkerPlace == TilePosition.None) {
			TilePosition starting = getGs().MBs.iterator().next().getTilePosition();
			TilePosition buildingSize = UnitType.Terran_Bunker.tileSize();
			int size = Math.max(buildingSize.getY(), buildingSize.getX());
			int x = starting.getY();
			int y = starting.getX();

			int i = 4;
			int j = 4;

			double dist = Double.MAX_VALUE;
			for(int ii = (x - i); ii <= (x + i + 4); ii++) {
				for(int jj = (y - j); jj <= (y + j + 3); jj++) {
					if((ii >= 0 && ii < height) && (jj >= 0 && jj < width)) {
						if((map[ii][jj] != "M" && map[ii][jj] != "V" && map[ii][jj] != "E" && map[ii][jj] != "B") && Integer.parseInt(map[ii][jj]) >= size) {
							if(BWTA.getRegion(new TilePosition(jj, ii)).getCenter().equals(getGs().naturalRegion.getCenter())) {
								continue;
							}
							if(!checkUnitsChosenBuildingGrid(new TilePosition(jj, ii), UnitType.Terran_Bunker)) {
								TilePosition newPosition = new TilePosition(jj, ii);
								double newDist = getGs().broodWarDistance(getGs().getCenterFromBuilding(newPosition.toPosition(), UnitType.Terran_Bunker), choke.getCenter());
								if(bunkerPlace == TilePosition.None || newDist < dist) {
									bunkerPlace = newPosition;
									dist = newDist;
								}
							}
						}
					}
				}
			}
		}

		return bunkerPlace;
	}

	private Side getSide(Position start, Position end) {
		double x = end.getX() - start.getX();
		double y = end.getY() - start.getY();
		if(Math.abs(x) > Math.abs(y)) {
			return x > 0 ? Side .LEFT : Side.RIGHT;
		}
		return y > 0 ? Side.BOTTOM : Side.TOP;
	}

	//Writes the map to a file
	public void writeMap(String fileName){
		FileWriter sw = null;
		try {
			sw = new FileWriter(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(int ii=0; ii<height; ii++) {
			for(int jj=0; jj<width; jj++) {
				try {
					sw.write(map[ii][jj]);
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
