package ecgberht.BehaviourTrees.Harass;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.unit.Unit;

public class ChooseBuilderToHarass extends Action {

    public ChooseBuilderToHarass(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (this.handler.enemyRace != Race.Terran) {
                return State.FAILURE;
            }
            /*if (((GameState) this.handler).chosenUnitToHarass != null && ((GameState) this.handler).chosenUnitToHarass instanceof Worker) {
                return State.FAILURE;
            }*/
            for (Unit u : this.handler.enemyCombatUnitMemory) {
                Unit aux = null;
                if (this.handler.enemyMainBase != null) {
                    if (u instanceof SCV && ((SCV) u).isConstructing()) {
                        if (this.handler.bwem.getMap().getArea(u.getTilePosition()).equals(this.handler.bwem.getMap().getArea(this.handler.enemyMainBase.getLocation()))) {
                            if (((SCV) u).getBuildType().canProduce()) {
                                this.handler.chosenUnitToHarass = u;
                                return State.SUCCESS;
                            }
                            aux = u;
                        }
                        if (aux != null) {
                            this.handler.chosenUnitToHarass = aux;
                            return State.SUCCESS;
                        }
                    }
                }
            }
            this.handler.chosenUnitToHarass = null;
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
