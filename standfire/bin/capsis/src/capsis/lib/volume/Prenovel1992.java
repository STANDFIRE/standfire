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
 * Prenovel customized tree volume table (valid from 1992). Volume is computed with Dbh (cm).
 *
 * @author G. Lagarrigues - March 2013
 */
public class Prenovel1992 implements VolumeRule, Serializable {

	static {
		Translator.addBundle("capsis.lib.volume.Prenovel1992");
	}

	/**
	 * Constructor
	 */
	public Prenovel1992() {
	}

	// Prenovel volume table, for 5 cm width classes of diameter.
	// First class is class '5': for trees with dbh between 2.5 and 7.5 cms.
	// The volume for a tree with dbh = 0 is 0m3 (approximation)
	// The volume for classes 5, 10, 15, 120 and 125cm are computed by inter/extra-polation, using equation : V = 0.0003552513 * dbh ^ 2.2299159107
	// Max dbh considered is 125cm ; Volumes for dbh > 125cm constantly equal 16.8m3

	private double[] table = { 0.013, 0.060, 0.149, 0.2, 0.4, 0.7, 1.0,
								1.4, 1.8, 2.3, 2.8, 3.4, 4.0, 4.6, 5.4,
								6.2, 7.0, 8.0, 9.0, 10.1, 11.4, 12.7, 14.2, 15.4, 16.8};

	@Override
	public String getName() {
		return Translator.swap("Prenovel1992.name");
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
			return 16.8; // if dbh bigger than biggest class, return max volume
		}

		return v;
	}

	@Override
	public String getEncoding() {return "Prenovel1992";}



}
