package capsis.extension.treelogger.pp3treelogger;

import repicea.simulation.treelogger.TreeLogCategory;
import repicea.simulation.treelogger.TreeLogCategoryPanel;
import repicea.simulation.treelogger.WoodPiece;

public class Pp3LoggingTreeLogCategory extends TreeLogCategory {

	public static final double STUMP_HEIGHT = 0.65;
	public static final double TOP_GIRTH = 20;
	public static final double TOP1_GIRTH = 150;
	public static final double LOG1_LENGTH = 2.6;
	public static final double LOG2_LENGTH = 2.1;
	public static final boolean EXPORT_ASKED = true;
	public static final String EXPORT_FILE_NAME = "pp3logging.csv";

	public Pp3LoggingTreeLogCategory(String name) {
		super(name);
	}
	
	@Override
	public TreeLogCategoryPanel getUI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getYieldFromThisPiece(WoodPiece piece) throws Exception {
		// TODO Auto-generated method stub
		return 1d;
	}

}
