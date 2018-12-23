package ecgberht.BehaviourTrees.AddonBuild;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Factory;
import org.openbw.bwapi4j.unit.MachineShop;

public class ChooseMachineShop extends Action {

    public ChooseMachineShop(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if(this.handler.strat.name.equals("VultureRush") && this.handler.Fs.size() < 2 && this.handler.UBs.stream().anyMatch(u -> u instanceof MachineShop)) return State.FAILURE;
            if (!this.handler.Fs.isEmpty()) {
                for (Factory c : this.handler.Fs) {
                    if (!c.isTraining() && c.getAddon() == null) {
                        this.handler.chosenBuildingAddon = c;
                        this.handler.chosenAddon = UnitType.Terran_Machine_Shop;
                        return State.SUCCESS;
                    }
                }
            }
            this.handler.chosenBuildingAddon = null;
            this.handler.chosenAddon = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
