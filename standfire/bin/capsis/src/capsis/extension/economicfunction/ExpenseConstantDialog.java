/* 
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2000-2001  Francois de Coligny
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
package capsis.extension.economicfunction;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;

/**
 * This dialog box is used to set ExpenseConstant parameters in interactive context.
 * 
 * @author C. Orazio - january 2003
 */
public class ExpenseConstantDialog extends AmapDialog implements ActionListener {

	private JTextField value;
	
	protected JButton ok;
	protected JButton cancel;
	protected JButton help;


	public ExpenseConstantDialog () {
		super ();
		createUI ();
		setTitle (Translator.swap ("ExpenseConstantDialog"));
		
		
		setModal (true);
		
		// location is set by AmapDialog
		pack ();	// uses component's preferredSize
		show ();
		
	}
	
	/**
	 * 
	 */
	public double getValue () {
		return Check.doubleValue (value.getText ().trim ());
	}

	// 
	// Action on ok button.
	// 
	private void okAction () {
		
		// Checks...
		if (!Check.isDouble (value.getText ().trim ())) {
			MessageDialog.print (this, Translator.swap ("ExpenseConstantDialog.valueMustBeDouble"));
			return;
		}
		
		setValidDialog (true);
	}

	// 
	// Action on cancel button.
	// 
	private void cancelAction () {setValidDialog (false);}

	/** 
	 * Someone hit a button.
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (ok)) {
			okAction ();
		} else if (evt.getSource ().equals (cancel)) {
			cancelAction ();
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}
	
	// 
	// Create the dialog box user interface.
	// 
	private void createUI () {
		
		// 1. Util panel
		ColumnPanel panel = new ColumnPanel ();
		
		LinePanel l1 = new LinePanel ();
		l1.add (new JLabel (Translator.swap ("ExpenseConstantDialog.value")+" :"));
		value = new JTextField (5);
		l1.add (value);
		l1.addGlue ();
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
		getContentPane ().add (pControl, BorderLayout.SOUTH);
		
		// sets ok as default (see AmapDialog)
		ok.setDefaultCapable (true);
		getRootPane ().setDefaultButton (ok);
		
	}

}

