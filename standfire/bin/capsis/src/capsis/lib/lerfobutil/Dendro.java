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
 * Dendro : utility methods for computing standard dendrometric variables
 * All the methods are static
 *
 * @author F. Mothe - november 2009
 */
public class Dendro {

	/**
	 * Number of trees to be used for computing dominant diameter.
	 * @param area_ha the plot area (ha)
	 * @return the number of dominant trees required to calculate the dominant height
	 */
	public static int getNbDominantTrees (double area_ha)  {
		int N100 = (int) (100. * area_ha);	// floor
		if (area_ha <= 0.02) {
			N100 = 1;
		} else if (area_ha <= 0.3) {
			N100 --;
		}
		return Math.max (1, N100);
	}

	/**
	 * Thinning coefficient Kg = Dg2 removed / Dg2 before = (G/N removed) / (G/N before)
	 */
	public static double calcKg (double Gbefore, double Nbefore,
			double Gafter, double Nafter)
	{
		// Kg = (G/N of removed trees) / (G/N before thinning)
		//	= ((Gbefore - Gafter) / (Nbefore - Nafter)) / (Gbefore / Nbefore)
		//	= (1 - Gafter / Gbefore) / (1 - Nafter / Nbefore)
		double Kg = 0.;
		if (Gbefore > 0 && Nbefore > Nafter) {
			Kg = (1. - Gafter / Gbefore) / (1. - Nafter / Nbefore);
		}
		return Kg;
	}

	/**
	 * Computes Dg from G and N
	 */
	public static double calcDg_cm (double G_m2, double N)
	{
		return N > 0. ? Math.sqrt (G_m2 * 40000. / Math.PI / N) : 0.;	// cm2
	}

	/**
	 * Computes G from Dg and N (or g from dg and 1)
	 */
	public static double calcG_m2 (double Dg_cm, double N)
	{
		return Dg_cm * Dg_cm * Math.PI * N / 40000. ;	// m2
	}

}
