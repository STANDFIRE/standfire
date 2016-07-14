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

import jeeb.lib.util.Translator;
import fireparadox.model.FmModel;


/**	MichaletzAndJohnson : a crown damage model
*	@author F. Pimont - september 2009
*/
public class MichaletzAndJohnsonCrown implements CrownDamageModel {

	private double MVR;
	private double SVR;
	private double moistureContent;
	
	
	
	/**	Constructor.
	*/
	public MichaletzAndJohnsonCrown () {}

	/**	This model needs specific extra parameters. Call this set
	*	method before getDamageHeight for each plant.
	*/
	public void set (double MVR, 				// mass to volume ratio, kg/m3
			double SVR, 						// surface to volume ratio, m2/m3
			double moistureContent) { 			//%) {				
		this.MVR = MVR;
		this.SVR = SVR;
		this.moistureContent = moistureContent;
		
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
			double windVelocity,		// m/s
			String damageType) {
		// - implement the model here -
		double U = 5; // m/s velocity for Reynolds number
		double MC=this.moistureContent*0.01; //in fraction
		double D; // particle diameter in m; should be derived from real
		// particle properties
		double ADividedByFreshMass; // m2/kg (fresh sla)
		// the following formulas relies on two assumption: Vfrais=Vsec+Veau
		// (pas d'air dans le bois), la longueur des aiguilles seche et fraiche
		// est identique
		// if shape= cylinders
		D = Math.sqrt(1.0 + MC * MVR / 1000) * 4.0 / SVR;
		// if shape= hemicylinders
		D = Math.sqrt(1.0 + MC * MVR / 1000) * 6.55 / SVR;

		ADividedByFreshMass = SVR / MVR * Math.sqrt(1.0 + MC * MVR / 1000.0)
				/ (1.0 + MC); // SVR/MVR*Dfrais/Dsec/(1+MC)

		double Re = U * D / (1.5 * Math.pow(10.0, -5.0));
		double Nu = 0.05 * Math.pow(Re, 0.7); // other parameters are possible
		// cylinder 0.683,0.466
		// tube bank 0.3,0.578
		double h = 2.37 * Math.pow(10.0, -2.0) * Nu / D; // convective
															// coefficient
		double c; // specific heat capacity
		if (damageType.equals(FmModel.BUD_KILLED)) {// buds
			c = (1902.6 + 4180.0 * MC) / (1.0 + MC);
		} else { // scorch=foliage
			c = (1543 + 4180.0 * MC) / (1.0 + MC);
		}
		double necroseTemperature = 1.0 ;
		//Tools.getDamageTemperature(damageType,
		//		speciesName, season);
		
		
		// in �C for foliage, for buds varies between
		// 90 and 150 in M&J2006a
		// foliage
		/*
		 * necroseTemperature = 60; c = (1543 + 4180 * MC) / (1 + MC); // buds
		 * necroseTemperature = 90; c = (1902.6 + 4180 * MC) / (1 + MC);
		 */
		// critical temperature computation
		double theta = Math.exp(-h * ADividedByFreshMass * residenceTime / c);
		double criticalTemperature = (necroseTemperature - theta
				* ambiantTemperature)
				/ (1.0 - theta);
		// air properties for Michaletz&Johnson
		double rhoa = 376.41 * Math.pow(ambiantTemperature + 273.0, -1.01);
		double t = ambiantTemperature;
		double cp = 1.01;

		double k = 2.6 * Math.pow((ambiantTemperature + 273.0)
				/ (9.8 * cp * cp * rhoa * rhoa), 1.0 / 3.0);
		return k
		* Tools.plume(fireIntensity, ambiantTemperature, criticalTemperature);
	}
	
	public String getName() {
		return Translator.swap("Michaletz And Johnson 2006");
	}
	
	@Override
	public String toString () {return getName ();}
	
}












