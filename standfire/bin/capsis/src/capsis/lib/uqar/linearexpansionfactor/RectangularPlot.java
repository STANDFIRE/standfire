package capsis.lib.uqar.linearexpansionfactor;

/**
 * Computes a correction of a competition index for a rectangular plot.
 * 
 * <pre>
 * # under a Linux terminal, from capsis4/
 * java -cp class/:ext/ capsis.lib.uqar.linearexpansionfactor.RectangularPlot
 * 
 * # under a windows terminal, from capsis4\
 * java -cp class/;ext/ capsis.lib.uqar.linearexpansionfactor.RectangularPlot
 * 
 * </pre>
 * 
 * @author Tony Franceschini, F. de Coligny - June 2016
 */
public class RectangularPlot {

	// if trace is set to true, computeCorrection () will write messages in the
	// terminal
	private static boolean trace = false;

	private static double[] affine(double x1, double y1, double x2, double y2) {
		double slope = (y2 - y1) / (x2 - x1);
		double intercept = y2 - slope * x2;
		return new double[] { slope, intercept };
	}

	private static double computeGamma(double x1, double y1, double x2, double y2, double xTarget, double yTarget,
			double circleRadius, double[] linearEquation) {

		double slope = linearEquation[0];
		double intercept = linearEquation[1];

		if (Double.isInfinite(slope)) {
			double xInt1 = x1;
			double xInt2 = x1;

			double A = 1;
			double B = -2 * yTarget;
			double C = yTarget * yTarget + (xInt1 - xTarget) * (xInt1 - xTarget) - circleRadius * circleRadius;
			double discriminant = B * B - 4 * A * C;

			if (discriminant < 0) {
				return 0;

			} else {
				double yInt1 = (-B - Math.sqrt(discriminant)) / (2 * A);
				double yInt2 = (-B + Math.sqrt(discriminant)) / (2 * A);

				if (yInt1 > y2)
					yInt1 = y2;
				if (yInt2 > y2)
					yInt2 = y2;
				if (yInt1 < y1)
					yInt1 = y1;
				if (yInt2 < y1)
					yInt2 = y1;

				double chord = Math.sqrt((xInt2 - xInt1) * (xInt2 - xInt1) + (yInt2 - yInt1) * (yInt2 - yInt1));

				double edge1 = Math.sqrt((xInt1 - xTarget) * (xInt1 - xTarget) + (yInt1 - yTarget) * (yInt1 - yTarget));
				double edge2 = Math.sqrt((xInt2 - xTarget) * (xInt2 - xTarget) + (yInt2 - yTarget) * (yInt2 - yTarget));

				double gamma = Math.acos((edge1 * edge1 + edge2 * edge2 - chord * chord) / (2 * edge1 * edge2));
				return gamma;
			}

		} else {
			double A = slope * slope + 1;
			double B = 2 * (slope * (intercept - yTarget) - xTarget);
			double C = xTarget * xTarget + (intercept - yTarget) * (intercept - yTarget) - circleRadius * circleRadius;
			double discriminant = B * B - 4 * A * C;

			if (discriminant < 0) {
				return 0;

			} else {
				double xInt1 = (-B - Math.sqrt(discriminant)) / (2 * A);
				double yInt1 = slope * xInt1 + intercept;
				double xInt2 = (-B + Math.sqrt(discriminant)) / (2 * A);
				double yInt2 = slope * xInt2 + intercept;

				if ((slope >= 0 && xInt1 > x2) || (slope < 0 && xInt1 < x2)) {
					xInt1 = x2;
					yInt1 = y2;
				}
				if ((slope >= 0 && xInt2 > x2) || (slope < 0 && xInt2 < x2)) {
					xInt2 = x2;
					yInt2 = y2;
				}
				if ((slope >= 0 && xInt1 < x1) || (slope < 0 && xInt1 > x1)) {
					xInt1 = x1;
					yInt1 = y1;
				}
				if ((slope >= 0 && xInt2 < x1) || (slope < 0 && xInt2 > x1)) {
					xInt2 = x1;
					yInt2 = y1;
				}

				double chord = Math.sqrt((xInt2 - xInt1) * (xInt2 - xInt1) + (yInt2 - yInt1) * (yInt2 - yInt1));

				double edge1 = Math.sqrt((xInt1 - xTarget) * (xInt1 - xTarget) + (yInt1 - yTarget) * (yInt1 - yTarget));
				double edge2 = Math.sqrt((xInt2 - xTarget) * (xInt2 - xTarget) + (yInt2 - yTarget) * (yInt2 - yTarget));

				double gamma = Math.acos((edge1 * edge1 + edge2 * edge2 - chord * chord) / (2 * edge1 * edge2));
				return gamma;
			}

		}

	}

