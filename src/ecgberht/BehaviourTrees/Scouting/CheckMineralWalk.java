package ecgberht.BehaviourTrees.Scouting;

import bwem.Area;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.unit.MineralPatch;

public class CheckMineralWalk extends Action {

    public CheckMineralWalk(String name, GameState gh) {
        super(name, gh);
    }

    private MineralPatch movingMineral = null;

    private MineralPatch getCloserMineral() {
        double bestDist = Double.MAX_VALUE;
        MineralPatch closerMineral = null;
        for (MineralPatch mineralPatch : gameState.walkingMinerals) {
            if (!mineralPatch.isVisible() || gameState.chosenScout.getDistance(mineralPatch) <= 32 * 4) continue;
            double dist = mineralPatch.getDistance(gameState.scoutSLs.iterator().next().getLocation().toPosition());
            if (dist > gameState.chosenScout.getDistance(gameState.scoutSLs.iterator().next().getLocation().toPosition()))
                continue;
            Area mineralArea = gameState.bwem.getMap().getArea(mineralPatch.getTilePosition());
            Area workerArea = gameState.bwem.getMap().getArea(gameState.chosenScout.getTilePosition());
            if (mineralPatch.equals(movingMineral) && mineralArea != null && mineralArea.equals(workerArea)) continue;
            if (dist < bestDist) {
                bestDist = dist;
                closerMineral = mineralPatch;
            }
        }
        return closerMineral;
    }

    @Override
    public State execute() {
        try {
            if (gameState.chosenScout == null) return State.FAILURE;
            if (gameState.walkingMinerals.isEmpty() || gameState.scoutSLs.isEmpty()) return State.SUCCESS;
            if (movingMineral == null) {
                MineralPatch target = getCloserMineral();
                if (target == null) {
                    if (gameState.chosenScout.getOrderTarget() != null)
                        gameState.chosenScout.move(gameState.scoutSLs.iterator().next().getLocation().toPosition());
                    return State.SUCCESS;
                }
                movingMineral = target;
                gameState.chosenScout.rightClick(target, false);
                return State.FAILURE;
            } else if (gameState.chosenScout.getDistance(movingMineral) < 32) {
                gameState.chosenScout.move(gameState.scoutSLs.iterator().next().getLocation().toPosition());
                movingMineral = null;
            }
            return State.SUCCESS;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
