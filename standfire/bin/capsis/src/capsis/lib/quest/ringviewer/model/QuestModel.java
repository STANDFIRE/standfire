package capsis.lib.quest.ringviewer.model;

import java.awt.Color;

import javax.swing.JPanel;

import jeeb.lib.util.ColorGradient;
import capsis.lib.quest.ringviewer.QuestRing;

/**
 * A model in the QuEST library.
 * 
 * @author Alexis Achim, F. de Coligny - December 2014
 */
public abstract class QuestModel {

	protected static final Color C1 = Color.decode("#00007F");
	protected static final Color C2 = Color.BLUE;
	protected static final Color C3 = Color.decode("#007FFF");
	protected static final Color C4 = Color.CYAN;
	protected static final Color C5 = Color.decode("#7FFF7F");
	protected static final Color C6 = Color.YELLOW;
	protected static final Color C7 = Color.decode("#FF7F00");
	protected static final Color C8 = Color.RED;
	protected static final Color C9 = Color.decode("#7F0000");

	public static final Color[] BLUE_RED_GRADIENT = ColorGradient.createMultiGradient(new Color[] { C1, C2, C3, C4, C5,
			C6, C7, C8, C9 }, 100);

	public ColorGradient colorGradient;
	
	
	public double minValue;
	public double maxValue;
	
	/**
	 * Constructor
	 */
	public QuestModel() {
	}
	
	// May be made better some day
	public void setMinAndMax (double min, double max) {
		minValue = min; 
		maxValue = max;
	}
	
	/**
	 * Returns the name of this model
	 */
	abstract public String getName ();
	
	/**
	 * Returns the value of this model for the given ring
	 */
	abstract public double getValue (QuestRing ring);

	/**
	 * Returns graphical legend to be added next to the graph
	 */
	abstract public JPanel getLegend ();
	
	/**
	 * Returns the textual comment to be added under the graph (may contain litterature references
	 */
	abstract public String getCaption ();
	
	/**
	 * Please keep this function unchanged, the name will appear in the lists.
	 */
	public String toString () {
		return getName ();
	}
}
