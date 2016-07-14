package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
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


/**	FiShapeVirtualDialog : Plant shape choice (2D, 2D*2, 3D)
*
*	@author I. Lecomte - march 2008
*/
public class FmShapeVirtualDialog extends AmapDialog implements ActionListener {

	private FmModel model;

	private FmDBShape sample, shape;
	private long sampleId, shapeId;					//id in the database
	private FmDBPlant plant;						//id of the plant in the database


	//sampling method
	private ButtonGroup methodGroup;
	private JRadioButton radioCube;
	private JRadioButton radioCage;
	private boolean isCubeMethod; 			//true=cube

	//voxel dimension
	private JTextField xVoxelField;
	private JTextField yVoxelField;
	private JTextField zVoxelField;
	private int xVoxelSize, yVoxelSize, zVoxelSize;
	private double xVoxel, yVoxel, zVoxel;

	//sample dimension (cube method)
	private JTextField zSampleField;
	private int zSampleSize;
	private double zSample;

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
	public FmShapeVirtualDialog (FmModel _model, FmDBPlant _plant, FmDBShape _sample) {

		super ();
		model  = _model;
		plant  = _plant;
		sample = _sample;

		//default shape size is tree size
		long val = Math.round(plant.getCrownDiameter() * 100);
		xShapeSize = (int) (val);
		val = Math.round(plant.getCrownPerpendicularDiameter() * 100);
		yShapeSize = (int) (val);
		val = Math.round(plant.getHeight() * 100);
		zShapeSize = (int) (val);


		if (yShapeSize <= 0) yShapeSize = xShapeSize;


		//voxels size
		isCubeMethod = true;

		if (sample != null) {
			sampleId = sample.getShapeId();
			val = Math.round(sample.getVoxelXSize() * 100);
			xVoxelSize = (int) (val);
			val = Math.round(sample.getVoxelYSize() * 100);
			yVoxelSize = (int) (val);
			val = Math.round(sample.getVoxelZSize() * 100);
			zVoxelSize = (int) (val);

			val = Math.round(sample.getZMax() * 100);
			zSampleSize = (int) (val);

		}
		else {
			zSampleSize = 75;		//3 cubes
			xVoxelSize = 25;
			yVoxelSize = 25;
			zVoxelSize = 25;
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

		} else if (evt.getSource ().equals (radioCube)) {
			setCube (true);
		} else if (evt.getSource ().equals (radioCage)) {
			setCube (false);

		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}

	/**	Protocole method choice modification
	*/
	private void setCube (boolean cube) {
		isCubeMethod = cube;

		if (isCubeMethod) {
			zSampleField.setEnabled (true);
			repaint();

		}
		else {
			zSampleField.setEnabled (false);
			repaint();
		}
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
						JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeVirtualDialog.zVoxelSizeIsNotInt"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
			}
			zVoxelSize = Integer.parseInt (zVoxelField.getText ());
			if (zVoxelSize == 0) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeVirtualDialog.zVoxelSizeIsNotFilled"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}

			if (Check.isEmpty (xVoxelField.getText ()) ) xVoxelField.setText("0");
			if (!Check.isInt (xVoxelField.getText ())) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeVirtualDialog.xVoxelSizeIsNotInt"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}
			xVoxelSize = Integer.parseInt (xVoxelField.getText ());
			if (xVoxelSize == 0) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeVirtualDialog.xVoxelSizeIsNotFilled"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}

			if (Check.isEmpty (yVoxelField.getText ()) ) yVoxelField.setText("0");
			if (!Check.isInt (yVoxelField.getText ())) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeVirtualDialog.yVoxelSizeIsNotInt"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}
			yVoxelSize = Integer.parseInt (yVoxelField.getText ());
			if (yVoxelSize == 0) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeVirtualDialog.yVoxelSizeIsNotFilled"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}

			//sample size
			if (isCubeMethod) {
				if (Check.isEmpty (zSampleField.getText ()) ) zSampleField.setText("0");
				if (!Check.isInt (zSampleField.getText ())) {
							JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeVirtualDialog.zSampleSizeIsNotInt"),
							Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
							return false;
				}
				zSampleSize = Integer.parseInt (zSampleField.getText ());
				if (zSampleSize == 0) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeVirtualDialog.zSampleSizeIsNotFilled"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
				}
				if (zSampleSize%zVoxelSize != 0) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeVirtualDialog.zSampleSizeIsNotValid"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
				}
			}
		}

		//Shape dimension
		if (Check.isEmpty (xShapeField.getText ()) ) xShapeField.setText("0");
		if (!Check.isInt (xShapeField.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeVirtualDialog.xShapeSizeIsNotInt"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
		}
		xShapeSize = Integer.parseInt (xShapeField.getText ());
		if (xShapeSize == 0) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeVirtualDialog.xShapeSizeIsNotFilled"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
		}
		if (xShapeSize%xVoxelSize != 0) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeVirtualDialog.xShapeSizeIsNotValid"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
		}



		if (Check.isEmpty (yShapeField.getText ()) ) yShapeField.setText("0");
		if (!Check.isInt (yShapeField.getText ())) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeVirtualDialog.yShapeSizeIsNotInt"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
		}
		yShapeSize = Integer.parseInt (yShapeField.getText ());
		if (yShapeSize == 0) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeVirtualDialog.yShapeSizeIsNotFilled"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
		}
		if (yShapeSize%yVoxelSize != 0) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeVirtualDialog.yShapeSizeIsNotValid"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
		}


		if (Check.isEmpty (zShapeField.getText ()) ) zShapeField.setText("0");
		if (!Check.isInt (zShapeField.getText ())) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeVirtualDialog.zShapeSizeIsNotInt"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
		}
		zShapeSize = Integer.parseInt (zShapeField.getText ());
		if (zShapeSize == 0) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeVirtualDialog.zShapeSizeIsNotFilled"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
		}
		if (zShapeSize%zVoxelSize != 0) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiShapeVirtualDialog.zShapeSizeIsNotValid"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
		}

		return true;
	}

	/**	SHAPE validation
	*/
	private void validateAction () {

		if (controlValues() ) {

			xVoxel = ((double) xVoxelSize) / 100;
			yVoxel = ((double) yVoxelSize) / 100;
			zVoxel = ((double) zVoxelSize) / 100;

			//sample creation (if not yet in database)

			if ((isCubeMethod) && (sample == null)) {



				zSample = ((double) zSampleSize) / 100;

				sample = new FmDBShape (-1, plant,
										3,
										"XZ",
										xVoxel, yVoxel, zVoxel,
										xVoxel, yVoxel, zSample,
										0.0, 0.0, 0.0,
										0.0, 0.0,
										"Virtual",
										false,
										false, false);

				FmSample2DEditor dialog = new FmSample2DEditor (model, sample, 0, 1, 2);
				sampleId = dialog.getShapeId();

			}

			//shape creation
			xShape = ((double) xShapeSize) / 100;
			yShape = ((double) yShapeSize) / 100;
			zShape = ((double) zShapeSize) / 100;

			shape = new FmDBShape (-1, plant,
									1,
									"XYZ",
									xVoxel, yVoxel, zVoxel,
									xShape, yShape, zShape,
									0.0, 0.0, 0.0,
									0.0, 0.0,
									"Virtual",
									false,
									false, false);

			if ((isCubeMethod) && (sample != null)) {
				FmPlant3DEditor dialog = new FmPlant3DEditor (model, sample, shape, null, 0);
				shapeId = dialog.getShapeId();

			}
			else {
				FmPlant3DCageEditor dialog = new FmPlant3DCageEditor (model, shape, null);
				shapeId = dialog.getShapeId();
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

		//Method
		Box methodBox = Box.createVerticalBox ();


		methodGroup = new ButtonGroup ();
		radioCube = new JRadioButton (Translator.swap ("FiShapeVirtualDialog.cube"), false);
		radioCube.addActionListener (this);
		radioCage = new JRadioButton (Translator.swap ("FiShapeVirtualDialog.cage"), false);
		radioCage.addActionListener (this);

		methodGroup.add (radioCube);
		methodGroup.add (radioCage);

		if (isCubeMethod) methodGroup.setSelected (radioCube.getModel (), true);
		else methodGroup.setSelected (radioCage.getModel (), true);


		methodBox.add (new JWidthLabel (Translator.swap ("FiShapeVirtualDialog.method")+" :",100));
		methodBox.add (radioCube);
		methodBox.add (radioCage);

		if (sample != null) {
			radioCube.setEnabled(false);
			radioCage.setEnabled(false);
		}


		shapeBox.add (methodBox);


		//VOXEL SIZE
		Box sampleDef = Box.createVerticalBox ();
		Border sampleEtched = BorderFactory.createEtchedBorder ();
		Border sampleBorder = BorderFactory.createTitledBorder (sampleEtched, Translator.swap ("FiShapeVirtualDialog.voxelSize"));
		sampleDef.setBorder (sampleBorder);

		JPanel boxS1 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel boxS2 = new JPanel (new FlowLayout (FlowLayout.LEFT));


		xVoxelField = new JTextField (5);
		xVoxelField.setText (""+xVoxelSize);
		yVoxelField= new JTextField (5);
		yVoxelField.setText (""+yVoxelSize);
		zVoxelField= new JTextField (5);
		zVoxelField.setText (""+zVoxelSize);

		zSampleField= new JTextField (5);
		zSampleField.setText (""+zSampleSize);

		boxS1.add (new JWidthLabel (Translator.swap ("FiShapeVirtualDialog.zVoxelSize")+" :", 150));
		boxS1.add (zVoxelField);
		boxS1.add (new JWidthLabel (Translator.swap ("FiShapeVirtualDialog.xVoxelSize")+" :", 10));
		boxS1.add (xVoxelField);
		boxS1.add (new JWidthLabel (Translator.swap ("FiShapeVirtualDialog.yVoxelSize")+" :", 10));
		boxS1.add (yVoxelField);

		boxS2.add (new JWidthLabel (Translator.swap ("FiShapeVirtualDialog.zSampleSize")+" :", 150));
		boxS2.add (zSampleField);

		if (sample != null) {
			xVoxelField.setEnabled(false);
			yVoxelField.setEnabled(false);
			zVoxelField.setEnabled(false);
			zSampleField.setEnabled (false);
		}

		sampleDef.add (boxS1);
		sampleDef.add (boxS2);

		shapeBox.add (sampleDef);


		//SHAPE SIZE

		JPanel shapeDef = new JPanel (new FlowLayout (FlowLayout.LEFT));
		Border shapeEtched = BorderFactory.createEtchedBorder ();
		Border shapeBorder = BorderFactory.createTitledBorder (shapeEtched, Translator.swap ("FiShapeVirtualDialog.shapeSize"));
		shapeDef.setBorder (shapeBorder);

		xShapeField = new JTextField (5);
		xShapeField.setText (""+xShapeSize);
		yShapeField= new JTextField (5);
		yShapeField.setText (""+yShapeSize);
		zShapeField= new JTextField (5);
		zShapeField.setText (""+zShapeSize);

		shapeDef.add (new JWidthLabel (Translator.swap ("FiShapeVirtualDialog.zShapeSize")+" :", 150));
		shapeDef.add (zShapeField);
		shapeDef.add (new JWidthLabel (Translator.swap ("FiShapeVirtualDialog.xShapeSize")+" :", 10));
		shapeDef.add (xShapeField);
		shapeDef.add (new JWidthLabel (Translator.swap ("FiShapeVirtualDialog.yShapeSize")+" :", 10));
		shapeDef.add (yShapeField);


		shapeBox.add (shapeDef);



		shapePanel.add (shapeBox);
		shapeScroll.getViewport().setView(shapePanel);


		// Control panel
		JPanel controlPanel = new JPanel (new FlowLayout (FlowLayout.CENTER));
		save = new JButton (Translator.swap ("FiShapeVirtualDialog.save"));
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

		setTitle (Translator.swap ("FiShapeVirtualDialog.title"));
		setSize (new Dimension(450,300));


		setModal (true);
	}

	public long getSampleId() {
		return sampleId;
	}

	public long getShapeId() {
		return shapeId;
	}

}

