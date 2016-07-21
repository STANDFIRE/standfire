package capsis.lib.fire.intervener.physicalfireeffect;

import capsis.lib.fire.fuelitem.FiPlant;
import fireparadox.model.FmModel;

/**
 * This class contain the different properties require for damage computation in
 * a given firetec voxel
 * 
 * @author pimont
 * 
 */
public class FiretecVoxel {

	public static final boolean LEAVEb = true;

	public int i;// index in firetecmatrix
	public int j;
	public int k;

	public double plantMoisture; // fraction
	public double plantVolume; // crown volume in the cell
	public double plantBiomass; // biomass in the cell

	public double leaveLiveCellPop; // starting from 1
	public double leaveTemperature; // K
	public boolean leaveScorched;
	public double budLiveCellPop; //
	public double budTemperature;
	public boolean budKilled;

	public double maxGasTemperatureInCell;// K
	public double residenceTimeInCell;

	private FiPlant plant;
	private double D; // current particle diameter

	public FiretecVoxel(int i, int j, int k, double plantVolume, double plantBiomass, FiPlant plant) {
		// FiretecMatrix fm) {
		this.i = i;
		this.j = j;
		this.k = k;
		this.plantVolume = plantVolume;
		this.plantBiomass = plantBiomass;
		this.plant = plant;
		this.leaveLiveCellPop = 1d;
		this.budLiveCellPop = 1d;
		this.leaveScorched = false;
		this.budKilled = false;
		this.residenceTimeInCell = 0d;
		this.maxGasTemperatureInCell = 0d;
	}

	public void setInitialPhysicalProperties(double tGas) {
		this.leaveTemperature = tGas;
		this.budTemperature = tGas;
	}

	// compute Nusselt number assuming a given D and gas velocity
	private double Nusselt(double velocity) {
		double B = 0.05;
		double n = 0.7;
		double Re = velocity * D / (1.5e-5);
		return B * Math.pow(Re, n);
	}

	// compute ConvectiveCoefficient assuming a given D and gas velocity
	private double ConvectiveCoefficient(double velocity) {
		double Nu = Nusselt(velocity);
		return 2.37e-2 * Nu / D;
	}

	/**
	 * This method update the temperature and cell properties of the fuel on a
	 * period "time" for different value of parameters
	 * 
	 * @param time
	 *            (s)
	 * @param tGas
	 *            (K)
	 * @param Tambiant
	 *            (K)
	 * @param velocity
	 *            (m/s)
	 * @param radFlux
	 *            (kW/m2)
	 * @throws Exception
	 */
	public void update(double time, double tGas, double Tambiant, double velocity, double radFlux) throws Exception {

		maxGasTemperatureInCell = Math.max(tGas, maxGasTemperatureInCell);
		if (tGas > 500d)
			residenceTimeInCell += time;
		if (!leaveScorched) {
			// Leaves
			double SVR = plant.getSVR(FmModel.LEAVE_LIVE);
			double MVR = plant.getMVR(FmModel.LEAVE_LIVE);
			double cp = 1d;
			double dt = 0.1;// s
			leaveTemperature = computeTnew(time, dt, leaveTemperature, cp, MVR, SVR, tGas, Tambiant, velocity, radFlux);
			double Z = FiLocalPhysicalProperties.getZ(plant.getSpeciesName(), LEAVEb);
			double E = FiLocalPhysicalProperties.getE(plant.getSpeciesName(), LEAVEb);
			leaveLiveCellPop = computeSnew(time, leaveLiveCellPop, leaveTemperature, Z, E);

			if (leaveLiveCellPop < 0.5) {
				leaveScorched = true;
			}
		}
	}

	/**
	 * This method computes the new value of solid temperature after "time"
	 * second During this period of time, the physical conditions are assumed
	 * constant This method has a internal time step dt (but there is always at
	 * least one iteration of the temperature update even if dt is bigger than
	 * timeBetweenFrame
	 * 
	 * @param time
	 *            (s) final time
	 * @param dt
	 *            internal time step (s)
	 * @param Told
	 *            (K initial solid temperature)
	 * @param cp
	 *            wood thermic capacity
	 * @param MVR
	 *            (kg/m3
	 * @param SVR
	 *            (m2/m3)
	 * @param tGas
	 *            (K)
	 * @param Tambiant
	 *            (K, for radiation sink)
	 * @param radFlux
	 *            (kW/m2, for radiation source)
	 * @return
	 */
	private double computeTnew(double time, double dt, double Told, double cp, double MVR, double SVR, double tGas,
			double Tambiant, double velocity, double radFlux) {

		// TODO : should be different for flat leaves
		D = 2d / SVR;
		double h = ConvectiveCoefficient(velocity);
		double Tnew = Told;
		int itNumber = Math.max(1, (int) Math.round(time / dt));
		dt = time / itNumber;
		for (int it = 1; it <= itNumber; it++) {
			double emittedRadFlux = 0d;
			// double emittedRadFlux = 5.67e-8 * (Math.pow(Tnew, 4d) - Math.pow(
			// Tambiant, 4d));
			Tnew += dt / (MVR * cp) * SVR * (h * (tGas - Tnew) + radFlux - emittedRadFlux);
		}
		return Tnew;
	}

	/**
	 * This method computes the cell number in the population after exposure to
	 * T during time
	 * 
	 * @param time
	 *            (s)
	 * @param Sold
	 *            (Original Cell population in fraction)
	 * @param T
	 *            (solid temperature K)
	 * @param Z
	 *            (frequency factor in s-1)
	 * @param E
	 *            (activation energy)
	 * @return
	 */

	private double computeSnew(double time, double Sold, double T, double Z, double E) {
		// TODO temporary for test...

		if (T > 273.5 + 60.0) {
			return 0d;
		} else {
			return Sold;
		}
		// double k = Z * Math.exp(-E / (8.314 * T));
		// return Sold * (1d - k * time);
	}
}
