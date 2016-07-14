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

package capsis.extension.filter.gfish;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.IconLoader;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import capsis.commongui.util.Tools;
import capsis.util.Configurable;
import capsis.util.ConfigurationPanel;
import capsis.util.Group;
import capsis.util.GrouperManager;
import capsis.util.Pilotable;

/**
 * Configuration panel for FishInReachGroup.
 * 
 * @author J. Labonne - may 2005
 */
public class FishInReachGroupDialog extends ConfigurationPanel implements ActionListener, Pilotable {
	private static final int BUTTON_SIZE = 23;
	
	private FishInReachGroup mum;		// mummy is being configured
	
	private JButton helpButton;
	
	protected JComboBox grouperNames;
	//~ protected JTextField lowValue;
	//~ protected JTextField highValue;
	//~ protected Map humanKey_grouperNames;

	//~ private NumberFormat formater;
	
	
	/**	Constructor
	*/
	protected FishInReachGroupDialog (Configurable c) {
		super (c);
		
		mum = (FishInReachGroup) c;
		
		setLayout (new BorderLayout ());
		
		ColumnPanel master = new ColumnPanel ();
		
		Collection collection = GrouperManager.getInstance ().getGrouperNames ((String) Group.REACH);
//~ System.out.println ("FIRG > grouperNames="+collection);
		// order
		Vector v = new Vector (new TreeSet (collection));
		
		// line 0
		LinePanel l0 = new LinePanel ();
		JLabel lab1 = new JLabel (Translator.swap ("FishInReachGroup.selectFishes"));
		l0.add (lab1);
		l0.addGlue ();
		master.add (l0);
		
		// line 1
		LinePanel l1 = new LinePanel ();
		JLabel lab20 = new JWidthLabel (Translator.swap ("FishInReachGroup.inReachGroup")+" :", 150);
		grouperNames = new JComboBox (v);
		
		l1.add (lab20);
		l1.add (grouperNames);
		l1.addStrut0 ();
		master.add (l1);
		
		master.addGlue ();
		
		add (master, BorderLayout.NORTH);
		
	}

	/**	Events processing
	*/
	public void actionPerformed (ActionEvent e) {
		if (e.getSource ().equals (helpButton)) {
			Helper.helpFor (this);
		}
	}

	/**	From Pilotable interface
	*/
	public JComponent getPilot () {
		
		ImageIcon icon = IconLoader.getIcon ("help_16.png");
		helpButton = new JButton (icon);
		Tools.setSizeExactly (helpButton, BUTTON_SIZE, BUTTON_SIZE);
		helpButton.setToolTipText (Translator.swap ("Shared.help"));
		helpButton.addActionListener (this);
		
		JToolBar toolbar = new JToolBar ();
		toolbar.add (helpButton);
		toolbar.setVisible (true);
		
		return toolbar;
	}
	
	/**	From ConfigurationPanel
	*/
	public boolean checksAreOk () {
		return true;
	}	// no possible error in a combo box

	/**	Retrieve selected grouper name
	*/
	protected String getGrouperName () {
		String name = "";
		try {
			name = (String) grouperNames.getSelectedItem ();
		} catch (Exception e) {
			Log.println (Log.ERROR, "FishInReachGroupDialog.getGrouperName ()", "Exception caught: ", e);
		}	// see checksAreOk ()
		return name;
	}
	
	
}
	
