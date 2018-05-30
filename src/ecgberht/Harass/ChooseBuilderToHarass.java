package ecgberht.Harass;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.unit.SCV;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.unit.Worker;

public class ChooseBuilderToHarass extends Action {

    public ChooseBuilderToHarass(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).enemyRace != Race.Terran) {
                return State.FAILURE;
            }
            /*if (((GameState) this.handler).chosenUnitToHarass != null && ((GameState) this.handler).chosenUnitToHarass instanceof Worker) {
                return State.FAILURE;
            }*/
            for (Unit u : ((GameState) this.handler).enemyCombatUnitMemory) {
                Unit aux = null;
                if (((GameState) this.handler).enemyBase != null) {
                    if (u instanceof SCV && ((SCV) u).isConstructing()) {
                        if (((GameState) this.handler).bwem.getMap().getArea(u.getTilePosition()).equals(((GameState) this.handler).bwem.getMap().getArea(((GameState) this.handler).enemyBase.getLocation()))) {
                            if (((SCV) u).getBuildType().canProduce()) {
                                ((GameState) this.handler).chosenUnitToHarass = u;
                                return State.SUCCESS;
                            }
                            aux = u;
                        }
                        if (aux != null) {
                            ((GameState) this.handler).chosenUnitToHarass = aux;
                            return State.SUCCESS;
                        }
                    }
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
