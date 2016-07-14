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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import repicea.gui.REpiceaPanel;
import repicea.gui.UIControlManager;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldDocument.NumberFieldEvent;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldListener;
import repicea.gui.components.NumberFormatFieldFactory.Range;
import repicea.gui.components.NumberFormatFieldFactory.Type;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;


public class IndicatorPanel extends REpiceaPanel implements ItemListener, NumberFieldListener {

	protected static enum MessageID implements TextableEnum {
		Coefficient("Coefficient", "Coefficient"),
		Penalty("Penalty", "P\u00E9nalit\u00E9"),
		Enable("Enable", "Activer");

		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
		
	}
	
	
	private final Indicator caller;

	private final JLabel label;
	private final JFormattedNumericField lowerBoundField;
	private final JFormattedNumericField upperBoundField;
	private final JFormattedNumericField weightField;
	private final JFormattedNumericField penaltyField;
	private final JCheckBox enabledCheckBox;
	
	protected IndicatorPanel(Indicator caller) {
		this.caller = caller;
		label = UIControlManager.getLabel(this.caller.indicatorID); 
		lowerBoundField = NumberFormatFieldFactory.createNumberFormatField(5, Type.Double, Range.All, true);
		upperBoundField = NumberFormatFieldFactory.createNumberFormatField(5, Type.Double, Range.All, true);
		weightField = NumberFormatFieldFactory.createNumberFormatField(5, Type.Double, Range.All, false);
		penaltyField = NumberFormatFieldFactory.createNumberFormatField(10, Type.Double, Range.StrictlyPositive, false);		enabledCheckBox = new JCheckBox();

//		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setLayout(new BorderLayout());
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(labelPanel, BorderLayout.WEST);
		labelPanel.add(Box.createHorizontalStrut(5));
		labelPanel.add(label);
		
		JPanel componentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(componentPanel, BorderLayout.EAST);
		
		componentPanel.add(Box.createHorizontalStrut(10));
		componentPanel.add(UIControlManager.getLabel(BoundableVariable.MessageID.Minimum));
		componentPanel.add(Box.createHorizontalStrut(5));
		componentPanel.add(lowerBoundField);
		componentPanel.add(Box.createHorizontalStrut(5));
		
		componentPanel.add(UIControlManager.getLabel(BoundableVariable.MessageID.Maximum));
		componentPanel.add(Box.createHorizontalStrut(5));
		componentPanel.add(upperBoundField);
		componentPanel.add(Box.createHorizontalStrut(5));

		componentPanel.add(UIControlManager.getLabel(MessageID.Coefficient));
		componentPanel.add(Box.createHorizontalStrut(5));
		componentPanel.add(weightField);
		componentPanel.add(Box.createHorizontalStrut(5));

		componentPanel.add(UIControlManager.getLabel(MessageID.Penalty));
		componentPanel.add(Box.createHorizontalStrut(5));
		componentPanel.add(penaltyField);
		componentPanel.add(Box.createHorizontalStrut(5));
		
		componentPanel.add(UIControlManager.getLabel(MessageID.Enable));
		componentPanel.add(Box.createHorizontalStrut(5));
		componentPanel.add(enabledCheckBox);
		componentPanel.add(Box.createHorizontalStrut(5));
	}

	
	@Override
	public void refreshInterface() {
		lowerBoundField.setText(((Double) caller.minValue).toString());
		upperBoundField.setText(((Double) caller.maxValue).toString());
		weightField.setText(((Double) caller.coefficient).toString());
		penaltyField.setText(((Double) caller.penalty).toString());
		enabledCheckBox.setSelected(caller.enabled);
		enableFeatures(enabledCheckBox.isSelected());
	}

	@Override
	public void listenTo() {
		enabledCheckBox.addItemListener(this);
		lowerBoundField.addNumberFieldListener(this);
		upperBoundField.addNumberFieldListener(this);
		weightField.addNumberFieldListener(this);
		penaltyField.addNumberFieldListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		enabledCheckBox.removeItemListener(this);
		lowerBoundField.removeNumberFieldListener(this);
		upperBoundField.removeNumberFieldListener(this);
		weightField.removeNumberFieldListener(this);
		penaltyField.removeNumberFieldListener(this);
	}
	
	private void enableFeatures(boolean enabled) {
		lowerBoundField.setEnabled(enabled);
		upperBoundField.setEnabled(enabled);
		weightField.setEnabled(enabled);
		penaltyField.setEnabled(enabled);
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		if (arg0.getSource().equals(enabledCheckBox)) {
			caller.enabled = enabledCheckBox.isSelected();
			enableFeatures(enabledCheckBox.isSelected());
		}
	}


	@Override
	public void numberChanged(NumberFieldEvent e) {
		if (e.getSource().equals(lowerBoundField)) {
			if (lowerBoundField.getText().isEmpty()) {
				caller.setMinimumValue(Double.NEGATIVE_INFINITY);
			} else {
				caller.setMinimumValue(Double.parseDouble(lowerBoundField.getText()));
			}
		} else if (e.getSource().equals(upperBoundField)) {
			if (upperBoundField.getText().isEmpty()) {
				caller.setMinimumValue(Double.POSITIVE_INFINITY);
			} else {
				caller.setMaximumValue(Double.parseDouble(upperBoundField.getText()));
			}
		} else if (e.getSource().equals(weightField)) {
			caller.coefficient = Double.parseDouble(weightField.getText());
		} else if (e.getSource().equals(penaltyField)) {
			caller.penalty = Double.parseDouble(penaltyField.getText());
		}
	}

}
