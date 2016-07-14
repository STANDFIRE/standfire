package fireparadox.model.database;

import java.io.Serializable;

/**	FiLTVoxel: a voxel in the description of the crown of a FiPlant
*	or a FiLayer.
*	LT means Lite description, compared to DB, used for objects related
*	to database edition.
*	All the voxels of a given FiPlant / FiLayer have the same size
*	(see dx, dy, dz in the owner). The voxel can be located in a matrix
*	with i, j and k (the matrix may have empty parts).
*	The voxel contains dead and alive biomasses in two maps. For each map,
*	it is possible to get the biomass for a given family name (i.e. particle
*	name: leaves, twigs...).
*
*	@author F. Pimont - july 2009
*/
public class FmLTVoxel implements Serializable {
	// Coordinates in the crown matrix of the Plant / Layer
	private int i;
	private int j;
	private int k;

	// These 2 maps stay null if nothing is added inside
	private FmLTFamilyProperty aliveBiomasses;
	private FmLTFamilyProperty deadBiomasses;


    /**	Constructor
    */
	public FmLTVoxel (int _i, int _j, int _k) {
		i = _i;
		j = _j;
		k = _k;
	}

	public int getI () {return i;}
	public int getJ () {return j;}
	public int getK () {return k;}

	public FmLTFamilyProperty getAliveBiomasses () {return aliveBiomasses;}
	public FmLTFamilyProperty getDeadBiomasses () {return deadBiomasses;}

	/**	Return alive biomass for the given family of particles.
	*/
	public double getAliveBiomass (String familyName) {
		if (aliveBiomasses == null) {return 0d;}	// No alive biomass, return 0.
		return aliveBiomasses.get (familyName);
	}

	/**	Return dead biomass for the given family of particles.
	*/
	public double getDeadBiomass (String familyName) {
		if (deadBiomasses == null) {return 0d;}		// No dead biomass, return 0.
		return deadBiomasses.get (familyName);
	}

	/**	Set biomass for a given family of particles.
	*	If the related alive/dead biomass map is null, create it on the fly.
	*/
	public void setBiomass (String familyName, boolean alive, double value) {
		if (alive) {
			if (aliveBiomasses == null) {aliveBiomasses = new FmLTFamilyProperty ();}
			aliveBiomasses.put (familyName, value);
		} else {
			if (deadBiomasses == null) {deadBiomasses = new FmLTFamilyProperty ();}
			deadBiomasses.put (familyName, value);
		}
	}

	public void setAliveBiomasses (FmLTFamilyProperty biomass) {
		aliveBiomasses = biomass;
	}
	public void setDeadBiomasses (FmLTFamilyProperty biomass) {
		deadBiomasses = biomass;
	}
	/**	String description of the biomasses inside the voxel.
	*/
	@Override
	public String toString () {
		StringBuffer b = new StringBuffer ("Voxel ");
		b.append (i);
		b.append (' ');
		b.append (j);
		b.append (' ');
		b.append (k);
		b.append (" alive: ");
		b.append (aliveBiomasses.toString ());
		b.append (" dead: ");
		b.append (deadBiomasses.toString ());

		return b.toString ();
	}


	/**	Test method
	*	cd capsis4/bin
	*	java -cp .:../ext/* fireparadox.model.FiLTVoxel
	*/
	public final static void main (String[] args) {
		FmLTVoxel voxel = new FmLTVoxel (0, 0, 0);

		// for better understanding below
		boolean alive = true;
		boolean dead = false;

		voxel.setBiomass ("Leaves", alive, 0.45);
		voxel.setBiomass ("Twigs", alive, 0.65);
		voxel.setBiomass ("Twigs", dead, 0.23);

		System.out.println (voxel.toString ());

	}


}
