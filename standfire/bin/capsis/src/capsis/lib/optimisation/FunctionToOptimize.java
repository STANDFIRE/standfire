package capsis.lib.optimisation;

import java.util.List;
import java.util.Map;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.MultivariateRealFunction;

/**
 * A superclass for the function to be optimized.
 * 
 * @author G. Le Moguédec, F. de Coligny - November 2015
 */
abstract public class FunctionToOptimize implements MultivariateRealFunction {

	/** Names of function inputs */
	protected String[] listVar;

	/** Names of simple indicators */
	protected String[] listSimpleIndic;

	/** Names of composite indicators */
	protected String[] listComplexIndic;

	/** Names of function outputs */
	protected String[] listOutput;
	
	/** Historic of the function calls */
	protected List<Map<String, Double>> historic ;
	
	/** type optimization : maximize or not ** */
	protected boolean maximized ;
	
	/** Number of call of value function */
	protected int nbCall = 0 ;

	/**
	 * Value method with an array of real numbers as input. This is the version
	 * used by optimisation procedures.
	 */
	abstract public double value(double[] parametres) throws FunctionEvaluationException, IllegalArgumentException;

	/**
	 * Conversion of an array of real numbers to an array containing the
	 * command variable values
	 */
	abstract public double[] arrayToPoint(double[] realArray) throws Exception;
		
	/**
	 * Conversion of an array containing the command variable values to an array of
	 * real numbers
	 */
	abstract public double[] pointToArray(double[] point) throws Exception;

	/** Return a randomly generated point (=parameter array) */
	public double[] randomPoint() throws Exception {
		double[] randomArray = new double[listVar.length];
		for (int i = 0; i < randomArray.length; i++) {
			randomArray[i] = ParameterTools.paramToReal(Math.random(), 0, 1);
		}
		double[] point = arrayToPoint(randomArray);
		return point;
	}

	/** Return an array of randomly generated points */
	public double[][] randomPoint(int n) throws Exception {
		double[][] randomPointArray = new double[n][listVar.length];
		for (int i = 0; i < n; i++) {
			randomPointArray[i] = randomPoint();
		}
		return randomPointArray;
	}

	/** Return a random Simplex of reals */
	public double[][] randomSimplex() {
		double[][] simplex = new double[listVar.length + 1][listVar.length];
		for (int i = 0; i < listVar.length + 1; i++) {
			for (int j = 0; j < listVar.length; j++) {
				simplex[i][j] = ParameterTools.paramToReal(Math.random(), 0, 1);
			}
		}
		return simplex;
	}

	/**
	 * Initialize a vector of length {number of indicators} with the given value
	 */
	public double[] initVector(double value) {
		double vector[] = new double[listSimpleIndic.length + listComplexIndic.length];
		for (int i = 0; i < vector.length; i++) {
			vector[i] = value;
		}
		return (vector);
	}

	/** Search the index of a scenario command variable */
	public int searchIndexVariable(String name) {
		int i = -1;
		for (int k = 0; k < listVar.length; k++) {
			if (listVar[k] == name) {
				i = k;
			}
		}
		return i;
	}

	/** Search for the index of an indicator within the vectors */
	public int searchIndexIndic(String name) {
		int i = -1;
		for (int k = 0; k < listSimpleIndic.length; k++) {
			if (listSimpleIndic[k].equals(name)) {
				i = k;
			}
		}
		for (int k = 0; k < listComplexIndic.length; k++) {
			if (listComplexIndic[k].equals(name)) {
				i = k + listSimpleIndic.length;
			}
		}
		return i;
	}

	/** Get the names of function inputs as a table */
	public String[] getListVar() {
		return listVar;
	}

	/** Get the names of function outputs as a table */
	public String[] getListOutput() {
		return listOutput;
	}

	/** Get the names of indicators outputs as a table */
	public String[] getListIndic() {
		String[] listIndic = new String[listSimpleIndic.length + listComplexIndic.length];
		for (int i = 0; i < listSimpleIndic.length; i++) {
			listIndic[i] = listSimpleIndic[i];
		}
		for (int i = 0; i < listComplexIndic.length; i++) {
			listIndic[i + listSimpleIndic.length] = listComplexIndic[i];
		}
		return listIndic;
	}

	/** Get the names of function inputs and function output as a table */
	public String[] getHeader() {
		String[] header = new String[listVar.length + 2 * (listSimpleIndic.length + listComplexIndic.length)
				+ listOutput.length + 1];
		String[] listIndic = getListIndic();
		for (int i = 0; i < listVar.length; i++) {
			header[i] = listVar[i];
		}
		for (int i = 0; i < listIndic.length; i++) {
			header[listVar.length + i] = listIndic[i] + "Mean";
		}
		for (int i = 0; i < listIndic.length; i++) {
			header[listVar.length + listIndic.length + i] = listIndic[i] + "Sd";
		}
		for (int i = 0; i < listOutput.length; i++) {
			header[listVar.length + 2 * listIndic.length + i] = listOutput[i];
		}
		header[header.length - 1] = "nEval";
		return header;
	}

	/** Return the names of function inputs and function output in a String */
	public String headerToString() {
		String s = listVar[0];
		String[] listIndic = getListIndic();
		for (int i = 1; i < listVar.length; i++) {
			s += "\t" + listVar[i];
		}
		for (int i = 0; i < listIndic.length; i++) {
			s += "\t" + listIndic[i] + "Mean";
		}
		for (int i = 0; i < listIndic.length; i++) {
			s += "\t" + listIndic[i] + "Sd";
		}
		for (int i = 0; i < listOutput.length; i++) {
			s += "\t" + listOutput[i];
		}
		s += "\t" + "nEval";
		return s;
	}

