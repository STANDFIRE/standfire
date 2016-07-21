/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2000-2003 Francois de Coligny
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */
package capsis.lib.fire.exporter;

/**
 *  Grid contains the geometry of the mesh of physically-based model such as Firetec or WFDS
 *  
 *  NB : This grid assumes that the exported zone has the following properties:
 *   for a given i, corr[i][j][k].x= cste (see getIndiceX and intersection method)
 *   for a given j, corr[i][j][k].y= cste (see getIndiceY and intersection method)
 *  
 *  corr[i][j][0].x is not cste (terrain following), neither corr[i][j][nz].x=cste (see getIndicesXYZ);
 */

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;

import jeeb.lib.util.Log;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.StatusDispatcher;
import jeeb.lib.util.Translator;
import capsis.lib.fire.fuelitem.FuelMatrix;
import capsis.util.FortranBinaryInputStream;
import capsis.util.Vertex3f;

/**
 * Grid
 * 
 * @author F. Pimont - june 2013
 */
public class Grid {

	public int nx; // number of voxels in x, y, z
	public int ny;
	public int nz;
	public Vertex3f[][][] coor; // coordinates of cell edges, dimension nx + 1,
								// ny + 1, nz + 1 (in the capsis stand
								// coordinates)
	public double dx; // mean voxel size on x, y, z
	public double dy;
	private double dz;
	private boolean iStretch; // mesh is strech on x, y ,z if true
	private boolean jStretch;
	private boolean kStretch;
	private boolean iTopo; // tell if topo or not

	/**
	 * Constructor of the grid when nothing is known about the grid
	 */
	public Grid() {
	}

	/**
	 * Constructor of the grid
	 * 
	 * @param nx
	 *            : number of voxel on the x axis
	 * @param ny
	 *            : number of voxel on the y axis
	 * @param nz
	 *            : number of voxel on the z axis
	 */
	public Grid(int nx, int ny, int nz) {
		this.nx = nx;
		this.ny = ny;
		this.nz = nz;
		this.coor = new Vertex3f[nx + 1][ny + 1][nz + 1];
	}

	/**
	 * this method computes the mean value of dx, dy, dz and to set
	 * iStretch,jStretch,kStretch
	 */

