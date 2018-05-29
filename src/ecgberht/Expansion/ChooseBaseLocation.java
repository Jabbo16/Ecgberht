package ecgberht.Expansion;

import bwem.Base;
import ecgberht.EnemyBuilding;
import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Action;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Attacker;
import org.openbw.bwapi4j.unit.Unit;
import org.openbw.bwapi4j.unit.Worker;

import java.util.ArrayList;
import java.util.List;

public class ChooseBaseLocation extends Action {

    public ChooseBaseLocation(String name, GameHandler gh) {
        super(name, gh);

    }

    @Override
    public State execute() {
        try {
            if (((GameState) this.handler).chosenBaseLocation != null) {
                return State.SUCCESS;
            }
            TilePosition main = null;
            if (((GameState) this.handler).MainCC != null) {
                main = ((GameState) this.handler).MainCC.getTilePosition();
            } else {
                main = ((GameState) this.handler).getPlayer().getStartLocation();
            }
            List<Base> valid = new ArrayList<>();
            for (Base b : ((GameState) this.handler).BLs) {
                if (!((GameState) this.handler).CCs.containsKey(b.getArea().getTop().toPosition()) && !((GameState) this.handler).bwem.getMap().getPath(b.getLocation().toPosition(), main.toPosition()).isEmpty()) {
                    valid.add(b);
                }
            }
            List<Base> remove = new ArrayList<>();
            for (Base b : valid) {
                for (Unit u : ((GameState) this.handler).enemyCombatUnitMemory) {
                    if (((GameState) this.handler).bwem.getMap().getArea(u.getTilePosition()) == null || !(u instanceof Attacker) || u instanceof Worker) {
                        continue;
                    }
                    if (((GameState) this.handler).bwem.getMap().getArea(u.getTilePosition()).equals(b.getArea())) {
                        remove.add(b);
                        break;
                    }
                }
                for (EnemyBuilding u : ((GameState) this.handler).enemyBuildingMemory.values()) {
                    if (((GameState) this.handler).bwem.getMap().getArea(u.pos) == null) {
                        continue;
                    }
                    if (((GameState) this.handler).bwem.getMap().getArea(u.pos).equals(b.getArea())) {
                        remove.add(b);
                        break;
                    }
                }
            }
            valid.removeAll(remove);
            if (valid.isEmpty()) {
                ((GameState) this.handler).chosenBaseLocation = null;
                ((GameState) this.handler).movingToExpand = false;
                ((GameState) this.handler).chosenBuilderBL.stop(false);
                ((GameState) this.handler).workerIdle.add(((GameState) this.handler).chosenBuilderBL);
                ((GameState) this.handler).chosenBuilderBL = null;
                ((GameState) this.handler).expanding = false;
                ((GameState) this.handler).deltaCash.first -= UnitType.Terran_Command_Center.mineralPrice();
                ((GameState) this.handler).deltaCash.second -= UnitType.Terran_Command_Center.gasPrice();
                return State.FAILURE;
            }
            ((GameState) this.handler).chosenBaseLocation = valid.get(0).getLocation();
            return State.SUCCESS;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}