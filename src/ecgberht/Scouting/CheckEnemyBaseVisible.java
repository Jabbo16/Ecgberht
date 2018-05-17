package ecgberht.Scouting;

import ecgberht.GameState;
import ecgberht.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.Unit;

import java.util.HashSet;
import java.util.List;

//import ecgberht.BaseLocationComparator;

public class CheckEnemyBaseVisible extends Action {

    public CheckEnemyBaseVisible(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            List<PlayerUnit> enemies = ((GameState) this.handler).getGame().getUnits(((GameState) this.handler).getIH().enemy());
            if (!enemies.isEmpty()) {
                for (Unit u : enemies) {
                    if (u instanceof Building) {
                        if (((GameState) this.handler).broodWarDistance(((GameState) this.handler).chosenScout.getPosition(), u.getPosition()) <= 500) {
                            ((GameState) this.handler).enemyBase = Util.getClosestBaseLocation(u.getPosition());
                            ((GameState) this.handler).ScoutSLs = new HashSet<>();
                            //((GameState)this.handler).choosenScout.stop();
                            //((GameState)this.handler).workerIdle.add(((GameState)this.handler).choosenScout);
                            ((GameState) this.handler).chosenHarasser = ((GameState) this.handler).chosenScout;
                            ((GameState) this.handler).chosenScout = null;
                            ((GameState) this.handler).getIH().sendText("!");
                            ((GameState) this.handler).playSound("gear.mp3");
                            ((GameState) this.handler).EnemyBLs.clear();
                            ((GameState) this.handler).EnemyBLs.addAll(((GameState) this.handler).BLs);
                            //((GameState)this.handler).EnemyBLs.sort(new BaseLocationComparator(true));
                            return State.SUCCESS;
                        }
                    }
                }
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            System.err.println(e);
            return State.ERROR;
        }
    }
}