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
import fireparadox.model.database.FmDBPlant;
import fireparadox.model.database.FmDBShape;
import fireparadox.model.database.FmDBUpdator;
import fireparadox.model.database.FmDBVoxel;

/**
 * FiLayer2DCageEditor : layer crown 2D design for CAGE METHOD (vs cube method)
 *
 * @author I. Lecomte - January 2010
 */
public class FmLayer2DCageEditor extends AmapDialog implements ActionListener, jeeb.lib.util.Listener {

	private FmModel model;
	private FmDBCommunicator bdCommunicator;		//to read database
	private FmDBUpdator bdUpdator;					//to update database

	//shape information
	private FmDBShape shape;
	private FmDBPlant plant;
	private long plantId, shapeId;							//id in the database
	private int shapeType;

	//Grid core values
	private HashMap<Long, FmDBVoxel> voxelMap;		//map of core  voxels
	private int [][][] gridVoxelValues;				//voxels after  update
	private FmDBVoxel [][][] gridVoxelRef;			//voxel references
	private int iNbVoxels, jNbVoxels, kNbVoxels;		//grid dimensions in number of voxels

	//Grid edge values
	private HashMap<Long, FmDBVoxel> edgeMap;			//map of edge voxels
	private int [][][] gridEdgeValues;				//voxels after  update
	private FmDBVoxel [][][] gridEdgeRef;				//voxel references
	private int iNbEdges, jNbEdges, kNbEdges;			//grid dimensions in number of voxels

	//Voxels size
	private int iSizeVoxels, jSizeVoxels, kSizeVoxels;


	//Selections
	private int iSelected,jSelected,kSelected;		//voxel coordinate
	private boolean isEdgeFocus = false;

	//Color control
	private Map<Integer,Color> colorMap;
	private FmColorFreeLegend colorLegendPanel;
	private int selectedColor = 9;

	//Voxel panel 2D
	private JSplitPane splitPanel;
	private FmVoxel2DPanel voxelPanel2D;
	private FmVoxel2DPanel edgePanel2D;
	private int nbPlusLeft, nbPlusRight, nbPlusTop, nbPlusBottom;
	private int nbPlusEdgeLeft, nbPlusEdgeRight, nbPlusEdgeTop, nbPlusEdgeBottom;


	//Particle panel
	private FmVoxelParticleLayerCageEditor voxelParticlePanel;
	private String [] particleName;
	private long   [][][][][] particleId;
	private long   [][][][][] biomassId;
	private double [][][][][] newBiomass;
	private double [][][][][] oldBiomass;
	private long   [][][][][] edgeParticleId;
	private long   [][][][][] edgeBiomassId;
	private double [][][][][] edgeNewBiomass;
	private double [][][][][] edgeOldBiomass;
	private int nbParticle;
	private int nbParticleMax = 30;


	//Validation control
	private JButton addParticle;
	private JButton save;
	private JButton cancel;
	private JButton help;

	//for undo/redo
	private JButton undoButton;
	private JButton redoButton;
	private History selectionHistory;
	private boolean firstHistory = false;


