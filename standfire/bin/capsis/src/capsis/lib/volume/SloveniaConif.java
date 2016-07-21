/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2011  Francois de Coligny, Benoit Courbaud
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
//import java.util.Iterator;

//import capsis.defaulttype.SpatializedTree;

//import jeeb.lib.util.DefaultNumberFormat;
import jeeb.lib.util.Translator;

/**
 * SloveniaConif volume rule.
 *
 * @author B. Courbaud - sept 2013
 */
public class SloveniaConif implements VolumeRule, Serializable {

	static {
		Translator.addBundle("capsis.lib.volume.SloveniaConif");
	}


	/**
	 * Constructor
	 */
	public SloveniaConif() {
	}

	@Override
	public String getName() {
		return Translator.swap("SloveniaConif.name");
	}

	/**
	 * Computes the volume of the tree with the given tree dbh (cm).
	 * Volume is returned in m3.
	 */
	@Override
	public double getV(double dbh, double height) throws Exception {

		double v =  0.0002 * Math.pow (dbh, 2.402);	// (m3)

		return v;
	}

	@Override
	public String getEncoding() {return "SloveniaConif";}


	// Test
	private void test(double dbh) {
		try {
			System.out.println("SloveniaConif dbh = " + dbh + " v = " + getV(dbh, 0));
		} catch (Exception e) {
			System.out.println("SloveniaConif dbh = " + dbh + " " + e);
		}
	}

	// Test
	public static void main(String[] args) {
		try {
			SloveniaConif a = new SloveniaConif();
			a.test(26);

			a.test(27);
		} catch (Exception e) {
			System.out.println("Exception : " + e);
		}
	}



}
