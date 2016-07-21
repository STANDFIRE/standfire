package capsis.lib.phenofit.function;

import java.io.Serializable;
import java.util.StringTokenizer;


/**
 * A superclass for functions in Phenofit.
 * 
 * @author Isabelle Chuine, Yassine Motie - January 2015
 */
public abstract class FitlibFunction implements Serializable {

	/**
	 * Turns the given encoded string into a ready-to-use function.
	 */
	static public FitlibFunction getFunction(String s) throws Exception {

		if (s.startsWith("FlowerLocFixed"))
			return new FitlibFlowerLocFixed(s);
		if (s.startsWith("LeafLocFixed"))
			return new FitlibLeafLocFixed(s);
		if (s.startsWith("Unified"))
			return new FitlibUnified(s);
		// 27/03/2015 when test model if the condition if
		// (s.startsWith("UniChill")) is tested before gonna have confusion
		// because it's another model
		if (s.startsWith("UniChill_Threshold"))
			return new FitlibUnichill_Threshold(s);
		if (s.startsWith("UniChill_Unimodal"))
			return new FitlibUniChill_Unimodal(s);
		if (s.startsWith("UniChill"))
			return new FitlibUniChill(s);
		if (s.startsWith("UniForc"))
			return new FitlibUniForc(s);
		if (s.startsWith("SpringWarming"))
			return new FitlibSpringWarming(s);
		if (s.startsWith("FixedLeafFlower"))
			return new FitlibFixedLeafFlower(s);
		if (s.startsWith("RegressionLatitude"))
			return new FitlibRegressionLatitude(s);
		if (s.startsWith("Fructification2phases"))
			return new FitlibFructification2phases(s);
		if (s.startsWith("Fructification1phaseSigmoid"))
			return new FitlibFructification1phaseSigmoid(s);
		if (s.startsWith("Fructification1phaseGDD"))
			return new FitlibFructification1phaseGDD(s);
		if (s.startsWith("FixedFructification"))
			return new FitlibFixedFructification(s);
		if (s.startsWith("MatLocFixed"))
			return new FitlibMatLocFixed(s);
		if (s.startsWith("SenDelpierre2"))
			return new FitlibSenDelpierre2(s);
		if (s.startsWith("SenDelpierre"))
			return new FitlibSenDelpierre(s);
		if (s.startsWith("SenWhite"))
			return new FitlibSenWhite(s);
		if (s.startsWith("FixedSenescence"))
			return new FitlibFixedSenescence(s);
		if (s.startsWith("SenLocFixed"))
			return new FitlibSenLocFixed(s);
		if (s.startsWith("SenTypeGDD"))
			return new FitlibSenTypeGDD(s);
		if (s.startsWith("SenTypeSigmoid"))
			return new FitlibSenTypeSigmoid(s);
		if (s.startsWith("SenRegressionLatitude"))
			return new FitlibSenRegressionLatitude(s);
		if (s.startsWith("LimitPrecipitationRamp"))
			return new FitlibLimitPrecipitationRamp(s);
		if (s.startsWith("LimitPrecipitationAnnual"))
			return new FitlibLimitPrecipitationAnnual(s);
		if (s.startsWith("LimitPrecipitationGrowth"))
			return new FitlibLimitPrecipitationGrowth(s);
		if (s.startsWith("LimitETP"))
			return new FitlibLimitETP(s);
		if (s.startsWith("LimitETA"))
			return new FitlibLimitETA(s);
		if (s.startsWith("LimitDroughtIndex"))
			return new FitlibLimitDroughtIndex(s);
		if (s.startsWith("PriestleyTaylor"))
			return new FitlibPriestleyTaylor(s);
		if (s.startsWith("PenmanBadeau"))
			return new FitlibPenmanBadeau(s);
		if (s.startsWith("PenmanFAO"))
			return new FitlibPenmanFAO(s);
		if (s.startsWith("Gauzere"))
			return new FitlibGauzere(s);

		throw new Exception("FitlibFunction.getFunction (): could not get a function for: " + s);

	}

