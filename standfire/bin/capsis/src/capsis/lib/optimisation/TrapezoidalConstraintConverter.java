package capsis.lib.optimisation;

/**
 * Conversion of a pair of command variables linked by inequality constraints to
 * or from a pair of free variables.
 * 
 * <pre>
 * x, y a pair of command variables
 * m <= x <= y <= M
 * 0 <=delta_m <= y - x <= delta_M
 * </pre>
 * 
 * @author G. Le Moguedec, F. de Coligny - November 2015
 */
public class TrapezoidalConstraintConverter {

	public static final double SQRT_2 = Math.sqrt(2d);

	private double m; // min value for x and y
	private double M; // max value for x and y
	private double delta_m; // min value for (y - x)
	private double delta_M; // max value for (y - x)

	/**
	 * Constructor.
	 */
	public TrapezoidalConstraintConverter(double m, double M, double delta_m, double delta_M) throws Exception {
		this.m = m;
		this.M = M;
		this.delta_m = delta_m;
		this.delta_M = delta_M;

		if (delta_m < 0)
			throw new Exception(
					"TrapezoidalConstraintConverter [error_9] violated condition: delta_m must be >= 0, delta_m = "
							+ delta_m);
		if (delta_M < 0)
			throw new Exception(
					"TrapezoidalConstraintConverter [error_10] violated condition: delta_M must be >= 0, delta_M = "
							+ delta_M);

		if (m > M)
			throw new Exception("TrapezoidalConstraintConverter [error_1] violated condition: m(" + m + ") > M(" + M
					+ ")");
		if (delta_m > delta_M)
			throw new Exception("TrapezoidalConstraintConverter [error_2] violated condition: delta_m(" + delta_m
					+ ") > delta_M(" + delta_M + ")");
		if (m + delta_m > M)
			throw new Exception("TrapezoidalConstraintConverter [error_3] violated condition: m(" + m + ") + delta_m("
					+ delta_m + ") > M(" + M + ")");

		if (m == M)
			if (delta_m > 0)
				throw new Exception("TrapezoidalConstraintConverter [error_4] problem is not feasable: m = M (" + m
						+ " and delta_m > 0, delta_m = " + delta_m);
			else
				throw new Exception("TrapezoidalConstraintConverter [error_5] degenerated problem: x = y = m = M (" + m
						+ ")");

		if (delta_m == delta_M)
			if (M - m < delta_m)
				throw new Exception(
						"TrapezoidalConstraintConverter [error_6] problem is not feasable: M - m < delta_m = delta_M, m = "
								+ m + ", M = " + M + ", delta_m = " + delta_m);
			else if (M - m == delta_m)
				throw new Exception(
						"TrapezoidalConstraintConverter [error_7] degenerated problem: delta_m = delta_M, x = m and y = M, m = "
								+ m + ", M = " + M + ", delta_m = " + delta_m);
			else
				throw new Exception(
						"TrapezoidalConstraintConverter [error_8] degenerated problem: m <= x <= M and y = x + delta_m, m = "
								+ m + ", M = " + M + ", delta_m = delta_M = " + delta_m);

	}

	/**
	 * A couple of command variables with trapezoidal constrains to unbounded
	 * and independent reals.
	 */
	public double[] trapezoidal_x_y_to_r1_r2(double x, double y) throws Exception {

		double[] u_theta = trapezoidal_x_y_to_u_theta(x, y);

		if (delta_M > M - m - delta_m) { // Particular case: triangular

			double[] r1_r2 = ParameterTools.orderedRealArrayToUnboundedRealArray(u_theta, m, M - delta_m);
			return r1_r2;

		} else {

			double r1 = ParameterTools.paramToReal(u_theta[0], delta_m / SQRT_2, (delta_m + delta_M) / SQRT_2);
			double r2 = ParameterTools.paramToReal(u_theta[1], 0, 1);

			return new double[] { r1, r2 };

		}
	}

