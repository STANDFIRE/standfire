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

import java.util.Collection;
import java.util.Set;

/**
 * A factory to build and return Volume rules by their name and optional
 * parameters.
 *
 * @author B. Courbaud, F. de Coligny - nov 2011
 */
public class VolumeRuleFactory {

	/**
	 * Returns a VolumeRule with the given name. The given params are sent to
	 * the constructor.
	 * E.g. VolumeRuleFactory.getVolumeRule ("Algan8");
	 * E.g. VolumeRuleFactory.getVolumeRule ("TaperVolume", 0.7);
	 */
	public static VolumeRule getVolumeRule(String classSimpleName,
			double... params) throws Exception {


		if (classSimpleName.equals("Algan8")) {
			return new Algan8();
		} else if (classSimpleName.equals("Algan10")) { //added by T. Cordonnier 2013
			return new Algan10();
		} else if (classSimpleName.equals("Algan12")) { //added by T. Cordonnier 2013
			return new Algan12();
		} else if (classSimpleName.equals("Algan13")) { //added by T. Cordonnier 2013
			return new Algan13();
		} else if (classSimpleName.equals("Prenovel1953")) { //added by G.Lagarrigues 2013
			return new Prenovel1953();
		} else if (classSimpleName.equals("Prenovel1992")) { //added by G.Lagarrigues 2013
			return new Prenovel1992();
		} else if (classSimpleName.equals("Schaeffer13")) { //added by B.Courbaud 2013
			return new Schaeffer13();
		} else if (classSimpleName.equals("SloveniaConif")) { //added by B.Courbaud 2013
			return new SloveniaConif();
		} else if (classSimpleName.equals("SloveniaBrLeaves")) { //added by B.Courbaud 2013
			return new SloveniaBrLeaves();
		} else if (classSimpleName.equals("TaperVolume")) {
			return new TaperVolume(params[0]); // taperCoef
		} else {
//			VolumeTableReader volumeTableReader = new VolumeTableReader("data/samsara2/volumeTables/completedVolumeRules.txt");
			VolumeTableReader volumeTableReader = new VolumeTableReader("src/capsis/lib/volume/completedVolumeRules.txt");
			Set<String> volumeTableNames = volumeTableReader.getVolumeTableNames();
			if (volumeTableNames.contains(classSimpleName)) {
				VolumeTable volumeTable = new VolumeTable(volumeTableReader.getTable(classSimpleName));
				volumeTable.volumeTableName = classSimpleName;
				return volumeTable;
			}
		}

		throw new Exception("VolumeRuleFactory: unknown VolumeRule, simpleClassName: "
				+ classSimpleName);

	}

}
