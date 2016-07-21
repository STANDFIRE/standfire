package capsis.lib.samsaralight;

import java.io.Serializable;

import jeeb.lib.util.Log;
import capsis.defaulttype.SquareCell;

/**
 * A SamsaraLight class for creating virtual sensor that store radiation energy
 * within the scene canopy. It store also potential measured values of irradiance.
 * @author ligot.g
 * 19/02/2013
 *
 */
public class SLSensor implements Serializable{
	private int id;
	private double x;
	private double y;
	private double height; //m
	private double aboveCanopyHorizontalEnergy;
	private double directHorizontalEnergy; //MJ/m2
	private double diffuseHorizontalEnergy; //MJ/m2
	private double totalHorizonthalEnergy; //MJ/m2
	private double directSlopeEnergy; //MJ/m2 along the slope
	private double diffuseSlopeEnergy; //MJ/m2 along the slope
	private double totalSlopeEnergy; //MJ/m2 along the slope
	private double relativeHorizontalDiffuseEnergy; //%
	private double relativeHorizontalDirectEnergy; //%
	private double relativeHorizontalTotalEnergy; //%
	private double measuredRelativeDiffuseEnergy; //%
	private double measuredRelativeDirectEnergy; //%
	private double measuredRelativeTotalEnergy; //%
	private SquareCell belowCell;
	
	private boolean hasmeasuredRelativeTotalEnergy = false;
	private boolean hasmeasuredRelativeDiffusEnergy = false;
	private boolean hasmeasuredRelativeDirectEnergy = false;
	
	/**
	 * Constructor
	 * @param id
	 * @param x (m)
	 * @param y (m)
	 * @param height (m)
	 */
	public SLSensor(int id, double x, double y, double height){
		this.setId (id);
		this.x = x;
		this.y = y;
		this.height = height;
		resetEnergy();
	}
	
	public SLSensor getCopy(){
		SLSensor copiedSensor = new SLSensor(this.id,this.x,this.y,this.height);
		
		//gl - copy also last computation results (for regeneration purpose, regeneration is computed before process lighting) - 6-12-2013
		copiedSensor.setDirectHorizontalEnergy (this.directHorizontalEnergy);
		copiedSensor.setDiffuseHorizontalEnergy (this.diffuseHorizontalEnergy);
		copiedSensor.setTotalHorizontalEnergy (this.totalHorizonthalEnergy);
		copiedSensor.setDirectSlopeEnergy (this.directSlopeEnergy);
		copiedSensor.setDiffuseSlopeEnergy (this.diffuseSlopeEnergy);
		copiedSensor.setTotalSlopeEnergy (this.totalSlopeEnergy);
		copiedSensor.setRelativeHorizontalDiffuseEnergy (this.relativeHorizontalDiffuseEnergy);
		copiedSensor.setRelativeHorizontalDirectEnergy (this.relativeHorizontalDirectEnergy);
		copiedSensor.setRelativeHorizontalTotalEnergy (this.relativeHorizontalTotalEnergy);
		
		if(this.hasmeasuredRelativeDiffusEnergy) copiedSensor.setMeasuredRelativeDiffuseEnergy (this.measuredRelativeDiffuseEnergy);
		if(this.hasmeasuredRelativeTotalEnergy) copiedSensor.setMeasuredRelativetotalEnergy (this.measuredRelativeTotalEnergy);
		if(this.hasmeasuredRelativeDirectEnergy) copiedSensor.setMeasuredRelativeDirectEnergy (this.measuredRelativeDirectEnergy);
		
		return copiedSensor;
	}
	
	public void resetEnergy(){
		directHorizontalEnergy = 0;
		totalHorizonthalEnergy = 0;
		relativeHorizontalDiffuseEnergy = 0;
		relativeHorizontalDirectEnergy = 0;
		relativeHorizontalTotalEnergy = 0;
	}
	
	/**
	 * The z coordinates require to know over which cell is the sensor
	 * If this cell has not been specified, the method returns 0 with messages in the samsara Log
	 */
	public double getZ () {
		if(belowCell != null){
			return belowCell.getZCenter () + this.height;
		}else{
			System.out.println ("SLSensor.getZ() - z has not been specified yet. sensor.height is returned");
			Log.println ("SamsaraLight", "z has not been specified yet. sensor.height is returned");
			return this.height;
		}
		
	}
	/**
	 * The cell above which is the sensor
	 */
	public void setBelowCell(SquareCell cell){this.belowCell = cell;}
	public SquareCell getBelowCell(){return belowCell;}
	
