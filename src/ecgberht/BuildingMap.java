package ecgberht;

import bwem.BWEM;
import bwem.Base;
import bwem.ChokePoint;
import bwem.area.Area;
import org.openbw.bwapi4j.BW;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static ecgberht.Ecgberht.getGs;

public class BuildingMap implements Cloneable {

    private Player self;
    private int height;
    private int width;
    private String map[][];
    private BW bw;
    private BWEM bwem;

    public BuildingMap(BW bw, Player self, BWEM bwem) {
        this.bw = bw;
        this.self = self;
        this.height = bw.getBWMap().mapHeight();
        this.width = bw.getBWMap().mapWidth();
        this.map = new String[this.height][this.width];
        this.bwem = bwem;
    }

    public BuildingMap(BW bw, Player self, int height, int width, String[][] map, BWEM bwem) {
        this.bw = bw;
        this.self = self;
        this.height = height;
        this.width = width;
        this.map = map.clone();
        this.bwem = bwem;
    }

    public String[][] getMap() {
        return this.map;
    }

    @Override
    public BuildingMap clone() {
        return new BuildingMap(bw, self, height, width, map, bwem);
    }

    // Generates an initial building map
    public void initMap() {
        //Find valid and no valid positions for building
        for (int jj = 0; jj < height; jj++) {
            for (int ii = 0; ii < width; ii++) {
                TilePosition x = new TilePosition(ii, jj);
                if (bw.getBWMap().isBuildable(x, false)) {
                    map[jj][ii] = "6";
                } else {
                    map[jj][ii] = "0";
                }
            }
        }
        // Finds minerals and geysers
        for (MineralPatch resource : bw.getMineralPatches()) {
            TilePosition resourceTile = resource.getTilePosition();

            TilePosition resourceSize = resource.getInitialType().tileSize();
            for (int i = resourceTile.getY(); i < resourceTile.getY() + resourceSize.getY(); i++) {
                for (int j = resourceTile.getX(); j < resourceTile.getX() + resourceSize.getX(); j++) {
                    if (i < 0 || i >= height || j < 0 || j >= width) {
                        continue;
                    }
                    if (map[i][j] != "V") {
                        map[i][j] = "M";
                    }
                }
            }
        }
        for (VespeneGeyser resource : bw.getVespeneGeysers()) {
            TilePosition resourceTile = resource.getTilePosition();
            TilePosition resourceSize = resource.getInitialType().tileSize();
            for (int i = resourceTile.getY(); i < resourceTile.getY() + resourceSize.getY(); i++) {
                for (int j = resourceTile.getX(); j < resourceTile.getX() + resourceSize.getX(); j++) {
                    if (i < 0 || i >= height || j < 0 || j >= width) {
                        continue;
                    }
                    map[i][j] = "V";
                }
            }
        }
        for (Area a : bwem.getMap().getAreas()) {
            for (Base b : a.getBases()) {
                TilePosition starting = b.getLocation();
                for (int i = starting.getY(); i < starting.getY() + UnitType.Terran_Command_Center.tileHeight(); i++) {
                    for (int j = starting.getX(); j < starting.getX() + UnitType.Terran_Command_Center.tileWidth(); j++) {
                        if (i < 0 || i >= height || j < 0 || j >= width) {
                            continue;
                        }
                        map[i][j] = "E";
                    }
                }
                map[starting.getY() + 1][starting.getX() + UnitType.Terran_Command_Center.tileWidth()] = "E";
                map[starting.getY() + 2][starting.getX() + UnitType.Terran_Command_Center.tileWidth()] = "E";
                map[starting.getY() + 1][starting.getX() + UnitType.Terran_Command_Center.tileWidth() + 1] = "E";
                map[starting.getY() + 2][starting.getX() + UnitType.Terran_Command_Center.tileWidth() + 1] = "E";
            }
        }
        map = fillMap(map);
    }

