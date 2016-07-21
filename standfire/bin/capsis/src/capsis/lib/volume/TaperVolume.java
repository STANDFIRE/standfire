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
import java.util.Iterator;

import capsis.defaulttype.SpatializedTree;
import capsis.util.methodprovider.TaperCoefProvider;

import jeeb.lib.util.DefaultNumberFormat;
import jeeb.lib.util.Translator;

/**
 * TaperVolume volume rule. 
 * 
 * @author B. Courbaud, F. de Coligny - nov 2011
 */
public class TaperVolume implements VolumeRule, Serializable {

	static {
		Translator.addBundle("capsis.lib.volume.TaperVolume");
	}

	private double taperCoef; // in ]0;1[, log middle diameter / dbh 
	
	/**
	 * Constructor
	 */
	public TaperVolume(double taperCoef) {
		this.taperCoef = taperCoef;
	}

	@Override
	public String getName() {
		return Translator.swap("TaperVolume.name");
	}
	
	/**
	 * Computes the volume of the tree with the given tree dbh (cm) and height (m).
	 * Volume is returned in m3.
	 */
	@Override
	public double getV(double dbh, double height) throws Exception {

		double v =  Math.PI/4 * height * Math.pow ((taperCoef * dbh / 100), 2);	// (m3)
		
		return v;
	}

	@Override
	public String getEncoding() {return "TaperVolume_"+DefaultNumberFormat.getInstance ().format(taperCoef);}

	
	// Test
	private void test(double dbh, double height) {
		try {
			System.out.println("TaperVolume dbh = " + dbh + " v = " + getV(dbh, height));
		} catch (Exception e) {
			System.out.println("TaperVolume dbh = " + dbh + " " + e);
		}
	}

	// Test
	public static void main(String[] args) {
		try {
			TaperVolume a = new TaperVolume(0.7);
			a.test(26, 18.747);
			
			a.test(27.19, 19.36);
		} catch (Exception e) {
			System.out.println("Exception : " + e);
		}
	}

	
	
}
