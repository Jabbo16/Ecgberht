package ecgberht.BehaviourTrees.Harass;

import bwem.Base;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.type.Order;

public class Explore extends Conditional {

    public Explore(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() { // TODO improve
        try {
            if (((GameState) this.handler).enemyMainBase == null) {
                ((GameState) this.handler).chosenUnitToHarass = null;
                ((GameState) this.handler).chosenHarasser = null;
                return State.FAILURE;
            }
            if (((GameState) this.handler).directionScoutMain == 0) ((GameState) this.handler).directionScoutMain = 1;
            /*Base enemyMainBase = ((GameState) this.handler).enemyMainBase;
            Area enemyArea = ((GameState) this.handler).bwem.getMap().getArea(enemyMainBase.getLocation());
            Area harasserArea = ((GameState) this.handler).bwem.getMap().getArea(((GameState) this.handler).chosenHarasser.getTilePosition());
            if (harasserArea != null && enemyArea != null && !harasserArea.equals(enemyArea)) {
                ((GameState) this.handler).chosenHarasser.move(((GameState) this.handler).enemyMainBase.getLocation().toPosition());
                return State.SUCCESS;
            }*/
            Position nextExplorePos = chooseExplorePos();
            if (((GameState) this.handler).chosenHarasser.getDistance(nextExplorePos) > 2 * 32 || ((GameState) this.handler).chosenHarasser.getOrder() == Order.PlayerGuard) {
                ((GameState) this.handler).chosenHarasser.move(nextExplorePos);
                return State.SUCCESS;
            }
            ((GameState) this.handler).directionScoutMain++;
            if (((GameState) this.handler).directionScoutMain == 5) ((GameState) this.handler).directionScoutMain = 1;
            return State.SUCCESS;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }

    private Position chooseExplorePos() {
        // Logic borrowed from LetaBot, should be improved/replaced
        // Credits to Martin Rooijackers
        int BaseSize = 10 * 32;
        int Xlocation = 0;
        int Ylocation = 0;
        Base enemyBase = ((GameState) this.handler).enemyMainBase;
        if (((GameState) this.handler).directionScoutMain == 1) {
            Xlocation = enemyBase.getLocation().toPosition().getX() + BaseSize;
            Ylocation = enemyBase.getLocation().toPosition().getY() + BaseSize;
        } else if (((GameState) this.handler).directionScoutMain == 2) {
            Xlocation = enemyBase.getLocation().toPosition().getX() - BaseSize;
            Ylocation = enemyBase.getLocation().toPosition().getY() + BaseSize;
        } else if (((GameState) this.handler).directionScoutMain == 3) {
            Xlocation = enemyBase.getLocation().toPosition().getX() - BaseSize;
            Ylocation = enemyBase.getLocation().toPosition().getY() - BaseSize;
        } else if (((GameState) this.handler).directionScoutMain == 4) {
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
