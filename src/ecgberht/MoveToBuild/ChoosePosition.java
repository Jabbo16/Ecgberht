package ecgberht.MoveToBuild;

import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;

import bwapi.Game;
import bwapi.Pair;
import bwapi.Player;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import ecgberht.GameState;

public class ChoosePosition extends Action {

	public ChoosePosition(String name, GameHandler gh) {
		super(name, gh);
	}

	@Override
	public State execute() {
		try {
			Game juego = ((GameState)this.handler).getGame();
			Player jugador = ((GameState)this.handler).getPlayer();
			TilePosition origin = null;
			if(((GameState)this.handler).chosenToBuild.isRefinery()) {
				if(!((GameState)this.handler).refineriesAssigned.isEmpty()) {
					for (Pair<Pair<Unit,Integer>,Boolean> g : ((GameState)this.handler).refineriesAssigned) {
						if (!g.second) {
							((GameState)this.handler).chosenPosition = g.first.first.getTilePosition();
							return State.SUCCESS;
						}
					}
				}

			} else {
				if(!((GameState)this.handler).workerBuild.isEmpty()) {
					for(Pair<Unit,Pair<UnitType,TilePosition> > w : ((GameState)this.handler).workerBuild) {
						((GameState)this.handler).testMap.updateMap(w.second.second, w.second.first, false);
					}
				}

				if(!((GameState)this.handler).chosenToBuild.equals(UnitType.Terran_Bunker) && !((GameState)this.handler).chosenToBuild.equals(UnitType.Terran_Missile_Turret)) {
					if(((GameState)this.handler).strat.proxy && ((GameState)this.handler).chosenToBuild == UnitType.Terran_Barracks) {
						origin = new TilePosition(((GameState)this.handler).getGame().mapWidth()/2, ((GameState)this.handler).getGame().mapHeight()/2);
					}
					else {
						//origin = BWTA.getRegion(jugador.getStartLocation()).getCenter().toTilePosition();
						origin = jugador.getStartLocation();
					}
				}
				else{
					if(((GameState)this.handler).chosenToBuild.equals(UnitType.Terran_Missile_Turret)) {
						if(((GameState)this.handler).DBs.isEmpty()) {
							origin = BWTA.getNearestChokepoint(jugador.getStartLocation()).getCenter().toTilePosition();
						}
						else {
							for(Unit b : ((GameState)this.handler).DBs.keySet()) {
								origin = b.getTilePosition();
								break;
							}
						}
					}
					else {
						if(((GameState)this.handler).EI.naughty && ((GameState)this.handler).enemyRace == Race.Zerg) {

							origin = ((GameState)this.handler).testMap.findBunkerPositionAntiPool();
							if(origin != null) {
								((GameState)this.handler).testMap = ((GameState)this.handler).map.clone();
								((GameState)this.handler).chosenPosition = origin;
								return State.SUCCESS;
							}
							else {
								origin = ((GameState)this.handler).getBunkerPositionAntiPool();
								if(origin != null) {
									((GameState)this.handler).testMap = ((GameState)this.handler).map.clone();
									((GameState)this.handler).chosenPosition = origin;
									return State.SUCCESS;
								}
								else {
									if(((GameState)this.handler).MainCC != null) {
										origin = ((GameState)this.handler).MainCC.getTilePosition();
									}
									else {
										origin = ((GameState)this.handler).getPlayer().getStartLocation();
									}
								}
							}
						}
						else {
							if(((GameState)this.handler).Ts.isEmpty()) {
								if(((GameState)this.handler).closestChoke != null) {
									origin = ((GameState)this.handler).testMap.findBunkerPosition(((GameState)this.handler).closestChoke);
									if(origin != null) {
										((GameState)this.handler).testMap = ((GameState)this.handler).map.clone();
										((GameState)this.handler).chosenPosition = origin;
										return State.SUCCESS;
									}
									else {
										origin = ((GameState)this.handler).closestChoke.getCenter().toTilePosition();
									}

								}
//								else {
//									origin = BWTA.getNearestChokepoint(jugador.getStartLocation()).getCenter().toTilePosition();
//								}

							}
							else {
								for(Unit b : ((GameState)this.handler).Ts) {
									origin = b.getTilePosition();
									break;
								}
							}
						}

					}

				}
				TilePosition posicion = ((GameState)this.handler).testMap.findPosition(((GameState)this.handler).chosenToBuild, origin);
				((GameState)this.handler).testMap = ((GameState)this.handler).map.clone();
				if(posicion != null) {
					((GameState)this.handler).chosenPosition = posicion;
					return State.SUCCESS;
				}
			}

			TilePosition posicion = juego.getBuildLocation(((GameState)this.handler).chosenToBuild, BWTA.getRegion(jugador.getStartLocation()).getCenter().toTilePosition(), 500);
			if(posicion != null) {
				if(juego.canBuildHere(posicion, ((GameState)this.handler).chosenToBuild)) {
					((GameState)this.handler).chosenPosition = posicion;
					return State.SUCCESS;
				}
			}
			return State.FAILURE;
		} catch(Exception e) {
			System.err.println(this.getClass().getSimpleName());
			System.err.println(e);
			return State.ERROR;
		}
	}
}
