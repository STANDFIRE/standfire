package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBPlant;
import fireparadox.model.database.FmDBShape;


/**	FiShapeMeasuredDialog : Plant shape measured choice (2D, 2D*2, 3D)
*
*	@author I. Lecomte - march 2008
*/
public class FmShapeMeasuredDialog extends AmapDialog implements ActionListener {

	private FmModel model;

	private FmDBShape shape, shape2D;
	private FmDBShape sample;
	private FmDBShape sampleEdge;

	private long sampleId, shapeId;					//id in the database
	private FmDBPlant plant;						//id of the plant in the database
	private int shapeType;							//type of shape
	private String shapeKind;						//XZ, XZ_XZ, XYZ
	private boolean isCubeMethod; 					//true=cube


	//type of shape
	private ButtonGroup kindGroup;
	private JRadioButton radio2D;
	private JRadioButton radio2DB;
	private JRadioButton radio3D;
	private JRadioButton radio3DF2D;

	//sampling method
	private ButtonGroup methodGroup;
	private JRadioButton radioCube;
	private JRadioButton radioCage;
	private int method = 0;

	//voxel dimension
	private JTextField xVoxelField;
	private JTextField yVoxelField;
	private JTextField zVoxelField;
	private int xVoxelSize, yVoxelSize, zVoxelSize;
	private double xVoxel, yVoxel, zVoxel;

	//cage dimension
	private JTextField xSampleField;
	private JTextField ySampleField;
	private JTextField zSampleField;
	private int xSampleSize, ySampleSize, zSampleSize;
	private double xSample, ySample, zSample;

	//shape dimension
	private JTextField xShapeField;
	private JTextField yShapeField;
	private JTextField zShapeField;
	private int xShapeSize, yShapeSize, zShapeSize;
	private double xShape, yShape, zShape;

	private JButton save;
	private JButton close;
	private JButton help;

	/**	Constructor.
	*/
	public FmShapeMeasuredDialog (FmModel _model, FmDBPlant _plant, FmDBShape _sample, FmDBShape _shape2D) {

		super ();
		model = _model;
		plant = _plant;
		shape2D = _shape2D;
		sample = _sample;

		//default options
		shapeType = 1;
		shapeKind = "XYZ";
		isCubeMethod = true;

		//default shape size is tree size
		long val = Math.round(plant.getCrownDiameter() * 100);
		xShapeSize = (int) (val);
		val = Math.round(plant.getCrownPerpendicularDiameter() * 100);
		yShapeSize = (int) (val);
		val = Math.round(plant.getHeight() * 100);
		zShapeSize = (int) (val);

		//voxels size
		if (sample != null) {
			sampleId = sample.getShapeId();
			isCubeMethod = sample.isCubeMethod();

			val = Math.round(sample.getVoxelXSize() * 100);
			xVoxelSize = (int) (val);
			val = Math.round(sample.getVoxelYSize() * 100);
			yVoxelSize = (int) (val);
			val = Math.round(sample.getVoxelZSize() * 100);
			zVoxelSize = (int) (val);

			val = Math.round(sample.getZMax() * 100);
			zSampleSize = (int) (val);
			val = Math.round(sample.getXMax() * 100);
			xSampleSize = (int) (val);
			val = Math.round(sample.getYMax() * 100);
			ySampleSize = (int) (val);


		}
		else { //default
			xVoxelSize = 25;
			yVoxelSize = 25;
			zVoxelSize = 25;
			zSampleSize = zShapeSize;
			xSampleSize = 25;
			ySampleSize = 25;
		}


		createUI ();
		pack ();
		show ();

	}

