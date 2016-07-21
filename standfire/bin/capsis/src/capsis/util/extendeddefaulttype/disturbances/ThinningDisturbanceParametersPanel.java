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

import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import jeeb.lib.util.Settings;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;
import repicea.simulation.REpiceaLogisticPredictor;
import repicea.simulation.covariateproviders.standlevel.StochasticInformationProvider;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.util.extendeddefaulttype.ExtCompositeStand;
import capsis.util.extendeddefaulttype.ExtCompositeStand.PredictorID;
import capsis.util.extendeddefaulttype.disturbances.ApplicationScaleProvider.ApplicationScale;
import capsis.util.extendeddefaulttype.disturbances.DisturbanceParameters.DisturbanceType;
import capsis.util.methodprovider.GProvider;
import capsis.util.methodprovider.VProvider;

public class ThinningDisturbanceParametersPanel extends DisturbanceParametersPanel {

	public static enum BoundaryVariable implements TextableEnum {
//		N("N (trees/ha)", "N (arbre/ha)"),
		G("G (m2/ha)", "G (m2/ha)"),
		V("V (m3/ha)", "V (m3/ha)"),
		;

		BoundaryVariable(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}

	

	private enum MessageID implements TextableEnum {
		AccordingToRuleLabel("According to this rule", "Selon cette r\u00E8gle"),
		AccordingToModelLabel("According to the model (business-as-usual)", "Selon le mod\u00E8le (business-as-usual)"),
		MinimumLabel("Min","Min"),
		MaximumLabel("Max","Max"),
		HarvestOccurrence("Harvest occurrence", "Occurrence de coupe"),
		;

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

	private JRadioButton accordingToModelButton;
	private JRadioButton accordingToRuleButton;
	private JComboBox<BoundaryVariable> boundaryVariableSelector;
	private JFormattedNumericField minBoundaryValueField;
	private JFormattedNumericField maxBoundaryValueField;


	public ThinningDisturbanceParametersPanel(Step step) {
		super(DisturbanceType.Harvest, step);
	}
	
	@Override
	protected void initializeControls() {
		super.initializeControls();
		accordingToModelButton = new JRadioButton(MessageID.AccordingToModelLabel.toString());
		accordingToRuleButton = new JRadioButton(MessageID.AccordingToRuleLabel.toString());
		buttonGroup.add(accordingToModelButton);
		buttonGroup.add(accordingToRuleButton);
		List<BoundaryVariable> eligibleBoundaryVariables = new ArrayList<BoundaryVariable>();

		MethodProvider mp = null;
		if (model != null) {
			mp = model.getMethodProvider();
		}

//		if (mp instanceof NProvider || mp == null) {
//			eligibleBoundaryVariables.add(BoundaryVariable.N);
//		}
		if (mp instanceof GProvider || mp == null) {
			eligibleBoundaryVariables.add(BoundaryVariable.G);
		}
		if (mp instanceof VProvider || mp == null) {
			eligibleBoundaryVariables.add(BoundaryVariable.V);
		}
		boundaryVariableSelector = new JComboBox<BoundaryVariable>();
		boundaryVariableSelector.setModel(new DefaultComboBoxModel<BoundaryVariable>(eligibleBoundaryVariables.toArray(new BoundaryVariable[]{})));
		minBoundaryValueField = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Double, NumberFormatFieldFactory.Range.Positive, true);
		maxBoundaryValueField = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Double, NumberFormatFieldFactory.Range.Positive, true);
	}
	
	@Override
	public void loadDefaultValues() {
		super.loadDefaultValues();
		setIndex(Settings.getProperty(getPropertyName("boundaryselector"), 0), boundaryVariableSelector);
		setJFormattedNumericField(maxBoundaryValueField, Settings.getProperty(getPropertyName("maxbound"), 50d));
		setJFormattedNumericField(minBoundaryValueField, Settings.getProperty(getPropertyName("minbound"), 0d));
		accordingToModelButton.setSelected(Settings.getProperty(getPropertyName("modelButton"), false));
		accordingToRuleButton.setSelected(Settings.getProperty(getPropertyName("ruleButton"), false));
		if (accordingToRuleButton.isSelected() && !accordingToRulePanelEnabled()) { // that would  inconsistent
			nextStepButton.setSelected(true);	// default value
		}
	}
	
	private void setJFormattedNumericField(JFormattedNumericField field, Double value) {
		if (value == null || Double.isNaN(value)) {
			field.setText("");
		} else {
			field.setText(value.toString());
		}
		
	}
	
	@Override
	public void setEnabled(boolean bool) {
		super.setEnabled(bool);
		boolean checkBoxSelected = occurrenceChkBox.isSelected();
		accordingToModelButton.setEnabled(bool && checkBoxSelected);
		accordingToRuleButton.setEnabled(bool && checkBoxSelected);
		boundaryVariableSelector.setEnabled(bool && checkBoxSelected);
		minBoundaryValueField.setEnabled(bool && checkBoxSelected);
		maxBoundaryValueField.setEnabled(bool && checkBoxSelected);
	}

	private void setIndex(int selectedIndex, JComboBox comboBox) {
		if (selectedIndex > comboBox.getModel().getSize() - 1) {
			comboBox.setSelectedIndex(0);
		} else {
			comboBox.setSelectedIndex(selectedIndex);
		}
	}

