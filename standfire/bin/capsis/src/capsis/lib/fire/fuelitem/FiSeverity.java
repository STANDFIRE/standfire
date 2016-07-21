package capsis.lib.fire.fuelitem;

import java.io.Serializable;

public class FiSeverity implements Serializable {

	// damage variables
	// private double hCMin; // min height of trunk carbonization
	// private double hCMax; // max height of trunk carbonization

	// Bole damage : BLC : in %, derived from boleCharHeight and treeHeight
	private double meanBoleLengthCharred = 0d; // %
	private double minBoleLengthCharred = 0d; // %
	private double maxBoleLengthCharred = 0d; // %

	// Bark Damage : BCN +cambiumIsKilled, etc.
	// BCN
	// 0 : no damage
	// 1 :light, when bark is not completely blackened
	// 2 :moderate, when bark is uniformly black
	// 3 :deep, when bark is deeply charred, with the surface characteristics of
	// the bark no more discernible.mortality.

	private double meanBarkCharNote = 0d; // 0-3
	// private double minBarkCharNote = -1; // 0-3
	private double maxBarkCharNote = 0d; // 0-3
	private boolean cambiumIsKilled = false;

	// canopy damage
	private double crownLengthScorched = 0d; // CLS (m) done
	private double crownVolumeScorched = 0d; // CVS(%) done
	private double crownScorchHeight = 0d; // m done
	private double crownKillHeight = 0d; // m
	private double crownLengthKilled = 0d; // CLK (m) done
	private double crownVolumeKilled = 0d; // CVK(%) done

	// mortality
	private double mortalityProbability = 0d; // after fire
	private boolean isKilled = false; // status after fire

	private boolean burn = false;

	/**
	 * Returns true if any damage is recorded in this severity object for the
	 * owner FiPlant.
	 */
	public boolean containsDamages() { // fc+sg-6.1.2015
		return burn || cambiumIsKilled || crownKillHeight != 0 || crownLengthKilled != 0 || crownLengthScorched != 0
				|| crownScorchHeight != 0 || crownVolumeKilled != 0 || crownVolumeScorched != 0 || isKilled
				|| maxBarkCharNote != 0 || maxBoleLengthCharred != 0 || meanBarkCharNote != 0
				|| meanBoleLengthCharred != 0 || minBoleLengthCharred != 0 || mortalityProbability != 0;
	}

	public FiSeverity() {
	}

	public boolean isAlreadyBurn() {
		return burn;
	}

	public void alreadyBurn(boolean value) {
		this.burn = value;
	}

	// this method compute CrownVolume under a height h for a crown length
	// and tree height
	// assuming a paraboloid distribtion (Ryan @ Peterson 1986)
	// this could be done using the real characteristics of the fuel
	public double computeCrownVolumeUnderH(double h, double treeHeight, double crownBaseHeight) {
		if (h <= crownBaseHeight) {
			return 0.;
		}
		if (h >= treeHeight) {
			return 100.;
		}
		double crownLength = treeHeight - crownBaseHeight;
		return 100. * (h - treeHeight + crownLength) * (treeHeight - h + crownLength) / (crownLength * crownLength);
	}

	// this method compute CrownVolume under a height h for a crown length
	// and tree height, crownBased Height and maxcrowndiameterheight
	// (Linn 2005)
	// this could be done using the real characteristics of the fuel also
	public double computeCrownVolumeUnderH2(double sh, FiPlant plant) {
		double ht = plant.getHeight();
		double cbh = plant.getCrownBaseHeight();
		double l = ht - cbh; // crown length
		double cmdh = plant.getMaxDiameterHeight();
		double d = 4. / 5. * l; // upper part of the crown
		double h = 1. / 5. * l; // lower part of the crown

		if (cmdh < cbh || cmdh > ht) { // wrong value of cmdh
			d = 4. / 5. * l; // upper part of the crown
			h = 1. / 5. * l; // lower part of the crown
		}
		double vtot = h * h / (3.0 * l * l) * (1.0 + 0.5 * d / h) + 0.5 - l / (6.0 * d) + h * h * h / (6.0 * d * l * l);
		if (sh <= cbh) {
			return 0.;
		}
		if (sh <= cbh + h) {// lower part
			return 100.0 * (Math.pow(sh - cbh, 3.0) * (1 + 0.5 * d / h) / (3.0 * l * l * h)) / vtot;
		}
		if (sh <= ht) {// lower part
			return 100.0
					* (h * h * h * (1 + 0.5 * d / h) / (3.0 * l * l * h) + 0.5
							* (l * l * (sh - cbh - h) - 0.33 * (Math.pow(sh - cbh, 3.0) - h * h * h)) / (l * l * d))
					/ vtot;
		}
		return 100.;
	}