	/**
	 * Changing coordinates to the double constrained interval problem from
	 * dependent coordinates (x, y) to independent coordinates (u, theta).
	 */
	public double[] trapezoidal_x_y_to_u_theta(double x, double y) throws Exception {

		if (x < m || x > M)
			throw new Exception(
					"TrapezoidalConstraintConverter.trapezoidal_x_y_to_u_theta() [error_11] violated condition: "
							+ "x must be in [m, M], x = " + x + ", m = " + m + ", M = " + M);
		if (y < m || y > M)
			throw new Exception(
					"TrapezoidalConstraintConverter.trapezoidal_x_y_to_u_theta() [error_12] violated condition: "
							+ "y must be in [m, M], y = " + y + ", m = " + m + ", M = " + M);
		if (x > y)
			throw new Exception(
					"TrapezoidalConstraintConverter.trapezoidal_x_y_to_u_theta() [error_13] violated condition: "
							+ "x must be <= y, x = " + x + ", y = " + y);

		// Particular case: triangular
		if (delta_M > M - m - delta_m)
			return triangular_x_y_to_u_theta(x, y);

		double u = (y - x) / SQRT_2;
		double theta = (x - m) / (M - m + x - y);

		double[] ret = new double[2];
		ret[0] = u;
		ret[1] = theta;
		return ret;
	}

	/**
	 * Particular case of trapezoidal_x_y_to_u_theta(): degenerated trapeze:
	 * delta_M > M - m - delta_m
	 */
	private double[] triangular_x_y_to_u_theta(double x, double y) throws Exception {

		double u = x;
		double theta = y - delta_m;

		double[] ret = new double[2];
		ret[0] = u;
		ret[1] = theta;
		return ret;

	}

	/**
	 * Unbounded and independent reals to a couple of command variables with
	 * trapezoidal constrains.
	 */
	public double[] trapezoidal_r1_r2_to_x_y(double r1, double r2) throws Exception {

		if (delta_M > M - m - delta_m) { // Particular case: triangular

			double[] a = new double[2];
			a[0] = r1;
			a[1] = r2;

			double[] u_theta = ParameterTools.realArrayToOrderedRealArray(a, m, M - delta_m);

			double u = u_theta[0];
			double theta = u_theta[1];

			return triangular_u_theta_to_x_y(u, theta);

		} else {

			double u = ParameterTools.realToParam(r1, delta_m / SQRT_2, (delta_m + delta_M) / SQRT_2);
			double theta = ParameterTools.realToParam(r2, 0, 1);

			return trapezoidal_u_theta_to_x_y(u, theta);

		}

	}

	/**
	 * Changing coordinates to the double constrained interval problem from
	 * independent coordinates (u, theta) to dependent coordinates (x, y).
	 */
	public double[] trapezoidal_u_theta_to_x_y(double u, double theta) throws Exception {

		if (u < delta_m / SQRT_2)
			throw new Exception(
					"TrapezoidalConstraintConverter.trapezoidal_u_theta_to_x_y() [error_21] violated condition: "
							+ "u < delta_m / SQRT_2, u = " + u + ", delta_m = " + delta_m);
		if (u > (delta_m + delta_M) / SQRT_2)
			throw new Exception(
					"TrapezoidalConstraintConverter.trapezoidal_u_theta_to_x_y() [error_22] violated condition: "
							+ "u > (delta_m + delta_M) / SQRT_2, u = " + u + ", delta_m = " + delta_m + ", delta_M = "
							+ delta_M);
		if (theta < 0 || theta > 1)
			throw new Exception(
					"TrapezoidalConstraintConverter.trapezoidal_u_theta_to_x_y() [error_23] violated condition: "
							+ "theta must be in [0, 1], theta = " + theta);

		// Particular case: triangular
		if (delta_M > M - m - delta_m)
			return triangular_u_theta_to_x_y(u, theta);

		double x = m + theta * (M - m - SQRT_2 * u);
		double y = m + theta * (M - m) + (1 - theta) * SQRT_2 * u;

		double[] ret = new double[2];
		ret[0] = x;
		ret[1] = y;
		return ret;

	}

	/**
	 * Particular case of trapezoidal_u_theta_to_x_y(): degenerated trapeze:
	 * delta_M > M - m - delta_m
	 */
	public double[] triangular_u_theta_to_x_y(double u, double theta) throws Exception {

		double x = u;
		double y = theta + delta_m;

		double[] ret = new double[2];
		ret[0] = x;
		ret[1] = y;
		return ret;

	}

}
