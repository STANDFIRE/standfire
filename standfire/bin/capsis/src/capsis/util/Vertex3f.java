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

import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;

/**	A Serializable vertex with 3D float coordinates. 
*	@author F. de Coligny - january 2008
*/
public class Vertex3f {
	public float x;
	public float y;
	public float z;

	/**	Usual constructor.
	*/
	public Vertex3f (float a, float b, float c) {
		x = a;
		y = b;
		z = c;
	}

	/**	Constructor 2.
	*/
	public Vertex3f (Vertex3f v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}
	
	/**
	 * This constructor tries to build a Vertex3f with the given String. 
	 * Failure causes an exception to be thrown. Waited format is "(a, b, c)"
	 * with a, b and c of type float.
	 */
	public Vertex3f (String s) throws Exception {
		s = s.trim ();
		if (s.charAt (0) != '(' || s.charAt (s.length ()-1) != ')') {throw new Exception ();}
		s = s.substring (1, s.length () -1).trim ();

		StringTokenizer st = new StringTokenizer (s, ", ");
		if (st.countTokens () != 3) {throw new Exception ();}

		x = new java.lang.Float (st.nextToken ().trim ()).floatValue ();
		y = new java.lang.Float (st.nextToken ().trim ()).floatValue ();
		z = new java.lang.Float (st.nextToken ().trim ()).floatValue ();
	}
	
	//~ public static Vertex3f convert (Vertex2d v2) {return new Vertex3f (v2.x, v2.y, 0d);}
	
	/**	Translates the Vertex3f
	*/
	public void translate (float xShift, float yShift, float zShift) {
		x += xShift;
		y += yShift;
		z += zShift;
	}
	
	public Object clone () {
		return new Vertex3f (x, y, z);
	}
	
	public boolean equals (Vertex3f other) {
		return x == other.x && y == other.y && z == other.z;
	}
	
	public String toString () {
		NumberFormat nf = NumberFormat.getNumberInstance (Locale.ENGLISH);
		nf.setMaximumFractionDigits (2);
		nf.setGroupingUsed (false);
		
		StringBuffer b = new StringBuffer ();
		b.append ("(");
		b.append (nf.format(x));
		b.append (", ");
		b.append (nf.format(y));
		b.append (", ");
		b.append (nf.format(z));
		b.append (")");
	
		return b.toString ();
	}

	public static void main (String [] a) {
		Vertex3f v1 = new Vertex3f (12.12f, 23.23f, 34.34f);
		System.out.println ("v1="+v1);
		try {
			Vertex3f v2 = new Vertex3f ("(1.2, 2.3, 3.4)");
			System.out.println ("v2="+v2);
		} catch (Exception e) {
			System.out.println ("Exception: "+e);
		}
	}

}

/* Option1: Vertex3f extends Point3D.Double. Opposite option P3D extends V3D is better for evolution. - fc
public class Vertex3f extends capsis.util.Point3D.Double {
	public Vertex3f (double a, double b, double c) {super (a, b, c);}
	public Vertex3f (String s) throws Exception {super (s);}
	
	public static void main (String [] a) {
		Vertex3f v1 = new Vertex3f (12.12, 23.23, 34.34);
		System.out.println ("v1="+v1);
		try {
			Vertex3f v2 = new Vertex3f ("(1.2, 2.3, 3.4)");
			System.out.println ("v2="+v2);
		} catch (Exception e) {
			System.out.println ("Exception: "+e);
		}
	}
}*/
