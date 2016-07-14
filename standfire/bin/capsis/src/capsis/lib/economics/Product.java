package capsis.lib.economics;

import java.util.HashMap;
import java.util.Map;

import jeeb.lib.util.Translator;
import capsis.extension.PaleoWorkingProcess;
import capsis.kernel.GScene;

/**	Product - ex: log, stem...
*
*	@author O. Pain - september 2007
*/
public class Product extends BillBookLine {

	// Product names
	public static final String STAND_TREE = "STAND_TREE";
	public static final String TREE = "TREE";		// felled tree
	public static final String LOG = "LOG";
	public static final String CROWN = "CROWN";
	public static final String BUNDLE = "BUNDLE";
	public static final String CHIPS = "CHIPS";
	public static final String TREE_ROADSIDE = "TREE_ROADSIDE";
	public static final String LOG_ROADSIDE = "LOG_ROADSIDE";
	public static final String CROWN_ROADSIDE = "CROWN_ROADSIDE";
	public static final String BUNDLE_ROADSIDE = "BUNDLE_ROADSIDE";
	public static final String CHIPS_ROADSIDE = "CHIPS_ROADSIDE";
	public static final String TREE_MILL = "TREE_MILL";
	public static final String LOG_MILL = "LOG_MILL";
	public static final String CROWN_MILL = "CROWN_MILL";
	public static final String BUNDLE_MILL = "BUNDLE_MILL";
	public static final String CHIPS_MILL = "CHIPS_MILL";

	static private Map<String,String> nameMap;

	// Product units
	public static final String DRY_TON = "DRY_TON";
	public static final String GREEN_TON = "GREEN_TON";
	public static final String M3 = "M3";
	public static final String MAP = "MAP";
	public static final String MWH = "MWH";

	static private Map<String,String> productUnitsMap;

	//~ private String name;	// in product names
	//~ private double quantity;
	//~ private String unit;	// in product units
	//~ private double size;	// ex: log or bundle length
	//~ private GStand stand;

	private String name;	// in product names
	private GScene stand;
	private double qty_ts;	// ts = "tonne sèche" = dry ton
	private double qty_tb;	// tb = "tonne brute ou verte" = green ton
	private double qty_m3;	// if name == CHIPS, this qty is map
	private double size;	// ex: log or bundle length
	private String preferredUnit;	// Log -> m3, chips -> map
	private double unitPrice;
	private double totalFuelConsumption;

	private Producer producer;

	private String billBookOperation;
	private String billBookDetail;
	private String billBookType;

	//~ private String wpClassName;			// additionnal - fc + op - 4.12.2007
	private String inputProductName;	// additionnal - fc + op - 4.12.2007

	static {
		Translator.addBundle("capsis.lib.economics.Product");

		nameMap = new HashMap<String,String> ();
		nameMap.put (Translator.swap ("STAND_TREE"), STAND_TREE);
		nameMap.put (Translator.swap ("TREE"), TREE);
		nameMap.put (Translator.swap ("LOG"), LOG);
		nameMap.put (Translator.swap ("CROWN"), CROWN);
		nameMap.put (Translator.swap ("BUNDLE"), BUNDLE);
		nameMap.put (Translator.swap ("CHIPS"), CHIPS);
		nameMap.put (Translator.swap ("TREE_ROADSIDE"), TREE_ROADSIDE);
		nameMap.put (Translator.swap ("LOG_ROADSIDE"), LOG_ROADSIDE);
		nameMap.put (Translator.swap ("CROWN_ROADSIDE"), CROWN_ROADSIDE);
		nameMap.put (Translator.swap ("BUNDLE_ROADSIDE"), BUNDLE_ROADSIDE);
		nameMap.put (Translator.swap ("CHIPS_ROADSIDE"), CHIPS_ROADSIDE);
		nameMap.put (Translator.swap ("TREE_MILL"), TREE_MILL);
		nameMap.put (Translator.swap ("LOG_MILL"), LOG_MILL);
		nameMap.put (Translator.swap ("CROWN_MILL"), CROWN_MILL);
		nameMap.put (Translator.swap ("BUNDLE_MILL"), BUNDLE_MILL);
		nameMap.put (Translator.swap ("CHIPS_MILL"), CHIPS_MILL);

		productUnitsMap = new HashMap<String,String> ();
		productUnitsMap.put (Translator.swap ("M3"), M3);
		productUnitsMap.put (Translator.swap ("GREEN_TON"), GREEN_TON);
		productUnitsMap.put (Translator.swap ("DRY_TON"), DRY_TON);
		productUnitsMap.put (Translator.swap ("MAP"), MAP);
		productUnitsMap.put (Translator.swap ("MWH"), MWH);
	}


