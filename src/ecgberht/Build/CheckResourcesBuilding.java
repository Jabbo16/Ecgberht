package ecgberht.Build;

import ecgberht.GameState;
import org.iaie.btree.state.State;
import org.iaie.btree.task.leaf.Conditional;
import org.iaie.btree.util.GameHandler;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.Worker;
import org.openbw.bwapi4j.util.Pair;

public class CheckResourcesBuilding extends Conditional {

    public CheckResourcesBuilding(String name, GameHandler gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            Pair<Integer, Integer> cash = ((GameState) this.handler).getCash();
            Worker chosen = ((GameState) this.handler).chosenWorker;
            TilePosition start = chosen.getTilePosition();
            TilePosition end = ((GameState) this.handler).chosenPosition;
            Position realEnd = ((GameState) this.handler).getCenterFromBuilding(end.toPosition(), ((GameState) this.handler).chosenToBuild);

            if (((GameState) this.handler).strat.name.equals("ProxyBBS") && ((GameState) this.handler).chosenToBuild == UnitType.Terran_Barracks) {
                if (((GameState) this.handler).countUnit(UnitType.Terran_Barracks) < 1) {
                    // TODO Improve
                    if (cash.first + ((GameState) this.handler).getMineralsWhenReaching(start, realEnd.toTilePosition()) >= (((GameState) this.handler).chosenToBuild.mineralPrice() * 2 + 40 + ((GameState) this.handler).deltaCash.first) && cash.second >= (((GameState) this.handler).chosenToBuild.gasPrice() * 2) + ((GameState) this.handler).deltaCash.second) {
                        return State.SUCCESS;
                    }
                } else if (((GameState) this.handler).countUnit(UnitType.Terran_Barracks) == 1) {
//                    if (cash.first + ((GameState) this.handler).getMineralsWhenReaching(chosen, start, realEnd.toTilePosition()) >= (((GameState) this.handler).chosenToBuild.mineralPrice() + ((GameState) this.handler).deltaCash.first) && cash.second >= (((GameState) this.handler).chosenToBuild.gasPrice()) + ((GameState) this.handler).deltaCash.second) {
                    return State.SUCCESS;
//                    }
                }
                return State.ERROR;
            } else if (cash.first + ((GameState) this.handler).getMineralsWhenReaching(start, realEnd.toTilePosition()) >= (((GameState) this.handler).chosenToBuild.mineralPrice() + ((GameState) this.handler).deltaCash.first) && cash.second >= (((GameState) this.handler).chosenToBuild.gasPrice()) + ((GameState) this.handler).deltaCash.second) {
                return State.SUCCESS;
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
