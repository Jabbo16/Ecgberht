package ecgberht.BehaviourTrees.Build;

import bwem.Area;
import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.unit.Unit;

import java.util.Map;

public class CheckMineralWalkGoldRush extends Action {

    public CheckMineralWalkGoldRush(String name, GameState gh) {
        super(name, gh);
    }

    private MineralPatch getCloserMineral(Map.Entry<SCV, MutablePair<UnitType, TilePosition>> entry) {
        double bestDist = Double.MAX_VALUE;
        MineralPatch closerMineral = null;
        SCV scv = entry.getKey();
        Position buildTarget = entry.getValue().second.toPosition();
        for (MineralPatch mineralPatch : gameState.walkingMinerals) {
            if (!mineralPatch.isVisible() || scv.getDistance(mineralPatch) <= 32 * 4) continue;
            double dist = mineralPatch.getDistance(buildTarget) * 1.2;
            if (dist > scv.getDistance(buildTarget)) continue;
            Area mineralArea = gameState.bwem.getMap().getArea(mineralPatch.getTilePosition());
            Area workerArea = gameState.bwem.getMap().getArea(scv.getTilePosition());
            if (mineralPatch.equals(scv.getTargetUnit()) && mineralArea != null && mineralArea.equals(workerArea))
                continue;
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
            if (gameState.walkingMinerals.isEmpty()) return State.SUCCESS;
            for (Map.Entry<SCV, MutablePair<UnitType, TilePosition>> u : gameState.workerBuild.entrySet()) {
                if (u.getValue().first != UnitType.Terran_Command_Center) continue;
                SCV scv = u.getKey();
                Unit movingMineral = u.getKey().getTargetUnit();
                if (movingMineral == null) {
                    MineralPatch target = getCloserMineral(u);
                    if (target == null) {
                        if (scv.getOrderTarget() != null && scv.getOrder() != Order.Move) {
                            scv.move(u.getValue().second.toPosition());
                        }
                        continue;
                    }
                    if (!target.equals(scv.getTargetUnit()))
                        scv.rightClick(target, false);
                } else if (scv.getDistance(movingMineral) < 32) {
                    scv.move(u.getValue().second.toPosition());
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
