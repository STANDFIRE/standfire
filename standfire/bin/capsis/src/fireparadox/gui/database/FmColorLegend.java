package fireparadox.gui.database;


import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

import jeeb.lib.util.ColoredButton;
import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.LinePanel;
import jeeb.lib.util.ListenedTo;
import jeeb.lib.util.Listener;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;


/** FiColorLegend : color legend in fuel voxels editor
 *                  only 3 colors are available for 3 different values : TOP, CENTER, BOTTOM
 *                  if value = 0, the voxel is empty
 *
 * @author I. Lecomte - September 2009
 */
public class FmColorLegend extends JPanel implements ActionListener, ListenedTo  {

	// One color and value for each type of cube
	private Map<Integer,Color> colorMap;
	public static int TOP_VALUE = 1;
	public static int CENTER_VALUE = 2;
	public static int BOTTOM_VALUE = 3;
	public static int NO_VALUE = 0;

	//One button for each color
	private JLabel labelTop, labelCenter, labelBottom,  labelNoset;
	private ColoredButton top, center, bottom, notSet;

	private int currentValue = -1;
	private boolean isActive = false;
	private boolean isTop, isCenter, isBottom;

	//List of listener for event dispaching
	private HashSet<Listener> listeners;


	/**	Constructor.
	*/
	public FmColorLegend (boolean _isActive, boolean _isTop, boolean _isCenter, boolean _isBottom)  {
		super ();
		isActive = _isActive;
		isTop 	 = _isTop;
		isCenter = _isCenter;
		isBottom = _isBottom;

		colorMap = new HashMap<Integer,Color> ();
		colorMap.put (0, Color.WHITE);
		colorMap.put (1, new Color (0, 51, 0));
		colorMap.put (2, new Color (0, 153, 0));
		colorMap.put (3, new Color (153, 255, 153));


		createUI ();
		show ();
	}


	/**	Initialize the GUI.
	*/
	private void createUI () {

		this.setLayout(new FlowLayout (FlowLayout.LEFT)) ;

		// LEGEND panel
		ColumnPanel legend = new ColumnPanel (Translator.swap ("FiColorLegend.legend"));

		if (isTop) {
			LinePanel l1 = new LinePanel ();
			top = new ColoredButton ();
			top.colorize (colorMap.get(1) , 0);
			if (isActive) top.addActionListener (this);
			l1.add (top);
			labelTop = new JLabel (Translator.swap ("FiColorLegend.top"));
			l1.add (labelTop);
			l1.addGlue ();
			legend.add (l1);
		}

		if (isCenter) {
			LinePanel l2 = new LinePanel ();
			center = new ColoredButton ();
			center.colorize (colorMap.get(2), 0);
			if (isActive) center.addActionListener (this);
			l2.add (center);
			labelCenter = new JLabel (Translator.swap ("FiColorLegend.center"));
			l2.add (labelCenter);
			l2.addGlue ();
			legend.add (l2);
		}

		if (isBottom) {
			LinePanel l3 = new LinePanel ();
			bottom = new ColoredButton ();
			bottom.colorize (colorMap.get(3), 0);
			if (isActive) bottom.addActionListener (this);
			l3.add (bottom);
			labelBottom = new JLabel (Translator.swap ("FiColorLegend.bottom"));
			l3.add (labelBottom);
			l3.addGlue ();
			legend.add (l3);
		}



		LinePanel l4 = new LinePanel ();
		notSet = new ColoredButton ();
		notSet.colorize (colorMap.get(0), 0);
		if (isActive) notSet.addActionListener (this);
		l4.add (notSet);
		labelNoset = new JLabel (Translator.swap ("FiColorLegend.notSet"));
		l4.add (labelNoset);
		l4.addGlue ();
		legend.add (l4);

		this.add (legend);

	}

	/**	Some button was hit by user.
	*/
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (top)) {
			changeColor (TOP_VALUE);
			tellSomethingHappened (TOP_VALUE);
		} else if (evt.getSource ().equals (center)) {
			changeColor (CENTER_VALUE);
			tellSomethingHappened (CENTER_VALUE);
		} else if (evt.getSource ().equals (bottom)) {
			changeColor (BOTTOM_VALUE);
			tellSomethingHappened (BOTTOM_VALUE);
		} else if (evt.getSource ().equals (notSet)) {
			changeColor (NO_VALUE);
			tellSomethingHappened (NO_VALUE);
		}
	}

	/**	Click on color button
	*/
	public void changeColor (int newValue) {

		if (currentValue == TOP_VALUE) labelTop.setForeground(Color.BLACK);
		else if (currentValue == CENTER_VALUE) labelCenter.setForeground(Color.BLACK);
		else if (currentValue == BOTTOM_VALUE) labelBottom.setForeground(Color.BLACK);
		else if (currentValue == NO_VALUE) labelNoset.setForeground(Color.BLACK);

		currentValue = newValue;

		if (currentValue == TOP_VALUE) labelTop.setForeground(Color.RED);
		else if (currentValue == CENTER_VALUE) labelCenter.setForeground(Color.RED);
		else if (currentValue == BOTTOM_VALUE) labelBottom.setForeground(Color.RED);
		else if (currentValue == NO_VALUE) labelNoset.setForeground(Color.RED);


		repaint();
	}
	/**	Get the color attached to a value
	*/
	public Map<Integer,Color> getColorMap () {
		return colorMap;
	}
	/**	Get the color attached to a value
	*/
	public Color getColorValue (int value) {
		return colorMap.get(value);
	}
	/**	Get the current color value
	*/
	public int getCurrentValue () {
		return currentValue;
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
				Log.println (Log.ERROR, "FiColorLegend.tellSomethingHappened ()",
						"listener caused the following exception, passed: "+l, e);
			}
		}
	}
}

