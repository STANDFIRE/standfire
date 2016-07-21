/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2001  Philippe Dreyfus
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

package capsis.extension.intervener;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;

import jeeb.lib.util.Check;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import capsis.defaulttype.TreeList;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.lib.fire.FiModel;
import capsis.lib.fire.intervener.FiIntervenerWithRetentionDialog;
import capsis.util.SmartFlowLayout;

//import fiesta.model.*;
//import nrg.model.*;
//import fireparadox.model.*;

/**
 * NrgThinner2Dialog - Dialog box to input the limit parameters for NrgThinner2
 *
 * @author Ph. Dreyfus - February 2008
 */
public class NrgThinner2Dialog extends FiIntervenerWithRetentionDialog implements ActionListener {

	private JTextField fldMinDist;
	private double minDist;
	
	private JButton ok;
	private JButton cancel;
	private JButton help;

	private ButtonGroup rdStemOrCrown;
	private JRadioButton rdStem;
	private JRadioButton rdCrown;
	private int distCriterion;

	private JCheckBox ckKeepBigTrees;
	
	private ButtonGroup thinningStrategy;
	private JRadioButton randomWalk;
	private JRadioButton keepBigTrees2;
	private JRadioButton foresterLike;
	private JTextField martellingDistance;
	private double martellingDist;
	private JRadioButton simulatedAnnealing;
	private JRadioButton optimal;
	private int thinningCriterion;

	// private boolean KeepBigTrees;

	private GScene stand;
	private GModel model;

	protected MethodProvider mp;
	
// ------------------------------------------------------------------------------------------------------------------------------------->
//               Fonction de lancement du dialogue																		>
// ------------------------------------------------------------------------------------------------------------------------------------->
	public NrgThinner2Dialog (GScene s, GModel m) {
		super ();

		stand = s;
		model = m;	
		
		int treeNumber = ((TreeList) stand).getTrees().size();
		// 7 trees in a martelling cell...
		martellingDist = Math.sqrt(7d / ((TreeList) stand).getTrees().size()
				* s.getArea());
		martellingDist = Math.min(martellingDist, 15d);
		martellingDist = Math.max(martellingDist, 3d);
		martellingDist = (int) martellingDist;
		createUI ();
		// location is set by AmapDialog
		pack ();
		show ();
	}

	
	/**	Called when ok is hit
	 */
	private void okAction () {
		// Retrieve method provider
//		mp = model.getMethodProvider ();
//		TreeList gtcstand = (TreeList) stand;
//		Collection trees = gtcstand.getTrees ();

		if (!Check.isDouble (fldMinDist.getText ().trim ()) ) {
			MessageDialog.print (this, Translator.swap ("NrgThinner2Dialog.minDistMustBeANumber"));
			return;
		}
		
		if (!Check.isDouble (martellingDistance.getText().trim())) {
			MessageDialog.print(this, Translator.swap ("NrgThinner2Dialog.martellingDistanceMustBeAPositiveNumber"));
			return;
		}
		double _martellingDistance = Check.doubleValue (martellingDistance.getText().trim());
		if (_martellingDistance <= 0d) {
			MessageDialog.print(this, Translator.swap ("NrgThinner2Dialog.martellingDistanceMustBeAPositiveNumber"));
			return;
		}
		
		// All checks correct: update the values
		minDist = Check.doubleValue (fldMinDist.getText ());
		martellingDist = _martellingDistance;
		if (model instanceof FiModel) {
			if (!okActionFuelRetention()) return;
		}
		setValidDialog (true);

	}

	public void actionPerformed (ActionEvent evt) {
		
		if (rdStem.isSelected()) {distCriterion = 0;}
		
		if (rdCrown.isSelected()) {distCriterion = 1;}
		
		// KeepBigTrees = (ckKeepBigTrees.isSelected()) ? true : false;
		
		if (randomWalk.isSelected()) {
			thinningCriterion = 0;
		} else if (keepBigTrees2.isSelected()) {
			thinningCriterion = 1;
		} else if (foresterLike.isSelected()) {
			thinningCriterion = 2;
		} else if (simulatedAnnealing.isSelected()) {
			thinningCriterion = 3;
		} else if (optimal.isSelected()) {
			thinningCriterion = 4;
		}
		actionPerformedFuelRetention(evt);
		if (evt.getSource ().equals (ok)) {
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor(this);
		}
		
	}

