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
package capsis.util.extendeddefaulttype;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;
import java.util.concurrent.CancellationException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;

import jeeb.lib.util.Log;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Settings;
import repicea.gui.CommonGuiUtility;
import repicea.gui.CommonGuiUtility.FileChooserOutput;
import repicea.gui.REpiceaShowableUIWithParent;
import repicea.gui.SynchronizedListening;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.Range;
import repicea.io.GFileFilter;
import repicea.io.GFileFilter.FileType;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;
import capsis.commongui.InitialDialog;
import capsis.commongui.util.Helper;
import capsis.util.extendeddefaulttype.ExtInitialParameters.UseMode;


public abstract class ExtInitialDialog<M extends ExtModel> extends InitialDialog implements ActionListener, MouseListener, SynchronizedListening {
	
	private static final long serialVersionUID = 20101109L;

	public static enum MessageID implements TextableEnum {
		UseScriptMode("Use assisted script mode",
				"Utiliser le mode script assist\u00E9"),
		ScriptHelpPopUp("This option makes it possible to perform a series of simulations on a group of strata. The strata are all simulated according to a predefined scenario and the results can be saved in an output .dbf or .csv file.",
				"Cette option permet d'effectuer des simulations en s\u00E9rie sur un groupe de strates. Les strates sont toutes simul\u00E9es selon le m\u00EAme sc\u00E9nario pr\u00E9d\u00E9fini et les r\u00E9sultats peuvent \u00EAtre export\u00E9s dans un fichier de type .dbf ou .csv."),
		ChargingInventoryFile("Load an inventory:", 
				"Chargement d'un inventaire :"),
		InitialYear("Initial simulation year:", "Ann\u00E9e de d\u00E9part de la simulation :"),
		FileNameMustNotBeEmpty("A filename has to be specified.", 
				"Un nom de fichier doit \u00EAtre sp\u00E9cifi\u00E9."),
		IsNotAFile("This file does not exist!", 
				"Ce fichier n'existe pas !"),
		FileIsNotDBForCSV("Only .dbf and .csv files are compatible with this module.", 
				"Seuls les fichiers de type .dbf et .csv sont compatibles avec ce module."),
		InitialSimYearNotBetween1965and2030("The initial year must be an integer between 1965 and 2030.", 
				"L'ann\u00E9e de d\u00E9part de la simulation doit \u00EAtre comprise entre 1965 et 2030."),
		NumberMCITerNotBetween1and1000("The number of Monte Carlo realizations must be an integer between 1 and 1000.", 
				"Le nombre de r\u00E9alisations Monte Carlo doit \u00EAtre un entier entre 1 et 1000."),
		ResidualStandLabel("Residual stand or stratum", 
				"Peuplement ou strate r\u00E9siduelle"),
		NumberOfMonteCarloIter("Number of Monte Carlo realizations:", 
				"Nombre de r\u00E9alisations Monte Carlo :"),
		SimulationMode("Simulation mode:", "M\u00E9thode de simulation :"),
		Stochastic("Stochastic", "Stochastique"),
		Deterministic("Deterministic", "D\u00E9terministe"),
		ProjectName("Project name:", "Nom du projet :"),
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
	
	protected final int leftSpacer = 10;
	protected final int rightSpacer = 10;
	
	
	protected final JTextField fldProjectName;
	
	protected JTextField fldFileName;	// name 
	protected JTextField numberIterationMC;
	protected JLabel numberOfMCIterationsLabel;
	protected JTextField fldInitialSimulationYear;

	private M model;

	protected JButton ok;
	protected JButton cancel;
	protected JButton help;
	protected JButton browse;
	protected JButton useScriptMode;
	
	protected JCheckBox residualStandButton;
	protected JLabel residualStandLabel;
	protected JRadioButton stochasticButton;
	protected JRadioButton deterministicButton;
	

	protected Calendar systemDate;

	protected PopupFactory infoWindowMaker = new PopupFactory();
	protected Popup infoWindow;
	protected JTextArea selectedField;
	protected JPanel mainPanel;
	
