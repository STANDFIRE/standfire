/**
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 1999-2010 INRA
 * 
 * Authors: F. de Coligny, S. Dufour-Kowalski,
 * 
 * This file is part of Capsis Capsis is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 2.1 of the License, or (at your option) any later version.
 * 
 * Capsis is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU lesser General Public License along with Capsis. If
 * not, see <http://www.gnu.org/licenses/>.
 * 
 */

package capsis.extension;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.MemoPanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Tools;
import capsis.extension.AbstractDataExtractor.ItemSelector;
import capsis.extension.AbstractDataExtractor.SelectButton;
import capsis.extensiontype.DataBlock;
import capsis.gui.DListSelector;
import capsis.gui.GrouperChooser;
import capsis.gui.GrouperChooserListener;
import capsis.kernel.GScene;
import capsis.lib.genetics.GeneticScene;
import capsis.lib.genetics.GenoSpecies;
import capsis.lib.genetics.Genotypable;
import capsis.util.ConfigurationPanel;
import capsis.util.GrouperManager;
import capsis.util.SharedConfigurable;
import capsis.util.Spiable;
import capsis.util.Spy;

/**
 * Panel for a data block common configuration considerations. These configurations apply to each
 * data extractor in the block.
 * 
 * @author F. de Coligny - march 2003
 */
// fc - 6.2.2004 - added isPropertyEnabled (name) management for all properties
public class DEMultiConfPanel extends ConfigurationPanel implements ActionListener {

	private DataBlock dataBlock; // the data block of the extractor given at construction time

	// These IV are public to be accessible in ex.functionalTestsAreOk ()
	// redefinitions

	private Collection orderedProperties; // fc - 16.10.2006

	// Specific ones (oldies : still there)
	public JCheckBox perHa;
	public JCheckBox percentage;
	public JTextField classWidth;
	public JTextField intervalNumber;
	public JTextField intervalSize;
	public JTextField icNumberOfSimulations;
	public JTextField icRisk;
	public JTextField icPrecision;

	// Groupers
	public GrouperChooser grouperChooser;

	// Generic properties
	public Map booleanPropertiesCheckBoxes; // checkbox
	public Map radioPropertiesRadioButtons; // radio button
	public Map intPropertiesTextFields; // field for integer value
	public Map doublePropertiesTextFields; // field for decimal value
	public Map comboPropertiesComboBoxes; // combo

	private AbstractDataExtractor ex; // Reference to the extractor being
										// configured

	/**
	 * Constructor.
	 */
	protected DEMultiConfPanel (SharedConfigurable c) {
		super (c);

		ex = (AbstractDataExtractor) c; // fc - 28.3.2003
		dataBlock = ex.getDataBlock ();
		
		// fc - 16.10.2006 - all these lines moved here
		booleanPropertiesCheckBoxes = new HashMap ();
		radioPropertiesRadioButtons = new HashMap ();
		intPropertiesTextFields = new HashMap ();
		doublePropertiesTextFields = new HashMap ();
		comboPropertiesComboBoxes = new HashMap ();

		ColumnPanel master = new ColumnPanel ();

		// fc - 16.10.2006 [S Turbis] - order exactly the properties (starting
		// with o-)
		// on specified lines and columns
		// 1. search ordered properties
		orderedProperties = extractOrderedProperties ();

		layoutSpecificProperties (master);

		layoutComboProperties (master);
		layoutSetProperties (master);
		layoutDoubleProperties (master);
		layoutIntProperties (master);
		layoutBooleanProperties (master);
		layoutRadioProperties (master);

		// 2. layout ordered properties
		layoutOrderedProperties (master); // fc - 16.10.2006

		// fc-27.3.2012 to avoid an empty panel
		if (master.getComponentCount () == 0) {
			LinePanel l1 = new LinePanel ();
			l1.add (new JLabel (Translator.swap ("DataExtractor.noCommonConfiguration")));
			l1.addGlue ();
			master.add (l1);
		}

		// Add master panel
		setLayout (new BorderLayout ());
		JPanel aux = new JPanel (new BorderLayout ());
		aux.add (master, BorderLayout.NORTH);

		add (new JScrollPane (aux), BorderLayout.CENTER);

		MemoPanel userMemo = new MemoPanel (Translator.swap("DEMultiConfPanel.commonConfigurationExplanation"));
		add (userMemo, BorderLayout.SOUTH);
		
		
	}

	// fc - 16.10.2006
	//
	private Collection extractOrderedProperties () {
		Collection c = new TreeSet (); // ordered
		// ~ for (Iterator i = ex.settings.configProperties.iterator ();
		// i.hasNext ();) {
		// ~ String propKey = (String) i.next ();
		// ~ if (propKey.startsWith ("o-")) {c.add (propKey);}
		// ~ }
		for (Iterator i = ex.settings.booleanProperties.keySet ().iterator (); i.hasNext ();) {
			String propKey = (String) i.next ();
			if (propKey.startsWith ("o-")) {
				c.add (propKey);
			}
		}
		for (Iterator i = ex.settings.radioProperties.keySet ().iterator (); i.hasNext ();) {
			String propKey = (String) i.next ();
			if (propKey.startsWith ("o-")) {
				c.add (propKey);
			}
		}
		for (Iterator i = ex.settings.intProperties.keySet ().iterator (); i.hasNext ();) {
			String propKey = (String) i.next ();
			if (propKey.startsWith ("o-")) {
				c.add (propKey);
			}
		}
		for (Iterator i = ex.settings.doubleProperties.keySet ().iterator (); i.hasNext ();) {
			String propKey = (String) i.next ();
			if (propKey.startsWith ("o-")) {
				c.add (propKey);
			}
		}
		for (Iterator i = ex.settings.setProperties.keySet ().iterator (); i.hasNext ();) {
			String propKey = (String) i.next ();
			if (propKey.startsWith ("o-")) {
				c.add (propKey);
			}
		}
		for (Iterator i = ex.settings.comboProperties.keySet ().iterator (); i.hasNext ();) {
			String propKey = (String) i.next ();
			if (propKey.startsWith ("o-")) {
				c.add (propKey);
			}
		}

		return c;
	}

