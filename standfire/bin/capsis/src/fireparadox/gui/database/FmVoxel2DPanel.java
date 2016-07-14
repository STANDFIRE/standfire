package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.IconLoader;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Listener;
import jeeb.lib.util.Log;
import jeeb.lib.util.Vertex3d;
import capsis.util.Drawer;
import capsis.util.Panel2D;

/**
 * FiVoxel2DPanel : Fuel crown 2D design (cube method)
 *
 * @author I. Lecomte - March 2008
 */
public class FmVoxel2DPanel extends JPanel implements Drawer, ActionListener, ListenedTo {

	//Grid color values
	private int [][][] gridVoxelValues;						//voxels color values
	private int iNbVoxels, jNbVoxels, kNbVoxels;			//grid dimensions in number of voxels
	private int iSizeVoxels, jSizeVoxels, kSizeVoxels;		//Size of the voxels in cm
	private int shapeType; 									//1=plant 2=layer 3=unique 4=core 5=edge
	private String shapeKind;								//XZ XY_XZ XYZ

	//Panel 2D
	private JScrollPane scroll;
	private Panel2D panel2D;
	private Collection<Rectangle2D.Double> cellList;		//list of 2D cells
	private Rectangle2D.Double userRectangle;
	private int iMargin, kMargin;							//margin

	//voxel display slide selection
	private int depthSelected;
	private int viewSelected;
	private int colorSelected;

	//selected voxel on click
	private int iSelected, jSelected, kSelected;
	private Rectangle2D.Double selectedCell = null;

	//Color control
	private Map<Integer,Color> colorMap;


	// ListenedTo interface
	private HashSet<Listener> listeners;

	//Action buttons
	private boolean isUpdateActive;
	private JButton copyRight;
	private JButton copyLeft;
	private JButton plusRight;
	private JButton plusLeft;
	private JButton lessRight;
	private JButton lessLeft;
	private JButton plusTop;
	private JButton plusBottom;
	private JButton lessTop;
	private JButton lessBottom;


	//for copy/paste
	private int [][] tempValues;
	private boolean copy = false;

	private JButton copyButton;
	private JButton pasteButton;


