package capsis.lib.castanea;

import java.io.Serializable;
import java.util.Collection;
import jeeb.lib.util.Log;
import java.util.Collection;

/**
 * FmCanopyLayer : a layer of leaves of Dynaclim model.
 *
 * @author Hendrik Davi - July 2010
 */
// class for stogking the daily results from increment hourly data

public class FmDailyResults implements Serializable, Cloneable {

	private double[] canopyPhotosynthesis; // here in ?molCo2
	private double[] canopyRespiration; // here in ?molCo2
	private double[] canopyConductance; // here in ?molCo2

	private double[] canopyTranspiration;
	private double[] maxHourlyTranspiration;
	private double[] canopyEvapoTranspiration;
	private double[] canopyPotentialEvaporation;
	private double[] woodRespiration;
	private double[] fineRootsRespiration;
	private double[] coarseRootsRespiration;
	private double[] maintenanceRespiration;
	private double[] growthRespiration;
	private double[] leafGrowthRespiration;
	private double[] deltasum13C;

	// public double [] leafGrowth;
	// public double [] woodGrowth;
	// public double [] coarseRootsGrowth;
	// public double [] fineRootsGrowth;
	// public double [] reservesGrowth;

	public double soilEvaporation;

	/**
	 * Constructor for new logical FmCanopy.
	 */

	public FmDailyResults(FmCell cell, FmSettings settings) {

		FmSpecies[] fmSpeciesList = cell.usedFmSpecies;
		FmSpecies species=fmSpeciesList[0];

		int nSpecies = fmSpeciesList.length;

		canopyPhotosynthesis = new double[nSpecies];
		deltasum13C = new double[nSpecies];
		canopyRespiration = new double[nSpecies];
		canopyConductance = new double[nSpecies];
		canopyTranspiration = new double[nSpecies];
		maxHourlyTranspiration= new double[nSpecies];
		canopyEvapoTranspiration = new double[nSpecies];
		canopyPotentialEvaporation = new double[nSpecies];
		woodRespiration = new double[nSpecies];
		fineRootsRespiration = new double[nSpecies];
		coarseRootsRespiration = new double[nSpecies];

		maintenanceRespiration = new double[nSpecies];
		growthRespiration = new double[nSpecies];
		leafGrowthRespiration = new double[nSpecies];



	}

	public void setCanopyPhotosynthesis(double[] v) {
		canopyPhotosynthesis = v;
	}

	public void setDeltasum13C(double[] v) {
		deltasum13C = v;
	}

	public void setCanopyRespiration(double[] v) {
		canopyRespiration = v;
	}

	public void setCanopyConductance(double[] v) {
		canopyConductance = v;
	}

	public void setCanopyTranspiration(double[] v) {
		canopyTranspiration = v;
	}
	public void setMaxHourlyTranspiration(double[] v) {
			maxHourlyTranspiration = v;
	}

	public void setCanopyEvapoTranspiration(double[] v) {
		canopyEvapoTranspiration = v;
	}

	public void setCanopyPotentialEvaporation(double[] v) {
		canopyPotentialEvaporation = v;
	}

	public void setMaintenanceRespiration(double[] v) {
		maintenanceRespiration = v;
	}

	public void setGrowthRespiration(double[] v) {
		growthRespiration = v;
	}

	public void setLeafGrowthRespiration(double[] v) {
		leafGrowthRespiration = v;
	}

	public void setFineRootsRespiration(double[] v) {
		fineRootsRespiration = v;
	}

	public void setWoodRespiration(double[] v) {
		woodRespiration = v;
	}

	public void setCoarseRootsRespiration(double[] v) {
		coarseRootsRespiration = v;
	}

	public void setSoilEvaporation(double v) {
		soilEvaporation = v;
	}

	// public void setDrainage (double v) {drainage=v;}

	public double getCanopyPhotosynthesisSp(int sp) {
		return canopyPhotosynthesis[sp];
	}

	public double getDeltasum13CSp(int sp) {
		return deltasum13C[sp];
	}

	public double getCanopyRespirationSp(int sp) {
		return canopyRespiration[sp];
	}

	public double getCanopyConductanceSp(int sp) {
		return canopyConductance[sp];
	}

	public double getMaintenanceRespirationSp(int sp) {
		return maintenanceRespiration[sp];
	}

	public double getGrowthRespirationSp(int sp) {
		return growthRespiration[sp];
	}

	public double getLeafGrowthRespirationSp(int sp) {
		return leafGrowthRespiration[sp];
	}

	public double[] getCanopyPhotosynthesis() {
		return canopyPhotosynthesis;
	}

	public double[] getDeltasum13C() {
		return deltasum13C;
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
	public double[] getMaxHourlyTranspiration() {
			return maxHourlyTranspiration;
	}

	public double[] getCanopyEvapoTranspiration() {
		return canopyEvapoTranspiration;
	}

	public double[] getCanopyPotentialEvaporation() {
		return canopyPotentialEvaporation;
	}

	public double[] getMaintenanceRespiration() {
		return maintenanceRespiration;
	}

	public double[] getGrowthRespiration() {
		return growthRespiration;
	}

	public double[] getLeafGrowthRespiration() {
		return leafGrowthRespiration;
	}

	public double[] getFineRootsRespiration() {
		return fineRootsRespiration;
	}

	public double[] getWoodRespiration() {
		return woodRespiration;
	}

	public double[] getCoarseRootsRespiration() {
		return coarseRootsRespiration;
	}

	public double getCanopyTranspirationSp(int sp) {
		return canopyTranspiration[sp];
	}
	public double getMaxHourlyTranspirationSp(int sp) {
		return maxHourlyTranspiration[sp];
	}
	public double getCanopyEvapoTranspirationSp(int sp) {
		return canopyEvapoTranspiration[sp];
	}

	public double getCanopyPotentialEvaporationSp(int sp) {
		return canopyPotentialEvaporation[sp];
	}

	public double getWoodRespirationSp(int sp) {
		return woodRespiration[sp];
	}

	public double getFineRootsRespirationSp(int sp) {
		return fineRootsRespiration[sp];
	}

	public double getCoarseRootsRespirationSp(int sp) {
		return coarseRootsRespiration[sp];
	}

	public double getSoilEvaporation() {
		return soilEvaporation;
	}

}
