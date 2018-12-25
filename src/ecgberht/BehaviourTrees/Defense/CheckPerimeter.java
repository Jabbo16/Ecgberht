package ecgberht.BehaviourTrees.Defense;

import bwem.Base;
import bwem.area.Area;
import ecgberht.GameState;
import ecgberht.Squad;
import ecgberht.Squad.Status;
import ecgberht.UnitStorage;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

import java.util.*;
import java.util.stream.Collectors;

public class CheckPerimeter extends Conditional {

    public CheckPerimeter(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {

        try {
            this.handler.enemyInBase.clear();
            this.handler.defense = false;
            Set<Unit> enemyInvaders = new TreeSet<>(this.handler.enemyCombatUnitMemory);
            for (UnitStorage.UnitInfo u : this.handler.unitStorage.getEnemyUnits().values().stream().filter(u -> u.unitType.isBuilding()).collect(Collectors.toSet())) {
                if (u.unitType.canAttack() || u.unitType == UnitType.Protoss_Pylon || u.unitType.canProduce() || u.unitType.isRefinery()) {
                    enemyInvaders.add(u.unit);
                }
            }
            for (Unit u : enemyInvaders) {
                UnitType uType = u.getType();
                if (u instanceof Building || ((uType.canAttack() || uType.isSpellcaster() || u instanceof Loadable)
                        && uType != UnitType.Zerg_Scourge && uType != UnitType.Terran_Valkyrie
                        && uType != UnitType.Protoss_Corsair && !(u instanceof Overlord))) {
                    Area enemyArea = this.handler.bwem.getMap().getArea(u.getTilePosition());
                    if (enemyArea != null) {
                        Area myMainArea = this.handler.mainCC != null ? this.handler.mainCC.first.getArea() : null;
                        Area myNatArea = this.handler.naturalArea;
                        for (Base b : this.handler.CCs.keySet()) {
                            if (!b.getArea().equals(enemyArea)) continue;
                            if ((myMainArea != null && !b.getArea().equals(myMainArea)
                                    && (myNatArea != null && !b.getArea().equals(myNatArea)))) continue;
                            this.handler.enemyInBase.add(u);
                            break;
                        }
                    }
                    for (Map.Entry<SCV, Building> c : this.handler.workerTask.entrySet()) {
                        int dist = c.getValue() instanceof CommandCenter ? 500 : 200;
                        if (Util.broodWarDistance(u.getPosition(), c.getValue().getPosition()) <= dist) {
                            this.handler.enemyInBase.add(u);
                            break;
                        }
                    }
                    for (Unit c : this.handler.CCs.values()) {
                        if (Util.broodWarDistance(u.getPosition(), c.getPosition()) <= 500) {
                            this.handler.enemyInBase.add(u);
                            break;
                        }
                    }
                    for (Unit c : this.handler.DBs.keySet()) {
                        if (Util.broodWarDistance(u.getPosition(), c.getPosition()) <= 200) {
                            this.handler.enemyInBase.add(u);
                            break;
                        }
                    }
                    for (Unit c : this.handler.SBs) {
                        if (Util.broodWarDistance(u.getPosition(), c.getPosition()) <= 200) {
                            this.handler.enemyInBase.add(u);
                            break;
                        }
                    }
                    for (ResearchingFacility c : this.handler.UBs) {
                        if (Util.broodWarDistance(u.getPosition(), c.getPosition()) <= 200) {
                            this.handler.enemyInBase.add(u);
                            break;
                        }
                    }
                    if (!this.handler.strat.name.equals("ProxyBBS") && !this.handler.strat.name.equals("ProxyEightRax")) {
                        for (Unit c : this.handler.MBs) {
                            if (Util.broodWarDistance(u.getPosition(), c.getPosition()) <= 200) {
                                this.handler.enemyInBase.add(u);
                                break;
                            }
                        }
                    }
                }
            }
            if (!this.handler.enemyInBase.isEmpty()) {
                /*if ((((GameState) this.handler).getArmySize() >= 50 && ((GameState) this.handler).getArmySize() / ((GameState) this.handler).enemyInBase.size() > 10)) {
                    return State.FAILURE;
                }*/
                this.handler.defense = true;
                return State.SUCCESS;
            }
            int cFrame = this.handler.frameCount;
            List<Worker> toDelete = new ArrayList<>();
            for (Worker u : this.handler.workerDefenders.keySet()) {
                if (u.getLastCommandFrame() == cFrame) continue;
                Position closestDefense;
                if (this.handler.learningManager.isNaughty()) {
                    if (!this.handler.DBs.isEmpty()) {
                        closestDefense = this.handler.DBs.keySet().iterator().next().getPosition();
                        u.move(closestDefense);
                        toDelete.add(u);
                        continue;
                    }
                }
                closestDefense = this.handler.getNearestCC(u.getPosition(), false);
                if (closestDefense != null) {
                    u.move(closestDefense);
                    toDelete.add(u);
                }
            }
            for (Worker u : toDelete) {
                u.stop(false);
                this.handler.workerDefenders.remove(u);
                this.handler.workerIdle.add(u);
            }
            for (Squad u : this.handler.sqManager.squads.values()) {
                if (u.status == Status.DEFENSE) {
                    Position closestCC = this.handler.getNearestCC(u.getSquadCenter(), false);
                    if (closestCC != null) {
                        Area squad = this.handler.bwem.getMap().getArea(u.getSquadCenter().toTilePosition());
                        Area regCC = this.handler.bwem.getMap().getArea(closestCC.toTilePosition());
                        if (squad != null && regCC != null) {
                            if (!squad.equals(regCC)) {
                                if (!this.handler.DBs.isEmpty() && this.handler.CCs.size() == 1) {
                                    u.giveMoveOrder(this.handler.DBs.keySet().iterator().next().getPosition());
                                } else {
                                    u.giveMoveOrder(Util.getClosestChokepoint(u.getSquadCenter()).getCenter().toPosition());
                                }
                                u.status = Status.IDLE;
                                u.attack = null;
                                continue;
                            }
                        }
                        u.status = Status.IDLE;
                        u.attack = null;
                        continue;
                    }
                    u.status = Status.IDLE;
                    u.attack = null;
                }
            }
            this.handler.defense = false;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}