package ecgberht.brain;

import ecgberht.GameState;

public class BrainParallel extends BrainNode {
    
    private final int requireSuccessCount;

    public BrainParallel(String label, int requireSuccessCount, BrainNode... subNodes) {
        super(label, null, subNodes);
        this.requireSuccessCount = requireSuccessCount;
    }

    @Override
    public BrainStatus evaluate() {
        int successCount = 0;
        int failureCount = 0;
        
        for (BrainNode child : children) {
            BrainStatus status = child.evaluate();
            if (status == BrainStatus.SUCCESS) {
                successCount++;
            } else if (status == BrainStatus.FAILURE) {
                failureCount++;
            }
        }
        
        if (successCount >= requireSuccessCount) {
            return BrainStatus.SUCCESS;
        }
        
        // If it's impossible to reach the required success count
        if (failureCount > children.size() - requireSuccessCount) {
            return BrainStatus.FAILURE;
        }
        
        return BrainStatus.RUNNING;
    }
}





