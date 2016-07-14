package capsis.lib.quest.ringviewer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jeeb.lib.util.Log;
import capsis.defaulttype.Tree;
import capsis.extension.treelogger.GPiece;
import capsis.extension.treelogger.GPieceDisc;
import capsis.extension.treelogger.GPieceRing;
import capsis.lib.quest.commons.QuestSpecies;
import capsis.lib.quest.ringviewer.model.QuestModel;

/**
 * Writes the ring control table.
 * 
 * @author Emmanuel Duchateau, F. de Coligny - March 2015
 */
public class QuestRingControlWriter {

	private StringBuffer buffer;
	private Tree tree;
	private QuestSpecies species;
	private GPiece piece;

	private List<QuestModel> ringModels;
	
	/**
	 * Constructor.
	 */
	public QuestRingControlWriter(Tree tree, QuestSpecies species, GPiece piece) {
		this.tree = tree;
		this.species = species;
		this.piece = piece;

		ringModels = new ArrayList<>(QuestSpecies.getSupportedModels(species));

		buffer = new StringBuffer();

		buffer.append("# Quest Ring control file,  " + new Date() + "\n");
		buffer.append("\n");
		buffer.append("#treeId species discId discHeight_m nRings ringRadius_mm ringWidth_mm ringCambialAge ringV1 ringV2 ringV3 ringV4 ringCenter");
		for (QuestModel ringModel : ringModels) {
			buffer.append(" ");
			String name = ringModel.getName();
			name = name.replace (" ", "_");
			buffer.append(name);
		}
		buffer.append("\n");
		
		appendRing(buffer, species, piece);

	}

	private void appendRing(StringBuffer b, QuestSpecies species, GPiece piece) {

		for (GPieceDisc disc : piece.getDiscs()) {

			List<GPieceRing> rings = disc.getRings();
			if (rings == null)
				continue; // next disc

			int nRing = disc.getRings().size();

			for (GPieceRing r : rings) {
				QuestRing ring = (QuestRing) r;

				b.append(tree.getId());
				b.append(" ");
				b.append(species.getCodeName());
				b.append(" ");
				b.append(disc.getId());
				b.append(" ");
				b.append(disc.getHeight_m());
				b.append(" ");
				b.append(nRing);
				b.append(" ");

				b.append(ring.radius_mm);
				b.append(" ");
				b.append(ring.width_mm);
				b.append(" ");
				b.append(ring.cambialAge);
				b.append(" ");
				b.append(ring.v1.toString ().replace (" ", ""));
				b.append(" ");
				b.append(ring.v2.toString ().replace (" ", ""));
				b.append(" ");
				b.append(ring.v3.toString ().replace (" ", ""));
				b.append(" ");
				b.append(ring.v4.toString ().replace (" ", ""));
				b.append(" ");
				b.append(ring.center.toString ().replace (" ", ""));

				for (QuestModel ringModel : ringModels) {
					b.append(" ");
					b.append(ringModel.getValue(ring));
				}
				
				b.append("\n");
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
			Log.println(Log.ERROR, "QuestRingControlWriter.save ()", "Could not write in file: " + fileName, e);
			throw e;
		}
	}

}
