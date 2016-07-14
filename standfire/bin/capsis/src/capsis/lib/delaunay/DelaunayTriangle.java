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
import java.util.Random;

/**
 * DelaunayTriangle - Triangle of the Delaunay triangulation.
 *
 * @author A. Piboule - february 2004
 */
public class DelaunayTriangle {


	private static Random rdm = new Random ();


	public DelaunayVertex v1, v2, v3; // 3 triangle vertices

	public DelaunayTriangle n12, n23, n31; // neighbor triangles, each between two vertices

	public double ccX, ccY; // triangle defined circle center coordinates

	public double r; // triangle defined circle radius (from orthocenter)



	public DelaunayTriangle (DelaunayVertex v1t, DelaunayVertex v2t, DelaunayVertex v3t) {
		v1 = v1t;
		v2 = v2t;
		v3 = v3t;

		n12 = null;
		n23 = null;
		n31 = null;

		calculateCircle ();
	}



	// set the good neighbor, in function of the two vertices given
	public void setNeighbor (DelaunayVertex vt1, DelaunayVertex vt2, DelaunayTriangle ngb) {

		if (vt1==v1) {
			if (vt2==v2) {
				n12 = ngb;
			} else if (vt2==v3) {
				n31 = ngb;
			}
		} else if (vt1==v2) {
			if (vt2==v1) {
				n12 = ngb;
			} else if (vt2==v3) {
				n23 = ngb;
			}
		} else if (vt1==v3) {
			if (vt2==v1) {
				n31 = ngb;
			} else if (vt2==v2) {
				n23 = ngb;
			}
		}
	}



	// calculate center coordinates and radius of the triangle defined circle
	public void calculateCircle () {

		double dx12 = v2.x - v1.x;
		double dy12 = v2.y - v1.y;
		double dx23 = v3.x - v2.x;
		double dy23 = v3.y - v2.y;
		double x12 = (v1.x + v2.x)/2d;
		double y12 = (v1.y + v2.y)/2d;
		double x23 = (v2.x + v3.x)/2d;
		double y23 = (v2.y + v3.y)/2d;

		if (dx12*dy23 - dy12*dx23==0) { // vertices are aligned :
		// should not happen during adding delaunay triangulation method
		// (if two point never have same position: verified by DelaunayTriangulation.doInsertion ())
			System.out.println ("Vertices "+v1.ref+", "+v2.ref+", "+v3.ref+" aligned");
			ccX = Double.NaN;
			ccY = Double.NaN;
			r = Double.NaN;
			return;
		}

		if (dy12==0) { // (v1v2) is horizontal
			ccX = x12;
			ccY = y23 - (ccX - x23)*dx23/dy23;
		} else {  //  (v1v2) is oblique
			if (dy23==0) {
				ccX = x23;
				ccY = y12 - (ccX - x12)*dx12/dy12;
			} else {
				ccX = (y23 - y12 + dx23*x23/dy23 - dx12*x12/dy12) / (dx23/dy23 - dx12/dy12);
				ccY = y23 - (ccX - x23)*dx23/dy23;
			}
		}

		// circle radius calculation
		r = Math.sqrt (Math.pow(ccX-v1.x,2) + Math.pow(ccY-v1.y,2));
	}



	// test if a point is in triangle defined circle
	public boolean circleContains (double xt, double yt) {
		return ( r>= (Math.sqrt (Math.pow(ccX - xt,2) + Math.pow(ccY - yt,2))) );
	}



	// test if the point (xt, yt) is in the triangle
	// for this purpose we cut the triangle in two part, by an horizontal (y=constant) line
	// and we test if the point is in each part (more easy because of coordinates simplfication)
	public boolean contains (double xt, double yt) {

		DelaunayVertex max, med, min;
		double x1, x2;

		// 1) sorting vertices by y coordinate

		if (v1.y>v2.y) {
			max = v1;
			med = v2;
		} else {
			max = v2;
			med = v1;
		}

		if (v3.y>max.y) {
			min = med;
			med = max;
			max = v3;
		} else if (v3.y>med.y) {
			min = med;
			med = v3;
		} else {
			min = v3;
		}

		// test if point is in the good y coordinate range
		if ((yt>max.y) || (yt<min.y)) {
			return false;
		} else {

			// 2) caclulation of the point on max-min which split the triangle : (xs, ys)
			double ys = med.y;
			double xs = (ys - max.y) * (max.x - min.x) / (max.y - min.y) + max.x;


			// 3) top subTriangle analysis
			if (yt>ys) {

				// x-bounds calculations for xt to be in subTriangle, with y=yt=constant
				x1 = (yt - max.y) * (max.x - med.x) / (max.y - med.y) + max.x;
				x2 = (yt - max.y) * (max.x - xs) / (max.y - ys) + max.x;


			// 4) bottom subTriangle analysis
			} else if (yt<ys) {

				// x-bounds calculations for xt to be in subTriangle, with y=yt=constant
				x1 = (yt - med.y) * (med.x - min.x) / (med.y - min.y) + med.x;
				x2 = (yt - ys) * (xs - min.x) / (ys - min.y) + xs;

			} else { // 5) yt==ys
					// x-bounds calculations for xt to be in subTriangle, with y=yt=constant

				x1 = xs;
				x2 = med.x;
			}

			// test if xt is included between x1 and x2 bounds
			if (x1<x2) {
				if ( (xt>=x1) && (xt<=x2) ) {
					return true;
				} else {
					return false;
				}
			} else {
				if ( (xt>=x2) && (xt<=x1) ) {
					return true;
				} else {
					return false;
				}

			}

		}

	}