	public FmVoxel2DPanel (int [][][] gridVoxelValues,
							int iNbVoxels, int jNbVoxels, int kNbVoxels,
							int iSizeVoxels, int jSizeVoxels, int kSizeVoxels,
							Map<Integer,Color> colorMap,
							boolean update, int type, String kind) {
		super ();

		this.gridVoxelValues = gridVoxelValues;
		this.iNbVoxels = iNbVoxels;
		this.jNbVoxels = jNbVoxels;
		this.kNbVoxels = kNbVoxels;

		this.iSizeVoxels = iSizeVoxels;
		this.jSizeVoxels = jSizeVoxels;
		this.kSizeVoxels = kSizeVoxels;

		this.colorMap = colorMap;
		this.isUpdateActive = update;
		this.shapeType = type;
		this.shapeKind = kind;


		//Create 2D grids with crown description
		viewSelected = 1; 	//front
		depthSelected = 0;
		colorSelected = -1;
		iMargin = 20;
		kMargin = 20;
		iSelected = 0;
		jSelected = 0;
		kSelected = 0;


		//creation of temporary matrix for copy/paste action
		int maxSize = Math.max (iNbVoxels, jNbVoxels);
		maxSize = Math.max (maxSize, kNbVoxels);
		tempValues =  new int [maxSize][maxSize];

		createUI ();
		update ();

	 }
	/**	To modify the grid dimension.
	 */
	public void setGridVoxelValues (int [][][] gridVoxelValues, int iNbVoxels, int jNbVoxels, int kNbVoxels){
		this.gridVoxelValues = gridVoxelValues;
		this.iNbVoxels = iNbVoxels;
		this.jNbVoxels = jNbVoxels;
		this.kNbVoxels = kNbVoxels;
		this.refresh();
	}
	/**	Actions.
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (copyLeft)) {
			leftCopyAction ();

		} else if (evt.getSource ().equals (copyRight)) {
			rightCopyAction ();

		} else if (evt.getSource ().equals (plusLeft)) {
			gridSizeAction ("L+");

		} else if (evt.getSource ().equals (plusRight)) {
			gridSizeAction ("R+");

		} else if (evt.getSource ().equals (lessLeft)) {
			gridSizeAction ("L-");

		} else if (evt.getSource ().equals (lessRight)) {
			gridSizeAction ("R-");

		} else if (evt.getSource ().equals (plusTop)) {
			gridSizeAction ("T+");

		} else if (evt.getSource ().equals (plusBottom)) {
			gridSizeAction ("B+");

		} else if (evt.getSource ().equals (lessTop)) {
			gridSizeAction ("T-");

		} else if (evt.getSource ().equals (lessBottom)) {
			gridSizeAction ("B-");

		} else if (evt.getSource ().equals (copyButton)) {
			copyAction ();

		} else if (evt.getSource ().equals (pasteButton)) {
			pasteAction ();
		}

	}

	/**	Draws the whole matrix according to the current viewPoint
	*/
	public void draw (Graphics g, Rectangle.Double r) {


		cellList = new ArrayList<Rectangle2D.Double>();

		Graphics2D g2 = (Graphics2D) g;
		int j = depthSelected;
		for (int i = 0; i < getNi (); i++) {
			for (int k = 0; k < getNk () ; k++) {

				boolean drawn = drawVoxel (g2, r, i, j, k);
			}
		}

		//draw in red the selected cell
		if (selectedCell != null) {
			g2.setColor (Color.RED);
			g2.draw (selectedCell);
		}


		//TYPE OF GRID
		g2.setColor (Color.RED);
		if (shapeType == 4) g2.drawString ("CORE", (getNi()*getDi())/2, getNk()*getDk()+10);
		if (shapeType == 5) g2.drawString ("EDGE", (getNi()*getDi())/2, getNk()*getDk()+10);


		//Z graduation
		NumberFormat f = NumberFormat.getInstance ();
		f.setMinimumFractionDigits (0);
		f.setMaximumFractionDigits (2);
		g2.setFont (g2.getFont ().deriveFont (Font.BOLD,12f));
		g2.setColor (Color.black);

		for (int xk=0; xk < getNk(); xk++) {
			g2.drawString (""+xk*getDk(), (getNi()*getDi())+10, xk*getDk());
		}


	}

	/**	Draws a voxel according to current viewpoint.
	*	If no color found for this voxel, does not draw it.
	*	Returns true if a voxel was drawn.
	*/
	private boolean drawVoxel (Graphics2D g2, Rectangle.Double r, int i, int j, int k) {

		int voxelValue = getVoxel (i, j, k);

		Rectangle2D.Double cell2 = new Rectangle2D.Double (i*getDi(), k*getDk(), getDi(), getDk());

		//draw the cell in the right color
		Color c2 = 	colorMap.get (voxelValue);
		g2.setColor (c2);
		g2.fill (cell2);
		g2.setColor (Color.lightGray);
		g2.draw (cell2);
		cellList.add (cell2);

		//keep reference of the selected cell
		if ((i == iSelected) && (k == kSelected)) {
			selectedCell = cell2;
		}

		return true;
	}

	/**	+ or -  was hit
	*/
	private void gridSizeAction (String action) {
		tellSomethingHappened (action);
	}


	/**	RIGHT copy  was hit
	*/
	private void rightCopyAction () {

		tellSomethingHappened (new Vertex3d(0,0,0)); //JUST TO TRIGGER HISTORY FOR UNDO/REDO !!!

		int i = 0;
		int iCopy = getNi() - 1;
		int j = depthSelected;

		//copy the cells on each X lines from 0 to midsize
		while (i < (getNi()/2)) {
			int k = 0;
			while (k < getNk()) {
				int value = getVoxel(i, j, k);
				setVoxel(iCopy, j, k, value);
				k++;
			}
			iCopy--;
			i++;
		}

		update();
	}


