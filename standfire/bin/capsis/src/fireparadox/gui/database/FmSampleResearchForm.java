package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

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
import fireparadox.model.database.FmDBPlant;
import fireparadox.model.database.FmDBShape;
import fireparadox.model.database.FmDBSite;
import fireparadox.model.database.FmDBTeam;
import fireparadox.model.database.FmPlantTable;

/**	FiSampleResearchForm : research criterias for samples
*
*	@author I. Lecomte - march 2008
*/
public class FmSampleResearchForm extends JPanel implements ActionListener {

    private FmModel model;
    private FmInitialParameters settings;
    private FmDBCommunicator bdCommunicator;		//to read database
    private int rightLevel;							//to manage user rights
    private FmDBTeam teamLogged; 					//team logged


	//team selection
    private JComboBox teamCombo;
    private LinkedHashMap<Long, FmDBTeam>  teamMap;
	private ArrayList<FmDBTeam>  teamList;
	private String teamSelected = "-------";

    //site selection
    private JComboBox siteCombo;
    private LinkedHashMap<Long, FmDBSite>  siteMap;
	private ArrayList<FmDBSite>   siteList;
	private String siteSelected = "-------";

    //species selection
    private JComboBox speciesCombo;
	private ArrayList<String>  speciesList;
	private String speciesSelected = "-------";

	//size selection
    private JComboBox heightCombo;					//height
    private Vector listH = new Vector();

	//origin selection
    private JComboBox originCombo;
    private Vector listOrigin = new Vector();

	//validated and deleted selection
    private JComboBox validCombo;
    private JComboBox deleteCombo;
    private Vector listValid = new Vector();



	//to store plant list as result table
	private LinkedHashMap<Long, FmDBShape>  listAllShape;
    private JTable resultTable;
    private FmSampleTableModel tableModel;

	/**	Constructor.
	*/
    public FmSampleResearchForm (FmModel _model, FmDBTeam _teamLogged, int _rightLevel) {

		model = _model;
		settings = model.getSettings();
		rightLevel = _rightLevel;
		teamLogged = _teamLogged;



		tableModel = new  FmSampleTableModel (rightLevel);
		tableModel.clear();

		//loading constant data
		loadEnums();

		try {
			bdCommunicator = model.getBDCommunicator ();

			//load team map
			teamMap = new LinkedHashMap<Long, FmDBTeam> ();
			teamMap = bdCommunicator.getTeams ();


			//Load site map
			siteMap = new LinkedHashMap<Long, FmDBSite> ();
			siteMap = bdCommunicator.getSites (null);


		} catch (Exception e) {
			Log.println (Log.ERROR, "FiSampleResearchForm ()", "error while opening data base", e);
		}

		//load all plants
		loadAllData();

		//team selection
		if ((rightLevel < 9) && (teamLogged != null)) {
			teamSelected = teamLogged.getTeamCode();
			teamCombo.setSelectedItem (teamLogged.getTeamCode());
			loadData ("team");
		}

		createUI();
    }
	/**	Changing criterias selection
	 */
    public void actionPerformed(ActionEvent e) {

		if (e.getSource ().equals (speciesCombo)) {

			if(speciesCombo.getSelectedIndex () >= 0) {
				String speciesCode = speciesCombo.getSelectedItem ().toString ();
				if (!speciesCode.equals(speciesSelected)) {
					speciesSelected = speciesCode;
					loadData ("species");
				}
			}

		} else if (e.getSource ().equals (heightCombo)) {
			loadData ("");

		} else if (e.getSource ().equals (siteCombo)) {

			if(siteCombo.getSelectedIndex () >= 0) {
				String siteCode = siteCombo.getSelectedItem ().toString ();
				if (!siteCode.equals(siteSelected)) {
					siteSelected = siteCode;
					loadData ("site");
				}
			}


		} else if (e.getSource ().equals (teamCombo)) {
			if(teamCombo.getSelectedIndex () >= 0) {
				String teamCode = teamCombo.getSelectedItem ().toString ();
				if (!teamCode.equals(teamSelected)) {
					teamSelected = teamCode;
					loadData ("team");
				}
			}

		} else if (e.getSource ().equals (originCombo)) {
			loadData ("");
		} else if (e.getSource ().equals (validCombo)) {
			loadData ("");
		} else if (e.getSource ().equals (deleteCombo)) {
			loadData ("");
		}
	}

