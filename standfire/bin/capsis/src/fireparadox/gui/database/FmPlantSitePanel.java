package fireparadox.gui.database;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;

import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBCommunicator;
import fireparadox.model.database.FmDBCountry;
import fireparadox.model.database.FmDBMunicipality;
import fireparadox.model.database.FmDBPlant;
import fireparadox.model.database.FmDBSite;
import fireparadox.model.database.FmDBTeam;


/** FiPlantSitePanel : panel for choosing a site for a plant or sample
 *
 * @author I. Lecomte - December 2007
 */
public class FmPlantSitePanel extends JPanel implements ActionListener {


	//Entry parameters
	private FmModel model;
	private FmDBCommunicator bdCommunicator;			//to read database
	private FmDBPlant plant;							//plant if update or delete
	private LinkedHashMap<Long, FmDBSite> siteList;		//map of id = FiDBSite
	private LinkedHashMap<Long, FmDBCountry>  countryList;
    private LinkedHashMap<Long, FmDBMunicipality> municipalityList;
    private int rightLevel;
    private FmDBTeam teamLogged;				//team logged

	//Return information on VALID
	private FmDBSite site;
	private long siteId;
	private String siteCode;



	//entry field for UI
	private JButton siteButton;
	private JComboBox siteComboBox;
	private LinkedHashMap<String, Long>   siteMap; 		//map of team code = id



	private JLabel municipality;
	private JLabel country;
	private JLabel topography;
	private JLabel slope;
	private JLabel aspect;
	private JLabel slopeValue;
	private JLabel aspectValue;
	private JLabel latitude;
	private JLabel longitude;
	private JLabel elevation;
	private JLabel description;



	/**		Constructor
	 */
	 public FmPlantSitePanel (FmModel _model, FmDBPlant _plant) {

		super (new FlowLayout (FlowLayout.LEFT));

		model = _model;
		plant = _plant;
		teamLogged = _model.getTeamLogged();
		rightLevel = _model.getRightLevel();
		bdCommunicator = model.getBDCommunicator ();

		//loading data from database
		loadData ();

		//LOAD FUEL information for UPDATE ou COPY
		if (plant != null) {

			//Load site info
			siteCode = new String("");
			country = new JLabel("");
			municipality = new JLabel("");
			description = new JLabel("");
			topography = new JLabel("");
			slope = new JLabel("");
			aspect = new JLabel("");
			slopeValue =  new JLabel("");
			aspectValue =  new JLabel("");
			latitude =  new JLabel("");
			longitude =  new JLabel("");
			elevation =  new JLabel("");


			site = plant.getSite();
			if (site != null) {
				siteId = site.getSiteId();
				site = initSite (siteId);		//read site details in database
			}

		}

		//FUEL CREATION
		else {

			site = null;
			siteCode = new String("");
			country = new JLabel("");
			municipality = new JLabel("");
			description = new JLabel("");
			topography = new JLabel("");
			slope = new JLabel("");
			aspect = new JLabel("");
			slopeValue =  new JLabel("");
			aspectValue =  new JLabel("");
			latitude =  new JLabel("");
			longitude =  new JLabel("");
			elevation =  new JLabel("");

		}

		siteComboBox = new JComboBox ();
		loadSitesCombo ();
		createUI ();

	}

	/**	Actions on the buttons
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (siteComboBox)) {
			selectSite ();
		} else if (evt.getSource ().equals (siteButton)) {
			siteEdition ();
		}

	}

	/**	Site edition dialog
	 */
	private void siteEdition () {

		FmSiteListDialog dial  = new FmSiteListDialog (model);


		loadData ();		//reload sites from database
		loadSitesCombo ();
		repaint();
	}



	/**	site selection has changed
	 */
	 private void selectSite() {
		int n = siteComboBox.getSelectedIndex();
		if (n > 0) {
			String name = (String) siteComboBox.getSelectedItem ();
			siteId = siteMap.get (name);
			site = initSite (siteId);
			this.repaint();
		}
	}


	/**	Control the value in the ComboBoxes and the TextFields
	 */
	public boolean controlValues ()   {

		//Check the site info
		int n = siteComboBox.getSelectedIndex();
		if (n <= 0) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.siteIsEmpty"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}

		return true;
	}



