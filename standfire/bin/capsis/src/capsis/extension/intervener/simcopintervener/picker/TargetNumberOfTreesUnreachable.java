package capsis.extension.intervener.simcopintervener.picker;

/**
 *
 * @author thomas.bronner@gmail.com
 */
public class TargetNumberOfTreesUnreachable extends Exception {

    String message;
    int target;
    int total;

    public TargetNumberOfTreesUnreachable(String message) {
        this.message = message;
    }

    public TargetNumberOfTreesUnreachable(int target, int total) {
        this.target = target;
        this.total = total;
    }

    public TargetNumberOfTreesUnreachable() {
    }

    @Override
    public String getMessage() {
        if (message != null) {
            return message;
        }
        return "Requested number of tree impossible to reach : target = " + target + " pickable trees = " + total;
    }
}
