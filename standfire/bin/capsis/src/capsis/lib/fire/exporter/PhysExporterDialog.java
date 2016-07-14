package capsis.lib.fire.exporter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Question;
import jeeb.lib.util.Settings;
import jeeb.lib.util.StatusDispatcher;
import jeeb.lib.util.Translator;
import jeeb.lib.util.task.StatusBar;
import jeeb.lib.util.task.Task;
import jeeb.lib.util.task.TaskManager;
import capsis.gui.MainFrame;
import capsis.util.DirectoryExport;

public abstract class PhysExporterDialog extends AmapDialog implements DirectoryExport, ActionListener {

	static {
		Translator.addBundle("capsis.lib.fire.exporter.PhysExporter");
	}

	// buildGridButton
	protected JButton buildGridButton;

	// fuel matrix options
	protected JTextField fiPlantDiscretization;
	protected JTextField fiLayerSetHorizontalDistributionDx;
	protected JTextField horizontalDistribVoxelNumberMaximum;
	protected JTextField fiLayerSetVerticalDiscretization;
	protected JTextField fiLayerSetMinDz;
	protected JCheckBox verbose;

	// TODO: François de C
	// creer une JList de cases à cocher où un truc équivalent : Les différentes
	// checkbox auront pour nom les FiModel.particleNames (Set<String>)
	// l'utilisateur en sélectionne un certain nombre pour définir les
	// exp.fmo.particleNames (Set<String>)

	protected List<JCheckBox> particleNames; // fc-6.5.2015
	protected JButton checkAll; // fc-6.5.2015
	protected JButton uncheckAll; // fc-6.5.2015
	protected JScrollPane scroll;

	// physical data options
	// Control maps
	// private SetMap contributionMap;
	// private Map<String,FuelMatrix> voxelMatrixMap; // key = voxelMatrix
	// source
	protected JCheckBox addFuelMatrixVisualControl;
	protected JCheckBox overlappingPermitted;
	protected JCheckBox produceTreeCrownVoxel;
	// produce a list of voxels associated to a given tree on physexporter mesh

	protected StatusBar statusBar;
	protected boolean dataWasCalculated;
	protected JButton addFuelMatrixButton;

	//
	protected JTextField exportedFileName;

	protected JButton ok;
	protected JButton close;
	protected JButton help;

	protected PhysExporter exp;

	/**
	 * Build or load the Grid in a Task
	 */
	protected void buildGridInATask() {

		System.out.println("PhysExporterDialog.buildGridInATask ()...");

		Task<ArrayList<Grid>, Void> task1 = new Task<ArrayList<Grid>, Void>(
				Translator.swap("PhysExporterDialog.buildingGrid")) {

			public void doFirstInEDT() {

				System.out.println("PhysExporterDialog.doFirstInEDT ()...");
				dataWasCalculated = false;
				buildGridButton.setEnabled(true);
				addFuelMatrixButton.setEnabled(false);
				ok.setEnabled(false);
				statusBar.print(Translator.swap("PhysExporterDialog.buildingGrid"));
				StatusDispatcher.print(Translator.swap("PhysExporterDialog.buildingGrid"));
			}

			public ArrayList<Grid> doInWorker() {

				System.out.println("PhysExporterDialog.doInWorker ()...");

				try {
					return buildGrids();
				} catch (Throwable t) {
					Log.println(Log.ERROR, "PhysExporterDialog.buildGridInATask ()",
							"Error while creating the grid (doInWorker)", t);
					return null;
				}
			}

			public void doInEDTafterWorker() {

				System.out.println("PhysExporterDialog.doInEDTafterWorker ()...");

				try {
					ArrayList<Grid> grs = get();
					exp.grids.addAll(grs);
					if (grs == null) {
						throw new Exception();
					}
					afterGridBuilt(grs);
					// grid is done once without problem
					buildGridButton.setEnabled(false);

				} catch (Throwable t) {
					Log.println(Log.ERROR, "PhysExporterDialog.buildGrid ()",
							"Error while creating the grid (doInEDTafterWorker)", t);
					MessageDialog.print(PhysExporterDialog.this,
							Translator.swap("PhysExporterDialog.anErrorOccuredWhileCreatingtheGrid"), t);
				}
				statusBar.print(Translator.swap("PhysExporterDialog.gridBuilt"));
				StatusDispatcher.print(Translator.swap("PhysExporterDialog.gridBuilt"));
			}

		};

		task1.setIndeterminate();
		TaskManager.getInstance().add(task1);
	}

