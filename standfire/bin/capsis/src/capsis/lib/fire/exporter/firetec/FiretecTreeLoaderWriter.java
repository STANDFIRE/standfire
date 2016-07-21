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
package capsis.lib.fire.exporter.firetec;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jeeb.lib.util.Import;
import jeeb.lib.util.Log;
import jeeb.lib.util.Record;
import jeeb.lib.util.RecordSet;
import jeeb.lib.util.Translator;
import capsis.lib.fire.FiModel;
import capsis.lib.fire.FiStand;
import capsis.lib.fire.fuelitem.FiPlant;
import capsis.lib.fire.intervener.physicalfireeffect.FiretecVoxel;

/**
 * NOT USED: This loader/writer is used to write or load tree description in
 * terms of firetec voxel as described in the FiretecMatrix
 * 
 * - and also individual trees
 * 
 * @author F. Pimont - dec 2009
 */
public class FiretecTreeLoaderWriter extends RecordSet {

	/**
	 * 
	 * This class is an element of the crown list element of a given tree in the
	 * firetec matrix
	 * 
	 * @author pimont
	 * 
	 */
	public class FiretecTreeListElement {

		// public FiretecMatrix fm;

		public int i;// index in firetecmatrix
		public int j;
		public int k;

		public double volume; // crown volume in the cell
		public double biomass; // biomass in the cell

		FiretecTreeListElement(int i, int j, int k, double volume, double biomass) {
			// FiretecMatrix fm) {

			this.i = i;
			this.j = j;
			this.k = k;
			this.volume = volume;
			this.biomass = biomass;
			// this.fm = fm;

			// int nx = fm.nx;
			// int ny = fm.ny;
			// int nz = fm.nz;

		}

	}

	// This map contain treeId and a map <cellNumber, FiretecTreeListElement>
	// to be able to rebuild the crown of all tree from firetecMatrix
	public Map<Integer, Map> treeCrownList = new HashMap<Integer, Map>();
	public String fileName;
	public int nx;
	public int ny;
	public int nz;
	public double dx;
	public double dy;

	public double sceneOriginX;
	public double sceneOriginY;
	public double sceneSizeX;
	public double sceneSizeY;
//	FmPlot plot = null;  // fc-29.1.2015

	static {
		Translator.addBundle("capsis.lib.fire.exporter.FiretecTreeLoaderWriter");
	}

	// firetecmatrix properties described here
	@Import
	static public class FiretecMatrixRecord extends Record {
		public FiretecMatrixRecord() {
			super();
		}

		public FiretecMatrixRecord(String line) throws Exception {
			super(line);
		}

		// public String getSeparator () {return ";";} // to change default "\t"
		// separator
		public int nx;
		public int ny;
		public int nz;
		public double dx;
		public double dy;
		public double sceneOriginX;
		public double sceneOriginY;
		public double sceneSizeX;
		public double sceneSizeY;
	}

	// voxel record is described here
	@Import
	static public class VoxelRecord extends Record {
		public VoxelRecord() {
			super();
		}

		public VoxelRecord(String line) throws Exception {
			super(line);
		}

		// public String getSeparator () {return ";";} // to change default "\t"
		// separator
		public int id;
		public int i;
		public int j;
		public int k;
		public double volume;
		public double biomass;

	}

	/**
	 * Phantom constructor. Only to ask for extension properties (authorName,
	 * version...).
	 */
	public FiretecTreeLoaderWriter() {
	}

	/**
	 * Official constructor Format in Export mode needs a Stand in starter (then
	 * call save (fileName))
	 * 
	 * @param s
	 *            Format in Import mode needs fileName in starter (then call
	 *            load (GModel))
	 */
	/*
	 * public FiretecTreeLoaderWriter(GenericExtensionStarter s) throws
	 * Exception { if (s.getStand () != null) { // Export mode createRecordSet
	 * ((FiStand) s.getStand ());
	 * 
	 * } else if (s.getString () != null) { // Import mode createRecordSet
	 * (s.getString ());
	 * 
	 * } else { throw new Exception ("Unable to recognize mode Import/Export."
	 * +" stand (starter.getStand ())="+s.getStand ()
	 * +" fileName (starter.getString ())="+s.getString ()); }
	 * 
	 * }
	 */
	/**
	 * Direct constructor
	 */
	// public FiretecTreeLoaderWriter(String fileName) throws Exception {
	// this.fileName=fileName;
	// createRecordSet(fileName);
	// } // for direct use for Import