	/**	Initialize the GUI.
	 */
	private void createUI () {


		/*********** SITE panel **************/

		Box box = Box.createVerticalBox ();

		JPanel locationPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
		Box box21 = Box.createVerticalBox ();
		Border locationEtched = BorderFactory.createEtchedBorder ();
		Border locationBorder = BorderFactory.createTitledBorder (locationEtched, Translator.swap ("FiPlantEditor.siteLocation"));
		locationPanel.setBorder (locationBorder);

		JPanel f1 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f3 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f4 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f5 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f6 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f7 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f9 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f10 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f11 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f12 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f13 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f14 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f15 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f16 = new JPanel (new FlowLayout (FlowLayout.LEFT));


		siteComboBox.addActionListener (this);

		siteButton = new JButton (Translator.swap ("FiPlantEditor.siteButton"));
		siteButton.addActionListener (this);
		f1.add (new JWidthLabel (Translator.swap ("FiPlantEditor.siteSelection")+" :",200));
		f1.add (siteComboBox);
		f1.add (siteButton);
		f3.add (new JWidthLabel (Translator.swap ("FiSiteEditor.country")+" :",200));
		f3.add (country);
		f4.add (new JWidthLabel (Translator.swap ("FiSiteEditor.municipality")+" :",200));
		f4.add (municipality);
		f5.add (new JWidthLabel (Translator.swap ("FiSiteEditor.latitude")+" :",200));
		f5.add (latitude);
		f6.add (new JWidthLabel (Translator.swap ("FiSiteEditor.longitude")+" :", 200));
		f6.add (longitude);
		f7.add (new JWidthLabel (Translator.swap ("FiSiteEditor.elevation")+" :", 200));
		f7.add (elevation);


		box21.add(f1);
		box21.add(f3);
		box21.add(f4);
		box21.add(f5);
		box21.add(f6);
		box21.add(f7);

		locationPanel.add (box21);


		JPanel environmentPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
		Box box22 = Box.createVerticalBox ();
		Border environmentEtched = BorderFactory.createEtchedBorder ();
		Border environmentBorder = BorderFactory.createTitledBorder (environmentEtched, Translator.swap ("FiPlantEditor.envDescription"));
		environmentPanel.setBorder (environmentBorder);

		f9.add (new JWidthLabel (Translator.swap ("FiSiteEditor.description")+" :",200));
		f9.add (description);
		f10.add (new JWidthLabel (Translator.swap ("FiSiteEditor.topography")+" :",200));
		f10.add (topography);
		f11.add (new JWidthLabel (Translator.swap ("FiSiteEditor.slope")+" :",200));
		f11.add (slope);
		f12.add (new JWidthLabel (Translator.swap ("FiSiteEditor.slopeValue")+" :",200));
		f12.add (slopeValue);
		f13.add (new JWidthLabel (Translator.swap ("FiSiteEditor.aspect")+" :",200));
		f13.add (aspect);
		f14.add (new JWidthLabel (Translator.swap ("FiSiteEditor.aspectValue")+" :",200));
		f14.add (aspectValue);


		box22.add(f9);
		box22.add(f10);
		box22.add(f11);
		box22.add(f12);
		box22.add(f13);
		box22.add(f14);

		environmentPanel.add (box22);


		// NO UPDATE if plant is deleted or validated or team logged have no right for this plant

		if (plant != null) {
			if ( (plant.isDeleted())|| (plant.isValidated())
				|| ((rightLevel < 9) && (!teamLogged.getTeamCode().equals (plant.getTeam().getTeamCode()))) ) {
					siteComboBox.setEnabled(false);
			}
		}


		box.add (locationPanel);
		box.add (environmentPanel);
		this.add (box);

	}
	/**	Load data list from database (done once and after each site edition)
	 */
	private void loadData () {

		try {

			countryList = model.getCountryMap ();
			municipalityList = bdCommunicator.getMunicipalities (countryList);
			siteList = bdCommunicator.getSites (municipalityList) ;

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPlantEditor.c ()", "error while opening data base", e);
		}
	}

	/**	Load site list in combo box
	 */
	private void loadSitesCombo () {


		//site combo box
		siteComboBox.removeAllItems();
		siteComboBox.addItem ("--------");
		siteMap = new  LinkedHashMap<String, Long> (); 		//map of team code = id


		for (Iterator i = siteList.keySet().iterator(); i.hasNext ();) {
			Object cle = i.next();
			FmDBSite s = siteList.get(cle);

			//to discard deleted sites
			if (!s.isDeleted()) {

				String code = s.getSiteCode();
				long id = s.getSiteId();
				if (code != null) {
					siteMap.put (code, id);
					siteComboBox.addItem (code);
					if (siteCode.equals(code)) siteComboBox.setSelectedItem(code);
				}
			}
		}

	}

	/**	Load site object detail from database (done after site selection in combo box)
	 */
	private FmDBSite initSite (long siteId) {

		try {
			FmDBSite ss = bdCommunicator.getSite (municipalityList, siteId);			//database acces

			//fill UI fields
			siteCode = ss.getSiteCode();
			municipality.setText ("");
			country.setText ("");
			if (ss.getMunicipality() != null) {
				municipality.setText (ss.getMunicipality().getMunicipalityName());
				if (ss.getMunicipality().getCountry() != null) {
					country.setText (ss.getMunicipality().getCountry().getCountryCode());
				}
			}

			description.setText (ss.getDescription());
			topography.setText (ss.getTopography());
			slope.setText (ss.getSlope());
			aspect.setText (ss.getAspect());
			slopeValue.setText ("");
			aspectValue.setText ("");
			if (ss.getSlopeValue () > 0) slopeValue.setText (""+ss.getSlopeValue ());
			if (ss.getAspectValue () > 0) aspectValue.setText (""+ss.getAspectValue ());
			latitude.setText ("");
			longitude.setText ("");
			elevation.setText ("");
			if (ss.getX () > 0) latitude.setText(""+ss.getX ());
			if (ss.getY () > 0) longitude.setText(""+ss.getY ());
			if (ss.getZ () > 0) elevation.setText(""+ss.getZ());


			return ss;

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPlantEditor.loadSite() ", "error while opening SITE data base", e);
			return null;
		}
	}

	public FmDBSite getSite() {
		return site;
	}

}
