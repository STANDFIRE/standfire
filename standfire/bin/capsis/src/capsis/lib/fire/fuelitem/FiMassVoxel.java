package capsis.lib.fire.fuelitem;

import java.util.HashMap;
import java.util.Map;

public class FiMassVoxel {
	/**
	 * FiMass structure for the FuelMatrix
	 */
	public int i; // voxel coordinates
	public int j;
	public int k;
	public Map<FiParticle, Double> masses; // mass in kg for particle

	public FiMassVoxel(int i, int j, int k) {
		this.i = i;
		this.j = j;
		this.k = k;
		this.masses = new HashMap<FiParticle, Double>();
	}

	public FiMassVoxel copy() {
		FiMassVoxel cp = new FiMassVoxel(i, j, k);
		for (FiParticle particle : this.masses.keySet()) {
			cp.masses.put(particle, this.masses.get(particle));
		}
		return cp;
	}
}
