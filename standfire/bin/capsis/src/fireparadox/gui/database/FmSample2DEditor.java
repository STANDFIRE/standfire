package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import capsis.util.History;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBShape;
import fireparadox.model.database.FmDBVoxel;
import fireparadox.model.database.FmVoxelType;

/**
 * FiSample2DEditor : sample crown 2D design
 *
 * @author I. Lecomte - March 2008
 */
public class FmSample2DEditor extends FmPlant2DEditor implements ActionListener, jeeb.lib.util.Listener {

	//Validation control
	protected JButton addParticle;

	//Particle panel editor
	protected FmVoxelParticleEditor voxelParticlePanel;
	protected double [][][] newBiomass;
	protected double [][][] oldBiomass;
	protected long [][][]   biomassId;
	protected long [][][]   particleId;
	protected String [] particleNames;
	protected int nbParticleMax = 30;
	protected int nbParticle;



	public FmSample2DEditor (FmModel _model,  FmDBShape _shape, int _topSelected, int _centerSelected, int _bottomSelected) {

		super ();

		model = _model;
		shape = _shape;
		plant = shape.getPlant();
		shapeId = shape.getShapeId();
		plantId = plant.getPlantId();
		fuelType = shape.getFuelType();

		//connecting database
		bdCommunicator = model.getBDCommunicator ();
		bdUpdator = model.getBDUpdator ();

		//history for undo/redo
		selectionHistory = new History ();

		//Load fuel info and voxels list from database
		loadVoxels ();

		if ((voxelMap == null) || (voxelMap.size() == 0)) {

			if (kNbVoxels == 1) {
				gridVoxelValues[0][0][0] = FmColorLegend.TOP_VALUE;
				isTop = true;
			}
			else if (kNbVoxels == 2) {
				gridVoxelValues[0][0][1] = FmColorLegend.TOP_VALUE;
				gridVoxelValues[0][0][0] = FmColorLegend.BOTTOM_VALUE;
				isTop = true;
				isBottom = true;
			}
			else if (kNbVoxels == 3) {
				gridVoxelValues[0][0][2] = FmColorLegend.TOP_VALUE;
				gridVoxelValues[0][0][1] = FmColorLegend.CENTER_VALUE;
				gridVoxelValues[0][0][0] = FmColorLegend.BOTTOM_VALUE;
				isTop = true;
				isCenter = true;
				isBottom = true;
			}
			else if (kNbVoxels > 3) {
				if (_topSelected >= 0) 	  gridVoxelValues[0][0][_topSelected] = FmColorLegend.TOP_VALUE;
				if (_centerSelected >= 0) gridVoxelValues[0][0][_centerSelected] = FmColorLegend.CENTER_VALUE;
				if (_bottomSelected >= 0) gridVoxelValues[0][0][_bottomSelected] = FmColorLegend.BOTTOM_VALUE;
				isTop = true;
				isCenter = true;
				isBottom = true;
			}
		}


		iSelected = 0;
		jSelected = 0;
		kSelected = kNbVoxels - 1;
		typeSelected = gridVoxelValues[iSelected][jSelected][kSelected];

		createUI ();

		show ();

	 }

