/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2003  Francois de Coligny
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package capsis.extension.modeltool.amapsim2;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import capsis.gui.MainFrame;
import capsis.kernel.Step;
import capsis.util.JWidthRadioButton;

/**
 * Mode1Dialog is a dialog box to enter parameters for the related request.
 *
 * @author F. de Coligny - november 2002
 */
public class Mode1Dialog extends AmapDialog implements ActionListener {

	private Mode1 request;
	private Step step;

	// User data : default values, changeable by user
	private JTextField messageId;	// not changeable
	private JTextField species;

	private JTextField numberOfTreesToBeSimulated;

	private JCheckBox storeLineTree;
	private JCheckBox storeMtg;
	private JCheckBox storeBranches;
	private JCheckBox storeCrownLayers;
	private JCheckBox storePolycyclism;

		private JCheckBox storeTrunkShape;			// 19.1.2004
		private JTextField initialSimulationAge;	// 20.1.2004

	private JTextField surface;
	private JTextField numberOfTreesInStand;	// for density - fc - 22.10.2003
	private JTextField basalArea;
	private JTextField fertilityHDom;
	private JTextField fertilityAge;
	private JTextField coeffAge;

	private JCheckBox useAge;	// for management
		private	ButtonGroup ageGroup;
		private	JRadioButton rAgeM;
		private	JRadioButton rAgeG;
		private	JRadioButton rAgeDom;
	private JTextField ageMean;
	private JTextField ageg;
	private JTextField ageDom;
		private ButtonGroup ageDispersionGroup;
		private	JRadioButton rAgeStandardDeviation;
		private	JRadioButton rAgeMinMax;
	private JTextField ageStandardDeviation;
	private JTextField ageMin;
	private JTextField ageMax;

	private JCheckBox useH;	// for management
		private	ButtonGroup heightGroup;
		private	JRadioButton rHeightM;
		private	JRadioButton rHeightG;
		private	JRadioButton rHeightDom;
	private JTextField HMean;
	private JTextField Hg;
	private JTextField HDom;
		private ButtonGroup heightDispersionGroup;
		private	JRadioButton rHeightStandardDeviation;
		private	JRadioButton rHeightMinMax;
	private JTextField HStandardDeviation;
	private JTextField HMin;
	private JTextField HMax;

	private JCheckBox useD;	// for management
		private	ButtonGroup diamGroup;
		private	JRadioButton rDiamM;
		private	JRadioButton rDiamG;
		private	JRadioButton rDiamDom;
	private JTextField DMean;
	private JTextField Dg;
	private JTextField DDom;
		private ButtonGroup diamDispersionGroup;
		private	JRadioButton rDiamStandardDeviation;
		private	JRadioButton rDiamMinMax;
	private JTextField DStandardDeviation;
	private JTextField DMin;
	private JTextField DMax;

	private JCheckBox useCrown;	// for management		// added on 19.1.2003
	private JTextField crownBaseHeight;				// added on 19.1.2003
	private JTextField crownMaxDiameter;			// added on 19.1.2003

	// Control panel
	private JButton ok;
	private JButton cancel;
	private JButton help;

	/**	Request contains default values to be changed by user.
	*/
	public Mode1Dialog (Mode1 request, Step step) {
		super (MainFrame.getInstance ());
		this.request = request;
		this.step = step;

		setDefaultCloseOperation (DO_NOTHING_ON_CLOSE);
		addWindowListener (new WindowAdapter () {
			public void windowClosing(WindowEvent e) {
				escapePressed ();
			}
		});

		createUI ();

		pack ();
		setModal (true);
		
		setVisible (true);

	}

