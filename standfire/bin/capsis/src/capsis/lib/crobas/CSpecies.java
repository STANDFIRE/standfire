package capsis.lib.crobas;

import java.io.Serializable;

import jeeb.lib.util.Translator;
import jeeb.lib.util.autoui.annotations.AutoUI;
import jeeb.lib.util.autoui.annotations.Editor;
import capsis.defaulttype.Species;
import capsis.util.EnumProperty;

/**	Species specific parameters used in CroBas (A. Makela, 1997. Forest Science). 
* 	Values are set to those estimated through empirical relations for jack pine.
*	@author R. Schneider - 20.5.2008
*/
@AutoUI(title="CSpecies.name")  // this annotation is needed for the AutoPanel, all variables with @Editor will be in it
abstract public class CSpecies extends EnumProperty implements Species, Serializable {

	@Editor(group="CSpecies")
	public double avoinMin;			// Maximum mean available crown volume (m3) per meter of crown ???
	@Editor(group="CSpecies")
	public double avoinMax;			// Minimum mean available crown volume (m3) per meter of crown ???
		
	// Parameter definitions
	// Relationship crown area / leaf biomass
	@Editor(group="CSpecies")
	public double zMin;				// Minimum fractal dimension of the crown 
									// how completely a fractal appears to fill space
	@Editor(group="CSpecies")
	public double zMax;				// Maximum fractal dimension of the crown
	@Editor(group="CSpecies")
	public double ksiMin;			// Minimum surface area density of the foliage, kg/m^2z
	@Editor(group="CSpecies")
	public double ksiMax;			// Minimum surface area density of the foliage, kg/m^2z
	
	// Relationship crown radius/crown length
	@Editor(group="CSpecies")
	public double cb;				// ratio of crown radius to crown length
	
	// Relationship between crown length and average branch length (used in initialization)
	@Editor(group="CSpecies")
	public double br1;				// average branch length as a function of depth parameter1 (added EB 2010-12-09)
	@Editor(group="CSpecies")
	public double br2;				// average branch length as a function of depth parameter2 (added EB 2010-12-09)
	@Editor(group="CSpecies")
	public double gbl;				// gbl = 1 + (avoin-avoin_min) * (gbl_species - 1) (added EB 2010-12-09)
	@Editor(group="CSpecies")
	public double gammab;			// branch average pipe length to crown length ratio
	
	// Relationship transport root length/total height
	@Editor(group="CSpecies")
	public double ct;				// ratio of transport root length to stem length (added EB 2010-12-08)
	@Editor(group="CSpecies")
	public double gammat;			// transport root to stem pipe length ratio

	// Pipe model
	@Editor(group="CSpecies")
	public double alphas;			// stem sapwood area to foliage biomass ratio
	@Editor(group="CSpecies")
	public double alphab;			// branch sapwood area to foliage biomass ratio
	@Editor(group="CSpecies")
	public double alphat;			// transport root sapwood area to foliage biomass ratio				
	@Editor(group="CSpecies")
	public double alphar;			// Functional balance parameter: fine root to foliage biomass ratio

	// Form factors	
	@Editor(group="CSpecies")
	public double phis;				// Form factor of sapwood in stem below crown
	@Editor(group="CSpecies")
	public double phic;				// Form factor of sapwood in stem above crown
	@Editor(group="CSpecies")
	public double phib;				// Form factor of sapwood in branches
	@Editor(group="CSpecies")
	public double phit;				// Form factor of sapwood in transport roots
	@Editor(group="CSpecies")
	public double phivol;			// Form factor for volume calculation volume = phivol * dbh^2 * htot

	// Densities
	@Editor(group="CSpecies")
	public double rhos;				// Wood density of sapwood in stem, kg/m^3
	@Editor(group="CSpecies")
	public double rhob;				// Wood density of sapwood in branches, kg/m^3
	@Editor(group="CSpecies")
	public double rhot;				// Wood density of sapwood in transport roots, kg/m^3

	// Maintenance rate of respiration
	@Editor(group="CSpecies")
	public double r1;				// specific maintenance respiration rate of foliage and fine roots, kg C / kg dry weight year
	@Editor(group="CSpecies")
	public double r2;				// specific maintenance respiration rate of wood (stem, branches, transport roots), kg C / kg dry weight year

	// Self-Pruning coefficients
	@Editor(group="CSpecies")
	public double aq;				// parameter related to self-pruning
	@Editor(group="CSpecies")
	public double q;				// degree of control by crown coverage of self-pruning
	@Editor(group="CSpecies")
	public double minSelfPrun;		// minimum self-pruning coefficient

	// Self-mortality coefficients
	@Editor(group="CSpecies")
	public double m0;				// specific mortality rate independent of density, /year
	@Editor(group="CSpecies")
	public double m1;				// density-dependent mortality parameter, /year
	@Editor(group="CSpecies")
	public double p;				// degree of control by crown coverage of mortality
	@Editor(group="CSpecies")
	public double delta1;			// diameter increment mortality parameter
	@Editor(group="CSpecies")
	public double delta2;			// diameter increment squared mortality parameter
	@Editor(group="CSpecies")
	public double rNs0;				// proportion of stems which die when height is less than 2 m
	@Editor(group="CSpecies")
	public double rNs1;				// total stand crown coverage at which mortality is driven by crown coverage 

