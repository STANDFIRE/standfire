package capsis.lib.phelib;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.FloatByReference;

/**
 * SimpleDll: a link to the simpleDLL.dll dynamic link library.
 *
 * @author F. de Coligny, S. Griffon - July 2015
 */
public class SimpleDllTest /* extends OrgTools */{

//	static {
//		// Set jna.library.path
//		System.setProperty("jna.library.path", System.getProperty("java.library.path"));
//	}

	// This is the standard, stable way of mapping, which supports extensive
	// customization and mapping of Java to native types.

	public interface DllInterface extends Library {
		DllInterface INSTANCE = (DllInterface) Native.loadLibrary("simpleDLL", DllInterface.class);

		int multiply(float a, float b, FloatByReference result);

		int sumArray(float[] a, int length, FloatByReference result);

		String getVersion ();

		int addValue (float a [], int length, float value);

		// Using the foo object
		int initFoo (float initState);
		String getFooVersion ();
		float getFooState ();

	}

	public float multiply(float a, float b) {
		FloatByReference r = new FloatByReference (0);

		int rc = DllInterface.INSTANCE.multiply (a, b, r);

		return r.getValue ();
	}

	public float sumArray(float[] a) {
		FloatByReference r = new FloatByReference (0);
		int l = a.length;

		int rc = DllInterface.INSTANCE.sumArray (a, l, r);

		return r.getValue ();
	}

	public String getVersion () {
		String v = DllInterface.INSTANCE.getVersion ();
		return v;
	}

	public void addValue (float[] a, float v) {
		int l = a.length;
		DllInterface.INSTANCE.addValue (a, l, v);
	}

	// Using the foo object
	public int initFoo (float initState) {
		DllInterface.INSTANCE.initFoo (initState);
		return 0;
	}

	public String getFooVersion () {
		return DllInterface.INSTANCE.getFooVersion ();
	}

	public float getFooState () {
		return DllInterface.INSTANCE.getFooState ();
	}

	/**
	 * A test method
	 */
	public static void main(String[] args) throws Exception {

		SimpleDllTest t = new SimpleDllTest();

		System.out.println("SimpleDll calling multiply ()...");
		float r = t.multiply(3.1f, 3f);
		System.out.println("SimpleDll r: " + r);

		System.out.println("SimpleDll calling sumArray ()...");
		float[] a = new float[]{1.1f, 2.2f, 3.3f};
		r = t.sumArray(a);
		System.out.println("SimpleDll r: " + r);

		System.out.println("SimpleDll calling getVersion ()...");
		String s = t.getVersion ();
		System.out.println("SimpleDll r: " + s);

		System.out.println("SimpleDll calling addValue ()...");
		a = new float[]{1.1f, 2.2f, 3.3f};
		t.addValue(a, 5.5f);
		String b = "a[]: ";
		for (int i = 0; i < a.length; i++) {
			b += a[i];
			b+= " ";
		}
		System.out.println("SimpleDll r: " + b);

		System.out.println("SimpleDll calling foo methods...");
		t.initFoo (1f);
		System.out.println("SimpleDll fooVersion: "+t.getFooVersion ());
		System.out.println("SimpleDll fooState: "+t.getFooState ());
		System.out.println("SimpleDll fooState: "+t.getFooState ());
		System.out.println("SimpleDll fooState: "+t.getFooState ());
		System.out.println("SimpleDll fooState: "+t.getFooState ());
		System.out.println("SimpleDll fooState: "+t.getFooState ());
	}

}