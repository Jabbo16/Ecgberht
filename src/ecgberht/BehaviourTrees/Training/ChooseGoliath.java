package ecgberht.BehaviourTrees.Training;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Armory;
import org.openbw.bwapi4j.unit.Factory;
import org.openbw.bwapi4j.unit.Unit;

public class ChooseGoliath extends BrainAction {

    public ChooseGoliath(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (gameState.tech.UBs.stream().filter(unit -> unit instanceof Armory).count() < 1) return BrainStatus.FAILURE;
            int count = 0;
            for (Unit u : gameState.getGame().getUnits(gameState.getPlayer())) {
                if (!u.exists()) continue;
                if (u.getType() == UnitType.Terran_Goliath) count++;
                if (count >= gameState.maxGoliaths) return BrainStatus.FAILURE;
            }
            if (!gameState.tech.Fs.isEmpty()) {
                for (Factory b : gameState.tech.Fs) {
                    if (!b.isTraining() && b.canTrain(UnitType.Terran_Goliath)) {
                        gameState.chosenUnit = UnitType.Terran_Goliath;
                        gameState.chosenTrainingFacility = b;
                        return BrainStatus.SUCCESS;
                    }
                }
            }
            return BrainStatus.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}






