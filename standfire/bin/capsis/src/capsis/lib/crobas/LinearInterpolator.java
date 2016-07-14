package capsis.lib.crobas;

import java.io.Serializable;

/**	A Linear Interpolator
*	@author R. Schneider - 27.5.2008
*/
public class LinearInterpolator implements Serializable {

	private double[] xs;
	private double[] ys;
	private int n;
	
	public LinearInterpolator (double[] xs, double[] ys) throws Exception {
		this.xs = xs;
		this.ys = ys;
		this.n = xs.length;
		
		// Checks
		if (xs.length < 2 || ys.length < 2) {throw new Exception ("xs and ys length must be >= 2");}
		if (xs.length != ys.length) {throw new Exception ("xs and ys length must be equal");}
		for (int i = 0; i < n - 1; i++) {
			if (xs[i] > xs[i+1]) {throw new Exception ("xs values must be in ascending order");}
			if (xs[i] == xs[i+1]) {throw new Exception ("xs values must be unique");}
		}
		
	}
	
	public double interpolate (double x) throws Exception {
		if (x < xs[0]) {
			int i = 0;
			double slope = (ys[i+1] - ys[i]) / (xs[i+1] - xs[i]);
			return ys[i] + (x - xs[i]) * slope;
		}
		if (x > xs[n-1]) {
			int i = n - 2;
			double slope = (ys[i+1] - ys[i]) / (xs[i+1] - xs[i]);
			return ys[i] + (x - xs[i]) * slope;
		}
		for (int i = 0; i < n-1; i++) {
			if (x == xs[i]) {return ys[i];}
			
			if (x > xs[i] && x < xs[i+1]) {
				double slope = (ys[i+1] - ys[i]) / (xs[i+1] - xs[i]);
				return ys[i] + (x - xs[i]) * slope;
			}
		}
		if (x == xs[n-1]) {return ys[n-1];}
		throw new Exception (toString ()+"\ncould not evaluate y for x value : "+x);
	}
	
	public String toString () {
		StringBuffer b = new StringBuffer ("LinearInterpolator");
		b.append (" n="+n);
		b.append ("\n xs:[");
		for (int i = 0; i < n; i++) {
			b.append (xs[i]);
			b.append (" ");
		}
		b.append ("]\n ys:[");
		for (int i = 0; i < n; i++) {
			b.append (ys[i]);
			b.append (" ");
		}
		b.append ("]");
		return b.toString ();
	}
	
	public static void main (String[] args) {
		try {
			double[] xs = {1,2,3,4};
			double[] ys = {1,2,3,4};
			//~ double[] xs = {1};
			//~ double[] ys = {1};
			LinearInterpolator i = new LinearInterpolator (xs, ys);
			System.out.println ("i.interpolate (0.5)="+i.interpolate (0.5));
			System.out.println ("i.interpolate (1.5)="+i.interpolate (1.5));
			System.out.println ("i.interpolate (2.5)="+i.interpolate (2.5));
			System.out.println ("i.interpolate (3.5)="+i.interpolate (3.5));
			System.out.println ("i.interpolate (4.5)="+i.interpolate (4.5));
			System.out.println ("i.interpolate (1)="+i.interpolate (1));
			System.out.println ("i.interpolate (2)="+i.interpolate (2));
			System.out.println ("i.interpolate (3)="+i.interpolate (3));
			System.out.println ("i.interpolate (4)="+i.interpolate (4));
		} catch (Exception e) {
			System.out.println ("Exception: "+e);
			e.printStackTrace (System.out);
		}
	}
	
}


