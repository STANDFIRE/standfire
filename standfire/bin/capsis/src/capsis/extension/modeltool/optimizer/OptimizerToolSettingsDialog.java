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
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import repicea.gui.CommonGuiUtility;
import repicea.gui.CommonGuiUtility.FileChooserOutput;
import repicea.gui.OwnedWindow;
import repicea.gui.REpiceaDialog;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldDocument.NumberFieldEvent;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldListener;
import repicea.serial.Memorizable;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;
import capsis.extension.modeltool.scenariorecorder.ScenarioRecorderEvent;

class OptimizerToolSettingsDialog extends REpiceaDialog implements OwnedWindow, ActionListener, NumberFieldListener, ChangeListener, OptimizerListener {
	
	protected static enum MessageID implements TextableEnum {
		MaximumNumberEvaluations("Maximum number of evaluations", "Nombre maximum d'\u00E9valuations"),
		MaximumNumberIterations("Maximum number of iterations", "Nombre maximum d'it\u00E9rations"),
		RelativeConvergence("Relative convergence criterion", "Crit\u00E8re de convergence relative"),
		AbsoluteConvergence("Absolute convergence criterion", "Crit\u00E8re de convergence absolue"),
		LogPath("Log path", "Chemin de la log"),
		ConvergenceSettings("Convergence settings", "R\u00E9glages de convergence"),
		LogSettings("Log settings", "R\u00E9glages de la log"),
		GridSettings("Initial grid search settings", "R\u00E9glages de la grille de recherche initiale"),
		GridSize("Grid size divider", "Pas de la grille"),
		EstimatedNumberEvaluations("Estimated number of initial evaluations: ", "Estimation du nombre d'\u00E9valuations initiales : "),
		NumberOfRealizationsForGridSearch("Realizations in the grid search: ", "R\u00E9alisations dans la grille de recherche : "),
		NumberOfRealizationsForOptimization("Realizations in the optimization: ", "R\u00E9alisations lors de l'optimisation : "),
		StochasticSettings("Stochastic settings", "R\u00E9glages des param\u00E8tres stochastiques")
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
	
	
	private final OptimizerToolSettings caller;
	private final JFormattedNumericField relativeConvergenceField;
	private final JFormattedNumericField absoluteConvergenceField;
	private final JFormattedNumericField maxNumberEvaluationsField;
	private final JFormattedNumericField numberOfRealizationsForGridSearchField;
	private final JFormattedNumericField numberOfRealizationsForOptimizationField;
	private final JSlider numberIterationsSlider;
	private final JSlider gridSizeSlider;
	private final JTextField logPathField;
	private final JTextField estimatedNumberEvaluationsField;
	private final JButton openLogPathButton;
	
