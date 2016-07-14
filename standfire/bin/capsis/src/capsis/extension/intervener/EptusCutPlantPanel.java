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

package capsis.extension.intervener;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Translator;

/**
 * This panel is used to set EptusCutPlant parameters in interactive context.
 *
 * @author V. Cucchi - november 2005
 */
public class EptusCutPlantPanel extends JPanel implements ActionListener {

	public static final int MIN_PLANTING_DENSITY = 300;
	public static final int MAX_PLANTING_DENSITY = 2500;

	private ButtonGroup rdGroup1;

	private boolean panelIsEnabled;

	private JRadioButton clearcutting;
	private JRadioButton thinning;
	private JTextField dbhMax;	// NOT USED YET
	//private JCheckBox planting;
	private JComboBox plantingCloneName;
	private JTextField plantingDensity;
	private Set cloneNames;


	/**	Constructor
	*/
	public EptusCutPlantPanel (Set cloneNames) {
		super ();
		this.cloneNames = cloneNames;
		createUI ();
	}

	public boolean isClearcutting () {return clearcutting.isSelected ();}
	public boolean isThinning () {return thinning.isSelected ();}
	//public double getDbhMax () {return Check.doubleValue (dbhMax.getText ().trim ());}
	//public boolean isPlanting () {return planting.isSelected ();}
	public String getPlantingCloneName () {return (String) plantingCloneName.getSelectedItem ();}
	public int getPlantingDensity () {return Check.intValue (plantingDensity.getText ().trim ());}

	//	Controls all user entries for coherence
	//
	public boolean isCorrect () {

		// Checks...
		if (isThinning ()) {
			/*if (!Check.isDouble (dbhMax.getText ().trim ())) {
				MessageDialog.promptError (Translator.swap ("EptusCutPlantPanel.dbhMaxShouldBeDecimal"));
				return false;
			}
			double d = Check.doubleValue (dbhMax.getText ().trim ());
			if (d <= 0) {
				MessageDialog.promptError (Translator.swap ("EptusCutPlantPanel.dbhMaxShouldBeStrictlyPositive"));
				return false;
			}*/
		}

		/*if (isPlanting ()) {
			if (!isClearcutting ()) {
				MessageDialog.promptError (Translator.swap ("EptusCutPlantPanel.plantingAllowedOnlyIfClearcutting"));
				return false;
			}*/
		if (isClearcutting ()) {
			if (!Check.isInt (plantingDensity.getText ().trim ())) {
				JOptionPane.showMessageDialog (this,
						Translator.swap ("EptusCutPlantPanel.plantingDensityShouldBeAnInteger"),
						Translator.swap ("Shared.warning"),
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
			int i = Check.intValue (plantingDensity.getText ().trim ());
			if (i < MIN_PLANTING_DENSITY || i > MAX_PLANTING_DENSITY) {
				JOptionPane.showMessageDialog (this,
						Translator.swap ("EptusCutPlantPanel.plantingDensityShouldBeBetweenMinAndMax"),
						Translator.swap ("Shared.warning"),
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
		}


		return true;
	}

	public void setEnabled (boolean b) {
		panelIsEnabled = b;

		synchronizeOptions ();

	}

	/**	Someone hit a button.
	*/
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (clearcutting)) {
			synchronizeOptions ();
/*		} else if (evt.getSource ().equals (planting)) {
			synchronizeOptions ();*/
		} else if (evt.getSource ().equals (thinning)) {
			synchronizeOptions ();
		}
	}

	// Synchronize gui according to radio buttons
	//
	private void synchronizeOptions () {
		clearcutting.setEnabled (panelIsEnabled);
		thinning.setEnabled (false);

		//planting.setEnabled (panelIsEnabled && clearcutting.isSelected ());
		//plantingCloneName.setEnabled (panelIsEnabled && clearcutting.isSelected () && planting.isSelected ());
		//plantingDensity.setEnabled (panelIsEnabled && clearcutting.isSelected () && planting.isSelected ());
		plantingCloneName.setEnabled (panelIsEnabled && clearcutting.isSelected ());
		plantingDensity.setEnabled (panelIsEnabled && clearcutting.isSelected ());

		//dbhMax.setEnabled (panelIsEnabled && thinning.isSelected ());

	}

	//	Create the dialog box user interface.
	//
	private void createUI () {

		ColumnPanel panel = new ColumnPanel ();

		//
		LinePanel l1 = new LinePanel ();
		LinePanel l2 = new LinePanel ();
		clearcutting = new JRadioButton (Translator.swap ("EptusCutPlantPanel.clearCutting"));
		thinning = new JRadioButton (Translator.swap ("EptusCutPlantPanel.thinning"));
		clearcutting.addActionListener (this);
		thinning.addActionListener (this);

		rdGroup1 = new ButtonGroup ();
		rdGroup1.add (clearcutting);
		rdGroup1.add (thinning);

		rdGroup1.setSelected (clearcutting.getModel (), true);
		thinning.setEnabled (false);	// TEMPORARY

		l1.add (clearcutting);
		l1.addGlue ();
		panel.add (l1);

		/*LinePanel l4 = new LinePanel ();
		l4.add (new JWidthLabel("",30));
		planting = new JCheckBox (Translator.swap ("EptusCutPlantPanel.planting"), false);
		planting.addActionListener (this);
		l4.add (planting);
		l4.addGlue ();
		panel.add (l4);*/

		//
		LinePanel l5 = new LinePanel ();
		l5.add (new JWidthLabel("",30));
		Vector v = new Vector (cloneNames);
		plantingCloneName = new JComboBox (v);
		l5.add (new JLabel (Translator.swap ("EptusCutPlantPanel.plantingCloneName")+" : "));
		l5.add (plantingCloneName);
		l5.addStrut0 ();
		panel.add (l5);

		//
		LinePanel l6 = new LinePanel ();
		l6.add (new JWidthLabel("",30));
		plantingDensity = new JTextField (5);
		l6.add (new JLabel (Translator.swap ("EptusCutPlantPanel.plantingDensity")+" : "));
		l6.add (plantingDensity);
		l6.add (new JLabel ("("));
		l6.add (new JLabel (""+MIN_PLANTING_DENSITY));
		l6.add (new JLabel ("<= v <="));
		l6.add (new JLabel (""+MAX_PLANTING_DENSITY));
		l6.add (new JLabel (")"));
		l6.addStrut0 ();
		panel.add (l6);




		l2.add (thinning);
		l2.addGlue ();
		panel.add (l2);

		//
		LinePanel l3 = new LinePanel ();
		l3.add (new JWidthLabel("",30));
		dbhMax = new JTextField (5);
		l3.add (new JLabel (Translator.swap ("EptusCutPlantPanel.cutTreesWithDbhMoreThanInCm")+" : "));
		l3.add (dbhMax);
		l3.addStrut0 ();
		panel.add (l3);

		synchronizeOptions ();

		//
		setLayout (new BorderLayout ());
		add (panel, BorderLayout.NORTH);

	}

}

