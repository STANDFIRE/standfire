package capsis.lib.crobas;

//import uqar.jackpine.model.JackPineBranch;


import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import uqar.jackpine.model.jackpine.JackPineTree;
import uqar.jackpine.model.jackpine.JackPineWhorl;

import jeeb.lib.util.Log;

/** The PipeQual Model 
 *   by Annikki Makela.
 *   @author R. Schneider - 20.5.2008
 *   modified by Raymond Audet <raymond.audet@gmail.com> - 27.06.2008
 */
public class PipeQualWhorl implements Comparable, Cloneable, Serializable {
	protected CTree tree;	// R. Audet modified private to protected for JackPineWhorl

	protected static boolean ascending;	// R. audet modified private to protected for JackPineWhorl
	
	public double height;		// whorl height, m
	public int index;			// whorl index
	public boolean nodal;       // or internodal
	
	public int age;             // tree age when shoot appeared
	public double x;			// relative crown depth
	public double depth;		// whorl depth, m
	public double Wfi;			// whorl foliage biomass, kg
	public double asx;			// foliage biomass to stem sapwood area, kg/m2
	public double abx;			// foliage biomass to branch sapwood area, kg/m2
	public double Abi;			// branch sapwood area, m2
	public double Asi;			// stem sapwood area, m2
	public double Dbi;			// branch disused pipe area, m2
	public double DbiCurrent;	// branch disused pipe area put on this year, m2
	public double AbiTot;		// total branch area
	public double Dsi;			// stem disused pipe area, m2
	public double DsiCurrent;	// stem disused pipe area put on this year, m2
	public double Atoti;		// total stem cross-sectional area, m2
	public double Diai;			// stem diameter, cm
	public double Hbi;			// mean pipe length in branches, m
	// added by Raymond Audet
	public int nbBranches; 		// number of branches
	public Map<Integer,PipeQualBranch> branchMap; // branches

	
	
	public PipeQualWhorl() {
		// empty constructor
	}

	public PipeQualWhorl (CTree t, int index) {
		tree = t;
		this.index = index;
		PipeQualWhorl.ascending = false;
	}
	
	protected void initialiseBranches() {
		if (branchMap == null){
			branchMap = new TreeMap<Integer, PipeQualBranch>();

			this.nbBranches = findNbBranches();
			double diameter = findDiameter();
			double azimuthAngle = findStartingAzimuth();
			double insertionAngle;
			int difference = 360 / this.nbBranches;
			for (int i = 1; i <= this.nbBranches; i++) {
				insertionAngle = findInsertionAngle();
				branchMap.put(i, new PipeQualBranch(azimuthAngle, insertionAngle,
						diameter));
				// set up the azimut for the next branch
				azimuthAngle = (azimuthAngle + difference) % 360;
			} 
		}else {
			throw new IllegalStateException("the branches has already been initialized");
		}
	}

	public CTree getTree () {return tree;}
	public void setTree (CTree tree) {this.tree = tree;}        // needed for cloning this object, see CTree.clone ()

	public int compareTo (Object o) throws ClassCastException {
		if (!(o instanceof PipeQualWhorl)) {
			throw new ClassCastException ("Object is not a PipeQualWhorl : "+o);}
		PipeQualWhorl w = (PipeQualWhorl) o;
		if (index < w.index) {
			return PipeQualWhorl.ascending ? -1 : 1;        // asc : t1 < t2
		} else if  (index > w.index) {
			return PipeQualWhorl.ascending ? 1 : -1;        // asc : t1 > t2
		} else {
			return 0;       // t1 == t2
		}
	}

	/** Clone a PipeQualWhorl: first calls super.clone (), then see the PipeQualWhorl
	 *   instance variables.
	 */
	public Object clone () {
		try {
			PipeQualWhorl w = (PipeQualWhorl) super.clone ();
			w.tree = null;
			return w;
		} catch (Exception e) {
			Log.println (Log.ERROR, "PipeQualWhorl.clone ()", 
					"Error while cloning whorl, source whorl="+toString ()
					+" "+e.toString (), e);
			return null;
		}
	}

	public String toString () {
		return "Whorl_"+index
		+" "+((x > 0 || height >= tree.Hs) ? "in Crown" : "")
		+" "+((nodal ? "nodal" : ""));
	} 

	/** Update the diameter and the insertionAngle of all the branches of this Whorl */
	public void updateBranches(){
		for(int i = 1; i <= this.nbBranches;i++){
			this.branchMap.get(i).setDiameter(findDiameter());
			//insertionAngle
		}
	}

	/** the number of branches is a random number between 1 and 6 */
	protected int findNbBranches () {
		return (int) Math.floor(Math.random() * 6 + 1);
	}

	protected double findDiameter () {
		double abiBranche = (this.AbiTot * 1000 / this.nbBranches); // for cm2 conversion;            
		return 2 * Math.sqrt(Math.PI * abiBranche);
	}

	/** Find the startingAzimuth for all the branches of this Whorl. The first branch is 30 degrees right to
	 *  the first branch of the preceding Whorl. If it is the first Whorl, the first branch is at 0 degree. 
	 */
	protected double findStartingAzimuth () {
		return (30 * this.index) % 360;
	}

	/** For now, the insertion angle is a random number between */
	protected double findInsertionAngle () {
		return (double) Math.floor(Math.random() * 90) + 30.0;
	}
}
