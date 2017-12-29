package ecgberht;

import java.util.HashSet;
import java.util.Set;

import bwapi.Position;
import bwapi.Unit;

public class Squad {

	public enum Status {
		ATTACK, KITE, RETREAT, MOVE, IDLE
	}
	public String name;
	public Set<Unit> members;
	public Status estado;
	public Position attack;

	public Squad(String name) {
		this.name = name;
		members = new HashSet<Unit>();
		estado = Status.IDLE;
		attack = null;
	}

	public void addToSquad(Unit unit) {
		this.members.add(unit);
	}

	public void giveAttackOrder(Position pos) {
		for(Unit u : members) {
			u.attack(pos);
		}
		attack = pos;
		estado = Status.ATTACK;
	}
	
}