    // Fills the map with the correct values for each tile
    public String[][] fillMap(String[][] map) {
        int height = map.length;
        int width = map[0].length;
        for (int jj = height - 1; jj >= 0; jj--) {
            if (map[jj][width - 1] != "M" && map[jj][width - 1] != "V" && map[jj][width - 1] != "0" && map[jj][width - 1] != "E" && map[jj][width - 1] != "B") {
                if (jj == this.height - 1 || width - 1 == this.width - 1) {
                    map[jj][width - 1] = "1";
                }
            }
        }
        for (int ii = width - 1; ii >= 0; ii--) {
            if (map[height - 1][ii] != "M" && map[height - 1][ii] != "V" && map[height - 1][ii] != "0" && map[height - 1][ii] != "E" && map[height - 1][ii] != "B") {
                if (height - 1 == this.height - 1 || ii == this.width - 1) {
                    map[height - 1][ii] = "1";
                }
            }
        }
        // Sets to "B" adjacent tiles to 0,M,V by the left and top with value "6"
        for (int jj = height - 1; jj >= 0; jj--) {
            for (int ii = width - 1; ii >= 0; ii--) {
                if (map[jj][ii] == "E" || map[jj][ii] == "M" || map[jj][ii] == "V") {
                    if (jj - 1 >= 0) {
                        if (map[jj - 1][ii] == "6") {
                            map[jj - 1][ii] = "B";
                        }
                    }
                    if (ii - 1 >= 0) {
                        if (map[jj][ii - 1] == "6") {
                            map[jj][ii - 1] = "B";
                        }
                    }
                    if (jj - 1 >= 0 && ii - 1 >= 0) {
                        if (map[jj - 1][ii - 1] == "6") {
                            map[jj - 1][ii - 1] = "B";
                        }
                    }
                    if (jj + 1 < height) {
                        if (map[jj + 1][ii] == "6") {
                            map[jj + 1][ii] = "B";
                        }
                    }
                    if (ii + 1 < width) {
                        if (map[jj][ii + 1] == "6") {
                            map[jj][ii + 1] = "B";
                        }
                    }
                    if (jj + 1 < height && ii + 1 < width) {
                        if (map[jj + 1][ii + 1] == "6") {
                            map[jj + 1][ii + 1] = "B";
                        }
                    }
                    if (jj - 1 >= 0 && ii + 1 < width) {
                        if (map[jj - 1][ii + 1] == "6") {
                            map[jj - 1][ii + 1] = "B";
                        }
                    }
                    if (jj + 1 < height && ii - 1 >= 0) {
                        if (map[jj + 1][ii - 1] == "6") {
                            map[jj + 1][ii - 1] = "B";
                        }
                    }
                }
            }
        }
        for (int jj = height - 1; jj > 0; jj--) {
            for (int ii = width - 1; ii > 0; ii--) {
                if (map[jj][ii] == "B" || map[jj][ii] == "0" || map[jj][ii] == "M" || map[jj][ii] == "V" || map[jj][ii] == "E") {
                    if (map[jj - 1][ii] == "6") {
                        map[jj - 1][ii] = "1";
                    }
                    if (map[jj][ii - 1] == "6") {
                        map[jj][ii - 1] = "1";
                    }
                    if (map[jj - 1][ii - 1] == "6") {
                        map[jj - 1][ii - 1] = "1";
                    }
                }
            }
        }
        for (int jj = height - 1; jj > 0; jj--) {
            for (int ii = width - 1; ii > 0; ii--) {
                if (map[jj][ii] == "1") {
                    if (map[jj - 1][ii] == "6") {
                        map[jj - 1][ii] = "2";
                    }
                    if (map[jj][ii - 1] == "6") {
                        map[jj][ii - 1] = "2";
                    }
                    if (map[jj - 1][ii - 1] == "6") {
                        map[jj - 1][ii - 1] = "2";
                    }
                }
            }
        }
        for (int jj = height - 1; jj > 0; jj--) {
            for (int ii = width - 1; ii > 0; ii--) {
                if (map[jj][ii] == "2") {
                    if (map[jj - 1][ii] == "6") {
                        map[jj - 1][ii] = "3";
                    }
                    if (map[jj][ii - 1] == "6") {
                        map[jj][ii - 1] = "3";
                    }
                    if (map[jj - 1][ii - 1] == "6") {
                        map[jj - 1][ii - 1] = "3";
                    }
                }
            }
        }
        for (int jj = height - 1; jj > 0; jj--) {
            for (int ii = width - 1; ii > 0; ii--) {
                if (map[jj][ii] == "3") {
                    if (map[jj - 1][ii] == "6") {
                        map[jj - 1][ii] = "4";
                    }
                    if (map[jj][ii - 1] == "6") {
                        map[jj][ii - 1] = "4";
                    }
                    if (map[jj - 1][ii - 1] == "6") {
                        map[jj - 1][ii - 1] = "4";
                    }
                }
            }
        }
        for (int jj = height - 1; jj > 0; jj--) {
            for (int ii = width - 1; ii > 0; ii--) {
                if (map[jj][ii] == "4") {
                    if (map[jj - 1][ii] == "6") {
                        map[jj - 1][ii] = "5";
                    }
                    if (map[jj][ii - 1] == "6") {
                        map[jj][ii - 1] = "5";
                    }
                    if (map[jj - 1][ii - 1] == "6") {
                        map[jj - 1][ii - 1] = "5";
                    }
                }
            }
        }
        return map;
    }

