/** 
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 1999-2010 INRA 
 * 
 * Authors: F. de Coligny, S. Dufour-Kowalski, 
 * 
 * This file is part of Capsis
 * Capsis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Capsis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU lesser General Public License
 * along with Capsis.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package capsis.commongui.command;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import jeeb.lib.util.AmapTools;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Translator;
import capsis.app.CapsisExtensionManager;
import capsis.extension.memorizer.FrequencyMemorizer;
import capsis.kernel.MemorizerFactory;
import capsis.kernel.Project;
import capsis.kernel.extensiontype.Memorizer;

/**
 * Configuration panel for scenario memory options.
 *
 * @author F. de Coligny - october 2002
 */
public class MemorizerPanel extends LinePanel implements ItemListener {
	
	private static final long serialVersionUID = 1L;
	
	private Project scenario;
	private JComboBox combo;
	private JComponent witness;
	private String lastSelectedOption = "";
	private Map<String, String> class_label;
	private Map<String, String> label_class;
	
	/** 
	 * Constructor.
	 */
	public MemorizerPanel (Project scenario) {
		super ();
		this.scenario = scenario;
		
		makeOptions ();
		createUI ();
	}
	
	/** 
	 * Prepare possible options.
	 */
	private void makeOptions () {
		// Memorizer name - className
		class_label = new HashMap<String, String> ();
		
		Collection<String> classNames = CapsisExtensionManager.getInstance ().getExtensionClassNames (
				CapsisExtensionManager.MEMORIZER, scenario.getModel ());
		
		for (Iterator<String> i = classNames.iterator (); i.hasNext ();) {
			String className = (String) i.next ();
			String classLittleName = AmapTools.getClassSimpleName (className);	// wo package
			class_label.put (classLittleName, Translator.swap (classLittleName));
		}
		
		label_class = new HashMap<String, String> ();
		Iterator<String> c = class_label.keySet ().iterator ();
		Iterator<String> l = class_label.values ().iterator ();
		while (c.hasNext () && l.hasNext ()) {
			label_class.put ((String) l.next (), (String) c.next ());
		}
	}
	
	/** 
	 * Called when an item is selected in combo.
	 */
	public void itemStateChanged (ItemEvent evt) {
		if (evt.getSource().equals (combo)) {
			Object o = evt.getItem ();
			String option = (String) o;
			if (!(option.equals (lastSelectedOption))) {
				lastSelectedOption = option;
				optionChangeAction (option);
			}
		}
	}
	
	/** 
	 * Action done on combo choice.
	 */
	private void optionChangeAction (String option) {
		
		String className = (String) label_class.get (option);
		
		if (className.equals ("DefaultMemorizer")) {
			this.scenario.setMemorizer (MemorizerFactory.createDefaultMemorizer ());
			
			
		} else if (className.equals ("FrequencyMemorizer")) {
			
			int frequency = 5;
			// Get Frequency
			FrequencyMemorizerPanel dlg = new FrequencyMemorizerPanel (frequency);
			if (dlg.isValidDialog ()) {
				frequency = dlg.getFrequency ();
			}
			dlg.dispose ();
			this.scenario.setMemorizer (MemorizerFactory.createFrequencyMemorizer (frequency));	
			
			
		} else if (className.equals ("CompactMemorizer")) {
			this.scenario.setMemorizer (MemorizerFactory.createCompactMemorizer ());
			
			
		}
		
		showWitness (className);
		
	}
	
	/** 
	 * Choice may have a witness component to show current parameter.
	 */
	private void showWitness (String selectedOption) {
		if (selectedOption.equals ("FrequencyMemorizer")) {
			witness = new JTextField (5);	// do not add directly
			((JTextField) witness).setEditable (false);
			FrequencyMemorizer m = (FrequencyMemorizer) this.scenario.getMemorizer ();
			((JTextField) witness).setText ("f="+m.getFrequency ());
			add (witness);
		} else {
			try {
				remove (witness);
			} catch (Exception e) {}
		}
		revalidate ();
		
	}
	
	/** 
	 * Create the gui.
	 */
	private void createUI () {
		
		// set current option for scenario...
		Memorizer m = this.scenario.getMemorizer ();
//		String className = AmapTools.getClassSimpleName (m.getClass ().getName ());
		String className = m.getClass ().getSimpleName ();
		
		combo = new JComboBox (new Vector<String> (label_class.keySet ()));
		combo.setSelectedItem ((String) class_label.get (className));
		lastSelectedOption = (String) class_label.get (className);
		combo.addItemListener (this);
		
		add (combo);
		
		showWitness (className);
		
	}
	

}

