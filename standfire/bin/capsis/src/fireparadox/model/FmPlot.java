package fireparadox.model;

import java.awt.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.util.Log;
import jeeb.lib.util.TicketDispenser;
import jeeb.lib.util.Vertex2d;
import jeeb.lib.util.Vertex3d;
import capsis.defaulttype.RectangularPlot;
import capsis.defaulttype.Tree;
import capsis.kernel.GScene;

/**	FiPlot is an agregate of FireCells.
 *
 *	@author O. Vigy, E. Rigaud - september 2006
 */
public class FmPlot extends RectangularPlot implements Serializable {
	// WARNING: if references to objects (not primitive types) are added here,
	// implement a "public Object clone ()" method (see RectangularPlot.clone () for template)
	
	/**	This class contains immutable instance variables for a logical RectangularPlot.
	 *	@see Tree
	 */
	public static class Immutable extends RectangularPlot.Immutable {
		public double cellArea;
		public String name;
		public double altitude;
		// Related to sappling memorization in cells
		public int sapplingMemorySize = 5;	// fc - 5.4.2005
		public FmInitialParameters sets;
		//~ public Collection<FirePolygon> polygons;
		public Collection<Polygon> polygons;
	}
	
	// Index of the current date in the cells' sappling number arrays
	// possible values : 0 < sapplingIndex < settings.sapplingMemorySize
	private int sapplingIndex;
	
	/** Override Immutable accessor */
	@Override
	protected void createImmutable () { 
		setImmutable (new Immutable ()) ;		 
	}
	@Override
	protected Immutable getImmutable() { return (Immutable)super.getImmutable(); }
	
	
	/**	Constructor.
	 */
	public FmPlot (GScene stand, String name, double cellWidth, double altitude,
		Rectangle.Double r, FmInitialParameters sets) throws Exception {
		// fc - 14.5.2007 - added name...
		super (stand, cellWidth);
		getImmutable().name = name;
		getImmutable().altitude = altitude;
		getImmutable().cellArea = cellWidth * cellWidth;
		
			// fc - 17.4.2008
		//~ getImmutable().polygons = new ArrayList<FirePolygon> ();
		getImmutable().polygons = new ArrayList<Polygon> ();
			
		int nLin = (int) (r.getHeight () / cellWidth);
		int nCol = (int) (r.getWidth () / cellWidth);
		if (cellWidth > 0 && nLin * cellWidth != r.getHeight ()) {
			throw new Exception ("plot height must be a multiple of cell width = "+cellWidth);}
		if (cellWidth > 0 && nCol * cellWidth != r.getWidth ()) {
			throw new Exception ("plot width must be a multiple of cell width = "+cellWidth);}
		getImmutable().nLin = nLin;
		getImmutable().nCol = nCol;
		
		// 2. Prepare a cell matrix.
		defineMatrix (nLin, nCol);	// fc - 12.12.2002
		
		// 3. Set plot bottom left origin.
		setOrigin (new Vertex3d (r.getX (), r.getY (), 0d));
		setXSize (r.getWidth ());
		setYSize (r.getHeight ());
		
		// Create the cells
		TicketDispenser cellIdDispenser = new TicketDispenser ();
		for (int i = 0; i < getImmutable().nLin; i++) {
			for (int j = 0; j < getImmutable().nCol; j++) {
				// x of the bottom left corner of the Cell.
				double a = getOrigin ().x + j*getCellWidth ();
				
				// y of the bottom left corner of the Cell.
				double b = getOrigin ().y + (getImmutable().nLin - i - 1)*getCellWidth ();
				
				// Cells relative coordinates = line, column (i, j).
				int cellId = cellIdDispenser.getNext ();
				addCell (new FmCell (this, cellId, 0, new Vertex2d (a, b), i, j));
			}
		}
		
		// Create a table for fast tree registering in the good cell
		createTableBottomLeft_CellId ();
		
		sapplingIndex = 0;	// for the plot under the root step
		getImmutable().sets = sets;	// ref to FiInitialParameters
		
		// Update vertices list - fc + ov - 15.5.2007
		double xMin = r.getX ();
		double yMin = r.getY ();
		double xMax = r.getX ()+r.getWidth ();
		double yMax = r.getY ()+r.getHeight ();
		Collection vs = new ArrayList ();
		vs.add (new Vertex3d (xMin, yMin, altitude));
		vs.add (new Vertex3d (xMin, yMax, altitude));
		vs.add (new Vertex3d (xMax, yMax, altitude));
		vs.add (new Vertex3d (xMax, yMin, altitude));
		setVertices (vs);
	}
	
		
	/**	Redefines superclass initPlot to cancel it. Replaced by customInitPlot ().
	 */
	@Override
	protected void initPlot () {}


	/**	Clones a FiPlot.
	*/
	@Override
	public Object clone () {
		try {
			FmPlot p = (FmPlot) super.clone ();
			
			// polygons is in immutable at present time: not cloned at present time
			//~ p.polygons = ...
			
			return p;
		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPlot.clone ()", "Error while cloning FiPlot", e);
			return null;
		}
	}



		
	/**	Cell area
	 */
	public double getCellArea () {return getImmutable().cellArea;}
	
	/**	Name
	 */
	public String getName () {return getImmutable().name;}
	
	/**	Altitude
	 */
	public double getAltitude () {return getImmutable().altitude;}
	
	/**	Sappling index management
	 */
	public int getSapplingIndex () {return sapplingIndex;}
	public void setSapplingIndex (int i) {sapplingIndex = i;}
	
	/**	Sappling memory size management
	 */
	public int getSapplingMemorySize () {return getImmutable().sapplingMemorySize;}
	public void setSapplingMemorySize (int i) {getImmutable().sapplingMemorySize = i;}
	
	//~ public void add (FirePolygon p) {
	public void add (Polygon p) {
		//~ if (getImmutable().polygons == null) {
				//~ getImmutable().polygons = new ArrayList<FirePolygon> ();}
		getImmutable().polygons.add (p);
	}
	
	//~ public Collection<FirePolygon> getPolygons () {return getImmutable().polygons;}
	public Collection<Polygon> getPolygons () {return getImmutable().polygons;}

	
}
