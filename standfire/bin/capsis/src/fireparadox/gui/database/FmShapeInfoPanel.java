package fireparadox.gui.database;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import jeeb.lib.util.ColumnPanel;
import jeeb.lib.util.Translator;
import fireparadox.model.database.FmDBPlant;
import fireparadox.model.database.FmDBShape;

/** FiShapeInfoPanel : To display short shape information
 *
 * @author I. Lecomte - March 2008
 */
public class FmShapeInfoPanel extends JPanel  {

	private FmDBShape shape;
	private FmDBPlant plant;
	private long shapeId;
	private String shapeKind;
	private String fuelType;


	/**	Constructor.
	*/
	public FmShapeInfoPanel (FmDBShape _shape)  {

		super ();
		shape = _shape;

		if (shape != null) {
			shapeId = shape.getShapeId ();
			plant = shape.getPlant();

			if (shape.getFuelType() == 1) fuelType = "Plant";
			if (shape.getFuelType() == 2) fuelType = "Layer";
			if (shape.getFuelType() == 3) fuelType = "Sample unique";
			if (shape.getFuelType() == 4) fuelType = "Sample core";
			if (shape.getFuelType() == 5) fuelType = "Sample edge";

			shapeKind = shape.getShapeKind ();
		}

		createUI ();
		show ();
	}

	/**	Initialize the GUI.
	*/
	private void createUI () {

		this.setLayout(new FlowLayout (FlowLayout.LEFT)) ;

		// FUEL panel
		ColumnPanel fuelPanel = new ColumnPanel (Translator.swap ("FiShapeInfoPanel.title"));
		fuelPanel.add (new JLabel (Translator.swap ("FiShapeInfoPanel.shapeId")+" : "+shapeId));
		fuelPanel.add (new JLabel (Translator.swap ("FiShapeInfoPanel.type")+" : "+fuelType));
		fuelPanel.add (new JLabel (Translator.swap ("FiShapeInfoPanel.kind")+" : "+shapeKind));


		double xSize = shape.getVoxelXSize();
		double ySize = shape.getVoxelYSize();
		double zSize = shape.getVoxelZSize();

		fuelPanel.add (new JLabel (Translator.swap ("FiShapeInfoPanel.xSize")+" : "+xSize));
		fuelPanel.add (new JLabel (Translator.swap ("FiShapeInfoPanel.ySize")+" : "+ySize));
		fuelPanel.add (new JLabel (Translator.swap ("FiShapeInfoPanel.zSize")+" : "+zSize));




		if (shape.getFuelType() == 2) {
			double widthMax = shape.getLayerWidthMax();
			double widthMin = shape.getLayerWidthMin();
			fuelPanel.add (new JLabel (Translator.swap ("FiShapeInfoPanel.widthMin")+" : "+widthMin));
			fuelPanel.add (new JLabel (Translator.swap ("FiShapeInfoPanel.widthMax")+" : "+widthMax));

		}

		if (shape.isDeleted())
			fuelPanel.add (new JLabel (Translator.swap ("FiShapeInfoPanel.deleted")));



		this.add (fuelPanel);
	}

}

