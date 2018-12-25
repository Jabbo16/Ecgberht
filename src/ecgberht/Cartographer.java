package ecgberht;

import org.bk.ass.path.JPS;

public class Cartographer {
    private boolean[][] collisionGrid = new boolean[1024][1024];
    private JPS.Map mapJPS;

    private JPS jps;

    public Cartographer() {
        initCollisionGrid();
    }

    private void initCollisionGrid() {
    }
}
