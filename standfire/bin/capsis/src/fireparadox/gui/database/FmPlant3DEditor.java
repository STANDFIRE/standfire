package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBShape;
import fireparadox.model.database.FmDBVoxel;
import fireparadox.model.database.FmVoxelType;


/**
 * FiPlant3DEditor : fuel unique crown 3D design
 *
 * @author I. Lecomte - October 2009
 */
public class FmPlant3DEditor extends FmPlant2DEditor implements  ActionListener, jeeb.lib.util.Listener {


	private FmDBShape shape2D;
	private int method;

	//Split pane for selector and panel 2D
	private JSplitPane splitPanel;
	private FmVoxelMatrixDepthSelector selector; //Selector panel


	/**	Constructor for 3D.
	 */
	public FmPlant3DEditor (FmModel _model, FmDBShape _sample, FmDBShape _shape, FmDBShape _shape2D, int _method) {

		super (_model, _sample,  _shape, false) ;

		shape2D = _shape2D;

		//2D shape already existe, voxels can be copied
		if ((shape2D != null) && (_method == 1)) {
			this.init();
		}
		//voxel will be copied from sample
		else super.init ();

	 }


	@Override
	public void init () {


		//Load shape voxels
		isVoxels = false;
		loadVoxels ();

		//if no voxels, load voxel from shape2D
		if ((!isVoxels) && (shape2D != null)) 	loadVoxel2D();


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

	/**	Initialize the GUI.
	 */
	@Override
	public void createUI () {

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


		/*********** VOXEL view and depth selector  **************/
		selector = new FmVoxelMatrixDepthSelector (
						gridVoxelValues,
						iNbVoxels,
						jNbVoxels,
						kNbVoxels,
						colorMap);
		selector.addListener (this);

		/*********** VOXEL 2D panel  **************/
		String shapeKind = shape.getShapeKind();
		voxelPanel2D = new FmVoxel2DPanel (gridVoxelValues, iNbVoxels, jNbVoxels, kNbVoxels,
											iSizeVoxels, jSizeVoxels, kSizeVoxels,
											colorMap, true, 3, shapeKind);
		voxelPanel2D.addListener (this);
		voxelPanel2D.changeSelect (iSelected, jSelected, kSelected);


		splitPanel = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, selector, voxelPanel2D);
		splitPanel.setResizeWeight(0.5);


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


		save = new JButton (Translator.swap ("FiPlant3DEditor.validate"));
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
		calcul = new JTextField (7);
		calcul.setText (""+calculatedBiomass);
		JTextField measure = new JTextField (7);
		measure.setText (""+measuredBiomass);
		measure.setEnabled(false);
		calcul.setEnabled(false);

		controlPanel.add (new JLabel (Translator.swap ("FiPlant3DEditor.measuredBiomass")));
		controlPanel.add (measure);
		controlPanel.add (new JLabel (Translator.swap ("FiPlant3DEditor.calculatedBiomass")));
		controlPanel.add (calcul);


		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (splitPanel, BorderLayout.CENTER);
		getContentPane ().add (legend, BorderLayout.EAST);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		setTitle (Translator.swap ("FiPlant3DEditor.title"));

		this.setSize (new Dimension(800,600));

		setModal (true);
	}

	/**
	* grid dimension for plant in 3D
	*/
	@Override
	protected void setGridSize () {

		iSizeVoxels = (int) (shape.getVoxelXSize() * 100);	//convert m to cm
		jSizeVoxels = (int) (shape.getVoxelYSize() * 100);
		kSizeVoxels = (int) (shape.getVoxelZSize() * 100);

		kNbVoxels = (int) ((shape.getZMax () * 100) / kSizeVoxels);
		iNbVoxels = (int) ((shape.getXMax () * 100) / iSizeVoxels);
		jNbVoxels = (int) ((shape.getYMax () * 100) / jSizeVoxels);

		iSampleCopy = iNbVoxels/2;
		jSampleCopy = jNbVoxels/2;

	}


	/**	Load sample voxels from database
	 */
	protected void loadVoxel2D  () {

		try {

			shape2D = bdCommunicator.getShapeVoxels (shape2D, false);	//to get all shape info
			voxelMap = shape2D.getVoxels();
			boolean is2d = false;
			if (shape2D.getShapeKind().equals("XZ")) is2d = true;

			for (Iterator iter = voxelMap.keySet().iterator(); iter.hasNext ();) {

				Object cle = iter.next();
				FmDBVoxel voxel = voxelMap.get(cle);
				int i = voxel.getI();
				int j = voxel.getJ();
				if (is2d) j = jSampleCopy;
				int k = voxel.getK();


				//if no type, it is not a mesured plant with cube method !!!
				if (voxel.getVoxelType() != null) {

					FmVoxelType type = voxel.getVoxelType();
					int indexType = type.getTypeIndex();
					if (indexType >= 0) {

						storeVoxelType (indexType, type, voxel);

						if ((i<iNbVoxels) && (j<jNbVoxels) && (k<kNbVoxels) ) {

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

	/**	Retrieve external events
	 */
	@Override
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
					selector.setMatrix (gridVoxelValues);
					selector.refresh();
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
				typeSelected = voxelPanel2D.getSelectedVoxelValue();
				voxelParticlePanel.changeSelect (typeSelected, false);
			}
			else {
				Integer depth = (Integer) param;
				voxelPanel2D.setDepth (depth);
				typeSelected = voxelPanel2D.getSelectedVoxelValue();
				voxelParticlePanel.changeSelect (typeSelected, false);
			}

		}

	}
}