	/**	Initialize the GUI.
	 */
    public void createUI() {

		ColumnPanel fuelDBPanel = new ColumnPanel ();

		// 1. CriteriaBorder
		ColumnPanel colCriteria = new ColumnPanel ();
		Border etchedCrit = BorderFactory.createEtchedBorder ();
		Border bCrit = BorderFactory.createTitledBorder (etchedCrit, Translator.swap (
				"FiSampleResearchForm.critereBorder"));
		colCriteria.setBorder (bCrit);

		LinePanel ligCrit1 = new LinePanel ();

		// 1 TEAM
		ColumnPanel col1 = new ColumnPanel ();
		teamCombo.addActionListener(this);
		col1.add (new JLabel(Translator.swap ("FiSampleResearchForm.team")));
		col1.add (teamCombo);
		ligCrit1.add (col1);

		// 2 Site
		ColumnPanel col2 = new ColumnPanel ();
		siteCombo.addActionListener(this);
		col2.add (new JLabel(Translator.swap ("FiSampleResearchForm.site")));
		col2.add (siteCombo);
		ligCrit1.add (col2);

		// 3 Species
    	JLabel lbSpecies = new JLabel(Translator.swap ("FiSampleResearchForm.species"));
		ColumnPanel col3 = new ColumnPanel ();
		speciesCombo.setPreferredSize (new Dimension(100,20));
		speciesCombo.addActionListener (this);
		col3.add (lbSpecies);
		col3.add (speciesCombo);
		ligCrit1.add (col3);

		// 4 Crown Height
    	JLabel lbCrownHeight = new JLabel(Translator.swap ("FiSampleResearchForm.height"));
		ColumnPanel col4 = new ColumnPanel ();
		heightCombo.addActionListener(this);
		col4.add (lbCrownHeight);
		col4.add (heightCombo);
		ligCrit1.add (col4);


		// 6 Origin
		ColumnPanel col6 = new ColumnPanel ();
		originCombo.addActionListener(this);
		col6.add (new JLabel(Translator.swap ("FiSampleResearchForm.origin")));
		col6.add (originCombo);
		ligCrit1.add (col6);



		// 7 Validated
		ColumnPanel col7 = new ColumnPanel ();
		validCombo.addActionListener(this);
		col7.add (new JLabel(Translator.swap ("FiSampleResearchForm.valid")));
		col7.add (validCombo);
		ligCrit1.add (col7);

		// 8 Deleted
		ColumnPanel col8 = new ColumnPanel ();
		deleteCombo.addActionListener(this);
		col8.add (new JLabel(Translator.swap ("FiSampleResearchForm.delete")));
		col8.add (deleteCombo);
		ligCrit1.add (col8);



		colCriteria.add (ligCrit1);

		// 2. ResultBorder
		LinePanel ligResult = new LinePanel ();
		Border etchedRes = BorderFactory.createEtchedBorder ();
		Border bRes = BorderFactory.createTitledBorder (etchedRes, Translator.swap (
				"FiSampleResearchForm.resultBorder"));
		ligResult.setBorder (bRes);

		// 2.1 Result Table
		ColumnPanel colTable = new ColumnPanel ();
		resultTable = new JTable(tableModel);
		JScrollPane listSP = new JScrollPane(resultTable);
		listSP.setPreferredSize (new Dimension (300,240));
		colTable.add (listSP);
		ligResult.add (colTable);

		fuelDBPanel.add (colCriteria);
		fuelDBPanel.add (ligResult);

		setLayout (new BorderLayout ());
		add (fuelDBPanel, BorderLayout.NORTH);

    }

