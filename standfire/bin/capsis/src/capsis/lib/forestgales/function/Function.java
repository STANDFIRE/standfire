package capsis.lib.forestgales.function;


import java.io.Serializable;

/**
 * A superclass for functions in ForestGales.
 * 
 * @author B. Gardiner, K. Kamimura - August 2013
 */
public abstract class Function implements Serializable {

	/**
	 * The general form of a function: y = f(x), must be provided by all subclasses.
	 */
	abstract public double f (double x);

	/**
	 * Turns the given encoded string into a ready-to-use function.
	 */
	static public Function getFunction (String s) throws Exception {

		if (s.startsWith ("linear2p")) return new Linear2p (s);
		if (s.startsWith ("pow3p")) return new Pow3p (s);
		if (s.startsWith ("exp2p")) return new Exp2p (s);

		throw new Exception ("Function.getFunction (): could not get a function for: " + s);

	}

}
