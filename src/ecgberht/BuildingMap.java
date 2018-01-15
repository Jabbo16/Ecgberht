package ecgberht;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import bwapi.Game;
import bwapi.Player;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;

public class BuildingMap {

	private Game game;
	private Player self;
	private int alto;
	private int ancho;
	private String mapa[][];

	public BuildingMap(Game game, Player self) {
		this.game = game;
		this.self = self;
		this.alto = game.mapHeight();
		this.ancho = game.mapWidth();
		this.mapa = new String[this.alto][this.ancho];
	}

	public BuildingMap(Game game, Player self, int alto, int ancho, String[][] mapa) {
		this.game = game;
		this.self = self;
		this.alto = alto;
		this.ancho = ancho;
		this.mapa = mapa.clone();
	}

	public String[][] getMap(){
		return this.mapa;
	}

	public BuildingMap clone() {
		BuildingMap mapa2 = new BuildingMap(game,self,alto,ancho,mapa);
		return mapa2;
	}

	//Metodo para generar un mapa inicial
	public void initMap() {
		//Busca posiciones validas y no validas para construccion
		for(int jj=0; jj<alto; jj++) {
			for(int ii=0; ii<ancho; ii++) {
				TilePosition x = new TilePosition(ii, jj);
				if(game.isBuildable(x)) {
					mapa[jj][ii] = "6";
				} else {
					mapa[jj][ii] = "0";
				}
			}
		}
		//Recorre unidades neutrales para buscar minerales y vespeno
		for(Unit unidad : game.getStaticNeutralUnits()) {
			TilePosition recurso = unidad.getTilePosition();
			if(unidad.getType().isBuilding()) {
				if(unidad.getType().isMineralField()) {
					TilePosition tamaño_ed = unidad.getType().tileSize();
					for(int i = recurso.getY(); i < recurso.getY() + tamaño_ed.getY(); i++) {
						for(int j = recurso.getX(); j < recurso.getX() + tamaño_ed.getX(); j++) {
							if(i < 0 || i >= alto || j < 0 || j >= ancho) {
								continue;
							}
							if(mapa[i][j] != "V") {
								mapa[i][j] = "M";
							}
						}
					}
				} else if(unidad.getType() == UnitType.Resource_Vespene_Geyser) {
					TilePosition tamaño_ed = unidad.getType().tileSize();
					for(int i = recurso.getY(); i < recurso.getY() + tamaño_ed.getY(); i++) {
						for(int j = recurso.getX(); j < recurso.getX() + tamaño_ed.getX(); j++) {
							if(i < 0 || i >= alto || j < 0 || j >= ancho) {
								continue;
							}
							mapa[i][j] = "V";
						}
					}
				} else {
					TilePosition tamaño_ed = unidad.getType().tileSize();
					for(int i = recurso.getY(); i < recurso.getY() + tamaño_ed.getY(); i++) {
						for(int j = recurso.getX(); j < recurso.getX() + tamaño_ed.getX(); j++) {
							if(i < 0 || i >= alto || j < 0 || j >= ancho) {
								continue;
							}
							mapa[i][j] = "E";
						}
					}
				}
			}
		}
		for(BaseLocation b : BWTA.getBaseLocations()) {
			TilePosition starting = b.getTilePosition();
			for(int i = starting.getY(); i < starting.getY() + UnitType.Terran_Command_Center.tileHeight(); i++) {
				for(int j = starting.getX(); j < starting.getX() + UnitType.Terran_Command_Center.tileWidth(); j++) {
					if(i < 0 || i >= alto || j < 0 || j >= ancho) {
						continue;
					}
					mapa[i][j] = "E";
				}
			}
			mapa[starting.getY() + 1][starting.getX() + UnitType.Terran_Command_Center.tileWidth()] = "E";
			mapa[starting.getY() + 2][starting.getX() + UnitType.Terran_Command_Center.tileWidth()] = "E";
			mapa[starting.getY() + 1][starting.getX() + UnitType.Terran_Command_Center.tileWidth() + 1] = "E";
			mapa[starting.getY() + 2][starting.getX() + UnitType.Terran_Command_Center.tileWidth() + 1] = "E";
		}
		mapa = rellenaMapa(mapa);
	}

