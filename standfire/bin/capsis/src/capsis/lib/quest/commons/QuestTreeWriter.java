package capsis.lib.quest.commons;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import jeeb.lib.util.Log;
import capsis.defaulttype.Tree;

/**
 * Writes tree properties in a file for control.
 * 
 * @author Emmanuel Duchateau, F. de Coligny - March 2015
 */
public class QuestTreeWriter {
	
	private StringBuffer buffer;
	
	/**
	 * Constructor.
	 */
	public QuestTreeWriter(Tree tree, List<Double> dbhs, List<Double> heights) {
		buffer = new StringBuffer ();
		
		buffer.append("# Quest Tree file, "+new Date ()+"\n");

		buffer.append("\n");
		buffer.append("treeId = "+tree.getId()+"\n");
		
		QuestCompatible t = (QuestCompatible) tree;
		buffer.append("species = "+t.getQuestSpecies().getCodeName ()+"\n");

		buffer.append("\n");
		buffer.append("# dbh height\n");
		
		Iterator<Double> i1 = dbhs.iterator ();
		Iterator<Double> i2 = heights.iterator ();
		
		while (i1.hasNext () && i2.hasNext ()) {
			double d = i1.next ();
			double h = i2.next ();
			
			// REMOVED fc+ed-24.3.2015, moved on a single line
//			buffer.append(tree.getId());
//			buffer.append(" ");
			
			buffer.append(d);
			buffer.append(" ");
			buffer.append(+h);
			buffer.append("\n");

		}
		
	}
	
	public void save (String fileName) throws Exception {
		try {
		    BufferedWriter out = new BufferedWriter (new FileWriter (fileName));
		   
		    out.write (buffer.toString ());
		    out.newLine ();

		    out.close ();
		    
		} catch (Exception e) {
		    Log.println (Log.ERROR, "QuestTreeWriter.save ()", 
		            "Could not write in file: " + fileName, e);
		    throw e;
		}
	}

}
