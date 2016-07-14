/*
 * Delaunay triangulation and Voronoi diagram library for Capsis4.
 *
 * Copyright (C) 2004 Alexandre Piboule.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package capsis.lib.delaunay;




/**
 * DelaunaySide - side of the Delaunay triangulation.
 *
 * @author A. Piboule - february 2004
 */
public class DelaunaySide {

	public DelaunayVertex v1;
	public DelaunayVertex v2;
	public DelaunayTriangle tri;


	public DelaunaySide (DelaunayTriangle trit, DelaunayVertex v1t, DelaunayVertex v2t) {
		tri = trit;
		v1 = v1t;
		v2 = v2t;
	}

	// give the other vertex of the side
	public DelaunayVertex next (DelaunayVertex vt) {
		if (vt==v1) {return v2;}
		else if (vt==v2) {return v1;}
		else {return null;}
	}


	public boolean equals (Object o) {
		DelaunaySide sd = (DelaunaySide) o;

		if (((sd.v1==v1) && (sd.v2==v2)) || ((sd.v2==v1) && (sd.v1==v2))) {
			return true;
		} else {
			return false;
		}

	}


}