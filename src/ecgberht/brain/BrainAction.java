package ecgberht.brain;

import ecgberht.GameState;

public abstract class BrainAction extends BrainNode {

    public BrainAction(String label, GameState gs) {
        super(label, gs);
    }

    @Override
    public BrainStatus evaluate() {
        return execute();
    }

    public abstract BrainStatus execute();
}





