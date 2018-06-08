package ecgberht.Scouting;

import ecgberht.EnemyBuilding;
import ecgberht.GameState;
import ecgberht.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.unit.PlayerUnit;

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
                for (EnemyBuilding u : ((GameState) this.handler).enemyBuildingMemory.values()) {
                    if (((GameState) this.handler).broodWarDistance(((GameState) this.handler).chosenScout.getPosition(), u.pos.toPosition()) <= 500) {
                        ((GameState) this.handler).enemyBase = Util.getClosestBaseLocation(u.pos.toPosition());
                        ((GameState) this.handler).ScoutSLs = new HashSet<>();
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
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}