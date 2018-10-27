package ecgberht.BehaviourTrees.AddonBuild;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Academy;
import org.openbw.bwapi4j.unit.CommandCenter;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseComsatStation extends Action {

    public ChooseComsatStation(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            if (!this.handler.CCs.isEmpty()) {
                for (CommandCenter c : this.handler.CCs.values()) {
                    if (!c.isTraining() && c.getAddon() == null) {
                        for (ResearchingFacility u : this.handler.UBs) {
                            if (u instanceof Academy) {
                                this.handler.chosenBuildingAddon = c;
                                this.handler.chosenAddon = UnitType.Terran_Comsat_Station;
                                return BehavioralTree.State.SUCCESS;
                            }
                        }

                    }
                }
            }
            this.handler.chosenBuildingAddon = null;
            this.handler.chosenAddon = null;
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}
