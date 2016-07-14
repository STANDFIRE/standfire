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

package capsis.extension.intervener;

import capsis.kernel.AbstractSettings;

/**
 * C2ThinnerSettings - List of settings fro C2Thinner
 * May have defaults or not. May be modified by user action during model
 * initialization in interactive mode (initial dialog).
 *
 * @see C2DThinParameters
 * @author Ph. Dreyfus - March 2001
 */
public class C2ThinnerSettings extends AbstractSettings {
//upgraded for c4.0 - fc - 11.1.2001

	public static final int N = 0;
	public static final int G = 1;
	public static final int V = 2;
	public static final int R = 3;
	public static final int S = 4;

	protected int Sm; // stocking measure

	public C2ThinnerSettings () {
		Sm = N;
	}

	/*public C2ThinnerSettings () {
		resetSettings ();
	}
	public void resetSettings () {
		super.resetSettings ();
		Sm = N;
	}

	public String toString () {
		return " C2Thinner settings = "
			+super.toString ()
			+" Sm="+Sm;
	}*/

}



