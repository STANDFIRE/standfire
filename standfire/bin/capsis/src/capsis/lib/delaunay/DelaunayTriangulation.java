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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;



/**
 * DelaunayTriangulation - Delaunay triangulation.
 *
 * @author A. Piboule - february 2004
 */
public class DelaunayTriangulation {

	private ArrayList toInsert; // working list of to be inserted vertex


	private ArrayList insertedVertices; // list of inserted vertices
	private ArrayList outOfBoundsVertices; // list of not inserted vertices because a position out of bounds
	private HashMap duplicatePositions; // list of not inserted vertices because an already existing position
	private ArrayList triangles; // list of all triangles of the triangulation (created in compute ())

	private DelaunayVertex[] corners; // list of the 4 created corners (not present in insertedVertices list)


	private double minx; // working bounds of the triangulation
	private double miny;
	private double maxx;
	private double maxy;

	private DelaunayTriangle refTriangle; // actual reference triangle
										 // used as start point to find the inserted vertex triangle

	private ArrayList outlines; // outline defined by the triangulation

	private boolean initialized; // check if bounds have been set
	private boolean neighborsComputed; // check if neighbors are up to date
	private boolean voronoiDiagramComputed; // check if voronoi diagram is up to date

	private boolean savingMode; // are destr and border lists keeped in lastDestrLst and lastBorderLst ?

	private ArrayList lastDestrLst, lastBorderLst; // contains insertion process informations
												   // about (only) LAST vertex insertion
												   // only if savingMode==true


	public DelaunayTriangulation () {

		toInsert = new ArrayList ();
		insertedVertices = new ArrayList ();
		outOfBoundsVertices = new ArrayList ();
		corners = new DelaunayVertex[4];
		duplicatePositions = new HashMap ();
		triangles = null;
		outlines = null;

		refTriangle = null;

		initialized = false;
		neighborsComputed = false;
		voronoiDiagramComputed = false;
		savingMode = false;

		minx = Double.MAX_VALUE;
		miny = Double.MAX_VALUE;
		maxx = Double.MIN_VALUE;
		maxy = Double.MIN_VALUE;

	}


	// Construct the 4 corners of the triangulation, and the 2 first triangles
	public void init (double x1, double y1, double x2, double y2) {

		if (!initialized) {
			initialized = true;
			minx = x1;
			miny = y1;
			maxx = x2;
			maxy = y2;
		}

		// add the 4 corners
		corners[0] = new DelaunayVertex (null, x1, y1);
		corners[1] = new DelaunayVertex (null, x2, y1);
		corners[2] = new DelaunayVertex (null, x1, y2);
		corners[3] = new DelaunayVertex (null, x2, y2);

		DelaunayTriangle t1 = new DelaunayTriangle (corners[0], corners[2], corners[3]);
		DelaunayTriangle t2 = new DelaunayTriangle (corners[0], corners[1], corners[3]);

		t1.n31 = t2;
		t2.n31 = t1;


		refTriangle = t1;

	}



	// idem but corners are calculated from others points
	public boolean init () {

		if (toInsert.size ()<2) {return false;}


		for (int i = 0;i < toInsert.size ();i++) {
			DelaunayVertex vt = (DelaunayVertex) toInsert.get(i);

			if (vt.x<minx) {minx = vt.x;}
			if (vt.x>maxx) {maxx = vt.x;}
			if (vt.y<miny) {miny = vt.y;}
			if (vt.y>maxy) {maxy = vt.y;}
		}

		initialized = true;

		// corner should not be at the same position as an inserted vertex
		// so we add add a little (1%) offset
		double offset = (maxx-minx+maxy-miny)/200d;

		minx = minx-offset;
		miny = miny-offset;
		maxx = maxx+offset;
		maxy = maxy+offset;

		init (minx, miny, maxx, maxy);
		return true;

	}



	// Accesors:

