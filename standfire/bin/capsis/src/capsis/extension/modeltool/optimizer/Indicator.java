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

import java.awt.Component;
import java.util.Vector;

import repicea.gui.REpiceaUIObject;
import repicea.math.EvaluableFunction;
import repicea.simulation.covariateproviders.standlevel.AgeYrProvider;
import repicea.simulation.covariateproviders.standlevel.AreaHaProvider;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;
import capsis.defaulttype.TreeList;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.util.methodprovider.VMerchantProviderWithName;
//import capsis.kernel.Step;

public class Indicator extends BoundableVariable implements REpiceaUIObject, EvaluableFunction<Double> {

	
	/** Names of simple indicators */
	protected static enum IndicatorID implements TextableEnum {
		Productivity("Mean annual productivity (m\u00B3/ha/yr)", "Productivit\u00E9 annuelle moyenne (m\u00B3/ha/an)"),
		Duration("Duration (yr)", "Dur\u00E9e (ann\u00E9es)"),
		GlobalDiscountRate("Global Discount Rate", "Taux global"),
		NetPresentValue("Net Present Value (\u20AC/ha)", "Valeur nette actualis\u00E9e (\u20AC/ha)"),
		AverageIncome("Average Income (\u20AC/ha)", "Revenus moyens (\u20AC/ha)"),
		TIR("TIR", "Taux interne de rendement"),
		FaustmannValue("Faustmann Value (\u20AC/ha)", "Valeur de Faustmann (\u20AC/ha)");

