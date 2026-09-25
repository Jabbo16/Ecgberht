package ecgberht.brain;

import ecgberht.GameState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BrainNode {
    protected final String label;
    protected final List<BrainNode> children;
    protected GameState gameState;

    public BrainNode(String label, GameState gs) {
        this.label = label;
        this.gameState = gs;
        this.children = new ArrayList<>();
    }

    public BrainNode(String label, GameState gs, BrainNode... subNodes) {
        this(label, gs);
        this.children.addAll(Arrays.asList(subNodes));
    }

    public void addSubNode(BrainNode node) {
        this.children.add(node);
    }

    public abstract BrainStatus evaluate();
}