	// Net photosynthesis
	@Editor(group="CSpecies")
	public double an;				// specific leaf area, m^2/kg
	@Editor(group="CSpecies")
	public double P0;				// maximum rate of canopy photosynthesis per unit area, kg C / m^2 year
	@Editor(group="CSpecies")
	public double k;				// extinction coefficient
	@Editor(group="CSpecies")
	public double sa;				// leaf area to leaf biomass ratio per m2 coverage
	@Editor(group="CSpecies")
	public double asig;				// photosynthesis reduction due to crown radius 
	// public double aSigma;		// comment to see if it creates an error 
	@Editor(group="CSpecies")
	public double fc;				// carbon content of dry weight, kg C / kg dry weight
	@Editor(group="CSpecies")
	public double rg;				// specific growth respiration rate, kg C / kg dry weight
	@Editor(group="CSpecies")
	public double Y = fc + rg;		// carbon use efficiency, kg C / kg dry weight

	// Senescence rates
	@Editor(group="CSpecies")
	public double sf;				// specific senescence rate of foliage
	@Editor(group="CSpecies")
	public double sr;				// specific senescence rate of fine roots
	@Editor(group="CSpecies")
	public double ds0;				// specific sapwood in stem turnover rate per unit relative pruning
	@Editor(group="CSpecies")
	public double db0;				// specific sapwood in branches turnover rate per unit relative pruning
	@Editor(group="CSpecies")
	public double dt0;				// specific sapwood in transport roots turnover rate per unit relative pruning
	@Editor(group="CSpecies")
	public double ds1;				// specific sapwood in stem turnover rate in case of no pruning
	@Editor(group="CSpecies")
	public double db1;				// specific sapwood in branches turnover rate in case of no pruning
	@Editor(group="CSpecies")
	public double dt1;				// specific sapwood in transport roots turnover rate in case of no pruning

	@Editor(group="CSpecies")
	public double turnOverAge;		// age at which there is sapwood turnover due to ageing, years
	
	@Editor(group="CSpecies")
	public double psis;				// Form factor of senescent sapwood in stem below crown
	@Editor(group="CSpecies")
	public double psic;				// Form factor of senescent sapwood in stem above crown
	@Editor(group="CSpecies")
	public double psib;				// Form factor of senescent sapwood in branches
	@Editor(group="CSpecies")
	public double psit;				// Form factor of senescent sapwood in transport roots

	// Crontrol function parameters, according to A. Makela 1999 Functional ecology
	@Editor(group="CSpecies")
	public double taur;				// fine root control function
	@Editor(group="CSpecies")
	public double tauw;				// wood control functions: sapwood in stem, branches, transport roots
	@Editor(group="CSpecies")
	public double tauh;				// foliage control function

	// Standard volume table parameters for jack pine (1-3): Vtot = vol1 + vol2 * dbh + vol3 * ht * dbh^2, dm^3
	// Standard volume table parameters for spruces (black and white) (1-7): Vtot = vol1 + vol2 * dbh + vol3 * ht * dbh^2, dm^3
	@Editor(group="CSpecies")
	public double vol1;				// dm^3
	@Editor(group="CSpecies")
	public double vol2;				// dm^3/cm
	@Editor(group="CSpecies")
	public double vol3;				// dm^3/m cm^2
	@Editor(group="CSpecies")
	public double vol4;				// dm^3/cm m
	@Editor(group="CSpecies")
	public double vol5;				// dm^3/cm^2
	@Editor(group="CSpecies")
	public double vol6;				// dm^3/m^2
	@Editor(group="CSpecies")
	public double vol7;				// dm^3/cm m^2

	@Editor(group="CSpecies")
	protected PipeQualSpecies pipeQualSpecies;	// whorls
	// Foliage mass/active pipe area ratio in the stem
	// Using Makela 2002 (for calculateAsx in CTree)
	@Editor(group="CSpecies")
	public double amin=150;
	@Editor(group="CSpecies")
	public double amax=500;
	
	// Distance from treetop where foliage mass/active pipe area ratio in stem is 0.5(amax-amin)
	@Editor(group="CSpecies")
	public double bs=0.5;
	
	
	/**	Constructor.
	*	The public variables must be set after, see JpJackPineLoader.
	*	<PRE>
	*	CSpecies s = new CSpecies (0, "JackPpine", null, "Species");
	*	s.z = 1.5;
	*	s.zeta = 0.03;
	*	...
	*	</PRE>
	*/
	public CSpecies (	int 			v, 
						String 			name, 
						EnumProperty	model, 
						String 			propertyName 
						/* String			crobasLevel */
					) {
		super (v, name, model, propertyName);
		
//		if (crobasLevel.equals (CSettings.PIPEQUAL_NODAL) 
//				|| crobasLevel.equals (CSettings.PIPEQUAL_INTER_NODAL)) {
		
		// fc-18.2.2011 species always has the pipeQual description (but the trees may not)
		setPipeQualSpecies (new PipeQualSpecies ());
		
//		}
	}
	
	public void setPipeQualSpecies (PipeQualSpecies v) {pipeQualSpecies = v;}
	public PipeQualSpecies getPipeQualSpecies () {return pipeQualSpecies;}
	
	public String toString () {return Translator.swap (getName ());}

	// Needed to compare CSpecies instances after de-serialization.
	public boolean equals (Object obj) {
		if (obj == null) {return false;}
		boolean equal = false;
		if (((CSpecies) obj).getValue () == this.getValue ()) {
			equal = true;
		}
		return equal;
	}

	
	
}


