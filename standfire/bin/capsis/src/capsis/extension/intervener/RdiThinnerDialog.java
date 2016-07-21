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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import capsis.kernel.GModel;
import capsis.kernel.GScene;

/**
 * RdiThinnerDialog - Dialog box to input the parameters for RdiThinner
 *
 * @author P. Vallet - april 2003
 */
public class RdiThinnerDialog extends AmapDialog implements ActionListener {

	// private GModel model;
	// private GScene stand;

	private JButton ok;
	private JButton help;
	private JButton cancel;

	private JRadioButton rdHigh;
	private JRadioButton rdStandard;

	private JTextField objectiveRdi;
	private JTextField thinningCoef;

	private RdiThinner.Data m_data;


	/**
	 * Constructor
	 */
	public RdiThinnerDialog (GScene s, GModel m) {
		super ();

		// This dialog size should not be saved:
		Settings.setProperty (getClass ().getName () + ".size", "");

		m_data = new RdiThinner.Data ();

		// stand = s;
		// model = m;
		createUI ();
		// location is set by AmapDialog
		pack ();
		show ();
	}

	// Action on ok button
	//
	private void okAction () {
		// 1 - Checks :
		if (Check.isEmpty (objectiveRdi.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("RdiThinnerDialog.rdiIsEmpty"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return;
		} else if ((!Check.isDouble (objectiveRdi.getText ()))
				|| (Check.doubleValue (objectiveRdi.getText ()) < 0.)
				//~ || (Check.doubleValue (objectiveRdi.getText ())<0.05)
				|| (Check.doubleValue (objectiveRdi.getText ()) > 1.)
		) {
			JOptionPane.showMessageDialog (this, Translator.swap ("RdiThinnerDialog.wrongRdiValue"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return;
		}
		if (! Check.isEmpty (thinningCoef.getText ())) {
			if ((! Check.isDouble (thinningCoef.getText ()))
					|| (Check.doubleValue (thinningCoef.getText ()) < -1.)
					|| (Check.doubleValue (thinningCoef.getText ()) > 2.)
			) {
				JOptionPane.showMessageDialog (this, Translator.swap ("RdiThinnerDialog.wrongThinningCoef"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return;
			}
		}

		m_data.objectiveRdi = Check.doubleValue (objectiveRdi.getText ());
		m_data.thinningCoef = Check.isEmpty (thinningCoef.getText ())
				? -1.
				: Check.doubleValue (thinningCoef.getText ());

		if (rdHigh.isSelected ()) {
			m_data.typeOfThinning = "High";}
		else if (rdStandard.isSelected()) {
			m_data.typeOfThinning = "Standard";
		}

		setValidDialog (true);
		return;
	}

	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (ok)) {
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);	// fc - 16.6.2003
		}
	}

	// Initialize the GUI.
	//
	private void createUI () {

		Border etched = BorderFactory.createEtchedBorder ();

		JPanel mainPanel = new JPanel ();
		mainPanel.setLayout (new BoxLayout (mainPanel, BoxLayout.Y_AXIS));
		Border b1 = BorderFactory.createTitledBorder (etched, Translator.swap ("RdiThinnerDialog.thinningParameters") +" : ");
		mainPanel.setBorder (b1);

		JPanel l1a = new JPanel (new FlowLayout (FlowLayout.LEFT));
		l1a.add (new JWidthLabel (Translator.swap ("RdiThinnerDialog.objectiveRdi")+" :", 180));
		objectiveRdi = new JTextField (5);
		if (m_data.objectiveRdi >= 0.) {objectiveRdi.setText ("" + m_data.objectiveRdi);}
		l1a.add (objectiveRdi);

		JPanel l1b = new JPanel (new FlowLayout (FlowLayout.LEFT));
		l1b.add (new JWidthLabel (Translator.swap ("RdiThinnerDialog.thinningCoef")+" :", 180));
		thinningCoef = new JTextField (5);
		if (m_data.thinningCoef >= 0.) {thinningCoef.setText ("" + m_data.thinningCoef);}
		l1b.add (thinningCoef);

		JPanel l2 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		l2.add(new JLabel (Translator.swap ("RdiThinnerDialog.thinningType")+" :"));

		JPanel l3 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		rdHigh = new JRadioButton (Translator.swap ("RdiThinnerDialog.highThinning"));
		rdHigh.addActionListener (this);
		rdStandard = new JRadioButton (Translator.swap ("RdiThinnerDialog.standardThinning"));
		rdStandard.addActionListener (this);

		ButtonGroup rdGroup1 = new ButtonGroup ();
		rdGroup1.add (rdHigh);
		rdGroup1.add (rdStandard);
		l3.add (rdStandard);
		l3.add (new JWidthLabel (55));
		l3.add (rdHigh);
		rdGroup1.setSelected(rdStandard.getModel (), true);

		mainPanel.add (l1a);
		mainPanel.add (l1b);
		mainPanel.add (l2);
		mainPanel.add (l3);

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

		getContentPane ().add (mainPanel);
		getContentPane ().add (pControl, "South");

		setTitle (Translator.swap ("RdiThinnerDialog.title"));

		setModal (true);
	}

	public RdiThinner.Data getData () {return m_data;}
}
