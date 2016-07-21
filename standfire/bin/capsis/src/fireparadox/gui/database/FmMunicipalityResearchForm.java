package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;

import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import fireparadox.model.FmModel;
import fireparadox.model.FmInitialParameters;
import fireparadox.model.database.FmDBCommunicator;
import fireparadox.model.database.FmDBCountry;
import fireparadox.model.database.FmDBMunicipality;


/**	FiMunicipalityResearchForm : research criterias for FiDBMunicipality objects
*
*	@author I. Lecomte - february 2008
*/
public class FmMunicipalityResearchForm extends JPanel implements ActionListener {

	private FmModel model;
	private FmDBCommunicator bdCommunicator;			//to read database
	private FmInitialParameters settings;
	private int rightLevel;							//to manage user rights

	//searching criterias : country
    private JComboBox countryCombo;
    private LinkedHashMap  countryMap;
    private LinkedHashMap  countryMapCode;

	//to store and display results
    private LinkedHashMap  municipalityMap;
    private JTable resultTable;
    private JScrollPane listSP;
    private FmMunicipalityTableModel tableModel;

	/**	Constructor.
	*/
    public FmMunicipalityResearchForm (FmModel _model, int _rightLevel) {

		model = _model;
		bdCommunicator = model.getBDCommunicator ();
		settings = model.getSettings ();
		rightLevel = _rightLevel;

		//load countries for combo box
		countryCombo = new JComboBox ();
		loadCountries();

		//load municipality list with country  selection
		tableModel = new  FmMunicipalityTableModel (rightLevel);
		loadAllData();

		createUI();
    }

	/**	Load countries
	*/
	private void loadCountries () {

		countryCombo.removeAllItems();
		countryCombo.addItem ("---");

		try {
			countryMap = model.getCountryMap ();
			countryMapCode = new LinkedHashMap ();
			for (Iterator i = countryMap.keySet().iterator(); i.hasNext ();) {
		   		Object cle = i.next();
		   		FmDBCountry t = (FmDBCountry) countryMap.get(cle);
		   		countryMapCode.put (t.getCountryCode(), t.getCountryId());
		   		countryCombo.addItem (t.getCountryCode());
			}

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiMunicipalityResearchForm ()", "error while opening COUNTRY data base", e);
		}

	}
	/**	Load all municipalities
	*/
	public void loadAllData () {
		try {
			tableModel. clear();
			municipalityMap = bdCommunicator.getMunicipalities (countryMap);

			for (Iterator i = municipalityMap.keySet().iterator(); i.hasNext ();) {
				Object cle = i.next();
				FmDBMunicipality t = (FmDBMunicipality) municipalityMap.get(cle);

				//only super administrators can see deleted objects
				if ((rightLevel < 9) && t.isDeleted()) {}
				else tableModel.addMunicipality  (t);

			}

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiMunicipalityResearchForm ()", "error while opening MUNICIPALITY data base", e);
		}

	}

	/**	Load municipalities with research criterias
	*/
	public void loadData () {

		tableModel. clear();

		//country selection
		String countrySel = new String("");
		if(countryCombo.getSelectedIndex () > 0) countrySel = countryCombo.getSelectedItem ().toString ();

		for (Iterator i = municipalityMap.keySet().iterator(); i.hasNext ();) {
			Object cle = i.next();
			FmDBMunicipality t = (FmDBMunicipality) municipalityMap.get(cle);
			if ((countrySel.equals("")) ||
				(t.getCountry().getCountryCode().equals(countrySel))) {

				//only super administrators can see deleted objects
				if ((rightLevel < 9) && t.isDeleted()) {}
				else tableModel.addMunicipality  (t);
			}
		}
	}

	/**	Changing country criteria
	 */
    public void actionPerformed(ActionEvent e) {
		if (e.getSource ().equals (countryCombo)) {
			loadData ();
			repaint ();
		}
	}

	/**	Initialize the GUI.
	 */
    private void createUI() {

		ColumnPanel municipalityDBPanel = new ColumnPanel ();

		// 1. CriteriaBorder
		ColumnPanel colCriteria = new ColumnPanel ();
		Border etchedCrit = BorderFactory.createEtchedBorder ();
		Border bCrit = BorderFactory.createTitledBorder (etchedCrit, Translator.swap (
				"FiMunicipalityResearchForm.critereBorder"));
		colCriteria.setBorder (bCrit);

		LinePanel ligCrit1 = new LinePanel ();

		// 1. selection
		JLabel lbCountry = new JLabel(Translator.swap ("FiMunicipalityResearchForm.country"));
		ColumnPanel colCountry = new ColumnPanel ();
		countryCombo.addActionListener (this);
		countryCombo.setPreferredSize (new Dimension (100,20));
		colCountry.add (lbCountry);
		colCountry.add (countryCombo);
		ligCrit1.add (colCountry);

		colCriteria.add (ligCrit1);

		// 2. ResultBorder
		LinePanel ligResult = new LinePanel ();
		Border etchedRes = BorderFactory.createEtchedBorder ();
		Border bRes = BorderFactory.createTitledBorder (etchedRes, Translator.swap (
				"FiMunicipalityResearchForm.resultBorder"));
		ligResult.setBorder (bRes);

		// 3. Result Table
		ColumnPanel colTable = new ColumnPanel ();
		resultTable = new JTable(tableModel);
		listSP = new JScrollPane(resultTable);
		listSP.setPreferredSize (new Dimension (300,240));
		colTable.add (listSP);
		ligResult.add (colTable);

		municipalityDBPanel.add (colCriteria);
		municipalityDBPanel.add (ligResult);

		setLayout (new BorderLayout ());
		add (municipalityDBPanel, BorderLayout.NORTH);

    }

	//return the selected rows in the displayed table
	public int[] getSelectedRows () {
		return resultTable.getSelectedRows();
	}

	//return the site store at this index in the displayed table
	public FmDBMunicipality getMunicipality (int index) {
		return tableModel. getMunicipality (index);
	}

	//return the selected rows in the displayed table
	public LinkedHashMap  getCountryList() {
		return countryMap;
	}

}




