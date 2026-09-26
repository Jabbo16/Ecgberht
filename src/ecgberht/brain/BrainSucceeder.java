package ecgberht.brain;

import ecgberht.GameState;

public class BrainSucceeder extends BrainNode {

    public BrainSucceeder(String label, BrainNode child) {
        super(label, null, child);
    }

    @Override
    public BrainStatus evaluate() {
        if (!children.isEmpty()) {
            children.get(0).evaluate();
        }
        return BrainStatus.SUCCESS;
    }
}





