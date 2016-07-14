package capsis.lib.fire.exporter.wfds;

public class WFDSParam {
	public String canopyFuelRepresentation;// should be one of the 4 next
											// keyword
	public static String RECTANGLE = "RECTANGLE";
	public static String CYLINDER = "CYLINDER";
	public static String HET_RECTANGLE_TEXT = "HET_RECTANGLE_TEXT";
	public double bulkDensityAccuracy = 0.001; // kg/m3 :accuracy for the text
												// mode for WFDS
												// "element model format" for
												// HET_RECTANGLE_TEXT
	public static String HET_RECTANGLE_BIN = "HET_RECTANGLE_BIN";

	// public static String HET_RECTANGLE = "HET_RECTANGLE";
	public String format = "86";
	public String firstGridFile;
	public int gridNumber = 1;
	public String outDir;
	public String fileName = "wfds";
	public double vegetation_cdrag = 0.05;
	public double vegetation_char_fraction = 0.2;
	public double emissivity = 0.99;
	public boolean vegetation_arrhenius_degrad = false;
	public double fireline_mlr_max = 0.05;
	public double veg_initial_temperature = 20.0;
	public double veg_char_fraction = 0.25;
	public double veg_drag_coefficient = 0.125;
	public double veg_burning_rate_max = 0.4;
	public double veg_dehydratation_rate_max = 0.4;
	public boolean veg_remove_charred = true;

}
