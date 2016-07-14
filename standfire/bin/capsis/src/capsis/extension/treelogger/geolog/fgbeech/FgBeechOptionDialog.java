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

package capsis.extension.treelogger.geolog.fgbeech;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import capsis.extension.treelogger.geolog.util.DiaUtil;
import capsis.extension.treelogger.geolog.util.LogPriceModel;

/**	FgBeechOptionDialog : dialog box for FgBeech options
*
*	@author F. Mothe - august 2008
*/

public class FgBeechOptionDialog extends AmapDialog
		implements ActionListener {

	private static final long serialVersionUID = 20080901L;	// avoid java warning

	public static class Starter {
		public Vector <LogPriceModel> models;
		public Starter () {
			setDefaultValues ();
		}
		public void setDefaultValues () {
			models = new Vector <LogPriceModel> ();
			/*
			models.add (new LogPriceModel ( -143.5791565,	12.530968078));
			models.add (new LogPriceModel (-115.64215,	8.4330081901));
			models.add (new LogPriceModel (-43.63774817,	4.407878472));
			models.add (new LogPriceModel ( 10.866264144,	0.845996492));
			models.add (new LogPriceModel (1.5,	0));
			*/
		}
	}

	private Starter starter;
	private Vector <JTextField> slopes;
	private Vector <JTextField> intercepts;

	private JButton reset;
	private JButton ok;
	private JButton cancel;
	private JButton help;

	/**	Default constructor.
	*/
	public FgBeechOptionDialog (Starter starter) {
		super ();
		// System.out.println ("FgBeechOptionDialog.FgBeechOptionDialog ()");
		initialise (starter);
	}

	/**	Constructor for testing the dialog.
	*/
	public FgBeechOptionDialog (JFrame frame, Starter starter) {
		super (frame);
		initialise (starter);
	}

	private void initialise (Starter starter) {
		this.starter = starter;
		this.slopes = new Vector <JTextField> ();
		this.intercepts = new Vector <JTextField> ();

		createUI ();

		// location is set by GDialog
		pack ();	// validate (); does not work for this JDialog...
		setVisible (true);
	}

	private void okAction () {
		try {
			double [] slopes_ = new double [starter.models.size ()];
			double [] intercepts_ = new double [starter.models.size ()];

			for (int m = 0; m<starter.models.size (); ++m) {
				slopes_ [m] = DiaUtil.checkedDoubleValue (slopes.get (m).getText (),
						"FgBeechOptionDialog.slope", true, false, false);
				intercepts_ [m] = DiaUtil.checkedDoubleValue (intercepts.get (m).getText (),
						"FgBeechOptionDialog.intercepts", true, false, false);
			}
			// Here, all checks are ok: update the starter and set valid
			for (int m = 0; m<starter.models.size (); ++m) {
				starter.models.get (m).slope = slopes_ [m];
				starter.models.get (m).intercept = intercepts_ [m];
			}
			setValidDialog (true);

		} catch (DiaUtil.CheckException e) {
			System.out.println ("FgBeechOptionDialog : okAction () Exception" + e);
		}

	}

	private void resetAction () {
		starter.setDefaultValues ();
		for (int m = 0; m<starter.models.size (); ++m) {
			slopes.get (m).setText ("" + starter.models.get (m).slope);
			intercepts.get (m).setText ("" + starter.models.get (m).intercept);
		}
	}

	//	ActionListener:
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (ok)) {
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (reset)) {
			resetAction ();
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	// Initialize the dialog's GUI.
	//
	private void createUI () {

		Box c1 = Box.createVerticalBox ();
		Box c2 = Box.createVerticalBox ();
		Box c3 = Box.createVerticalBox ();

		/*
		for (int m = 0; m<starter.models.size (); ++m) {
			slopes.add (new JTextField ());
			intercepts.add (new JTextField ());
			c1.add (DiaUtil.newTextAlone ("FgBeechOptionDialog.model" + m,
				true, 50));
			c2.add (DiaUtil.newTextComponent ("FgBeechOptionDialog.slope", 50,
				slopes.get (m), 4, starter.models.get (m).slope));
			c3.add (DiaUtil.newTextComponent ("FgBeechOptionDialog.intercept", 50,
				intercepts.get (m), 4, starter.models.get (m).intercept));
		}
		*/

		Box panUp = Box.createHorizontalBox ();

		panUp.add (c1);
		panUp.add (c2);
		panUp.add (c3);

		panUp.add (new JLabel ("En construction..."));

		//	Reset ok cancel help buttons
		JPanel panControl = new JPanel (new FlowLayout (FlowLayout.RIGHT));
		ok = new JButton (Translator.swap ("Shared.ok"));
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		reset = new JButton (Translator.swap ("Shared.reset"));
		help = new JButton (Translator.swap ("Shared.help"));
		panControl.add (ok);
		panControl.add (cancel);
		panControl.add (reset);
		panControl.add (help);
		ok.addActionListener (this);
		cancel.addActionListener (this);
		reset.addActionListener (this);
		help.addActionListener (this);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (panUp, BorderLayout.CENTER);
		getContentPane ().add (panControl, BorderLayout.SOUTH);

		// sets ok as default (see GDialog)
		ok.setDefaultCapable (true);
		getRootPane ().setDefaultButton (ok);

		setTitle (Translator.swap ("FgBeechOptionDialog.title"));
		//
		setModal (true);

	}

}
