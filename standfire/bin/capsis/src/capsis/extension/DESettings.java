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

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import jeeb.lib.util.annotation.Ignore;
import capsis.kernel.AbstractSettings;

/**
 * DataExtractors Settings.
 *
 * @author F. de Coligny - December 2000
 */
public class DESettings extends AbstractSettings {
	
	public boolean perHa;
	public boolean percentage;
	public int classWidth = 15;
	public int intervalNumber = 10;
	public double intervalSize = 1;	// m.
	public int icNumberOfSimulations = 0;
	public double icRisk = 1;	// %
	public double icPrecision = 0.01;
	
	// Common groupers management.
	// (Individual groupers are stored in DataExtractor -> not memorized by ExtensionManager).
	//
	public String c_grouperType;	// added - fc - 13.9.2004
	public String i_grouperType;	// added - fc - 13.9.2004
	
	public boolean c_grouperMode;
	public String c_grouperName;
	public boolean c_grouperNot;	// fc - 21.4.2004
	
	//~ public String[] status;		// fc - 22.3.2004 - do not memorize individual config
	
	
	// General config properties (moved from DataExtractor - fc - 9.10.2003)
	public java.util.List configProperties = new ArrayList ();
	
	// A boolean property is a boolean with a name. Two cases :
	// name : rendered by a single checkbox (name is translated)
	// group_name : all the properties in same "group" are rendered by checkboxes 
	// in a bordered panel named "group"
	// exs: visibleStepsOnly, stand_G, stand_V, stand_N, thinning_V, thinning_G...
	// see DEStandTable.
	// String (property name) - Boolean
	public Map booleanProperties = new TreeMap (); 
	// NOTE: (TODO)
	// TreeMap just upper: ordered on untranslated keys -> not very nice
	// tried LinkedHashMap, but results in trouble: options order changes all the time, pb found by CMeredieu and TLabbé (august 2012), removed
	
	// Radio properties are represented by radio buttons.
	// Format : group_name1, group_name2,... group_namen
	// The buttons are in a bordered panel named "group".
	// String (property name) - Boolean
	public Map radioProperties = new TreeMap ();

	// Input management for int values
	// String (property name) - Integer
	public Map intProperties = new TreeMap ();

	// Input management for double values
	// String (property name) - Double
	public Map doubleProperties = new TreeMap ();

	// Input management for sets of String values
	// String (property name) - Set
	public Map setProperties = new TreeMap ();

	// A String value in a list of possibilities
	// fc - 10.6.2004 - for fg (selection of two groups)
	// 
	@Ignore
	public Map comboProperties = new TreeMap ();

	
	public String toString () {
		return "- DESettings -"
				+"  perHa="+perHa
				+"  booleanProperties="+booleanProperties
				+"  radioProperties="+radioProperties
				+"  intProperties="+intProperties
				+"  doubleProperties="+doubleProperties
				+"  setProperties="+setProperties
				+"- end-of-DESettings -";
	}

}

