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

package capsis.extensiontype;

import java.io.Serializable;

import jeeb.lib.defaulttype.PaleoExtension;
import jeeb.lib.util.Log;
import capsis.app.CapsisExtensionManager;
import capsis.extension.EconomicFunctionStarter;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.StepExtension;

/**
 *
 *
 * @author Ch. Orazio - january 2003
 */
public abstract class EconomicFunction implements StepExtension, PaleoExtension, Serializable {

	protected EconomicFunctionStarter starter;	// fc + op - 9.10.2007
	
	protected GScene stand;
	protected GModel model;
	public static final String separator = ";";

	
	
	@Override
	public void init(GModel m, Step s) {
		
		// fc-11.4.2011 - was missing ?
		this.stand = s.getScene ();
		this.model = m;
		
	}
	
	/**
	 * This is Extension dynamic compatibility mechanism.
	 *
	 * This matchwith method must be redefined for each extension subclass.
	 * It must check if the extension can deal (i.e. is compatible) with the referent.
	 * Here, referent must be at least a GModel instance.
	 */
	public boolean matchWith (Object referent) {
		Log.println (Log.ERROR, "EconomicFunction.matchWith ()",
				"This method was called because a subclass did not implement "+
				"public boolean matchWith (Object) method. "+
				"Subclass: "+this.getClass ().getName ()+
				". Referent was : "+referent);
		return false;
	}

	/**	Optional initialization processing. Called after constructor.
	*/
	public void activate () {}

	/**	From Extension interface.
	*/
	public String getType () {
		return CapsisExtensionManager.ECONOMIC_FUNCTION;
	}

	/**	From Extension interface.
	*/
	public String getClassName () {
		return this.getClass ().getName ();
	}

	/**	Calculate and return the function result.
	*/
	public abstract double getResult ();

	/**	Return a String description of the function parameters (Translated).
	*	Ex: "Cout à l'hectare, cout = 12.5"
	*/
	public abstract String getFunctionParameters ();

	/**	Return a String listing the parameters (Translated) that need the function using public separator
	*	Ex: "limite;prix1;prix2;"
	*	the same string must be used doing a file of parameters!
	*/
	public abstract String getParametersList ();

	// fc + op - september 2007
	
	/**	For EconomicFunctions that are instances of UnitEconomicFunction, 
	*	if unit = "ha" or "m3" or "unit", 
	*	the stand field must NOT be null and we calculate a coefficient 
	*	related to 
	*	(1) the area for "ha"
	*	(2) the volume for "m3"
	*	(maybe later) (3) the commercial density for "unit"
	* 	This coefficient must be used in getResult ().
	*/
	//~ public double getUnitCoefficient () {
		//~ if (!(this instanceof UnitEconomicFunction)) {return 1;}
		//~ UnitEconomicFunction f = (UnitEconomicFunction) this;
		//~ String unit = f.getUnit ();
		//~ if (unit == null) {
			//~ Log.println (Log.ERROR, "EconomicFunction.getUnitCoefficient ()", 
					//~ "EconomicFunction is instanceof UnitEconomicFunction and unit == null, returned 0: "+this);
			//~ return 0;
		//~ }
		//~ if (stand == null) {
			//~ Log.println (Log.ERROR, "EconomicFunction.getUnitCoefficient ()", 
					//~ "EconomicFunction is instanceof UnitEconomicFunction and stand == null, "
					//~ +"should be at least root stand, returned 0: "+this);
			//~ return 0;
			
		//~ }
		//~ if (unit.equals (UnitEconomicFunction.HA)) {
			//~ double area = stand.getArea ();	// in m2
			//~ return area / 10000;
		//~ } else if (unit.equals (UnitEconomicFunction.M3)) {
			//~ return 1;
			
		//~ } else {
			//~ return 1;
		//~ }
	//~ }
	
	
	//~ public void setDateMin (int d) {dateMin = d;}
	//~ public void setDateMax (int d) {dateMax = d;}
	//~ public void setName (String v) {name = v;}
	//~ public void setDetail (String v) {detail = v;}
	
	public int getDateMin () {return starter.dateMin;}
	public int getDateMax () {return starter.dateMax;}
	public String getName () {return starter.name;}
	public String getDetail () {return starter.detail;}
	
	public String toString () {
//		return AmapTools.getClassSimpleName (this.getClassName ())
		return getClass ().getSimpleName ()
				+" dateMin="+starter.dateMin
				+" dateMax="+starter.dateMax
				+" name="+starter.name
				+" detail="+starter.detail
				+" stand="+starter.getScene ()
				+" model="+starter.getModel ();
				//~ +" quantity="+f.getQuantity ()
				//~ +" utni="+f.getUnit ()
				//~ +" unitPrice="+f.getUnitPrice ();
	}
	// fc + op - september 2007
	
	
}