	/**
	 * Initializes the GUI.
	 */
	private void createUI () {

		Border etched = BorderFactory.createEtchedBorder ();

		JPanel MinDist_StemOrCrown = new JPanel ();
			MinDist_StemOrCrown.setLayout (new FlowLayout (FlowLayout.CENTER));
			Border b1 = BorderFactory.createTitledBorder (etched, Translator.swap ("NrgThinner2Dialog.chooseDistance"));
			MinDist_StemOrCrown.setBorder (b1);

			MinDist_StemOrCrown.add(new JWidthLabel (Translator.swap ("NrgThinner2Dialog.minDist"), 50));
			fldMinDist = new JTextField (8);
			MinDist_StemOrCrown.add (fldMinDist);
			fldMinDist.setText ("1");							// provisional

			JPanel l1 = new JPanel (new SmartFlowLayout (FlowLayout.LEFT));
			JPanel l2 = new JPanel (new SmartFlowLayout (FlowLayout.LEFT));

			rdStem = new JRadioButton (Translator.swap ("NrgThinner2Dialog.rdStem"));
			rdCrown = new JRadioButton (Translator.swap ("NrgThinner2Dialog.rdCrown"));

			rdStemOrCrown = new ButtonGroup ();
			rdStemOrCrown.add (rdStem);
			rdStemOrCrown.add (rdCrown);

			rdStemOrCrown.setSelected (rdStem.getModel (), true);

			l1.add (rdStem);
			l2.add (rdCrown);

			MinDist_StemOrCrown.add (l1);
			MinDist_StemOrCrown.add (l2);

		// JPanel KBT = new JPanel (new SmartFlowLayout (FlowLayout.LEFT));

		// 3 lines below to remove
		// ckKeepBigTrees = new JCheckBox(Translator.swap
		// ("NrgThinner2Dialog.ckKeepBigTrees"));
		// ckKeepBigTrees.setSelected(true);

		// KBT.add (ckKeepBigTrees);
			
		JPanel l3 = new JPanel(new SmartFlowLayout(FlowLayout.LEFT));
		JPanel l4 = new JPanel(new SmartFlowLayout(FlowLayout.LEFT));
		JPanel l5 = new JPanel(new SmartFlowLayout(FlowLayout.LEFT));
		JPanel l6 = new JPanel(new SmartFlowLayout(FlowLayout.LEFT));
		JPanel l7 = new JPanel(new SmartFlowLayout(FlowLayout.LEFT));

		randomWalk = new JRadioButton(Translator
				.swap("NrgThinner2Dialog.randomWalk"));
		keepBigTrees2 = new JRadioButton(Translator
				.swap("NrgThinner2Dialog.keepBigTrees"));
		foresterLike = new JRadioButton(Translator
				.swap("NrgThinner2Dialog.foresterLike"));
		simulatedAnnealing = new JRadioButton(Translator
				.swap("NrgThinner2Dialog.simulatedAnnealing"));
		optimal = new JRadioButton(Translator.swap("NrgThinner2Dialog.optimal"));

		thinningStrategy = new ButtonGroup();
		thinningStrategy.add(randomWalk);
		thinningStrategy.add(keepBigTrees2);
		thinningStrategy.add(foresterLike);
		thinningStrategy.add(simulatedAnnealing);
		thinningStrategy.add(optimal);

		thinningStrategy.setSelected(foresterLike.getModel(), true);

		l3.add(randomWalk);
		l4.add(keepBigTrees2);
		l5.add(foresterLike);
		l5.add(new JWidthLabel(Translator
				.swap("Nrg2Thinner2Dialog.martellingDistance")
				+ " : ", 200));
		martellingDistance = new JTextField(5);
		martellingDistance.setText("" + martellingDist);
		martellingDistance.setEditable(true);
		// martellingDistance.addActionListener(this);
		l5.add(martellingDistance);
		
		l6.add(simulatedAnnealing);
		l7.add(optimal);

				
		
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

		Box part1 = Box.createVerticalBox ();
		part1.add (MinDist_StemOrCrown);

		Box part2 = Box.createVerticalBox ();
		part2.add (new JLabel (Translator.swap ("NrgThinner2Dialog.ifCrownMinDistCanBeNegative")));
		part2.add (new JLabel ("________________________________"));
		// part2.add (KBT);

		part2.add(l3);
		part2.add(l4);
		part2.add(l5);
		part2.add(l6);
		part2.add(l7);
		if (model instanceof FiModel) {
			part2.add(this.getFuelRetentionPanel());
		}
		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (part1, BorderLayout.NORTH);
		getContentPane ().add (part2, BorderLayout.CENTER);
		getContentPane ().add (pControl, BorderLayout.SOUTH);

		setTitle (Translator.swap ("NrgThinner2Dialog.thinningParameters"));
		
		setModal (true);
	}
	
	public int getDistCriterion () {return distCriterion;}
	
	public double getMinDist () {return minDist;}

	// public boolean getKeepBigTrees () { return KeepBigTrees; }
	
	public int getThinningCriterion() {return thinningCriterion;}

	public double getMartellingDistance() {return martellingDist;}


}

