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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.lang.reflect.Method;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import repicea.app.Logger;
import repicea.gui.AutomatedHelper;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.UIControlManager.CommonMenuTitle;
import repicea.gui.dnd.DnDPanel;
import repicea.io.REpiceaIOFileHandlerUI;
import repicea.net.BrowserCaller;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;
import capsis.extension.modeltool.optimizer.OptimizerTask.TaskID;
import capsis.extension.modeltool.scenariorecorder.ScenarioRecorder;
import capsis.extension.modeltool.scenariorecorder.ScenarioRecorderDialog;
import capsis.gui.MainFrame;

/**
 * The OptimizerToolDialog class is the GUI of OptimizerTool.
 * @author Mathieu Fortin - May 2014
 */
public final class OptimizerToolDialog extends ScenarioRecorderDialog implements OptimizerListener, ItemListener {

	static {
		UIControlManager.setTitle (OptimizerToolDialog.class, "Optimization tool", "Outil d'optimisation");
		try {
			Method callHelp = BrowserCaller.class.getMethod("openUrl", String.class);
			String url = "http://www.inra.fr/capsis/help_"+ 
					REpiceaTranslator.getCurrentLanguage().getLocale().getLanguage() +
					"/capsis/extension/modeltool/optimizer/OptimizerToolDialog";
			AutomatedHelper helper = new AutomatedHelper(callHelp, new Object[]{url});
			UIControlManager.setHelpMethod(OptimizerToolDialog.class, helper);
		} catch (Exception e) {}
	}
	
	protected static enum MessageID implements TextableEnum {
		ScenarioMenuLabel("Scenario", "Sc\u00E9nario"),
		ControlVariables("Control Variables", "Variables de contr\u00F4le"),
		OptimizationDefinition("Optimization Problem", "D\u00E9finition de l'optimisation"),
		OptimizationMenuLabel("Optimization", "Optimisation"),
		LogTab("Log", "Journal"),
		StartOptimization("Start", "D\u00E9marrer"),
		SettingsMenuItem("Settings", "R\u00E9glages"),
		UseGridSearch("Enable grid search", "Activer la grille de recherche"),
		EnableRandomStart("Random start","Point de d\u00E9part al\u00E9atoire"),
		VerboseEnabled("Enabling verbose", "Afficher les messages"),
		CancelledMessage("The optimization has been cancelled.", "L'optimisation a \u00E9t\u00E9 annul\u00E9e."),
		AbortMessage("The optimization has aborted for the following reason: ", "L'optimisation s'est arr\u00EAt\u00E9e pour la raison suivante : "),
		AtLeastOneIndicatorMustBeEnabled("All the indicators are disabled. You must enable at least one indicator.", "Tous les indicateurs sont d\u00E9sactiv\u00E9s. Vous devez en activer au moins un."),
		MoreThanOneIndicatorIsEnabled("Objective functions with more than one indicator might yield inconsistent results." 
										+ System.getProperty("line.separator") 
										+ "Are you sure you want to proceed?",
									  "Une fonction objectif comprenant plus d'un indicateur peut donner des r\u00E9sultats incoh\u00E9rents."
										 + System.getProperty("line.separator") 
										 + "Etes-vous certain de vouloir continuer ?"),
		EconomicParametersOption("Economic parameters", "Param\u00E8tres \u00E9conomiques"),
		IsStochasticEnabledLabel("Stochastic optimization", "Optimisation stochastique"),
		ControlVariableIncorrect("Control variables are not properly set. Please check the bounds!", "Les variables de contr\u00F4le sont incorrectes. Veuillez v\u00E9rifier les bornes!");

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
	
	
	private final OptimizerTool optimizer;
	private final JMenuItem startOptimizationMenuItem;
	private final JMenuItem stopOptimizationMenuItem;
	private final JMenuItem economicParameters;
	private final JMenu scenarioMenu;
	private final JMenu optimizationMenu;
	private JMenu options;
	private final JMenuItem settingsMenuItem;
	private final Logger logger;
	private boolean doingSomething;
	private final JRadioButtonMenuItem enableGridSearchMenuItem;
	private final JCheckBoxMenuItem enableVerboseMenuItem;
	private final JRadioButtonMenuItem enableRandomStartMenuItem;
	private JTabbedPane tabbedPane;
	private JPanel scenarioPanel;
	private final JMenuItem saveOptimizer;
	private final JMenuItem saveAsOptimizer;
	private final JMenuItem loadOptimizer;
	private final JCheckBoxMenuItem isStochasticEnabled;
	private DnDPanel controlVariableManagerPanel;
	private ObjectiveFunctionPanel objectiveFunctionPanel;
	
