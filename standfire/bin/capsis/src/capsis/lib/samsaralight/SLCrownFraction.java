/*
 * Samsaralight library for Capsis4.
 * 
 * Copyright (C) 2008 / 2012 Benoit Courbaud.
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */
package capsis.lib.samsaralight;

import java.util.ArrayList;
import java.util.List;

import jeeb.lib.util.Log;
import jeeb.lib.util.Vertex3d;

/**
 * SLCrownFraction - For a crown made of 8 fractions of ellipsoid.
 * 
 * @author B. Courbaud, N. Donès, M. Jonard, G. Ligot, F. de Coligny - October 2008 / June 2012
 */
public class SLCrownFraction implements SLCrownPart {

	/**
	 * Inner class: a vertex with an associated length
	 */
	private static class Vertex {

		public double x;
		public double y;
		public double z;
		public double l; // a length

		public Vertex (double x, double y, double z, double l) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.l = l;
		}

		public String toString () {
			return "Vertex x: " + x + " y: " + y + " z: " + z + " l: " + l;
		}
	}

	// Ellipsoid center
	public double x0;
	public double y0;
	public double z0;

	// Ellipsoid parameters (semi-principal axes)
	// a, b and c may be positive or negative depending on the fraction
	public double a; // on X axis
	public double b; // on Y axis
	public double c; // on Z axis (heights)

	private double leafAreaDensity; // m2/m3
	private double extinctionCoefficient;

	private double directEnergy; // MJ
	private double diffuseEnergy; // MJ
	private double potentialEnergy; // MJ

	private boolean isSetLeafAreaDensity = false; // GL

	/**
	 * Constructor for a 8th of an ellipsoid. The signs of a, b and c determine which 8th is
	 * considered.
	 */
	public SLCrownFraction (double x0, double y0, double z0, double a, double b, double c) {
		this.x0 = x0;
		this.y0 = y0;
		this.z0 = z0;
		this.a = a;
		this.b = b;
		this.c = c;
	}

	// fc-15.5.2014 MJ found double energy in Heterofor after an intervention
	public void resetEnergy () {
		directEnergy = 0;
		diffuseEnergy = 0;
		potentialEnergy = 0;
	}

	/**
	 * give a shifted copy of the crown part The shift = new - old coordinates
	 */
	public SLCrownFraction getCopy (double xShift, double yShift, double zShift) {

		SLCrownFraction copy = new SLCrownFraction (x0 + xShift, y0 + yShift, z0 + zShift, a, b, c);

		copy.leafAreaDensity = leafAreaDensity;
		copy.extinctionCoefficient = extinctionCoefficient;

		// energy fields stay equal to zero
		return copy;
	}

	// fc-21.11.2013 to make copies (Heterofor, sometimes radiative balance not every years)
	public SLCrownFraction clone () throws CloneNotSupportedException {
		return (SLCrownFraction) super.clone ();
	}

	/**
	 * Evaluates if the given ray intercepts this crown part, previously relocated with the given
	 * shifts. Returns the interception path length and a distance to the origin (i.e. the target
	 * cell center) or null if no interception.
	 * 
	 * @param xShift , yShift, zShift: m, to relocate the crown part for the duration of this
	 *        interception
	 * @param elevation : angle, rad
	 * @param azimuth : angle, rad, trigonometric 0 and counter clockwise
	 */
	public double[] intercept (double xShift, double yShift, double zShift, double elevation, double azimuth) {

		double x2 = x0 + xShift;
		double y2 = y0 + yShift;
		double z2 = z0 + zShift;

		Vertex3d v0Shifted = new Vertex3d (x2, y2, z2);

		double cosElevation = Math.cos (elevation);
		double sinElevation = Math.sin (elevation);
		double cosAzimuth = Math.cos (azimuth);
		double sinAzimuth = Math.sin (azimuth);

		double A = cosElevation * cosElevation * cosAzimuth * cosAzimuth / (a * a) + cosElevation * cosElevation
				* sinAzimuth * sinAzimuth / (b * b) + sinElevation * sinElevation / (c * c);
		double B = -2 * x2 * cosElevation * cosAzimuth / (a * a) - 2 * y2 * sinAzimuth * cosElevation / (b * b) - 2
				* z2 * sinElevation / (c * c);
		double C = x2 * x2 / (a * a) + y2 * y2 / (b * b) + z2 * z2 / (c * c) - 1;

		double[] r = SLModel.solveQuadraticEquation (A, B, C);

		if (r.length == 0) return null; // no interception
		if (r.length == 1) return null; // tangent, ignored

		// Interception
		double root1 = r[0];
		double root2 = r[1];

		// Interception with plane x = x0
		double lx0 = x2 / (cosElevation * cosAzimuth);

		// Interception with plane y = y0
		double ly0 = y2 / (cosElevation * sinAzimuth);

		// Interception with plane z = z0
		double lz0 = z2 / sinElevation;

		// System.out.println("--- SLCrownFraction.intercept ()");
		// System.out.println("   shifted center, x: "+x2+" y: "+y2+" z: "+z2);
		// System.out.println("   a: "+a+" b: "+b+" c: "+c);
		// System.out.println("   elevation: "+elevation+" azimuth: "+azimuth);

		// Compute point coordinates of interception with crown part limits
		Vertex v1 = getVertex (root1, elevation, azimuth);
		Vertex v2 = getVertex (root2, elevation, azimuth);
		Vertex vx0 = getVertex (lx0, elevation, azimuth);
		Vertex vy0 = getVertex (ly0, elevation, azimuth);
		Vertex vz0 = getVertex (lz0, elevation, azimuth);
		Vertex vtarget = getVertex (0, elevation, azimuth); // the target point is in L=0, z=0, x=0,
															// y=0 (the origin for this computation)

		// Reset original x, y, z to avoid rounding errors
		vx0.x = x2;
		vy0.y = y2;
		vz0.z = z2;

		double xa = x2 + a;
		double yb = y2 + b;
		double zc = z2 + c;

		Vertex3d min = new Vertex3d (Math.min (x2, xa), Math.min (y2, yb), Math.min (z2, zc)); // box
																								// between
																								// shifted
																								// ellispoid
																								// center
																								// and
																								// crown
																								// limit
		Vertex3d max = new Vertex3d (Math.max (x2, xa), Math.max (y2, yb), Math.max (z2, zc));

		List<Vertex> list = new ArrayList<Vertex> ();
		if (inBBox (min, max, v0Shifted, v1) && v1.z >= 0) list.add (v1);
		if (inBBox (min, max, v0Shifted, v2) && v2.z >= 0) list.add (v2);
		if (inBBox (min, max, v0Shifted, vx0) && inEllipsoid (v0Shifted, vx0) && vx0.z > 0) list.add (vx0);
		if (inBBox (min, max, v0Shifted, vy0) && inEllipsoid (v0Shifted, vy0) && vy0.z > 0) list.add (vy0);
		if (inBBox (min, max, v0Shifted, vz0) && inEllipsoid (v0Shifted, vz0) && vz0.z > 0) list.add (vz0);
		if (inBBox (min, max, v0Shifted, vtarget) && inEllipsoid (v0Shifted, vtarget)) list.add (vtarget);
		if (list.size () == 0) return null; // intersected the ellipse outside the half quarter

		if (list.size () != 2) {
			double zTop2 = Math.max (z2, zc);
			// double zBottom2 = Math.min(z2, zc);

			// Particular cases
			boolean particularCase1 = (zTop2 == 0); // the part center is at the same z as the
													// target point
													// and the rest of the crown part is below the
													// target point

			boolean particularCase2 = false; // The root is at the periphery of the crown part
												// and the second root is below the target point

			boolean particularCase3 = false; // only the intersection with the origin (0,0,0) along
												// the inner sections

			if (list.size () == 1) {
				Vertex v = list.get (0);
				if (v == v1 || v == v2) {
					if (v.z == 0) particularCase2 = true;
				} else if (v == vtarget && onBBoxPeriphery (min, max, v)) {
					particularCase3 = true;
				}
			}

			if (!particularCase1 && !particularCase2 && !particularCase3) {
				Log.println (Log.WARNING, "SLCrownFraction.intercept ()", "The number of interception if neither 0, 2 or an expected particular case. "
						+ "It could be a tangent point with the inner section. A check is needed! The list size is "
						+ list.size ());
				System.out
						.println ("SLCrownFraction.intercept () - The number of interception if neither 0, 2 or an expected particular case. "
								+ "It could be a tangent point with the inner section. A check is needed! The list size is "
								+ list.size ());
			}
			return null;

		} else {
			Vertex w1 = list.get (0);
			Vertex w2 = list.get (1);
			double l1 = w1.l;
			double l2 = w2.l;
			double distance = (l1 + l2) / 2d;
			double pathLength = Math.abs (l1 - l2);

			return new double[] {pathLength, distance};
		}

	}

	/**
	 * Compute the coordinates of the interception point from the distance to the target cell and
	 * beam direction.
	 * 
	 * @param length
	 * @param elevation
	 * @param azimuth
	 * @return a vertex containing the coordinates of the interception point and its distance to the
	 *         target cell
	 */
	private Vertex getVertex (double length, double elevation, double azimuth) {
		double cosElevation = Math.cos (elevation);
		double sinElevation = Math.sin (elevation);
		double cosAzimuth = Math.cos (azimuth);
		double sinAzimuth = Math.sin (azimuth);

		double x = length * cosElevation * cosAzimuth;
		double y = length * cosElevation * sinAzimuth;
		double z = length * sinElevation;

		return new Vertex (x, y, z, length);

	}

	/**
	 * check if a vertex point is included between max and min vertex points
	 */
	// TODO remove unused vs?
	private boolean inBBox (Vertex3d min, Vertex3d max, Vertex3d vs, Vertex v) {

		// System.out.println("SLCrownFraction inBBox (min, max, v)... ");
		// System.out.println("min: "+min);
		// System.out.println("max: "+max);
		// System.out.println("v  : "+v);

		// 1. Test with bounding box
		boolean inBBox = (min.x <= v.x && v.x <= max.x && min.y <= v.y && v.y <= max.y && min.z <= v.z && v.z <= max.z);

		// System.out.println("inBBox: "+inBBox);

		return inBBox;

	}

	/**
	 * check if a vertex point is included between max and min vertex points
	 */
	private boolean onBBoxPeriphery (Vertex3d min, Vertex3d max, Vertex v) {
		boolean onBBoxPeriphery = (min.x == v.x || v.x == max.x || min.y == v.y || v.y == max.y || min.z == v.z || v.z == max.z);
		return onBBoxPeriphery;

	}

	/**
	 * check if a vertex is included in the ellispoid of the crownPart (inside and not at the
	 * periphery!)
	 */
	private boolean inEllipsoid (Vertex3d vs, Vertex v) {

		// System.out.println("SLCrownFraction inEllipsoid (v)... ");
		// System.out.println("v  : "+v);

		// 2. Finest test: within the ellipsoid ?
		// Note: vs is the shifted center of the crown
		double value = (v.x - vs.x) * (v.x - vs.x) / (a * a) + (v.y - vs.y) * (v.y - vs.y) / (b * b) + (v.z - vs.z)
				* (v.z - vs.z) / (c * c);
		boolean inEllipsoid = (value < 1);

		// System.out.println("inEllipsoid: "+inEllipsoid+" (value: "+value+")");

		return inEllipsoid;
	}

	/**
	 * get crown part volume in m3
	 */
	@Override
	public double getVolume () {
		// fc+mj22.11.2012 added Math.abs below
		double volume = Math.abs (4 / 3 * Math.PI * a * b * c); // full ellipsoid volume
		double fractionVolume = volume / 8; // -> divided by 8
		return fractionVolume;
	}

	public double getLeafAreaDensity () {
		return leafAreaDensity;
	}

	public void setLeafAreaDensity (double leafAreaDensity) {
		isSetLeafAreaDensity = true;
		this.leafAreaDensity = leafAreaDensity;
	}

	@Override
	public double getExtinctionCoefficient () {
		return this.extinctionCoefficient;
	}

	@Override
	public void setExtinctionCoefficient (double extinctionCoefficient) {
		this.extinctionCoefficient = extinctionCoefficient;
	}

	public void addDirectEnergy (double e) {
		this.directEnergy += e;
	} // MJ

	public void addDiffuseEnergy (double e) {
		this.diffuseEnergy += e;
	} // MJ

	public void addPotentialEnergy (double e) {
		this.potentialEnergy += e;
	} // MJ

	public double getDirectEnergy () {
		return directEnergy;
	} // direct beam energy in MJ

	public double getDiffuseEnergy () {
		return diffuseEnergy;
	} // diffuse beam energy in MJ

	public double getPotentialEnergy () {
		return potentialEnergy;
	} // in MJ, energy intercepted by this part but without neighbours

	/**
	 * get Leaf area m2
	 * 
	 * @pre LAD must be set
	 */
	public double getLeafArea () {
		if (isSetLeafAreaDensity) {
			return leafAreaDensity * getVolume ();
		} else {
			Log.println (Log.WARNING, "SLEllipsoidalCrownPart.getLeafArea(): this has been called before setting crown part LAD. It has returned 0.", null);
			return 0;
		}
	}

	@Override
	public boolean isIndsideCrownPart (double x, double y, double z) {
		Vertex3d v = new Vertex3d (x0,y0,z0);
		Vertex3d vs = new Vertex3d (x,y,z);
		double value = (v.x - vs.x) * (v.x - vs.x) / (a * a) + (v.y - vs.y) * (v.y - vs.y) / (b * b) + (v.z - vs.z)	* (v.z - vs.z) / (c * c);
		boolean inEllipsoid = (value < 1);
		Vertex3d min = new Vertex3d (Math.min (x0, x0+a), Math.min (y0, y0+a), Math.min (z0, z0+a)); // box
		Vertex3d max = new Vertex3d (Math.max (x0, x0+a), Math.max (y0, y0+a), Math.max (z0, z0+a));
		boolean inBBox = (min.x <= v.x && v.x <= max.x && min.y <= v.y && v.y <= max.y && min.z <= v.z && v.z <= max.z);
		return inEllipsoid && inBBox;
	}

}
