/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2013-2014  Mathieu Fortin - UMR LERFoB (AgroParisTech/INRA)
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

import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

abstract class BoundableVariable {

	protected static enum MessageID implements TextableEnum {
		Minimum("Min", "Min"),
		Maximum("Max", "Max");
		
		MessageID(String englishText, String frenchText) {
			setText (englishText, frenchText);
		}
		
		
		@Override
		public void setText (String englishText, String frenchText) {
			REpiceaTranslator.setString (this, englishText, frenchText);
		}
		
		@Override
		public String toString() {
			return REpiceaTranslator.getString (this);
		}
		
	}

	protected double minValue = Double.NEGATIVE_INFINITY;
	protected double maxValue = Double.POSITIVE_INFINITY;
	
	protected BoundableVariable() {}
	
	protected void setMinimumValue(double value) {
		this.minValue = value;
	}
	
	protected void setMaximumValue(double value) {
		this.maxValue = value;
	}

}