	public double getDirectHorizontalEnergy () {return directHorizontalEnergy;}
	public void setDirectHorizontalEnergy (double directEnergy) {this.directHorizontalEnergy = directEnergy;}
	public double getDiffuseHorizontalEnergy () {return diffuseHorizontalEnergy;}
	public void setDiffuseHorizontalEnergy (double diffuseEnergy) {this.diffuseHorizontalEnergy = diffuseEnergy;}
	public double getTotalHorizontalEnergy () {return totalHorizonthalEnergy;}
	public void setTotalHorizontalEnergy (double totalEnergy) {this.totalHorizonthalEnergy = totalEnergy;}
	public double getRelativeHorizontalDiffuseEnergy () {return relativeHorizontalDiffuseEnergy;}
	public void setRelativeHorizontalDiffuseEnergy (double relativeDiffuseEnergy) {this.relativeHorizontalDiffuseEnergy = relativeDiffuseEnergy;}
	public double getRelativeHorizontalDirectEnergy () {return relativeHorizontalDirectEnergy;}
	public void setRelativeHorizontalDirectEnergy (double relativeDirectEnergy) {this.relativeHorizontalDirectEnergy = relativeDirectEnergy;}
	public double getRelativeHorizontalTotalEnergy () {return relativeHorizontalTotalEnergy;}
	public void setRelativeHorizontalTotalEnergy (double relativeTotalEnergy) {this.relativeHorizontalTotalEnergy = relativeTotalEnergy;}
	public double getMeasuredRelativeDiffuseEnergy () {return measuredRelativeDiffuseEnergy;}
	public void setMeasuredRelativeDiffuseEnergy (double measuredRelativeDiffuseEnergy) {
		this.measuredRelativeDiffuseEnergy = measuredRelativeDiffuseEnergy;
		this.hasmeasuredRelativeDiffusEnergy = true;
	}
	public double getMeasuredRelativeDirectEnergy () {return measuredRelativeDirectEnergy;}
	public void setMeasuredRelativeDirectEnergy (double measuredRelativeDirectEnergy) {
		this.measuredRelativeDirectEnergy = measuredRelativeDirectEnergy;
		this.hasmeasuredRelativeDirectEnergy = true;
	}
	public double getMeasuredRelativetotalEnergy () {return measuredRelativeTotalEnergy;}
	public void setMeasuredRelativetotalEnergy (double measuredRelativetotalEnergy) {
		this.measuredRelativeTotalEnergy = measuredRelativetotalEnergy;
		this.hasmeasuredRelativeTotalEnergy = true;
	}
	public double getDirectSlopeEnergy () {return directSlopeEnergy;}
	public void setDirectSlopeEnergy (double directSlopeEnergy) {this.directSlopeEnergy = directSlopeEnergy;}
	public double getDiffuseSlopeEnergy () {return diffuseSlopeEnergy;}
	public void setDiffuseSlopeEnergy (double diffuseSlopeEnergy) {this.diffuseSlopeEnergy = diffuseSlopeEnergy;}
	public double getTotalSlopeEnergy () {return totalSlopeEnergy;}
	public void setTotalSlopeEnergy (double totalSlopeEnergy) {this.totalSlopeEnergy = totalSlopeEnergy;}
	
	public double getX () {return x;}
	public double getY () {return y;}
	public double getHeight () {return height;}
	public void setHeight (double height) {this.height = height;}
	public int getId () {return id;}
	public void setId (int id) {this.id = id;}
	
	public boolean hasmeasuredRelativeTotalEnergy () {return this.hasmeasuredRelativeTotalEnergy;}
	public boolean hasmeasuredRelativeDiffusEnergy () {return this.hasmeasuredRelativeDiffusEnergy;}
	public boolean hasmeasuredRelativeDirectEnergy () {return this.hasmeasuredRelativeDirectEnergy;}

	public double getAboveCanopyHorizontalEnergy() {return aboveCanopyHorizontalEnergy;}
	public void setAboveCanopyHorizontalEnergy(double aboveCanopyHorizontalEnergy) {this.aboveCanopyHorizontalEnergy = aboveCanopyHorizontalEnergy;}


}
