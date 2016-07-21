/* 
 * Samsaralight library for Capsis4.
 * 
 * Copyright (C) 2008 / 2012 Benoit Courbaud.
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
package capsis.lib.samsaralight;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import jeeb.lib.util.Log;
import jeeb.lib.util.RecordSet;

/**
 * A samsaraLigt class that read txt file with SamsaraLight options
 * 
 * @author B. Courbaud, N. Donès, M. Jonard, G. Ligot, F. de Coligny - October
 *         2008 / June 2012
 */
public class SLSettingsLoader extends RecordSet {

	private String fileName;

	// Generic keyword record is described in superclass: key = value
	// private boolean turbid_medium = true;
	// private boolean trunk_interception = false;
	// private int direct_angle_step = 5;
	// private int height_angle_min = 15;
	// private boolean use_diffuse = true;
	// private int diffuse_angle = 15;
	// private boolean soc = true;
	// private int leaf_on_date = 4;
	// private int leaff_off_date = 10;

	/**
	 * Constructor.
	 */
	public SLSettingsLoader(String fileName) throws Exception {
		this.fileName = fileName;
		// Add classes for lines recognition
		addAdditionalClass(SLMonthlyRecord.class);
		addAdditionalClass(SLHourlyRecord.class);
		createRecordSet(fileName);
	}

	/**
	 * Interprets the file, loads information in the given settings object.
	 */
	public void interpret(SLSettings sets) throws Exception {

		Collection keys = new ArrayList(); // to check is keys are missing

		for (Iterator i = this.iterator(); i.hasNext();) {
			Object record = i.next();

			if (record instanceof SLMonthlyRecord) {
				SLMonthlyRecord r = (SLMonthlyRecord) record;
				sets.addMontlyRecord(r);

			} else if (record instanceof SLHourlyRecord) {
				SLHourlyRecord r = (SLHourlyRecord) record;
				sets.addHourlyRecord(r);

			} else if (record instanceof KeyRecord) {
				KeyRecord r = (KeyRecord) record;
				keys.add(r.key);

				if (r.hasKey("turbid_medium")) {
					try {
						sets.turbidMedium = r.getBooleanValue();
					} catch (Exception e) {
						Log.println(Log.ERROR, "SLSettingsLoader.interpret ()",
								"Trouble with turbid_medium", e);
						throw e;
					}

				} else if (r.hasKey("trunk_interception")) {
					try {
						sets.trunkInterception = r.getBooleanValue();
					} catch (Exception e) {
						Log.println(Log.ERROR, "SLSettingsLoader.interpret ()",
								"Trouble with trunkInterception", e);
						throw e;
					}

				} else if (r.hasKey("direct_angle_step")) {
					try {
						sets.directAngleStep = r.getDoubleValue();
					} catch (Exception e) {
						Log.println(Log.ERROR, "SLSettingsLoader.interpret ()",
								"Trouble with directAngleStep", e);
						throw e;
					}

				} else if (r.hasKey("height_angle_min")) {
					try {
						sets.heightAngleMin = r.getDoubleValue();
					} catch (Exception e) {
						Log.println(Log.ERROR, "SLSettingsLoader.interpret ()",
								"Trouble with heightAngleMin", e);
						throw e;
					}

				} else if (r.hasKey("diffuse_angle_step")) {
					try {
						sets.diffuseAngleStep = r.getDoubleValue();
					} catch (Exception e) {
						Log.println(Log.ERROR, "SLSettingsLoader.interpret ()",
								"Trouble with diffuseAngleStep", e);
						throw e;
					}

				} else if (r.hasKey("soc")) {
					try {
						sets.soc = r.getBooleanValue();
					} catch (Exception e) {
						Log.println(Log.ERROR, "SLSettingsLoader.interpret ()",
								"Trouble with soc", e);
						throw e;
					}

				} else if (r.hasKey("GMT")) {
					try {
						sets.setGMT(r.getIntValue());
					} catch (Exception e) {
						Log.println(Log.ERROR, "SLSettingsLoader.interpret ()",
								"Trouble with GMT", e);
						throw e;
					}
				} else if (r.hasKey("leaf_on_doy")) {
					try {
						sets.leafOnDoy = r.getIntValue();
					} catch (Exception e) {
						Log.println(Log.ERROR, "SLSettingsLoader.interpret ()",
								"Trouble with leafOnDoy", e);
						throw e;
					}

				} else if (r.hasKey("leaf_off_doy")) {
					try {
						sets.leafOffDoy = r.getIntValue();
					} catch (Exception e) {
						Log.println(Log.ERROR, "SLSettingsLoader.interpret ()",
								"Trouble with leafOffDoy", e);
						throw e;
					}

				}

			} else {
				throw new Exception("wrong format in " + fileName
						+ " near record " + record);
			}

		}

		// If monthlyRecords were found, we expect 12 exactly
		if (sets.getMontlyRecords() != null
				&& sets.getMontlyRecords().size() != 12) {
			throw new Exception("Error in file " + fileName
					+ ", wrong number of monthly records: "
					+ sets.getMontlyRecords().size() + ", should be 12");

		}

	}

}
