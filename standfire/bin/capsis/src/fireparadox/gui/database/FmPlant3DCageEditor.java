package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import jeeb.lib.util.Vertex3d;
import capsis.commongui.util.Helper;
import capsis.util.History;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBCommunicator;
import fireparadox.model.database.FmDBParameter;
import fireparadox.model.database.FmDBParticle;
import fireparadox.model.database.FmDBPlant;
import fireparadox.model.database.FmDBShape;
import fireparadox.model.database.FmDBUpdator;
import fireparadox.model.database.FmDBVoxel;

/**
 * FiPlant3DCageEditor : plant crown 3D design for CAGE METHOD (vs cube method)
 *
 * @author I. Lecomte - October 2009
 */
public class FmPlant3DCageEditor extends AmapDialog implements ActionListener, jeeb.lib.util.Listener {

	protected FmModel model;
	protected FmDBCommunicator bdCommunicator;		//to read database
	protected FmDBUpdator bdUpdator;					//to update database

	//sample information
	protected FmDBShape shape;
	protected FmDBShape shape2D;
	protected FmDBPlant plant;
	protected long plantId, shapeId;							//id in the database
	protected HashMap<Long, FmDBVoxel> voxelMap;	//map of sample voxels
	protected int sampleType;
	protected boolean isVoxels;

	//Grid color values
	protected int [][][] gridVoxelValues;				//voxels after  update
	protected FmDBVoxel [][][] gridVoxelRef;			//voxel references
	protected int iNbVoxels, jNbVoxels, kNbVoxels;		//grid dimensions in number of voxels
	protected int iSizeVoxels, jSizeVoxels, kSizeVoxels;	//Size of the voxels in cm
	protected int nbVoxels;


	//Selections
	protected int iSelected,jSelected,kSelected;		//voxel coordinate


	//Color control
	protected Map<Integer,Color> colorMap;
	protected FmColorFreeLegend colorLegendPanel;
	protected int selectedColor = 99;

	//Voxel panel 2D
	protected FmVoxel2DPanel voxelPanel2D;
	boolean addParticlePossible;

	//Particle panel
	protected FmVoxelParticleCageEditor voxelParticlePanel;
	protected String [] particleName;
	protected long [][][][][] particleId;
	protected long [][][][][] biomassId;
	protected double [][][][][] newBiomass;
	protected double [][][][][] oldBiomass;
	protected int nbParticle;
	protected Vector particleList;
	protected int nbParticleMax = 30;

	//Split pane for selector and panel 2D
	protected JSplitPane splitPanel;
	protected FmVoxelMatrixDepthSelector selector; //Selector panel


	//Validation control
	protected JButton addParticle;
	protected JButton save;
	protected JButton cancel;
	protected JButton help;

	//for undo/redo
	protected JButton undoButton;
	protected JButton redoButton;
	protected History selectionHistory;
	protected boolean firstHistory = false;


	/**	Constructor for 3D.
	 */
	public FmPlant3DCageEditor (FmModel _model, FmDBShape _shape,   FmDBShape _shape2D) {

		model = _model;
		shape = _shape;
		shape2D = _shape2D;

		addParticlePossible = true;

		if (shape != null) {
			shapeId = shape.getShapeId();
			plant = shape.getPlant();
			plantId = plant.getPlantId();
			if ((plant.isValidated()) || (shape.isDeleted())) {
				addParticlePossible = false;
			}
		}


		//connecting database
		bdCommunicator = model.getBDCommunicator ();
		bdUpdator = model.getBDUpdator ();

		//history for undo/redo
		selectionHistory = new History ();

		//Load sample info and voxels list from database
		setGridSize ();
		loadVoxels ();

		//2D shape already existe, 3D can be generated
		if ((!isVoxels) && (shape2D != null))  {
			String kind = shape2D.getShapeKind();
			if (kind.equals("XZ")) 		generate3Dfrom2D ();		//from 2D
			if (kind.equals("XZ_YZ")) 	generate3Dfrom2DD ();		//from 2*2D
			if (kind.equals("XYZ")) 	generate3Dfrom3D ();		//from 3D
			addParticlePossible = false;
		}

		//The selected cell is (0,0,0)
		iSelected = 0;
		jSelected = 0;
		kSelected = 0;

		createUI ();

		//synchronize voxel particule panel with selected cell type
		voxelParticlePanel.changeSelect (iSelected, jSelected, kSelected);

		show ();

	 }