	/*
	 * for a locs fixed dates "phenology model=9 -->LocFixed we have for :"
	 * 
	 * -- Leaflocfixed : for each location two fixed dates :
	 * LeafDormancyBreakDateMean and LeafUnfoldingDateMean -- Flowerlocfixed :
	 * for each location two fixed dates : FlowerDormancyBreakDateMean and
	 * FlowerUnfoldingDateMean
	 */
	/*
	 * static public FitlibFunction[] getLocFixedFunctions(String s) throws
	 * Exception {
	 * 
	 * if (s.startsWith("LocFixed")) { FitlibLeafLocFixed f1 = new
	 * FitlibLeafLocFixed(s); FitlibFlowerLocFixed f2 = new FitlibFlowerLocFixed (s);
	 * FitlibFunction[] res = new FitlibFunction[2]; res[0] = f1; res[1] = f2; return
	 * res;
	 * 
	 * }
	 * 
	 * throw new Exception(
	 * "FitlibFunction.getLocFixedFunctions (): could not get the frost functions for: "
	 * + s);
	 * 
	 * }
	 */
	/**
	 * Turns the given encoded string into a ready-to-use function. Special case
	 * for Frost models: 2 functions are returned for a single parameters set.
	 * The returned array contains the FitlibFlowerFrost.
	 */
	static public FitlibFunction[] getFrostFunctions(String s) throws Exception {

		if (s.startsWith("Frost")) {
			FitlibLeafFrostFunction f1 = new FitlibLeafFrostFunction(s);
			FitlibFlowerFrostFunction f2 = new FitlibFlowerFrostFunction(s);
			FitlibFunction[] res = new FitlibFunction[2];
			res[0] = f1;
			res[1] = f2;
			return res;

		}

		throw new Exception("FitlibFunction.getFrostFunctions (): could not get the frost functions for: " + s);

	}

	/**
	 * Function name, as named in the species file. // fc+ym-5.5.2015
	 */
	abstract public String getName(); // e.g. "Unified"

	/**
	 * Optional: function expected parameter names and order, as specified in
	 * the species file. If given, a test is performed to ensure the params are
	 * read in the good order. E.g. "a,b,c,d,e,w,z,Ccrit,tc". If the function
	 * has no params, not needed. // fc+ym-5.5.2015
	 */
	public String getExpectedParams() {
		// default: no params expected
		// functions may override this method to activate the order check
		return "";
	}

	/**
	 * A method to check that the params are in good order in the given string.
	 * The string contains param names separated by commas. If the order is
	 * wrong, throws an exception, kind of "Function Name needs params a, b and
	 * z in this order.
	 */
	public void checkParamsOrder(String params) throws Exception { // fc+ym-5.5.2015

//		System.out.println("FitlibFunction checkParamsOrder() functionName: "+getName()+" params: "+params+" expectedParams: "+getExpectedParams());
		
		String expectedParams = getExpectedParams();
		if (expectedParams.length() == 0)
			return; // no expected params
		StringTokenizer expParams = new StringTokenizer(expectedParams, ", ");
		int nExp = expParams.countTokens();

		StringTokenizer readParams = new StringTokenizer(params, ", ");

		boolean found = false;
		int nFound = 0;
		while (expParams.hasMoreTokens()) {

			String exp = expParams.nextToken().trim();
			found = false;
			
//			System.out.println("FitlibFunction searching: "+exp+"...");

			while (!found && readParams.hasMoreTokens()) {
				String token = readParams.nextToken().trim();

				if (token.equals(exp)) {
					found = true;
					
//					System.out.println("FitlibFunction found: "+exp);
					
					nFound++;
				}

			}
			if (!found)
				throw new Exception("Wrong param order for " + getName() + ", expected this order: " + expectedParams);

		}

		if (nFound != nExp)
			throw new Exception("Wrong param order for " + getName() + ", expected this order: " + expectedParams);

	}

}