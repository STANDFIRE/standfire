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

package capsis.kernel;

/**	This interface describes the initial parameters of a model in
 * 	the capsis.kernel.
 * 	It must be able to create an initial scene for a Project. 
 * 	E.g. it can be done by reading an inventory file.
 *
 * @author F. de Coligny - september 2001, september 2010
 */
public interface InitialParameters {
	
	/**	Returns an instance of GScene to be linked to the root Step
	 * 	of a Project.
	 */
	public void buildInitScene(GModel model) throws Exception;
	
	/**	An accessor on the scene that was built by buildInitScene ().
	 */
	public GScene getInitScene ();
	
}