	abstract protected ArrayList<Grid> buildGrids() throws Exception;

	protected void afterGridBuilt(ArrayList<Grid> grs) {
		return;
	}

	/**
	 * Load the Fuel Matrix Option and PhysDataOption and call the thread that
	 * build FuelMatrix of Fuel items and add them to the PhysData.
	 */
	protected void loadOptionsAndStartTaskAction() {
		// Check entries
		if (!Check.isDouble(fiPlantDiscretization.getText().trim())) {
			MessageDialog.print(this, Translator.swap("PhysExporterDialog.fiPlantDiscretizationMustBeADouble"));
			return;
		}
		double _fiPlantDiscretization = Check.doubleValue(fiPlantDiscretization.getText().trim());
		if (_fiPlantDiscretization <= 0.) {
			MessageDialog.print(this, Translator.swap("PhysExporterDialog.fiPlantDiscretizationMustBeGreaterThan0"));
			return;
		}
		if (_fiPlantDiscretization > 1.) {
			MessageDialog.print(this, Translator.swap("PhysExporterDialog.fiPlantDiscretizationMustBeLowerThan1"));
			return;
		}
		Settings.setProperty("physexporter.dialog.last.fiPlantDiscretization", "" + _fiPlantDiscretization);

		if (!Check.isDouble(fiLayerSetHorizontalDistributionDx.getText().trim())) {
			MessageDialog.print(this,
					Translator.swap("PhysExporterDialog.fiLayerSetHorizontalDistributionDxMustBeADouble"));
			return;
		}
		double _fiLayerSetHorizontalDistributionDx = Check.doubleValue(fiLayerSetHorizontalDistributionDx.getText()
				.trim());
		if (_fiLayerSetHorizontalDistributionDx <= 0.) {
			MessageDialog.print(this,
					Translator.swap("PhysExporterDialog.fiLayerSetHorizontalDistributionDxMustBeGreaterThan0"));
			return;
		}
		Settings.setProperty("physexporter.dialog.last.fiLayerSetHorizontalDistributionDx", ""
				+ _fiLayerSetHorizontalDistributionDx);

		if (!Check.isDouble(fiLayerSetMinDz.getText().trim())) {
			MessageDialog.print(this, Translator.swap("PhysExporterDialog.fiLayerSetMinDzMustBeADouble"));
			return;
		}
		double _fiLayerSetMinDz = Check.doubleValue(fiLayerSetMinDz.getText().trim());
		if (_fiLayerSetMinDz <= 0.) {
			MessageDialog.print(this, Translator.swap("PhysExporterDialog.fiLayerSetMinDzMustBeGreaterThan0"));
			return;
		}
		Settings.setProperty("physexporter.dialog.last.fiLayerSetMinDz", "" + _fiLayerSetMinDz);

		if (!Check.isDouble(fiLayerSetVerticalDiscretization.getText().trim())) {
			MessageDialog.print(this,
					Translator.swap("PhysExporterDialog.fiLayerSetVerticalDiscretizationMustBeADouble"));
			return;
		}
		double _fiLayerSetVerticalDiscretization = Check.doubleValue(fiLayerSetVerticalDiscretization.getText().trim());
		if (_fiLayerSetVerticalDiscretization <= 0.) {
			MessageDialog.print(this,
					Translator.swap("PhysExporterDialog.fiLayerSetVerticalDiscretizationMustBeGreaterThan0"));
			return;
		}
		if (_fiLayerSetVerticalDiscretization > 1.) {
			MessageDialog.print(this,
					Translator.swap("PhysExporterDialog.fiLayerSetVerticalDiscretizationMustBeLowerThan1"));
			return;
		}
		Settings.setProperty("physexporter.dialog.last.fiLayerSetVerticalDiscretization", ""
				+ _fiLayerSetVerticalDiscretization);

		if (!Check.isDouble(horizontalDistribVoxelNumberMaximum.getText().trim())) {
			MessageDialog.print(this,
					Translator.swap("PhysExporterDialog.horizontalDistribVoxelNumberMaximumMustBeADouble"));
			return;
		}
		double _horizontalDistribVoxelNumberMaximum = Check.doubleValue(horizontalDistribVoxelNumberMaximum.getText()
				.trim());
		if (_horizontalDistribVoxelNumberMaximum <= 0.) {
			MessageDialog.print(this,
					Translator.swap("PhysExporterDialog.horizontalDistribVoxelNumberMaximumMustBeGreaterThan0"));
			return;
		}
		Settings.setProperty("physexporter.dialog.last.horizontalDistribVoxelNumberMaximum", ""
				+ _horizontalDistribVoxelNumberMaximum);

		// Check at least one particle name checked // fc-6.5.2015
		int n = 0;
		for (JCheckBox b : particleNames) {
			if (b.isSelected())
				n++;
		}
		if (n == 0) {
			MessageDialog.print(this,
					Translator.swap("PhysExporterDialog.pleaseSelectAtLeastOneParticleNameInTheList"));
			return;
		}
		
		
		// If matrix was already fed, overwrite ?
		if (dataWasCalculated) {
			if (!Question.ask(MainFrame.getInstance(), Translator.swap("PhysExporterDialog.confirm"),
					Translator.swap("PhysExporterDialog.confirmMatrixRecalculation"))) {
				return;
			}
		}

		// DEFINTION OF OPTIONS
		exp.fmo.fiPlantDiscretization = _fiPlantDiscretization;
		exp.fmo.fiLayerSetHorizontalDistributionDx = _fiLayerSetHorizontalDistributionDx;
		exp.fmo.fiLayerSetMinDz = _fiLayerSetMinDz;
		exp.fmo.fiLayerSetVerticalDiscretization = _fiLayerSetVerticalDiscretization;
		exp.fmo.horizontalDistribVoxelNumberMaximum = _horizontalDistribVoxelNumberMaximum;

		// fc-6.5.2015 Restrict to the checked particle names
		Set<String> set = new HashSet<>();
		for (JCheckBox b : particleNames) {
			if (b.isSelected())
				set.add(b.getText());
		}
		exp.fmo.particleNames = set; // fc-6.5.2015
		// exp.fmo.particleNames = exp.modelParticleNames; // temporary

		System.out.println("	Exported particles = " + exp.fmo.particleNames);
		exp.fmo.verbose = verbose.isSelected();
		exp.pdo.overlappingPermitted = overlappingPermitted.isSelected();
		exp.pdo.produceTreeCrownVoxel = produceTreeCrownVoxel.isSelected();
		exp.pdo.verbose = verbose.isSelected();
		System.out.println("fmo and pdo are defined");
		buildPhysDataTask();
	}