	/**	LEFT copy  was hit
	*/
	private void leftCopyAction () {

		tellSomethingHappened (new Vertex3d(0,0,0)); //JUST TO TRIGGER HISTORY FOR UNDO/REDO !!!

		int i = getNi() - 1;
		int iCopy = 0;
		int j = depthSelected;

		//copy the cells on each X lines from end to midsize
		while (i >= (getNi()/2)) {
			int k = 0;
			while (k < getNk()) {
				int value = getVoxel(i, j, k);
				setVoxel(iCopy, j, k, value);
				k++;
			}
			iCopy++;
			i--;
		}

		update();
	}



	/**	COPY was hit
	*/
	private void copyAction () {

		for (int i=0; i<getNi() ; i++) {
			for (int k=0; k<getNk() ; k++) {
				int value = getVoxel (i, depthSelected, k);
				tempValues [i][k] =  value;
			}
		}
		copy = true;

	}

	/**	PASTE was hit
	*/
	private void pasteAction () {

		if (copy) {
			tellSomethingHappened (new Vertex3d(0,0,0)); //JUST TO TRIGGER HISTORY FOR UNDO/REDO !!!
			for (int i=0; i<getNi() ; i++) {
				for (int k=0; k<getNk() ; k++) {
					int value = tempValues [i][k];
					setVoxel (i, depthSelected, k, value);
				}
			}

			update ();
		}
	}


	/**	View selection
	*/
	public void setView (int view) {

		viewSelected = view;
		if (iSelected > getNi()-1) iSelected = 0;
		if (jSelected > getNj()-1) jSelected = 0;
		if (kSelected > getNk()-1) kSelected = 0;

		update();
	}
	/**	Depth selection
	*/
	public void setDepth (int depth) {

		depthSelected = depth;
		jSelected = depthSelected;

		if (iSelected > getNi()-1) iSelected = 0;
		if (kSelected > getNk()-1) kSelected = 0;

		update();
	}
	/**	Color selection
	*/
	public void setColor (int color) {
		colorSelected = color;

	}

	/**	If ctrl down adds voxels in selection
	*	else clear selection then put voxels in selection
	*/
	public JPanel select (Rectangle.Double r, boolean notUsed) {

		int selectedValue = 0;


		//Searching for the voxel in the selection
		for (Iterator cells = cellList.iterator (); cells.hasNext ();) {
			Rectangle2D.Double c = (Rectangle2D.Double) cells.next ();
			if (c.intersects (r)) {
				iSelected = (int) (c.getX() / getDi());
				kSelected = (int) (c.getY() / getDk());
				jSelected = depthSelected;
				if (colorSelected < 0) {
					selectedValue =  getVoxel (iSelected, jSelected, kSelected);
					tellSomethingHappened (selectedValue);
				}
				else {
					double di = iSelected * 1.0;
					double dj = jSelected * 1.0;
					double dk = kSelected * 1.0;
					tellSomethingHappened (new Vertex3d(di, dj, dk)); //BEFORE UPDATING VOXEL !!!!!
					if (colorSelected < 99)
						setVoxel (iSelected, jSelected, kSelected, colorSelected);
				}
			}
		}


		refresh ();

		return null;
	}

	public int getSelectedVoxelValue () {
		return getVoxel (iSelected, jSelected, kSelected);
	}


	public void changeSelect (int i, int j, int k) {
		iSelected = i;
		jSelected = j;
		kSelected = k;
		refresh ();

	}


