package ecgberht.BehaviourTrees.Build;

import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Academy;
import org.openbw.bwapi4j.unit.Factory;
import org.openbw.bwapi4j.unit.MachineShop;

public class ChooseNothingBuilding extends Action {

    public ChooseNothingBuilding(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            // Bio Builds and no Stim
            boolean stim = this.handler.strat.techToResearch.contains(TechType.Stim_Packs);
            if (stim && !this.handler.getPlayer().hasResearched(TechType.Stim_Packs) &&
                    !this.handler.getPlayer().isResearching(TechType.Stim_Packs)
                    && (int) this.handler.UBs.stream().filter(u -> u instanceof Academy).count() >= 1) {
                this.handler.chosenToBuild = UnitType.None;
                return State.SUCCESS;
            }
            // Mech builds and no siege
            boolean siege = this.handler.strat.techToResearch.contains(TechType.Tank_Siege_Mode);
            if (siege && Util.getNumberCCs() >= 2 &&
                    !this.handler.getPlayer().hasResearched(TechType.Tank_Siege_Mode) &&
                    !this.handler.getPlayer().isResearching(TechType.Tank_Siege_Mode) &&
                    checkSiege()) {
                this.handler.chosenToBuild = UnitType.None;
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
        if (this.handler.Fs.isEmpty()) return false;
        int mS = (int) this.handler.UBs.stream().filter(u -> u instanceof MachineShop).count();
        if (mS == 0) {
            for (Factory f : this.handler.Fs) {
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
