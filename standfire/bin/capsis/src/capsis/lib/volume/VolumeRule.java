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

/**
 * Volume rule. Calculate the volume from tree dbh (cm) and height (m).
 * 
 * @author B. Courbaud, F. de Coligny - march 2010, nov 2011
 */
public interface VolumeRule {

	/**
	 * Returns the volume name (e.g. fr: "Tarif de cubage Algan n°8" / en:
	 * "Algan n°8 volume table").
	 */
	public String getName();

	/**
	 * Computes the volume of the tree with the given tree dbh (cm) and height
	 * (m). Volume is returned in m3.
	 */
	public double getV(double dbh, double height) throws Exception;

	/**
	 * Returns a String with a conventional encoding: Class simple name followed
	 * by parameters if any in constructor order separated by "_".
	 * E.g. "Algan8"
	 * E.g. "TaperVolume_0.7"
	 */
	public String getEncoding();

}
