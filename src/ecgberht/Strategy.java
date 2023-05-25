package ecgberht;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.type.UpgradeType;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
public abstract class Strategy implements Comparable<Strategy> {

    public boolean bunker = false;
    public boolean proxy = false;
    public boolean harass = true;
    public int armyForAttack = 0;
    public int armyForExpand = 20;
    public int armyForTurret = 0;
    public int facPerCC = 0;
    public int numBays = 0;
    public int numCCForPort = 0;
    public int workerGas = 3;
    public int numCCForScience = 0;
    public int numRaxForAca = 2;
    public int numRaxForFac = 0;
    public int numFacForPort = 1;
    public int extraSCVs = 3;
    public int portPerCC = 0;
    public int raxPerCC = 0;
    public int supplyForFirstRefinery = 0;
    public Set<TechType> techToResearch = new HashSet<>();
    public Set<UnitType> buildAddons = new HashSet<>();
    public Set<UnitType> buildUnits = new HashSet<>();
    public Set<UnitType> trainUnits = new HashSet<>();
    public Set<UpgradeType> upgradesToResearch = new HashSet<>();
    public String name = "";
    public int armyForBay = 0;
    public int facForArmory = 0;
    public int numArmories = 0;

    public abstract void initStrategy();

    public abstract void initTrainUnits();

    public abstract void initBuildUnits();

    public abstract void initBuildAddons();

    public abstract void initTechToResearch();

    public abstract void initUpgradesToResearch();


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Strategy)) return false;
        Strategy strat = (Strategy) o;
        return strat.name.equals(this.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(Strategy v1) {
        return this.name.compareTo(v1.name);
    }

    public boolean requiredUnitsForAttack() {
        return true;
    }
}
