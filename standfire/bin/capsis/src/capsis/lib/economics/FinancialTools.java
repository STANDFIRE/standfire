package capsis.lib.economics;

import java.util.ArrayList;
import java.util.Collection;


/**	FinancialTools : some useful methods for fagacees.
*
*	@author Olivier Pain - december 2007
*/
public class FinancialTools {

	/**	Net Present Value NPV = "BAo".
	*	rate in [0,1]
	*/
	public static double getNetPresentValue ( double rate, Collection<Integer> years, Collection<Double> cashFlows) {
	// rate in percent : 0.04
		// transformation ArrayList -> array
		Integer[] y = new Integer[years.size ()];
		y = years.toArray (y);
		Double[] v = new Double[cashFlows.size ()];
		v = cashFlows.toArray (v);
		double npv = 0;
		for (int i = 0; i < v.length; i++) {
			npv += v[i] / Math.pow(1.0+rate, y[i]);
		}
/*
		String messLog="taux="+rate+"\n";
		for (int i = 0; i < v.length; i++) { messLog +=  "an="+y[i]+", value="+v[i]+"\n";}
		MessageDialog.promptInfo(messLog);
*/
		return npv;
	}

	// NPVi = "BASIo"
	// rate in [0,1]
	public static double getNetPresentValueI ( double rate, Collection<Integer> years, Collection<Double> cashFlows) {
		// search max year
		Integer[] y = new Integer[years.size ()];
		y = years.toArray (y);
		int ymax = -1;
		for (int i = 0; i < y.length; i++) {
			if (y[i] > ymax) {ymax = y[i];}
		}
		double NPV = getNetPresentValue ( rate, years, cashFlows);
		double coeff = Math.pow(1+rate, ymax);
		double NPVI = NPV * (coeff/(coeff-1));
		return NPVI;
	}

	// CA = "BASIo*rate: annuite constante"
	// rate in [0,1]
	public static double getConstantAnnuity ( double rate, Collection<Integer> years, Collection<Double> cashFlows) {
		double CA = getNetPresentValueI ( rate, years, cashFlows) * rate;
		return CA;
	}

	// IRR = "TIR"
	// return a value in [0, 1]
	public static double getInternalRateOfReturn ( Collection<Integer> years, Collection<Double> cashFlows) {
	  	double mini = 0.000000001;
	  	double maxi = 10;
		double pas = 0.0001;
  		double idx = (mini+maxi)/2;
  		double x = getNetPresentValue(idx, years, cashFlows);
  		while (x != 0)	{
			if (x < 0) {maxi = idx;}
     		else { mini = idx;}
			if ((maxi-mini) < pas){return idx;}
			idx = (mini+maxi)/2;
     		x = getNetPresentValue(idx, years, cashFlows);
		}
		return 0;
	}

	public static void main (String[] args) {
		Collection<Integer> years = new ArrayList<Integer> ();
		Collection<Double> cashFlows = new ArrayList<Double> ();
		years.add (1);
		cashFlows.add (-1000d);
		years.add (2);
		cashFlows.add (1200d);

		double NPV = getNetPresentValue (0.04, years, cashFlows);
		System.out.println ("FinancialTools NPV="+NPV);

		double NPVI = getNetPresentValueI (0.04, years, cashFlows);
		System.out.println ("FinancialTools NPVI="+NPVI);

		double IRR = getInternalRateOfReturn (years, cashFlows);
		System.out.println ("FinancialTools IRR="+IRR);

	}

} // end of FinancialTools
