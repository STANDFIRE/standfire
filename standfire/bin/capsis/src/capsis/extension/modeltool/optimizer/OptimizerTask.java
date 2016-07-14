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

import repicea.app.AbstractGenericTask;


class OptimizerTask extends AbstractGenericTask {

	protected static enum TaskID {
		ShowInterface,
		Optimize,
		GridSearch
	}
	
	private OptimizerTool caller;
	private TaskID taskID;
	
	OptimizerTask(TaskID taskID, OptimizerTool caller) {
		super();
		setName(taskID.toString());
		this.taskID = taskID;
		this.caller = caller;
	}
	
	@Override
	protected void doThisJob() throws Exception {
		switch (taskID) {
		case ShowInterface:
			caller.showUI();
			break;
		case Optimize:
			caller.optimize();
			break;
		case GridSearch:
			caller.gridSearch();
			break;
		}
	}
	
	@Override
	public void cancel() {
		if (taskID == TaskID.Optimize || taskID == TaskID.GridSearch) {
			caller.of.cancelRequested = true;
		} else {
			super.cancel();
		}
	}


}
