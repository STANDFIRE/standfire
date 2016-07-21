package capsis.lib.phelib;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.FloatByReference;



 
/**
 * SimpleDll: a link to the simpleDLL.dll dynamic link library.
 *
 * @author F. de Coligny, S. Griffon - July 2015
 */
public class PhelibDllTest /* extends OrgTools */{
 
//	static {
//		// Set jna.library.path
//		System.setProperty("jna.library.path", System.getProperty("java.library.path"));
//	}
 
	// This is the standard, stable way of mapping, which supports extensive
	// customization and mapping of Java to native types.
 
	public interface DllInterface extends Library {
		DllInterface INSTANCE = (DllInterface) Native.loadLibrary("libPheLib", DllInterface.class);
		// Using the MetaModel object
		String getVersion ();
		void init(String s); 
	
		void f1(int site, int year, int doy, float latitude, int nVariables, 
				float climateDoy[], float climateDayAfter[], float prevValue,
				FloatByReference newValue);
 
	}
	
	public String getVersion () {
		String v = DllInterface.INSTANCE.getVersion ();
		return v;
	}
	
	public void init(String s){
		DllInterface.INSTANCE.init(s);
	}
 
	public void f1(int site, int year, int doy, float latitude, int nVariables, 
			float []climateDoy, float []climateDayAfter, float prevValue) {
		FloatByReference r = new FloatByReference (0);
 
		DllInterface.INSTANCE.f1( site, year, doy, latitude, nVariables, 
				climateDoy, climateDayAfter, prevValue,
				r);
 
		System.out.println(r.getValue ());
	}
 

 
	/**
	 * A test method
	 */
	public static void main(String[] args) throws Exception {
 
		PhelibDllTest t = new PhelibDllTest();
		String s = "C:/tmp/phelib/standalone/test/test_files/GDD.xml";
		
		System.out.println("SimpleDll calling getVersion ()...");
		String s1 = t.getVersion ();
		System.out.println("SimpleDll r: " + s1);
		
		System.out.println("PhelibDll calling init ()...");
		//t.init(s);
		
		System.out.println("PhelibDll calling f1 ()...");
		float[] a = new float[]{1.1f, 2.2f, 3.3f};
		t.f1(1,2015,14,5.5f,12,a,null, 3f);
		
		
	}
 
}