	protected ExtInitialDialog(M model) {
		super();

		fldProjectName = new JTextField(25);
		fldProjectName.setText(model.getSettings().getProjectName());
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		selectedField = new JTextArea();
		selectedField.setFont(ExtSimulationSettings.TEXT_FONT);
		selectedField.setLineWrap(true);
		selectedField.setWrapStyleWord(true);
		selectedField.setEditable(false);
		selectedField.setBackground(ExtSimulationSettings.POPUP_COLOR);
		selectedField.setMargin(new Insets(2,2,2,2));

		this.model = model;

		fldFileName = new JTextField(35);
		fldFileName.setHorizontalAlignment(JTextField.RIGHT);
		String modelName = model.getIdCard().getModelName().toLowerCase().trim();
		String defaultInventoryPath = Settings.getProperty("capsis." + modelName + ".defaultInventoryFile", getModel().getSettings().getInventoryPath());
		fldFileName.setText(defaultInventoryPath);

		numberIterationMC = NumberFormatFieldFactory.createNumberFormatField(6, NumberFormatFieldFactory.Type.Integer, Range.StrictlyPositive, false);
		int numberIterMC = Settings.getProperty("capsis." + modelName + ".last.numberIterationMC", 1);
		numberIterationMC.setText(((Integer) numberIterMC).toString());
		numberIterationMC.setHorizontalAlignment(SwingConstants.RIGHT);

		systemDate = new GregorianCalendar();	// local object to set default year to current year
		
		fldInitialSimulationYear = NumberFormatFieldFactory.createNumberFormatField(NumberFormatFieldFactory.Type.Integer, Range.StrictlyPositive, false);
		int initialSimulationYear = Settings.getProperty("capsis." + modelName + ".initialsimulationyear", systemDate.get(Calendar.YEAR));
		fldInitialSimulationYear.setText(((Integer) initialSimulationYear).toString());
		fldInitialSimulationYear.setHorizontalAlignment(SwingConstants.RIGHT);

		ok = UIControlManager.createCommonButton(CommonControlID.Ok);
		cancel = UIControlManager.createCommonButton(CommonControlID.Cancel);
		help = UIControlManager.createCommonButton(CommonControlID.Help);
		browse = UIControlManager.createCommonButton(CommonControlID.Browse);
		useScriptMode = new JButton(MessageID.UseScriptMode.toString());
		
		residualStandButton = new JCheckBox();
		residualStandLabel = UIControlManager.getLabel(MessageID.ResidualStandLabel);

		deterministicButton = new JRadioButton(MessageID.Deterministic.toString());
		stochasticButton = new JRadioButton(MessageID.Stochastic.toString());
		ButtonGroup rg = new ButtonGroup();
		rg.add(deterministicButton);
		rg.add(stochasticButton);
		if (model.getSettings().isStochastic()) {
			stochasticButton.setSelected(true);
		} else {
			deterministicButton.setSelected(true);
		}
		stochasticOrDeterministicModel();
		
		// implementation of a control + T listener to access the GeneralSettingsDialog object
		ActionListener ctrlTListener = new ActionListener () {
			public void actionPerformed (ActionEvent actionEvent) {
				ctrlTPressed();
			}
		};
		
		KeyStroke ctrlTStroke = KeyStroke.getKeyStroke(KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK);
		rootPane.registerKeyboardAction(ctrlTListener, ctrlTStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	/**
	 * This method returns the model from which the dialog comes from.
	 * @return an instance that derives from QuebecMRNFModel
	 */
	public M getModel() {return model;}

	/**
	 * Method called when the browse button is hit.
	 */
	protected void browseAction() {
		try {
			Vector<FileFilter> fileFilters = new Vector<FileFilter>();
			fileFilters.add(GFileFilter.DBF);
			fileFilters.add(GFileFilter.CSV);
			FileChooserOutput fileChooserOutput = CommonGuiUtility.browseAction(this, 
					JFileChooser.FILES_ONLY, 
					fldFileName.getText(),
					fileFilters,
					JFileChooser.OPEN_DIALOG);
			fldFileName.setText(fileChooserOutput.getFilename());
		} catch (Exception e) {
			Log.println(Log.ERROR, "QuebecMRNFInitialDialog.browseAction",
					"Error while opening the file chooser. Consult the log", e);
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Method okAction()
	 * Launch all the processes once ok button is pressed.
	 */
	protected void okAction() {
		hidePopup();
		if (!performFieldChecks())			// if a field is not correctly set then the method returns false
			return;
		try {
			ExtInitialParameters ip = ((ExtModel) getModel()).getSettings();
			setInitialParameters(ip); 

			ip.setUseMode(UseMode.GUI_MODE);
			ip.getRecordReader().setStrataSelectionEnabled(true);
			ip.initImport();
			setEnabled(false);
			ip.buildInitScene(getModel());
		} catch (CancellationException cancelled) {
			return;
		} catch (Exception exc) {
			MessageDialog.print(this, 
					UIControlManager.InformationMessage.ErrorWhileLoadingData.toString(),
					exc);
			setEnabled(true);
			return;
		}
		setValidDialog(true);
	}

	@Override
	public void setValidDialog(boolean valid) {
		hidePopup();
		doNotListenToAnymore();
		super.setValidDialog(valid);
	}
		
	/**
	 * This method performs the regular checks on the field values of the dialog
	 * @return true if everything is ok or false otherwise
	 */
	protected boolean performFieldChecks() {
		
		getModel().getSettings().setProjectName(fldProjectName.getText());
		
		// Checks on the inventory file
		boolean rTextFieldCheck =  performTextFieldChecks(fldFileName);
		if(!rTextFieldCheck){
			return false;
		}

		String fileName = fldFileName.getText();
		getModel().getSettings().setFilename(fileName);
		
		// record the inventory file into a property
		String modelName = model.getIdCard().getModelName().toLowerCase().trim();
		Settings.setProperty("capsis." + modelName + ".defaultInventoryFile", fileName);

		// initial simulation year checks
		int initialSimulationYear = Integer.parseInt(fldInitialSimulationYear.getText());
		
//		if (initialSimulationYear < 1965 || initialSimulationYear > 2030) {
//			JOptionPane.showMessageDialog(this, 
//					MessageID.InitialSimYearNotBetween1965and2030.toString(),
//					UIControlManager.InformationMessageTitle.Error.toString(),
//					JOptionPane.ERROR_MESSAGE);
//			return false;
//		}

		getModel().getSettings().setInitialSimulationYear(initialSimulationYear);
		Settings.setProperty("capsis." + modelName + ".initialsimulationyear", initialSimulationYear);
		
		getModel().getSettings().setStochastic(stochasticButton.isSelected());
		if (stochasticButton.isSelected()) {
			int i;
			try {
				i = Integer.parseInt(numberIterationMC.getText().trim());
				if (i < 1 || i > ExtSimulationSettings.MAX_NUMBER_ITERATIONS_MC) {
					JOptionPane.showMessageDialog(this, 
							MessageID.NumberMCITerNotBetween1and1000.toString(),
							UIControlManager.InformationMessageTitle.Error.toString(),
							JOptionPane.ERROR_MESSAGE);
					return false;
				} 
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, 
						MessageID.NumberMCITerNotBetween1and1000.toString(),
						UIControlManager.InformationMessageTitle.Error.toString(),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}

			// Method setNumberIterationMC requires the setting parameters to be known. Consequently it must come after the ReadInitParameters method
			getModel().getSettings().setNumberOfIterations(i);
			Settings.setProperty("capsis." + modelName + ".last.numberIterationMC", i);
		} else {
			getModel().getSettings().setNumberOfIterations(1);
		}

		return true;
	}

	protected boolean performTextFieldChecks(JTextField pTextField) {
		boolean result = true;
		if (pTextField.getText().length() == 0) {
			JOptionPane.showMessageDialog(this, 
					MessageID.FileNameMustNotBeEmpty.toString(),
					REpiceaTranslator.getString(UIControlManager.InformationMessageTitle.Error),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		if (!new File(pTextField.getText()).isFile()) {
			JOptionPane.showMessageDialog(this, 
					MessageID.IsNotAFile.toString(),
					REpiceaTranslator.getString(UIControlManager.InformationMessageTitle.Error),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (GFileFilter.getFileType(pTextField.getText()) != FileType.DBF && GFileFilter.getFileType(fldFileName.getText()) != FileType.CSV) {
			JOptionPane.showMessageDialog(this, 
					MessageID.FileIsNotDBForCSV.toString(),
					REpiceaTranslator.getString(UIControlManager.InformationMessageTitle.Error),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return result;
	}

	protected void hidePopup() {
		if (infoWindow != null) {
			infoWindow.hide();
		}
	}

	@Override
	public void show() {
		listenTo();
		super.show();
	}
	
	/**
	 * Events management
	 */
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource().equals(browse)) {
			browseAction();
		} else if (evt.getSource ().equals (ok)) {
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		} else if (evt.getSource().equals(useScriptMode)) {
			if (this.infoWindow != null) {
				infoWindow.hide();
			}
			scriptAction();
		} else if (evt.getSource().equals(stochasticButton) || evt.getSource ().equals(deterministicButton)) {
			stochasticOrDeterministicModel();
		} 
	}

	protected void stochasticOrDeterministicModel() {
		numberIterationMC.setEnabled(stochasticButton.isSelected());
	}
	
	
	/**
	 * Method scriptAction()
	 * Launch all the processes once useScriptMode button is pressed.
	 */
	protected void scriptAction() {
		if (!performFieldChecks())			// if a field is not correctly set then the method returns false
			return;
			
		ExtInitialParameters ip = ((ExtModel) getModel()).getSettings();
		ip.setUseMode(UseMode.ASSISTED_SCRIPT_MODE);

		ExtAssistedScriptDialog scriptDlg = new ExtAssistedScriptDialog(this, getModel());
		if (!scriptDlg.isValidDialog()) {
			scriptDlg.dispose();
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (e.getSource().equals(useScriptMode)) {
			selectedField.setText(MessageID.ScriptHelpPopUp.toString());
			showPopup(e);
		}
	}

	/**
	 * This method shows the popup message whose text has been previously defined. 
	 * @param e a MouseEvent in order to locate the message
	 */
	protected void showPopup(MouseEvent e) {
		selectedField.setSize(new Dimension(200,200));
		JPanel pane = new JPanel();
		pane.add(selectedField);
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED, Color.BLACK, Color.GRAY);
		pane.setBorder(border);
		pane.setBackground(ExtSimulationSettings.POPUP_COLOR);
		infoWindow = this.infoWindowMaker.getPopup(this, pane, 
				((MouseEvent) e).getLocationOnScreen().x+20, 
				((MouseEvent) e).getLocationOnScreen().y+10);
		infoWindow.show();
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		hidePopup();
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	public void listenTo() {
		browse.addActionListener(this);
		stochasticButton.addActionListener(this);
		deterministicButton.addActionListener(this);
		ok.addActionListener (this);
		cancel.addActionListener (this);
		help.addActionListener (this);
		useScriptMode.addActionListener(this);
		useScriptMode.addMouseListener(this);
	}

	public void doNotListenToAnymore() {
		browse.removeActionListener(this);
		stochasticButton.removeActionListener(this);
		deterministicButton.removeActionListener(this);
		ok.removeActionListener (this);
		cancel.removeActionListener (this);
		help.removeActionListener (this);
		useScriptMode.removeActionListener(this);
		useScriptMode.removeMouseListener(this);
	}
	
	protected JPanel getControlPanel() {
		// Control panel
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		controlPanel.add (useScriptMode);
		useScriptMode.setFont(ExtSimulationSettings.LABEL_FONT);
		controlPanel.add (ok);
		controlPanel.add (cancel);
		controlPanel.add (help);

		// sets ok as default (see AmapDialog)
		ok.setDefaultCapable (true);
		getRootPane ().setDefaultButton (ok);
		return controlPanel;
	}
	
	
	protected void createUI() {
		
		JPanel projectNamePanel = new JPanel();
		projectNamePanel.setLayout(new BoxLayout(projectNamePanel, BoxLayout.X_AXIS));
		JLabel labelProject = UIControlManager.getLabel(MessageID.ProjectName);
		projectNamePanel.add(Box.createHorizontalStrut(leftSpacer));
		projectNamePanel.add(labelProject);
		projectNamePanel.add(Box.createHorizontalStrut(300));
		projectNamePanel.add(Box.createGlue());
		projectNamePanel.add(fldProjectName);
		projectNamePanel.add(Box.createHorizontalStrut(rightSpacer));
		
		
		// Panel inventory file
		JPanel inventoryPanel = new JPanel();
		inventoryPanel.setLayout(new BoxLayout(inventoryPanel, BoxLayout.X_AXIS));
		JLabel label2 = UIControlManager.getLabel(MessageID.ChargingInventoryFile);
		inventoryPanel.add(Box.createHorizontalStrut(leftSpacer));
		inventoryPanel.add(label2);
		inventoryPanel.add(Box.createHorizontalStrut(50));
		inventoryPanel.add(Box.createGlue());
		inventoryPanel.add(browse);
		inventoryPanel.add(Box.createHorizontalStrut(10));
		inventoryPanel.add(fldFileName);
		inventoryPanel.add(Box.createHorizontalStrut(rightSpacer));
		
		// Panel initial simulation year
		JPanel initialYearPanel = new JPanel();
		initialYearPanel.setLayout(new BoxLayout(initialYearPanel, BoxLayout.X_AXIS));
		JLabel initialYearLabel = UIControlManager.getLabel(MessageID.InitialYear);
		initialYearPanel.add(Box.createHorizontalStrut(leftSpacer));
		initialYearPanel.add (initialYearLabel);
		initialYearPanel.add(Box.createHorizontalStrut(300));
		inventoryPanel.add(Box.createGlue());
		initialYearPanel.add (fldInitialSimulationYear);
		fldInitialSimulationYear.setColumns(10);
		initialYearPanel.add(Box.createHorizontalStrut(rightSpacer));
		
		
		JPanel simulationModePanel = new JPanel();
		simulationModePanel.setLayout(new BoxLayout(simulationModePanel, BoxLayout.X_AXIS));
		JLabel simulationModeLabel = UIControlManager.getLabel(MessageID.SimulationMode);
		simulationModePanel.add(Box.createHorizontalStrut(leftSpacer));
		simulationModePanel.add(simulationModeLabel);
		simulationModePanel.add(Box.createGlue());
		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.Y_AXIS));
		subPanel.add(deterministicButton);
		subPanel.add(stochasticButton);
		simulationModePanel.add(subPanel);
		simulationModePanel.add(Box.createHorizontalStrut(rightSpacer));
		
		// Panel number of Monte Carlo iterations
		JPanel numberOfMCIterationsPanel = new JPanel();
		numberOfMCIterationsPanel.setLayout(new BoxLayout(numberOfMCIterationsPanel, BoxLayout.X_AXIS));
		numberOfMCIterationsLabel = UIControlManager.getLabel(MessageID.NumberOfMonteCarloIter);
		numberOfMCIterationsPanel.add(Box.createHorizontalStrut(leftSpacer));
		numberOfMCIterationsPanel.add(Box.createHorizontalStrut(leftSpacer));
		numberOfMCIterationsPanel.add (numberOfMCIterationsLabel);
		numberOfMCIterationsPanel.add(Box.createHorizontalStrut(300));
		numberOfMCIterationsPanel.add(Box.createGlue());
		numberOfMCIterationsPanel.add (numberIterationMC);
		numberOfMCIterationsPanel.add(Box.createHorizontalStrut(rightSpacer));
		
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(projectNamePanel);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(inventoryPanel);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(initialYearPanel);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(simulationModePanel);
		mainPanel.add(numberOfMCIterationsPanel);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(mainPanel, BorderLayout.NORTH);
		getContentPane().add(getControlPanel(), BorderLayout.SOUTH);

		setTitle(UIControlManager.getTitle(getClass()));
		
		setModal(true);
	}
	
	protected void ctrlTPressed() {
		ExtSimulationSettings generalSettings = getModel().getSettings().getGeneralSettings();
		if (generalSettings instanceof REpiceaShowableUIWithParent) {
			hidePopup();
			((REpiceaShowableUIWithParent) generalSettings).showUI(this);
		}
	}

}