	// get the  gravity center of the triangle X coordinate
	public double getBaryX () {
		return (v1.x+v2.x+v3.x)/3;
	}


	// get the  gravity center of the triangle Y coordinate
	public double getBaryY () {
		return (v1.y+v2.y+v3.y)/3;
	}



	// two vertices are given, get the third
	public DelaunayVertex getThirdVertex (DelaunayVertex vt1, DelaunayVertex vt2) {

		if ((vt1==v1) && (vt2==v2)) {return v3;}
		else if ((vt1==v2) && (vt2==v3)) {return v1;}
		else if ((vt1==v1) && (vt2==v3)) {return v2;}
		else {return null;}
	}


	// idem but with a DelaunayVertex[]
	public DelaunayVertex getThirdVertex (DelaunayVertex[] vt) {

		if ((vt[0]==v1) && (vt[1]==v2)) {return v3;}
		else if ((vt[0]==v2) && (vt[1]==v3)) {return v1;}
		else if ((vt[0]==v1) && (vt[1]==v3)) {return v2;}
		else {return null;}
	}



	// get next neighbor triangle to destination point (xt, yt)
	public DelaunayTriangle getNextTriangleTo (double xt, double yt) {

		double dx, dy;
		double a, b;
		boolean n12ok = false;
		boolean n23ok = false;
		boolean n31ok = false;

		if (n12!=null) {
			dx = v2.x-v1.x;
			dy = v2.y-v1.y;

			if (dx==0) {
				if (((v3.x < v1.x) && (xt > v1.x)) || ((v3.x > v1.x) && (xt < v1.x))) {
					n12ok=true;
				}

			} else  {
				a = (v1.y + (dy/dx)*(v3.x - v1.x));
				b = (v1.y + (dy/dx)*(xt - v1.x));

				if (((v3.y < a) && (yt > b)) || ((v3.y > a) && (yt < b))) {
					n12ok=true;
				}
			}
		}

		if (n23!=null) {
			dx = v3.x-v2.x;
			dy = v3.y-v2.y;

			if (dx==0) {
				if (((v1.x < v2.x) && (xt > v2.x)) || ((v1.x > v2.x) && (xt < v2.x))) {
					n23ok=true;
				}

			} else  {
				a = (v2.y + (dy/dx)*(v1.x - v2.x));
				b = (v2.y + (dy/dx)*(xt - v2.x));

				if (((v1.y < a) && (yt > b)) || ((v1.y > a) && (yt < b))) {
					n23ok=true;
				}
			}
		}

		if (n31!=null) {
			dx = v1.x-v3.x;
			dy = v1.y-v3.y;

			if (dx==0) {
				if (((v2.x < v3.x) && (xt > v3.x)) || ((v2.x > v3.x) && (xt < v3.x))) {
					n31ok=true;
				}

			} else  {
				a = (v3.y + (dy/dx)*(v2.x - v3.x));
				b = (v3.y + (dy/dx)*(xt - v3.x));

				if (((v2.y < a) && (yt > b)) || ((v2.y > a) && (yt < b))) {
					n31ok=true;
				}
			}
		}


		if ((n12ok) && (!n23ok) && (!n31ok)) {return n12;}
		if ((!n12ok) && (n23ok) && (!n31ok)) {return n23;}
		if ((!n12ok) && (!n23ok) && (n31ok)) {return n31;}


		// stochastic process
		// (to avoid cycles, which normally sould not happen i delaunay triangulation)

		if ((n12ok) && (n23ok)) {if (rdm.nextInt (2)==1) {return n12;} else {return n23;}}
		if ((n23ok) && (n31ok)) {if (rdm.nextInt (2)==1) {return n23;} else {return n31;}}
		if ((n12ok) && (n31ok)) {if (rdm.nextInt (2)==1) {return n12;} else {return n31;}}


		// if (n12ok==false) && (n23ok==false) && (n31ok==false) :
		// then point (xt,yt) is in the this triangle
		return this;

	}



	// returns a drawable shape (a general path) of the triangle
	public Shape getShape () {

		GeneralPath gp = new GeneralPath ();
		gp.moveTo ((float) v1.x, (float) v1.y);
		gp.lineTo ((float) v2.x, (float) v2.y);
		gp.lineTo ((float) v3.x, (float) v3.y);
		gp.closePath ();

		return gp;

	}


}