package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import capsis.kernel.PathManager;
import fireparadox.model.FmModel;
import fireparadox.model.FmInitialParameters;
import fireparadox.model.database.FmDBCommunicator;
import fireparadox.model.database.FmDBPlant;
import fireparadox.model.database.FmDBSite;
import fireparadox.model.database.FmDBTeam;
import fireparadox.model.database.FmDBUpdator;

/** FiPlantEditor : main screen to create or update a FiDBPlant object
 *
 * @author I. Lecomte - December 2007
 */
public class FmPlantEditor extends AmapDialog implements ActionListener {

	private FmModel model;
	private FmDBCommunicator bdCommunicator;	//to read database
	private FmDBUpdator bdUpdator;				//to update database
	private FmInitialParameters fiInitialParameters; 				//Settings
	private int rightLevel;						//to manage user rights

	//Fuel main informations
	private FmDBPlant plant;					//plant before update or delete
	private FmDBPlant newPlant;					//plant after add or update
	private long plantId;						//id of the plant in the database
	private long plantCopyId;					//id of the plant to copy
	private int fuelType; 						//1=plant 2=layer


	//Team PANEL information
	private FmPlantTeamPanel  teamPanel;
	private FmDBTeam teamLogged;
	private LinkedHashMap<Long, FmDBTeam> teamMap;		//map of id = FiDBTeam

	//Site PANEL information
	private FmPlantSitePanel  sitePanel;


	//Fields for a plant dimansion
	public JTextField heightField;
	public JTextField baseHeightField;
	public JTextField diameterField;
	public JTextField perpendDiameterField;
	public JTextField diameterHeightField;
	public JTextField biomassField;


	//PLANT individual informations
	private JTextField fuelLatitudeField;
	private JTextField fuelLongitudeField;
	private JTextField fuelElevationField;

	private JTextField referenceField;
	private JTextField coverPcField;
	private JComboBox speciesComboBox;
	private JComboBox dominantComboBox1;
	private JComboBox dominantComboBox2;
	private JComboBox openessComboBox;
	private Vector<String> speciesList;
	private Vector<String> openessList;
	private String specieName;
	private String dominantSpecies1,dominantSpecies2;

	private ButtonGroup originGroup;
	private JRadioButton measureRadio;
	private JRadioButton virtualRadio;


	private ButtonGroup statusGroup;
	private JRadioButton isolatedRadio;
	private JRadioButton dominantRadio;
	private JRadioButton subordinateRadio;
	private JRadioButton underTreeRadio;

	//comment
	private JTextArea commentField;

	//buttons
	private JButton ok;
	private JButton cancel;
	private JButton help;

	//properties ids in the database
	private long coordinateId, elevationId,  plantOriginId, plantStatusId, openessId;
	private long referenceId, commentId, samplingDateId, coverPcId, dominantTaxaId;

	//properties values
	private String reference,  plantOrigin, plantStatus, openess, comment;
	private double fuelLatitude, fuelLongitude, fuelElevation, coverPc;
	private Collection dominantTaxa, newDominantTaxa;

	//dimensions
	private long plantHeightId, baseHeightId, diameterId, perpendicularDiameterId, diameterHeightId;
	private int plantHeight, baseHeight, diameter, perpendDiameter, diameterHeight;
	private long biomassId;
	private double biomass;



