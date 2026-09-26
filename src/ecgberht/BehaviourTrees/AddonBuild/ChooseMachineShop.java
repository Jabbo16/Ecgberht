package ecgberht.BehaviourTrees.AddonBuild;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Factory;
import org.openbw.bwapi4j.unit.MachineShop;

public class ChooseMachineShop extends BrainAction {

    public ChooseMachineShop(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.getStrat().name.equals("VultureRush") && (gameState.tech.Fs.size() < 2 || gameState.tech.UBs.stream().anyMatch(u -> u instanceof MachineShop)))
                return BrainStatus.FAILURE;
            if (gameState.getStrat().name.equals("TheNitekat") && (gameState.tech.Fs.size() > 1 || gameState.tech.UBs.stream().anyMatch(u -> u instanceof MachineShop)))
                return BrainStatus.FAILURE;
            if (!gameState.tech.Fs.isEmpty()) {
                for (Factory c : gameState.tech.Fs) {
                    if (!c.isTraining() && c.getAddon() == null) {
                        gameState.tech.chosenBuildingAddon = c;
                        gameState.chosenAddon = UnitType.Terran_Machine_Shop;
                        return BrainStatus.SUCCESS;
                    }
                }
            }
            gameState.tech.chosenBuildingAddon = null;
            gameState.chosenAddon = null;
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}






