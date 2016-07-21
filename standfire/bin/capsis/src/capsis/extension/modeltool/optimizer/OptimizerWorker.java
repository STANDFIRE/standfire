package capsis.extension.modeltool.optimizer;

import java.text.NumberFormat;
import java.util.Queue;
import java.util.Vector;

import repicea.util.MemoryWatchDog;
import capsis.extension.modeltool.scenariorecorder.ScenarioRecorder.InvalidDialogException;
import capsis.kernel.Step;


class OptimizerWorker extends Thread {

	static NumberFormat formatter = NumberFormat.getInstance();
	static {
		formatter.setMinimumFractionDigits(1);
		formatter.setMaximumFractionDigits(1);
	}
	
	final ObjectiveFunction of;
	final Queue queue;
	final int id;
	
	OptimizerWorker(ObjectiveFunction of, Queue queue, int id) {
		this.id = id;
		this.of = of;
		this.queue = queue;
		start();
	}



	@SuppressWarnings("unused")
	@Override
	public void run() {
		while (queue.poll() != null && !of.cancelRequested && !of.interrupted) {
			try {
				Step lastStep = of.scenRecorder.playScenario();
				Vector<Step> steps = lastStep.getProject().getStepsFromRoot(lastStep);
				of.computeIndicators(steps, of.objectiveFunctionValue);
				of.tool.originalStep.setLeftSon(null);
				System.gc();
				double mgAvailable = MemoryWatchDog.checkAvailableMemory();
//				System.out.println("Available memory " + formatter.format(mgAvailable) + " Mg");
			} catch (InvalidDialogException e) {
				of.interrupted = true;
			}
		}
	}
	

	
}
