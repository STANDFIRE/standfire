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

package capsis.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**	ConfigPanel / ConfigPanelable for light configuration framework
*	A ConfigPanelable subject can return a ConfigPanel for its configuration.
*
*	@author F. de Coligny - march 2006
*/
public interface ConfigPanelable extends ActionListener {

	/**	Return a name (translated) - fc - 15.4.2006
	*/
	abstract public String getName ();

	/**	Get the config panel for the subject
	*/
	public ConfigPanel getConfigPanel ();

	/**	Called by config panel when config changed
	*/
	public void actionPerformed (ActionEvent e);
}