	private void okAction () {
		// checks here
		String id = messageId.getText ().trim ();

		// species is mandatory
		if (Check.isEmpty (species.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("Mode1Dialog.speciesMustBeSpecified"));
			return;
		}

		// numberOfTreesToBeSimulated > 0
		if (Check.isEmpty (numberOfTreesToBeSimulated.getText ().trim ())
				|| !Check.isInt (numberOfTreesToBeSimulated.getText ().trim ())
				|| Check.intValue (numberOfTreesToBeSimulated.getText ().trim ()) <= 0) {
			MessageDialog.print (this, Translator.swap ("Mode1Dialog.numberOfTreesToBeSimulatedMustBePositive"));
			return;
		}

		// surface > 0
		if (Check.isEmpty (surface.getText ().trim ())
				|| !Check.isDouble (surface.getText ().trim ())
				|| Check.doubleValue (surface.getText ().trim ()) <= 0) {
			MessageDialog.print (this, Translator.swap ("Mode1Dialog.surfaceMustBePositive"));
			return;
		}

		// numberOfTreesInStand > 0 (for density)
		if (Check.isEmpty (numberOfTreesInStand.getText ().trim ())
				|| !Check.isInt (numberOfTreesInStand.getText ().trim ())
				|| Check.intValue (numberOfTreesInStand.getText ().trim ()) <= 0) {
			MessageDialog.print (this, Translator.swap ("Mode1Dialog.numberOfTreesInStandMustBePositive"));
			return;
		}

		// basal area > 0
		if (Check.isEmpty (basalArea.getText ().trim ())
				|| !Check.isDouble (basalArea.getText ().trim ())
				|| Check.doubleValue (basalArea.getText ().trim ()) <= 0) {
			MessageDialog.print (this, Translator.swap ("Mode1Dialog.basalAreaMustBePositive"));
			return;
		}

		// fertilityHDom > 0f
		if (Check.isEmpty (fertilityHDom.getText ().trim ())
				|| !Check.isDouble (fertilityHDom.getText ().trim ())
				|| Check.doubleValue (fertilityHDom.getText ().trim ()) <= 0) {
			MessageDialog.print (this, Translator.swap ("Mode1Dialog.fertilityHDomMustBePositive"));
			return;
		}

		// fertilityAge > 0f
		if (Check.isEmpty (fertilityAge.getText ().trim ())
				|| !Check.isDouble (fertilityAge.getText ().trim ())
				|| Check.doubleValue (fertilityAge.getText ().trim ()) <= 0) {
			MessageDialog.print (this, Translator.swap ("Mode1Dialog.fertilityAgeMustBePositive"));
			return;
		}

		// coeffAge : no controls yet
		// coeffAge : no controls yet
		// coeffAge : no controls yet

		// Age, H and Diam Radio buttons must match valid values
		// Age
		if (useAge.isSelected () &&
				(!rAgeM.isSelected () && !rAgeG.isSelected () && !rAgeDom.isSelected ())) {
			MessageDialog.print (this, Translator.swap ("Mode1Dialog.chooseATargetForAge"));
			return;
		}
		if (useAge.isSelected () && rAgeM.isSelected ()) {
			if (Check.isEmpty (ageMean.getText ().trim ())
					|| Check.doubleValue (ageMean.getText ().trim ()) <= 0) {
				MessageDialog.print (this, Translator.swap ("Mode1Dialog.ageMeanMustBeSetAndPositive"));
				return;
			}
		}
		if (useAge.isSelected () && rAgeG.isSelected ()) {
			if (Check.isEmpty (ageg.getText ().trim ())
					|| Check.doubleValue (ageg.getText ().trim ()) <= 0) {
				MessageDialog.print (this, Translator.swap ("Mode1Dialog.agegMustBeSetAndPositive"));
				return;
			}
		}
		if (useAge.isSelected () && rAgeDom.isSelected ()) {
			if (Check.isEmpty (ageDom.getText ().trim ())
					|| Check.doubleValue (ageDom.getText ().trim ()) <= 0) {
				MessageDialog.print (this, Translator.swap ("Mode1Dialog.ageDomMustBeSetAndPositive"));
				return;
			}
		}

		// Height
		if (useH.isSelected () &&
				(!rHeightM.isSelected () && !rHeightG.isSelected () && !rHeightDom.isSelected ())) {
			MessageDialog.print (this, Translator.swap ("Mode1Dialog.chooseATargetForHeight"));
			return;
		}
		if (useH.isSelected () && rHeightM.isSelected ()) {
			if (Check.isEmpty (HMean.getText ().trim ())
					|| Check.doubleValue (HMean.getText ().trim ()) <= 0) {
				MessageDialog.print (this, Translator.swap ("Mode1Dialog.HMeanMustBeSetAndPositive"));
				return;
			}
		}
		if (useH.isSelected () && rHeightG.isSelected ()) {
			if (Check.isEmpty (Hg.getText ().trim ())
					|| Check.doubleValue (Hg.getText ().trim ()) <= 0) {
				MessageDialog.print (this, Translator.swap ("Mode1Dialog.HgMustBeSetAndPositive"));
				return;
			}
		}
		if (useH.isSelected () && rHeightDom.isSelected ()) {
			if (Check.isEmpty (HDom.getText ().trim ())
					|| Check.doubleValue (HDom.getText ().trim ()) <= 0) {
				MessageDialog.print (this, Translator.swap ("Mode1Dialog.HDomMustBeSetAndPositive"));
				return;
			}
		}

		// Diameter
		if (useD.isSelected () &&
				(!rDiamM.isSelected () && !rDiamG.isSelected () && !rDiamDom.isSelected ())) {
			MessageDialog.print (this, Translator.swap ("Mode1Dialog.chooseATargetForDiameter"));
			return;
		}
		if (useD.isSelected () && rDiamM.isSelected ()) {
			if (Check.isEmpty (DMean.getText ().trim ())
					|| Check.doubleValue (DMean.getText ().trim ()) <= 0) {
				MessageDialog.print (this, Translator.swap ("Mode1Dialog.DMeanMustBeSetAndPositive"));
				return;
			}
		}
		if (useD.isSelected () && rDiamG.isSelected ()) {
			if (Check.isEmpty (Dg.getText ().trim ())
					|| Check.doubleValue (Dg.getText ().trim ()) <= 0) {
				MessageDialog.print (this, Translator.swap ("Mode1Dialog.DgMustBeSetAndPositive"));
				return;
			}
		}
		if (useD.isSelected () && rDiamDom.isSelected ()) {
			if (Check.isEmpty (DDom.getText ().trim ())
					|| Check.doubleValue (DDom.getText ().trim ()) <= 0) {
				MessageDialog.print (this, Translator.swap ("Mode1Dialog.DDomMustBeSetAndPositive"));
				return;
			}
		}

		// ADD CONTROLS ON STANDARD DEVIATION, MIN & MAX ???
		// ADD CONTROLS ON STANDARD DEVIATION, MIN & MAX ???
		// ADD CONTROLS ON STANDARD DEVIATION, MIN & MAX ???

		// set data in request and set validation
		updateRequest ();

		setValidDialog (true);
	}

