package capsis.extension.intervener.simcopintervener;

import jeeb.lib.util.Translator;

public class SizeClassGenericEnums {

	public enum AlgorithmType {

		JMORandomSizeClassPick (Translator.swap ("JMORandomSizeClassPick")), MPSequentialSizeClassPickFromSmallest (
				Translator.swap ("MPSequentialSizeClassPickFromSmallest")), MPSequentialSizeClassPickFromLargest (
				Translator.swap ("MPSequentialSizeClassPickFromLargest"));

		private String displayString;

		private AlgorithmType (String displayString) {
			this.displayString = displayString;
		}

		@Override
		public String toString () {
			return displayString;
		}
	}

	public enum AutoThinTargetValueType {
	
	    DensityAfterThinning(Translator.swap("densityAfterThinning")),
	    NbTreesToPickPerHa(Translator.swap("nbTreesToPickPerHa"));
	    private String displayString;
	
	    private AutoThinTargetValueType(String displayString) {
	        this.displayString = displayString;
	    }
	
	    @Override
	    public String toString() {
	        return displayString;
	    }
	}

	// public static enum AverageDistanceComputationMethod {
	public enum AverageDistanceComputationMethod {
	
		HexMesh (Translator.swap ("hexMesh")), SquareMesh (Translator.swap ("squareMesh"));
	
		private String displayString;
	
		private AverageDistanceComputationMethod (String displayString) {
			this.displayString = displayString;
		}
	
		@Override
		public String toString () {
			return displayString;
		}
	
	}

	public enum SizeClassType {
	
		VolumeClass (Translator.swap ("volumeClass")), DiameterClass (Translator.swap ("diameterClass"));
	
		private String displayString;
	
		private SizeClassType (String displayString) {
			this.displayString = displayString;
		}
	
		@Override
		public String toString () {
			return displayString;
		}
	}
	
}