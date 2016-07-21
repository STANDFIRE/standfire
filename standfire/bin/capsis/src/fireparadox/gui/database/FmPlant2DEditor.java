package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Log;
import jeeb.lib.util.SetMap;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import capsis.util.History;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBCommunicator;
import fireparadox.model.database.FmDBPlant;
import fireparadox.model.database.FmDBShape;
import fireparadox.model.database.FmDBUpdator;
import fireparadox.model.database.FmDBVoxel;
import fireparadox.model.database.FmVoxelType;

/**
 * FiPlant2DEditor : shape unique crown 2D design
 *
 * @author I. Lecomte - March 2008
 */
public class FmPlant2DEditor extends AmapDialog implements ActionListener, jeeb.lib.util.Listener {

	protected FmModel model;
	protected FmDBCommunicator bdCommunicator;		//to read database
	protected FmDBUpdator bdUpdator;					//to update database

	//shape information
	protected FmDBShape shape;
	protected FmDBPlant plant;
	protected long plantId, shapeId;							//id in the database
	protected int fuelType;							//1=plant 2=layer >3=sample
	protected HashMap<Long, FmDBVoxel> voxelMap;	//map of sample voxels
	protected boolean isVoxels;

	protected long [] typeIds;						//voxel type ids for each color
	protected FmVoxelType [] types;					//voxel type for each color
	protected SetMap typeVoxel;						//voxel for each color
	protected int nbTypeMax = 4;					//nb type of voxels (NO/TOP/CENTER/BOTTOM)

	//Grid color values
	protected int [][][] ancVoxelValues;				//voxels before update
	protected int [][][] gridVoxelValues;				//voxels after  update
	protected FmDBVoxel [][][] gridVoxelRef;			//voxel references
	protected int iNbVoxels, jNbVoxels, kNbVoxels;		//grid dimensions in number of voxels
	protected int iSizeVoxels, jSizeVoxels, kSizeVoxels;	//Size of the voxels in cm

	//for sample voxel copy
	protected FmDBShape sample;
	protected int iSampleCopy, jSampleCopy;


	//Selections
	protected int iSelected,jSelected,kSelected;		//voxel coordinate
	protected int typeSelected = 0;						//voxel type


	//Color control
	protected Map<Integer,Color> colorMap;
	protected FmColorLegend colorLegendPanel;
	protected boolean isTop, isCenter, isBottom;

	//Voxel panel 2D
	protected FmVoxel2DPanel voxelPanel2D;
	protected int nbPlusLeft, nbPlusRight, nbPlusTop, nbPlusBottom;

	//Particle panel
	protected FmVoxelParticlePanel voxelParticlePanel;

	//check biomass cumulated
	protected JTextField calcul;
	protected double calculatedBiomass, measuredBiomass;


	//for undo/redo
	protected JButton undoButton;
	protected JButton redoButton;
	protected History selectionHistory;
	protected boolean firstHistory = false;


	//Validation control
	protected JPanel controlPanel;
	protected JButton save;
	protected JButton cancel;
	protected JButton help;

	public FmPlant2DEditor () {
	}

	/**	Constructor for 2D.
	 */
	public FmPlant2DEditor (FmModel _model, FmDBShape _sample, FmDBShape _shape, boolean isInit) {

		model = _model;
		shape = _shape;
		sample = _sample;
		plant = shape.getPlant();
		shapeId = shape.getShapeId();
		fuelType = shape.getFuelType();


		//connecting database
		bdCommunicator = model.getBDCommunicator ();
		bdUpdator = model.getBDUpdator ();

		//history for undo/redo
		selectionHistory = new History ();

		if (isInit) init();
	}

