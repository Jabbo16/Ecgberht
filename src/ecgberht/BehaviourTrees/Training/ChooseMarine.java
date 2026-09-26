package ecgberht.BehaviourTrees.Training;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.Util.Util;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Barracks;


public class ChooseMarine extends BrainAction {

    public ChooseMarine(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            if (!gameState.tech.MBs.isEmpty()) {
                int multiplier = 2;
                String strat = gameState.getStrat().name;
                Player self = gameState.getPlayer();
                if (strat.equals("FullMech") || strat.equals("MechGreedyFE") || strat.equals("VultureRush"))
                    multiplier = 15;
                if (!gameState.tech.Fs.isEmpty() && (self.isResearching(TechType.Tank_Siege_Mode) || self.hasResearched(TechType.Tank_Siege_Mode)) && self.gas() >= UnitType.Terran_Siege_Tank_Tank_Mode.gasPrice() && self.minerals() <= 200) {
                    if (Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Siege_Mode) + Util.countUnitTypeSelf(UnitType.Terran_Siege_Tank_Tank_Mode) < Util.countUnitTypeSelf(UnitType.Terran_Marine) * multiplier) {
                        return BrainStatus.FAILURE;
                    }
                }
                if ((strat.equals("FullMech") || strat.equals("MechGreedyFE") || strat.equals("2PortWraith"))
                        && Util.countUnitTypeSelf(UnitType.Terran_Marine) > (gameState.enemyRace == Race.Zerg ? 4 : 2) && !gameState.mil.defense)
                    return BrainStatus.FAILURE;
                if (strat.equals("VultureRush") && Util.countUnitTypeSelf(UnitType.Terran_Marine) > 2 && !gameState.mil.defense)
                    return BrainStatus.FAILURE;
                if (strat.equals("JoyORush") && Util.countBuildingAll(UnitType.Terran_Factory) < 2)
                    return BrainStatus.FAILURE;
                for (Barracks b : gameState.tech.MBs) {
                    if (!b.isTraining()) {
                        gameState.chosenUnit = UnitType.Terran_Marine;
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






