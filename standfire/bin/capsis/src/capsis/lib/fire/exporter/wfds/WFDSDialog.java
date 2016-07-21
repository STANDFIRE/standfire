package capsis.lib.fire.exporter.wfds;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Settings;
import jeeb.lib.util.StatusDispatcher;
import jeeb.lib.util.Translator;
import jeeb.lib.util.gui.NorthPanel;
import jeeb.lib.util.task.StatusBar;
import capsis.commongui.ProjectFileAccessory;
import capsis.commongui.util.Helper;
import capsis.kernel.PathManager;
import capsis.lib.fire.exporter.Grid;
import capsis.lib.fire.exporter.PhysData;
import capsis.lib.fire.exporter.PhysExporterDialog;

public class WFDSDialog extends PhysExporterDialog {

	// private WFDS wfds;
	private WFDSParam p;

	private JTextField firstGridFile;
	private JButton firstGridFileBrowse;
	// private boolean fileBrowsed = false;
	private JTextField gridNumber;

	private JTextField vegetation_cdrag;
	private JTextField vegetation_char_fraction;
	private JTextField emissivity;
	private JCheckBox vegetation_arrhenius_degrad;
	private JTextField fireline_mlr_max;
	private JTextField veg_initial_temperature;
	private JTextField veg_char_fraction;
	private JTextField veg_drag_coefficient;
	private JTextField veg_burning_rate_max;
	private JTextField veg_dehydratation_rate_max;
	private JCheckBox veg_remove_charred;
	private ButtonGroup canopyFuelRepresentation;
	private JRadioButton rectangle;
	private JRadioButton cylinder;
	private JRadioButton hetRectangleText;
	private JRadioButton hetRectangleBin;
	private JTextField bulkDensityAccuracy;
	private JButton loadWFDSParamButton;

	/**
	 * Constructor
	 */
	public WFDSDialog(WFDS wfds) {
		this.exp = wfds;
		// System.out.println("     "+this.exp.model.particleNames);
		this.p = wfds.p;
		setTitle(Translator.swap("WFDSDialog.title"));
		// scene may need enlargement if some crowns exceed on the borders
		createUI();
		statusBar.print(Translator.swap("PhysExporterDialog.ready"));
		setModal(true);
		pack();
		show();
		PhysDataWFDS physData = (PhysDataWFDS) wfds.physData;
	}

