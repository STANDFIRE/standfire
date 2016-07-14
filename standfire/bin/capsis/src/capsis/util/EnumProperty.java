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

import java.util.HashMap;

/**
 * Enum property : has one value among a known set of values.
 * 
 * @author F. de Coligny - february 2003
 */
public class EnumProperty implements QualitativeProperty {

	private EnumPropertyInfo info;

	private int value;	// current value
	
	/**
	 * Constructor.
	 * <pre>
	 * Example :
	 * EnumProperty beech = new EnumProperty (1, "LoxModel.beech", null, "LoxModel.species");
	 * EnumProperty spruce = new EnumProperty (2, "LoxModel.spruce", beech, null);
	 * EnumProperty fir = new EnumProperty (3, "LoxModel.fir", beech, null);
	 * </pre>
	 */
	public EnumProperty (int v, String name, EnumProperty model, String propertyName) {
		if (model != null) {
			info = model.info;	// share common data
		} else {
			info = new EnumPropertyInfo (propertyName);
		}
		
		setValue (v, name);
	}
	
	public String getPropertyName () {return info.propertyName;}	
	public int getValue () {return value;}	
	public String getName () {return (String) info.valueToName.get (new Integer (value));}
	public boolean isValue (int value) {return this.value == value;}
	
	public HashMap getValues () {return (HashMap) info.valueToName;}

	public EnumPropertyInfo getInfo () {return info;}

	// Set the value and name of the property.
	//
	protected void setValue (int v, String name) {
		value = v;
		Integer vI = new Integer (v);
		info.valueToName.put (vI, name);
		info.valueToProperty.put(vI, this);
	}

}