	//Metodo que rellena un mapa con los distintos valores permitidos
	public String[][] rellenaMapa(String[][] mapa){
		int alto = mapa.length;
		int ancho = mapa[0].length;
		for(int jj=alto-1; jj>=0; jj--) {
			if(mapa[jj][ancho-1] != "M" && mapa[jj][ancho-1] != "V" && mapa[jj][ancho-1] != "0" && mapa[jj][ancho-1] != "E" && mapa[jj][ancho-1] != "B") {
				if(jj == this.alto-1 || ancho-1 == this.ancho-1) {
					mapa[jj][ancho-1] = "1";
				}
			}
		}
		for(int ii=ancho-1; ii>=0; ii--) {
			if(mapa[alto-1][ii] != "M" && mapa[alto-1][ii]  != "V" && mapa[alto-1][ii]  != "0" && mapa[alto-1][ii]  != "E" && mapa[alto-1][ii]  != "B") {
				if(alto-1 == this.alto-1 || ii == this.ancho-1) {
					mapa[alto-1][ii] = "1";
				}
			}
		}
		//Se ponen a "B" las casillas adyacentes a 0,M,V por izquierda y arriba y sean 6
		for(int jj=alto-1; jj>=0; jj--) {
			for(int ii=ancho-1; ii>=0; ii--) {
				if(mapa[jj][ii] == "E" || mapa[jj][ii] == "M" || mapa[jj][ii] == "V") {
					if(jj-1>=0) {
						if(mapa[jj-1][ii] == "6") {
							mapa[jj-1][ii] = "B";
						}
					}
					if(ii-1>=0) {
						if(mapa[jj][ii-1] == "6") {
							mapa[jj][ii-1] = "B";
						}
					}
					if(jj-1>=0 && ii-1>=0) {
						if(mapa[jj-1][ii-1] == "6") {
							mapa[jj-1][ii-1] = "B";
						}
					}
					if(jj+1<alto) {
						if(mapa[jj+1][ii] == "6") {
							mapa[jj+1][ii] = "B";
						}
					}
					if(ii+1<ancho) {
						if(mapa[jj][ii+1] == "6") {
							mapa[jj][ii+1] = "B";
						}
					}
					if(jj+1<alto && ii+1<ancho) {
						if(mapa[jj+1][ii+1] == "6") {
							mapa[jj+1][ii+1] = "B";
						}
					}
					if(jj-1>=0 && ii+1<ancho) {
						if(mapa[jj-1][ii+1] == "6") {
							mapa[jj-1][ii+1] = "B";
						}
					}
					if(jj+1<alto && ii-1>=0) {
						if(mapa[jj+1][ii-1] == "6") {
							mapa[jj+1][ii-1] = "B";
						}
					}
				}
			}
		}
		for(int jj=alto-1; jj>0; jj--) {
			for(int ii=ancho-1; ii>0; ii--) {
				if(mapa[jj][ii] == "B" || mapa[jj][ii] == "0" || mapa[jj][ii] == "M" || mapa[jj][ii] == "V" || mapa[jj][ii] == "E") {
					if(mapa[jj-1][ii] == "6") {
						mapa[jj-1][ii] = "1";
					}
					if(mapa[jj][ii-1] == "6") {
						mapa[jj][ii-1] = "1";
					}
					if(mapa[jj-1][ii-1] == "6") {
						mapa[jj-1][ii-1] = "1";
					}
				}
			}
		}
		for(int jj=alto-1; jj>0; jj--) {
			for(int ii=ancho-1; ii>0; ii--) {
				if(mapa[jj][ii] == "1") {
					if(mapa[jj-1][ii] == "6") {
						mapa[jj-1][ii] = "2";
					}
					if(mapa[jj][ii-1] == "6") {
						mapa[jj][ii-1] = "2";
					}
					if(mapa[jj-1][ii-1] == "6") {
						mapa[jj-1][ii-1] = "2";
					}
				}
			}
		}
		for(int jj=alto-1; jj>0; jj--) {
			for(int ii=ancho-1; ii>0; ii--) {
				if(mapa[jj][ii] == "2") {
					if(mapa[jj-1][ii] == "6") {
						mapa[jj-1][ii] = "3";
					}
					if(mapa[jj][ii-1] == "6") {
						mapa[jj][ii-1] = "3";
					}
					if(mapa[jj-1][ii-1] == "6") {
						mapa[jj-1][ii-1] = "3";
					}
				}
			}
		}
		for(int jj=alto-1; jj>0; jj--) {
			for(int ii=ancho-1; ii>0; ii--) {
				if(mapa[jj][ii] == "3") {
					if(mapa[jj-1][ii] == "6") {
						mapa[jj-1][ii] = "4";
					}
					if(mapa[jj][ii-1] == "6") {
						mapa[jj][ii-1] = "4";
					}
					if(mapa[jj-1][ii-1] == "6") {
						mapa[jj-1][ii-1] = "4";
					}
				}
			}
		}
		for(int jj=alto-1; jj>0; jj--) {
			for(int ii=ancho-1; ii>0; ii--) {
				if(mapa[jj][ii] == "4") {
					if(mapa[jj-1][ii] == "6") {
						mapa[jj-1][ii] = "5";
					}
					if(mapa[jj][ii-1] == "6") {
						mapa[jj][ii-1] = "5";
					}
					if(mapa[jj-1][ii-1] == "6") {
						mapa[jj-1][ii-1] = "5";
					}
				}
			}
		}
		return mapa;
	}

