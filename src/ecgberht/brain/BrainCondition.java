package ecgberht.brain;

import java.util.function.BooleanSupplier;

public class BrainCondition extends BrainNode {

    private final BooleanSupplier condition;

    public BrainCondition(String label, BooleanSupplier condition) {
        super(label, null);
        this.condition = condition;
    }

    @Override
    public BrainStatus evaluate() {
        if (condition.getAsBoolean()) {
            return BrainStatus.SUCCESS;
        } else {
            return BrainStatus.FAILURE;
        }
    }
}
