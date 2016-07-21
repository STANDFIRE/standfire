/* 
 * The Standfire model.
 *
 * Copyright (C) September 2013: F. Pimont (INRA URFM).
 * 
 * This file is part of the Standfire model and is NOT free software.
 * It is the property of its authors and must not be copied without their 
 * permission. 
 * It can be shared by the modellers of the Capsis co-development community 
 * in agreement with the Capsis charter (http://capsis.cirad.fr/capsis/charter).
 * See the license.txt file in the Capsis installation directory 
 * for further information about licenses in Capsis.
 */

package standfire.model;

import java.util.HashMap;
import java.util.Map;

import capsis.kernel.GModel;
import capsis.kernel.Step;
import capsis.kernel.automation.AfterAutomation;
import capsis.kernel.automation.AutomationSummary;
import capsis.kernel.automation.VarColumn;
import capsis.util.Description;


/**	The Summary class of Standfire.
 *	This is an easy way to have a report on the scene linked to
 *	a given Step.
 *	Each function is used to compute an output variable. 
 *	Each function should be static and take 2 parameters: model and step.
 *    
 *	Use @VarColumn to return several columns in a map, otherwise the
 *	functions must return a single object.
 * 
 *	@author F. Pimont - September 2013
 */
public class SFSummary  {
	
	public static int nbTree(GModel mod, Step s) {
		
		SFScene stand = (SFScene) s.getScene(); 
		return stand.getTrees().size();
		
	}
	
	@Description(val="a Function")
	public static float aFunction(GModel mod, Step s) {
		 
		return 12.2f;
		
	}

	@Description(val="Test")
	@VarColumn
	public static Map<String, Object> testVarColumn(GModel mod, Step s) {
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("c1", 3);
		ret.put("c2", 4);
		
		return ret;
	}
	
	@AfterAutomation
	public static String anOtherFunction(GModel mod, AutomationSummary memo) {
		
		return "test2";
		
	}


}