	/**
	 * Convert the specified element of historic to a String with or without
	 * header for printing
	 */
	public String elementToString(int line, boolean header) {
		String s = new String();
		if (header) {
			s += this.headerToString() + "\n";
		}
		s += this.elementToString(line) + "\n";
		return s;
	}

	/** Convert the historic of function calls to a String for printing */
	public String historicToString() {
		String s = new String();
		String[] listIndic = getListIndic();
		for (Map<String, Double> ligne : historic) {
			s += ligne.get(listVar[0]);
			for (int i = 1; i < listVar.length; i++) {
				s += "\t" + ligne.get(listVar[i]);
			}
			for (int i = 0; i < listIndic.length; i++) {
				s += "\t" + ligne.get(listIndic[i] + "Mean");
			}
			for (int i = 0; i < listIndic.length; i++) {
				s += "\t" + ligne.get(listIndic[i] + "Sd");
			}
			for (int i = 0; i < listOutput.length; i++) {
				s += "\t" + ligne.get(listOutput[i]);
			}
			s += "\t" + ligne.get("nEval");
			;
			s += "\n";
		}
		return (s);
	}

	/**
	 * Convert the historic of function calls to a String for printing with or
	 * without header
	 */
	public String historicToString(boolean header) {
		String s = new String();
		if (header) {
			s += this.headerToString() + "\n";
		}
		s += this.historicToString();
		return (s);
	}

	/** Convert the specified element of historic to a String for printing */
	public String elementToString(int line) {
		String s = new String();
		String[] listIndic = getListIndic();
		if ((line > 0) && (line < historic.size())) {
			Map<String, Double> extrait = historic.get(line);
			s += extrait.get(listVar[0]);
			for (int i = 1; i < listVar.length; i++) {
				s += "\t" + extrait.get(listVar[i]);
			}
			for (int i = 0; i < listIndic.length; i++) {
				s += "\t" + extrait.get(listIndic[i] + "Mean");
			}
			for (int i = 0; i < listIndic.length; i++) {
				s += "\t" + extrait.get(listIndic[i] + "Sd");
			}
			for (int i = 0; i < listOutput.length; i++) {
				s += "\t" + extrait.get(listOutput[i]);
			}
			s += "\t" + extrait.get("nEval");
		}
		return s;
	}

	/** Return the names of function inputs in a String */
	public String varHeaderToString() {
		String s = listVar[0];
		for (int i = 1; i < listVar.length; i++) {
			s += "\t" + listVar[i];
		}
		return s;
	}

	/** Convert a table of parameters to a String */
	public String pointToString(double[] point) {
		String s = new String();
		if (point.length == listVar.length) {
			s += point[0];
			for (int i = 1; i < point.length; i++) {
				s += "\t" + point[i];
			}
		}
		return s;
	}

	/** Convert a table of parameters to a String with or without header */
	public String pointToString(double[] point, boolean header) {
		String s = new String();
		if (header) {
			s += varHeaderToString() + "\n";
		}
		s += pointToString(point) + "\n";
		return s;
	}

	/** Convert a table of points to a String */
	public String pointArrayToString(double[][] pointArray) {
		String s = new String();
		for (int i = 0; i < pointArray.length; i++) {
			double[] line = pointArray[i];
			s += pointToString(line, false);
		}
		return s;
	}

	/** Convert a table of points to a String with or without header */
	public String pointArrayToString(double[][] pointArray, boolean header) {
		String s = new String();
		if (header) {
			s += varHeaderToString() + "\n";
		}
		s += pointArrayToString(pointArray);
		return s;
	}

	/** Return a realPoint from a set of quantiles */
	public double[] quantileToRealPoint(double[] quantiles) {
		double[] realPoint = new double[quantiles.length];
		for (int i = 0; i < quantiles.length; i++) {
			realPoint[i] = ParameterTools.paramToReal(quantiles[i], 0, 1);
		}
		return (realPoint);
	}

	/** Return a point from a set of quantiles */
	public double[] quantileToPoint(double[] quantiles) throws Exception {
		for (int i = 0; i < quantiles.length; i++) {
			quantiles[i] = ParameterTools.paramToReal(quantiles[i], 0, 1);
		}
		double[] point = arrayToPoint(quantiles);
		return (point);
	}

	/** Get the historic of function calls as a list */
	public List<Map<String, Double>> getHistoric() {
		return (historic);
	}

	/**
	 * Returns the rank of the best historic element where "best" correspond to
	 * the maximum value taken by the function if the boolean "max" is true,
	 * else to the minimum
	 */
	public int rankBest(boolean max) {
		int rangBest = 0;
		for (int i = 1; i < historic.size(); i++) {
			if (max) {
				if (historic.get(i).get("value") > historic.get(rangBest).get("value")) {
					rangBest = i;
				}
			} else {
				if (historic.get(i).get("value") < historic.get(rangBest).get("value")) {
					rangBest = i;
				}
			}
		}
		return rangBest;
	}

	/** Set the number of run if it has to be re-initalised */
	public void setNbCall(int n) {
		this.nbCall = n;
	}

	/** Get the number of run */
	public int getNbCall() {
		return (this.nbCall);
	}

	/** Is the optimisation a maximisation ? */
	public void setMaximized(boolean maximized) {
		this.maximized = maximized;
	}

}
