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
 * FiSample3DCageEditor : sample crown 3D design for CAGE METHOD (vs cube method)
 *
 * @author I. Lecomte - October 2009
 */
public class FmSample3DCageEditor extends AmapDialog implements ActionListener, jeeb.lib.util.Listener {

	protected FmModel model;
	protected FmDBCommunicator bdCommunicator;		//to read database
	protected FmDBUpdator bdUpdator;					//to update database

	//sample information
	protected FmDBShape sample;
	protected FmDBPlant plant;
	protected long plantId, sampleId;							//id in the database
	protected HashMap<Long, FmDBVoxel> voxelMap;	//map of sample voxels
	protected int sampleType;

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
	protected int selectedColor = 9;

	//Voxel panel 2D
	protected FmVoxel2DPanel voxelPanel2D;

	//Particle panel
	protected FmVoxelParticleCageEditor voxelParticlePanel;
	protected String [] particleName;
	protected long [][][][][] particleId;
	protected long [][][][][] biomassId;
	protected double [][][][][] newBiomass;
	protected double [][][][][] oldBiomass;
	protected int nbParticle;
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
	public FmSample3DCageEditor (FmModel _model,  FmDBShape _sample) {

		model = _model;
		sample = _sample;

		//connecting database
		bdCommunicator = model.getBDCommunicator ();
		bdUpdator = model.getBDUpdator ();

		//history for undo/redo
		selectionHistory = new History ();


		//Load sample info and voxels list from database
		setGridSize ();

		if (sample != null) {
			sampleId = sample.getShapeId();
			sampleType = sample.getFuelType();
			plant = sample.getPlant();
			if (plant != null) {
				plantId = plant.getPlantId();
			}
			if (sampleId > 0) loadVoxels ();
		}

		//The selected cell is (0,0,0)
		iSelected = 0;
		jSelected = 0;
		kSelected = kNbVoxels - 1;

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
	protected boolean controlCellValues ()   {


		if (nbParticle <= 0) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiSample3DCageEditor.fillOneParticle"),
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
							JOptionPane.showMessageDialog (this, Translator.swap ("FiSample3DCageEditor.fillOneBiomasse"),
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
			JOptionPane.showMessageDialog (this, Translator.swap ("FiSample3DCageEditor.fillOneVoxel"),
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


		if ( controlCellValues()) {

			try {

				if (plantId < 0) {
					plantId = bdUpdator.createPlant (plant, 1);	// create in the database
					if (plantId > 0) {
						plant.setPlantId (plantId);
					}
				}

				//SAMPLE CREATION
				if (sampleId < 0) {

					sampleId = bdUpdator.createShape (sample);				// create in the database

					if (sampleId > 0) {

						sample.setShapeId (sampleId);

						//voxel creation
						for (int i=0; i<iNbVoxels ; i++) {
							for (int j=0; j<jNbVoxels ; j++) {
								for (int k=0; k<kNbVoxels ; k++) {

									long voxelId = -1;
									int ci = i;
									int cj = j;
									int ck = k;
									boolean edge = false;


									//voxel creation
									FmDBVoxel newVoxel = null;
									voxelId = -1;

									for (int part =0; part<nbParticle ; part++) {

										String partName  = particleName [part];

										//convert grammes to kilos
										double newAlive = newBiomass [part][i][j][k][0];
										double newDead  = newBiomass [part][i][j][k][1] ;
										if (newAlive > 0)  newAlive = newAlive / 1000.0;
										if (newDead > 0)  newDead = newDead / 1000.0;


										if (newVoxel == null) {

											newVoxel = new FmDBVoxel (voxelId, ci, cj, ck, null, edge);

											if (newAlive != -9) {

												//voxel and first particle creation
												voxelId = bdUpdator.createCell (sampleId, newVoxel,	partName,
																				newAlive, "Alive");

												//Adding dead particle in the same voxel
												if (newDead != -9) {
													long particleId = bdUpdator.addCellParticle (sampleId, voxelId,	partName,
																								newDead, "Dead");
												}
											}
											else if (newDead != -9)  {
												//voxel and first particle creation
												voxelId = bdUpdator.createCell (sampleId, newVoxel,	partName,
																			newDead, "Dead");
											}

										}
										//Adding particle in the same voxel
										else {
											if (newAlive != -9) {
												long particleId = bdUpdator.addCellParticle (sampleId, voxelId,partName,
																							newAlive, "Alive");
											}
											if (newDead != -9)  {
												long particleId = bdUpdator.addCellParticle (sampleId, voxelId,partName,
																							newDead, "Dead");
											}
										}
									}
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
										//add particle
										else if (newAlive != -9d)  {
											long partID = bdUpdator.addCellParticle (sampleId, voxelId, partName,
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
										else if (newDead != -9d)  {
											long partID = bdUpdator.addCellParticle (sampleId, voxelId, partName,
																					newDead, "Dead");
										}
									}

								}
							}
						}
					}

				}

			} catch (Exception e) {
				Log.println (Log.ERROR, "FiSample3DCageEditor", "error while CREATING sample in data base", e);
			}

			setValidDialog (true);

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

		FmShapeInfoPanel shapeInfoPanel = new FmShapeInfoPanel (sample);
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
											colorMap, false, sampleType, "");
		voxelPanel2D.addListener (this);
		voxelPanel2D.changeSelect (iSelected, jSelected, kSelected);
		voxelPanel2D.setColor (selectedColor);

		splitPanel = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, selector, voxelPanel2D);
		splitPanel.setResizeWeight(0.5);


		/*********** VOXEL particle panel  **************/
		voxelParticlePanel = new FmVoxelParticleCageEditor (model, sample, iNbVoxels, jNbVoxels, kNbVoxels, null, null);
		voxelParticlePanel.addListener (this);
		voxelParticlePanel.changeSelect (iSelected, jSelected, kSelected);
		legend.add (voxelParticlePanel);


		/*********** Control panel **************/
		JPanel controlPanel = new JPanel ();
		controlPanel.setLayout (new FlowLayout (FlowLayout.LEFT));

		addParticle = new JButton (Translator.swap ("FiSample3DCageEditor.addParticle"));
		controlPanel.add (addParticle);
		save = new JButton (Translator.swap ("FiSample3DCageEditor.validate"));
		controlPanel.add (save);
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		cancel.addActionListener (this);
		controlPanel.add (cancel);
		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		controlPanel.add (help);

		if ((plant.isValidated()) || (sample.isDeleted())) {
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

		setTitle (Translator.swap ("FiSample3DCageEditor.title"));

		this.setSize (new Dimension(800,600));

		setModal (true);
	}




	/**	Load shape voxels from database
	 */
	protected void loadVoxels () {


		try {

			sample = bdCommunicator.getShapeVoxels (sample, false);	//to get all shape info
			voxelMap = sample.getVoxels();

			//if there is voxels
			if (voxelMap != null) {

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

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiSample3DCageEditor.loadVoxels() ", "error while opening FUEL data base", e);
		}
	}
	/**
	* grid dimension for plant in 3D
	*/
	protected void setGridSize () {

		//CAGE size is 2m * 1m * 1m

		//for sample, column dimension is sample height
		iSizeVoxels = (int) (sample.getVoxelXSize() * 100);	//convert m to cm
		jSizeVoxels = (int) (sample.getVoxelYSize() * 100);
		kSizeVoxels = (int) (sample.getVoxelZSize() * 100);

		iNbVoxels = (int) ((sample.getXMax () * 100) / iSizeVoxels);
		jNbVoxels = (int) ((sample.getYMax () * 100) / jSizeVoxels);
		kNbVoxels = (int) ((sample.getZMax () * 100) / kSizeVoxels);


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

	}
	/** Return the new shape id
	 */
	public long getShapeId () {
		return sampleId;
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
