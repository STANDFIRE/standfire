package capsis.lib.fire.exporter.wfds;

public class BulkDensityVoxel {
	public int gridNumber;
	public int i;
	public int j;
	public int k;
	public double bulkDensity;

	public BulkDensityVoxel(int gridNumber, int i, int j, int k, double bulkDensity) {
		this.gridNumber = gridNumber;
		this.i = i;
		this.j = j;
		this.k = k;
		this.bulkDensity = bulkDensity;

	}
}
