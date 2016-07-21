/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2013-2015  Mathieu Fortin - UMR LERFoB (AgroParisTech/INRA)
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
package capsis.extension.modeltool.optimizer;

import java.io.Serializable;


public class CostRecord implements Serializable {
	
	protected static enum CostType {
		maintenance,
		harvest,
		annual		// TODO what to do with this one
	}
	
	protected static enum CostUnit {
		ha,
		m3,
		stem
	}

	private String label;
	private CostType type;
	private CostUnit unit;
	private int age = 0;
	private double cost = 0;
	private boolean activated;
	
	protected CostRecord(Object[] record) {
		label = record[0].toString().trim();
		if (record[1] instanceof CostType) {
			type = (CostType) record[1];
		} else {
			type = CostType.valueOf(record[1].toString().trim().toLowerCase());
		}
		if (record[2] instanceof CostUnit) {
			unit = (CostUnit) record[2];
		} else {
			unit = CostUnit.valueOf(record[2].toString().trim().toLowerCase());
		}
		if (!record[3].toString().isEmpty()) {
			age = ((Double) Double.parseDouble(record[3].toString())).intValue();
		}
		if (!record[4].toString().isEmpty()) {
			cost = Double.parseDouble(record[4].toString());
		}
		if (record.length > 5) {
			activated = Boolean.parseBoolean(record[5].toString());
		} else {
			activated = true;
		}
	}
	
	@Override
	public String toString() {
		return label + " " + type + " " + unit + " age = " + age + " cost = " + cost; 
	}

	protected Object[] getRecord() {
		Object[] record = new Object[6];
		record[0] = label;
		record[1] = type;
		record[2] = unit;
		record[3] = age;
		record[4] = cost;
		record[5] = activated;
		return record;
	}
	
	protected CostUnit getUnit() {return unit;}
	protected CostType getType() {return type;}
	protected double getCost() {return cost;}
	protected double getAge() {return age;}
	protected boolean isEnabled() {return activated;}
	
	
}
