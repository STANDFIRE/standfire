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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;


/**	This dialog box is used to set SardinUnevenThinner parameters
*	in interactive mode.
*	@author B.Courbaud, F. de Coligny - february 2010
*/
public class SardinUnevenThinnerDialog extends AmapDialog implements ActionListener {

	// The thinner we are configuring
	private SardinUnevenThinner thinner;

	private JTextField dHarvest;
	private JTextField dThin;

	private JTextField gCutMax;
	private JTextField gCutStandard;
	private JTextField gCutMin;

	private JTextField harvestRatioMax;
	private JTextField thinRatioMax;

//	private JTextField gStockMin;

	protected JButton ok;
	protected JButton cancel;
	protected JButton help;


	/**	Constructor
	*/
	public SardinUnevenThinnerDialog (SardinUnevenThinner thinner) {
		super ();

		this.thinner = thinner;

		createUI ();
		setTitle (Translator.swap ("SardinUnevenThinnerDialog"));

		setModal (true);

		// location is set by AmapDialog
		pack ();	// uses component's preferredSize
		show ();

	}

	/**	Action on ok button.
	*/
	private void okAction () {

		// Checks...
		if (!Check.isDouble (dHarvest.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("SardinUnevenThinnerDialog.dHarvestShouldBeANumber"));
			return;
		}

		if (!Check.isDouble (dThin.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("SardinUnevenThinnerDialog.dThinShouldBeANumber"));
			return;
		}

		if (!Check.isDouble (gCutMax.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("SardinUnevenThinnerDialog.gCutMaxShouldBeANumber"));
			return;
		}

		if (!Check.isDouble (gCutStandard.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("SardinUnevenThinnerDialog.gCutStandardShouldBeANumber"));
			return;
		}

		if (!Check.isDouble (gCutMin.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("SardinUnevenThinnerDialog.gCutMinShouldBeANumber"));
			return;
		}

		if (!Check.isDouble (harvestRatioMax.getText ().trim ())) {
					MessageDialog.print (this, Translator.swap ("SardinUnevenThinnerDialog.harvestRatioMaxShouldBeANumber"));
					return;
		}

		if (!Check.isDouble (thinRatioMax.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("SardinUnevenThinnerDialog.thinRatioMaxShouldBeANumber"));
			return;
		}
//		if (!Check.isDouble (gStockMin.getText ().trim ())) {
//			MessageDialog.print (this, Translator.swap ("SardinUnevenThinnerDialog.gStockMinShouldBeANumber"));
//			return;
//		}

		double _dHarvest = Check.doubleValue (dHarvest.getText ().trim ());
		double _dThin = Check.doubleValue (dThin.getText ().trim ());

		double _gCutMax = Check.doubleValue (gCutMax.getText ().trim ());
		double _gCutStandard = Check.doubleValue (gCutStandard.getText ().trim ());
		double _gCutMin = Check.doubleValue (gCutMin.getText ().trim ());

		double _harvestRatioMax = Check.doubleValue (harvestRatioMax.getText ().trim ());
		double _thinRatioMax = Check.doubleValue (thinRatioMax.getText ().trim ());

//		double _gStockMin = Check.doubleValue (gStockMin.getText ().trim ());

		if (_gCutStandard > _gCutMax) {
			MessageDialog.print (this, Translator.swap ("SardinUnevenThinnerDialog.gCutSandardMustBeLowerThangCutMax"));
			return;
		}
		if (_gCutMin > _gCutStandard) {
			MessageDialog.print (this, Translator.swap ("SardinUnevenThinnerDialog.gCutMinMustBeLowerThangCutStandard"));
			return;
		}

		setValidDialog (true);
	}

