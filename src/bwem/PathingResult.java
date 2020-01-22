package bwem;

/**
 * Result of a path query including the approximated length of the path.
 */
public final class PathingResult {

    private final CPPath cpPath;
    private final int approxDistance;

    PathingResult(CPPath cpPath, int approxDistance) {
        this.cpPath = cpPath;
        this.approxDistance = approxDistance;
    }

    public CPPath getCPPath() {
        return cpPath;
    }

    /**
     * Returns the approximate length of the path.
     */
    public int getLength() {
        return approxDistance;
    }
}
