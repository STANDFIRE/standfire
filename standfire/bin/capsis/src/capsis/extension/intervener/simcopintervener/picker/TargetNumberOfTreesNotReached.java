package capsis.extension.intervener.simcopintervener.picker;

/**
 *
 * @author thomas.bronner@gmail.com
 */
public class TargetNumberOfTreesNotReached extends Exception {

    String message;
    int target;
    int picked;

    public TargetNumberOfTreesNotReached(String message) {
        this.message = message;
    }

    public TargetNumberOfTreesNotReached(int target, int picked) {
        this.target = target;
        this.picked = picked;
        this.message = "Requested number of tree not reached : " + picked + " out of " + target;
    }

    public TargetNumberOfTreesNotReached() {
    }

    @Override
    public String getMessage() {
        return message;
    }
}
