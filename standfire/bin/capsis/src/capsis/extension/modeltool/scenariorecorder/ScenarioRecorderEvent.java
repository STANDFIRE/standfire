/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2014 Mathieu Fortin (AgroParisTech/INRA - UMR LERFoB)
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
package capsis.extension.modeltool.scenariorecorder;

import java.util.List;

/**
 * The ScenarioRecorderEvent class is a Event type class that is fired by the ScenarioRecorder class.
 * @author Mathieu Fortin - July 2014
 */
public class ScenarioRecorderEvent {

	public static enum EventType {
		PLAY_CALLED,
		WRAPPER_ADDED, 
		PLAY_TERMINATED, 
		ABOUT_TO_LOAD,
		JUST_LOADED,
		WRAPPER_APPROVED}
	
	private final EventType type;
	private final List<ParameterDialogWrapper> wrappers;
	
	protected ScenarioRecorderEvent(EventType type, List<ParameterDialogWrapper> wrappers) {
		this.type = type;
		this.wrappers = wrappers;
	}
	
	public EventType getType() {return type;}

	public List<ParameterDialogWrapper> getWrappers() {return wrappers;}
}
