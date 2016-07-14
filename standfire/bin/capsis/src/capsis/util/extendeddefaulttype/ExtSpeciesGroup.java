/* 
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2015 LERFoB AgroParisTech/INRA 
 * 
 * Authors: M. Fortin, 
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
package capsis.util.extendeddefaulttype;

import capsis.defaulttype.Species;
import capsis.util.EnumProperty;


public class ExtSpeciesGroup extends EnumProperty implements Species {

	private static final long serialVersionUID = 20101030L;
	
	private int nameID;
	private String name;
	
	/**
	 * Constructor for this class
	 * @param nameID an Integer that represents the species code
	 * @param name a String that represents the name of the species
	 */
	protected ExtSpeciesGroup(int nameID, String name) {
		this(nameID, name, null);
	}

	
	/**
	 * Constructor for this class
	 * @param nameID an Integer that represents the species code
	 * @param name a String that represents the name of the species
	 */
	protected ExtSpeciesGroup(int nameID, String name, EnumProperty model) {
		super(nameID, name, model, ExtConstantInitialParameters.MessageID.SpeciesGroup.toString ());
		this.nameID = nameID;
		this.name = name.trim().toUpperCase();
	}

	
	
	/**
	 * This method returns the code of the species.
	 * @return an Integer
	 */
	public int getValue() {return nameID;}
	
	/**
	 * This method returns the species name;
	 * @return a String
	 */
	public String getName() {return name;}

	
	
}
