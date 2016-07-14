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

package capsis.kernel.extensiontype;

import java.util.Collection;

import jeeb.lib.defaulttype.Extension;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;

/**
 * Intervener Interface. 
 * 
 */
public interface Intervener extends Extension {
	
	public static final int CUT = 0;
	public static final int MARK = 1;
	
	public void init(GModel m, Step s, GScene scene, Collection c);
	public boolean initGUI() throws Exception;
	
	/**
	 * Tells if construction was ok.
	 * It could be wrong because of wrong parameters in console mode or
	 * cancel in gui mode.
	 */
	public boolean isReadyToApply ();

	/**
	 * Makes the actual intervention.
	 */
	public Object apply () throws Exception;



}