	// Ordered properties : specific layout on lines / columns
	//
	private void layoutOrderedProperties (ColumnPanel master) {
		try {

			if (orderedProperties == null || orderedProperties.isEmpty ()) return; // fc-27.3.2012

			Border etched = BorderFactory.createEtchedBorder ();

			String blockName = "";
			String newBlockName = "";

			ColumnPanel block = new ColumnPanel ();
			// ~ master.add (block);

			LinePanel l = new LinePanel ();
			// ~ block.add (l);

			int prevLine = -1;
			for (Iterator i = orderedProperties.iterator (); i.hasNext ();) {
				String propKey = (String) i.next ();
				String aux = propKey.substring (2);

				// fc - 23.10.2006 - blocks with titled panels
				if (aux.startsWith ("block")) {
					int endOfBlock = aux.indexOf ("-");
					newBlockName = aux.substring (0, endOfBlock);
					aux = aux.substring (endOfBlock + 1);
				} else {
					newBlockName = "";
				}

				String lineNumber = aux.substring (0, aux.indexOf ("-"));
				int line = new Integer (lineNumber).intValue ();
				if (prevLine < 0) {
					prevLine = line;
				} // init prevLine
				aux = aux.substring (aux.indexOf ("-") + 1);
				String colNumber = aux.substring (0, aux.indexOf ("-"));

				if (line != prevLine) {
					l.addGlue ();
					block.add (l);
					l = new LinePanel ();
					prevLine = line;

				}

				if (!newBlockName.equals (blockName)) {
					if (!blockName.equals ("")) {
						Border b = BorderFactory.createTitledBorder (etched, Translator
								.swap (blockName));
						block.setBorder (b);
					}

					master.add (block);

					block = new ColumnPanel ();

				}

				blockName = newBlockName;

				l.add (getComponent (propKey));
			}

			l.addGlue (); // last line
			if (!blockName.equals ("")) {
				Border b = BorderFactory.createTitledBorder (etched, Translator.swap (blockName));
				block.setBorder (b);
			}
			block.add (l); // last line
			master.add (block); // last block

		} catch (Exception e) {
			Log.println (Log.ERROR, "DEMultiConfPanel.layoutOrderedProperties ()", "error while laying out ordered properties (o-...)", e);
		}
	}

	// fc - 16.10.2006
	//
	private JComponent getComponent (String propKey) {

		if (ex.settings.radioProperties.containsKey (propKey)) {
			// todo

		} else if (ex.settings.booleanProperties.containsKey (propKey)) {
			// Create jcheckbox
			Boolean yep = (Boolean) ex.settings.booleanProperties.get (propKey);
			JCheckBox cb = new JCheckBox (Translator.swap (propKey), yep.booleanValue ());
			booleanPropertiesCheckBoxes.put (propKey, cb);
			if (!ex.isPropertyEnabled (propKey)) {
				cb.setEnabled (false);
			} // fc - 6.2.2004
			return cb;

		} else if (ex.settings.intProperties.containsKey (propKey)) {
			// Create jtextfield for an int
			Integer value = (Integer) ex.settings.intProperties.get (propKey);
			// ~ JLabel label = new JWidthLabel (Translator.swap (propKey),
			// 190);
			JLabel label = new JLabel (Translator.swap (propKey));
			JTextField f = new JTextField ("" + value.intValue (), 5);
			intPropertiesTextFields.put (propKey, f);
			if (!ex.isPropertyEnabled (propKey)) {
				f.setEnabled (false);
			} // fc - 6.2.2004
			LinePanel l1 = new LinePanel ();
			l1.add (label);
			l1.add (f);
			l1.addGlue ();
			return l1;

		} else if (ex.settings.doubleProperties.containsKey (propKey)) {
			// Create JTextField
			Double value = (Double) ex.settings.doubleProperties.get (propKey);
			// ~ JLabel label = new JWidthLabel (Translator.swap (propKey),
			// 190);
			JLabel label = new JLabel (Translator.swap (propKey));
			JTextField f = new JTextField ("" + value.doubleValue (), 5);
			doublePropertiesTextFields.put (propKey, f);
			if (!ex.isPropertyEnabled (propKey)) {
				f.setEnabled (false);
			} // fc - 6.2.2004
			LinePanel l1 = new LinePanel ();
			l1.add (label);
			l1.add (f);
			l1.addGlue ();
			return l1;

		} else if (ex.settings.setProperties.containsKey (propKey)) {
			// todo

		} else if (ex.settings.comboProperties.containsKey (propKey)) {
			// Create JComboBox
			LinkedList value = (LinkedList) ex.settings.comboProperties.get (propKey);
			Object selectedItem = value.getFirst ();
			
			// ~ JLabel label = new JWidthLabel (Translator.swap (propKey),
			// 190);
			JLabel label = new JLabel (Translator.swap (propKey));
			Map human2key = new LinkedHashMap ();
			for (Iterator i = value.iterator (); i.hasNext ();) {
				String key = (String) i.next ();
				String human = Translator.swap (key);
				human2key.put (human, key);
			}
			// Translate values... maybe we should not translate here ? User
			// could decide
			// to translate or not at building combo property time...
			// Alphabetical order - needed
			JComboBox f = new JComboBox (new TreeSet (human2key.keySet ()).toArray ());
			// Select first value in values
			f.setSelectedItem (Translator.swap ((String) selectedItem)); // fc -
																			// 1.2.2008
																			// -
																			// translation
																			// added
			comboPropertiesComboBoxes.put (propKey, f);
			if (!ex.isPropertyEnabled (propKey)) {
				f.setEnabled (false);
			} // fc - 6.2.2004
			LinePanel l1 = new LinePanel ();
			l1.add (label);
			l1.add (f);
			l1.addGlue ();
			return l1;

		}
		return null;
	}

