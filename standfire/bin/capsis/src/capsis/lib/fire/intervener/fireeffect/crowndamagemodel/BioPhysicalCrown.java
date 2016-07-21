/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2003  Francois de Coligny
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package capsis.lib.fire.intervener.fireeffect.crowndamagemodel;

import java.util.Map;

import capsis.lib.fire.intervener.physicalfireeffect.FiretecVoxel;
import jeeb.lib.util.Translator;
import fireparadox.model.FmModel;


/**	BioPhysical : a crown damage model
*	@author F. Pimont - december 2009
*/
public class BioPhysicalCrown implements CrownDamageModel {

	private Map<Integer, FiretecVoxel> voxelMap; //treeCrownMap.get(plant.getId());
	private int nx;
	private int ny;
	private int nz;
	
	/**	Constructor.
	*/
	public BioPhysicalCrown() {
	}

	/**	This model needs specific extra parameters. Call this set
	*	method before getDamageHeight for each plant.
	*/
	public void set (Map<Integer, FiretecVoxel> voxelMap, int nx, int ny, int nz) {
		this.voxelMap = voxelMap;
		this.nx = nx;
		this.ny = ny;
		this.nz = nz;
	}

	/**	Returns the damage height in the crown (m). 
	*	The height is relative to the ground level.
	*/
	public double getDamageHeight(double thresholdTemperature, // threshold
																// temperature
																// for the
			// damage �C
			double fireIntensity, 		// kW/m
			double residenceTime,		// s
			double ambiantTemperature, // �C
			double windVelocity, 		//m/s
			String damageType) {		
		// - implement the model here -
		
		
		double damageHeight = 0d;
		double [][][] zheight = new double[nx][ny][nz];
		double meanDz = 15d;
		//mesh parameters
		double zb = 41d*meanDz;
		double aa1 = 0.1;
		double aa3 = (1.0-aa1); 
		// topo not initialized yet
		
		double _dz = 0;
		double _dzk = 0;
		double prev_dz = 0;
		// FP : implement real function (aa1, zb)
		for (int k = 0; k < nz; k++) {	// k: 0 -> nz-1
			int k1 = k+1;	// k1: 1 -> nz

			//_dz = ((double) (9*15*k1*k1*k1)) / ((double) (10*41*41)) + 15d/10d*k1;
			double kMeanDz = (k1)*meanDz;
			_dz = aa3*Math.pow(kMeanDz,3.0)/(zb*zb)+aa1*kMeanDz;
			_dzk = _dz - prev_dz;
			prev_dz = _dz;
			for (int i=0; i<nx;i++) {
				for (int j=0; j<ny; j++) {
					zheight[i][j][k] = _dz; //no topo yet *(zb-zs[i][j])/zb;
				}
			}
		}
		for (FiretecVoxel fc:voxelMap.values()) {
			boolean refBoolean;
			if (damageType == FmModel.CROWN_SCORCHED) {
				refBoolean = fc.leaveScorched;
			} else {
				refBoolean = fc.budKilled;
			}
			if (refBoolean) { // killed or scorched
				damageHeight = Math.max(damageHeight,zheight[fc.i][fc.j][fc.k]);
			}
		}
		return damageHeight;	
	}
	
	public String getName() {
		return Translator.swap("BioPhysicalCrown");
	}
	
	@Override
	public String toString () {return getName ();}
	
}