	/**	Actions on the buttons
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (save)) {
			validateAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (addParticle)) {
			voxelParticlePanel.addParticleAction ();
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**	Control before validation
	 */
	protected boolean controlCellValues ()   {

		int total = -1;

		//all voxels have to be filled
		for (int i=0; i<iNbVoxels ; i++) {
			for (int j=0; j<jNbVoxels ; j++) {
				for (int k=0; k<kNbVoxels ; k++) {
					total += gridVoxelValues[i][j][k];
				}
			}
		}


		if (total <= 0) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPlant3DCageEditor.fillOneVoxel"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}


		return true;
	}
	/**	Validation
	 */
	protected void validateAction () {

		voxelParticlePanel.saveValues();

		gridVoxelValues = voxelPanel2D.getNewVoxelValues ();
		newBiomass = voxelParticlePanel.getNewBiomass ();
		oldBiomass = voxelParticlePanel.getOldBiomass ();
		nbParticle = voxelParticlePanel.getNbParticle ();
		particleName = voxelParticlePanel.getParticleName ();
		particleId = voxelParticlePanel.getParticleId ();
		biomassId = voxelParticlePanel.getBiomassId ();

		if (controlCellValues()) {

			try {

				//PLANT CREATION
				if (plantId < 0) {
					plantId = bdUpdator.createPlant (plant, 1);	// create in the database
					if (plantId > 0) {
						plant.setPlantId (plantId);
					}
				}


				//SHAPE CREATION
				if (shapeId < 0) {


					shapeId = bdUpdator.createShape (shape);				// create in the database

					if (shapeId > 0) {

						shape.setShapeId (shapeId);

						//voxel creation
						for (int i=0; i<iNbVoxels ; i++) {
							for (int j=0; j<jNbVoxels ; j++) {
								for (int k=0; k<kNbVoxels ; k++) {

									voxelCreation (i, j, k);
								}
							}
						}
					}
				}

				//BIOMASSE UPDATE
				else {

					for (int i=0; i<iNbVoxels ; i++) {
						for (int j=0; j<jNbVoxels ; j++) {
							for (int k=0; k<kNbVoxels ; k++) {

								FmDBVoxel voxel = gridVoxelRef[i][j][k];
								if (voxel != null) voxelUpdate (i, j, k);
								else voxelCreation (i, j, k);


							}
						}
					}
				}

			} catch (Exception e) {
				Log.println (Log.ERROR, "FiPlant3DCageEditor", "error while CREATING sample in data base", e);
			}

			System.out.println("CREATION FORME TERMINEE !");

			setValidDialog (true);
		}
	}

	/**	Voxel creation in the shape
	 */
	private void voxelCreation (int _i, int _j, int _k) {

		try {
			int i = _i;
			int j = _j;
			int k = _k;
			boolean creation = false;

			//prepare particle name list with biomasses
			double [] newAlive = new double [nbParticleMax];
			double [] newDead = new double [nbParticleMax];

			for (int part=0; part<nbParticle ; part++) {
				newAlive [part] = newBiomass [part][i][j][k][0];
				newDead [part] = newBiomass [part][i][j][k][1];

				if ((newAlive [part] >= 0) || (newDead [part] >= 0)) creation = true;
				if (newAlive [part] > 0)  newAlive [part] = newAlive [part] / 1000.0; //convert grammes to kilos
				if (newDead [part] > 0)  newDead [part] = newDead [part] / 1000.0; //convert grammes to kilos
			}

			//voxel creation
			if (creation) {
				long voxelId = -1;
				boolean edge = false;
				FmDBVoxel newVoxel = new FmDBVoxel (voxelId, i, j, k, null, edge);
				voxelId = bdUpdator.createCellComplexe (shapeId, newVoxel, particleName, newAlive, newDead);
				System.out.println("CELL CREATION ID="+voxelId);
			}


		} catch (Exception e) {
				Log.println (Log.ERROR, "FiPlant3DCageEditor", "error while CREATING VOXEL in data base", e);
		}
	}