	//	Initialize the GUI.
	//
	private void createUI () {

		this.setLayout (new BorderLayout ());

	    scroll = new JScrollPane ();

	    /*********** TOOL Panel  **************/
	    if (isUpdateActive) {
			JPanel toolPanel = new ColumnPanel ();
			JPanel topPanel = new LinePanel ();
			JPanel centerPanel = new LinePanel ();
			JPanel bottomPanel = new LinePanel ();
			JPanel lastPanel = new LinePanel ();

			//copy from left to right
			ImageIcon left = IconLoader.getIcon ("go-previous_16.png");
			ImageIcon right = IconLoader.getIcon ("go-next_16.png");
			copyLeft = new JButton ("", left);
			copyRight = new JButton ("", right);
			copyRight.addActionListener (this);
			copyLeft.addActionListener (this);


			//increase top and bottom size
			plusTop = new JButton ("+");
			plusBottom = new JButton ("+");
			plusTop.addActionListener (this);
			plusBottom.addActionListener (this);
			lessTop = new JButton ("-");
			lessBottom = new JButton ("-");
			lessTop.addActionListener (this);
			lessBottom.addActionListener (this);

			topPanel.add (lessTop);
			topPanel.add (plusTop);
			bottomPanel.add (lessBottom);
			bottomPanel.add (plusBottom);

			//increase left and right size
			//buton not available for 2 * 2D shape
			if (!shapeKind.equals("XZ_YZ")) {
				plusLeft = new JButton ("+");
				plusRight = new JButton ("+");
				plusRight.addActionListener (this);
				plusLeft.addActionListener (this);
				lessLeft = new JButton ("-");
				lessRight = new JButton ("-");
				lessRight.addActionListener (this);
				lessLeft.addActionListener (this);

				centerPanel.add (lessLeft);
				centerPanel.add (plusLeft);
				centerPanel.add (copyLeft);
				centerPanel.add (copyRight);
				centerPanel.add (plusRight);
				centerPanel.add (lessRight);

			}

			else {
				centerPanel.add (copyLeft);
				centerPanel.add (copyRight);
			}

			toolPanel.add (topPanel);
			toolPanel.add (centerPanel);
			toolPanel.add (bottomPanel);

			//copy paste
			if (shapeKind.equals("XYZ")) {
				copyButton = new JButton ("COPY");
				pasteButton = new JButton ("PASTE");
				copyButton.addActionListener (this);
				pasteButton.addActionListener (this);
				lastPanel.add (copyButton);
				lastPanel.add (pasteButton);
				toolPanel.add (lastPanel);
			}

			this.add (toolPanel, BorderLayout.SOUTH);
		}

		this.add (scroll, BorderLayout.CENTER);
	}

	/**	Updates the panel2D.
	*	Must be called after setViewPoint () or setDepth ().
	*/
	public void update () {

		userRectangle = new Rectangle2D.Double (
				0, 0, iNbVoxels*iSizeVoxels, kNbVoxels*kSizeVoxels);


		panel2D = new Panel2D (this, userRectangle, iMargin, kMargin);
		panel2D.setZoomEnabled (true);
		panel2D.setMoveEnabled (false);
		panel2D.setInfoIconEnabled (false);
		scroll.getViewport().setView(panel2D);
	}

	public void refresh () {
		panel2D.reset ();
		panel2D.repaint ();
		update();
	}

	/**	Add a listener to this object.
	*/
	public void addListener (Listener l) {
		if (listeners == null) {listeners = new HashSet<Listener> ();}
		listeners.add (l);
	}

	/**	Remove a listener to this object.
	*/
	public void removeListener (Listener l) {
		if (listeners == null) {return;}
		listeners.remove (l);
	}

	/**	Notify all the listeners by calling their somethingHappened (listenedTo, param) method.
	*/
	public void tellSomethingHappened (Object evt) {
		if (listeners == null) {return;}

		for (Listener l : listeners) {
			try {
				l.somethingHappened (this, evt);
			} catch (Exception e) {
				Log.println (Log.ERROR, "FiVoxel2DPanel.tellSomethingHappened ()",
						"listener caused the following exception, passed: "+l, e);
			}
		}
	}

	/**	Returns number of voxels on K axe according to current viewPoint
	*/
	private int getNi () {
		if ((viewSelected == 1) || (viewSelected == 3)) {		//front rear
			return iNbVoxels;
		} else if ((viewSelected == 2) || (viewSelected == 4)) {		//right left
			return jNbVoxels;
		} else if ((viewSelected == 5) || (viewSelected == 6)) {		//bottom top
			return iNbVoxels;
		}
		return -1;
	}

	/**	Returns number of voxels on K axe according to current viewPoint
	*/
	private int getNj () {
		if ((viewSelected == 1) || (viewSelected == 3)) {		//front rear
			return jNbVoxels;
		} else if ((viewSelected == 2) || (viewSelected == 4)) {		//right left
			return iNbVoxels;
		} else if ((viewSelected == 5) || (viewSelected == 6)) {		//bottom top
			return kNbVoxels;
		}
		return -1;
	}