	protected OptimizerToolDialog(OptimizerTool optimizer, Window parent) {
		super(ScenarioRecorder.getInstance(), parent);
		optimizer.addOptimizerListener(this);
		this.logger = new Logger(4);
		this.optimizer = optimizer;
		scenarioMenu = new JMenu(MessageID.ScenarioMenuLabel.toString());
		startOptimizationMenuItem = UIControlManager.createCommonMenuItem(CommonControlID.PlayRecord);
		startOptimizationMenuItem.setText(MessageID.StartOptimization.toString());
		stopOptimizationMenuItem = UIControlManager.createCommonMenuItem(CommonControlID.StopRecord);
		settingsMenuItem = new JMenuItem(MessageID.SettingsMenuItem.toString());
		enableGridSearchMenuItem = new JRadioButtonMenuItem(MessageID.UseGridSearch.toString());
		enableGridSearchMenuItem.setSelected(this.optimizer.currentSettings.enableGridSearch);
		enableRandomStartMenuItem = new JRadioButtonMenuItem(MessageID.EnableRandomStart.toString());
		enableRandomStartMenuItem.setSelected(this.optimizer.currentSettings.enableRandomStart);
		economicParameters = new JMenuItem(MessageID.EconomicParametersOption.toString());
		optimizationMenu = new JMenu(MessageID.OptimizationMenuLabel.toString());
		saveOptimizer = UIControlManager.createCommonMenuItem(CommonControlID.Save);
		saveAsOptimizer = UIControlManager.createCommonMenuItem(CommonControlID.SaveAs);
		loadOptimizer = UIControlManager.createCommonMenuItem(CommonControlID.Open);
		new REpiceaIOFileHandlerUI(this, optimizer, saveOptimizer, saveAsOptimizer, loadOptimizer);

		isStochasticEnabled = new JCheckBoxMenuItem(MessageID.IsStochasticEnabledLabel.toString());
		isStochasticEnabled.setSelected(optimizer.currentSettings.isStochasticAllowed && optimizer.currentSettings.isStochastic);
		isStochasticEnabled.setEnabled(optimizer.currentSettings.isStochasticAllowed);
		
		ButtonGroup bp = new ButtonGroup();
		bp.add(enableRandomStartMenuItem);
		bp.add(enableGridSearchMenuItem);
		enableVerboseMenuItem = new JCheckBoxMenuItem(MessageID.VerboseEnabled.toString());
		enableVerboseMenuItem.setSelected(this.optimizer.currentSettings.verbose);
		//		enableSimplexMenuItem = new JCheckBoxMenuItem(MessageID.UseSimplex.toString());
	}
	
	@Override
	protected void setMinimumSize() {
		Container parent = getParent();
		if (parent != null) {
			Dimension dim = parent.getSize();
			setPreferredSize(new Dimension(dim.width, (int) (dim.height * .75)));
			scenarioPanel.setPreferredSize(new Dimension((int) (dim.width * .2), (int) (dim.height * .55)));
		} else {
			Dimension preferredSize = this.getPreferredSize();
			scenarioPanel.setMinimumSize(new Dimension((int) (preferredSize.width * .2), (int) (preferredSize.height * .55)));
		}
	}

	@Override
	protected JMenuBar setMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		menuBar.add(file);
		file.add(loadOptimizer);
		file.add(saveOptimizer);
		file.add(saveAsOptimizer);
		file.addSeparator();
		file.add(quit);
		
		menuBar.add(edit);
		edit.add(reset);
		