	/**	Actions on the buttons
	*/
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (close)) {
			closeAction ();

		} else if (evt.getSource ().equals (save)) {
			validateAction ();

		} else if (evt.getSource ().equals (radio2D)) {
			setKind ("XZ");
			setMethod (0);
		} else if (evt.getSource ().equals (radio2DB)) {
			setKind ("XZ_YZ");
			setMethod (0);
		} else if (evt.getSource ().equals (radio3D)) {
			setKind ("XYZ");
			setMethod (0);
		} else if (evt.getSource ().equals (radio3DF2D)) {
			setKind ("XYZ");
			setMethod (1);

		} else if (evt.getSource ().equals (radioCube)) {
			setCube (true);
		} else if (evt.getSource ().equals (radioCage)) {
			setCube (false);


		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**	Kind of shape choice modification
	*/
	private void setKind (String kind) {
		shapeKind = kind;

		if (kind.equals("XZ")) {
			yShapeField.setEnabled (false);
		}
		else {
			yShapeField.setEnabled (true);
		}

	}

	/**	Protocole method choice modification
	*/
	private void setCube (boolean cube) {
		isCubeMethod = cube;

		if (isCubeMethod) {
			ySampleField.setEnabled (false);
			xSampleField.setEnabled (false);
			radio2D.setEnabled (true);
			radio2DB.setEnabled (true);
			repaint();

		}
		else { 	//if cage method, 3d only
			if (sample == null) {
				ySampleField.setEnabled (true);
				xSampleField.setEnabled (true);
			}
			shapeKind = "XYZ";
			radio2D.setEnabled (false);
			radio2DB.setEnabled (false);
			kindGroup.setSelected (radio3D.getModel (), true);
			repaint();
		}
	}
	/**	Method for 2D to 3D
	*/
	private void setMethod (int met) {
		method = met;

	}


	/**	Close was hit
	*/
	private void closeAction () {
		setVisible (false);
	}

	/**	Control before validation
	 */
	private boolean controlValues ()   {

		//VOXEL DIMENSION
		if (sample == null) {

			if (Check.isEmpty (zVoxelField.getText ()) ) zVoxelField.setText("0");
			if (!Check.isInt (zVoxelField.getText ())) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.zVoxelSizeIsNotInt"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
			}
			zVoxelSize = Integer.parseInt (zVoxelField.getText ());
			if (zVoxelSize == 0) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.zVoxelSizeIsNotFilled"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}

			if (Check.isEmpty (xVoxelField.getText ()) ) xVoxelField.setText("0");
			if (!Check.isInt (xVoxelField.getText ())) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.xVoxelSizeIsNotInt"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}
			xVoxelSize = Integer.parseInt (xVoxelField.getText ());
			if (xVoxelSize == 0) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.xVoxelSizeIsNotFilled"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}

			if (Check.isEmpty (yVoxelField.getText ()) ) yVoxelField.setText("0");
			if (!Check.isInt (yVoxelField.getText ())) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.yVoxelSizeIsNotInt"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}
			yVoxelSize = Integer.parseInt (yVoxelField.getText ());
			if (yVoxelSize == 0) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.yVoxelSizeIsNotFilled"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}

			//SAMPLE Z SIZE
			if (Check.isEmpty (zSampleField.getText ()) ) zSampleField.setText("0");
			if (!Check.isInt (zSampleField.getText ())) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.zSampleSizeIsNotInt"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}
			zSampleSize = Integer.parseInt (zSampleField.getText ());
			if (zSampleSize == 0) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.zSampleSizeIsNotFilled"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}
			if (zSampleSize%zVoxelSize != 0) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.zSampleSizeIsNotValid"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}


			//Sample dimensions
			if (!isCubeMethod) {

				if (Check.isEmpty (xSampleField.getText ()) ) xSampleField.setText("0");
				if (!Check.isInt (xSampleField.getText ())) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.xSampleSizeIsNotInt"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
				}
				xSampleSize = Integer.parseInt (xSampleField.getText ());
				if (xSampleSize == 0) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.xSampleSizeIsNotFilled"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
				}
				if (xSampleSize%xVoxelSize != 0) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.xSampleSizeIsNotValid"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
				}

				if (Check.isEmpty (ySampleField.getText ()) ) ySampleField.setText("0");
				if (!Check.isInt (ySampleField.getText ())) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.ySampleSizeIsNotInt"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
				}
				ySampleSize = Integer.parseInt (ySampleField.getText ());
				if (ySampleSize == 0) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.ySampleSizeIsNotFilled"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
				}
				if (ySampleSize%yVoxelSize != 0) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.ySampleSizeIsNotValid"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
				}

			}

		}



		//Shape dimension
		if (Check.isEmpty (xShapeField.getText ()) ) xShapeField.setText("0");
		if (!Check.isInt (xShapeField.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.xShapeSizeIsNotInt"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
		}
		xShapeSize = Integer.parseInt (xShapeField.getText ());
		if (xShapeSize == 0) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.xShapeSizeIsNotFilled"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
		}
		if (xShapeSize%xVoxelSize != 0) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.xShapeSizeIsNotValid"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
		}


		if (shapeKind.equals("XZ")) {
			yShapeField.setText("0");
			yShapeSize = 0;
		}
		else {
			if (Check.isEmpty (yShapeField.getText ()) ) yShapeField.setText("0");
			if (!Check.isInt (yShapeField.getText ())) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.yShapeSizeIsNotInt"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}
			yShapeSize = Integer.parseInt (yShapeField.getText ());
			if (yShapeSize == 0) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.yShapeSizeIsNotFilled"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}
			if (yShapeSize%yVoxelSize != 0) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.yShapeSizeIsNotValid"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}
		}

		if (Check.isEmpty (zShapeField.getText ()) ) zShapeField.setText("0");
		if (!Check.isInt (zShapeField.getText ())) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.zShapeSizeIsNotInt"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
		}
		zShapeSize = Integer.parseInt (zShapeField.getText ());
		if (zShapeSize == 0) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.zShapeSizeIsNotFilled"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
		}
		if (zShapeSize%zVoxelSize != 0) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeMeasuredDialog.zShapeSizeIsNotValid"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
		}

		return true;
	}

	/**	SHAPE validation
	*/
	private void validateAction () {

		if (controlValues() ) {

			//sample or shape creation (not yet in database)
			xVoxel = ((double) xVoxelSize) / 100;
			yVoxel = ((double) yVoxelSize) / 100;
			zVoxel = ((double) zVoxelSize) / 100;


			xSample = ((double) xSampleSize) / 100;
			ySample = ((double) ySampleSize) / 100;
			zSample  = ((double) zSampleSize) / 100;


			xShape = ((double) xShapeSize) / 100;
			yShape = ((double) yShapeSize) / 100;
			zShape = ((double) zShapeSize) / 100;


			if (isCubeMethod) {
				if (sample == null) {
					sample = new FmDBShape (-1, plant,
											3,
											"XZ",
											xVoxel, yVoxel, zVoxel,
											1, 1, zSample,
											0.0, 0.0, 0.0,
											0.0, 0.0,
											"Measured",
											isCubeMethod,
											false, false);

					FmSample2DChooser dialog = new FmSample2DChooser (model, sample, false);
					sampleId = dialog.getShapeId();
				}

				//sample already exist or has been created with success
				if (sampleId > 0) {
					shape = new FmDBShape (-1, plant,
											1,
											shapeKind,
											xVoxel, yVoxel, zVoxel,
											xShape, yShape, zShape,
											0.0, 0.0, 0.0,
											0.0, 0.0,
											"Measured",
											isCubeMethod,
											false, false);


					if  (shapeKind.equals("XZ")) {
						FmPlant2DEditor blockEntry = new FmPlant2DEditor (model, sample, shape, true);
					}
					if  (shapeKind.equals("XZ_YZ")) {
						FmPlant2DCrossEditor dialog = new FmPlant2DCrossEditor (model, sample, shape);
					}
					if  (shapeKind.equals("XYZ")) {
						if (method == 0) {
							int copy = 0;
							if (shape2D != null) copy = 1;	//copy
							FmPlant3DEditor dialog = new FmPlant3DEditor (model, sample, shape, shape2D, copy);
						}
						else {
							if (shape2D != null) {
								FmPlant3DCageEditor dialog = new FmPlant3DCageEditor (model, shape, shape2D);
							}

						}
					}
				}

			}
			else {
				if (sample == null) {

					sample = new FmDBShape (-1, plant,
											3,
											"XYZ",
											xVoxel, yVoxel, zVoxel,
											xSample, ySample, zSample,
											0.0, 0.0, 0.0,
											0.0, 0.0,
											"Measured",
											isCubeMethod,
											false, false);


					FmSample3DCageEditor dialog = new FmSample3DCageEditor (model, sample);
					sampleId = dialog.getShapeId();
				}

				//sample already exist or has been created with success
				if (sampleId > 0) {
					shape = new FmDBShape (-1, plant,
											1,
											"XYZ",
											xVoxel, yVoxel, zVoxel,
											xShape, yShape, zShape,
											0.0, 0.0, 0.0,
											0.0, 0.0,
											"Measured",
											isCubeMethod,
											false, false);

					FmPlant3DCageEditor dialog = new FmPlant3DCageEditor (model, shape, sample);
				}
			}

			setValidDialog (true);
			setVisible (false);

		}

	}


	/**	Initialize the GUI.
	*/
	private void createUI () {

		JScrollPane shapeScroll = new JScrollPane ();
		JPanel shapePanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
		Box shapeBox = Box.createVerticalBox ();

		// Option Choice panel
		JPanel choicePanel = new JPanel (new GridLayout(1,2) );

		Box box1 = Box.createVerticalBox ();
		Box box2 = Box.createVerticalBox ();


		methodGroup = new ButtonGroup ();
		radioCube = new JRadioButton (Translator.swap ("FiShapeMeasuredDialog.cube"), false);
		radioCube.addActionListener (this);
		radioCage = new JRadioButton (Translator.swap ("FiShapeMeasuredDialog.cage"), false);
		radioCage.addActionListener (this);

		methodGroup.add (radioCube);
		methodGroup.add (radioCage);

		if (isCubeMethod) methodGroup.setSelected (radioCube.getModel (), true);
		else methodGroup.setSelected (radioCage.getModel (), true);

		if (sample != null) {
			radioCube.setEnabled (false);
			radioCage.setEnabled (false);
		}


		box2.add (new JWidthLabel (Translator.swap ("FiShapeMeasuredDialog.method")+" :",100));
		box2.add (radioCube);
		box2.add (radioCage);

		choicePanel.add(box2);


		kindGroup = new ButtonGroup ();
		radio2D = new JRadioButton (Translator.swap ("FiShapeMeasuredDialog.2D"), false);
		radio2D.addActionListener (this);
		radio2DB = new JRadioButton (Translator.swap ("FiShapeMeasuredDialog.2DB"), false);
		radio2DB.addActionListener (this);
		radio3D = new JRadioButton (Translator.swap ("FiShapeMeasuredDialog.3D"), false);
		radio3D.addActionListener (this);
		radio3DF2D = new JRadioButton (Translator.swap ("FiShapeMeasuredDialog.3DF2D"), false);
		radio3DF2D.addActionListener (this);

		kindGroup.add (radio2D);
		kindGroup.add (radio2DB);
		kindGroup.add (radio3D);
		kindGroup.add (radio3DF2D);


		kindGroup.setSelected (radio3D.getModel (), true);

		if (shape2D != null) {
			radio2D.setEnabled (false);
			radio2DB.setEnabled (false);
		}
		else {
			radio3DF2D.setEnabled (false);
		}

		box1.add (new JWidthLabel (Translator.swap ("FiShapeMeasuredDialog.kind")+" :",100));
		box1.add (radio2D);
		box1.add (radio2DB);
		box1.add (radio3D);
		box1.add (radio3DF2D);

		choicePanel.add(box1);




		shapeBox.add (choicePanel);


		//VOXEL SIZE
		JPanel sampleDef = new JPanel (new FlowLayout (FlowLayout.LEFT));
		Border sampleEtched = BorderFactory.createEtchedBorder ();
		Border sampleBorder = BorderFactory.createTitledBorder (sampleEtched, Translator.swap ("FiShapeMeasuredDialog.sampleSize"));
		sampleDef.setBorder (sampleBorder);

		JPanel boxS1 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel boxS2 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		Box boxS3 = Box.createVerticalBox ();

		xVoxelField = new JTextField (5);
		xVoxelField.setText (""+xVoxelSize);
		yVoxelField= new JTextField (5);
		yVoxelField.setText (""+yVoxelSize);
		zVoxelField= new JTextField (5);
		zVoxelField.setText (""+zVoxelSize);

		boxS1.add (new JWidthLabel (Translator.swap ("FiShapeMeasuredDialog.zVoxelSize")+" :", 150));
		boxS1.add (zVoxelField);
		boxS1.add (new JWidthLabel (Translator.swap ("FiShapeMeasuredDialog.xVoxelSize")+" :", 10));
		boxS1.add (xVoxelField);
		boxS1.add (new JWidthLabel (Translator.swap ("FiShapeMeasuredDialog.yVoxelSize")+" :", 10));
		boxS1.add (yVoxelField);


		xSampleField = new JTextField (5);
		xSampleField.setText (""+xSampleSize);
		ySampleField= new JTextField (5);
		ySampleField.setText (""+ySampleSize);
		zSampleField= new JTextField (5);
		zSampleField.setText (""+zSampleSize);

		boxS2.add (new JWidthLabel (Translator.swap ("FiShapeMeasuredDialog.zSampleSize")+" :", 150));
		boxS2.add (zSampleField);
		boxS2.add (new JWidthLabel (Translator.swap ("FiShapeMeasuredDialog.xSampleSize")+" :", 10));
		boxS2.add (xSampleField);
		boxS2.add (new JWidthLabel (Translator.swap ("FiShapeMeasuredDialog.ySampleSize")+" :", 10));
		boxS2.add (ySampleField);

		boxS3.add (boxS1);
		boxS3.add (boxS2);
		sampleDef.add (boxS3);

		shapeBox.add (sampleDef);

		if (sample != null) {
			xVoxelField.setEnabled (false);
			yVoxelField.setEnabled (false);
			zVoxelField.setEnabled (false);
			xSampleField.setEnabled (false);
			ySampleField.setEnabled (false);
			zSampleField.setEnabled (false);
		}

		//SHAPE SIZE

		JPanel shapeDef = new JPanel (new FlowLayout (FlowLayout.LEFT));
		Border shapeEtched = BorderFactory.createEtchedBorder ();
		Border shapeBorder = BorderFactory.createTitledBorder (shapeEtched, Translator.swap ("FiShapeMeasuredDialog.shapeSize"));
		shapeDef.setBorder (shapeBorder);

		xShapeField = new JTextField (5);
		xShapeField.setText (""+xShapeSize);
		yShapeField= new JTextField (5);
		yShapeField.setText (""+yShapeSize);
		zShapeField= new JTextField (5);
		zShapeField.setText (""+zShapeSize);

		shapeDef.add (new JWidthLabel (Translator.swap ("FiShapeMeasuredDialog.zShapeSize")+" :", 150));
		shapeDef.add (zShapeField);
		shapeDef.add (new JWidthLabel (Translator.swap ("FiShapeMeasuredDialog.xShapeSize")+" :", 10));
		shapeDef.add (xShapeField);
		shapeDef.add (new JWidthLabel (Translator.swap ("FiShapeMeasuredDialog.yShapeSize")+" :", 10));
		shapeDef.add (yShapeField);


		shapeBox.add (shapeDef);



		shapePanel.add (shapeBox);
		shapeScroll.getViewport().setView(shapePanel);


		// Control panel
		JPanel controlPanel = new JPanel (new FlowLayout (FlowLayout.CENTER));
		save = new JButton (Translator.swap ("FiShapeMeasuredDialog.save"));
		close = new JButton (Translator.swap ("Shared.close"));
		help = new JButton (Translator.swap ("Shared.help"));
		controlPanel.add (save);
		controlPanel.add (close);
		controlPanel.add (help);

		save.addActionListener (this);
		close.addActionListener (this);
		help.addActionListener (this);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (shapeScroll, BorderLayout.CENTER);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);

		setTitle (Translator.swap ("FiShapeMeasuredDialog.title"));
		setSize (new Dimension(450,300));


		setModal (true);
	}

	public long getSampleId() {
		return sampleId;
	}
}

