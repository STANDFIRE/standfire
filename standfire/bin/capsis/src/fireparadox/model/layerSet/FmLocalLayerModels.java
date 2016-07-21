package fireparadox.model.layerSet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/** Contains the property needed to define some local layer models, for some species, read in file with 
 * the FiLocalLayerModelLoader (extension.ioformat)
 * These models can be used to compute the bulkdensities of different understorey species, when their speciesName and Height is known
 *	@author F.Pimont - sept 2009
 */
public class FmLocalLayerModels implements Serializable {

	private class SyntheticProperties {
		public double mvr;
		public double svr;
		public boolean cylinder;
		public double [][] aliveBulkDensityArray; // array height*bulkdensity
		public double [][] deadBulkDensityArray;



		public SyntheticProperties (double mvr,
				double svr, boolean cylinder, double [][] aliveBulkDensityArray,double [][] deadBulkDensityArray) {
			this.mvr=mvr;
			this.svr=svr;
			this.cylinder=cylinder;
			this.aliveBulkDensityArray=aliveBulkDensityArray;
			this.deadBulkDensityArray=deadBulkDensityArray;
		}
	}

	public Map map;


	public FmLocalLayerModels() {
		map = new HashMap <String,SyntheticProperties> ();
	}

	/*public void fill() {
		double [] heights = {0.25,0.5,0.75};
		double [] densities = {3.31,2.17,1.65};	
		double [][] temp = {heights, densities};
		add(FiModel.QUERCUS_COCCIFERA, 500d, 5000d, false, temp, null);
		double [] heights1 = {0.7};
		double [] densities1 = {1.0};	
		double [][] temp1 = {heights1, densities1};
		add(FiModel.AUSTRALIAN_GRASSLAND, 500d, 10000d, false, temp1, null);
		double [] heights2 = {0.25};
		double [] densities2 = {0.442};	
		double [][] temp2 = {heights2, densities2};
		add("Grass", 500d, 10000d, false, temp2, null);
	}*/
	
	public void add(String speciesName, double mvr,
			double svr, boolean isACylinder, double [][] aliveBulkDensityArray,double [][] deadBulkDensityArray) {
		// TODO if (map.containsKey(speciesName)) {
		// TODO somes checks on the properties
		SyntheticProperties properties = new SyntheticProperties(mvr,svr,isACylinder,aliveBulkDensityArray, deadBulkDensityArray);
		map.put(speciesName, properties);
	}
	private SyntheticProperties get(String speciesName) {
		// TODO somes checks on the properties
		return (SyntheticProperties) map.get(speciesName);		
	}
	
	public Set getSpeciesSet () {
		return this.map.keySet();
	}
	public double getMVR (String speciesName) {
		return this.get(speciesName).mvr;
	}
	public double getSVR (String speciesName) {
		return this.get(speciesName).svr;
	}
	public double getLowerBoundary (String speciesName) {
		double min = Double.MAX_VALUE;
		double[][] distrib;
		distrib = this.get(speciesName).aliveBulkDensityArray;
		for (int i=0; i<distrib[0].length;i++) {
			if (distrib[1][i]>0.0) {
				min = Math.min(min, distrib[0][i]);
			}
		}
		distrib = this.get(speciesName).deadBulkDensityArray;
		for (int i=0; i<distrib[0].length;i++) {
			if (distrib[1][i]>0.0) {
				min = Math.min(min, distrib[0][i]);
			}
		}
		return min;
	}
	public double getUpperBoundary (String speciesName) {
		double max = 0.0;
		double[][] distrib;
		distrib = this.get(speciesName).aliveBulkDensityArray;
		for (int i=0; i<distrib[0].length;i++) {
			if (distrib[1][i]>0.0) {
				max = Math.max(max, distrib[0][i]);
			}
		}
		distrib = this.get(speciesName).deadBulkDensityArray;
		for (int i=0; i<distrib[0].length;i++) {
			if (distrib[1][i]>0.0) {
				max = Math.max(max, distrib[0][i]);
			}
		}
		return max;
	}
	public boolean isACylinder (String speciesName) {
		return this.get(speciesName).cylinder;
	}
	public double computeBulkDensity(String speciesName, double height, boolean alive) {
		double[][] distrib;
		if (alive) {
			distrib = this.get(speciesName).aliveBulkDensityArray;
		} else {
			distrib = this.get(speciesName).deadBulkDensityArray;
		}
		if (distrib == null) {
			return 0d;
		}
		int n=distrib[0].length;
		if (height<=distrib[0][n-1]&&height>=0.0) {
			double aH = distrib[0][0];
			double aD = distrib[1][0];
			if (height<=aH) {
				return aD;
			}
			for (int i=1; i<n; i++) {
				double bH = distrib[0][i];
				double bD = distrib[1][i];
				if (height<=bH) 
					return ((bH-height)*aD+(height-aH)*bD)/(bH-aH);
			}
		}	
		return distrib[1][n-1]*distrib[0][n-1]/height;
	}

}
