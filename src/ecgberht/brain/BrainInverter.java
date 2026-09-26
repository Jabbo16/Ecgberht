package ecgberht.brain;

import ecgberht.GameState;

public class BrainInverter extends BrainNode {

    public BrainInverter(String label, BrainNode child) {
        super(label, null, child);
    }

    @Override
    public BrainStatus evaluate() {
        if (children.isEmpty()) return BrainStatus.SUCCESS;
        BrainStatus status = children.get(0).evaluate();
        if (status == BrainStatus.SUCCESS) {
            return BrainStatus.FAILURE;
        } else if (status == BrainStatus.FAILURE) {
            return BrainStatus.SUCCESS;
        }
        return status;
    }
}





