package ecgberht;

import bwapi.*;
import bwem.Base;
import bwem.ChokePoint;
import bwem.Mineral;
import cameraModule.CameraModule;
import ecgberht.Agents.*;
import ecgberht.Util.ColorUtil;
import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;

import java.util.List;
import java.util.Map;

public class DebugManager {

    private Game game;

    DebugManager(Game game) {
        this.game = game;
    }

    void keyboardInteraction(String text, CameraModule skycladObserver) {
        boolean setting;
        switch (text) {
            case "dt":
                setting = ConfigManager.getConfig().ecgConfig.debugText;
                game.sendText(!setting ? "debugText enabled" : "debugText disabled");
                ConfigManager.getConfig().ecgConfig.debugText = !setting;
                break;
            case "dc":
                setting = ConfigManager.getConfig().ecgConfig.debugConsole;
                game.sendText(!setting ? "debugConsole enabled" : "debugConsole disabled");
                ConfigManager.getConfig().ecgConfig.debugConsole = !setting;
                break;
            case "ds":
                setting = ConfigManager.getConfig().ecgConfig.debugScreen;
                game.sendText(!setting ? "debugScreen enabled" : "debugScreen disabled");
                ConfigManager.getConfig().ecgConfig.debugScreen = !setting;
                break;
            case "obs":
                setting = ConfigManager.getConfig().ecgConfig.enableSkyCladObserver;
                game.sendText(!setting ? "Observer enabled" : "Observer disabled");
                ConfigManager.getConfig().ecgConfig.enableSkyCladObserver = !setting;
                skycladObserver.toggle();
                break;
            case "sounds":
                setting = ConfigManager.getConfig().ecgConfig.sounds;
                game.sendText(!setting ? "Sounds Effects enabled" : "Sounds Effects disabled");
                ConfigManager.getConfig().ecgConfig.sounds = !setting;
                break;
        }
    }