		menuBar.add(scenarioMenu);
//		scenarioMenu.add(reset);
//		scenarioMenu.addSeparator();
		scenarioMenu.add(load);
		load.setAccelerator(null);
		scenarioMenu.add(save);
		save.setAccelerator(null);
		scenarioMenu.add(saveAs);
		saveAs.setAccelerator(null);
		scenarioMenu.addSeparator();
		scenarioMenu.add(stop);
		scenarioMenu.add(record);
		scenarioMenu.add(play);
		
		menuBar.add(optimizationMenu);
		optimizationMenu.add(startOptimizationMenuItem);
		optimizationMenu.add(stopOptimizationMenuItem);
		optimizationMenu.addSeparator();
		optimizationMenu.add(enableGridSearchMenuItem);
		optimizationMenu.add(enableRandomStartMenuItem);
		optimizationMenu.add(settingsMenuItem);
		optimizationMenu.add(isStochasticEnabled);
		
//		actionMenu.add(optimizeMenuItem);

		options = UIControlManager.createCommonMenu(CommonMenuTitle.Options);
		menuBar.add (options);
		options.add(enableVerboseMenuItem);
		options.add(economicParameters);
		
		JMenu about = UIControlManager.createCommonMenu(CommonMenuTitle.About);
		menuBar.add(about);
		help = UIControlManager.createCommonMenuItem(CommonControlID.Help);
		about.add(help);

		stop.setEnabled(false);
		play.setEnabled(false);
		return menuBar;
	}

