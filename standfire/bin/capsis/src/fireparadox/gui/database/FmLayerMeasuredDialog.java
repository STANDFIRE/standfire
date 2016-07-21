package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Check;
import jeeb.lib.util.JWidthLabel;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import fireparadox.model.FmModel;
import fireparadox.model.database.FmDBPlant;
import fireparadox.model.database.FmDBShape;


/**	FiLayerMeasuredDialog : Layer measured shape choice
*
*	@author I. Lecomte - march 2008
*/
public class FmLayerMeasuredDialog extends AmapDialog implements ActionListener {

	private FmModel model;

	private FmDBShape shape;
	private FmDBShape sample;
	private FmDBShape sampleEdge;
	private FmDBPlant plant;

	private long sampleId, sampleEdgeId, shapeId;

	private JTextField xVoxelField;
	private JTextField yVoxelField;
	private JTextField zVoxelField;
	private int xVoxelSize, yVoxelSize, zVoxelSize;
	private double xVoxel, yVoxel, zVoxel;

	private JCheckBox edgeSampleBox;
	private JCheckBox coreSampleBox;
	private JTextField zCoreSampleField;
	private JTextField zEdgeSampleField;
	private int zCoreSampleSize, zEdgeSampleSize;
	private double zCoreSample, zEdgeSample;
	private boolean isSampleCoreCreation;
	private boolean isSampleEdgeCreation;

	private JCheckBox layerEdge;
	private JCheckBox layerCore;
	private JTextField xMaxCoreField;
	private JTextField xMaxEdgeField;
	private JTextField zMaxCoreField;
	private JTextField zMaxEdgeField;
	private int xMaxCoreSize, zMaxCoreSize, xMaxEdgeSize, zMaxEdgeSize;
	private double xCoreMax, xEdgeMax, zCoreMax, zEdgeMax;
	private boolean isCore, isEdge;

	private JTextField widthMinField;
	private JTextField widthMaxField;
	private JTextField spreadMaxField;
	private int widthMinSize, widthMaxSize, spreadMaxSize;
	private double widthMin, widthMax, spreadMax;

	private JButton save;
	private JButton close;
	private JButton help;


