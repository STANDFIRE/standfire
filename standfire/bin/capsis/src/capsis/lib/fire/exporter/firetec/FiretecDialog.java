/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2000-2001 Francois de Coligny
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package capsis.lib.fire.exporter.firetec;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
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
import capsis.lib.fire.FiStand;
import capsis.lib.fire.exporter.Grid;
import capsis.lib.fire.exporter.PhysData;
import capsis.lib.fire.exporter.PhysExporterDialog;
import capsis.lib.fire.fuelitem.FuelItem;

/**
 * Configuration dialog for Firetec
 * 
 * @author F. de Coligny, O. Vigy - october 2007
 */
public class FiretecDialog extends PhysExporterDialog {

	static {
		Translator.addBundle("capsis.lib.fire.exporter.PhysExporter");
	}

	// fc - september 2009 review
	private StringBuffer report;
	private double[] sceneRect; // x, y, sizeX, sizeY
	private double dxv;
	private double dyv;
	private double dzv;
	private int nzv;
	private double aa1v;

	private JTextField dx; // Firetec input physData
	private JTextField dy;
	private JTextField meanDz;
	private JTextField nzTot;
	private JTextField aa1;
	private JTextField exportedZoneOriginX;
	private JTextField exportedZoneOriginY;
	private JTextField exportedZoneLengthX; // From the capsis stand or user
											// defined (FP)
	private JTextField exportedZoneLengthY;
	private JCheckBox allSceneExported; // check box to make the above
										// modifiable

	// Creation of the grid
	private JTextField nx; // Firetec output to check
	private JTextField ny;
	private JTextField zTotal;
	private JList dzk; // a list of heights of the z layers from k=0 to k=nz-1
	private DefaultListModel dzkListModel;

	// update for topo
	private JCheckBox complexTerrain;
	boolean topofileOK = false;
	private JTextField topoFileName;
	private JButton topoFileBrowse;
	private ButtonGroup topoFormatGroup;
	private JRadioButton littleEndianTopo;
	private JRadioButton bigEndianTopo;

	// outputFormat
	private ButtonGroup outputFormatGroup;
	private JRadioButton littleEndianOutput;
	private JRadioButton bigEndianOutput;

	/**
	 * Constructor
	 */
	public FiretecDialog(Firetec firetec) {
		this.exp = firetec; // can be transtyped in Firetec in locally required

		report = new StringBuffer(); // fc - 29.4.2008
		report.append("Starting Old Firetec export...");

		dataWasCalculated = false;
		topofileOK = false;
		setTitle(Translator.swap("FiretecDialog.title"));

		// scene may need enlargement if some crowns exceed on the borders
		sceneRect = new double[4];
		sceneRect[0] = firetec.stand.getOrigin().x;
		sceneRect[1] = firetec.stand.getOrigin().y;
		sceneRect[2] = firetec.stand.getXSize();
		sceneRect[3] = firetec.stand.getYSize();

		createUI(sceneRect);

		statusBar.print(Translator.swap("PhysExporterDialog.ready"));

		setModal(true);
		pack();
		show();		
	}

