package ecgberht.BehaviourTrees.Scouting;

import ecgberht.GameState;
import ecgberht.UnitInfo;
import ecgberht.Util.BaseLocationComparator;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.Worker;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class CheckEnemyBaseVisible extends Action {

    public CheckEnemyBaseVisible(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            List<PlayerUnit> enemies = this.handler.getGame().getUnits(this.handler.getIH().enemy());
            if (!enemies.isEmpty()) {
                for (UnitInfo u : this.handler.unitStorage.getEnemyUnits().values().stream().filter(u -> u.unitType.isBuilding()).collect(Collectors.toSet())) {
                    if (Util.broodWarDistance(this.handler.chosenScout.getPosition(), u.lastPosition) <= 500) {
                        this.handler.enemyMainBase = Util.getClosestBaseLocation(u.lastPosition);
                        this.handler.scoutSLs = new HashSet<>();
                        if (!this.handler.strat.name.equals("PlasmaWraithHell")) {
                            this.handler.chosenHarasser = (Worker) this.handler.chosenScout;
                        }
                        this.handler.chosenScout = null;
                        this.handler.getIH().sendText("!");
                        this.handler.playSound("gearthere.mp3");
                        this.handler.enemyBLs.clear();
                        this.handler.enemyBLs.addAll(this.handler.BLs);
                        this.handler.enemyBLs.sort(new BaseLocationComparator(this.handler.enemyMainBase));
                        if (this.handler.firstScout) {
                            this.handler.enemyStartBase = this.handler.enemyMainBase;
                            this.handler.enemyMainArea = this.handler.enemyStartBase.getArea();
                            this.handler.enemyNaturalBase = this.handler.enemyBLs.get(1);
                            this.handler.enemyNaturalArea = this.handler.enemyNaturalBase.getArea();
                        }
                        return State.SUCCESS;
                    }
                }
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}