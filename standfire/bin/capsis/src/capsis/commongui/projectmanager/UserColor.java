package capsis.commongui.projectmanager;

import java.awt.Color;

/**
 * A color managed by a colorProvider. When the colorProvider is asked for a color for a given bag
 * index, it returns a userColor and sets it unavailable. When requested for another color for the
 * same bag index, it will return another color. When not needed any more, the user color can be set
 * available again to be returned again at next request to the ColorProvider.
 * 
 * @author F. de Coligny - September 2012
 */
public class UserColor extends Color {

	private boolean available;
	private ColorProvider colorProvider;
	private int bag;

	/**
	 * Constructor. Bag is a valid bag index in the given ColorProvider.
	 */
	public UserColor (ColorProvider colorProvider, int bag, int r, int g, int b) {
		super (r, g, b);
		this.available = true;
		this.colorProvider = colorProvider;
		this.bag = bag;
	}

	/**
	 * Constructor 2.
	 */
	public UserColor (ColorProvider colorProvider, int bag, Color c) {
		super (c.getRed (), c.getGreen (), c.getBlue ());
		this.available = true;
		this.colorProvider = colorProvider;
		this.bag = bag;
	}

	public boolean isAvailable () {
		return available;
	}

	public void setAvailable (boolean available) {
		this.available = available;
	}

	public ColorProvider getColorProvider () {
		return colorProvider;
	}

	public int getBag () {
		return bag;
	}

}