	// Define the firetec grid with the good sizes
	private void buildGridButtonAction() {
		// Check entries
		//
		if (!Check.isDouble(exportedZoneOriginX.getText().trim())) {
			MessageDialog.print(this, Translator.swap("FiretecDialog.firetecMatrixOriginXMustBeADouble"));
			return;
		}
		if (!Check.isDouble(exportedZoneOriginY.getText().trim())) {
			MessageDialog.print(this, Translator.swap("FiretecDialog.firetecMatrixOriginYMustBeADouble"));
			return;
		}
		if (!Check.isDouble(exportedZoneLengthX.getText().trim())) {
			MessageDialog.print(this, Translator.swap("FiretecDialog.sceneWidthMustBeADouble"));
			return;
		}

		if (!Check.isDouble(exportedZoneLengthY.getText().trim())) {
			MessageDialog.print(this, Translator.swap("FiretecDialog.sceneHeightMustBeADouble"));
			return;
		}
		if (!Check.isDouble(dx.getText().trim())) {
			MessageDialog.print(this, Translator.swap("FiretecDialog.dxMustBeADouble"));
			return;
		}
		if (!Check.isDouble(dy.getText().trim())) {
			MessageDialog.print(this, Translator.swap("FiretecDialog.dyMustBeADouble"));
			return;
		}
		if (!Check.isInt(nzTot.getText().trim())) {
			MessageDialog.print(this, Translator.swap("FiretecDialog.nzMustBeAnInteger"));
			return;
		}
		final double _gridOriginX = Check.doubleValue(exportedZoneOriginX.getText().trim());
		final double _gridOriginY = Check.doubleValue(exportedZoneOriginY.getText().trim());
		double _sceneWidth = Check.doubleValue(exportedZoneLengthX.getText().trim());
		double _sceneHeight = Check.doubleValue(exportedZoneLengthY.getText().trim());

		final double _dx = Check.doubleValue(dx.getText().trim());
		final double _dy = Check.doubleValue(dy.getText().trim());
		final double _dz = Check.doubleValue(meanDz.getText().trim());
		final int _nz = Check.intValue(nzTot.getText().trim());
		final double _aa1 = Check.doubleValue(aa1.getText().trim());

		FiStand stand = exp.stand;
		if (_gridOriginX < stand.getOrigin().x || _gridOriginX > stand.getOrigin().x + stand.getXSize()) {
			MessageDialog.print(this, Translator.swap("FiretecDialog.gridOriginXMustBeInTheScene"));
			return;
		}
		if (_gridOriginY < stand.getOrigin().y || _gridOriginY > stand.getOrigin().y + stand.getYSize()) {
			MessageDialog.print(this, Translator.swap("FiretecDialog.gridOriginYMustBeInTheScene"));
			return;
		}
		if (_sceneWidth < 0) {
			MessageDialog.print(this, Translator.swap("FiretecDialog.sceneWidthMustBeGreaterThanZero"));
			return;
		}
		if (_sceneHeight < 0) {
			MessageDialog.print(this, Translator.swap("FiretecDialog.sceneHeightMustBeGreaterThanZero"));
			return;
		}
		if (_gridOriginX + _sceneWidth > stand.getOrigin().x + stand.getXSize()) {
			MessageDialog.print(this, Translator.swap("FiretecDialog.sceneWidthTooBigToFitTheScene"));
			return;
		}
		if (_gridOriginY + _sceneHeight > stand.getOrigin().y + stand.getYSize()) {
			MessageDialog.print(this, Translator.swap("FiretecDialog.sceneHeightTooBigToFitTheScene"));
			return;
		}
		// update sceneRect
		sceneRect[0] = _gridOriginX;
		sceneRect[1] = _gridOriginY;
		sceneRect[2] = _sceneWidth;
		sceneRect[3] = _sceneHeight;

		if (_dx <= 0.0) {
			MessageDialog.print(this, Translator.swap("FiretecDialog.dxMustBeGreaterThan0"));
			return;
		}

		if (_dy <= 0.0) {
			MessageDialog.print(this, Translator.swap("FiretecDialog.dyMustBeGreaterThan025"));
			return;
		}
		if (_dz <= 0.0) {
			MessageDialog.print(this, Translator.swap("FiretecDialog.dzMustBeGreaterThan025"));
			return;
		}
		if (_aa1 <= 0.0 || _aa1 > 1) {
			MessageDialog.print(this, Translator.swap("FiretecDialog.aa1MustBeBetween0And1"));
			return;
		}

		if (sceneRect[2] % _dx != 0) {
			MessageDialog.print(this, Translator.swap("FiretecDialog.dxMustBeADividerOfSceneWidth"));
			return;
		}
		if (sceneRect[3] % _dy != 0) {
			MessageDialog.print(this, Translator.swap("FiretecDialog.dyMustBeADividerOfSceneHeight"));
			return;
		}
		if (_nz < 1) {
			MessageDialog.print(this, Translator.swap("FiretecDialog.nzMustBeGreaterThan1"));
			return;
		}

		Settings.setProperty("firetec.dialog.last.dx", "" + _dx);
		Settings.setProperty("firetec.dialog.last.dy", "" + _dy);
		Settings.setProperty("firetec.dialog.last.dz", "" + _dz);
		Settings.setProperty("firetec.dialog.last.nz", "" + _nz);
		Settings.setProperty("firetec.dialog.last.aa1", "" + _aa1);

		dxv = _dx;
		dyv = _dy;
		dzv = _dz;
		nzv = _nz;
		aa1v = _aa1;
		this.buildGridInATask();
	}

