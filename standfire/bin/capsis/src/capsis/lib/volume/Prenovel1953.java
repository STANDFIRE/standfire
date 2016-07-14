/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2003  Francois de Coligny
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
package capsis.lib.volume;

import java.io.Serializable;

import jeeb.lib.util.Translator;

/**
 * Prenovel customized tree volume table (valid for period 1953 - 1972). Volume is computed with Dbh (cm).
 *
 * @author G. Lagarrigues - March 2013
 */
public class Prenovel1953 implements VolumeRule, Serializable {

	static {
		Translator.addBundle("capsis.lib.volume.Prenovel1953");
	}

	/**
	 * Constructor
	 */
	public Prenovel1953() {
	}

	// Prenovel volume table, for 5 cm width classes of diameter.
	// First class is class '5': for trees with dbh between 2.5 and 7.5 cms.
	// The volume for a tree with dbh = 0 is 0m3 (approximation)
	// The volume for classes 5,10,15 are computed by interpolation, using equation : V = 0.0005124767 * dbh ^ 2.1849481407
	// Max dbh considered is 125cm ; Volumes for dbh > 125cm constantly equal 18.9m3

	private double[] table = { 0.017, 0.078, 0.19, 0.2, 0.5, 0.7, 1.1, 1.4,
								1.8, 2.4, 3.0, 3.7, 4.6, 5.4, 6.3, 7.2, 8.4,
								9.8, 11.2, 12.6, 13.8, 15.1, 16.3, 17.6, 18.9 };

	@Override
	public String getName() {
		return Translator.swap("Prenovel1953.name");
	}

	/**
	 * Computes the volume of the tree with the given tree dbh (cm) and height (m).
	 * Volume is returned in m3.
	 */
	@Override
	public double getV(double dbh, double height) throws Exception {
		if (dbh < 2.5) {
			return 0d;
		} // if dbh is less than 2.5, return 0

		int i = (int) ((dbh - 2.5) / 5d);
		double v = 0;
		if (i < table.length) {
			v = table[i];
		} else {
			return 18.9; // if dbh bigger than biggest class, return max volume
		}

		return v;
	}

	@Override
	public String getEncoding() {return "Prenovel1953";}


}
