package ecgberht.BehaviourTrees.Scanner;

import bwem.Base;
import ecgberht.GameState;
import ecgberht.UnitStorage;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;
import org.openbw.bwapi4j.unit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CheckScan extends Conditional {

    public CheckScan(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (this.handler.CSs.isEmpty()) return State.FAILURE;
            if (this.handler.frameCount - this.handler.startCount > 40 + this.handler.getIH().getLatency()) {
                for (UnitStorage.UnitInfo u : this.handler.unitStorage.getEnemyUnits().values()) {
                    PlayerUnit pU = u.unit;
                    if ((pU.isCloaked() || (pU instanceof Burrowable && u.burrowed)) && !pU.isDetected() && u.unit instanceof Attacker) {
                        if (this.handler.sim.getSimulation(u, true).allies.isEmpty()) continue;
                        this.handler.checkScan = u.tileposition;
                        return State.SUCCESS;
                    }
                }
            }
            List<Base> valid = new ArrayList<>();
            for (Base b : this.handler.enemyBLs) {
                if (this.handler.getGame().getBWMap().isVisible(b.getLocation()) || b.getArea().getAccessibleNeighbors().isEmpty()) {
                    continue;
                }
                if (this.handler.enemyMainBase != null && this.handler.enemyMainBase.getLocation().equals(b.getLocation())) {
                    continue;
                }
                valid.add(b);
            }
            if (valid.isEmpty()) return State.FAILURE;
            for (ComsatStation u : this.handler.CSs) {
                if (u.getEnergy() == 200) {
                    Random random = new Random();
                    this.handler.checkScan = valid.get(random.nextInt(valid.size())).getLocation();
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
