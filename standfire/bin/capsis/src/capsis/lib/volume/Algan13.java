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
 * Algan 13 tree volume table. Volume is computed with Dbh (cm).
 *
 * @author G.Lagarrigues - August 2014
 */
public class Algan13 implements VolumeRule, Serializable {

	static {
		Translator.addBundle("capsis.lib.volume.Algan13");
	}

	/**
	 * Constructor
	 */
	public Algan13() {
	}

	// Algan 13 volume table, for 5 cm width classes of diameter,
	// First class is class '5': for trees with dbh between 2.5 and 7.5 cms.

	private double[] table = { 0.012, 0.062, 0.16, // Simulated
								0.25, 0.5, 0.8, 1.2, 1.6,2.1, 2.7, 3.4, 4.2, 5.0, 5.9, 6.9, 7.9, 9.0, 10.1, 11.3, 12.6};  // Extracted from table

	@Override
	public String getName() {
		return Translator.swap("Algan13.name");
	}

	/**
	 * Computes the volume of the tree with the given tree dbh (cm) and height (m).
	 * Volume is returned in m3.
	 */
	@Override
	public double getV(double dbh, double height) throws Exception { // note: Algan does not use the height
		if (dbh < 2.5) {
			return 0d;
		} // if dbh is less than 2.5, return 0

		int i = (int) ((dbh - 2.5) / 5d);
		double v = 0;
		if (i < table.length) {
			v = table[i];
		} else {
			return (double) Math.round((table[8]/0.14) * (dbh/100 - 0.05) * (dbh/100 - 0.1)); // if dbh bigger than biggest class, return estimation by Schaeffer (quick volume rule)
		}

		return v;
	}

	@Override
	public String getEncoding() {return "Algan13";}

}
