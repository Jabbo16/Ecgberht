package ecgberht.BehaviourTrees.Upgrade;

import ecgberht.GameState;
import org.iaie.btree.BehavioralTree.State;
import org.iaie.btree.task.leaf.Action;
import org.openbw.bwapi4j.type.UpgradeType;
import org.openbw.bwapi4j.unit.EngineeringBay;
import org.openbw.bwapi4j.unit.ResearchingFacility;

public class ChooseWeaponInfUp extends Action {

    public ChooseWeaponInfUp(String name, GameState gh) {
        super(name, gh);
    }

    @Override
    public State execute() {
        try {
            if (this.handler.UBs.isEmpty()) return State.FAILURE;
            for (ResearchingFacility u : this.handler.UBs) {
                if (!(u instanceof EngineeringBay)) continue;
                if (u.canUpgrade(UpgradeType.Terran_Infantry_Weapons) && !u.isResearching() && !u.isUpgrading() && this.handler.getPlayer().getUpgradeLevel(UpgradeType.Terran_Infantry_Weapons) < 3) {
                    this.handler.chosenUnitUpgrader = u;
                    this.handler.chosenUpgrade = UpgradeType.Terran_Infantry_Weapons;
                    return State.SUCCESS;
                }
            }
            return State.FAILURE;
        } catch (Exception e) {
            System.err.println(this.getClass().getSimpleName());
            e.printStackTrace();
            return State.ERROR;
        }
    }
}
