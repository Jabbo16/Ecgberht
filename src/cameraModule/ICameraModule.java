package cameraModule;


import bwapi.Game;
import bwapi.Position;
import bwapi.Unit;

public abstract class ICameraModule {

    Position myStartLocation;
    int scrWidth = 640; //Default width ChaosLauncher with WMODE plugin
    int scrHeight = 410; //Default height ChaosLauncher with WMODE plugin, tweaked a bit to improve real center camera location
    int cameraMoveTime = 150;
    int cameraMoveTimeMin = 50;
    int watchScoutWorkerUntil = 7500;
    int lastMoved = 0;
    int lastMovedPriority = 0;
    Position lastMovedPosition = new Position(0, 0);
    Position currentCameraPosition;
    Position cameraFocusPosition = new Position(0, 0);
    Unit cameraFocusUnit = null;
    boolean followUnit = false;
    boolean enabled = false;
    Game game;

    public abstract void onFrame();

    public abstract boolean isNearStartLocation(Position pos);

    public abstract boolean isNearOwnStartLocation(Position pos);

    public abstract boolean isArmyUnit(Unit unit);

    public abstract boolean shouldMoveCamera(int priority);

    public abstract void moveCamera(Position pos, int priority);

    public abstract void moveCamera(Unit unit, int priority);

    public abstract void moveCameraIsAttacking();

    public abstract void moveCameraIsUnderAttack();

    public abstract void moveCameraScoutWorker();

    public abstract void moveCameraFallingNuke();

    public abstract void moveCameraNukeDetect(Position target);

    public abstract void moveCameraDrop();

    public abstract void moveCameraArmy();

    public abstract void moveCameraUnitCreated(Unit unit);

    public abstract void updateCameraPosition();

    public abstract void toggle();

}