	// gives all vertices successfully inserted in the triangulation
	public Collection getInsertedVertices () {return insertedVertices;}

	// gives all out of bounds vertices (which have NOT been inserted)
	public Collection getOutOfBoundsVertices () {return outOfBoundsVertices;}

	// the returned hashMap gives for each (not inserted) duplicated vertex (in term of position)
	// a link to the same position vertex which has been inserted.
	public HashMap getDuplicatePositions () {return duplicatePositions;}

	// gives the 4 bounds conners which have been added in init (),
	// these 4 points are NOT in insertedVertex List
	public DelaunayVertex[] getCorners () {return corners;}

	// gives a list of destructed triangles during last vertex insertion
	public Collection getLastDestructedTriangles () {return lastDestrLst;}

	// gives a list of border triangles during last vertex insertion
	// these triangles are destructed triangles neighbors
	// new triangles created during insertion have 1 common side with them
	public Collection getLastBorderTriangles () {return lastBorderLst;}

	// gives the triangle used to enter in triangulation to find "to insert" vertex triangle

	public DelaunayTriangle getRefTriangle () {return refTriangle;}


	// accessors for booleans
	public void setSavingMode (boolean t) {savingMode = t;}
	public boolean getSavingMode () {return savingMode;}

	public boolean isInitialized () {return initialized;}





	// gives the up to date list of all triangles
	public Collection getTriangles () {
		if (triangles!=null) {
			return triangles;
		} else {
			computeTriangles ();
			return triangles;
		}
	}

	// gives the insertedVertices list with up to date computed neighbors for each vertex
	public Collection getVerticesNeighbors () {
		if (!neighborsComputed) {
			return computeVerticesNeighbors ();
		} else {
			return insertedVertices;
		}
	}

	// gives the insertedVertices list with up to date computed Voronoi Diagram for each vertex
	// Voronoi diagram consist for each vertex, in a sorted list of DelaunayVoroVertex defining the Voronoi Polygon for this vertex
	public Collection getVoronoiDiagram () {
		if (!voronoiDiagramComputed) {
			return computeVoronoiDiagram ();
		} else {
			return insertedVertices;
		}
	}



	// gives the same thing that getVoronoiDiagram
	// but without voronoi polygon of corners neighbors (to avoid border effects)

	public Collection getCleanVoronoiDiagram () {
		getVoronoiDiagram ();

		for (int i=0;i<insertedVertices.size ();i++) {
			DelaunayVertex v = (DelaunayVertex) insertedVertices.get (i);

			for (int j=0;j<v.getVoroVertices ().size ();j++) {
				DelaunayVoroVertex vv = (DelaunayVoroVertex) ((ArrayList) v.getVoroVertices ()).get (j);
				if ((vv.x<minx) || (vv.x>maxx) || (vv.y<miny) || (vv.y>maxy)) {
					v.initVoroVertices ();
				}
			}
		}
		voronoiDiagramComputed = false;
		return insertedVertices;
	}


	// gives the outline defined by the triangulation (corners excluded)
	public Collection getOutlines () {
		if (outlines==null) {
			return computeOutlines ();
		} else {
			return outlines;
		}
	}



	// adds all triangles of the triangulation to an ArrayList
	private void computeTriangles () {

		triangles = new ArrayList (insertedVertices.size ());

		DelaunayTriangle tri = refTriangle;
		DelaunayTriangle n1 = null;

		if (tri!=null) {triangles.add (tri);}

		for (int i = 0; i<triangles.size ();i++) {

			tri = (DelaunayTriangle) triangles.get (i);


			n1 = tri.n12;
			if ((n1!=null) && (!triangles.contains (n1))) {triangles.add (n1);}
			n1 = tri.n23;
			if ((n1!=null) && (!triangles.contains (n1))) {triangles.add (n1);}
			n1 = tri.n31;
			if ((n1!=null) && (!triangles.contains (n1))) {triangles.add (n1);}

		}

	}



