package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import ecgberht.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Academy;
import org.openbw.bwapi4j.unit.Barracks;
import org.openbw.bwapi4j.unit.ResearchingFacility;
import org.openbw.bwapi4j.unit.Unit;

import java.util.Set;


public class ChooseMedic extends Action {

    public ChooseMedic(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).UBs.isEmpty()) {
                return State.FAILURE;
            } else {
                for (ResearchingFacility u : ((GameState) this.handler).UBs) {
                    if (u instanceof Academy) {
                        int marine_count = 0;
                        if (!((GameState) this.handler).DBs.isEmpty()) {
                            for (Set<Unit> p : ((GameState) this.handler).DBs.values()) {
                                marine_count += p.size();
                            }
                        }
                        if (!((GameState) this.handler).MBs.isEmpty() && Util.countUnitTypeSelf(UnitType.Terran_Medic) * 4 < Util.countUnitTypeSelf(UnitType.Terran_Marine) - marine_count) {
                            for (Barracks b : ((GameState) this.handler).MBs) {
                                if (!b.isTraining()) {
                                    ((GameState) this.handler).chosenUnit = UnitType.Terran_Medic;
                                    ((GameState) this.handler).chosenBuilding = b;
                                    return State.SUCCESS;
                                }
                            }
                        }
                        break;
                    }
                }
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
