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
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Standard volume table. Volume is computed with Dbh (cm).
 *
 * @author B.  G.Lagarrigues - august 2014
 */
public class VolumeTable implements VolumeRule, Serializable {

	static {
		Translator.addBundle("capsis.lib.volume.VolumeTable");
	}

	private TreeMap<Double,Double> map;

	/**
	 * Constructor
	 */
	public VolumeTable(TreeMap<Double,Double> map) {
		this.map = map;
	}

	@Override
	public String getName() {
		return Translator.swap("VolumeTable.name");
	}
	
	public String volumeTableName;

	/**
	 * Computes the volume of the tree with the given tree dbh (cm) and height (m).
	 * Volume is returned in m3.
	 */
	@Override
	public double getV(double dbh, double height) throws Exception { // note: VolumeTable does not use the height
		TreeSet<Double> diameters = new TreeSet<Double>(map.keySet());
		double minDbh = diameters.first();
		double maxDbh = diameters.last();
		double width = (maxDbh - minDbh) / (diameters.size()-1);
		double v = 0d;

		if (dbh >= minDbh - width/2) { // if dbh is less than minDbh - width/2, return 0
			int classIndex = (int) Math.floor(((dbh - minDbh + width/2) / width));
			double key = minDbh + classIndex*5;
			if (key <= maxDbh) {
				if (!diameters.contains(key)) {
					throw new Exception("Diameter is missing in volume table");
				}
				v = map.get(key);
			} else {
				double refV = 0d;
				if (diameters.contains(45d)) { // if dbh bigger than biggest class, return estimation by Schaeffer (quick volume rule)
					refV = map.get(45d);
				} else {
					// Compute v using Schaeffer formula, using as reference the volume class just below 45cm
					int refClassIndex = (int) Math.floor(((45 - minDbh + width/2 ) / width)) - 1 ; // "-1" to select the class just below
					double refKey = minDbh + refClassIndex*5;
					if (!diameters.contains(refKey)) {
						throw new Exception("Reference diameter is missing in volume table");
					}
					refV = map.get(refKey);
				}
				v = (double) Math.round((refV/0.14) * (dbh/100 - 0.05) * (dbh/100 - 0.1));
			}
		}

		return v;
	}

	@Override
	public String getEncoding() {return volumeTableName;}

}
