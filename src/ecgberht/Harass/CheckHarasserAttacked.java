package ecgberht.Harass;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.*;

import java.util.Set;
import java.util.TreeSet;

import static ecgberht.Ecgberht.getGs;

public class CheckHarasserAttacked extends Conditional {
    public CheckHarasserAttacked(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).chosenUnitToHarass != null) {
                if (!((GameState) this.handler).bw.getBWMap().isValidPosition(((GameState) this.handler).chosenUnitToHarass.getPosition())) {
                    ((GameState) this.handler).chosenUnitToHarass = null;
                }
            }
            Unit attacker = null;
            int workers = 0;
            Set<Unit> attackers = new TreeSet<>();
            //Thanks to @N00byEdge for cleaner code
            for (PlayerUnit u : ((GameState) this.handler).getGame().getUnits(((GameState) this.handler).getIH().enemy())) {
                if (!(u instanceof Building) && u instanceof Attacker && u.exists()) {
                    Unit target = ((Attacker) u).getTargetUnit() == null ? u.getOrderTarget() : ((Attacker) u).getTargetUnit();
                    if (target != null && target.equals(((GameState) this.handler).chosenHarasser)) {
                        if (u instanceof Worker) {
                            workers++;
                            attacker = u;
                        }
                        attackers.add(u);
                        continue;
                    }
                }
            }
            if (workers > 1) {
                ((GameState) this.handler).EI.defendHarass = true;
            }
            if (attackers.isEmpty()) {
                if (!((GameState) this.handler).getGame().getBWMap().isVisible(((GameState) this.handler).enemyBase.getLocation()) && ((GameState) this.handler).chosenUnitToHarass == null) {
                    ((GameState) this.handler).chosenHarasser.move(((GameState) this.handler).enemyBase.getLocation().toPosition());
                }
                return State.SUCCESS;
            } else {
                boolean winHarass = ((GameState) this.handler).simulateHarass(((GameState) this.handler).chosenHarasser, attackers, 70);
                if (winHarass) {
                    if (workers == 1 && !attacker.equals(((GameState) this.handler).chosenUnitToHarass)) {
                        ((GameState) this.handler).chosenHarasser.attack(attacker);
                        ((GameState) this.handler).chosenUnitToHarass = attacker;
                        return State.SUCCESS;
                    }
                } else {
                    if (((GameState) this.handler).chosenHarasser.getHitPoints() <= 15) {
                        ((GameState) this.handler).getIH().sendText("Harasser: You will pay for this!, I will be back with friends");
                        ((GameState) this.handler).workerIdle.add(((GameState) this.handler).chosenHarasser);
                        ((GameState) this.handler).chosenHarasser.stop(winHarass);
                        ((GameState) this.handler).chosenHarasser = null;
                        ((GameState) this.handler).chosenUnitToHarass = null;
                    } else {
                        Position kite = getGs().kiteAway(((GameState) this.handler).chosenHarasser, attackers);
                        if (((GameState) this.handler).bw.getBWMap().isValidPosition(kite)) {
                            ((GameState) this.handler).chosenHarasser.move(kite);
                            ((GameState) this.handler).chosenUnitToHarass = null;
                        }

                    }

                    return State.FAILURE;
                }
            }

            return State.SUCCESS;
        } catch (Exception e) {
            //System.err.println(this.getClass().getSimpleName());
            //e.printStackTrace();
            return State.ERROR;
        }
    }

}
