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

package capsis.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Information about a set of qualitative properties.
 * 
 * @author F. de Coligny - february 2003
 */
public class EnumPropertyInfo implements Serializable {

	protected String propertyName;	// ex : "Species"
	protected Map valueToName;		// ex : 1 - "Beech"
	protected Map valueToProperty;	// ex : 1 - reference to the beech QualitativeProperty

	public EnumPropertyInfo (String propertyName) {
		this.propertyName = propertyName;
		valueToName = new HashMap ();
		valueToProperty = new HashMap ();
	}

	public String getPropertyName () {return propertyName;}
	public Map getValueToName () {return valueToName;}
	public Map getValueToProperty () {return valueToProperty;}

}