	// Radio properties : add radio buttons
	//
	private void layoutRadioProperties (ColumnPanel master) {
		// fc-30.8.2012 added TreeSet below, order was changing inconsistently (CM & TL)...
		Iterator keys = new TreeSet (ex.settings.radioProperties.keySet ()).iterator ();
		String prevRoot = "";
		JPanel panel = null;
		JPanel inside = null;
		Border etched = BorderFactory.createEtchedBorder ();
		ButtonGroup radioGroup = new ButtonGroup ();
		while (keys.hasNext ()) {
			String name = (String) keys.next ();
			Boolean yep = (Boolean) ex.settings.radioProperties.get (name); // fc-30.8.2012

			if (ex.isIndividualProperty (name)) {
				continue;
			}
			if (orderedProperties.contains (name)) {
				continue;
			} // fc - 16.10.2006

			String root = "";
			try {
				root = name.substring (0, name.indexOf ("_"));
			} catch (Exception e) {}
			String suffix = "";
			try {
				suffix = name.substring (name.indexOf ("_") + 1);
			} catch (Exception e) {}

			// If root changes, write old root's panel
			if (!root.equals (prevRoot)) {
				if (panel != null) {
					master.add (panel);
				}
				panel = null;
				prevRoot = root;
				radioGroup = new ButtonGroup ();
			}

			// Create radio buttons
			JRadioButton rb = new JRadioButton (Translator.swap (name), yep.booleanValue ());
			radioPropertiesRadioButtons.put (name, rb);

			if (!ex.isPropertyEnabled (name)) {
				rb.setEnabled (false);
			}

			// Add the buttons in groups
			radioGroup.add (rb);

			if (!"".equals (root)) {
				if (panel == null) {
					panel = new JPanel (new BorderLayout ());
					Border border = BorderFactory.createTitledBorder (etched, Translator
							.swap (root));
					panel.setBorder (border);

					inside = new JPanel ();
					inside.setLayout (new BoxLayout (inside, BoxLayout.Y_AXIS));
					panel.add (inside, BorderLayout.CENTER);
				}
				inside.add (rb);
			} else {
				LinePanel l1 = new LinePanel ();
				l1.add (rb);
				l1.addGlue ();
				master.add (l1);
			}
		}

		// Add last radio property panel if needed
		if (panel != null) {
			master.add (panel);
		}
	}

	// Boolean properties : add JCheckBoxes
	//
	private void layoutBooleanProperties (ColumnPanel master) {
		// fc-30.8.2012 added TreeSet below, order was changing inconsistently (CM & TL)...
		Iterator keys = new TreeSet (ex.settings.booleanProperties.keySet ()).iterator ();
		String prevRoot = "";
		JPanel panel = null;
		JPanel inside = null;
		Border etched = BorderFactory.createEtchedBorder ();
		while (keys.hasNext ()) {
			String name = (String) keys.next ();
			Boolean yep = (Boolean) ex.settings.booleanProperties.get (name); // fc-30.8.2012

			if (ex.isIndividualProperty (name)) {
				continue;
			}
			if (orderedProperties.contains (name)) {
				continue;
			}

			String root = "";
			try {
				root = name.substring (0, name.indexOf ("_"));
			} catch (Exception e) {}
			String suffix = "";
			try {
				suffix = name.substring (name.indexOf ("_") + 1);
			} catch (Exception e) {}

			// If root changes, write old root's panel
			if (!root.equals (prevRoot)) {
				if (panel != null) {
					master.add (panel);
				}
				panel = null;
				prevRoot = root;
			}

			// fc + op - 20.3.2008 - wpn_Debardage -> Debardage (suppression du
			// prefixe, le name est deja traduit)
			// Create checkbox
			String translatedName = Translator.swap (name);
			if (translatedName.equals (Translator.swap (name))
					&& translatedName.indexOf ('_') != -1) {
				translatedName = suffix;
			}
			JCheckBox cb = new JCheckBox (translatedName, yep.booleanValue ());
			// ~ JCheckBox cb = new JCheckBox (Translator.swap (name),
			// yep.booleanValue ());
			// fc + op - 20.3.2008 - wpn_Debardage -> Debardage (suppression du
			// prefixe, le name est deja traduit)

			booleanPropertiesCheckBoxes.put (name, cb);

			if (!ex.isPropertyEnabled (name)) {
				cb.setEnabled (false);
			} // fc - 6.2.2004

			if (!"".equals (root)) {
				if (panel == null) {
					panel = new JPanel (new BorderLayout ());
					Border border = BorderFactory.createTitledBorder (etched, Translator
							.swap (root));
					panel.setBorder (border);

					inside = new JPanel ();
					inside.setLayout (new BoxLayout (inside, BoxLayout.Y_AXIS));
					panel.add (inside, BorderLayout.CENTER);
				}
				inside.add (cb);
			} else {
				LinePanel l1 = new LinePanel ();
				l1.add (cb);
				l1.addGlue ();
				master.add (l1);
			}
		}

		// Add last boolean property panel if needed
		if (panel != null) {
			master.add (panel);
		}
	}

