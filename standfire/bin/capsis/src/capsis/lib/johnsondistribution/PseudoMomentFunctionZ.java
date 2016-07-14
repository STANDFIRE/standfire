/* 
* The Johnson distribution library for Capsis4
* 
* Copyright (C) 2009 Bernard Parresol, Teresa Fonseca
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
package capsis.lib.johnsondistribution;

import flanagan.integration.IntegralFunction;

/**	This function will be used for the evaluation of the Ipsilon pseudomoments.
*	
*	@author Teresa Fonseca - june 2009
*/
public class PseudoMomentFunctionZ implements IntegralFunction {
	private double dmed;
	private double csi;
	private double initialLambda;
	private double initialDelta;
	private int order;
	private double gamma;
	
	
	/**	This function will be used for the evaluation of the Ipsilon moments.
	*/
	public PseudoMomentFunctionZ (double dmed, double csi, double initialLambda, double initialDelta, int order) {
		this.dmed = dmed;
		this.csi = csi;
		this.initialLambda = initialLambda;
		this.initialDelta = initialDelta;
		this.order = order;
		
		this.gamma = initialDelta * Math.log (initialLambda / (dmed - csi) - 1);
		
	}
	
	/**	Returns f(z)
	*/
	public double function (double z) {
		return Math.exp (-0.5 * z * z) / Math.pow ((1 + Math.exp ((gamma - z) / initialDelta)), order) * z;
	}
	
}