	public Product (String name, GScene stand,
			double qty_ts, double qty_tb, double qty_m3, double size,
			String preferredUnit, double unitPrice, double totalFuelConsumption,
			Producer producer) {
		this.name = name;
		this.stand = stand;
		this.qty_ts = qty_ts;
		this.qty_tb = qty_tb;
		this.qty_m3 = qty_m3;
		this.size = size;
		this.preferredUnit = preferredUnit;
		this.unitPrice = unitPrice;
		this.totalFuelConsumption = totalFuelConsumption;
		this.producer = producer;
		setCost (true);

		if (producer instanceof GScene) {
			billBookOperation = Translator.swap ("Product.standBillBookOperation");
			billBookDetail = Translator.swap ("Product.standBillBookDetail");
			billBookType = BillBookLine.EXPENSE_TYPE_PURCHASE;
		} else if (producer instanceof PaleoWorkingProcess) {
			PaleoWorkingProcess wp = (PaleoWorkingProcess) producer;
			billBookOperation = Translator.swap (wp.getName ());		// "Forwarding"
			billBookDetail = Translator.swap (wp.getStarter ().requestedInputProductName)
					+" - "
					+Translator.swap (getName ());	// "Log - Log road side"
			billBookType = BillBookLine.EXPENSE_TYPE_MECHANIZATION;
			inputProductName = wp.getStarter ().requestedInputProductName;	// fc + op - 4.12.2007
		} else {
			billBookOperation = "unknown operation";
			billBookDetail = "unknown detail";
			billBookType = "unknown type";
		}
	}

	public String getName () {return name;}
	public GScene getStand () {return stand;}
	public double getQty_ts () {return qty_ts;}
	public double getQty_tb () {return qty_tb;}
	public double getQty_m3 () {return qty_m3;}
	public double getSize () {return size;}
	public String getPreferredUnit () {return preferredUnit;}
	public double getQuantityInPreferredUnit () {
		if (preferredUnit.equals (DRY_TON)) {
			return qty_ts;
		} else if (preferredUnit.equals (GREEN_TON)) {
			return qty_tb;
		} else if (preferredUnit.equals (M3)) {
			return qty_m3;
		} else if (preferredUnit.equals (MAP)) {
			return qty_m3;
		} else {
			return -1;
		}
	}
	public double getUnitPrice () {return unitPrice;}
	public double getTotalFuelConsumption () {return totalFuelConsumption;}
	public void setTotalFuelConsumption (double v) {totalFuelConsumption = v;}
	public void setUnitPrice (double v) {unitPrice = v;}	// fc + op - 3.12.2007 - user can change this cost

	static public Map<String,String> getNameMap () {return nameMap;}
	static public Map<String,String> getProductUnitsMap () {return productUnitsMap;}

	// BillBookLine interface
	//~ public GStand getStand () {return stand;}	// fc + op - 3.12.2007
	public int getBillBookRotationOrder () {return stand.getDate ();}	// fc + op - 3.12.2007
	public int getBillBookYear () {return ((BillBookCompatible) stand).getPlantationAge ();}
	public String getBillBookOperation () {return billBookOperation;}
	public String getBillBookDetail () {return billBookDetail;}
	public String getBillBookType () {return billBookType;}
	public double getBillBookQuantity () {return getQuantityInPreferredUnit ();}
	public String getBillBookQuantityUnit () {return getPreferredUnit ();}
	public double getBillBookUnitPrice () {return unitPrice;}
	public double getBillBookTotalFuelConsumption () {return totalFuelConsumption;}
	// BillBookLine interface

	public Producer getProducer () {return producer;}	// fc + op - 4.12.2007
	public String getInputProductName () {return inputProductName;}	// fc + op - 4.12.2007

	public String toString () {
		return getName ();
	}

}



