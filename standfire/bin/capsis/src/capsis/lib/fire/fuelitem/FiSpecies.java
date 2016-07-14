package capsis.lib.fire.fuelitem;

import java.awt.Color;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jeeb.lib.util.Translator;
import capsis.defaulttype.Species;
import capsis.lib.fire.fuelitem.function.Allom2Function;
import capsis.lib.fire.fuelitem.function.AllomFunction;
import capsis.lib.fire.fuelitem.function.MassProfileFunction;
import capsis.util.EnumProperty;

/**
 * A Species property for the FiPlant
 * 
 * @author F. Pimont, F de Coligny, completely refactored in sept 2013
 */
public class FiSpecies extends EnumProperty implements Serializable, Cloneable, Species {

	private Map<String, FiSpecies> speciesMap; // map containing all species
	public static final String DEFAULT = "Default"; // default species name

	static public final String TRAIT_RESINEOUS = "Resineous";
	static public final String TRAIT_BROADLEAVES = "Broadleaves";
	static public final String TRAIT_HERBACEOUS = "Herbaceous";
	static public final int SPECIES_TAXON_LEVEL = 1;
	static public final int GENUS_TAXON_LEVEL = 2;
	static public final int TRAIT_TAXON_LEVEL = 3;

	private String name;
	private String genus;
	private String trait; // TRAIT_RESINEOUS, TRAIT_BROADLEAVES,
							// TRAIT_HERBACEOUS
	private int taxonomicLevel; // TRAIT_TAXON_LEVEL, GENUS_TAXON_LEVEL or
								// SPECIES_TAXON_LEVEL
	private String information;
	private double sla;
	private Color color;

	// in the following maps, the key is the particle name (type_status : ex.
	// Leave_Live)
	// the keyset is generally known in the model
	private Set<FiParticle> particles; // mass to volume ratio, surface to
										// volume ratio, moisture content
	private Map<String, MassProfileFunction> verticalProfiles; // vertical
																// biomass
																// profile
	private Map<String, MassProfileFunction> horizontalProfiles; // horizontal
																	// biomass
																	// profile

	private double[][] crownGeometry;
	public Allom2Function cbhEq; // equation for crown base height f(DBH,H)
	public AllomFunction hEq; // equation for height f(DBH)
	public Allom2Function cdEq; // equation for crown diameter f(DBH,H)
	public Map<String, Allom2Function> massEqs; // mass equations 

	/**
	 * Constructor. This constructor is used mostly in Fuel manager (database)
	 */
	public FiSpecies(int v, FiSpecies specimen, String trait, String genus, String name, int taxonomicLevel) {
		super(v, name, specimen, Translator.swap("FiSpecies.species"));

		// The species map is shared by all the FiSpecies instance of the
		// project (it is NOT static
		// any more) fc-13.9.2013
		if (specimen != null)
			speciesMap = specimen.speciesMap; // share common data
		else
			speciesMap = new HashMap<String, FiSpecies>();

		this.genus = genus;
		this.trait = trait;
		this.taxonomicLevel = taxonomicLevel;

	}

	/**
	 * Constructor. The specimen param is another FiSpecies instance, needed for
	 * EnumProperty interconnection (they share an EnumPropertyInfo).
	 */
	public FiSpecies(int v, FiSpecies specimen, String trait, String genus, String name, int taxonomicLevel,
			String information, double sla, Color color, double[][] crownGeometry) {
		this(v, specimen, trait, genus, name, taxonomicLevel);

		this.information = information;
		this.sla = sla;
		this.color = color;
		this.particles = new HashSet<FiParticle>();
		this.verticalProfiles = new HashMap<String, MassProfileFunction>();
		this.horizontalProfiles = new HashMap<String, MassProfileFunction>();
		this.massEqs = new HashMap<String, Allom2Function>();
		this.crownGeometry = crownGeometry;

		memoSpecies(getName(), this);

	}

	static public Map<String, FiSpecies> loadSpeciesMap(String speciesFileName) throws Exception {
		return new FiSpeciesLoader(speciesFileName).interpret();
	}

	/**
	 * method to get the default species
	 * 
	 * @return
	 */
	public FiSpecies getDefaultSpecies() {
		return this.speciesMap.get(DEFAULT);
	}

	public void addParticle(FiParticle particle) {
		this.particles.add(particle);
	}

	public Set<FiParticle> getParticles() {
		return this.particles;
	}

	public FiParticle getParticle(String particleName) {
		// System.out.println(" FiSpecies.getParticle ("+particleName+") for species "+name);
		// System.out.println(particles);
		for (FiParticle particle : particles) {
			if (particle.name.equals(particleName)) {
				return particle;
			}
		}
		return getDefaultSpecies().getParticle(particleName);
	}

	public FiParticle getParticle(String type, String status) {
		return getParticle(FiParticle.makeKey(type, status));
	}

	public double[][] getCrownGeometry() {
		return crownGeometry;
	}

	public double getMoisture(String particleName) {
		return getParticle(particleName).moisture;
	}

	public double getMoisture(String type, String status) throws Exception {
		return getParticle(type, status).moisture;
	}

	public void setMoisture(String type, String status, double value) throws Exception {
		setMoisture(FiParticle.makeKey(type, status), value);
	}

