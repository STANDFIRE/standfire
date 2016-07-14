package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBCountry;
import fireparadox.model.database.FmDBMunicipality;
import fireparadox.model.database.FmDBUpdator;

/** FiMunicipalityEditor : Creation/Modification of a FiDBMunicipality in DB4O database
 *
 * @author I. Lecomte - December 2007
 */
public class FmMunicipalityEditor extends AmapDialog implements ActionListener {

	private FmModel model;
	private FmDBUpdator bdUpdator;				//to update database

	//FiDBMunicipality to update
	private FmDBMunicipality municipality;
	private long municipalityId;
	private boolean deleteAction;					//if true, delete/undelete action is required for this team

	//Fields for user interface
	private FmDBCountry country;
	private JTextField nameField;
	private JComboBox countryComboBox;
	private JButton ok;
	private JButton cancel;
	private JButton help;

	//Existing countries
	private LinkedHashMap  countryList= new LinkedHashMap  ();
	private LinkedHashMap  countryMap = new LinkedHashMap  ();

	//local data for retrieving user entries
	private String name, countryName;
	private long countryId;


	/**
	 * Constructor : UPDATE if municipality is not null
	 */
	public FmMunicipalityEditor (FmModel _model,  LinkedHashMap  _countryList, FmDBMunicipality _municipality, boolean _deleteAction) {

		super ();
		model = _model;
		deleteAction = _deleteAction;

		//municipality field loading
		municipalityId = -1;
		countryId = 0;
		name = new String("");
		countryName = new String("");
		country = null;

		municipality = _municipality;
		if (municipality != null) {
			municipalityId = municipality.getMunicipalityId();
			name = municipality.getMunicipalityName();
			country = municipality.getCountry();
			if (country != null) {
				countryName = country.getCountryCode();
				countryId = country.getCountryId();
			}
		}

		//load country list to fill comboBox
		countryList = _countryList;
		countryMap = new LinkedHashMap ();
		for (Iterator i = countryList.keySet().iterator(); i.hasNext ();) {
			Object cle = i.next();
			FmDBCountry t = (FmDBCountry) countryList.get(cle);
			countryMap.put (t.getCountryCode(),t.getCountryId());
		}

		createUI ();
		pack ();
		show ();
	}

	/**	Actions on the buttons
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (ok)) {
			validateAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**	Validation = save in the database
	 */
	private void validateAction () {

		if (controlValues ()) {		//if control OK

			//country
			country = null;
			int n = countryComboBox.getSelectedIndex();
			if (n >= 0) {
				String code = (String) countryComboBox.getSelectedItem ();
				Long id = (Long) countryMap.get (code);
				country = (FmDBCountry) countryList.get (id);
			}

			name = nameField.getText ();

			//new municipality CREATION
			if (municipality == null) {
				municipalityId = -1;
				boolean deleted = false;
				FmDBMunicipality newMunicipality = new FmDBMunicipality (municipalityId, name, country, deleted);

				try {
					bdUpdator = model.getBDUpdator ();
					bdUpdator.createMunicipality (newMunicipality);			//update in the database
					newMunicipality = null;									//to clear memory

				} catch (Exception e) {
					Log.println (Log.ERROR, "FiMunicipalityEditor.validateAction ()", "error while UPDATING MUNICIPALITY data base", e);
				}
			}
			//municipality UPDATE
			else if ((municipality != null) && (deleteAction != true)) {
				boolean deleted = false;
				FmDBMunicipality newMunicipality = new FmDBMunicipality (municipalityId, name, country, deleted);

				try {
					bdUpdator = model.getBDUpdator ();
					bdUpdator.updateMunicipality (municipality, newMunicipality);	//update in the database
					newMunicipality = null;											//to clear memory

				} catch (Exception e) {
					Log.println (Log.ERROR, "FiMunicipalityEditor.validateAction ()", "error while UPDATING MUNICIPALITY data base", e);
				}
			}
			//municipality REACTIVATION/DESACTIVATION
			else if ((municipality != null) && (deleteAction == true)) {
				boolean deleted = false;
				if (!municipality.isDeleted()) deleted = true;
				FmDBMunicipality newMunicipality = new FmDBMunicipality (municipalityId, name, country, deleted);

				try {
					bdUpdator = model.getBDUpdator ();
					bdUpdator.updateMunicipality (municipality, newMunicipality);	//update in the database
					newMunicipality = null;											//to clear memory

				} catch (Exception e) {
					Log.println (Log.ERROR, "FiMunicipalityEditor.validateAction ()", "error while UPDATING MUNICIPALITY data base", e);
				}
			}

			setValidDialog (true);
		}
	}

	/**	Control the value in the ComboBoxes and the TextFields
	 */
	 private boolean controlValues ()   {

		//Check the name
		if (Check.isEmpty (nameField.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiMunicipalityEditor.municipalityNameIsEmpty"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}

		//Check the country
		int n = countryComboBox.getSelectedIndex();
		if (n <= 0) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiMunicipalityEditor.countryIsEmpty"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}
		return true;
	}

	/**	Initialize the GUI.
	 */
	private void createUI () {

		/*********** MUNICIPALITY panel **************/
		JPanel municipalityPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
		Box box = Box.createVerticalBox ();

		JPanel f1 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f2 = new JPanel (new FlowLayout (FlowLayout.LEFT));

		nameField = new JTextField (25);
		nameField.setText (""+name);

		countryComboBox = new JComboBox ();
		countryComboBox.addItem ("-----------------");
		for (Iterator i = countryMap.keySet().iterator(); i.hasNext ();) {
			String countryName = (String) i.next();
		   	countryComboBox.addItem (countryName);
		}

		if ((countryName != null) && (countryName.compareTo("") != 0))
			countryComboBox.setSelectedItem (countryName);
		else
			countryComboBox.setSelectedIndex(0);

		f1.add (new JWidthLabel (Translator.swap ("FiMunicipalityEditor.municipalityName")+" :",150));
		f1.add (nameField);
		f2.add (new JWidthLabel (Translator.swap ("FiMunicipalityEditor.country")+" :",150));
		f2.add (countryComboBox);

		box.add(f1);
		box.add(f2);

		municipalityPanel.add (box);

		/*********** CONTROL panel **************/
		JPanel controlPanel = new JPanel ();
		controlPanel.setLayout (new FlowLayout (FlowLayout.RIGHT));

		if ((municipality != null) && (deleteAction == true)) {
			if (municipality.isDeleted())
				ok = new JButton (Translator.swap ("FiMunicipalityEditor.unsupress"));
			else
				ok = new JButton (Translator.swap ("FiMunicipalityEditor.supress"));
		}
		else
			ok = new JButton (Translator.swap ("FiMunicipalityEditor.validate"));

		ok.addActionListener (this);
		controlPanel.add (ok);
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		cancel.addActionListener (this);
		controlPanel.add (cancel);
		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		controlPanel.add (help);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (municipalityPanel, "Center");
		getContentPane ().add (controlPanel, "South");
		setTitle (Translator.swap ("FiMunicipalityEditor.title"));

		
		setModal (true);
	}

}