	/**
	 * Browse the name of the base grid file to open
	 */
	private void firstGridFileBrowseAction() {
		try {
			// fc-2.2.2015 first time, opens on data/
			JFileChooser chooser = new JFileChooser(Settings.getProperty("wfds.dialog.firstgridfile.path",
					PathManager.getDir("data")));
			// JFileChooser chooser = new
			// JFileChooser(Settings.getProperty("wfds.dialog.firstgridfile.path",
			// ""));

			ProjectFileAccessory acc = new ProjectFileAccessory();
			chooser.setAccessory(acc);
			chooser.addPropertyChangeListener(acc);
			int returnVal = chooser.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				Settings.setProperty("wfds.dialog.firstgridfile.path", chooser.getSelectedFile().toString());
				String fileName = chooser.getSelectedFile().toString();
				firstGridFile.setText(fileName);
//				p.firstGridFile = firstGridFile.getText();
				// fileBrowsed = true;
				Settings.setProperty("wfds.dialog.last.firstGridFile", "" + fileName); // fc-2.2.2015
				// Settings.setProperty("wfds.dialog.last.firstGridFile", "" +
				// firstGridFile);
			}

		} catch (Exception e) {
			Log.println(Log.ERROR, "WFDSDialog.firstGridFileBrowseAction ()", "Problem during file browsing", e);
			MessageDialog.print(this, Translator.swap("WFDSDialog.fileNotReadCorrectly"), e);
		}

	}

	/**
	 * Load the grid file(s) based on firstGridFile and gridNumber
	 */
	private void loadGridButtonAction() {
		try {
			// fc-6.5.2015 the fileName is sometimes remembered by the gui and
			// browse is not needed
			// if (!fileBrowsed) {
			if (firstGridFile.getText().trim().length() == 0) {
				MessageDialog.print(this, Translator.swap("GridFileDialog.youShouldChooseAFirstGridFile"));
				return;
			}
			p.firstGridFile = firstGridFile.getText();
			if (!Check.isInt(gridNumber.getText().trim())) {
				MessageDialog.print(this, Translator.swap("GridFileDialog.gridNumberMustBeAnInteger"));
				return;
			}
			final int _n = Check.intValue(gridNumber.getText().trim());
			if (_n < 1) {
				MessageDialog.print(this, Translator.swap("GridFileDialog.gridNumberMustBeGreaterThan1"));
				return;
			}
			Settings.setProperty("wfds.dialog.last.gridnumber", "" + _n);
			p.gridNumber = _n;
			buildGridInATask();
		} catch (Exception e) {
			Log.println(Log.ERROR, "WFDSDialog.gridFileBrowseAction ()", "Problem during file loading", e);
			MessageDialog.print(this, Translator.swap("WFDSDialog.gridFilesNotReadCorrectly"), e);
		}
	}

	/**
	 * abstract buildGrids() call by the task
	 */
	@Override
	protected ArrayList<Grid> buildGrids() throws Exception {
		return ((WFDS) exp).buildGrids();
	}

	@Override
	protected void afterGridBuilt(ArrayList<Grid> grs) {
		this.loadWFDSParamButton.setEnabled(true);
		return;
	}

	/**
	 * Load WFDS parameters
	 */
	private void loadWFDSParamAction() {
		if (!Check.isDouble(bulkDensityAccuracy.getText().trim())) {
			MessageDialog.print(this, Translator.swap("WFDSDialog.bulkDensityAccuracy"));
			return;
		}
		double _bulkDensityAccuracy = Check.doubleValue(bulkDensityAccuracy.getText().trim());
		if (_bulkDensityAccuracy <= 0.) {
			MessageDialog.print(this, Translator.swap("WFDSDialog.bulkDensityAccuracyMustBeGreaterThan0"));
			return;
		}
		Settings.setProperty("wfds.dialog.last.bulkDensityAccuracy", "" + _bulkDensityAccuracy);

		if (!Check.isDouble(vegetation_cdrag.getText().trim())) {
			MessageDialog.print(this, Translator.swap("WFDSDialog.vegetation_cdrag"));
			return;
		}
		double _vegetation_cdrag = Check.doubleValue(vegetation_cdrag.getText().trim());
		if (_vegetation_cdrag <= 0.) {
			MessageDialog.print(this, Translator.swap("WFDSDialog.vegetation_cdragMustBeGreaterThan0"));
			return;
		}
		Settings.setProperty("wfds.dialog.last.vegetation_cdrag", "" + _vegetation_cdrag);

		if (!Check.isDouble(vegetation_char_fraction.getText().trim())) {
			MessageDialog.print(this, Translator.swap("WFDSDialog.vegetation_char_fraction"));
			return;
		}
		double _vegetation_char_fraction = Check.doubleValue(vegetation_char_fraction.getText().trim());
		if (_vegetation_char_fraction <= 0.) {
			MessageDialog.print(this, Translator.swap("WFDSDialog.vegetation_char_fractionMustBeGreaterThan0"));
			return;
		}
		Settings.setProperty("wfds.dialog.last.vegetation_char_fraction", "" + _vegetation_char_fraction);

		if (!Check.isDouble(emissivity.getText().trim())) {
			MessageDialog.print(this, Translator.swap("WFDSDialog.emissivity"));
			return;
		}
		double _emissivity = Check.doubleValue(emissivity.getText().trim());
		if (_emissivity <= 0.) {
			MessageDialog.print(this, Translator.swap("WFDSDialog.emissivityMustBeGreaterThan0"));
			return;
		}
		Settings.setProperty("wfds.dialog.last.emissivity", "" + _emissivity);
		if (!Check.isDouble(fireline_mlr_max.getText().trim())) {
			MessageDialog.print(this, Translator.swap("WFDSDialog.fireline_mlr_max"));
			return;
		}
		double _fireline_mlr_max = Check.doubleValue(fireline_mlr_max.getText().trim());
		if (_fireline_mlr_max <= 0.) {
			MessageDialog.print(this, Translator.swap("WFDSDialog.fireline_mlr_maxMustBeGreaterThan0"));
			return;
		}
		Settings.setProperty("wfds.dialog.last.fireline_mlr_max", "" + _fireline_mlr_max);

		if (!Check.isDouble(veg_initial_temperature.getText().trim())) {
			MessageDialog.print(this, Translator.swap("WFDSDialog.veg_initial_temperature"));
			return;
		}
		double _veg_initial_temperature = Check.doubleValue(veg_initial_temperature.getText().trim());
		if (_veg_initial_temperature <= 0.) {
			MessageDialog.print(this, Translator.swap("WFDSDialog.veg_initial_temperatureMustBeGreaterThan0"));
			return;
		}
		Settings.setProperty("wfds.dialog.last.veg_initial_temperature", "" + _veg_initial_temperature);

		if (!Check.isDouble(veg_char_fraction.getText().trim())) {
			MessageDialog.print(this, Translator.swap("WFDSDialog.veg_char_fraction"));
			return;
		}
		double _veg_char_fraction = Check.doubleValue(veg_char_fraction.getText().trim());
		if (_veg_char_fraction <= 0.) {
			MessageDialog.print(this, Translator.swap("WFDSDialog.veg_char_fractionMustBeGreaterThan0"));
			return;
		}
		Settings.setProperty("wfds.dialog.last.veg_char_fraction", "" + _veg_char_fraction);

		if (!Check.isDouble(veg_drag_coefficient.getText().trim())) {
			MessageDialog.print(this, Translator.swap("WFDSDialog.veg_drag_coefficient"));
			return;
		}
		double _veg_drag_coefficient = Check.doubleValue(veg_drag_coefficient.getText().trim());
		if (_veg_drag_coefficient <= 0.) {
			MessageDialog.print(this, Translator.swap("WFDSDialog.veg_drag_coefficientMustBeGreaterThan0"));
			return;
		}
		Settings.setProperty("wfds.dialog.last.veg_drag_coefficient", "" + _veg_drag_coefficient);

		if (!Check.isDouble(veg_burning_rate_max.getText().trim())) {
			MessageDialog.print(this, Translator.swap("WFDSDialog.veg_burning_rate_max"));
			return;
		}
		double _veg_burning_rate_max = Check.doubleValue(veg_burning_rate_max.getText().trim());
		if (_veg_burning_rate_max <= 0.) {
			MessageDialog.print(this, Translator.swap("WFDSDialog.veg_burning_rate_maxMustBeGreaterThan0"));
			return;
		}
		Settings.setProperty("wfds.dialog.last.veg_burning_rate_max", "" + _veg_burning_rate_max);

		if (!Check.isDouble(veg_dehydratation_rate_max.getText().trim())) {
			MessageDialog.print(this, Translator.swap("WFDSDialog.veg_dehydratation_rate_max"));
			return;
		}
		double _veg_dehydratation_rate_max = Check.doubleValue(veg_dehydratation_rate_max.getText().trim());
		if (_veg_dehydratation_rate_max <= 0.) {
			MessageDialog.print(this, Translator.swap("WFDSDialog.veg_dehydratation_rate_maxMustBeGreaterThan0"));
			return;
		}
		Settings.setProperty("wfds.dialog.last.veg_dehydratation_rate_max", "" + _veg_dehydratation_rate_max);

		if (rectangle.isSelected()) {
			p.canopyFuelRepresentation = WFDSParam.RECTANGLE;
		}
		if (cylinder.isSelected()) {
			p.canopyFuelRepresentation = WFDSParam.CYLINDER;
		}
		if (hetRectangleText.isSelected()) {
			p.canopyFuelRepresentation = WFDSParam.HET_RECTANGLE_TEXT;
		}
		if (hetRectangleBin.isSelected()) {
			p.canopyFuelRepresentation = WFDSParam.HET_RECTANGLE_BIN;
		}

		p.bulkDensityAccuracy = _bulkDensityAccuracy;
		p.vegetation_cdrag = _vegetation_cdrag;
		p.vegetation_char_fraction = _vegetation_char_fraction;
		p.emissivity = _emissivity;
		p.vegetation_arrhenius_degrad = vegetation_arrhenius_degrad.isSelected();
		p.fireline_mlr_max = _fireline_mlr_max;
		p.veg_initial_temperature = _veg_initial_temperature;
		p.veg_char_fraction = _veg_char_fraction;
		p.veg_drag_coefficient = _veg_drag_coefficient;
		p.veg_burning_rate_max = _veg_burning_rate_max;
		p.veg_dehydratation_rate_max = _veg_dehydratation_rate_max;
		p.veg_remove_charred = veg_remove_charred.isSelected();
		this.addFuelMatrixButton.setEnabled(true);
		this.loadWFDSParamButton.setEnabled(false);
		// System.out.println("fmo and pdo are defined");
		statusBar.print(Translator.swap("WFDSDialog.WFDSParameterLoaded"));
		StatusDispatcher.print(Translator.swap("WFDSDialog.WFDSParameterLoaded"));

	}

	@Override
	protected PhysData buildPhysData() throws Exception {
		System.out.println("Build the PhysDataWFDS");
		((WFDS) exp).buildPhysDataWFDS();
		return exp.physData;
	}

	/**
	 * Checks before leaving on Ok.
	 */
	public void okAction() {
		//
		if (exp.grids.get(0) == null) {
			MessageDialog.print(this, Translator.swap("WFDSDialog.youNeedToCreateAGrid"));
			return;
		}

		if (!dataWasCalculated) {
			MessageDialog.print(this, Translator.swap("PhysExporterDialog.pleaseCalculateDataFirst"));
			return;
		}

		this.buildGridButton.setEnabled(false);
		addFuelMatrixButton.setEnabled(false);
		ok.setEnabled(false);

		Settings.setProperty("wfds.dialog.last.fileName", exportedFileName.getText().trim());
		p.fileName = exportedFileName.getText().trim();
		statusBar.print(Translator.swap("PhysExporterDialog.done"));
		StatusDispatcher.print(Translator.swap("PhysExporterDialog.done"));
		setValidDialog(true);
	}

	public void actionPerformed(ActionEvent evt) {
		super.actionPerformed(evt); // fc-6.5.2015 needed for checkboxes

		if (evt.getSource().equals(firstGridFileBrowse)) {
			firstGridFileBrowseAction();
		} else if (evt.getSource().equals(buildGridButton)) {
			loadGridButtonAction();
		} else if (evt.getSource().equals(loadWFDSParamButton)) {
			loadWFDSParamAction();
		} else if (evt.getSource().equals(addFuelMatrixButton)) {
			loadOptionsAndStartTaskAction();
		} else if (evt.getSource().equals(ok)) {
			okAction();
		} else if (evt.getSource().equals(close)) {
			escapePressed(); // fc-6.5.2015
//			setValidDialog(false);
		} else if (evt.getSource().equals(help)) {
			Helper.helpFor(this);
		}
	}

	/**
	 * Initializes the GUI.
	 */
	private void createUI() {

		LinePanel main = new LinePanel();

		ColumnPanel col1 = new ColumnPanel();
		LinePanel l0 = new LinePanel();
		l0.add(new JWidthLabel(Translator.swap("WFDSDialog.ChooseAFileName") + " : ", 200));

		// 1. Read WFDS grids
		ColumnPanel part0 = new ColumnPanel(Translator.swap("WFDSDialog.WFDSParameters"));
		col1.add(part0);
		col1.addStrut0();

		ColumnPanel part0a = new ColumnPanel(Translator.swap("WFDSDialog.WFDSGrid"));
		LinePanel l0a = new LinePanel();
		l0a.add(new JWidthLabel(Translator.swap("WFDSDialog.gridFileName") + " : ", 200));
		firstGridFile = new JTextField(15);
		firstGridFile.setEnabled(true);
		String v = Settings.getProperty("wfds.dialog.last.firstGridFile", "");
		firstGridFile.setText(v);
		l0a.add(firstGridFile);
		firstGridFileBrowse = new JButton(Translator.swap("PhysExporterDialog.browse"));
		firstGridFileBrowse.setEnabled(true);
		firstGridFileBrowse.addActionListener(this);
		l0a.add(firstGridFileBrowse);
		l0a.addStrut0();
		part0a.add(l0a);
		LinePanel l0b = new LinePanel();
		l0b.add(new JWidthLabel(Translator.swap("WFDSDialog.numberOfGridFile") + " : ", 200));
		gridNumber = new JTextField(5);
		v = Settings.getProperty("wfds.dialog.last.gridnumber", "1");
		gridNumber.setText(v);
		gridNumber.setEditable(true);
		l0b.add(gridNumber);
		l0b.addStrut0();
		part0a.add(l0b);
		LinePanel l0c = new LinePanel();
		buildGridButton = new JButton(Translator.swap("WFDSDialog.loadGrids"));
		buildGridButton.setEnabled(true);
		buildGridButton.addActionListener(this);
		l0c.add(buildGridButton);
		part0a.add(l0c);
		part0.add(part0a);

		ColumnPanel part1 = new ColumnPanel(Translator.swap("WFDSDialog.WFDSParameters"));
		ButtonGroup canopyFuelRepresentation = new ButtonGroup();
		LinePanel l1a = new LinePanel();
		l1a.add(new JWidthLabel(Translator.swap("WFDSDialog.canopyFuelRepresentationFormat") + " : ", 200));
		l1a.addStrut0();
		part1.add(l1a);
		LinePanel l1a0 = new LinePanel();
		rectangle = new JRadioButton(WFDSParam.RECTANGLE, false);
		canopyFuelRepresentation.add(rectangle);
		rectangle.setEnabled(true);
		l1a0.add(rectangle);
		cylinder = new JRadioButton(WFDSParam.CYLINDER, true);
		canopyFuelRepresentation.add(cylinder);
		cylinder.setEnabled(true);
		l1a0.add(cylinder);
		hetRectangleText = new JRadioButton(WFDSParam.HET_RECTANGLE_TEXT, false);
		canopyFuelRepresentation.add(hetRectangleText);
		hetRectangleText.setEnabled(true);
		l1a0.add(hetRectangleText);
		hetRectangleBin = new JRadioButton(WFDSParam.HET_RECTANGLE_BIN, false);
		canopyFuelRepresentation.add(hetRectangleBin);
		hetRectangleBin.setEnabled(true);
		l1a0.add(hetRectangleBin);
		l1a0.addStrut0();
		part1.add(l1a0);
		LinePanel l1b = new LinePanel();
		l1b.add(new JWidthLabel(Translator.swap("WFDSDialog.bulkDensityAccuracy") + " : ", 200));
		bulkDensityAccuracy = new JTextField(5);
		v = Settings.getProperty("wfds.dialog.last.bulkAccuracyDensity", "" + p.bulkDensityAccuracy);
		bulkDensityAccuracy.setEditable(true);
		bulkDensityAccuracy.setText(v);
		l1b.add(bulkDensityAccuracy);
		l1b.addStrut0();
		part1.add(l1b);
		LinePanel l1c = new LinePanel();
		l1c.add(new JWidthLabel("VEGETATION_CDRAG: ", 200));
		vegetation_cdrag = new JTextField(5);
		v = Settings.getProperty("wfds.dialog.last.vegetation_cdrag", "" + p.vegetation_cdrag);
		vegetation_cdrag.setEditable(true);
		vegetation_cdrag.setText(v);
		l1c.add(vegetation_cdrag);
		l1c.addStrut0();
		part1.add(l1c);
		LinePanel l1d = new LinePanel();
		l1d.add(new JWidthLabel("VEGETATION_CHAR_FRACTION: ", 200));
		vegetation_char_fraction = new JTextField(5);
		v = Settings.getProperty("wfds.dialog.last.vegetation_char_fraction", "" + p.vegetation_char_fraction);
		vegetation_char_fraction.setEditable(true);
		vegetation_char_fraction.setText(v);
		l1d.add(vegetation_char_fraction);
		l1d.addStrut0();
		part1.add(l1d);
		LinePanel l1d1 = new LinePanel();
		l1d1.add(new JWidthLabel("EMISSIVITY: ", 200));
		emissivity = new JTextField(5);
		v = Settings.getProperty("wfds.dialog.last.emissivity", "" + p.emissivity);
		emissivity.setEditable(true);
		emissivity.setText(v);
		l1d1.add(emissivity);
		l1d1.addStrut0();
		part1.add(l1d1);
		LinePanel l1e = new LinePanel();
		vegetation_arrhenius_degrad = new JCheckBox("VEGETATION_ARRHENIUS_DEGRAD", p.vegetation_arrhenius_degrad);
		l1e.add(vegetation_arrhenius_degrad);
		l1e.addStrut0();
		part1.add(l1e);
		LinePanel l1f = new LinePanel();
		l1f.add(new JWidthLabel("FIRELINE_MLR_MAX: ", 200));
		fireline_mlr_max = new JTextField(5);
		v = Settings.getProperty("wfds.dialog.last.fireline_mlr_max", "" + p.fireline_mlr_max);
		fireline_mlr_max.setEditable(true);
		fireline_mlr_max.setText(v);
		l1f.add(fireline_mlr_max);
		l1f.addStrut0();
		part1.add(l1f);
		LinePanel l1g = new LinePanel();
		l1g.add(new JWidthLabel("VEG_INITIAL_TEMPERATURE: ", 200));
		veg_initial_temperature = new JTextField(5);
		v = Settings.getProperty("wfds.dialog.last.veg_initial_temperature", "" + p.veg_initial_temperature);
		veg_initial_temperature.setEditable(true);
		veg_initial_temperature.setText(v);
		l1g.add(veg_initial_temperature);
		l1g.addStrut0();
		part1.add(l1g);
		LinePanel l1g1 = new LinePanel();
		l1g1.add(new JWidthLabel("VEG_CHAR_FRACTION: ", 200));
		veg_char_fraction = new JTextField(5);
		v = Settings.getProperty("wfds.dialog.last.veg_char_fraction", "" + p.veg_char_fraction);
		veg_char_fraction.setEditable(true);
		veg_char_fraction.setText(v);
		l1g1.add(veg_char_fraction);
		l1g1.addStrut0();
		part1.add(l1g1);

		LinePanel l1h = new LinePanel();
		l1h.add(new JWidthLabel("VEG_DRAG_COEFFICIENT: ", 200));
		veg_drag_coefficient = new JTextField(5);
		v = Settings.getProperty("wfds.dialog.last.veg_drag_coefficient", "" + p.veg_drag_coefficient);
		veg_drag_coefficient.setEditable(true);
		veg_drag_coefficient.setText(v);
		l1h.add(veg_drag_coefficient);
		l1h.addStrut0();
		part1.add(l1h);
		LinePanel l1i = new LinePanel();
		l1i.add(new JWidthLabel("VEG_BURNING_RATE_MAX: ", 200));
		veg_burning_rate_max = new JTextField(5);
		v = Settings.getProperty("wfds.dialog.last.veg_burning_rate_max", "" + p.veg_burning_rate_max);
		veg_burning_rate_max.setEditable(true);
		veg_burning_rate_max.setText(v);
		l1i.add(veg_burning_rate_max);
		l1i.addStrut0();
		part1.add(l1i);
		LinePanel l1j = new LinePanel();
		l1j.add(new JWidthLabel("VEG_DEHYDRATATION_RATE_MAX: ", 200));
		veg_dehydratation_rate_max = new JTextField(5);
		v = Settings.getProperty("wfds.dialog.last.veg_dehydratation_rate_max", "" + p.veg_dehydratation_rate_max);
		veg_dehydratation_rate_max.setEditable(true);
		veg_dehydratation_rate_max.setText(v);
		l1j.add(veg_dehydratation_rate_max);
		l1j.addStrut0();
		part1.add(l1j);
		LinePanel l1k = new LinePanel();
		veg_remove_charred = new JCheckBox("VEG_REMOVE_CHARRED", p.veg_remove_charred);
		l1k.add(veg_remove_charred);
		l1k.addStrut0();
		part1.add(l1k);

		LinePanel l1z = new LinePanel();
		loadWFDSParamButton = new JButton(Translator.swap("WFDSDialog.loadWFDSParam"));
		loadWFDSParamButton.setEnabled(false);
		loadWFDSParamButton.addActionListener(this);
		l1z.add(loadWFDSParamButton);
		part1.add(l1z);
		part0.add(part1);

		// 2. Insert the vegetation
		ColumnPanel col2 = new ColumnPanel();
		col2.add(this.getFireExportPanel());

		// WFDS outputs
		ColumnPanel part3 = new ColumnPanel(Translator.swap("WFDS.outputFileNames"));
		col2.add(part3);
		col2.addStrut0();

		LinePanel l3b = new LinePanel();
		l3b.add(new JWidthLabel(Translator.swap("WFDSDialog.fileName") + " : ", 200));
		exportedFileName = new JTextField(5);
		v = Settings.getProperty("wfds.dialog.last.fileName", p.fileName);
		exportedFileName.setEditable(true);
		exportedFileName.setText(v);
		l3b.add(exportedFileName);
		l3b.addStrut0();
		part3.add(l3b);
		col2.add(part3);

		main.add(new NorthPanel(col1));
		main.add(new NorthPanel(col2));
		main.addStrut0();

		// Status bar at the bottom
		LinePanel l99 = new LinePanel();
		statusBar = new StatusBar();
		l99.add(statusBar);
		l99.addStrut0();
		// main.add (statusBar);

		// 2. Control panel (ok close help);
		JPanel pControl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		// checkOnOk = new JCheckBox (Translator.swap
		// ("FiretecDialog.checkOnOk"), false);
		// pControl.add (checkOnOk);

		ok = new JButton(Translator.swap("PhysExporter.ok"));
		close = new JButton(Translator.swap("Shared.close"));
		help = new JButton(Translator.swap("Shared.help"));

		pControl.add(ok);
		pControl.add(close);
		pControl.add(help);
		ok.addActionListener(this);
		close.addActionListener(this);
		help.addActionListener(this);

		ok.setEnabled(false); // will be enabled later

		// sets ok as default (see AmapDialog)
		// ~ ok.setDefaultCapable (true);
		// ~ getRootPane ().setDefaultButton (ok);

		JPanel aux = new JPanel(new BorderLayout());
		aux.add(main, BorderLayout.CENTER);
		aux.add(l99, BorderLayout.SOUTH);

		// ~ getContentPane ().add (new JScrollPane (aux), BorderLayout.CENTER);
		getContentPane().add(aux, BorderLayout.CENTER);
		getContentPane().add(pControl, BorderLayout.SOUTH);

	}
}
