/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copycolumn2 (C) 2000-2013 Francois de Coligny
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

package capsis.extension.modeltool.forestgales2013;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jeeb.lib.util.Alert;
import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.IconLoader;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Question;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import jeeb.lib.util.gui.NorthPanel;
import capsis.commongui.util.Helper;
import capsis.extension.DialogModelTool;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.PathManager;
import capsis.kernel.Project;
import capsis.kernel.Step;
import capsis.lib.forestgales.FGConfiguration;
import capsis.lib.forestgales.FGRootingDepth;
import capsis.lib.forestgales.FGSLStandInterface;
import capsis.lib.forestgales.FGSoilType;
import capsis.lib.forestgales.FGStand;
import capsis.lib.forestgales.FGStandLevelRoughnessMethod;
import capsis.lib.forestgales.FGSimpleStandLevelRoughnessMethod;
import capsis.lib.forestgales.FGTLStandInterface;
import capsis.lib.forestgales.FGTree;

/**
 * A tool to run a damage simulation on a stand with ForestGales.
 *
 * @author B. Gardiner, K. Kamimura - August 2013
 */
public class FGDamageCalculatorDialog extends DialogModelTool implements ActionListener {

	static {
		Translator.addBundle ("capsis.extension.modeltool.forestgales2013.FGDamageCalculator");
	}

	public static final String NAME = Translator.swap ("FGDamageCalculator");
	static public final String VERSION = "1.0";
	static public final String AUTHOR = "B. Gardiner, K. Kamimura, T. Labbe, C.Meredieu";
	public static final String DESCRIPTION = Translator.swap ("FGDamageCalculator.description");

	private Step refStep;
	private GModel model;
	private Project project;
	private FGSLStandInterface refScene;

	private boolean standLevelIsPossible;
	private FGSLStandInterface slStand; // set if standLevelIsPossible, else null

	private boolean treeLevelIsPossible;
	private FGTLStandInterface tlStand; // set if standLevelIsPossible, else null

	private FGStand stand;
	private FGConfiguration configuration;
	private Map<String,String> speciesLinkage; // if empty, ignored

	// private FGDamageCalculator calculator;

	private Map<String,JComboBox> speciesSwitchMap;

	private JComboBox soilType;
	private JComboBox rootingDepth;
	private JTextField gapWidth;
	private JTextField gapHeight;

	private JCheckBox windClimateAvailable;
	private JTextField windClimateWeibullA;
	private JTextField windClimateWeibullK;
	private JTextField snowDensity;
	private JTextField vonKarmanConstant;
	private JTextField airDensity;
	private JTextField gravityAcceleration;
	private JTextField treeHeightsNumberFromEdge;
	private JTextField sizeOfUpwindGap;
	private JTextField resolutionOfCalculation;
	private JTextField elementDragCoefficient;
	private JTextField surfaceDragCoefficient;
	private JTextField roughnessConstant;
	private JTextField heightOfCalculation;
	private JTextField surroundingLandRoughness;
	private JTextField Ua;
	private JTextField U_C1;
	private JTextField U_C2;
	private JTextField U_C3;
	private JTextField U_C4;
	private JTextField DAMStoWeibullA1;
	private JTextField DAMStoWeibullA2;

	private JCheckBox tmcInterventionsOccurredInThe5PastYears;
	private JTextField tmcHowManyYearsInThePast;
	private JTextField tmcMeanDbhBeforeIntervention;
	private JTextField tmcMeanHeightBeforeIntervention;
	private JTextField tmcNhaBeforeIntervention;

	private JCheckBox scenarioMode; // method is ran for each step

	private JPanel reportPanel;

	private JLabel statusBar;

	// private JButton ...Method;
	private JButton standLevelRoughnessMethod;
	private JButton simpleStandLevelRoughnessMethod;


	private JButton speciesParameters;

	private JButton showReports;
	private JButton close;
	private JButton help;

	/**
	 * Default constructor.
	 */
	public FGDamageCalculatorDialog () throws Exception {
		super ();
		// calculator = new FGDamageCalculator ();
		standLevelIsPossible = false;
		treeLevelIsPossible = false;
	}

	private void initStand () throws Exception {
		stand = new FGStand ();
	}

	private void initConfiguration() throws Exception {
		configuration = new FGConfiguration ();
		// Load the species file
		configuration.loadSpeciesMap (PathManager.getDir ("data") + "/forestGales/forestGalesSpecies.txt");

	}