	/**
	 * abstract buildGrids() call by the task
	 */
	@Override
	protected ArrayList<Grid> buildGrids() throws Exception {
		Grid grid = new Grid(); // grid definition
		grid.buildFiretecGrid(dxv, dyv, dzv, nzv, aa1v, sceneRect[0], sceneRect[1], sceneRect[2], sceneRect[3]);
		ArrayList<Grid> grs = new ArrayList<Grid>();
		grs.add(grid);
		return grs;
	}

	@Override
	protected void afterGridBuilt(ArrayList<Grid> grs) {
		// a single grid for firetec
		Grid gr = grs.get(0);
		nx.setText("" + gr.nx);
		ny.setText("" + gr.ny);

		System.out.println("gr.nz: " + gr.nz);
		dzkListModel.clear();
		// for (int k = 0; k < gr.nz; k++) {
		for (int k = 0; k < gr.nz; k++) { // fc-10.9.2013
			dzkListModel.add(k, gr.coor[0][0][k + 1].z - gr.coor[0][0][k].z);
		}
		zTotal.setText("" + gr.coor[0][0][gr.nz].z);
		addFuelMatrixButton.setEnabled(true);
		complexTerrain.setEnabled(true);
	}

	private void allSceneExportedAction() {
		exportedZoneOriginX.setEditable(!allSceneExported.isSelected());
		exportedZoneOriginY.setEditable(!allSceneExported.isSelected());
		exportedZoneLengthX.setEditable(!allSceneExported.isSelected());
		exportedZoneLengthY.setEditable(!allSceneExported.isSelected());

	}

	private void complexTerrainAction() {
		topoFileBrowse.setEnabled(complexTerrain.isSelected());
		littleEndianTopo.setEnabled(complexTerrain.isSelected());
		bigEndianTopo.setEnabled(complexTerrain.isSelected());
	}

