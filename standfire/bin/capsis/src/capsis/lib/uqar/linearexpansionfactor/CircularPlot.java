package capsis.lib.uqar.linearexpansionfactor;

/**
 * Computes a correction of a competition index for a circular plot.
 * 
 * <pre>
 * # under a Linux terminal, from capsis4/
 * java -cp class/:ext/ capsis.lib.uqar.linearexpansionfactor.CircularPlot
 * 
 * # under a windows terminal, from capsis4\
 * java -cp class/;ext/ capsis.lib.uqar.linearexpansionfactor.CircularPlot
 * 
 * </pre>
 * 
 * *
 * 
 * @author Tony Franceschini, F. de Coligny - June 2016
 */
public class CircularPlot {

	// if trace is set to true, computeCorrection () will write messages in the
	// terminal
	private static boolean trace = false;

	/**
	 * Computes the correction for a circular plot. (xp, yp) are the plot center
	 * coordinates. rp is the radius of the plot. (xci, yci) are the target tree
	 * coordinates. (xco, yco) are the competitor tree coordinates.
	 * 
	 * Returns the value of the correction (min value of 1).
	 * 
	 * <pre>
	 * double f = CircularPlot.computeCorrection(xp, yp, rp, xci, yci, xco, yco);
	 * </pre>
	 */
	public static double computeCorrection(double xp, double yp, double rp, double xci, double yci, double xco,
			double yco) {

		double distanceCentreTarget = Math.sqrt((xp - xci) * (xp - xci) + (yp - yci) * (yp - yci));

		// Distance competitor target
		double rc = Math.sqrt((xco - xci) * (xco - xci) + (yco - yci) * (yco - yci));

		double gamma = 0;
		if (rp > distanceCentreTarget + rc) {
			gamma = 2 * Math.PI;

		} else {

			// 2 intersections points
			double xInt1 = 0;
			double xInt2 = 0;
			double yInt1 = 0;
			double yInt2 = 0;

			if (yp != yci) {
				double a = (xci - xp) / (yp - yci);
				double b = (rc * rc - rp * rp - xci * xci + xp * xp - yci * yci + yp * yp) / (2 * (yp - yci));
				double A = a * a + 1;
				double B = 2 * a * b - 2 * xp - 2 * a * yp;
				double C = xp * xp + yp * yp + b * b - rp * rp - 2 * yp * b;
				double discriminant = B * B - 4 * A * C;
				xInt1 = (-B - Math.sqrt(discriminant)) / (2 * A);
				xInt2 = (-B + Math.sqrt(discriminant)) / (2 * A);
				yInt1 = a * xInt1 + b;
				yInt2 = a * xInt2 + b;

			} else {
				xInt1 = (rp * rp - rc * rc + xci * xci - xp * xp) / (2 * xci - 2 * xp);
				xInt2 = xInt1;
				double A = 1;
				double B = -2 * yp;
				double C = yp * yp + (xInt1 - xp) * (xInt1 - xp) - rp * rp;
				double discriminant = B * B - 4 * A * C;
				yInt1 = (-B - Math.sqrt(discriminant)) / (2 * A);
				yInt2 = (-B + Math.sqrt(discriminant)) / (2 * A);

			}

			double chord = Math.sqrt((xInt2 - xInt1) * (xInt2 - xInt1) + (yInt2 - yInt1) * (yInt2 - yInt1));

			gamma = 2 * Math.PI - Math.acos((2 * rp * rp - chord * chord) / (2 * rp * rp));

		}

		double correction = 2 * Math.PI / gamma;

		// Write only if trace is true
		if (trace) {
			System.out.println("CircularPlot.computeCorrection ()...");
			System.out.println("xp: " + xp + " yp: " + yp + " rp: " + rp + " xci: " + xci + " yci: " + yci + " xco: "
					+ xco + " yco: " + yco);
			System.out.println("correction: " + correction);
		}

		return correction;
	}

	/**
	 * This method is just to test computeCorrection().
	 */
	public static void main(String[] args) {

		trace = true;

		double xp = 0;
		double yp = 0;
		double rp = 10;
		double xci = -5;
		double yci = 4;
		double xco = -4;
		double yco = 3;
		CircularPlot.computeCorrection(xp, yp, rp, xci, yci, xco, yco);

		xp = 0;
		yp = 0;
		rp = 10;
		xci = -9;
		yci = 4;
		xco = -4;
		yco = 3;
		CircularPlot.computeCorrection(xp, yp, rp, xci, yci, xco, yco);

		xp = 0;
		yp = 0;
		rp = 10;
		xci = -9;
		yci = 0;
		xco = -4;
		yco = 3;
		CircularPlot.computeCorrection(xp, yp, rp, xci, yci, xco, yco);

	}

}