	@Override
	public void init (GModel model, Step refStep) {

		try {
			this.refStep = refStep;
			this.model = model;
			this.project = refStep.getProject ();

			refScene = (FGSLStandInterface) refStep.getScene ();

			initStand ();
			initConfiguration ();

			// // Moved this here, bug under windows...
			// calculator = new FGDamageCalculator ();

			speciesLinkage = new HashMap<String,String> ();

			// At least one simulation level may be possible, possibly both
			if (refScene instanceof FGSLStandInterface) {
				standLevelIsPossible = true;
				slStand = (FGSLStandInterface) refScene;
				// Prepare species linkage map
				String speciesName = slStand.getFGSpeciesName ();
				speciesLinkage.put (speciesName, speciesName);
			}
			if (refScene instanceof FGTLStandInterface) {
				treeLevelIsPossible = true;
				tlStand = (FGTLStandInterface) refScene;
				// Prepare species linkage map (one single entry per species name)
				for (FGTree t : tlStand.getFGTrees ()) {
					String speciesName = t.getSpeciesName ();
					speciesLinkage.put (speciesName, speciesName);
				}
			}

			// There is a strange bug over there on windows only, trying to find out what it is
			// setTitle (Translator.swap ("FGDamageCalculator") + " - " + refStep.getCaption ());

			createUI ();

			// setSize(new Dimension(600, 400));
			pack (); // sets the size
			setModal (false);
			setVisible (true);

		} catch (Exception e) {
			Log.println (Log.ERROR, "FGDamageCalculator.init ()", "Trouble during construction", e);
		}

	}

