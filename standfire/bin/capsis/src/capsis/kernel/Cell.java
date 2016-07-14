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

import jeeb.lib.util.Identifiable;
import jeeb.lib.util.Vertex3d;

/**	Interface for plot cells.
 *	A mother Cell may be divided into nested Cells at a lower level. 
 *	If elements (e.g. trees) are registered in the Cells, only one level is 
 *	the element level and the elements are registered at this unique level.
 *	Plot and Cells are a static infrastructure: all the cells are built on the 
 *	first Step of the project and cells can never be removed.
 *	At each Step, the cells are new instances with the same ids and may have 
 *	properties changing in time. 
 * 
 *	@author F. de Coligny - march 2001 / june 2001, september 2010
 * 	@see Plot, AbstractPlot
 */
public interface Cell extends Identifiable {

// The plot this Cell is part of
	
	public Plot getPlot ();
	public void setPlot (Plot plot);

	
// Nested Cells management (optional)
	
	/**	Mother is the Cell containing this Cell
	 */
	public Cell getMother ();
	
	public void setMother (Cell cell);
	
	/**	Returns true if this cell contains nested Cells, i.e. is their mother
	 */
	public boolean isMother ();
	
	/**	Returns the list of Cells nested in this Cell
	 */
	public Collection<Cell> getCells ();

	/**	Add a nested Cell
	 */
	public void addCell (Cell cell);

	/**	Returns the nesting level. If one level only, this is level 1.	
	 */
	public int getLevel ();
	
	/**	Cells may contain elements (e.g. trees located within their shape). It is 
	 * 	possible only at one level of nesting: the element level.
	 */
	public boolean isElementLevel();


// Cell basic geometry
	
	/**	Returns the origin of the Cell: the point at the bottom left 
	 * 	corner if the Cell is Rectangular, or a point of the shape if not.	
	 */
	public Vertex3d getOrigin ();  // Vertex3d
	
	/**	Returns the list of vertices of the Cell polygon.
	 */
	public Collection<Vertex3d> getVertices ();  // Vertex3d inside
	
	/**	Returns the area of the Cell in m2
	 */
	public double getArea ();
	
	/**	Returns the geometrical shape of the Cell (from the top)	
	 */
	public Shape getShape ();
	
	public void setOrigin (Vertex3d v);
	public void setVertices (Collection<Vertex3d> c);
	public void setArea (double a);

	
// Cloning cells in the history (Cell 1 has different instances at date 0, 1, ... n).
	
	public Object clone();		

	
// String representations
	
	public String toString ();
	public String bigString ();

	

}

