package capsis.extension.treelogger.geolog.logcategories;

import capsis.extension.treelogger.geolog.GeoLogTreeData;

public class TesterLibrary {


	
	
	protected static class GenericTester extends Tester {

		private GenericLogCategory logCategory;
		
		protected GenericTester (GeoLogTreeData td, double botHeight_m, GenericLogCategory logCategory) {
			super (td, botHeight_m);
			this.logCategory = logCategory;
		}

		// Returns true if knotDiam_cm <= maxKnotDiam_cm
		// 	&& heartDiam_cm>=minHeartDiam_cm
		// 	&& juveDiam_cm>=maxJuveDiam_cm
		// (negative min/max diams mean no test)
		public boolean isValid (double length_m) {
			double topHeight = getBottomHeight() + length_m;
			boolean valid = true;
			if (logCategory.maxKnotDiam_cm >= 0) {
				double knotDiam_cm =
						getTreeData().getKnottyCoreRadius_mm (topHeight) / 10.0 * 2;
				valid = knotDiam_cm <= logCategory.maxKnotDiam_cm;
			}
			if (valid && logCategory.minHeartDiam_cm >= 0) {
				double heartDiam_cm =
						getTreeData().getHeartWoodRadius_mm (topHeight) / 10.0 * 2;
				valid = heartDiam_cm >= logCategory.minHeartDiam_cm;
			}
			if (valid && logCategory.maxJuveDiam_cm >= 0) {
				double juveDiam_cm =
						getTreeData().getJuvenileWoodRadius_mm (topHeight) / 10.0 * 2;
				valid = juveDiam_cm <= logCategory.maxJuveDiam_cm;
			}
			return valid;
		}
	}

	

	

	



	
	// Diameter tester
	// (uses this.minDiam_cm, this.diamRelPos and this.diamOverBark)
	public static class DiameterTester extends Tester {
	// public class DiameterTester extends Tester {
		private double minDiam_cm;
		private double diamRelPos;
		private boolean diamOverBark;

		//public DiameterTester (TreeData td, double botHeight_m) {
		//	super (td, botHeight_m);
		//}

		public DiameterTester(GeoLogTreeData td, 
				double botHeight_m,
				double minDiam_cm, 
				double diamRelPos,
				boolean diamOverBark) {
			super (td, botHeight_m);
			this.minDiam_cm = minDiam_cm;
			this.diamRelPos = diamRelPos;
			this.diamOverBark = diamOverBark;
		}

		// Returns true if diam >= minDiam_cm considering diamRelPos
		// and diamOverBark
		// (minDiam_cm < 0 means no test)
		public boolean isValid (double length_m) {
			boolean valid = true;
			if (minDiam_cm >= 0) {
				double height = getBottomHeight() + length_m * diamRelPos;
				double diam = getTreeData().getTreeRadius_cm (height, diamOverBark) * 2;
				valid = diam >= minDiam_cm;
				// System.out.println ("DiameterTester : diam="  + diam + " mindiam=" + minDiam_cm + " valid=" + valid);
			}
			return valid;
		}
	}
	
	
	// KnottyCoreTester (static) :
	protected static class KnottyCoreTester extends Tester {
		
		private double maxKnotDiam_cm;

		protected KnottyCoreTester (GeoLogTreeData td, 
				double botHeight_m,
				double maxKnotDiam_cm) {
			super (td, botHeight_m);
			this.maxKnotDiam_cm = maxKnotDiam_cm;
		}

		// Returns true if knotDiam_cm <= maxKnotDiam_cm :
		// (maxKnotDiam_cm<0 means no test)
		public boolean isValid (double length_m) {
			boolean valid = true;
			if (maxKnotDiam_cm >= 0) {
				double topHeight = getBottomHeight() + length_m;
				double knotDiam_cm =
						getTreeData().getKnottyCoreRadius_mm (topHeight) / 10.0 * 2;
				valid = knotDiam_cm <= maxKnotDiam_cm;
			}
			return valid;
		}
	}

	

	
}
