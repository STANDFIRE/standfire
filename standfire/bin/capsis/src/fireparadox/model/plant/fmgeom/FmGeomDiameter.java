package fireparadox.model.plant.fmgeom;

import java.io.Serializable;

/**
 * A diameter of a FmGeom. Width and Height in %
 * 
 * @author S. Griffon - May 2007
 */
public class FmGeomDiameter implements Comparable, Cloneable, Serializable {
	
	private double height; // Height in % of crown height
	private double width; // %
	
	
	/**
	 * Creates a new instance of FmGeomDiameter with width and height = 0.0
	 */
	public FmGeomDiameter () {
		height = 50.;
		width = 100.;
	}
	
	/**
	 * Creates a new instance of FmGeomDiameter with width=w and height=h
	 */
	public FmGeomDiameter (double h, double w) {
		height=h;
		width=w;
	}
	
	@Override
	public FmGeomDiameter clone () {
		
		FmGeomDiameter fpd = null;
		
		try {
			fpd = (FmGeomDiameter)super.clone ();
		} catch(CloneNotSupportedException cnse) {
			// Should never be here because we implement Cloneable
		}
		
		return fpd;
	}
	
	public double getWidth () {return width;}
	
	public void setHeight (double height) {this.height = height;}
	
	public void setWidth (double width) {this.width = width;}
	
	public double getHeight () {return height;}
	
	@Override
	public String toString () {
		return "H = "+height+" W = "+width;
	}
	
	public int compareTo (Object o) {
		
		FmGeomDiameter fpDiameter = (FmGeomDiameter) o;
		if (getHeight () > fpDiameter.getHeight ()) {
			return 1;
		} else  if (getHeight () < fpDiameter.getHeight ()) {
			return -1;
		} else {
			return 0;
		}
	}
	
}
