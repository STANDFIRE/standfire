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
import java.util.Random;


/**
 * DelaunayOutline - Outline of a Delaunay triangulation.
 *
 * @author A. Piboule - february 2004
 */
public class DelaunayOutline {

	private static Random rdm = new Random ();


	private ArrayList vertices;



	public DelaunayOutline () {

		vertices = new ArrayList ();

	}


	public void addVertex (DelaunayVertex vt) {

		for (int i=0;i<vertices.size ();i++) {
			DelaunayVertex vt2 = (DelaunayVertex) vertices.get (i);
			if (vt2.equals (vt)) {return;}
		}

		vertices.add (vt);
	}

	public int getN () {return vertices.size ();}

	public Collection getVertices () {return vertices;}


	public double area () {
		double sum = 0;
		DelaunayVertex v1, v2;
		vertices.add (vertices.get (0));

		for (int i=0;i<vertices.size ()-1;i++) {
			v1 = (DelaunayVertex) vertices.get (i);
			v2 = (DelaunayVertex) vertices.get (i+1);
			sum = sum + (v1.x*v2.y)-(v1.y*v2.x);
		}

		vertices.remove (vertices.size ()-1);
		return Math.abs (sum/2);
	}

	public boolean contains (double x, double y) {

		double a, b, a2, b2, xInter, yInter, maxx, maxy, minx, miny;
		boolean ok = false;
		DelaunayVertex vt1, vt2;
		int nbInter = 0;
		int nbInterVertices = 0;

		// if the point (x,y) is a vertex, it's contained in the polygon
		for (int i=0;i<vertices.size ();i++) {
			vt1 = (DelaunayVertex) vertices.get (i);
			if ((x==vt1.x) && (y==vt1.y)) {
				return true;
			}
		}

		vertices.add (vertices.get(0));

		while (!ok) {
			a = rdm.nextDouble ();
			b = y - a*x;

			ok = true;
			nbInter = 0;
			nbInterVertices = 0;

			for (int i=0;i<(vertices.size ()-1);i++) {
				vt1 = (DelaunayVertex) vertices.get (i);
				vt2 = (DelaunayVertex) vertices.get (i+1);

				if (vt1.x!=vt2.x) {
					a2 = (vt1.y - vt2.y) / (vt1.x - vt2.x);
					b2 = vt1.y - a2*vt1.x;

					if (a!=a2) {
						xInter = (b2-b)/(a-a2);
					} else {
						ok = false;
						break;
					}
				} else {
					xInter = vt1.x;
				}

				yInter = a*xInter + b;

				if (xInter<=x) {

					if (vt1.x>vt2.x) {maxx = vt1.x; minx = vt2.x;}
						else {maxx = vt2.x; minx = vt1.x;}

					if (vt1.y>vt2.y) {maxy = vt1.y; miny = vt2.y;}
						else {maxy = vt2.y; miny = vt1.y;}

					if ((xInter>=minx) && (xInter<=maxx) && (yInter>=miny) && (yInter<=maxy)) {

						nbInter = nbInter + 1;

					}
				}


				for (int j=0;j<vertices.size ()-1;j++) {
					vt1 = (DelaunayVertex) vertices.get (j);

					if ((vt1.x==xInter) && (vt1.y==yInter)) {
						ok = false;
						break;
					}
				}

			}

		}

		vertices.remove (vertices.get(vertices.size ()-1));


		if ((nbInter==0) || ((nbInter % 2) ==0)) {return false;}
			else {return true;}
	}


	// returns a drawable shape (a general path) of the outline Polygon
	public Shape getShape () {

		if (vertices.size()==0) {return null;}

		GeneralPath gp = new GeneralPath ();

		DelaunayVertex vt = ((DelaunayVertex) vertices.get(0));

		gp.moveTo ((float) vt.x, (float) vt.y);

		for (int i = 1;i<vertices.size ();i++) {

			vt = ((DelaunayVertex) vertices.get(i));
			gp.lineTo ((float) vt.x, (float) vt.y);

		}

		gp.closePath ();

		return gp;

	}


}