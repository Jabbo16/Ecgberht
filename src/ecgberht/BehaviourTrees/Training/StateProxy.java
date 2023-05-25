package ecgberht.BehaviourTrees.Training;

import org.iaie.btree.BehavioralTree.State;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.TrainingFacility;

import ecgberht.GameState;
import ecgberht.Util.Util;

public abstract class StateProxy {
	protected TrainingFacility facility;
	protected int numberOfBarraks;
	protected boolean condition;
	protected GameState gameState;
	public StateProxy(TrainingFacility currentFacility, int numberOfBarraks, boolean condition ,GameState gameState){
		this.facility =currentFacility;
		this.numberOfBarraks = numberOfBarraks;
		this.condition = condition;
		this.gameState = gameState;
	}
	protected State checkProxyStrategy(GameState gameState) {
		{
			if (Util.countBuildingAll(UnitType.Terran_Barracks) == numberOfBarraks && condition) {
			    trainFail(gameState);	
			}
				if (gameState.getSupply() > 0) {
				    facility.train(gameState.chosenUnit);
				    return State.SUCCESS;
				}
		
			return State.FAILURE;
		}
	}
	protected State trainFail(GameState gameState) {
		gameState.chosenToBuild = UnitType.None;
		return State.FAILURE;
	}
}
