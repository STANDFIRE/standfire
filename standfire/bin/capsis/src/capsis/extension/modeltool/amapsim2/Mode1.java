/* 
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2000-2003  Francois de Coligny
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

package capsis.extension.modeltool.amapsim2;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import jeeb.lib.util.Log;
import jeeb.lib.util.Settings;
import capsis.defaulttype.Tree;
import capsis.defaulttype.TreeList;
import capsis.kernel.GModel;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.util.SwapDataOutputStream;
import capsis.util.methodprovider.AgeDomProvider;
import capsis.util.methodprovider.AgeGProvider;
import capsis.util.methodprovider.DdomProvider;
import capsis.util.methodprovider.DgProvider;
import capsis.util.methodprovider.FertilityProvider;
import capsis.util.methodprovider.GProvider;
import capsis.util.methodprovider.HdomProvider;
import capsis.util.methodprovider.HgProvider;
import capsis.util.methodprovider.MeanAgeProvider;
import capsis.util.methodprovider.MeanDbhProvider;
import capsis.util.methodprovider.MeanHProvider;
import capsis.util.methodprovider.NProvider;

/**
 * Mode1 request for AMAPsim client/server connection.
 * 
 * @author F. de Coligny - november 2002
 */
public class Mode1 extends Request {
	
	// Data to be sent to server
	public int dataLength;		// this field is not counted in dataLength	
	public String messageId;	// 5 chars
	public int requestType;		// 1 = Mode1
	
	// Stand global data
	public String species;		
	public boolean storeLineTree;
	public boolean storeMtg;
	public boolean storeBranches;
	public boolean storeCrownLayers;
	public boolean storePolycyclism;
	
		public boolean storeTrunkShape;		// 19.1.2004
		public int initialSimulationAge;	// 20.1.2004
	
	public float surface;		
	public int numberOfTreesInStand;
	public float basalArea;		
	public float fertilityHDom;
	public float fertilityAge;
	public float coeffAge;
	
	public boolean useAge;
	public float ageMean;
	public float ageg;
	public float ageDom;
	public float ageStandardDeviation;
	public float ageMin;
	public float ageMax;
	
	public boolean useH;
	public float HMean;
	public float Hg;
	public float HDom;
	public float HStandardDeviation;
	public float HMin;
	public float HMax;
	
	public boolean useD;
	public float DMean;
	public float Dg;
	public float DDom;
	public float DStandardDeviation;
	public float DMin;
	public float DMax;
	
		public boolean useCrown;			// added on 19.1.2004
		public float crownBaseHeight;		// added on 19.1.2004
		public float crownMaxDiameter;		// added on 19.1.2004
	
	public int numberOfTreesToBeSimulated;
	
	// Individual data
	public int numberOfTrees;
	public Collection trees;
	
	protected class TreeDesc implements Serializable {
		public int treeId;
		public String treeSpecies;
		public float finalCrownBaseHeight;
		public float finalCrownDiameter;
		
		public int numberOfTreeSteps;
		public Collection treeSteps;
	}
	
	protected class TreeStep implements Serializable {
			public int age;				// 20.1.2003 - float -> int
		public float height;
		public float dbh;
	}
	
	// Management properties
	// step won't be serialized : only known during construction
	transient protected Step step;