	//Metodo que actualiza una porcion del mapa
	public void actualizaMapa(TilePosition posicion,UnitType edificio,boolean Destroyed){
		TilePosition tamaño_ed = edificio.tileSize();
		int tamY = tamaño_ed.getY();
		int tamX = tamaño_ed.getX();
		//Actualiza el mapa con el proximo edificio a construir
		for(int i = posicion.getY()-1; i < posicion.getY()+tamY+1; i++) {
			for(int j = posicion.getX()-1; j < posicion.getX()+tamX+1; j++) {
				if(i < 0 || i >= alto || j < 0 || j >= ancho) {
					continue;
				}
				if(Destroyed) {
					if(i == posicion.getY()-1 || i == posicion.getY()+tamY || j == posicion.getX()-1 || j == posicion.getX()+tamX) {
						if(mapa[i][j] != "0") {
							mapa[i][j] = "6";
						}
					} else {
						if(mapa[i][j] != "V") {
							mapa[i][j] = "6";
						}
					}
				} else {
					if(i != posicion.getY()-1 && i != posicion.getY()+tamY && j != posicion.getX()-1 && j != posicion.getX()+tamX) {
						if(mapa[i][j] != "M" && mapa[i][j] != "V" && mapa[i][j] != "0" && mapa[i][j] != "E" && mapa[i][j] != "B") {
							if(edificio == UnitType.Terran_Bunker) {
								mapa[i][j] = "0";
							} else {
								mapa[i][j] = "E";
							}
						}
					}
				}
			}
		}
		if(edificio.canBuildAddon()) {
			for(int i = posicion.getY()+tamY; i > posicion.getY()+tamY-4; i--) {
				for(int j = posicion.getX()+tamX-1; j < posicion.getX()+tamX+3; j++) {
					if(i < 0 || i >= alto || j < 0 || j >= ancho) {
						continue;
					}
					if(Destroyed) {
						if(i == posicion.getY()+tamY-3 || i == posicion.getY()+tamY || j == posicion.getX()+tamX+2 || j == posicion.getX()+tamX-1) {
							if(mapa[i][j] != "0") {
								mapa[i][j] = "6";
							}
						} else {
							if(mapa[i][j] != "V") {
								mapa[i][j] = "6";
							}
						}
					} else {
						if(i != posicion.getY()+tamY-3 && i != posicion.getY()+tamY && j != posicion.getX()+tamX+2 && j != posicion.getX()+tamX-1) {
							if(mapa[i][j] != "M" && mapa[i][j] != "V" && mapa[i][j] != "0" && mapa[i][j] != "E" && mapa[i][j] != "B") {
								mapa[i][j] = "E";
							}
						}
					}
				}
			}
		}
		//Se hallan las esquinas de la submatriz que contiene al edificio y sus alrededores
		int init_i = 0;
		if(posicion.getY() - alto > 0) {
			init_i = posicion.getY() - alto;
		}
		int fin_i = alto;
		if(posicion.getY() + tamY + alto < alto) {
			fin_i = posicion.getY() + tamY + alto;
		}
		int init_j = 0;
		if(posicion.getX() - ancho > 0) {
			init_j = posicion.getX() - ancho;
		}
		int fin_j = ancho;
		if(posicion.getX() + tamX + ancho < ancho) {
			fin_j = posicion.getX() + tamX + ancho;
		}
		//Se genera la submatriz como copia de la porcion del mapa delimitado por las esquinas y reseteando los 1,2,3 por 4
		String[][] submapa = new String[fin_i-init_i][fin_j-init_j];
		int i = 0;
		int j = 0;
		for(int ii = init_i; ii < fin_i; ii++) {
			j = 0;
			for(int jj = init_j; jj < fin_j; jj++) {
				if(mapa[ii][jj]=="M" || mapa[ii][jj]=="V" || mapa[ii][jj]=="0" || mapa[ii][jj]=="E" || mapa[ii][jj]=="B") {
					submapa[i][j] = mapa[ii][jj];
				} else {
					submapa[i][j] = "6";
				}
				j++;
			}
			i++;
		}
		submapa = rellenaMapa(submapa);
		//Se pasan los cambios de la submatriz al mapa
		i = 0;
		j = 0;
		for(int ii = init_i; ii < fin_i; ii++) {
			j = 0;
			for(int jj = init_j; jj < fin_j; jj++) {
				mapa[ii][jj] = submapa[i][j];
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
			if(blockers.isEmpty()) {
				return false;
			}
			else {
				if(blockers.size() > 1) {
					return true;
				}
				else {
					Unit blocker = blockers.get(0);
					if(blocker.getPlayer().getID() == self.getID() && blocker.getType().isWorker() && blocker.getBuildType() == type) {
						return false;
					}
				}
			}
			return true;
		} catch(Exception e) {
			System.err.println(e);
		}
		return true;
		
	}
	
	//Metodo que busca una posicion apta para construir segun el mapa
	public TilePosition buscaPosicion(UnitType edificio, TilePosition starting){
		TilePosition tamaño_ed = edificio.tileSize();
		int tamaño = Math.max(tamaño_ed.getY(), tamaño_ed.getX());
		if(edificio.canBuildAddon()) {
			tamaño = Math.max(tamaño_ed.getY(), tamaño_ed.getX() + 2);
		}
		int x = starting.getY();
		int y = starting.getX();
		int i = 2;
		int j = 2;
		boolean control = false;
		int[] coord = new int[2];
		//Busca la primera posicion apta desde el centro de mando inicial
		while(!control) {
			for(int ii = (x - i); ii <= (x + i); ii++) {
				for(int jj = (y - j); jj <= (y + j); jj++) {
					if((ii >= 0 && ii < alto) && (jj >= 0 && jj < ancho)) {
						if((mapa[ii][jj] != "M" && mapa[ii][jj] != "V" && mapa[ii][jj] != "E" && mapa[ii][jj] != "B") && Integer.parseInt(mapa[ii][jj]) >= tamaño) {
							if(edificio == UnitType.Terran_Bunker) {
								if(!BWTA.getRegion(new TilePosition(jj, ii)).getCenter().equals(BWTA.getRegion(self.getStartLocation()).getCenter())) {
									continue;
								}
							}
							if(!checkUnitsChosenBuildingGrid( new TilePosition(jj, ii), edificio)) {
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
		TilePosition posicion = new TilePosition(coord[1], coord[0]);
		return posicion;
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
					sw.write(mapa[ii][jj]);
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