    private void debugScreen(GameState gameState) {
        try {
            if (!ConfigManager.getConfig().ecgConfig.debugScreen) return;
            if (gameState.naturalArea != null) {
                print(gameState.naturalArea.getTop().toTilePosition(), Color.Red);
                for (ChokePoint c : gameState.naturalArea.getChokePoints()) {
                    if (c.getGeometry().size() > 2)
                        game.drawLineMap(c.getGeometry().get(0).toPosition(), c.getGeometry().get(c.getGeometry().size() - 1).toPosition(), Color.Grey);
                }
            }
            for (ChokePoint c : gameState.bwem.getMap().getChokePoints()) {
                if (c.getGeometry().size() > 2)
                    game.drawLineMap(c.getGeometry().get(0).toPosition(), c.getGeometry().get(c.getGeometry().size() - 1).toPosition(), Color.Green);
            }
        /*for(Entry<Base, MutablePair<MineralPatch, MineralPatch>> u : fortressSpecialBLs.entrySet()){
            if(u.getValue().first != null) bw.getMapDrawer().drawLineMap(u.getKey().getLocation().toPosition(), u.getValue().first.getPosition(),Color.Red);
            if(u.getValue().second != null)bw.getMapDrawer().drawLineMap(u.getKey().getLocation().toPosition(), u.getValue().second.getPosition(),Color.Orange);
        }*/
            for (Unit d : gameState.blockingMinerals.values()) print(d, Color.Red);
            int counter = 0;
            for (Base b : gameState.BLs) {
                game.drawTextMap(Util.getUnitCenterPosition(b.getLocation().toPosition(), UnitType.Terran_Command_Center), ColorUtil.formatText(Integer.toString(counter), ColorUtil.White));
                for (Mineral m : b.getBlockingMinerals()) print(m.getUnit(), Color.Red);
                counter++;
            }
            for (Unit b : gameState.buildingLot) print(b, Color.Purple);
            for (UnitInfo u : gameState.enemyInBase) print(u.unit, Color.Red);
            for (Base b : gameState.islandBases)
                game.drawTextMap(b.getLocation().toPosition(), ColorUtil.formatText("Island", ColorUtil.White));
            for (Agent ag : gameState.agents.values()) {
                if (ag instanceof VultureAgent) {
                    VultureAgent vulture = (VultureAgent) ag;
                    game.drawTextMap(vulture.myUnit.getPosition(), ColorUtil.formatText(ag.statusToString(), ColorUtil.White));
                } else if (ag instanceof VesselAgent) {
                    VesselAgent vessel = (VesselAgent) ag;
                    game.drawTextMap(vessel.myUnit.getPosition(), ColorUtil.formatText(ag.statusToString(), ColorUtil.White));
                    if (vessel.follow != null)
                        game.drawLineMap(vessel.myUnit.getPosition(), vessel.follow.getSquadCenter(), Color.Yellow);
                } else if (ag instanceof WraithAgent) {
                    WraithAgent wraith = (WraithAgent) ag;
                    game.drawTextMap(wraith.myUnit.getPosition().add(new Position(-16,
                            UnitType.Terran_Wraith.dimensionUp())), ColorUtil.formatText(wraith.name, ColorUtil.White));
                } else if (ag instanceof DropShipAgent) {
                    DropShipAgent dropShip = (DropShipAgent) ag;
                    game.drawTextMap(dropShip.myUnit.getPosition(), ColorUtil.formatText(ag.statusToString(), ColorUtil.White));
                } else if (ag instanceof WorkerScoutAgent) {
                    WorkerScoutAgent worker = (WorkerScoutAgent) ag;
                    game.drawTextMap(worker.myUnit.getPosition().add(new Position(-16,
                            UnitType.Terran_SCV.dimensionUp())), ColorUtil.formatText(worker.statusToString(), ColorUtil.White));
                }

            }
            if (gameState.enemyStartBase != null)
                game.drawTextMap(gameState.enemyStartBase.getLocation().toPosition(), ColorUtil.formatText("EnemyStartBase", ColorUtil.White));
            if (gameState.disrupterBuilding != null)
                game.drawTextMap(gameState.disrupterBuilding.getPosition().add(new Position(0, -8)), ColorUtil.formatText("BM!", ColorUtil.White));
            if (gameState.enemyNaturalBase != null)
                game.drawTextMap(gameState.enemyNaturalBase.getLocation().toPosition(), ColorUtil.formatText("EnemyNaturalBase", ColorUtil.White));
            if (gameState.mainChoke != null) {
                game.drawTextMap(gameState.mainChoke.getCenter().toPosition(), ColorUtil.formatText("MainChoke", ColorUtil.White));
                //bw.getMapDrawer().drawTextMap(mainChoke.getCenter().toPosition(), ColorUtil.formatText(Double.toString(Util.getChokeWidth(mainChoke)), ColorUtil.White));
            }
            if (gameState.naturalChoke != null)
                game.drawTextMap(gameState.naturalChoke.getCenter().toPosition(), ColorUtil.formatText("NatChoke", ColorUtil.White));
            if (gameState.chosenHarasser != null) {
                game.drawTextMap(gameState.chosenHarasser.getPosition(), ColorUtil.formatText("Harasser", ColorUtil.White));
                print(gameState.chosenHarasser, Color.Blue);
            }
            for (Map.Entry<Unit, MutablePair<UnitType, TilePosition>> u : gameState.workerBuild.entrySet()) {
                print(u.getKey(), Color.Teal);
                game.drawTextMap(u.getKey().getPosition(), ColorUtil.formatText("Building " + u.getValue().first.toString(), ColorUtil.White));
                print(u.getValue().second, u.getValue().first, Color.Teal);
                game.drawLineMap(u.getKey().getPosition(), Util.getUnitCenterPosition(u.getValue().second.toPosition(), u.getValue().first), Color.Red);
            }
            if (gameState.chosenUnitToHarass != null) {
                print(gameState.chosenUnitToHarass, Color.Red);
                game.drawTextMap(gameState.chosenUnitToHarass.getPosition(), ColorUtil.formatText("UnitToHarass", ColorUtil.White));
            }
            for (Map.Entry<Unit, Unit> r : gameState.repairerTask.entrySet()) {
                print(r.getKey(), Color.Yellow);
                game.drawTextMap(r.getKey().getPosition(), ColorUtil.formatText("Repairer", ColorUtil.White));
                if (r.getValue() == null || !r.getValue().exists()) continue;
                print(r.getValue(), Color.Yellow);
                game.drawLineMap(r.getKey().getPosition(), r.getValue().getPosition(), Color.Yellow);
            }
            for (UnitInfo ui : gameState.unitStorage.getEnemyUnits().values()) {
                game.drawTextMap(ui.lastPosition.add(new Position(0, 16)), ColorUtil.formatText(ui.unitType.toString(), ColorUtil.White));
                print(ui.unit, Color.Red);
            }
            /*for (UnitInfo ui : gameState.unitStorage.getAllyUnits().values()) {
                game.drawTextMap(ui.position.add(new Position(0, 16)), ColorUtil.formatText(ui.unitType.toString(), ColorUtil.White));
                print(ui.unit, Color.Blue);
            }*/
           /* for (UnitInfo ui : gameState.myArmy) {
                game.drawTextMap(ui.position.add(new Position(0, 16)), ColorUtil.formatText(ui.unitType.toString(), ColorUtil.White));
                print(ui.unit, Color.Red);
            }*/
            if (gameState.chosenScout != null) {
                game.drawTextMap(gameState.chosenScout.getPosition(), ColorUtil.formatText("Scouter", ColorUtil.White));
                print(gameState.chosenScout, Color.Purple);
            }
            if (gameState.chosenRepairer != null)
                game.drawTextMap(gameState.chosenRepairer.getPosition(), ColorUtil.formatText("ChosenRepairer", ColorUtil.White));
            for (ChokePoint c : gameState.bwem.getMap().getChokePoints()) {
                List<WalkPosition> sides = c.getGeometry();
                if (sides.size() == 3) {
                    game.drawLineMap(sides.get(1).toPosition(), sides.get(2).toPosition(), Color.Green);
                }
            }
            for (Unit u : gameState.CCs.values()) {
                print(u, Color.Yellow);
                game.drawCircleMap(u.getPosition(), 500, Color.Orange);
            }
            for (Unit u : gameState.DBs.keySet()) {
                game.drawCircleMap(u.getPosition(), 300, Color.Orange);
            }
            for (Unit u : gameState.workerIdle) print(u, Color.Orange);
            for (Map.Entry<Unit, Unit> u : gameState.workerTask.entrySet()) {
                print(u.getKey(), Color.Teal);
                game.drawTextMap(u.getKey().getPosition(), ColorUtil.formatText("Tasked: " + u.getValue().getType().toString(), ColorUtil.White));
                print(u.getValue(), Color.Teal);
                game.drawLineMap(u.getKey().getPosition(), u.getValue().getPosition(), Color.Red);
            }
            for (Unit u : gameState.workerDefenders.keySet()) {
                print(u, Color.Purple);
                game.drawTextMap(u.getPosition(), ColorUtil.formatText("SpartanSCV", ColorUtil.White));
            }
            for (Map.Entry<Unit, Unit> u : gameState.workerMining.entrySet()) {
                print(u.getKey(), Color.Cyan);
                game.drawLineMap(u.getKey().getPosition(), u.getValue().getPosition(), Color.Cyan);
            }
            for (Map.Entry<Unit, Unit> u : gameState.workerGas.entrySet()) {
                if (u.getKey().getOrder() == Order.HarvestGas) continue;
                print(u.getKey(), Color.Green);
                game.drawLineMap(u.getKey().getPosition(), u.getValue().getPosition(), Color.Green);
            }
            for (Map.Entry<Unit, Boolean> u : gameState.vespeneGeysers.entrySet()) {
                print(u.getKey(), Color.Green);
                if (gameState.refineriesAssigned.containsKey(u.getKey())) {
                    int gas = gameState.refineriesAssigned.get(u.getKey());
                    game.drawTextMap(u.getKey().getPosition(), ColorUtil.formatText(Integer.toString(gas), ColorUtil.White));
                }
            }
            gameState.sim.drawClusters();
            for (Squad s : gameState.sqManager.squads.values()) {
                if (s.status == Squad.Status.ATTACK && s.attack != null)
                    game.drawLineMap(s.getSquadCenter(), s.attack, Color.Orange);
            }
            for (Squad s : gameState.sqManager.squads.values()) {
                if (s.members.isEmpty()) continue;
                Position center = s.getSquadCenter();
                //game.drawCircleMap(center, 90, Color.Green);
                game.drawTextMap(center.add(new Position(0, UnitType.Terran_Marine.dimensionUp())), ColorUtil.formatText(s.status.toString(), ColorUtil.White));
                game.drawTextMap(center.add(new Position(0, UnitType.Terran_Marine.dimensionUp() * 2)), ColorUtil.formatText(s.lose ? "Lose" : "Win", ColorUtil.White));
            }
            for (Map.Entry<Unit, Integer> m : gameState.mineralsAssigned.entrySet()) {
                print(m.getKey(), Color.Cyan);
                if (m.getValue() == 0) continue;
                game.drawTextMap(m.getKey().getPosition(), ColorUtil.formatText(m.getValue().toString(), ColorUtil.White));
            }
        } catch (Exception e) {
            System.err.println("debugScreen Exception");
            e.printStackTrace();
        }
    }

