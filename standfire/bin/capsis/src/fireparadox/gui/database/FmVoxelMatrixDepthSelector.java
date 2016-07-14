package fireparadox.gui.database;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import jeeb.lib.util.Check;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Listener;
import jeeb.lib.util.Translator;
import jeeb.lib.util.Vertex2d;
import capsis.util.Drawer;
import capsis.util.Panel2D;


/**	FiVoxelMatrixDepthSelector - a panel showing a simple voxelMatrix
*	(an int[][][]) to select a depth index.
*	See main below for an exemple of use.
*	After creation, it is possible to change the selector context with setMatrix (),
*	setViewPoint () or setDepth (), then update () must be called.
*	@author F. de Coligny - october 2009
*/
public class FmVoxelMatrixDepthSelector extends JPanel implements Drawer,
		ActionListener, ListenedTo {
	static {
		Translator.addBundle("fireparadox.FiLabels");
	}
	// The 6 possible viewPoints
	public enum ViewPoint {FRONT, REAR, LEFT, RIGHT, TOP, BOTTOM}
	//~ public static final double ALPHA = Math.PI / 16;		// alpha default value
	public static final double ALPHA = Math.PI / 4;		// alpha default value
	public static final Color SELECTION_COLOR = Color.RED;

	private int[][][] matrix;
	private int di;	// size of the voxels along the x axis
	private int dj;
	private int dk;
	// For each value in the matrix, a corresponding color for drawing
	private Map<Integer,Color> colorMap;

	private int ni;
	private int nj;
	private int nk;

	private double alpha = ALPHA;
	private Color selectionColor = SELECTION_COLOR;
	// the depth index according to current viewPoint
	private int depth;
	// The face in front of the user
	private ViewPoint viewPoint;

    private Panel2D panel2D;
	private Rectangle.Double userBounds;

	private JScrollPane scroll;
	private JButton front;
	private JButton rear;
	private JButton left;
	private JButton right;
	private JButton top;
	private JButton bottom;
	// Current state
	private JTextField numberOfVovels;
	private JTextField viewPointValue;
	private JTextField depthValue;
	// Depth commands
	private JButton deeper;
	private JButton lessDeep;
	// ListenedTo interface
	private List<Listener> listeners;


	/**	Constructor.
	*	The matrix must be a complete cube.
	*	ViewPoint: i index grows with x axis, j grows with y and ka grows with z.
	*/
	public FmVoxelMatrixDepthSelector (int[][][] matrix,
			int di, int dj, int dk,
			Map<Integer,Color> colorMap) {
		super ();
		this.di = di;
		this.dj = dj;
		this.dk = dk;
		this.colorMap = colorMap;

		createUI ();

		// Default viewPoint and depth
		setViewPoint (ViewPoint.FRONT);
		setDepth (0);
		setMatrix (matrix);

		update ();
	}

	/**	Change the selector matrix.
	*/
	public void setMatrix (int[][][] matrix) {
		this.matrix = matrix;
	}

	/**	Change the selector viewPoint.
	*/
	public void setViewPoint (ViewPoint viewPoint) {
		this.viewPoint = viewPoint;
		viewPointValue.setText (getName (viewPoint) );
		tellSomethingHappened (getName (viewPoint) );
	}

	/**	Change the selector depth.
	*/
	public void setDepth (int depth) {
		this.depth = depth;
		depthValue.setText (""+depth);
		tellSomethingHappened (depth);
	}

	/**	Updates the selector.
	*	Must be called after setMatrix (), setViewPoint () or setDepth ().
	*/
	public void update () {

		// The matrix must be a complete cube
		// ni, nj and nk from the FRONT point of view (default viewPoint)
		ni = matrix.length;
		nj = matrix[0].length;
		nk = matrix[0][0].length;

		double iShift = Math.cos (alpha) * getDj ();	// suspicious... (left / right)
		double kShift = Math.sin (alpha) * getDj ();

		double iExtension = getDi () * getNi () + iShift * getNj ();
		double kExtension = getDk () * getNk () + kShift * getNj ();

		if (depth > getNj ()-1) {setDepth (getNj ()-1);}
		if (depth < 0) {setDepth (0);}


		userBounds = new Rectangle.Double (0, 0, iExtension, kExtension);
		panel2D = new Panel2D (this, userBounds, 20, 20);
		panel2D.setInfoIconEnabled (false);
		scroll.getViewport ().setView (panel2D);

		//tellSomethingHappened (null);	// null: no param
	}

	/**	Draws the whole matrix according to the current viewPoint
	*/
	public void draw (Graphics g, Rectangle.Double r) {

		Graphics2D g2 = (Graphics2D) g;
		int cpt = 0;
		for (int i = 0; i < getNi (); i++) {
			for (int j = getNj () - 1; j >= 0; j--) {	// from rear to front
				for (int k = 0; k < getNk () ; k++) {
					boolean drawn = drawVoxel (g2, r, i, j, k);
					if (drawn) {cpt++;}
				}
			}
		}

		numberOfVovels.setText (""+cpt);
	}

	/**	Draws a voxel according to current viewpoint.
	*	If no color found for this voxel, does not draw it.
	*	Returns true if a voxel was drawn.
	*/
	private boolean drawVoxel (Graphics2D g2, Rectangle.Double r, int i, int j, int k) {
		int voxel = getVoxel (i, j, k);

		// choose the color in the colorMap according to the voxel value
		Color color = colorMap.get (voxel);
		// voxels with a null color are not drawn
		if (color == null) {return false;}

		if (j == depth) {color = selectionColor;}

		double iShift = Math.cos (alpha) * getDj ();
		double kShift = Math.sin (alpha) * getDj ();

		// Four points at the front
		Vertex2d f0 = new Vertex2d (i * getDi () + j * iShift, k * getDk () + j * kShift);
		Vertex2d f1 = new Vertex2d (f0.x, f0.y + getDk ());
		Vertex2d f2 = new Vertex2d (f0.x + getDi (), f0.y + getDk ());
		Vertex2d f3 = new Vertex2d (f0.x + getDi (), f0.y);

		// Four points at the rear
		Vertex2d r0 = new Vertex2d (f0.x + iShift, f0.y + kShift);
		Vertex2d r1 = new Vertex2d (f1.x + iShift, f1.y + kShift);
		Vertex2d r2 = new Vertex2d (f2.x + iShift, f2.y + kShift);
		Vertex2d r3 = new Vertex2d (f3.x + iShift, f3.y + kShift);

		GeneralPath top = new GeneralPath ();
		top.moveTo (f1.x, f1.y);
		top.lineTo (r1.x, r1.y);
		top.lineTo (r2.x, r2.y);
		top.lineTo (f2.x, f2.y);
		top.closePath ();

		GeneralPath right = new GeneralPath ();
		right.moveTo (f2.x, f2.y);
		right.lineTo (r2.x, r2.y);
		right.lineTo (r3.x, r3.y);
		right.lineTo (f3.x, f3.y);

		Shape front = new Rectangle2D.Double (f0.x, f0.y, getDi (), getDk ());

		if (r.intersects (front.getBounds2D ()) || r.intersects (top.getBounds2D ())) {
			g2.setColor (color.brighter ());
			g2.fill (top);
			g2.setColor (Color.GRAY);
			g2.draw (top);

			g2.setColor (color.brighter ());
			g2.fill (right);
			g2.setColor (Color.GRAY);
			g2.draw (right);

			g2.setColor (color);
			g2.fill (front);
			g2.setColor (Color.GRAY.darker());
			g2.draw (front);
		}
		return true;
	}
	public void refresh () {
			panel2D.reset ();
			panel2D.repaint ();
			update();
	}
	/**	Returns the voxel at the given coordinates considering the current
	*	viewPoint.
	*/
	private int getVoxel (int a, int b, int c) {
		int i = 0;
		int j = 0;
		int k = 0;
		if (viewPoint.equals (ViewPoint.FRONT)) {
			i = a;
			j = b;
			k = c;
		} else if (viewPoint.equals (ViewPoint.REAR)) {
			i = ni-1-a;
			j = nj-1-b;
			k = c;
		} else if (viewPoint.equals (ViewPoint.RIGHT)) {
			i = ni-1-b;
			j = a;
			k = c;
		} else if (viewPoint.equals (ViewPoint.LEFT)) {
			i = b;
			j = nj-1-a;
			k = c;
		} else if (viewPoint.equals (ViewPoint.TOP)) {
			i = a;
			j = c;
			k = nk-1-b;
		} else if (viewPoint.equals (ViewPoint.BOTTOM)) {
			i = a;
			j = nj-1-c;
			k = b;
		}
		return matrix[i][j][k];
	}

	/**	No selection here
	*/
	public JPanel select (Rectangle.Double r, boolean more) {
		return null;
	}

	/**	Returns di according to current viewPoint
	*/
	private int getDi () {
		if (viewPoint.equals (ViewPoint.FRONT) || viewPoint.equals (ViewPoint.REAR)) {
			return di;
		} else if (viewPoint.equals (ViewPoint.LEFT) || viewPoint.equals (ViewPoint.RIGHT)) {
			return dj;
		} else if (viewPoint.equals (ViewPoint.TOP) || viewPoint.equals (ViewPoint.BOTTOM)) {
			return di;
		}
		return -1;
	}

	/**	Returns dj according to current viewPoint
	*/
	private int getDj () {
		if (viewPoint.equals (ViewPoint.FRONT) || viewPoint.equals (ViewPoint.REAR)) {
			return dj;
		} else if (viewPoint.equals (ViewPoint.LEFT) || viewPoint.equals (ViewPoint.RIGHT)) {
			return di;
		} else if (viewPoint.equals (ViewPoint.TOP) || viewPoint.equals (ViewPoint.BOTTOM)) {
			return dk;
		}
		return -1;
	}

	/**	Returns dk according to current viewPoint
	*/
	private int getDk () {
		if (viewPoint.equals (ViewPoint.FRONT) || viewPoint.equals (ViewPoint.REAR)) {
			return dk;
		} else if (viewPoint.equals (ViewPoint.LEFT) || viewPoint.equals (ViewPoint.RIGHT)) {
			return dk;
		} else if (viewPoint.equals (ViewPoint.TOP) || viewPoint.equals (ViewPoint.BOTTOM)) {
			return dj;
		}
		return -1;
	}

	/**	Returns ni according to current viewPoint
	*/
	private int getNi () {
		if (viewPoint.equals (ViewPoint.FRONT) || viewPoint.equals (ViewPoint.REAR)) {
			return ni;
		} else if (viewPoint.equals (ViewPoint.LEFT) || viewPoint.equals (ViewPoint.RIGHT)) {
			return nj;
		} else if (viewPoint.equals (ViewPoint.TOP) || viewPoint.equals (ViewPoint.BOTTOM)) {
			return ni;
		}
		return -1;
	}

	/**	Returns nj according to current viewPoint
	*/
	private int getNj () {
		if (viewPoint.equals (ViewPoint.FRONT) || viewPoint.equals (ViewPoint.REAR)) {
			return nj;
		} else if (viewPoint.equals (ViewPoint.LEFT) || viewPoint.equals (ViewPoint.RIGHT)) {
			return ni;
		} else if (viewPoint.equals (ViewPoint.TOP) || viewPoint.equals (ViewPoint.BOTTOM)) {
			return nk;
		}
		return -1;
	}

	/**	Returns nk according to current viewPoint
	*/
	private int getNk () {
		if (viewPoint.equals (ViewPoint.FRONT) || viewPoint.equals (ViewPoint.REAR)) {
			return nk;
		} else if (viewPoint.equals (ViewPoint.LEFT) || viewPoint.equals (ViewPoint.RIGHT)) {
			return nk;
		} else if (viewPoint.equals (ViewPoint.TOP) || viewPoint.equals (ViewPoint.BOTTOM)) {
			return nj;
		}
		return -1;
	}

	/**	Actions.
	 */
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (front)) {
			setViewPoint (ViewPoint.FRONT);
			update ();
		} else if (evt.getSource ().equals (rear)) {
			setViewPoint (ViewPoint.REAR);
			update ();
		} else if (evt.getSource ().equals (left)) {
			setViewPoint (ViewPoint.LEFT);
			update ();
		} else if (evt.getSource ().equals (right)) {
			setViewPoint (ViewPoint.RIGHT);
			update ();
		} else if (evt.getSource ().equals (top)) {
			setViewPoint (ViewPoint.TOP);
			update ();
		} else if (evt.getSource ().equals (bottom)) {
			setViewPoint (ViewPoint.BOTTOM);
			update ();
		} else if (evt.getSource ().equals (depthValue)) {
			int min = 0;
			int max = getNj ()-1;
			String errorMessage = Translator.swap ("FiVoxelMatrixDepthSelector.depthMustBeAnIntegerInTheRange")
					+" ["+min+","+max+"]";
			if (!Check.isInt (depthValue.getText ().trim ())) {
				MessageDialog.print (this, errorMessage);
				return;
			}
			int v = Check.intValue (depthValue.getText ().trim ());
			if (v < min || v > max) {
				MessageDialog.print (this, errorMessage);
				return;
			}
			setDepth (v);
			update ();

		} else if (evt.getSource ().equals (deeper)) {
			if (depth < getNj ()-1) {setDepth (++depth);}
			update ();
		} else if (evt.getSource ().equals (lessDeep)) {
			if (depth > 0) {setDepth (--depth);}
			update ();
		}
	}

	/**	Initialize the GUI.
	*/
	private void createUI () {

		scroll = new JScrollPane ();

		JPanel lateral = new JPanel (new BorderLayout ());

		front = new JButton (Translator.swap ("FiVoxelMatrixDepthSelector.front"));
		front.addActionListener (this);
		rear = new JButton (Translator.swap ("FiVoxelMatrixDepthSelector.rear"));
		rear.addActionListener (this);
		left = new JButton (Translator.swap ("FiVoxelMatrixDepthSelector.left"));
		left.addActionListener (this);
		right = new JButton (Translator.swap ("FiVoxelMatrixDepthSelector.right"));
		right.addActionListener (this);
		top = new JButton (Translator.swap ("FiVoxelMatrixDepthSelector.top"));
		top.addActionListener (this);
		bottom = new JButton (Translator.swap ("FiVoxelMatrixDepthSelector.bottom"));
		bottom.addActionListener (this);

		deeper = new JButton (Translator.swap ("FiVoxelMatrixDepthSelector.deeper"));
		deeper.addActionListener (this);

		lessDeep = new JButton (Translator.swap ("FiVoxelMatrixDepthSelector.lessDeep"));
		lessDeep.addActionListener (this);


		JPanel grid = new JPanel (new GridLayout (9, 1));
		grid.add (front);
		grid.add (rear);
		grid.add (left);
		grid.add (right);
		grid.add (top);
		grid.add (bottom);
		grid.add (new JLabel(""));
		grid.add (deeper);
		grid.add (lessDeep);
		lateral.add (grid, BorderLayout.NORTH);

		ColumnPanel c1 = new ColumnPanel ();
		LinePanel l8 = new LinePanel ();
		l8.add (new JLabel (Translator.swap ("FiVoxelMatrixDepthSelector.numberOfVovels")+" : "));
		numberOfVovels = new JTextField (5);
		numberOfVovels.setEditable (false);
		l8.add (numberOfVovels);
		l8.addStrut0 ();
		c1.add (l8);

		LinePanel l0 = new LinePanel ();
		l0.add (new JLabel (Translator.swap ("FiVoxelMatrixDepthSelector.viewPointValue")+" : "));
		viewPointValue = new JTextField (5);
		viewPointValue.setEditable (false);
		l0.add (viewPointValue);
		l0.addStrut0 ();
		c1.add (l0);

		LinePanel l1 = new LinePanel ();
		l1.add (new JLabel (Translator.swap ("FiVoxelMatrixDepthSelector.depthValue")+" : "));
		depthValue = new JTextField (3);
		depthValue.addActionListener (this);
		l1.add (depthValue);
		l1.addStrut0 ();
		c1.add (l1);

		/*LinePanel l2 = new LinePanel ();
		deeper = new JButton (Translator.swap ("FiVoxelMatrixDepthSelector.deeper"));
		deeper.addActionListener (this);
		l2.add (deeper);
		lessDeep = new JButton (Translator.swap ("FiVoxelMatrixDepthSelector.lessDeep"));
		lessDeep.addActionListener (this);
		l2.add (lessDeep);
		l2.addStrut0 ();
		c1.add (l2);
		c1.addStrut0 ();*/

		lateral.add (c1, BorderLayout.SOUTH);

		setLayout (new BorderLayout ());

		add (scroll, BorderLayout.CENTER);
		add (lateral, BorderLayout.EAST);

	}

	/**	Returns the translated name of the viewpoint to be writen on the gui.
	*/
	private String getName (ViewPoint o) {
		if (o.equals (ViewPoint.FRONT)) {
			return Translator.swap ("FiVoxelMatrixDepthSelector.front");
		} else if (o.equals (ViewPoint.REAR)) {
			return Translator.swap ("FiVoxelMatrixDepthSelector.rear");
		} else if (o.equals (ViewPoint.LEFT)) {
			return Translator.swap ("FiVoxelMatrixDepthSelector.left");
		} else if (o.equals (ViewPoint.RIGHT)) {
			return Translator.swap ("FiVoxelMatrixDepthSelector.right");
		} else if (o.equals (ViewPoint.TOP)) {
			return Translator.swap ("FiVoxelMatrixDepthSelector.top");
		} else if (o.equals (ViewPoint.BOTTOM)) {
			return Translator.swap ("FiVoxelMatrixDepthSelector.bottom");
		}
		return ""+o;
	}

	/**	Accessor for the viewPoint (for the listeners).
	*/
	public ViewPoint getViewPoint () {return viewPoint;}

	/**	Accessor for the depth (for the listeners).
	*/
	public int getDepth () {return depth;}

	/**	ListenedTo interface: add a listener to this object.
	*/
	public void addListener (Listener l) {
		if (listeners == null) {listeners = new ArrayList<Listener> ();}
		listeners.add (l);
	}

	/**	ListenedTo interface: remove a listener to this object.
	*/
	public void removeListener (Listener l) {
		if (listeners == null) {return;}
		listeners.remove (l);
	}

	/**	ListenedTo interface: notify all the listeners by calling
	*	their somethingHappened (listenedTo, param) method.
	*/
	public void tellSomethingHappened (Object param) {
		if (listeners == null) {return;}
		for (Listener l : listeners) {
			l.somethingHappened (this, param);
		}
	}



}