	/**	Voxel creation in the shape
	 */
	private void voxelUpdate (int _i, int _j, int _k) {

		try {

			int i = _i;
			int j = _j;
			int k = _k;


			FmDBVoxel voxel = gridVoxelRef[i][j][k];
			long voxelId = voxel.getDBId();

			for (int part =0; part<nbParticle ; part++) {

				String partName  = particleName [part];


				double newAlive = newBiomass [part][i][j][k][0];
				double newDead  = newBiomass [part][i][j][k][1] ;
				double oldAlive = oldBiomass [part][i][j][k][0];
				double oldDead  = oldBiomass [part][i][j][k][1] ;



				if (newAlive != oldAlive) {

					//convert grammes to kilos
					if (newAlive > 0)  newAlive = newAlive / 1000.0;

					Long biomassID  = biomassId [part][i][j][k][0];
					if (biomassID > 0) {
						//delete particle
						if (newAlive == -9) {
							Long partID  = particleId [part][i][j][k][0];
							bdUpdator.deleteCellParticle (voxelId, partID);

						}
						//update particle
						else {
							bdUpdator.updateParameter (biomassID, newAlive);
						}
					}
					//Adding ALIVE particle in the  voxel
					else if (newAlive != -9)  {
						long partID = bdUpdator.addCellParticle (shapeId, voxelId, partName,
															newAlive , "Alive");
					}

				}

				if (newDead != oldDead) {


					//convert grammes to kilos
					if (newDead > 0)  newDead = newDead / 1000.0;

					Long biomassID  = biomassId [part][i][j][k][1];
					if (biomassID > 0) {
						//delete particle
						if (newDead == -9) {
							Long partID  = particleId [part][i][j][k][1];
							bdUpdator.deleteCellParticle (voxelId, partID);

						}
						//update particle
						else {

							bdUpdator.updateParameter (biomassID, newDead);
						}
					}
					//Adding DEAD particle in the  voxel
					else if (newDead != -9) {

						long partID = bdUpdator.addCellParticle (shapeId, voxelId, partName,
																newDead , "Dead");
					}
				}

			}
		} catch (Exception e) {
				Log.println (Log.ERROR, "FiPlant3DCageEditor", "error while UPDATING VOXEL in data base", e);
		}
	}

	/**	Initialize the GUI.
	 */
	protected void createUI () {

		/*********** Fuel info and color legend panel **************/
		ColumnPanel legend = new ColumnPanel ();

		LinePanel bb = new LinePanel ();

		FmPlantInfoPanel plantInfoPanel = new FmPlantInfoPanel (plant);
		bb.add (plantInfoPanel);

		FmShapeInfoPanel shapeInfoPanel = new FmShapeInfoPanel (shape);
		bb.add (shapeInfoPanel);

		legend.add (bb);

		colorLegendPanel = new FmColorFreeLegend (true);
		colorLegendPanel.addListener (this);
		legend.add (colorLegendPanel);
		colorMap = colorLegendPanel.getColorMap();

		/*********** VOXEL view and depth selector  **************/
		selector = new FmVoxelMatrixDepthSelector (
						gridVoxelValues,
						iNbVoxels,
						jNbVoxels,
						kNbVoxels,
						colorMap);
		selector.addListener (this);

		/*********** VOXEL 2D panel  **************/
		voxelPanel2D = new FmVoxel2DPanel (gridVoxelValues, iNbVoxels, jNbVoxels, kNbVoxels,
											iSizeVoxels, jSizeVoxels, kSizeVoxels,
											colorMap, true, sampleType, "");
		voxelPanel2D.addListener (this);
		voxelPanel2D.setColor (selectedColor);

		splitPanel = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, selector, voxelPanel2D);
		splitPanel.setResizeWeight(0.5);

		/*********** VOXEL particle panel  **************/
		voxelParticlePanel = new FmVoxelParticleCageEditor (model, shape, iNbVoxels, jNbVoxels, kNbVoxels,
														newBiomass, particleList);
		voxelParticlePanel.addListener (this);
		voxelParticlePanel.changeSelect (iSelected, jSelected, kSelected);
		legend.add (voxelParticlePanel);

		/*********** Control panel **************/
		JPanel controlPanel = new JPanel ();
		controlPanel.setLayout (new FlowLayout (FlowLayout.LEFT));

		if (addParticlePossible) {
			addParticle = new JButton (Translator.swap ("FiPlant3DCageEditor.addParticle"));
			addParticle.addActionListener (this);
			controlPanel.add (addParticle);
		}
		save = new JButton (Translator.swap ("FiPlant3DCageEditor.validate"));
		controlPanel.add (save);
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		cancel.addActionListener (this);
		controlPanel.add (cancel);
		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		controlPanel.add (help);

		if ((plant.isValidated()) || (shape.isDeleted())) {
			save.setEnabled(false);
		}
		else {
			save.addActionListener (this);
		}


		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (splitPanel, BorderLayout.CENTER);
		getContentPane ().add (legend, BorderLayout.EAST);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		setTitle (Translator.swap ("FiPlant3DCageEditor.title"));

