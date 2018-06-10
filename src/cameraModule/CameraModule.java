package cameraModule;

import bwapi.*;

import java.util.List;

public class CameraModule extends ICameraModule {

    /*
     * Constructor for the CameraModule object
     * Receives the player starting Position and the Game object
     */
    public CameraModule(Position startPos, Game game) {
        myStartLocation = startPos;
        cameraFocusPosition = startPos;
        currentCameraPosition = startPos;
        this.game = game;
    }

    @Override
    public void onFrame() {
        if (enabled) {
            moveCameraFallingNuke();
            moveCameraIsUnderAttack();
            moveCameraIsAttacking();
            if (game.getFrameCount() <= watchScoutWorkerUntil) {
                moveCameraScoutWorker();
            }
            moveCameraArmy();
            moveCameraDrop();
            updateCameraPosition();
        }
    }

    @Override
    public boolean isNearStartLocation(Position pos) {
        int distance = 1000;
        List<TilePosition> startLocations = game.getStartLocations();

        for (TilePosition it : startLocations) {
            Position startLocation = it.toPosition();
            // if the start position is not our own home, and the start position is closer than distance
            if (!isNearOwnStartLocation(startLocation) && startLocation.getDistance(pos) <= distance) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isNearOwnStartLocation(Position pos) {
        int distance = 10 * TilePosition.SIZE_IN_PIXELS; // 10*32
        return (myStartLocation.getDistance(pos) <= distance);
    }

    @Override
    public boolean isArmyUnit(Unit unit) {
        return !(unit.getType().isWorker()
                || unit.getType().isBuilding()
                || unit.getType() == UnitType.Terran_Vulture_Spider_Mine
                || unit.getType() == UnitType.Zerg_Overlord
                || unit.getType() == UnitType.Zerg_Larva);
    }

    @Override
    public boolean shouldMoveCamera(int priority) {
        boolean isTimeToMove = game.getFrameCount() - lastMoved >= cameraMoveTime;
        boolean isTimeToMoveIfHigherPrio = game.getFrameCount() - lastMoved >= cameraMoveTimeMin;
        boolean isHigherPrio = lastMovedPriority < priority;
        // camera should move IF: enough time has passed OR (minimum time has passed AND new prio is higher)
        return isTimeToMove || (isHigherPrio && isTimeToMoveIfHigherPrio);
    }

    @Override
    public void moveCamera(Position pos, int priority) {
        if (!shouldMoveCamera(priority)) {
            return;
        }
        if (followUnit == false && cameraFocusPosition == pos) {
            // don't register a camera move if the position is the same
            return;
        }

        cameraFocusPosition = pos;
        lastMovedPosition = cameraFocusPosition;
        lastMoved = game.getFrameCount();
        lastMovedPriority = priority;
        followUnit = false;

    }

    @Override
    public void moveCamera(Unit unit, int priority) {
        if (!shouldMoveCamera(priority)) {
            return;
        }
        if (followUnit == true && cameraFocusUnit == unit) {
            // don't register a camera move if we follow the same unit
            return;
        }

        cameraFocusUnit = unit;
        lastMovedPosition = cameraFocusUnit.getPosition();
        lastMoved = game.getFrameCount();
        lastMovedPriority = priority;
        followUnit = true;

    }

    @Override
    public void moveCameraIsAttacking() {
        int prio = 3;
        if (!shouldMoveCamera(prio)) {
            return;
        }

        for (Unit unit : game.self().getUnits()) {
            if (unit.isAttacking()) {
                moveCamera(unit, prio);
            }
        }

    }

    @Override
    public void moveCameraIsUnderAttack() {
        int prio = 3;
        if (!shouldMoveCamera(prio)) {
            return;
        }

        for (Unit unit : game.self().getUnits()) {
            if (unit.isUnderAttack()) {
                moveCamera(unit, prio);
            }
        }
    }

    @Override
    public void moveCameraScoutWorker() {
        int highPrio = 2;
        int lowPrio = 0;
        if (!shouldMoveCamera(lowPrio)) {
            return;
        }

        for (Unit unit : game.self().getUnits()) {
            if (!unit.getType().isWorker()) {
                continue;
            }
            if (isNearStartLocation(unit.getPosition())) {
                moveCamera(unit, highPrio);
            } else if (!isNearOwnStartLocation(unit.getPosition())) {
                moveCamera(unit, lowPrio);
            }
        }

    }

    @Override
    public void moveCameraFallingNuke() {
        int prio = 5;
        if (!shouldMoveCamera(prio)) {
            return;
        }

        for (Unit unit : game.getAllUnits()) {
            if (unit.getType() == UnitType.Terran_Nuclear_Missile && unit.getVelocityY() > 0) {
                moveCamera(unit, prio);
                return;
            }
        }

    }

    @Override
    public void moveCameraNukeDetect(Position target) {
        int prio = 4;
        if (!shouldMoveCamera(prio)) {
            return;
        } else {
            moveCamera(target, prio);
        }

    }

    @Override
    public void moveCameraDrop() {
        int prio = 2;
        if (!shouldMoveCamera(prio)) {
            return;
        }
        for (Unit unit : game.self().getUnits()) {
            if ((unit.getType() == UnitType.Zerg_Overlord || unit.getType() == UnitType.Terran_Dropship || unit.getType() == UnitType.Protoss_Shuttle)
                    && isNearStartLocation(unit.getPosition()) && unit.getLoadedUnits().size() > 0) {
                moveCamera(unit, prio);
            }
        }

    }

    @Override
    public void moveCameraArmy() {
        int prio = 1;
        if (!shouldMoveCamera(prio)) {
            return;
        }
        // Double loop, check if army units are close to each other
        int radius = 50;

        Unit bestPosUnit = null;
        int mostUnitsNearby = 0;

        for (Unit unit1 : game.getAllUnits()) {
            if (!isArmyUnit(unit1)) {
                continue;
            }
            Position uPos = unit1.getPosition();

            int nrUnitsNearby = 0;
            for (Unit unit2 : game.getUnitsInRadius(uPos, radius)) {
                if (!isArmyUnit(unit2)) {
                    continue;
                }
                nrUnitsNearby++;
            }

            if (nrUnitsNearby > mostUnitsNearby) {
                mostUnitsNearby = nrUnitsNearby;
                bestPosUnit = unit1;
            }
        }

        if (mostUnitsNearby > 1) {
            moveCamera(bestPosUnit, prio);
        }

    }

    @Override
    public void moveCameraUnitCreated(Unit unit) {
        if (enabled) {
            int prio = 1;
            if (!shouldMoveCamera(prio)) {
                return;
            } else if (unit.getPlayer() == game.self() && !unit.getType().isWorker()) {
                moveCamera(unit, prio);
            }
        }
    }

    @Override
    public void updateCameraPosition() {
        double moveFactor = 0.1;
        if (followUnit && cameraFocusUnit.getPosition().isValid()) {
            cameraFocusPosition = cameraFocusUnit.getPosition();
        }
        currentCameraPosition = new Position(currentCameraPosition.getX() + (int) (moveFactor * (cameraFocusPosition.getX() - currentCameraPosition.getX())), currentCameraPosition.getY() + (int) (moveFactor * (cameraFocusPosition.getY() - currentCameraPosition.getY())));
        Position currentMovedPosition = new Position(currentCameraPosition.getX() - scrWidth / 2, currentCameraPosition.getY() - scrHeight / 2 - 40); // -40 to account for HUD

        if (currentCameraPosition.isValid()) {
            game.setScreenPosition(currentMovedPosition);
        }
    }

    @Override
    public void toggle() {
        // Enables or disables the observer
        enabled = !enabled;
    }

}
