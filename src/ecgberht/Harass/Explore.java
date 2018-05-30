package ecgberht.Harass;

import bwem.Base;
import bwem.area.Area;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.Attacker;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.Unit;

import java.util.Set;
import java.util.TreeSet;

public class Explore extends Conditional {

    public Explore(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() { // TODO improve
        try {
            if (((GameState) this.handler).directionScoutMain == 0) {
                ((GameState) this.handler).directionScoutMain = 1;
            }
            Base enemyBase = ((GameState) this.handler).enemyBase;
            Area enemyArea = ((GameState) this.handler).bwem.getMap().getArea(enemyBase.getLocation());
            Area harasserArea = ((GameState) this.handler).bwem.getMap().getArea(((GameState) this.handler).chosenHarasser.getTilePosition());
            if (harasserArea != null && enemyArea != null && !harasserArea.equals(enemyArea)) {
                ((GameState) this.handler).chosenHarasser.move(((GameState) this.handler).enemyBase.getLocation().toPosition());
                return State.SUCCESS;
            }
            /*Set<Unit> attackers = new TreeSet<>();
            //Thanks to @N00byEdge for cleaner code
            for (Unit u : ((GameState) this.handler).enemyCombatUnitMemory) {
                if (!(u instanceof Building) && u instanceof Attacker && u.exists()) {
                    Unit target = ((Attacker) u).getTargetUnit() == null ? ((PlayerUnit)u).getOrderTarget() :
                            ((Attacker) u).getTargetUnit();
                    if (target != null && target.equals(((GameState) this.handler).chosenHarasser)) {
                        attackers.add(u);
                    }
                }
            }*/
            Position nextExplorePos = chooseExplorePos();
            if (((GameState) this.handler).chosenHarasser.getDistance(nextExplorePos) > 2 * 32) {
                ((GameState) this.handler).chosenHarasser.move(nextExplorePos);
                return State.SUCCESS;
            } else {
                ((GameState) this.handler).directionScoutMain++;
                if (((GameState) this.handler).directionScoutMain == 5) {
                    ((GameState) this.handler).directionScoutMain = 1;
                }
            }
            return State.SUCCESS;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }

    private Position chooseExplorePos() {
        // Logic borrowed from LetaBot,
        int BaseSize = 10 * 32;
        int Xlocation = 0;
        int Ylocation = 0;
        Base enemyBase = ((GameState) this.handler).enemyBase;
        if (((GameState) this.handler).directionScoutMain == 1) {
            Xlocation = enemyBase.getLocation().toPosition().getX() + BaseSize;
            Ylocation = enemyBase.getLocation().toPosition().getY() + BaseSize;
        }
        if (((GameState) this.handler).directionScoutMain == 2) {
            Xlocation = enemyBase.getLocation().toPosition().getX() - BaseSize;
            Ylocation = enemyBase.getLocation().toPosition().getY() + BaseSize;
        }
        if (((GameState) this.handler).directionScoutMain == 3) {
            Xlocation = enemyBase.getLocation().toPosition().getX() - BaseSize;
            Ylocation = enemyBase.getLocation().toPosition().getY() - BaseSize;
        }
        if (((GameState) this.handler).directionScoutMain == 4) {
            Xlocation = enemyBase.getLocation().toPosition().getX() + BaseSize;
            Ylocation = enemyBase.getLocation().toPosition().getY() - BaseSize;
        }
        if (Xlocation < 0) Xlocation = 1;
        if (Ylocation < 0) Ylocation = 1;
        if (Xlocation > ((GameState) this.handler).getGame().getBWMap().mapWidth() * 32) {
            Xlocation = ((GameState) this.handler).getGame().getBWMap().mapWidth() * 32 - 1;
        }
        if (Ylocation > ((GameState) this.handler).getGame().getBWMap().mapHeight() * 32) {
            Ylocation = ((GameState) this.handler).getGame().getBWMap().mapHeight() * 32 - 1;
        }
        return new Position(Xlocation, Ylocation);
    }
}
