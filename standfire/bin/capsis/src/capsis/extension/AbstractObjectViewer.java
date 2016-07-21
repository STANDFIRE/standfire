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

import java.awt.Dimension;
import java.util.Collection;
import java.util.Collections;

import jeeb.lib.defaulttype.ObjectViewer;

/**
 * ObjectViewer - Superclass of all sorts of viewers in a dialog box (ex : tree viewers).
 * 
 * @author F. de Coligny - august 2002
 */
abstract public class AbstractObjectViewer extends ObjectViewer {
	
	protected Collection realSelection;
	
	public void activate () {}

	// fc - 9.9.2008 - new OVSelector framework
	public Collection getRealSelection () {return realSelection;}		// fc - 9.9.2008 - OVSelector
	// fc - 9.9.2008 - new OVSelector framework
	public Collection show (Collection candidateSelection) {return Collections.EMPTY_LIST;}
	
	
}