	/**	Constructor.
	*/
	public FmLayerMeasuredDialog (FmModel _model, FmDBPlant _plant, FmDBShape _sample, FmDBShape _sampleEdge) {

		super ();
		model = _model;
		plant = _plant;
		sample = _sample;
		sampleEdge = _sampleEdge;

		isCore = true;
		isEdge = false;
		isSampleEdgeCreation = false;
		isSampleCoreCreation = false;

		xVoxelSize = 25;
		yVoxelSize = 25;
		zVoxelSize = 25;
		//zVoxelSize = (int) FiInitialParameters.Z_CUBE_SIZE * 100;

		long val = Math.round(plant.getHeight() * 100);

		zMaxCoreSize = (int) (val);

		zMaxEdgeSize = zMaxCoreSize;
		zCoreSampleSize = zMaxCoreSize;
		zEdgeSampleSize = zMaxEdgeSize;
		widthMinSize = xVoxelSize;

		//is there already a CORE sample ?
		if (sample != null) {
			sampleId = sample.getShapeId();

			val = Math.round(sample.getVoxelXSize() * 100);
			xVoxelSize = (int) (val);
			val = Math.round(sample.getVoxelYSize() * 100);
			yVoxelSize = (int) (val);
			val = Math.round(sample.getVoxelZSize() * 100);
			zVoxelSize = (int) (val);

			val = Math.round(sample.getZMax() * 100);
			zCoreSampleSize = (int) (val);
			val = Math.round(sample.getXMax() * 100);
			widthMinSize = (int) (val);
		}
		else {
			if (sampleEdge == null) {
				isSampleCoreCreation = true;
			}
		}


		//is there already an EDGE sample ?

		if (sampleEdge != null) {

			sampleEdgeId = sampleEdge.getShapeId();

			if (sample == null) {
				val = Math.round(sampleEdge.getVoxelXSize() * 100);
				xVoxelSize = (int) (val);
				val = Math.round(sampleEdge.getVoxelYSize() * 100);
				yVoxelSize = (int) (val);
				val = Math.round(sampleEdge.getVoxelZSize() * 100);
				zVoxelSize = (int) (val);
			}
			val = Math.round(sampleEdge.getZMax() * 100);
			zEdgeSampleSize = (int) (val);
			val = Math.round(sampleEdge.getXMax() * 100);
			widthMinSize += (int) (val);
		}


		xMaxCoreSize = xVoxelSize;
		val = Math.round(plant.getCrownDiameter() * 100);
		xMaxEdgeSize = (int) (val);
		widthMaxSize = xMaxEdgeSize;
		spreadMaxSize = widthMaxSize;


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

		} else if (evt.getSource ().equals (layerEdge)) {
			setEdge ();

		} else if (evt.getSource ().equals (layerCore)) {
			setCore ();

		} else if (evt.getSource ().equals (edgeSampleBox)) {
			setSampleEdge ();

		} else if (evt.getSource ().equals (coreSampleBox)) {
			setSampleCore ();

		} else if (evt.getSource ().equals (xVoxelField)) {
			setXVoxel ();

		} else if (evt.getSource ().equals (help)) {
			Helper.helpFor (this);
		}
	}


	/**	Core sample creation activation
	*/
	private void setSampleCore() {
		if (isSampleCoreCreation) {
			isSampleCoreCreation = false;
			zCoreSampleField.setEnabled(false);
		}
		else {
			isSampleCoreCreation = true;
			zCoreSampleField.setEnabled(true);
		}
	}
	/**	Edge sample creation activation
	*/
	private void setSampleEdge () {

		if (isSampleEdgeCreation) {
			isSampleEdgeCreation = false;
			zEdgeSampleField.setEnabled(false);
		}
		else {
			isSampleEdgeCreation = true;
			zEdgeSampleField.setEnabled(true);
		}
	}
	/**	Core part of the shape creation activation
	*/
	private void setCore() {

		if (isCore) {
			isCore = false;
			zMaxCoreField.setEnabled(false);
		}
		else {
			isCore = true;
			zMaxCoreField.setEnabled(true);
		}
	}

	/**	Edge part of the shape creation activation
	*/
	private void setEdge () {

		if (isEdge) {
			isEdge = false;
			xMaxEdgeField.setEnabled(false);
			zMaxEdgeField.setEnabled(false);
		}
		else {
			isEdge = true;
			xMaxEdgeField.setEnabled(true);
			zMaxEdgeField.setEnabled(true);
		}
	}
	private void setXVoxel () {
		if (Check.isEmpty (xVoxelField.getText ()) ) xVoxelField.setText("0");
		if (Check.isInt (xVoxelField.getText ())) {
			xVoxelSize = Integer.parseInt (xVoxelField.getText ());
			xMaxCoreSize = xVoxelSize;
			xMaxCoreField.setText (""+xVoxelSize);
		}
	}
	/**	Close was hit
	*/
	private void closeAction () {
		setVisible (false);
	}
	/**	Control the value in the ComboBoxes and the TextFields
		 */
	private boolean controlValues ()   {

		if ((sample == null)  && (sampleEdge == null)) {
			//grid voxel size
			if (Check.isEmpty (xVoxelField.getText ()) ) xVoxelField.setText("0");
			if (!Check.isInt (xVoxelField.getText ())) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.xVoxelSizeIsNotInt"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}
			xVoxelSize = Integer.parseInt (xVoxelField.getText ());
			if (xVoxelSize == 0) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.xVoxelSizeIsNotFilled"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}

			if (Check.isEmpty (yVoxelField.getText ()) ) yVoxelField.setText("0");
			if (!Check.isInt (yVoxelField.getText ())) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.yVoxelSizeIsNotInt"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}
			yVoxelSize = Integer.parseInt (yVoxelField.getText ());
			if (yVoxelSize == 0) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.yVoxelSizeIsNotFilled"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}

			if (Check.isEmpty (zVoxelField.getText ()) ) zVoxelField.setText("0");
			if (!Check.isInt (zVoxelField.getText ())) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.zVoxelSizeIsNotInt"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
			}
			zVoxelSize = Integer.parseInt (zVoxelField.getText ());
			if (zVoxelSize == 0) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.zVoxelSizeIsNotFilled"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}

			if ((!isSampleCoreCreation) && (!isSampleEdgeCreation)) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.selectOneSample"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
			}

			//sample CORE  dimensions
			if (isSampleCoreCreation) {
				if (Check.isEmpty (zCoreSampleField.getText ()) ) zCoreSampleField.setText("0");
				if (!Check.isInt (zCoreSampleField.getText ())) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.zCoreSampleSizeIsNotInt"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
				}
				zCoreSampleSize = Integer.parseInt (zCoreSampleField.getText ());
				if (zCoreSampleSize == 0) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.zCoreSampleSizeIsNotFilled"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
				}
				if (zCoreSampleSize%zVoxelSize != 0) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.zCoreSampleSizeIsNotValid"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
				}
			}
			else zCoreSampleSize = 0;

			//sample EDGE  dimensions
			if (isSampleEdgeCreation) {
				if (Check.isEmpty (zEdgeSampleField.getText ()) ) zEdgeSampleField.setText("0");
				if (!Check.isInt (zEdgeSampleField.getText ())) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.zEdgeSampleSizeIsNotInt"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
				}
				zEdgeSampleSize = Integer.parseInt (zEdgeSampleField.getText ());
				if (zEdgeSampleSize == 0) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.zEdgeSampleSizeIsNotFilled"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
				}
				if (zEdgeSampleSize%zVoxelSize != 0) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.zEdgeSampleSizeIsNotValid"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
				}

			}
			else zEdgeSampleSize = 0;
		}



		if ((!isCore) && (!isEdge)) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.selectOneShape"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
			return false;
		}

		//Grid dimension
		if (isCore) {
			if (Check.isEmpty (xMaxCoreField.getText ()) ) xMaxCoreField.setText("0");
			if (!Check.isInt (xMaxCoreField.getText ())) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.xMaxSizeIsNotInt"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}
			xMaxCoreSize = Integer.parseInt (xMaxCoreField.getText ());
			if (xMaxCoreSize == 0) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.xMaxSizeIsNotFilled"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}
			if (xMaxCoreSize%xVoxelSize != 0) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.xMaxSizeIsNotValid"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}

			if (Check.isEmpty (zMaxCoreField.getText ()) ) zMaxCoreField.setText("0");
			if (!Check.isInt (zMaxCoreField.getText ())) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.zMaxSizeIsNotInt"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
			}
			zMaxCoreSize = Integer.parseInt (zMaxCoreField.getText ());
			if (zMaxCoreSize == 0) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.zMaxSizeIsNotFilled"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}
			if (zMaxCoreSize%zVoxelSize != 0) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.zMaxSizeIsNotValid"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}

		}
		else {
			xMaxCoreSize = 0;
			zMaxCoreSize = 0;
		}

		if (isEdge) {
			if (Check.isEmpty (xMaxEdgeField.getText ()) ) xMaxEdgeField.setText("0");
				if (!Check.isInt (xMaxEdgeField.getText ())) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.xMaxEdgeSizeIsNotInt"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
				}
				xMaxEdgeSize = Integer.parseInt (xMaxEdgeField.getText ());
				if (xMaxEdgeSize == 0) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.xMaxEdgeSizeIsNotFilled"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
				}
				if (xMaxEdgeSize%xVoxelSize != 0) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.xMaxEdgeSizeIsNotValid"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
			}

			if (Check.isEmpty (zMaxEdgeField.getText ()) ) zMaxEdgeField.setText("0");
			if (!Check.isInt (zMaxEdgeField.getText ())) {
						JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.zMaxEdgeSizeIsNotInt"),
						Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
						return false;
			}
			zMaxEdgeSize = Integer.parseInt (zMaxEdgeField.getText ());
			if (zMaxEdgeSize == 0) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.zMaxEdgeSizeIsNotFilled"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
			}
			if (zMaxEdgeSize%zVoxelSize != 0) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.zMaxEdgeSizeIsNotValid"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
								return false;
			}

		}
		else {
			xMaxEdgeSize = 0;
			zMaxEdgeSize = 0;
		}


		//MAX LAYER SIZE
		if (Check.isEmpty (spreadMaxField.getText ()) ) spreadMaxField.setText("0");
		if (!Check.isInt (spreadMaxField.getText ())) {
					JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.widthMaxIsNotInt"),
					Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
					return false;
		}
		spreadMaxSize = Integer.parseInt (spreadMaxField.getText ());
		if (spreadMaxSize == 0) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.widthMaxIsNotFilled"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
		}
		if ((spreadMaxSize < widthMinSize) ||  (spreadMaxSize < widthMaxSize)) {
				JOptionPane.showMessageDialog (this, Translator.swap ("FiLayerMeasuredDialog.widthMaxIsNotValid"),
				Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
				return false;
		}
		return true;
	}
	/**	TSHAPE validation
	*/
	private void validateAction () {

		if (controlValues() ) {

			xVoxel = ((double) xVoxelSize) / 100;
			yVoxel = ((double) yVoxelSize) / 100;
			zVoxel = ((double) zVoxelSize) / 100;

			xCoreMax = ((double) xMaxCoreSize) / 100;
			xEdgeMax = ((double) xMaxEdgeSize) / 100;
			zCoreMax = ((double) zMaxCoreSize) / 100;
			zEdgeMax = ((double) zMaxEdgeSize) / 100;

			zCoreSample = ((double) zCoreSampleSize) / 100;
			zEdgeSample = ((double) zEdgeSampleSize) / 100;

			widthMin = ((double) widthMinSize) / 100;
			spreadMax = ((double) spreadMaxSize) / 100;

			boolean isCubeMethod = true;

			try {
				//SAMPLE CREATION
				if ((sample == null)  && (sampleEdge == null)) {
					//CORE Sample creation
					if ((isSampleCoreCreation) && (sample == null)) {
						sampleId = -1;
						sample = new FmDBShape (sampleId, plant,
											4,
											"XZ",
											xVoxel, yVoxel, zVoxel,
											1, 1, zCoreSample,
											0.0, 0.0, 0.0,
											0.0, 0.0,
											"Measured",
											isCubeMethod,
											false,false);

						FmSample2DChooser dialog = new FmSample2DChooser (model, sample,  true);
						sampleId = dialog.getShapeId();

					}
					//EDGE sample creation
					if ((isSampleEdgeCreation) && (sampleEdge == null)) {
						sampleEdgeId = -1;
						sampleEdge = new FmDBShape (sampleEdgeId, plant,
											5,
											"XZ",
											xVoxel, yVoxel, zVoxel,
											1, 1, zEdgeSample,
											0.0, 0.0, 0.0,
											0.0, 0.0,
											"Measured",
											isCubeMethod,
											false,false);

						FmSample2DChooser dialog = new FmSample2DChooser (model, sampleEdge, true);
						sampleEdgeId = dialog.getShapeId();


					}
				}
				//SHAPE creation
				System.out.println("sampleEdgeId="+sampleEdgeId);

				if ((sampleId > 0) || (sampleEdgeId > 0)) {
					shapeId = -1;

					shape = new FmDBShape (shapeId, plant,
											2,
											"XZ",
											xVoxel, yVoxel, zVoxel,
											xCoreMax, 1, zCoreMax,
											xEdgeMax, 1, zEdgeMax,
											widthMin, spreadMax,
											"Measured",
											isCubeMethod,
											false,false);


					FmLayer2DEditor blockEntry = new FmLayer2DEditor (model, shape, sample, sampleEdge);

					setValidDialog (true);
					setVisible (false);
				}


			} catch (Exception e) {
				Log.println (Log.ERROR, "FiLayerMeasuredDialog", "error while UPDATE shape in data base", e);
			}
		}

	}


	/**	Initialize the GUI.
	*/
	private void createUI () {

		JScrollPane shapeScroll = new JScrollPane ();
		JPanel shapePanel = new JPanel (new FlowLayout (FlowLayout.LEFT));
		Box shapeBox = Box.createVerticalBox ();


		//VOXEL DIMENSION
		JPanel sampleDef = new JPanel (new FlowLayout (FlowLayout.LEFT));
		Border sampleEtched = BorderFactory.createEtchedBorder ();
		Border sampleBorder = BorderFactory.createTitledBorder (sampleEtched, Translator.swap ("FiLayerMeasuredDialog.samplesDef"));
		sampleDef.setBorder (sampleBorder);

		Box sampleBox = Box.createVerticalBox ();

		JPanel v1 =  new JPanel (new FlowLayout (FlowLayout.LEFT));

		xVoxelField = new JTextField (5);
		xVoxelField.setText (""+xVoxelSize);
		yVoxelField= new JTextField (5);
		yVoxelField.setText (""+yVoxelSize);
		zVoxelField= new JTextField (5);
		zVoxelField.setText (""+zVoxelSize);
		xVoxelField.addActionListener (this);

		v1.add (new JWidthLabel (Translator.swap ("FiLayerMeasuredDialog.zVoxelSize")+" :", 150));
		v1.add (zVoxelField);
		v1.add (new JWidthLabel (Translator.swap ("FiLayerMeasuredDialog.xVoxelSize")+" :", 10));
		v1.add (xVoxelField);
		v1.add (new JWidthLabel (Translator.swap ("FiLayerMeasuredDialog.yVoxelSize")+" :", 10));
		v1.add (yVoxelField);

		sampleBox.add (v1);



		//SAMPLE DIMENSIONS
		JPanel v4 =  new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel v5 =  new JPanel (new FlowLayout (FlowLayout.LEFT));

		coreSampleBox = new JCheckBox();
		coreSampleBox.addActionListener (this);
		coreSampleBox.setSelected (true);

		zCoreSampleField = new JTextField (5);
		zCoreSampleField.setText (""+zCoreSampleSize);

		v4.add (coreSampleBox);
		v4.add (new JWidthLabel (Translator.swap ("FiLayerMeasuredDialog.coreSample")+" :", 200));
		v4.add (zCoreSampleField);


		edgeSampleBox = new JCheckBox();
		edgeSampleBox.addActionListener (this);
		edgeSampleBox.setSelected (false);


		zEdgeSampleField= new JTextField (5);
		zEdgeSampleField.setText (""+zEdgeSampleSize);
		zEdgeSampleField.setEnabled (false);

		v5.add(edgeSampleBox);
		v5.add (new JWidthLabel (Translator.swap ("FiLayerMeasuredDialog.edgeSample")+" :", 200));
		v5.add(zEdgeSampleField);

		if ((sample != null) ||  (sampleEdge != null)) {
			if (sample != null) {
				coreSampleBox.setVisible(false);
			}
			else {
				v4.setVisible(false);
			}
			if (sampleEdge != null) {
				edgeSampleBox.setVisible (false);
			}
			else {
				v5.setVisible(false);
			}

		}



		sampleBox.add (v4);
		sampleBox.add (v5);

		sampleDef.add (sampleBox);

		shapeBox.add (sampleDef);

		//Samples informations are not available if sample already exist
		if ((sample != null)  || (sampleEdge != null)) {
			xVoxelField.setEnabled (false);
			yVoxelField.setEnabled (false);
			zVoxelField.setEnabled (false);
			zCoreSampleField.setEnabled (false);
			zEdgeSampleField.setEnabled (false);
			coreSampleBox.setEnabled (false);
			edgeSampleBox.setEnabled (false);
		}


		//SHAPE SIZE DEFINITION
		JPanel shapeDef = new JPanel (new FlowLayout (FlowLayout.LEFT));
		Border shapeEtched = BorderFactory.createEtchedBorder ();
		Border shapeBorder = BorderFactory.createTitledBorder (shapeEtched, Translator.swap ("FiLayerMeasuredDialog.shapeDimension"));
		shapeDef.setBorder (shapeBorder);
		Box boxShape = Box.createVerticalBox ();


		JPanel v6 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel v7 = new JPanel (new FlowLayout (FlowLayout.LEFT));

		layerCore = new JCheckBox();
		layerCore.addActionListener (this);
		layerCore.setSelected (isCore);

		xMaxCoreField = new JTextField (5);
		xMaxCoreField.setText (""+xMaxCoreSize);
		zMaxCoreField= new JTextField (5);
		zMaxCoreField.setText (""+zMaxCoreSize);

		v6.add (layerCore);
		v6.add (new JWidthLabel (Translator.swap ("FiLayerMeasuredDialog.zMaxSize")+" :", 124));
		v6.add (zMaxCoreField);
		v6.add (new JWidthLabel (Translator.swap ("FiLayerMeasuredDialog.xMaxSize")+" :", 50));
		v6.add (xMaxCoreField);
		//xMaxCoreField.setEnabled (false);


		layerEdge = new JCheckBox();
		layerEdge.addActionListener (this);
		layerEdge.setSelected (isEdge);

		xMaxEdgeField = new JTextField (5);
		xMaxEdgeField.setText (""+xMaxEdgeSize);
		zMaxEdgeField = new JTextField (5);
		zMaxEdgeField.setText (""+zMaxEdgeSize);

		v7.add (layerEdge);
		v7.add (new JWidthLabel (Translator.swap ("FiLayerMeasuredDialog.zMaxEdgeSize")+" :", 124));
		v7.add (zMaxEdgeField);
		v7.add (new JWidthLabel (Translator.swap ("FiLayerMeasuredDialog.xMaxEdgeSize")+" :", 50));
		v7.add (xMaxEdgeField);


		boxShape.add (v6);
		boxShape.add (v7);

		shapeDef.add (boxShape);
		shapeBox.add (shapeDef);



		//LAYER SIZE DEFINITION
		JPanel layerDef = new JPanel (new FlowLayout (FlowLayout.LEFT));
		Border layerEtched = BorderFactory.createEtchedBorder ();
		Border layerBorder = BorderFactory.createTitledBorder (layerEtched, Translator.swap ("FiLayerMeasuredDialog.layerDimension"));
		layerDef.setBorder (layerBorder);
		Box boxLayer = Box.createVerticalBox ();

		JPanel v8 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel v9 = new JPanel (new FlowLayout (FlowLayout.LEFT));
		JPanel v10 = new JPanel (new FlowLayout (FlowLayout.LEFT));

		widthMinField = new JTextField (5);
		widthMinField.setText (""+widthMinSize);
		widthMaxField= new JTextField (5);
		widthMaxField.setText (""+widthMaxSize);
		spreadMaxField= new JTextField (5);
		spreadMaxField.setText (""+spreadMaxSize);

		v8.add (new JWidthLabel (Translator.swap ("FiLayerMeasuredDialog.widthMinSize")+" :", 150));
		v8.add (widthMinField);
		v9.add (new JWidthLabel (Translator.swap ("FiLayerMeasuredDialog.widthMaxSize")+" :", 150));
		v9.add (widthMaxField);
		v10.add (new JWidthLabel (Translator.swap ("FiLayerMeasuredDialog.spreadMaxSize")+" :", 150));
		v10.add (spreadMaxField);
		widthMinField.setEnabled (false);
		widthMaxField.setEnabled (false);
		boxLayer.add (v8);
		boxLayer.add (v9);
		boxLayer.add (v10);

		layerDef.add (boxLayer);
		shapeBox.add (layerDef);

		shapePanel.add (shapeBox);
		shapeScroll.getViewport().setView(shapePanel);


		// Control panel
		JPanel controlPanel = new JPanel (new FlowLayout (FlowLayout.CENTER));
		save = new JButton (Translator.swap ("FiLayerMeasuredDialog.save"));
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

		setTitle (Translator.swap ("FiLayerMeasuredDialog.title"));
		setPreferredSize (new Dimension(450,300));

		setModal (true);
	}

	public long getSampleId() {
		return sampleId;
	}
	public long getSampleEdgeId() {
		return sampleEdgeId;
	}
	public long getShapeId() {
		return shapeId;
	}
}

