package ecgberht.BehaviourTrees.Scanner;

import bwem.Base;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.unit.*;

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
            if (((GameState) this.handler).frameCount - ((GameState) this.handler).startCount > 40 + ((GameState) this.handler).getIH().getLatency()) {
                for (Unit u : ((GameState) this.handler).enemyCombatUnitMemory) {
                    PlayerUnit pU = (PlayerUnit) u;
                    if ((pU.isCloaked() || (pU instanceof Burrowable && ((Burrowable) pU).isBurrowed())) && !pU.isDetected() && u instanceof Attacker) {
                        if (((GameState) this.handler).sim.getSimulation(u, true).allies.isEmpty()) continue;
                        ((GameState) this.handler).checkScan = u.getTilePosition();
                        return State.SUCCESS;
                    }
                }
            }
            List<Base> valid = new ArrayList<>();
            for (Base b : ((GameState) this.handler).enemyBLs) {
                if (((GameState) this.handler).getGame().getBWMap().isVisible(b.getLocation()) || b.getArea().getAccessibleNeighbors().isEmpty()) {
                    continue;
                }
                if (((GameState) this.handler).enemyMainBase != null) {
                    if (((GameState) this.handler).enemyMainBase.getLocation().equals(b.getLocation())) {
                        continue;
                    }
                }
                valid.add(b);
            }
            if (valid.isEmpty()) {
                return State.FAILURE;
            }
            for (ComsatStation u : ((GameState) this.handler).CSs) {
                if (u.getEnergy() == 200) {
                    Random random = new Random();
                    ((GameState) this.handler).checkScan = valid.get(random.nextInt(valid.size())).getLocation();
                    return State.SUCCESS;
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