	/**		Constructor
	 */
	 public FmPlantEditor (FmModel _model,  long _plantId,  long _plantCopyId, int _fuelType) {

		super ();
		model = _model;
		rightLevel = _model.getRightLevel();
		teamLogged = _model.getTeamLogged();

		bdCommunicator = model.getBDCommunicator ();
		fiInitialParameters = model.getSettings();


		plantId = _plantId;
		plantCopyId = _plantCopyId;
		fuelType = _fuelType;


		//create empty combo boxes
		speciesComboBox = new JComboBox ();
		dominantComboBox1 = new JComboBox ();
		dominantComboBox2 = new JComboBox ();

		//loading data from database
		loadDataBase ();


		//LOAD PLANT information for UPDATE ou COPY
		plant = null;
		if (plantId > 0)
			plant = loadPlant (plantId);		//update
		else if (plantCopyId > 0)
			plant = loadPlant (plantCopyId); 	//copy

		if (plant != null) {

			//load fuel data in local fields
			specieName = new String(plant.getSpecie());
			fuelLatitude = plant.getX ();
			fuelLongitude = plant.getY ();
			fuelElevation = plant.getZ ();

			referenceId = plant.getReferenceId ();
			reference = plant.getReference ();

			plantHeightId = plant.getHeightId();
			baseHeightId = plant.getCrownBaseHeightId();
			diameterId = plant.getCrownDiameterId();
			perpendicularDiameterId = plant.getCrownPerpendicularDiameterId();
			diameterHeightId = plant.getMaxDiameterHeightId();

			long val = Math.round(plant.getHeight() * 100);
			plantHeight = (int) (val);
			val = Math.round(plant.getCrownBaseHeight() * 100);
			baseHeight = (int) (val);
			val = Math.round(plant.getCrownDiameter() * 100);
			diameter = (int) (val);

			val = Math.round(plant.getCrownPerpendicularDiameter() * 100);
			perpendDiameter = (int) (val);
			val = Math.round(plant.getMaxDiameterHeight() * 100);
			diameterHeight = (int) (val);

			biomassId = plant.getBiomassId ();
			biomass = plant.getTotalMeasuredBiomass ();


			plantOrigin = plant.getOrigin ();
			commentId = plant.getCommentId();
			comment = plant.getComment ();
			openessId = plant.getOpenessId ();
			openess = plant.getOpeness ();
			dominantTaxaId = plant.getDominantTaxaId();
			dominantTaxa   = plant.getDominantTaxa();
			coverPcId = plant.getCoverPcId ();
			coverPc = plant.getCoverPc ();
			plantStatusId = plant.getPlantStatusId ();
			plantStatus = plant.getPlantStatus ();
			if (plantStatus == null)
				plantStatus = "Isolated"; //default value
			else if (plantStatus.compareTo("") == 0)
				plantStatus = "Isolated"; //default value

			//dominant species names
			int j = 0;
			if (dominantTaxa != null)  {
				for (Iterator i = dominantTaxa.iterator(); i.hasNext ();) {
					String specieName = (String) i.next();
					if (specieName != null)  {
						if (j==0) dominantSpecies1 = specieName;
						if (j==1) dominantSpecies2 = specieName;
						j++;
					}
				}
			}

		}

		//PLANT CREATION
		else {
			specieName = new String("");
			dominantSpecies1 = new String("");
			dominantSpecies2 = new String("");
			openess = new String("");
			dominantTaxa = null;
			coverPc = 0.0;
			comment = new String("");
			reference = new String("");
			plantOrigin = new String("Measured");
			plantStatus = new String("Isolated");
		}

		loadSpecies ();
		createUI ();
		pack ();
		show ();
	}

	/**	Actions on the buttons
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (ok)) {
			validateAction ();

		} else if (evt.getSource ().equals (isolatedRadio)) {
			statusAction ("Isolated");
		} else if (evt.getSource ().equals (dominantRadio)) {
			statusAction ("Dominant");
		} else if (evt.getSource ().equals (subordinateRadio)) {
			statusAction ("Intermediate");
		} else if (evt.getSource ().equals (underTreeRadio)) {
			statusAction ("Overtopped");
		} else if (evt.getSource ().equals (measureRadio)) {
			originAction ("Measured");
		} else if (evt.getSource ().equals (virtualRadio)) {
			originAction ("Virtual");
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**	radio buttons has changed
	 */
	private void statusAction (String value) {plantStatus = value;}
	private void originAction (String value) {
		plantOrigin = value;
		if (plantOrigin.equals("Virtual")) {
			fuelLatitudeField.setEnabled(false);
			fuelLongitudeField.setEnabled(false);
			fuelElevationField.setEnabled(false);
			isolatedRadio.setEnabled(false);
			dominantRadio.setEnabled(false);
			subordinateRadio.setEnabled(false);
			underTreeRadio.setEnabled(false);
			openessComboBox.setEnabled(false);
			dominantComboBox1.setEnabled(false);
			dominantComboBox2.setEnabled(false);
			coverPcField.setEnabled(false);
			biomassField.setEnabled(false);
		}
		else {
			fuelLatitudeField.setEnabled(true);
			fuelLongitudeField.setEnabled(true);
			fuelElevationField.setEnabled(true);
			isolatedRadio.setEnabled(true);
			dominantRadio.setEnabled(true);
			subordinateRadio.setEnabled(true);
			underTreeRadio.setEnabled(true);
			openessComboBox.setEnabled(true);
			dominantComboBox1.setEnabled(true);
			dominantComboBox2.setEnabled(true);
			coverPcField.setEnabled(true);
			biomassField.setEnabled(true);
		}
		repaint();

	}

