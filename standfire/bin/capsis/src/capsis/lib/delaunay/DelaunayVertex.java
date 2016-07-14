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


import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.Collection;


/**
 * DelaunayVertex - vertex of the Delaunay triangulation.
 *
 * @author A. Piboule - february 2004
 */
public class DelaunayVertex {

	public static final int INITIALCAPACITY = 5;

	public Object ref; // referent object
	public double x;
	public double y;

	private ArrayList neighbors; // neighbors vertices of this vertex
	private ArrayList voroVertices; // Voronoi polygon's vertices of this vertex

	public DelaunayVertex (Object rt, double xt, double yt) {
		ref = rt;
		x = xt;
		y = yt;
		neighbors = null;
		voroVertices = null;
	}

	// initialize the neighbors list
	public void initNeighbors () {
		neighbors = new ArrayList (INITIALCAPACITY);
	}


	// initialize the voroVertices list
	public void initVoroVertices () {
		voroVertices = new ArrayList (INITIALCAPACITY);
	}


	// add a neighbor
	public void addNeighbor (DelaunayVertex vt) {

		if (neighbors==null) {initNeighbors ();}
		if (!neighbors.contains (vt)) {neighbors.add (vt);}

	}


	// add a voroVertex to voroVertices
	public void addVoroVertex (DelaunayVoroVertex vrt) {

		if (voroVertices==null) {initVoroVertices ();}
		if (!voroVertices.contains (vrt)) {voroVertices.add (vrt);}

	}


	// gives the neighbors list
	public Collection getNeighbors () {return neighbors;}

	// gives the Voronoi's polygon vertices list
	public Collection getVoroVertices () {return voroVertices;}


	// 2 DelaunayVertex are equals if their positions are identical
	public boolean equals (Object obj) {
		if ((((DelaunayVertex) obj).x == x) && (((DelaunayVertex) obj).y == y)) {
			return true;
		} else {
			return false;
		}
	}



	// return all vertices at a distance <= dist of this vertex
	// N.B.: uses neighbors, so these neighbors should have been computed before...
	public Collection getVerticesWithin (double dist) {
		ArrayList lst = new ArrayList (INITIALCAPACITY);
		DelaunayVertex n;
		ArrayList neigh;
		int i;

		if (neighbors!=null) {
			// test all direct neighbors
			for (i=0;i<neighbors.size ();i++) {

				n = (DelaunayVertex) neighbors.get (i);

				if (Math.sqrt (Math.pow (x-n.x,2)+Math.pow (y-n.y,2))<=dist) {
					lst.add (n);
				}
			}

			// test neighbors of [neighbors which are within dist] and so on
			for (i=0;i<lst.size ();i++) {

				neigh = ((DelaunayVertex) lst.get(i)).neighbors;

				if (neigh!=null) {
					for (int j=0;j<neigh.size ();j++) {
						n = (DelaunayVertex) neigh.get (j);

						// do not add this and already added vertices, to avoid infinite loop
						if ((n!=this) && (!lst.contains (n))) {
							if (Math.sqrt (Math.pow (x-n.x,2)+Math.pow (y-n.y,2))<=dist) {
								lst.add (n);
							}
						}
					}
				}
			}
		}


		return lst;
	}



	// returns a drawable shape (a general path) of the Voronoi Polygon
	public Shape getVoroShape () {

		if (voroVertices.size()==0) {return null;}

		GeneralPath gp = new GeneralPath ();

		DelaunayVoroVertex vrt = ((DelaunayVoroVertex) voroVertices.get(0));

		gp.moveTo ((float) vrt.x, (float) vrt.y);

		for (int i = 1;i<voroVertices.size ();i++) {

			vrt = ((DelaunayVoroVertex) voroVertices.get(i));
			gp.lineTo ((float) vrt.x, (float) vrt.y);

		}

		gp.closePath ();

		return gp;

	}


}