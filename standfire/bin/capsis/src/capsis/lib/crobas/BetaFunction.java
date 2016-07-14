package capsis.lib.crobas;

import flanagan.integration.IntegralFunction;

/**	The integration of the beta function x^p * (1-x)^q
*	@author R. Schneider - 23.5.2008
*/
public class BetaFunction implements IntegralFunction {
    private double p = 0d;
	private double q = 0d;

    public double function (double x){
        double y = Math.pow (x, p) * Math.pow (1 - x, q);
        return y;
    }

    public void setP (double p) {
        this.p = p;
    }

    public void setQ (double q) {
        this.q = q;
    }

}


