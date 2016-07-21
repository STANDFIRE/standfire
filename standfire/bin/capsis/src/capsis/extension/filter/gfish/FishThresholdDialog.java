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
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.IconLoader;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import jeeb.lib.util.extensionmanager.ExtensionManager;
import capsis.commongui.util.Helper;
import capsis.commongui.util.Tools;
import capsis.util.Configurable;
import capsis.util.ConfigurationPanel;
import capsis.util.Pilotable;

/**
 * Configuration panel for FishThreshold.
 *
 * @author F. de Coligny - september 2004
 */
public class FishThresholdDialog extends ConfigurationPanel implements ActionListener, Pilotable {
	private static final int BUTTON_SIZE = 23;

	private FishThreshold mum;		// mummy is being configured

	private JButton helpButton;

	protected JComboBox mode;
	protected JTextField lowValue;
	protected JTextField highValue;
	protected Map humanKey_mode;

	private NumberFormat formater;


	/**	Constructor
	*/
	protected FishThresholdDialog (Configurable c) {
		super (c);

		mum = (FishThreshold) c;

		setLayout (new BorderLayout ());

		formater = NumberFormat.getInstance (Locale.ENGLISH);
		formater.setGroupingUsed (false);
		if (mum.mode == FishThreshold.AGE
				//|| mum.mode == FishThreshold.SPAWNING_AGE
				|| mum.mode == FishThreshold.SPAWN_COUNT
				) {	// when integer values required
			formater.setMaximumFractionDigits (0);
		}

		ColumnPanel master = new ColumnPanel ();

		humanKey_mode = new Hashtable ();
		humanKey_mode.put (Translator.swap ("FishThreshold.forkLength"), new Integer (FishThreshold.FORK_LENGTH));
		//humanKey_mode.put (Translator.swap ("FishThreshold.dispersalProba"), new Integer (FishThreshold.DISPERSAL_PROBA));
		//humanKey_mode.put (Translator.swap ("FishThreshold.movementProba"), new Integer (FishThreshold.MOVEMENT_PROBA));
		humanKey_mode.put (Translator.swap ("FishThreshold.age"), new Integer (FishThreshold.AGE));
		//humanKey_mode.put (Translator.swap ("FishThreshold.spawningAge"), new Integer (FishThreshold.SPAWNING_AGE));
		humanKey_mode.put (Translator.swap ("FishThreshold.survivalProba"), new Integer (FishThreshold.SURVIVAL_PROBA));
		humanKey_mode.put (Translator.swap ("FishThreshold.spawnCount"), new Integer (FishThreshold.SPAWN_COUNT));

		Vector v = new Vector ();
		for (Iterator i = humanKey_mode.keySet ().iterator (); i.hasNext ();) {
			String s = (String) i.next ();
			v.add (s);
		}

		// order
		v = new Vector (new TreeSet (v));

		// line 0
		LinePanel l0 = new LinePanel ();
		JLabel lab1 = new JLabel (Translator.swap ("FishThreshold.selectFishes"));
		l0.add (lab1);
		l0.addGlue ();
		master.add (l0);

		// line 1
		LinePanel l1 = new LinePanel ();
		JLabel lab20 = new JWidthLabel (Translator.swap ("FishThreshold.whose")+" :", 150);
		mode = new JComboBox (v);

		for (int i = 0; i< mode.getItemCount (); i++) {
			String k = (String) mode.getItemAt (i);
			if (humanKey_mode.get (k).equals (new Integer ((mum.mode)))) {
				mode.setSelectedItem (k);
				break;
			}
		}

		l1.add (lab20);
		l1.add (mode);
		l1.addStrut0 ();
		master.add (l1);

		// line 2
		LinePanel l2 = new LinePanel ();
		JLabel lab2 = new JWidthLabel (Translator.swap ("FishThreshold.between")+" :", 150);
		lowValue = new JTextField (5);
		if (mum.lowValue == Double.MIN_VALUE
				|| (mum.lowValue == 0 && mum.highValue == 0)) {
			lowValue.setText ("");
		} else {
			lowValue.setText (formater.format (mum.lowValue));
		}
		JLabel lab22 = new JWidthLabel (Translator.swap ("FishThreshold.included"), 100);
		l2.add (lab2);
		l2.add (lowValue);
		l2.add (lab22);
		l2.addStrut0 ();
		master.add (l2);

		// line 3
		LinePanel l3 = new LinePanel ();
		JLabel lab3 = new JWidthLabel (Translator.swap ("FishThreshold.and")+" :", 150);
		highValue = new JTextField (5);
		if (mum.highValue == Double.MAX_VALUE
				|| (mum.lowValue == 0 && mum.highValue == 0)) {
			highValue.setText ("");
		} else {
			highValue.setText (formater.format (mum.highValue));
		}
		JLabel lab23 = new JWidthLabel (Translator.swap ("FishThreshold.excluded"), 100);
		l3.add (lab3);
		l3.add (highValue);
		l3.add (lab23);
		l3.addStrut0 ();
		master.add (l3);

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
		int t = 0;
		try {
			String s = (String) mode.getSelectedItem ();
			t =  ((Integer) humanKey_mode.get (s)).intValue ();
		} catch (Exception e) {}

		String name = ExtensionManager.getName(mum.getClass().getName());
		// age (int needed)
		if (t == FishThreshold.AGE
				//|| t == FishThreshold.SPAWNING_AGE
				|| t == FishThreshold.SPAWN_COUNT
				) {

			if (!Check.isEmpty (lowValue.getText ()) && !Check.isInt (lowValue.getText ())) {
				JOptionPane.showMessageDialog (this,
					Translator.swap ("FishThreshold.ageSpawningAgeAndSpawnCountLowValueMustBeInteger"),
					name,
					JOptionPane.WARNING_MESSAGE );
				return false;
			}

			if (!Check.isEmpty (highValue.getText ()) && !Check.isInt (highValue.getText ())) {
				JOptionPane.showMessageDialog (this,
					Translator.swap ("FishThreshold.ageSpawningAgeAndSpawnCountHighValueMustBeInteger"),
					name,
					JOptionPane.WARNING_MESSAGE );
				return false;
			}

			//~ if (!Check.isEmpty (lowValue.getText ()) && !Check.isEmpty (highValue.getText ())) {
				//~ if (Check.intValue (lowValue.getText ()) >= Check.intValue (highValue.getText ())) {
					//~ JOptionPane.showMessageDialog (this,
						//~ Translator.swap ("FishThreshold.lowMustBeLessThanHigh"),
						//~ mum.getName (),	// extension name in title bar
						//~ JOptionPane.WARNING_MESSAGE );
					//~ return false;
				//~ }
			//~ }

		}

		// Fork length (double needed)
		if (t == FishThreshold.FORK_LENGTH
				//|| t == FishThreshold.DISPERSAL_PROBA
				//|| t == FishThreshold.MOVEMENT_PROBA
				|| t == FishThreshold.AGE
				//|| t == FishThreshold.SPAWNING_AGE
				|| t == FishThreshold.SURVIVAL_PROBA
				|| t == FishThreshold.SPAWN_COUNT) {

			if (!Check.isEmpty (lowValue.getText ()) && !Check.isDouble (lowValue.getText ())) {
				JOptionPane.showMessageDialog (this,
					Translator.swap ("FishThreshold.LowValueMustBeDouble"),
					name,
					JOptionPane.WARNING_MESSAGE );
				return false;
			}

			if (!Check.isEmpty (highValue.getText ()) && !Check.isDouble (highValue.getText ())) {
				JOptionPane.showMessageDialog (this,
					Translator.swap ("FishThreshold.HighValueMustBeDouble"),
					name,
					JOptionPane.WARNING_MESSAGE );
				return false;
			}

			if (!Check.isEmpty (lowValue.getText ()) && !Check.isEmpty (highValue.getText ())) {
				if (Check.doubleValue (lowValue.getText ()) >= Check.doubleValue (highValue.getText ())) {
					JOptionPane.showMessageDialog (this,
						Translator.swap ("FishThreshold.lowMustBeLessThanHigh"),
						name,
						JOptionPane.WARNING_MESSAGE );
					return false;
				}
			}
		}

		return true;
	}	// no possible error on a check box


	/**	Return lowValue
	*/
	protected double getLowValue () {
		String low = lowValue.getText ().trim ();
		if (low.length () == 0) {
			return Double.MIN_VALUE;
		} else {
			return Check.doubleValue (low);
		}
	}


	/**	Return highValue
	*/
	protected double getHighValue () {
		String high = highValue.getText ().trim ();
		if (high.length () == 0) {
			return Double.MAX_VALUE;
		} else {
			return Check.doubleValue (high);
		}
	}


	/**	Retrieve mode
	*/
	protected int getMode () {
		int r = 0;
		try {
			String s = (String) mode.getSelectedItem ();
			r =  ((Integer) humanKey_mode.get (s)).intValue ();
		} catch (Exception e) {
			Log.println (Log.ERROR, "FishThresholdDialog.getMode ()", "Exception caught: ", e);
		}	// see checksAreOk ()
		return r;
	}


}

