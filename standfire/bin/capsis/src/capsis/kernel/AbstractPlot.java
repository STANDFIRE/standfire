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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jeeb.lib.util.Spatialized;
import jeeb.lib.util.Vertex3d;

/**	An abstract superclass for the plot subclasses. A plot is a geometrical 
 * 	description of the terrain associated to the GScene.
 *	@author F. de Coligny - march 2001, september 2010
 */
public abstract class AbstractPlot<T extends Cell> implements Plot<T>, Serializable {
	
	/**	This inner class describes the properties that do not change 
	 * 	when considering several instances of AbstractPlot in the Project
	 * 	time history. E.g. The plot at time 0 and 10 years both have the 
	 * 	same area. This was introduced to save memory space.
	 */
	public static class Immutable implements Cloneable, Serializable {
		private static final long serialVersionUID = 1L;
		public Vertex3d origin;  // origin: point at the bottom left corner of the bounding rectangle from top view
		public double xSize;
		public double ySize;
		public double area;
	}
	
	private static final long serialVersionUID = 1L;
	/**	The reference to the immutable part of the AbstractPlot */
	protected Immutable immutable;
	/**	The scene */
	private GScene scene;
	/**	The Map of cells (optional) */
	protected Map<Integer,T> cells;

	protected boolean autoSize = true;

	
	/**	Default constructor 
	 */
	public AbstractPlot() {
		createImmutable ();

		immutable.origin = null;
		immutable.xSize = 0d;
		immutable.ySize = 0d;

		cells = new HashMap<Integer, T>();

	}

	
// Immutable part management
	
	/**	Creates an Immutable object whose class is declared at one level of the hierarchy.
	 *	This is -called- only in the constructor of the object at the top of the hierarchy.
	 *	If the Immutable is overriden in a subclass, the subclass must -override- this method
	 *	(same body) to create an Immutable defined in this subclass.
	 */
	protected void createImmutable () {immutable = new Immutable ();}
	
	protected Immutable getImmutable() {return (Immutable) immutable;}
	protected void setImmutable (Immutable i) {immutable = i;}

	
// Linked to the scene
	
	public GScene getScene () {return scene;}
	public void setScene (GScene s) {scene = s;}

	
// Cells management: not in AbstractPlot (abstract here)

	abstract public void addCell (T cell);	// for cells at levels!=1, this method is called by GCell.addCell (GCell)
	abstract public Collection<Vertex3d> getVertices ();
	abstract public void setVertices (Collection<Vertex3d> c);
	abstract public Shape getShape ();
	abstract public String bigString ();
	abstract public T matchingCell (Spatialized t);

	public Collection<T> getCellsAtLevel1 () {return getCellsAtLevel (1);}
	public Collection<T> getCellsAtLevel2 () {return getCellsAtLevel (2);}
	public Collection<T> getCellsAtLevel3 () {return getCellsAtLevel (3);}

	@Override
	public T getCell (int id) {
		if (id == 0) {return null;}
		return cells.get (id);
	}

	@Override
	public Collection<T> getCells () {
		return cells.values ();
	}

	@Override
	public boolean hasCells () {
		return !cells.isEmpty();
	}
	
	@Override
	public boolean isEmpty () {
		return cells.isEmpty ();
	}

	@Override
	public Collection<T> getCellsAtLevel (int l) {
		Collection<T> v = new ArrayList<T> ();
		for (T c : getCells ()) {
			if (c.getLevel () == l) {v.add (c);}
		}
		return v;
	}

	@Override
	public T getFirstCell () {
		if (cells.isEmpty ()) {return null;}
		return getCells ().iterator ().next ();
	}

	
// Simple geometry

	@Override
	public Vertex3d getOrigin () {

		if (immutable.origin != null) {
			return immutable.origin;
		} else {
			return new Vertex3d (0, 0, 0);	// fc - 21.9.2006 - SVLollypop / Spatializers need an origin even for non spatialized models (ex: PP3)
		}
	}

	@Override
	public void setOrigin (Vertex3d origin) {immutable.origin = origin;}
	
	@Override
	public void setXSize (double w) {immutable.xSize = (float) w;}
	
	@Override
	public void setYSize (double h) {immutable.ySize = (float) h;}
	
	@Override
	public double getXSize () {
		if (immutable.xSize >= 0) {
			return immutable.xSize;
		} else {
			double sqrt = Math.sqrt (getArea ());
			if(sqrt != Double.NaN) immutable.xSize = sqrt;
			return sqrt;
		}
	}
	
	@Override
	public double getYSize () {
		if (immutable.ySize >= 0) {
			return immutable.ySize;
		} else {
			double sqrt = Math.sqrt (getArea ());
			if(sqrt != Double.NaN) immutable.ySize = sqrt;
			return sqrt;
		}
	}
	
	@Override
	public double getArea () {
		if (immutable.area > 0) {
			return immutable.area;
		} else if(immutable.xSize > 0 && immutable.ySize > 0){
			return immutable.xSize * immutable.ySize;
		} else {
			return -1;  // not set, could not be computed
		}
	}
	
	@Override
	public void setArea (double a) {immutable.area = a;}

	/**	Updates the plot size to include the given (x, y) point.
	 * 	Only if autoSize is true. 
	 */
	@Override
	public void adaptSize(Spatialized t) {
		
		if(!autoSize) {return;}

		if (immutable.origin == null) {
			setOrigin (new Vertex3d (t.getX (), t.getY (), t.getZ ()));
			setXSize (0);
			setYSize (0);
		} 

		double x = t.getX();
		double y = t.getY();

		double upright_x = immutable.origin.x + getXSize();
		double upright_y = immutable.origin.y + getYSize();

		if (x < immutable.origin.x) immutable.origin.x = x;
		if (y < immutable.origin.y) immutable.origin.y = y;
		if (x > upright_x) upright_x = x;
		if (y > upright_y) upright_y = y;

		immutable.xSize = upright_x - immutable.origin.x;
		immutable.ySize = upright_y - immutable.origin.y;


	}

	
// Cloning
	
	public Object clone () {
		try {
			AbstractPlot<T> s = (AbstractPlot<T>) super.clone();
			s.immutable = immutable;  // same immutable part
			return s;

		} catch (CloneNotSupportedException e) {
			return null;
		}

	}
	

// String descriptions
	
	@Override
	public String toString () {
		StringBuffer b = new StringBuffer ();
		b.append (this.getClass().getName());
		if (scene != null) {
			b.append (" (");
			b.append (scene.toString ());
			b.append (")");
		}
		return b.toString ();	// light version
	}
	
	
	protected String completeString () {
		StringBuffer sb = new StringBuffer ();
		sb.append ("Cells details {\n");
		for (Cell cell: getCells ()) {

			sb.append (cell.bigString ());
			sb.append ("\n");
		}
		sb.append ("}\n");
		return sb.toString ();
	}

}

