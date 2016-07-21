/* 
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2000-2003  Francois de Coligny
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version. *  * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied 
 * warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package capsis.extension.modeltool.amapsim2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import jeeb.lib.util.Log;
import jeeb.lib.util.Settings;
import capsis.lib.amapsim.AMAPsimBranch;
import capsis.lib.amapsim.AMAPsimCycle;
import capsis.lib.amapsim.AMAPsimLayer;
import capsis.lib.amapsim.AMAPsimRing;
import capsis.lib.amapsim.AMAPsimTree;
import capsis.lib.amapsim.AMAPsimTreeStep;
import capsis.lib.amapsim.AMAPsimUC;
import capsis.lib.amapsim.AMAPsimUT;
import capsis.util.SwapDataInputStream;

/**
 * Mode1Response.
 * 
 * @author F. de Coligny - november 2002 / january 2004
 */
public class Mode1Response extends Response {

	// Data to be received
	public int dataLength;		// this field is not counted in dataLength	
	public String messageId;	// 5 chars without NULL after
	public int requestType;		// 1 = Mode_1
	public int returnCode;		// 0 = correct, other, see server nomenclature

	public int satisfactionIndex;
	
		public float surface;				// same value than in request
		public int numberOfTreesInStand;	// same value than in request
	
	public int numberOfTrees;
	public Collection trees;			// capsis.lib.amapsim.AMAPsimTree


