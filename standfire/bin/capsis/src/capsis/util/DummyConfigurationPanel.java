/* 
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2000-2014  Francois de Coligny
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

package capsis.util;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.LinePanel;

/**
 * A dummy configuration panel, e.g. for filters without configuration needed.
 * 
 * @author F. de Coligny - January 2014
 */
public class DummyConfigurationPanel extends ConfigurationPanel 
		implements ActionListener, Pilotable {
	
	private JTextField message;
	
	
	/**	Constructor
	*/
	public DummyConfigurationPanel (Configurable c, String userMessage) {
		super (c);
		
//		filter = (FBiggestTrees) c;
		
		setLayout (new BorderLayout ());
		
		ColumnPanel master = new ColumnPanel ();
		
		// line 0
		LinePanel l0 = new LinePanel ();
		JLabel lab1 = new JLabel (userMessage);
		l0.add (lab1);
		l0.addGlue ();
		
		master.add (l0);
		
		
		master.addGlue ();
		
		add (master, BorderLayout.NORTH);
		
	}

	/**	Events processing
	*/
	public void actionPerformed (ActionEvent e) {
	}

	/**	From Pilotable interface
	*/
	public JComponent getPilot () {
		
//		ImageIcon icon = IconLoader.getIcon ("help_16.png");
//		helpButton = new JButton (icon);
//		Tools.setSizeExactly (helpButton, BUTTON_SIZE, BUTTON_SIZE);
//		helpButton.setToolTipText (Translator.swap ("Shared.help"));
//		helpButton.addActionListener (this);
		
		JToolBar toolbar = new JToolBar ();
//		toolbar.add (helpButton);
		toolbar.setVisible (true);
		
		return toolbar;
	}
	
	/**	From ConfigurationPanel
	*/
	public boolean checksAreOk () {
		
//		String name = ExtensionManager.getName(filter.getClass().getName());
//		
//		if (Check.isEmpty (number.getText ()) || !Check.isInt (number.getText ())) {
//			JOptionPane.showMessageDialog (this, 
//				Translator.swap ("FBiggestTrees.numberMustBeAnInteger"),
//				name ,
//				JOptionPane.WARNING_MESSAGE );
//			return false;
//		}
//		
//		int n = Check.intValue (number.getText ());
//		if (n < 1) {
//			JOptionPane.showMessageDialog (this, 
//				Translator.swap ("FBiggestTrees.numberMustBeGreaterThanOne"),
//				name,
//				JOptionPane.WARNING_MESSAGE );
//			return false;
//		}
		
		return true;
	}

	
	/**	Write the total number of trees as information
	*/
	private void preset () {
		
//		int n = filter.candidates.size ();
//		number.setText (""+n);
		
	}
	
//	/**	Return number of biggest trees
//	*/
//	protected int getNumber () {
//		return Check.intValue (number.getText ());
//	}

	
}
	
