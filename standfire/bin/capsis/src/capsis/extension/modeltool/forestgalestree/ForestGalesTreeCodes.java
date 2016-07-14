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

package capsis.extension.modeltool.forestgalestree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import jeeb.lib.util.Import;
import jeeb.lib.util.Record;
import jeeb.lib.util.RecordSet;

/**
 * A format description for ForestGalesTree codes : Species, Soil, Cultivation and Drainage.
 *
 * @author C. Meredieu - august 2003
 */
public class ForestGalesTreeCodes extends RecordSet {
	private String fileName;
	private String currentSection;

	private Map speciesMap;
	private Map soilMap;
	private Map cultivationMap;
	private Map drainageMap;
	private Collection aCollection;
	private Collection kCollection;

	private int damsMin;
	private int damsMax;


	// Every RecordSet may contain DoubleValueRecords (ex: 12.75).
	@Import
	static public class DoubleValueRecord extends Record {
		public Double value;
		public DoubleValueRecord (String line) throws Exception {
			super ();
			String candidate = line.trim ();
			try {
				value = new Double (candidate);
			} catch (Exception e) {
				throw e;
			}
		}

		public String toString () {return ""+value;}
	}



	public ForestGalesTreeCodes (String fileName) throws Exception {
		super ();
		this.fileName = fileName;
		createRecordSet (fileName);
		createMaps ();
	}

	private void createMaps () throws Exception {
		speciesMap = new HashMap ();
		soilMap = new HashMap ();
		cultivationMap = new HashMap ();
		drainageMap = new HashMap ();
		aCollection = new Vector ();
		kCollection = new Vector ();

		for (Iterator i = this.iterator (); i.hasNext ();) {
			Object record = i.next ();
			if (record instanceof SectionRecord) {
				SectionRecord r = (SectionRecord) record;
				currentSection = r.sectionName.trim ().toLowerCase ();
			} else if (record instanceof KeyRecord) {
				KeyRecord r = (KeyRecord) record;

				if ("species".equals (currentSection)) {
					speciesMap.put (r.key, new Integer (r.getIntValue ()));

				} else if ("soil".equals (currentSection)) {
					soilMap.put (r.key, new Integer (r.getIntValue ()));

				} else if ("cultivation".equals (currentSection)) {
					cultivationMap.put (r.key, new Integer (r.getIntValue ()));

				} else if ("drainage".equals (currentSection)) {
					drainageMap.put (r.key, new Integer (r.getIntValue ()));

				} else if ("general".equals (currentSection)) {
					if ("damsMin".equals (r.key)) {
						damsMin = r.getIntValue ();
					} else if ("damsMax".equals (r.key)) {
						damsMax = r.getIntValue ();
					} else {
						throw new Exception ("wrong format in "+fileName+" near record "+r);
					}

				} else {
					throw new Exception ("wrong format in "+fileName+" near record "+r);
				}
			} else if (record instanceof DoubleValueRecord) {
				DoubleValueRecord r = (DoubleValueRecord) record;

				if ("a".equals (currentSection)) {
					aCollection.add (r.value);

				} else if ("k".equals (currentSection)) {
					kCollection.add (r.value);

				}

			} else {
				throw new Exception ("wrong format in "+fileName+" near record "+record);
			}
		}
	}

	public Map getSpeciesMap () {return speciesMap;}
	public Map getSoilMap () {return soilMap;}
	public Map getCultivationMap () {return cultivationMap;}
	public Map getDrainageMap () {return drainageMap;}
	public Collection getACollection () {return aCollection;}
	public Collection getKCollection () {return kCollection;}

	public int getDamsMin () {return damsMin;}
	public int getDamsMax () {return damsMax;}

}


