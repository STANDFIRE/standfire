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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import capsis.extension.modeltool.optimizer.OptimizerEvent.EventType;
import capsis.extension.modeltool.scenariorecorder.ScenarioRecorderEvent;


class OptimizerLogFileWriter implements OptimizerListener {

	private File logFile;
	private boolean writing;
	private FileOutputStream fos;
	private final OptimizerTool optimizer;
	
	protected OptimizerLogFileWriter(OptimizerTool optimizer) {
		this.optimizer = optimizer;
		this.optimizer.addOptimizerListener(this);
	}
	
	@Override
	public void scenarioRecorderJustDidThis (ScenarioRecorderEvent evt) {}

	@Override
	public void optimizerJustDidThis(OptimizerEvent event) {
		try {
			if (event.getType () == EventType.GRID_START || event.getType () == EventType.OPTIMIZATION_START) {
				if (!writing) {
					logFile = new File(optimizer.currentSettings.logPath + File.separator + "logOptimizer.txt");
					if (logFile.exists()) {
						logFile.delete();
					}
					logFile.createNewFile();
					fos = new FileOutputStream(logFile);
				}
				writing = true;
			}
			if (writing && event.getMessage() != null && !event.getMessage().isEmpty()) {
				String message = event.getMessage () + "\n";
				fos.write(message.getBytes(Charset.defaultCharset()));
			}
			if (event.getType() == EventType.GRID_CANCELLED || event.getType() == EventType.OPTIMIZATION_CANCELLED || event.getType() == EventType.OPTIMIZATION_END) {	// the EventType.GRID_END is not considered here otherwise the log file would be deleted
				if (writing) {
					fos.flush();
					fos.close();
				}
				writing = false;
			}
		} catch (IOException e) {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e1) {}
			}
		}
	}

}
