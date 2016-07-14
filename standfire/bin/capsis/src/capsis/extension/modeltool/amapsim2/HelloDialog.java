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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;

/**
 * HelloDialog is a dialog box to enter parameters for Hello request.
 * 
 * @author F. de Coligny - november 2002
 */
public class HelloDialog extends AmapDialog implements ActionListener {

	private JTextField zonSessionName;
	private JCheckBox firstScenario;
	private JButton ok;
	private JButton cancel;
	private JButton help;

	public HelloDialog () {
		super ();
		
		createUI ();
		
		pack ();	// validate (); does not work for this JDialog...
		setVisible (true);
	}

	private void okAction () {
		// checks here
		setValidDialog (true);
	}

	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (ok)) {
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/** 
	 * Initializes the dialog's GUI. 
	 */
	private void createUI () {
		
		LinePanel l1 = new LinePanel ();
		l1.add (new JWidthLabel (Translator.swap ("HelloDialog.sessionName")+" :", 160));
		zonSessionName = new JTextField (15);
		l1.add (zonSessionName);
		l1.addStrut0 ();
		
		LinePanel l2 = new LinePanel ();
		firstScenario = new JCheckBox (Translator.swap ("HelloDialog.createFirstScenario"), true);
		l2.add (firstScenario);
		l2.addGlue ();
		
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
		
		ColumnPanel part1 = new ColumnPanel ();
		part1.add (l1);
		part1.add (l2);
		part1.addGlue ();
		
		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (part1, BorderLayout.NORTH);
		getContentPane ().add (pControl, BorderLayout.SOUTH);
		
		// sets ok as default (see AmapDialog)
		ok.setDefaultCapable (true);
		getRootPane ().setDefaultButton (ok);
		
		setTitle (Translator.swap ("HelloDialog.newSession"));
		
		setModal (true);
	}

	public Object getData () {
		return null;
	}
	
	
}