	/**
	 * browse a topofile
	 * 
	 * @throws Exception
	 * 
	 */
	private void topoFileBrowseAction() {
		try {

			JFileChooser chooser = new JFileChooser(System.getProperty("topofile.path"));
			ProjectFileAccessory acc = new ProjectFileAccessory();
			chooser.setAccessory(acc);
			chooser.addPropertyChangeListener(acc);
			int returnVal = chooser.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				Settings.setProperty("topofile.path", chooser.getSelectedFile().toString());
				String fileName = chooser.getSelectedFile().toString(); // ov
																		// 07.08.07
				topoFileName.setText(fileName);
				System.out.println("Topofile name " + fileName);
				float[][] topo = loadTopoFile();
				exp.grids.get(0).updateFiretecGridForTopo(topo);
				topofileOK = true;
				statusBar.print(Translator.swap("FiretecDialog.gridUpdatedForTopo"));
				StatusDispatcher.print(Translator.swap("FiretecDialog.gridUpdatedForTopo"));
			}
			// System.out.println(topoFileName.getText ());
			if (complexTerrain.isSelected() && !topofileOK) {
				MessageDialog.print(this, Translator.swap("FiretecDialog.TheTopoFileIsNotCorrect"));
				return;
			}

		} catch (Exception e) {
			Log.println(Log.ERROR, "FiretecDialog.topoFileBrowseAction ()", "Problem during file loading", e);
			MessageDialog.print(this, Translator.swap("FiretecDialog.topofileNotReadCorrectly"), e);
		}

	}

	private float[][] loadTopoFile() throws Exception {

		((Firetec) exp).topofileName = topoFileName.getText();
		int inx = Check.intValue(nx.getText().trim());
		int iny = Check.intValue(ny.getText().trim());
		String format = ""; // NativeBinaryInputStream.X86=littleEndian
		// NativeBinaryInputStream.SPARC=bigEndian
		if (bigEndianTopo.isSelected()) {
			format = exp.BIGENDIAN;
		}
		if (littleEndianTopo.isSelected()) {
			format = exp.LITTLEENDIAN;
		}
		((Firetec) exp).topoFormat = format;
		FiretecTopoReader reader = new FiretecTopoReader(((Firetec) exp).topofileName, inx, iny, format);
		reader.read();
		return reader.getTopoMatrix();
		
		
	}

	@Override
	protected PhysData buildPhysData() throws Exception {
		// Consider all plants and layers in the stand, get their voxel
		// description ('crown') and put them in the "physData"

		Grid grid = exp.grids.get(0);
		System.out.println("Grid param :" + grid.nx + "," + grid.ny + "," + grid.nz);
		PhysDataOF physData = new PhysDataOF(grid.nx, grid.ny, grid.nz,  grid);
		exp.pdo.produceTreeCrownVoxel = produceTreeCrownVoxel.isSelected();
		exp.physData = physData;
		Collection<FuelItem> fuelItems = exp.stand.getFuelItems();
		for (FuelItem fi : fuelItems) {
			this.exp.exportedBiomass[0] += fi.getFuelMass(exp);// initial biomass to export
			this.exp.exportedBiomass[1] += fi.addFuelTo(exp);// biomass in PhysData
		}
		((PhysDataOF) physData).putDefaultValuesInEmptyCells(grid);this.exp.exportedBiomass[2] = ((PhysDataOF) this.exp.physData).getExportedFuel();
		System.out.println("EXPORTED BIOMASS IN FIRETEC IS:"+this.exp.exportedBiomass[0]+", "+this.exp.exportedBiomass[1]+", "+this.exp.exportedBiomass[2]);
		
		return physData;
	}

	/**
	 * Checks before leaving on Ok.
	 */
	public void okAction() {
		// FiretecMaxtrix not defined
		if (exp.grids.get(0) == null) {
			MessageDialog.print(this, Translator.swap("PhysExporterDialog.youNeedToCreateAGrid"));
			return;
		}

		// FiretecMatrix not calculated
		if (!dataWasCalculated) {
			MessageDialog.print(this, Translator.swap("PhysExporterDialog.pleaseCalculateDataFirst"));
			return;
		}

		buildGridButton.setEnabled(false);
		addFuelMatrixButton.setEnabled(false);
		ok.setEnabled(false);


		// Check the matrix before doing the export
		if (addFuelMatrixVisualControl.isSelected()) {
			// fc - 20.10.2009 - if the scene is too 'loud', the builder may
			// have
			// let those two control maps empty.
			PhysDataOF physData = (PhysDataOF) exp.physData;
			if ((physData.contributionMap == null || physData.contributionMap.isEmpty())
					&& (physData.voxelMatrixMap == null || physData.voxelMatrixMap.isEmpty())) {

				MessageDialog.print(this,
						Translator.swap("FiretecDialog.informationLoudSceneTheControlMapsWereNotCalculated"));

			} else {

				StatusDispatcher.print(Translator.swap("FiretecDialog.showingControl") + "...");
				FiretecControlDialog dlg = new FiretecControlDialog(exp, physData);
				StatusDispatcher.print(Translator.swap("FiretecDialog.done"));

				if (!dlg.isValidDialog()) {
					buildGridButton.setEnabled(true);
					addFuelMatrixButton.setEnabled(true);
					ok.setEnabled(true);
					return;
				}

			}

		}
		
		Settings.setProperty("firetec.dialog.basefilename", exportedFileName.getText().trim());
		((Firetec) exp).baseFileName = exportedFileName.getText().trim();
		if (littleEndianOutput.isSelected()) {
			((Firetec) exp).outputFormat = Firetec.LITTLEENDIAN;
		} else {
			((Firetec) exp).outputFormat = Firetec.BIGENDIAN;
		}
		System.out.println("	Output format=" + ((Firetec) exp).outputFormat);
		
		statusBar.print(Translator.swap("PhysExporterDialog.done"));
		StatusDispatcher.print(Translator.swap("PhysExporterDialog.done"));
		setValidDialog(true);
	}

	/**
	 * ActionListener interface.
	 */
	public void actionPerformed(ActionEvent evt) {
		super.actionPerformed(evt); // fc-6.5.2015 needed for checkboxes

		if (evt.getSource().equals(allSceneExported)) {
			allSceneExportedAction();
		} else if (evt.getSource().equals(buildGridButton)) {
			buildGridButtonAction();
		} else if (evt.getSource().equals(complexTerrain)) {
			complexTerrainAction();
		} else if (evt.getSource().equals(topoFileBrowse)) {
			System.out.println("topoFileBrowse");
			topoFileBrowseAction();
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
	private void createUI(double[] sceneRect) {

		LinePanel main = new LinePanel();

		ColumnPanel col1 = new ColumnPanel();

		// fc-2.2.2015
		// LinePanel l0 = new LinePanel();
		// l0.add(new
		// JWidthLabel(Translator.swap("FiretecDialog.ChooseADirName") + " : ",
		// 200));

		// 1. Calculate mesh
		ColumnPanel part0 = new ColumnPanel(Translator.swap("FiretecDialog.firetecGrid"));
		col1.add(part0);
		col1.addStrut0();

		ColumnPanel part0a = new ColumnPanel(Translator.swap("FiretecDialog.zoneExported"));
		LinePanel l00a = new LinePanel();
		allSceneExported = new JCheckBox(Translator.swap("FiretecDialog.allSceneExported"), true);
		allSceneExported.addActionListener(this);
		l00a.add(allSceneExported);
		part0a.add(l00a);

		LinePanel l00b = new LinePanel();
		l00b.add(new JWidthLabel(Translator.swap("FiretecDialog.firetecMatrixOriginX") + " : ", 200));
		exportedZoneOriginX = new JTextField(5);
		exportedZoneOriginX.setText("" + sceneRect[0]);
		exportedZoneOriginX.setEditable(false);
		exportedZoneOriginX.addActionListener(this);
		l00b.add(exportedZoneOriginX);
		l00b.addStrut0();
		part0a.add(l00b);

		LinePanel l00c = new LinePanel();
		l00c.add(new JWidthLabel(Translator.swap("FiretecDialog.firetecMatrixOriginY") + " : ", 200));
		exportedZoneOriginY = new JTextField(5);
		exportedZoneOriginY.setText("" + sceneRect[1]);
		exportedZoneOriginY.setEditable(false);
		exportedZoneOriginY.addActionListener(this);
		l00c.add(exportedZoneOriginY);
		l00c.addStrut0();
		part0a.add(l00c);

		LinePanel l01 = new LinePanel();
		l01.add(new JWidthLabel(Translator.swap("FiretecDialog.sceneWidth") + " : ", 200));
		exportedZoneLengthX = new JTextField(5);
		exportedZoneLengthX.setText("" + sceneRect[2]);
		exportedZoneLengthX.setEditable(false);
		exportedZoneLengthX.addActionListener(this);
		l01.add(exportedZoneLengthX);
		l01.addStrut0();
		part0a.add(l01);

		LinePanel l02 = new LinePanel();
		l02.add(new JWidthLabel(Translator.swap("FiretecDialog.sceneHeight") + " : ", 200));
		exportedZoneLengthY = new JTextField(5);
		exportedZoneLengthY.setText("" + sceneRect[3]);
		exportedZoneLengthY.setEditable(false);
		exportedZoneLengthY.addActionListener(this);
		l02.add(exportedZoneLengthY);
		l02.addStrut0();
		part0a.add(l02);
		part0.add(part0a);

		ColumnPanel part0b = new ColumnPanel(Translator.swap("FiretecDialog.meshCharacteristics"));
		LinePanel l1 = new LinePanel();
		l1.add(new JWidthLabel(Translator.swap("FiretecDialog.dx") + " : ", 200));
		dx = new JTextField(5);

		String v = Settings.getProperty("firetec.dialog.last.dx", Firetec.DX_DEFAULT);

		dx.setText(v);

		l1.add(dx);
		l1.addStrut0();
		part0b.add(l1);

		LinePanel l2 = new LinePanel();
		l2.add(new JWidthLabel(Translator.swap("FiretecDialog.dy") + " : ", 200));
		dy = new JTextField(5);
		dy.setText(Settings.getProperty("firetec.dialog.last.dy", Firetec.DY_DEFAULT));
		l2.add(dy);
		l2.addStrut0();
		part0b.add(l2);

		LinePanel l3 = new LinePanel();
		l3.add(new JWidthLabel(Translator.swap("FiretecDialog.dz") + " : ", 200));
		meanDz = new JTextField(5);
		meanDz.setText(Settings.getProperty("firetec.dialog.last.dz", Firetec.DZ_DEFAULT));

		l3.add(meanDz);
		l3.addStrut0();
		part0b.add(l3);

		LinePanel l4 = new LinePanel();
		l4.add(new JWidthLabel(Translator.swap("FiretecDialog.nz") + " : ", 200));
		nzTot = new JTextField(5);
		nzTot.setText(Settings.getProperty("firetec.dialog.last.nz", Firetec.NZ_DEFAULT));
		l4.add(nzTot);
		l4.addStrut0();
		part0b.add(l4);

		LinePanel l5 = new LinePanel();
		l5.add(new JWidthLabel(Translator.swap("FiretecDialog.dilatation") + " : ", 200));
		aa1 = new JTextField(5);
		aa1.setText(Settings.getProperty("firetec.dialog.last.dilatation", Firetec.AA1_DEFAULT));
		l5.add(aa1);
		l5.addStrut0();
		part0b.add(l5);

		LinePanel l6 = new LinePanel();
		buildGridButton = new JButton(Translator.swap("FiretecDialog.createButton"));
		buildGridButton.addActionListener(this);
		l6.add(buildGridButton);
		l6.addStrut0();
		part0b.add(l6);

		LinePanel l10 = new LinePanel();
		l10.add(new JWidthLabel(Translator.swap("FiretecDialog.nx") + " : ", 200));
		nx = new JTextField(5);
		nx.setEditable(false);
		l10.add(nx);
		l10.addStrut0();
		part0b.add(l10);

		LinePanel l11 = new LinePanel();
		l11.add(new JWidthLabel(Translator.swap("FiretecDialog.ny") + " : ", 200));
		ny = new JTextField(5);
		ny.setEditable(false);
		l11.add(ny);
		l11.addStrut0();
		part0b.add(l11);

		LinePanel l12 = new LinePanel();
		l12.add(new JWidthLabel(Translator.swap("FiretecDialog.zTotal") + " : ", 200));
		zTotal = new JTextField(5);
		zTotal.setEditable(false);
		l12.add(zTotal);
		l12.addStrut0();
		part0b.add(l12);

		LinePanel l13 = new LinePanel();
		l13.add(new JWidthLabel(Translator.swap("FiretecDialog.dzk") + " : ", 200));
		l13.addGlue();
		part0b.add(l13);

		LinePanel l14 = new LinePanel();
		dzkListModel = new DefaultListModel();
		dzk = new JList(dzkListModel);
		dzk.setVisibleRowCount(4); // fc-6.5.2015
		// ~ dzk.setEditable (false);
		l14.add(new JScrollPane(dzk));
		l14.addStrut0();
		part0b.add(l14);

		part0.add(part0b);

		// 1b. Add topo
		ColumnPanel part1 = new ColumnPanel(Translator.swap("FiretecDialog.addTopo"));
		part0.add(part1);

		LinePanel l1b1 = new LinePanel();
		complexTerrain = new JCheckBox(Translator.swap("FiretecDialog.complexTerrain"), false);
		complexTerrain.setEnabled(false);
		complexTerrain.addActionListener(this);
		l1b1.add(complexTerrain);
		topoFileName = new JTextField(15);
		l1b1.add(topoFileName);
		topoFileBrowse = new JButton(Translator.swap("PhysExporterDialog.browse"));
		l1b1.add(topoFileBrowse);
		topoFileName.setEnabled(false);
		topoFileBrowse.setEnabled(false);
		topoFileBrowse.addActionListener(this);
		l1b1.addStrut0();
		part1.add(l1b1);

		ButtonGroup topoFormatGroup = new ButtonGroup();
		LinePanel l1b2 = new LinePanel();
		l1b2.add(new JWidthLabel(Translator.swap("FiretecDialog.topoFileFormat") + " : ", 200));
		littleEndianTopo = new JRadioButton(Translator.swap("FiretecDialog.littleEndian"), true);
		littleEndianTopo.addActionListener(this);
		topoFormatGroup.add(littleEndianTopo);
		littleEndianTopo.setEnabled(false);
		l1b2.add(littleEndianTopo);
		bigEndianTopo = new JRadioButton(Translator.swap("FiretecDialog.bigEndian"), false);
		bigEndianTopo.addActionListener(this);
		bigEndianTopo.setEnabled(false);
		topoFormatGroup.add(bigEndianTopo);
		l1b2.add(bigEndianTopo);
		l1b2.addStrut0();
		part1.add(l1b2);

		// 2. Insert the vegetation using panel created with method from
		// superclass
		ColumnPanel col2 = new ColumnPanel();
		col2.add(this.getFireExportPanel());

		// Firetec outputs
		ColumnPanel part3 = new ColumnPanel(Translator.swap("FiretecDialog.outputFileNames"));
		col2.add(part3);
		col2.addStrut0();

		LinePanel l20 = new LinePanel();
		l20.add(new JWidthLabel(Translator.swap("FiretecDialog.baseFileName") + " : ", 150));
		exportedFileName = new JTextField(5);
		v = Settings.getProperty("firetec.dialog.basefilename", Firetec.TREES);
		exportedFileName.setText(v);
		l20.add(exportedFileName);
		l20.addStrut0();
		part3.add(l20);

		ButtonGroup outputFormatGroup = new ButtonGroup();
		LinePanel l24 = new LinePanel();
		l24.add(new JWidthLabel(Translator.swap("FiretecDialog.outputFileFormat") + " : ", 200));
		littleEndianOutput = new JRadioButton(Translator.swap("FiretecDialog.littleEndian"), true);
		littleEndianOutput.addActionListener(this);
		outputFormatGroup.add(littleEndianOutput);
		littleEndianOutput.setEnabled(true);
		l24.add(littleEndianOutput);
		bigEndianOutput = new JRadioButton(Translator.swap("FiretecDialog.bigEndian"), false);
		bigEndianOutput.addActionListener(this);
		bigEndianOutput.setEnabled(true);
		outputFormatGroup.add(bigEndianOutput);
		l24.add(bigEndianOutput);
		l24.addStrut0();
		part3.add(l24);

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
