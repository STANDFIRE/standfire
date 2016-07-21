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
 * Algan 10 tree volume table. Volume is computed with Dbh (cm).
 *
 * @author T. Cordonnier - march 2013
 * modified by G.Lagarrigues - august 2014
 */
public class Algan10 implements VolumeRule, Serializable {

	static {
		Translator.addBundle("capsis.lib.volume.Algan10");
	}

	/**
	 * Constructor
	 */
	public Algan10() {
	}

	// Algan 10 volume table, for 5 cm width classes of diameter.
	// First class is class '5': for trees with dbh between 2.5 and 7.5 cms.

	private double[] table = { 0.009, 0.046, 0.122, // Simulated
								0.2, 0.4, 0.7, 1.0, 1.4,1.8, 2.3, 2.9, 3.6, 4.3, 5.1, 5.9, 6.7, 7.6, 8.6, 9.7, 10.8}; // Extracted from table

	@Override
	public String getName() {
		return Translator.swap("Algan10.name");
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
	public String getEncoding() {return "Algan10";}

}
