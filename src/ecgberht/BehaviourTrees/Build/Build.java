package ecgberht.BehaviourTrees.Build;
import ecgberht.brain.*;

import ecgberht.GameState;
import ecgberht.Util.MutablePair;
import ecgberht.brain.BrainStatus;
import ecgberht.brain.BrainAction;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.Order;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.SCV;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class Build extends BrainAction {

    public Build(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public BrainStatus execute() {
        try {
            List<SCV> toRemove = new ArrayList<>();
            for (Entry<SCV, MutablePair<UnitType, TilePosition>> u : gameState.eco.workerBuild.entrySet()) {
                if (u.getKey().getOrder() != Order.PlaceBuilding && gameState.getGame().getBWMap().isVisible(u.getValue().second) && gameState.canAfford(u.getValue().first)) {
                    SCV chosen = u.getKey();
                    TilePosition tp = u.getValue().second;
                    UnitType type = u.getValue().first;
                    
                    boolean mineBlocking = false;
                    for (ecgberht.UnitInfo ally : gameState.unitStorage.getAllyUnits().values()) {
                        if (ally.unitType == UnitType.Terran_Vulture_Spider_Mine) {
                            int tx = ally.tileposition.getX();
                            int ty = ally.tileposition.getY();
                            if (tx >= tp.getX() && tx < tp.getX() + type.tileWidth() &&
                                ty >= tp.getY() && ty < tp.getY() + type.tileHeight()) {
                                chosen.attack(ally.unit);
                                mineBlocking = true;
                                break;
                            }
                        }
                    }
                    if (mineBlocking) continue;
                    
                    if (u.getValue().first == UnitType.Terran_Bunker) {
                        if (!chosen.build(u.getValue().second, u.getValue().first)) {
                            gameState.eco.deltaCash.first -= u.getValue().first.mineralPrice();
                            gameState.eco.deltaCash.second -= u.getValue().first.gasPrice();
                            toRemove.add(chosen);
                            chosen.stop(false);
                            gameState.workerIdle.add(chosen);
                        }
                    } else if (u.getKey().getOrder() == Order.PlayerGuard) {
                        if (Math.random() < 0.8) chosen.build(u.getValue().second, u.getValue().first);
                        else chosen.move(u.getKey().getPosition().add(new Position(32, 0)));
                    } else chosen.build(u.getValue().second, u.getValue().first);
                }
            }
            for (SCV s : toRemove) gameState.eco.workerBuild.remove(s);
            return BrainStatus.SUCCESS;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return BrainStatus.ERROR;
        }
    }
}