	// Int properties : add JTextFields
	//
	private void layoutIntProperties (ColumnPanel master) {
		// fc-30.8.2012 added TreeSet below, order was changing inconsistently (CM & TL)...
		Iterator keys = new TreeSet (ex.settings.intProperties.keySet ()).iterator ();
		String prevRoot = "";
		JPanel panel = null;
		JPanel inside = null;
		Border etched = BorderFactory.createEtchedBorder ();
		while (keys.hasNext ()) {
			String name = (String) keys.next ();
			Integer value = (Integer) ex.settings.intProperties.get (name); // fc-30.8.2012

			if (ex.isIndividualProperty (name)) {
				continue;
			}
			if (orderedProperties.contains (name)) {
				continue;
			}

			String root = "";
			try {
				root = name.substring (0, name.indexOf ("_"));
			} catch (Exception e) {}
			String suffix = "";
			try {
				suffix = name.substring (name.indexOf ("_") + 1);
			} catch (Exception e) {}

			// If root changes, write old root's panel
			if (!root.equals (prevRoot)) {
				if (panel != null) {
					master.add (panel);
				}
				panel = null;
				prevRoot = root;
			}

			// Create JTextField
			JLabel label = new JWidthLabel (Translator.swap (name) + " :", 190);
			JTextField f = new JTextField ("" + value.intValue (), 5);
			intPropertiesTextFields.put (name, f);

			if (!ex.isPropertyEnabled (name)) {
				f.setEnabled (false);
			}

			if (!"".equals (root)) {
				if (panel == null) {
					panel = new JPanel (new BorderLayout ());
					Border border = BorderFactory.createTitledBorder (etched, Translator
							.swap (root)); // bordered panel name
					panel.setBorder (border);

					inside = new JPanel ();
					inside.setLayout (new BoxLayout (inside, BoxLayout.Y_AXIS));
					panel.add (inside, BorderLayout.CENTER);
				}
				LinePanel l1 = new LinePanel ();
				l1.add (label);
				l1.add (f);
				l1.addGlue ();
				inside.add (l1);
			} else {
				LinePanel l1 = new LinePanel ();
				l1.add (label);
				l1.add (f);
				l1.addGlue ();
				master.add (l1);
			}
		}

		// Add last property panel if needed
		if (panel != null) {
			master.add (panel);
		}
	}

	// Double properties : add JTextFields
	//
	private void layoutDoubleProperties (ColumnPanel master) {
		// fc-30.8.2012 added TreeSet below, order was changing inconsistently (CM & TL)...
		Iterator keys = new TreeSet (ex.settings.doubleProperties.keySet ()).iterator ();
		String prevRoot = "";
		JPanel panel = null;
		JPanel inside = null;
		Border etched = BorderFactory.createEtchedBorder ();
		while (keys.hasNext ()) {
			String name = (String) keys.next ();
			Double value = (Double) ex.settings.doubleProperties.get (name); // fc-30.8.2012

			if (ex.isIndividualProperty (name)) {
				continue;
			}
			if (orderedProperties.contains (name)) {
				continue;
			}

			String root = "";
			try {
				root = name.substring (0, name.indexOf ("_"));
			} catch (Exception e) {}
			String suffix = "";
			try {
				suffix = name.substring (name.indexOf ("_") + 1);
			} catch (Exception e) {}

			// If root changes, write old root's panel
			if (!root.equals (prevRoot)) {
				if (panel != null) {
					master.add (panel);
				}
				panel = null;
				prevRoot = root;
			}

			// Create JTextField
			JLabel label = new JWidthLabel (Translator.swap (name) + " :", 190);
			JTextField f = new JTextField ("" + value.doubleValue (), 5);
			doublePropertiesTextFields.put (name, f);

			if (!ex.isPropertyEnabled (name)) {
				f.setEnabled (false);
			}

			if (!"".equals (root)) {
				if (panel == null) {
					panel = new JPanel (new BorderLayout ());
					Border border = BorderFactory.createTitledBorder (etched, Translator
							.swap (root)); // bordered panel name
					panel.setBorder (border);

					inside = new JPanel ();
					inside.setLayout (new BoxLayout (inside, BoxLayout.Y_AXIS));
					panel.add (inside, BorderLayout.CENTER);
				}
				LinePanel l1 = new LinePanel ();
				l1.add (label);
				l1.add (f);
				l1.addGlue ();
				inside.add (l1);
			} else {
				LinePanel l1 = new LinePanel ();
				l1.add (label);
				l1.add (f);
				l1.addGlue ();
				master.add (l1);
			}
		}

		// Add last property panel if needed
		if (panel != null) {
			master.add (panel);
		}

	}

	// Combo properties : add JComboBoxes
	//
	private void layoutComboProperties (ColumnPanel master) {
		// fc-30.8.2012 added TreeSet below, order was changing inconsistently (CM & TL)...
		Iterator keys = new TreeSet (ex.settings.comboProperties.keySet ()).iterator ();
		String prevRoot = "";
		JPanel panel = null;
		JPanel inside = null;
		Border etched = BorderFactory.createEtchedBorder ();
		while (keys.hasNext ()) {
			String name = (String) keys.next ();
			try {

				if (ex.isIndividualProperty (name)) {
					continue;
				}
				if (orderedProperties.contains (name)) {
					continue;
				}

				LinkedList value = (LinkedList) ex.settings.comboProperties.get (name); // fc-30.8.2012
				Object selectedItem = value.getFirst ();

				String root = "";
				try {
					root = name.substring (0, name.indexOf ("_"));
				} catch (Exception e) {}
				String suffix = "";
				try {
					suffix = name.substring (name.indexOf ("_") + 1);
				} catch (Exception e) {}

				// If root changes, write old root's panel
				if (!root.equals (prevRoot)) {
					if (panel != null) {
						master.add (panel);
					}
					panel = null;
					prevRoot = root;
				}

				// Create JComboBox
				JLabel label = new JWidthLabel (Translator.swap (name) + " :", 190);
				Map human2key = new LinkedHashMap ();
				for (Iterator i = value.iterator (); i.hasNext ();) {
					String key = (String) i.next ();
					String human = Translator.swap (key);
					human2key.put (human, key);
				}

				// Translate values... maybe we should not translate here ? User
				// could decide
				// to translate or not at building combo property time...
				// Alphabetical order - needed
				JComboBox f = new JComboBox (new TreeSet (human2key.keySet ()).toArray ());

				// Select first value in values
				f.setSelectedItem (Translator.swap ((String) selectedItem));

				comboPropertiesComboBoxes.put (name, f);

				if (!ex.isPropertyEnabled (name)) {
					f.setEnabled (false);
				}

				if (!"".equals (root)) {
					if (panel == null) {
						panel = new JPanel (new BorderLayout ());
						Border border = BorderFactory.createTitledBorder (etched, Translator
								.swap (root)); // bordered
												// panel name
						panel.setBorder (border);

						inside = new JPanel ();
						inside.setLayout (new BoxLayout (inside, BoxLayout.Y_AXIS));
						panel.add (inside, BorderLayout.CENTER);
					}
					LinePanel l1 = new LinePanel ();
					l1.add (label);
					l1.add (f);
					l1.addGlue ();
					inside.add (l1);
				} else {
					LinePanel l1 = new LinePanel ();
					l1.add (label);
					l1.add (f);
					l1.addGlue ();
					master.add (l1);
				}
			} catch (Exception e) {
				Log.println (Log.WARNING, "DEMultiConfPanel.layoutComboProperties ()", "Error while laying out combo "
						+ name + ", passed", e);
			}
		}

		// Add last property panel if needed
		if (panel != null) {
			master.add (panel);
		}

	}

