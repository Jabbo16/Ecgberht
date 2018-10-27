package ecgberht.BehaviourTrees.Harass;

import ecgberht.GameState;
import ecgberht.Util.UtilMicro;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Conditional;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.*;

import java.util.Set;
import java.util.TreeSet;

public class CheckHarasserAttacked extends Conditional {
    public CheckHarasserAttacked(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (this.handler.enemyMainBase == null) {
                this.handler.chosenUnitToHarass = null;
                this.handler.chosenHarasser = null;
                return State.FAILURE;
            }
            if (this.handler.chosenUnitToHarass != null) {
                if (!this.handler.bw.getBWMap().isValidPosition(this.handler.chosenUnitToHarass.getPosition())) {
                    this.handler.chosenUnitToHarass = null;
                }
            }
            Unit attacker = null;
            int workers = 0;
            Set<Unit> attackers = new TreeSet<>();
            //Thanks to @N00byEdge for cleaner code
            for (Unit u : this.handler.enemyCombatUnitMemory) {
                if (!(u instanceof Building) && u instanceof Attacker && u.exists()) {
                    Unit target = ((Attacker) u).getTargetUnit() == null ? ((PlayerUnit) u).getOrderTarget() :
                            ((Attacker) u).getTargetUnit();
                    if (target != null && target.equals(this.handler.chosenHarasser)) {
                        if (u instanceof Worker) {
                            workers++;
                            attacker = u;
                        }
                        attackers.add(u);
                    }
                }
            }
            if (workers > 1) this.handler.EI.defendHarass = true;
            if (attackers.isEmpty()) {
                if (!this.handler.getGame().getBWMap().isVisible(this.handler.enemyMainBase.getLocation()) &&
                        this.handler.chosenUnitToHarass == null) {
                    this.handler.chosenHarasser.move(this.handler.enemyMainBase.getLocation().toPosition());
                }
                return State.SUCCESS;
            } else {
                boolean winHarass = this.handler.sim.simulateHarass(this.handler.chosenHarasser, attackers, 70);
                if (winHarass) {
                    if (workers == 1 && !attacker.equals(this.handler.chosenUnitToHarass)) {
                        this.handler.chosenHarasser.attack(attacker);
                        this.handler.chosenUnitToHarass = attacker;
                        return State.SUCCESS;
                    }
                } else {
                    if (this.handler.chosenHarasser.getHitPoints() <= 15) {
                        this.handler.workerIdle.add(this.handler.chosenHarasser);
                        this.handler.chosenHarasser.stop(false);
                        this.handler.chosenHarasser = null;
                        this.handler.chosenUnitToHarass = null;
                    } else {
                        Position kite = UtilMicro.kiteAway(this.handler.chosenHarasser, attackers);
                        if (this.handler.bw.getBWMap().isValidPosition(kite)) {
                            this.handler.chosenHarasser.move(kite);
                            this.handler.chosenUnitToHarass = null;
                        }
                    }
                    return State.FAILURE;
                }
            }
            return State.SUCCESS;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }

}
