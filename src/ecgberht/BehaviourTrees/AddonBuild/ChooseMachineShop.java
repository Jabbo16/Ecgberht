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
            if (gameState.strat.name.equals("VultureRush") && (gameState.Fs.size() < 2 || gameState.UBs.stream().anyMatch(u -> u instanceof MachineShop)))
                return State.FAILURE;
            if (gameState.strat.name.equals("TheNitekat") && (gameState.Fs.size() > 1 || gameState.UBs.stream().anyMatch(u -> u instanceof MachineShop)))
                return State.FAILURE;
            if (!gameState.Fs.isEmpty()) {
                for (Factory c : gameState.Fs) {
                    if (!c.isTraining() && c.getAddon() == null) {
                        gameState.chosenBuildingAddon = c;
                        gameState.chosenAddon = UnitType.Terran_Machine_Shop;
                        return State.SUCCESS;
                    }
                }
            }
            gameState.chosenBuildingAddon = null;
            gameState.chosenAddon = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
