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

package capsis.util;


/**
 * Point with 2 double coordinates.
 * It can save integer or floating values for x or y. It returns these values as
 * integer or double as needed. Used for Outputs (DataExtractors)/
 * 
 * @author F; de Coligny
 */
public class Smart2DPoint {
	private double x;
	private double y;

	public Smart2DPoint () {this (0, 0);}
	public Smart2DPoint (double a, double b) {
		x = a;
		y = b;
	}
	public Smart2DPoint (int a, double b) {
		x = (double) a;
		y = b;
	}
	public Smart2DPoint (int a, int b) {
		x = (double) a;
		y = (double) b;
	}

	public Smart2DPoint (double a, int b) {
		x = a;
		y = (double) b;
	}

	public double getX () {return x;}
	public double getY () {return y;}
	public int getIntX () {return (int) x;}
	public int getIntY () {return (int) y;}
	public String getStringX () {
		if ((double) ((int) x) == x) {
			return "" + (int) x;
		} else {
			return "" + x;
		}
	}
	public String getStringY () {
		if ((double) ((int) y) == y) {
			return "" + (int) y;
		} else {
			return "" + y;
		}
	}

	public String toString () {
		StringBuffer b = new StringBuffer ();
		b.append ("(");
		b.append (x);
		b.append (", ");
		b.append (y);
		b.append (")");
		
		return b.toString ();
	}
}
