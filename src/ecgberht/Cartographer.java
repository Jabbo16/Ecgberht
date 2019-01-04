package ecgberht;

import org.bk.ass.path.Jps;
import org.bk.ass.path.Map;
import org.bk.ass.path.Position;
import org.bk.ass.path.Result;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.WalkPosition;

import static ecgberht.Ecgberht.getGs;

public class Cartographer {
    private int mapTileHeight;
    private int mapTileWidth;
    public TilePosition mapCenter;
    private boolean[][] walkableGrid = new boolean[1024][1024];
    private boolean[][] tileWalkableGrid;
    private Jps mapJPS;
    private Jps mapJPSTile;

    public Cartographer(int width, int height) {
        mapTileWidth = width;
        mapTileHeight = height;
        tileWalkableGrid = new boolean[mapTileHeight][mapTileWidth];
        mapCenter = new TilePosition(mapTileWidth / 2, mapTileHeight / 2);
        initWalkableGrids();
    }

    public Result getWalkablePath(WalkPosition start, WalkPosition end) {
        return mapJPS.findPath(new Position(start.getX(), start.getY()), new Position(end.getX(), end.getY()));
    }

    public Result getWalkablePath(TilePosition start, TilePosition end) {
        return mapJPS.findPath(new Position(start.getX(), start.getY()), new Position(end.getX(), end.getY()));
    }

    private void initWalkableGrids() {
        for (int ii = 0; ii < mapTileWidth * 4; ii++) {
            for (int jj = 0; jj < mapTileHeight * 4; jj++) {
                walkableGrid[jj][ii] = getGs().getGame().getBWMap().isWalkable(ii, jj);
            }
        }
        for (int ii = 0; ii < mapTileWidth; ii++) {
            for (int jj = 0; jj < mapTileHeight; jj++) {
                tileWalkableGrid[jj][ii] = getGs().getGame().getBWMap().isWalkable(ii * 4, jj * 4);
            }
        }
        mapJPS = new Jps(Map.fromBooleanArray(walkableGrid));
        mapJPSTile = new Jps(Map.fromBooleanArray(tileWalkableGrid));
    }

    enum Resolution {
        TilePosition, WalkPosition
    }

    ;
}
