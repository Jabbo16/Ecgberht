package ecgberht;

import bwapi.Unit;

import java.util.Map;
import java.util.TreeMap;


public class SpellsManager {
    public Map<Unit, Unit> irradiatedUnits = new TreeMap<>();
    public Map<Unit, Unit> defenseMatrixedUnits = new TreeMap<>();
    public Map<Unit, Unit> EMPedUnits = new TreeMap<>(); // TODO change when using Position instead

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

    private void updateIrradiated() {
        irradiatedUnits.entrySet().removeIf(u -> u.getValue().isIrradiated() || !u.getValue().exists() || !u.getKey().exists());
    }

    private void updateDefenseMatrixed() {
        defenseMatrixedUnits.entrySet().removeIf(u -> u.getValue().isDefenseMatrixed() || !u.getValue().exists() || !u.getKey().exists());
    }

    private void updateEMPed() {
        EMPedUnits.entrySet().removeIf(u -> u.getValue().getShields() <= 1 || !u.getValue().exists() || !u.getKey().exists());
    }

    public boolean isUnitIrradiated(Unit u) {
        return irradiatedUnits.values().contains(u);
    }

    public boolean isDefenseMatrixed(Unit u) {
        return defenseMatrixedUnits.values().contains(u);
    }

    public boolean isUnitEMPed(Unit u) {
        return EMPedUnits.values().contains(u);
    }

    public void addIrradiated(Unit vessel, Unit unit) {
        irradiatedUnits.put(vessel, unit);
    }

    public void addDefenseMatrixed(Unit vessel, Unit unit) {
        defenseMatrixedUnits.put(vessel, unit);
    }

    public void addEMPed(Unit vessel, Unit unit) {
        EMPedUnits.put(vessel, unit);
    }

}
