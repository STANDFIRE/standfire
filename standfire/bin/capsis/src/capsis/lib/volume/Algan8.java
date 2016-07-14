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
 * Algan 8 tree volume table. Volume is computed with Dbh (cm).
 *
 * @author B. Courbaud, F. de Coligny - march 2010, nov 2011
 * modified by G.Lagarrigues - august 2014
 */
public class Algan8 implements VolumeRule, Serializable {

	static {
		Translator.addBundle("capsis.lib.volume.Algan8");
	}

	/**
	 * Constructor
	 */
	public Algan8() {
	}

	// Algan 8 volume table, for 5 cm width classes of diameter.
	// First class is class '5': for trees with dbh between 2.5 and 7.5 cms.

	private double[] table = { 0.009, 0.045, 0.117, // Simulated
								0.2, 0.4, 0.6, 0.9, 1.2,1.6, 2.1, 2.6, 3.2, 3.8, 4.5, 5.2, 6.0, 7.0, 7.7, 8.6, 9.6}; // Extracted from table

	@Override
	public String getName() {
		return Translator.swap("Algan8.name");
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
	public String getEncoding() {return "Algan8";}


	// Test
	private void test(double dbh) {
		try {
			System.out.println("Algan8 dbh = " + dbh + " v = " + getV(dbh, 0)); // Algan does not use the height
		} catch (Exception e) {
			System.out.println("Algan8 dbh = " + dbh + " " + e);
		}
	}

	// Test
	public static void main(String[] args) {
		try {
			Algan8 a = new Algan8();
			a.test(27);
			a.test(22);
			a.test(55);
			a.test(21);

			a.test(27.88);
			a.test(23.29);
			a.test(23.88);
		} catch (Exception e) {
			System.out.println("Exception : " + e);
		}
	}

}