	/**	Control the value in the ComboBoxes and the TextFields
	 */
	private boolean controlValues ()   {

		boolean teamValid = teamPanel.controlValues ();
		if (!teamValid) return false;

		boolean siteValid = sitePanel.controlValues ();
		if (!siteValid) return false;


		//Check the species info
		int n = speciesComboBox.getSelectedIndex();
		if (n <= 0) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.specieIsEmpty"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}

		//Check the fuel coordinates
		if (Check.isEmpty (fuelLatitudeField.getText ())) fuelLatitudeField.setText("0");
			if (!Check.isDouble (fuelLatitudeField.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.fuelLatitudeMustBeANumberLowerThan90"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
			fuelLatitude = Check.doubleValue (fuelLatitudeField.getText ());
			if (fuelLatitude > 90) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.fuelLatitudeMustBeANumberLowerThan90"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}

			if (Check.isEmpty (fuelLongitudeField.getText ())) fuelLongitudeField.setText("0");
			if (!Check.isDouble (fuelLongitudeField.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.fuelLongitudeMustBeANumberLowerThan360"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
			fuelLongitude = Check.doubleValue (fuelLongitudeField.getText ());
			if (fuelLongitude > 360) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.fuelLongitudeMustBeANumberLowerThan360"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}

			if (Check.isEmpty (fuelElevationField.getText ()) ) fuelElevationField.setText("0");
			if (!Check.isDouble (fuelElevationField.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.fuelElevationMustBeANumberLowerThan4000"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
			fuelElevation = Check.doubleValue (fuelElevationField.getText ());
			if (fuelElevation > 4000) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.fuelElevationMustBeANumberLowerThan4000"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
		}


		//Check the tree cover %
		if (Check.isEmpty (coverPcField.getText ()) ) coverPcField.setText("0");
		if (!Check.isDouble (coverPcField.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.treeCoverMustBeANumberLowerThan100"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}
		coverPc = Check.doubleValue (coverPcField.getText ());
		if (coverPc > 100) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.treeCoverMustBeANumberLowerThan100"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}

		//dimensions
		//Check  height
		if (Check.isEmpty (heightField.getText ()) ) heightField.setText("0");
		if (!Check.isInt (heightField.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.heightIsNotInt"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}
		plantHeight = Integer.parseInt (heightField.getText ());
		if (plantHeight == 0) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.heightIsEmpty"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}

		//Check base height
		if (Check.isEmpty (baseHeightField.getText ()) ) baseHeightField.setText("0");
		if (!Check.isInt (baseHeightField.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.baseHeightIsNotInt"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}
		baseHeight = Integer.parseInt (baseHeightField.getText ());
		if (baseHeight > plantHeight) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.baseHeightIsGreaterThanHeight"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}

		if (Check.isEmpty (diameterField.getText ()) ) diameterField.setText("0");
		if (!Check.isInt (diameterField.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.diameterIsNotInt"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}
		diameter = Integer.parseInt (diameterField.getText ());
		if ((fuelType ==1) && (diameter == 0)) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.diameterIsEmpty"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}
		if (Check.isEmpty (perpendDiameterField.getText ()) ) perpendDiameterField.setText("0");
		if (!Check.isInt (perpendDiameterField.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.perpendDiameterIsNotInt"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}
		perpendDiameter = Integer.parseInt (perpendDiameterField.getText ());

		if (Check.isEmpty (diameterHeightField.getText ()) ) diameterHeightField.setText("0");
		if (!Check.isInt (diameterHeightField.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.diameterHeightIsNotInt"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}
		diameterHeight = Integer.parseInt (diameterHeightField.getText ());


		if (Check.isEmpty (biomassField.getText ()) ) biomassField.setText("0");
		if (!Check.isDouble (biomassField.getText ())) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantEditor.biomassIsNotDouble"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}
		biomass = Check.doubleValue (biomassField.getText ());


		return true;
	}

	/**	Validation = save in the database
	 */
	private void validateAction () {

		bdUpdator = model.getBDUpdator ();

		if (controlValues ()) {

			//team
			long teamId = -1;
			FmDBTeam team = teamPanel.getTeam();
			if (team != null) teamId = team.getTeamId();
			long [] persons = teamPanel.getPersons();
			long samplingDateId = teamPanel.getSamplingDateId ();
			String samplingDate = teamPanel.getSamplingDate ();

			//site
			long siteId = -1;
			FmDBSite site = sitePanel.getSite();
			if (site != null) siteId = site.getSiteId();

			//species name
			int n = speciesComboBox.getSelectedIndex();
			specieName = (String) speciesComboBox.getItemAt (n);

			//dominant taxa
			n = dominantComboBox1.getSelectedIndex();
			newDominantTaxa = new ArrayList();
			if (n > 0) newDominantTaxa.add((String) dominantComboBox1.getItemAt (n));
			n = dominantComboBox2.getSelectedIndex();
			if (n > 0) 	newDominantTaxa.add((String) dominantComboBox2.getItemAt (n));

			//openess
			openess = "";
			n = openessComboBox.getSelectedIndex();
			if (n > 0) openess = (String) openessComboBox.getItemAt (n);

			//other fuel info
			reference = referenceField.getText();
			fuelLatitude = Check.doubleValue (fuelLatitudeField.getText ());
			fuelLongitude= Check.doubleValue (fuelLongitudeField.getText ());
			fuelElevation = Check.doubleValue (fuelElevationField.getText ());
			coverPc = Check.doubleValue (coverPcField.getText ());
			comment = commentField.getText();
			biomass = Check.doubleValue (biomassField.getText ());
			if (plantOrigin.equals("Virtual")) biomass = 0.0;

			//dimensions
			double plHeight = (double) plantHeight / 100;
			double plBaseHeight = (double) baseHeight / 100;
			double plDiameter = (double) diameter / 100;
			double plPerpendDiameter = (double) perpendDiameter / 100;
			double plDiameterHeight = (double) diameterHeight / 100;



			//PLANT creation
			if (plantId < 0) {

				newPlant = new FmDBPlant (-1,  specieName,
								-1, reference,
								plantOrigin,
								teamId, team, persons,
								siteId, site,
								-1, samplingDate,
								-1, fuelLatitude, fuelLongitude,
								-1, fuelElevation,
								-1, comment,
								-1, plHeight,
								-1, plBaseHeight,
								-1, plDiameter,
								-1, plPerpendDiameter,
								-1, plDiameterHeight,
								-1, biomass,
								-1, plantStatus,
								-1, openess,
								-1, coverPc,
								-1, newDominantTaxa,
								false, false);		//deleted and validated are false

				//shape copy
				if (plantCopyId > 0) {
					FmPlantShapeCopyDialog dialog = new FmPlantShapeCopyDialog (model, newPlant, plant);
					if ((dialog.getSampleId() > 0) || (dialog.getSampleEdgeId() > 0) || (dialog.getShapeId() > 0))  {
						setValidDialog (true);
					}
				}
				//shape creation
				else {
					if (fuelType == 1) {
						if (plantOrigin.equals("Measured")) {
							FmShapeMeasuredDialog dialog = new FmShapeMeasuredDialog (model, newPlant, null, null);
							if (dialog.getSampleId() > 0) {
								setValidDialog (true);
							}
						}
						else {
							FmShapeVirtualDialog dialog = new FmShapeVirtualDialog (model, newPlant, null);
							if ((dialog.getSampleId() > 0) || (dialog.getShapeId() > 0))  {
								setValidDialog (true);
							}
						}
					}
					else {
						if (plantOrigin.equals("Measured")) {
							FmLayerMeasuredDialog dialog = new FmLayerMeasuredDialog (model, newPlant, null, null);
							if ((dialog.getSampleId() > 0) || (dialog.getSampleEdgeId() > 0) || (dialog.getShapeId() > 0))  {
								setValidDialog (true);
							}
						}
						else {
							FmLayerVirtualDialog dialog = new FmLayerVirtualDialog (model, newPlant, null, null);
							if ((dialog.getSampleId() > 0) || (dialog.getSampleEdgeId() > 0) || (dialog.getShapeId() > 0))  {
								setValidDialog (true);
							}
						}
					}
				}

			}
			//plant update
			else {

				newPlant = new FmDBPlant (plantId, specieName,
								referenceId, reference,
								plantOrigin,
								teamId, team, persons,
								siteId, site,
								samplingDateId, samplingDate,
								coordinateId, fuelLatitude, fuelLongitude,
								elevationId, fuelElevation,
								commentId, comment,
								plantHeightId, plHeight,
								baseHeightId, plBaseHeight,
								diameterId, plDiameter,
								perpendicularDiameterId, plPerpendDiameter,
								diameterHeightId, plDiameterHeight,
								biomassId, biomass,
								plantStatusId, plantStatus,
								openessId, openess,
								coverPcId, coverPc,
								dominantTaxaId, newDominantTaxa,
								false, false);		//deleted and validated are false

				try {
					bdUpdator.updatePlant (plant, newPlant);		// update in the database

				} catch (Exception e) {
					Log.println (Log.ERROR, "FiPlantEditor.validateAction ()", "error while UPDATING PLANT data base", e);
				}

				setValidDialog (true);

			}

		}
	}


	/**	Initialize the GUI.
	 */
	private void createUI () {

		JTabbedPane tab1 = new JTabbedPane();

		/*********** TEAM panel **************/
		teamPanel = new FmPlantTeamPanel (model, plant,  teamMap);


		/*********** SITE panel **************/
		sitePanel = new FmPlantSitePanel (model, plant);

		/*********** INDIVIDUAL panel **************/

		JScrollPane individualScroll = new JScrollPane ();
		JPanel individualPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
		Box individualBox = Box.createVerticalBox ();

		JPanel plantPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
		Border plantEtched = BorderFactory.createEtchedBorder ();
		Border plantBorder = BorderFactory.createTitledBorder (plantEtched, Translator.swap ("FiPlantEditor.plantData"));
		plantPanel.setBorder (plantBorder);
		Box plantBox = Box.createVerticalBox ();

		JPanel id1 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel id2 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel id3 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel id4 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel id5 = new JPanel (new FlowLayout (FlowLayout.LEFT));

		referenceField = new JTextField (10);
		referenceField.setText (reference);
		fuelLatitudeField = new JTextField (5);
		fuelLatitudeField.setText (""+fuelLatitude);
		fuelLongitudeField = new JTextField (5);
		fuelLongitudeField.setText (""+fuelLongitude);
		fuelElevationField= new JTextField (5);
		fuelElevationField.setText (""+fuelElevation);

		originGroup = new ButtonGroup ();
		measureRadio = new JRadioButton (Translator.swap ("FiPlantEditor.originMeasure"), false);
		measureRadio.addActionListener (this);
		virtualRadio = new JRadioButton (Translator.swap ("FiPlantEditor.originVirtual"), false);
		virtualRadio.addActionListener (this);
		originGroup.add (measureRadio);
		originGroup.add (virtualRadio);

		if (plantOrigin.equals("Virtual")) {
			originGroup.setSelected (virtualRadio.getModel (), true);
			fuelLatitudeField.setEnabled(false);
			fuelLongitudeField.setEnabled(false);
			fuelElevationField.setEnabled(false);
		}
		else {
			originGroup.setSelected (measureRadio.getModel (), true);
			fuelLatitudeField.setEnabled(true);
			fuelLongitudeField.setEnabled(true);
			fuelElevationField.setEnabled(true);
		}

		id1.add (new JWidthLabel (Translator.swap ("FiPlantEditor.species")+" :",190));
		id1.add (speciesComboBox);
		id5.add (new JWidthLabel (Translator.swap ("FiPlantEditor.origin")+" :",190));
		id5.add (measureRadio);
		id5.add (virtualRadio);
		id2.add (new JWidthLabel (Translator.swap ("FiPlantEditor.id")+" :",190));
		id2.add (referenceField);
		id3.add (new JWidthLabel (Translator.swap ("FiPlantEditor.latitude")+" :",190));
		id3.add (fuelLatitudeField);
		id3.add (new JWidthLabel (Translator.swap ("FiPlantEditor.longitude")+" :",15));
		id3.add (fuelLongitudeField);
		id4.add (new JWidthLabel (Translator.swap ("FiPlantEditor.elevation")+" :",190));
		id4.add (fuelElevationField);


		plantBox.add (id1);
		plantBox.add (id5);
		plantBox.add (id2);
		plantBox.add (id3);
		plantBox.add (id4);
		plantPanel.add (plantBox);

		/*********** FUEL DIMENSION panel **************/
		JPanel dimensionPanel = new JPanel ();
		Border dimEtched = BorderFactory.createEtchedBorder ();
		Border dimBorder = BorderFactory.createTitledBorder (dimEtched, Translator.swap ("FiPlantEditor.dimension"));
		dimensionPanel.setBorder (dimBorder);


		Box boxDim = Box.createVerticalBox ();
		JPanel dim1 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel dim2 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel dim3 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel dim4 = new JPanel (new FlowLayout (FlowLayout.LEFT));


		heightField= new JTextField (5);
		heightField.setText (""+plantHeight);
		baseHeightField= new JTextField (5);
		baseHeightField.setText (""+baseHeight);
		diameterField= new JTextField (5);
		diameterField.setText (""+diameter);
		perpendDiameterField= new JTextField (5);
		perpendDiameterField.setText (""+perpendDiameter);
		diameterHeightField= new JTextField (5);
		diameterHeightField.setText (""+diameterHeight);

		biomassField= new JTextField (5);
		biomassField.setText (""+biomass);

		dim1.add (new JWidthLabel (Translator.swap ("FiPlantEditor.plantHeight")+" :", 190));
		dim1.add (heightField);
		dim1.add (new JWidthLabel (Translator.swap ("FiPlantEditor.baseHeight")+" :", 210));
		dim1.add (baseHeightField);
		dim2.add (new JWidthLabel (Translator.swap ("FiPlantEditor.diameter")+" :", 190));
		dim2.add (diameterField);
		dim2.add (new JWidthLabel (Translator.swap ("FiPlantEditor.perpendDiameter")+" :", 210));
		dim2.add (perpendDiameterField);
		dim3.add (new JWidthLabel (Translator.swap ("FiPlantEditor.diameterHeight")+" :", 190));
		dim3.add (diameterHeightField);


		dim4.add (new JWidthLabel (Translator.swap ("FiPlantEditor.biomass")+" :", 190));
		dim4.add (biomassField);

		boxDim.add (dim1);
		boxDim.add (dim2);
		boxDim.add (dim3);
		boxDim.add (dim4);
		dimensionPanel.add (boxDim);


		//plant status
		JPanel statusPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));

		Border statusEtched = BorderFactory.createEtchedBorder ();
		Border statusBorder = BorderFactory.createTitledBorder (statusEtched, Translator.swap ("FiPlantEditor.plantStatus"));
		statusPanel.setBorder (statusBorder);

		//Create icons from images directory
		String repName = PathManager.getInstallDir()
							+File.separator+"bin"
							+File.separator+"fireparadox"
							+File.separator+"gui"
							+File.separator+"images";

		ImageIcon isolated = new ImageIcon (repName+File.separator+"isolated.jpg");
		ImageIcon dominant = new ImageIcon (repName+File.separator+"dominant.jpg");
		ImageIcon subordinate = new ImageIcon (repName+File.separator+"subordinate.jpg");
		ImageIcon underTree = new ImageIcon (repName+File.separator+"underTree.jpg");

		statusGroup = new ButtonGroup ();
		isolatedRadio = new JRadioButton (Translator.swap ("FiPlantEditor.statusIsolated"), false);
		isolatedRadio.addActionListener (this);
		dominantRadio = new JRadioButton (Translator.swap ("FiPlantEditor.statusDominant"), false);
		dominantRadio.addActionListener (this);
		subordinateRadio = new JRadioButton (Translator.swap ("FiPlantEditor.statusSubordinate"), false);
		subordinateRadio.addActionListener (this);
		underTreeRadio = new JRadioButton (Translator.swap ("FiPlantEditor.statusUnderTree"), false);
		underTreeRadio.addActionListener (this);

		statusGroup.add (isolatedRadio);
		statusGroup.add (dominantRadio);
		statusGroup.add (subordinateRadio);
		statusGroup.add (underTreeRadio);

		if (plantStatus.compareTo("Isolated") == 0)
			statusGroup.setSelected (isolatedRadio.getModel (), true);
		if (plantStatus.compareTo("Dominant") == 0)
			statusGroup.setSelected (dominantRadio.getModel (), true);
		if (plantStatus.compareTo("Intermediate") == 0)
			statusGroup.setSelected (subordinateRadio.getModel (), true);
	    if (plantStatus.compareTo("Overtopped") == 0)
	    	statusGroup.setSelected (underTreeRadio.getModel (), true);

		if (plantOrigin.equals("Virtual")) {
			isolatedRadio.setEnabled(false);
			dominantRadio.setEnabled(false);
			subordinateRadio.setEnabled(false);
			underTreeRadio.setEnabled(false);
		}
		else {
			isolatedRadio.setEnabled(true);
			dominantRadio.setEnabled(true);
			subordinateRadio.setEnabled(true);
			underTreeRadio.setEnabled(true);
		}

		Box box1 = Box.createVerticalBox ();
		box1.add (new JLabel(isolated));
		box1.add (isolatedRadio);
		Box box2 = Box.createVerticalBox ();
		box2.add (new JLabel(dominant));
		box2.add (dominantRadio);
		Box box3 = Box.createVerticalBox ();
		box3.add (new JLabel(subordinate));
		box3.add (subordinateRadio);
		Box box4 = Box.createVerticalBox ();
		box4.add (new JLabel(underTree));
		box4.add (underTreeRadio);
		JPanel box5 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		box5.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		box5.setBackground(Color.WHITE) ;
		box5.add (box1);
		box5.add (box2);
		box5.add (box3);
		box5.add (box4);
		statusPanel.add (box5);

		//tree cover %
		JPanel coverPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
		Border coverEtched = BorderFactory.createEtchedBorder ();
		Border coverBorder = BorderFactory.createTitledBorder (coverEtched, Translator.swap ("FiPlantEditor.environ"));
		coverPanel.setBorder (coverBorder);

		Box coverBox = Box.createVerticalBox ();
		JPanel c1 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel c2 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel c3 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel c4 = new JPanel (new FlowLayout (FlowLayout.LEFT));

		openessList.add (0,"------------");
		openessComboBox = new JComboBox (openessList);
		if ((openess != null) && (openess.compareTo("") != 0)) openessComboBox.setSelectedItem (openess);
		else openessComboBox.setSelectedIndex(0);

		coverPcField= new JTextField (5);
		coverPcField.setText (""+coverPc);

		if (plantOrigin.equals("Virtual")) {
			openessComboBox.setEnabled(false);
			dominantComboBox1.setEnabled(false);
			dominantComboBox2.setEnabled(false);
			coverPcField.setEnabled(false);
		}
		else {
			openessComboBox.setEnabled(true);
			dominantComboBox1.setEnabled(true);
			dominantComboBox2.setEnabled(true);
			coverPcField.setEnabled(true);
		}

		c1.add (new JWidthLabel (Translator.swap ("FiPlantEditor.openess")+" :",150));
		c1.add (openessComboBox);
		c2.add (new JWidthLabel (Translator.swap ("FiPlantEditor.dominantSpecies")+" 1 :", 150));
		c2.add (dominantComboBox1);
		c3.add (new JWidthLabel (Translator.swap ("FiPlantEditor.dominantSpecies")+" 2 :", 150));
		c3.add (dominantComboBox2);
		c4.add (new JWidthLabel (Translator.swap ("FiPlantEditor.treeCover")+" :", 150));
		c4.add (coverPcField);
		coverBox.add (c1);
		coverBox.add (c2);
		coverBox.add (c3);
		coverBox.add (c4);
		coverPanel.add (coverBox);

		individualBox.add(plantPanel);
		individualBox.add(dimensionPanel);
		individualBox.add(statusPanel);
		individualBox.add(coverPanel);
		individualPanel.add(individualBox);
		individualScroll.getViewport().setView(individualPanel);

		/*********** COMMENT panel **************/
		JPanel commentPanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
		Box commentBox = Box.createVerticalBox ();
		commentField = new JTextArea(10, 50);
		commentField.setText(comment);
		commentBox.add (new JWidthLabel (Translator.swap ("FiPlantEditor.comment")+" :", 50));
		commentBox.add (commentField);
		commentPanel.add (commentBox);

		/*********** CONTROL panel **************/
		JPanel controlPanel = new JPanel ();
		controlPanel.setLayout (new FlowLayout (FlowLayout.RIGHT));

		ok = new JButton (Translator.swap ("FiPlantEditor.validate"));
		ok.addActionListener (this);
		controlPanel.add (ok);
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		cancel.addActionListener (this);
		controlPanel.add (cancel);
		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		controlPanel.add (help);



		// NO UPDATE if plant is deleted or validated or team logged have no right for this plant
		if (plant != null) {

			if ( (plant.isDeleted())|| (plant.isValidated())
				|| ((rightLevel < 9) && (!teamLogged.getTeamCode().equals (plant.getTeam().getTeamCode()))) ) {

					speciesComboBox.setEnabled(false);
					measureRadio.setEnabled(false);
					virtualRadio.setEnabled(false);
					referenceField.setEnabled(false);
					fuelLatitudeField.setEnabled(false);
					fuelLongitudeField.setEnabled(false);
					fuelElevationField.setEnabled(false);
					isolatedRadio.setEnabled(false);
					dominantRadio.setEnabled(false);
					subordinateRadio.setEnabled(false);
					underTreeRadio.setEnabled(false);
					openessComboBox.setEnabled(false);
					dominantComboBox1.setEnabled(false);
					dominantComboBox2.setEnabled(false);
					coverPcField.setEnabled(false);

					heightField.setEnabled(false);
					baseHeightField.setEnabled(false);
					diameterField.setEnabled(false);
					perpendDiameterField.setEnabled(false);
					diameterHeightField.setEnabled(false);

					biomassField.setEnabled(false);
					commentField.setEnabled(false);
					ok.setEnabled(false);
					ok.removeActionListener (this);

			}
		}


		tab1.add (Translator.swap ("FiPlantEditor.team"), teamPanel);
		tab1.add (Translator.swap ("FiPlantEditor.site"), sitePanel);
		tab1.add (Translator.swap ("FiPlantEditor.individual"), individualScroll);
		tab1.add (Translator.swap ("FiPlantEditor.comment"), commentPanel);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (tab1, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		if (plantId > 0)
			setTitle (Translator.swap ("FiPlantEditor.title")+ "  (id="+plantId+" "+specieName+")");
		else
			setTitle (Translator.swap ("FiPlantEditor.title"));

		setModal (true);
	}

	/*********************************LOADING INFO FROM DATABASE********************************************/
	/**/
	/**	Load enum list from database (done once)
	 */
	private void  loadDataBase () {

		try {

			openessList = bdCommunicator.get0peness ();
			teamMap 	= bdCommunicator.getTeams ();

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPlantEditor.c ()", "error while opening data base", e);
		}
	}
	/**	Load species list from database (done once)
	 */
	private void loadSpecies () {

		speciesList = new Vector<String> (model.getSpeciesNames ());

		//species combo box
		speciesComboBox.removeAllItems();
		speciesComboBox.addItem ("--------");
		dominantComboBox1.removeAllItems();
		dominantComboBox1.addItem ("--------");
		dominantComboBox2.removeAllItems();
		dominantComboBox2.addItem ("--------");

		for (Iterator i = speciesList.iterator(); i.hasNext ();) {
			String name = (String)  i.next();
			speciesComboBox.addItem (name);
			dominantComboBox1.addItem (name);
			dominantComboBox2.addItem (name);
			if (name.equals(specieName))
				speciesComboBox.setSelectedItem(name);
			if (name.equals(dominantSpecies1))
				dominantComboBox1.setSelectedItem(name);
			if (name.equals(dominantSpecies2))
				dominantComboBox2.setSelectedItem(name);
		}

	}

	/**	Load fuel object detail from database (done once)
	 */
	private FmDBPlant loadPlant (long id) {
		try {
			FmDBPlant ff = bdCommunicator.getPlant (model, id);			//fuel info

			return ff;

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPlantEditor.loadPlant() ", "error while opening PLANT data base", e);
			return null;
		}
	}



	/**	To get update from outside
	 */
	public FmDBPlant getNewPlant () {
		return newPlant;
	}
}
