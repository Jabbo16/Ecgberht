package ecgberht;

import ecgberht.Util.MutablePair;
import ecgberht.Util.Util;
import org.openbw.bwapi4j.BW;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.*;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class InfluenceMap {

    private static final int bio = 1;
    private static final int defensive = 5;
    private static final int flying = 6;
    private static final int mech = 3;
    private static final int neutral = 3;
    private static final int depot = 6;
    private static final int offensive = 4;
    private static final int propagation = 2;
    private static final int umbral = 3;
    public double[][] map;
    private BW bw;
    private int height;
    private int width;

    public InfluenceMap(BW bw, int height, int width) {
        this.bw = bw;
        this.height = height;
        this.width = width;
        map = new double[height][width];
    }

    public void clear() {
        map = new double[this.height][this.width];
    }

    public void updateMap(Unit arg0, boolean destroyed) {
        try {
            int influence;
            UnitType type;
            if (arg0 instanceof MineralPatch || arg0 instanceof VespeneGeyser || arg0 instanceof SpecialBuilding)
                return;
            else type = Util.getType((PlayerUnit) arg0);
            TilePosition tile = arg0.getTilePosition();
            if (type.isBuilding()) {
                if (type.canAttack() || type.equals(UnitType.Terran_Bunker)) influence = defensive;
                else if (type.isResourceDepot()) influence = depot;
                else if (type.canProduce()) influence = offensive;
                else influence = neutral;
            } else if (type.isFlyer()) influence = flying;
            else if (type.isMechanical()) influence = mech;
            else influence = bio;
            if (destroyed) influence *= -1;
            if (Util.isEnemy(((PlayerUnit) arg0).getPlayer())) influence *= -1;
            updateCellInfluence(new MutablePair<>(new Point(tile.getY(), tile.getX()), influence), type.isBuilding());
        } catch (Exception e) {
            System.err.println("updateInMap Exception");
            e.printStackTrace();
        }
    }

    public void updateCellInfluence(MutablePair<Point, Integer> tile, boolean building) {
        map[tile.first.x][tile.first.y] += tile.second;
        if (!building) {
            int init_i = 0;
            if (tile.first.y - propagation > init_i) init_i = tile.first.y - propagation;
            int end_i = width - 1;
            if (tile.first.y + propagation < end_i) end_i = tile.first.y + propagation;
            int init_j = 0;
            if (tile.first.x - propagation > init_j) init_j = tile.first.x - propagation;
            int end_j = height - 1;
            if (tile.first.x + propagation < end_j) end_j = tile.first.x + propagation;
            for (int ii = init_i; ii <= end_i; ii++) {
                for (int jj = init_j; jj <= end_j; jj++) {
                    if (!(jj == tile.first.x && ii == tile.first.y))
                        map[jj][ii] += Math.round(tile.second / Math.pow(1 + Math.sqrt(Math.pow(ii - tile.first.y, 2) + Math.pow(jj - tile.first.x, 2)), 2));
                }
            }
        }
    }

    public void updateCellsInfluence(List<MutablePair<Point, Integer>> tiles) {
        for (MutablePair<Point, Integer> p : tiles) updateCellInfluence(p, true);
    }

    public double getInfluence(Point cell) {
        return map[cell.x][cell.y];
    }

    public int getMyInfluenceLevel() {
        int myInfluence = 0;
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                if (map[x][y] > 0) myInfluence += map[x][y];
            }
        }
        return myInfluence;
    }

    public int getEnemyInfluenceLevel() {
        int enemyInfluence = 0;
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                if (map[x][y] < 0) enemyInfluence += map[x][y];
            }
        }
        return enemyInfluence;
    }

    public int getMyInfluenceArea() {
        int count = 0;
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                if (map[x][y] > 0) count++;
            }
        }
        return count;
    }

    public double getEnemyInfluenceArea() {
        int count = 0;
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                if (map[x][y] < 0) count++;
            }
        }
        return count;
    }

    public MutablePair<Integer, Integer> getPosition(TilePosition start, boolean attack) {
        try {
            double count = 0;
            MutablePair<Integer, Integer> p = new MutablePair<>(-1, -1);
            for (int x = 0; x < height; x++) {
                for (int y = 0; y < width; y++) {
                    if (map[x][y] < count) {
                        if (attack && fixMap(x, y)) continue;
                        //count = map[x][y] / (2 * (Math.pow(1 + Math.sqrt(Math.pow(x - sY, 2) + Math.pow(y - sX, 2)), 2)));
                        count = getEuclideanDist(x, y, start);
                        p.first = x;
                        p.second = y;
                    }
                }
            }
            return p;
        } catch (Exception e) {
            System.err.println("getPosition InMap Exception");
            e.printStackTrace();
            return new MutablePair<>(-1, -1);
        }
    }

    private double getEuclideanDist(int x, int y, TilePosition start){
        return map[x][y] / (2 * (Math.pow(1 + Math.sqrt(Math.pow(x - start.getY(), 2) + Math.pow(y - start.getX(), 2)), 2)));
    }

    private double getGroundDist(int x, int y, TilePosition start){ // SLOW!!
        return Util.getGroundDistance(new TilePosition(y, x).toPosition(), start.toPosition());
    }

    private boolean fixMap(int x, int y) {
        TilePosition pos = new TilePosition(y, x);
        if (bw.getBWMap().isVisible(pos)) {
            for (Unit u : bw.getAllUnits()) {
                if (u.exists() && u.getTilePosition().equals(pos) && u instanceof Building) return false;
            }
            updateCellInfluence(new MutablePair<>(new Point(x, y), (int) map[x][y] * (-1)), true);
            return true;
        }
        return false;
    }

    // Writes Influence Map to a file
    public void writeMapa(String fileName) {
        FileWriter sw = null;
        try {
            sw = new FileWriter(fileName);
            for (int ii = 0; ii < height; ii++) {
                for (int jj = 0; jj < width; jj++) {
                    try {
                        sw.write(Integer.toString((int) map[ii][jj]));
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
