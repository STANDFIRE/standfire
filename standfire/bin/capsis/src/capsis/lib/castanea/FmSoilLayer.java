package capsis.lib.castanea;

import java.io.Serializable;

import jeeb.lib.util.Log;

/**	FmSoilLayer : a layer of soil of Dynaclim model.
*
*	@author Hendrik Davi - march 2006
*/
public class FmSoilLayer implements Serializable, Cloneable {

	// WARNING: if references to objects (not primitive types) are added here,
	// implement a "public Object clone ()" method (see RectangularPlot.clone () for template)

	private int rank;				// 1 is top
	private double thickness;		// m

//	private double respiration;		// µmol co2 m-2 s-1

	// other properties here...


	/**	Constructor for new logical FmSoilLayer.
	*/
	public FmSoilLayer (int rank,
						double thickness) {

		this.rank = rank;
		this.thickness = thickness;
	}

	/**	Clone method.
	*/
	public Object clone () {
		try {
			FmSoilLayer l = (FmSoilLayer) super.clone ();	// calls protected Object Object.clone () {}

			return l;
		} catch (Exception e) {
			Log.println (Log.ERROR, "FmSoilLayer.clone ()", "Error while cloning", e);
			return null;
		}
	}


}

