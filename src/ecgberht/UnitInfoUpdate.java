package ecgberht;

import bwem.util.Utils;
import ecgberht.Util.Util;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.unit.*;

import static ecgberht.Ecgberht.getGs;

public class UnitInfoUpdate {

    private UnitInfoUpdate() {
        throw new IllegalStateException("Utility class");
    }

    public static void updatePlayer(UnitInfo unitInfo) {
        unitInfo.player = unitInfo.unit.getPlayer();
    }

    public static void updateUnitType(UnitInfo unitInfo) {
        unitInfo.unitType = unitInfo.unit.getType();
    }

    public static void updateVisibility(UnitInfo unitInfo) {
        unitInfo.visible = unitInfo.unit.isVisible();
    }

    public static void updatePosition(UnitInfo unitInfo) {
        unitInfo.position = unitInfo.unit.getPosition();
    }

    public static void updateOrder(UnitInfo unitInfo) {
        unitInfo.currentOrder = unitInfo.unit.getOrder();
    }

    public static void updateCompletion(UnitInfo unitInfo) {
        unitInfo.completed = unitInfo.unit.isCompleted();
    }

    public static void updateTilePosition(UnitInfo unitInfo) {
        unitInfo.tileposition = unitInfo.unit.getTilePosition();
    }

    public static void updateWalkPosition(UnitInfo unitInfo) {
        if(!unitInfo.unitType.isBuilding()){
            unitInfo.walkposition = new Position(unitInfo.unit.getLeft(), unitInfo.unit.getTop()).toWalkPosition();
        }else unitInfo.walkposition = unitInfo.tileposition.toWalkPosition();
    }

    public static void updateLastPositions(UnitInfo unitInfo) {
        if (unitInfo.visible) {
            unitInfo.lastPosition = unitInfo.position;
            unitInfo.lastTileposition = unitInfo.tileposition;
            unitInfo.lastWalkposition = unitInfo.walkposition;
        }
    }

    public static void updateFrames(UnitInfo unitInfo) {
        unitInfo.lastVisibleFrame = unitInfo.visible ? getGs().frameCount : unitInfo.lastVisibleFrame;
        unitInfo.lastAttackFrame = unitInfo.unit.isStartingAttack() ? getGs().frameCount : unitInfo.lastVisibleFrame;
    }

    public static void updateRanges(UnitInfo unitInfo) {
        if (unitInfo.unit instanceof GroundAttacker) {
            unitInfo.groundRange = unitInfo.player.getUnitStatCalculator().weaponMaxRange(unitInfo.unitType.groundWeapon());
        }
        if (unitInfo.unit instanceof AirAttacker) {
            unitInfo.airRange = unitInfo.player.getUnitStatCalculator().weaponMaxRange(unitInfo.unitType.airWeapon());
        }
        if (unitInfo.unit instanceof Bunker) {
            unitInfo.airRange = 5 * 32;
            unitInfo.groundRange = 5 * 32;
        }
    }

    public static void updateHealth(UnitInfo unitInfo) {
        unitInfo.health = unitInfo.visible ? unitInfo.unit.getHitPoints() : UnitInfoCalculations.getExpectedHealth(unitInfo);
        unitInfo.percentHealth = unitInfo.unitType.maxHitPoints() > 0 ? (double) unitInfo.health / (double) unitInfo.unitType.maxHitPoints() : 1.0;
    }

    public static void updateShields(UnitInfo unitInfo) {
        unitInfo.shields = unitInfo.visible ? unitInfo.unit.getShields() : UnitInfoCalculations.getExpectedShields(unitInfo);
        unitInfo.percentShield = unitInfo.unitType.maxShields() > 0 ? (double) unitInfo.shields / (double) unitInfo.unitType.maxShields() : 1.0;
    }

    public static void updateEnergy(UnitInfo unitInfo) {
        if (unitInfo.unit instanceof SpellCaster) {
            unitInfo.energy = ((SpellCaster) unitInfo.unit).getEnergy();
        }
    }

    public static void updateBurrowed(UnitInfo unitInfo) {
        if (unitInfo.visible && unitInfo.unit instanceof Burrowable) {
            unitInfo.burrowed = ((Burrowable) unitInfo.unit).isBurrowed();
        }
    }

    public static void updateFlying(UnitInfo unitInfo) {
        if (unitInfo.visible) {
            unitInfo.flying = unitInfo.unit.isFlying();
        }
    }

    public static void updateSpeed(UnitInfo unitInfo) {
        unitInfo.speed = Util.getSpeed(unitInfo);
    }

    public static void updateTarget(UnitInfo unitInfo) {
        unitInfo.target = unitInfo.unit instanceof Attacker ? ((Attacker) unitInfo.unit).getTargetUnit() :  unitInfo.unit.getOrderTarget();
    }

    public static void clearAttackers(UnitInfo unitInfo) {
        unitInfo.attackers.clear();
    }

}
