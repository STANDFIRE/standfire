package capsis.lib.quest.ringviewer;

import java.util.List;

import jeeb.lib.util.Vertex2d;
import jeeb.lib.util.Vertex3d;
import repicea.simulation.treelogger.LoggableTree;
import capsis.defaulttype.Tree;
import capsis.extension.treelogger.GPiece;
import capsis.extension.treelogger.GPieceDisc;
import capsis.lib.quest.commons.QuestTaper;


/**
 * A logger Black spruce (based on Log2Job).
 * 
 * @author Alexis Achim, F. de Coligny - December 2014
 */
public class QuestLogger /* extends TreeLogger implements TreeLoggerImpl */{

	static public GPiece makeFakePiece(LoggableTree tree, QuestTaper taper, List<Double> dbhs, List<Double> heights) {

//		System.out.println("QuestLogger.makeFakePiece()...");
		
		// Number of growth units
		int nGU = heights.size ();

//		QuestBlackSpruceTaper taper = new QuestBlackSpruceTaper();

		Tree t = (Tree) tree;

		double length_m = t.getHeight();

		int j = 1; // one piece only fc-8.12.2014
		double h0_m = 0;
		int pieceId = 1;

		GPiece piece = new GPiece(tree, pieceId, // fc - 1.3.2006
				j, 1d, length_m * 1000, // pieceLength_mm
				h0_m * 1000., // pieceY_mm
				true, // pieceWithBark
				true, // pieceWithPith
				(byte) 1, // numberOfRadius
				"alive", null); // logCategory = null fc-8.12.2014

		piece.setOrigin("Quest piece");

//		System.out.println("QuestLogger piece: "+piece);

		
		// We may add a disc on the ground here 
		
		double centreX_mm = 0;
		double centreZ_mm = 0;

		// Number of discs in the piece (no disc at the top)
		int nDiscs = nGU; // fc+ed-18.3.2015 one disc is missing
//		int nDiscs = nGU - 1;
		
		// Number of rings in the first disc
		int nRings =  nGU; // fc+ed-18.3.2015 one disc is missing
//		int nRings =  nGU - 1;
		
		double[][] ringWidths = new double[nDiscs][nRings];
				
		// We do not create a disc at the top of the piece
		
		double discHeight_m = 0; // fc+ed-18.3.2015 first disc on the ground
		
		for (int i = 0; i < nGU; i++) { // fc+ed-18.3.2015 one disc is missing
//		for (int i = 0; i < nGU - 1; i++) {

			double discHeight_mm = discHeight_m * 1000d;

			// Pith
			Vertex3d pith = new Vertex3d(centreX_mm, discHeight_mm, centreZ_mm);
			piece.addPithPoint(pith);

			// Disc
			int discId = i + 1;
			GPieceDisc disc = new GPieceDisc(discId, discHeight_mm);
			piece.addDisc(disc);

			int discIndex = i;

//			System.out.println("QuestLogger disc: "+discIndex);
			
			int ringIndex = 0;
			int ringCount = 0;
			do {
				ringCount++;

				double d1 = dbhs.get(discIndex + ringIndex); // fc+ed-18.3.2015
				double h1 = heights.get (discIndex + ringIndex); // fc+ed-18.3.2015
//				double d1 = dbhs.get(discIndex + ringIndex + 1);
//				double h1 = heights.get (discIndex + ringIndex + 1);
				boolean overBark = true;

				double r_mm = taper.getTreeRadius_cm(d1, h1, discHeight_m, overBark) * 10;

				ringWidths[discIndex][ringIndex] = r_mm;

				ringIndex++;
				
				// QuestRing ring = new QuestRing(r, disc.getId(), r_mm);
				// disc.addRing(ring);

			} while (ringCount < nRings);

			nRings--;

			discHeight_m = heights.get (i);

			
		}


//		System.out.println("QuestLogger: a mon avis jusque là on est bon...");

		
		// TRACE
//		System.out.println();
//		System.out.println("ringWidths...");
//		for (int d = 0; d < ringWidths.length; d++) {
//			for (int r = 0; r < ringWidths[0].length;r++) {
//				System.out.print(""+ringWidths[d][r]+" ");
//			}
//			System.out.println();
//		}
//		System.out.println();
		
		// Number of rings in the first disc
		nRings =  nGU;
//		nRings =  nGU - 1;
		for (int d = 0; d < ringWidths.length; d++) {

//			System.out.println("QuestLogger creating rings for disc: "+d+" nRings: "+nRings);
			
			for (int r = 0; r < nRings; r++) {
				double r_mm = ringWidths[d][r];
				
				GPieceDisc disc0 = piece.getDiscs().get (d);
				
				double h0 = disc0.getHeight_m();
				double h1 = t.getHeight (); // default
				try {
					GPieceDisc disc1 = piece.getDiscs().get (d + 1);
					h1 = disc1.getHeight_m();
				} catch (Exception e) {} // keep t.getHeight ();
				
				double x1 = r_mm;
				
				double x2 = 0;
				try {
					x2 = ringWidths[d + 1][r - 1];
				} catch (Exception e) {} // keep 0
				
				double x3 = 0;
				try {
					x3 = ringWidths[d + 1][r - 2];
				} catch (Exception e) {} // keep 0
				
				double x4 = 0;
				try {
					x4 = ringWidths[d][r - 1];
				} catch (Exception e) {} // keep 0
				
				Vertex2d v1 = new Vertex2d (x1, h0);
				Vertex2d v2 = new Vertex2d (x2, h1);
				Vertex2d v3 = new Vertex2d (x3, h1);
				Vertex2d v4 = new Vertex2d (x4, h0);
				
				int cambialAge = r + 1;
				double width_mm = v1.x - v4.x;
				
				
				QuestRing ring = new QuestRing(r + 1, disc0.getId(), r_mm, width_mm, cambialAge, v1, v2, v3, v4);
				disc0.addRing(ring);		
			}
			nRings--; 
			
		}


//		System.out.println("QuestLogger piece completed");

		
//		Vector<GPieceDisc> discs = piece.getDiscs();
//		// The highest disc has no rings
//		for (int i = 0; i < discs.size() - 1; i++) {
//
//			int discIndex = i;
//
//			GPieceDisc disc = discs.get(discIndex);
//			
//			// disc0 will contain rings from disc0.height to disc1.height
//			GPieceDisc disc0 = discs.get(i);
//			GPieceDisc disc1 = discs.get(i + 1);
//
//			int r = 1;
//			for (int k = 0; k < nRings; k++) {
//
//				double r_mm = ringWidths[discIndex][k];
//
//				if (r_mm > 0) {
//
//					QuestRing ring = new QuestRing(r++, disc.getId(), r_mm);
//					disc.addRing(ring);
//					
//				}
//			}
//
//		}

		return piece;

	}

}
