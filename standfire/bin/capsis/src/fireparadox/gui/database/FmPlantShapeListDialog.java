package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBCommunicator;
import fireparadox.model.database.FmDBPlant;
import fireparadox.model.database.FmDBShape;
import fireparadox.model.database.FmDBTeam;

/**	FiPlantShapeListDialog : Shape list for a plant
*
*	@author I. Lecomte - march 2008
*/
public class FmPlantShapeListDialog extends AmapDialog implements ActionListener {

	private FmModel model;
	private FmDBCommunicator bdCommunicator;		//to read database
	private int rightLevel;							//user right management
	private FmDBTeam teamLogged;					//logged team
	private FmDBTeam teamPlant;						//plant team

	private FmDBPlant plant;
	private FmDBShape shape2D;						//at least one shape2D for this plant
	private FmDBShape shape3D;						//at least one shape3D for this plant
	private FmDBShape sample;						//at least one sample for this plant
	private FmDBShape sampleEdge;					//at least one sample edge for this layer

	private long plantId;
	private int fuelType;
	private String specieName;
	private String origin;

	//to store shapes as result table
	private LinkedHashMap<Long, FmDBShape>  shapeMap;
    private JTable resultTable;
    private FmPlantShapeTableModel tableModel;
	private int delete = 0;

	private JButton close;
	private JButton help;
	private JButton add;
	private JButton modify;
	private JButton supress;

	/**	Constructor.
	*/
	public FmPlantShapeListDialog (FmModel _model, FmDBPlant _plant, int _fuelType) {

		super ();
		model = _model;
		rightLevel = _model.getRightLevel();
		teamLogged = _model.getTeamLogged();
		fuelType = _fuelType;
		plant = _plant;
		plantId = plant.getPlantId ();
		teamPlant = plant.getTeam();
		origin = plant.getOrigin();

		loadAllData();		//load data from database

		createUI ();
		pack ();
		show ();
	}