	// compute for each vertex its neighbors
	private Collection computeVerticesNeighbors () {

		ArrayList lst = (ArrayList) getTriangles ();
		DelaunayTriangle tri;

		neighborsComputed = true;

		// clear old neighbors
		for (int i = 0; i<insertedVertices.size ();i++) {
			((DelaunayVertex) insertedVertices.get (i)).initNeighbors ();
		}

		corners[0].initNeighbors ();
		corners[1].initNeighbors ();
		corners[2].initNeighbors ();
		corners[3].initNeighbors ();


		// compute new neighbors
		for (int i = 0; i<lst.size ();i++) {

			tri = (DelaunayTriangle) lst.get(i);

			tri.v1.addNeighbor (tri.v2);
			tri.v2.addNeighbor (tri.v1);

			tri.v2.addNeighbor (tri.v3);
			tri.v3.addNeighbor (tri.v2);

			tri.v1.addNeighbor (tri.v3);
			tri.v3.addNeighbor (tri.v1);
		}


		// corner are not treated as valid neighbors for insertedVertices
		ArrayList n;
		for (int i=0;i<4;i++) {
			n = (ArrayList) (corners[i]).getNeighbors ();

			for (int j=0;j<n.size ();j++) {
				((ArrayList) ((DelaunayVertex) n.get (j)).getNeighbors ())
				.remove (corners[i]);
			}
		}

		return insertedVertices; // corners not exported here

	}



	// create an HashMap : the keys are inserted vertex
	//					   the values are voronoi's polygons for these inserted vertex
	public Collection computeVoronoiDiagram () {


		ArrayList lst = (ArrayList) getTriangles ();
		DelaunayTriangle tri;
		DelaunayVertex vert, first, next;
		ArrayList voroList, result;
		DelaunayVoroVertex voro;

		voronoiDiagramComputed = true;

		// clear old sides
		for (int i = 0; i<insertedVertices.size ();i++) {
			((DelaunayVertex) insertedVertices.get (i)).initVoroVertices ();
		}

		corners[0].initVoroVertices ();
		corners[1].initVoroVertices ();
		corners[2].initVoroVertices ();
		corners[3].initVoroVertices ();


		// compute new sides
		for (int i = 0; i<lst.size ();i++) {

			tri = (DelaunayTriangle) lst.get(i);

			tri.v1.addVoroVertex (new DelaunayVoroVertex (tri, tri.v2, tri.v3));

			tri.v2.addVoroVertex (new DelaunayVoroVertex (tri, tri.v1, tri.v3));

			tri.v3.addVoroVertex (new DelaunayVoroVertex (tri, tri.v1, tri.v2));
		}



		for (int i= 0;i<insertedVertices.size ();i++) {
			vert = (DelaunayVertex) insertedVertices.get (i);

			voroList = (ArrayList) vert.getVoroVertices ();

			voro = (DelaunayVoroVertex) voroList.remove (0);

			vert.initVoroVertices ();

			vert.addVoroVertex (voro);

			first = voro.v1;
			next = voro.next (voro.v1);


			while (next!=first) {

				for (int j = 0;j<voroList.size ();j++) {
					voro = (DelaunayVoroVertex) voroList.get (j);
					if ((voro.v1==next) || (voro.v2==next)) {
						break;
					}
				}

				vert.addVoroVertex (voro);
				voroList.remove (voro);

				next = voro.next (next);

			}

		}

		return insertedVertices;
	}


