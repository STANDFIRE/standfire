/*
 * This file is part of the Lerfob modules for Capsis4.
 *
 * Copyright (C) 2009-2010 Jean-François Dhôte, Patrick Vallet,
 * Jean-Daniel Bontemps, Fleur Longuetaud, Frédéric Mothe,
 * Laurent Saint-André, Ingrid Seynave.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package capsis.lib.lerfobutil;

/**
 * DichotomicSolver : abstract class for searching x0 such as y0 = f (x0).
 * The function f (which should be monotone) has to be
 * defined in the derived class.
 * Example of use :
 * <code>
 *		class Solver extends abial.util.DichotomicSolver {
 *			public Solver () {super (1e-5);}
 *			public double function (double x) {
 *				return x * x;
 *			}
 *		}
 *		double x = new Solver ().solveInRange (9., 0., 10.);	// should return 3
 * </code>
 *
 * @author F. Mothe - august 2009
 */
 public abstract class DichotomicSolver {
	private int m_nbMaxIterations = 1000;
	private double m_epsilon = 0.001;
	private boolean m_converge = true;
	private int m_nbIterations = 0;

	/**
	 * Default constructor (with epsilon = 0.001)
	 */
	public DichotomicSolver () {}

	/**
	 * General constructor
	 */
	public DichotomicSolver (double epsilon) {
		setEpsilon (epsilon);
	}

	/**
	 * Function y = f (x) to solve
	 * (should be constantly increasing)
	 */
	abstract public double function (double x);

	/**
	 * Convergence state after the last call to solveXXX
	 */
	public boolean converge () {
		return m_converge;
	}

	/**
	 * Number of iterations after the last call to solveXXX
	 */
	public int getNbIterations () {
		return m_nbIterations;
	}

	/**
	 * Set epsilon value
	 * (solveXXX converge if abs (y - y0) < epsilon)
	 */
	public void setEpsilon (double epsilon) {
		m_epsilon = epsilon;
	}

	/**
	 * Set the maximal number of iterations to get convergence
	 */
	public void setNbMaxIterations (int nbMaxIterations) {
		m_nbMaxIterations = nbMaxIterations;
	}

	/**
	 * Solve method.
	 * Returns x0 such as y0 = f (x0).
	 * xMin & xMax are such as f (xMin0) <= y0 <= f (xMax))
	 * (xMin can be < xMax if the function is monotone decreasing)
	 */
	public double solveInRange (double y0, double xMin, double xMax) {
		m_converge = false;
		m_nbIterations = 0;
		double x0;
		double y;

		double fxMin = function (xMin);
		double fxMax = function (xMax);
		if (fxMin > y0) {
			System.out.println ("DichotomicSolver.solveInRange (y0="
					+ y0 + ", xMin=" + xMin + ", xMax=" + xMax + ") : "
					+ "ERREUR : function (xMin)=" + fxMin + " > y0"
			);
			x0 = xMin;
		} else if (fxMax < y0) {
			System.out.println ("DichotomicSolver.solveInRange (y0="
					+ y0 + ", xMin=" + xMin + ", xMax=" + xMax + ") : "
					+ "ERREUR : function (xMax)=" + fxMax + " < y0"
			);
			x0 = xMax;
		} else {

			do {
				x0 = (xMin + xMax) / 2.;
				y = function (x0);
				if (y > y0) {
					xMax = x0;
				} else {
					xMin = x0;
				}
				// What if xMax = 0 ?
				// m_converge = Math.abs (xMax - xMin) < m_epsilon * xMax;
				//~ m_converge = Math.abs (xMax - xMin) < m_epsilon;
				m_converge = Math.abs (y - y0) < m_epsilon;
			} while (! m_converge  && (++ m_nbIterations < m_nbMaxIterations));
		}
		//~ System.out.println ("DichotomicSolver.solveInRange (" + y0 + ") = " + x0
			//~ + " converge=" + m_converge
			//~ + " nbIterations=" + m_nbIterations
		//~ );
		return x0;
	}

	/**
	 * Solve method.
	 * Returns x0 such as f (x0) = y0.
	 * xMin is a value such as f (xMin0) <= y0).
	 * The function should be monotone increasing.
	 */
	public double solveFromMin (double y0, double xMin, double dx) {
		// Initialisation : search for xMax such as function (xMax) > y0
		m_converge = false;
		m_nbIterations = 0;
		double xMax = xMin;

		if (function (xMin) > y0) {
			System.out.println ("DichotomicSolver.solveFromMin (y0="
					+ y0 + ", xMin=" + xMin + ", dx=" + dx + ") : "
					+ "ERREUR : function (xMin) > y0"
			);
		} else {
			do {
				xMax += dx;
				m_converge = function (xMax) >= y0;
			} while (! m_converge && ++ m_nbIterations < m_nbMaxIterations);
		}

		return m_converge ? solveInRange (y0, xMin, xMax) : xMin;
	}

	/**
	 * For test purpose
	 */
	public static void main (String [] args) {
		class DgSolver extends capsis.lib.lerfobutil.DichotomicSolver {
			public double nbTrees = 0;
			// G = f (Dg)
			public double function (double Dg) {
				// G_m2 for 1 ha
				nbTrees = lerfob.abial.model.AbialModelSelfThinning.calcNMaxTrees_perHa (Dg);
				return Math.PI / 40000. * Dg * Dg * nbTrees;	// m2
			}
		}
		{
			DgSolver solver = new DgSolver ();
			solver.setEpsilon (1e-10);
			for (double y = -10.; y < 100.; y += 10.) {
				// double x = solver.solveFromMin (y, 0.1, 0.001);
				double x = solver.solveInRange (y, 0.1, 100.);
				System.out.println ("y=" + y + " :> x=" + x + " f(x)=" + solver.function (x)
					+ " n=" + solver.getNbIterations ()
				);
			}
		}
		{
			class TestSolver extends capsis.lib.lerfobutil.DichotomicSolver {
				public TestSolver () {super (1e-10);}
				public double function (double x) {
					return x > 0 ? x*x : x;
				}
			}
			TestSolver solver = new TestSolver ();
			for (double y = -1.; y < 10.; y += 1.) {
				// double x = solver.solveFromMin (y, 0.001, 0.1);
				double x = solver.solveInRange (y, -1e10, 1e10);
				System.out.println ("y=" + y + " :> x=" + x + " f(x)=" + solver.function (x)
					+ " n=" + solver.getNbIterations ()
				);
			}
		}

		{
			class Solver extends capsis.lib.lerfobutil.DichotomicSolver {
				public Solver () {super (1e-5);}
				public double function (double x) {
					return x * x;
				}
			}
			double y = 9;
			double x = new Solver ().solveInRange (y, 0., 10.);
			System.out.println ("y=" + y + " :> x=" + x);
		}


	}

}

