package ecgberht.Agents;

import ecgberht.Simulation.SimInfo;
import ecgberht.UnitInfo;
import ecgberht.Util.UtilMicro;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.Unit;

import static ecgberht.Ecgberht.getGs;

public class BaseScoutAgent extends Agent {
    private static int currentBaseScoutIndex = 0;
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
                if (getGs().BLs.isEmpty()) return false;
                
                bwem.Base targetBase = getGs().BLs.get(currentBaseScoutIndex);
                int checks = 0;
                
                while (checks < getGs().BLs.size() && (myUnit.getDistance(targetBase.getLocation().toPosition()) < 300 || getGs().bw.getBWMap().isVisible(targetBase.getLocation()))) {
                    currentBaseScoutIndex = (currentBaseScoutIndex + 1) % getGs().BLs.size();
                    targetBase = getGs().BLs.get(currentBaseScoutIndex);
                    checks++;
                }
                
                if (checks < getGs().BLs.size()) {
                    UtilMicro.move((org.openbw.bwapi4j.unit.MobileUnit)myUnit, targetBase.getLocation().toPosition());
                } else {
                    // All bases visible, just move to center
                    UtilMicro.move((org.openbw.bwapi4j.unit.MobileUnit)myUnit, getGs().mapCenter);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