	/**	Actions on the buttons
	 */
	@Override
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (save)) {
			validateAction ();
		} else if (evt.getSource ().equals (addParticle)) {
			voxelParticlePanel.addParticleAction ();
		} else if (evt.getSource ().equals (cancel)) {
			setValidDialog (false);
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**	Validation
	 */
	@Override
	protected void validateAction () {

		//retrieve data from particle panel
		voxelParticlePanel.saveValues();
		newBiomass = voxelParticlePanel.getNewBiomass ();
		oldBiomass = voxelParticlePanel.getOldBiomass ();
		biomassId = voxelParticlePanel.getBiomassId ();
		nbParticle = voxelParticlePanel.getNbParticle ();
		particleNames = voxelParticlePanel.getParticleNames ();
		particleId = voxelParticlePanel.getParticleId ();

		if (controlCellValues ()) {

			try {
				bdUpdator = model.getBDUpdator ();

				//PLANT CREATION IN DATABASE
				if (plantId < 0) {
					plantId = bdUpdator.createPlant (plant, 1);
					if (plantId > 0) {
						plant.setPlantId (plantId);
					}
				}


				//SAMPLE CREATION IN DATABASE
				if (shapeId < 0) {

					shapeId = bdUpdator.createShape (shape);

					if (shapeId > 0) {

						shape.setShapeId (shapeId);


						//SAVE VOXELS TYPE
						for (int i=0; i<iNbVoxels ; i++) {
							for (int j=0; j<jNbVoxels ; j++) {
								for (int k=0; k<kNbVoxels ; k++) {

									//CREATION : the voxel color is not empty
									if (gridVoxelValues[i][j][k] > 0) {

										String typeName = "";
										if (gridVoxelValues[i][j][k] == 1)  typeName="Top_INRA";
										if (gridVoxelValues[i][j][k] == 2)  typeName="Center_INRA";
										if (gridVoxelValues[i][j][k] == 3)  typeName="Bottom_INRA";

										long voxelId = -1;
										int ci = i;
										int cj = j;
										int ck = k;
										boolean edge = false;

										try {

											//voxel type creation (layer in DB)
											long typeId = bdUpdator.createLayer (plant, shapeId, typeName);
											FmVoxelType type = new FmVoxelType (typeId, typeName);
											int indexType = type.getTypeIndex();

											//voxel creation
											FmDBVoxel newVoxel = null;
											voxelId = -1;

											for (int part =0; part<nbParticle ; part++) {
												String particleName  = particleNames[part];

												//convert grammes to kilos
												double newAlive = newBiomass [part][indexType][0];
												double newDead  = newBiomass [part][indexType][1];
												if (newAlive > 0)  newAlive = newAlive / 1000.0;
												if (newDead > 0)  newDead = newDead / 1000.0;

												if (newVoxel == null) {
													newVoxel = new FmDBVoxel (voxelId, 0, 0, ck, type, edge);

													if (newAlive != -9) {
														//voxel and first particle creation
														voxelId = bdUpdator.createCell (shapeId, newVoxel,
																						particleName, newAlive, "Alive");


														//Adding dead particle in the same voxel
														if (newDead != -9) {
															long particleId = bdUpdator.addCellParticle (shapeId, voxelId,
																										particleName, newDead, "Dead");
														}
													}
													else if (newDead != -9)  {
														//voxel and first particle creation
														voxelId = bdUpdator.createCell (shapeId, newVoxel,
																					particleName, newDead, "Dead");
													}
													newVoxel.setDBId (voxelId);
													voxelMap.put(voxelId, newVoxel);

												}
												//Adding particle in the same voxel
												else {
													if (newAlive != -9) {
														long particleId = bdUpdator.addCellParticle (shapeId, voxelId,
																									particleName, newAlive, "Alive");
													}
													if (newDead != -9)  {
														long particleId = bdUpdator.addCellParticle (shapeId, voxelId,
																									particleName, newDead, "Dead");
													}
												}
											}

										} catch (Exception e) {
											Log.println (Log.ERROR, "FiSample2DEditor", "error while UPDATING data base", e);
										}
									}

								}//end on loop for k
							}//end on loop for j
						}//end on loop for i

					}
				}
				//ONLY BIOMASSE UPDATE
				else {

					for (int i=0; i<iNbVoxels ; i++) {
						for (int j=0; j<jNbVoxels ; j++) {
							for (int k=0; k<kNbVoxels ; k++) {

								//the voxel color is not empty
								if (gridVoxelValues[i][j][k] > 0) {

									FmDBVoxel voxel = gridVoxelRef[i][j][k];

									long voxelId = voxel.getDBId();
									int ci = voxel.getI();
									int cj = voxel.getJ();
									int ck = voxel.getK();

									int indexType = gridVoxelValues[i][j][k];

									for (int part =0; part<nbParticle ; part++) {
										String particleName  = particleNames[part];
										double newAlive = newBiomass [part][indexType][0];
										double newDead  = newBiomass [part][indexType][1];
										double oldAlive = oldBiomass [part][indexType][0];
										double oldDead  = oldBiomass [part][indexType][1];

										try {

											if (newAlive != oldAlive) {

												//convert grammes to kilos
												if (newAlive > 0)  newAlive = newAlive / 1000.0;

												long biomassID  = biomassId [part][indexType][0];
												if (biomassID > 0) {

													//delete particle
													if (newAlive == -9d) {
														bdUpdator.deleteCellParticle (voxelId, particleId [part][indexType][0]);
													}
													//update  particle
													else {
														bdUpdator.updateParameter (biomassID, newAlive);													}
												}
												//Adding ALIVE particle in the  voxel
												else if (newAlive != -9d)  {
													long particleId = bdUpdator.addCellParticle (shapeId, voxelId,
																								particleName, newAlive , "Alive");
												}

											}
											if (newDead != oldDead) {

												//convert grammes to kilos
												if (newDead > 0) newDead = newDead / 1000.0;

												long biomassID  = biomassId [part][indexType][1];
												if (biomassID > 0) {

													//delete particle
													if (newDead == -9d) {
														bdUpdator.deleteCellParticle (voxelId, particleId [part][indexType][1]);
													}
													//update particle
													else {
														bdUpdator.updateParameter (biomassID, newDead);
													}
												}
												//Adding DEAD particle in the  voxel
												else if (newDead != -9d) {
													long particleId = bdUpdator.addCellParticle (shapeId, voxelId,
																								particleName, newDead , "Dead");
												}
											}
										} catch (Exception e) {
											Log.println (Log.ERROR, "FiSample2DEditor", "error while UPDATING data base", e);
										}
									}
								}

							}//end on loop for k
						}//end on loop for j
					}//end on loop for i

				}


			} catch (Exception e) {
				Log.println (Log.ERROR, "FiSample2DEditor", "error while CREATING sample in data base", e);
			}


		 	setValidDialog (true);
		}

	 }

	/**	Control before validation
	 */
	@Override
	protected boolean controlCellValues ()   {
		int  nbColoredVoxel = 0;



		for (int i=0; i<iNbVoxels ; i++) {
			for (int j=0; j<jNbVoxels ; j++) {
				for (int k=0; k<kNbVoxels ; k++) {

					//the voxel has to be filled
					if (gridVoxelValues[i][j][k] != 0) {

						int indexType = gridVoxelValues[i][j][k];

						if (nbParticle <= 0) {
							JOptionPane.showMessageDialog (this, Translator.swap ("FiSample2DEditor.fillOneParticle"),
							Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
							return false;
						}

						//Each voxel should have a biomass value
						int nbBiomass = 0;
						for (int part =0; part<nbParticle ; part++) {
							if ((newBiomass [part][indexType][0] != -9) || (newBiomass [part][indexType][1] != -9)
							 || (newBiomass [part][indexType][0] != Double.NaN) || (newBiomass [part][indexType][1] != Double.NaN)) {
								nbBiomass++;
							}
						}
						if (nbBiomass <= 0) {
							JOptionPane.showMessageDialog (this, Translator.swap ("FiSample2DEditor.fillOneBiomasse"),
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
			JOptionPane.showMessageDialog (this, Translator.swap ("FiSample2DEditor.fillOneVoxel"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}

		return true;
	}


	//	Initialize the GUI.
	//
	@Override
	protected void createUI () {


		/*********** Fuel info and color legend panel **************/
		ColumnPanel legend = new ColumnPanel ();

		LinePanel bb = new LinePanel ();

		FmPlantInfoPanel plantInfoPanel = new FmPlantInfoPanel (plant);
		bb.add (plantInfoPanel);

		FmShapeInfoPanel shapeInfoPanel = new FmShapeInfoPanel (shape);
		bb.add (shapeInfoPanel);

		legend.add (bb);

		colorLegendPanel = new FmColorLegend (false, isTop, isCenter, isBottom);
		legend.add (colorLegendPanel);
		colorMap = colorLegendPanel.getColorMap();

		/*********** VOXEL 2D panel  **************/
		voxelPanel2D = new FmVoxel2DPanel (gridVoxelValues, iNbVoxels, jNbVoxels, kNbVoxels,
											iSizeVoxels, jSizeVoxels, kSizeVoxels,
											colorMap, false, fuelType, "");
		voxelPanel2D.addListener (this);
		voxelPanel2D.changeSelect (iSelected, jSelected, kSelected);

		/*********** VOXEL particle panel  **************/

		voxelParticlePanel = new FmVoxelParticleEditor (model, shape, nbTypeMax);
		legend.add (voxelParticlePanel);
		voxelParticlePanel.changeSelect (typeSelected, false);
		voxelParticlePanel.addListener (this);


		/*********** Control panel **************/
		JPanel controlPanel = new JPanel ();
		controlPanel.setLayout (new FlowLayout (FlowLayout.LEFT));

		addParticle = new JButton (Translator.swap ("FiSample2DEditor.addParticle"));
		controlPanel.add (addParticle);
		save = new JButton (Translator.swap ("FiSample2DEditor.validate"));
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
		getContentPane ().add (voxelPanel2D, BorderLayout.CENTER);
		getContentPane ().add (legend, BorderLayout.EAST);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		if (shape.getFuelType() == 3) {
			setTitle (Translator.swap ("FiSample2DEditor.title"));
		}
		else if (shape.getFuelType() == 4) {
			setTitle (Translator.swap ("FiSample2DEditor.titleCore"));
		}
		else if (shape.getFuelType() == 5) {
			setTitle (Translator.swap ("FiSample2DEditor.titleEdge"));
		}

		setSize (new Dimension(600, 500));


		setModal (true);
	}


	/**
	* grid dimension for sample in 2D
	*/
	@Override
	protected void setGridSize () {

		//for sample, column dimension is sample height
		iSizeVoxels = (int) (shape.getVoxelXSize() * 100);	//convert m to cm
		jSizeVoxels = (int) (shape.getVoxelYSize() * 100);
		kSizeVoxels = (int) (shape.getVoxelZSize() * 100);

		kNbVoxels = (int) ((shape.getZMax () * 100) / kSizeVoxels);
		iNbVoxels = 1;
		jNbVoxels = 1;

	}

	/**	Retrieve external events
	 */
	@Override
	public void somethingHappened (ListenedTo l, Object param) {

		if (l instanceof FmVoxel2DPanel) {
			if (Integer.class.isInstance (param)) {
				voxelParticlePanel.saveValues ();
				Integer type = (Integer) param;
				typeSelected = type;
				voxelParticlePanel.changeSelect (typeSelected, false);
			}
		}
		else if (l instanceof FmVoxelParticleEditor) {

			addHistory();
		}
	}
	/** Return the new shape id
	 */
	public long getShapeId () {
		return shapeId;
	}

	/**	To store history used for UNDO-REDO.
	*/
	@Override
	protected void addHistory () {

		//retrieve data from particle panel
		newBiomass = voxelParticlePanel.getNewBiomass ();

		double[][][] ancValues = new double [nbParticleMax][nbTypeMax][2];

		for (int part=0; part <nbParticleMax; part++) {
			for (int type=0; type <nbTypeMax; type++) {
				ancValues[part][type][0] =  newBiomass[part][type][0];
				ancValues[part][type][1] =  newBiomass[part][type][1];
			}
		}

		selectionHistory.add (ancValues);

		firstHistory = true;
	}
	/**	Called on ctrl-Z. UNDO
	*/
	@Override
	protected void ctrlZPressed () {

		double[][][] ancValues;
		if (firstHistory) ancValues = (double[][][]) selectionHistory.current ();
		else ancValues = (double[][][]) selectionHistory.back ();

		if (ancValues != null) {

			newBiomass = ancValues;
			voxelParticlePanel.setBiomass (newBiomass);
			voxelParticlePanel.changeSelect (typeSelected, false);
			firstHistory = false;
		}
	}
	/**	Called on ctrl-Y. REDO
	*/
	@Override
	protected void ctrlYPressed () {

		double[][][] ancValues;
		ancValues = (double[][][]) selectionHistory.next ();
		if (ancValues != null) {
			newBiomass = ancValues;
			voxelParticlePanel.setBiomass (newBiomass);
			voxelParticlePanel.changeSelect (typeSelected, false);
		}
	}
}
