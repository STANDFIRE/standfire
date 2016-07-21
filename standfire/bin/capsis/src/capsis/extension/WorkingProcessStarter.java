/** 
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 1999-2010 INRA 
 * 
 * Authors: F. de Coligny, S. Dufour-Kowalski, 
 * 
 * This file is part of Capsis
 * Capsis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Capsis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU lesser General Public License
 * along with Capsis.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package capsis.extension;

import java.io.Serializable;
import java.util.Collection;

import jeeb.lib.util.AmapTools;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.lib.economics.Product;

/**	Superclass for all Working Process starters
* 
*	@author F. de Coligny + O. Pain - november 2007
*/
public class WorkingProcessStarter extends GenericExtensionStarter implements Serializable {

	public String priceUnit;			// typology in Product
	public double price;
	public String fuelUnit;
	public double fuel;
	
	public String requestedInputProductName;				// from MacroWP
	public Collection<String> requestedOutputProductNames;	// from MacroWP
	
	public Product inputProduct;	// set when the working process is applied
	
	
	public WorkingProcessStarter (String priceUnit, 
			double price, 
			String fuelUnit, 
			double fuel, 
			String requestedInputProductName, 
			Collection<String> requestedOutputProductNames 
			) {
		this.priceUnit = priceUnit;
		this.price = price;
		this.fuelUnit = fuelUnit;
		this.fuel = fuel;
		this.requestedInputProductName = requestedInputProductName;
		this.requestedOutputProductNames = requestedOutputProductNames;
	}

	public void setInputProduct (Product p) {inputProduct = p;}
	
	
	//~ in Extensionstarter superclass
	//~ public GStand getStand () {return stand;}
	//~ public GModel getModel () {return model;}
	
	public String toString () {
//		return AmapTools.getClassSimpleName (getClass ().getName ())
		return getClass ().getSimpleName ()
				+" requestedInputProductName="+requestedInputProductName
				+" requestedOutputProductNames="+requestedOutputProductNames
				+" inputProduct="+inputProduct;
	}
}
