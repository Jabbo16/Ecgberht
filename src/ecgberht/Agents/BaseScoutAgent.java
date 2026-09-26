package ecgberht.Agents;

import ecgberht.Simulation.SimInfo;
import ecgberht.UnitInfo;
import ecgberht.Util.UtilMicro;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.Unit;

import static ecgberht.Ecgberht.getGs;

public class BaseScoutAgent extends Agent {
    private SimInfo mySim;
    
    public BaseScoutAgent(Unit unit) {
        super(unit);
    }
    
    @Override
    public boolean runAgent() {
        try {
            if (!myUnit.exists() || unitInfo == null) return true;
            
            mySim = getGs().sim.getSimulation(unitInfo, SimInfo.SimType.GROUND);
            
            if (!mySim.enemies.isEmpty()) {
                // Enemies nearby, try to retreat
                status = Status.RETREAT;
            } else {
                status = Status.SCOUT;
            }
            
            if (status == Status.RETREAT) {
                Position CC = getGs().getNearestCC(myUnit.getPosition(), true);
                if (CC != null) UtilMicro.move((org.openbw.bwapi4j.unit.MobileUnit)myUnit, CC);
                else UtilMicro.move((org.openbw.bwapi4j.unit.MobileUnit)myUnit, getGs().getPlayer().getStartLocation().toPosition());
                return false;
            } else if (status == Status.SCOUT) {
                java.util.List<ecgberht.BaseManager.Garrison> bases = getGs().baseManager.getScoutingBasesSorted();
                if (bases.isEmpty()) {
                    UtilMicro.move((org.openbw.bwapi4j.unit.MobileUnit)myUnit, getGs().mapCenter);
                    return false;
                }
                
                ecgberht.BaseManager.Garrison targetGarrison = bases.get(0);
                if (getGs().getGame().getBWMap().isVisible(targetGarrison.tile) || myUnit.getDistance(targetGarrison.tile.toPosition()) < 300) {
                    if (bases.size() > 1) {
                        targetGarrison = bases.get(1);
                    } else {
                        UtilMicro.move((org.openbw.bwapi4j.unit.MobileUnit)myUnit, getGs().mapCenter);
                        return false;
                    }
                }
                UtilMicro.move((org.openbw.bwapi4j.unit.MobileUnit)myUnit, targetGarrison.tile.toPosition());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
