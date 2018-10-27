package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

public class ChooseFireBat extends Action {

    public ChooseFireBat(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (this.handler.enemyRace != Race.Zerg) return State.FAILURE;
            if (this.handler.UBs.isEmpty()) {
                return State.FAILURE;
            } else if (Util.countUnitTypeSelf(UnitType.Terran_Marine) >= 4) {
                for (ResearchingFacility r : this.handler.UBs) {
                    if (r instanceof Academy) {
                        int count = 0;
                        for (Unit u : this.handler.getGame().getUnits(this.handler.getPlayer())) {
                            if (!u.exists()) continue;
                            if (u instanceof Firebat) count++;
                            if (count >= this.handler.maxBats) return State.FAILURE;
                        }
                        for (Barracks b : this.handler.MBs) {
                            if (!b.isTraining()) {
                                this.handler.chosenUnit = UnitType.Terran_Firebat;
                                this.handler.chosenBuilding = b;
                                return State.SUCCESS;
                            }
                        }
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
