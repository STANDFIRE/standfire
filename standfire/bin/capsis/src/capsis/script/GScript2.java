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

package capsis.script;

import jeeb.lib.util.Log;
import capsis.extension.memorizer.DefaultMemorizer;
import capsis.kernel.GModel;
import capsis.kernel.InitialParameters;
import capsis.kernel.extensiontype.Memorizer;


/**	GScript2 is an abstract superclass for scripts in Java.
 * 	This class must be subclassed, see capsis.app.C4Script.
 * 	See the script doc on the Capsis web site documentation pages.
 *  
 *	@author F. de Coligny - september 2010
 */
public abstract class GScript2 extends GScript {
	
	/**	Default constructor, needed for specific scripts subclassing  
	 * 	GScript2 and implementing a main method to do simple tasks (M. Fortin).
	 * 	E.g. load a project and run some exporter.
	 */
	public GScript2 () {}
	
	/** Constructor 1
	 */
	public GScript2 (String modelName) {
		model = loadModel (modelName);
	}
	
	/** Constructor 2
	 */
	public GScript2 (GModel m) {
		model = m;
	}
	
	/** Constructor 3
	 */
	public GScript2 (String modelName, InitialParameters param) throws Exception {
		model = loadModel (modelName);
		init(param);
	}
	
	/** Init 
	 */
	public void init (InitialParameters param) throws Exception {
		init (param, new DefaultMemorizer ());
	}
	
	/** Init 
	 */
	public void init (InitialParameters param, Memorizer memorizer) throws Exception {
		resetCurrentStep ();
		
		param.buildInitScene (model);
		
		String projectName = this.getClass ().getName ();
		project = createProject (projectName, model, param);
		
		try {
			setMemorizer (project, memorizer);
			
		} catch (Exception e) {
			Log.println (Log.ERROR, "GScript2.init ()", "Memorizer error", e);
			throw new Exception ("Memorizer error", e);
		}
		
	}

	/**	Unused in GScript2.
	 * 	Legacy scripts used to extend directly GScript and were built
	 * 	with a constructor and a run () method.
	 * 	More recent scripts extend a subclass of GScript2 and do not 
	 * 	use the run () method any more (see capsis.app.C4Script).
	 */
	public void run () throws Exception {}

		
}