	// compute the Outline of the triangulation
	public Collection computeOutlines () {

		ArrayList lst = (ArrayList) getTriangles ();
		ArrayList sides = new ArrayList ();
		outlines = new ArrayList ();
		DelaunayTriangle tri;
		boolean okV1, okV2, okV3;
		DelaunayOutline outline;

		for (int i = 0;i < lst.size ();i++) {
			tri = (DelaunayTriangle) lst.get (i);

			okV1 = true;
			okV2 = true;
			okV3 = true;

			for (int j=0;j<4;j++) {
				if (tri.v1==corners[j]) {okV1=false;}
				if (tri.v2==corners[j]) {okV2=false;}
				if (tri.v3==corners[j]) {okV3=false;}
			}

			if (okV1 && okV2 && !okV3) {sides.add (new DelaunaySide (tri, tri.v1, tri.v2));}
			if (okV2 && okV3 && !okV1) {sides.add (new DelaunaySide (tri, tri.v2, tri.v3));}
			if (okV3 && okV1 && !okV2) {sides.add (new DelaunaySide (tri, tri.v3, tri.v1));}

		}

		if (insertedVertices.size ()>2) {

			for (int i=0;i<sides.size ();i++) {
				for (int j=0;j<sides.size ();j++) {
					if ((i!=j) && (sides.get(i).equals (sides.get(j)))) {
						if (i<j) {
							sides.remove (j);
							sides.remove (i);
						} else {
							sides.remove (i);
							sides.remove (j);
						}
						break;
					}
				}
			}


			while (sides.size ()>2) {

				outline = new DelaunayOutline ();

				DelaunaySide sd = (DelaunaySide) sides.remove (0);

				DelaunayVertex first = sd.v1;
				DelaunayVertex next = sd.next (first);


				while (next!=first) {

					for (int j = 0;j<sides.size ();j++) {
						sd = (DelaunaySide) sides.get (j);
						if ((sd.v1==next) || (sd.v2==next)) {
							break;
						}
					}

					outline.addVertex (next);
					sides.remove (sd);

					next = sd.next (next);

				}

				outline.addVertex (next);
				outlines.add (outline);
			}


		}

		return outlines;

	}



	// add a DelaunayVertex to the toInsert list
	public void addVertex (DelaunayVertex vt) {

			for (int i = 0;i<insertedVertices.size ();i++) {
				if (vt.equals (insertedVertices.get(i))) {

					duplicatePositions.put (vt, insertedVertices.get(i));
					return;

				}
			}


			for (int i = 0;i<toInsert.size ();i++) {
				if (vt.equals (toInsert.get(i))) {

					duplicatePositions.put (vt, toInsert.get(i));
					return;

				}
			}

			toInsert.add (vt);

	}



