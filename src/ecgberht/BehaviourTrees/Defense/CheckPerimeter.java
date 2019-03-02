package ecgberht.BehaviourTrees.Defense;

import bwem.Base;
import bwem.area.Area;
import ecgberht.GameState;
import ecgberht.Squad;
import ecgberht.Squad.Status;
import ecgberht.UnitInfo;
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
            gameState.enemyInBase.clear();
            gameState.defense = false;
            Set<Unit> enemyInvaders = new TreeSet<>(gameState.enemyCombatUnitMemory);
            for (UnitInfo u : gameState.unitStorage.getEnemyUnits().values().stream().filter(u -> u.unitType.isBuilding()).collect(Collectors.toSet())) {
                if (u.unitType.canAttack() || u.unitType == UnitType.Protoss_Pylon || u.unitType.canProduce() || u.unitType.isRefinery()) {
                    enemyInvaders.add(u.unit);
                }
            }
            for (Unit u : enemyInvaders) {
                UnitType uType = u.getType();
                if (u instanceof Building || ((uType.canAttack() || uType.isSpellcaster() || u instanceof Loadable)
                        && uType != UnitType.Zerg_Scourge && uType != UnitType.Terran_Valkyrie
                        && uType != UnitType.Protoss_Corsair && !(u instanceof Overlord))) {
                    Area enemyArea = gameState.bwem.getMap().getArea(u.getTilePosition());
                    if (enemyArea != null) {
                        Area myMainArea = gameState.mainCC != null ? gameState.mainCC.first.getArea() : null;
                        Area myNatArea = gameState.naturalArea;
                        for (Base b : gameState.CCs.keySet()) {
                            if (!b.getArea().equals(enemyArea)) continue;
                            if ((myMainArea != null && !b.getArea().equals(myMainArea)
                                    && (myNatArea != null && !b.getArea().equals(myNatArea)))) continue;
                            gameState.enemyInBase.add(u);
                            break;
                        }
                    }
                    for (Map.Entry<SCV, Building> c : gameState.workerTask.entrySet()) {
                        int dist = c.getValue() instanceof CommandCenter ? 500 : 200;
                        if (Util.broodWarDistance(u.getPosition(), c.getValue().getPosition()) <= dist) {
                            gameState.enemyInBase.add(u);
                            break;
                        }
                    }
                    for (Unit c : gameState.CCs.values()) {
                        if (Util.broodWarDistance(u.getPosition(), c.getPosition()) <= 500) {
                            gameState.enemyInBase.add(u);
                            break;
                        }
                    }
                    for (Unit c : gameState.DBs.keySet()) {
                        if (Util.broodWarDistance(u.getPosition(), c.getPosition()) <= 200) {
                            gameState.enemyInBase.add(u);
                            break;
                        }
                    }
                    for (Unit c : gameState.SBs) {
                        if (Util.broodWarDistance(u.getPosition(), c.getPosition()) <= 200) {
                            gameState.enemyInBase.add(u);
                            break;
                        }
                    }
                    for (ResearchingFacility c : gameState.UBs) {
                        if (Util.broodWarDistance(u.getPosition(), c.getPosition()) <= 200) {
                            gameState.enemyInBase.add(u);
                            break;
                        }
                    }
                    if (!gameState.getStrat().name.equals("ProxyBBS") && !gameState.getStrat().name.equals("ProxyEightRax")) {
                        for (Unit c : gameState.MBs) {
                            if (Util.broodWarDistance(u.getPosition(), c.getPosition()) <= 200) {
                                gameState.enemyInBase.add(u);
                                break;
                            }
                        }
                    }
                }
            }
            if (!gameState.enemyInBase.isEmpty()) {
                /*if ((((GameState) gameState).getArmySize() >= 50 && ((GameState) gameState).getArmySize() / ((GameState) gameState).enemyInBase.size() > 10)) {
                    return State.FAILURE;
                }*/
                gameState.defense = true;
                return State.SUCCESS;
            }
            int cFrame = gameState.frameCount;
            List<Worker> toDelete = new ArrayList<>();
            for (Worker u : gameState.workerDefenders.keySet()) {
                if (u.getLastCommandFrame() == cFrame) continue;
                Position closestDefense;
                if (gameState.learningManager.isNaughty()) {
                    if (!gameState.DBs.isEmpty()) {
                        closestDefense = gameState.DBs.keySet().iterator().next().getPosition();
                        u.move(closestDefense);
                        toDelete.add(u);
                        continue;
                    }
                }
                closestDefense = gameState.getNearestCC(u.getPosition(), false);
                if (closestDefense != null) {
                    u.move(closestDefense);
                    toDelete.add(u);
                }
            }
            for (Worker u : toDelete) {
                u.stop(false);
                gameState.workerDefenders.remove(u);
                gameState.workerIdle.add(u);
            }
            for (Squad u : gameState.sqManager.squads.values()) {
                if (u.status == Status.DEFENSE) {
                    Position closestCC = gameState.getNearestCC(u.getSquadCenter(), false);
                    if (closestCC != null) {
                        Area squad = gameState.bwem.getMap().getArea(u.getSquadCenter().toTilePosition());
                        Area regCC = gameState.bwem.getMap().getArea(closestCC.toTilePosition());
                        if (squad != null && regCC != null) {
                            if (!squad.equals(regCC)) {
                                if (!gameState.DBs.isEmpty() && gameState.CCs.size() == 1) {
                                    u.giveMoveOrder(gameState.DBs.keySet().iterator().next().getPosition());
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
            gameState.defense = false;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}