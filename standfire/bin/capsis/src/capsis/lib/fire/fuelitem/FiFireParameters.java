package capsis.lib.fire.fuelitem;

import java.io.Serializable;

/**
 * This class is attached to each plant and contains the information about the
 * last fire experimented by the plant. This class is mostly used for fire
 * effect assessment
 * 
 * @author pimont
 * 
 */
public class FiFireParameters implements Serializable {
	// the following parameters are used to describe fire conditions for
	// empirical models
	private double fireIntensity; // kW/m
	private double residenceTime; // s
	private double maxTemperatureReached; // �C
	private double ambiantTemperature; // �C
	private double windVelocity; // m/s
	private double airHygrometry; // %

	public FiFireParameters() {
		this.fireIntensity = 0d;
		this.residenceTime = 0d;
		this.maxTemperatureReached = 20d;
		this.ambiantTemperature = 20d;
		this.windVelocity = 0d;
	}

	public FiFireParameters(double _fireIntensity, double _residenceTime, double _maxTemperatureReached,
			double _ambiantTemperature, double _windVelocity) {
		this.fireIntensity = _fireIntensity;
		this.residenceTime = _residenceTime;
		this.maxTemperatureReached = _maxTemperatureReached;
		this.ambiantTemperature = _ambiantTemperature;
		this.windVelocity = _windVelocity;
	}

	public double getFireIntensity() {
		return fireIntensity;
	}

	public double getResidenceTime() {
		return residenceTime;
	}

	public double getMaxTemperatureReached() {
		return maxTemperatureReached;
	}

	public double getAmbiantTemperature() {
		return ambiantTemperature;
	}

	public double getWindVelocity() {
		return windVelocity;
	}

	public double getAirHygrometry() {
		return airHygrometry;
	}

	public void setFireIntensity(double value) {
		fireIntensity = value;
	}

	public void setResidenceTime(double value) {
		residenceTime = value;
	}

	public void setMaxTemperatureReached(double value) {
		maxTemperatureReached = value;
	}

	public void setAmbiantTemperature(double value) {
		ambiantTemperature = value;
	}

	public void setWindVelocity(double value) {
		windVelocity = value;
	}

	public void setAirHygrometry(double value) {
		airHygrometry = value;
	}
}