	/**	Actions on the buttons
	*/
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (close)) {
			closeAction ();
		} else if (evt.getSource ().equals (add)) {
			addShape ();
		} else if (evt.getSource ().equals (modify)) {
			modifyShape ();
		} else if (evt.getSource ().equals (supress)) {
			desactivateShape ();

		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**	Close was hit
	*/
	private void closeAction () {
		setVisible (false);
	}


	/**	SHAPE adding
	*/
	private void addShape () {
		if (!plant.isDeleted()) {
			if (!plant.isValidated()) {
				if (fuelType == 1) {
					if (origin.equals("Measured")) {
						FmShapeMeasuredDialog dialog = new FmShapeMeasuredDialog (model, plant, sample, shape2D);
					}
					else {
						FmShapeVirtualDialog dialog = new FmShapeVirtualDialog (model, plant, sample);
					}
				}
				if (fuelType == 2) {
					if (origin.equals("Measured")) {
						FmLayerMeasuredDialog dialog = new FmLayerMeasuredDialog (model, plant, sample, sampleEdge);
					}
					else {
						FmLayerVirtualDialog dialog = new FmLayerVirtualDialog (model, plant, sample, sampleEdge);

					}
				}

				//relaod shapes liste
				loadPlantShapes ();
				repaint ();
			}
			else {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantShapeListDialog.validatedPlant"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			}
		}
		else {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantShapeListDialog.deletedPlant"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
		}

	}

	/**	SHAPE modification
	*/
	private void modifyShape () {
		int [] selRow  = this.getSelectedRows ();
		if (selRow.length > 0) {
			int selectedRow = selRow[0];
			FmDBShape fuelSelected = this.getShape (selectedRow);
			int typeSelected = fuelSelected.getFuelType();
			String shapeKind = fuelSelected.getShapeKind();
			boolean isCubeMethod = fuelSelected.isCubeMethod();
			boolean deleted = this.getDeleted (selectedRow);

			//team restinction
			if ( (rightLevel < 9) && ( !teamPlant.getTeamCode().equals(teamLogged.getTeamCode()))) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantShapeListDialog.noRightForThisAction"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			}
			else  {
				if (isCubeMethod) {
					if (typeSelected == 1) {

						if  (shapeKind.equals("XZ")) {
							FmPlant2DEditor blockEntry = new FmPlant2DEditor (model, sample, fuelSelected, true);
						}
						if  (shapeKind.equals("XZ_YZ")) {
							FmPlant2DCrossEditor dialog = new FmPlant2DCrossEditor (model, sample, fuelSelected);
						}
						if  (shapeKind.equals("XYZ")) {
							FmPlant3DEditor dialog = new FmPlant3DEditor (model, sample, fuelSelected, null, 0);
						}

					}
					if (typeSelected == 2) {
						FmLayer2DEditor dialog = new FmLayer2DEditor (model, fuelSelected, sample, sampleEdge);
					}
					if (typeSelected == 3) {
						FmSample2DEditor dialog = new FmSample2DEditor (model, sample, -1, -1, -1);
					}
					if (typeSelected == 4) {
						FmSample2DEditor dialog = new FmSample2DEditor (model, sample, -1, -1, -1);
					}
					if (typeSelected == 5) {
						FmSample2DEditor dialog = new FmSample2DEditor (model, sampleEdge, -1, -1, -1);
				}
				}
				else {
					if (typeSelected == 1) {
						FmPlant3DCageEditor dialog = new FmPlant3DCageEditor (model, fuelSelected, sample);
					}
					else if (typeSelected == 2) {
						FmLayer2DCageEditor dialog = new FmLayer2DCageEditor (model, fuelSelected);
					}
					else {
						FmSample3DCageEditor dialog = new FmSample3DCageEditor (model,  fuelSelected);
					}
				}
			}

			//relaod shapes liste
			loadPlantShapes ();
			repaint ();


		}
	}



	/**	SHAPE desactivation
	*/
	private void desactivateShape() {
		int [] selRow  = this.getSelectedRows ();
		if (selRow.length > 0) {
			boolean deleted = true; 		//action is deleting
			int selectedRow = selRow[0];
			FmDBShape fuelSelected = this.getShape (selectedRow);

			//team restinction
			if ((rightLevel < 9) && (!teamPlant.getTeamCode().equals(teamLogged.getTeamCode()))) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiPlantShapeListDialog.noRightForThisAction"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			}

			else  {

				FmShapeDesactivateDialog dialog = new FmShapeDesactivateDialog (model, fuelSelected);

				//refreshing fuel list
				FmDBShape newShape = dialog.getNewShape ();
				if (newShape != null) {
					this.removeShapeRow (fuelSelected);
					this.addShapeRow (newShape);
					repaint();
				}
			}
		}
	}


	/**	Initialize the GUI.
	*/
	private void createUI () {

		FmPlantInfoPanel fuelPanel = new FmPlantInfoPanel (plant);


		//Research Form
		// 2.1 Result Table
		JPanel tablePanel = new JPanel (new BorderLayout ());
		ColumnPanel colTable = new ColumnPanel ();
		resultTable = new JTable(tableModel);
		JScrollPane listSP = new JScrollPane(resultTable);
		listSP.setPreferredSize (new Dimension (600,240));
		colTable.add (listSP);

		tablePanel.add (colTable);


		// Control panel
		JPanel controlPanel = new JPanel (new FlowLayout (FlowLayout.CENTER));


		if (rightLevel > 1) {
			if ((rightLevel == 9) || (teamPlant.getTeamCode().equals(teamLogged.getTeamCode()))) {
				add = new JButton (Translator.swap ("FiPlantShapeListDialog.add"));
				supress = new JButton (Translator.swap ("FiPlantShapeListDialog.supress"));

				if ((plant.isDeleted()) || (plant.isValidated())) {
					add.setEnabled(false);
					supress.setEnabled(false);
				}
				else {
					supress.addActionListener (this);
					if (fuelType == 1) {
						if (shape3D == null)
							add.addActionListener (this);
						else add.setEnabled(false);
					}
					else {
						if (shape2D == null)
							add.addActionListener (this);
						else add.setEnabled(false);
					}
				}

				modify = new JButton (Translator.swap ("FiPlantShapeListDialog.modify"));
				modify.addActionListener (this);

				controlPanel.add (add);
				controlPanel.add (modify);
				controlPanel.add (supress);


			}
		}

		close = new JButton (Translator.swap ("Shared.close"));
		help = new JButton (Translator.swap ("Shared.help"));
		controlPanel.add (close);
		controlPanel.add (help);
		close.addActionListener (this);
		help.addActionListener (this);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (fuelPanel, BorderLayout.NORTH);
		getContentPane ().add (tablePanel, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		setTitle (Translator.swap ("FiPlantShapeListDialog.title"));

		setModal (true);
	}

	/**
	* Load ALL DATA from database
	*/
	public void loadAllData () {

		try {

			tableModel = new  FmPlantShapeTableModel (rightLevel);
			shapeMap = new LinkedHashMap<Long, FmDBShape> ();

			//visitors can see only validated and none deleted objects
			int delete = 0;
			if (rightLevel == 1) {
				delete=1;
			}

			bdCommunicator = model.getBDCommunicator ();

			loadPlantShapes ();			//Load shapes for the plant

		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPlantShapeListDialog ()", "error while opening data base", e);
		}

	}

	/**
	* Load ALL SHAPES for a PLANT from database
	*/
	public void loadPlantShapes () {

		try {

			tableModel.clear();

			shapeMap = bdCommunicator.getPlantShapes (plant,  delete);
			for (Iterator i = shapeMap.keySet().iterator(); i.hasNext ();) {
				Object cle = i.next();
				FmDBShape f = shapeMap.get(cle);

				if (!f.isDeleted()) {

					//sample already exist
					if ((f.getFuelType() == 3) || (f.getFuelType() == 4)) {
						sample = f;
					}
					else if (f.getFuelType() == 5) {
						sampleEdge = f;
					}
					//2D shape already exist
					else if (f.getFuelType() == 1) {
						if (f.getShapeKind().equals("XZ")) {
							if (shape2D == null) {
								shape2D = f;
							}
						}
						if (f.getShapeKind().equals("XZ_YZ")) {
							shape2D = f;
						}
						if (f.getShapeKind().equals("XYZ")) {
							shape3D = f;
						}
					}
					else if (f.getFuelType() == 2) {
						if (shape2D == null) {
							shape2D = f;
						}
					}
				}
				tableModel.addShape (f);


			}
		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPlantShapeListDialog ()", "error while loading plant shapes", e);
		}
	}

    /** return the selected rows in the displayed table
     */
	public int[] getSelectedRows () {
		return resultTable.getSelectedRows();
	}
	/** Add a new shape in the displayed table
	 */
	public void addShapeRow (FmDBShape shape) {
		shapeMap.put (shape.getShapeId(), shape);
		tableModel.addShape (shape);
	}
	/** remove a shape in the displayed table
	 */
	public void removeShapeRow (FmDBShape shape) {
		shapeMap.remove (shape);
		tableModel.removeShape (shape);
	}
    /** return the shape store at this index in the displayed table
     */
	public FmDBShape getShape (int index) {
		return tableModel.getShape(index);
	}

    /** return the shape id store at this index in the displayed table
     */
	public long getShapeId (int index) {
		FmDBShape shape =  tableModel.getShape (index);
		return shape.getShapeId ();
	}

    /** return if the shape is deleted or not at this index in the displayed table
     */
	public boolean getDeleted (int index) {
		FmDBShape shape =  tableModel.getShape (index);
		return shape.isDeleted();

	}
    /** return if the shape is deleted or not at this index in the displayed table
     */
	public boolean getValidated (int index) {
		FmDBShape shape =  tableModel.getShape (index);
		return shape.isValidated ();

	}
}

