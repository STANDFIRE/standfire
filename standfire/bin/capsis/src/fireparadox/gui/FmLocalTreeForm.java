package fireparadox.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import capsis.lib.fire.FiConstants;
import capsis.lib.fire.fuelitem.FiSpecies;
import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Listener;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import fireparadox.model.FmModel;


/**	A form to create Ph. Dreyfus trees.
*	@author F. Pimont, F. de Coligny - june 2009
*/
public class FmLocalTreeForm extends JPanel implements ActionListener, 
		ListenedTo {

	private FmModel model;
	private Collection<Listener> listeners;
	private Collection<String> speciesNames;
	
	private JComboBox speciesCombo;
	private JTextField dominantHeight;
	private JTextField meanAge;
	private JTextField ageStandardDeviation;
	private JTextField stemDensity;
	private JTextField liveMoisture;
	private JTextField deadMoisture;

	
	/**	Constructor.
	*/
	public FmLocalTreeForm (FmModel model) {
		this.model = model;
		
		// These species names should be in the data base
		// i.e. in the speciesList in FiModel
		// otherwise, trouble may occur when constructing a tree from this form
		Collection <FiSpecies> speciesList = model.getLocalTreeSpeciesList(); 
		speciesNames = new Vector<String> ();
		for (FiSpecies sp : speciesList) {
			speciesNames.add (sp.getName());
		}
		createUI ();
	}
	
	/**	If buttons are hit / textfields are validated
	*/
	public void actionPerformed (ActionEvent e) {
		// Tell listeners
		tellSomethingHappened (null);
		
		//~ if (e.getSource ().equals (add)) {
			//~ addAction ();
		//~ } else if (e.getSource ().equals (remove)) {
			//~ removeAction ();
		//~ } else if (e.getSource ().equals (clear)) {
			//~ clearAction ();
		//~ } else if (e.getSource ().equals (showHideEditor)) {
			//~ showHideEditorAction ();
		//~ } else {
			//~ synchro ();
		//~ }
	}

	/**	Tests correctness of the form.
	 */
	public boolean isCorrect () {
		// check all user entries, in case of trouble, tell him and return false 
		// DominantHeight
		if (!Check.isDouble(dominantHeight.getText().trim())) {
			MessageDialog
					.print(
							this,
							Translator
									.swap("FiLocalTreeForm.dominantHeightShouldBeADoubleGreaterThanZero"));
			return false;
		}

		double dh = Check.doubleValue(dominantHeight.getText().trim());
		if (dh <= 0) {
			MessageDialog
					.print(
							this,
							Translator
									.swap("FiLocalTreeForm.dominantHeightShouldBeADoubleGreaterThanZero"));
			return false;
		}
		Settings.setProperty("fi.local.tree.form.last.dominantHeight", "" + dh);
		
		// Mean age
		if (!Check.isDouble (meanAge.getText ().trim ())) {
			MessageDialog.print (this, 
					Translator.swap ("FiLocalTreeForm.meanAgeShouldBeADoubleGreaterThanZero"));
			return false;
		}

		double a = Check.doubleValue (meanAge.getText ().trim ());
		if (a <= 0) {
			MessageDialog.print (this, 
					Translator.swap ("FiLocalTreeForm.meanAgeShouldBeADoubleGreaterThanZero"));
			return false;
		}
		Settings.setProperty("fi.local.tree.form.last.meanAge", "" + a);
		
		// Age standard deviation
		if (!Check.isDouble (ageStandardDeviation.getText ().trim ())) {
			MessageDialog.print (this, 
					Translator.swap ("FiLocalTreeForm.ageStandardDeviationShouldBeADoubleGreaterThanZero"));
			return false;
		}

		double asd = Check.doubleValue (ageStandardDeviation.getText ().trim ());
		if (asd < 0) {
			MessageDialog.print (this, 
					Translator.swap ("FiLocalTreeForm.ageStandardDeviationShouldBeADoubleGreaterThanZero"));
			return false;
		}
		Settings.setProperty("fi.local.tree.form.last.ageStandardDeviation", ""
				+ asd);
		
		// StemDensity
		if (!Check.isDouble(stemDensity.getText().trim())) {
			MessageDialog
					.print(
							this,
							Translator
									.swap("FiLocalTreeForm.stemDensityShouldBeADoubleGreaterThanZero"));
			return false;
		}

		double sd = Check.doubleValue(stemDensity.getText().trim());
		if (sd <= 0) {
			MessageDialog
					.print(
							this,
							Translator
									.swap("FiLocalTreeForm.stemDensityShouldBeADoubleGreaterThanZero"));
			return false;
		}
		Settings.setProperty("fi.local.tree.form.last.stemDensity", "" + sd);
		
		// moisture
		if (!Check.isDouble(liveMoisture.getText().trim())
				|| !Check.isDouble(deadMoisture.getText().trim())) {
			MessageDialog.print(this, Translator.swap("FiLocalTreeForm.moistureShouldBeADoubleGreaterThanZero"));
			return false;
		}

		double m = Check.doubleValue(liveMoisture.getText().trim());
		if (m <= 0) {
			MessageDialog
					.print(
							this,
							Translator
									.swap("FiLocalTreeForm.moistureShouldBeADoubleGreaterThanZero"));
			return false;
		}
		Settings.setProperty("fi.local.tree.form.last.liveMoisture", "" + m);

		m = Check.doubleValue(deadMoisture.getText().trim());
		if (m <= 0) {
			MessageDialog.print(this, Translator.swap("FiLocalTreeForm.moistureShouldBeADoubleGreaterThanZero"));
			return false;
		}
		Settings.setProperty("fi.local.tree.form.last.deadMoisture", "" + m);
		
		return true;
	}
	
	/**	ListenedTo interface. Add a listener to this object.
	*/
	public void addListener (Listener l) {
		if (listeners == null) {listeners = new ArrayList<Listener> ();}
		listeners.add (l);
	}

	/**	ListenedTo interface. Remove a listener to this object.
	*/
	public void removeListener (Listener l) {
		if (listeners == null) {return;}
		listeners.remove (l);
	}

	/**	ListenedTo interface. Notify all the listeners by calling their somethingHappened (listenedTo, param) method.
	*/
	public void tellSomethingHappened (Object param) {
		if (listeners == null) {return;}
		for (Listener l : listeners) {
			l.somethingHappened (this, param);	// param may be null
		}
	}
	
	/**	Initialize the GUI.
	*/
	public void createUI() {

		ColumnPanel main = new ColumnPanel ();
		
		// Species
		LinePanel l1 = new LinePanel ();
		l1.add (new JWidthLabel (Translator.swap ("FiLocalTreeForm.species")+" : ", 100));
		speciesCombo = new JComboBox (new Vector (speciesNames));
		speciesCombo.addActionListener (this);
		l1.add (speciesCombo);
		l1.addStrut0 ();
		main.add (l1);
		
		// DominantHeight
		LinePanel l2 = new LinePanel ();
		l2.add(new JWidthLabel(Translator
				.swap("FiLocalTreeForm.dominantHeight")
				+ " : ", 200));
		dominantHeight = new JTextField(5);
		try {
			String v = System
					.getProperty("fi.local.tree.form.last.dominantHeight");
			if (v == null) {
				v = "10.0";
			}
			dominantHeight.setText(v); 
		} catch (Exception e) {
		}
		dominantHeight.addActionListener(this);
		l2.add(dominantHeight);
		l2.addStrut0 ();
		main.add (l2);
		
		// Age mean
		LinePanel l3 = new LinePanel();
		l3.add(new JWidthLabel(Translator.swap("FiLocalTreeForm.meanAge")
				+ " : ", 200));
		meanAge = new JTextField(5);
		try {
			String v = System.getProperty("fi.local.tree.form.last.meanAge");
			if (v == null) {
				v = "30";
			}
			meanAge.setText(v);
		} catch (Exception e) {
		}
		meanAge.addActionListener(this);
		l3.add(meanAge);
		l3.addStrut0();
		main.add(l3);
		
		// Age standard deviation
		LinePanel l4 = new LinePanel();
		l4.add(new JWidthLabel(Translator
				.swap("FiLocalTreeForm.ageStandardDeviation")
				+ " : ", 200));
		ageStandardDeviation = new JTextField (5);
		try {
			String v = System
					.getProperty("fi.local.tree.form.last.ageStandardDeviation");
			if (v == null) {
				v = "0";
			}
			ageStandardDeviation.setText(v);
		} catch (Exception e) {
		}
		ageStandardDeviation.addActionListener (this);
		l4.add(ageStandardDeviation);
		l4.addStrut0();
		main.add(l4);

		// StemDensity
		LinePanel l5 = new LinePanel();
		l5.add(new JWidthLabel(Translator.swap("FiLocalTreeForm.stemDensity")
				+ " : ", 200));
		stemDensity = new JTextField(5);
		try {
			String v = System
					.getProperty("fi.local.tree.form.last.stemDensity");
			if (v == null) {
				v = "400";
			}
			stemDensity.setText(v);
		} catch (Exception e) {
		}
		stemDensity.addActionListener(this);
		l5.add(stemDensity);
		l5.addStrut0();
		main.add(l5);
		
		// Moisture
		LinePanel l6 = new LinePanel();
		l6.add(new JWidthLabel(Translator.swap("FiLocalTreeForm.liveMoisture")
				+ " : ", 200));
		liveMoisture = new JTextField(5);
		try {
			String v = System
					.getProperty("fi.local.tree.form.last.liveMoisture");
			if (v == null) {
				v = "" + FiConstants.TREE_MOISTURE;
			} // to get default value
			liveMoisture.setText(v);
		} catch (Exception e) {
		}
		liveMoisture.addActionListener(this);
		l6.add(liveMoisture);
		l6.addStrut0();
		main.add(l6);
		
		LinePanel l7 = new LinePanel();
		l7.add(new JWidthLabel(Translator.swap("FiLocalTreeForm.deadMoisture")
				+ " : ", 200));
		deadMoisture = new JTextField(5);
		try {
			String v = System
					.getProperty("fi.local.tree.form.last.deadMoisture");
			if (v == null) {
				v = "" + FiConstants.DEAD_MOISTURE;
			} // to get default value
			deadMoisture.setText(v);
		} catch (Exception e) {
		}
		deadMoisture.addActionListener(this);
		l7.add(deadMoisture);
		l7.addStrut0();
		main.add(l7);
		
		
		setLayout (new BorderLayout ());
		add (main, BorderLayout.NORTH);
		
    }


	/**	Accessor
	*/
	public String getSpeciesName () {return (String) speciesCombo.getSelectedItem ();}

	/**	Accessor
	*/
	public double getDominantHeight() {
		return Check.doubleValue(dominantHeight.getText().trim());
	}

	public double getMeanAge () {return Check.doubleValue (meanAge.getText ().trim ());}

	/**	Accessor
	*/
	public double getAgeStandardDeviation () {return Check.doubleValue (ageStandardDeviation.getText ().trim ());}
	/**
	 * Accessor
	 */
	public double getStemDensity() {
		return Check.doubleValue(stemDensity.getText().trim());
	}

	public double getLiveMoisture() {
		return Check.doubleValue(liveMoisture.getText().trim());
	}

	public double getDeadMoisture() {
		return Check.doubleValue(deadMoisture.getText().trim());
	}
}