	// Set user interface data into request before leaving
	//
	private void updateRequest () {

		//request.dataLength = 0;		// deferred calculation (see Mode1)
		//request.messageId 			// already set (see Mode1)
		//request.requestType = 1;		// already set (see Mode1)

		request.species = species.getText ().trim ();

		request.numberOfTreesToBeSimulated = Check.intValue (numberOfTreesToBeSimulated.getText ().trim ());

		request.storeLineTree = storeLineTree.isSelected ();
		request.storeMtg = storeMtg.isSelected ();
		request.storeBranches = storeBranches.isSelected ();
		request.storeCrownLayers = storeCrownLayers.isSelected ();
		request.storePolycyclism = storePolycyclism.isSelected ();

			request.storeTrunkShape = storeTrunkShape.isSelected ();		// 19.1.2004
			request.initialSimulationAge = Check.intValue (initialSimulationAge.getText ().trim ());	// 20.1.2004

		request.surface = (float) Check.doubleValue (surface.getText ().trim ());
		request.numberOfTreesInStand = Check.intValue (numberOfTreesInStand.getText ().trim ());	// for density
		request.basalArea = (float) Check.doubleValue (basalArea.getText ().trim ());
		request.fertilityHDom = (float) Check.doubleValue (fertilityHDom.getText ().trim ());
		request.fertilityAge = (float) Check.doubleValue (fertilityAge.getText ().trim ());
		request.coeffAge = (float) Check.doubleValue (coeffAge.getText ().trim ());

		request.useAge = useAge.isSelected ();
		request.ageMean = rAgeM.isSelected () ? (float) Check.doubleValue (ageMean.getText ().trim ()) : 0f;
		request.ageg = rAgeG.isSelected () ? (float) Check.doubleValue (ageg.getText ().trim ()) : 0f;
		request.ageDom = rAgeDom.isSelected () ? (float) Check.doubleValue (ageDom.getText ().trim ()) : 0f;
			request.ageStandardDeviation = rAgeStandardDeviation.isSelected () ? (float) Check.doubleValue (ageStandardDeviation.getText ().trim ()) : 0f;
			request.ageMin = rAgeMinMax.isSelected () ? (float) Check.doubleValue (ageMin.getText ().trim ()) : 0f;
			request.ageMax = rAgeMinMax.isSelected () ? (float) Check.doubleValue (ageMax.getText ().trim ()) : 0f;

		request.useH = useH.isSelected ();
		request.HMean = rHeightM.isSelected () ? (float) Check.doubleValue (HMean.getText ().trim ()) : 0f;
		request.Hg = rHeightG.isSelected () ? (float) Check.doubleValue (Hg.getText ().trim ()) : 0f;
		request.HDom = rHeightDom.isSelected () ? (float) Check.doubleValue (HDom.getText ().trim ()) : 0f;
			request.HStandardDeviation = rHeightStandardDeviation.isSelected () ? (float) Check.doubleValue (HStandardDeviation.getText ().trim ()) : 0f;
			request.HMin = rHeightMinMax.isSelected () ? (float) Check.doubleValue (HMin.getText ().trim ()) : 0f;
			request.HMax = rHeightMinMax.isSelected () ? (float) Check.doubleValue (HMax.getText ().trim ()) : 0f;

		request.useD = useD.isSelected ();
		request.DMean = rDiamM.isSelected () ? (float) Check.doubleValue (DMean.getText ().trim ()) : 0f;
		request.Dg = rDiamG.isSelected () ? (float) Check.doubleValue (Dg.getText ().trim ()) : 0f;
		request.DDom = rDiamDom.isSelected () ? (float) Check.doubleValue (DDom.getText ().trim ()) : 0f;
			request.DStandardDeviation = rDiamStandardDeviation.isSelected () ? (float) Check.doubleValue (DStandardDeviation.getText ().trim ()) : 0f;
			request.DMin = rDiamMinMax.isSelected () ? (float) Check.doubleValue (DMin.getText ().trim ()) : 0f;
			request.DMax = rDiamMinMax.isSelected () ? (float) Check.doubleValue (DMax.getText ().trim ()) : 0f;

			// added on 19.1.2004
			request.useCrown = useCrown.isSelected ();
			request.crownBaseHeight = useCrown.isSelected () ? (float) Check.doubleValue (crownBaseHeight.getText ().trim ()) : 0f;
			request.crownMaxDiameter = useCrown.isSelected () ? (float) Check.doubleValue (crownMaxDiameter.getText ().trim ()) : 0f;

		// No individual data in Mode 1
		request.numberOfTrees = 0;	// we send no individual tree data
		request.trees = new Vector ();
	}

