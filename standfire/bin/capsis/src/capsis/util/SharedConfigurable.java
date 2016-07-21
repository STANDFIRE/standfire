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



/**
 * Interface implemented by graphical configurable objects. 
 * Used to configure several objects with the same configuration panel 
 * (ex: several instances of same class).
 * Retrieve panel from the first object, then add other configurable
 * objects to the panel. 
 * Panel.execute () reconfigure everybody.
 *
 * @author F. de Coligny - september 2000
 */
public interface SharedConfigurable {

	public String getSharedConfLabel ();
	public ConfigurationPanel getSharedConfPanel (Object param);
	public void sharedConfigure (ConfigurationPanel p);
	public void postConfiguration ();

}

