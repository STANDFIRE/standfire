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

/**
 * The ScenarioRecorderListener interface ensures a particular instance can listen a ScenarioRecorderEvent that is fired by
 * the ScenarioRecorder class.
 * @author Mathieu Fortin - July 2014
 */
public interface ScenarioRecorderListener {

	public void scenarioRecorderJustDidThis(ScenarioRecorderEvent evt);
	
}
