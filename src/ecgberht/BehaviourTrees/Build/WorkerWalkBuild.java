package ecgberht.BehaviourTrees.Build;

import bwem.Base;
import bwem.area.Area;
import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.MineralPatch;
import org.openbw.bwapi4j.unit.SCV;

import java.util.Map.Entry;

public class WorkerWalkBuild extends Action {

    public WorkerWalkBuild(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            for (Entry<SCV, MutablePair<UnitType, TilePosition>> u : this.handler.workerBuild.entrySet()) {
                SCV chosen = u.getKey();
                if (u.getValue().first != UnitType.Terran_Command_Center
                        || this.handler.getGame().getBWMap().isVisible(u.getValue().second)
                        || !this.handler.fortressSpecialBLsTiles.contains(u.getValue().second))
                    continue;
                Base myBase = Util.getClosestBaseLocation(u.getValue().second.toPosition());
                MutablePair<MineralPatch, MineralPatch> minerals = this.handler.fortressSpecialBLs.get(myBase);
                Area scvArea = this.handler.bwem.getMap().getArea(chosen.getTilePosition());
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