		this.setSize (new Dimension(800,600));

		setModal (true);
	}
	/**
	* grid dimension for plant in 3D
	*/
	protected void setGridSize () {

		//for sample, column dimension is sample height
		iSizeVoxels = (int) (shape.getVoxelXSize() * 100);	//convert m to cm
		jSizeVoxels = (int) (shape.getVoxelYSize() * 100);
		kSizeVoxels = (int) (shape.getVoxelZSize() * 100);

		iNbVoxels = (int) ((shape.getXMax () * 100) / iSizeVoxels);
		jNbVoxels = (int) ((shape.getYMax () * 100) / jSizeVoxels);
		kNbVoxels = (int) ((shape.getZMax () * 100) / kSizeVoxels);

		gridVoxelValues = new int[iNbVoxels][jNbVoxels][kNbVoxels];
		gridVoxelRef    = new FmDBVoxel [iNbVoxels][jNbVoxels][kNbVoxels];
		newBiomass = new double [nbParticleMax][iNbVoxels][jNbVoxels][kNbVoxels][2];


		//all voxels have to be filled
		for (int i=0; i<iNbVoxels ; i++) {
			for (int j=0; j<jNbVoxels ; j++) {
				for (int k=0; k<kNbVoxels ; k++) {
					gridVoxelValues[i][j][k]  = 0;
					gridVoxelRef[i][j][k]  = null;

					for (int np =0; np<nbParticleMax ; np++) {
						newBiomass [np][i][j][k][0] = -9;
						newBiomass [np][i][j][k][1] = -9;
					}
				}
			}
		}


	}

	/**	Load shape voxels from database
	 */
	protected void loadVoxels () {

		isVoxels = false;

		try {

			shape = bdCommunicator.getShapeVoxels (shape, false);	//to get all shape info
			voxelMap = shape.getVoxels();

			//if there is voxels
			if ((voxelMap != null) && (voxelMap.size() > 0)) {

				isVoxels = true;

				for (Iterator iter = voxelMap.keySet().iterator(); iter.hasNext ();) {

					Object cle = iter.next();
					FmDBVoxel voxel = voxelMap.get(cle);
					int i = voxel.getI();
					int j = voxel.getJ();
					int k = voxel.getK();

					if ((i<iNbVoxels) && (j<jNbVoxels)) {
						gridVoxelRef[i][j][k] = voxel;
						gridVoxelValues[i][j][k] = 9;
					}
				}
			}

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPlant3DCageEditor.loadVoxels() ", "error while opening FUEL data base", e);
		}
	}


	/** Return the new shape id
	 */
	public long getShapeId () {
		return shapeId;
	}

	/**
	* generate 3D from 2D
	*/
	private void generate3Dfrom2D () {

		double[][][][] biomass = new double[nbParticleMax][iNbVoxels][kNbVoxels][2];
		newBiomass = new double[nbParticleMax][iNbVoxels][jNbVoxels][kNbVoxels][2];

		for (int np =0; np<nbParticleMax ; np++) {
			for (int i=0 ; i<iNbVoxels ; i++) {
				for (int j=0 ; j<jNbVoxels ; j++) {
					for (int k=0 ; k<kNbVoxels ; k++) {
						biomass [np][i][k][0] = -9;
						biomass [np][i][k][1] = -9;
						newBiomass [np][i][j][k][0] = -9;
						newBiomass [np][i][j][k][1] = -9;
					}
				}
			}
		}


		int index = 0;
		biomass = fillBiomasse2D (shape2D, biomass, index);


		for (int np =0; np<nbParticle ; np++) {

			for (int i=0 ; i<iNbVoxels ; i++) {

				for (int j=0 ; j<jNbVoxels ; j++) {

					double d = distance (i, j);


					for (int k=0 ; k<kNbVoxels ; k++) {
						//alive convert form kilos to grammes
						double val = biomasseDistributionCentered (d,  k, np, 0, biomass);
						if (val > 0) {
							val = val * 1000.0;
							val = Math.round(val * Math.pow(10, 3)) / Math.pow(10, 3);
						}
						newBiomass [np][i][j][k][0] = val;

						//dead convert form kilos to grammes
						val = biomasseDistributionCentered (d,  k, np, 1, biomass);
						if (val > 0) {
							val = val * 1000.0;
							val = Math.round(val * Math.pow(10, 3)) / Math.pow(10, 3);
						}
						newBiomass [np][i][j][k][1] = val;

						if (!((newBiomass [np][i][j][k][0] < 0) &&  (newBiomass [np][i][j][k][1] < 0))) {
							gridVoxelValues[i][j][k] = 9;
						}
					}
				}
			}
		}


	}

	/**
	* generate 3D from 2*2D
	*/
	private void generate3Dfrom2DD () {

		double[][][][] biomass = new double[nbParticleMax][iNbVoxels][kNbVoxels][2];
		double[][][][] biomass2 = new double[nbParticleMax][jNbVoxels][kNbVoxels][2];
		newBiomass = new double[nbParticleMax][iNbVoxels][jNbVoxels][kNbVoxels][2];

		for (int np =0; np<nbParticleMax ; np++) {
			for (int i=0 ; i<iNbVoxels ; i++) {
				for (int j=0 ; j<jNbVoxels ; j++) {
					for (int k=0 ; k<kNbVoxels ; k++) {
						biomass [np][i][k][0] = -9;
						biomass2 [np][j][k][1] = -9;
						newBiomass [np][i][j][k][0] = -9;
						newBiomass [np][i][j][k][1] = -9;
					}
				}
			}
		}

		int index  = jNbVoxels/2;
		biomass  = fillBiomasse2D  (shape2D, biomass, index);
		index  = iNbVoxels/2;
		biomass2 = fillBiomasse2DD (shape2D, biomass2, index);

		for (int np =0; np<nbParticle ; np++) {
			for (int i=0 ; i<iNbVoxels ; i++) {
				for (int j=0 ; j<jNbVoxels ; j++) {

					double d = distance (i, j);

					for (int k=0 ; k<kNbVoxels ; k++) {

						//ALIVE
						double val1 = biomasseDistributionCentered (d,  k, np, 0, biomass); //for the first 2D
						double val2 = biomasseDistributionCentered (d,  k, np, 0, biomass2);//for the second 2D

						//mean calculation and convert form kilos to grammes
						double val;
						if (val1 < 0) val1 = 0.0;
						if (val2 < 0) val2 = 0.0;

						val = 0.5 * (val1 + val2) * 1000.0;
						val = Math.round(val * Math.pow(10, 3)) / Math.pow(10, 3);
						newBiomass [np][i][j][k][0] = val;


						//DEAD
						val1 = biomasseDistributionCentered (d,  k, np, 1, biomass); //for the first 2D
						val2 = biomasseDistributionCentered (d,  k, np, 1, biomass2);//for the second 2D

						//mean calculation and convert form kilos to grammes
						if (val1 < 0) val = val2 * 1000.0;
						else if (val2 < 0) val = val1 * 1000.0;
						else val = 0.5 * (val1 + val2) * 1000.0;
						val = Math.round(val * Math.pow(10, 3)) / Math.pow(10, 3);
						newBiomass [np][i][j][k][1] = val;


						if (!((newBiomass [np][i][j][k][0] < 0) &&  (newBiomass [np][i][j][k][1] < 0))) {
							gridVoxelValues[i][j][k] = 9;
						}
					}
				}
			}
		}
	}
	/**
	* generate 3D from 3D
	*/
	private void generate3Dfrom3D () {
		double[][][][][] biomass = new double[nbParticleMax][iNbVoxels][jNbVoxels][kNbVoxels][2];
		newBiomass = new double[nbParticleMax][iNbVoxels][jNbVoxels][kNbVoxels][2];

		for (int np =0; np<nbParticleMax ; np++) {
			for (int i=0 ; i<iNbVoxels ; i++) {
				for (int j=0 ; j<jNbVoxels ; j++) {
					for (int k=0 ; k<kNbVoxels ; k++) {
						biomass [np][i][j][k][0] = -9;
						biomass [np][i][j][k][1] = -9;
						newBiomass [np][i][j][k][0] = -9;
						newBiomass [np][i][j][k][1] = -9;
					}
				}
			}
		}

		biomass = fillBiomasse3D (shape2D, biomass);

		//crown dimmensions
		double rayMax = 0.5 * Math.max (iNbVoxels, jNbVoxels);
		double maxDiameterHeight = plant.getMaxDiameterHeight ();
		double cbh = plant.getCrownBaseHeight ();
		if (maxDiameterHeight > 0) {
			maxDiameterHeight = maxDiameterHeight - cbh;
		}


		//average biomasses calculation
		for (int np =0; np<nbParticle ; np++) {

			int alive = 0;
			int dead = 0;
			double biomassAlive = -9;
			double biomassDead = -9;

			for (int i=0 ; i<iNbVoxels ; i++) {
				for (int j=0 ; j<jNbVoxels ; j++) {
					for (int k=0 ; k<kNbVoxels ; k++) {
						if (biomass [np][i][j][k][0] >=  0) {
							alive++;
							if (biomassAlive < 0) biomassAlive = 0;
							biomassAlive +=biomass [np][i][j][k][0] ;
						}
						if (biomass [np][i][j][k][1] >=  0) {
							dead++;
							if (biomassDead < 0) biomassDead = 0;
							biomassDead +=biomass [np][i][j][k][1]  ;
						}
					}
				}
			}

			//Set the average in the right table
			for (int i=0 ; i<iNbVoxels ; i++) {
				for (int j=0 ; j<jNbVoxels ; j++) {
					for (int k=0 ; k<kNbVoxels ; k++) {

						//check if the voxel is inside the shape
						boolean isInside = true;
						if (maxDiameterHeight > 0) {
							double ray = Math.pow((iNbVoxels / 2) - (i + 0.5),2) + Math.pow((jNbVoxels / 2) - (j + 0.5),2);
							ray = Math.sqrt(ray);
							if (k < maxDiameterHeight) {
								double r = rayMax * Math.sqrt( kNbVoxels / maxDiameterHeight);
								if (ray <= r) isInside = true;
								else isInside = false;
							}
							else {
								double r = rayMax * Math.sqrt( (kNbVoxels - k) / (kNbVoxels - maxDiameterHeight));
								if (ray <= r) isInside = true;
								else isInside = false;
							}
						}

						if (isInside) {

							if (biomassAlive >=  0) {

								//convert form kilos to grammes
								Double val = (biomassAlive/alive) * 1000.0;
								val = Math.round(val * Math.pow(10, 3)) / Math.pow(10, 3);
								newBiomass [np][i][j][k][0] = val ;
								gridVoxelValues[i][j][k] = 9;
							}
							if (biomassDead >=  0) {
								Double val = (biomassDead/dead) * 1000.0;
								val = Math.round(val * Math.pow(10, 3)) / Math.pow(10, 3);
								newBiomass [np][i][j][k][1] = val ;
								gridVoxelValues[i][j][k] = 9;
							}
						}
					}
				}
			}
		}
	}



	/**
	* fill biomass table with 2D shape voxel biomass
	*/
	private double[][][][] fillBiomasse2D (FmDBShape _shape, double[][][][] _biomass, int _select) {

		try {

			_shape = bdCommunicator.getShapeVoxels (_shape, false);	//to get all shape info

			HashMap voxels = _shape.getVoxels ();
			particleList= new Vector();

			//For each voxels
			if (voxels != null) {
				for (Iterator t = voxels.keySet().iterator(); t.hasNext ();) {
					Object cle = t.next();
					FmDBVoxel voxel = (FmDBVoxel) voxels.get(cle);

					int i = voxel.getI();
					int j = voxel.getJ();
					int k = voxel.getK();

					//FOR 2D j=0 !!!!

					if (j == _select) {

						HashMap particleMap = voxel.getParticleMap();
						if (particleMap != null) {

							//for each particle
							for (Object o : particleMap.values()) {

								//Store name
								FmDBParticle particle = (FmDBParticle) o;
								String name = particle.getName();
								Long partId = particle.getId();
								Long id = particle.getId();
								if (!particleList.contains (name)) {
									particleList.add (name);
									nbParticle++;
								}

								int part  = particleList.indexOf (name);
								if (part >=0) {

									//for each parameter
									HashMap parameterMap = particle.getParameterMap();
									for (Object o2 : parameterMap.values()) {
										FmDBParameter parameter = (FmDBParameter) o2;
										if (parameter.getName().equals("Biomass")) {
											double val = parameter.getValue();


											if (particle.isAlive()) {
												_biomass [part][i][k][0] = val;
											}
											else {
												_biomass [part][i][k][1] = val;
											}
										}
									}
								}
							}
						}
					}

				}
			}
		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPlant3DCageEditor.fillBiomasse() ", "error while opening FUEL data base", e);
		}

		return _biomass;

	}