	/**	Returns number of voxels on K axe according to current viewPoint
	*/
	private int getNk () {
		if ((viewSelected == 1) || (viewSelected == 3)) {		//front rear
			return kNbVoxels;
		} else if ((viewSelected == 2) || (viewSelected == 4)) {		//right left
			return kNbVoxels;
		} else if ((viewSelected == 5) || (viewSelected == 6)) {		//bottom top
			return jNbVoxels;
		}
		return -1;
	}

	/**	Returns voxel size on I axe according to current viewPoint
	*/
	private int getDi () {
		if ((viewSelected == 1) || (viewSelected == 3)) {		//front rear
			return iSizeVoxels;
		} else if ((viewSelected == 2) || (viewSelected == 4)) {		//right left
			return jSizeVoxels;
		} else if ((viewSelected == 5) || (viewSelected == 6)) {		//bottom top
			return iSizeVoxels;
		}
		return -1;
	}

	/**	Returns voxel size on J axe according to current viewPoint
	*/
	private int getDj () {
		if ((viewSelected == 1) || (viewSelected == 3)) {		//front rear
			return jSizeVoxels;
		} else if ((viewSelected == 2) || (viewSelected == 4)) {		//right left
			return iSizeVoxels;
		} else if ((viewSelected == 5) || (viewSelected == 6)) {		//bottom top
			return kSizeVoxels;
		}
		return -1;
	}

	/**	Returns voxel size on K axe according to current viewPoint
	*/
	private int getDk () {
		if ((viewSelected == 1) || (viewSelected == 3)) {		//front rear
			return kSizeVoxels;
		} else if ((viewSelected == 2) || (viewSelected == 4)) {		//right left
			return kSizeVoxels;
		} else if ((viewSelected == 5) || (viewSelected == 6)) {		//bottom top
			return jSizeVoxels;
		}
		return -1;
	}

	/**	Returns the voxel at the given coordinates considering the current viewPoint.
	*/
	private int getVoxel (int a, int b, int c) {
		int i = 0;
		int j = 0;
		int k = 0;
		if (viewSelected == 1) {		//front
			i = a;
			j = b;
			k = c;
		} else if (viewSelected == 3) {	//rear
			i = iNbVoxels - 1 - a;
			j = jNbVoxels - 1 - b;
			k = c;
		} else if (viewSelected == 2) {	//left
			i = iNbVoxels - 1 - b;
			j = a;
			k = c;
		} else if (viewSelected == 4) {	//right
			i = b;
			j = jNbVoxels - 1 - a;
			k = c;
		} else if (viewSelected == 6) {	///top
			i = a;
			j = c;
			k = kNbVoxels - 1 - b;
		} else if (viewSelected == 5) {	//bottom
			i = a;
			j = jNbVoxels - 1 - c;
			k = b;
		}
		return gridVoxelValues[i][j][k];
	}

	/**	Set the voxel at the given coordinates considering the current viewPoint.
	*/
	private void setVoxel (int a, int b, int c, int newValue) {
		int i = 0;
		int j = 0;
		int k = 0;
		if (viewSelected == 1) {		//front
			i = a;
			j = b;
			k = c;
		} else if (viewSelected == 3) {	//rear
			i = iNbVoxels - 1 - a;
			j = jNbVoxels - 1 - b;
			k = c;
		} else if (viewSelected == 2) {	//left
			i = iNbVoxels - 1 - b;
			j = a;
			k = c;
		} else if (viewSelected == 4) {	//right
			i = b;
			j = jNbVoxels - 1 - a;
			k = c;
		} else if (viewSelected == 6) {	///top
			i = a;
			j = c;
			k = kNbVoxels - 1 - b;
		} else if (viewSelected == 5) {	//bottom
			i = a;
			j = jNbVoxels - 1 - c;
			k = b;
		}
		gridVoxelValues[i][j][k] = newValue;

	}



	public int [][][] getNewVoxelValues() {
		return gridVoxelValues;
	}
}
