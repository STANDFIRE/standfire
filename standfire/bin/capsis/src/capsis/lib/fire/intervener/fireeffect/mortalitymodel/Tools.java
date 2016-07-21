package capsis.lib.fire.intervener.fireeffect.mortalitymodel;

public class Tools {
	/**
	 * compute barkFactor (between 0 and 1) as a function of barkThickness (cm)
	 * required for some mortality models and logit
	 */
	static public double computeBarkFactor(double barkThickness) {
		return 1 - Math.exp(-barkThickness);
	}

	/*
	 * compute logistic probability
	 */
	static public double logit(double linearCombination) {
		return 1 / (1 + Math.exp(linearCombination));
	}

}
