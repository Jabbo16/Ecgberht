package ecgberht;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import bwapi.Unit;
import bwapi.UnitType;

public class IntelligenceAgency {
	private static Map<String, TreeSet<Unit>> enemyBases = new TreeMap<>();
	private static Map<String, HashSet<UnitType>> enemyTypes = new TreeMap<>();

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
		String player = unit.getPlayer().getName();
		if(type.isResourceDepot()) {
			if(!enemyBases.containsKey(player)) {
				TreeSet<Unit> aux = new TreeSet<>(new UnitComparator());
				aux.add(unit);
				enemyBases.put(player, aux);

			} else {
				enemyBases.get(player).add(unit);
			}
		}
		if(!type.isBuilding() && !type.isWorker() && (type.canAttack() || type.isSpellcaster() || (type.spaceProvided() > 0 && type.supplyProvided() == 0))) {
			if(!enemyTypes.containsKey(player)) {
				HashSet<UnitType> aux = new HashSet<>();
				aux.add(type);
				enemyTypes.put(player, aux);

			} else {
				enemyTypes.get(player).add(type);
			}
		}
	}

	public static void onDestroy(Unit unit, UnitType type) {
		String player = unit.getPlayer().getName();
		if(type.isResourceDepot() && enemyBases.containsKey(player)) {
			if(enemyBases.get(player).contains(unit)) enemyBases.get(player).remove(unit);
		}
	}

}
