package ecgberht.BehaviourTrees.Training;

import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.TrainingFacility;

import ecgberht.GameState;
import ecgberht.Util.Util;

public class StateProxyFactory {
	public static StateProxy getStateProxy(TrainingFacility currentFacility, GameState gameState) {
		StateProxy proxy = null;
		final int barraksForEightRax = 0;
		boolean gt16_Supplies = gameState.supplyMan.getSupplyUsed() >= 16;	
		final int barraksForBBS = 2;	
		boolean noSupplyDepot = Util.countBuildingAll(UnitType.Terran_Supply_Depot) == 0;	
		final String bbs = "ProxyBBS";
		final String eightRax = "ProxyEightRax";
		String strategyName = gameState.getStrategyFromManager().name;
		switch(strategyName){
		case bbs: proxy = new StateProxyBBS(currentFacility,barraksForBBS,noSupplyDepot,gameState);
		break;
		case eightRax : proxy = new StateProxyEightRax(currentFacility,barraksForEightRax,gt16_Supplies,gameState);
		break;
		}
		return proxy;
	}
}