	/**
	 * Extension dynamic compatibility mechanism. This matchwith method checks if the extension can
	 * deal (i.e. is compatible) with the referent.
	 */
	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) { return false; }
			GModel m = (GModel) referent;
			GScene scene = ((Step) m.getProject ().getRoot ()).getScene ();
			if (!(scene instanceof FGSLStandInterface)) { return false; }

		} catch (Exception e) {
			Log.println (Log.ERROR, "FGDamageCalculator.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
		return true;
	}

	/**
	 * Run forestGales' simple stand level roughness method.
	 */
	private void simpleStandLevelRoughnessMethod () {
		try {
			if (!scenarioMode.isSelected ()) {
				// Run on the refStep only
				simpleStandLevelRoughnessMethod (refScene);
			} else {
				// Run on every step of the project
				Vector<Step> steps = project.getStepsFromRoot (refStep); // from root step to refStep
				for (Step step : steps) {
					simpleStandLevelRoughnessMethod ((FGSLStandInterface) step.getScene ());
				}
			}

			project.setSaved (false); // user may want to save the project before exiting

		} catch (Exception e) {
			// nothing more to be done here: everything processed in simpleStandLevelRoughnessMethod (scene)
		}
	}

	/**
	 * Run forestGales' stand level roughness method.
	 */
	private void standLevelRoughnessMethod () {
		try {
			if (!scenarioMode.isSelected ()) {
				// Run on the refStep only
				standLevelRoughnessMethod (refScene);
			} else {
				// Run on every step of the project
				Vector<Step> steps = project.getStepsFromRoot (refStep); // from root step to refStep
				for (Step step : steps) {
					standLevelRoughnessMethod ((FGSLStandInterface) step.getScene ());
				}
			}

			project.setSaved (false); // user may want to save the project before exiting

		} catch (Exception e) {
			// nothing more to be done here: everything processed in standLevelRoughnessMethod (scene)
		}
	}
	/**
	 * Simple stand level roughness method on the given scene
	 */
	private void simpleStandLevelRoughnessMethod (FGSLStandInterface scene) throws Exception {

		// 1. Common: creates stand with default values
		try {
			initStand ();
		} catch (Exception e) {
			Alert.print (Translator.swap ("FGDamageCalculatorDialog.errorPleaseCheckTheLog"), e);
			throw e;
		}

		// 2. Common: checks and copies user entries in stand and configuration
		// If user mistake, he is told by a message and an exception is thrown -> stop the process
		try {
			checkUserEntries ();
		} catch (Exception e) {
			throw e; // the user will fix his entries and retry
		}

		try {

			// 3. Method dependent: complete the stand by asking the connected model's scene
			stand.setNha (scene.getNha ());
			stand.setDominantHeight (scene.getDominantHeight ());

			// Create a single tree in the stand : the mean tree
			double dbh_m = scene.getMeanDbh (); // m
			double height = scene.getMeanHeight (); // m
			double crownWidth = scene.getMeanCrownWidth (); // m, optional (-1)
			double crownDepth = scene.getMeanCrownDepth (); // m, optional (-1)
			double stemVolume = scene.getMeanStemVolume (); // m3, optional (-1)
			double stemWeight = scene.getMeanStemWeight (); // kg, optional (-1)
			double crownVolume = scene.getMeanCrownVolume (); // m3, optional (-1)
			double crownWeight = scene.getMeanCrownWeight (); // kg, optional (-1)
			double [] diam = null;
			double [] z = null;
			double [] mass = null;


			// Get the speciesName of the FGStand
			String speciesName = scene.getFGSpeciesName ();
			// Switch to another species if linkage says so (aborts if wrong species name)
			if (!speciesLinkage.isEmpty ()) speciesName = speciesLinkage.get (speciesName);

			// Create and add a single mean tree in the stand
			FGTree meanTree = new FGTree (dbh_m, height, crownWidth, crownDepth, stemVolume, stemWeight, crownVolume,
					crownWeight, diam, z, mass, speciesName);
			stand.addTree (meanTree);

			// Launch the method
			FGSimpleStandLevelRoughnessMethod m = new FGSimpleStandLevelRoughnessMethod (stand, configuration);
			m.setWriteInTerminal (true);
			m.run ();

			// Result: add the method in the connected model's scene
			// The results are in the trees
			scene.addFGMethod (m);

		} catch (Exception e) {
			Log.println (Log.ERROR, "FGDamageCalculatorDialog.simpleStandLevelRoughnessMethodAction ()", "Method failed", e);
			Alert.print (Translator.swap ("FGDamageCalculatorDialog.errorPleaseCheckTheLog"), e);
			throw e;
		}

	}


	/**
	 * Stand level roughness method on the given scene.
	 */
	private void standLevelRoughnessMethod (FGSLStandInterface scene) throws Exception {

		// 1. Common: creates stand with default values
		try {
			initStand ();
		} catch (Exception e) {
			Alert.print (Translator.swap ("FGDamageCalculatorDialog.errorPleaseCheckTheLog"), e);
			throw e;
		}

		// 2. Common: checks and copies user entries in stand and configuration
		// If user mistake, he is told by a message and an exception is thrown -> stop the process
		try {
			checkUserEntries ();
		} catch (Exception e) {
			throw e; // the user will fix his entries and retry
		}

		try {

			// 3. Method dependent: complete the stand by asking the connected model's scene
			stand.setNha (scene.getNha ());
			stand.setDominantHeight (scene.getDominantHeight ());

			// Create a single tree in the stand : the mean tree
			double dbh_m = scene.getMeanDbh (); // m
			double height = scene.getMeanHeight (); // m
			double crownWidth = scene.getMeanCrownWidth (); // m, optional (-1)
			double crownDepth = scene.getMeanCrownDepth (); // m, optional (-1)
			double stemVolume = scene.getMeanStemVolume (); // m3, optional (-1)
			double stemWeight = scene.getMeanStemWeight (); // kg, optional (-1)
			double crownVolume = scene.getMeanCrownVolume (); // m3, optional (-1)
			double crownWeight = scene.getMeanCrownWeight (); // kg, optional (-1)
			double [] diam = null;
			double [] z = null;
			double [] mass = null;


			// Get the speciesName of the FGStand
			String speciesName = scene.getFGSpeciesName ();
			// Switch to another species if linkage says so (aborts if wrong species name)
			if (!speciesLinkage.isEmpty ()) speciesName = speciesLinkage.get (speciesName);

			// Create and add a single mean tree in the stand
			FGTree meanTree = new FGTree (dbh_m, height, crownWidth, crownDepth, stemVolume, stemWeight, crownVolume,
					crownWeight, diam, z, mass, speciesName);
			stand.addTree (meanTree);

			// Launch the method
			FGStandLevelRoughnessMethod m = new FGStandLevelRoughnessMethod (stand, configuration);
			m.setWriteInTerminal (true);
			m.run ();

			// Result: add the method in the connected model's scene
			// The results are in the trees
			scene.addFGMethod (m);

		} catch (Exception e) {
			Log.println (Log.ERROR, "FGDamageCalculatorDialog.standLevelRoughnessMethodAction ()", "Method failed", e);
			Alert.print (Translator.swap ("FGDamageCalculatorDialog.errorPleaseCheckTheLog"), e);
			throw e;
		}

	}

	/**
	 * Run forestGales' ... method.
	 */
	// private void ...Method () {
	//
	// // 1. Common: creates stand and configuration with default values
	// try {
	// initConfigurationAndStand ();
	// } catch (Exception e) {
	// Alert.print (Translator.swap ("FGDamageCalculatorDialog.errorPleaseCheckTheLog"), e);
	// return;
	// }
	//
	// // 2. Common: checks and copies user entries in stand and configuration
	// // If user mistake, he is told by a message and an exception is thrown -> return
	// try {
	// checkUserEntries ();
	// } catch (Exception e) {
	// return; // the user will fix his entries and retry
	// }
	//
	// try {
	//
	// // 3. Method dependent: complete the stand by asking the connected model's scene
	//
	// // SEE standLevelRoughnessMethod () as a template
	//
	// } catch (Exception e) {
	// Log.println (Log.ERROR, "FGDamageCalculatorDialog....Method ()", "Method failed", e);
	// Alert.print (Translator.swap ("FGDamageCalculatorDialog.errorPleaseCheckTheLog"), e);
	//
	// }
	// }

	private void speciesParameters () throws Exception {

		// Load the species linkage map
		for (String name1 : speciesSwitchMap.keySet ()) {
			String name2 = (String) speciesSwitchMap.get (name1).getSelectedItem ();
			speciesLinkage.put (name1, name2);
		}

		// Get the speciesName of the FGStand
		String speciesName = refScene.getFGSpeciesName ();
		// Switch to another species if linkage says so (aborts if wrong species name)
		if (!speciesLinkage.isEmpty ()) speciesName = speciesLinkage.get (speciesName);

		int idSoilType = ((FGSoilType) soilType.getSelectedItem ()).getId();
		int idRootingDepth = ((FGRootingDepth) rootingDepth.getSelectedItem ()).getId();

		FGDamageSpeciesDialog speciesDlg = new FGDamageSpeciesDialog (configuration, speciesName, idSoilType, idRootingDepth);

		if (speciesDlg.isValidDialog ()) {
			// all is done in dlg (input + checks + update of configuration)
		}
		speciesDlg.dispose ();		// kills the box
	}

	/**
	 * Updates the reports panel.
	 */
	private void showReports () {
		reportPanel.removeAll ();
		reportPanel.add (new FGDamageReportsPanel (refStep), BorderLayout.CENTER);
		validate ();
		repaint ();
	}

	@Override
	public void escapePressed () {
		if (Question.ask (this, Translator.swap ("FGDamageCalculatorDialog.confirm"), Translator
				.swap ("FGDamageCalculatorDialog.confirmClose"))) {
			setVisible (false);
			dispose ();
		}

	}

	@Override
	public void actionPerformed (ActionEvent evt) {

		if (evt.getSource ().equals (simpleStandLevelRoughnessMethod)) {
			simpleStandLevelRoughnessMethod ();

		} else if (evt.getSource ().equals (standLevelRoughnessMethod)) {
			standLevelRoughnessMethod ();

		} else if (evt.getSource ().equals (speciesParameters)) {
			try {
				speciesParameters ();
			} catch (Exception e) {
//				throw e; // the user will fix his entries and retry
			}

		} else if (evt.getSource ().equals (showReports)) {
			showReports ();

		} else if (evt.getSource ().equals (close)) {
			escapePressed ();

		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}

		synchro ();

	}

	/**
	 * Check all the user entries.
	 */
	private void checkUserEntries () throws Exception {

		// Classic checks...
		stand.setSoilType((FGSoilType) soilType.getSelectedItem ());

		stand.setRootingDepth((FGRootingDepth) rootingDepth.getSelectedItem ());



		stand.setGapWidth (checkIsDouble (gapWidth, "gapWidth", true));
		stand.setGapHeight (checkIsDouble (gapHeight, "gapHeight", true));
		stand.setWindClimateWeibullA (checkIsDouble (windClimateWeibullA, "windClimateWeibullA", true));
		stand.setWindClimateWeibullK (checkIsDouble (windClimateWeibullK, "windClimateWeibullK", true));
		stand.setTreeHeightsNumberFromEdge (checkIsDouble (treeHeightsNumberFromEdge, "treeHeightsNumberFromEdge", true));
		stand.setSizeOfUpwindGap (checkIsDouble (sizeOfUpwindGap, "sizeOfUpwindGap", true));

		configuration.setSnowDensity (checkIsDouble (snowDensity, "snowDensity", true));
		configuration.setVonKarmanConstant (checkIsDouble (vonKarmanConstant, "vonKarmanConstant", true));
		configuration.setAirDensity (checkIsDouble (airDensity, "airDensity", true));
		configuration.setGravityAcceleration (checkIsDouble (gravityAcceleration, "gravityAcceleration", true));
		configuration
				.setResolutionOfCalculation (checkIsDouble (resolutionOfCalculation, "resolutionOfCalculation", true));
		configuration
				.setElementDragCoefficient (checkIsDouble (elementDragCoefficient, "elementDragCoefficient", true));
		configuration
				.setSurfaceDragCoefficient (checkIsDouble (surfaceDragCoefficient, "surfaceDragCoefficient", true));
		configuration.setRoughnessConstant (checkIsDouble (roughnessConstant, "roughnessConstant", true));
		configuration.setHeightOfCalculation (checkIsDouble (heightOfCalculation, "heightOfCalculation", true));
		configuration.setSurroundingLandRoughness (checkIsDouble (surroundingLandRoughness, "surroundingLandRoughness", true));

		configuration.setUa (checkIsDouble (Ua, "Ua", true));
		configuration.setU_C1 (checkIsDouble (U_C1, "U_C1", false));
		configuration.setU_C2 (checkIsDouble (U_C2, "U_C2", false));
		configuration.setU_C3 (checkIsDouble (U_C3, "U_C3", false));
		configuration.setU_C4 (checkIsDouble (U_C4, "U_C4", false));
		configuration.setDAMStoWeibullA1 (checkIsDouble (DAMStoWeibullA1, "DAMStoWeibullA1", false));
		configuration.setDAMStoWeibullA2 (checkIsDouble (DAMStoWeibullA2, "DAMStoWeibullA2", false));

		configuration
				.setTmcInterventionsOccurredInThe5PastYears (tmcInterventionsOccurredInThe5PastYears.isSelected ());
		configuration
				.setTmcHowManyYearsInThePast (checkIsInt (tmcHowManyYearsInThePast, "tmcHowManyYearsInThePast", true));
		configuration
				.setTmcMeanDbhBeforeIntervention (checkIsDouble (tmcMeanDbhBeforeIntervention, "tmcMeanDbhBeforeIntervention", true));
		configuration
				.setTmcMeanHeightBeforeIntervention (checkIsDouble (tmcMeanHeightBeforeIntervention, "tmcMeanHeightBeforeIntervention", true));
		configuration
				.setTmcNhaBeforeIntervention (checkIsDouble (tmcNhaBeforeIntervention, "tmcNhaBeforeIntervention", true));

		// Load the species linkage map
		for (String name1 : speciesSwitchMap.keySet ()) {
			String name2 = (String) speciesSwitchMap.get (name1).getSelectedItem ();
			speciesLinkage.put (name1, name2);
		}

		// Memo for next time
		Settings.setProperty ("forestgales.scenarioMode", scenarioMode.isSelected ());

	}

	private int checkIsInt (JTextField f, String variableName, boolean mustBePositive) throws Exception {
		if (!Check.isInt (f.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FGDamageCalculatorDialog.thisVariableMustBeAInteger") + " : "
					+ variableName);
			throw new Exception ();
		}
		if (mustBePositive) {
			double i = Check.intValue (f.getText ().trim ());
			if (i < 0) {
				MessageDialog.print (this, Translator.swap ("FGDamageCalculatorDialog.thisVariableMustBePositive")
						+ " : " + variableName);
				throw new Exception ();
			}
		}
		return Check.intValue (f.getText ().trim ());
	}

	private double checkIsDouble (JTextField f, String variableName, boolean mustBePositive) throws Exception {
		if (!Check.isDouble (f.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("FGDamageCalculatorDialog.thisVariableMustBeANumber") + " : "
					+ variableName);
			throw new Exception ();
		}
		if (mustBePositive) {
			double d = Check.doubleValue (f.getText ().trim ());
			if (d < 0) {
				MessageDialog.print (this, Translator.swap ("FGDamageCalculatorDialog.thisVariableMustBePositive")
						+ " : " + variableName);
				throw new Exception ();
			}
		}
		return Check.doubleValue (f.getText ().trim ());
	} // set if standLevelIsPossible, else null

	/**
	 * User interface definition
	 */
	private void createUI () {

		LinePanel main = new LinePanel ();

		// Main layout
		ColumnPanel column1 = new ColumnPanel ();
		main.add (new NorthPanel (column1));
		ColumnPanel column2 = new ColumnPanel ();
		main.add (new NorthPanel (column2));
		ColumnPanel column3 = new ColumnPanel (Translator.swap ("FGDamageCalculatorDialog.reports"));
		main.add (column3);
		main.addStrut0 ();

		// Stand
		ColumnPanel c1 = new ColumnPanel (Translator.swap ("FGDamageCalculatorDialog.stand"));
		column2.add (c1);

		LinePanel l1 = new LinePanel ();
		l1.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.soilType") + " : ", 120));
		Vector v = new Vector ();
		v.add (FGSoilType.SOIL_TYPE_A);
		v.add (FGSoilType.SOIL_TYPE_B);
		v.add (FGSoilType.SOIL_TYPE_C);
		v.add (FGSoilType.SOIL_TYPE_D);
		soilType = new JComboBox (v);
		l1.add (soilType);
		l1.addStrut0 ();

		c1.add (l1);

		LinePanel l2 = new LinePanel ();
		l2.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.rootingDepth") + " : ", 120));
		v = new Vector ();
		v.add (FGRootingDepth.SHALLOW);
		v.add (FGRootingDepth.MEDIUM);
		v.add (FGRootingDepth.DEEP);
		rootingDepth = new JComboBox (v);
		l2.add (rootingDepth);
		l2.addStrut0 ();

		c1.add (l2);

		LinePanel l3 = new LinePanel ();
		l3.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.gapWidth") + " : ", 120));
		gapWidth = new JTextField (15);
		gapWidth.setText ("" + stand.getGapWidth ());
		l3.add (gapWidth);

		c1.add (l3);

		LinePanel l4 = new LinePanel ();
		l4.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.gapHeight") + " : ", 120));
		gapHeight = new JTextField (15);
		gapHeight.setText ("" + stand.getGapHeight ());
		l4.add (gapHeight);

		c1.add (l4);

		LinePanel l14 = new LinePanel ();
		l14.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.treeHeightsNumberFromEdge") + " : ", 200));
		treeHeightsNumberFromEdge = new JTextField ();
		treeHeightsNumberFromEdge.setText ("" + stand.getTreeHeightsNumberFromEdge ());
		l14.add (treeHeightsNumberFromEdge);

		c1.add (l14);

		LinePanel l15 = new LinePanel ();
		l15.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.sizeOfUpwindGap") + " : ", 200));
		sizeOfUpwindGap = new JTextField ();
		sizeOfUpwindGap .setText ("" + stand.getSizeOfUpwindGap  ());
		l15.add (sizeOfUpwindGap );

		c1.add (l15);


		LinePanel l6 = new LinePanel ();
		windClimateAvailable = new JCheckBox (Translator.swap ("FGDamageCalculatorDialog.windClimateAvailable"), false);
		windClimateAvailable.addActionListener (this);
		l6.add (windClimateAvailable);
		l6.addGlue ();

		c1.add (l6);

		LinePanel l7 = new LinePanel ();
		l7.addMargin (20);
		l7.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.windClimateWeibullA") + " : ", 120));
		windClimateWeibullA = new JTextField ();
		windClimateWeibullA.setText ("" + stand.getWindClimateWeibullA ());
		l7.add (windClimateWeibullA);

		c1.add (l7);

		LinePanel l8 = new LinePanel ();
		l8.addMargin (20);
		l8.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.windClimateWeibullK") + " : ", 120));
		windClimateWeibullK = new JTextField ();
		windClimateWeibullK.setText ("" + stand.getWindClimateWeibullK ());
		l8.add (windClimateWeibullK);

		c1.add (l8);

		// Species linkage
		ColumnPanel c2 = new ColumnPanel (Translator.swap ("FGDamageCalculatorDialog.species"));
		column2.add (c2);

		speciesSwitchMap = new HashMap<String,JComboBox> ();
		for (String name1 : new TreeSet<String> (speciesLinkage.keySet ())) {
			String name2 = speciesLinkage.get (name1);
			JTextField f = new JTextField ();
			f.setText (name1);

			Vector availableSpeciesNames = new Vector (new TreeSet<String> (configuration.getSpeciesMap ().keySet ()));
			JComboBox combo = new JComboBox (availableSpeciesNames);
			combo.setSelectedItem (name2);

			LinePanel line = new LinePanel ();
			line.add (new JLabel (Translator.swap ("FGDamageCalculatorDialog.switch")));
			line.add (f);
			line.add (new JLabel (Translator.swap ("FGDamageCalculatorDialog.to")));
			line.add (combo);
			line.addStrut0 ();

			c2.add (line);

			// At the end, name1 will be replaced by the the selected name in combo
			speciesSwitchMap.put (name1, combo);
		}

		// Species parameters
		//ColumnPanel c9 = new ColumnPanel (Translator.swap ("FGDamageCalculatorDialog.speciesParameters"));
		//column2.add (c9);

		LinePanel l51 = new LinePanel ();
		speciesParameters = new JButton (Translator.swap ("FGDamageCalculatorDialog.speciesParameters"));
		speciesParameters.addActionListener (this);
		speciesParameters.setEnabled (true);
		l51.add (speciesParameters);
		l51.addStrut0 ();

		c2.add (l51);






		// Specific settings
		ColumnPanel c8 = new ColumnPanel (Translator.swap ("FGDamageCalculatorDialog.specificSettings"));
		column2.add (c8);

		LinePanel l50 = new LinePanel ();
		scenarioMode = new JCheckBox (Translator.swap ("FGDamageCalculatorDialog.scenarioMode"));
		scenarioMode.setSelected (Settings.getProperty ("forestgales.scenarioMode", false));
		l50.add (scenarioMode);
		l50.addGlue ();

		c8.add (l50);

		// Configuration
		ColumnPanel c3 = new ColumnPanel (Translator.swap ("FGDamageCalculatorDialog.configuration"));
		column1.add (c3);

		LinePanel l10 = new LinePanel ();
		l10.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.snowDensity") + " : ", 150));
		snowDensity = new JTextField ();
		snowDensity.setText ("" + configuration.getSnowDensity ());
		l10.add (snowDensity);

		c3.add (l10);

		LinePanel l11 = new LinePanel ();
		l11.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.vonKarmanConstant") + " : ", 150));
		vonKarmanConstant = new JTextField ();
		vonKarmanConstant.setText ("" + configuration.getVonKarmanConstant ());
		l11.add (vonKarmanConstant);

		c3.add (l11);

		LinePanel l12 = new LinePanel ();
		l12.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.airDensity") + " : ", 150));
		airDensity = new JTextField ();
		airDensity.setText ("" + configuration.getAirDensity ());
		l12.add (airDensity);

		c3.add (l12);

		LinePanel l13 = new LinePanel ();
		l13.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.gravityAcceleration") + " : ", 150));
		gravityAcceleration = new JTextField ();
		gravityAcceleration.setText ("" + configuration.getGravityAcceleration ());
		l13.add (gravityAcceleration);

		c3.add (l13);


		LinePanel l16 = new LinePanel ();
		l16.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.resolutionOfCalculation") + " : ", 200));
		resolutionOfCalculation = new JTextField ();
		resolutionOfCalculation.setText ("" + configuration.getResolutionOfCalculation ());
		l16.add (resolutionOfCalculation);

		c3.add (l16);

		LinePanel l17 = new LinePanel ();
		l17.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.elementDragCoefficient") + " : ", 200));
		elementDragCoefficient = new JTextField ();
		elementDragCoefficient.setText ("" + configuration.getElementDragCoefficient ());
		l17.add (elementDragCoefficient);

		c3.add (l17);

		LinePanel l18 = new LinePanel ();
		l18.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.surfaceDragCoefficient") + " : ", 200));
		surfaceDragCoefficient = new JTextField ();
		surfaceDragCoefficient.setText ("" + configuration.getSurfaceDragCoefficient ());
		l18.add (surfaceDragCoefficient);

		c3.add (l18);

		LinePanel l19 = new LinePanel ();
		l19.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.roughnessConstant") + " : ", 200));
		roughnessConstant = new JTextField ();
		roughnessConstant.setText ("" + configuration.getRoughnessConstant ());
		l19.add (roughnessConstant);

		c3.add (l19);

		LinePanel l20 = new LinePanel ();
		l20.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.heightOfCalculation") + " : ", 200));
		heightOfCalculation = new JTextField ();
		heightOfCalculation.setText ("" + configuration.getHeightOfCalculation ());
		l20.add (heightOfCalculation);

		c3.add (l20);

		LinePanel l20b = new LinePanel ();
		l20b.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.surroundingLandRoughness") + " : ", 200));
		surroundingLandRoughness = new JTextField ();
		surroundingLandRoughness.setText ("" + configuration.getSurroundingLandRoughness ());
		l20b.add (surroundingLandRoughness);

		c3.add (l20b);

		LinePanel l21 = new LinePanel ();
		l21.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.Ua") + " : ", 120));
		Ua = new JTextField ();
		Ua.setText ("" + configuration.getUa ());
		l21.add (Ua);

		c3.add (l21);

		LinePanel l22 = new LinePanel ();
		l22.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.U_C1") + " : ", 120));
		U_C1 = new JTextField ();
		U_C1.setText ("" + configuration.getU_C1 ());
		l22.add (U_C1);

		c3.add (l22);

		LinePanel l23 = new LinePanel ();
		l23.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.U_C2") + " : ", 120));
		U_C2 = new JTextField ();
		U_C2.setText ("" + configuration.getU_C2 ());
		l23.add (U_C2);

		c3.add (l23);

		LinePanel l24 = new LinePanel ();
		l24.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.U_C3") + " : ", 120));
		U_C3 = new JTextField ();
		U_C3.setText ("" + configuration.getU_C3 ());
		l24.add (U_C3);

		c3.add (l24);

		LinePanel l25 = new LinePanel ();
		l25.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.U_C4") + " : ", 120));
		U_C4 = new JTextField ();
		U_C4.setText ("" + configuration.getU_C4 ());
		l25.add (U_C4);

		c3.add (l25);

		LinePanel l26 = new LinePanel ();
		l26.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.DAMStoWeibullA1") + " : ", 150));
		DAMStoWeibullA1 = new JTextField ();
		DAMStoWeibullA1.setText ("" + configuration.getDAMStoWeibullA1 ());
		l26.add (DAMStoWeibullA1);

		c3.add (l26);

		LinePanel l27 = new LinePanel ();
		l27.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.DAMStoWeibullA2") + " : ", 150));
		DAMStoWeibullA2 = new JTextField ();
		DAMStoWeibullA2.setText ("" + configuration.getDAMStoWeibullA2 ());
		l27.add (DAMStoWeibullA2);

		c3.add (l27);

		// TMC
		LinePanel l30 = new LinePanel ();
		tmcInterventionsOccurredInThe5PastYears = new JCheckBox (
				Translator.swap ("FGDamageCalculatorDialog.tmcInterventionsOccurredInThe5PastYears"), false);
		tmcInterventionsOccurredInThe5PastYears.addActionListener (this);
		l30.add (tmcInterventionsOccurredInThe5PastYears);
		l30.addGlue ();

		c3.add (l30);

		LinePanel l32 = new LinePanel ();
		l32.addMargin (20);
		l32.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.tmcHowManyYearsInThePast") + " : ", 120));
		tmcHowManyYearsInThePast = new JTextField ();
		tmcHowManyYearsInThePast.setText ("" + configuration.getTmcHowManyYearsInThePast ());
		l32.add (tmcHowManyYearsInThePast);

		c3.add (l32);

		LinePanel l32a = new LinePanel ();
		l32a.addMargin (20);
		l32a.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.tmcMeanDbhBeforeIntervention") + " : ",
				120));
		tmcMeanDbhBeforeIntervention = new JTextField ();
		tmcMeanDbhBeforeIntervention.setText ("" + configuration.getTmcMeanDbhBeforeIntervention ());
		l32a.add (tmcMeanDbhBeforeIntervention);

		c3.add (l32a);

		LinePanel l33 = new LinePanel ();
		l33.addMargin (20);
		l33.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.tmcMeanHeightBeforeIntervention") + " : ",
				120));
		tmcMeanHeightBeforeIntervention = new JTextField ();
		tmcMeanHeightBeforeIntervention.setText ("" + configuration.getTmcMeanHeightBeforeIntervention ());
		l33.add (tmcMeanHeightBeforeIntervention);

		c3.add (l33);

		LinePanel l34 = new LinePanel ();
		l34.addMargin (20);
		l34.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.tmcNhaBeforeIntervention") + " : ", 120));
		tmcNhaBeforeIntervention = new JTextField ();
		tmcNhaBeforeIntervention.setText ("" + configuration.getTmcNhaBeforeIntervention ());
		l34.add (tmcNhaBeforeIntervention);

		c3.add (l34);


		// Reports
		reportPanel = new JPanel (new BorderLayout ());
		reportPanel.add (new FGDamageReportsPanel (refStep), BorderLayout.CENTER); // will be fed later
		column3.add (reportPanel);
		column3.addStrut0 ();



		// Simulation buttons
		LinePanel commands = new LinePanel ();
		commands.addGlue ();

		LinePanel l40 = new LinePanel ();
		simpleStandLevelRoughnessMethod = new JButton (Translator.swap ("FGDamageCalculatorDialog.simpleStandLevelRoughnessMethod"));
		simpleStandLevelRoughnessMethod.addActionListener (this);
		simpleStandLevelRoughnessMethod.setEnabled (standLevelIsPossible);
		l40.add (simpleStandLevelRoughnessMethod);
		l40.addStrut0 ();

		commands.add (l40);

		LinePanel l41 = new LinePanel ();
		standLevelRoughnessMethod = new JButton (Translator.swap ("FGDamageCalculatorDialog.standLevelRoughnessMethod"));
		standLevelRoughnessMethod.addActionListener (this);
		standLevelRoughnessMethod.setEnabled (standLevelIsPossible);
		l41.add (standLevelRoughnessMethod);
		l41.addStrut0 ();

		commands.add (l41);


		// LinePanel l42 = new LinePanel ();
		// ...Method = new JButton (Translator.swap ("FGDamageCalculatorDialog....Method"));
		// ...Method.addActionListener (this);
		// ...Method.setEnabled (treeLevelIsPossible); // OR standLevelIsPossible
		// l42.add (...Method);
		// l42.addStrut0 ();
		//
		// commands.add (l42);

		commands.addGlue ();

		// Control panel
		JPanel controlPanel = new JPanel (new FlowLayout (FlowLayout.RIGHT));

		statusBar = new JLabel ();
		statusBar.setText (Translator.swap ("Shared.ready"));

		showReports = new JButton (Translator.swap ("FGDamageCalculatorDialog.showReports"));
		showReports.addActionListener (this);

		close = new JButton (Translator.swap ("Shared.close"));
		close.addActionListener (this);
		ImageIcon icon = IconLoader.getIcon ("close_16.png");
		close.setIcon (icon);

		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		icon = IconLoader.getIcon ("help_16.png");
		help.setIcon (icon);

		controlPanel.add (showReports);
		controlPanel.add (close);
		controlPanel.add (help);

		// Sets ok as default
		// setDefaultButton (ok);

		JPanel aux = new JPanel (new BorderLayout ());
		aux.add (main, BorderLayout.CENTER);
		aux.add (commands, BorderLayout.SOUTH);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (aux, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		synchro ();

		pack ();
//		setSize (new Dimension (main.getWidth (), 400));



	}

	private void synchro () {

		windClimateWeibullA.setEnabled (windClimateAvailable.isSelected ());
		windClimateWeibullK.setEnabled (windClimateAvailable.isSelected ());

		tmcHowManyYearsInThePast.setEnabled (tmcInterventionsOccurredInThe5PastYears.isSelected ());
		tmcMeanDbhBeforeIntervention.setEnabled (tmcInterventionsOccurredInThe5PastYears.isSelected ());
		tmcMeanHeightBeforeIntervention.setEnabled (tmcInterventionsOccurredInThe5PastYears.isSelected ());
		tmcNhaBeforeIntervention.setEnabled (tmcInterventionsOccurredInThe5PastYears.isSelected ());

	}

}
