package cameraModule;

import org.openbw.bwapi4j.BW;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

import java.util.List;

public class CameraModule {

    private Position myStartLocation;
    private int scrWidth = 640; //Default width ChaosLauncher with WMODE plugin
    private int scrHeight = 410; //Default height ChaosLauncher with WMODE plugin, tweaked a bit to improve real center camera location
    private int cameraMoveTime = 150;
    private int cameraMoveTimeMin = 50;
    private int watchScoutWorkerUntil = 7500;
    private int lastMoved = 0;
    private int lastMovedPriority = 0;
    private Position currentCameraPosition;
    private Position cameraFocusPosition;
    private Unit cameraFocusUnit = null;
    private boolean followUnit = false;
    private boolean enabled = false;
    private BW game;
    private Player self;

    /*
     * Constructor for the CameraModule object
     * Receives the player starting Position and the Game object
     */
    public CameraModule(TilePosition startPos, BW game) {
        myStartLocation = startPos.toPosition();
        cameraFocusPosition = startPos.toPosition();
        currentCameraPosition = startPos.toPosition();
        this.game = game;
        self = game.getInteractionHandler().self();
    }

    public void onFrame() {
        if (enabled) {
            moveCameraFallingNuke();
            moveCameraIsUnderAttack();
            moveCameraIsAttacking();
            if (game.getInteractionHandler().getFrameCount() <= watchScoutWorkerUntil) moveCameraScoutWorker();
            moveCameraArmy();
            moveCameraDrop();
            updateCameraPosition();
        }
    }

    private boolean isNearStartLocation(Position pos) {
        int distance = 1000;
        List<TilePosition> startLocations = game.getBWMap().getStartPositions();
        for (TilePosition it : startLocations) {
            Position startLocation = it.toPosition();
            // if the start position is not our own home, and the start position is closer than distance
            if (!isNearOwnStartLocation(startLocation) && startLocation.getDistance(pos) <= distance) return true;
        }
        return false;
    }

    private boolean isNearOwnStartLocation(Position pos) {
        int distance = 10 * TilePosition.SIZE_IN_PIXELS; // 10*32
        return (myStartLocation.getDistance(pos) <= distance);
    }

    private boolean isArmyUnit(Unit unit) {
        return !(unit.getType().isWorker() || unit.getType().isBuilding()
                || unit.getType() == UnitType.Terran_Vulture_Spider_Mine || unit.getType() == UnitType.Zerg_Overlord
                || unit.getType() == UnitType.Zerg_Larva);
    }

    private boolean shouldMoveCamera(int priority) {
        boolean isTimeToMove = game.getInteractionHandler().getFrameCount() - lastMoved >= cameraMoveTime;
        boolean isTimeToMoveIfHigherPrio = game.getInteractionHandler().getFrameCount() - lastMoved >= cameraMoveTimeMin;
        boolean isHigherPrio = lastMovedPriority < priority;
        // camera should move IF: enough time has passed OR (minimum time has passed AND new prio is higher)
        return isTimeToMove || (isHigherPrio && isTimeToMoveIfHigherPrio);
    }

    private void moveCamera(Position pos, int priority) {
        if (!shouldMoveCamera(priority)) return;
        // don't register a camera move if the position is the same
        if (!followUnit && cameraFocusPosition == pos) return;
        cameraFocusPosition = pos;
        lastMoved = game.getInteractionHandler().getFrameCount();
        lastMovedPriority = priority;
        followUnit = false;
    }

    private void moveCamera(Unit unit, int priority) {
        if (!shouldMoveCamera(priority)) return;
        // don't register a camera move if we follow the same unit
        if (followUnit && cameraFocusUnit == unit) return;
        cameraFocusUnit = unit;
        lastMoved = game.getInteractionHandler().getFrameCount();
        lastMovedPriority = priority;
        followUnit = true;
    }

    private void moveCameraIsAttacking() {
        int prio = 3;
        if (!shouldMoveCamera(prio)) return;
        for (PlayerUnit unit : game.getUnits(self)) {
            if (unit.isAttacking()) moveCamera(unit, prio);
        }

    }

