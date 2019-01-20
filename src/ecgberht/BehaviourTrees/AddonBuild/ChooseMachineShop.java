package ecgberht.BehaviourTrees.AddonBuild;

import bwapi.Unit;
import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import bwapi.UnitType;

public class ChooseMachineShop extends Action {

    public ChooseMachineShop(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (gameState.getStrat().name.equals("VultureRush") && (gameState.Fs.size() < 2 || gameState.UBs.stream().anyMatch(u -> u.getType() == UnitType.Terran_Machine_Shop)))
                return State.FAILURE;
            if (gameState.getStrat().name.equals("TheNitekat") && (gameState.Fs.size() > 1 || gameState.UBs.stream().anyMatch(u -> u.getType() == UnitType.Terran_Machine_Shop)))
                return State.FAILURE;
            if (!gameState.Fs.isEmpty()) {
                for (Unit c : gameState.Fs) {
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
