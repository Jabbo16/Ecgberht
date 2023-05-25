package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.TrainingFacility;

public class TrainUnit extends Action {
    public TrainUnit(String name, GameState gamestate) {
        super(name, gamestate);
    }
    @Override
    public State execute() {
        try {
            if (gameState.chosenUnit == UnitType.None) return State.FAILURE;
            TrainingFacility currentFacility = gameState.chosenTrainingFacility;
            StateProxy proxy = StateProxyFactory.getStateProxy(currentFacility, gameState);	
            proxy.checkProxyStrategy(gameState);	   
            final int conditionForTrain_supply = 4;	
            final int conditionForTrain_totalSupply = 400;	
			boolean gtFourSupplies = gameState.getSupply() > conditionForTrain_supply;	
			boolean gtTotalFourHundredSupplies = gameState.getPlayer().supplyTotal() >= conditionForTrain_totalSupply;	
			if (gtFourSupplies || gameState.checkSupply() || gtTotalFourHundredSupplies) {
                final boolean gameStateNotDefense = !gameState.defense;	
				final boolean chooseToBuildCommandCenter = gameState.chosenToBuild == UnitType.Terran_Command_Center;	
				if (gameStateNotDefense && chooseToBuildCommandCenter) {
                    boolean found = checkCommandCenter(); 
                    if (!found) {
                    	gameState.chosenTrainingFacility = null;
                		gameState.chosenToBuild = UnitType.None;
                		return State.FAILURE;
                    }
                }
                currentFacility.train(gameState.chosenUnit);	
                return State.SUCCESS;
            }
            return State.FAILURE;
            
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
	public boolean checkCommandCenter() {
		boolean found = false;
		for (MutablePair<UnitType, TilePosition> pairOfUnitTile : gameState.workerBuild.values()) {	
		    if (pairOfUnitTile.first == UnitType.Terran_Command_Center) {
		        found = true;
		        break;
		    }
		}
		return found;
	}
}
