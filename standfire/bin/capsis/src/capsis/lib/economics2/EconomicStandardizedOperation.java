package capsis.lib.economics2;


/**
 * Operations date by date (validityDate has been interpreted)
 * @author GL - 01/02/12
 *
 */
public class EconomicStandardizedOperation implements java.lang.Comparable {
	
	private int date;
	private String label;
	private double expanse;
	private double income;
	private double harvestedVolume; //m3/ha
	private EconomicOperation operation;

	EconomicStandardizedOperation (int date, String label, double income, double expanse, double harvestedVolume, EconomicOperation operation) {
		this.date = date;
		this.label = label;
		this.expanse = expanse;
		this.income = income;
		this.harvestedVolume = harvestedVolume;
		this.operation=operation;
	}

	@Override
	public int compareTo(Object comparableOperations) {
		int date1 = ((EconomicStandardizedOperation) comparableOperations).getDate();
		int date2 = this.getDate();
		if (date1 > date2) return -1;
		else if(date1 == date2) return 0;
		else return 1;
	}

	/**
	 * method that could be used to produce a txt file
	 * GL - 22/02/2012
	 */
	public String toString(String separator){
		String msg;
		msg = getDate() + separator + getLabel() + separator + Math.round(getIncome()*100d)/100d + separator + Math.round(getExpanse()*100d)/100d + separator + Math.round(getHarvestedVolume ()*100)/100d ;
		return msg;
	}

	// --- Accessors
	public int getDate() {return this.date;}
	public String getLabel() {return label;}
	public double getExpanse() {return expanse;}
	public double getIncome() {return income;}
	public EconomicOperation getOperation(){return operation;}
	public double getHarvestedVolume () {return harvestedVolume;}
	public void setHarvestedVolume (double harvestedVolume) {this.harvestedVolume = harvestedVolume;}
}