    // Updates a portion of the map around the building
    public void updateMap(TilePosition position, UnitType building, boolean destroyed) {
        TilePosition buildingSize = building.tileSize();
        int tamY = buildingSize.getY();
        int tamX = buildingSize.getX();
        //Updates the map with the next building to be built
        for (int i = position.getY() - 1; i < position.getY() + tamY + 1; i++) {
            for (int j = position.getX() - 1; j < position.getX() + tamX + 1; j++) {
                if (i < 0 || i >= height || j < 0 || j >= width) {
                    continue;
                }
                if (destroyed) {
                    if (i == position.getY() - 1 || i == position.getY() + tamY || j == position.getX() - 1 || j == position.getX() + tamX) {
                        if (map[i][j] != "0") {
                            map[i][j] = "6";
                        }
                    } else {
                        if (map[i][j] != "V") {
                            map[i][j] = "6";
                        }
                    }
                } else {
                    if (i != position.getY() - 1 && i != position.getY() + tamY && j != position.getX() - 1 && j != position.getX() + tamX) {
                        if (map[i][j] != "M" && map[i][j] != "V" && map[i][j] != "0" && map[i][j] != "E" && map[i][j] != "B") {
                            if (building == UnitType.Terran_Bunker) {
                                map[i][j] = "0";
                            } else {
                                map[i][j] = "E";
                            }
                        }
                    }
                }
            }
        }
        if (building.canBuildAddon()) {
            for (int i = position.getY() + tamY; i > position.getY() + tamY - 4; i--) {
                for (int j = position.getX() + tamX - 1; j < position.getX() + tamX + 3; j++) {
                    if (i < 0 || i >= height || j < 0 || j >= width) {
                        continue;
                    }
                    if (destroyed) {
                        if (i == position.getY() + tamY - 3 || i == position.getY() + tamY || j == position.getX() + tamX + 2 || j == position.getX() + tamX - 1) {
                            if (map[i][j] != "0") {
                                map[i][j] = "6";
                            }
                        } else {
                            if (map[i][j] != "V") {
                                map[i][j] = "6";
                            }
                        }
                    } else {
                        if (i != position.getY() + tamY - 3 && i != position.getY() + tamY && j != position.getX() + tamX + 2 && j != position.getX() + tamX - 1) {
                            if (map[i][j] != "M" && map[i][j] != "V" && map[i][j] != "0" && map[i][j] != "E" && map[i][j] != "B") {
                                map[i][j] = "E";
                            }
                        }
                    }
                }
            }
        }
        // Finds the corners around the building
        int init_i = 0;
        if (position.getY() - height > 0) {
            init_i = position.getY() - height;
        }
        int fin_i = height;
        if (position.getY() + tamY + height < height) {
            fin_i = position.getY() + tamY + height;
        }
        int init_j = 0;
        if (position.getX() - width > 0) {
            init_j = position.getX() - width;
        }
        int fin_j = width;
        if (position.getX() + tamX + width < width) {
            fin_j = position.getX() + tamX + width;
        }
        // Generates a submatrix as a portion of the map delimited by the corners and resets the 1,2,3 values for 4
        String[][] submap = new String[fin_i - init_i][fin_j - init_j];
        int i = 0;
        int j = 0;
        for (int ii = init_i; ii < fin_i; ii++) {
            j = 0;
            for (int jj = init_j; jj < fin_j; jj++) {
                if (map[ii][jj] == "M" || map[ii][jj] == "V" || map[ii][jj] == "0" || map[ii][jj] == "E" || map[ii][jj] == "B") {
                    submap[i][j] = map[ii][jj];
                } else {
                    submap[i][j] = "6";
                }
                j++;
            }
            i++;
        }
        submap = fillMap(submap);
        // Updates the map using the submatrix
        i = 0;
        j = 0;
        for (int ii = init_i; ii < fin_i; ii++) {
            j = 0;
            for (int jj = init_j; jj < fin_j; jj++) {
                map[ii][jj] = submap[i][j];
                j++;
            }
            i++;
        }
    }


