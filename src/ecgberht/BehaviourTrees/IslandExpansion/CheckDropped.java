package ecgberht.BehaviourTrees.IslandExpansion;

import ecgberht.Agents.DropShipAgent;
import ecgberht.GameState;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.*;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.unit.Worker;


public class CheckDropped extends BrainAction {

    public CheckDropped(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            Worker scv = gameState.chosenWorkerDrop;
            DropShipAgent ship = gameState.chosenDropShip;
            if (ship == null) return BrainStatus.SUCCESS;
            if (scv != null && ship.statusToString().equals("RETREAT")) {
                Unit transport = scv.getTransport();
                if (transport == null) {
                    gameState.chosenDropShip = null;
                    return BrainStatus.SUCCESS;
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

