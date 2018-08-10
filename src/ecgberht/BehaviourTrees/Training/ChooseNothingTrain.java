package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Academy;
import org.openbw.bwapi4j.unit.Factory;
import org.openbw.bwapi4j.unit.MachineShop;

public class ChooseNothingTrain extends Action {

    public ChooseNothingTrain(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            // Bio Builds and no Stim
            boolean stim = ((GameState) this.handler).strat.techToResearch.contains(TechType.Stim_Packs);
            if (stim && !((GameState) this.handler).getPlayer().hasResearched(TechType.Stim_Packs) &&
                    !((GameState) this.handler).getPlayer().isResearching(TechType.Stim_Packs)
                    && (int) ((GameState) this.handler).UBs.stream().filter(u -> u instanceof Academy).count() >= 1) {
                ((GameState) this.handler).chosenUnit = UnitType.None;
                return State.SUCCESS;
            }
            // Mech builds and no siege
            boolean siege = ((GameState) this.handler).strat.techToResearch.contains(TechType.Tank_Siege_Mode);
            if (siege && ((GameState) this.handler).CCs.size() >= 2 &&
                    !((GameState) this.handler).getPlayer().hasResearched(TechType.Tank_Siege_Mode) &&
                    !((GameState) this.handler).getPlayer().isResearching(TechType.Tank_Siege_Mode) &&
                    checkSiege()) {
                ((GameState) this.handler).chosenUnit = UnitType.None;
                return State.SUCCESS;
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }

    private boolean checkSiege() {
        boolean machineShop = false;
        if (((GameState) this.handler).Fs.isEmpty()) return false;
        int mS = (int) ((GameState) this.handler).UBs.stream().filter(u -> u instanceof MachineShop).count();
        if (mS == 0) {
            for (Factory f : ((GameState) this.handler).Fs) {
                if (f.getMachineShop() != null) {
                    machineShop = true;
                    break;
                }
            }
            return !machineShop;
        }
        return mS >= 1;
    }
}
