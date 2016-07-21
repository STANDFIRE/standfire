package capsis.lib.castanea;

import java.io.Serializable;

import jeeb.lib.util.Vertex2d;

/**	FmSpeciesProperties : properties related to castanea species
*
*	@author Hendrik Davi - february 2008
*/
public class FmSpeciesProperties implements Serializable {
	
	private int rank;
	private Vertex2d soilLayersHeights;	// (litterHeight, topHeight)
	

	/**	Constructor.
	*/
	public FmSpeciesProperties (
			int rank, 
			Vertex2d soilLayersHeights) {
		this.rank = rank;
		this.soilLayersHeights = soilLayersHeights;
	}

	public int getRank () {return rank;}
	public double getLitterHeight () {return soilLayersHeights.x;}
	public double getTopHeight () {return soilLayersHeights.y;}

}

