/** 
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 1999-2010 INRA 
 * 
 * Authors: F. de Coligny, S. Dufour-Kowalski, 
 * 
 * This file is part of Capsis
 * Capsis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Capsis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU lesser General Public License
 * along with Capsis.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package capsis.kernel;

import java.awt.Shape;
import java.util.Collection;

import jeeb.lib.util.Spatialized;
import jeeb.lib.util.Vertex3d;

/**	A plot is a geometric description of the terrain associated 
 * 	to the GScene. It has an origin and a bounding rectangle and may be 
 * 	divided in cells. The cells may be nested in several levels (cells 
 * 	may contain cells of the lower level). Some cells may contain trees, 
 * 	they are said to be tree level cells.
 *
 *	@author F. de Coligny - march 2001, september 2010
 */
public interface Plot<T extends Cell> {

	/**	The plot is linked to a GSCene
	 */
	public GScene getScene ();
	public void setScene (GScene s);

// Cells management methods
	
	/**	Returns all the cells (all nested levels if any)
	 */
	public Collection<T> getCells ();  // all cells (in all nesting levels if any) are returned
	
	/**	Returns only the cells at the given nesting level
	 */
	public Collection<T> getCellsAtLevel (int l);
	
	/**	Returns the cell with the given id (starting to 1)
	 */
	public T getCell (int id);
	
	/**	Returns a cell (maybe not the cell with id 1)
	 */
	public T getFirstCell ();
	
	/**	Add a cell in the plot
	 */
	public void addCell (T cell);  // for cells at levels != 1, this method is called by Cell.addCell (Cell)
	
	/**	Returns true if the plot contains at least one cell
	 */
	boolean hasCells();
	
	/**	Returns true if the plot contains no cells at all	
	 */
	public boolean isEmpty ();
	
	/**	Returns the reference to the cell containing the given spatialized	
	 */
	public T matchingCell (Spatialized s);

// Plot simple geometry	
	
	/**	Returns the origin of the plot (the point at the bottom left 
	 * 	corner of the bounding rectangle when looking from the top)	
	 */
	public Vertex3d getOrigin ();
	
	/**	Returns the size of the bounding rectangle on the X axis (from the top)	
	 */
	public double getXSize ();
	
	/**	Returns the size of the bounding rectangle on the Y axis (from the top)	
	 */
	public double getYSize ();
	
	/**	Returns the area of the plot in m2. 
	 * 	May be the bounding rectangle area or the area of a polygon 
	 * 	within this rectangle.	
	 */
	public double getArea ();  // m2
	
	/**	Updates the plot size to include the given (x, y) point 
	 */
	public void adaptSize (Spatialized s);
	
	/**	Returns the list of vertices of the plot polygon.
	 * 	This may be the vertices of the bounding rectangle or the vertices of 
	 * 	a polygon within this rectangle.	
	 */
	public Collection<Vertex3d> getVertices ();
	
	public void setOrigin (Vertex3d v);	
	public void setXSize (double s);	
	public void setYSize (double s);	
	public void setArea (double a);	
	public void setVertices (Collection<Vertex3d> c);	
	
	/**	Returns the geometric shape of the plot (from the top)	
	 */
	public Shape getShape ();

// Miscellaneous
	
	/**	A short description of the plot as a String	
	 */
	public String toString ();
	
	/**	A more complete description than toString ()	
	 */
	public String bigString ();

	/**	Returns a clone of the plot, maybe not an accurate copy. 
	 * 	Can be used when making the steps history during the simulations.	
	 */
	public Object clone ();
	
	

}

