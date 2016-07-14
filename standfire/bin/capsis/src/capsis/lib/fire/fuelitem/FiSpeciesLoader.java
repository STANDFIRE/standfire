package capsis.lib.fire.fuelitem;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import jeeb.lib.util.Import;
import jeeb.lib.util.Record;
import jeeb.lib.util.RecordSet;
import jeeb.lib.util.Vertex2d;
import capsis.lib.fire.fuelitem.function.Allom2Function;
import capsis.lib.fire.fuelitem.function.AllomFunction;
import capsis.lib.fire.fuelitem.function.MassProfileFunction;

/**
 * A loader for a FiSpecies file
 * 
 * @author F. Pimont, F. de Coligny - September 2013
 */
public class FiSpeciesLoader extends RecordSet {

	private String fileName;
	final private String VERTICAL = "Vertical";
	final private String HORIZONTAL = "Horizontal";
	final String CBH ="CBH"; //crown base height (m)
	final String H ="H"; // height (m)
	final String CD ="CD";// crown diameter (m)	

	@Import
	static public class SpeciesRecord extends Record {
		public SpeciesRecord() {
			super();
		}
		public SpeciesRecord(String line) throws Exception {
			super(line);
		}
		public String name;
		public String genus;
		public String trait;
		public int taxonomicLevel;
		public String info;
		public double sla;
		public String colorRGB; // color of the species
		public Collection crownGeometry;
	}

	@Import
	static public class ParticulePropertiesRecord extends Record {
		public ParticulePropertiesRecord() {
			super();
		}
		public ParticulePropertiesRecord(String line) throws Exception {
			super(line);
		}
		public String particleType;// Leave / Twig1 / Twig2 / Twig3
		public String status; // LIVE / DEAD
		public String speciesName;
		public double mvr;
		public double svr;
		public double moisture;
		public String source; // data source
	}

	@Import
	static public class DimensionEquationRecord extends Record {
		public DimensionEquationRecord() {
			super();
		}
		public DimensionEquationRecord(String line) throws Exception {
			super(line);
		}
		public String dimension; // CBH, H, CD
		public String speciesName;
		public String function; // e.g. aDBHpowb(0.996;2.403)
		public String source; // data source
	}
	
	@Import
	static public class MassRecord extends Record {

		public MassRecord() {
			super();
		}
		public MassRecord(String line) throws Exception {
			super(line);
		}
		public String particleType; // Leave / Twig1 / Twig2 / Twig3
		public String status; // LIVE / DEAD
		public String speciesName;
		public String function; // e.g. aDBHpowb(0.996;2.403)
		public String source; // data source
	}

	@Import
	static public class MassProfileRecord extends Record {
		public MassProfileRecord() {
			super();
		}
		public MassProfileRecord(String line) throws Exception {
			super(line);
		}
		public String particleType; // Leave / Twig1 / Twig2 / Twig3
		public String status; // LIVE / DEAD
		public String speciesName;
		public String equationType; // vertical / horizontal
		public String function; // e.g. function1(0.996;2.403;13.086), biomass
								// distribution function: f (hrel, Tree)
		public String source; // data source
	}

	/**
	 * Constructor 1: reads the given file
	 */
	public FiSpeciesLoader(String fileName) throws Exception {
		super();
		this.fileName = fileName;
		createRecordSet(fileName);
	}

