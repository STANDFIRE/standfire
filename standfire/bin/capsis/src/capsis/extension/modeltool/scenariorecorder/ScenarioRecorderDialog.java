/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2014 Mathieu Fortin (AgroParisTech/INRA - UMR LERFoB)
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
package capsis.extension.modeltool.scenariorecorder;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import repicea.app.SettingMemory;
import repicea.gui.AutomatedHelper;
import repicea.gui.REpiceaDialog;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.UIControlManager.CommonMenuTitle;
import repicea.gui.WindowSettings;
import repicea.io.IOUserInterface;
import repicea.io.REpiceaIOFileHandlerUI;
import repicea.net.BrowserCaller;
import repicea.util.REpiceaSystem;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;
import capsis.commongui.projectmanager.ProjectManager;
import capsis.extension.modeltool.scenariorecorder.ScenarioRecorder.InvalidDialogException;
import capsis.extension.modeltool.scenariorecorder.ScenarioRecorderEvent.EventType;
import capsis.gui.MainFrame;
import capsis.kernel.Step;


public class ScenarioRecorderDialog extends REpiceaDialog implements IOUserInterface, ActionListener, ScenarioRecorderListener {

	static {
		UIControlManager.setTitle(ScenarioRecorderDialog.class, "LERFoB Scenario recorder", "Enregistreur de sc\u00E9narios LERFoB");
		try {
			Method callHelp = BrowserCaller.class.getMethod("openUrl", String.class);
			String url = "http://www.inra.fr/capsis/help_"+ 
					REpiceaTranslator.getCurrentLanguage().getLocale().getLanguage() +
					"/capsis/extension/modeltool/scenariorecorder/ScenarioRecorderDialog";
			AutomatedHelper helper = new AutomatedHelper(callHelp, new Object[]{url});
			UIControlManager.setHelpMethod(ScenarioRecorderDialog.class, helper);
		} catch (Exception e) {}
	}
	
	
	protected static enum MessageID implements TextableEnum {

