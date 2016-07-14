/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2001  Francois de Coligny
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package capsis.extension.modeltool.castaneaclimateviewer;

//import nz1.model.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import capsis.lib.castanea.FmClimate;


/**	A factory for graphs
*	@author H. Davi, F. de Coligny - march 2009
*/
public class GraphFactory  {

	static private Map<String,Graph> map;			// Graph name -> Graph
	
	// step : Graph.YEAR, Graph.MONTH, Graph.DAY, Graph.HOUR
	// dateMin / dateMax: yyyy ddd, ex: 1995 123
	static public Graph getGraph (String graphName, String variableName, String step, int yyyyMin, int dddMin, int yyyyMax, int dddMax, 
			double latitude, double longitude) throws Exception {
		if (map == null) {return null;}

		Graph g = map.get (graphName);
		g.set (variableName, step, yyyyMin, dddMin, yyyyMax, dddMax, latitude, longitude);
		
		return g;
	}

	static public void initMap (FmClimate climate) {
		map = new LinkedHashMap<String,Graph> ();
		
		Graph g = new EvoGraph (climate);
		map.put (g.getName (), g);
		g = new CineGraph (climate);
		map.put (g.getName (), g);
		
	}
	
	static public Collection<String> getGraphNames () {
		return map.keySet ();
	}
	

}