	@Override
	protected void resetAction() {
		super.resetAction();
		doNotListenToAnymore();
		optimizer.reset();
		optimizer.controlVariableManager.getUI().refreshInterface();
		optimizer.of.getUI().refreshInterface();
		logger.clear();
		refreshTitle();
		listenTo();
	}
	
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource().equals(startOptimizationMenuItem)) {
			optimizeAction();
		} else if (evt.getSource().equals(stopOptimizationMenuItem) && doingSomething) {
			optimizer.cancelRunningTask();
		} else if (evt.getSource().equals(settingsMenuItem)) {
			optimizer.currentSettings.showUI(this);
		} else if (evt.getSource().equals(economicParameters)) {
			optimizer.of.cbc.showUI(this);
		} else {
			super.actionPerformed(evt);
		}
	}
	
	@Override
	protected void refreshTitle() {
		REpiceaIOFileHandlerUI.RefreshTitle(optimizer, this);
	}

	private void optimizeAction() {
		logger.clear();
		optimizer.of.saveParametersIntoSettings();
		int nbIndicators = optimizer.of.indicators.numberOfContributingIndicators();
		if (nbIndicators == 0) {
			JOptionPane.showMessageDialog(this, 
					MessageID.AtLeastOneIndicatorMustBeEnabled.toString(), 
					UIControlManager.InformationMessageTitle.Error.toString(), 
					JOptionPane.ERROR_MESSAGE);
		} else {
			if (nbIndicators > 1) {
				int result = JOptionPane.showConfirmDialog(this, 
						MessageID.MoreThanOneIndicatorIsEnabled.toString(), 
						UIControlManager.InformationMessageTitle.Warning.toString(), 
						JOptionPane.WARNING_MESSAGE);
				if (result != 0) {
					return;
				}
			}
			tabbedPane.setSelectedComponent(logger.getUI());
			if (optimizer.currentSettings.enableGridSearch) {
				optimizer.addTask(new OptimizerTask(TaskID.GridSearch, optimizer));
			}
			OptimizerTask optimizerTask = new OptimizerTask(TaskID.Optimize, optimizer);
			optimizer.addTask(optimizerTask);
		}
	}
	
	@Override
	protected void recordAction() {
		optimizationMenu.setEnabled(false);
		options.setEnabled(false);
		load.setEnabled(false);
		super.recordAction();
	}

	@Override
	protected void stopAction() {
		load.setEnabled(true);
		optimizationMenu.setEnabled(true);
		options.setEnabled(true);
		super.stopAction();
	}

	

	@Override
	protected void initUI() {
		refreshTitle();
		setMenuBar();
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setDividerLocation(0.3);
		splitPane.setResizeWeight(0.3);
//		splitPane.setOneTouchExpandable(true);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(splitPane, BorderLayout.CENTER);
	
		scenarioPanel = createScenarioPanel();
		tabbedPane = new JTabbedPane();
		
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.add(Box.createVerticalStrut(5));
		leftPanel.add(getStepPanel(new GridLayout(2,2)));
		leftPanel.add(Box.createVerticalStrut(5));
		JPanel subPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		subPanel.add(Box.createHorizontalStrut(2));
		subPanel.add(UIControlManager.getLabel(scenarioPanel.getName()));
		subPanel.add(Box.createHorizontalGlue());
		leftPanel.add(subPanel);
		leftPanel.add(Box.createVerticalStrut(5));
		leftPanel.add(scenarioPanel);
		leftPanel.add(Box.createVerticalStrut(5));
		
		splitPane.setRightComponent(tabbedPane);
		splitPane.setLeftComponent(leftPanel);
		controlVariableManagerPanel = optimizer.controlVariableManager.getUI();
		tabbedPane.addTab(MessageID.ControlVariables.toString(), controlVariableManagerPanel);
		objectiveFunctionPanel = optimizer.of.getUI();
		tabbedPane.addTab(MessageID.OptimizationDefinition.toString(), objectiveFunctionPanel);
		tabbedPane.addTab(MessageID.LogTab.toString(), logger.getUI());
		startOptimizationMenuItem.setEnabled(false);
		stopOptimizationMenuItem.setEnabled(false);
	}

	@Override
	public void listenTo() {
		super.listenTo();
		optimizer.of.scenRecorder.addScenarioRecorderListener(this);
		startOptimizationMenuItem.addActionListener(this);
		stopOptimizationMenuItem.addActionListener(this);
		settingsMenuItem.addActionListener(this);
		enableGridSearchMenuItem.addItemListener(this);
		enableVerboseMenuItem.addItemListener(this);
		enableRandomStartMenuItem.addItemListener(this);
		economicParameters.addActionListener(this);
		saveOptimizer.addActionListener(this);
		saveAsOptimizer.addActionListener(this);
		loadOptimizer.addActionListener(this);
		isStochasticEnabled.addItemListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		super.doNotListenToAnymore();
		optimizer.of.scenRecorder.removeScenarioRecorderListener(this);
		startOptimizationMenuItem.removeActionListener(this);
		stopOptimizationMenuItem.removeActionListener(this);
		settingsMenuItem.removeActionListener(this);
		enableGridSearchMenuItem.removeItemListener(this);
		enableVerboseMenuItem.removeItemListener(this);
		enableRandomStartMenuItem.removeItemListener(this);
		economicParameters.removeActionListener(this);
		saveOptimizer.removeActionListener(this);
		saveAsOptimizer.removeActionListener(this);
		loadOptimizer.removeActionListener(this);
		isStochasticEnabled.removeItemListener(this);
	}

	@Override
	public void optimizerJustDidThis(OptimizerEvent event) {
		if (event.getType() == OptimizerEvent.EventType.PARAMETER_ADDED || event.getType() == OptimizerEvent.EventType.PARAMETER_REMOVED) {
			boolean enabled = !optimizer.controlVariableManager.getList().isEmpty();
			startOptimizationMenuItem.setEnabled(enabled);
		} else if (event.getType() == OptimizerEvent.EventType.EVALUATING && optimizer.currentSettings.verbose) {
			String stringToLog = event.getMessage().toString();
			logThisMessage(stringToLog);
		} else if (event.getType() == OptimizerEvent.EventType.OPTIMIZATION_START || event.getType () == OptimizerEvent.EventType.GRID_START) {
			longJob(true);
			logThisMessage(event.getMessage().toString());
		} else if (event.getType() == OptimizerEvent.EventType.OPTIMIZATION_END || event.getType() == OptimizerEvent.EventType.GRID_END) {
			longJob(false);
			logThisMessage(event.getMessage());
		} else if (event.getType() == OptimizerEvent.EventType.OPTIMIZATION_CANCELLED || event.getType() == OptimizerEvent.EventType.GRID_CANCELLED) {
			longJob(false);
			logThisMessage(event.getMessage().toString());
			JOptionPane.showMessageDialog(this, 
						MessageID.CancelledMessage.toString(), 
						UIControlManager.InformationMessageTitle.Information.toString(), 
						JOptionPane.INFORMATION_MESSAGE);
		} else if (event.getType() == OptimizerEvent.EventType.OPTIMIZATION_ABORTED || event.getType() == OptimizerEvent.EventType.OPTIMIZATION_INVALIDPARAMETERS) {
			longJob(false);
			logThisMessage(event.getMessage().toString());
			if (event.getType() == OptimizerEvent.EventType.OPTIMIZATION_ABORTED) {
				JOptionPane.showMessageDialog(this, 
						MessageID.AbortMessage.toString() + System.getProperty("line.separator") + event.getMessage().toString(), 
						UIControlManager.InformationMessageTitle.Information.toString(), 
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(this, 
						MessageID.ControlVariableIncorrect.toString(), 
						UIControlManager.InformationMessageTitle.Error.toString(), 
						JOptionPane.ERROR_MESSAGE);
			}
		} else if (event.getType() == OptimizerEvent.EventType.BEST_SCENARIO_PLAYED) {
			logThisMessage(event.getMessage().toString());
		} 
	}
	
	private void longJob(boolean justBegan) {
		MainFrame.getInstance().setEnabled(!justBegan);
//		stop.setEnabled(!justBegan);
		edit.setEnabled(!justBegan);
		play.setEnabled(!justBegan);
		record.setEnabled(!justBegan);
		file.setEnabled(!justBegan);
		scenarioMenu.setEnabled(!justBegan);
		options.setEnabled(!justBegan);
		economicParameters.setEnabled(!justBegan);
		startOptimizationMenuItem.setEnabled(!justBegan);
		stopOptimizationMenuItem.setEnabled(justBegan);
		isStochasticEnabled.setEnabled(!justBegan);
		tabbedPane.setEnabled(!justBegan);
		doingSomething = justBegan;
		enableGridSearchMenuItem.setEnabled(!justBegan);
		enableRandomStartMenuItem.setEnabled(!justBegan);
		settingsMenuItem.setEnabled(!justBegan);
	}

	private void logThisMessage(String message) {
		try {
			message = message + System.getProperty("line.separator");
			logger.write(message.getBytes());
			logger.flush();
		} catch (IOException e) {}
	}
	
	@Override
	public void cancelAction() {
		if (!doingSomething) {
			super.cancelAction();
			optimizer.requestShutdown();
		}
	}

	@Override
	public void itemStateChanged (ItemEvent evt) {
		if (evt.getSource().equals(enableGridSearchMenuItem)) {
			optimizer.currentSettings.enableGridSearch = enableGridSearchMenuItem.isSelected();
		} else if (evt.getSource().equals(enableVerboseMenuItem)) {
			optimizer.currentSettings.verbose = enableVerboseMenuItem.isSelected();
		} else if (evt.getSource().equals(enableRandomStartMenuItem)) {
			optimizer.currentSettings.enableRandomStart = enableRandomStartMenuItem.isSelected();
		} else if (evt.getSource().equals(isStochasticEnabled)) {
			optimizer.currentSettings.isStochastic = isStochasticEnabled.isSelected();
		}
	}
	
	
	@Override
	public void refreshInterface() {
		super.refreshInterface();
		objectiveFunctionPanel.refreshInterface();
		controlVariableManagerPanel.refreshInterface();
		optimizer.currentSettings.getUI(this).synchronizeUIWithOwner();
		isStochasticEnabled.setSelected(optimizer.currentSettings.isStochasticAllowed && optimizer.currentSettings.isStochastic);
		isStochasticEnabled.setEnabled(optimizer.currentSettings.isStochasticAllowed && !doingSomething);
		saveOptimizer.setEnabled(save.isEnabled());
		saveAsOptimizer.setEnabled(saveAs.isEnabled());
	}
	

}