     /** Init criterias
     */
	public void loadEnums () {

		// Combo box implementation: Crown Height and Crown Diameter
		listH.add ("--------");
		listH.add ("0:0.5");
		listH.add ("0.5:1");
		listH.add ("1:2");
		listH.add ("2:5");
		listH.add ("5:10");
		listH.add ("10:50");

		heightCombo = new JComboBox (listH);


		listOrigin.add ("--------");
		listOrigin.add ("Measured");
		listOrigin.add ("Virtual");
		originCombo = new JComboBox(listOrigin);

		listValid.add ("--------");
		listValid.add ("True");
		listValid.add ("False");
		validCombo = new JComboBox(listValid);
		deleteCombo = new JComboBox(listValid);

	}


/** Load database information (all)
     */
	public void loadAllData () {

		try {

			//team combo box
			teamCombo = new JComboBox ();
			teamCombo.addItem ("-------");
			teamList  = new ArrayList<FmDBTeam> ();

			//site combo box
			siteCombo = new JComboBox ();
			siteCombo.addItem ("-------");
			siteList  = new ArrayList<FmDBSite> ();

			//species combo box
			speciesCombo = new JComboBox ();
			speciesCombo.addItem ("-------");
			speciesList  = new ArrayList<String> ();

			//load plant list
			tableModel.clear ();
			listAllShape = new LinkedHashMap<Long, FmDBShape> ();


			//visitors can see only validated and none deleted objects
			int delete = 0;
			int validate = 0;
			if (rightLevel == 1) {
				delete=1;
				validate=2;
			}

			ArrayList<FmPlantTable> sortedPlant = new ArrayList<FmPlantTable>();

			listAllShape = bdCommunicator.getShapes (teamMap, siteMap, delete, validate);
			for (Iterator i = listAllShape.keySet().iterator(); i.hasNext ();) {
				Object cle = i.next();
				FmDBShape sh = listAllShape.get(cle);
				FmDBPlant pl = sh.getPlant();

				String teamName = " ";
				if (pl.getTeam() != null) teamName = pl.getTeam().getTeamCode();

				String siteName = " ";
				if (pl.getSite() != null) siteName = pl.getSite().getSiteCode();

				FmPlantTable pltb = 	new FmPlantTable (sh.getShapeId(),
														teamName,
														siteName,
														pl.getSpeciesName(),
														sh.getZMax(),
														sh.getXMax(),
														sh.getOrigin(),
														sh.isValidated(),
														sh.isDeleted());
				sortedPlant.add(pltb);


				//fill team combo box
				FmDBTeam t = pl.getTeam();
				if (t != null) {
					if (!teamList.contains(t)) {
						teamList.add (t);
					}
				}

				//fill site combo box
				FmDBSite s = pl.getSite();
				if (s != null) {
					if (!siteList.contains (s)) {
						siteList.add (s);
					}
				}

				//fill species combo box
				if (!speciesList.contains (pl.getSpeciesName())) {
					speciesList.add (pl.getSpeciesName());
				}
			}

			//sort the results
			Collections.sort(sortedPlant);
			for (Iterator iter = sortedPlant.iterator(); iter.hasNext ();) {
				FmPlantTable pltb = (FmPlantTable) iter.next();
				Long cle = pltb.getId();
				FmDBShape sh = listAllShape.get(cle);
				tableModel.addShape (sh);
			}

			//sort the team combos
			Collections.sort(teamList);
			for (Iterator iter = teamList.iterator(); iter.hasNext ();) {
				FmDBTeam t = (FmDBTeam) iter.next();
				teamCombo.addItem (t.getTeamCode ());
			}

			//sort the site combos
			Collections.sort(siteList);
			for (Iterator iter = siteList.iterator(); iter.hasNext ();) {
				FmDBSite s = (FmDBSite) iter.next();
				siteCombo.addItem (s.getSiteCode ());
			}

			//sort the species combos
			Collections.sort(speciesList);
			for (Iterator iter = speciesList.iterator(); iter.hasNext ();) {
				String sp = (String) iter.next();
				speciesCombo.addItem (sp);
			}

		} catch (Exception e) {
					Log.println (Log.ERROR, "FiSampleResearchForm ()", "error while opening data base", e);
		}
	}