		Scenario("Scenario", "Sc\u00E9nario"),
		StepInformationLabel("Beginning/End", "D\u00E9but/Fin"),
		BeginningStep("Scenario beginning", "Pas de d\u00E9part"),
		EndStep("Scenario end", "Pas de fin"),
		WrongParameters("The evolution parameters seem to be invalid. Please check the values!", "Les param\u00E8tres d'\u00E9volution semblent \u00EAtre incorrects. Veuillez v\u00E9rifier les valeurs!");
		
		
		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}

		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {
			return REpiceaTranslator.getString(this);
		}
	}

	protected JMenuItem quit;
	protected JMenuItem help;
	protected JMenuItem record;
	protected JMenuItem stop;
	protected JMenuItem play;
	protected JMenuItem reset;
	protected JMenu file;
	protected JMenu edit;
	protected JMenu actionMenu;
	
	protected JMenuItem load;
	protected JMenuItem save;
	protected JMenuItem saveAs;
	
	protected final JPanel stepPanel;
	
	private final ScenarioRecorder scenarioRecorder;
	private JList parametersList;
	private DefaultListModel listModel;

	private boolean initialized;
	
	private final WindowSettings settings;
	
	protected ScenarioRecorderDialog(ScenarioRecorder scenarioRecorder, Window parent) {
		super(parent);
		settings = new WindowSettings(REpiceaSystem.getJavaIOTmpDir () + getClass().getSimpleName () + ".ser", this);
		this.askUserBeforeExit = true;
		this.scenarioRecorder = scenarioRecorder;
		stepPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		scenarioRecorder.addScenarioRecorderListener(this);
		init();
	}
	
	protected void init() {
		file = UIControlManager.createCommonMenu(CommonMenuTitle.File);
		actionMenu = new JMenu("Actions");
		edit = UIControlManager.createCommonMenu(CommonMenuTitle.Edit);
		
		quit = UIControlManager.createCommonMenuItem(CommonControlID.Close);
		help = UIControlManager.createCommonMenuItem(CommonControlID.Help);
		
		load = UIControlManager.createCommonMenuItem(CommonControlID.Open);
		save = UIControlManager.createCommonMenuItem(CommonControlID.Save);
		saveAs = UIControlManager.createCommonMenuItem(CommonControlID.SaveAs);
		
		new REpiceaIOFileHandlerUI(this, scenarioRecorder, save, saveAs, load);
		
				
		play = UIControlManager.createCommonMenuItem(CommonControlID.PlayRecord);
		stop = UIControlManager.createCommonMenuItem(CommonControlID.StopRecord);
		record = UIControlManager.createCommonMenuItem(CommonControlID.Record);
		
		reset = UIControlManager.createCommonMenuItem(CommonControlID.Reset);
		
		listModel = new DefaultListModel();
		parametersList = new JList(listModel);
		parametersList.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		MouseAdapter adapter = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (event.getClickCount() >= 2) {
					if (parametersList.getSelectedValue() != null) {
						ParameterDialogWrapper wrapper = (ParameterDialogWrapper) parametersList.getSelectedValue();
						if (wrapper.guiInterface != null) {
							wrapper.showUI();
						}
					}
				}
			}
		};
		parametersList.addMouseListener(adapter);
	}
	
	protected JMenuBar setMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		menuBar.add(file);
		file.add(load);
		file.add(save);
		file.add(saveAs);
		file.add(quit);

		menuBar.add(edit);
		edit.add(reset);
		
		menuBar.add(actionMenu);
		actionMenu.add(stop);
		actionMenu.add(record);
		actionMenu.add(play);

		JMenu about = UIControlManager.createCommonMenu(CommonMenuTitle.About);
		menuBar.add(about);
		help = UIControlManager.createCommonMenuItem(CommonControlID.Help);
		about.add(help);

		stop.setEnabled(false);
		play.setEnabled(false);
		return menuBar;
	}

	protected JPanel createScenarioPanel() {
		
		JPanel listPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		listPanel.add(Box.createHorizontalStrut (10));
		listPanel.add(parametersList);
		listPanel.add(Box.createHorizontalStrut (10));
		listPanel.setName(MessageID.Scenario.toString());
	
		return listPanel;
	}
	
	
	protected void defineStepPanel() {
		stepPanel.removeAll();
		stepPanel.add(new JLabel(MessageID.BeginningStep.toString()));
		if (scenarioRecorder.initialStep != null) {
			stepPanel.add(new JLabel(scenarioRecorder.initialStep.toString()));
		} else {
			stepPanel.add(new JLabel());
		}
		stepPanel.add(new JLabel(MessageID.EndStep.toString()));
		if (scenarioRecorder.lastStepInScenario != null) {
			stepPanel.add(new JLabel(scenarioRecorder.lastStepInScenario.toString()));
		} else {
			stepPanel.add(new JLabel());
		}
		
	}
	
	protected JPanel getStepPanel(GridLayout layout) {
		Border etchedBorder = BorderFactory.createEtchedBorder();
		stepPanel.setLayout(layout);
		stepPanel.setBorder(BorderFactory.createTitledBorder(etchedBorder, MessageID.StepInformationLabel.toString()));
		defineStepPanel();
		return stepPanel;
	}
	
	
	@Override
	protected void initUI() {
		refreshTitle();
		setMenuBar();
		setLayout(new BorderLayout());
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(Box.createVerticalStrut(5));
		
		mainPanel.add(getStepPanel(new GridLayout(1,4)));
		
		mainPanel.add(Box.createVerticalStrut(5));
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		labelPanel.add(UIControlManager.getLabel(MessageID.Scenario));
		mainPanel.add(labelPanel);
		mainPanel.add(Box.createVerticalStrut(5));

		mainPanel.add(createScenarioPanel());
		mainPanel.add(Box.createVerticalStrut(5));

		add(mainPanel, BorderLayout.NORTH);
	}

	protected ScenarioRecorder getScenarioRecorder() {
		return scenarioRecorder;
	}

	@Override
	public void listenTo(){
		quit.addActionListener(this);
		help.addActionListener(this);
		play.addActionListener(this);
		stop.addActionListener(this);
		record.addActionListener(this);
		reset.addActionListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		quit.removeActionListener(this);
		help.removeActionListener(this);
		play.removeActionListener (this);
		stop.removeActionListener(this);
		record.removeActionListener(this);
		reset.removeActionListener(this);
	}

	
	protected void refreshTitle() {
		REpiceaIOFileHandlerUI.RefreshTitle(scenarioRecorder, this);
	}
	
	@Override
	public void refreshInterface() {
		refreshTitle();
		listModel.removeAllElements();
		int i = 0;
		for (ParameterDialogWrapper wrapper : getScenarioRecorder().parameterWrappers) {
			listModel.add(i++, wrapper);
		}
		save.setEnabled(!listModel.isEmpty());
		saveAs.setEnabled(!listModel.isEmpty());
		play.setEnabled(!listModel.isEmpty() && !stop.isEnabled());
		boolean justLoadedButNotPlayedYet = !listModel.isEmpty() && scenarioRecorder.initialStep.equals(scenarioRecorder.lastStepInScenario);
		record.setEnabled(!stop.isEnabled() && !justLoadedButNotPlayedYet);
		defineStepPanel();
		validate();
	}

	@Override
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource().equals(play)) {
			playAction();
		} else if (evt.getSource().equals(stop)) {
			stopAction();
		} else if (evt.getSource().equals(record)) {
			recordAction();
		} else if (evt.getSource().equals(quit)) {
			cancelAction();
		} else if (evt.getSource().equals(help)) {
			helpAction();
		} else if (evt.getSource().equals(reset)) {
			resetAction();
		}
	}

	
	protected void resetAction() {
		closeWrapperWindows(scenarioRecorder.parameterWrappers);
		scenarioRecorder.reset();
		doNotListenToAnymore();
		refreshInterface();
		listenTo();
	}
	
	@Override
	public void cancelAction() {
		stopAction();				// make sure there is no recording before closing
		closeWrapperWindows(scenarioRecorder.parameterWrappers);
		super.cancelAction();
	}
	
	protected void closeWrapperWindows(List<ParameterDialogWrapper> wrappers) {
		for (ParameterDialogWrapper wrapper : wrappers) {
			wrapper.hideInterface();
		}
	}

	protected void stopAction() {
		getScenarioRecorder().stopRecording();
		stop.setEnabled(false);
		record.setEnabled(true);
		file.setEnabled(true);
		edit.setEnabled(true);
		refreshInterface();
	}

	protected void recordAction() {
		file.setEnabled(false);
		edit.setEnabled(false);
		getScenarioRecorder().startRecording();
		stop.setEnabled(true);
		record.setEnabled(false);
		play.setEnabled(false);
		MainFrame.getInstance().requestFocus();
	}

	protected void playAction() {
		try {
			Step currentStep = getScenarioRecorder().playScenario();
			currentStep.setVisible(true);
			currentStep.getProject().updateVisibility(true); 
			ProjectManager.getInstance().update();
		} catch (InvalidDialogException e) {
			JOptionPane.showMessageDialog(this, 
					MessageID.WrongParameters.toString(), 
					UIControlManager.InformationMessageTitle.Error.toString(), 
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
	protected void setMinimumSize() {
		setMinimumSize(new Dimension(500,200));
	}
	
	@Override
	public void setVisible(boolean bool) {
		if (!isVisible()) {
			if (!initialized) {
				initUI();
				setMinimumSize();
				setModal(false);
				setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
				pack();
				initialized = true;
			}
			Dimension size = getSize();
			Container parent = getParent();
			Dimension screenSize;
			Point leftCorner;
			if (parent != null) {
				if (parent instanceof JFrame) {
					screenSize = ((JFrame) parent).getContentPane().getSize();
				} else {
					screenSize = parent.getSize();
				}
				leftCorner = parent.getLocation();
			} else {
				screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				leftCorner = new Point(50,50);
			}
			double x = leftCorner.x + screenSize.getWidth() - size.getWidth();
			double y = leftCorner.y + screenSize.getHeight() - size.getHeight();
			Point point = new Point((int) x, (int) y);
			UIControlManager.setLocation(this, point);
			refreshInterface();
		}
		super.setVisible(bool);
	}
		
	
	@Override
	public void scenarioRecorderJustDidThis(ScenarioRecorderEvent evt) {
		if (evt.getType() == ScenarioRecorderEvent.EventType.PLAY_CALLED || evt.getType() == ScenarioRecorderEvent.EventType.ABOUT_TO_LOAD) {
			closeWrapperWindows(evt.getWrappers());
		} else if (evt.getType() == EventType.PLAY_TERMINATED ||
				evt.getType() == EventType.WRAPPER_ADDED ||
				evt.getType() == EventType.WRAPPER_APPROVED) {
			if (SwingUtilities.isEventDispatchThread()) {
				if (isVisible()) {
					refreshInterface();
				}
			} else {		// if not then send to the event dispatch thread
				Runnable doRun = new Runnable() {
					@Override
					public void run () {
						if (isVisible()) {
							refreshInterface();
						}
					}
				};
				SwingUtilities.invokeLater(doRun);
			}
		} 
	}


	@Override
	public void postLoadingAction() {
		playAction();
		refreshInterface();
	}

	@Override
	public void postSavingAction() {
		refreshTitle();
	}

	@Override
	public SettingMemory getSettingMemory() {return settings;}


}