	public Mode1Response (SwapDataInputStream in) throws IOException {
		super (in);

boolean trace = false;			// activate trace : t = true;
if (Settings.getProperty ("amapsim.trace", false)) {trace = true;}
System.out.println ("amapsim.trace "+trace);

		if (in == null) {return;}
		
		// Technical data
		//
		dataLength = in.readInt ();
		StringBuffer sb = new StringBuffer ();
		for (int i = 0; i < 5; i++) {
			sb.append ((char) in.readByte ());
		}
		messageId = sb.toString ();
		requestType = in.readInt ();
		returnCode = in.readInt ();
if (trace) Log.println ("");
if (trace) Log.println ("Mode 1/2 Response...");
if (trace) Log.println ("dataLength="+dataLength+" messageId="+messageId+" requestType="+requestType+" returnCode="+returnCode);
		
		if (returnCode != 0) {return;}
		
		// Functionnal data
		//
		satisfactionIndex = in.readInt ();
if (trace) Log.println ("satisfactionIndex="+satisfactionIndex);
		
		surface = in.readFloat ();
		numberOfTreesInStand = in.readInt ();
if (trace) Log.println ("surface="+surface+" numberOfTreesInStand="+numberOfTreesInStand);
		
		numberOfTrees = in.readInt ();
if (trace) Log.println ("numberOfTrees="+numberOfTrees);
		trees = new ArrayList ();	// fc - 4.2.2004 (Vector)
		
		for (int i = 0; i < numberOfTrees; i++) {
			AMAPsimTree t = new AMAPsimTree ();		// 21.1.2004
			trees.add (t);
			
			t.treeId = in.readInt ();
if (trace) Log.println ("beginning tree "+t.treeId);
			t.fileName = Response.readString (in);
if (trace) Log.println ("  fileName "+t.fileName);
			
			// TreeSteps
			//
			t.numberOfTreeSteps = in.readInt ();
if (trace) Log.println ("  numberOfTreeSteps "+t.numberOfTreeSteps);
			//~ t.treeSteps = new HashSet ();	// Strange order, trying Vector (seems better for chronological order)
			t.treeSteps = new ArrayList ();	// *** order of response = order of request -> do not sort *** - 8.7.2003
			
			for (int j = 0; j < t.numberOfTreeSteps; j++) {
				AMAPsimTreeStep s = new AMAPsimTreeStep ();
				t.treeSteps.add (s);
if (trace) Log.println ("  step "+j+" (of tree "+t.treeId+")");
				
				s.age = in.readInt ();						// 20.1.2004
if (trace) Log.println ("    age "+s.age);
				
				s.dbh = in.readFloat ();
if (trace) Log.println ("    dbh "+s.dbh);
					s.trunkDiameter260 = in.readFloat ();		// 19.1.2004
					s.mediumDiameter = in.readFloat ();			// 19.1.2004
if (trace) Log.println ("    trunkDiameter260 "+s.trunkDiameter260+" mediumDiameter "+s.mediumDiameter);
				
				s.height = in.readFloat ();
if (trace) Log.println ("    height "+s.height);
					s.heightDiameter7 = in.readFloat ();		// 19.1.2004
					s.heightDiameter2 = in.readFloat ();		// 19.1.2004
					s.mediumHeight = in.readFloat ();			// 19.1.2004
if (trace) Log.println ("    heightDiameter7 "+s.heightDiameter7+" heightDiameter2 "+s.heightDiameter2+" mediumHeight    "+s.mediumHeight);
				
				s.trunkVolume = in.readFloat ();
if (trace) Log.println ("    trunkVolume "+s.trunkVolume);
					s.trunkVolumeDplus20 = in.readFloat ();			// 19.1.2004
					s.trunkVolumeD20to7 = in.readFloat ();			// 19.1.2004
					s.trunkVolumeD7to4 = in.readFloat ();			// 19.1.2004
					s.trunkVolumeD4to0 = in.readFloat ();			// 19.1.2004
					s.trunkVolume260 = in.readFloat ();				// 19.1.2004
					s.trunkVolumeDplus7 = in.readFloat ();			// 19.1.2004
					s.trunkVolumeD7to2 = in.readFloat ();			// 19.1.2004
if (trace) Log.println ("    trunkVolumeDplus20 "+s.trunkVolumeDplus20+" trunkVolumeD20to7  "+s.trunkVolumeD20to7+" trunkVolumeD7to4   "+s.trunkVolumeD7to4);
if (trace) Log.println ("    trunkVolumeD4to0   "+s.trunkVolumeD4to0+" trunkVolume260     "+s.trunkVolume260+" trunkVolumeDplus7  "+s.trunkVolumeDplus7);
if (trace) Log.println ("    trunkVolumeD7to2   "+s.trunkVolumeD7to2);
				
				s.branchVolume = in.readFloat ();
if (trace) Log.println ("    branchVolume "+s.branchVolume);
					s.branchVolumeDplus20 = in.readFloat ();		// 19.1.2004
					s.branchVolumeD20to7 = in.readFloat ();			// 19.1.2004
					s.branchVolumeD7to4 = in.readFloat ();			// 19.1.2004
					s.branchVolumeD4to0 = in.readFloat ();			// 19.1.2004
						s.branchVolumeOrder2 = in.readFloat ();			// 19.1.2004
						s.branchVolumeOrder3 = in.readFloat ();			// 19.1.2004
						s.branchVolumeOrdern = in.readFloat ();			// 19.1.2004
if (trace) Log.println ("    branchVolumeDplus20 "+s.branchVolumeDplus20+" branchVolumeD20to7  "+s.branchVolumeD20to7+" branchVolumeD7to4   "+s.branchVolumeD7to4);
if (trace) Log.println ("    branchVolumeD4to0   "+s.branchVolumeD4to0+" branchVolumeOrder2  "+s.branchVolumeOrder2+" branchVolumeOrder3  "+s.branchVolumeOrder3);
if (trace) Log.println ("    branchVolumeOrdern  "+s.branchVolumeOrdern);
				
				s.leafSurface = in.readFloat ();
if (trace) Log.println ("    leafSurface "+s.leafSurface);
				
				// Branches
				//
				s.numberOfBranches = in.readInt ();
if (trace) Log.println ("    numberOfBranches="+s.numberOfBranches);
				s.branches = new ArrayList ();
				
				for (int u = 0; u < s.numberOfBranches; u++) {
					AMAPsimBranch b = new AMAPsimBranch ();
					s.branches.add (b);
if (trace) Log.println ("    branch "+u);
					
					b.branchId = in.readInt ();
if (trace) Log.println ("      branchId "+b.branchId);
					b.branchStatus = in.readInt ();
					b.branchDiameter = in.readFloat ();
					b.branchLength = in.readFloat ();
					b.branchAngle = in.readFloat ();
					b.branchHeight = in.readFloat ();
if (trace) Log.println ("      branchStatus "+b.branchStatus+" branchDiameter "+b.branchDiameter+" branchLength "+b.branchLength);
if (trace) Log.println ("      branchAngle "+b.branchAngle+" branchHeight "+b.branchHeight);
					
						b.branchComplexity = in.readInt ();		// 19.1.2004
						b.branchBearerId = in.readInt ();		// 19.1.2004
if (trace) Log.println ("      branchComplexity "+b.branchComplexity+" branchBearerId "+b.branchBearerId);
					
				}
				
				// Layers
				//
				s.numberOfLayers = in.readInt ();
if (trace) Log.println ("    numberOfLayers="+s.numberOfLayers);
				s.layers = new ArrayList ();
				
				for (int k = 0; k < s.numberOfLayers; k++) {
					AMAPsimLayer l = new AMAPsimLayer ();
					s.layers.add (l);
					
					l.layerHeight = in.readFloat ();
					l.layerDiameter = in.readFloat ();
if (trace) Log.println ("      layerHeight "+l.layerHeight+" layerDiameter "+l.layerDiameter);
				}
				
				// UTs + Cycles
				//
				s.numberOfUTs = in.readInt ();		// 19.1.2004
if (trace) Log.println ("    numberOfUTs="+s.numberOfUTs);
				s.UTs = new ArrayList ();				// 19.1.2004
				
				for (int m = 0; m < s.numberOfUTs; m++) {	// 19.1.2004
					AMAPsimUT ut = new AMAPsimUT ();		// 19.1.2004
					s.UTs.add (ut);							// 19.1.2004
					
if (trace) Log.println ("    UT "+m);
					ut.numberOfCycles = in.readInt ();		// 19.1.2004
if (trace) Log.println ("      numberOfCycles="+ut.numberOfCycles);
					ut.cycles = new ArrayList ();
					
					for (int p = 0; p < ut.numberOfCycles; p++) {
						AMAPsimCycle c = new AMAPsimCycle ();
						ut.cycles.add (c);
						
						c.cycleHeight = in.readFloat ();
						c.numberOfBranches = in.readInt ();
if (trace) Log.println ("      cycleHeight "+c.cycleHeight+" numberOfBranches "+c.numberOfBranches);
					}
				}
				
				// UCs + Rings
				//
				s.numberOfUCs = in.readInt ();					// 19.1.2004
if (trace) Log.println ("    numberOfUCs="+s.numberOfUCs);
				s.UCs = new ArrayList ();							// 19.1.2004
				
				for (int q = 0; q < s.numberOfUCs; q++) {		// 19.1.2004
					AMAPsimUC uc = new AMAPsimUC ();			// 19.1.2004
					s.UCs.add (uc);								// 19.1.2004
if (trace) Log.println ("    UC "+q);
					
					uc.topHeight = in.readFloat ();				// 19.1.2004
if (trace) Log.println ("      topHeight "+uc.topHeight);
					uc.numberOfRings = in.readInt ();			// 19.1.2004
if (trace) Log.println ("      numberOfRings="+uc.numberOfRings);
					uc.rings = new ArrayList ();					// 19.1.2004
					
					for (int v = 0; v < uc.numberOfRings; v++) {	// 19.1.2004
						AMAPsimRing r = new AMAPsimRing ();			// 19.1.2004
						uc.rings.add (r);							// 19.1.2004
						
						r.width = in.readFloat ();				// 19.1.2004
if (trace) Log.println ("        ringWidth "+r.width);
					}												// 19.1.2004
					
				}												// 19.1.2004
				
				
			}
			
		}
		
	}
	
	public int getDataLength () {return dataLength;}
	public String getMessageId () {return messageId;}
	public int getRequestType () {return requestType;}
	public int getReturnCode () {return returnCode;}
	
	public String toString () {
		StringBuffer b = new StringBuffer ("");
		b.append (" dataLength=");
		b.append (dataLength);
		b.append (" messageId=");
		b.append (messageId);
		b.append (" requestType=");
		b.append (requestType);
		b.append (" returnCode=");
		b.append (returnCode);
		
		b.append (" satisfactionIndex=");
		b.append (satisfactionIndex);
		b.append (" numberOfTrees=");
		b.append (numberOfTrees);
		
		return b.toString ();
	}
	
}