		IndicatorID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		} 
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	} 
	
	protected double coefficient = 1;
	protected double penalty = 1E6;
	protected boolean enabled = true;
	protected Double value = null;
	
	protected final IndicatorID indicatorID;

	protected transient OptimizerTool tool;
	private transient IndicatorPanel guiInterface;
	protected transient IndicatorList indicatorList;
	
	protected Indicator(IndicatorID indicatorID) {
		super();
		this.indicatorID = indicatorID;
//		this.indicatorList = subIndicators;
	}

	@Override
	public Component getUI() {
		if (guiInterface == null) {
			guiInterface = new IndicatorPanel(this);
		}
		return guiInterface;
	}

	@Override
	public Double getValue() {
		if (value == null) {
			value = calculate();
		}
		return value;
	}
	
	
	
	private Double calculate() {
		Vector<Step> steps = indicatorList.steps;		// TODO what is relative to original step
		CostBenefitCalculator cbc;
		Indicator duration;
		Indicator netPresentValue;
		GScene lastScene;
		double value;
		
		switch(indicatorID){
		case Productivity:
			VMerchantProviderWithName mp = (VMerchantProviderWithName) tool.of.methodProvider;
			double harvestedVolume = 0;
			for (Step step : steps) {
				GScene stand = step.getScene();
				if (stand.isInterventionResult()) {
					harvestedVolume += mp.getVMerchant(stand, ((TreeList) stand).getTrees(StatusClass.cut.name()));
				}
			}
			lastScene = steps.lastElement().getScene();
			double areaHa = ((AreaHaProvider) lastScene).getAreaHa();
			double age = ((AgeYrProvider) lastScene).getAgeYr();		
			double averageHarvestedVolume = harvestedVolume / areaHa / age;
			
			double averageFinalVolume = mp.getVMerchant(lastScene, ((TreeList) lastScene).getTrees()) / areaHa / age;
			value = averageHarvestedVolume + averageFinalVolume;
			return value;
		case Duration:
			lastScene = steps.lastElement().getScene();
			value = ((Integer) ((AgeYrProvider) lastScene).getAgeYr()).doubleValue();
			return value;
		case GlobalDiscountRate:
			duration = indicatorList.getIndicator(IndicatorID.Duration);;
			cbc = tool.of.cbc;
			value = Math.pow(1 + cbc.getDiscountRate(), - duration.getValue()); 
			return value;
		case NetPresentValue:
			cbc = tool.of.cbc;
			value = cbc.getNetPresentValue(tool.of.findStepVectorFromOriginalStep(steps));
			return value;
		case AverageIncome:
			cbc = tool.of.cbc;
			double previousDiscountRate = cbc.getDiscountRate();
			cbc.setDiscountRate(0d);
			double incomes = cbc.getNetPresentValue(tool.of.findStepVectorFromOriginalStep(steps));
			duration = indicatorList.getIndicator(IndicatorID.Duration);
			value = incomes / duration.getValue();
			cbc.setDiscountRate(previousDiscountRate);
			return value;
		case TIR:
			double discountRateSup = 2;
			double discountRateInf = -1;
			double EPSILON = 1E-16;
			value = Double.NaN;
			cbc = tool.of.cbc;
			double originalDiscountRate = cbc.getDiscountRate();
			
			while (Math.abs(discountRateSup - discountRateInf) > EPSILON) {		// FIXME too long
				double discountRate = (discountRateSup + discountRateInf) / 2d;
				cbc.setDiscountRate(discountRate);
				double BAS = cbc.getNetPresentValue(tool.of.findStepVectorFromOriginalStep(steps));
				if (BAS > 0) {
					discountRateInf = discountRate;
				} else {
					discountRateSup = discountRate;
				}
				value = discountRate;
			}
			
			cbc.setDiscountRate(originalDiscountRate);
			return value;
		case FaustmannValue:
			netPresentValue = indicatorList.getIndicator(IndicatorID.NetPresentValue);
			Indicator globalDiscountRate = indicatorList.getIndicator(IndicatorID.GlobalDiscountRate);
			value = netPresentValue.getValue() / (1 - globalDiscountRate.getValue());
			return value;
		default:
			return null;
		}
	}
	
	protected double getWeightedValue() {
		double value = getValue();
		if (value < minValue) {
			return (value - minValue) * penalty;
		} else if (value > maxValue) {
			return (maxValue - value) * penalty;
		} else {
			return coefficient * value;
		}
	}

	protected void restoreParameters(OptimizerTool tool) {
		OptimizerToolDefaultSettings settings = (OptimizerToolDefaultSettings) tool.getSettingMemory();
		if (settings != null) {
			String coefficientProperty = "optimizer.indicator.coefficient".concat(indicatorID.name());
			coefficient = settings.getProperty(coefficientProperty, 1d);
			String enabledProperty = "optimizer.indicator.enabled".concat(indicatorID.name());
			enabled = settings.getProperty(enabledProperty, true);
			String penaltyProperty = "optimizer.indicator.penalty".concat(indicatorID.name());
			penalty = settings.getProperty(penaltyProperty, 1E6);
			String minProperty = "optimizer.indicator.max".concat(indicatorID.name());
			minValue = settings.getProperty(minProperty, Double.NEGATIVE_INFINITY);
			String maxProperty = "optimizer.indicator.min".concat(indicatorID.name());
			maxValue = settings.getProperty(maxProperty, Double.POSITIVE_INFINITY);
		}
	}

	protected void saveParametersIntoSettings(OptimizerTool tool) {
		OptimizerToolDefaultSettings settings = (OptimizerToolDefaultSettings) tool.getSettingMemory();
		if (settings != null) {
			String coefficientProperty = "optimizer.indicator.coefficient".concat(indicatorID.name());
			settings.setProperty(coefficientProperty, coefficient);
			String enabledProperty = "optimizer.indicator.enabled".concat(indicatorID.name());
			settings.setProperty(enabledProperty, enabled);
			String penaltyProperty = "optimizer.indicator.penalty".concat(indicatorID.name());
			settings.setProperty(penaltyProperty, penalty);
			String minProperty = "optimizer.indicator.max".concat(indicatorID.name());
			settings.setProperty(minProperty, minValue);
			String maxProperty = "optimizer.indicator.min".concat(indicatorID.name());
			settings.setProperty(maxProperty, maxValue);
		}
	}

	@Override 
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

	
	
}