    // Finds a valid position in the map for a specific building type starting with a given tileposition
    public TilePosition findPosition(UnitType buildingType, TilePosition starting) {
        TilePosition buildingSize = buildingType.tileSize();
        int size = Math.max(buildingSize.getY(), buildingSize.getX());
        if (buildingType.canBuildAddon()) {
            size = Math.max(buildingSize.getY(), buildingSize.getX() + 2);
        }
        int x = starting.getY();
        int y = starting.getX();
        int[] coord = new int[2];
        int i = 2;
        int j = 2;
        boolean control = false;

        // Finds the first valid tileposition starting around the given tileposition
        while (!control) {
            for (int ii = (x - i); ii <= (x + i); ii++) {
                for (int jj = (y - j); jj <= (y + j); jj++) {
                    if ((ii >= 0 && ii < height) && (jj >= 0 && jj < width)) {
                        if ((map[ii][jj] != "M" && map[ii][jj] != "V" && map[ii][jj] != "E" && map[ii][jj] != "B") && Integer.parseInt(map[ii][jj]) >= size) {
                            if (buildingType == UnitType.Terran_Bunker) {
                                if (!bwem.getMap().getArea(new TilePosition(jj, ii)).equals(bwem.getMap().getArea(self.getStartLocation()))) {
                                    continue;
                                }
                            }
                            if (!checkUnitsChosenBuildingGrid(new TilePosition(jj, ii), buildingType)) {
                                coord[0] = ii;
                                coord[1] = jj;
                                control = true;
                                break;
                            }
                        }
                    }
                }
                if (control) {
                    break;
                }
            }
            i++;
            j++;
        }
        return new TilePosition(coord[1], coord[0]);
    }

    public TilePosition findBunkerPosition(ChokePoint choke) {
        TilePosition buildingSize = UnitType.Terran_Bunker.tileSize();
        int size = Math.max(buildingSize.getY(), buildingSize.getX());
        Position starting = choke.getCenter().toPosition();
        int x = starting.toTilePosition().getY();
        int y = starting.toTilePosition().getX();
        int i = 10;
        int j = 10;
        // Finds the first valid tileposition starting around the given tileposition
        TilePosition position = null;
        double dist = Double.MAX_VALUE;
        for (int ii = (x - i); ii <= (x + i); ii++) {
            for (int jj = (y - j); jj <= (y + j); jj++) {
                if ((ii >= 0 && ii < height) && (jj >= 0 && jj < width)) {
                    if ((map[ii][jj] != "M" && map[ii][jj] != "V" && map[ii][jj] != "E" && map[ii][jj] != "B") && Integer.parseInt(map[ii][jj]) >= size) {
                        Area area = bwem.getMap().getArea(new TilePosition(jj, ii));
                        if (area != null && area.equals(getGs().naturalRegion)) continue;
                        if (!checkUnitsChosenBuildingGrid(new TilePosition(jj, ii), UnitType.Terran_Bunker)) {
                            TilePosition newPosition = new TilePosition(jj, ii);
                            double newDist = getGs().broodWarDistance(getGs().getCenterFromBuilding(newPosition.toPosition(), UnitType.Terran_Bunker), starting);
                            if (position == null || newDist < dist) {
                                position = newPosition;
                                dist = newDist;
                            }
                        }
                    }
                }
            }
        }
        return position;
    }