	/**	Process buttons events.
	*/
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (ok)) {
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**	Create the dialog box user interface.
	*/
	private void createUI () {

		// Main panel
		ColumnPanel panel = new ColumnPanel ();

		LinePanel l0 = new LinePanel ();
		l0.add (new JWidthLabel (Translator.swap ("SardinUnevenThinnerDialog.dHarvest")+" : ", 370));
		dHarvest = new JTextField (5);
		dHarvest.setText (""+thinner.dHarvest);
		l0.add (dHarvest);
		panel.add (l0);

		LinePanel l1 = new LinePanel ();
		l1.add (new JWidthLabel (Translator.swap ("SardinUnevenThinnerDialog.dThin")+" : ", 370));
		dThin = new JTextField (5);
		dThin.setText (""+thinner.dThin);
		l1.add (dThin);
		panel.add (l1);

		LinePanel l2 = new LinePanel ();
		l2.add (new JWidthLabel (Translator.swap ("SardinUnevenThinnerDialog.gCutMax")+" : ", 370));
		gCutMax = new JTextField (5);
		gCutMax.setText (""+thinner.gCutMax);
		l2.add (gCutMax);
		panel.add (l2);

		LinePanel l3 = new LinePanel ();
		l3.add (new JWidthLabel (Translator.swap ("SardinUnevenThinnerDialog.gCutStandard")+" : ", 370));
		gCutStandard = new JTextField (5);
		gCutStandard.setText (""+thinner.gCutStandard);
		l3.add (gCutStandard);
		panel.add (l3);

		LinePanel l4 = new LinePanel ();
		l4.add (new JWidthLabel (Translator.swap ("SardinUnevenThinnerDialog.gCutMin")+" : ", 370));
		gCutMin = new JTextField (5);
		gCutMin.setText (""+thinner.gCutMin);
		l4.add (gCutMin);
		panel.add (l4);

//		LinePanel l5 = new LinePanel ();
//		l5.add (new JWidthLabel (Translator.swap ("SardinUnevenThinnerDialog.gStockMin")+" : ", 370));
//		gStockMin = new JTextField (5);
//		gStockMin.setText (""+thinner.gStockMin);
//		l5.add (gStockMin);
//		panel.add (l5);

		LinePanel l6 = new LinePanel ();
		l6.add (new JWidthLabel (Translator.swap ("SardinUnevenThinnerDialog.harvestRatioMax")+" : ", 420));
		harvestRatioMax = new JTextField (5);
		harvestRatioMax.setText (""+thinner.harvestRatioMax);
		l6.add (harvestRatioMax);
		panel.add (l6);

		LinePanel l7 = new LinePanel ();
		l7.add (new JWidthLabel (Translator.swap ("SardinUnevenThinnerDialog.thinRatioMax")+" : ", 420));
		thinRatioMax = new JTextField (5);
		thinRatioMax.setText (""+thinner.thinRatioMax);
		l7.add (thinRatioMax);
		panel.add (l7);


		panel.addGlue ();

		// Control panel (ok, cancel, help)
		LinePanel pControl = new LinePanel ();
		ok = new JButton (Translator.swap ("Shared.ok"));
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		help = new JButton (Translator.swap ("Shared.help"));
		pControl.addGlue ();
		pControl.add (ok);
		pControl.add (cancel);
		pControl.add (help);
		pControl.addStrut0 ();
		ok.addActionListener (this);
		cancel.addActionListener (this);
		help.addActionListener (this);

		// sets ok as default
		setDefaultButton(ok);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (panel, BorderLayout.NORTH);
		getContentPane ().add (pControl, BorderLayout.SOUTH);

	}

	public double get_dHarvest () {return Check.doubleValue (dHarvest.getText ().trim ());}
	public double get_dThin () {return Check.doubleValue (dThin.getText ().trim ());}

	public double get_gCutMax () {return Check.doubleValue (gCutMax.getText ().trim ());}
	public double get_gCutStandard () {return Check.doubleValue (gCutStandard.getText ().trim ());}
	public double get_gCutMin () {return Check.doubleValue (gCutMin.getText ().trim ());}

	public double get_harvestRatioMax () {return Check.doubleValue (harvestRatioMax.getText ().trim ());}
	public double get_thinRatioMax () {return Check.doubleValue (thinRatioMax.getText ().trim ());}

//	public double get_gStockMin () {return Check.doubleValue (gStockMin.getText ().trim ());}

}