    private void debugText(GameState gameState) {
        try {
            if (!ConfigManager.getConfig().ecgConfig.debugText) return;
            game.drawTextScreen(320, 5, ColorUtil.formatText(gameState.supplyMan.getSupplyUsed() + "/" + gameState.supplyMan.getSupplyTotal(), ColorUtil.White));
            game.drawTextScreen(320, 20, ColorUtil.formatText(gameState.getArmySize() + "/" + gameState.getStrat().armyForAttack, ColorUtil.White));
            String defending = gameState.defense ? ColorUtil.formatText("Defense", ColorUtil.Green) : ColorUtil.formatText("Defense", ColorUtil.Red);
            game.drawTextScreen(320, 35, defending);
            game.drawTextScreen(320, 50, ColorUtil.formatText("I want to train: " + gameState.chosenUnit.toString(), ColorUtil.White));
            game.drawTextScreen(320, 65, ColorUtil.formatText("I want to build: " + gameState.chosenToBuild.toString(), ColorUtil.White));
            game.drawTextScreen(320, 80, ColorUtil.formatText("Max_Goliaths: " + gameState.maxGoliaths, ColorUtil.White));
            game.drawTextScreen(320, 95, ColorUtil.formatText("Max_Vessels: " + gameState.maxVessels, ColorUtil.White));
            if (gameState.enemyRace == Race.Zerg)
                game.drawTextScreen(320, 110, ColorUtil.formatText("Max_Firebats: " + gameState.maxBats, ColorUtil.White));
            if (gameState.getGame().allies().size() + gameState.getGame().enemies().size() == 1) {
                game.drawTextScreen(10, 5,
                        ColorUtil.formatText(gameState.self.getName(), ColorUtil.getColor(gameState.self.getColor())) +
                                ColorUtil.formatText(" vs ", ColorUtil.White) +
                                ColorUtil.formatText(gameState.bw.enemy().getName(), ColorUtil.getColor(game.enemy().getColor())));
            }
            game.drawTextScreen(10, 5,
                    ColorUtil.formatText(gameState.bw.self().getName(), ColorUtil.Green) + // TODO convert jbwapi color to byte
                            ColorUtil.formatText(" vs ", ColorUtil.White) +
                            ColorUtil.formatText(gameState.bw.enemy().getName(), ColorUtil.Red));
            if (gameState.chosenScout != null) {
                game.drawTextScreen(10, 20, ColorUtil.formatText("Scouting: ", ColorUtil.White) + ColorUtil.formatText("Yes", ColorUtil.Green));
            } else {
                game.drawTextScreen(10, 20, ColorUtil.formatText("Scouting: ", ColorUtil.White) + ColorUtil.formatText("No", ColorUtil.Red));
            }
            if (gameState.enemyMainBase != null) {
                game.drawTextScreen(10, 35, ColorUtil.formatText("Enemy Base Found: ", ColorUtil.White) + ColorUtil.formatText("Yes", ColorUtil.Green));
            } else {
                game.drawTextScreen(10, 35, ColorUtil.formatText("Enemy Base Found: ", ColorUtil.White) + ColorUtil.formatText("No", ColorUtil.Red));
            }
            game.drawTextScreen(10, 50, ColorUtil.formatText("Framecount: ", ColorUtil.White) + ColorUtil.formatText(Integer.toString(gameState.frameCount), ColorUtil.Yellow));
            game.drawTextScreen(10, 65, ColorUtil.formatText("FPS: ", ColorUtil.White) + ColorUtil.formatText(Integer.toString(gameState.bw.getFPS()), ColorUtil.Yellow));
            game.drawTextScreen(65, 65, ColorUtil.formatText("APM: ", ColorUtil.White) + ColorUtil.formatText(Integer.toString(gameState.bw.getAPM()), ColorUtil.Yellow));
            game.drawTextScreen(10, 80, ColorUtil.formatText("Strategy: ", ColorUtil.White) + ColorUtil.formatText(gameState.getStrat().name, ColorUtil.Yellow));
            game.drawTextScreen(10, 95, ColorUtil.formatText("EnemyStrategy: ", ColorUtil.White) + ColorUtil.formatText(IntelligenceAgency.getEnemyStrat().toString(), ColorUtil.Yellow));
            game.drawTextScreen(10, 110, ColorUtil.formatText("SimTime(ms): ", ColorUtil.White) + ColorUtil.formatText(String.valueOf(gameState.sim.time), ColorUtil.Teal));
            if (gameState.enemyRace == Race.Zerg && gameState.learningManager.isNaughty()) {
                game.drawTextScreen(10, 125, ColorUtil.formatText("Naughty Zerg: ", ColorUtil.White) + ColorUtil.formatText("yes", ColorUtil.Green));
            }
        } catch (Exception e) {
            System.err.println("debugText Exception");
            e.printStackTrace();
        }
    }

    private void print(Unit u, Color color) {
        game.drawBoxMap(u.getLeft(), u.getTop(), u.getRight(), u.getBottom(), color);
    }

    private void print(TilePosition u, UnitType type, Color color) {
        Position leftTop = new Position(u.getX() * TilePosition.SIZE_IN_PIXELS, u.getY() * TilePosition.SIZE_IN_PIXELS);
        Position rightBottom = new Position(leftTop.getX() + type.tileWidth() * TilePosition.SIZE_IN_PIXELS, leftTop.getY() + type.tileHeight() * TilePosition.SIZE_IN_PIXELS);
        game.drawBoxMap(leftTop, rightBottom, color);
    }

    private void print(TilePosition u, Color color) {
        Position leftTop = new Position(u.getX() * TilePosition.SIZE_IN_PIXELS, u.getY() * TilePosition.SIZE_IN_PIXELS);
        Position rightBottom = new Position(leftTop.getX() + TilePosition.SIZE_IN_PIXELS, leftTop.getY() + TilePosition.SIZE_IN_PIXELS);
        game.drawBoxMap(leftTop, rightBottom, color);
    }

    public void onFrame(GameState gs) {
        debugScreen(gs);
        debugText(gs);
    }
}