	/**
	 * Computes the correction for a rectangular plot.
	 * 
	 * Returns the value of the correction (min value of 1).
	 * 
	 * <pre>
	 * double f = RectangularPlot
	 * 		.computeCorrection(x0, xS, xE, xN, yO, yS, yE, yN, xTarget, yTarget, xCompetitor, yCompetitor);
	 * </pre>
	 */
	public static double computeCorrection(double xO, double xS, double xE, double xN, double yO, double yS, double yE,
			double yN, double xTarget, double yTarget, double xCompetitor, double yCompetitor) {

		double[] eqSE = RectangularPlot.affine(xS, yS, xE, yE);
		double[] eqEN = RectangularPlot.affine(xE, yE, xN, yN);
		double[] eqNO = RectangularPlot.affine(xN, yN, xO, yO);
		double[] eqOS = RectangularPlot.affine(xO, yO, xS, yS);

		// Distance competitor target
		double circleRadius = Math.sqrt((xTarget - xCompetitor) * (xTarget - xCompetitor) + (yTarget - yCompetitor)
				* (yTarget - yCompetitor));

		double gammaSE = RectangularPlot.computeGamma(xS, yS, xE, yE, xTarget, yTarget, circleRadius, eqSE);
		double gammaEN = RectangularPlot.computeGamma(xE, yE, xN, yN, xTarget, yTarget, circleRadius, eqEN);
		double gammaNO = RectangularPlot.computeGamma(xN, yN, xO, yO, xTarget, yTarget, circleRadius, eqNO);
		double gammaOS = RectangularPlot.computeGamma(xO, yO, xS, yS, xTarget, yTarget, circleRadius, eqOS);

		double gamma = 2 * Math.PI - (gammaSE + gammaEN + gammaNO + gammaOS);

		double correction = 2 * Math.PI / gamma;

		// Write only if trace is true
		if (trace) {
			System.out.println("RectangularPlot.computeCorrection ()...");
			System.out.println("xO: " + xO + " xS: " + xS + " xE: " + xE + " xN: " + xN + " yO: " + yO + " yS: " + yS
					+ " yE: " + yE + " yN: " + yN + " xTarget: " + xTarget + " yTarget: " + yTarget + " xCompetitor: "
					+ xCompetitor + " yCompetitor: " + yCompetitor);
			System.out.println("correction: " + correction);
		}

		return correction;
	}

	/**
	 * This method is just to test computeCorrection().
	 */
	public static void main(String[] args) {

		trace = true;

		// RectangularPlot.computeCorrection(-4, 0, 0, -4, 0, 0, 5, 5, -2, 2,
		// -1, 1);
		//
		// RectangularPlot.computeCorrection(-4, 0, 0, -4, 0, 0, 5, 5, -2, 2,
		// -3, 4);
		//
		// RectangularPlot.computeCorrection(-4, 0, 4, 0, 4, 0, 4, 8, 1, 3, 1,
		// 4);

		RectangularPlot.computeCorrection(-1, 0, 4, 3, 4, 0, 1, 5, 3, 2, 1, 3);

		// RectangularPlot.computeCorrection(-4, 0, 4, 0, 4, 0, 4, 8, 1, 3, -2,
		// 4);

	}

}
