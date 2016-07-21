package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBCommunicator;
import fireparadox.model.database.FmDBCountry;
import fireparadox.model.database.FmDBEvent;
import fireparadox.model.database.FmDBMunicipality;
import fireparadox.model.database.FmDBSite;
import fireparadox.model.database.FmDBUpdator;

/** FiSiteEditor : Creation/Modification of a FiDBSite  in DB4O database
 *
 * @author I. Lecomte - December 2007
 */
public class FmSiteEditor extends AmapDialog implements ActionListener {

	private FmModel model;
	private FmDBCommunicator bdCommunicator;	//to read   database
	private FmDBUpdator bdUpdator;			//to update database
	private int rightLevel;						//to manage user rights
	private boolean deleteAction;				//if true, delete/undelete action is required for this team

	//site object to update
	private FmDBSite site;
	private long siteId;

	//Fields for user interface
	private JTextField siteCodeField;
	private JTextField latitudeField;
	private JTextField longitudeField;
	private JTextField elevationField;
	private JTextField descriptionField;
	private JTextField slopeField;
	private JTextField aspectField;


	//country and municipality selection
	private JComboBox countryComboBox;
	private LinkedHashMap<Long, FmDBCountry> countryMap = new LinkedHashMap <Long, FmDBCountry> ();

	private LinkedHashMap<Long, FmDBMunicipality> municipalityMap  = new LinkedHashMap <Long, FmDBMunicipality>  ();
	private LinkedHashMap<String, Long> municipalityMapName  = new LinkedHashMap <String, Long> ();
	private JComboBox municipalityComboBox;

	private FmDBMunicipality municipality;
	private FmDBCountry country;
	private JLabel countryLabel;
	private JButton municipalityEdit;

	//enums selection
	private Vector topographyList= new Vector ();
	private Vector slopeList= new Vector ();
	private Vector aspectList= new Vector ();
	private JComboBox topographyComboBox;
	private JComboBox slopeComboBox;
	private JComboBox aspectComboBox;

	//species for dominant taxa selection
	private Vector<String> speciesList;
	private JComboBox dominantComboBox1;
	private JComboBox dominantComboBox2;
	private JComboBox dominantComboBox3;

	//event management
	private JButton eventEdit;
	private	LinkedHashMap<Long, FmDBEvent> eventMap;

	//control buttons
	private JButton ok;
	private JButton cancel;
	private JButton help;

	//local data for retrieving user entries
	private String siteCode;
	private String slope, aspect, description, topography;
	private double latitude, longitude, elevation;
	private double slopeValue, aspectValue;

	private Collection dominantTaxa;
	private Collection newDominantTaxa;

	//properties ID
	private long slopeId, aspectId, descriptionId, topographyId, slopeValueId, aspectValueId;
	private long coordinateId, elevationId;
	private long dominantTaxaId;