    /** Load database information with research criterias
     */
	public void loadData (String newCriteria) {

		tableModel.clear ();

		boolean checkTeam = false;
		boolean checkSite = false;
		boolean checkSpecie = false;
		boolean checkHeight = false;
		boolean checkOrigin = false;
		boolean checkValid = false;
		boolean checkDelete = false;

		//site and species combo will be refilled if team selection have changed
		if ((newCriteria == "team") || (newCriteria == "site")) {
			if (newCriteria == "team") {
				siteCombo.removeActionListener(this);
				siteCombo.removeAllItems ();
				siteCombo.addItem ("--------");
				siteList  = new ArrayList<FmDBSite> ();
			}

			speciesCombo.removeActionListener(this);
			speciesCombo.removeAllItems ();
			speciesCombo.addItem ("--------");
			speciesList  = new ArrayList<String> ();
		}


		//team code selection
		String teamCode = "";
		if(teamCombo.getSelectedIndex () > 0)  {
			teamCode = teamCombo.getSelectedItem ().toString ();
		    checkTeam = true;
		}

		//site name selection
		String siteCode = "";
		if(siteCombo.getSelectedIndex () > 0) {
			siteCode = siteCombo.getSelectedItem ().toString ();
		    checkSite = true;
		}

		//species selection
		String speciesName = "";
		if(speciesCombo.getSelectedIndex () > 0) {
			speciesName = speciesCombo.getSelectedItem ().toString ();
			checkSpecie = true;
		}

		//origin selection
		String origin = "";
		if(originCombo.getSelectedIndex () > 0) {
			origin = originCombo.getSelectedItem ().toString ();
			checkOrigin = true;
		}


		//validated selection
		boolean validated = false;
		if(validCombo.getSelectedIndex () > 0) {
			int valid = validCombo.getSelectedIndex ();
			if (valid ==1) validated = true;
			checkValid = true;
		}


		//deleted selection
		boolean deleted = false;
		if(deleteCombo.getSelectedIndex () > 0) {
			int del = deleteCombo.getSelectedIndex ();
			if (del ==1) deleted = true;
			checkDelete = true;
		}



		// height selection
		String height = "";
		if(heightCombo.getSelectedIndex () > 0) {
			height = heightCombo.getSelectedItem ().toString ();
		    checkHeight = true;
		}


		//read the fuel lis and check if data is matching criterias
		ArrayList<FmPlantTable> sortedPlant = new ArrayList<FmPlantTable>();
		for (Iterator i = listAllShape.keySet().iterator(); i.hasNext ();) {
			Object cle = i.next();
			FmDBShape sh = listAllShape.get(cle);
			FmDBPlant pl = sh.getPlant();

			boolean check1 = true;
			boolean check2 = true;
			boolean check3 = true;
			boolean check4 = true;
			boolean check6 = true;
			boolean check7 = true;
			boolean check8 = true;

			if (checkTeam) 	 check1 = pl.checkTeamCode (teamCode);
			if (checkSite) 	 check2 = pl.checkSiteCode (siteCode);
			if (checkSpecie) check3 = pl.checkSpeciesName (speciesName);
			if (checkHeight) check4 = sh.checkHeight (height);
			if (checkOrigin) check6 = sh.checkOrigin (origin);
			if (checkValid)  check7 = sh.getPlant().checkValidated (validated);
			if (checkDelete)  check8 = sh.checkDeleted (deleted);

			if (check1 && check2 && check3 && check4 &&  check6 && check7 && check8) {

				String teamName = " ";
				if (pl.getTeam() != null) teamName = pl.getTeam().getTeamCode();

				String siteName = " ";
				if (pl.getSite() != null) siteName = pl.getSite().getSiteCode();

				FmPlantTable pltb = 	new FmPlantTable (sh.getShapeId(),
														teamName,
														siteName,
														pl.getSpeciesName(),
														sh.getZMax(),
														sh.getXMax(),
														sh.getOrigin(),
														sh.getPlant().isValidated(),
														sh.isDeleted());
				sortedPlant.add(pltb);

				//refill site and species combo if team selection have changed
				if ((newCriteria == "team") || (newCriteria == "site")) {

					if (newCriteria == "team") {
						FmDBSite s = pl.getSite();
						if (s != null) {
							if (!siteList.contains (s)) {
								siteList.add (s);
							}
						}
					}

					if (!speciesList.contains (pl.getSpeciesName())) {
						speciesList.add (pl.getSpeciesName());
					}
				}

			}
		}

		//sort the results
		Collections.sort(sortedPlant);
		for (Iterator iter = sortedPlant.iterator(); iter.hasNext ();) {
			FmPlantTable pltb = (FmPlantTable) iter.next();
			Long cle = pltb.getId();
			FmDBShape sh = listAllShape.get(cle);
			tableModel.addShape (sh);
		}

		//sort the site combos
		if ((newCriteria == "team") || (newCriteria == "site")) {
			if (newCriteria == "team") {
				Collections.sort(siteList);
				for (Iterator iter = siteList.iterator(); iter.hasNext ();) {
					FmDBSite s = (FmDBSite) iter.next();
					siteCombo.addItem (s.getSiteCode ());
				}
				siteCombo.addActionListener(this);
			}

			//sort the species combos
			Collections.sort(speciesList);
			for (Iterator iter = speciesList.iterator(); iter.hasNext ();) {
				String sp = (String) iter.next();
				speciesCombo.addItem (sp);
			}
			speciesCombo.addActionListener(this);
		}

	}


    /** return the selected rows in the displayed table
     */
	public int[] getSelectedRows () {
		return resultTable.getSelectedRows();
	}
    /** return the plant store at this index in the displayed table
     */
	public FmDBShape getShape (int index) {
		return tableModel.getShape(index);
	}

    /** return the plant store at this index in the displayed table
     */
	public long getShapeId (int index) {
		FmDBShape shape =  tableModel.getShape(index);
		return shape.getShapeId ();
	}

    /** return if the plant is deleted or not
     */
	public boolean getDeleted (int index) {
		FmDBShape shape =  tableModel.getShape(index);
		return shape.isDeleted();

	}

    /** return the team store at this index in the displayed table
     */
	public FmDBTeam getTeam (int index) {
		FmDBShape shape =  tableModel.getShape(index);
		FmDBPlant plant = shape.getPlant();
		return plant.getTeam();

	}


}




