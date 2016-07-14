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
import java.util.ArrayList;
import java.util.Collection;

import jeeb.lib.defaulttype.PaleoExtension;
import jeeb.lib.util.AmapTools;
import jeeb.lib.util.Log;
import capsis.app.CapsisExtensionManager;
import capsis.extensiontype.WorkingProcess;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.lib.economics.Producer;
import capsis.lib.economics.Product;

/**	WorkingProcess extensions superclass. 
* 
*	@author F. de Coligny - november 2007
*/
abstract public class PaleoWorkingProcess implements PaleoExtension, WorkingProcess,
Producer, Serializable {
	
	protected WorkingProcessStarter starter;
	
	private Collection<Product> products;	// the products that are made by this working process when executed

	
	/**	Phantom constructor. 
	*	Only to ask for extension properties (authorName, version...).
	*/
	public PaleoWorkingProcess () {}

	/**	Official constructor. It uses the standard Extension starter.
	*/
	public PaleoWorkingProcess (GenericExtensionStarter s) {
		this.starter = (WorkingProcessStarter) s;
	}

	/**	This is Extension dynamic compatibility mechanism.
	* 
	*	This matchwith method must be redefined for each extension subclass.
	*	It must check if the extension can deal (i.e. is compatible) with the referent.
	*	Here, referent must be compatible with the filter.
	*/
	public boolean matchWith (Object referent) {
		Log.println (Log.ERROR, "WorkingProcess.matchWith ()", 
				"This method was called because a subclass did not implement "+
				"public boolean matchWith (Object) method. "+
				"Subclass: "+this.getClass ().getName ()+
				". Referent was : "+referent);
		return false;
	}

	/**	Extension interface */
	public abstract String getName ();

	/**	Extension interface */
	public String getType () {
		return CapsisExtensionManager.WORKING_PROCESS;
	}

	/**	Extension interface */
	public String getClassName () {
		return this.getClass ().getName ();
	}

	/**	Extension interface.
	*	May be redefined by subclasses. Called after constructor
	*	at extension creation.
	*/
	public void activate () {}

	public WorkingProcessStarter getStarter () {return starter;}
	
	/**	WorkingProcess superclass 
	*	List of names of input products that this Working Process can possibly process
	*/
	public abstract Collection<String> getInputProductNames ();
	
	/**	Producer interface.
	*	List of names of output products that this Working Process can possibly create.
	*/
	public abstract Collection<String> getOutputProductNames ();
	
	/**	Process input product and create requested output products.
	*	Finally memorize the output products with addProduct ().
	*	Method execute () in subclasses must call super.execute () to make the checks below.
	*/
	public void execute () throws Exception {
//		String className = this.getClass ().getName ();
//		className = AmapTools.getClassSimpleName (className);
		String className = this.getClass ().getSimpleName ();
		if (starter.inputProduct == null) {
			throw new Exception (className+": missing input product: "
					+starter.inputProduct);
		}
		if (starter.requestedOutputProductNames == null
				|| starter.requestedOutputProductNames.isEmpty ()) {
			throw new Exception (className+": output request is null or empty: "
					+starter.requestedOutputProductNames);
		}
		for (String reqOut : starter.requestedOutputProductNames) {
			if (!getOutputProductNames ().contains (reqOut)) {
					throw new Exception (className+": can not output "+reqOut);}
		}
	}
	
	// Producer
	public void addProduct (Product p) {
		if (products == null) {products = new ArrayList<Product> ();}
		products.add (p);
	}
	public void removeProduct (Product p) {
		if (products == null) {return;}
		products.remove (p);
	}
	public Collection<Product> getProducts () {return products;}
	// Producer

}

