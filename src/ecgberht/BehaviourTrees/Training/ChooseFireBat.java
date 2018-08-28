package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

public class ChooseFireBat extends Action {

    public ChooseFireBat(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).enemyRace != Race.Zerg) return State.FAILURE;
            if (((GameState) this.handler).UBs.isEmpty()) {
                return State.FAILURE;
            } else if (Util.countUnitTypeSelf(UnitType.Terran_Marine) >= 4) {
                for (ResearchingFacility r : ((GameState) this.handler).UBs) {
                    if (r instanceof Academy) {
                        int count = 0;
                        for (Unit u : ((GameState) this.handler).getGame().getUnits(((GameState) this.handler).getPlayer())) {
                            if (!u.exists()) continue;
                            if (u instanceof Firebat) count++;
                            if (count >= ((GameState) this.handler).maxBats) return State.FAILURE;
                        }
                        for (Barracks b : ((GameState) this.handler).MBs) {
                            if (!b.isTraining()) {
                                ((GameState) this.handler).chosenUnit = UnitType.Terran_Firebat;
                                ((GameState) this.handler).chosenBuilding = b;
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
