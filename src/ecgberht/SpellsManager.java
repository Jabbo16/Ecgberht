package ecgberht;

import org.openbw.bwapi4j.unit.MobileUnit;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.ScienceVessel;
import org.openbw.bwapi4j.unit.Unit;

import java.util.Map;
import java.util.TreeMap;


public class SpellsManager {
    private Map<ScienceVessel, PlayerUnit> irradiatedUnits = new TreeMap<>();
    private Map<ScienceVessel, MobileUnit> defenseMatrixedUnits = new TreeMap<>();
    private Map<ScienceVessel, PlayerUnit> EMPedUnits = new TreeMap<>(); // TODO change when using Position instead

    void onFrameSpellManager() {
        try {
            updateIrradiated();
            updateDefenseMatrixed();
            updateEMPed();
        } catch (Exception e) {
            System.err.println("onFrameSpellManager Exception");
            e.printStackTrace();
        }
    }

    private void updateEMPed() {
        EMPedUnits.entrySet().removeIf(u -> u.getValue().getShields() <= 1 || !u.getValue().exists() || !u.getKey().exists());
    }

    private void updateDefenseMatrixed() {
        defenseMatrixedUnits.entrySet().removeIf(u -> u.getValue().isDefenseMatrixed() || !u.getValue().exists() || !u.getKey().exists());
    }

    public boolean isUnitIrradiated(Unit u) {
        return irradiatedUnits.values().contains(u);
    }

    public boolean isUnitEMPed(Unit u) {
        return EMPedUnits.values().contains(u);
    }

    public boolean isDefenseMatrixed(Unit u) {
        return defenseMatrixedUnits.values().contains(u);
    }

    private void updateIrradiated() {
        irradiatedUnits.entrySet().removeIf(u -> u.getValue().isIrradiated() || !u.getValue().exists() || !u.getKey().exists());
    }

    public void addIrradiated(ScienceVessel vessel, PlayerUnit unit) {
        irradiatedUnits.put(vessel, unit);
    }

    public void addDefenseMatrixed(ScienceVessel vessel, MobileUnit unit) {
        defenseMatrixedUnits.put(vessel, unit);
    }

    public void addEMPed(ScienceVessel vessel, PlayerUnit unit) {
        EMPedUnits.put(vessel, unit);
    }
}
