package ecgberht.BehaviourTrees.Defense;

import bwapi.Unit;
import bwem.Base;
import bwem.Area;
import ecgberht.GameState;
import ecgberht.Squad;
import ecgberht.Squad.Status;
import ecgberht.UnitInfo;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;
import bwapi.Position;
import bwapi.UnitType;

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
            Set<UnitInfo> enemyInvaders = new TreeSet<>();
            for (UnitInfo u : gameState.unitStorage.getEnemyUnits().values().stream().filter(u -> u.unitType.isBuilding()).collect(Collectors.toSet())) {
                if (u.unitType.canAttack() || u.unitType == UnitType.Protoss_Pylon || u.unitType.canProduce() || u.unitType.isRefinery()) {
                    enemyInvaders.add(u);
                }
            }
            for (UnitInfo u : enemyInvaders) {
                if(!u.unit.exists()) continue;
                UnitType uType = u.unitType;
                if (uType.isBuilding() || ((uType.canAttack() || uType.isSpellcaster() || uType.spaceProvided() > 0)
                        && uType != UnitType.Zerg_Scourge && uType != UnitType.Terran_Valkyrie
                        && uType != UnitType.Protoss_Corsair && uType != UnitType.Zerg_Overlord)) {
                    Area enemyArea = gameState.bwem.getMap().getArea(u.tileposition);
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
                    for (Map.Entry<Unit, Unit> c : gameState.workerTask.entrySet()) {
                        int dist = c.getValue().getType() == UnitType.Terran_Command_Center ? 500 : 200;
                        if (Util.broodWarDistance(u.position, c.getValue().getPosition()) <= dist) {
                            gameState.enemyInBase.add(u);
                            break;
                        }
                    }
                    for (Unit c : gameState.CCs.values()) {
                        if (Util.broodWarDistance(u.position, c.getPosition()) <= 500) {
                            gameState.enemyInBase.add(u);
                            break;
                        }
                    }
                    for (Unit c : gameState.DBs.keySet()) {
                        if (Util.broodWarDistance(u.position, c.getPosition()) <= 200) {
                            gameState.enemyInBase.add(u);
                            break;
                        }
                    }
                    for (Unit c : gameState.SBs) {
                        if (Util.broodWarDistance(u.position, c.getPosition()) <= 200) {
                            gameState.enemyInBase.add(u);
                            break;
                        }
                    }
                    for (Unit c : gameState.UBs) {
                        if (Util.broodWarDistance(u.position, c.getPosition()) <= 200) {
                            gameState.enemyInBase.add(u);
                            break;
                        }
                    }
                    if (!gameState.getStrat().name.equals("ProxyBBS") && !gameState.getStrat().name.equals("ProxyEightRax")) {
                        for (Unit c : gameState.MBs) {
                            if (Util.broodWarDistance(u.position, c.getPosition()) <= 200) {
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
            List<Unit> toDelete = new ArrayList<>();
            for (Unit u : gameState.workerDefenders.keySet()) {
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
            for (Unit u : toDelete) {
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