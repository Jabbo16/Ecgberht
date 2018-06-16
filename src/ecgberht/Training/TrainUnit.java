package ecgberht.Training;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.MachineShop;
import org.openbw.bwapi4j.unit.ResearchingFacility;
import org.openbw.bwapi4j.unit.TrainingFacility;
import org.openbw.bwapi4j.util.Pair;

public class TrainUnit extends Action {

    public TrainUnit(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).strat.name == "BioMech") {
                boolean mShop = false;
                for (ResearchingFacility u : ((GameState) this.handler).UBs) {
                    if (u instanceof MachineShop) {
                        mShop = true;
                        break;
                    }
                }
                if (!((GameState) this.handler).Fs.isEmpty() && mShop && (!((GameState) this.handler).getPlayer().isResearching(TechType.Tank_Siege_Mode) &&
                        !((GameState) this.handler).getPlayer().hasResearched(TechType.Tank_Siege_Mode)) && ((GameState) this.handler).vulturesTrained >= 2) {
                    return State.FAILURE;
                }
            }
            TrainingFacility chosen = ((GameState) this.handler).chosenBuilding;
            if (((GameState) this.handler).strat.name == "ProxyBBS") {
                if (((GameState) this.handler).countUnit(UnitType.Terran_Barracks) == 2 &&
                        ((GameState) this.handler).countUnit(UnitType.Terran_Supply_Depot) == 0) return State.FAILURE;
                if (((GameState) this.handler).getSupply() > 0) {
                    chosen.train(((GameState) this.handler).chosenUnit);
                    return State.SUCCESS;
                }
            }

            if (((GameState) this.handler).getSupply() > 4 || ((GameState) this.handler).checkSupply() ||
                    ((GameState) this.handler).getPlayer().supplyTotal() >= 400) {
                /*if (((GameState) this.handler).EI.naughty) { // TODO test
                    if (((GameState) this.handler).MBs.isEmpty() && ((GameState) this.handler).countUnit(UnitType.Terran_Bunker) == 0) {
                        if (((GameState) this.handler).getPlayer().minerals() + ((GameState) this.handler).deltaCash.first < 100) {
                            return State.FAILURE;
                        }
                    }
                }*/
                if (!((GameState) this.handler).defense) {
                    if (((GameState) this.handler).chosenToBuild == UnitType.Terran_Command_Center) {
                        boolean found = false;
                        for (Pair<UnitType, TilePosition> w : ((GameState) this.handler).workerBuild.values()) {
                            if (w.first == UnitType.Terran_Command_Center) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) return State.FAILURE;
                    }
                }
                chosen.train(((GameState) this.handler).chosenUnit);
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
