package capsis.commongui.projectmanager;

/**
 * An interface for a colorProvider managing bags of colors. Can return userColors for a given bag
 * index. Colors are different for the same bag. When no color available any more for a given bag
 * index (bag empty), may return a default color.
 * 
 * @author F. de Coligny - September 2012
 */
public interface ColorProvider {

	/**
	 * Draws and returns a color in the given bag.
	 */
	public UserColor getColor (int bag);

	/**
	 * Draws and returns a color in bag 0 (for convenience).
	 */
	public UserColor getColor ();

}
