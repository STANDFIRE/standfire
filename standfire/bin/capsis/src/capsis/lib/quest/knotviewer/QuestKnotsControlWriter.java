package capsis.lib.quest.knotviewer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Date;
import java.util.List;

import capsis.defaulttype.Tree;
import jeeb.lib.util.Log;

/**
 * Writes the given knots properties in a control table.
 * 
 * @author Emmanuel Duchateau, F. de Coligny - March 2015
 */
public class QuestKnotsControlWriter {

	private StringBuffer buffer;
	private Tree tree;

	/**
	 * Constructor.
	 */
	public QuestKnotsControlWriter(Tree tree, List<QuestGU> gus) {
		this.tree = tree;

		buffer = new StringBuffer();

		buffer.append("# Quest Knots Data file,  " + new Date() + "\n");
		buffer.append("\n");
		buffer.append("#treeId guId guZ0_m guLength_m guN guTreeDbh_cm guTreeHeight_m knotId knotZr knotZ0_mm knotAzimut ringNumber x_mm y_mm z_mm diameter_mm alive\n");

		appendKnots(buffer, gus);

	}

	private void appendKnots(StringBuffer b, List<QuestGU> gus) {

		for (QuestGU gu : gus) {

			for (QuestKnot k : gu.getKnots()) {

				for (QuestKnotDiameter d : k.getDiameters()) {
					b.append(tree.getId());
					b.append(" ");
					
					b.append(gu.id);
					b.append(" ");
					b.append(gu.z0_m);
					b.append(" ");
					b.append(gu.length_m);
					b.append(" ");
					b.append(gu.n);
					b.append(" ");
					b.append(gu.treeDbh_cm);
					b.append(" ");
					b.append(gu.treeHeight_m);
					b.append(" ");

					b.append(k.id);
					b.append(" ");
					b.append(k.zr);
					b.append(" ");
					b.append(k.z0_mm);
					b.append(" ");
					b.append(k.azimut);
					b.append(" ");

					b.append(d.ringNumber);
					b.append(" ");
					b.append(d.x);
					b.append(" ");
					b.append(d.y);
					b.append(" ");
					b.append(d.z);
					b.append(" ");
					b.append(d.diameter);
					b.append(" ");
					b.append(d.alive);
					b.append("\n");
				}
			}
		}

	}

	public void save(String fileName) throws Exception {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));

			out.write(buffer.toString());
			out.newLine();

			out.close();

		} catch (Exception e) {
			Log.println(Log.ERROR, "QuestKnotsControlWriter.save ()", "Could not write in file: " + fileName, e);
			throw e;
		}
	}

}