/**
	* fill biomass table with 2*2D shape voxel biomass
	*/
	private double[][][][] fillBiomasse2DD (FmDBShape _shape, double[][][][] _biomass, int _select) {

		try {

			_shape = bdCommunicator.getShapeVoxels (_shape, false);	//to get all shape info

			HashMap voxels = _shape.getVoxels ();
			particleList= new Vector();

			//For each voxels
			if (voxels != null) {
				for (Iterator t = voxels.keySet().iterator(); t.hasNext ();) {
					Object cle = t.next();
					FmDBVoxel voxel = (FmDBVoxel) voxels.get(cle);

					int i = voxel.getI();
					int j = voxel.getJ();
					int k = voxel.getK();

					//FOR 2*2D i=nbIvoxels/2 !!!!

					if (i == _select) {

						HashMap particleMap = voxel.getParticleMap();
						if (particleMap != null) {

							//for each particle
							for (Object o : particleMap.values()) {

								//Store name
								FmDBParticle particle = (FmDBParticle) o;
								String name = particle.getName();
								Long partId = particle.getId();
								Long id = particle.getId();
								if (!particleList.contains (name)) {
									particleList.add (name);
									nbParticle++;
								}

								int part  = particleList.indexOf (name);
								if (part >=0) {

									//for each parameter
									HashMap parameterMap = particle.getParameterMap();
									for (Object o2 : parameterMap.values()) {
										FmDBParameter parameter = (FmDBParameter) o2;
										if (parameter.getName().equals("Biomass")) {
											double val = parameter.getValue();

											if (particle.isAlive()) {
												_biomass [part][j][k][0] = val;
											}
											else {
												_biomass [part][j][k][1] = val;
											}
										}
									}
								}
							}
						}
					}

				}
			}
		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPlant3DCageEditor.fillBiomasse() ", "error while opening FUEL data base", e);
		}

		return _biomass;

	}
