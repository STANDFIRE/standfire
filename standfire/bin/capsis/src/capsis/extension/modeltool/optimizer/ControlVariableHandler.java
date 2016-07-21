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
import java.awt.Container;
import java.util.Random;

import javax.swing.JPanel;

import repicea.gui.CommonGuiUtility;
import repicea.gui.REpiceaUIObject;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldDocument.NumberFieldEvent;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldListener;
import repicea.serial.MemorizerPackage;
import repicea.serial.cloner.BasicSerialCloner;
import capsis.extension.modeltool.scenariorecorder.ParameterDialogWrapperLinker;

/**
 * The ControlVariableHandler class handles the value and the bounds for a particular control variable.
 * @author Mathieu Fortin - May 2014
 */
public class ControlVariableHandler extends BoundableVariable implements REpiceaUIObject, NumberFieldListener {

	
	protected final ParameterDialogWrapperLinker wrapperLinker;
	private transient Container container;
	private transient BasicSerialCloner cloner;
	
	private transient ControlVariableHandlerPanel guiInterface;
	
	
	protected ControlVariableHandler(ParameterDialogWrapperLinker wrapperLinker, Container container) {
		super();
		this.wrapperLinker = wrapperLinker;
		MemorizerPackage mp = new MemorizerPackage();
		mp.add(container);
		getCloner().memorize(mp);
	}
	
	
	private BasicSerialCloner getCloner() {
		if (cloner == null) {
			cloner = new BasicSerialCloner();
		}
		return cloner;
	}
	
	protected double getRealValue() {
		return getRealValueFromCurrentValue(getValue(), minValue, maxValue);
	}
	
	protected double getValue() {
		String text = wrapperLinker.getText();
		double currentValue = 0d;
		if (!text.isEmpty()) {
			currentValue = Double.parseDouble(text);
		}
		return currentValue;
	}
	
	
	protected static double getRealValueFromCurrentValue(double currentValue, double lowerBound, double upperBound) {
		double realValue;
		if (Double.isInfinite(lowerBound)){
			if (Double.isInfinite(upperBound)){
				realValue = currentValue;
			} else {
				realValue = -Math.log(upperBound - currentValue);
			}
		} else {
			if (Double.isInfinite(upperBound)) {
				realValue = Math.log(currentValue - lowerBound);
			} else {
				realValue = Math.tan(Math.PI * ((currentValue - lowerBound) / (upperBound - lowerBound) - 0.5));
			}
		}
		return realValue;
	}
	
	protected void setValue(double realValue) {
		double val;
		if (Double.isInfinite(minValue)) {
			if (Double.isInfinite(maxValue)) {
				val = realValue;
			} else {
				val = maxValue - Math.exp(-realValue) ;
			}
		} else {
			if (Double.isInfinite(maxValue)) {
				val = minValue + Math.exp(realValue) ;
			} else {
				val = minValue + (maxValue - minValue) * (0.5 + Math.atan(realValue)/Math.PI) ;
			}
		}
		wrapperLinker.setText(((Double) val).toString());		// finally the value is set in the JTextField 
	}

	
	
	@Override
	public ControlVariableHandlerPanel getUI() {
		if (guiInterface == null) {
			guiInterface = new ControlVariableHandlerPanel(this);
		}
		return guiInterface;
	}

	protected double getMinimum () {return minValue;}
	protected double getMaximum () {return maxValue;}

	@Override
	public void numberChanged (NumberFieldEvent e) {
		if (e.getSource().equals(guiInterface.minField)) {
			String text = guiInterface.minField.getText();
			if (text.isEmpty ()) {
				minValue = Double.NEGATIVE_INFINITY;
			} else {
				minValue = Double.parseDouble(text);
			}
		} else if (e.getSource().equals(guiInterface.maxField)) {
			String text = guiInterface.maxField.getText ();
			if (text.isEmpty ()) {
				maxValue = Double.POSITIVE_INFINITY;
			} else {
				maxValue = Double.parseDouble(text);
			}
		}
	}
	
	protected static double getRandomReal(Random random) {
		double currentValue = random.nextDouble();
		return Math.tan(Math.PI * (currentValue - 0.5));
	}

	protected Component getContainer() {
		if (container == null) {
			MemorizerPackage mp = getCloner().retrieve();
			if (mp != null) {
				container = (Container) mp.get(0);
			} else {
				Component comp = wrapperLinker.getComponent();
				Container container = CommonGuiUtility.getParentComponent(comp, JPanel.class);
				mp = new MemorizerPackage();
				mp.add(container);
				getCloner().cloneThisObject(mp);
				mp = getCloner().retrieve();
				this.container = (Container) mp.get(0);
			}
		}
		return container;
	}
	
	@Override 
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

}
