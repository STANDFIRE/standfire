/*
 * Nelder optimization library for Capsis4.
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

package capsis.lib.nelderoptimization;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

public class NelderOptimizer {

	private int nbCalls = 0; // count the number of times the function is called
	private double alpha; // reflexion coefficient
	private double beta; // contraction coefficient
	private double gam; // expansion coefficient
	private double acc; //precision
	private int itmax; // max number of iterations
	private boolean conv; // is convergence criterium met?
	private PrintWriter out1; // log file

	// contructor with standart values for optimisation factors
	public NelderOptimizer (String f) {
		this (f, 1d, 0.5d, 2d, 0.0001d, 300);
	}

	// constructor with custom values for optimisation factors
	public NelderOptimizer (String f, double a, double b, double g, double ac, int it) {
		alpha = a;
		beta = b;
		gam = g;
		acc = ac;
		itmax = it;
		conv=false;

		try {
			if (f.equals ("")) {
				out1 = new PrintWriter(System.out, true);
			} else {
				out1 = new PrintWriter(
					new BufferedWriter(
						new FileWriter(f)));
			}
		} catch (Exception e) {
			System.err.println("Error");
		}


	}

	public double getFunction (NelderFunctionProvider prov, double[] param) {
		nbCalls++;
		return prov.getFunctionToMinimizeValue (param);
	}


	public boolean hasConverged () {return conv;}



	// Nelder and Mead Algorithm from "Optimization Techniques with fortran", J.L. Kuester, J. H. Mize, 1973
	// Original work: Nelder J.A. and Mead R., "A Simplex method for function minimization", Computer Journal, 7, 308-313, 1964.
	// Traducted to C++ (and modified, see the following code) by J.-C. Pierrat (Engref Nancy, France)
	// Traducted to java by Alexandre Piboule
	public double[] minimizeFunction (NelderFunctionProvider prov, double[] param) {
	// param = new double[n+1]

		int n = param.length-1; // number of parameters to optimize, param[0] = value of the function

		double[][] x = new double[n+2][n+1]; // parameters
		double[] xcen = new double[n+1]; // Centroid
		double[] xref = new double[n+1]; // reflected point
		double[] xcon = new double[n+1]; // contracted point
		double[] xex = new double[n+1];  // expanded point
		double[] z = new double[n+2]; // objective function
		double[] coef = new double[n+1]; // parameters passed to function calculation

		double a; // side length of simplex
		double p; // starting simplex parameter
		double q; // starting simplex parameter
		double sum;
		double sqrt1;
		double sqrt2;

		double zhi=Double.MIN_VALUE; // highest function value in simplex
		double zlo=Double.MAX_VALUE; // lowest function value in simplex
		double zcen; // function value at centroid
		double zref; // function value at reflected point
		double zcon; // function value at contracted point
		double zex;  // function value at expanded point

		int i, j, k, ap;
		int l = 1;

		double ej = 0; // convergence criterium
		int np1 = n+1; // number of points in polygon (= n+1)
		double en = n; // number of parameters
		int itr = 0; // number of iterations
		conv = false; // is convergence criterium met?


		for (i=1;i<=n;i++) {
			x[1][i] = param[i];
		}

		/*  initialisation of the simplex */
		sum = 0d;
		for (i=1;i<=n;i++) {
			sum += x[1][i]*x[1][i];
		}
		a = 0.4d*Math.pow (sum,0.5)/en;

		sqrt1 = Math.pow (en+1,0.5d);
		sqrt2 = Math.pow (2d,0.5d);
		q = a*(sqrt1-1)/(en*sqrt2);
		p = a*(sqrt1+n-1)/(en*sqrt2);

		for (i=2;i<=np1;i++) {
			ap=1;
			for(j=1;j<=n;j++) {
				ap++;
				if(i == ap) {
					x[i][j] = x[1][j] + p;
				} else {
					x[i][j] = x[1][j] + q;
				}
			}
		}

		for (i=1;i<=np1;i++) {
			for(j=1;j<=n;j++) {
				coef[j] = x[i][j];
			}
			z[i]=getFunction(prov, coef);
		}

		Date dt = new Date ();
		out1.println ("Begin optimization at "+dt);
		out1.println ();
		out1.println ("*****************");


		/* iteration */
		for (itr=0;itr<itmax;itr++) {
			if (itr>0) {
				dt = new Date ();
				out1.println (dt);
				out1.println ("Beginning of iteration n°"+itr);
				for (int ii=1;ii<param.length;ii++) {
					out1.println ("parameter "+ii+"="+x[l][ii]);
				}
				out1.println ("Convergence Criterium Value ="+ej);
				out1.println ("*****************");
			}

			zhi = z[1];
			k = 1;
			for (i=2;i<=np1;i++) {
				if (z[i]>= zhi) {
					k = i;
					zhi = z[i];
				}
			}

			zlo = z[1];l = 1;
			for (i=2;i<=np1;i++) {
				if(z[i]< zlo) {
					l = i;
					zlo = z[i];
				}
			}


			for (j=1;j<=n;j++) {
				sum=0d;
				for(i=1;i<=np1;i++) {
					if (i!=k) {
						sum += x[i][j];
					}
				}
				xcen[j] = sum/en;
			}

			zcen = getFunction(prov, xcen);

			//******************************************
			// here is the modification by J.C. Pierrat
			// (simplification of sum calculation):
			sum=0d;
			for (i=1;i<=np1;i++) {
				if (i!=k) {
					sum += (z[i]-zcen)*(z[i]-zcen)/en;
				}
			}
			ej = Math.pow(sum,0.5);
			//******************************************

			if (ej<acc) {break;}

			for (j=1;j<=n;j++) {
				xref[j] = xcen[j] + alpha*(xcen[j]-x[k][j]);
			}

			zref = getFunction(prov, xref);

			if (zref > z[l] ) {
				if(zref<z[k]) {
					for (j=1;j<=n;j++) {
						x[k][j] = xref[j];
					}
					z[k] = zref;
					continue;
				}

				for (j=1;j<=n;j++) {
					xcon[j] = xcen[j] + beta*(x[k][j]-xcen[j]);
				}
				zcon = getFunction(prov, xcon);
				if (zcon >= z[k]) {
					for (j=1;j<=n;j++) {
						for (i=1;i<=np1;i++) {
							x[i][j] = (x[i][j]+x[l][j])/2d;
						}
					}
					for (i=1;i<=np1;i++) {
						for (j=1;j<=n;j++) {
							coef[j] = x[i][j];
						}
						z[i] = getFunction(prov, coef);
					}
					continue;
				}

				for (j=1;j<=n;j++) {
					x[k][j] = xcon[j];
					z[k] = zcon;
				}
				continue;

			}

			for (j=1;j<=n;j++) {
				xex[j] = xcen[j]+gam*(xref[j]-xcen[j]);
			}
			zex = getFunction(prov, xex);
			if(zex > zref ) {
				for (j=1;j<=n;j++) {
					x[k][j] = xref[j];
				}
				z[k] = zref;
				continue;
			}

			for (j=1;j<=n;j++) {
				x[k][j]=xex[j];
			}
			z[k] = zex;
			continue;
		} // end of for



		for (j=1;j<=n;j++) {
			param[j] = x[l][j];
			coef[j] = param[j];
		}
		param[0] = getFunction(prov, coef);


		// Final Outputs
		if (itr>=itmax)	{
			out1.println ("Optimization has NOT converged in "+itr+" iterations");
		} else {
			out1.println ("Optimization has converged in "+itr+" iterations");
			conv = true;
		}
		out1.println ("Function has been called "+nbCalls+" times.");
		out1.println ("Convergence criterion last value: "+ej+" < "+acc);
		out1.println ("Function value at optimum: "+param[0]);
		out1.println ("Optimum values of parameters:");
		for (int cp=1;cp<n+1;cp++) {
			out1.println ("Parameter n°"+cp+": "+param[cp]);
		}

		dt = new Date ();
		out1.println ();
		out1.println ("Optimization ended at "+dt);
		out1.close ();
		return param;
	}

	public static void main (String[] argv) {

		NelderOptimizer v = new NelderOptimizer ("C:\\CAPSIS\\capsis4\\bin\\capsis\\lib\\nelderoptimization\\toto.out");
		double[] tab = {0,1,0.5}; // initial values for parameters
		v.minimizeFunction (new NelderFunctionExample (), tab);

	}

}