	// Set properties : add JTextFields and select buttons
	//
	private void layoutSetProperties (ColumnPanel master) {
		// fc-30.8.2012 added TreeSet below, order was changing inconsistently (CM & TL)...
		Iterator keys = new TreeSet (ex.settings.setProperties.keySet ()).iterator ();
		String prevRoot = "";
		JPanel panel = null;
		JPanel inside = null;
		Border etched = BorderFactory.createEtchedBorder ();
		while (keys.hasNext ()) {
			String name = (String) keys.next ();
			ItemSelector itemSelector = (ItemSelector) ex.settings.setProperties.get (name); // fc-30.8.2012

			if (ex.isIndividualProperty (name)) {
				continue;
			}
			if (orderedProperties.contains (name)) {
				continue;
			}

			String root = "";
			try {
				root = name.substring (0, name.indexOf ("_"));
			} catch (Exception e) {}
			String suffix = "";
			try {
				suffix = name.substring (name.indexOf ("_") + 1);
			} catch (Exception e) {}

			// If root changes, write old root's panel
			if (!root.equals (prevRoot)) {
				if (panel != null) {
					master.add (panel);
				}
				panel = null;
				prevRoot = root;
			}

			// Create JTextField and select button
			JLabel label = new JWidthLabel (Translator.swap (name) + " :", 120);

			// When something happens in itemSelector, f will know (and clear
			// itself)
			SpyJTextField f = new SpyJTextField (itemSelector,
					Tools.toString (itemSelector.selectedValues), 5);
			f.setEditable (false);

			JButton b = createSelectButton (name, itemSelector, f);

			if (!ex.isPropertyEnabled (name)) { // fc - 6.2.2004
				f.setEnabled (false);
				b.setEnabled (false);
			}

			if (!"".equals (root)) {
				if (panel == null) {
					panel = new JPanel (new BorderLayout ());
					Border border = BorderFactory.createTitledBorder (etched, Translator
							.swap (root)); // bordered panel name
					panel.setBorder (border);

					inside = new JPanel ();
					inside.setLayout (new BoxLayout (inside, BoxLayout.Y_AXIS));
					panel.add (inside, BorderLayout.CENTER);
				}
				LinePanel l1 = new LinePanel ();
				l1.add (label);
				l1.add (f);

				l1.add (b);

				l1.addGlue ();
				inside.add (l1);
			} else {
				LinePanel l1 = new LinePanel ();
				l1.add (label);
				l1.add (f);

				l1.add (b);

				l1.addGlue ();
				master.add (l1);
			}
		}

		// Add last property panel if needed
		if (panel != null) {
			master.add (panel);
		}

	}

