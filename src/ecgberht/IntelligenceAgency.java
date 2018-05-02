package ecgberht;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import static ecgberht.Ecgberht.getGs;

import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Drone;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.Unit;

public class IntelligenceAgency {
	private static Map<String, TreeSet<Unit>> enemyBases = new TreeMap<>();
	private static Map<String, HashSet<UnitType>> enemyTypes = new TreeMap<>();
	private static TreeSet<Unit> drones = new TreeSet<>(new UnitComparator());

	public static int getNumDrones() {
		return drones.size();
	}

	public static int getNumEnemyBases(String player) {
		if(enemyBases.containsKey(player))return enemyBases.get(player).size();
		return 0;
	}

	public static boolean enemyHasType(String player, UnitType type) {
		if(enemyTypes.containsKey(player)) {
			if(enemyTypes.get(player).contains(type)) return true;
		}
		return false;
	}

	public static boolean enemyHasType(UnitType type) {
		for(HashSet<UnitType> set : enemyTypes.values()) {
			if(set.contains(type)) return true;
		}
		return false;
	}

	public static void printEnemyTypes() {
		for(Entry<String, HashSet<UnitType>> entry : enemyTypes.entrySet()) {
			for(UnitType type : entry.getValue()) {
				System.out.println(entry.getKey() + ": " + type);
			}
		}
	}

	public static void onShow(Unit unit, UnitType type) {
		String player = ((PlayerUnit)unit).getPlayer().getName();
		if(getGs().enemyRace == Race.Zerg) {
			if(unit instanceof Drone) {
				if(!drones.contains(unit)) drones.add(unit);
			}
		}
		// If base and player known skip
		if(enemyBases.containsKey(player) && enemyBases.get(player).contains(unit)) return;

		// Bases
		if(type.isResourceDepot()) {
			if(!enemyBases.containsKey(player)) {
				TreeSet<Unit> aux = new TreeSet<>(new UnitComparator());
				aux.add(unit);
				enemyBases.put(player, aux);

			} else {
				enemyBases.get(player).add(unit);
			}
		}

		// If player and type known skip
		if(enemyTypes.containsKey(player) && enemyTypes.get(player).contains(type)) return;

		// Normal units (no workers and no real combat or support units)
		else if(!type.isBuilding() && !type.isWorker() && (type.canAttack() || type.isSpellcaster() || (type.spaceProvided() > 0 && type.supplyProvided() == 0))) {
			if(!enemyTypes.containsKey(player)) {
				HashSet<UnitType> aux = new HashSet<>();
				aux.add(type);
				enemyTypes.put(player, aux);

			} else {
				enemyTypes.get(player).add(type);
			}
		}
		// Eggs
		else if(type == UnitType.Zerg_Lurker_Egg) {
			if(!enemyTypes.containsKey(player)) {
				HashSet<UnitType> aux = new HashSet<>();
				aux.add(UnitType.Zerg_Lurker);
				enemyTypes.put(player, aux);

			} else {
				enemyTypes.get(player).add(UnitType.Zerg_Lurker);
			}
		}
		// Buildings
		else if(type.isBuilding()) {
			// Protoss tech
			if(type == UnitType.Protoss_Arbiter_Tribunal) {
				if(!enemyTypes.containsKey(player)) {
					HashSet<UnitType> aux = new HashSet<>();
					aux.add(UnitType.Protoss_Arbiter);
					enemyTypes.put(player, aux);

				} else {
					enemyTypes.get(player).add(UnitType.Protoss_Arbiter);
				}
			}
			else if(type == UnitType.Protoss_Templar_Archives) {
				if(!enemyTypes.containsKey(player)) {
					HashSet<UnitType> aux = new HashSet<>();
					aux.add(UnitType.Protoss_Dark_Templar);
					aux.add(UnitType.Protoss_High_Templar);
					enemyTypes.put(player, aux);

				} else {
					enemyTypes.get(player).add(UnitType.Protoss_Dark_Templar);
					enemyTypes.get(player).add(UnitType.Protoss_High_Templar);
				}
			}
			else if(type == UnitType.Protoss_Fleet_Beacon) {
				if(!enemyTypes.containsKey(player)) {
					HashSet<UnitType> aux = new HashSet<>();
					aux.add(UnitType.Protoss_Carrier);
					enemyTypes.put(player, aux);

				} else {
					enemyTypes.get(player).add(UnitType.Protoss_Carrier);
				}
			}
			else if(type == UnitType.Protoss_Robotics_Support_Bay) {
				if(!enemyTypes.containsKey(player)) {
					HashSet<UnitType> aux = new HashSet<>();
					aux.add(UnitType.Protoss_Reaver);
					enemyTypes.put(player, aux);

				} else {
					enemyTypes.get(player).add(UnitType.Protoss_Reaver);
				}
			}
			// Zerg tech
			else if(type == UnitType.Zerg_Spawning_Pool) {
				if(!enemyTypes.containsKey(player)) {
					HashSet<UnitType> aux = new HashSet<>();
					aux.add(UnitType.Zerg_Zergling);
					enemyTypes.put(player, aux);

				} else {
					enemyTypes.get(player).add(UnitType.Zerg_Zergling);
				}
			}
			else if(type == UnitType.Zerg_Spire) {
				if(!enemyTypes.containsKey(player)) {
					HashSet<UnitType> aux = new HashSet<>();
					aux.add(UnitType.Zerg_Mutalisk);
					enemyTypes.put(player, aux);

				} else {
					enemyTypes.get(player).add(UnitType.Zerg_Mutalisk);
				}
			}
			else if(type == UnitType.Zerg_Hydralisk_Den) {
				if(!enemyTypes.containsKey(player)) {
					HashSet<UnitType> aux = new HashSet<>();
					aux.add(UnitType.Zerg_Hydralisk);
					enemyTypes.put(player, aux);

				} else {
					enemyTypes.get(player).add(UnitType.Zerg_Hydralisk);
				}
			}
			else if(type == UnitType.Zerg_Queens_Nest) {
				if(!enemyTypes.containsKey(player)) {
					HashSet<UnitType> aux = new HashSet<>();
					aux.add(UnitType.Zerg_Queen);
					enemyTypes.put(player, aux);

				} else {
					enemyTypes.get(player).add(UnitType.Zerg_Queen);
				}
			}
			else if(type == UnitType.Zerg_Defiler_Mound) {
				if(!enemyTypes.containsKey(player)) {
					HashSet<UnitType> aux = new HashSet<>();
					aux.add(UnitType.Zerg_Defiler);
					enemyTypes.put(player, aux);

				}
				else {
					enemyTypes.get(player).add(UnitType.Zerg_Defiler);
				}
			}
		}
	}

	public static void onDestroy(Unit unit, UnitType type) {
		String player = ((PlayerUnit)unit).getPlayer().getName();
		if(type.isResourceDepot() && enemyBases.containsKey(player)) {
			if(enemyBases.get(player).contains(unit)) enemyBases.get(player).remove(unit);
		}
		if(getGs().enemyRace == Race.Zerg) {
			if(unit instanceof Drone) {
				if(drones.contains(unit)) drones.remove(unit);
			}
		}
	}
}
