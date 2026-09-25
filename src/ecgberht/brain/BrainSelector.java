package ecgberht.brain;

import ecgberht.GameState;

public class BrainSelector extends BrainNode {

    public BrainSelector(String label, BrainNode... subNodes) {
        super(label, null, subNodes);
    }

    @Override
    public BrainStatus evaluate() {
        for (BrainNode child : children) {
            BrainStatus status = child.evaluate();
            if (status == BrainStatus.SUCCESS || status == BrainStatus.RUNNING) {
                return status;
            }
        }
        return BrainStatus.FAILURE;
    }
}
