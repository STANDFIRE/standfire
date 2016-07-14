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
import fireparadox.model.database.FmDBSite;


/**	FiSiteResearchForm : research criterias for FiDBSite objects
*
*	@author I. Lecomte - december 2007
*/
public class FmSiteResearchForm extends JPanel implements ActionListener {

	private FmModel model;
	private FmDBCommunicator bdCommunicator;			//to read database
	private FmInitialParameters settings;
	private int rightLevel;							//to manage user rights

	//searching criterias : country and municipality
    private JComboBox countryCombo;
    private LinkedHashMap  countryMap;
    private LinkedHashMap  countryMapCode;

    private JComboBox municipalityCombo;
    private LinkedHashMap  municipalityMap;
    private LinkedHashMap  municipalityMapCode;

	//to store and display results
    private JTable resultTable;
    private JScrollPane listSP;
    private FmSiteTableModel tableModel;
    private LinkedHashMap  siteMap;


	/**	Constructor.
	*/
    public FmSiteResearchForm (FmModel _model, int _rightLevel) {

		model = _model;
		bdCommunicator = model.getBDCommunicator ();
		settings = model.getSettings ();
		rightLevel = _rightLevel;

		//loading data for combo box
		countryCombo = new JComboBox ();
		municipalityCombo = new JComboBox ();
		tableModel = new  FmSiteTableModel (rightLevel);

		loadAllData ();

		createUI();
    }

	/**	Load countries from database
	*/
	public void loadAllData () {

		countryCombo.removeAllItems();
		countryCombo.addItem ("---");
		municipalityCombo.removeAllItems();
		municipalityCombo.addItem ("-------------");
		tableModel. clear();

		try {
			//load countries
			countryMap = model.getCountryMap ();
			countryMapCode = new LinkedHashMap ();
			for (Iterator i = countryMap.keySet().iterator(); i.hasNext ();) {
		   		Object cle = i.next();
		   		FmDBCountry t = (FmDBCountry) countryMap.get(cle);
		   		countryMapCode.put (t.getCountryCode(), t.getCountryId());
		   		countryCombo.addItem (t.getCountryCode());
			}

			//load municipalities
			municipalityMap = bdCommunicator.getMunicipalities (countryMap);
			municipalityMapCode = new LinkedHashMap ();
			for (Iterator i = municipalityMap.keySet().iterator(); i.hasNext ();) {
		   		Object cle = i.next();
		   		FmDBMunicipality t = (FmDBMunicipality) municipalityMap.get (cle);
		   		municipalityMapCode.put (t.getMunicipalityName(),t.getMunicipalityId ());
		   		municipalityCombo.addItem (t.getMunicipalityName());
			}

			//load sites
			siteMap = bdCommunicator.getSites(municipalityMap);
			for (Iterator i = siteMap.keySet().iterator(); i.hasNext ();) {
				Object cle = i.next();
				FmDBSite t = (FmDBSite) siteMap.get(cle);

				//only super administrators can see deleted objects
				if ((rightLevel < 9) && t.isDeleted()) {}
				else {
					tableModel.addSite  (t);
				}
			}

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiSiteResearchForm ()", "error while opening data base", e);
		}

	}

	public void loadMunicipalities () {


		//country selection
		String countrySel = new String("");
		if(countryCombo.getSelectedIndex () > 0)
			countrySel = countryCombo.getSelectedItem ().toString ();


		municipalityCombo.removeAllItems();
		municipalityCombo.addItem ("-------------");

		for (Iterator i = municipalityMap.keySet().iterator(); i.hasNext ();) {
			Object cle = i.next();
			FmDBMunicipality t = (FmDBMunicipality) municipalityMap.get (cle);
			if ((countrySel.equals("")) ||
				(countrySel.equals(t.getCountry().getCountryCode()))) {

				municipalityMapCode.put (t.getMunicipalityName(),t.getMunicipalityId ());
				municipalityCombo.addItem (t.getMunicipalityName());
			}
		}

	}