	public void init () {

		//Load shape voxels
		isVoxels = false;
		loadVoxels ();

		//if no voxels, load voxel from sample
		if ((!isVoxels) && (sample != null)) loadVoxelSample ();

		measuredBiomass = plant.getTotalMeasuredBiomass();

		//to count + and - on grid side
		nbPlusLeft = 0;
		nbPlusRight = 0;
		nbPlusTop = 0;
		nbPlusBottom = 0;

		//selected voxel
		iSelected = 0;
		jSelected = 0;
		kSelected = kNbVoxels - 1;
		typeSelected = gridVoxelValues[iSelected][jSelected][kSelected];

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
		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**	Control before validation
	 */
	protected boolean controlCellValues ()   {

		if ((iNbVoxels>0) && (kNbVoxels>0)) {

			int total = 0;
			for (int i=0; i<iNbVoxels ; i++) {
				for (int j=0; j<jNbVoxels ; j++) {
					for (int k=0; k<kNbVoxels ; k++) {
						total += gridVoxelValues[i][j][k];
					}
				}
			}

			if (total <= 0) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiPlant2DEditor.fillOneVoxel"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}
		}


		return true;
	}
	/**	Validation
	 */
	protected void validateAction () {

		gridVoxelValues = voxelPanel2D.getNewVoxelValues ();

		if ( controlCellValues()) {

			//SHAPE CREATION IN DATABASE
			if (shapeId < 0) {
				try {

					shapeId = bdUpdator.createShape (shape);

					if (shapeId > 0) {
						shape.setShapeId (shapeId);
					}
				} catch (Exception e) {
					Log.println (Log.ERROR, "FiPlant2DEditor", "error while CREATING SHAPE in data base", e);
				}
			}

			//first path for voxel creation
			for (int i=0; i<iNbVoxels ; i++) {
				for (int j=0; j<jNbVoxels ; j++) {
					for (int k=0; k<kNbVoxels ; k++) {


						if  ((gridVoxelValues[i][j][k] != ancVoxelValues[i][j][k]) &&	//Something has changed
							(gridVoxelRef[i][j][k] == null))  {							//voxel ref is null

							int indexType = gridVoxelValues[i][j][k];
							if (indexType > 0)   {

								FmVoxelType type = types [indexType];
								FmDBVoxel voxel = getFirstVoxel(type) ;
								try {
									long voxelId = -1;
									boolean edge = false;
									FmDBVoxel newVoxel = new FmDBVoxel (voxelId, i, j, k, type, edge);
									voxelId = bdUpdator.copyCell (shapeId, newVoxel, voxel);
									newVoxel.setDBId (voxelId);

									//set of voxel by type has to be modified
									typeVoxel.addObject (type, newVoxel);

								} catch (Exception e) {
									Log.println (Log.ERROR, "FiPlant2DEditor", "error while CREATING VOXEL in data base", e);
								}

							}
						}
					}
				}
			}
			//Second path for voxel update and delete
			for (int i=0; i<iNbVoxels ; i++) {
				for (int j=0; j<jNbVoxels ; j++) {
					for (int k=0; k<kNbVoxels ; k++) {

						//Something has changed
						if  ((gridVoxelValues[i][j][k] != ancVoxelValues[i][j][k]) && 	//Something has changed
							(gridVoxelRef[i][j][k] != null))  {							//Voxel ref exists

							int indexType = gridVoxelValues[i][j][k];
							if (indexType > 0)   { 								//This voxel has a type
								FmVoxelType type = types [indexType];

								try {
									FmDBVoxel voxel = gridVoxelRef[i][j][k];
									indexType = ancVoxelValues[i][j][k];
									FmDBVoxel copyVoxel = getFirstVoxel(type, voxel.getDBId()) ;
									bdUpdator.updateCell (voxel, copyVoxel);
									FmVoxelType copyType = copyVoxel.getVoxelType();

									//set of voxel by type has to be modified
									typeVoxel.removeObject (copyType, copyVoxel);
									typeVoxel.addObject (type, voxel);
								} catch (Exception e) {
									Log.println (Log.ERROR, "FiPlant2DEditor", "error while UPDATING VOXEL in data base", e);
								}

							}
							//VOXEL delete
							else   {
								try {
									FmDBVoxel voxel = gridVoxelRef[i][j][k];
									FmVoxelType type = voxel.getVoxelType();
									bdUpdator.deleteCell (shapeId, voxel);

									//set of voxel by type has to be modified
									typeVoxel.removeObject (type, voxel);
								} catch (Exception e) {
									Log.println (Log.ERROR, "FiPlant2DEditor", "error while DELETING VOXEL in data base", e);
								}

							}
						}

					}//end on loop for k
				}//end on loop for j
			}//end on loop for i

			setValidDialog (true);

		}
	}

	/**	Initialize the GUI.
	 */
	protected void createUI () {

		/*********** Fuel info and color legend panel **************/
		ColumnPanel legend = new ColumnPanel ();

		LinePanel info = new LinePanel ();

		FmPlantInfoPanel plantInfoPanel = new FmPlantInfoPanel (plant);
		info.add (plantInfoPanel);

		FmShapeInfoPanel shapeInfoPanel = new FmShapeInfoPanel (shape);
		info.add (shapeInfoPanel);

		legend.add (info);


		colorLegendPanel = new FmColorLegend (true, isTop, isCenter, isBottom);
		colorLegendPanel.addListener (this);
		colorMap = colorLegendPanel.getColorMap();
		legend.add (colorLegendPanel);


		/*********** VOXEL 2D panel  **************/
		String shapeKind = shape.getShapeKind();
		voxelPanel2D = new FmVoxel2DPanel (gridVoxelValues, iNbVoxels, jNbVoxels, kNbVoxels,
											iSizeVoxels, jSizeVoxels, kSizeVoxels,
											colorMap, true, 3, shapeKind);
		voxelPanel2D.addListener (this);

		//show the right depth for 2D
		int depth = 0;
		voxelPanel2D.setDepth (depth);
		voxelPanel2D.changeSelect (iSelected, jSelected, kSelected);

		/*********** VOXEL particle panel  **************/
		if (sample != null) {
			voxelParticlePanel = new FmVoxelParticlePanel (model, sample, nbTypeMax);
		}
		else {
			voxelParticlePanel = new FmVoxelParticlePanel (model, shape, nbTypeMax);
		}
		legend.add (voxelParticlePanel);
		voxelParticlePanel.changeSelect (typeSelected, false);

		/*********** Control panel **************/
		controlPanel = new JPanel ();
		controlPanel.setLayout (new FlowLayout (FlowLayout.LEFT));

		save = new JButton (Translator.swap ("FiPlant2DEditor.validate"));
		if ((plant.isValidated()) || (shape.isDeleted())) {
			save.setEnabled(false);
		}
		else {
			save.addActionListener (this);
		}
		controlPanel.add (save);
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		cancel.addActionListener (this);
		controlPanel.add (cancel);
		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		controlPanel.add (help);

		calculatedBiomass = calculateBiomass ();
		calcul = new JTextField (7);
		calcul.setText (""+calculatedBiomass);
		JTextField measure = new JTextField (7);
		measure.setText (""+measuredBiomass);
		measure.setEnabled(false);
		calcul.setEnabled(false);

		controlPanel.add (new JLabel (Translator.swap ("FiPlant2DEditor.measuredBiomass")));
		controlPanel.add (measure);
		controlPanel.add (new JLabel (Translator.swap ("FiPlant2DEditor.calculatedBiomass")));
		controlPanel.add (calcul);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (voxelPanel2D, BorderLayout.CENTER);
		getContentPane ().add (legend, BorderLayout.EAST);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		setTitle (Translator.swap ("FiPlant2DEditor.title"));

		this.setSize (new Dimension(800,600));

		setModal (true);
	}

	/**	Load shape voxels from database
	 */
	protected void loadVoxels () {

		//to store voxel type ids for each color
		typeIds = new long [nbTypeMax];
		types = new FmVoxelType [nbTypeMax];
		typeVoxel = new SetMap ();
		for (int i=0; i<nbTypeMax; i++) {
			typeIds[i] =  -1;
			types[i] =  null;
		}


		try {

			if (fuelType < 3) {
				long plantId = plant.getPlantId();
				plant = bdCommunicator.getPlant (model, plantId);	//to get all plant info
			}

			shape = bdCommunicator.getShapeVoxels (shape, false);	//to get all shape info
			voxelMap = shape.getVoxels();

			//grid dimension for plant in 2D
			setGridSize ();

			gridVoxelValues = new int[iNbVoxels][jNbVoxels][kNbVoxels];
			ancVoxelValues  = new int[iNbVoxels][jNbVoxels][kNbVoxels];
			gridVoxelRef    = new FmDBVoxel [iNbVoxels][jNbVoxels][kNbVoxels];

			//if there is already voxels in the shape
			if ((voxelMap != null) && (voxelMap.size() > 0)) {

				isVoxels = true;

				for (Iterator iter = voxelMap.keySet().iterator(); iter.hasNext ();) {

					Object cle = iter.next();
					FmDBVoxel voxel = voxelMap.get(cle);
					int i = voxel.getI();
					int j = voxel.getJ();
					int k = voxel.getK();

					//if no type, it is not a mesured plant with cube method !!!
					if (voxel.getVoxelType() != null) {
						FmVoxelType type = voxel.getVoxelType();
						int indexType = type.getTypeIndex();
						if (indexType >= 0) {

							storeVoxelType (indexType, type, voxel);

							if ((i<iNbVoxels) && (j<jNbVoxels)) {
								gridVoxelValues[i][j][k] = indexType;
								gridVoxelRef[i][j][k] = voxel;
								ancVoxelValues[i][j][k]  = indexType;
							}
						}
					}
				}
			}

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPlant2DEditor.loadVoxels() ", "error while opening FUEL data base", e);
		}

	}

	/**	Load sample voxels from database
	 */
	protected void loadVoxelSample  () {


		try {

			sample = bdCommunicator.getShapeVoxels (sample, false);	//to get all shape info
			voxelMap = sample.getVoxels();

			for (Iterator iter = voxelMap.keySet().iterator(); iter.hasNext ();) {

				Object cle = iter.next();
				FmDBVoxel voxel = voxelMap.get(cle);
				int i = iSampleCopy;
				int k = voxel.getK();
				int j = jSampleCopy;


				//if no type, it is not a mesured plant with cube method !!!
				if (voxel.getVoxelType() != null) {

					FmVoxelType type = voxel.getVoxelType();
					int indexType = type.getTypeIndex();
					if (indexType >= 0) {

						storeVoxelType (indexType, type, voxel);

						if ((i<iNbVoxels) && (j<jNbVoxels)) {
							gridVoxelRef[i][j][k] = null;
							gridVoxelValues[i][j][k] = indexType;
							ancVoxelValues[i][j][k]  = -1;
						}
					}
				}
			}

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPlant2DEditor.loadSample() ", "error while opening FUEL data base", e);
		}
	}
	/**
	* Store voxel type in a tables
	*/
	protected void storeVoxelType (int indexType, FmVoxelType type, FmDBVoxel voxel) {
		typeIds[indexType] = type.getDBId();
		types[indexType] = type;
		typeVoxel.addObject (type, voxel);

		if ((indexType == 1) &&  (!isTop)) {
			isTop = true;
		}
		if ((indexType == 2) &&  (!isCenter)){
			isCenter = true;
		}
		if ((indexType == 3) &&  (!isBottom)) {
			isBottom = true;
		}


	}

	/** Return the new shape id
	 */
	public long getShapeId () {
		return shapeId;
	}
	/**	Return the first voxel for this voxel type
	 */
	protected FmDBVoxel getFirstVoxel (FmVoxelType type) {

		HashSet voxelList = (HashSet) typeVoxel.getObjects(type);
		if (voxelList.size() > 0) {
			Object [] voxelArray = voxelList.toArray();
			return (FmDBVoxel) voxelArray[0];
		}
		else return null;
	}

	/**	Return first the voxel for this voxel type (except if voxel.getDBId()=id)
	 */
	protected FmDBVoxel getFirstVoxel (FmVoxelType type, long id) {

		HashSet voxelList = (HashSet) typeVoxel.getObjects(type);
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
	* grid dimension initialization
	*/
	protected void setGridSize () {

		iSizeVoxels = (int) (shape.getVoxelXSize () * 100);	//convert m to cm
		jSizeVoxels = (int) (shape.getVoxelYSize () * 100);
		kSizeVoxels = (int) (shape.getVoxelZSize () * 100);

		kNbVoxels = (int) ((shape.getZMax () * 100) / kSizeVoxels);
		iNbVoxels = (int) ((shape.getXMax () * 100) / iSizeVoxels);
		jNbVoxels = 1;

		iSampleCopy = iNbVoxels/2;
		jSampleCopy = 0;

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

		//+/- left or right
		if ((left) || (right)) {

			if ((nbPlusLeft >=0) && (nbPlusRight >= 0) ) {

				int[][][] savGridVoxelValues = new int[iNbVoxels][jNbVoxels][kNbVoxels];
				int[][][] savAncVoxelValues  = new int[iNbVoxels][jNbVoxels][kNbVoxels];
				FmDBVoxel[][][] savGridVoxelRef    = new FmDBVoxel [iNbVoxels][jNbVoxels][kNbVoxels];


				for (int i=0;i<iNbVoxels;i++) {
					for (int j=0;j<jNbVoxels;j++) {
						for (int k=0;k<kNbVoxels;k++) {
							savGridVoxelValues[i][j][k] = gridVoxelValues[i][j][k];
							savAncVoxelValues[i][j][k] = ancVoxelValues[i][j][k];
							savGridVoxelRef[i][j][k] = gridVoxelRef[i][j][k];
						}
					}
				}

				//NEW GRID CREATION
				int newi;
				if (plus)  newi = iNbVoxels + 1;
				else newi = iNbVoxels - 1;

				gridVoxelValues = new int[newi][jNbVoxels][kNbVoxels];
				ancVoxelValues  = new int[newi][jNbVoxels][kNbVoxels];
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
								ancVoxelValues[i][j][k] = savAncVoxelValues[i][j][k];
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
								ancVoxelValues[i][j][k] = savAncVoxelValues[i][j][k];
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
				changeGridHeigth (plus, bottom);

			}

		}
		if (nbPlusLeft < 0)   nbPlusLeft=0;
		if (nbPlusRight < 0)  nbPlusRight=0;
		if (nbPlusTop < 0) 	  nbPlusTop=0;
		if (nbPlusBottom < 0) nbPlusBottom=0;


	}

	protected void changeGridHeigth (boolean plus, boolean bottom) {

		int[][][] savGridVoxelValues = new int[iNbVoxels][jNbVoxels][kNbVoxels];
		int[][][] savAncVoxelValues  = new int[iNbVoxels][jNbVoxels][kNbVoxels];
		FmDBVoxel[][][] savGridVoxelRef    = new FmDBVoxel [iNbVoxels][jNbVoxels][kNbVoxels];


		for (int i=0;i<iNbVoxels;i++) {
			for (int j=0;j<jNbVoxels;j++) {
				for (int k=0;k<kNbVoxels;k++) {
					savGridVoxelValues[i][j][k] = gridVoxelValues[i][j][k];
					savAncVoxelValues[i][j][k] = ancVoxelValues[i][j][k];
					savGridVoxelRef[i][j][k] = gridVoxelRef[i][j][k];
				}
			}
		}

		//NEW GRID CREATION
		int newk;
		if (plus)  newk = kNbVoxels + 1;
		else newk = kNbVoxels - 1;

		gridVoxelValues = new int[iNbVoxels][jNbVoxels][newk];
		ancVoxelValues  = new int[iNbVoxels][jNbVoxels][newk];
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
						ancVoxelValues[i][j][k] = savAncVoxelValues[i][j][k];
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
						ancVoxelValues[i][j][k] = savAncVoxelValues[i][j][k];
						gridVoxelRef[i][j][k] = savGridVoxelRef[i][j][k];
					}
				}
			}

		}

		kNbVoxels = newk;
	}

	/**
	* calculate total biomass for the shape
	*/
	protected double calculateBiomass () {

		double total = 0.0;
		double [][][] biomass = voxelParticlePanel.getNewBiomass();
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
		return total;
	}

	/**	Retrieve external events
	 */
	public void somethingHappened (ListenedTo l, Object param) {
		if (l instanceof FmColorLegend) {
			Integer color = (Integer) param;
			typeSelected = color;
			voxelParticlePanel.changeSelect (typeSelected, false);
			voxelPanel2D.setColor (typeSelected);
		}
		if (l instanceof FmVoxel2DPanel) {
			if (Integer.class.isInstance (param)) {
				Integer type = (Integer) param;
				typeSelected = type;
				voxelParticlePanel.changeSelect (typeSelected, false);
			}
			else {

				if (String.class.isInstance (param)) {
					addHistory();
					String action = (String) param;
					changeGridSize (action);
					voxelPanel2D.setGridVoxelValues (gridVoxelValues, iNbVoxels, jNbVoxels, kNbVoxels);
				}
				else if (Integer.class.isInstance (param)) {}
				else {
					addHistory();
					calculatedBiomass = calculateBiomass ();
					calcul.setText (""+calculatedBiomass);
					controlPanel.repaint ();
					this.repaint ();
				}
			}
		}
	}
	/**	To store history used for UNDO-REDO.
	*/
	protected void addHistory () {

		int[][][] ancGridValues = new int[iNbVoxels][jNbVoxels][kNbVoxels];

		for (int i=0; i<iNbVoxels ; i++) {
			for (int j=0; j<jNbVoxels ; j++) {
				for (int k=0; k<kNbVoxels ; k++) {
					ancGridValues[i][j][k] =  gridVoxelValues[i][j][k];
				}
			}
		}

		selectionHistory.add (ancGridValues);	// for undo /redo - fc - 2.3.2005

		firstHistory = true;
	}

	/**	Called on ctrl-Z. UNDO
	*/
	@Override
	protected void ctrlZPressed () {

		int[][][] ancGridValues;
		if (firstHistory) ancGridValues = (int[][][]) selectionHistory.current ();
		else ancGridValues = (int[][][]) selectionHistory.back ();

		if (ancGridValues != null) {

			iNbVoxels = ancGridValues.length ;
			jNbVoxels = ancGridValues[0].length;
			kNbVoxels = ancGridValues[0][0].length;

			gridVoxelValues = ancGridValues;
			voxelPanel2D.setGridVoxelValues (gridVoxelValues, iNbVoxels, jNbVoxels, kNbVoxels);

			calculatedBiomass = calculateBiomass ();
			calcul.setText (""+calculatedBiomass);
			controlPanel.repaint ();
			this.repaint ();
			firstHistory = false;
		}


	}


	/**	Called on ctrl-Y. REDO
	*/
	@Override
	protected void ctrlYPressed () {

		int[][][] ancGridValues;
		ancGridValues = (int[][][]) selectionHistory.next ();
		if (ancGridValues != null) {

			iNbVoxels = ancGridValues.length ;
			jNbVoxels = ancGridValues[0].length;
			kNbVoxels = ancGridValues[0][0].length;

			gridVoxelValues = ancGridValues;
			voxelPanel2D.setGridVoxelValues (gridVoxelValues, iNbVoxels, jNbVoxels, kNbVoxels);

			calculatedBiomass = calculateBiomass ();
			calcul.setText (""+calculatedBiomass);
			controlPanel.repaint ();
			this.repaint ();
		}
	}
}