	/**
	 * Interprets the species file, creates and returns a map.
	 */
	public Map<String, FiSpecies> interpret() throws Exception {
		Map<String, FiSpecies> speciesMap = new HashMap<String, FiSpecies>();
		int speciesCode = 0;
		FiSpecies specimen = null;
		System.out.println("Species list from file " + fileName);
		for (Iterator i = this.iterator(); i.hasNext();) {
			Object record = i.next();
			// species record
			if (record instanceof SpeciesRecord) {
				SpeciesRecord r = (SpeciesRecord) record;
				speciesCode++;
				Color color = decodeColor(r.colorRGB);
				int ni = r.crownGeometry.size();
				int nj = 2;
				double[][] cg = new double[ni][nj];
				int k = 0;
				for (Object o : r.crownGeometry) {
					Vertex2d v2 = (Vertex2d) o;
					cg[k][0] = v2.x;
					cg[k][1] = v2.y;
					k++;
				}
				FiSpecies sp = new FiSpecies(speciesCode, specimen, r.trait, r.genus, r.name, r.taxonomicLevel, r.info,
						r.sla, color, cg);
				if (specimen == null)
					specimen = sp;
				speciesMap.put(sp.getName(), sp);
				System.out.println("-" + sp.getName());

			} else if (record instanceof ParticulePropertiesRecord) {
				ParticulePropertiesRecord r = (ParticulePropertiesRecord) record;
				if (!(r.status.equals(FiParticle.LIVE) || r.status.equals(FiParticle.DEAD))) {
					throw new Exception("wrong format in " + fileName + " near record " + record + "particle status "
							+ r.status + " is unkown");
				}
				if (!specimen.containsSpecies(r.speciesName)) {
					throw new Exception("unkown species name for MVR, SVR, moisture:" + r.speciesName);
				}
				FiSpecies sp = specimen.getSpecies(r.speciesName);
				try {
					FiParticle pt = new FiParticle(r.particleType, r.status, r.mvr, r.svr, r.moisture, r.speciesName);
					sp.addParticle(pt);
					// System.out.println("FiSpeciesLoader: species "+sp.getName
					// ()+ " particle "+pt.name+" "+ pt);
				} catch (Exception e) {
					throw new Exception("wrong format in " + fileName + " near record " + record
							+ "particule type or status unknown", e);
				}
			} else if (record instanceof DimensionEquationRecord) {
				DimensionEquationRecord r = (DimensionEquationRecord) record;
				if (!specimen.containsSpecies(r.speciesName)) {
					throw new Exception("unknown species name for CBH,H or CD :" + r.speciesName);
				}
				if (!(r.dimension.equals(this.CBH) || r.dimension.equals(this.H)|| r.dimension.equals(this.CD))) {
					throw new Exception("wrong format in " + fileName + " near record " + record + "dimension :  "
							+ r.dimension + " is unkown");
				}
				FiSpecies sp = specimen.getSpecies(r.speciesName);
				if (r.dimension.equals(this.CBH)) {
					sp.cbhEq = Allom2Function.getFunction(r.function);
				} else if (r.dimension.equals(this.H)) {
					sp.hEq = AllomFunction.getFunction(r.function);
				} else {
					sp.cdEq = Allom2Function.getFunction(r.function);
				}
				//System.out.println(r.speciesName+":"+r.dimension+","+r.function);
			}else if (record instanceof MassRecord) {
				MassRecord r = (MassRecord) record;
				if (!(r.status.equals(FiParticle.LIVE) || r.status.equals(FiParticle.DEAD))) {
					throw new Exception("wrong format in " + fileName + " near record " + record + "particle status "
								+ r.status + " is unkown");
				}
				if (!specimen.containsSpecies(r.speciesName)) {
					throw new Exception("unkown species name for mass :" + r.speciesName);
				}
	         	FiSpecies sp = specimen.getSpecies(r.speciesName);
	         	sp.massEqs.put(FiParticle.makeKey(r.particleType, r.status), Allom2Function.getFunction(r.function));
	         	
			} else if (record instanceof MassProfileRecord) {
				MassProfileRecord r = (MassProfileRecord) record;
				if (!(r.status.equals(FiParticle.LIVE) || r.status.equals(FiParticle.DEAD))) {
					throw new Exception("wrong format in " + fileName + " near record " + record + "particle status "
							+ r.status + " is unkown");
				}
				if (!specimen.containsSpecies(r.speciesName)) {
					throw new Exception("unkown species name for mass crown profile:" + r.speciesName);
				}
				if (!(r.equationType.equals(this.VERTICAL) || r.equationType.equals(this.HORIZONTAL))) {
					throw new Exception("wrong format in " + fileName + " near record " + record + "equation Type "
							+ r.equationType + " is unkown");
				}
				FiSpecies sp = specimen.getSpecies(r.speciesName);
				if (r.equationType.equals(this.VERTICAL)) {
					sp.addVerticalProfile(r.particleType, r.status, MassProfileFunction.getFunction(r.function));
				} else {// HORIZONTAL
					sp.addHorizontalProfile(r.particleType, r.status, MassProfileFunction.getFunction(r.function));
				}
			} else {
				throw new Exception("wrong format in " + fileName + " near record " + record);
			}
		}
		return speciesMap;
	}

	private Color decodeColor(String encodedColor) throws Exception {
		try {
			encodedColor = encodedColor.trim();
			StringTokenizer st = new StringTokenizer(encodedColor, ",");
			int r = Integer.parseInt(st.nextToken().trim());
			int g = Integer.parseInt(st.nextToken().trim());
			int b = Integer.parseInt(st.nextToken().trim());
			return new Color(r, g, b);
		} catch (Exception e) {
			throw new Exception("FiSpeciesLoader: could not decode this color: " + encodedColor, e);
		}
	}

}
