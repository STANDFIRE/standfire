/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2014  Francois de Coligny
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package capsis.extension.generictool.memoryview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;

import jeeb.lib.util.AmapDialog;
import jeeb.lib.util.Translator;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Second;
import org.jfree.data.xy.XYDataset;

/**
 * MemoryMonitor to check the memory usage.
 * 
 * @author F. de Coligny - October 2014
 * @see http://stackoverflow.com/questions/5048852
 */
public class MemoryMonitor extends AmapDialog implements ActionListener {

	static {
		Translator.addBundle("capsis.extension.generictool.memoryview.MemoryMonitor");
	}

	private DynamicTimeSeriesCollection dataset;

	private JButton pauseResume;
	private JButton cleanMemory;

	private static final String TITLE = ""; // no title (there is one in the
											// dialog title bar)
	private static final String RESUME = Translator.swap("MemoryMonitor.resume");
	private static final String PAUSE = Translator.swap("MemoryMonitor.pause");
	private static final float MIN = 0;
	private static float MAX;
	private static final int COUNT = 2 * 60; // 2 mn
	private static final int REFRESH = 250; // 1 s
//	private static final int REFRESH = 1000; // 1 s
	private Timer timer;

	/**
	 * Constructor
	 */
	public MemoryMonitor(Window window) {
		super(window);

		setTitle(Translator.swap("MemoryMonitor"));

		long[] maxTotalFreeUsed = MemoryMonitor.memoryMaxTotalFreeUsedValues_Mb();
		MAX = maxTotalFreeUsed[0]; // max memory available

		dataset = new DynamicTimeSeriesCollection(3, COUNT, new Second());
		dataset.setTimeBase(new Second(0, 0, 0, 1, 1, 2011));
		dataset.addSeries(initData(0), 0, Translator.swap("MemoryMonitor.usedMemory"));
		dataset.addSeries(initData(0), 1, Translator.swap("MemoryMonitor.allocatedMemory"));
		dataset.addSeries(initData(MAX), 2, Translator.swap("MemoryMonitor.maxMemoryAvailable"));
		JFreeChart chart = createChart(dataset);

		pauseResume = new JButton(PAUSE);
		pauseResume.addActionListener(this);

		cleanMemory = new JButton(Translator.swap("MemoryMonitor.cleanMemory"));
		cleanMemory.addActionListener(this);

		this.add(new ChartPanel(chart), BorderLayout.CENTER);
		JPanel l0 = new JPanel(new FlowLayout());
		l0.add(pauseResume);
		l0.add(cleanMemory);
		this.add(l0, BorderLayout.SOUTH);

		timer = new Timer(REFRESH, this);

		setSize(new Dimension(500, 350));
		setVisible(true);
		timer.start();

	}

	@Override
	public void escapePressed() {
		super.escapePressed();
		if (timer != null)
			timer.stop();
		// System.out.println("Closing MemoryMonitor -> stopped the timer"); //
		// checked
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource().equals(pauseResume)) {
			String cmd = e.getActionCommand();
			if (PAUSE.equals(cmd)) {
				timer.stop();
				pauseResume.setText(RESUME);
			} else {
				timer.start();
				pauseResume.setText(PAUSE);
			}

		} else if (e.getSource().equals(cleanMemory)) {
			System.gc();
			updateChart();

		} else if (e.getSource().equals(timer)) {
			updateChart();

		}
	}

	private void updateChart() {
		float[] newData = new float[3];

		long[] maxTotalFreeUsed = MemoryMonitor.memoryMaxTotalFreeUsedValues_Mb();

		// used: used memory in the JV
		// free: free memory in the JVM
		// total: total memory in the JVM, used + free
		// max: -Xmx, max amount the JVM can have from the OS

		long max = maxTotalFreeUsed[0];
		long total = maxTotalFreeUsed[1];
		long free = maxTotalFreeUsed[2];
		long used = maxTotalFreeUsed[3];

		newData[0] = used;
		newData[1] = total;
		newData[2] = max;
		dataset.advanceTime();
		dataset.appendData(newData);
	}

	/**
	 * Create the chart.
	 */
	private JFreeChart createChart(final XYDataset dataset) {
		final JFreeChart result = ChartFactory.createTimeSeriesChart(TITLE, Translator.swap("MemoryMonitor.time"),
				Translator.swap("MemoryMonitor.memoryInMb"), dataset, true, true, false);

		// Set background
		result.setBackgroundPaint(Color.WHITE);

		final XYPlot plot = result.getXYPlot();
		ValueAxis domain = plot.getDomainAxis();
		domain.setAutoRange(true);
		ValueAxis range = plot.getRangeAxis();
		range.setRange(MIN, MAX + MAX * 0.1f);

		// Line colors
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
		renderer.setSeriesPaint(0, Color.BLUE); // set series colors
		renderer.setSeriesPaint(1, Color.GREEN.darker()); // set series colors
		renderer.setSeriesPaint(2, Color.RED); // set series colors

		return result;
	}

	/**
	 * Populate the chart at opening time with these data.
	 */
	private float[] initData(float value) {
		float[] a = new float[COUNT];
		for (int i = 0; i < a.length; i++) {
			a[i] = value;
		}
		return a;
	}

	/**
	 * Return 4 memory values in an array: max, total, free, used.
	 * 
	 * used: used memory in the JVM ; free: free memory in the JVM ; total:
	 * total memory in the JVM (= used + free) ; max: -Xmx, max amount the JVM
	 * can have from the OS.
	 */
	public static long[] memoryMaxTotalFreeUsedValues_Mb() {

		Runtime rt = Runtime.getRuntime();

		long max = rt.maxMemory() / 1000000; // bytes -> Mb
		long total = rt.totalMemory() / 1000000;
		long free = rt.freeMemory() / 1000000;
		long used = total - free;

		return new long[] { max, total, free, used };
	}

}