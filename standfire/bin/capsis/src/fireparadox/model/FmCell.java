package fireparadox.model;

//~ import java.awt.*;
import java.io.Serializable;

import jeeb.lib.util.Vertex2d;
import jeeb.lib.util.Vertex3d;
import capsis.defaulttype.SquareCell;

/**	FiCell is a square part of Plot.
 *
 *	@author O. Vigy, E. Rigaud - september 2006
 */
public class FmCell extends SquareCell implements Serializable {
	
	// WARNING: if references to objects (not primitive types) are added here,
	// implement a "public Object clone ()" method (see RectangularPlot.clone () for template)
	
	// This array memorizes the sappling numbers for the recent years
	// Its size is plot.getSapplingMemorySize ()
	// For the current date (stand.getDate ()), the matching index is plot.getSapplingIndex ()
	// see setSapplingNumber () and getSapplingNumber () below
	private int[] saplingNumber;
	
	
	/**	Constructor.
	 */
	public FmCell (FmPlot plot,
		int id,
		int motherId,
		Vertex2d origin,
		int iGrid,
		int jGrid) {	// motherId = 0 (no cell nesting)
		
		super (plot, id, motherId, Vertex3d.convert (origin),
			iGrid, jGrid);
		
		int sapplingMemorySize = (plot).getSapplingMemorySize ();
		saplingNumber = new int[sapplingMemorySize];
	}
	
	/**	Set the sappling number at the given date.
	 *	ex: currentDate = 20, date = 18 : change sappling number 2 years ago (ex: mortality)
	 */
	public void setSapplingNumber (int date, int number) throws Exception {
		int i = getSapplingI (date);
		saplingNumber[i] = number;
	}
	
	/**	Return the sappling number that appeared at the given date in the past.
	 *	ex: currentDate = 20, date = 18 : how many sappling 2 years ago ?
	 */
	public int getSapplingNumber (int date) throws Exception {
		int i = getSapplingI (date);
		return saplingNumber[i];
	}
	
	//	Utility method
	//
	private int getSapplingI (int date) throws Exception {
		FmPlot p = (FmPlot) plot;
		FmStand stand = (FmStand) p.getScene ();
		int sapplingMemorySize = p.getSapplingMemorySize ();
		int sapplingIndex = p.getSapplingIndex ();
		
		int currentDate = stand.getDate ();
		if (date > currentDate) {throw new Exception (
			"Wrong date ("+date+"), must be <= currentDate ("+currentDate+")");}
		int nbYearsAgo = currentDate - date;
		if (nbYearsAgo >= sapplingMemorySize) {throw new Exception (
			"Sappling memory overflow (max="+sapplingMemorySize
			+"): date ("+date+") is too old");}
		int i = (sapplingIndex + sapplingMemorySize - nbYearsAgo) % sapplingMemorySize;
		return i;
	}
	
	/**	Clone a FiCell: first calls super.clone (), then clone the FiCell properties.
	 *	deals with sappling
	 */
	@Override
	public Object clone () {
		FmCell c = (FmCell) super.clone ();	// calls protected Object Object.clone () {}
		
		// Cloning the saplingNumber array
		int [] copy = new int[saplingNumber.length];
		for (int i = 0 ; i < saplingNumber.length; i++) {
			copy[i] = saplingNumber[i];
		}
		c.saplingNumber = copy;
		
		return c;
	}
	
	
	// This method is for control in the capsis interactive inspectors.
	// For saplingNumber management, better use setSapplingNumber () and getSapplingNumber ()
	// Please do not use this method - fc - 5.4.2005
	public int[] getSapplingNumber () {return saplingNumber;}
	// This method is for control in the capsis interactive inspectors.
	// Please do not use this method - fc - 5.4.2005
	public int getSapplingIndex () {return ((FmPlot) plot).getSapplingIndex ();}
	
}