	public void checkStretching() {
		dx = (coor[nx][0][0].x - coor[0][0][0].x) / nx;
		dy = (coor[0][ny][0].y - coor[0][0][0].y) / ny;
		dz = (coor[0][0][nz].z - coor[0][0][0].z) / nz;
		iStretch = false;
		jStretch = false;
		kStretch = false;
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				for (int k = 0; k < nz; k++) {
					if (coor[i + 1][j][k].x - coor[i][j][k].x != dx) {
						iStretch = true;
					}
					if (coor[i][j + 1][k].y - coor[i][j][k].y != dy) {
						jStretch = true;
					}
					if (coor[i][j][k + 1].z - coor[i][j][k].z != dz) {
						kStretch = true;
					}
				}
			}
		}
	}

	/**
	 * method to build the firetec default grid, with polynomial stretching over
	 * z only
	 * 
	 * @param dx
	 * @param dy
	 * @param meanDz
	 *            : mean dz over the vertical stretching
	 * @param nz
	 *            : number of cell (domain height = meanDz * nz)
	 * @param aa1
	 *            :: ratio of height of cell k=0 to meanDz
	 * @param exportedZoneLengthX
	 *            : dimension of the zone to export on x, y
	 * @param exportedZoneLengthY
	 * @param exportedZoneOriginX
	 *            : origin of the FiretecGrid in the capsis scene
	 * @param exportedZoneOriginY
	 * @throws Exception
	 */
	public void buildFiretecGrid(double dx, double dy, double meanDz, int nz, double aa1, double exportedZoneOriginX,
			double exportedZoneOriginY, double exportedZoneLengthX, double exportedZoneLengthY) throws Exception {
		// assertions
		this.dx = dx;
		this.dy = dy;
		this.dz = meanDz;

		this.nz = nz;

		if (dx <= 0) {
			throw new Exception("buildFiretecGrid, dx (" + dx + ") must be greater than 0, aborted");
		}
		if (dy <= 0) {
			throw new Exception("buildFiretecGrid, dy (" + dy + ") must be greater than 0, aborted");
		}
		if (nz <= 0) {
			throw new Exception("buildFiretecGrid, nz (" + nz + ") must be greater than 0, aborted");
		}
		if (exportedZoneLengthX <= 0) {
			throw new Exception("buildFiretecGrid, exportedZoneLengthX (" + exportedZoneLengthX
					+ ") must be greater than 0, aborted");
		}
		if (exportedZoneLengthY <= 0) {
			throw new Exception("buildFiretecGrid, exportedZoneLengthY (" + exportedZoneLengthY
					+ ") must be greater than 0, aborted");
		}
		if (exportedZoneLengthX % dx != 0) {
			throw new Exception("buildFiretecGrid, exportedZoneLengthX (" + exportedZoneLengthX
					+ ") is not a multiple of dx (" + dx + "), aborted");
		}
		if (exportedZoneLengthY % dy != 0) {
			throw new Exception("buildFiretecGrid, exportedZoneLengthY (" + exportedZoneLengthY
					+ ") is not a multiple of dy (" + dy + "), aborted");
		}

		nx = (int) (exportedZoneLengthX / dx);
		ny = (int) (exportedZoneLengthY / dy);

		this.coor = new Vertex3f[nx + 1][ny + 1][nz + 1];

		// mesh parameters
		double zb = nz * meanDz;
		double aa3 = (1.0 - aa1);
		for (int k = 0; k < nz + 1; k++) {
			double kMeanDz = k * meanDz;
			double zBottomCell = aa3 * Math.pow(kMeanDz, 3.0) / (zb * zb) + aa1 * kMeanDz;
			for (int i = 0; i < nx + 1; i++) {
				double cx = exportedZoneOriginX + i * dx;
				for (int j = 0; j < ny + 1; j++) {
					double cy = exportedZoneOriginY + j * dy;
					coor[i][j][k] = new Vertex3f((float) cx, (float) cy, (float) zBottomCell);
					if (i == 0 & j == 0) {
						System.out.println("for cell k=" + k + ";bottom cell is z=" + zBottomCell);
					}
				}
			}
		}
		iStretch = false;
		jStretch = false;
		if (aa1 == 1.) { // no stretching
			kStretch = false;
		} else { // stretching
			kStretch = true;
		}
	}

	/**
	 * Method to adjust the coordinates with a topo file topo is defined at cell
	 * center (dim nx, ny)
	 */

	public void updateFiretecGridForTopo(float[][] topo) throws Exception {
		int dimx = topo[1].length;
		int dimy = topo.length;
		if ((dimx != nx) && (dimy != ny)) {
			throw new Exception("updateFiretecGridForTopo, wrong topo size : " + dimx + "," + "dimy");
		}
		// float[][] zs = new float[nx + 1][ny + 1]; // topo at the edges
		if (topo == null) {
			MessageDialog.print(this, Translator.swap("FiretecDialog.noTopoWillBeAssumed"));
		}
		// computation of minimum elevation in topofile
		float minElevation = Float.MAX_VALUE;
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				minElevation = Math.min(minElevation, topo[i][j]);
			}
		}
		float zb = (float) (nz * dz);
		System.out.println("minimum elevation of the topofile " + minElevation);
		StatusDispatcher.print(Translator.swap("Grid.setTopo") + "...");

		for (int i = 0; i < nx + 1; i++) {
			for (int j = 0; j < ny + 1; j++) {
				// computation of edge topo values
				float zsij = (float) (0.25 * (topo[Math.max(i - 1, 0)][Math.max(j - 1, 0)]
						+ topo[Math.max(i - 1, 0)][Math.min(j, ny - 1)] + topo[Math.min(i, nx - 1)][Math.max(j - 1, 0)] + topo[Math
						.min(i, nx - 1)][Math.min(j, ny - 1)]) - minElevation);
				for (int k = 0; k < nz + 1; k++) {
					coor[i][j][k].z = coor[i][j][k].z * (zb - zsij) / zb + zsij;
				}
			}
		}
		StatusDispatcher.print(Translator.swap("FiretecMatrix.topoSetCorrectly"));
	}

	/**
	 * method to build the wfds grid from a WFDS grid file
	 * 
	 * @param dir
	 * @param fileName
	 * @param format
	 * @throws Exception
	 */
	public void readWFDSGrid(String fileName, String format) throws Exception {
		try {
			// String dirName = dir.getAbsolutePath ();
			System.out.println("WFDSGridReader.read grid " + fileName);

			boolean sequential = false;
			String architectureID = format; // e.g. littleEndian
			FortranBinaryInputStream in = new FortranBinaryInputStream(new FileInputStream(fileName), sequential,
					architectureID);

			int size = in.readInteger();
			// System.out.println ("size in bytes of nx+1,ny+1,nz+1=" + size);
			this.nx = in.readInteger() - 1; // -1 because the wfds grid is
											// defined at corners, nx is
			// number of voxels
			this.ny = in.readInteger() - 1;
			this.nz = in.readInteger() - 1;
			System.out.println("nx: " + nx + " ny: " + ny + " nz: " + nz);
			// System.out.println
			// ("size of the file must be be "+16*(this.nx+1)*(this.ny+1)*(this.nz+1));
			this.coor = new Vertex3f[nx + 1][ny + 1][nz + 1];
			size = in.readInteger();
			size = in.readInteger();
			// System.out.println ("size in bytes of grid arrays=" + size +
			// "; should be " + 16 * (this.nx + 1)
			// * (this.ny + 1) * (this.nz + 1));

			for (int k = 0; k < this.nz + 1; k++) {
				for (int j = 0; j < this.ny + 1; j++) {
					for (int i = 0; i < this.nx + 1; i++) {
						coor[i][j][k] = new Vertex3f(0, 0, 0);
						coor[i][j][k].x = in.readReal();
						// // test:
						// if (k <= 0 && i <= nx + 1 && j <= 0)
						// System.out.println ("x(=" + i + "," + j + "," + k +
						// ")=" + coor[i][j][k].x);
					}
				}
			}
			for (int k = 0; k < this.nz + 1; k++) {
				for (int j = 0; j < this.ny + 1; j++) {
					for (int i = 0; i < this.nx + 1; i++) {
						coor[i][j][k].y = in.readReal();
						// // test:
						// if (k <= 0 && i <= 0 && j <= ny + 1)
						// System.out.println ("y(=" + i + "," + j + "," + k +
						// ")=" + coor[i][j][k].y);
					}
				}
			}
			for (int k = 0; k < this.nz + 1; k++) {
				for (int j = 0; j < this.ny + 1; j++) {
					for (int i = 0; i < this.nx + 1; i++) {
						coor[i][j][k].z = in.readReal();
						// // test:
						// if (k <= nz + 1 && i <= 0 && j <= 0)
						// System.out.println ("z(=" + i + "," + j + "," + k +
						// ")=" + coor[i][j][k].z);
					}
				}
			}

			System.out.println("coor[0][0][0]: " + coor[0][0][0]);
			System.out.println("coor[nx][ny][nz]: " + coor[nx][ny][nz]);

			in.close();
			this.checkStretching();

		} catch (Exception e) {
			Log.println(Log.ERROR, "WFDSGridReader.read ()", "Exception", e);
			throw e;
		}
	}

	public void setITopo(boolean iTopo) {
		this.iTopo = iTopo;
	}

	public boolean isITopo() {
		return iTopo;
	}

	/**
	 * Returns the i in the grid for point x return -1 when not in grid
	 * if flag=0, 1 is in vox 1, if flag=1, 1 is in vox 0
	 */

	public int getIndiceX(double x, double flag) {
		if (x < coor[0][0][0].x || x > coor[nx][0][0].x)
			return -1;
		if (iStretch) { // stretching
			for (int i = 0; i < nx - 1; i++) {
				if (coor[i + 1][0][0].x > x) {
					return i;
				} else if ((flag>0)&&(coor[i + 1][0][0].x == x)) {
					return i;
				}
			}
			return -1;
		} else { // no stretching
			if (x == coor[nx][0][0].x) {
				return nx - 1;
			} else {
				return (int) Math.max(0,Math.floor((x - coor[0][0][0].x-flag*1e-6) / dx));
			}
		}
	}

	/**
	 * Returns the j in the grid for point y return -1 when not in grid
	 * if flag=0, 1 is in vox 1, if flag=1, 1 is in vox 0
	 */

	public int getIndiceY(double y, double flag) {
		if (y < coor[0][0][0].y || y > coor[0][ny][0].y)
			return -1;
		if (jStretch) { // stretching
			for (int j = 0; j < ny - 1; j++) {
				if (coor[0][j + 1][0].y > y) {
					return j;
				} else if ((flag>0)&&(coor[0][j+1][0].y == y)) {
					return j;
				}
			}
			return -1;
		} else { // no stretching
			if (y == coor[0][ny][0].y) {
				return ny - 1;
			} else {
				return (int) Math.max(0,Math.floor((y - coor[0][0][0].y-flag*1e-6) / dy));
			}
		}
	}

	/**
	 * Returns the i, j in the grid for point (x,y),if flag=0, 1 is in vox 1, if flag=1, 1 is in vox 0
	 */

	public int[] getIndicesXY(double x, double y,double flag) {
		int[] ij = new int[2];
		int iRes = getIndiceX(x,flag);
		// if (iRes == -1) return null;
		int jRes = getIndiceY(y,flag);
		// if (jRes == -1) return null;
		ij[0] = iRes;
		ij[1] = jRes;
		return ij;
	}

	/**
	 * Returns the indices of the voxel containing (x,y,z), if flag=0, 1 is in vox 1, if flag=1, 1 is in vox 0
	 */
	public int[] getIndicesXYZ(double x, double y, double z, double flag) {
		int[] ijk = new int[3];
		int[] ij = getIndicesXY(x, y, flag);
		// if (ij == null) return null;

		int iRes = ij[0];
		int jRes = ij[1];
		ijk[0] = iRes;
		ijk[1] = jRes;
		ijk[2] = -1;

		if ((z < coor[iRes][jRes][0].z || z > coor[iRes][jRes][nz].z)) {
			return ijk;
		}
		if (kStretch) {
			for (int k = 0; k < nz - 1; k++) {
				if (coor[iRes][jRes][0].z > z) {
					ijk[2] = k;
					return ijk;
				}
			}
		} else { // no stretching
			ijk[2] = (int) Math.max(Math.floor((z - coor[iRes][jRes][0].z - flag*1e-6) / dz),0);
			return ijk;
		}
		return ijk;
	}

	public float getX0() {
		return coor[0][0][0].x;
	}

	public float getY0() {
		return coor[0][0][0].y;
	}

	public float getX1() {
		return coor[nx][0][0].x;
	}

	public float getY1() {
		return coor[0][ny][0].y;
	}

	public float getVolume(int i, int j, int k) {
		return (coor[i + 1][j][k].x - coor[i][j][k].x) * (coor[i][j + 1][k].y - coor[i][j][k].y)
				* (coor[i][j][k + 1].z - coor[i][j][k].z);
	}

	/**
	 * Method for computation of the intersection of the voxel fmi, fmj, fmk of
	 * a fuelMatrix with the grid Return an array of quadruplets (at least one).
	 * i varies between 0 and nx-1 // Each quadruplet is fi, fj, fk of the
	 * intersecting firetec voxel and contribution depending // on the
	 * intersection volume. // If only one quadruplet, its contribution is 1
	 * (the crownMatrix voxel is completely in the // returned // firetecMatrix
	 * voxel: full contribution. // If several quadruplets, the sum of their
	 * contribution is equal to 1. // if the crown voxel is outside the scene
	 * borders, returns null - fc - 29.4.2008 // fc - 10.1.2008
	 * 
	 * @param fm
	 *            FuelMatrix
	 * @param fmi
	 *            voxel indices of the fuelmatrix i,j,k
	 * @param fmj
	 * @param fmk
	 */
	public double[][] intersection(FuelMatrix fm, int fmi, int fmj, int fmk, String source) throws Exception {
		double[][] result = null;

		// compute the list of grid voxel concerned by the intersection
		Collection<int[]> voxels = findFiretecVoxelsConcerned(fm, fmi, fmj, fmk/*
																				 * ,
																				 * source
																				 */);
		if (voxels == null)
			return result;
		int nb = voxels.size();
		result = new double[nb][4];
		int cpt = 0;
		for (int[] voxel : voxels) {
			int i = voxel[0];
			int j = voxel[1];
			int k = voxel[2];
			double contribution = calculateContribution(fm, fmi, fmj, fmk, i, j, k);

			result[cpt][0] = i;
			result[cpt][1] = j;
			result[cpt][2] = k;
			result[cpt][3] = contribution;
			cpt++;
		}

		return result;
	}

	/**
	 * this method return a collection of the i,j,k of Grid that are intersected
	 * with the voxel fi,fj,fk of the fm
	 * 
	 * @throws Exception
	 */
	private Collection<int[]> findFiretecVoxelsConcerned(FuelMatrix fm, int fmi, int fmj, int fmk/*
																								 * ,
																								 * String
																								 * source
																								 */) throws Exception
	/* throws Exception */{

		Collection<int[]> result = new ArrayList();

		//
		int fmip1 = fmi + 1;
		int fmjp1 = fmj + 1;
		int fmkp1 = fmk + 1;
		// if no intersection at all:
		if (fm.getX(fmip1) < this.getX0() || fm.getX(fmi) > this.getX1() || fm.getY(fmjp1) < this.getY0()
				|| fm.getY(fmj) > this.getY1()) {
			return null;
		}

		int imin = getIndiceX(fm.getX(fmi),0d);
		if (imin == -1) {
			imin = 0;
		}
		int imax = getIndiceX(fm.getX(fmip1),0d);
/*		if (imax == -1) {
			imax = nx - 1;
		}
	*/	int jmin = getIndiceY(fm.getY(fmj),0d);
		if (jmin == -1) {
			jmin = 0;
		}
		int jmax = getIndiceY(fm.getY(fmjp1),0d);
//		if (jmax == -1) {
//			jmax = ny - 1;
//		}

		if (imin < 0 || imax >= nx || jmin < 0 || jmax >= ny) {
			throw new Exception("grid.findVoxelConcerned, wrong min max index : " + imin + "," + imax + "," + jmin
					+ "," + jmax);
		}

		for (int i = imin; i <= imax; i++) {
			for (int j = jmin; j <= jmax; j++) {
				for (int k = 0; k < nz; k++) {
					// low point of fm is between or low point of fm is below
					// and high point is
					// above or high point is between
					double lowfm = fm.getZ(fmk, fmi, fmj);
					double highfm = fm.getZ(fmkp1, fmi, fmj);
					if ((lowfm >= coor[i][j][k].z && lowfm < coor[i][j][k + 1].z)
							|| (lowfm < coor[i][j][k].z && highfm >= coor[i][j][k + 1].z)
							|| (highfm >= coor[i][j][k].z && highfm < coor[i][j][k + 1].z)) {
						int[] triplet = new int[3];
						triplet[0] = i;
						triplet[1] = j;
						triplet[2] = k;
						result.add(triplet);
					}
				}
			}
		}

		return result;
	}

	/**
	 * Calculate the contribution of the given crown voxel in the given firetec
	 * voxel, equal to the percentage of its volume in the firetec voxel.
	 * (i,j,k) : grid (fmi,fmj,fmk) : fuelMatrix
	 */
	private double calculateContribution(FuelMatrix fm, int fmi, int fmj, int fmk, int i, int j, int k) {

		int fmip1 = fmi + 1;
		int fmjp1 = fmj + 1;
		int fmkp1 = fmk + 1;

		double xMin = Math.max(fm.getX(fmi), coor[i][j][k].x);
		double xMax = Math.min(fm.getX(fmip1), coor[i + 1][j][k].x);
		double wx = xMax - xMin;

		double yMin = Math.max(fm.getY(fmj), coor[i][j][k].y);
		double yMax = Math.min(fm.getY(fmjp1), coor[i][j + 1][k].y);
		double wy = yMax - yMin;
		double wz = 0;
		if (iTopo) {
			// TODO FP:topo calculateContribution fo the fuelMatrix
		} else { // no topo fm.coor[fmi][fmj][fmk]=cst for a given fmk)
			double zMin = Math.max(fm.getZ(fmk, fmi, fmj), coor[i][j][k].z);
			double zMax = Math.min(fm.getZ(fmkp1, fmi, fmj), coor[i][j][k + 1].z);
			wz = zMax - zMin;
		}
		double insideVolume = wx * wy * wz;
		double crownVoxelVolume = fm.getVoxelVolume();

		double contribution = insideVolume / crownVoxelVolume;
		return contribution;
	}
}