	protected OptimizerToolSettingsDialog(OptimizerToolSettings settings, Window parent) {
		super(parent);
		caller = settings;
		cancelOnClose = false;
		maxNumberEvaluationsField = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Integer, NumberFormatFieldFactory.Range.StrictlyPositive, false);
		numberOfRealizationsForGridSearchField = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Integer, NumberFormatFieldFactory.Range.StrictlyPositive, false);
		numberOfRealizationsForOptimizationField = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Integer, NumberFormatFieldFactory.Range.StrictlyPositive, false);
		relativeConvergenceField = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Double, NumberFormatFieldFactory.Range.StrictlyPositive, false);
		absoluteConvergenceField = NumberFormatFieldFactory.createNumberFormatField(10, NumberFormatFieldFactory.Type.Double, NumberFormatFieldFactory.Range.StrictlyPositive, false);
		logPathField = new JTextField(20);
		logPathField.setEditable(false);
		logPathField.setBackground(Color.WHITE);
		openLogPathButton = UIControlManager.createCommonButton(CommonControlID.Open);
		gridSizeSlider = new JSlider(2,10);
		gridSizeSlider.setPaintTicks(true);
		gridSizeSlider.setPaintLabels(true);
		gridSizeSlider.setMajorTickSpacing(1);
		gridSizeSlider.setSnapToTicks(true);
			
		numberIterationsSlider = new JSlider(100,500);
		numberIterationsSlider.setPaintTicks(true);
		numberIterationsSlider.setPaintLabels(true);
		numberIterationsSlider.setMajorTickSpacing(100);
		numberIterationsSlider.setSnapToTicks(true);
		
		estimatedNumberEvaluationsField = new JTextField(6);
		estimatedNumberEvaluationsField.setEditable(false);
		estimatedNumberEvaluationsField.setBackground(Color.WHITE);
		
		caller.optimizer.addOptimizerListener(this);
		
		synchronizeUIWithOwner();
		initUI();
		pack();
	}
	
	@Override
	public void listenTo() {
		openLogPathButton.addActionListener(this);
		relativeConvergenceField.addNumberFieldListener(this);
		absoluteConvergenceField.addNumberFieldListener(this);
		maxNumberEvaluationsField.addNumberFieldListener(this);
		numberOfRealizationsForGridSearchField.addNumberFieldListener(this);
		numberOfRealizationsForOptimizationField.addNumberFieldListener(this);
		gridSizeSlider.addChangeListener(this);
		numberIterationsSlider.addChangeListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		openLogPathButton.removeActionListener(this);
		relativeConvergenceField.removeNumberFieldListener(this);
		absoluteConvergenceField.removeNumberFieldListener(this);
		maxNumberEvaluationsField.removeNumberFieldListener(this);
		numberOfRealizationsForGridSearchField.removeNumberFieldListener(this);
		numberOfRealizationsForOptimizationField.removeNumberFieldListener(this);
		gridSizeSlider.removeChangeListener(this);
		numberIterationsSlider.removeChangeListener(this);
	}

	@Override
	protected void initUI() {
		setTitle(OptimizerToolDialog.MessageID.SettingsMenuItem.toString());
		getContentPane().setLayout(new BorderLayout());
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		
		Border etchedBorder = BorderFactory.createEtchedBorder();
		JPanel subPanel1 = new JPanel();
		mainPanel.add(subPanel1);
		
		subPanel1.setBorder(BorderFactory.createTitledBorder(etchedBorder, MessageID.ConvergenceSettings.toString()));
		subPanel1.setLayout(new BoxLayout(subPanel1, BoxLayout.Y_AXIS));
		subPanel1.add(Box.createVerticalStrut(5));
		subPanel1.add(createPanel(MessageID.MaximumNumberEvaluations, maxNumberEvaluationsField));
		subPanel1.add(Box.createVerticalStrut(5));
		subPanel1.add(createPanel(MessageID.MaximumNumberIterations, numberIterationsSlider));
		subPanel1.add(Box.createVerticalStrut(5));
		subPanel1.add(createPanel(MessageID.RelativeConvergence, relativeConvergenceField));
		subPanel1.add(Box.createVerticalStrut(5));
		subPanel1.add(createPanel(MessageID.AbsoluteConvergence, absoluteConvergenceField));
		subPanel1.add(Box.createVerticalStrut(5));

		JPanel subPanel1_2 = new JPanel();
		subPanel1_2.setLayout(new BoxLayout(subPanel1_2, BoxLayout.Y_AXIS));
		mainPanel.add(subPanel1_2);
		
		subPanel1_2.setBorder(BorderFactory.createTitledBorder(etchedBorder, MessageID.StochasticSettings.toString()));
		subPanel1_2.add(Box.createVerticalStrut(5));
		subPanel1_2.add(createPanel(MessageID.NumberOfRealizationsForGridSearch, numberOfRealizationsForGridSearchField));
		subPanel1_2.add(Box.createVerticalStrut(5));
		subPanel1_2.add(createPanel(MessageID.NumberOfRealizationsForOptimization, numberOfRealizationsForOptimizationField));
		subPanel1_2.add(Box.createVerticalStrut(5));

		
		JPanel subPanel2 = new JPanel();
		subPanel2.setLayout(new BoxLayout(subPanel2, BoxLayout.Y_AXIS));
		mainPanel.add(subPanel2);

		subPanel2.setBorder(BorderFactory.createTitledBorder(etchedBorder, MessageID.GridSettings.toString()));
		subPanel2.add(Box.createVerticalStrut(5));
		subPanel2.add(createPanel(MessageID.GridSize, gridSizeSlider));
		subPanel2.add(Box.createVerticalStrut(5));
		subPanel2.add(createPanel(MessageID.EstimatedNumberEvaluations, estimatedNumberEvaluationsField));
		subPanel2.add(Box.createVerticalStrut(5));
		
		JPanel subPanel3 = new JPanel();
		subPanel3.setLayout(new BoxLayout(subPanel3, BoxLayout.Y_AXIS));
		mainPanel.add(subPanel3);

		subPanel3.setBorder(BorderFactory.createTitledBorder(etchedBorder, MessageID.LogSettings.toString()));
		subPanel3.add(Box.createVerticalStrut(5));
		subPanel3.add(createPanel(MessageID.LogPath, logPathField, openLogPathButton));
		subPanel3.add(Box.createVerticalStrut(5));
	}
	
	protected static JPanel createPanel(TextableEnum message, Component... comps) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(Box.createHorizontalStrut(5));
		panel.add(UIControlManager.getLabel(message));
		panel.add(Box.createGlue());
		for (int i = 0; i < comps.length; i++) {
			panel.add(comps[i]);
			panel.add(Box.createHorizontalStrut(5));
		}
		return panel;
	}
	


	@Override
	public void synchronizeUIWithOwner() {
		DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.CANADA);	// to ensure the digits are separed by a dot
		formatter.setMaximumFractionDigits(Integer.MAX_VALUE);
		maxNumberEvaluationsField.setText(((Integer) caller.maxNumberEvaluations).toString());
		numberIterationsSlider.setValue(caller.maxNumberIterations);
		relativeConvergenceField.setText(formatter.format(caller.relativeConvergence));
		absoluteConvergenceField.setText(formatter.format(caller.absoluteConvergence));
		numberOfRealizationsForGridSearchField.setText(((Integer) caller.numberOfRealizationsForGridSearch).toString());
		numberOfRealizationsForOptimizationField.setText(((Integer) caller.numberOfRealizationsForOptimization).toString());
		logPathField.setText(caller.logPath);
		gridSizeSlider.setValue(caller.gridDivision);
		numberOfRealizationsForGridSearchField.setEnabled(caller.isStochastic && caller.isStochasticAllowed);				// enabling only if the model is stochastic		
		numberOfRealizationsForOptimizationField.setEnabled(caller.isStochastic && caller.isStochasticAllowed);				// enabling only if the model is stochastic		

		refreshEstimatedNumberGridEval();
	}

	@Override
	public void setVisible(boolean bool) {
		if (!isVisible() && bool) {
			synchronizeUIWithOwner();
		}
		super.setVisible(bool);
	}
	
	
	
	private void refreshEstimatedNumberGridEval() {
		int numberEval = 0;
		if (caller.getNumberOfControlVariables() > 0) {
			numberEval = (int) Math.pow(caller.gridDivision, caller.getNumberOfControlVariables());
		}
		estimatedNumberEvaluationsField.setText(((Integer) numberEval).toString());
	}
	
	@Override
	public Memorizable getWindowOwner () {return caller;}

	@Override
	public void actionPerformed (ActionEvent evt) {
//		if (evt.getSource().equals(cancelButton)) {
//			cancelAction();
//		} else if (evt.getSource().equals(okButton)) {
//			okAction();
//		} else 
		if (evt.getSource().equals(openLogPathButton)) {
			openLogPathButtonAction();
		}
	}

	private void openLogPathButtonAction () {
		FileChooserOutput fco = CommonGuiUtility.browseAction(this, 
				JFileChooser.DIRECTORIES_ONLY, 
				caller.logPath, 
				null, 
				JFileChooser.OPEN_DIALOG);
		if (fco.isValid()) {
			caller.logPath = fco.getFilename();
			logPathField.setText(caller.logPath);
		}
	}

	@Override
	public void numberChanged(NumberFieldEvent e) {
		if (e.getSource().equals(maxNumberEvaluationsField)) {
			caller.maxNumberEvaluations = Integer.parseInt(maxNumberEvaluationsField.getText());
//		} else if (e.getSource().equals(maxNumberIterationsField)) {
//			caller.maxNumberIterations = Integer.parseInt(maxNumberIterationsField.getText());
		} else if (e.getSource().equals(relativeConvergenceField)) {
			caller.relativeConvergence = Double.parseDouble(relativeConvergenceField.getText());
		} else if (e.getSource().equals(absoluteConvergenceField)) {
			caller.absoluteConvergence = Double.parseDouble(absoluteConvergenceField.getText());
		} else if (e.getSource().equals(numberOfRealizationsForGridSearchField)) {
			caller.numberOfRealizationsForGridSearch = Integer.parseInt(numberOfRealizationsForGridSearchField.getText());
		} else if (e.getSource().equals(numberOfRealizationsForOptimizationField)) {
			caller.numberOfRealizationsForOptimization = Integer.parseInt(numberOfRealizationsForOptimizationField.getText());
		}
	}

	@Override
	public void stateChanged(ChangeEvent evt) {
		if (evt.getSource().equals(gridSizeSlider)) {
			caller.gridDivision = gridSizeSlider.getValue();
			refreshEstimatedNumberGridEval();
		} else if (evt.getSource().equals(numberIterationsSlider)) {
			caller.maxNumberIterations = numberIterationsSlider.getValue();
		}
	}

	@Override
	public void scenarioRecorderJustDidThis (ScenarioRecorderEvent evt) {}

	@Override
	public void optimizerJustDidThis (OptimizerEvent event) {
		if (event.getType() == OptimizerEvent.EventType.PARAMETER_ADDED || event.getType() == OptimizerEvent.EventType.PARAMETER_REMOVED) {
			refreshEstimatedNumberGridEval();
		}
	}


}