	public void setMoisture(String particleName, double value) throws Exception {
		FiParticle particle = getParticle(particleName);
		if (particle.name.equals(DEFAULT)) { // create a new particle
			throw new Exception("FiSpecies.setMoisture: particle" + particleName + " not define for species " + name);
			// FiParticle particle = new FiParticle(particleName);
			// particle.moisture = value;
			// particles.add(particle);
		} else if (particle.moisture == 0d) {
			particle.moisture = value;
		} else {
			throw new Exception("FiSpecies.setMoisture: overwriting value for species " + name + " and particle "
					+ particleName);
		}
	}

	public double getSVR(String particleName) {
		return getParticle(particleName).svr;
	}

	public double getSVR(String type, String status) {
		return getParticle(type, status).svr;
	}

	public void setSVR(String type, String status, double value) throws Exception {
		setSVR(FiParticle.makeKey(type, status), value);
	}

	public void setSVR(String particleName, double value) throws Exception {
		FiParticle particle = getParticle(particleName);
		if (particle.name.equals(DEFAULT)) { // create a new particle
			throw new Exception("FiSpecies.setSVR: particle" + particleName + " not define for species " + name);
			// FiParticle particle = new FiParticle(particleName);
			// particle.svr = value;
			// particles.add(particle);
		} else if (particle.svr == 0d) {
			particle.svr = value;
		} else {
			throw new Exception("FiSpecies.setSVR: overwriting value for species " + name + " and particle "
					+ particleName);
		}
	}

	public double getMVR(String particleName) {
		return getParticle(particleName).mvr;
	}

	public double getMVR(String type, String status) throws Exception {
		return getParticle(type, status).mvr;
	}

	public void setMVR(String type, String status, double value) throws Exception {
		setMVR(FiParticle.makeKey(type, status), value);
	}

	public void setMVR(String particleName, double value) throws Exception {
		FiParticle particle = getParticle(particleName);
		if (particle.name.equals(DEFAULT)) { // create a new particle
			throw new Exception("FiSpecies.setMVR: particle" + particleName + " not define for species " + name);
			// FiParticle particle = new FiParticle(particleName);
			// particle.mvr = value;
			// particles.add(particle);
		} else if (particle.mvr == 0d) {
			particle.mvr = value;
		} else {
			throw new Exception("FiSpecies.setMVR: overwriting value for species " + name + " and particle "
					+ particleName);
		}
	}

	/**
	 * get Specific leaf area (m2/kg)
	 */
	public double getSla() {
		return sla;
	}

	public void setSla(double sla) {
		this.sla = sla;
	}

	/**
	 * Returns the species with the given name, null if not found.
	 */
	// This method was static: several projects must be able to load several
	// species files without
	// sharing a static map: source of errors, FIXED fc-13.9.2013
	public FiSpecies getSpecies(String speciesName) {
		return speciesMap.get(speciesName);
	}

	public boolean isDefaultSpecies() {
		// fc-2.2.2015 added name != null (a bug occurred in the terminal)
		return name != null && this.name.equals(DEFAULT);
	}

	public boolean containsSpecies(String speciesName) {
		return speciesMap.containsKey(speciesName);
	}

	// This method was static: several projects must be able to load several
	// species files without
	// sharing a static map: source of errors, FIXED fc-13.9.2013
	private void memoSpecies(String speciesName, FiSpecies species) { // fc -
																		// 10.7.2007
		speciesMap.put(speciesName, species);
	}

	@Override
	public String toString() {
		return Translator.swap(getName());
	}

	/**
	 * Needed to compare FiSpecies instances after de-serialization.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} // fc - 29.11.2001
		boolean equal = false;
		if (((FiSpecies) obj).getValue() == this.getValue()) {
			equal = true;
		}
		return equal;
	}

	public String getTrait() {
		return trait;
	}

	public String getGenus() {
		return genus;
	}

	public int getTaxonomicLevel() {
		return taxonomicLevel;
	}

	public String getInformation() {
		return information;
	}

	public void setInformation(String information) {
		this.information = information;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public MassProfileFunction getHorizontalProfile(String particleName) {
		if (horizontalProfiles.containsKey(particleName)) {
			return horizontalProfiles.get(particleName);
		} else {
			return getDefaultSpecies().getHorizontalProfile(particleName);
		}
	}

	public MassProfileFunction getHorizontalProfile(String particuleType, String status) {
		return getHorizontalProfile(FiParticle.makeKey(particuleType, status));
	}

	public void addHorizontalProfile(String particuleType, String status, MassProfileFunction horizontalProfile) {
		horizontalProfiles.put(FiParticle.makeKey(particuleType, status), horizontalProfile);
	}
	
	public MassProfileFunction getVerticalProfile(String particleName) {
		if (verticalProfiles.containsKey(particleName)) {
			return verticalProfiles.get(particleName);
		} else {
			return getDefaultSpecies().getVerticalProfile(particleName);
		}
	}
	
	public double getCumulativeVerticalFraction(double relHeightInCrown, String particleName, FiPlant plant) {
		MassProfileFunction mpf = this.getVerticalProfile(particleName);
		if (relHeightInCrown > 1d) { return 0d;}
		if (relHeightInCrown < 0d) { return 0d;}
		double res = 0d;
		int nh = 10;
		for (int ih = 0; ih < nh;ih++) {
			double relH = relHeightInCrown / nh * (ih + 0.5);
			res += mpf.f (relH, plant) / nh;
		}
		return res;
	}

	public MassProfileFunction getVerticalProfile(String particuleType, String status) {
		return getVerticalProfile(FiParticle.makeKey(particuleType, status));
	}

	public void addVerticalProfile(String particuleType, String status, MassProfileFunction verticalProfile) {
		verticalProfiles.put(FiParticle.makeKey(particuleType, status), verticalProfile);
	}

}
