package capsis.lib.crobas;

import java.io.Serializable;

/**
 * Represent the concept of branch. A branch is defined by it diameter, it insertion
 * angle and it azimuth angle.
 * 
 * @author R. Audet <raymond.audet@gmail.com> - 13.6.2008
 */
public class PipeQualBranch implements Serializable {	// fc - 12.9.2008 - Serializable for Project > SaveAs

    private double insertionAngle;		// Branch insertion angle, degrees
    private double azimuthAngle;		// Branch orientation, 0 -> North, degrees
    private double diameter;			// Branch diameter, cm

    public PipeQualBranch() {
        this.insertionAngle = 0.0;
        this.azimuthAngle = 0.0;
        this.diameter = 0.0;
    }

    public PipeQualBranch(double azimuthAngle, double insertionAngle, double diameter) {
        this.insertionAngle = insertionAngle;
        this.azimuthAngle = azimuthAngle;
        this.diameter = diameter;
    }

    /** Branch accessors
    */
    
    public double getInsertionAngle() {
        return insertionAngle;
    }

    public double getAzimuthAngle() {
        return azimuthAngle;
    }

    public double getDiameter() {
        return diameter;
    }

    public void setInsertionAngle(double insertionAngle) {
        this.insertionAngle = insertionAngle;
    }

    public void setDiameter(double diameter) {
        this.diameter = diameter;
    }

    /**
     * Returns a <code>String</code> representation of this branch.
     */
    @Override
    public String toString() {
        return "diameter = " + diameter + "\t azimuthAngle = " + azimuthAngle
            + "\t insertionAngle = " + insertionAngle;
    }
}
