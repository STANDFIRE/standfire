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

import jeeb.lib.defaulttype.PaleoExtension;
import jeeb.lib.util.Log;
import capsis.app.CapsisExtensionManager;
import capsis.kernel.extensiontype.GenericExtensionStarter;

/**
 * Superclass for all Old data extractors.
 *
 * @author F. de Coligny - November 2000
 */
abstract public class PaleoDataExtractor extends AbstractDataExtractor implements PaleoExtension {

	public PaleoDataExtractor () {}
	
	public PaleoDataExtractor (GenericExtensionStarter s) {
		try {
			init(s.getModel() , s.getStep ());
		} catch (Exception e) {
			Log.println (Log.ERROR, "PaleoDataExtractor.c ()", "Exception", e);
		}

	}

	/**	Returns the type of the PaleoExtension.	*/
	@Override
	public String getType () {
		return CapsisExtensionManager.DATA_EXTRACTOR;
	}

	/**	Returns the class name of the PaleoExtension: getClass ().getName ().	*/
	@Override
	public String getClassName () {
		return this.getClass().getName();
	}

	
}