	/**
	 * Constructor : UPDATE if siteId is not null
	 */
	public FmSiteEditor (FmModel _model,  long _siteId, boolean _deleteAction) {

		super ();
		model = _model;
		siteId = _siteId;
		deleteAction = _deleteAction;

		rightLevel = _model.getRightLevel();

		bdCommunicator =  model.getBDCommunicator ();

		//loading enum and species list
		loadEnum ();
		loadSpecies ();
		loadMunicipalities ();
		setComboBox();


		//loading site fields
		if (siteId > 0) {
			site = loadSite (siteId);
			siteCode = site.getSiteCode();
			municipality = site.getMunicipality();

			if (municipality != null) {
				country = municipality.getCountry();
			}

			description = site.getDescription ();
			topography = site.getTopography ();
			slope = site.getSlope ();
			aspect = site.getAspect ();
			slopeValue = site.getSlopeValue ();
			aspectValue = site.getAspectValue ();

			descriptionId = site.getDescriptionId ();
			topographyId = site.getTopographyId ();
			slopeId = site.getSlopeId ();
			aspectId = site.getAspectId ();
			slopeValueId = site.getSlopeValueId ();
			aspectValueId = site.getAspectValueId ();

			latitude = site.getX();
			longitude = site.getY();
			elevation = site.getZ();
			coordinateId = site.getCoordinatesId();
			elevationId = site.getZId();


			dominantTaxaId = site.getDominantTaxaId();
			dominantTaxa   = site.getDominantTaxa();
		}
		//new site fields are empty
		else {
			siteCode = new String("");
			municipality = null;
			country = null;
			description = new String("");
			topography = new String("");
			slope = new String("");
			aspect = new String("");

			latitude = 0;
			longitude = 0;
			elevation = 0;
			slopeValue = 0;
			aspectValue = 0;
			dominantTaxa = null;

			slopeId = -1;
			aspectId = -1;
			descriptionId = -1;
			topographyId = -1;
			slopeValueId = -1;
			aspectValueId = -1;
			elevationId = -1;
			coordinateId = -1;
			dominantTaxaId = -1;
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
		} else if (evt.getSource ().equals (municipalityEdit)) {
			municipalityAction();
			loadMunicipalities ();
			setComboBox();
			repaint ();
		} else if (evt.getSource ().equals (eventEdit)) {
			eventAction();
		} else if (evt.getSource ().equals (countryComboBox)) {
			setComboBox();
			repaint ();
		} else if (evt.getSource ().equals (cancel)) {
			if (eventMap != null) {
				if(exitQuestion()) setValidDialog (false);
			}
			else setValidDialog (false);

		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	private boolean exitQuestion () {

		boolean answer = false;
		Object[] options = {Translator.swap ("Shared.yes"), Translator.swap ("Shared.no")};
		int n = JOptionPane.showOptionDialog(
				this,
				Translator.swap ("FiSiteEditor.eventHasBeenValidated"),
				"",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,			//don't use a custom Icon
				options,		//the titles of buttons
				null /*options[1]*/);	//default button title
		if (n == JOptionPane.YES_OPTION) {
			answer = true;
		} else if (n == JOptionPane.NO_OPTION) {
			answer = false;
		}
		return answer;

	}
	/**	Municipality editor
	 */
	private void municipalityAction () {
		FmMunicipalityListDialog dial  = new FmMunicipalityListDialog (model);
	}

	/**	Event editor
	 */
	private void eventAction () {
		FmSiteEventListDialog eventDial  = new FmSiteEventListDialog (model, site, eventMap);
		if (eventDial.getIsValidated()) {
			eventMap = eventDial.getEvents();
		}
	}

	/**	Validation = save in the database
	 */
	private void validateAction () {

		if (controlValues ()) {		//if control OK

			if (deleteAction != true) {

				//retrieve site code name
				siteCode = siteCodeField.getText ();
				description = descriptionField.getText();

				//municipality
				municipality = null;
				int n = municipalityComboBox.getSelectedIndex();
				if (n > 0) {
					String name = (String) municipalityComboBox.getSelectedItem ();
					Long municipalityId = (Long) municipalityMapName.get (name);
					municipality = (FmDBMunicipality) municipalityMap.get (municipalityId);
				}

				//dominant taxa
				newDominantTaxa = new ArrayList();
				n = dominantComboBox1.getSelectedIndex();
				if (n > 0) newDominantTaxa.add((String) dominantComboBox1.getItemAt (n));
				n = dominantComboBox2.getSelectedIndex();
				if (n > 0) 	newDominantTaxa.add((String) dominantComboBox2.getItemAt (n));
				n = dominantComboBox3.getSelectedIndex();
				if (n > 0) 	newDominantTaxa.add((String) dominantComboBox3.getItemAt (n));

				//enum properties
				topography = "";
				n = topographyComboBox.getSelectedIndex();
				if (n > 0) topography = (String) topographyComboBox.getItemAt (n);

				slope = "";
				n = slopeComboBox.getSelectedIndex();
				if (n > 0) slope = (String) slopeComboBox.getItemAt (n);

				aspect = "";
				n = aspectComboBox.getSelectedIndex();
				if (n > 0) aspect = (String) aspectComboBox.getItemAt (n);

				//SITE CREATION
				if (site == null) {
					try {
						boolean deleted = false;
						//this new site is temporary created to help the database update
						FmDBSite newSite = new FmDBSite (-1, siteCode,
											municipality,
											-1, latitude, longitude,
											-1, elevation,
											-1, description,
											-1, topography,
											-1, slope,
											-1, slopeValue,
											-1, aspect,
											-1, aspectValue,
											-1, newDominantTaxa,
											deleted);

						bdUpdator = model.getBDUpdator ();
						siteId = bdUpdator.createSite (newSite);		//create in the database
						newSite = null;									//to clear memory

					} catch (Exception e) {
						Log.println (Log.ERROR, "FiSiteEditor.c ()", "error while UPDATING SITE data base", e);
					}
				}
				//SITE UPDATE
				else  {

					try {
						//this new site is temporary created to help the database update
						boolean deleted = false;
						FmDBSite newSite = new FmDBSite (siteId, siteCode,
											municipality,
											coordinateId, latitude, longitude,
											elevationId, elevation,
											descriptionId, description,
											topographyId, topography,
											slopeId, slope,
											slopeValueId, slopeValue,
											aspectId, aspect,
											aspectValueId, aspectValue,
											dominantTaxaId, newDominantTaxa,
											deleted);

						bdUpdator = model.getBDUpdator ();
						bdUpdator.updateSite (site, newSite);		//update in the database
						newSite = null;								//to clear memory

					} catch (Exception e) {
						Log.println (Log.ERROR, "FiSiteEditor.c ()", "error while UPDATING data base", e);
					}
				}

				//UPDATE EVENTS
				if ((siteId > 0) && (eventMap != null)) {
					for (Iterator i = eventMap.keySet().iterator(); i.hasNext ();) {
						Long cle = (Long) i.next();
						FmDBEvent eve = (FmDBEvent) eventMap.get(cle);
						boolean delete = false;
						if ((cle > 0) && (eve.isDeleted())) delete = true;
						try {
							bdUpdator = model.getBDUpdator ();
							bdUpdator.manageSiteEvent (siteId, eve, delete);		//add in the database
						} catch (Exception e) {
							Log.println (Log.ERROR, "FiSiteEditor.c ()", "error while UPDATING data base", e);
						}
					}
				}
			}


			//SITE REACTIVATION/DESACTIVATION
			else if (site != null) {

				try {

					boolean deleted = false;
					if (!site.isDeleted()) deleted = true;

					//this new site is temporary created to help the database update
					FmDBSite newSite = new FmDBSite (siteId, siteCode,
										municipality,
										coordinateId, latitude, longitude,
										elevationId, elevation,
										descriptionId, description,
										topographyId, topography,
										slopeId, slope,
										slopeValueId, slopeValue,
										aspectId, aspect,
										aspectValueId, aspectValue,
										dominantTaxaId, newDominantTaxa,
										deleted);

					bdUpdator = model.getBDUpdator ();
					bdUpdator.updateSite (site, newSite);		//update in the database
					newSite = null;								//to clear memory

				} catch (Exception e) {
					Log.println (Log.ERROR, "FiSiteEditor.c ()", "error while UPDATING data base", e);
				}
			}





			setValidDialog (true);
		}
	}

	/**	Control the value in the ComboBoxes and the TextFields
	 */
	 private boolean controlValues ()   {

		 if (deleteAction) return true;


		//Check the site code
		if (Check.isEmpty (siteCodeField.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEditor.siteCodeIsEmpty"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}

		//Check the  municipality
		int n = municipalityComboBox.getSelectedIndex();
		if (n <= 0) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEditor.municipalityIsEmpty"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}

		//Check the site coordinates
		if (Check.isEmpty (latitudeField.getText ())) latitudeField.setText("0");
		if (!Check.isDouble (latitudeField.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEditor.latitudeIsNotDouble"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}
		latitude = Check.doubleValue (latitudeField.getText ());
		if (latitude > 90) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEditor.latitudeIsNotValid"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}

		if (Check.isEmpty (longitudeField.getText ())) longitudeField.setText("0");
		if (!Check.isDouble (longitudeField.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEditor.longitudeIsNotDouble"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}
		longitude = Check.doubleValue (longitudeField.getText ());
		if (longitude > 360) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEditor.longitudeIsNotValid"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}

		if (Check.isEmpty (elevationField.getText ()) ) elevationField.setText("0");
		if (!Check.isDouble (elevationField.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEditor.elevationIsNotDouble"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}
		elevation = Check.doubleValue (elevationField.getText ());
		if (elevation > 8000) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEditor.elevationIsNotValid"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}

		//Check the site slope and aspect
		if (Check.isEmpty (slopeField.getText ()) ) slopeField.setText("0");
		if (!Check.isDouble (slopeField.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEditor.slopeIsNotDouble"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}
		slopeValue = Check.doubleValue (slopeField.getText ());
		if (slopeValue > 360) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEditor.slopeIsNotValid"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}
		if (Check.isEmpty (aspectField.getText ()) ) aspectField.setText("0");
		if (!Check.isDouble (aspectField.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEditor.aspectIsNotDouble"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}
		aspectValue = Check.doubleValue (aspectField.getText ());
		if (aspectValue > 360) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiSiteEditor.aspectIsNotValid"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}


		return true;
	}


	/**	Initialize the GUI.
	 */
	private void createUI () {

		/*********** SITE panel **************/
		JScrollPane siteScroll = new JScrollPane ();
		JPanel sitePanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
		Box box2 = Box.createVerticalBox ();

		/*********** LOCATION panel **************/
		JPanel locationPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
		Box box21 = Box.createVerticalBox ();
		Border locationEtched = BorderFactory.createEtchedBorder ();
		Border locationBorder = BorderFactory.createTitledBorder (locationEtched,
				Translator.swap ("FiSiteEditor.siteLocation"));
		locationPanel.setBorder (locationBorder);

		JPanel f1 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f2 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f3 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f4 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f5 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f6 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f7 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f8 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f9 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f10 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f11 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f12 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f13 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f14 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f15 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel f16 = new JPanel (new FlowLayout (FlowLayout.LEFT));

		siteCodeField = new JTextField (10);
		siteCodeField.setText (""+siteCode);
		f1.add (new JWidthLabel (Translator.swap ("FiSiteEditor.siteCode")+" :",150));
		f1.add (siteCodeField);

		if (country != null) {
			String name = country.getCountryCode();
			countryComboBox.setSelectedItem(name);

		}
		if (municipality != null) {
			String name = municipality.getMunicipalityName();
			municipalityComboBox.setSelectedItem(name);
		}

		countryComboBox.addActionListener (this);
		municipalityComboBox.addActionListener (this);
		municipalityEdit = new JButton(Translator.swap ("FiSiteEditor.municipalityEditor"));
		municipalityEdit.addActionListener (this);


		f2.add (new JWidthLabel (Translator.swap ("FiSiteEditor.country")+" :",150));
		f2.add (countryComboBox);
		f2.add (new JWidthLabel ("  "+Translator.swap ("FiSiteEditor.municipality")+" :",20));
		f2.add (municipalityComboBox);
		f2.add (municipalityEdit);

		latitudeField = new JTextField (7);
		latitudeField.setText (""+latitude);
		longitudeField = new JTextField (7);
		longitudeField.setText (""+longitude);
		elevationField= new JTextField (7);
		elevationField.setText (""+elevation);

		f3.add (new JWidthLabel (Translator.swap ("FiSiteEditor.latitude")+" :",150));
		f3.add (latitudeField);
		f4.add (new JWidthLabel (Translator.swap ("FiSiteEditor.longitude")+" :", 150));
		f4.add (longitudeField);
		f5.add (new JWidthLabel (Translator.swap ("FiSiteEditor.elevation")+" :", 150));
		f5.add (elevationField);

		box21.add(f1);
		box21.add(f2);
		box21.add(f3);
		box21.add(f4);
		box21.add(f5);

		locationPanel.add (box21);

		/*********** ENVIRONMENT panel **************/
		JPanel environmentPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
		Box box22 = Box.createVerticalBox ();
		Border environmentEtched = BorderFactory.createEtchedBorder ();
		Border environmentBorder = BorderFactory.createTitledBorder (environmentEtched,
				Translator.swap ("FiSiteEditor.envDescription"));
		environmentPanel.setBorder (environmentBorder);

		descriptionField = new JTextField (50);
		descriptionField.setText (""+description);

		topographyList.add (0,"------------");
		topographyComboBox = new JComboBox (topographyList);
		if ((topography != null) && (topography.compareTo("") != 0)) topographyComboBox.setSelectedItem (topography);
		else topographyComboBox.setSelectedIndex(0);

		slopeList.add (0,"------------");
		slopeComboBox = new JComboBox (slopeList);
		if ((slope != null) && (slope.compareTo("") != 0)) slopeComboBox.setSelectedItem (slope);
		else slopeComboBox.setSelectedIndex(0);

		aspectList.add (0,"------------");
		aspectComboBox = new JComboBox (aspectList);
		if ((aspect != null) && (aspect.compareTo("") != 0)) aspectComboBox.setSelectedItem (aspect);
		else aspectComboBox.setSelectedIndex(0);

		slopeField = new JTextField (5);
		slopeField.setText (""+slopeValue);
		aspectField = new JTextField (5);
		aspectField.setText (""+aspectValue);



		//choice of dominantSpecies in species list
		dominantComboBox1 = new JComboBox (speciesList);
		dominantComboBox2 = new JComboBox (speciesList);
		dominantComboBox3 = new JComboBox (speciesList);
		dominantComboBox1.setSelectedIndex(0);
		dominantComboBox2.setSelectedIndex(0);
		dominantComboBox3.setSelectedIndex(0);
		int j = 0;
		if (dominantTaxa != null)  {
			for (Iterator i = dominantTaxa.iterator(); i.hasNext ();) {
				String specieName = (String) i.next();
				if (specieName != null)  {
					int index = speciesList.indexOf (specieName);
					if (index < 0) index = 0; //sinon ne marche pas ????
					if (j==0) dominantComboBox1.setSelectedIndex(index);
					if (j==1) dominantComboBox2.setSelectedIndex(index);
					if (j==2) dominantComboBox3.setSelectedIndex(index);
					j++;
				}
			}
		}

		f9.add (new JWidthLabel (Translator.swap ("FiSiteEditor.description")+" :",150));
		f9.add (descriptionField);
		f10.add (new JWidthLabel (Translator.swap ("FiSiteEditor.topography")+" :",150));
		f10.add (topographyComboBox);
		f11.add (new JWidthLabel (Translator.swap ("FiSiteEditor.slope")+" :",150));
		f11.add (slopeComboBox);
		f11.add (new JWidthLabel (Translator.swap ("FiSiteEditor.slopeValue")+" :",170));
		f11.add (slopeField);
		f12.add (new JWidthLabel (Translator.swap ("FiSiteEditor.aspect")+" :",150));
		f12.add (aspectComboBox);
		f12.add (new JWidthLabel (Translator.swap ("FiSiteEditor.aspectValue")+" :",170));
		f12.add (aspectField);

		f13.add (new JWidthLabel (Translator.swap ("FiSiteEditor.dominantTaxa")+" 1 :",150));
		f13.add (dominantComboBox1);
		f14.add (new JWidthLabel (Translator.swap ("FiSiteEditor.dominantTaxa")+" 2 :",150));
		f14.add (dominantComboBox2);
		f15.add (new JWidthLabel (Translator.swap ("FiSiteEditor.dominantTaxa")+" 3 :",150));
		f15.add (dominantComboBox3);


		eventEdit = new JButton(Translator.swap ("FiSiteEditor.eventEditor"));
		eventEdit.addActionListener (this);
		f16.add (eventEdit);


		box22.add(f9);
		box22.add(f10);
		box22.add(f11);
		box22.add(f12);
		box22.add(f13);
		box22.add(f14);
		box22.add(f15);
		box22.add(f16);

		environmentPanel.add (box22);

		box2.add (locationPanel);
		box2.add (environmentPanel);
		sitePanel.add (box2);
		siteScroll.getViewport().setView(sitePanel);


		if (deleteAction) {
			siteCodeField.setEnabled (false);
			countryComboBox.setEnabled (false);
			municipalityComboBox.setEnabled (false);
			latitudeField.setEnabled (false);
			longitudeField.setEnabled (false);
			elevationField.setEnabled (false);

			descriptionField.setEnabled (false);
			topographyComboBox.setEnabled (false);
			slopeComboBox.setEnabled (false);
			aspectComboBox.setEnabled (false);

			slopeField.setEnabled (false);
			aspectField.setEnabled (false);

			dominantComboBox1.setEnabled (false);
			dominantComboBox2.setEnabled (false);
			dominantComboBox3.setEnabled (false);


		}

		/*********** CONTROL panel **************/
		JPanel controlPanel = new JPanel ();
		controlPanel.setLayout (new FlowLayout (FlowLayout.CENTER));
		if ((site != null) && (deleteAction == true)) {
			if (site.isDeleted())
				ok = new JButton (Translator.swap ("FiSiteEditor.unsupress"));
			else
				ok = new JButton (Translator.swap ("FiSiteEditor.supress"));
		}
		else
			ok = new JButton (Translator.swap ("FiSiteEditor.validate"));

		ok.addActionListener (this);
		controlPanel.add (ok);
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		cancel.addActionListener (this);
		controlPanel.add (cancel);
		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		controlPanel.add (help);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (siteScroll, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);
		setTitle (Translator.swap ("FiSiteEditor.title"));

		setModal (true);
	}

	//Load enum list from database
	private void  loadEnum () {

		countryComboBox = new JComboBox ();
		municipalityComboBox = new JComboBox ();

		try {
			//load enums
			topographyList = bdCommunicator.getTopographies ();
			aspectList = bdCommunicator.getAspects ();
			slopeList = bdCommunicator.getSlopes ();


			//load countries
			countryComboBox.removeAllItems();
			countryComboBox.addItem ("--");
			countryMap = model.getCountryMap ();
			for (Iterator i = countryMap.keySet().iterator(); i.hasNext ();) {
		   		Object cle = i.next();
		   		FmDBCountry t = (FmDBCountry) countryMap.get(cle);
		   		countryComboBox.addItem (t.getCountryCode());
			}

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiSiteEditor.c ()", "error while opening ENUM data base", e);
		}
	}

	//Load species list from database
	private void  loadSpecies() {
		speciesList = new Vector<String> (model.getSpeciesNames ());
		speciesList.add(0, "--------------");

	}

	//load municipalities from data base
	public void loadMunicipalities () {
		try {
			municipalityMap = bdCommunicator.getMunicipalities (countryMap);


		} catch (Exception e) {
			Log.println (Log.ERROR, "FiSiteEditor.c ()", "error while opening ENUM data base", e);
		}
	}

	//load municipalities for the country
	public void setComboBox () {

		//country selection
		String countrySel = new String("");
		if(countryComboBox.getSelectedIndex () > 0)
			countrySel = countryComboBox.getSelectedItem ().toString ();


		municipalityComboBox.removeAllItems();
		municipalityComboBox.addItem ("-------------");

		for (Iterator i = municipalityMap.keySet().iterator(); i.hasNext ();) {
			Object cle = i.next();
			FmDBMunicipality t = (FmDBMunicipality) municipalityMap.get (cle);
			if ((countrySel.equals("")) ||
				(countrySel.equals(t.getCountry().getCountryCode()))) {

				municipalityMapName.put (t.getMunicipalityName(),t.getMunicipalityId ());
				municipalityComboBox.addItem (t.getMunicipalityName());
			}
		}

	}
	//Load site object from database
	private FmDBSite loadSite (long siteId) {
		try {
			FmDBSite ss = bdCommunicator.getSite (municipalityMap, siteId);
			return ss;

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiSiteEditor.loadFuel() ", "error while opening SITE data base", e);
			return null;
		}
	}

}