	// Some specific properties : HECTARE, PERCENTAGE, TREE_GROUP, CELL_GROUP,
	// CLASS_WIDTH
	// + some (old manner) properties for Ripley : INTERVAL_NUMBER,
	// INTERVAL_SIZE,
	// IC_NUMBER_OF_SIMULATIONS, IC_RISK, IC_PRECISION
	// New properties should use the new framework (boolean, radio, set, int and
	// double properties (...)).
	//
	private void layoutSpecificProperties (ColumnPanel master) {

		// Per hectare computation combo box
		//
		if (ex.hasConfigProperty (AbstractDataExtractor.HECTARE)) {
			LinePanel l1 = new LinePanel ();
			perHa = new JCheckBox (Translator.swap ("DataExtractor.perHa"), ex.settings.perHa);

			if (!ex.isPropertyEnabled (AbstractDataExtractor.HECTARE)) {
				perHa.setEnabled (false);
			} // fc - 6.2.2004

			l1.add (perHa);
			l1.addGlue ();
			master.add (l1);
		}

		// Percentage computation combo box
		//
		if (ex.hasConfigProperty (AbstractDataExtractor.PERCENTAGE)) {
			percentage = new JCheckBox (Translator.swap ("DataExtractor.percentage"),
					ex.settings.percentage);

			if (!ex.isPropertyEnabled (AbstractDataExtractor.PERCENTAGE)) {
				percentage.setEnabled (false);
			} // fc - 6.2.2004

			LinePanel l1 = new LinePanel ();
			l1.add (percentage);
			l1.addGlue ();
			master.add (l1);
		}

		// Groupers in COMMON context
		//
		if (ex.getCGrouperType () != null) {
			String type = ex.getCGrouperType ();

			// fc - 13.9.2004
			// ~ Object composite = Group.getComposite (ex.getStep ().getStand
			// (), type);

			// NEW...
			boolean checked = ex.isCommonGrouper () && ex.isGrouperMode ();
			GrouperManager gm = GrouperManager.getInstance ();
			String selectedGrouperName = gm.removeNot (ex.getGrouperName ());
			grouperChooser = new GrouperChooser (ex.getStep ().getScene (), type,
					selectedGrouperName, ex.isCommonGrouperNot (), true, checked);

			// fc - 13.9.2004 - removed next line
			// ~ grouperChooser.setEnabled (enabled); // fc - 6.2.2004

			// Call listener when grouper change
			grouperChooser.addGrouperChooserListener ((GrouperChooserListener) ex);

			LinePanel l8 = new LinePanel ();
			l8.add (grouperChooser);
			l8.addStrut0 ();
			master.add (l8);
		}

		// JTextField for class width
		//
		if (ex.hasConfigProperty (AbstractDataExtractor.CLASS_WIDTH)) {
			LinePanel l1 = new LinePanel ();
			l1.add (new JWidthLabel (Translator.swap ("DataExtractor.classWidth") + " :", 190));
			classWidth = new JTextField ("" + ex.settings.classWidth, 3);

			if (!ex.isPropertyEnabled (AbstractDataExtractor.CLASS_WIDTH)) {
				classWidth.setEnabled (false);
			} // fc - 6.2.2004

			l1.add (classWidth);
			l1.addStrut0 ();
			master.add (l1);
		}

		// JTextField for intervalNumber
		//
		if (ex.hasConfigProperty (AbstractDataExtractor.INTERVAL_NUMBER)) {
			LinePanel l1 = new LinePanel ();
			l1.add (new JWidthLabel (Translator.swap ("DataExtractor.intervalNumber") + " :", 190));
			intervalNumber = new JTextField ("" + ex.settings.intervalNumber, 3);

			if (!ex.isPropertyEnabled (AbstractDataExtractor.INTERVAL_NUMBER)) {
				intervalNumber.setEnabled (false);
			} // fc - 6.2.2004

			l1.add (intervalNumber);
			l1.addStrut0 ();
			master.add (l1);
		}

		// JTextField for intervalSize
		//
		if (ex.hasConfigProperty (AbstractDataExtractor.INTERVAL_SIZE)) {
			LinePanel l1 = new LinePanel ();
			l1.add (new JWidthLabel (Translator.swap ("DataExtractor.intervalSize") + " :", 190));
			intervalSize = new JTextField ("" + ex.settings.intervalSize, 3);

			if (!ex.isPropertyEnabled (AbstractDataExtractor.INTERVAL_SIZE)) {
				intervalSize.setEnabled (false);
			} // fc - 6.2.2004

			l1.add (intervalSize);
			l1.addStrut0 ();
			master.add (l1);
		}

		// JTextField for icNumberOfSimulations
		//
		if (ex.hasConfigProperty (AbstractDataExtractor.IC_NUMBER_OF_SIMULATIONS)) {
			LinePanel l1 = new LinePanel ();
			l1.add (new JWidthLabel (
					Translator.swap ("DataExtractor.icNumberOfSimulations") + " :", 190));
			icNumberOfSimulations = new JTextField ("" + ex.settings.icNumberOfSimulations, 3);

			if (!ex.isPropertyEnabled (AbstractDataExtractor.IC_NUMBER_OF_SIMULATIONS)) {
				icNumberOfSimulations.setEnabled (false);
			} // fc - 6.2.2004

			l1.add (icNumberOfSimulations);
			l1.addStrut0 ();
			master.add (l1);
		}

		// JTextField for icRisk
		//
		if (ex.hasConfigProperty (AbstractDataExtractor.IC_RISK)) {
			LinePanel l1 = new LinePanel ();
			l1.add (new JWidthLabel (Translator.swap ("DataExtractor.icRisk") + " :", 190));
			icRisk = new JTextField ("" + ex.settings.icRisk, 3);

			if (!ex.isPropertyEnabled (AbstractDataExtractor.IC_RISK)) {
				icRisk.setEnabled (false);
			} // fc - 6.2.2004

			l1.add (icRisk);
			l1.addStrut0 ();
			master.add (l1);
		}

		// JTextField for icPrecision
		//
		if (ex.hasConfigProperty (AbstractDataExtractor.IC_PRECISION)) {
			LinePanel l1 = new LinePanel ();
			l1.add (new JWidthLabel (Translator.swap ("DataExtractor.icPrecision") + " :", 190));
			icPrecision = new JTextField ("" + ex.settings.icPrecision, 3);

			if (!ex.isPropertyEnabled (AbstractDataExtractor.IC_PRECISION)) {
				icPrecision.setEnabled (false);
			} // fc - 6.2.2004

			l1.add (icPrecision);
			l1.addStrut0 ();
			master.add (l1);
		}

	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////:
	// //////////////////////////////////////////// Set selection tools
	// ///////////////////////////////////////////////////////////////////////////////////////////////:
	// ///////////////////////////////////////////////////////////////////////////////////////////////:

	// A select button to open a listselector related to a setProperty
	//
	private JButton createSelectButton (String propertyName, ItemSelector itemSelector,
			JTextField textField) {
		JButton select = new SelectButton (Translator.swap ("Shared.select"), propertyName,
				itemSelector, textField);
		select.addActionListener (this);
		return select;
	}

	// Selection processing
	//
	public void processSelection (SelectButton selectButton) {
		String propertyName = selectButton.propertyName;
		Set possibleValues = selectButton.itemSelector.possibleValues;
		Set selectedValues = selectButton.itemSelector.selectedValues;
		JTextField textField = selectButton.textField;

		DListSelector dlg = new DListSelector (Translator.swap ("propertyName"),
				Translator.swap ("DataExtractor.selectionText"), new Vector (possibleValues));
		if (dlg.isValidDialog ()) {
			Set selection = new TreeSet (dlg.getSelectedVector ());
			textField.setText (Tools.toString (selection)); // update textfield
			selectButton.itemSelector.selectedValues = selection; // remember
																	// selection
		}
		dlg.dispose ();
	}

	// Button events processing
	//
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource () instanceof SelectButton) {
			processSelection ((SelectButton) evt.getSource ());
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////////:
	// //////////////////////////////////////////// Controls
	// ///////////////////////////////////////////////////////////////////////////////////////////////:
	// ///////////////////////////////////////////////////////////////////////////////////////////////:

	public boolean checksAreOk () {
		if (ex.hasConfigProperty (AbstractDataExtractor.CLASS_WIDTH)) {
			String text = classWidth.getText ();
			if (Check.isEmpty (text) || !Check.isInt (text)) {
				JOptionPane.showMessageDialog (this, Translator
						.swap ("DataExtractor.classWidthMustBeInteger"), Translator
						.swap ("Shared.error"), JOptionPane.WARNING_MESSAGE); // Shared.error ->
																				// title
																				// bar
				return false;
			}
			int i = Check.intValue (classWidth.getText ());
			if (i <= 0) {
				JOptionPane.showMessageDialog (this, Translator
						.swap ("DataExtractor.classWidthMustBeGreaterThanZero"), Translator
						.swap ("Shared.error"), JOptionPane.WARNING_MESSAGE); // Shared.error ->
																				// title
																				// bar
				return false;
			}
		}

		if (ex.hasConfigProperty (AbstractDataExtractor.INTERVAL_NUMBER)) {
			String text = intervalNumber.getText ();
			if (Check.isEmpty (text) || !Check.isInt (text)) {
				JOptionPane.showMessageDialog (this, Translator
						.swap ("DataExtractor.intervalNumberMustBeInteger"), Translator
						.swap ("Shared.error"), JOptionPane.WARNING_MESSAGE); // Shared.error ->
																				// title
																				// bar
				return false;
			}
			int i = Check.intValue (intervalNumber.getText ());
			if (i <= 0) {
				JOptionPane.showMessageDialog (this, Translator
						.swap ("DataExtractor.intervalNumberMustBeGreaterThanZero"), Translator
						.swap ("Shared.error"), JOptionPane.WARNING_MESSAGE); // Shared.error ->
																				// title bar
				return false;
			}
		}

		if (ex.hasConfigProperty (AbstractDataExtractor.INTERVAL_SIZE)) {
			String text = intervalSize.getText ();
			if (Check.isEmpty (text) || !Check.isDouble (text)) {
				JOptionPane.showMessageDialog (this, Translator
						.swap ("DataExtractor.intervalSizeMustBeANumber"), Translator
						.swap ("Shared.error"), JOptionPane.WARNING_MESSAGE); // Shared.error ->
																				// title
																				// bar
				return false;
			}
			double d = Check.doubleValue (intervalSize.getText ());
			if (d <= 0) {
				JOptionPane.showMessageDialog (this, Translator
						.swap ("DataExtractor.intervalSizeMustBeGreaterThanZero"), Translator
						.swap ("Shared.error"), JOptionPane.WARNING_MESSAGE); // Shared.error ->
																				// title bar
				return false;
			}
		}

		if (ex.hasConfigProperty (AbstractDataExtractor.IC_NUMBER_OF_SIMULATIONS)) {
			String text = icNumberOfSimulations.getText ();
			if (Check.isEmpty (text) || !Check.isInt (text)) {
				JOptionPane.showMessageDialog (this, Translator
						.swap ("DataExtractor.icNumberOfSimulationsMustBeInteger"), Translator
						.swap ("Shared.error"), JOptionPane.WARNING_MESSAGE); // Shared.error ->
																				// title bar
				return false;
			}
		}

		if (ex.hasConfigProperty (AbstractDataExtractor.IC_RISK)) {
			String text = icRisk.getText ();
			if (Check.isEmpty (text) || !Check.isDouble (text)) {
				JOptionPane.showMessageDialog (this, Translator
						.swap ("DataExtractor.icRiskMustBeANumber"), Translator
						.swap ("Shared.error"), JOptionPane.WARNING_MESSAGE); // Shared.error ->
																				// title
																				// bar
				return false;
			}
		}

		if (ex.hasConfigProperty (AbstractDataExtractor.IC_PRECISION)) {
			String text = icPrecision.getText ();
			if (Check.isEmpty (text) || !Check.isDouble (text)) {
				JOptionPane.showMessageDialog (this, Translator
						.swap ("DataExtractor.icPrecisionMustBeANumber"), Translator
						.swap ("Shared.error"), JOptionPane.WARNING_MESSAGE); // Shared.error ->
																				// title
																				// bar
				return false;
			}
		}

		// Give a chance to the real extractor subclass to perform functional
		// checks
		if (!ex.functionalTestsAreOk (this)) { return false; }

		// check intProperties type (must be int)
		Iterator keys = intPropertiesTextFields.keySet ().iterator ();
		Iterator values = intPropertiesTextFields.values ().iterator ();
		while (keys.hasNext () && values.hasNext ()) {
			String name = (String) keys.next ();
			JTextField f = (JTextField) values.next ();
			if (ex.isIndividualProperty (name)) {
				continue;
			}

			String value = f.getText ();
			try {
				new Integer (value);
			} catch (Exception e) {
				JOptionPane.showMessageDialog (this, Translator.swap (name) + " "
						+ Translator.swap ("DataExtractor.mustBeAnInteger"), Translator
						.swap ("Shared.error"), JOptionPane.WARNING_MESSAGE); // Shared.error ->
																				// title
																				// bar
				return false;
			}
		}

		// check doubleProperties type (must be double)
		keys = doublePropertiesTextFields.keySet ().iterator ();
		values = doublePropertiesTextFields.values ().iterator ();
		while (keys.hasNext () && values.hasNext ()) {
			String name = (String) keys.next ();
			JTextField f = (JTextField) values.next ();
			if (ex.isIndividualProperty (name)) {
				continue;
			}

			String value = f.getText ();
			try {
				new Double (value);
			} catch (Exception e) {
				JOptionPane.showMessageDialog (this, Translator.swap (name) + " "
						+ Translator.swap ("DataExtractor.mustBeADouble"), Translator
						.swap ("Shared.error"), JOptionPane.WARNING_MESSAGE); // Shared.error ->
																				// title
																				// bar
				return false;
			}
		}

		// check setProperties type (must be a list of string separated by ',')
		// TO BE REVIEWED
		//
		keys = ex.settings.setProperties.keySet ().iterator ();
		values = ex.settings.setProperties.values ().iterator ();
		while (keys.hasNext () && values.hasNext ()) {
			String name = (String) keys.next ();
			ItemSelector is = (ItemSelector) values.next ();
			if (ex.isIndividualProperty (name)) {
				continue;
			}

			// Check here
			/*
			 * try {
			 * 
			 * } catch (Exception e) { JOptionPane.showMessageDialog (this, name+" "+Translator.swap
			 * ("DataExtractor.mustBeAListOfStrings"), Translator.swap ("Shared.error"),
			 * JOptionPane.WARNING_MESSAGE ); // Shared.error -> title bar return false; }
			 */
		}

		// check for combo boxes properties can not be wrong
		// no possible error
		// fc - 1.6.2004

		// fc - 23.3.2004
		if (grouperChooser != null && grouperChooser.isGrouperAvailable ()) {
			if (grouperChooser.getGrouperName () == null
					|| grouperChooser.getGrouperName ().equals ("")) {
				MessageDialog.print (this, Translator.swap ("DEMultiConfPanel.wrongGrouperName"));
				return false;
			}
		}

		// Add here a control for calibration data : if some model is selected
		// in the comboProperty, we must be in hectare mode, else, prompt
		// user and return false;
		// fc - 14.10.2004
		if (ex.hasConfigProperty ("activateCalibration")) {

			// We can not use here directly :
			// String modelName = getComboProperty ("activateCalibration");
			// because this method runs before this value is updated in
			// DataExtractor.multiConfigure ()
			// fc - 14.10.2004
			JComboBox cb = (JComboBox) comboPropertiesComboBoxes.get ("activateCalibration");
			String modelName = (String) cb.getSelectedItem ();

			if (!modelName.equals (Translator.swap ("Shared.noneFeminine"))) {
				// ~ if (!ex.hasConfigProperty (DataExtractor.HECTARE) ||
				// !perHa.isSelected ()) {
				if (ex.hasConfigProperty (AbstractDataExtractor.HECTARE) && !perHa.isSelected ()) {
					MessageDialog.print (this, Translator
							.swap ("DEMultiConfPanel.calibrationRequiresHectareProperty"));
					return false;
				}
			}
		}

		// Special test for genetics extractors : group must contain Genotypable
		// indivs (only)
		// and selected loci must exist in indivs.
		// fc - 23.12.2004
		if (ex.hasConfigProperty ("afLociIds") || ex.hasConfigProperty ("gfLociIds")) {

			// Which loci were selected ?
			Set loci = null;
			if (ex.hasConfigProperty ("afLociIds")) {
				loci = ((ItemSelector) ex.settings.setProperties.get ("afLociIds")).selectedValues;
			} else {
				loci = ((ItemSelector) ex.settings.setProperties.get ("gfLociIds")).selectedValues;
			}
			if (loci == null || loci.isEmpty ()) {
				MessageDialog.print (this, Translator.swap ("Shared.noLociWereSelected"));
				return false;
			}

			// Which individuals to consider ? (group or not)
			GScene stand = ex.getStep ().getScene ();
			if (!(stand instanceof GeneticScene)) {
				MessageDialog.print (this, Translator.swap ("Shared.standMustBeAGeneticScene"));
				return false;
			}
			GeneticScene scene = (GeneticScene) stand;
			Collection indivs = scene.getGenotypables ();

			String grouperType = ex.getCGrouperType ();

			if (grouperChooser != null && grouperChooser.isGrouperAvailable ()) {
				String grouperName = grouperChooser.getGrouperName ();
				if (grouperName != null && !grouperName.equals ("")) {
					indivs = GrouperManager.getInstance ().getGrouper (grouperName)
							.apply (indivs, grouperName.toLowerCase ().startsWith ("not "));
				}
			}

			GenoSpecies prevSpecies = null;
			Genotypable gee = null;
			for (Iterator i = indivs.iterator (); i.hasNext ();) {
				Object indiv = i.next ();
				if (!(indiv instanceof Genotypable && ((Genotypable) indiv).getGenotype () != null)) {
					MessageDialog.print (this, Translator.swap ("Shared.mustBeAllGenotyped"));
					return false;
				}
				gee = (Genotypable) indiv;
				if (prevSpecies == null) {
					prevSpecies = gee.getGenoSpecies ();
				} else {
					if (!gee.getGenoSpecies ().equals (prevSpecies)) {
						MessageDialog.print (this, Translator.swap ("Shared.mustBeAllSameSpecies"));
						return false;
					}
				}

			}
			if (gee == null) {
				MessageDialog.print (this, Translator.swap ("Shared.oneGenotypableNeededAtLeast"));
				return false;
			}

		}

		return true;
	}


	
	public DataBlock getDataBlock () {
		return dataBlock;
	}


	// This text field can be cleared if the spied object notifies it to do so.
	// It is used here with ItemSelector.
	//
	private class SpyJTextField extends JTextField implements Spy {

		public SpyJTextField (Spiable o, String text, int columns) {
			super (text, columns);
			o.setSpy (this); // he he
		}

		public void action (Spiable m, Object sth) {
			setText ("");
		}
	}

}