	/**	Constructor.
	*/
	public Mode1 () {}
	
	
	/**	Constructor.
	*/
	public Mode1 (Object params, Step step, String messageId) throws Exception {
		this.step = step;
		this.messageId = messageId;		// messageId is now given - fc - 30.1.2004
		
		// New request or consider a model request
		//
		if (params != null) {
			// fc - 13.10.2003 - open dialog with model request
			init ((Mode1) params);
		} else {
			setDefaultValues ();
		}
		Mode1Dialog dlg = new Mode1Dialog (this, step);
		dlg.dispose ();
		dataLength = getLength ();	// does not include dataLength field
	}
	
	
	/**	Write the request on the given output stream.
	*/
	public void write (SwapDataOutputStream out) throws IOException {
	
boolean trace = false;			// activate trace : t = true;
if (Settings.getProperty ("amapsim.trace", false)) {trace = true;}
System.out.println ("amapsim.trace "+trace);

		out.writeInt (dataLength);
		out.writeBytes (messageId);		// 5 bytes for 5 chars, NO NULL after (known length)
		out.writeInt (requestType);
		
if (trace) Log.println ("");
if (trace) Log.println ("Mode 1/2 Request...");
if (trace) Log.println ("dataLength="+dataLength+" messageId="+messageId+" requestType="+requestType);
		
		Request.writeString (out, species);		// one byte per char, one NULL (0 value byte) at the end
if (trace) Log.println ("species "+species);
		
		out.writeBoolean (storeLineTree);
		out.writeBoolean (storeMtg);
		out.writeBoolean (storeBranches);
		out.writeBoolean (storeCrownLayers);
		out.writeBoolean (storePolycyclism);
		out.writeBoolean (storeTrunkShape);		// 19.1.2004
		out.writeInt (initialSimulationAge);	// 20.1.2004
if (trace) Log.println ("storeLineTree "+storeLineTree);
if (trace) Log.println ("storeMtg      "+storeMtg);
if (trace) Log.println ("storeBranches "+storeBranches);
if (trace) Log.println ("storeCrownLayers "+storeCrownLayers);
if (trace) Log.println ("storePolycyclism "+storePolycyclism);
if (trace) Log.println ("storeTrunkShape  "+storeTrunkShape);
if (trace) Log.println ("initialSimulationAge "+initialSimulationAge);
		
		out.writeFloat (surface);
		out.writeInt (numberOfTreesInStand);
		out.writeFloat (basalArea);
		out.writeFloat (fertilityHDom);
		out.writeFloat (fertilityAge);
		out.writeFloat (coeffAge);
if (trace) Log.println ("surface "+surface);
if (trace) Log.println ("numberOfTreesInStand "+numberOfTreesInStand);
if (trace) Log.println ("basalArea "+basalArea);
if (trace) Log.println ("fertilityHDom "+fertilityHDom);
if (trace) Log.println ("fertilityAge "+fertilityAge);
if (trace) Log.println ("coeffAge "+coeffAge);
		
		out.writeBoolean (useAge);
		out.writeFloat (ageMean);
		out.writeFloat (ageStandardDeviation);
		out.writeFloat (ageMin);
		out.writeFloat (ageMax);
		out.writeFloat (ageg);
		out.writeFloat (ageDom);
if (trace) Log.println ("useAge "+useAge);
if (trace) Log.println ("ageMean "+ageMean+" ageg "+ageg+" ageDom "+ageDom);
if (trace) Log.println ("ageStandardDeviation "+ageStandardDeviation+" ageMin "+ageMin+" ageMax "+ageMax);
		
		out.writeBoolean (useH);
		out.writeFloat (HMean);
		out.writeFloat (HStandardDeviation);
		out.writeFloat (HMin);
		out.writeFloat (HMax);
		out.writeFloat (Hg);
		out.writeFloat (HDom);
if (trace) Log.println ("useH "+useH);
if (trace) Log.println ("HMean "+HMean+" Hg "+Hg+" HDom "+HDom);
if (trace) Log.println ("HStandardDeviation "+HStandardDeviation+" HMin "+HMin+" HMax "+HMax);
		
		out.writeBoolean (useD);
		out.writeFloat (DMean);
		out.writeFloat (DStandardDeviation);
		out.writeFloat (DMin);
		out.writeFloat (DMax);
		out.writeFloat (Dg);
		out.writeFloat (DDom);
if (trace) Log.println ("useD "+useD);
if (trace) Log.println ("DMean "+DMean+" Dg "+Dg+" DDom "+DDom);
if (trace) Log.println ("DStandardDeviation "+DStandardDeviation+" DMin "+DMin+" DMax "+DMax);
		
		out.writeBoolean (useCrown);			// added on 19.1.2004
		out.writeFloat (crownBaseHeight);		// added on 19.1.2004
		out.writeFloat (crownMaxDiameter);		// added on 19.1.2004
if (trace) Log.println ("useCrown "+useCrown);
if (trace) Log.println ("crownBaseHeight "+crownBaseHeight+"crownMaxDiameter "+crownMaxDiameter);
		
		out.writeInt (numberOfTreesToBeSimulated);
if (trace) Log.println ("numberOfTreesToBeSimulated "+numberOfTreesToBeSimulated);
		
		// Individual data (unused by Mode 1 but inherited by Mode2)
		out.writeInt (numberOfTrees);
if (trace) Log.println ("numberOfTrees "+numberOfTrees+" (individual data for mode 2 only)");
		
		for (Iterator i = trees.iterator (); i.hasNext ();) {
			TreeDesc t = (TreeDesc) i.next ();
			
			out.writeInt (t.treeId);
			Request.writeString (out, t.treeSpecies);	// one byte per char, one NULL (0 value byte) at the end
			out.writeFloat (t.finalCrownBaseHeight);
			out.writeFloat (t.finalCrownDiameter);
if (trace) Log.println ("treeId "+t.treeId);
if (trace) Log.println ("  treeSpecies "+t.treeSpecies);
if (trace) Log.println ("  finalCrownBaseHeight "+t.finalCrownBaseHeight);
if (trace) Log.println ("  finalCrownDiameter "+t.finalCrownDiameter);
			
			out.writeInt (t.numberOfTreeSteps);
if (trace) Log.println ("  numberOfTreeSteps "+t.numberOfTreeSteps);
			
			for (Iterator j = t.treeSteps.iterator (); j.hasNext ();) {
				TreeStep s = (TreeStep) j.next ();
				
				out.writeInt (s.age);	// 20.1.2004
				out.writeFloat (s.height);
				out.writeFloat (s.dbh);
if (trace) Log.println ("  age "+s.age);
if (trace) Log.println ("    height "+s.height);
if (trace) Log.println ("    dbh "+s.dbh);
				
			}
		}
		
		out.flush ();
	}

	
	/**	Calculate length of message to be sent in bytes
	*/
	public int getLength () {
		// boolean : 1 byte
		// float, int : 4 bytes
		// writeString () : length + 1 byte for final /00
		
		//~ int l = 121;		// fix part (dataLength is not taken into account)
		int l = 135;	// fix part (dataLength is not taken into account) // added 14 bytes (20.1.2004)
		l+=Request.stringLength (species);
		
		for (Iterator i = trees.iterator (); i.hasNext ();) {
			TreeDesc t = (TreeDesc) i.next ();
			l+=16;
			l+=Request.stringLength (t.treeSpecies);
			
			for (Iterator j = t.treeSteps.iterator (); j.hasNext ();) {
				TreeStep s = (TreeStep) j.next ();
				l+=12;
			}
		}
		return l;
	}
	
	
	// Accessors.
	//
	public int getDataLength () {return dataLength;}
	public String getMessageId () {return messageId;}
	public int getRequestType () {return requestType;}
	public String toString () {
		return "Mode1: dataLength="+dataLength+" messageId="+messageId
				+" requestType="+requestType+" (...)";
	}
	
	
	// Init request from model request before dialog opening.
	// fc - 13.10.2003
	//
	protected void init (Mode1 modelRequest) {
		TreeList stand = (TreeList) step.getScene ();
		GModel model = step.getProject ().getModel ();
		MethodProvider mp = model.getMethodProvider ();
		
		dataLength = 0;				// MUST BE RECALCULATED LATER
		//~ messageId = ProtocolManager.nextMessageId ();	// NEW MESSAGEID
		requestType = modelRequest.requestType;
		
		species = modelRequest.species;
		
		storeLineTree = modelRequest.storeLineTree;
		storeMtg = modelRequest.storeMtg;
		storeBranches = modelRequest.storeBranches;
		storeCrownLayers = modelRequest.storeCrownLayers;
		storePolycyclism = modelRequest.storePolycyclism;
		
			storeTrunkShape = modelRequest.storeTrunkShape;				// 19.1.2004
			initialSimulationAge = modelRequest.initialSimulationAge;	// 20.1.2004
		
		surface = modelRequest.surface;
		numberOfTreesInStand = modelRequest.numberOfTreesInStand;
		basalArea = modelRequest.basalArea;
		fertilityHDom = modelRequest.fertilityHDom;
		fertilityAge = modelRequest.fertilityAge;
		coeffAge = modelRequest.coeffAge;
		
		useAge = modelRequest.useAge;
		ageMean = modelRequest.ageMean;
		ageg = modelRequest.ageg;
		ageDom = modelRequest.ageDom;
		ageStandardDeviation = modelRequest.ageStandardDeviation;
		ageMin = modelRequest.ageMin;
		ageMax = modelRequest.ageMax;
		
		useH = modelRequest.useH;
		HMean = modelRequest.HMean;
		Hg = modelRequest.Hg;
		HDom = modelRequest.HDom;
		HStandardDeviation = modelRequest.HStandardDeviation;
		HMin = modelRequest.HMin;
		HMax = modelRequest.HMax;
		
		useD = modelRequest.useD;
		DMean = modelRequest.DMean;
		Dg = modelRequest.Dg;
		DDom = modelRequest.DDom;
		DStandardDeviation = modelRequest.DStandardDeviation;
		DMin = modelRequest.DMin;
		DMax = modelRequest.DMax;
		
			useCrown = modelRequest.useCrown;					// added on 19.1.2004
			crownBaseHeight = modelRequest.crownBaseHeight;		// added on 19.1.2004
			crownMaxDiameter = modelRequest.crownMaxDiameter;	// added on 19.1.2004
		
		numberOfTreesToBeSimulated = modelRequest.numberOfTreesToBeSimulated;
			
		// No individual data in Mode 1
		numberOfTrees = 0;
		trees = new ArrayList ();
		
	}
	
	
	// Set default values for every request property.
	//
	protected void setDefaultValues () {
		
		TreeList stand = (TreeList) step.getScene ();
		Collection wTrees = stand.getTrees ();	// fc - 9.4.2004
		
		GModel model = step.getProject ().getModel ();
		MethodProvider mp = model.getMethodProvider ();
		
		dataLength = 0;		// deferred calculation
		//~ messageId = ProtocolManager.nextMessageId ();
		
		requestType = 1;
		
		species = "";
		
		numberOfTreesToBeSimulated = 0;
			
		storeLineTree = false;
		storeMtg = false;
		storeBranches = false;
		storeCrownLayers = false;
		storePolycyclism = false;
		
			storeTrunkShape = false;	// 19.1.2004
			initialSimulationAge = 0;	// 20.1.2004
		
		surface = (float) stand.getArea ();
		
		//~ numberOfTreesInStand = stand.getTrees ().size ();	// In case of MAID, one tree per cohort
		try {numberOfTreesInStand = (int) ((NProvider) mp).getN (stand, wTrees);	// fc - 22.8.2006 - Numberable is double
				} catch (Exception e) {numberOfTreesInStand = 0;}
		
		try {basalArea = (float) ((GProvider) mp).getG (stand, wTrees);
				} catch (Exception e) {basalArea = 0;}
		
		try {
			// double[] contains a couple (Hdom, age)
			double[] fertility = ((FertilityProvider) mp).getFertility (stand);
			fertilityHDom = (float) fertility[0];
			fertilityAge = (int) fertility[1];
		} catch (Exception e) {
			fertilityHDom = 0;
			fertilityAge = 0;
		}
		
		coeffAge = 1;	// default value
		
		//---------------------------------------------------- Age
		//
		useAge = false;
		
		try {ageMean = (float) ((MeanAgeProvider) mp).getMeanAge (stand, wTrees);	// fc - 30.8.2005
				} catch (Exception e) {ageMean = 0;}
		
		try {ageg = (float) ((AgeGProvider) mp).getAgeG (stand, wTrees);	// fc - 13.12.2005 - added wTrees
				} catch (Exception e) {ageg = 0;}
		
		try {ageDom = (float) ((AgeDomProvider) mp).getAgeDom (stand, wTrees);	// fc - 13.12.2005 - added wTrees
				} catch (Exception e) {ageDom = 0;}
		
		ageStandardDeviation = 0;
		ageMin = Float.MAX_VALUE;	// see below
		ageMax = 0;
		
		//---------------------------------------------------- H
		//
		useH = false;
		
		try {HMean = (float) ((MeanHProvider) mp).getMeanH (stand, stand.getTrees ());	// fc - 30.8.2005
				} catch (Exception e) {HMean = 0;}
		
		try {Hg = (float) ((HgProvider) mp).getHg (stand, wTrees);
				} catch (Exception e) {Hg = 0;}
		
		try {HDom = (float) ((HdomProvider) mp).getHdom (stand, wTrees);
				} catch (Exception e) {HDom = 0;}
		
		HStandardDeviation = 0;
		HMin = Float.MAX_VALUE;		// see below
		HMax = 0;
		
		//---------------------------------------------------- D
		//
		useD = false;
		
		try {DMean = (float) ((MeanDbhProvider) mp).getMeanDbh (stand, wTrees);	// fc - 30.8.2005
				} catch (Exception e) {DMean = 0;}
		
		try {Dg = (float) ((DgProvider) mp).getDg (stand, wTrees);
				} catch (Exception e) {Dg = 0;}
		
		try {DDom = (float) ((DdomProvider) mp).getDdom (stand, wTrees);
				} catch (Exception e) {DDom = 0;}
		
		DStandardDeviation = 0;
		DMin = Float.MAX_VALUE;		// see below
		DMax = 0;
		
			//---------------------------------------------------- Crown	// added on 19.1.2004
			//
			useCrown = false;
			crownBaseHeight = 0f;
			crownMaxDiameter = 0f;
			
		// Find some particular data in the trees
		// fc - 22.10.2003
		for (Iterator i = stand.getTrees ().iterator (); i.hasNext ();) {
			Tree t = (Tree) i.next ();
			ageMin = Math.min (ageMin, t.getAge ());
			ageMax = Math.max (ageMax, t.getAge ());
			HMin = Math.min (HMin, (float) t.getHeight ());
			HMax = Math.max (HMax, (float) t.getHeight ());
			DMin = Math.min (DMin, (float) t.getDbh ());
			DMax = Math.max (DMax, (float) t.getDbh ());
		}
			
		// No individual data in Mode 1
		numberOfTrees = 0;		// or -1 ?
		trees = new ArrayList ();
		
	}
	
}

