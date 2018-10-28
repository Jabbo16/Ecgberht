package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Building;
import org.openbw.bwapi4j.unit.SupplyDepot;

public class ChooseSupply extends Action {

    public ChooseSupply(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (this.handler.getPlayer().supplyTotal() >= 400) return State.FAILURE;
            if (this.handler.strat.name.equals("ProxyBBS") && Util.countBuildingAll(UnitType.Terran_Barracks) < 2) {
                return State.FAILURE;
            }
            if (this.handler.strat.name.equals("EightRax") && Util.countBuildingAll(UnitType.Terran_Barracks) < 1) {
                return State.FAILURE;
            }
            if (this.handler.learningManager.isNaughty() && this.handler.enemyRace == Race.Zerg
                    && Util.countBuildingAll(UnitType.Terran_Barracks) < 1) {
                return State.FAILURE;
            }
            if (this.handler.learningManager.isNaughty() && this.handler.enemyRace == Race.Zerg
                    && Util.countBuildingAll(UnitType.Terran_Barracks) == 1
                    && Util.countBuildingAll(UnitType.Terran_Supply_Depot) > 0
                    && Util.countBuildingAll(UnitType.Terran_Bunker) < 1) {
                return State.FAILURE;
            }
            if (this.handler.getSupply() <= 4 * this.handler.getCombatUnitsBuildings()) {
                for (MutablePair<UnitType, TilePosition> w : this.handler.workerBuild.values()) {
                    if (w.first == UnitType.Terran_Supply_Depot) return State.FAILURE;
                }
                for (Building w : this.handler.workerTask.values()) {
                    if (w instanceof SupplyDepot) return State.FAILURE;
                }
                this.handler.chosenToBuild = UnitType.Terran_Supply_Depot;
                return State.SUCCESS;
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