	/**	Responses to events.
	*/
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (ok)) {
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			escapePressed ();
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}


	/**	Called on Escape. Redefinition of method in AmapDialog.
	*/
	protected void escapePressed () {
		request.setCanceled (true);
		setValidDialog (false);
	}

	// Initializes the dialog's GUI.
	//
	private void createUI () {
		Border etched = BorderFactory.createEtchedBorder ();

		ColumnPanel part1 = new ColumnPanel ();

		part1.add (createTextFieldLine (null, "messageId", messageId = new JTextField (10), ""+request.messageId, 150));
		messageId.setEditable (false);
		part1.add (createTextFieldLine (null, "species", species = new JTextField (10), ""+request.species, 150));

		part1.add (createTextFieldLine (null, "numberOfTreesToBeSimulated", numberOfTreesToBeSimulated = new JTextField (10), ""+request.numberOfTreesToBeSimulated, 150));

		// checkBoxes
		part1.add (createCheckBoxLine ("storeLineTree", storeLineTree = new JCheckBox (), request.storeLineTree));
		part1.add (createCheckBoxLine ("storeMtg", storeMtg = new JCheckBox (), request.storeMtg));
		part1.add (createCheckBoxLine ("storeBranches", storeBranches = new JCheckBox (), request.storeBranches));
		part1.add (createCheckBoxLine ("storeCrownLayers", storeCrownLayers = new JCheckBox (), request.storeCrownLayers));
		part1.add (createCheckBoxLine ("storePolycyclism", storePolycyclism = new JCheckBox (), request.storePolycyclism));

			part1.add (createCheckBoxLine ("storeTrunkShape", storeTrunkShape = new JCheckBox (), request.storeTrunkShape));	// 19.1.2004
			part1.add (createTextFieldLine (null, "initialSimulationAge", initialSimulationAge = new JTextField (10), ""+request.initialSimulationAge, 150));	// 20.1.2004

		part1.add (createTextFieldLine (null, "surface", surface = new JTextField (10), ""+request.surface, 150));
		part1.add (createTextFieldLine (null, "numberOfTreesInStand", numberOfTreesInStand = new JTextField (10), ""+request.numberOfTreesInStand, 150));
		part1.add (createTextFieldLine (null, "basalArea", basalArea = new JTextField (10), ""+request.basalArea, 150));
		part1.add (createTextFieldLine (null, "fertilityHDom", fertilityHDom = new JTextField (10), ""+request.fertilityHDom, 150));
		part1.add (createTextFieldLine (null, "fertilityAge", fertilityAge = new JTextField (10), ""+request.fertilityAge, 150));
		part1.add (createTextFieldLine (null, "coeffAge", coeffAge = new JTextField (10), ""+request.coeffAge, 150));

		// Age
		ColumnPanel sub1 = new ColumnPanel ();
		Border b1 = BorderFactory.createTitledBorder (etched, Translator.swap ("Mode1Dialog.age"));
		sub1.setBorder (b1);
		sub1.add (createCheckBoxLine ("useAge", useAge = new JCheckBox (), request.useAge));

			rAgeM = new JWidthRadioButton (80);
			rAgeG = new JWidthRadioButton (80);
			rAgeDom = new JWidthRadioButton (80);
			ageGroup = new ButtonGroup ();
			ageGroup.add (rAgeM);
			ageGroup.add (rAgeG);
			ageGroup.add (rAgeDom);
			// select "the first significant one"
			if (request.ageDom > 0) {rAgeDom.setSelected (true);}
			if (request.ageg > 0) {rAgeG.setSelected (true);}
			if (request.ageMean > 0) {rAgeM.setSelected (true);}

		sub1.add (concat (Translator.swap ("Shared.target")+" :", 80,
				createTextFieldLine (rAgeM, "ageMean", ageMean = new JTextField (10), ""+request.ageMean, 80)));
		sub1.add (concat ("", 80,
				createTextFieldLine (rAgeG, "ageg", ageg = new JTextField (10), ""+request.ageg, 80)));
		sub1.add (concat ("", 80,
				createTextFieldLine (rAgeDom, "ageDom", ageDom = new JTextField (10), ""+request.ageDom, 80)));

			// fc - 23.1.2004
			rAgeStandardDeviation = new JWidthRadioButton (Translator.swap ("Mode1Dialog.ageStandardDeviation")+" :", 65);
			rAgeMinMax = new JWidthRadioButton (Translator.swap ("Mode1Dialog.ageMin")+" :", 65);
			ageDispersionGroup = new ButtonGroup ();
			ageDispersionGroup.add (rAgeStandardDeviation);
			ageDispersionGroup.add (rAgeMinMax);
			rAgeStandardDeviation.setSelected (true);

			LinePanel aux5 = new LinePanel ();
			ageStandardDeviation = new JTextField (10);
			ageStandardDeviation.setText (""+request.ageStandardDeviation);
			aux5.add (new JWidthLabel (Translator.swap ("Shared.dispersion")+" :", 80));
			LinePanel aux5a = new LinePanel ();
			aux5a.add (rAgeStandardDeviation);
			aux5a.add (ageStandardDeviation);
			aux5a.addStrut0 ();
			aux5.add (aux5a);
			aux5.addStrut0 ();
			sub1.add (aux5);

			LinePanel aux6 = new LinePanel ();
			ageMin = new JTextField (5);
			ageMin.setText (""+request.ageMin);
			ageMax = new JTextField (5);
			ageMax.setText (""+request.ageMax);
			aux6.add (new JWidthLabel (" ", 80));	// filler
			LinePanel aux6a = new LinePanel ();
			aux6a.add (rAgeMinMax);
			aux6a.add (ageMin);
			aux6a.add (new JWidthLabel (Translator.swap ("Mode1Dialog.ageMax")+" :", 50));
			aux6a.add (ageMax);
			aux6a.addStrut0 ();
			aux6.add (aux6a);
			aux6.addStrut0 ();
			sub1.add (aux6);

/*		sub1.add (concat (Translator.swap ("Shared.dispersion")+" :", 80,
				createTextFieldLine (null, "ageStandardDeviation", ageStandardDeviation = new JTextField (10), ""+request.ageStandardDeviation, 80)));
		LinePanel aux1 = new LinePanel (0, 0);
		aux1.add (createTextFieldLine (null, "ageMin", ageMin = new JTextField (10), ""+request.ageMin, 80));
		aux1.add (createTextFieldLine (null, "ageMax", ageMax = new JTextField (10), ""+request.ageMax, 50));
		sub1.add (concat ("", 80, aux1));*/

		sub1.addStrut0 ();
		part1.add (sub1);

		// H
		ColumnPanel sub2 = new ColumnPanel ();
		Border b2 = BorderFactory.createTitledBorder (etched, Translator.swap ("Mode1Dialog.H"));
		sub2.setBorder (b2);
		sub2.add (createCheckBoxLine ("useH", useH = new JCheckBox (), request.useH));

			rHeightM = new JWidthRadioButton (80);
			rHeightG = new JWidthRadioButton (80);
			rHeightDom = new JWidthRadioButton (80);
			heightGroup = new ButtonGroup ();
			heightGroup.add (rHeightM);
			heightGroup.add (rHeightG);
			heightGroup.add (rHeightDom);
			// select "the first significant one"
			if (request.HDom > 0) {rHeightDom.setSelected (true);}
			if (request.Hg > 0) {rHeightG.setSelected (true);}
			if (request.HMean > 0) {rHeightM.setSelected (true);}

		sub2.add (concat (Translator.swap ("Shared.target")+" :", 80,
				createTextFieldLine (rHeightM, "HMean", HMean = new JTextField (10), ""+request.HMean, 80)));
		sub2.add (concat ("", 80,
				createTextFieldLine (rHeightG, "Hg", Hg = new JTextField (10), ""+request.Hg, 80)));
		sub2.add (concat ("", 80,
				createTextFieldLine (rHeightDom, "HDom", HDom = new JTextField (10), ""+request.HDom, 80)));

			// fc - 23.1.2004
			rHeightStandardDeviation = new JWidthRadioButton (Translator.swap ("Mode1Dialog.HStandardDeviation")+" :", 65);
			rHeightMinMax = new JWidthRadioButton (Translator.swap ("Mode1Dialog.HMin")+" :", 65);
			heightDispersionGroup = new ButtonGroup ();
			heightDispersionGroup.add (rHeightStandardDeviation);
			heightDispersionGroup.add (rHeightMinMax);
			rHeightStandardDeviation.setSelected (true);

			LinePanel aux7 = new LinePanel ();
			HStandardDeviation = new JTextField (10);
			HStandardDeviation.setText (""+request.HStandardDeviation);
			aux7.add (new JWidthLabel (Translator.swap ("Shared.dispersion")+" :", 80));
			LinePanel aux7a = new LinePanel ();
			aux7a.add (rHeightStandardDeviation);
			aux7a.add (HStandardDeviation);
			aux7a.addStrut0 ();
			aux7.add (aux7a);
			aux7.addStrut0 ();
			sub2.add (aux7);

			LinePanel aux8 = new LinePanel ();
			HMin = new JTextField (5);
			HMin.setText (""+request.HMin);
			HMax = new JTextField (5);
			HMax.setText (""+request.HMax);
			aux8.add (new JWidthLabel (" ", 80));	// filler
			LinePanel aux8a = new LinePanel ();
			aux8a.add (rHeightMinMax);
			aux8a.add (HMin);
			aux8a.add (new JWidthLabel (Translator.swap ("Mode1Dialog.HMax")+" :", 50));
			aux8a.add (HMax);
			aux8a.addStrut0 ();
			aux8.add (aux8a);
			aux8.addStrut0 ();
			sub2.add (aux8);

/*		sub2.add (concat (Translator.swap ("Shared.dispersion")+" :", 80,
				createTextFieldLine (null, "HStandardDeviation", HStandardDeviation = new JTextField (10), ""+request.HStandardDeviation, 80)));
		LinePanel aux2 = new LinePanel (0, 0);
		aux2.add (createTextFieldLine (null, "HMin", HMin = new JTextField (10), ""+request.HMin, 80));
		aux2.add (createTextFieldLine (null, "HMax", HMax = new JTextField (10), ""+request.HMax, 50));
		sub2.add (concat ("", 80, aux2));*/

		sub2.addStrut0 ();
		part1.add (sub2);

		// D
		ColumnPanel sub3 = new ColumnPanel ();
		Border b3 = BorderFactory.createTitledBorder (etched, Translator.swap ("Mode1Dialog.D"));
		sub3.setBorder (b3);
		sub3.add (createCheckBoxLine ("useD", useD = new JCheckBox (), request.useD));

			rDiamM = new JWidthRadioButton (80);
			rDiamG = new JWidthRadioButton (80);
			rDiamDom = new JWidthRadioButton (80);
			diamGroup = new ButtonGroup ();
			diamGroup.add (rDiamM);
			diamGroup.add (rDiamG);
			diamGroup.add (rDiamDom);
			// select "the first significant one"
			if (request.DDom > 0) {rDiamDom.setSelected (true);}
			if (request.Dg > 0) {rDiamG.setSelected (true);}
			if (request.DMean > 0) {rDiamM.setSelected (true);}

		sub3.add (concat (Translator.swap ("Shared.target")+" :", 80,
				createTextFieldLine (rDiamM, "DMean", DMean = new JTextField (10), ""+request.DMean, 80)));
		sub3.add (concat ("", 80,
				createTextFieldLine (rDiamG, "Dg", Dg = new JTextField (10), ""+request.Dg, 80)));
		sub3.add (concat ("", 80,
				createTextFieldLine (rDiamDom, "DDom", DDom = new JTextField (10), ""+request.DDom, 80)));


			// fc - 23.1.2004
			rDiamStandardDeviation = new JWidthRadioButton (Translator.swap ("Mode1Dialog.DStandardDeviation")+" :", 65);
			rDiamMinMax = new JWidthRadioButton (Translator.swap ("Mode1Dialog.DMin")+" :", 65);
			diamDispersionGroup = new ButtonGroup ();
			diamDispersionGroup.add (rDiamStandardDeviation);
			diamDispersionGroup.add (rDiamMinMax);
			rDiamStandardDeviation.setSelected (true);

			LinePanel aux9 = new LinePanel ();
			DStandardDeviation = new JTextField (10);
			DStandardDeviation.setText (""+request.DStandardDeviation);
			aux9.add (new JWidthLabel (Translator.swap ("Shared.dispersion")+" :", 80));
			LinePanel aux9a = new LinePanel ();
			aux9a.add (rDiamStandardDeviation);
			aux9a.add (DStandardDeviation);
			aux9a.addStrut0 ();
			aux9.add (aux9a);
			aux9.addStrut0 ();
			sub3.add (aux9);

			LinePanel aux10 = new LinePanel ();
			DMin = new JTextField (5);
			DMin.setText (""+request.DMin);
			DMax = new JTextField (5);
			DMax.setText (""+request.DMax);
			aux10.add (new JWidthLabel (" ", 80));	// filler
			LinePanel aux10a = new LinePanel ();
			aux10a.add (rDiamMinMax);
			aux10a.add (DMin);
			aux10a.add (new JWidthLabel (Translator.swap ("Mode1Dialog.DMax")+" :", 50));
			aux10a.add (DMax);
			aux10a.addStrut0 ();
			aux10.add (aux10a);
			aux10.addStrut0 ();
			sub3.add (aux10);



/*		sub3.add (concat (Translator.swap ("Shared.dispersion")+" :", 80,
				createTextFieldLine (null, "DStandardDeviation", DStandardDeviation = new JTextField (10), ""+request.DStandardDeviation, 80)));
		LinePanel aux3 = new LinePanel (0, 0);
		aux3.add (createTextFieldLine (null, "DMin", DMin = new JTextField (10), ""+request.DMin, 80));
		aux3.add (createTextFieldLine (null, "DMax", DMax = new JTextField (10), ""+request.DMax, 50));
		sub3.add (concat ("", 80, aux3));*/

		sub3.addStrut0 ();
		part1.add (sub3);

			// Crown - added on 19.1.204
			ColumnPanel sub4 = new ColumnPanel ();
			Border b4 = BorderFactory.createTitledBorder (etched, Translator.swap ("Mode1Dialog.crown"));
			sub4.setBorder (b4);
			sub4.add (createCheckBoxLine ("useCrown", useCrown = new JCheckBox (), request.useCrown));
			sub4.add (createTextFieldLine (null, "crownBaseHeight", crownBaseHeight = new JTextField (10), ""+request.crownBaseHeight, 80));
			sub4.add (createTextFieldLine (null, "crownMaxDiameter", crownMaxDiameter = new JTextField (10), ""+request.crownMaxDiameter, 50));
			
useCrown.setEnabled (false);			// Jeff - 6.2.2004 - unused by AMAPsim server
crownBaseHeight.setEnabled (false);		// Jeff - 6.2.2004 - unused by AMAPsim server
crownMaxDiameter.setEnabled (false);	// Jeff - 6.2.2004 - unused by AMAPsim server
			
			sub4.addStrut0 ();
			part1.add (sub4);

		part1.addGlue ();

		JScrollPane pane1 = new JScrollPane (part1);
		pane1.setPreferredSize (new Dimension (500, 400));


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

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (pane1, BorderLayout.CENTER);
		getContentPane ().add (pControl, BorderLayout.SOUTH);

		// sets ok as default (see AmapDialog)
		ok.setDefaultCapable (true);
		getRootPane ().setDefaultButton (ok);

		setTitle (Translator.swap ("Mode1Dialog"));
	}

	// returns one line width str followed by somePanel
	//
	private LinePanel concat (String str, int approxWidth, JPanel somePanel) {
		LinePanel p = new LinePanel ();
		p.add (new JWidthLabel (str, approxWidth));
		p.add (somePanel);
		p.addStrut0 ();
		return p;
	}

	// ex : part1.add (createTextFieldLine ("species", species = new JTextField (10)));
	//
	private LinePanel createTextFieldLine (JRadioButton radio, 					// radio is optional (null permited)
			String code, JTextField textField, String value, int approxWidth) {
		LinePanel l1 = new LinePanel ();
		if (radio != null) {
			radio.setText (Translator.swap ("Mode1Dialog."+code)+" :");
			l1.add (radio);
		} else {
			l1.add (new JWidthLabel (Translator.swap ("Mode1Dialog."+code)+" :", approxWidth));
		}

		textField.setText (value);
		l1.add (textField);
		l1.addStrut0 ();
		return l1;
	}

	//
	//
	private LinePanel createCheckBoxLine (String code, JCheckBox checkBox, boolean selected) {
		LinePanel l1 = new LinePanel ();
		checkBox.setText (Translator.swap ("Mode1Dialog."+code));
		checkBox.setSelected (selected);	// fc - 13.10.2003
		l1.add (checkBox);
		l1.addGlue ();
		return l1;
	}

}



