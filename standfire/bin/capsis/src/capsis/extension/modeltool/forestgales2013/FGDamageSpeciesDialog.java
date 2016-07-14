package capsis.extension.modeltool.forestgales2013;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Translator;
import jeeb.lib.util.Check;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.ColumnPanel ;

import capsis.commongui.util.Helper;
import capsis.lib.forestgales.FGConfiguration;
import capsis.lib.forestgales.FGSpecies;
import capsis.lib.forestgales.function.Function;
import capsis.lib.forestgales.FGSoilType;
import capsis.lib.forestgales.FGRootingDepth;


/**
 * FGDamageSpeciesDialog - Dialog box to update species parameters for ForestGales model tool
 *
 * @author C. Meredieu, T. Labbé - March 2014
 */
public class FGDamageSpeciesDialog extends AmapDialog implements ActionListener {

	private JButton ok;
	private JButton cancel;
	private JButton help;

    private JTextField name;
	private JTextField topHeightMultiplier;
    private JTextField topHeightIntercept;
    private JTextField canopyWidthFunction;
    private JTextField canopyDepthFunction;
    private JTextField greenWoodDensity;
    private JTextField canopyDensity;
    private JTextField modulusOfRupture;
    private JTextField knotFactor;
    private JTextField crownFactor;
    private JTextField modulusOfElasticity;
    private JTextField canopyStreamliningC;
    private JTextField canopyStreamliningN;
    private JTextField rootBendingK;
	private JTextField[][] fieldM = new JTextField[4][3];
	private JTextField[][] fieldW = new JTextField[4][3];


	private FGConfiguration configuration;
	private String speciesName;
	private FGSpecies species;
	private int idSoilType;
	private int idRootingDepth;
	private double[][] overturningMomentMultipliers = new double [4][3]; // N.m/kg
	private double[][] overturningMomentMaximumStemWeights = new double [4][3]; // kg


	/**
	 * Constructor.
	 */
	public FGDamageSpeciesDialog (FGConfiguration configuration, String speciesName , int idSoilType, int idRootingDepth) {
		this.configuration = configuration;
		this.speciesName = speciesName;
		this.idSoilType = idSoilType;
		this.idRootingDepth = idRootingDepth;


		// Find the FGSpecies matching the given name (aborts if wrong species name)
		try {
			species = configuration.getSpecies (speciesName);
		} catch (Exception e) {
//				throw e; // the user will fix his entries and retry
		}

		createUI ();
		// location is set by AmapDialog
		pack ();
		show ();
	}

	/**
	 * Action on ok button.
	 */
	private void okAction ()  throws Exception {
		// Common: checks and copies user entries in stand and configuration
		// If user mistake, he is told by a message and an exception is thrown -> stop the process
		try {
			checkUserEntries ();
		} catch (Exception e) {
			throw e; // the user will fix his entries and retry
		}
		setValidDialog (true);
	}

