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

import java.awt.Color;
import java.util.Collection;

import jeeb.lib.defaulttype.Extension;
import capsis.kernel.GModel;
import capsis.kernel.Step;

public interface DataExtractor extends Extension {

	public void init(GModel m, Step s) throws Exception;
	
	/**
	 * Effectively process the extraction.
	 * Should only be called if needed (time consuming).
	 * One solution is to trigger real extraction by data renderer 
	 * paintComponent (). So, the work will only be done when needed.
	 * Return false if trouble.
	 */
	public boolean doExtraction () throws Exception; 
	// added throws Exception, fc-20.1.2014 trying to prevent an extractor from blocking Capsis
	
	
	public void setColor (Color forcedColor);
	
	public void setGrouperName (String grouperName);
	
	public String getDefaultDataRendererClassName ();
	
	public void setRank(int r);
	
	public boolean hasConfigProperty (String property);
		
	public String getComboProperty (String property);
	
	public Step getStep();

	// added throws Exception, fc-20.1.2014 trying to prevent an extractor from blocking Capsis
	boolean update(Step fromStep, Step toStep) throws Exception;

	public String getName();

	public Color getColor();

	public String getCaption();

	public Collection<String> getDocumentationKeys();

	
}
