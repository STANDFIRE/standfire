/*
 * Nelder optimization library for Capsis4.
 *
 * Copyright (C) 2004 Alexandre Piboule.
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

package capsis.lib.nelderoptimization;

public class NelderFunctionExample implements NelderFunctionProvider {

	public double getFunctionToMinimizeValue (double[] param) {
		if (param.length==3) {
			return -3803.84d - 138.08d*param[1] - 232.92d*param[2] +123.08d*param[1]*param[1] +203.64d*param[2]*param[2] + 182.25d*param[1]*param[2];
		} else {return Double.NaN;}

	}

}