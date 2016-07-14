package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Log;
import jeeb.lib.util.SetMap;
import jeeb.lib.util.Translator;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBShape;
import fireparadox.model.database.FmDBVoxel;
import fireparadox.model.database.FmVoxelType;


/**
 * FiLayer2DEditor : fuel voxel 2D design (layers)
 *
 * @author I. Lecomte - March 2008
 */
public class FmLayer2DEditor extends FmPlant2DEditor {

	//Grid of value (EDGE)
	protected HashMap<Long, FmDBVoxel> edgeMap;		//map of sample voxels
	protected int [][][] ancEdgeValues;				//voxels before update
	protected int [][][] gridEdgeValues;			//voxels after  update
	protected FmDBVoxel [][][] gridEdgeRef;			//voxel references
	protected int iNbEdges, jNbEdges, kNbEdges;		//grid dimensions for edge in number of voxels

	protected FmDBShape sampleEdge;
	protected int iEdgeCopy, jEdgeCopy;

	protected long [] typeEdgeIds;					//edge voxel type ids for each color
	protected FmVoxelType [] typesEdge;				//edge voxel type for each color					//voxel for each color
	protected SetMap typeEdgeVoxel;					//edge voxel for each color
	protected boolean isEdgeVoxels;
	protected boolean isEdgeFocus;						//is focus on edge side ?


	//Split pane for panel 2D
	protected JSplitPane splitPanel;
	protected FmVoxel2DPanel edgePanel2D;
	protected int nbPlusEdgeLeft, nbPlusEdgeRight, nbPlusEdgeTop, nbPlusEdgeBottom;
	protected FmVoxelParticleLayerPanel voxelParticlePanel;

	/**	Constructor for 2D.
	 */
	 public FmLayer2DEditor  (FmModel _model, FmDBShape _shape, FmDBShape _sample, FmDBShape _sampleEdge) {

		super (_model, _sample,  _shape, false) ;

		sampleEdge = _sampleEdge;


		//Load fuel info and voxels list from database
		//Load shape voxels
		isVoxels = false;
		isEdgeVoxels = false;
		loadVoxels ();
		loadVoxelEdges();

		//if no voxels, load voxel from sample
		if ((!isVoxels) && (sample != null)) loadVoxelSample ();
		if (!isEdgeVoxels) loadVoxelSampleEdges ();


		isEdgeFocus = false;
		measuredBiomass = plant.getTotalMeasuredBiomass();

		//to count + and - on grid side
		nbPlusLeft = 0;
		nbPlusRight = 0;
		nbPlusTop = 0;
		nbPlusBottom = 0;

		//selected voxel
		iSelected = 0;
		jSelected = 0;
		if (kNbVoxels > 0) {
			kSelected = kNbVoxels - 1;
			typeSelected = gridVoxelValues[iSelected][jSelected][kSelected];
		}
		else {
			kSelected = kNbEdges - 1;
			typeSelected = gridEdgeValues[iSelected][jSelected][kSelected];
		}


		createUI ();

		show ();

	 }

	/**	Initialize the GUI.
	 */
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

		colorLegendPanel = new FmColorLegend (true, isTop, isCenter, isBottom);
		colorLegendPanel.addListener (this);
		legend.add (colorLegendPanel);
		colorMap = colorLegendPanel.getColorMap();


		/*********** VOXEL 2D panel  **************/
		isEdgeFocus = false;
		String shapeKind = shape.getShapeKind();
		if (kNbVoxels > 0) {
			voxelPanel2D = new FmVoxel2DPanel (gridVoxelValues, iNbVoxels, jNbVoxels, kNbVoxels,
											iSizeVoxels, jSizeVoxels, kSizeVoxels,
											colorMap, true, 4, shapeKind);

			voxelPanel2D.addListener (this);
			voxelPanel2D.changeSelect (iSelected, jSelected, kSelected);
			isEdgeFocus = false;

		}
		else isEdgeFocus = true;

		if (kNbEdges > 0) {

			edgePanel2D = new FmVoxel2DPanel (gridEdgeValues, iNbEdges, jNbEdges, kNbEdges,
											iSizeVoxels, jSizeVoxels, kSizeVoxels,
											colorMap, true, 5, shapeKind);

			edgePanel2D.addListener (this);
		}

