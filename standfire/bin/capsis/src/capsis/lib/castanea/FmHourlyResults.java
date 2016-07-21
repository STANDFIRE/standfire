package capsis.lib.castanea;

import java.io.Serializable;
import java.util.Collection;
import jeeb.lib.util.Log;
import java.util.Collection;

/**
 * FmHourlyResults : to store hourly variables
 *
 * @author Hendrik Davi - January 2014
 */
// not used to be changed

public class FmHourlyResults implements Serializable, Cloneable {

	private double[] canopyPhotosynthesis;
	private double[] canopyDelta13C;
	private double[] canopyRespiration;
	private double[] canopyConductance;

	private double[] canopyTranspiration;
	private double[] canopyEvapoTranspiration;
	private double[] canopyPotentialEvaporation;
	private double[] woodRespiration;
	private double[] fineRootsRespiration;
	private double[] coarseRootsRespiration;

	private double soilEvaporation;

	/**
	 * Constructor for new logical FmCanopy.
	 */

	public FmHourlyResults(FmCell cell, FmSettings settings) {

		FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
		int nSpecies = fmSpeciesList.length;

		canopyPhotosynthesis = new double[nSpecies];
		canopyDelta13C = new double[nSpecies];
		canopyRespiration = new double[nSpecies];
		canopyConductance = new double[nSpecies];
		canopyTranspiration = new double[nSpecies];
		canopyEvapoTranspiration = new double[nSpecies];
		canopyPotentialEvaporation = new double[nSpecies];
		woodRespiration = new double[nSpecies];
		fineRootsRespiration = new double[nSpecies];
		coarseRootsRespiration = new double[nSpecies];

		soilEvaporation = 0;


	}

	public void setCanopyPhotosynthesis(double[] v) {
		canopyPhotosynthesis = v;
	}

	public void setCanopyRespiration(double[] v) {
		canopyRespiration = v;
	}

	public void setCanopyConductance(double[] v) {
		canopyConductance = v;
	}

	public void setCanopyDelta13C(double[] v) {
		canopyDelta13C = v;
	}

	public void setCanopyTranspiration(double[] v) {
		canopyTranspiration = v;
	}

	public void setCanopyEvapoTranspiration(double[] v) {
		canopyEvapoTranspiration = v;
	}

	public void setCanopyPotentialEvaporation(double[] v) {
		canopyPotentialEvaporation = v;
	}

	public void setFineRootsRespiration(int sp, double v) {
		fineRootsRespiration[sp] = v;
	}

	public void setWoodRespiration(int sp, double v) {
		woodRespiration[sp] = v;
	}

	public void setCoarseRootsRespiration(int sp, double v) {
		coarseRootsRespiration[sp] = v;
	}

	public void setSoilEvaporation(double v) {
		soilEvaporation = v;
	}

	public double getCanopyPhotosynthesisSp(int sp) {
		return canopyPhotosynthesis[sp];
	}

	public double getCanopyDelta13CSp(int sp) {
		return canopyDelta13C[sp];
	}

	public double getCanopyRespirationsp(int sp) {
		return canopyRespiration[sp];
	}

	public double getCanopyConductanceSp(int sp) {
		return canopyConductance[sp];
	}

	public double[] getCanopyPhotosynthesis() {
		return canopyPhotosynthesis;
	}

	public double[] getCanopyDelta13C() {
		return canopyDelta13C;
	}

	public double[] getCanopyRespiration() {
		return canopyRespiration;
	}

	public double[] getCanopyConductance() {
		return canopyConductance;
	}

	public double[] getCanopyTranspiration() {
		return canopyTranspiration;
	}

	public double[] getCanopyEvapoTranspiration() {
		return canopyEvapoTranspiration;
	}

	public double[] getCanopyPotentialEvaporation() {
		return canopyPotentialEvaporation;
	}

	public double getCanopyTranspirationSp(int sp) {
		return canopyTranspiration[sp];
	}

	public double getCanopyEvapoTranspirationSp(int sp) {
		return canopyEvapoTranspiration[sp];
	}

	public double getCanopyPotentialEvaporationSp(int sp) {
		return canopyPotentialEvaporation[sp];
	}

	public double getWoodRespiration(int sp) {
		return woodRespiration[sp];
	}

	public double getFineRootsRespiration(int sp) {
		return fineRootsRespiration[sp];
	}

	public double getcoarseRootsRespiration(int sp) {
		return coarseRootsRespiration[sp];
	}

	public double getSoilEvaporation() {
		return soilEvaporation;
	}

}
