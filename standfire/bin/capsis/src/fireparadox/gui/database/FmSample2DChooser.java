package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.Translator;
import capsis.commongui.util.Helper;
import capsis.util.Drawer;
import capsis.util.Panel2D;
import fireparadox.model.FmModel;
import fireparadox.model.FmInitialParameters;
import fireparadox.model.database.FmDBPlant;
import fireparadox.model.database.FmDBShape;

/**
 * FiSample2DChooser : Column construction for shape sample crown 2D design
 *
 * @author I. Lecomte - March 2008
 */
public class FmSample2DChooser extends AmapDialog implements Drawer, ActionListener {

	private FmModel model;
	private FmInitialParameters settings;

	//shape information
	private FmDBShape sample;
	private FmDBPlant plant;
	private long shapeId;							//id in the database
	private String shapeKind;						//XZ, XZ_XZ, XYZ
	private int fuelType;							//1=plant 2=layer >3=sample
	private boolean edge, isLayer;

	//Grid of value
	private int [][][] values;						//voxels values after update
	private int iNbVoxels, jNbVoxels, kNbVoxels;	//grid dimensions in number of voxels
	private int iMax, jMax, kMax;					//grid dimensions in cm
	private int iVoxelSize, jVoxelSize, kVoxelSize;				//Size of the voxels in cm

	//Panel 2D
	private Panel2D panel2D;
	private Rectangle2D.Double userRectangle;
	private Collection<Rectangle2D.Double> voxelList;		//list of 2D cells
	private JScrollPane grid2D;
	private int iMargin, kMargin;

	//voxel selection coordinate
	private int iSelected, jSelected, kSelected;
	private int top, center, bottom;
	private int kPrevious;

	//Color legend
	private FmColorLegend colorLegendPanel;

	//Validation control
	private JButton save;
	private JButton cancel;
	private JButton help;


	/**	Constructor
	 */
	public FmSample2DChooser () {
		super ();
	}

