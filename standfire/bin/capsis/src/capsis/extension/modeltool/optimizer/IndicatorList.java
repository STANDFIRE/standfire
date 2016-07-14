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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import repicea.gui.REpiceaUIObject;
import repicea.gui.Resettable;
import repicea.math.EvaluableFunction;
import repicea.math.Matrix;
import capsis.extension.modeltool.optimizer.Indicator.IndicatorID;
import capsis.kernel.Step;

public class IndicatorList extends ArrayList<Indicator> implements EvaluableFunction<Double>, Resettable,
		REpiceaUIObject {

	protected transient Vector<Step> steps;
	protected transient IndicatorListPanel guiInterface;
	protected transient final OptimizerTool tool;

	protected IndicatorList(OptimizerTool tool) {
		super();
		this.tool = tool;
		Indicator currentIndicator;
		for (IndicatorID indicatorID : IndicatorID.values()) {
			currentIndicator = new Indicator(indicatorID);
			currentIndicator.restoreParameters(tool);
			add(currentIndicator);
		}
		reset();
	}

	@Override
	public boolean add(Indicator indicator) {
		indicator.tool = tool;
		indicator.indicatorList = this;
		return super.add(indicator);
	}

	@Override
	public boolean addAll(Collection<? extends Indicator> indicators) {
		for (Indicator indicator : indicators) {
			add(indicator);
		}
		return true;
	}

	protected int numberOfContributingIndicators() {
		int nbIndicators = 0;
		for (Indicator indicator : this) {
			if (indicator.enabled && indicator.coefficient != 0) {
				nbIndicators++;
			}
		}
		return nbIndicators;
	}

	protected void resetValues() {
		for (Indicator indicator : this) {
			indicator.value = null;
		}
	}

	protected Indicator getIndicator(IndicatorID indicatorID) {
		return get(indicatorID.ordinal());
	}

	protected Matrix getIndividualIndicatorValues() {
		Matrix matrix = new Matrix(size(), 1);
		for (int i = 0; i < size(); i++) {
			matrix.m_afData[i][0] = get(i).getValue();
		}
		return matrix;
	}

	@Override
	public Double getValue() {
		double value = 0;
		for (Indicator indicator : this) {
			if (indicator.enabled) {
				value += indicator.getWeightedValue();
			}
		}
		return value;
	}

	@Override
	public void reset() {
		for (Indicator indicator : this) {
			if (indicator.indicatorID == IndicatorID.Productivity) {
				indicator.enabled = true;
			} else {
				indicator.enabled = false;
			}
		}

	}

	@Override
	public IndicatorListPanel getUI() {
		if (guiInterface == null) {
			guiInterface = new IndicatorListPanel(this);
		}
		return guiInterface;
	}
}
