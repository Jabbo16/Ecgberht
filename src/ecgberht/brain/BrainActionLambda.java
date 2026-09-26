package ecgberht.brain;

import java.util.function.Supplier;

public class BrainActionLambda extends BrainNode {

    private final Supplier<BrainStatus> action;

    public BrainActionLambda(String label, Supplier<BrainStatus> action) {
        super(label, null);
        this.action = action;
    }

    @Override
    public BrainStatus evaluate() {
        return action.get();
    }
}