	/**	Load site from database with research criterias (country - municipality)
	*/
	public void loadSites () {

		tableModel. clear();

		String countrySel = new String("");
		String municipalitySel = new String("");

		//country selection
		if(countryCombo.getSelectedIndex () > 0)
			countrySel = countryCombo.getSelectedItem ().toString ();

		//municipality selection
		if(municipalityCombo.getSelectedIndex () > 0)
			municipalitySel = municipalityCombo.getSelectedItem ().toString ();

		for (Iterator i = siteMap.keySet().iterator(); i.hasNext ();) {
			Object cle = i.next();
			FmDBSite t = (FmDBSite) siteMap.get(cle);
			String municipalityName = "";
			String countryCode = "";
			FmDBMunicipality muni = t.getMunicipality();
			if (muni != null) {
				municipalityName = muni.getMunicipalityName();
				countryCode = muni.getCountry().getCountryCode();
			}

			if ((municipalitySel.equals("")) ||
				(municipalityName.equals(municipalitySel))) {

				if ((countrySel.equals("")) ||
					(countryCode.equals(countrySel))) {

					//only super administrators can see deleted objects
					if ((rightLevel < 9) && t.isDeleted()) {}
					else {
						tableModel.addSite  (t);
					}
				}
			}
		}


	}

	/**	Changing criterias selection
	 */
    public void actionPerformed(ActionEvent e) {

		if (e.getSource ().equals (countryCombo)) {
			loadMunicipalities ();
			loadSites ();								//reload sites
			repaint ();
		}
		if (e.getSource ().equals (municipalityCombo)) {
			loadSites ();								//reload sites
			repaint ();
		}
	}

	/**	Initialize the GUI.
	 */
    private void createUI() {

		ColumnPanel siteDBPanel = new ColumnPanel ();

		// 1. CriteriaBorder
		ColumnPanel colCriteria = new ColumnPanel ();
		Border etchedCrit = BorderFactory.createEtchedBorder ();
		Border bCrit = BorderFactory.createTitledBorder (etchedCrit, Translator.swap (
				"FiPlantResearchForm.critereBorder"));
		colCriteria.setBorder (bCrit);

		LinePanel ligCrit1 = new LinePanel ();

		// 1 Country selection
		JLabel lbCountry = new JLabel(Translator.swap ("FiSiteResearchForm.country"));
		ColumnPanel colCountry = new ColumnPanel ();
		countryCombo.setPreferredSize (new Dimension(100,20));
		countryCombo.addActionListener (this);
		colCountry.add (lbCountry);
		colCountry.add (countryCombo);
		ligCrit1.add (colCountry);

		// 2 Municipality selection
		JLabel lbMunicipality = new JLabel(Translator.swap ("FiSiteResearchForm.municipality"));
		ColumnPanel colMunicipality = new ColumnPanel ();
		municipalityCombo.setPreferredSize (new Dimension(100,20));
		municipalityCombo.addActionListener (this);
		colMunicipality.add (lbMunicipality);
		colMunicipality.add (municipalityCombo);
		ligCrit1.add (colMunicipality);

		colCriteria.add (ligCrit1);

		// 3. ResultBorder
		LinePanel ligResult = new LinePanel ();
		Border etchedRes = BorderFactory.createEtchedBorder ();
		Border bRes = BorderFactory.createTitledBorder (etchedRes, Translator.swap (
				"FiSiteResearchForm.resultBorder"));
		ligResult.setBorder (bRes);

		// 4. Result Table
		ColumnPanel colTable = new ColumnPanel ();
		resultTable = new JTable(tableModel);
		listSP = new JScrollPane(resultTable);
		listSP.setPreferredSize (new Dimension (300,240));
		colTable.add (listSP);
		ligResult.add (colTable);

		siteDBPanel.add (colCriteria);
		siteDBPanel.add (ligResult);

		setLayout (new BorderLayout ());
		add (siteDBPanel, BorderLayout.NORTH);

    }

	//return the selected rows in the displayed table
	public int[] getSelectedRows () {
		return resultTable.getSelectedRows();
	}

	//return the site OBJECT at this index in the displayed table
	public FmDBSite getSite (int index) {
		return tableModel.getSite(index);
	}

	//return the site ID at this index in the displayed table
	public long getSiteId (int index) {
		FmDBSite site = tableModel.getSite(index);
		return site.getSiteId ();
	}

	//return the selected rows in the displayed table
	public LinkedHashMap  getCountryList() {
		return countryMap;
	}
	//return the selected rows in the displayed table
	public LinkedHashMap  getMunicipalityList() {
		return municipalityMap;
	}
	 /** return if the site is deleted or not
	  */
	public boolean getDeleted (int index) {
		FmDBSite site =  tableModel.getSite(index);
		return site.isDeleted();

	}
}




