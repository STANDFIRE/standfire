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


/** FiColorFreeLegend : color legend in fuel voxels editor
 *                      if value = 9, the voxel is filled
 *                      if value = 0, the voxel is empty
 *
 * @author I. Lecomte - October 2009
 */
public class FmColorFreeLegend extends JPanel implements ActionListener, ListenedTo  {

	// One color and value for each type of cube
	private Map<Integer,Color> colorMap;
	public static int SET_VALUE = 9;
	public static int NO_VALUE = 0;
	public static int SELECT = 99;

	//One button for each color
	private JLabel labelSet, labelNoset, labelSelect;
	private ColoredButton set, notSet, select;

	private int currentValue = 99;
	private boolean isActive = false;

	//List of listener for event dispaching
	private HashSet<Listener> listeners;


	/**	Constructor.
	*/
	public FmColorFreeLegend (boolean _isActive)  {
		super ();
		isActive = _isActive;

		colorMap = new HashMap<Integer,Color> ();
		colorMap.put (0, Color.white);
		colorMap.put (9, Color.gray);


		createUI ();
		show ();
	}


	/**	Initialize the GUI.
	*/
	private void createUI () {

		this.setLayout(new FlowLayout (FlowLayout.LEFT)) ;

		// LEGEND panel
		ColumnPanel legend = new ColumnPanel (Translator.swap ("FiColorFreeLegend.legend"));

		LinePanel l1 = new LinePanel ();
		set = new ColoredButton ();
		set.colorize (colorMap.get(9) , 0);
		if (isActive) set.addActionListener (this);
		l1.add (set);
		labelSet= new JLabel (Translator.swap ("FiColorFreeLegend.set"));
		l1.add (labelSet);
		l1.addGlue ();
		legend.add (l1);



		LinePanel l2 = new LinePanel ();
		notSet = new ColoredButton ();
		notSet.colorize (colorMap.get(0), 0);
		if (isActive) notSet.addActionListener (this);
		l2.add (notSet);
		labelNoset = new JLabel (Translator.swap ("FiColorFreeLegend.notSet"));
		l2.add (labelNoset);
		l2.addGlue ();
		legend.add (l2);


		LinePanel l3 = new LinePanel ();
		select = new ColoredButton ();
		select.colorize (colorMap.get(0), 0);
		if (isActive) select.addActionListener (this);
		l3.add (select);
		labelSelect = new JLabel (Translator.swap ("FiColorFreeLegend.select"));
		l3.add (labelSelect);
		l3.addGlue ();
		legend.add (l3);

		if (currentValue == SET_VALUE) labelSet.setForeground(Color.RED);
		else if (currentValue == NO_VALUE) labelNoset.setForeground(Color.RED);
		else if (currentValue == SELECT) labelSelect.setForeground(Color.RED);

		this.add (legend);

	}

	/**	Some button was hit by user.
	*/
	public void actionPerformed (ActionEvent evt) {
		if (evt.getSource ().equals (set)) {
			changeColor (SET_VALUE);
			tellSomethingHappened (SET_VALUE);

		} else if (evt.getSource ().equals (notSet)) {
			changeColor (NO_VALUE);
			tellSomethingHappened (NO_VALUE);

		} else if (evt.getSource ().equals (select)) {
			changeColor (SELECT);
			tellSomethingHappened (SELECT);
		}
	}

	/**	Click on color button
	*/
	public void changeColor (int newValue) {

		if (currentValue == SET_VALUE) labelSet.setForeground(Color.BLACK);
		else if (currentValue == NO_VALUE) labelNoset.setForeground(Color.BLACK);
		else if (currentValue == SELECT) labelSelect.setForeground(Color.BLACK);

		currentValue = newValue;

		if (currentValue == SET_VALUE) labelSet.setForeground(Color.RED);
		else if (currentValue == NO_VALUE) labelNoset.setForeground(Color.RED);
		else if (currentValue == SELECT) labelSelect.setForeground(Color.RED);


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
				Log.println (Log.ERROR, "FiColorFreeLegend.tellSomethingHappened ()",
						"listener caused the following exception, passed: "+l, e);
			}
		}
	}
}

