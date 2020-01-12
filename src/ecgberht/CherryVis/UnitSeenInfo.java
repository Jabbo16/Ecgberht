package ecgberht.CherryVis;

import org.openbw.bwapi4j.unit.Unit;

class UnitSeenInfo {
    private int id;
    private int type;
    private int x;
    private int y;

    UnitSeenInfo(Unit u) {
        id = u.getId();
        type = u.getType().getId();
        x = u.getTilePosition().toWalkPosition().getX();
        y = u.getTilePosition().toWalkPosition().getY();
    }
}
