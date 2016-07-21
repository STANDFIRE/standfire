package capsis.lib.economics;

import java.util.HashMap;
import java.util.Map;

import jeeb.lib.util.Translator;
import capsis.kernel.GScene;

/**	A crop related expense. Possibly an entry in a rotation in a rotation library. 
*	A rotation contains a crop and such crop expense entries.
*
*	@author O. Pain - october 2007
*/
public class CropExpense extends BillBookLine {

	// fc - 14.5.2008
	public static final String CROP_EXPENSE_TYPE_INPUT_LOCATION = "CROP_EXPENSE_TYPE_INPUT_LOCATION";	// fertilization, herbicide ("intrant")
	public static final String CROP_EXPENSE_TYPE_MECHANIZATION = "CROP_EXPENSE_TYPE_MECHANIZATION";	// Application of input location or soil ploughing
	public static final String CROP_EXPENSE_TYPE_NONE = "CROP_EXPENSE_TYPE_NONE";	// 

	// fc - 14.5.2008
	private static Map cropExpenseTypesMap;
	
	// fc - 14.5.2008
	static {
		Translator.addBundle("capsis.lib.economics.BillBook");
		
		cropExpenseTypesMap = new HashMap<String,String> ();
		cropExpenseTypesMap.put (Translator.swap ("CROP_EXPENSE_TYPE_INPUT_LOCATION"), CROP_EXPENSE_TYPE_INPUT_LOCATION);
		cropExpenseTypesMap.put (Translator.swap ("CROP_EXPENSE_TYPE_MECHANIZATION"), CROP_EXPENSE_TYPE_MECHANIZATION);
		cropExpenseTypesMap.put (Translator.swap ("CROP_EXPENSE_TYPE_NONE"), CROP_EXPENSE_TYPE_NONE);
	}

	private GScene stand;
	private int date;
	private String name;
	private String detail;
	private String expenseType;		// typology in BillBookLine
	private double quantity;
	private String unit;			// typology in Product
	private double unitPrice;
	private double fuelConsumption;


	/**	Constructor.
	*/
	public CropExpense (
			int date, 
			String name, 
			String detail, 
			String expenseType, 	// typology in UnitExpense
			double quantity, 
			String unit, 			// typology in Product
			double unitPrice, 
			double fuelConsumption) {
		this.date = date;
		this.name = name;
		this.detail = detail;
		this.expenseType = expenseType;
		this.quantity = quantity;
		this.unit = unit;
		this.unitPrice = unitPrice;
		this.fuelConsumption = fuelConsumption;
		setCost (true);
	}

	/**	Return a kind of clone of this CropExpense, only the stand is different, 
	*	forced to null.
	*/
	public CropExpense getClone () {	// fc + op - 3.12.2007
		CropExpense ce =  new CropExpense (date, 
				name, 
				detail, 
				expenseType,
				quantity, 
				unit,
				unitPrice, 
				fuelConsumption);
		ce.setStand (null);
		return ce;
	}
	
	public void setStand (GScene stand) {this.stand = stand;}

	public GScene getStand () {return stand;}

	// fc - 14.5.2008
	static public Map<String,String> getCropExpenseTypesMap () {return cropExpenseTypesMap;}

	public int getDate () {return date;}
	public String getName () {return name;}
	public String getDetail () {return detail;}
	public String getExpenseType () {return expenseType;}
	public double getQuantity () {return quantity;}
	public String getUnit () {return unit;}
	public double getUnitPrice () {return unitPrice;}
	public double getFuelConsumption () {return fuelConsumption;}

	public void setDate (int v) {date = v;}
	public void setName (String v ) {name = v;}
	public void setDetail (String v) {detail = v;}
	public void setExpenseType (String v) {expenseType = v;}
	public void setQuantity (double v) {quantity = v;}
	public void setUnit (String v) {unit = v;}
	public void setUnitPrice (double v) {unitPrice = v;}
	public void setFuelConsumption (double v) {fuelConsumption = v;}

	// BillBookLine interface
	//~ public GStand getStand () {return stand;}	// fc + op - 3.12.2007
	public int getBillBookRotationOrder () {return stand.getDate ();}	// fc + op - 3.12.2007
	public int getBillBookYear () {
		BillBookCompatible bbc = (BillBookCompatible) stand;
		int year = bbc.getPlantationAge () - bbc.getRotationAge () + date;
		return year;
	}
	public String getBillBookOperation () {return name;}
	public String getBillBookDetail () {return detail;}
	public String getBillBookType () {return expenseType;}
	public double getBillBookQuantity () {return quantity;}
	public String getBillBookQuantityUnit () {return unit;}
	public double getBillBookUnitPrice () {return unitPrice;}
	public double getBillBookTotalFuelConsumption () {return fuelConsumption;}
	// BillBookLine interface
	

}