/**
	* fill biomass table with 3D shape voxel biomass
	*/
	private double[][][][][] fillBiomasse3D (FmDBShape _shape, double[][][][][] _biomass) {

		try {

			_shape = bdCommunicator.getShapeVoxels (_shape, false);	//to get all shape info

			HashMap voxels = _shape.getVoxels ();
			particleList= new Vector();

			//For each voxels
			if (voxels != null) {
				for (Iterator t = voxels.keySet().iterator(); t.hasNext ();) {
					Object cle = t.next();
					FmDBVoxel voxel = (FmDBVoxel) voxels.get(cle);

					int i = voxel.getI();
					int j = voxel.getJ();
					int k = voxel.getK();


						HashMap particleMap = voxel.getParticleMap();
						if (particleMap != null) {

							//for each particle
							for (Object o : particleMap.values()) {

								//Store name
								FmDBParticle particle = (FmDBParticle) o;
								String name = particle.getName();
								Long partId = particle.getId();
								Long id = particle.getId();
								if (!particleList.contains (name)) {
									particleList.add (name);
									nbParticle++;
								}

								int part  = particleList.indexOf (name);
								if (part >=0) {

									//for each parameter
									HashMap parameterMap = particle.getParameterMap();
									for (Object o2 : parameterMap.values()) {
										FmDBParameter parameter = (FmDBParameter) o2;
										if (parameter.getName().equals("Biomass")) {
											double val = parameter.getValue();

											if (particle.isAlive()) {
												_biomass [part][i][j][k][0] = val;
											}
											else {
												_biomass [part][i][j][k][1] = val;
											}
										}
									}
								}
							}

					}

				}
			}
		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPlant3DCageEditor.fillBiomasse() ", "error while opening FUEL data base", e);
		}

		return _biomass;

	}
	/**
	* distance in number of voxels between 2 voxels
	*/
	private double distance (int i, int j) {

		double demie = 0.5;

		return Math.sqrt
		(
			Math.pow(((iNbVoxels/2d)-(i+demie)),2)
			+
			Math.pow(((jNbVoxels/2d)-(j+demie)),2)
		);

	}

	private double biomasseDistributionCentered (double dist,  int k, int particle, int alive, double[][][][]biomass) {

		double middle = (iNbVoxels / 2d);
		if (dist > middle) return -9.0;

		double part1 = ((iNbVoxels) /2d) - dist;
		double part2 = ((iNbVoxels) /2d) + dist;

		double res1 = biomasseDistribution (part1,  k,  particle, alive, biomass);
		double res2 = biomasseDistribution (part2,  k,  particle, alive, biomass);



		return (0.5 *(res1 +res2));

	}

	private double biomasseDistribution (double x,  int k, int particle, int alive, double[][][][] biomass) {

		double demie = 0.5;
		int xSup = (int) Math.floor (x + demie); //x+1/2
		int xInf = (int) Math.floor (x - demie); //x-1/2

		if (x < demie)  {
			return Math.max(0d,biomass [particle][0][k][alive]);
		}
		else if (x >= (iNbVoxels - 1 + demie) ) {
			return Math.max(0d,biomass [particle][iNbVoxels-1][k][alive]);
		}
		else {
			double biomassA = Math.max(0d, biomass [particle] [xInf][k][alive]);
			double biomassB = Math.max(0d, biomass [particle] [xSup][k][alive]);
			double alphaB = demie + xSup - x;
			double alphaA = x - (demie + xInf );
			double res = (alphaB * biomassA) + (alphaA * biomassB);
			return (res);
		}
	}

