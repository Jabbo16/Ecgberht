package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Academy;
import org.openbw.bwapi4j.unit.Barracks;
import org.openbw.bwapi4j.unit.ResearchingFacility;
import org.openbw.bwapi4j.unit.Unit;

import java.util.Set;


public class ChooseMedic extends Action {

    public ChooseMedic(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BehavioralTree.State execute() {
        try {
            if (this.handler.UBs.isEmpty()) {
                return BehavioralTree.State.FAILURE;
            } else {
                for (ResearchingFacility u : this.handler.UBs) {
                    if (u instanceof Academy) {
                        int marine_count = 0;
                        if (!this.handler.DBs.isEmpty()) {
                            for (Set<Unit> p : this.handler.DBs.values()) marine_count += p.size();
                        }
                        if (!this.handler.MBs.isEmpty() && Util.countUnitTypeSelf(UnitType.Terran_Medic) * 4 < Util.countUnitTypeSelf(UnitType.Terran_Marine) - marine_count) {
                            for (Barracks b : this.handler.MBs) {
                                if (!b.isTraining()) {
                                    this.handler.chosenUnit = UnitType.Terran_Medic;
                                    this.handler.chosenBuilding = b;
                                    return BehavioralTree.State.SUCCESS;
                                }
                            }
                        }
                        break;
                    }
                }
            }
            return BehavioralTree.State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BehavioralTree.State.ERROR;
        }
    }
}