	/**
	 * Build PhysDataData in a Task, i.e. insert the vegetation in it.
	 */
	protected void buildPhysDataTask() {

		Task<PhysData, Void> task2 = new Task<PhysData, Void>(Translator.swap("PhysExporterDialog.exportingFuelItems")) {

			public void doFirstInEDT() {
				buildGridButton.setEnabled(false);
				addFuelMatrixButton.setEnabled(false);
				ok.setEnabled(false);
				StatusDispatcher.print(Translator.swap("PhysExporterDialog.exportingFuelItems"));
				statusBar.print(Translator.swap("PhysExporterDialog.exportingFuelItems"));
			}

			public PhysData doInWorker() {
				try {
					return buildPhysData();
				} catch (Throwable t) {
					Log.println(Log.ERROR, "PhysExporterDialog.buildPhysDataTask ()",
							"Error while building  the PhysData", t);
					return null;
				}
			}

			public void doInEDTafterWorker() {
				try {
					exp.physData = get();
					// Get the feeder's control maps
					dataWasCalculated = true;

				} catch (Throwable t) {
					Log.println(Log.ERROR, "PhysDataDialog.buildPhysData ()",
							"Error while feeding PhysData (doInEDTafterWorker)", t);
					MessageDialog.print(PhysExporterDialog.this,
							Translator.swap("PhysDataDialog.couldNotBuildPhysDataSeeLog"), t);
				}
				StatusDispatcher.print(Translator.swap("PhysExporterDialog.fuelItemExported"));
				statusBar.print(Translator.swap("PhysExporterDialog.fuelItemExported"));

				buildGridButton.setEnabled(false);
				//addFuelMatrixButton.setEnabled(false);
				ok.setEnabled(true);
			}

		};

		task2.setIndeterminate();
		TaskManager.getInstance().add(task2);

	}

