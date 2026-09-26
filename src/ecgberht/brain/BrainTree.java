package ecgberht.brain;

public class BrainTree {
    private final String treeName;
    private BrainNode rootNode;

    public BrainTree(String name) {
        this.treeName = name;
    }

    public void addSubNode(BrainNode node) {
        this.rootNode = node;
    }

    public BrainNode getRootNode() {
        return this.rootNode;
    }

    public void executeTree() {
        if (rootNode != null) {
            rootNode.evaluate();
        }
    }
}





