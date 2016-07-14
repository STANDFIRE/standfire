package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBCommunicator;
import fireparadox.model.database.FmDBPlant;
import fireparadox.model.database.FmDBShape;
import fireparadox.model.database.FmDBUpdator;


/**	FiPlantShapeCopyDialog : Shape copy
*
*	@author I. Lecomte - december 2009
*/
public class FmPlantShapeCopyDialog extends AmapDialog implements ActionListener {

	private FmModel model;
	private FmDBCommunicator bdCommunicator;			//to read database
	private FmDBUpdator bdUpdator;				//to update database

	private FmDBPlant plant;
	private FmDBPlant newPlant;
	private FmDBShape sample, sampleEdge;
	private FmDBShape shape2D, shape3D;

	private long plantId;
	private long newSampleId, newSampleEdgeId,newShapeId;

	private JButton close;
	private JButton help;
	private JButton valid;


	/**	Constructor.
	*/
	public FmPlantShapeCopyDialog (FmModel _model, FmDBPlant _newPlant, FmDBPlant _plant)  {

		super ();
		model = _model;
		plant = _plant;
		newPlant = _newPlant;
		plantId = newPlant.getPlantId();

		loadPlantShapes();		//load data from database

		createUI ();
		pack ();
		show ();
	}

	/**	Actions on the buttons
	*/
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (close)) {
			closeAction ();

		} else if (evt.getSource ().equals (valid)) {
			validAction ();

		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**	Close was hit
	*/
	private void closeAction () {
		setVisible (false);
	}

	/**	Validation
	*/
	private void validAction () {

		bdUpdator = model.getBDUpdator ();
		newShapeId = -1;
		newSampleId = -1;
		newSampleEdgeId = -1;


		try {
			//PLANT CREATION IN DATABASE
			if (plantId < 0) {
				String newComment = plant.getComment();
				System.out.println("newComment="+newComment);
				newComment = newComment+" PLANT COPIED FROM ID="+plant.getPlantId()+
												" SITE="+plant.getSite()+
												" HEIGHT="+plant.getHeight();
													System.out.println("newComment="+newComment);
				newPlant.setComment(newComment);


				plantId = bdUpdator.createPlant (newPlant, 1);
				if (plantId > 0) {
					newPlant.setPlantId (plantId);

					//SAMPLE COPY
					if ((sample != null) || (sampleEdge != null))  {
						if (sample != null)  {
							newSampleId = bdUpdator.copyShape (plantId, sample, newPlant.getOrigin());
						}
						if (sampleEdge != null) {
							newSampleEdgeId = bdUpdator.copyShape (plantId, sampleEdge, newPlant.getOrigin());
						}

						//SHAPE COPY IF NOT CUBE METHOD !
						if ((shape3D != null) && (!shape3D.isCubeMethod())) {
							newShapeId = bdUpdator.copyShape (plantId, shape3D, newPlant.getOrigin());
						}
						else if ((shape2D != null) && (!shape2D.isCubeMethod())) {
							newShapeId = bdUpdator.copyShape (plantId, shape2D, newPlant.getOrigin());
						}

					}
					//SHAPE COPY IF NOT CUBE METHOD !
					else {
						if (shape3D != null) {
							newShapeId = bdUpdator.copyShape (plantId, shape3D, newPlant.getOrigin());
						}
						else if (shape2D != null) {
							newShapeId = bdUpdator.copyShape (plantId, shape2D, newPlant.getOrigin());
						}
					}
				}
			}


		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPlantShapeCopyDialog.validateAction ()", "error while UPDATING SHAPE data base", e);
		}
		setValidDialog (true);

	}


	/**	Initialize the GUI.
	*/
	private void createUI () {



		/*********** Fuel info and color legend panel **************/
		JPanel verifPanel = new JPanel (new FlowLayout (FlowLayout.CENTER));

		LinePanel info = new LinePanel ();

		FmPlantInfoPanel plantInfoPanel = new FmPlantInfoPanel (newPlant);
		info.add (plantInfoPanel);

		//SAMPLE INFO
		if ((sample != null) || (sampleEdge != null))  {
			if (sample != null) {
				FmShapeInfoPanel sampleInfoPanel = new FmShapeInfoPanel (sample);
				info.add (sampleInfoPanel);
			}
			if (sampleEdge != null) {
					FmShapeInfoPanel sampleEdgeInfoPanel = new FmShapeInfoPanel (sampleEdge);
					info.add (sampleEdgeInfoPanel);
			}

			if ((shape3D != null) && (!shape3D.isCubeMethod())) {
				FmShapeInfoPanel shapeInfoPanel = new FmShapeInfoPanel (shape3D);
				info.add (shapeInfoPanel);
			}
			else if ((shape2D != null) && (!shape2D.isCubeMethod())) {
				FmShapeInfoPanel shapeInfoPanel = new FmShapeInfoPanel (shape2D);
				info.add (shapeInfoPanel);
			}

		}
		//SHAPE INFO
		else {
			if (shape3D != null) {
				FmShapeInfoPanel shapeInfoPanel = new FmShapeInfoPanel (shape3D);
				info.add (shapeInfoPanel);
			}
			else if (shape2D != null) {
				FmShapeInfoPanel shapeInfoPanel = new FmShapeInfoPanel (shape2D);
				info.add (shapeInfoPanel);
			}
		}

		verifPanel.add (info);



		// Choice panel

		Box box = Box.createVerticalBox ();
		JLabel texte = new JLabel(Translator.swap ("FiPlantShapeCopyDialog.label"));
		box.add (texte);

		verifPanel.add (box);

		// Control panel
		JPanel controlPanel = new JPanel (new FlowLayout (FlowLayout.CENTER));
		valid = new JButton (Translator.swap ("FiPlantShapeCopyDialog.valid"));
		close = new JButton (Translator.swap ("Shared.close"));
		help = new JButton (Translator.swap ("Shared.help"));
		controlPanel.add (valid);
		controlPanel.add (close);
		controlPanel.add (help);


		valid.addActionListener (this);
		close.addActionListener (this);
		help.addActionListener (this);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (verifPanel, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		setTitle (Translator.swap ("FiPlantShapeCopyDialog.title"));
		setPreferredSize (new Dimension(450,200));

		setModal (true);
	}

/**
	* Load ALL SHAPES for a PLANT from database
	*/
	public void loadPlantShapes () {

		try {
			bdCommunicator = model.getBDCommunicator ();

			LinkedHashMap<Long, FmDBShape> shapeMap = new LinkedHashMap<Long, FmDBShape> ();
			shapeMap = bdCommunicator.getPlantShapes (plant,  0);
			for (Iterator i = shapeMap.keySet().iterator(); i.hasNext ();) {
				Object cle = i.next();
				FmDBShape f = shapeMap.get(cle);

				if (!f.isDeleted()) {

					//sample already exist
					if ((f.getFuelType() == 3) || (f.getFuelType() == 4)) {
						sample = f;
							System.out.println("sample="+sample);
					}
					else if (f.getFuelType() == 5) {
						sampleEdge = f;
						System.out.println("sampleEdge="+sampleEdge);
					}
					//2D shape already exist
					else if (f.getFuelType() == 1) {
						if (f.getShapeKind().equals("XZ")) {
							if (shape2D == null) {
								shape2D = f;
								System.out.println("shape2D="+shape2D);
							}
						}
						if (f.getShapeKind().equals("XZ_YZ")) {
							shape2D = f;
							System.out.println("shape22D="+shape2D);
						}
						if (f.getShapeKind().equals("XYZ")) {
							shape3D = f;
							System.out.println("shape3D="+shape3D);
						}
					}
					else if (f.getFuelType() == 2) {
						if (shape2D == null) {
							shape2D = f;
							System.out.println("shape2D="+shape2D);
						}
					}
				}
			}
		} catch (Exception e) {
			Log.println (Log.ERROR, "FiPlantShapeListDialog ()", "error while loading plant shapes", e);
		}
	}
	public long getShapeId() {return newShapeId;}
	public long getSampleId() {return newSampleId;}
	public long getSampleEdgeId() {return newSampleEdgeId;}
}

