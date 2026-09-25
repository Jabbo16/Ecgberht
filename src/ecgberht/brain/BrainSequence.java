package ecgberht.brain;

import ecgberht.GameState;

public class BrainSequence extends BrainNode {

    public BrainSequence(String label, BrainNode... subNodes) {
        super(label, null, subNodes);
    }

    @Override
    public BrainStatus evaluate() {
        for (BrainNode child : children) {
            BrainStatus status = child.evaluate();
            if (status != BrainStatus.SUCCESS) {
                return status;
            }
        }
        return BrainStatus.SUCCESS;
    }
}