	/**
	 * Extension dynamic compatibility mechanism. This matchwith method checks
	 * if the extension can deal (i.e. is compatible) with the referent.
	 */
	public boolean matchWith(Object referent) {
		try {
			if (!(referent instanceof FiModel)) { // fc-29.1.2015 - was FmModel
				return false;
			}

		} catch (Exception e) {
			Log.println(Log.ERROR, "FiretecTreeLoaderWriter.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}
		return true;
	}

	// TODO FP: restore this write crownListMap
	// /**
	// * this method create a recordset in fileName for the crownListMap defined
	// * in FiretecMatrix
	// *
	// * @param crownListMap
	// * @param fileName
	// * @throws Exception
	// */
	// public void createRecordSet(Grid grid)
	// throws Exception {
	// Map<Integer, Map> treeCrownMap = fm.treeCrownMap;
	// add(new CommentRecord(
	// "This file contains the description of trees in firetec voxels "));
	// add(new CommentRecord("nx	ny	nz	dx	dy	dz"));
	// FiretecMatrixRecord fmr = new FiretecMatrixRecord();
	// fmr.nx = grid.nx;
	// fmr.ny = grid.ny;
	// fmr.nz = grid.nz;
	// fmr.dx = grid.dx;
	// fmr.dy = grid.dy;
	//
	// fmr.sceneOriginX = grid.sceneOriginX;
	// fmr.sceneOriginY = grid.sceneOriginY;
	// fmr.sceneSizeX = grid.sceneSizeX;
	// fmr.sceneSizeY = grid.sceneSizeY;
	// add(fmr);
	// add(new EmptyRecord());
	//
	// add(new CommentRecord("TreeId	i	j	k	volumeInVoxel	biomassInVoxel"));
	// for (int treeId : treeCrownMap.keySet()) {
	// add(new CommentRecord("Tree id " + treeId));
	// Map<Integer, FiretecTreeListElement> voxelListMap = treeCrownMap
	// .get(treeId);
	// for (int voxelNumber : voxelListMap.keySet()) {
	// FiretecTreeListElement el = voxelListMap.get(voxelNumber);
	// VoxelRecord vr = new VoxelRecord();
	// vr.id = treeId;
	// vr.i = el.i;
	// vr.j = el.j;
	// vr.k = el.k;
	// vr.volume = el.volume;
	// vr.biomass = el.biomass;
	// add(vr);
	// }
	// add(new EmptyRecord());
	// }
	// }
	//
	public Map<Integer, Map> load(String fileName, FiStand stand) throws Exception {
		super.createRecordSet(fileName);

		Map<Integer, Map> treeCrownMap = new HashMap<Integer, Map>();
		for (Iterator i = this.iterator(); i.hasNext();) {
			Record record = (Record) i.next();
			if (record instanceof FiretecTreeLoaderWriter.FiretecMatrixRecord) {
				FiretecMatrixRecord fmr = (FiretecMatrixRecord) record;
				this.nx = fmr.nx;
				this.ny = fmr.ny;
				this.nz = fmr.nz;
				this.dx = fmr.dx;
				this.dy = fmr.dy;
				this.sceneOriginX = fmr.sceneOriginX;
				this.sceneOriginY = fmr.sceneOriginY;
				this.sceneSizeX = fmr.sceneSizeX;
				this.sceneSizeY = fmr.sceneSizeY;

			} else {
				VoxelRecord vr = (VoxelRecord) record;
				Map<Integer, FiretecVoxel> voxelMap;
				if (!treeCrownMap.containsKey(vr.id)) {
					// voxelMap = new HashMap<Integer,
					// FiretecTreeListElement>();
					voxelMap = new HashMap<Integer, FiretecVoxel>();
					treeCrownMap.put(vr.id, voxelMap);
				} else {
					voxelMap = treeCrownMap.get(vr.id);
				}
				int voxelNumber = vr.i + 1 + (vr.j) * nx + vr.k * nx * ny;
				if (!voxelMap.containsKey(voxelNumber)) {
					// FiretecTreeListElement el = new FiretecTreeListElement(
					// vr.i, vr.j, vr.k, vr.volume, vr.biomass);
					FiretecVoxel el = new FiretecVoxel(vr.i, vr.j, vr.k, vr.volume, vr.biomass,
							(FiPlant) stand.getTree(vr.id));
					voxelMap.put(voxelNumber, el);
				} else {
					FiretecVoxel el = voxelMap.get(voxelNumber);
					el.plantVolume += vr.volume;
					el.plantBiomass += vr.biomass;
				}
			}
		}
		return treeCrownMap;
	}

	// //////////////////////////////////////////////// Extension stuff
	/**
	 * From Extension interface.
	 */
	public String getName() {
		return Translator.swap("FiretecTreeLoaderWriter");
	}

	/**
	 * From Extension interface.
	 */
	public String getVersion() {
		return VERSION;
	}

	public static final String VERSION = "1.0";

	/**
	 * From Extension interface.
	 */
	public String getAuthor() {
		return "F. Pimont";
	}

	/**
	 * From Extension interface.
	 */
	public String getDescription() {
		return Translator.swap("FiretecTreeLoaderWriter.description");
	}

	// //////////////////////////////////////////////// IOFormat stuff
	public boolean isImport() {
		return true;
	}

	public boolean isExport() {
		return true;
	}
}
