/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2001  Francois de Coligny
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package capsis.extension.modeltool.forestgales;

import java.util.StringTokenizer;

/**
 * A format description for ForestGales fgo files records.
 *
 * @author F. de Coligny, C. Meredieu - december 2002
 */
public class FgoRecord {

	public String standId;

	public double breakageProbability;
	public double overturningProbability;
	public double breakageCWindSpeed;	// c stands for critical
	public double overturningCWindSpeed;

	public FgoRecord (String line) throws Exception {
		line = line.replace ('\00', ' ');
		StringTokenizer st = new StringTokenizer (line, ForestGales.columnSeparator);
		standId = st.nextToken ().trim ();

		breakageProbability = new Double (st.nextToken ()).doubleValue ();
		overturningProbability = new Double (st.nextToken ()).doubleValue ();
		breakageCWindSpeed = new Double (st.nextToken ()).doubleValue ();
		overturningCWindSpeed = new Double (st.nextToken ()).doubleValue ();

	}

	public String toString () {
		StringBuffer b = new StringBuffer ();
		b.append (standId);

		b.append (" ");
		b.append (breakageProbability);
		b.append (" ");
		b.append (overturningProbability);
		b.append (" ");
		b.append (breakageCWindSpeed);
		b.append (" ");
		b.append (overturningCWindSpeed);

		return b.toString ();
	}

}


