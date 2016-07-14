/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2013-2014  Mathieu Fortin - UMR LERFoB (AgroParisTech/INRA)
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
package capsis.extension.modeltool.optimizer;


/**
 * The OptimizerEvent class is an Event type class that is fired by the OptimizerTool class.
 * @author Mathieu Fortin - July 2014
 */
public class OptimizerEvent {

	public static enum EventType {
		PARAMETER_ADDED, 
		PARAMETER_REMOVED,
		OPTIMIZATION_START,
		EVALUATING,
		SUBITERATING,
		OPTIMIZATION_END,
		OPTIMIZATION_CANCELLED,
		OPTIMIZATION_ABORTED,
		OPTIMIZATION_INVALIDPARAMETERS,
		GRID_START,
		GRID_CANCELLED,
		GRID_END,
		BEST_SCENARIO_PLAYED,
		JUST_LOADED}
	
	private final EventType type;
	private final String message;
	
	protected OptimizerEvent(EventType type, String message) {
		this.type = type;
		this.message = message;
	}
	
	public EventType getType() {return type;}
	
	public String getMessage() {return message;}

}