	public FmSample2DChooser (FmModel _model, FmDBShape _sample,  boolean _isLayer) {

		super ();

		model = _model;
		settings = model.getSettings ();
		sample = _sample;
		isLayer = _isLayer;
		shapeId = -1;

		plant = sample.getPlant();
		fuelType = sample.getFuelType();
		shapeKind = sample.getShapeKind ();
		edge = false;
		if (fuelType == 5)  edge = true;



		//the column dimension is crown height
		iVoxelSize = (int) (sample.getVoxelXSize() * 100);	//convert m to cm
		jVoxelSize = (int) (sample.getVoxelYSize() * 100);
		kVoxelSize = (int) (sample.getVoxelZSize() * 100);


		kNbVoxels = (int) ((sample.getZMax () * 100) / kVoxelSize);
		iNbVoxels = 1;
		jNbVoxels = 1;

		//Create 2D grid and display
		createGrid ();
		createUI ();
		setSize (400, 600);
		show ();


	 }
	/**	Create 2D grid
	 */
	private void createGrid () {

		values = new int[iNbVoxels][jNbVoxels][kNbVoxels];

		iMargin = 20;
		kMargin = 20;

		iSelected = 0;
		jSelected = 0;
		kSelected = -1;

		//Default color values depending of column height
		if (kNbVoxels == 1) {
			values[0][0][0] = FmColorLegend.TOP_VALUE;
			top = 0;
		}
		else if (kNbVoxels == 2) {
			values[0][0][1] = FmColorLegend.TOP_VALUE;
			values[0][0][0] = FmColorLegend.BOTTOM_VALUE;
			top = 1;
			bottom = 0;
		}
		else if (kNbVoxels == 3) {
			values[0][0][2] = FmColorLegend.TOP_VALUE;
			values[0][0][1] = FmColorLegend.CENTER_VALUE;
			values[0][0][0] = FmColorLegend.BOTTOM_VALUE;
			kSelected = 1;
			top = 2;
			center = 1;
			bottom = 0;
		}
		else if (kNbVoxels > 3) {
			int mid = kNbVoxels / 2;
			values[0][0][kNbVoxels-1] = FmColorLegend.TOP_VALUE;
			values[0][0][mid] = FmColorLegend.CENTER_VALUE;
			values[0][0][0] = FmColorLegend.BOTTOM_VALUE;
			top = kNbVoxels-1;
			kSelected = mid;
			kPrevious = mid;
			center = mid;
			bottom = 0;
		}

		iMax = iNbVoxels * iVoxelSize;
		jMax = jNbVoxels * jVoxelSize;
		kMax = kNbVoxels * kVoxelSize;
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

	/**	Validation : Biomass edition for this sample
	 */
	private void validateAction () {

		if ((top == -1) && (top == -1)  && (top == -1) ) {
			JOptionPane.showMessageDialog (this, Translator.swap ("FiSample2DChooser.fillOneVoxel"),
			Translator.swap ("Shared.warning"), JOptionPane.WARNING_MESSAGE );
		}
		else {
			FmSample2DEditor dialog = new FmSample2DEditor (model, sample, top, center, bottom);
			shapeId = dialog.getShapeId();
			setValidDialog (true);
		}
	 }


	/**	Drawing delegation : should draw something in the given user space Graphics
	*/
	public void draw (Graphics g, Rectangle2D.Double r) {

		NumberFormat f = NumberFormat.getInstance ();
		f.setMinimumFractionDigits (0);
		f.setMaximumFractionDigits (2);

		Graphics2D g2 = (Graphics2D) g;
		g2.setColor (Color.lightGray);

		Rectangle2D.Double redCell = null;

		int i = 0;
		int j = 0;
		int k = 0;

		voxelList = new ArrayList();

		//Draw the voxels on  horizontal lines
		int ci = 0;
		while (ci < iMax) {

			int ck = 0;
			k = 0;
			//Draw the voxels on vertical columns
			while (ck < kMax) {

				Rectangle2D.Double cell2 = new Rectangle2D.Double (ci, ck, iVoxelSize, kVoxelSize);

				//draw the cell in the right color
				Color c2 = 	colorLegendPanel.getColorValue (values[i][j][k]);
				g2.setColor (c2);
				g2.fill (cell2);
				g2.setColor (Color.lightGray);
				g2.draw (cell2);

				//to keep reference of the selected cell
				if ((ci==iSelected*iVoxelSize) && (ck==kSelected*kVoxelSize))
					redCell = cell2;

				voxelList.add (cell2);	//add the voxel in the list for selection later

				ck += kVoxelSize;	//next column
				k++;

			}

			ci += iVoxelSize;	//next line
			i++;
		}

		//draw in red the selected cell
		if (redCell != null) {
			g2.setColor (Color.RED);
			g2.draw (redCell);
		}

		//X graduation
		g2.setFont (g2.getFont ().deriveFont (Font.BOLD,12f));
		g2.setColor (Color.black);

		//Y graduation
		g2.drawString ("0", iMax+10, 0);
		g2.drawString ("25", iMax+10, 25);

		if (kMax > 50)  g2.drawString ("50", iMax+10, 50);
		if (kMax > 100) g2.drawString ("100", iMax+10, 100);
		if (kMax > 150) g2.drawString ("150", iMax+10, 150);
		if (kMax > 300) g2.drawString ("300", iMax+10, 300);
		if (kMax > 600) g2.drawString ("600", iMax+10, 600);
	}

	/**	Voxel selection = affect the right color
	*/
	public JPanel select (Rectangle.Double r, boolean notUsed) {

		iSelected = 0;
		jSelected = 0;

		//Searching for the voxel in the selection
		for (Iterator cells = voxelList.iterator (); cells.hasNext ();) {
			Rectangle2D.Double c = (Rectangle2D.Double) cells.next ();

			if (c.intersects (r)) {
				int newSelected = (int) (c.getY() / kVoxelSize);

				//changing voxel color on top
				if (newSelected == 0)  {
					if (values[0][0][newSelected] == FmColorLegend.BOTTOM_VALUE) {
						values[0][0][newSelected] = FmColorLegend.NO_VALUE;
						bottom = -1;
					}
					else {
						values[0][0][newSelected] = FmColorLegend.BOTTOM_VALUE;
						bottom = newSelected;
					}

				}
				//changing voxel color on bottom
				else if (newSelected == kNbVoxels-1) {
					if (values[0][0][newSelected] == FmColorLegend.TOP_VALUE) {
						values[0][0][newSelected] = FmColorLegend.NO_VALUE;
						top = -1;
					}
					else {
						values[0][0][newSelected] = FmColorLegend.TOP_VALUE;
						top = newSelected;
					}
				}
				//changing other voxel color (center)
				else  {
					if (values[0][0][newSelected] == FmColorLegend.CENTER_VALUE) {
						values[0][0][kPrevious] = FmColorLegend.NO_VALUE;
						kPrevious = newSelected;
						kSelected = -1;
						center = -1;
					}
					else {
						values[0][0][newSelected] = FmColorLegend.CENTER_VALUE;
						if (newSelected != kPrevious)
							values[0][0][kPrevious] = FmColorLegend.NO_VALUE;
						kSelected = newSelected;
						kPrevious = newSelected;
						center = newSelected;
					}
				}


			}
		}

		refresh ();

		return null;
	}

	private void reBuildPanel2D () {
		grid2D = new JScrollPane ();
		userRectangle = new Rectangle2D.Double (
				0, 0, iMax, kMax);
		panel2D = new Panel2D (this, userRectangle, iMargin, kMargin);
		panel2D.setZoomEnabled (false);
		panel2D.setMoveEnabled (false);
		grid2D.getViewport().setView(panel2D);

		getContentPane ().add (grid2D, BorderLayout.CENTER);
		getContentPane ().validate();
		refresh ();
	}

	private void refresh () {
		panel2D.reset ();
		panel2D.repaint ();
	}

	/**	Initialize the GUI.
	*/
	private void createUI () {

		/*********** 2D grid  **************/
	    grid2D = new JScrollPane ();
		userRectangle = new Rectangle2D.Double (0, 0, iMax, kMax);

		panel2D = null;
		panel2D = new Panel2D (this, userRectangle, iMargin, kMargin);
		panel2D.setZoomEnabled (true);
		panel2D.setMoveEnabled (false);
		grid2D.getViewport().setView(panel2D);

		validate();
		refresh ();


		/*********** Fuel info and color legend panel **************/
		ColumnPanel legend = new ColumnPanel ();

		FmShapeInfoPanel fuelInfoPanel = new FmShapeInfoPanel (sample);
		legend.add (fuelInfoPanel);

		colorLegendPanel = new FmColorLegend (false, true, true, true);
		legend.add (colorLegendPanel);


		/*********** Control panel **************/
		JPanel controlPanel = new JPanel ();
		controlPanel.setLayout (new FlowLayout (FlowLayout.LEFT));
		save = new JButton (Translator.swap ("FiSample2DChooser.validate"));
		save.addActionListener (this);
		controlPanel.add (save);
		cancel = new JButton (Translator.swap ("Shared.cancel"));
		cancel.addActionListener (this);
		controlPanel.add (cancel);
		help = new JButton (Translator.swap ("Shared.help"));
		help.addActionListener (this);
		controlPanel.add (help);

		getContentPane ().setLayout (new BorderLayout ());
		getContentPane ().add (grid2D, BorderLayout.CENTER);
		getContentPane ().add (legend, BorderLayout.EAST);
		getContentPane ().add (controlPanel, BorderLayout.SOUTH);


		if (sample.getFuelType() == 3) {
			setTitle (Translator.swap ("FiSample2DChooser.title"));
		}
		else if (sample.getFuelType() == 4) {
			setTitle (Translator.swap ("FiSample2DChooser.titleCore"));
		}
		else if (sample.getFuelType() == 5) {
			setTitle (Translator.swap ("FiSample2DChooser.titleEdge"));
		}


		setModal (true);
	}

	/** Return the new shape id
	 */
	public long getShapeId () {
		return shapeId;
	}
}