/**	Retrieve external events
	 */
	public void somethingHappened (ListenedTo l, Object param) {
		if (l instanceof FmColorFreeLegend) {
			Integer color = (Integer) param;
			selectedColor = color;
			voxelPanel2D.setColor (selectedColor);
			voxelParticlePanel.setColor (selectedColor);
		}
		if (l instanceof FmVoxel2DPanel) {
			if (param instanceof Vertex3d) {
				Vertex3d cord = (Vertex3d) param;
				iSelected = (int) cord.x;
				jSelected = (int) cord.y;
				kSelected = (int) cord.z;
				voxelParticlePanel.saveValues();
				voxelParticlePanel.changeSelect (iSelected, jSelected, kSelected);
			}
		}
		if (l instanceof FmVoxelParticleCageEditor) {
			addHistory();
		}
		if (l instanceof FmVoxelMatrixDepthSelector) {
			if (param instanceof String) {

				int view = 1;
				if (param.equals(Translator.swap ("FiVoxelMatrixDepthSelector.front"))) view = 1;
				if (param.equals(Translator.swap ("FiVoxelMatrixDepthSelector.left"))) view = 2;
				if (param.equals(Translator.swap ("FiVoxelMatrixDepthSelector.rear"))) view = 3;
				if (param.equals(Translator.swap ("FiVoxelMatrixDepthSelector.right"))) view = 4;
				if (param.equals(Translator.swap ("FiVoxelMatrixDepthSelector.bottom"))) view = 5;
				if (param.equals(Translator.swap ("FiVoxelMatrixDepthSelector.top"))) view = 6;
				voxelPanel2D.setView (view);
				selectedColor = voxelPanel2D.getSelectedVoxelValue();

			}
			else {
				Integer depth = (Integer) param;
				voxelPanel2D.setDepth (depth);
				selectedColor = voxelPanel2D.getSelectedVoxelValue();
			}

		}

	}
	/**	To store history used for UNDO-REDO.
	*/
	protected void addHistory () {


		//retrieve data from particle panel
		newBiomass = voxelParticlePanel.getNewBiomass ();

		double[][][][][] ancValues = new double [nbParticleMax][iNbVoxels][jNbVoxels][kNbVoxels][2];

		for (int part=0; part <nbParticleMax; part++) {
			for (int i=0; i<iNbVoxels ; i++) {
				for (int j=0; j<jNbVoxels ; j++) {
					for (int k=0; k<kNbVoxels ; k++) {
						ancValues[part][i][j][k][0] =  newBiomass[part][i][j][k][0];
						ancValues[part][i][j][k][1] =  newBiomass[part][i][j][k][1];
					}
				}
			}
		}

		selectionHistory.add (ancValues);

		firstHistory = true;
	}
	/**	Called on ctrl-Z. UNDO
	*/
	@Override
	protected void ctrlZPressed () {

		double[][][][][] ancValues;
		if (firstHistory) ancValues = (double[][][][][]) selectionHistory.current ();
		else ancValues = (double[][][][][]) selectionHistory.back ();

		if (ancValues != null) {

			newBiomass = ancValues;
			voxelParticlePanel.setBiomass (newBiomass);
			voxelParticlePanel.changeSelect (iSelected, jSelected, kSelected);
			firstHistory = false;
		}
	}
	/**	Called on ctrl-Y. REDO
	*/
	@Override
	protected void ctrlYPressed () {

		double[][][][][] ancValues;
		ancValues = (double[][][][][]) selectionHistory.next ();
		if (ancValues != null) {
			newBiomass = ancValues;
			voxelParticlePanel.setBiomass (newBiomass);
			voxelParticlePanel.changeSelect (iSelected, jSelected, kSelected);
		}
	}
}
