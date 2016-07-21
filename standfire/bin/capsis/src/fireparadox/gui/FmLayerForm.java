package fireparadox.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import jeeb.lib.sketch.scene.item.Polygon;
import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Listener;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import capsis.lib.fire.fuelitem.FiParticle;
import capsis.lib.fire.fuelitem.FiSpecies;
import fireparadox.model.FmModel;
import fireparadox.model.FmPlot;
import fireparadox.model.layerSet.FmLayer;
import fireparadox.model.layerSet.FmLayerSet;
import fireparadox.model.layerSet.FmLocalLayerModels;
import fireparadox.model.layerSet.FmLocalLayerSetModels;

/**
 * A form to create local layer sets, i.e. NOT from the database. - New form
 * made in Avignon -
 * 
 * @author F. Pimont, F. de Coligny - october 2009
 */
public class FmLayerForm extends JPanel implements ActionListener, 
TableModelListener, ListenedTo {

	public static final String SHOW_EDITOR = Translator.swap ("FiLocalLayerForm.showEditor");	//T
	public static final String HIDE_EDITOR = Translator.swap ("FiLocalLayerForm.hideEditor");	//T

	private FmModel model;
	private FiSpecies defaultSpecies;
	private Collection<Listener> listeners;
	private Collection<String> speciesNames;

	
	
	private JComboBox layerSetCombo; // added by fp
	private JRadioButton defaultMode;
	private JRadioButton specifyFieldProperties;
	private ButtonGroup rdGroup;
	
	
	private JButton addLayerSet;

	
	private JComboBox speciesCombo;
	private JTextField lowerHeightBoundary;// boundaries of local layer models
	private JTextField upperHeightBoundary;
	private JTextField layerHeight;

	private JButton add;
	private JButton remove;
	private JButton clear;

	private boolean mute;

	private JTable table;
	private FmLayerFormTableModel tableModel;

	private JTextField layerSetLoad; //kg/m2
	private JTextField layerSetLAI; // m2/m2
	private JTextField layerSetInternalCover; //%

	private boolean creationMode;			// creation / edition
	private boolean withShowHideEditor;		// if edition, with / without button below
	private JButton showHideEditor;
	private LinePanel showHideEditorLine;

	private JPanel part0; // layerset
	private JPanel part1;
	private ColumnPanel part2;
	private JSplitPane split;

	private FmLocalLayerModels propertyMap;
	private Vector<String> layerSetNames;
	private FmLocalLayerSetModels propertyLayerSetMap;
	private JTextField age;
	private JTextField fertility;


	/**	Constructor 1 - creation mode: a FiLocalLayerForm for creating a new
	 *	FmLayerSet interactively.
	 */
	public FmLayerForm (FmModel model) {
		this.model = model;
		this.defaultSpecies = model.getSpeciesSpecimen ().getDefaultSpecies ();
		this.propertyMap = model.localLayerModels;
		this.propertyLayerSetMap = model.localLayerSetModels;
		this.creationMode = true;
		this.withShowHideEditor = false;

		init ();

		tableModel = new FmLayerFormTableModel ();
		tableModel.addTableModelListener (this);

		prepareUI ();
		refreshUI ();
	}

	/**	Constructor 2 - edition: a FiLocalLayerForm for editing an existing FmLayerSet.
	 *	We are editing: creationMode is false. If withShowHideEditor is true, show the
	 *	showHideEditor button.
	 */
	public FmLayerForm (FmModel model, FmLayerSet layerSet, boolean withShowHideEditor) {

		this.model = model;
		//~ settings = model.getSettings();
		// fc - 27.10.2009 - added these lines, seemed to be missing
			this.propertyMap = model.localLayerModels;
			this.propertyLayerSetMap = model.localLayerSetModels;
		// fc - 27.10.2009 - added these lines, seemed to be missing

		this.creationMode = false;
		this.withShowHideEditor = withShowHideEditor;

		init ();

		tableModel = new FmLayerFormTableModel ();
		tableModel.addTableModelListener (this);

		prepareUI ();
		refreshUI ();

		setLayerSet (layerSet);

	}

	private void init () {
		speciesNames = new Vector<String> ();
		speciesNames.addAll(propertyMap.getSpeciesSet());
		layerSetNames = new Vector<String> ();
		layerSetNames.addAll(propertyLayerSetMap.getNames());

	}

	/**	Set the given layerSet in the form.
	 */
	public void setLayerSet (FmLayerSet layerSet) {
		// Copy the layers of the layerSet to be edited into the table2Model
		tableModel.setLayers (layerSet.getLayers ());
		layerSet.setAge(getAge());
		layerSet.setFertility(getFertility());
		//System.out.println("fertility="+getFertility());
	}

	/**	Called when table2 is edited.
	 */
	public void tableChanged (TableModelEvent e) {
		if (e.getSource ().equals (tableModel)) {
			try {
				updateInternalCover ();
			} catch (Exception e1) {
				// TODO FP Auto-generated catch block
				e1.printStackTrace();
			}

			if (mute) {return;}
			tellSomethingHappened (null);
		}
	}

	/**
	 * Add button for individual layers.
	 * 
	 * @throws Exception
	 */
	private void addAction () throws Exception {

		// Test the layer properties are correct
		if (!checkBeforeAdd ()) {return;}

		// Add a layer in the table
		String speciesName = getSpeciesName ();
		double height = getLayerHeight ();

		// Local layers additional properties
		double aliveBulkDensity = propertyMap.computeBulkDensity(speciesName, height, true); 
		double deadBulkDensity = propertyMap.computeBulkDensity(speciesName, height, false);
		double svr = propertyMap.getSVR(speciesName);
		double mvr = propertyMap.getMVR(speciesName);

		addLayer (speciesName, height, 0d, 0d, 0d, 0, -1, -1, aliveBulkDensity, deadBulkDensity, svr, mvr, defaultSpecies);
	}

	/**
	 * Add button for default layerSets.
	 * 
	 * @throws Exception
	 */
	private void addActionLayerSet () throws Exception {

		String layerSetName = getDefaultLayerSetName ();
		int n=propertyLayerSetMap.getLayerNumber(layerSetName);
		if (specifyFieldProperties.isSelected()) {
			//TODO : get poly, get tree collection, plot...
			
			Polygon poly = null;
			Collection trees = null; 
			double heightThreshold = 0d;
			FmPlot plot = null;
			Collection layerSets = null;
			//double [] multicov = FiDendromStandProperties.calcMultiCov(trees, layerSets, plot, heightThreshold, poly);
			double treeCover = 0d; // multicov[2];
			
			
			//FmLayerSet temp = FmLayerSet (0, layers);
			
			//height = FiLocalLayerEvolution.height(age, fertility, lastClearingType, treatmentEffect, treeCover);
			
			double liveBulkDensity = -3;
			 double deadBulkDensity = -3;
		}

		for (int i=0; i<n; i++) {
			
			String speciesName = propertyLayerSetMap.getLayerName(layerSetName, i);
			int spatialGroup = propertyLayerSetMap.getSpatialGroup(layerSetName, i);
			double height = propertyLayerSetMap.getLayerHeight(layerSetName, i);
			double bottomHeight = 0d;
			double percentage = propertyLayerSetMap.getCoverFraction(layerSetName, i);
			double characteristicSize = propertyLayerSetMap.getCharacteristicSize(layerSetName, i);
			double aliveMoisture = propertyLayerSetMap.getAliveMoisture(layerSetName, i);
			double deadMoisture = propertyLayerSetMap.getDeadMoisture(layerSetName, i);
			
			// Local layers additional properties should be modified when dynamic...
			double liveBulkDensity = propertyLayerSetMap.getLiveBulkDensity(layerSetName, i); 
			double deadBulkDensity = propertyLayerSetMap.getDeadBulkDensity(layerSetName, i);
			double svr = propertyLayerSetMap.getSvr(layerSetName, i);
			double mvr = propertyLayerSetMap.getMvr(layerSetName, i);
						
			addLayer (speciesName, height, bottomHeight, percentage, characteristicSize, spatialGroup, aliveMoisture, deadMoisture, liveBulkDensity, deadBulkDensity, svr, mvr, defaultSpecies);	
		}
	}

	/**	updateBoundaries when a species is selected
	 */
	private void updateBoundaries () {
		// Update the boundaries for the current species
		//~ System.out.println ("FiLocalLayerForm updateBoundaries () for species: "+getSpeciesName ());
		lowerHeightBoundary.setText (""+getLowerBoundary ());
		upperHeightBoundary.setText (""+getUpperBoundary ());
		
		// what should be done??
		tellSomethingHappened (null);
	}

	private void addLayer(String speciesName, double height,
			double bottomHeight, double percentage, double characteristicSize,
			int spatialGroup, double aliveMoisture, double deadMoisture,
			double liveBulkDensity, double deadBulkDensity, double svr,
			double mvr, FiSpecies defaultSpecies)
			throws Exception {
		
		double[] liveBulkDensityArray = new double[1];
		double[] deadBulkDensityArray = new double[1];
		double[] svrArray = new double[1];
		double[] mvrArray = new double[1];
		liveBulkDensityArray[0] = liveBulkDensity;
		deadBulkDensityArray[0] = deadBulkDensity;
		mvrArray[0] = mvr;
		svrArray[0] = svr;
		// Create the layer
		FmLayer localLayer = new FmLayer(speciesName, height,
				bottomHeight,
				percentage, 
				characteristicSize,
				spatialGroup, 
				aliveMoisture, 
				deadMoisture,aliveMoisture, //aliveMoisture is used for twigs too
				liveBulkDensityArray, deadBulkDensityArray, mvrArray,
				svrArray, defaultSpecies);
		// Add it in the table
		tableModel.addLayer (localLayer);
		tellSomethingHappened (null);

	}

	/**	Remove button.
	 */
	private void removeAction () {
		int[] selectedRows = table.getSelectedRows ();
		if (selectedRows.length == 0) {return;}
		int sel = 0;
		for (int cpt = 0; cpt < selectedRows.length; cpt++) {
			// consider sorting
			selectedRows[cpt] = table.convertRowIndexToModel (selectedRows[cpt]);
			// selection is now in terms of the underlying TableModel
			sel = selectedRows[cpt];
			tableModel.removeLayer (tableModel.getLayer (sel));
		}
		// reselect some layer
		try {
			if (sel > tableModel.getRowCount () - 1) {sel = tableModel.getRowCount () - 1;}
			table.getSelectionModel ().setSelectionInterval (sel, sel);
		} catch (Exception e) {
			table.getSelectionModel ().setSelectionInterval (0, 0);
		}
		tellSomethingHappened (null);
	}

	/**	Clear button.
	 */
	private void clearAction () {
		tableModel.clear ();
		tellSomethingHappened (null);
	}

	/**	Show / Hide editor button.
	 */
	private void showHideEditorAction () {
		if (showHideEditor.getText ().equals (SHOW_EDITOR)) {
			showHideEditor.setText (HIDE_EDITOR);
			//~ if (table1Model.getRowCount () == 0) {
			//~ updateTable1 ();	// first load : no specific criteria
			//~ }

		} else {
			showHideEditor.setText (SHOW_EDITOR);
		}
		refreshUI ();
	}

	/**	If buttons are hit / textfields are validated
	 */
	public void actionPerformed (ActionEvent e) {
		// Tell listeners
		tellSomethingHappened (null);

		if (e.getSource ().equals (speciesCombo)) {
			updateBoundaries ();
		} else if (e.getSource ().equals (this.specifyFieldProperties)) {
			age.setEnabled(true);
			fertility.setEnabled(true);
		} else if (e.getSource ().equals (addLayerSet)) {
			try {
				addActionLayerSet ();
			} catch (Exception e1) {
				// TODO FP Auto-generated catch block
				e1.printStackTrace ();
			}
		} else if (e.getSource ().equals (add)) {
			try {
				addAction ();
			} catch (Exception e1) {
				// TODO FP Auto-generated catch block
				e1.printStackTrace ();
			}
		} else if (e.getSource ().equals (remove)) {
			removeAction ();
		} else if (e.getSource ().equals (clear)) {
			clearAction ();
		} else if (e.getSource ().equals (showHideEditor)) {
			showHideEditorAction ();
		}
	}

	/**	Check on 'Add' that the layer properties are correct
	 *	before making the layer and put it in the table.
	 */
	public boolean checkBeforeAdd () {
		// layerHeight
		if (!Check.isDouble (layerHeight.getText ().trim ())) {
			MessageDialog.print (this, 
					Translator.swap ("FiLocalLayerForm.layerHeightShouldBeADoubleGreaterThanZero"));
			return false;
		}
		double lh = Check.doubleValue (layerHeight.getText ().trim ());
		if (lh < 0) {
			MessageDialog.print (this, 
					Translator.swap ("FiLocalLayerForm.layerHeightShouldBeADoubleGreaterThanZero"));
			return false;
		}
		Settings.setProperty ("fi.local.tree.form.last.layerHeight", "" + lh);	
		return true;
	}

	/**
	 * Tests correctness of the form.
	 * 
	 * @throws Exception
	 */
	public boolean isCorrect () throws Exception {
		// Check all user entries, in case of trouble, tell him and return false

		// Check layers in table
		Collection<FmLayer> layers = getLayers();

		// No layers added
		if (layers == null || layers.isEmpty ()) {
			MessageDialog.print (this,
					Translator.swap ("FiLocalLayerForm.FiLocalLayerForm-pleaseAddAtLeastOneLayerInTheResultTable"));
			return false;
		}

		// If table is under edition, finish edition -> TROUBLE
		//~ if (table2.isEditing ()) {
		//~ MessageDialog.print (this,
		//~ Translator.swap ("FiLocalLayerForm.pleaseFinishTableEdition"));
		//~ return false;
		//~ }
		// Check name != null
		for (FmLayer layer : layers) {
			if (layer.getLayerType() == null) {
				MessageDialog
				.print(
						this,
						Translator
						.swap("FiLocalLayerForm.FiLocalLayerForm-allLayerShouldHaveAName"));
				return false;
			}
		
		// Check minHeight >= 0
		
			if (layer.getBaseHeight() < 0) {
				MessageDialog
				.print(
						this,
						Translator
						.swap("FiLocalLayerForm.FiLocalLayerForm-allBottomHeightMustBeGreaterOrEqualToZero"));
				return false;
			}
		
		// Check maxHeight >= minHeight
		
			if (layer.getHeight() <= layer.getBaseHeight()) {
				MessageDialog
				.print(
						this,
						Translator
						.swap("FiLocalLayerForm.FiLocalLayerForm-allHeightMustBeGreaterToBottomHeight"));
				return false;
			}
	
		// Check characteristic size >= 0
	
			if (layer.getCharacteristicSize () < 0) {
				MessageDialog.print (this,
						Translator.swap ("FiLocalLayerForm.FiLocalLayerForm-allCharacteristicSizesMustBeGreaterOrEqualToZero"));
				return false;
			}
	
		// Check biomass >= 0
	
			if (layer.getBulkDensity (0, FiParticle.LIVE) < 0 || layer.getBulkDensity (0, FiParticle.DEAD) < 0) {
				MessageDialog
				.print(
						this,
						Translator
						.swap("FiLocalLayerForm.FiLocalLayerForm-allBiomassMustBeGreaterOrEqualToZero"));
				return false;
			}
	
		// Check moisture >= 0
	
			if (layer.getMoisture (0, FiParticle.LIVE) < 0 || layer.getMoisture (0, FiParticle.DEAD) < 0) {
				MessageDialog
				.print(
						this,
						Translator
						.swap("FiLocalLayerForm.FiLocalLayerForm-allMoistureMustBeGreaterOrEqualToZero"));
				return false;
			}
			
			if (layer.getSVR (0, FiParticle.LIVE) < 0) {
				MessageDialog
				.print(
						this,
						Translator
						.swap("FiLocalLayerForm.FiLocalLayerForm-allSVRMustBeGreaterOrEqualToZero"));
				return false;
			}
			if (layer.getMVR (0, FiParticle.LIVE) < 0) {
				MessageDialog
						.print(
								this,
								Translator
										.swap("FiLocalLayerForm.FiLocalLayerForm-allMVRMustBeGreaterOrEqualToZero"));
				return false;
			}
		}
		// Check cover fraction > 0
		double sum = 0;
		for (FmLayer layer : layers) {
			if (layer.getCoverFraction() <= 0) {
				MessageDialog
						.print(
								this,
								Translator
										.swap ("FiLocalLayerForm.FiLocalLayerForm-allCoversMustBeGreaterThanZero"));
				return false;
			}
			sum += layer.getCoverFraction();
		}

		// Check coverfraction sum <= 1
		if (sum > 1d) {
			MessageDialog
					.print(
							this,
							Translator
									.swap ("FiLocalLayerForm.FiLocalLayerForm-sumOfTheCoversMustBeLowerOrEqualTo1"));
			return false;
		}
		
		// Everything is ok, return true
		return true;
	}

	/**
	 * Count the cover column and cumulate
	 */
	private void updateInternalCover () throws Exception {
		if (layerSetInternalCover == null) {return;}	// too early
		Map <Integer, Double> intCovPerSpatialGroup = new HashMap <Integer, Double> ();
		double intCov = 0;
		double intLoad = 0;
		double intLAI = 0;

		for (FmLayer layer : tableModel.getLayers()) {
			if (intCovPerSpatialGroup.containsKey(layer.getSpatialGroup())) {
				intCovPerSpatialGroup.put(layer.getSpatialGroup(), 
							intCovPerSpatialGroup.get(layer.getSpatialGroup())+layer.getCoverFraction ());
			} else {
				intCovPerSpatialGroup.put(layer.getSpatialGroup(), layer.getCoverFraction ());
			}
			intLoad += layer.getLoad (FiParticle.ALL);
			intLAI += layer.getLai ();
		}
		double emptiness = 1d;
		for (int i : intCovPerSpatialGroup.keySet()) {
			emptiness *= 1d - 0.01 * intCovPerSpatialGroup.get(i);
		}
		intCov = 100d *(1d - emptiness);
		layerSetInternalCover.setText (""+intCov);
		layerSetLoad.setText (""+intLoad);
		layerSetLAI.setText (""+intLAI);
	}

	/**	Accessor for the layers in table: the current 'layerSet'.
	 */
	public Collection<FmLayer> getLayers() {
		//~ // Stop current edition if needed
		if (table.isEditing ()) {
			mute = true;
			table.getCellEditor ().stopCellEditing ();
			mute = false;
		}

		List<FmLayer> resultLayers = new ArrayList<FmLayer>();
		for (FmLayer l : tableModel.getLayers()) {
			resultLayers.add (l);
		}
		return resultLayers;
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
	public void prepareUI () {

		// Show/hide editor button layer
		showHideEditorLine = new LinePanel ();
		showHideEditorLine.addGlue ();
		showHideEditorLine.add (new JLabel (Translator.swap ("FiLocalLayerForm.toEditTheLayerSet")+" : "));	//T
		showHideEditor = new JButton (SHOW_EDITOR);
		showHideEditor.addActionListener (this);
		showHideEditorLine.add (showHideEditor);
		showHideEditorLine.addStrut0 ();

		
		part1 = new JPanel (new BorderLayout ());

		ColumnPanel layersAdder = new ColumnPanel(); 
		// Default layersets
		ColumnPanel layerSetProps = new ColumnPanel (Translator.swap ("FiLocalLayerForm.chooseAPredefinedLayerSet") );
		LinePanel l0 = new LinePanel ();
		l0.add (new JWidthLabel (Translator.swap ("FiLocalLayerForm.defaultLayerSets") + " : ", 100));
		layerSetCombo = new JComboBox (new Vector (layerSetNames));
		layerSetCombo.addActionListener (this);
		l0.addGlue();
		//l0.addStrut0 ();
		l0.add (layerSetCombo);
		layerSetProps.add(l0);
		
		LinePanel l0a = new LinePanel ();
		defaultMode = new JRadioButton (Translator.swap ("FiLocalLayerForm.default"));
		l0a.add(defaultMode);
		l0a.addGlue();
		layerSetProps.add(l0a);
		
		LinePanel l0b = new LinePanel ();
		specifyFieldProperties = new JRadioButton (Translator.swap ("FiLocalLayerForm.specifyFieldProperties"));
		specifyFieldProperties.addActionListener(this);
		l0b.add(specifyFieldProperties);
		l0b.addGlue();
		layerSetProps.add(l0b);
		
		rdGroup = new ButtonGroup ();
		rdGroup.add (defaultMode);
		rdGroup.add (specifyFieldProperties);
		rdGroup.setSelected (defaultMode.getModel(), true);
		
		LinePanel l0c = new LinePanel ();
		l0c.add(new JWidthLabel (Translator.swap ("FiLocalLayerForm.age") + " : ", 50));
		age = new JTextField(5);
		String v = Settings.getProperty("fi.local.tree.form.last.layerSetAge", ""+1);
		age.setText(v);
		age.addActionListener(this);
		age.setEnabled(false);
		l0c.add(age);
		l0c.add(new JWidthLabel (Translator.swap ("FiLocalLayerForm.fertility") + " : ", 50));
		fertility = new JTextField(5);
		v = Settings.getProperty("fi.local.tree.form.last.layerSetFertility", ""+1.5);
		fertility.setText(v);
		fertility.addActionListener(this);
		fertility.setEnabled(false);
		l0c.add(fertility);
		
		l0c.addGlue();
		layerSetProps.add (l0c);

		LinePanel l0d = new LinePanel ();
		l0d.addGlue ();

		addLayerSet = new JButton (Translator.swap ("FiLocalLayerForm.addLayerSet"));
		addLayerSet.addActionListener (this);
		l0d.add (addLayerSet);
		l0d.addGlue ();
		layerSetProps.add(l0d);
		layerSetProps.add(addLayerSet);
		
		layersAdder.add(layerSetProps);
		
		
		// layers
		ColumnPanel layerProps = new ColumnPanel (Translator.swap ("FiLocalLayerForm.buildWithIndividualLayers"));

		// Species
		LinePanel l1 = new LinePanel ();
		l1.add (new JWidthLabel (Translator.swap ("FiLocalLayerForm.species") + " : ", 100));
		speciesCombo = new JComboBox (new Vector (speciesNames));
		speciesCombo.addActionListener (this);
		l1.add (speciesCombo);
		l1.addStrut0 ();
		layerProps.add (l1);
		// validity range
		LinePanel l1a = new LinePanel ();
		l1a.add (new JWidthLabel (Translator.swap ("FiLocalLayerForm.validityRange") + " : ", 100));
		lowerHeightBoundary = new JTextField (""+getLowerBoundary());
		lowerHeightBoundary.addActionListener (this);
		l1a.add (lowerHeightBoundary);
		l1a.add (new JWidthLabel (Translator.swap ("FiLocalLayerForm.and") + " : ", 100));
		upperHeightBoundary = new JTextField (""+getUpperBoundary());
		upperHeightBoundary.addActionListener (this);
		l1a.add (upperHeightBoundary);
		l1a.addStrut0 ();
		
		layerProps.add (l1a);
		// layerHeight
		LinePanel l2 = new LinePanel ();
		l2.add (new JWidthLabel (Translator.swap ("FiLocalLayerForm.layerHeight") + " : ", 200));
		layerHeight = new JTextField (5);
		
		v = Settings.getProperty ("fi.local.tree.form.last.layerHeight", ""+1);
		layerHeight.setText (v); 
		
		layerHeight.addActionListener (this);
		l2.add (layerHeight);
		l2.addStrut0 ();
		//layerProps.add (l2);
		layerProps.add (l2);
		// Edit buttons
		LinePanel l3 = new LinePanel ();
		l3.addGlue ();
		add = new JButton (Translator.swap ("FiLocalLayerForm.add"));
		add.addActionListener (this);
		l3.add (add);
		layerProps.add(l3);
		layersAdder.add(layerProps);
		part1.add (layersAdder, BorderLayout.NORTH);


		// Remove buttons	
		LinePanel l21 = new LinePanel ();					
		l21.addGlue ();
		remove = new JButton (Translator.swap ("FiLocalLayerForm.remove"));
		remove.addActionListener (this);
		l21.add (remove);

		clear = new JButton (Translator.swap ("FiLocalLayerForm.clear"));
		clear.addActionListener (this);
		l21.add (clear);
		l21.addGlue ();

		part1.add (ColumnPanel.addWithStrut0 (l21), BorderLayout.SOUTH);
		part1.setMinimumSize (new Dimension (100, 100));


		// Part2. LayerSet
		part2 = new ColumnPanel (0, 0);

		// 2.1 Table panel
		JPanel tablePanel = new JPanel (new BorderLayout ());
		tablePanel.setBorder (BorderFactory.createTitledBorder (Translator.swap (
		"FiLocalLayerForm.table")));

		// Table
		table = new JTable (tableModel);
		table.setAutoCreateRowSorter (true);
		TableColumn col = table.getColumnModel ().getColumn (0);
		col.setPreferredWidth (150);
		JScrollPane scroll = new JScrollPane (table);
			scroll.setMinimumSize (new Dimension (100, 150));	// fc - 27.10.2009
		tablePanel.add (scroll, BorderLayout.CENTER);

		// 2.2 load, LAI, percentage sum + manual
		ColumnPanel bottom = new ColumnPanel (0, 0);

		LinePanel l28 = new LinePanel ();
		l28.addGlue ();
		l28.add (new JLabel (Translator.swap ("FiLocalLayerForm.layerSetLoad")+" : "));
		layerSetLoad = new JTextField (4);
		layerSetLoad.setPreferredSize (new Dimension (30, layerSetLoad.getPreferredSize ().height));
		layerSetLoad.setEditable (false);
		l28.add (layerSetLoad);
		l28.addStrut0 ();
		bottom.add (l28);

		LinePanel l29 = new LinePanel ();
		l29.addGlue ();
		l29.add (new JLabel (Translator.swap ("FiLocalLayerForm.layerSetLAI")+" : "));
		layerSetLAI = new JTextField (4);
		layerSetLAI.setPreferredSize (new Dimension (30, layerSetLAI.getPreferredSize ().height));
		layerSetLAI.setEditable (false);
		l29.add (layerSetLAI);
		l29.addStrut0 ();
		bottom.add (l29);


		LinePanel l30 = new LinePanel ();
		l30.addGlue ();
		l30.add (new JLabel (Translator.swap ("FiLocalLayerForm.layerSetInternalCover")+" : "));
		layerSetInternalCover = new JTextField (4);
		layerSetInternalCover.setPreferredSize (new Dimension (30, layerSetInternalCover.getPreferredSize ().height));
		layerSetInternalCover.setEditable (false);
		l30.add (layerSetInternalCover);
		l30.addStrut0 ();
		bottom.add (l30);

		// User explanation
		LinePanel l9b = new LinePanel ();
		l9b.add (new JLabel ("<html>"
				+Translator.swap ("FiLocalLayerForm.usage")
				+"</html>"));
		l9b.addStrut0 ();
		bottom.add (l9b);
		bottom.addStrut0 ();

		part2.add (tablePanel);
		part2.add (bottom);
		part2.setMinimumSize (new Dimension (100, 150));



		//~ setLayout (new BorderLayout ());
		//~ JSplitPane split = new JSplitPane (JSplitPane.VERTICAL_SPLIT, new JScrollPane (part1), part2);
		//~ split.setResizeWeight (0.5);
		//~ split.setContinuousLayout (true);
		//~ split.setBorder (null);
		//~ add (split, BorderLayout.CENTER);
		//~ setPreferredSize (new Dimension (200, 450));

	}

	/**	Returns true if the layerSet editor is shown.
	 */
	private boolean isEditorShown () {
		return creationMode ||
		(withShowHideEditor
				&& showHideEditor != null
				&& showHideEditor.getText ().equals (HIDE_EDITOR));
	}

	/**	Updates the UI according to current context.
	 */
	private void refreshUI () {
		removeAll ();

		// Layout according to current options
		// - if the editor is required, use a split pane
		// - otherwise only the layerSet table
		setLayout (new BorderLayout ());

		// Edition mode: editor part is hidden, a button to show/hide it
		if (withShowHideEditor) {
			add (showHideEditorLine, BorderLayout.NORTH);
		}

		// If the editor is/can be opened, use a split pane
		if (isEditorShown ()) {
			split = new JSplitPane (JSplitPane.VERTICAL_SPLIT ,  part1, part2);
			split.setResizeWeight (0.5);
			split.setContinuousLayout (true);
			split.setBorder (null);
			add (split, BorderLayout.CENTER);
			setPreferredSize (new Dimension (200, 450));

		} else {
			// If no editor at all, no split pane
			add (part2, BorderLayout.CENTER);
			setPreferredSize (new Dimension (200, 250));
		}

		revalidate ();
	}

	private String getSpeciesName () {return (String) speciesCombo.getSelectedItem ();}
	private double getLowerBoundary () {return propertyMap.getLowerBoundary(getSpeciesName ());}
	private double getUpperBoundary () {return propertyMap.getUpperBoundary(getSpeciesName ());}
	private double getLayerHeight () {return Check.doubleValue (layerHeight.getText ().trim ());}
	private String getDefaultLayerSetName () {return (String) layerSetCombo.getSelectedItem ();}
	private int getAge () {
		if (this.specifyFieldProperties.isSelected()) {
				return Check.intValue (age.getText ().trim ());
		} else {
			return -1;
		}	
	}
	private double getFertility () {
		if (this.specifyFieldProperties.isSelected()) {
				return Check.doubleValue (fertility.getText ().trim ());
		} else {
			return -1;
		}	
	}
}
