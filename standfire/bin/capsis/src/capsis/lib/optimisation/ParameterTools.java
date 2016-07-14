package capsis.lib.optimisation;

import java.util.Arrays;

/**
 * Tools to manipulate vectors of parameters in optimisation methods.
 * 
 * @author G. Le Moguédec, F. de Coligny - November 2015
 */
public class ParameterTools {
	
	/**
	 * Conversion of an unbounded real to a possibly bounded real. This method
	 * is used to convert a real x to a parameter value. It uses a lower and an
	 * upper bound parameters that may be infinite.
	 */
	static public double realToParam(double x, double borneInf, double borneSup) {
		double bInf = Math.min(borneInf, borneSup);
		double bSup = Math.max(borneInf, borneSup);

		double val;
		if (bInf == bSup) {
			val = bInf;
		} else {
			if (Double.isInfinite(bInf)) {
				if (Double.isInfinite(bSup)) {
					val = x;
				} else {
					val = bSup - Math.exp(-x);
				}
			} else {
				if (Double.isInfinite(bSup)) {
					val = bInf + Math.exp(x);
				} else {
					val = bInf + (bSup - bInf) * (0.5 + Math.atan(x) / Math.PI);
				}
			}

		}
		return (val);
	}

	/**
	 * Conversion of a possibly bounded real to an unbounded one. Method used to
	 * convert an explicit scenario parameter to a real value that will be used
	 * as input in optimisation procedures. It uses a lower and an upper bound
	 * parameters that may be infinite.
	 */
	static public double paramToReal(double x, double borneInf, double borneSup) {
		double bInf = Math.min(borneInf, borneSup);
		double bSup = Math.max(borneInf, borneSup);

		double val;
		if (bInf == bSup) {
			val = Double.NaN;
		} else {
			if (Double.isInfinite(bInf)) {
				if (Double.isInfinite(bSup)) {
					val = x;
				} else {
					if (x > bSup) {
						val = Double.NaN;
					} else {
						if (x == bSup) {
							val = Double.POSITIVE_INFINITY;
						} else {
							val = -Math.log(bSup - x);
						}
					}
				}
			} else {
				if (x < bInf) {
					val = Double.NaN;
				} else {
					if (x == bInf) {
						val = Double.NEGATIVE_INFINITY;
					} else {
						if (Double.isInfinite(bSup)) {
							val = Math.log(x - bInf);
						} else {
							if (x > bSup) {
								val = Double.NaN;
							} else {
								if (x == bSup) {
									val = Double.POSITIVE_INFINITY;
								} else {
									val = Math.tan(Math.PI * ((x - bInf) / (bSup - bInf) - 0.5));
								}
							}
						}
					}
				}
			}

		}
		return (val);
	}

	/**
	 * Conversion of an array of doubles to an array of ordered doubles,
	 * possibly bounded.
	 */
	static public double[] realArrayToOrderedRealArray(double[] x, double borneInf, double borneSup) {
		double bInf = Math.min(borneInf, borneSup);
		double bSup = Math.max(borneInf, borneSup);

		double val = 0.;
		double[] sortie = new double[x.length];
		if (bInf == bSup) {
			for (int i = 0; i < x.length; i++) {
				sortie[i] = bInf;
			}
		} else {
			if (Double.isInfinite(bInf)) {
				if (Double.isInfinite(bSup)) {
					for (int i = 0; i < x.length; i++) {
						val += Math.exp(x[i]);
						sortie[i] = Math.log(val);
					}
				} else {
					for (int i = x.length - 1; i >= 0; i--) {
						val += Math.exp(-x[i]);
						sortie[i] = bSup - val;
					}
				}
			} else {
				if (Double.isInfinite(bSup)) {
					for (int i = 0; i < x.length; i++) {
						val += Math.exp(x[i]);
						sortie[i] = bInf + val;
					}
				} else {
					for (int i = 0; i < x.length; i++) {
						val += Math.exp(x[i]);
						sortie[i] = val;
					}
					for (int i = 0; i < x.length; i++) {
						if (Double.isInfinite(val)) {
							if (Double.isInfinite(sortie[i])) {
								sortie[i] = 1.;
							} else {
								sortie[i] = 0.;
							}
						} else {
							sortie[i] /= 1 + val;
						}
						sortie[i] = bInf + (bSup - bInf) * sortie[i];
					}
				}
			}
		}
		return (sortie);
	}

	/**
	 * Conversion of an array of ordered doubles (possibly bounded) to an array
	 * of (unbounded)doubles.
	 */
	static public double[] orderedRealArrayToUnboundedRealArray(double[] x, double borneInf, double borneSup) {
		double bInf = Math.min(borneInf, borneSup);
		double bSup = Math.max(borneInf, borneSup);

		double val = 0.;
		double[] sortie = new double[x.length];
		double test[] = x;
		Arrays.sort(test);
		if ((bInf == bSup) | (bInf > test[0]) | (bSup < test[x.length - 1])) {
			for (int i = 0; i < x.length; i++) {
				sortie[i] = Double.NaN;
			}
		} else {
			if (Double.isInfinite(bInf)) {
				if (Double.isInfinite(bSup)) {
					for (int i = 0; i < x.length; i++) {
						sortie[i] = Math.log(Math.exp(x[i]) - val);
						val = Math.exp(x[i]);
					}
				} else {
					val = bSup;
					for (int i = x.length - 1; i >= 0; i--) {
						sortie[i] = -Math.log(val - x[i]);
						val = x[i];
					}
				}
			} else {
				if (Double.isInfinite(bSup)) {
					val = bInf;
					for (int i = 0; i < x.length; i++) {
						sortie[i] = Math.log(x[i] - val);
						val = x[i];
					}
				} else {
					val = bInf;
					for (int i = 0; i < x.length; i++) {
						if (x[i] <= val) {
							sortie[i] = Double.NEGATIVE_INFINITY;
						} else {
							if ((x[i] >= bSup) | (x[x.length - 1] >= bSup)) {
								sortie[i] = Double.POSITIVE_INFINITY;
							} else {
								sortie[i] = Math.log((x[i] - val) / (bSup - x[x.length - 1]));
							}
						}
						val = x[i];
					}
				}
			}
		}

		return (sortie);
	}

	/** Conversion of a time length (in milliseconds) to a String */
	static public String timeToString(long duree) {
		int dec = (int) duree % 1000;
		int sec = (int) duree / 1000;
		int min = sec / 60;
		sec = sec % 60;
		int h = min / 60;
		min = min % 60;
		String s = h + "h " + min + "min " + sec + "." + dec + "sec.";
		return s;
	}

}
