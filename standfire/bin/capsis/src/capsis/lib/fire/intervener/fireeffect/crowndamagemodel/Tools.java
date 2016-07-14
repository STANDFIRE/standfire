package capsis.lib.fire.intervener.fireeffect.crowndamagemodel;

/**
 * This call contain basic equation from van Wagner to compute the scorch height and wind effect
 */
public class Tools {
	static public double plume(double fireIntensity,
			double ambiantTemperature, double scorchTemperature) {
		return Math.pow(fireIntensity, 2.0 / 3.0)
				/ (scorchTemperature - ambiantTemperature);
	}

	static public double includeWindEffect(double scorchHeight,
			double fireIntensity, double ambiantTemperature, double wind) { // need
		// to
		// be
		// checked:
		// wind-0=>z=0,
		// probably of cos instead of sin
		/*
		 * double tmp = 1.2 * 1.005 * (273.0 + ambiantTemperature) / (9.8 *
		 * fireIntensity); // ambiant=25� for air density and // Cp double
		 * tanAlpha = Math.pow(tmp, 1. / 3.) * wind; return scorchHeight *
		 * Math.cos(Math.atan(tanAlpha));
		 */
		return scorchHeight
				/ Math.sqrt(1.0 + 38.0 * Math.pow(wind, 3.0) / fireIntensity);
		//return scorchHeight * Math.sin(Math.atan(Math.sqrt(0.026*fireIntensity/Math.pow(wind, 3.0))));
		
	}

	

}