    private boolean checkUnitsChosenBuildingGrid(TilePosition BL, UnitType type) {
        try {
            Position topLeft = new Position(BL.getX() * TilePosition.SIZE_IN_PIXELS, BL.getY() * TilePosition.SIZE_IN_PIXELS);
            Position bottomRight = new Position(topLeft.getX() + type.tileWidth() * TilePosition.SIZE_IN_PIXELS, topLeft.getY() + type.tileHeight() * TilePosition.SIZE_IN_PIXELS);
            List<Unit> blockers = Util.getUnitsInRectangle(topLeft, bottomRight); // Test
            if (blockers.isEmpty() && !getGs().getGame().canBuildHere(BL, type)) return true;
            if (blockers.isEmpty()) return false;
            if (blockers.size() > 1) return true;
            else {
                Unit blocker = blockers.get(0);
                if (blocker instanceof PlayerUnit && ((PlayerUnit) blocker).getPlayer().getId() == self.getId() && blocker instanceof Worker &&
                        ((Worker) blocker).getBuildType() == type) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public TilePosition findBunkerPositionAntiPool() {
        TilePosition starting = getGs().MBs.iterator().next().getTilePosition();
        TilePosition buildingSize = UnitType.Terran_Bunker.tileSize();
        int size = Math.max(buildingSize.getY(), buildingSize.getX());
        int x = starting.getY();
        int y = starting.getX();
        ChokePoint choke = getGs().mainChoke;
        int i = 4;
        int j = 4;
        //Finds the first valid tileposition starting around the given tileposition
        TilePosition bunkerPlace = null;
        double dist = Double.MAX_VALUE;
        for (int ii = (x - i); ii <= (x + i); ii++) {
            for (int jj = (y - j); jj <= (y + j); jj++) {
                if ((ii >= 0 && ii < height) && (jj >= 0 && jj < width)) {
                    if ((map[ii][jj] != "M" && map[ii][jj] != "V" && map[ii][jj] != "E" && map[ii][jj] != "B") && Integer.parseInt(map[ii][jj]) >= size) {
                        if (bwem.getMap().getArea(new TilePosition(jj, ii)).equals(getGs().naturalRegion)) continue;
                        if (!checkUnitsChosenBuildingGrid(new TilePosition(jj, ii), UnitType.Terran_Bunker)) {
                            TilePosition newPosition = new TilePosition(jj, ii);
                            double newDist = getGs().broodWarDistance(getGs().getCenterFromBuilding(newPosition.toPosition(), UnitType.Terran_Bunker), choke.getCenter().toPosition());
                            if (bunkerPlace == null || newDist < dist) {
                                bunkerPlace = newPosition;
                                dist = newDist;
                            }
                        }
                    }
                }
            }
        }
        return bunkerPlace;
    }

    // Writes the map to a file
    public void writeMap(String fileName) {
        FileWriter sw = null;
        try {
            sw = new FileWriter(fileName);
            for (int ii = 0; ii < height; ii++) {
                for (int jj = 0; jj < width; jj++) {
                    try {
                        if (sw != null) {
                            sw.write(map[ii][jj]);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (ii != height - 1) {
                    try {
                        sw.write("\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            sw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