	abstract protected PhysData buildPhysData() throws Exception;

	/**
	 * Called on escape : ask for confirmation.
	 */
	protected void escapePressed() {
		if (Question.ask(MainFrame.getInstance(), Translator.swap("PhysExporterDialog.confirm"),
				Translator.swap("PhysExporterDialog.confirmClose"))) {
			dispose();
		}
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		// fc-6.5.2015
		if (evt.getSource().equals(checkAll)) {
			selectAllParticles(true);
		} else if (evt.getSource().equals(uncheckAll)) {
			selectAllParticles(false);
		}

	}

	/**
	 * Select / unselects all particles
	 */
	private void selectAllParticles(boolean yep) { // fc-6.5.2015
		// If yep, select all, if !yep, deselect all
		for (JCheckBox b : particleNames) {
			b.setSelected(yep);
		}
		scroll.repaint();
	}

	protected ColumnPanel getFireExportPanel() {

		// 2. Insert the vegetation
		ColumnPanel part2 = new ColumnPanel(Translator.swap("PhysExporterDialog.ExporterParameters"));

		LinePanel l28 = new LinePanel();
		l28.add(new JWidthLabel(Translator.swap("PhysExporterDialog.fiPlantDiscretization") + " : ", 200));
		fiPlantDiscretization = new JTextField(5);
		String v = Settings.getProperty("physexporter.dialog.last.fiPlantDiscretization", 0.1 + "");
		fiPlantDiscretization.setText(v);
		l28.add(fiPlantDiscretization);
		l28.addStrut0();
		part2.add(l28);
		LinePanel l28a = new LinePanel();
		l28a.add(new JWidthLabel(Translator.swap("PhysExporterDialog.fiLayerSetHorizontalDiscretizationDx") + " : ",
				200));
		fiLayerSetHorizontalDistributionDx = new JTextField(5);
		v = Settings.getProperty("physexporter.dialog.last.fiLayerSetHorizontalDistributionDx", 0.5 + "");
		fiLayerSetHorizontalDistributionDx.setText(v);
		l28a.add(fiLayerSetHorizontalDistributionDx);
		l28a.addStrut0();
		part2.add(l28a);
		LinePanel l28b = new LinePanel();
		l28b.add(new JWidthLabel(Translator.swap("PhysExporterDialog.fiLayerSetMinDz") + " : ", 200));
		fiLayerSetMinDz = new JTextField(5);
		v = Settings.getProperty("physexporter.dialog.last.fiLayerSetMinDz", 0.2 + "");
		fiLayerSetMinDz.setText(v);
		l28b.add(fiLayerSetMinDz);
		l28b.addStrut0();
		part2.add(l28b);
		LinePanel l28c = new LinePanel();
		l28c.add(new JWidthLabel(Translator.swap("PhysExporterDialog.fiLayerSetVerticalDiscretization") + " : ", 200));
		fiLayerSetVerticalDiscretization = new JTextField(5);
		v = Settings.getProperty("physexporter.dialog.last.fiLayerSetVerticalDiscretization", 0.2 + "");
		fiLayerSetVerticalDiscretization.setText(v);
		l28c.add(fiLayerSetVerticalDiscretization);
		l28c.addStrut0();
		part2.add(l28c);
		LinePanel l28d = new LinePanel();
		l28d.add(new JWidthLabel(Translator.swap("PhysExporterDialog.horizontalDistribVoxelNumberMaximum") + " : ", 200));
		horizontalDistribVoxelNumberMaximum = new JTextField(5);
		v = Settings.getProperty("physexporter.dialog.last.horizontalDistribVoxelNumberMaximum", 2000000 + "");
		horizontalDistribVoxelNumberMaximum.setText(v);
		l28d.add(horizontalDistribVoxelNumberMaximum);
		l28d.addStrut0();
		part2.add(l28d);

		LinePanel l29 = new LinePanel();
		overlappingPermitted = new JCheckBox(Translator.swap("PhysExporterDialog.overlappingPermitted"), false);
		l29.add(overlappingPermitted);
		l29.addStrut0();
		part2.add(l29);

		LinePanel l30 = new LinePanel();
		produceTreeCrownVoxel = new JCheckBox(Translator.swap("PhysExporterDialog.produceTreeCrownVoxel"));
		produceTreeCrownVoxel.setEnabled(false);
		l30.add(produceTreeCrownVoxel);
		l30.addStrut0();
		part2.add(l30);
		LinePanel l36 = new LinePanel();
		verbose = new JCheckBox(Translator.swap("PhysExporterDialog.verbose"), true);
		l36.add(verbose);
		// control permitted
		addFuelMatrixVisualControl = new JCheckBox(Translator.swap("PhysExporterDialog.VisualControl"), false);
		addFuelMatrixVisualControl.setEnabled(false);
		l36.add(addFuelMatrixVisualControl);
		l36.addStrut0();
		part2.add(l36);

		// fc-5.6.2015
		// User can choose particles to be exported
		ColumnPanel c1 = new ColumnPanel(Translator.swap("PhysExporterDialog.particles"));
		part2.add(c1);

		LinePanel l0 = new LinePanel();
		l0.addGlue();
		l0.add(new JLabel(Translator.swap("PhysExporterDialog.checkTheParticlesToBeExported")));
		l0.addGlue();
		c1.add(l0);

		// A list of checkbox
		particleNames = new ArrayList<JCheckBox>();

		ColumnPanel p = new ColumnPanel();

		List<String> l = new ArrayList<String>(exp.modelParticleNames); // copy
		Collections.sort(l); // sorted for user

		for (String n : l) {
			JCheckBox b = new JCheckBox(n);
			particleNames.add(b);

			LinePanel l50 = new LinePanel();
			l50.add(b);
			l50.addGlue();

			p.add(l50);
		}
		scroll = new JScrollPane(p);

		c1.add(scroll);

		// Convenient buttons
		LinePanel l51 = new LinePanel();
		checkAll = new JButton(Translator.swap("PhysExporterDialog.checkAll"));
		checkAll.addActionListener(this);
		uncheckAll = new JButton(Translator.swap("PhysExporterDialog.uncheckAll"));
		uncheckAll.addActionListener(this);
		l51.addGlue();
		l51.add(checkAll);
		l51.add(uncheckAll);
		l51.addGlue();
		c1.add(l51);

		selectAllParticles(true); // default
		// fc-5.6.2015

		LinePanel l15 = new LinePanel();
		addFuelMatrixButton = new JButton(Translator.swap("PhysExporterDialog.addFuelMatrixButton"));
		addFuelMatrixButton.addActionListener(this);
		addFuelMatrixButton.setEnabled(false); // will be enabled later
		l15.add(addFuelMatrixButton);
		l15.addStrut0();
		part2.add(l15);

		return part2;

	}

}