	/**
	 * Events redirection according to their source.
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (ok)) {
			try {
				okAction ();
			} catch (Exception e) {
	//			throw e; // the user will fix his entries and retry
			}
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**
	 * Check all the user entries.
	 */
	private void checkUserEntries () throws Exception {

		// Classic checks...
		species.setTopHeightMultiplier (checkIsDouble (topHeightMultiplier, "topHeightMultiplier", true));
		species.setCanopyWidthFunction (Function.getFunction (canopyWidthFunction.getText ()));
		species.setTopHeightIntercept (checkIsDouble (topHeightIntercept, "topHeightIntercept", true));
		species.setCanopyDepthFunction (Function.getFunction (canopyDepthFunction.getText ()));
		species.setGreenWoodDensity (checkIsDouble (greenWoodDensity, "greenWoodDensity", true));
		species.setCanopyDensity (checkIsDouble (canopyDensity, "canopyDensity", true));


		species.setKnotFactor (checkIsDouble (knotFactor, "knotFactor", true));
		species.setCrownFactor (checkIsDouble (crownFactor, "crownFactor", true));

		species.setModulusOfRupture (checkIsDouble (modulusOfRupture, "modulusOfRupture", true)* 1E7);
		species.setModulusOfElasticity (checkIsDouble (modulusOfElasticity, "modulusOfElasticity", true)* 1E9);

		species.setCanopyStreamliningC (checkIsDouble (canopyStreamliningC, "canopyStreamliningC", true));
		species.setCanopyStreamliningN (checkIsDouble (canopyStreamliningN, "canopyStreamliningN", true));
		species.setRootBendingK (checkIsDouble (rootBendingK, "rootBendingK", true));


		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 3; j++) {
				overturningMomentMultipliers[i][j]=checkIsDouble(fieldM[i][j]," Coefficient OTM (Creg) ", true);
			}
		}
		species.setOverturningMomentMultipliers (overturningMomentMultipliers);


		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 3; j++) {
				overturningMomentMaximumStemWeights[i][j]=checkIsDouble(fieldW[i][j],"Maximum stem weight ", true);
			}
		}
		species.setOverturningMomentMaximumStemWeights (overturningMomentMaximumStemWeights);





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
	}

	/**
	 * Dialog box initialization.
	 */
	private void createUI () {
		Box part1 = Box.createVerticalBox ();

		JPanel mainPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
		Box box = Box.createVerticalBox ();

		LinePanel l0 = new LinePanel ();
		l0.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.name") + " : ", 200));
		name = new JTextField ();
		name.setText (species.getName ());
		name.setEditable (false);
		l0.add (name);
		box.add (l0);

		LinePanel l1 = new LinePanel ();
		l1.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.topHeightMultiplier") + " : ", 200));
		topHeightMultiplier = new JTextField ();
		topHeightMultiplier.setText ("" + species.getTopHeightMultiplier ());
		l1.add (topHeightMultiplier);
		box.add (l1);

		LinePanel l2 = new LinePanel ();
		l2.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.topHeightIntercept") + " : ", 200));
		topHeightIntercept = new JTextField ();
		topHeightIntercept.setText ("" + species.getTopHeightIntercept ());
		l2.add (topHeightIntercept);
		box.add (l2);

		LinePanel l3 = new LinePanel ();
		l3.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.canopyWidthFunction") + " : ", 200));
		canopyWidthFunction = new JTextField (50);
		canopyWidthFunction.setText ("" + species.getCanopyWidthFunction ());
		l3.add (canopyWidthFunction);
		box.add (l3);

		LinePanel l4 = new LinePanel ();
		l4.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.canopyDepthFunction") + " : ", 200));
		canopyDepthFunction = new JTextField (50);
		canopyDepthFunction.setText ("" + species.getCanopyDepthFunction ());
		l4.add (canopyDepthFunction);
		box.add (l4);

		LinePanel l5 = new LinePanel ();
		l5.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.greenWoodDensity") + " : ", 200));
		greenWoodDensity = new JTextField ();
		greenWoodDensity.setText ("" + species.getGreenWoodDensity ());
		l5.add (greenWoodDensity);
		box.add (l5);

		LinePanel l6 = new LinePanel ();
		l6.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.canopyDensity") + " : ", 200));
		canopyDensity = new JTextField ();
		canopyDensity.setText ("" + species.getCanopyDensity ());
		l6.add (canopyDensity);
		box.add (l6);

		LinePanel l7 = new LinePanel ();
		l7.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.modulusOfRupture") + " : ", 200));
		modulusOfRupture = new JTextField ();
		modulusOfRupture.setText ("" + species.getModulusOfRupture ()/1E7);
		l7.add (modulusOfRupture);
		box.add (l7);

		LinePanel l8 = new LinePanel ();
		l8.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.knotFactor") + " : ", 200));
		knotFactor = new JTextField ();
		knotFactor.setText ("" + species.getKnotFactor ());
		l8.add (knotFactor);
		box.add (l8);

		LinePanel l9 = new LinePanel ();
		l9.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.crownFactor") + " : ", 200));
		crownFactor = new JTextField ();
		crownFactor.setText ("" + species.getCrownFactor ());
		l9.add (crownFactor);
		box.add (l9);

		LinePanel l10 = new LinePanel ();
		l10.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.modulusOfElasticity") + " : ", 200));
		modulusOfElasticity = new JTextField ();
		modulusOfElasticity.setText ("" + species.getModulusOfElasticity ()/1E9);
		l10.add (modulusOfElasticity);
		box.add (l10);

		LinePanel l12 = new LinePanel ();
		l12.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.canopyStreamliningC") + " : ", 200));
		canopyStreamliningC = new JTextField ();
		canopyStreamliningC.setText ("" + species.getCanopyStreamliningC ());
		l12.add (canopyStreamliningC);
		box.add (l12);

		LinePanel l13 = new LinePanel ();
		l13.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.canopyStreamliningN") + " : ", 200));
		canopyStreamliningN = new JTextField ();
		canopyStreamliningN.setText ("" + species.getCanopyStreamliningN ());
		l13.add (canopyStreamliningN);
		box.add (l13);

		LinePanel l14 = new LinePanel ();
		l14.add (new JWidthLabel (Translator.swap ("FGDamageCalculatorDialog.rootBendingK") + " : ", 200));
		rootBendingK = new JTextField ();
		rootBendingK.setText ("" + species.getRootBendingK ());
		l14.add (rootBendingK);
		box.add (l14);

		ColumnPanel c1 = new ColumnPanel (Translator.swap ("FGDamageCalculatorDialog.fieldM"));
		box.add (c1);

		String[] SoilTypeName = new String [4];
		SoilTypeName [0] = FGSoilType.SOIL_TYPE_A.getName().substring(0,1);
		SoilTypeName [1] = FGSoilType.SOIL_TYPE_B.getName().substring(0,1);
		SoilTypeName [2] = FGSoilType.SOIL_TYPE_C.getName().substring(0,1);
		SoilTypeName [3] = FGSoilType.SOIL_TYPE_D.getName().substring(0,1);

		LinePanel l15 = new LinePanel ();
		l15.add (new JWidthLabel ("", 150));
		l15.add (new JWidthLabel (FGRootingDepth.SHALLOW.getName(), 150));
		l15.add (new JWidthLabel (FGRootingDepth.MEDIUM.getName(), 150));
		l15.add (new JWidthLabel (FGRootingDepth.DEEP.getName(), 150));
		l15.addGlue();
		c1.add (l15);

		LinePanel[] l16 = new LinePanel[4];
		for (int i = 0; i < 4; i++) {
			l16[i] = new LinePanel ();
			l16[i].add (new JWidthLabel ("Soil Type "+ SoilTypeName [i] + " : ", 150));
			for (int j = 0; j < 3; j++) {
				fieldM[i][j] = new JTextField (15);
				fieldM[i][j].setText ("" + species.getOverturningMomentMultipliers ()[i][j]);
				if ( i==idSoilType && j==idRootingDepth ) {
					fieldM[i][j].setEditable (true);
				} else {
					fieldM[i][j].setEditable (false);
				}
				l16[i].add (fieldM[i][j]);
			}
		l16[i].addGlue();
		c1.add (l16[i]);
		}

		ColumnPanel c2 = new ColumnPanel (Translator.swap ("FGDamageCalculatorDialog.fieldW"));
		box.add (c2);


		LinePanel l17 = new LinePanel ();
		l17.add (new JWidthLabel ("" , 150));
		l17.add (new JWidthLabel (FGRootingDepth.SHALLOW.getName() , 150));
		l17.add (new JWidthLabel (FGRootingDepth.MEDIUM.getName() , 150));
		l17.add (new JWidthLabel (FGRootingDepth.DEEP.getName(),150));
		l17.addGlue();
		c2.add (l17);

		LinePanel[] l18 = new LinePanel[4];
		for (int i = 0; i < 4; i++) {
			l18[i] = new LinePanel ();
			l18[i].add (new JWidthLabel ("Soil Type "+ SoilTypeName [i] + " : ", 150));
			for (int j = 0; j < 3; j++) {
				fieldW[i][j] = new JTextField (15);
				fieldW[i][j].setText ("" + species.getOverturningMomentMaximumStemWeights ()[i][j]);
				if ( i==idSoilType && j==idRootingDepth ) {
					fieldW[i][j].setEditable (true);
				} else {
					fieldW[i][j].setEditable (false);
				}
				l18[i].add (fieldW[i][j]);
			}
		l18[i].addGlue();
		c2.add (l18[i]);
		}


		mainPanel.add (box);

		// Control panel
		JPanel pControl = new JPanel (new FlowLayout (FlowLayout.RIGHT));
		ok = new JButton (Translator.swap ("Shared.ok"));
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		help = new JButton (Translator.swap ("Shared.help"));
		pControl.add (ok);
		pControl.add (cancel);
		pControl.add (help);
		ok.addActionListener (this);
		cancel.addActionListener (this);
		help.addActionListener (this);

		setDefaultButton (ok);	// from AmapDialog

		part1.add (mainPanel);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (part1, "Center");
		getContentPane ().add (pControl, "South");

		setTitle (Translator.swap ("FGDamageCalculatorDialog.Title"));

		setModal (true);
	}

}