	@Override
	public void saveProperties() {
		super.saveProperties();
		Settings.setProperty(getPropertyName("boundaryselector"), boundaryVariableSelector.getSelectedIndex());
		Settings.setProperty(getPropertyName("maxbound"), maxBoundaryValueField.getValue().doubleValue());
		Settings.setProperty(getPropertyName("minbound"), minBoundaryValueField.getValue().doubleValue());
		Settings.setProperty(getPropertyName("modelButton"), accordingToModelButton.isSelected());
		Settings.setProperty(getPropertyName("ruleButton"), accordingToRuleButton.isSelected());
	}
	
	private boolean accordingToRulePanelEnabled() {
		boolean showAccordingToRulePanel = true;	// default value
		if (step.getScene() instanceof ApplicationScaleProvider) {
			showAccordingToRulePanel = ((ApplicationScaleProvider) step.getScene()).getApplicationScale() == ApplicationScale.Stand;	// the panel is shown only at the stand scale
		}
		return showAccordingToRulePanel;
	}
	
	private boolean accordingToModelPanelEnabled() {
		boolean showAccordingToModelPanel = false;	// default value
		if (step.getScene() instanceof StochasticInformationProvider) {
			showAccordingToModelPanel = ((StochasticInformationProvider) step.getScene()).isStochastic();
		}
		return showAccordingToModelPanel;
	}
	
	@Override
	protected void createUI(){
		super.createUI();
		
		if (accordingToModelPanelEnabled()) {
			JPanel occurrencePanel2 = new JPanel();
			occurrencePanel2.setLayout(new BoxLayout(occurrencePanel2, BoxLayout.X_AXIS));
			occurrencePanel2.add(Box.createHorizontalStrut(50));
			occurrencePanel2.add(accordingToModelButton);
			occurrencePanel2.add(Box.createGlue());
			add(occurrencePanel2);
		}
		
		if (accordingToRulePanelEnabled()) {
			JPanel boundaryPanel = new JPanel();
			boundaryPanel.setLayout(new BoxLayout(boundaryPanel, BoxLayout.X_AXIS));
			boundaryPanel.add(Box.createHorizontalStrut(50));
			boundaryPanel.add(accordingToRuleButton);
			boundaryPanel.add(Box.createHorizontalStrut(10));
			boundaryPanel.add(boundaryVariableSelector);
			boundaryPanel.add(Box.createHorizontalStrut(10));
			boundaryPanel.add(new JLabel(MessageID.MinimumLabel.toString()));
			boundaryPanel.add(minBoundaryValueField);
			boundaryPanel.add(Box.createHorizontalStrut(5));
			boundaryPanel.add(new JLabel(MessageID.MaximumLabel.toString()));
			boundaryPanel.add(maxBoundaryValueField);
			boundaryPanel.add(Box.createHorizontalGlue());

			add(boundaryPanel);
		} 
		
		
		
	}
	
	@Override
	protected void checkWhichFeatureShouldBeEnabled() {
		super.checkWhichFeatureShouldBeEnabled();
		boolean checkBoxSelected = occurrenceChkBox.isSelected();
		accordingToModelButton.setEnabled(checkBoxSelected);
		accordingToRuleButton.setEnabled(checkBoxSelected);
		boundaryVariableSelector.setEnabled(accordingToRuleButton.isSelected() && checkBoxSelected);
		maxBoundaryValueField.setEnabled(accordingToRuleButton.isSelected() && checkBoxSelected);
		minBoundaryValueField.setEnabled(accordingToRuleButton.isSelected() && checkBoxSelected);
	}

	@Override
	public void listenTo() {
		super.listenTo();
		accordingToModelButton.addItemListener(this);
		accordingToRuleButton.addItemListener(this);
	}



	@Override
	public void doNotListenToAnymore() {
		super.doNotListenToAnymore();
		accordingToModelButton.removeItemListener(this);
		accordingToRuleButton.removeItemListener(this);
	}
	
	/**
	 * This method returns the disturbance parameters as set through the dialog
	 * @return a DisturbanceParameters instance
	 */
	@Override
	public ThinningDisturbanceParameters getDisturbanceParameters() {
		if (!occurrenceChkBox.isSelected()) {
			return new ModelBasedThinningDisturbanceParameters();
		} else if (randomButton.isSelected()) {
			REpiceaLogisticPredictor treeModel = (REpiceaLogisticPredictor) ((ExtCompositeStand) step.getScene()).getPredictor(PredictorID.TREE_HARVESTING);
			return new ModelBasedThinningDisturbanceParameters(treeModel, Integer.parseInt(recurrence.getText()));
		} else if (nextStepButton.isSelected()) {
			REpiceaLogisticPredictor treeModel = (REpiceaLogisticPredictor) ((ExtCompositeStand) step.getScene()).getPredictor(PredictorID.TREE_HARVESTING);
			return new ModelBasedThinningDisturbanceParameters(treeModel);
		} else if (accordingToModelButton.isSelected()) {
			REpiceaLogisticPredictor standModel = (REpiceaLogisticPredictor) ((ExtCompositeStand) step.getScene()).getPredictor(PredictorID.STAND_HARVESTING);
			REpiceaLogisticPredictor treeModel = (REpiceaLogisticPredictor) ((ExtCompositeStand) step.getScene()).getPredictor(PredictorID.TREE_HARVESTING);
			return new ModelBasedThinningDisturbanceParameters(treeModel, standModel);
		} else {
			BoundaryVariable boundaryVariable = (BoundaryVariable) boundaryVariableSelector.getSelectedItem();
			double minimumValue = (Double) minBoundaryValueField.getValue();
			double maximumValue = (Double) maxBoundaryValueField.getValue();
			return new RuleBasedThinningDisturbanceParameters(boundaryVariable, minimumValue, maximumValue, step.getProject().getModel().getMethodProvider());
		}
	}


}
