package capsis.lib.quest.knotviewer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

import jeeb.lib.util.Log;
import capsis.defaulttype.Tree;

/**
 * Writes the given knots in Bil3D format (F. Mothe).
 * 
 * @author Emmanuel Duchateau, F. de Coligny - March 2015
 */
public class QuestKnotsWriter {

	private StringBuffer buffer;
	private Tree tree;

	/**
	 * Constructor.
	 */
	public QuestKnotsWriter(Tree tree, List<QuestGU> gus) {

		this.tree = tree;

		buffer = new StringBuffer();

		buffer.append("MOB 1.0\n");

		int i = 1;
		for (QuestGU gu : gus) {
			for (QuestKnot k : gu.getKnots()) {

				// if (k.gu == 1) continue; // fc-test-omit gu 1 (missing data)

				appendKnot(buffer, k, i++);
			}
		}

		buffer.append("Fin Mob\n");

	}

	private void appendKnot(StringBuffer b, QuestKnot k, int i) {
		b.append("# " + i + "\n");
		b.append("Tronc\n");
		b.append("Format=cercle\n");
		b.append("Orientation=auto\n");
		b.append("NbRy=20\n");
		b.append("cou = rvb256 255,  0,  0\n");
		b.append("Contours :\n");

		for (QuestKnotDiameter d : k.getDiameters()) {
			b.append("" + d.x);
			b.append(" , ");
			b.append("" + d.y);
			b.append(" , ");
			b.append("" + d.z);
			b.append(" , ");
			b.append("" + (d.diameter / 2d)); // Caution: Mob needs a radius
			b.append("\n");
		}

		b.append("Fin contours\n");
		b.append("Fin tronc\n");

	}

	public void save(String fileName) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));

			out.write(buffer.toString());
			out.newLine();

			out.close();

		} catch (Exception e) {
			Log.println(Log.ERROR, "QuestKnotsWriter.save ()", "Could not write in file: " + fileName, e);
		}
	}

}
