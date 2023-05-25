
package ecgberht;

import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.type.Race;

import static ecgberht.Ecgberht.getGs;

public class UnitInfoCalculations {
    private UnitInfoCalculations() {
        throw new IllegalStateException("Utility class");
    }
    private static final int PROTOSS_SHIELD_REGEN = 7;
    private static final int ZERG_REGEN = 4;


    public static int getExpectedHealth(UnitInfo unitInfo) {
        PlayerUnit unit = unitInfo.unit;
        int lastVisibleFrame = unitInfo.lastVisibleFrame;
        int health = unit.getHitPoints();
        int maxHitPoints = unit.getType().maxHitPoints();
        Race race = unit.getType().getRace();

        if (race == Race.Zerg && unit.getType().regeneratesHP()) {
            return Math.min(((getGs().frameCount - lastVisibleFrame) * ZERG_REGEN >> 8) + health, maxHitPoints);
        }

        return health;
    }

    public static int getExpectedShields(UnitInfo unitInfo) {
        PlayerUnit unit = unitInfo.unit;
        int lastVisibleFrame = unitInfo.lastVisibleFrame;
        int shields = unit.getShields();
        int maxShields = unit.getType().maxShields();
        Race race = unit.getType().getRace();

        if (race == Race.Protoss) {
            return Math.min(((getGs().frameCount - lastVisibleFrame) * PROTOSS_SHIELD_REGEN >> 8) + shields, maxShields);
        }

        return shields;
    }
}
