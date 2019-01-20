package ecgberht.BehaviourTrees.Build;

import bwapi.TilePosition;
import bwapi.Unit;
import bwem.Base;
import bwem.area.Area;
import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.UnitType;

import java.util.Map.Entry;

public class WorkerWalkBuild extends Action {

    public WorkerWalkBuild(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            for (Entry<Unit, MutablePair<UnitType, TilePosition>> u : gameState.workerBuild.entrySet()) {
                Unit chosen = u.getKey();
                if (u.getValue().first != UnitType.Terran_Command_Center
                        || gameState.bw.isVisible(u.getValue().second)
                        || !gameState.fortressSpecialBLsTiles.contains(u.getValue().second))
                    continue;
                Base myBase = Util.getClosestBaseLocation(u.getValue().second.toPosition());
                MutablePair<Unit, Unit> minerals = gameState.fortressSpecialBLs.get(myBase);
                Area scvArea = gameState.bwem.getMap().getArea(chosen.getTilePosition());
                if (!u.getValue().second.equals(new TilePosition(7, 118))) {
                    if (scvArea != null && scvArea.equals(myBase.getArea())) {
                        if (chosen.getDistance(minerals.first) > 3 * 32) {
                            chosen.move(u.getValue().second.toPosition());
                            continue;
                        }
                        if (minerals.second.isVisible()) {
                            chosen.gather(minerals.second);
                            continue;
                        }
                    }
                    if (minerals.second.isVisible()) {
                        chosen.gather(minerals.second);
                        continue;
                    }
                    if (minerals.first.isVisible()) {
                        chosen.gather(minerals.first);
                    }
                } else { // Weird logic :/
                    if (scvArea != null && scvArea.equals(myBase.getArea())) {
                        if (chosen.getDistance(minerals.first) > 3 * 32) {
                            chosen.move(u.getValue().second.toPosition());
                            continue;
                        } else {
                            chosen.gather(minerals.second);
                            continue;
                        }
                    }
                    chosen.gather(minerals.first);
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
