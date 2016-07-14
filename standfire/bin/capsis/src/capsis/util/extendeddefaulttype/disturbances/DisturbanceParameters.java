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
package capsis.util.extendeddefaulttype.disturbances;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mathilde.extension.ioformat.MathildeExportTool.MathildeFieldName;
import repicea.io.GExportFieldDetails;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;
import capsis.util.extendeddefaulttype.ExtModel;

public class DisturbanceParameters implements Serializable {
	
	
	public static enum DisturbanceType implements TextableEnum {
		Windstorm("Windstorm","Temp\u00EAte"),
		Drought("Drought", "S\u00E9cheresse"),
		Harvest("Harvest", "Coupe");
		
		DisturbanceType(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}
	
	
	public static enum DisturbanceMode {
		None, 
		NextStep, 
		Random,
		ModelBased,
		RuleBased;
	}

	private final DisturbanceType type;
	private final DisturbanceMode mode;
	protected final int recurrenceYrs;

	/**
	 * Constructor.
	 * @param type a DisturbanceType enum
	 * @param mode a DisturbanceMode enum
	 * @param recurrenceYrs
	 */
	public DisturbanceParameters(DisturbanceType type, DisturbanceMode mode, int recurrenceYrs) {
		this.type = type;
		this.mode = mode;
		this.recurrenceYrs = recurrenceYrs;
	}

	public List<GExportFieldDetails> getRecords() {
		List<GExportFieldDetails> fields = new ArrayList<GExportFieldDetails>();
		fields.add(new GExportFieldDetails(REpiceaTranslator.getString(MathildeFieldName.Disturbance).concat(type.toString()), mode.name()));
		fields.add(new GExportFieldDetails(REpiceaTranslator.getString(MathildeFieldName.Recurrence).concat(type.toString()), recurrenceYrs));
		return fields;
	}

	public DisturbanceType getType() {return type;}
	
	public DisturbanceMode getMode() {return mode;}

	/**
	 * This method returns an object that defines whether or not there is a disturbance. In deterministic mode, that object is a double while it is a boolean 
	 * in most stochastic implementations.
	 * @param yrs the length of the growth interval
	 * @param parms eventual parameters
	 * @return an Object either a double or a boolean
	 */
	public Object isThereADisturbance(int yrs, Object... parms) {
		if (mode == DisturbanceMode.None) {
			return false;
		} else if (mode == DisturbanceMode.NextStep) {
			return true;
		} else if (mode == DisturbanceMode.Random) {
			double annualProbability = 1d / recurrenceYrs;
			double probabilityOverInterval = 1 - Math.pow(1-annualProbability, yrs);
			return ExtModel.RANDOM.nextDouble() < probabilityOverInterval;
		} else return null;
	}
	
	public DisturbanceParameters mute() {
		DisturbanceMode mode = this.getMode();
		if (mode == DisturbanceMode.NextStep) {
			mode = DisturbanceMode.None;
		}
		DisturbanceParameters mutant = new DisturbanceParameters(getType(), mode, recurrenceYrs);
		return mutant;
	}
	
}