		splitPanel = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, voxelPanel2D, edgePanel2D);
		splitPanel.setResizeWeight(0.5);

		/*********** VOXEL particle panel  **************/
		if ((sample != null) && (sampleEdge != null)) {
			voxelParticlePanel = new FmVoxelParticleLayerPanel (model, sample, sampleEdge, nbTypeMax);
		}
		else if (sample != null)  {
			voxelParticlePanel = new FmVoxelParticleLayerPanel (model, sample, sample, nbTypeMax);
		}
		else if (sampleEdge != null) {
			voxelParticlePanel = new FmVoxelParticleLayerPanel (model, sampleEdge, sampleEdge, nbTypeMax);
		}
		else {
			voxelParticlePanel = new FmVoxelParticleLayerPanel (model, shape, shape, nbTypeMax);
		}

		voxelParticlePanel.changeSelect (typeSelected, false);

		legend.add (voxelParticlePanel);


		/*********** Control panel **************/
		controlPanel = new JPanel ();
		controlPanel.setLayout (new FlowLayout (FlowLayout.LEFT));


		save = new JButton (Translator.swap ("FiLayer2DEditor.validate"));
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

		calculatedBiomass = calculateBiomass ();
		calculatedBiomass += calculateEdgeBiomass ();
		calcul = new JTextField (7);
		calcul.setText (""+calculatedBiomass);
		JTextField measure = new JTextField (7);
		measure.setText (""+measuredBiomass);
		measure.setEnabled(false);
		calcul.setEnabled(false);

		controlPanel.add (new JLabel (Translator.swap ("FiLayer2DEditor.measuredBiomass")));
		controlPanel.add (measure);
		controlPanel.add (new JLabel (Translator.swap ("FiLayer2DEditor.calculatedBiomass")));
		controlPanel.add (calcul);



		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (splitPanel, BorderLayout.CENTER);
		getContentPane ().add (legend, BorderLayout.EAST);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		setTitle (Translator.swap ("FiLayer2DEditor.title"));

		this.setSize (new Dimension(800,600));

		setModal (true);
	}
	/**	Validation
	 */
	@Override
	protected void validateAction () {
		if (voxelPanel2D != null) 	super.validateAction ();
		if (edgePanel2D != null)  validateEdgeAction ();
		setValidDialog (true);

	}

	/**	Save in database
	 */
	protected void validateEdgeAction () {

		if (controlEdgeValues ())  {

			//SHAPE CREATION IN DATABASE
			if (shapeId < 0) {
				try {

					shapeId = bdUpdator.createShape (shape);

					if (shapeId > 0) {
						shape.setShapeId (shapeId);
					}
				} catch (Exception e) {
					Log.println (Log.ERROR, "FiLayer2DEditor", "error while CREATING SHAPE in data base", e);
				}
			}

			//first path for voxel creation
			for (int i=0; i<iNbEdges ; i++) {
				for (int j=0; j<jNbEdges ; j++) {
					for (int k=0; k<kNbEdges ; k++) {

						if  ((gridEdgeValues[i][j][k] != ancEdgeValues[i][j][k]) &&	//Something has changed
							(gridEdgeRef[i][j][k] == null))  {							//voxel ref is null


							int indexType = gridEdgeValues[i][j][k];

							if (indexType > 0)   {
								FmVoxelType type = typesEdge [indexType];
								FmDBVoxel voxel = getFirstEdgeVoxel(type) ;
								try {

									long voxelId = -1;
									boolean edge = true;
									FmDBVoxel newVoxel = new FmDBVoxel (voxelId, i, j, k, type, edge);
									voxelId = bdUpdator.copyCell (shapeId, newVoxel, voxel);
									if (voxelId > 0) {
										newVoxel.setDBId (voxelId);

										//set of voxel by type has to be modified
										typeEdgeVoxel.addObject (type, newVoxel);
									}

								} catch (Exception e) {
									Log.println (Log.ERROR, "FiLayer2DEditor", "error while CREATING VOXEL in data base", e);
								}

							}
						}
					}
				}
			}
			//Second path for voxel update and delete
			for (int i=0; i<iNbEdges ; i++) {
				for (int j=0; j<jNbEdges ; j++) {
					for (int k=0; k<kNbEdges ; k++) {

						//Something has changed
						if  ((gridEdgeValues[i][j][k] != ancEdgeValues[i][j][k]) && 	//Something has changed
							(gridEdgeRef[i][j][k] != null))  {							//Voxel ref exists

							int indexType = gridEdgeValues[i][j][k];
							if (indexType > 0)   { 								//This voxel has a type
								FmVoxelType type = typesEdge [indexType];

								try {
									FmDBVoxel voxel = gridEdgeRef[i][j][k];
									indexType = ancEdgeValues[i][j][k];
									FmDBVoxel copyVoxel = getFirstEdgeVoxel(type, voxel.getDBId()) ;
									bdUpdator.updateCell (voxel, copyVoxel);
									FmVoxelType copyType = copyVoxel.getVoxelType();

									//set of voxel by type has to be modified
									typeEdgeVoxel.removeObject (copyType, copyVoxel);
									typeEdgeVoxel.addObject (type, voxel);
								} catch (Exception e) {
									Log.println (Log.ERROR, "FiLayer2DEditor", "error while UPDATING VOXEL in data base", e);
								}

							}
							//VOXEL delete
							else   {
								try {
									FmDBVoxel voxel = gridEdgeRef[i][j][k];
									FmVoxelType type = voxel.getVoxelType();
									bdUpdator.deleteCell (shapeId, voxel);

									//set of voxel by type has to be modified
									typeEdgeVoxel.removeObject (type, voxel);
								} catch (Exception e) {
									Log.println (Log.ERROR, "FiLayer2DEditor", "error while DELETING VOXEL in data base", e);
								}

							}
						}

					}//end on loop for k
				}//end on loop for j
			}//end on loop for i
		}
	}

	/**	Control before validation
	 */
	private boolean controlEdgeValues ()   {

		if ((iNbEdges>0) && (kNbEdges>0)) {
			int total = 0;
			for (int i=0; i<iNbEdges ; i++) {
				for (int j=0; j<jNbEdges ; j++) {
					for (int k=0; k<kNbEdges ; k++) {
						total += gridEdgeValues[i][j][k];
					}
				}
			}

			if (total <= 0) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiLayer2DEditor.fillOneVoxel"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}

		}

		return true;
	}

	/**	Load fuel edges from database
	 */
	protected void loadVoxelEdges () {

		//to store voxel type ids for each color
		typeEdgeIds = new long [nbTypeMax];
		typesEdge = new FmVoxelType [nbTypeMax];
		typeEdgeVoxel = new SetMap ();
		for (int i=0; i<nbTypeMax; i++) {
			typeEdgeIds[i] =  -1;
			typesEdge[i] =  null;
		}

		if (shape != null) {
			edgeMap = shape.getEdgeVoxels(); 	//to get edge voxel map

			//if there is voxels
			if ((edgeMap != null) && (edgeMap.size() > 0)) {

				isEdgeVoxels = true;

				for (Iterator iter = edgeMap.keySet().iterator(); iter.hasNext ();) {

					Object cle = iter.next();
					FmDBVoxel voxel = edgeMap.get(cle);
					int i = voxel.getI();
					int j = voxel.getJ();
					int k = voxel.getK();

					//if no type, it is not a mesured plant with cube method !!!
					if (voxel.getVoxelType() != null) {
						FmVoxelType type = voxel.getVoxelType();
						int indexType = type.getTypeIndex();
						if (indexType >= 0) {

							storeEdgeVoxelType (indexType, type, voxel);

							if ((i < iNbEdges) && (j < jNbEdges) && (k < kNbEdges) ) {		//to avoid J dimension in 2D
								gridEdgeRef[i][j][k] = voxel;
								gridEdgeValues[i][j][k] = indexType;
								ancEdgeValues[i][j][k]  = indexType;
							}

						}
					}
				}
			}
		}

	}

	/**	Load sample voxels from database
	 */
	protected void loadVoxelSampleEdges () {

		try {

			//if no sample edge, the core sample is used also for edge
			if (sampleEdge != null) {
				sampleEdge = bdCommunicator.getShapeVoxels (sampleEdge, false);	//to get all shape info
				edgeMap = sampleEdge.getVoxels();
			}
			else if (sample != null) {
				edgeMap = sample.getVoxels();
			}

			for (Iterator iter = edgeMap.keySet().iterator(); iter.hasNext ();) {

				Object cle = iter.next();
				FmDBVoxel voxel = edgeMap.get(cle);
				int i = iSampleCopy;
				int k = voxel.getK();
				int j = jSampleCopy;


				//if no type, it is not a mesured plant with cube method !!!
				if (voxel.getVoxelType() != null) {

					FmVoxelType type = voxel.getVoxelType();
					int indexType = type.getTypeIndex();
					if (indexType >= 0) {
						storeEdgeVoxelType (indexType, type, voxel);

						if ((i < iNbEdges) && (j < jNbEdges) && (k < kNbEdges) ) {		//to avoid J dimension in 2D

							gridEdgeRef[i][j][k] = null;
							gridEdgeValues[i][j][k] = indexType;
							ancEdgeValues[i][j][k]  = -1;
						}

					}
				}
			}

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiLayer2DEditor.loadSampleEdges() ", "error while opening FUEL data base", e);
		}
	}

	/**
	* Store voxel type in a tables
	*/
	protected void storeEdgeVoxelType (int indexType, FmVoxelType type, FmDBVoxel voxel) {
		typeEdgeIds[indexType] = type.getDBId();
		typesEdge[indexType] = type;
		typeEdgeVoxel.addObject (type, voxel);
		if (indexType == 1) isTop = true;
		if (indexType == 2) isCenter = true;
		if (indexType == 3) isBottom = true;
	}

	/**	Return the first voxel for this voxel type
	 */
	protected FmDBVoxel getFirstEdgeVoxel (FmVoxelType type) {
		HashSet voxelList = (HashSet) typeEdgeVoxel.getObjects(type);
		if (voxelList.size() > 0) {
			Object [] voxelArray = voxelList.toArray();
			return (FmDBVoxel) voxelArray[0];
		}
		else return null;
	}
	/**	Return first the voxel for this voxel type (except if voxel.getDBId()=id)
	 */
	protected FmDBVoxel getFirstEdgeVoxel (FmVoxelType type, long id) {

		HashSet voxelList = (HashSet) typeEdgeVoxel.getObjects(type);
		if (voxelList.size() > 0) {
			Object [] voxelArray = voxelList.toArray();
			for (int i=0;  i<voxelList.size() ; i++) {
				FmDBVoxel voxel = (FmDBVoxel) voxelArray[i];
				if (voxel.getDBId() != id) return voxel;
			}
		}
		return null;
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

		//+/- left or right
		if ((left) || (right)) {

			if ((nbPlusEdgeLeft >=0) && (nbPlusEdgeRight >= 0) ) {

				int[][][] savGridEdgeValues = new int[iNbEdges][jNbEdges][kNbEdges];
				int[][][] savAncEdgeValues  = new int[iNbEdges][jNbEdges][kNbEdges];
				FmDBVoxel[][][] savEdgeVoxelRef    = new FmDBVoxel [iNbEdges][jNbEdges][kNbEdges];


				for (int i=0;i<iNbEdges;i++) {
					for (int j=0;j<jNbEdges;j++) {
						for (int k=0;k<kNbEdges;k++) {
							savGridEdgeValues[i][j][k] = gridEdgeValues[i][j][k];
							savAncEdgeValues[i][j][k] = ancEdgeValues[i][j][k];
							savEdgeVoxelRef[i][j][k] = gridEdgeRef[i][j][k];
						}
					}
				}

				//NEW GRID CREATION
				int newi;
				if (plus)  newi = iNbEdges + 1;
				else newi = iNbEdges - 1;

				gridEdgeValues = new int[newi][jNbEdges][kNbEdges];
				ancEdgeValues  = new int[newi][jNbEdges][kNbEdges];
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
								ancEdgeValues[i][j][k] = savAncEdgeValues[i][j][k];
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
								ancEdgeValues[i][j][k] = savAncEdgeValues[i][j][k];
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

				int[][][] savGridEdgeValues = new int[iNbEdges][jNbEdges][kNbEdges];
				int[][][] savAncEdgeValues  = new int[iNbEdges][jNbEdges][kNbEdges];
				FmDBVoxel[][][] savEdgeVoxelRef    = new FmDBVoxel [iNbEdges][jNbEdges][kNbEdges];


				for (int i=0;i<iNbEdges;i++) {
					for (int j=0;j<jNbEdges;j++) {
						for (int k=0;k<kNbEdges;k++) {
							savGridEdgeValues[i][j][k] = gridEdgeValues[i][j][k];
							savAncEdgeValues[i][j][k] = ancEdgeValues[i][j][k];
							savEdgeVoxelRef[i][j][k] = gridEdgeRef[i][j][k];
						}
					}
				}

				//NEW GRID CREATION
				int newk;
				if (plus)  newk = kNbEdges + 1;
				else newk = kNbEdges - 1;

				gridEdgeValues = new int[iNbEdges][jNbEdges][newk];
				ancEdgeValues  = new int[iNbEdges][jNbEdges][newk];
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
								ancEdgeValues[i][j][k] = savAncEdgeValues[i][j][k];
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
								ancEdgeValues[i][j][k] = savAncEdgeValues[i][j][k];
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

	/**
	* grid dimension for layer in 2D
	*/
	@Override
	protected void setGridSize () {

		iSizeVoxels = (int) (shape.getVoxelXSize() * 100);	//convert m to cm
		jSizeVoxels = (int) (shape.getVoxelYSize() * 100);
		kSizeVoxels = (int) (shape.getVoxelZSize() * 100);


		kNbVoxels = (int) ((shape.getZMax () * 100) / kSizeVoxels);
		iNbVoxels  = (int) ((shape.getXMax () * 100) / iSizeVoxels);
		jNbVoxels  = 1;

		kNbEdges = (int) ((shape.getZEdgeMax () * 100) / kSizeVoxels);;
		iNbEdges = (int) ((shape.getXEdgeMax () * 100) / iSizeVoxels);
		jNbEdges = 1;


		iSampleCopy = iNbVoxels/2;
		jSampleCopy = 0;

		iEdgeCopy = iNbEdges/2;
		jEdgeCopy = 0;

		gridEdgeValues = new int[iNbEdges][jNbEdges][kNbEdges];
		ancEdgeValues  = new int[iNbEdges][jNbEdges][kNbEdges];
		gridEdgeRef    = new FmDBVoxel [iNbEdges][jNbEdges][kNbEdges];
	}


	/**
	* calculate total biomass for the shape
	*/
	@Override
	protected double calculateBiomass () {

		double total = 0.0;
		double [][][] biomass = voxelParticlePanel.getNewBiomass();
		if (biomass != null) {
			int nbParticle = voxelParticlePanel.getNbParticle();
			for (int i=0;i<iNbVoxels;i++) {
				for (int j=0;j<jNbVoxels;j++) {
					for (int k=0;k<kNbVoxels;k++) {
						int type = gridVoxelValues[i][j][k];
						if (type > 0) {
							for (int np=0; np<nbParticle; np++) {

								if (biomass[np][type][0] > 0) {
									total += biomass[np][type][0];
								}
							}
						}
					}
				}
			}
		}
		return total;
	}

	/**
	* calculate total biomass for the shape
	*/
	protected double calculateEdgeBiomass () {

		double total = 0.0;
		double [][][] biomass = voxelParticlePanel.getNewEdgeBiomass();
		if (biomass != null) {
			int nbParticle = voxelParticlePanel.getNbEdgeParticle();
			for (int i=0;i<iNbEdges;i++) {
				for (int j=0;j<jNbEdges;j++) {
					for (int k=0;k<kNbEdges;k++) {
						int type = gridEdgeValues[i][j][k];
						if (type > 0) {
							for (int np=0; np<nbParticle; np++) {
								if (biomass[np][type][0] > 0) {
									total += biomass[np][type][0];
								}
							}
						}
					}
				}
			}
		}
		return total;
	}

	/**	Retrieve external events
	 */
	@Override
	public void somethingHappened (ListenedTo l, Object param) {
		if (l instanceof FmColorLegend) {
			Integer color = (Integer) param;
			typeSelected = color;
			voxelParticlePanel.changeSelect (typeSelected, isEdgeFocus);
			if (voxelPanel2D != null) voxelPanel2D.setColor (typeSelected);
			if (edgePanel2D != null) edgePanel2D.setColor (typeSelected);
		}
		if (l instanceof FmVoxel2DPanel) {
			if (l==voxelPanel2D) {
				isEdgeFocus = false;
			}
			else {
				isEdgeFocus = true;
			}

			if (Integer.class.isInstance (param)) {
				Integer type = (Integer) param;
				typeSelected = type;
				voxelParticlePanel.changeSelect (typeSelected, isEdgeFocus);
			}
			else {

				if (String.class.isInstance (param)) {

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
				else if (Integer.class.isInstance (param)) {}
				else {

					if (l==voxelPanel2D) {
						addHistory (false);
					}
					else {
						addHistory (true);
					}

					voxelParticlePanel.changeSelect (typeSelected, isEdgeFocus);
					calculatedBiomass = calculateBiomass ();
					calculatedBiomass += calculateEdgeBiomass ();
					calcul.setText (""+calculatedBiomass);
					controlPanel.repaint ();
					this.repaint ();
				}
			}
		}
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