	// add all vertices present in toInsert list to the triangulation
	public boolean doInsertion ()  {

		if (!initialized) {return false;} // we need 4 corners to continue

		DelaunayTriangle t1;
		DelaunayTriangle t2;
		DelaunayTriangle n1;
		DelaunayVertex vt;
		DelaunayVertex vBase;
		DelaunayVertex vNext;
		DelaunayVertex vFirst;
		DelaunaySide side;
		double xt;
		double yt;
		int j;

		// we use arrayList and not vector for performance reasons
		ArrayList destrLst = new ArrayList (5);
		ArrayList borderLst = new ArrayList (10);


		// initialisations
		t1 = refTriangle;
		t2 = null;


		// descending order to avoid multiples Time consuming ArrayList compression
		// so we always remove the last element
		for (int i = toInsert.size ()-1; i>=0;i--) {

			 vt = (DelaunayVertex) toInsert.remove (toInsert.size ()-1);


			// is the point in the triangulation, else add to outOfBoundsVertices list
			// and DO NOT insert
			if ((vt.x<=minx) || (vt.x>=maxx) || (vt.y<=miny) || (vt.y>=maxy)) {
				outOfBoundsVertices.add (vt);


			// we have to insert the point in the triangulation
			} else {

				insertedVertices.add (vt);
				xt = vt.x;
				yt = vt.y;

				// if only one insertion, triangles list is not more up to date:
				triangles = null;
				outlines = null;
				neighborsComputed = false;
				voronoiDiagramComputed = false;


				// 1) find the triangle which contains the point to be inserted
				// faster way, actually used (walk through the triangulation from refTriangle)

				while ((!t1.contains (xt, yt))) {

					t1 = t1.getNextTriangleTo (xt, yt);

				}

				// very slower way : look all triangles of the triangulation
				// no nore in use (use for process time comparision if needed)
/*				ArrayList tris = (ArrayList) getTriangles ();

				for (j = 0; j<tris.size ();j++) {

					t1 = ((DelaunayTriangle) tris.get(j));

					if (t1.contains (xt, yt)) {break;}
				}
*/


				// 2) create two lists :
				// - destrLst : contains triangle "to destroy"
				// - borderLst : contains the sides of the polygon formed by "to destroy" triangles
				// a side is composed of two vertices, and one bording triangle (or null if don't exist)

				destrLst.add (t1);

				for (j = 0; j<destrLst.size ();j++) {

					t2 = (DelaunayTriangle) destrLst.get (j);

					// for n12 neighbor triangle
					n1 = t2.n12;

					if (!destrLst.contains (n1)) {
						if ((n1!=null) && (n1.circleContains (xt,yt))) {
							destrLst.add (n1);
						} else {
							borderLst.add (new DelaunaySide (n1, t2.v1, t2.v2));
						}
					}

					// for n23 neighbor triangle
					n1 = t2.n23;

					if (!destrLst.contains (n1)) {
						if ((n1!=null) && (n1.circleContains (xt,yt))) {
							destrLst.add (n1);
						} else {
							borderLst.add (new DelaunaySide (n1, t2.v2, t2.v3));
						}
					}

					// for n31 neighbor triangle
					n1 = t2.n31;

					if (!destrLst.contains (n1)) {
						if ((n1!=null) && (n1.circleContains (xt,yt))) {
							destrLst.add (n1);
						} else {
							borderLst.add (new DelaunaySide (n1, t2.v3, t2.v1));
						}
					}

				}



				// for externals drawings : copy the two lists in public instance lists
				if (savingMode) {
					lastDestrLst = new ArrayList (destrLst);
					lastBorderLst = new ArrayList (borderLst);
				}


				// 3) Construct new triangles
				// each is composed by:
				// - the point (xt, yt)
				// - a borderLst triangle side referenced in borderLst
				// after each triangle, we create the next, which has a vertex in common with it
				// (thanks to borderLst side list)
				// so each triangle is the neighbor of the next,
				// and the first is the second neighbor of the last
				// for third neighbor we use the side's border triangle


				// First triangle construction
				side = (DelaunaySide) borderLst.get(0);

				vFirst = side.v1;

				vNext = side.v2;
				borderLst.remove (side);

				t1 = new DelaunayTriangle (vFirst, vNext, vt);
				n1 = side.tri;
				t1.n12 = n1;
				if (n1!=null) {n1.setNeighbor (vFirst, vNext, t1);}


				refTriangle = t1;
				// NB: so the refTriangle is set to be the first triangle created this step
				// to keep first triangle, see below: first and last triangles linking,
				// (and to have a refTriangle to begin next loop)


				// Others triangles construction
				while (vNext!=vFirst)  {
					vBase = vNext;

					// search the next side to link
					for (j=0;j<borderLst.size ();j++) {
						side = (DelaunaySide) borderLst.get (j);

						if ((side.v1==vBase) || (side.v2==vBase)) {break;}
					}

					vNext = side.next (vBase);
					borderLst.remove (side);

					t2 = new DelaunayTriangle (vBase, vNext, vt);
					n1 = side.tri;
					t2.n12 = n1;
					if (n1!=null) {n1.setNeighbor (vBase, vNext, t2);}
					t2.n31 = t1;
					t1.n23 = t2;

					t1 = t2;
				}

				// linking first triangle to last one
				t2.n23 = refTriangle;
				refTriangle.n31 = t2;

				// clearing the lists and tables for next loop
				destrLst.clear ();
				borderLst.clear ();

			}
		}

		return true;
	}

}