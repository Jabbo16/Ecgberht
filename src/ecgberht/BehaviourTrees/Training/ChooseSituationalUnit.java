package ecgberht.BehaviourTrees.Training;

import ecgberht.GameState;
import ecgberht.Util.Util;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;


public class ChooseSituationalUnit extends Action {

    public ChooseSituationalUnit(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            // Testing dropships islands
            boolean dropship = true;
            if (!((GameState) this.handler).islandBases.isEmpty()) {
                for (Unit u : ((GameState) this.handler).getGame().getUnits(((GameState) this.handler).getPlayer())) {
                    if (!u.exists()) continue;
                    if (u instanceof Dropship) {
                        dropship = false;
                        break;
                    }
                }
            } else dropship = false;

            boolean tower = false;
            if (dropship && !((GameState) this.handler).strat.name.equals("2PortWraith")) {
                for (ResearchingFacility u : ((GameState) this.handler).UBs) {
                    if (u instanceof ControlTower) {
                        tower = true;
                        break;
                    }
                }
                if (!tower) return State.FAILURE;
                for (Starport s : ((GameState) this.handler).Ps) {
                    if (s.getAddon() != null && s.getAddon().isCompleted() && !s.isTraining()) {
                        ((GameState) this.handler).chosenUnit = UnitType.Terran_Dropship;
                        ((GameState) this.handler).chosenBuilding = s;
                        return State.SUCCESS;
                    }
                }
            }
            // Testing dropships offensive drops
            /*if (Util.countUnitTypeSelf(UnitType.Terran_Dropship) > 0) return State.FAILURE;

            for (ResearchingFacility u : ((GameState) this.handler).UBs) {
                if (u instanceof ControlTower) {
                    tower = true;
                    break;
                }
            }
            if (!tower) return State.FAILURE;
            for (Starport s : ((GameState) this.handler).Ps) {
                if (s.getAddon() != null && s.getAddon().isCompleted() && !s.isTraining()) {
                    ((GameState) this.handler).chosenUnit = UnitType.Terran_Dropship;
                    ((GameState) this.handler).chosenBuilding = s;
                    return State.SUCCESS;
                }
            }*/

            // Testing vessels
            if (Util.countUnitTypeSelf(UnitType.Terran_Science_Vessel) > 2 || ((GameState) this.handler).workerMining.isEmpty())
                return State.FAILURE;
            if (Util.countUnitTypeSelf(UnitType.Terran_Science_Vessel) > 0 && !((GameState) this.handler).needToAttack())
                return State.FAILURE;
            String strat = ((GameState) this.handler).strat.name;
            if (strat.equals("FullMech") || strat.equals("MechGreedyFE") && Util.getNumberCCs() < 3)
                return State.FAILURE;
            tower = false;
            boolean science = false;
            for (ResearchingFacility u : ((GameState) this.handler).UBs) {
                if (u instanceof ControlTower) tower = true;
                else if (u instanceof ScienceFacility) science = true;
                if (science && tower) break;
            }
            if (!tower || !science) return State.FAILURE;
            for (Starport s : ((GameState) this.handler).Ps) {
                if (s.getAddon() != null && s.getAddon().isCompleted() && !s.isTraining()) {
                    if (((GameState) this.handler).getCash().second < UnitType.Terran_Science_Vessel.gasPrice()
                            && ((GameState) this.handler).getCash().first >= UnitType.Terran_Science_Vessel.mineralPrice() + 50) {
                        for (Barracks b : ((GameState) this.handler).MBs) {
                            if (!b.isTraining()) {
                                ((GameState) this.handler).chosenUnit = UnitType.Terran_Marine;
                                ((GameState) this.handler).chosenBuilding = b;
                                return State.SUCCESS;
                            }
                        }
                    }

                    ((GameState) this.handler).chosenUnit = UnitType.Terran_Science_Vessel;
                    ((GameState) this.handler).chosenBuilding = s;
                    return State.SUCCESS;
                }
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
