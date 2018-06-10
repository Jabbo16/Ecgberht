package ecgberht.Scanner;

import bwapi.Unit;
import bwta.BaseLocation;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CheckScan extends Conditional {

    public CheckScan(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).CSs.isEmpty()) {
                return State.FAILURE;
            }
            if (((GameState) this.handler).getGame().elapsedTime() - ((GameState) this.handler).startCount > 1) {
                for (Unit u : ((GameState) this.handler).enemyCombatUnitMemory) {
                    if ((u.isCloaked() || u.isBurrowed()) && !u.isDetected() && u.getType().canAttack()) {
                        ((GameState) this.handler).checkScan = u.getTilePosition();
                        return State.SUCCESS;
                    }
                }
            }
            List<BaseLocation> valid = new ArrayList<>();
            for (BaseLocation b : ((GameState) this.handler).EnemyBLs) {
                if (((GameState) this.handler).getGame().isVisible(b.getTilePosition()) || b.isIsland()) {
                    continue;
                }
                if (((GameState) this.handler).enemyBase != null) {
                    if (((GameState) this.handler).enemyBase.getTilePosition().equals(b.getTilePosition())) {
                        continue;
                    }
                }
                valid.add(b);
            }
            if (valid.isEmpty()) {
                return State.FAILURE;
            }
            for (Unit u : ((GameState) this.handler).CSs) {
                if (u.getEnergy() == 200) {
                    Random random = new Random();
                    ((GameState) this.handler).checkScan = valid.get(random.nextInt(valid.size())).getTilePosition();
                    return State.SUCCESS;
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
