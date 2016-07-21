/** 
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 1999-2010 INRA 
 * 
 * Authors: F. de Coligny, S. Dufour-Kowalski, 
 * 
 * This file is part of Capsis
 * Capsis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Capsis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU lesser General Public License
 * along with Capsis.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package capsis.extension;

import java.io.Serializable;

import capsis.kernel.extensiontype.GenericExtensionStarter;

/**
 * This class describes the object that is passed to an extension's constructor.
 * References are filled in according to extension types.
 * 
 * @author F. de Coligny - march 2001
 */
public class EconomicFunctionStarter extends GenericExtensionStarter implements Serializable {
	//~ in Extensionstarter superclass
	//~ private GStand stand;
	//~ private GModel model;

	public int dateMin;
	public int dateMax;	// optionnal, max = min
	public String name;
	public String detail;

	
	public EconomicFunctionStarter (int dateMin, 
			int dateMax, 
			String name, 
			String detail) {
		this.dateMin = dateMin;
		this.dateMax = dateMax;
		this.name = name;
		this.detail = detail;
	}

	
	
	//~ in Extensionstarter superclass
	//~ public GStand getStand () {return stand;}
	//~ public GModel getModel () {return model;}
	
}
