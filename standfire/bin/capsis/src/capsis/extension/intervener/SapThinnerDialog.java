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

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Translator;
import capsis.util.SmartFlowLayout;


/**
 * A dialog for the SapThinner.
 * 
 * @author F. de Coligny - april 2001
 */
public class SapThinnerDialog extends AmapDialog implements ActionListener {

	private JTextField freqZone;

	protected JButton ok;
	protected JButton cancel;
	protected JButton help;

	public int getFrequency () {return Check.intValue (freqZone.getText ().trim ());}



	public SapThinnerDialog () {
		super ();
		createUI ();
		setTitle (Translator.swap ("SapThinnerDialog"));


		setModal (true);

		// location is set by AmapDialog
		pack ();	// uses component's preferredSize
		show ();

	}

	/** 
	 * May be redefined in subclasses.
	 */
	public void okAction () {

		// checks...

		// 1. frequency
		if (freqZone.getText ().length () == 0) {
			MessageDialog.print (this, Translator.swap ("SapThinnerDialog.freqMustNotBeEmpty"));
			return;
		}

		if (!Check.isInt (freqZone.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("SapThinnerDialog.freqMustBeAnInteger"));
			return;
		}

		if (Check.intValue (freqZone.getText ().trim ()) <= 1) {
			MessageDialog.print (this, Translator.swap ("SapThinnerDialog.freqMustBeGreaterThanOne"));
			return;
		}

		setValidDialog (true);
	}

	/** 
	 * May be redefined in subclasses.
	 */
	public void cancelAction () {setValidDialog (false);}

	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (ok)) {
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			cancelAction ();
		} else if (evt.getSource ().equals (help)) {
			// nothing yet
		}
	}
	private void createUI () {

		// 1. util panel
		JPanel panel = new JPanel ();
		panel.setLayout (new BoxLayout (panel, BoxLayout.Y_AXIS));

		JPanel l1 = new JPanel (new SmartFlowLayout (FlowLayout.LEFT));
		l1.add (new JWidthLabel (Translator.swap ("SapThinner.frequency")+" :", 100));
		freqZone = new JTextField (5);
		l1.add (freqZone);
		panel.add (l1);


		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (panel, BorderLayout.NORTH);

		// 2. control panel (ok cancel help);
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
		getContentPane ().add (pControl, "South");

		// sets ok as default (see AmapDialog)
		ok.setDefaultCapable (true);
		getRootPane ().setDefaultButton (ok);


	}
	
}