	public void computeCrownLengthScorched(double treeHeight, double crownBaseHeight) {
		double res;
		double crownLength = treeHeight - crownBaseHeight;

		if (crownScorchHeight <= crownBaseHeight) {
			res = 0.;
		} else if (crownScorchHeight >= treeHeight) {
			res = crownLength;
		} else {
			res = crownScorchHeight - crownBaseHeight;
		}
		// System.out.println("TH " + treeHeight + "CBH " + crownBaseHeight
		// + "CSH " + crownScorchHeight + "res " + res);
		setCrownLengthScorched(res);
	}

	public void computeCrownLengthKilled(double treeHeight, double crownBaseHeight) {
		double res;
		double crownLength = treeHeight - crownBaseHeight;

		if (crownKillHeight <= crownBaseHeight) {
			res = 0.;
		} else if (crownKillHeight >= treeHeight) {
			res = crownLength;
		} else {
			res = crownKillHeight - crownBaseHeight;
		}
		setCrownLengthKilled(res);
	}

	public boolean getIsKilled() {
		return isKilled;
	}

	public double getMortalityProbability() {
		return mortalityProbability;
	}

	public boolean getCambiumIsKilled() {
		return cambiumIsKilled;
	}

	public double getMaxBarkCharNote() {
		return maxBarkCharNote;
	}

	public double getMeanBarkCharNote() {
		return meanBarkCharNote;
	}

	public double getCrownVolumeScorched() {
		return crownVolumeScorched;
	}

	public double getCrownVolumeKilled() {
		return crownVolumeKilled;
	}

	public double getCrownScorchHeight() {
		return crownScorchHeight;
	}

	public double getCrownKillHeight() {
		return crownKillHeight;
	}

	public double getCrownLengthScorched() {
		return crownLengthScorched;
	}

	public double getCrownLengthKilled() {
		return crownLengthKilled;
	}

	public double getMeanBoleLengthCharred() {
		return meanBoleLengthCharred;
	}

	public double getMinBoleLengthCharred() {
		return minBoleLengthCharred;
	}

	public double getMaxBoleLengthCharred() {
		return maxBoleLengthCharred;
	}

	public void setIsKilled(boolean value) {
		isKilled = value;
	}

	public void setMortalityProbability(double value) {
		mortalityProbability = value;
	}

	public void setCambiumIsKilled(boolean value) {
		cambiumIsKilled = value;
	}

	public void setMaxBarkCharNote(double value) {
		maxBarkCharNote = value;
	}

	public void setMeanBarkCharNote(double value) {
		meanBarkCharNote = value;
	}

	public void setCrownVolumeScorched(double value) {
		crownVolumeScorched = value;
	}

	public void setCrownVolumeKilled(double value) {
		crownVolumeKilled = value;
	}

	public void setCrownScorchHeight(double value) {
		crownScorchHeight = value;
	}

	public void setCrownKillHeight(double value) {
		crownKillHeight = value;
	}

	public void setCrownLengthScorched(double value) {
		crownLengthScorched = value;
	}

	public void setCrownLengthKilled(double value) {
		crownLengthKilled = value;
	}

	public void setMeanBoleLengthCharred(double value) {
		meanBoleLengthCharred = value;
	}

	public void setMinBoleLengthCharred(double value) {
		minBoleLengthCharred = value;
	}

	public void setMaxBoleLengthCharred(double value) {
		maxBoleLengthCharred = value;
	}
}