    private void moveCameraIsUnderAttack() {
        int prio = 3;
        if (!shouldMoveCamera(prio)) return;
        for (PlayerUnit unit : game.getUnits(self)) {
            if (unit.isUnderAttack()) moveCamera(unit, prio);
        }
    }

    private void moveCameraScoutWorker() {
        int highPrio = 2;
        int lowPrio = 0;
        if (!shouldMoveCamera(lowPrio)) return;
        for (PlayerUnit unit : game.getUnits(self)) {
            if (!unit.exists() || !(unit instanceof Worker) || !unit.isCompleted()) continue;
            if (isNearStartLocation(unit.getPosition())) moveCamera(unit, highPrio);
            else if (!isNearOwnStartLocation(unit.getPosition())) moveCamera(unit, lowPrio);
        }
    }

    private void moveCameraFallingNuke() {
        int prio = 5;
        if (!shouldMoveCamera(prio)) return;
        for (Unit unit : game.getAllUnits()) {
            if (unit instanceof NuclearMissile && ((NuclearMissile) unit).getVelocityY() > 0) {
                moveCamera(unit, prio);
                return;
            }
        }
    }

    public void moveCameraNukeDetect(Position target) {
        int prio = 4;
        if (shouldMoveCamera(prio)) moveCamera(target, prio);
    }

    private void moveCameraDrop() {
        int prio = 2;
        if (!shouldMoveCamera(prio)) return;
        for (PlayerUnit unit : game.getUnits(self)) {
            if (!unit.exists() || unit.isCompleted()) continue;
            if (unit instanceof Transporter && isNearStartLocation(unit.getPosition()) && ((Transporter) unit).getLoadedUnits().size() > 0) {
                moveCamera(unit, prio);
            }
        }

    }

    private void moveCameraArmy() {
        int prio = 1;
        if (!shouldMoveCamera(prio)) return;
        // Double loop, check if army units are close to each other
        int radius = 50;
        Unit bestPosUnit = null;
        int mostUnitsNearby = 0;
        for (Unit unit1 : game.getAllUnits()) {
            if (!unit1.exists() || !(unit1 instanceof PlayerUnit) || !isArmyUnit(unit1)) continue;
            int nrUnitsNearby = 0;
            for (Unit unit2 : unit1.getUnitsInRadius(radius, game.getAllUnits())) {
                if (!unit2.exists() || !(unit2 instanceof PlayerUnit) || !isArmyUnit(unit2)) continue;
                nrUnitsNearby++;
            }
            if (nrUnitsNearby > mostUnitsNearby) {
                mostUnitsNearby = nrUnitsNearby;
                bestPosUnit = unit1;
            }
        }
        if (mostUnitsNearby > 1) moveCamera(bestPosUnit, prio);
    }

    public void moveCameraUnitCompleted(Unit unit) {
        if (enabled) {
            int prio = 1;
            if (shouldMoveCamera(prio) && unit instanceof PlayerUnit && ((PlayerUnit) unit).getPlayer().equals(self)
                    && !(unit instanceof Worker)) {
                moveCamera(unit, prio);
            }
        }
    }

    private void updateCameraPosition() {
        double moveFactor = 0.1;
        if (followUnit && game.getBWMap().isValidPosition(cameraFocusUnit.getPosition())) {
            cameraFocusPosition = cameraFocusUnit.getPosition();
        }
        currentCameraPosition = new Position(currentCameraPosition.getX() + (int) (moveFactor * (cameraFocusPosition.getX() - currentCameraPosition.getX())), currentCameraPosition.getY() + (int) (moveFactor * (cameraFocusPosition.getY() - currentCameraPosition.getY())));
        Position currentMovedPosition = new Position(currentCameraPosition.getX() - scrWidth / 2, currentCameraPosition.getY() - scrHeight / 2 - 40); // -40 to account for HUD
        if (game.getBWMap().isValidPosition(currentCameraPosition))
            game.getInteractionHandler().setScreenPosition(currentMovedPosition);
    }

    public void toggle() {
        // Enables or disables the observer
        enabled = !enabled;
    }
}
