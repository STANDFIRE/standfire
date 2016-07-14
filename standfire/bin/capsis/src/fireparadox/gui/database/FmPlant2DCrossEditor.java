package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Translator;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBShape;
import fireparadox.model.database.FmDBVoxel;
/**
 * FiPlant2DCrossEditor : fuel voxel 2D in cross design
 *
 * @author I. Lecomte - March 2008
 */
public class FmPlant2DCrossEditor extends FmPlant2DEditor {


	//Split pane for panel 2D
	protected JSplitPane splitPanel;
	protected FmVoxel2DPanel crossPanel2D;
	protected int nbPlusCrossLeft, nbPlusCrossRight;


	/**	Constructor for 2D.
	 */
	 public FmPlant2DCrossEditor  (FmModel _model,  FmDBShape _sample, FmDBShape _shape) {

		super (_model, _sample,  _shape, true) ;

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
		String shapeKind = shape.getShapeKind();

		voxelPanel2D = new FmVoxel2DPanel (gridVoxelValues, iNbVoxels, jNbVoxels, kNbVoxels,
											iSizeVoxels, jSizeVoxels, kSizeVoxels,
											colorMap, true, 3, shapeKind);
		voxelPanel2D.addListener (this);
		voxelPanel2D.changeSelect (iSelected, jSelected, kSelected);


		//show the right depth for 2D
		int depth = jNbVoxels / 2;
		voxelPanel2D.setDepth (depth);


		crossPanel2D = new FmVoxel2DPanel (gridVoxelValues, iNbVoxels, jNbVoxels, kNbVoxels,
											iSizeVoxels, jSizeVoxels, kSizeVoxels,
											colorMap, true, 3, shapeKind);

		crossPanel2D.addListener (this);
		crossPanel2D.changeSelect (iSelected, jSelected, kSelected);


		//show the right depth for 2D
		crossPanel2D.setView (2);
		depth = iNbVoxels / 2;
		crossPanel2D.setDepth (depth);


		splitPanel = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, voxelPanel2D, crossPanel2D);
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


		save = new JButton (Translator.swap ("FiPlant2DEditor.validate"));
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

		controlPanel.add (new JLabel (Translator.swap ("FiPlant2DEditor.measuredBiomass")));
		controlPanel.add (measure);
		controlPanel.add (new JLabel (Translator.swap ("FiPlant2DEditor.calculatedBiomass")));
		controlPanel.add (calcul);

		measure.setEnabled(false);
		calcul.setEnabled(false);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (splitPanel, BorderLayout.CENTER);
		getContentPane ().add (legend, BorderLayout.EAST);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		setTitle (Translator.swap ("FiPlant2DCrossEditor.title"));

		this.setSize (new Dimension(800,600));

		setModal (true);
	}

	/**
	* changing cross grid dimension
	*/
	protected void changeCrossGridSize (String action) {


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
			if (plus) nbPlusCrossLeft++;
			else nbPlusCrossLeft--;
		} else if (right) {
			if (plus) nbPlusCrossRight++;
			else nbPlusCrossRight--;
		} else if (top) {
			if (plus) nbPlusTop++;
			else nbPlusTop--;
		} else if (bottom) {
			if (plus) nbPlusBottom++;
			else nbPlusBottom--;
		}

		//+/- left or right
		if ((left) || (right)) {


			if ((nbPlusCrossLeft >=0) && (nbPlusCrossRight >= 0)) {

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
				int newj;
				if (plus)  newj = jNbVoxels + 1;
				else newj = jNbVoxels - 1;

				gridVoxelValues = new int[iNbVoxels][newj][kNbVoxels];
				ancVoxelValues  = new int[iNbVoxels][newj][kNbVoxels];
				gridVoxelRef    = new FmDBVoxel [iNbVoxels][newj][kNbVoxels];

				//initialisation
				for (int i=0;i<iNbVoxels;i++) {
					for (int j=0;j<newj;j++) {
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
								int copyJ = j;
								if (left) {
									 copyJ++;
								}


								gridVoxelValues[i][copyJ][k] = savGridVoxelValues[i][j][k];
								ancVoxelValues[i][j][k] = savAncVoxelValues[i][j][k];
								gridVoxelRef[i][j][k] = savGridVoxelRef[i][j][k];
							}
						}
					}
				}
				//recopy -moins
				else {
					for (int i=0;i<iNbVoxels;i++) {
						for (int j=0;j<newj;j++) {
							for (int k=0;k<kNbVoxels;k++) {

								int copyJ = j;
								if (left) copyJ++;

								gridVoxelValues[i][j][k] = savGridVoxelValues[i][copyJ][k];
								ancVoxelValues[i][j][k] = savAncVoxelValues[i][j][k];
								gridVoxelRef[i][j][k] = savGridVoxelRef[i][j][k];
							}
						}
					}

				}

				jNbVoxels = newj;

			}
		}
		//+/- top or bottom
		else  if ((top) || (bottom)) {

			if ((nbPlusTop >= 0) && (nbPlusBottom >= 0) ) {

				changeGridHeigth (plus, bottom);
			}

		}
		if (nbPlusCrossLeft < 0)  nbPlusCrossLeft=0;
		if (nbPlusCrossRight < 0) nbPlusCrossRight=0;
		if (nbPlusTop < 0) 	  nbPlusTop=0;
		if (nbPlusBottom < 0) nbPlusBottom=0;

	}
	/**
	* grid dimension initialization
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

	/**	Retrieve external events
	 */
	@Override
	public void somethingHappened (ListenedTo l, Object param) {
		if (l instanceof FmColorLegend) {
			Integer color = (Integer) param;
			typeSelected = color;
			voxelParticlePanel.changeSelect (typeSelected, false);
			voxelPanel2D.setColor (typeSelected);
			crossPanel2D.setColor (typeSelected);
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
					if (l==voxelPanel2D) {
						changeGridSize (action);
					}
					else {
						changeCrossGridSize (action);
					}
					voxelPanel2D.setGridVoxelValues (gridVoxelValues, iNbVoxels, jNbVoxels, kNbVoxels);
					crossPanel2D.setGridVoxelValues (gridVoxelValues, iNbVoxels, jNbVoxels, kNbVoxels);

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
			crossPanel2D.setGridVoxelValues (gridVoxelValues, iNbVoxels, jNbVoxels, kNbVoxels);

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
			crossPanel2D.setGridVoxelValues (gridVoxelValues, iNbVoxels, jNbVoxels, kNbVoxels);

			calculatedBiomass = calculateBiomass ();
			calcul.setText (""+calculatedBiomass);
			controlPanel.repaint ();
			this.repaint ();
		}
	}

}