	/**	Constructor for 3D.
	 */
	public FmLayer2DCageEditor (FmModel _model,  FmDBShape _shape) {

		model = _model;
		shape = _shape;

		//connecting database
		bdCommunicator = model.getBDCommunicator ();
		bdUpdator = model.getBDUpdator ();

		//history for undo/redo
		selectionHistory = new History ();


		//Load shape info and voxels list from database
		setGridSize ();

		if (shape != null) {
			shapeId = shape.getShapeId();
			shapeType = shape.getFuelType();
			plant = shape.getPlant();
			if (plant != null) {
				plantId = plant.getPlantId();
			}
			if (shapeId > 0) loadVoxels ();
		}


		//selected voxel
		iSelected = 0;
		jSelected = 0;
		if (kNbVoxels > 0) {
			kSelected = kNbVoxels - 1;
		}
		else {
			kSelected = kNbEdges - 1;
		}

		createUI ();
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
	private boolean controlCellValues ()   {


		if (nbParticle <= 0) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiLayer2DCageEditor.fillOneParticle"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}

		int nbColoredVoxel = 0;

		for (int i=0; i<iNbVoxels ; i++) {
			for (int j=0; j<jNbVoxels ; j++) {
				for (int k=0; k<kNbVoxels ; k++) {

					//the voxel has to be filled
					if (gridVoxelValues[i][j][k] != 0) {

						//Each voxel should have a biomass value
						int nbBiomass = 0;
						for (int part =0; part<nbParticle ; part++) {
							if ((newBiomass [part][i][j][k][0] != -9) || (newBiomass [part][i][j][k][1]  != -9)
							|| (newBiomass [part][i][j][k][0] != Double.NaN) || (newBiomass [part][i][j][k][1]  != Double.NaN)) {
								nbBiomass++;
							}
						}
						if (nbBiomass <= 0) {
							JOptionPane.showMessageDialog (this, Translator.swap ("FiLayer2DCageEditor.fillOneBiomasse"),
							Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
							return false;
						}

						nbColoredVoxel++;
					}
				}
			}
		}
		//ERROR if No voxel
		if (nbColoredVoxel == 0) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiLayer2DCageEditor.fillOneVoxel"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}


		return true;
	}
	/**	Validation
	 */
	private void validateAction () {

		voxelParticlePanel.saveValues();

		//particles
		nbParticle = voxelParticlePanel.getNbParticle ();
		particleName = voxelParticlePanel.getParticleName ();

		//core
		if (kNbVoxels > 0) {
			gridVoxelValues = voxelPanel2D.getNewVoxelValues ();
			particleId = voxelParticlePanel.getParticleId ();
			biomassId = voxelParticlePanel.getBiomassId ();
			newBiomass = voxelParticlePanel.getNewBiomass ();
			oldBiomass = voxelParticlePanel.getOldBiomass ();
		}

		//Edge
		if (kNbEdges > 0) {
			gridEdgeValues = edgePanel2D.getNewVoxelValues ();
			edgeParticleId = voxelParticlePanel.getEdgeParticleId ();
			edgeBiomassId = voxelParticlePanel.getEdgeBiomassId ();
			edgeNewBiomass = voxelParticlePanel.getNewEdgeBiomass ();
			edgeOldBiomass = voxelParticlePanel.getOldEdgeBiomass ();
		}

		if ( controlCellValues()) {

			try {

				if (plantId < 0) {
					plantId = bdUpdator.createPlant (plant, 1);	// create in the database
					if (plantId > 0) {
						plant.setPlantId (plantId);
					}
				}

				//SAMPLE CREATION
				if (shapeId < 0) {

					shapeId = bdUpdator.createShape (shape);				// create in the database

					if (shapeId > 0) {

						shape.setShapeId (shapeId);

						//CORE voxel creation
						for (int i=0; i<iNbVoxels ; i++) {
							for (int j=0; j<jNbVoxels ; j++) {
								for (int k=0; k<kNbVoxels ; k++) {

									voxelCreation (i, j, k, false);
								}
							}
						}
						//EDGE VOXEL CREATION
						for (int i=0; i<iNbEdges ; i++) {
							for (int j=0; j<jNbEdges  ; j++) {
								for (int k=0; k<kNbEdges  ; k++) {

									voxelCreation (i, j, k, true);
								}
							}
						}

					}
				}

				//BIOMASSE UPDATE
				else {

					//CORE VOXEL UPDATE
					for (int i=0; i<iNbVoxels ; i++) {
						for (int j=0; j<jNbVoxels ; j++) {
							for (int k=0; k<kNbVoxels ; k++) {


								FmDBVoxel voxel = gridVoxelRef[i][j][k];
								if (voxel != null) voxelUpdate (i, j, k, false);
								else voxelCreation (i, j, k, false);

							}
						}
					}
					//EDGE VOXEL UPDATE
					for (int i=0; i<iNbEdges ; i++) {
						for (int j=0; j<jNbEdges  ; j++) {
							for (int k=0; k<kNbEdges  ; k++) {

								FmDBVoxel voxel = gridVoxelRef[i][j][k];
								if (voxel != null) voxelUpdate (i, j, k, true);
								else voxelCreation (i, j, k, true);
							}
						}
					}

				}

			} catch (Exception e) {
				Log.println (Log.ERROR, "FiLayer2DCageEditor", "error while CREATING shape in data base", e);
			}

			System.out.println("CREATION FORME TERMINEE !");

			setValidDialog (true);

		}
	}
	/**	Voxel creation in the shape
	 */
	private void voxelCreation (int _i, int _j, int _k, boolean _edge) {

		try {

			int i = _i;
			int j = _j;
			int k = _k;
			boolean edge = _edge;

			boolean creation = false;

			//prepare particle name list with biomasses
			double [] newAlive = new double [nbParticleMax];
			double [] newDead = new double [nbParticleMax];

			for (int part=0; part<nbParticle ; part++) {

				if (!edge) {
					newAlive [part]= newBiomass [part][i][j][k][0];
					newDead [part] = newBiomass [part][i][j][k][1];
				}
				else {
					newAlive [part]= edgeNewBiomass [part][i][j][k][0];
					newDead [part] = edgeNewBiomass [part][i][j][k][1];
				}


				if ((newAlive [part] >= 0) || (newDead [part] >= 0)) creation = true;
				if (newAlive [part] > 0)  newAlive [part] = newAlive [part] / 1000.0; //convert grammes to kilos
				if (newDead [part] > 0)  newDead [part] = newDead [part] / 1000.0; //convert grammes to kilos
			}

			//voxel creation
			if (creation) {
				long voxelId = -1;

				FmDBVoxel newVoxel = new FmDBVoxel (voxelId, i, j, k, null, edge);
				voxelId = bdUpdator.createCellComplexe (shapeId, newVoxel, particleName, newAlive, newDead);
				System.out.println("CELL CREATION ID="+voxelId);
			}


		} catch (Exception e) {
				Log.println (Log.ERROR, "FiLayer2DCageEditor", "error while CREATING voxels in data base", e);
		}
	}

	/**	Voxel update in the shape
	*/
	private void voxelUpdate (int _i, int _j, int _k, boolean _edge) {

		try {

			int i = _i;
			int j = _j;
			int k = _k;
			boolean edge = _edge;

			FmDBVoxel voxel = gridVoxelRef[i][j][k];
			long voxelId = voxel.getDBId();

				for (int part =0; part<nbParticle ; part++) {

					String partName  = particleName [part];

					//load biomasses from grid
					double newAlive = 0;
					double newDead = 0;
					double oldAlive = 0;
					double oldDead = 0;
					long aliveBiomassID = -1;
					long deadBiomassID = -1;
					long aliveParticleID = -1;
					long deadParticleID = -1;

					if (!edge) {
						newAlive = newBiomass [part][i][j][k][0];
						newDead  = newBiomass [part][i][j][k][1];
						oldAlive = oldBiomass [part][i][j][k][0];
						oldDead  = oldBiomass [part][i][j][k][1];
						aliveBiomassID  = biomassId [part][i][j][k][0];
						deadBiomassID  = biomassId [part][i][j][k][1];
						aliveParticleID  = particleId [part][i][j][k][0];
						deadParticleID  = particleId [part][i][j][k][1];
					}
					else {
						newAlive = edgeNewBiomass [part][i][j][k][0];
						newDead  = edgeNewBiomass [part][i][j][k][1];
						oldAlive = edgeOldBiomass [part][i][j][k][0];
						oldDead  = edgeOldBiomass [part][i][j][k][1];
						aliveBiomassID  = edgeBiomassId [part][i][j][k][0];
						deadBiomassID  = edgeBiomassId [part][i][j][k][1];
						aliveParticleID  = edgeParticleId [part][i][j][k][0];
						deadParticleID  = edgeParticleId [part][i][j][k][1];
					}

					//ALIVE
					if (newAlive != oldAlive) {

						//convert grammes to kilos
						if (newAlive > 0)  newAlive = newAlive / 1000.0;


						if (aliveBiomassID > 0) {
							//delete particle
							if (newAlive == -9) {
								bdUpdator.deleteCellParticle (voxelId, aliveParticleID);
							}
							//update particle
							else {
								bdUpdator.updateParameter (aliveBiomassID, newAlive);

							}
						}
						//add particle
						else if (newAlive != -9d)  {
							long partID = bdUpdator.addCellParticle (shapeId, voxelId, partName,
																newAlive , "Alive");
						}

					}
					if (newDead != oldDead) {

						//convert grammes to kilos
						if (newDead > 0)  newDead = newDead / 1000.0;

						if (deadBiomassID > 0) {
							//delete particle
							if (newDead == -9) {
								bdUpdator.deleteCellParticle (voxelId, deadParticleID);
							}
							//update particle
							else {
								bdUpdator.updateParameter (deadBiomassID, newDead);
							}
						}
						//Adding DEAD particle in the  voxel
						else if (newDead != -9d)  {
							long partID = bdUpdator.addCellParticle (shapeId, voxelId, partName,
																	newDead, "Dead");
						}
					}

				}


		} catch (Exception e) {
			Log.println (Log.ERROR, "FiLayer2DCageEditor", "error while UPDATING voxels in data base", e);
		}
	}


	/**	Initialize the GUI.
	 */
	private void createUI () {

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


		/*********** CORE 2D panel  **************/
		if (kNbVoxels > 0) {
			voxelPanel2D = new FmVoxel2DPanel (gridVoxelValues, iNbVoxels, jNbVoxels, kNbVoxels,
												iSizeVoxels, jSizeVoxels, kSizeVoxels,
												colorMap, true, shapeType, "");
			voxelPanel2D.addListener (this);
			voxelPanel2D.changeSelect (iSelected, jSelected, kSelected);
			voxelPanel2D.setColor (selectedColor);
		}
		else isEdgeFocus = true;


		/*********** EDGE 2D panel  **************/
		if (kNbEdges > 0) {
			edgePanel2D = new FmVoxel2DPanel (gridEdgeValues, iNbEdges, jNbEdges, kNbEdges,
												iSizeVoxels, jSizeVoxels, kSizeVoxels,
												colorMap, true, shapeType, "");
			edgePanel2D.addListener (this);
			edgePanel2D.changeSelect (iSelected, jSelected, kSelected);
			edgePanel2D.setColor (selectedColor);
		}

		splitPanel = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, voxelPanel2D, edgePanel2D);
		splitPanel.setResizeWeight(0.5);


		/*********** VOXEL particle panel  **************/


		voxelParticlePanel = new FmVoxelParticleLayerCageEditor (model, shape, iNbVoxels, jNbVoxels, kNbVoxels,
																	iNbEdges, jNbEdges, kNbEdges);
		voxelParticlePanel.addListener (this);
		voxelParticlePanel.changeSelect (iSelected, jSelected, kSelected, isEdgeFocus);
		legend.add (voxelParticlePanel);


		/*********** Control panel **************/
		JPanel controlPanel = new JPanel ();
		controlPanel.setLayout (new FlowLayout (FlowLayout.LEFT));

		addParticle = new JButton (Translator.swap ("FiLayer2DCageEditor.addParticle"));
		controlPanel.add (addParticle);
		save = new JButton (Translator.swap ("FiLayer2DCageEditor.validate"));
		controlPanel.add (save);
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		cancel.addActionListener (this);
		controlPanel.add (cancel);
		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		controlPanel.add (help);

		if ((plant.isValidated()) || (shape.isDeleted())) {
			save.setEnabled(false);
			addParticle.setEnabled(false);
		}
		else {
			save.addActionListener (this);
			addParticle.addActionListener (this);
		}

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (splitPanel, BorderLayout.CENTER);
		getContentPane ().add (legend, BorderLayout.EAST);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		setTitle (Translator.swap ("FiLayer2DCageEditor.title"));

		this.setSize (new Dimension(800,600));

		setModal (true);
	}




	/**	Load shape voxels from database
	 */
	private void loadVoxels () {


		try {

			shape = bdCommunicator.getShapeVoxels (shape, false);	//to get all shape info
			voxelMap = shape.getVoxels();

			//if there is voxels
			if ((voxelMap != null) && (voxelMap.size() > 0)) {

				for (Iterator iter = voxelMap.keySet().iterator(); iter.hasNext ();) {

					Object cle = iter.next();
					FmDBVoxel voxel = voxelMap.get(cle);
					int i = voxel.getI();
					int j = voxel.getJ();
					int k = voxel.getK();

					if ((i<iNbVoxels) && (j<jNbVoxels) && (k<kNbVoxels)) {
						gridVoxelRef[i][j][k] = voxel;
						gridVoxelValues[i][j][k] = 9;
					}
				}

			}

			edgeMap = shape.getEdgeVoxels(); 	//to get edge voxel map

			if ((edgeMap != null) && (edgeMap.size() > 0)) {
				for (Iterator iter = edgeMap.keySet().iterator(); iter.hasNext ();) {

					Object cle = iter.next();
					FmDBVoxel voxel = edgeMap.get(cle);
					int i = voxel.getI();
					int j = voxel.getJ();
					int k = voxel.getK();

					if ((i < iNbEdges) && (j < jNbEdges) && (k < kNbEdges) ) {
						gridEdgeRef[i][j][k] = voxel;
						gridEdgeValues[i][j][k] = 9;
					}


					if ((i<iNbVoxels) && (j<jNbVoxels) && (k<kNbVoxels)) {
						gridVoxelRef[i][j][k] = voxel;
						gridVoxelValues[i][j][k] = 9;
					}
				}
			}


		} catch (Exception e) {
			Log.println (Log.ERROR, "FiLayer2DCageEditor.loadVoxels() ", "error while opening FUEL data base", e);
		}
	}
	/**
	* grid dimension for plant in 3D
	*/
	private void setGridSize () {

		//VOXELS
		iSizeVoxels = (int) (shape.getVoxelXSize() * 100);	//convert m to cm
		jSizeVoxels = (int) (shape.getVoxelYSize() * 100);
		kSizeVoxels = (int) (shape.getVoxelZSize() * 100);

		//CORE
		kNbVoxels = (int) ((shape.getZMax () * 100) / kSizeVoxels);
		iNbVoxels = (int) ((shape.getXMax () * 100) / iSizeVoxels);
		jNbVoxels = 1;

		gridVoxelValues = new int[iNbVoxels][jNbVoxels][kNbVoxels];
		gridVoxelRef    = new FmDBVoxel [iNbVoxels][jNbVoxels][kNbVoxels];

		//voxel initialisation
		for (int i=0; i<iNbVoxels ; i++) {
			for (int j=0; j<jNbVoxels ; j++) {
				for (int k=0; k<kNbVoxels ; k++) {
					gridVoxelRef[i][j][k] = null;
					gridVoxelValues[i][j][k] = 9;
				}
			}
		}

		//EDGE
		kNbEdges = (int) ((shape.getZEdgeMax () * 100) / kSizeVoxels);;
		iNbEdges = (int) ((shape.getXEdgeMax () * 100) / iSizeVoxels);
		jNbEdges = 1;

		gridEdgeValues = new int[iNbEdges][jNbEdges][kNbEdges];
		gridEdgeRef    = new FmDBVoxel [iNbEdges][jNbEdges][kNbEdges];

		//voxel initialisation
		for (int i=0; i<iNbEdges ; i++) {
			for (int j=0; j<jNbEdges ; j++) {
				for (int k=0; k<kNbEdges ; k++) {
					gridEdgeRef[i][j][k] = null;
					gridEdgeValues[i][j][k] = 9;
				}
			}
		}
	}
	/** Return the new shape id
	 */
	public long getShapeId () {
		return shapeId;
	}

	/**	Retrieve external events
	 */
	public void somethingHappened (ListenedTo l, Object param) {
		if (l instanceof FmColorFreeLegend) {
			Integer color = (Integer) param;
			selectedColor = color;
			voxelPanel2D.setColor (selectedColor);
		}
		if (l instanceof FmVoxel2DPanel) {

			if (l==voxelPanel2D) {
				isEdgeFocus = false;
			}
			else {
				isEdgeFocus = true;
			}

			if (param instanceof Vertex3d) {
				Vertex3d cord = (Vertex3d) param;

				iSelected = (int) cord.x;
				jSelected = (int) cord.y;
				kSelected = (int) cord.z;

				voxelParticlePanel.saveValues();
				voxelParticlePanel.changeSelect (iSelected, jSelected, kSelected, isEdgeFocus);

			}
			else if (String.class.isInstance (param)) {

				String action = (String) param;
				if (l==voxelPanel2D) {
					addHistory (false);
					changeGridSize (action);
				}
				else {
					addHistory (true);
					changeEdgeGridSize (action);
				}

				voxelPanel2D.setGridVoxelValues (gridVoxelValues, iNbVoxels, jNbVoxels, kNbVoxels);
				edgePanel2D.setGridVoxelValues (gridEdgeValues, iNbEdges, jNbEdges, kNbEdges);
			}
		}
		if (l instanceof FmVoxelParticleCageEditor) {
			if (isEdgeFocus) addHistory(true);
			else addHistory(false);
		}


	}

	/**
	* changing grid dimension
	*/
	protected void changeGridSize (String action) {


		String side = action.substring (0,1);
		String oper = action.substring (1);

		boolean left = false;
		boolean right = false;
		boolean top = false;
		boolean bottom = false;

		if (side.equals("L")) left = true;
		if (side.equals("R")) right = true;
		if (side.equals("T")) top = true;
		if (side.equals("B")) bottom = true;

		boolean plus = false;
		if (oper.equals("+")) plus = true;


		if (left) {
			if (plus) nbPlusLeft++;
			else nbPlusLeft--;
		} else if (right) {
			if (plus) nbPlusRight++;
			else nbPlusRight--;
		} else if (top) {
			if (plus) nbPlusTop++;
			else nbPlusTop--;
		} else if (bottom) {
			if (plus) nbPlusBottom++;
			else nbPlusBottom--;
		}

		//SAVE GRID values
		int[][][] savGridVoxelValues = new int[iNbVoxels][jNbVoxels][kNbVoxels];
		FmDBVoxel[][][] savGridVoxelRef    = new FmDBVoxel [iNbVoxels][jNbVoxels][kNbVoxels];


		for (int i=0;i<iNbVoxels;i++) {
			for (int j=0;j<jNbVoxels;j++) {
				for (int k=0;k<kNbVoxels;k++) {
					savGridVoxelValues[i][j][k] = gridVoxelValues[i][j][k];
					savGridVoxelRef[i][j][k] = gridVoxelRef[i][j][k];
				}
			}
		}


		//+/- left or right
		if ((left) || (right)) {

			if ((nbPlusLeft >=0) && (nbPlusRight >= 0) ) {


				//NEW GRID CREATION
				int newi;
				if (plus)  newi = iNbVoxels + 1;
				else newi = iNbVoxels - 1;

				gridVoxelValues = new int[newi][jNbVoxels][kNbVoxels];
				gridVoxelRef    = new FmDBVoxel [newi][jNbVoxels][kNbVoxels];

				//initialisation
				for (int i=0;i<newi;i++) {
					for (int j=0;j<jNbVoxels;j++) {
						for (int k=0;k<kNbVoxels;k++) {
							gridVoxelRef[i][j][k] = null;
						}
					}
				}

				//recopy +plus
				if (plus) {
					for (int i=0;i<iNbVoxels;i++) {
						for (int j=0;j<jNbVoxels;j++) {
							for (int k=0;k<kNbVoxels;k++) {
								int copyI = i;
								if (left) {
									 copyI++;
								}


								gridVoxelValues[copyI][j][k] = savGridVoxelValues[i][j][k];
								gridVoxelRef[i][j][k] = savGridVoxelRef[i][j][k];
							}
						}
					}
				}
				//recopy -moins
				else {
					for (int i=0;i<newi;i++) {
						for (int j=0;j<jNbVoxels;j++) {
							for (int k=0;k<kNbVoxels;k++) {

								int copyI = i;
								if (left) copyI++;

								gridVoxelValues[i][j][k] = savGridVoxelValues[copyI][j][k];
								gridVoxelRef[i][j][k] = savGridVoxelRef[i][j][k];
							}
						}
					}

				}

				iNbVoxels = newi;

			}
		}
		//+/- top or bottom
		else  if ((top) || (bottom)) {

			if ((nbPlusTop >= 0) && (nbPlusBottom >= 0) ) {

				//NEW GRID CREATION
				int newk;
				if (plus)  newk = kNbVoxels + 1;
				else newk = kNbVoxels - 1;

				gridVoxelValues = new int[iNbVoxels][jNbVoxels][newk];
				gridVoxelRef    = new FmDBVoxel [iNbVoxels][jNbVoxels][newk];

				//initialisation
				for (int i=0;i<iNbVoxels;i++) {
					for (int j=0;j<jNbVoxels;j++) {
						for (int k=0;k<newk;k++) {
							gridVoxelRef[i][j][k] = null;
						}
					}
				}

				//recopy +plus
				if (plus) {
					for (int i=0;i<iNbVoxels;i++) {
						for (int j=0;j<jNbVoxels;j++) {
							for (int k=0;k<kNbVoxels;k++) {
								int copyK = k;
								if (bottom) {
									 copyK++;
								}


								gridVoxelValues[i][j][copyK] = savGridVoxelValues[i][j][k];
								gridVoxelRef[i][j][k] = savGridVoxelRef[i][j][k];
							}
						}
					}
				}
				//recopy -moins
				else {
					for (int i=0;i<iNbVoxels;i++) {
						for (int j=0;j<jNbVoxels;j++) {
							for (int k=0;k<newk;k++) {

								int copyK = k;
								if (bottom) copyK++;

								gridVoxelValues[i][j][k] = savGridVoxelValues[i][j][copyK];
								gridVoxelRef[i][j][k] = savGridVoxelRef[i][j][k];
							}
						}
					}

				}

				kNbVoxels = newk;

			}

		}
		if (nbPlusLeft < 0)   nbPlusLeft=0;
		if (nbPlusRight < 0)  nbPlusRight=0;
		if (nbPlusTop < 0) 	  nbPlusTop=0;
		if (nbPlusBottom < 0) nbPlusBottom=0;


	}

	/**
	* changing grid dimension for plant in 2D
	*/
	protected void changeEdgeGridSize (String action) {


		String side = action.substring (0,1);
		String oper = action.substring (1);

		boolean left = false;
		boolean right = false;
		boolean top = false;
		boolean bottom = false;

		if (side.equals("L")) left = true;
		if (side.equals("R")) right = true;
		if (side.equals("T")) top = true;
		if (side.equals("B")) bottom = true;

		boolean plus = false;
		if (oper.equals("+")) plus = true;


		if (left) {
			if (plus) nbPlusEdgeLeft++;
			else nbPlusEdgeLeft--;
		} else if (right) {
			if (plus) nbPlusEdgeRight++;
			else nbPlusEdgeRight--;
		} else if (top) {
			if (plus) nbPlusEdgeTop++;
			else nbPlusEdgeTop--;
		} else if (bottom) {
			if (plus) nbPlusEdgeBottom++;
			else nbPlusEdgeBottom--;
		}

		//SAVING actual GRIDS
		int[][][] savGridEdgeValues = new int[iNbEdges][jNbEdges][kNbEdges];
		FmDBVoxel[][][] savEdgeVoxelRef    = new FmDBVoxel [iNbEdges][jNbEdges][kNbEdges];


		for (int i=0;i<iNbEdges;i++) {
			for (int j=0;j<jNbEdges;j++) {
				for (int k=0;k<kNbEdges;k++) {
					savGridEdgeValues[i][j][k] = gridEdgeValues[i][j][k];
					savEdgeVoxelRef[i][j][k] = gridEdgeRef[i][j][k];
				}
			}
		}


		//+/- left or right
		if ((left) || (right)) {

			if ((nbPlusEdgeLeft >=0) && (nbPlusEdgeRight >= 0) ) {

				//NEW GRID CREATION
				int newi;
				if (plus)  newi = iNbEdges + 1;
				else newi = iNbEdges - 1;

				gridEdgeValues = new int[newi][jNbEdges][kNbEdges];
				gridEdgeRef    = new FmDBVoxel [newi][jNbEdges][kNbEdges];

				//initialisation
				for (int i=0;i<newi;i++) {
					for (int j=0;j<jNbEdges;j++) {
						for (int k=0;k<kNbEdges;k++) {
							gridEdgeRef[i][j][k] = null;
						}
					}
				}

				//recopy +plus
				if (plus) {
					for (int i=0;i<iNbEdges;i++) {
						for (int j=0;j<jNbEdges;j++) {
							for (int k=0;k<kNbEdges;k++) {
								int copyI = i;
								if (left) {
									 copyI++;
								}


								gridEdgeValues[copyI][j][k] = savGridEdgeValues[i][j][k];
								gridEdgeRef[i][j][k] = savEdgeVoxelRef[i][j][k];
							}
						}
					}
				}
				//recopy -moins
				else {
					for (int i=0;i<newi;i++) {
						for (int j=0;j<jNbEdges;j++) {
							for (int k=0;k<kNbEdges;k++) {

								int copyI = i;
								if (left) copyI++;

								gridEdgeValues[i][j][k] = savGridEdgeValues[copyI][j][k];
								gridEdgeRef[i][j][k] = savEdgeVoxelRef[i][j][k];
							}
						}
					}

				}

				iNbEdges = newi;

			}
		}
		//+/- top or bottom
		else  if ((top) || (bottom)) {

			if ((nbPlusEdgeTop >= 0) && (nbPlusEdgeBottom >= 0) ) {



				//NEW GRID CREATION
				int newk;
				if (plus)  newk = kNbEdges + 1;
				else newk = kNbEdges - 1;

				gridEdgeValues = new int[iNbEdges][jNbEdges][newk];
				gridEdgeRef    = new FmDBVoxel [iNbEdges][jNbEdges][newk];

				//initialisation
				for (int i=0;i<iNbEdges;i++) {
					for (int j=0;j<jNbEdges;j++) {
						for (int k=0;k<newk;k++) {
							gridEdgeRef[i][j][k] = null;
						}
					}
				}

				//recopy +plus
				if (plus) {
					for (int i=0;i<iNbEdges;i++) {
						for (int j=0;j<jNbEdges;j++) {
							for (int k=0;k<kNbEdges;k++) {
								int copyK = k;
								if (bottom) {
									 copyK++;
								}


								gridEdgeValues[i][j][copyK] = savGridEdgeValues[i][j][k];
								gridEdgeRef[i][j][k] = savEdgeVoxelRef[i][j][k];
							}
						}
					}
				}
				//recopy -moins
				else {
					for (int i=0;i<iNbEdges;i++) {
						for (int j=0;j<jNbEdges;j++) {
							for (int k=0;k<newk;k++) {

								int copyK = k;
								if (bottom) copyK++;

								gridEdgeValues[i][j][k] = savGridEdgeValues[i][j][copyK];
								gridEdgeRef[i][j][k] = savEdgeVoxelRef[i][j][k];
							}
						}
					}
				}

				kNbEdges = newk;
			}
		}
		if (nbPlusEdgeLeft < 0)   nbPlusEdgeLeft=0;
		if (nbPlusEdgeRight < 0)  nbPlusEdgeRight=0;
		if (nbPlusEdgeTop < 0) 	  nbPlusEdgeTop=0;
		if (nbPlusEdgeBottom < 0) nbPlusEdgeBottom=0;
	}

	/**	To store history used for UNDO-REDO.
	*/
	protected void addHistory (boolean edge) {

		int[][][] ancGridValues;

		if (!edge) {

			ancGridValues = new int[iNbVoxels][jNbVoxels][kNbVoxels];

			for (int i=0; i<iNbVoxels ; i++) {
				for (int j=0; j<jNbVoxels ; j++) {
					for (int k=0; k<kNbVoxels ; k++) {
						ancGridValues[i][j][k] =  gridVoxelValues[i][j][k];
					}
				}
			}
		}
		else {

			ancGridValues = new int[iNbEdges][jNbEdges][kNbEdges];

			for (int i=0; i<iNbEdges ; i++) {
				for (int j=0; j<jNbEdges ; j++) {
					for (int k=0; k<kNbEdges ; k++) {
						ancGridValues[i][j][k] =  gridEdgeValues[i][j][k];
					}
				}
			}
		}

		selectionHistory.add (new HistoryData (edge, ancGridValues));

		firstHistory = true;
	}

	/**	Called on ctrl-Z. UNDO
	*/
	@Override
	protected void ctrlZPressed () {

		HistoryData data = null;

		if (firstHistory) data = (HistoryData) selectionHistory.current ();
		else data = (HistoryData) selectionHistory.back ();

		if (data != null) {
			int[][][] ancGridValues = data.values;
			boolean edge = data.edge;

			if (ancGridValues != null) {
				if (edge) {

					iNbEdges = ancGridValues.length ;
					jNbEdges = ancGridValues[0].length;
					kNbEdges = ancGridValues[0][0].length;

					gridEdgeValues = ancGridValues;
					edgePanel2D.setGridVoxelValues (gridEdgeValues, iNbEdges, jNbEdges, kNbEdges);
				}
				else {

					iNbVoxels = ancGridValues.length ;
					jNbVoxels = ancGridValues[0].length;
					kNbVoxels = ancGridValues[0][0].length;

					gridVoxelValues = ancGridValues;
					voxelPanel2D.setGridVoxelValues (gridVoxelValues, iNbVoxels, jNbVoxels, kNbVoxels);
				}

				firstHistory = false;

			}
		}

	}


	/**	Called on ctrl-Y. REDO
	*/
	@Override
	protected void ctrlYPressed () {

		HistoryData data = (HistoryData) selectionHistory.next ();

		if (data != null) {
			int[][][] ancGridValues = data.values;
			boolean edge = data.edge;

			if (ancGridValues != null) {
				if (edge) {

					iNbEdges = ancGridValues.length ;
					jNbEdges = ancGridValues[0].length;
					kNbEdges = ancGridValues[0][0].length;

					gridEdgeValues = ancGridValues;
					edgePanel2D.setGridVoxelValues (gridEdgeValues, iNbEdges, jNbEdges, kNbEdges);
				}
				else {

					iNbVoxels = ancGridValues.length ;
					jNbVoxels = ancGridValues[0].length;
					kNbVoxels = ancGridValues[0][0].length;

					gridVoxelValues = ancGridValues;
					voxelPanel2D.setGridVoxelValues (gridVoxelValues, iNbVoxels, jNbVoxels, kNbVoxels);
				}

			}
		}

	}

	/**
	 * HistoryData : Structure DATA for history Store
	 */
	 private class HistoryData
		{
		public boolean edge;
		public int[][][] values;

		public HistoryData (boolean _edge, int[][][] _values)
		{
			edge = _edge;
			values = _values;
		}

	}
}
