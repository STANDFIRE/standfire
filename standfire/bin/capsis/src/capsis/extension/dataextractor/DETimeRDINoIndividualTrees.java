package capsis.extension.dataextractor;

import capsis.app.CapsisExtensionManager;
import capsis.defaulttype.TreeCollection;
import capsis.extension.AbstractDataExtractor;
import capsis.extension.dataextractor.format.DFCurves;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.util.methodprovider.DgProvider;
import capsis.util.methodprovider.NProvider;
import capsis.util.methodprovider.RDIProvider;
import capsis.util.methodprovider.RdiProviderEnhanced.RdiTool;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jeeb.lib.defaulttype.PaleoExtension;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;

public class DETimeRDINoIndividualTrees extends AbstractDataExtractor implements DFCurves, PaleoExtension {

    private static String className = "DETimeRDINoIndividualTrees";
    private List<List<String>> labels;
    protected List<List<? extends Number>> curves;
    private GModel model;

    
     static {
        Translator.addBundle("capsis.extension.dataextractor." + className);
    }

    /**
     * Phantom constructor. Only to ask for extension properties (authorName, version...).
     */
    public DETimeRDINoIndividualTrees() {
    }

    /**
     * Official constructor. It uses the standard Extension starter.
     */
    public DETimeRDINoIndividualTrees(GenericExtensionStarter s) {
        try {
            init(s.getModel(), s.getStep());
            curves = new ArrayList<List<? extends Number>>();
            labels = new ArrayList<List<String>>();
        }
        catch (Exception e) {
            Log.println(Log.ERROR, className + "()", "Exception occured during object construction : ", e);
        }
    }

    /**
     * Extension dynamic compatibility mechanism. This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
     */
    public boolean matchWith(Object referent) {
        try {
            if (!(referent instanceof GModel)) {
                return false;
            }
            GModel m = (GModel) referent;
            MethodProvider mp = m.getMethodProvider();
            if (!(mp instanceof RDIProvider)) {
                return false;
            }
            if (!(mp instanceof NProvider)) {
                return false;
            }
            if (!(mp instanceof DgProvider)) {
                return false;
            }
            GScene s = ((Step) m.getProject().getRoot()).getScene();
            if ((s instanceof TreeCollection)) {
                return false;
            }
        }
        catch (Exception e) {
            Log.println(Log.ERROR, className + ".matchWith ()", "Error in matchWith () (returned false)", e);
            return false;
        }
        return true;
    }

    @Override
    public void setConfigProperties() {
        //TODO
        // Choose configuration properties
    }

    public boolean doExtraction() {
        if (upToDate) {
            return true;
        }
        if (step == null) {
            return false;
        }

        // Retrieve method provider
        model = step.getProject().getModel();
        MethodProvider methodProvider = model.getMethodProvider();

        try {
            // Retrieve Steps from root to this step
            ArrayList<Step> steps = new ArrayList<Step>(step.getProject().getStepsFromRoot(step));
            ArrayList<Number> x = new ArrayList();					// x coordinates (years)
            ArrayList<Number> y1 = new ArrayList();					// y coordinates (Rdi)
            // Data extraction : points with (Integer, Double) coordinates
            for (Iterator i = steps.iterator(); i.hasNext();) {
                Step s = (Step) i.next();
                GScene stand = s.getScene();
                int year = stand.getDate();
                x.add(new Integer(year));
                double nha = ((NProvider) methodProvider).getN(stand, null) / (stand.getArea() / 10000d);
                double dg = ((DgProvider) methodProvider).getDg(stand, null);
                double rdi = 0;
                rdi = RdiTool.getRdiDependingOnMethodProviderInstance(stand, model, nha, dg);
                y1.add(new Double(rdi));
            }
            curves.clear();
            curves.add(x);
            curves.add(y1);
            labels.clear();
            labels.add(new ArrayList());		// no x labels
            ArrayList y1Labels = new ArrayList();
            y1Labels.add("RDI");
            labels.add(y1Labels);			// y1 : label "Rdi"

        }
        catch (Exception exc) {
            Log.println(Log.ERROR, "DETimeRdi.doExtraction ()", "Exception caught : ", exc);
            return false;
        }

        upToDate = true;
        return true;
    }

    public String getName() {
        return Translator.swap(className);
    }

    public List<List<? extends Number>> getCurves() {
        return curves;
    }

    public List<List<String>> getLabels() {
        return labels;
    }

    public List<String> getAxesNames() {
        ArrayList<String> v = new ArrayList<String>();
        v.add(Translator.swap(className + ".xLabel"));
        v.add(Translator.swap(className + ".yLabel"));
        return v;
    }

    public int getNY() {
        return curves.size() - 1;
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
        return "T. Bronner";
    }

    /**
     * From Extension interface.
     */
    public String getDescription() {
        return Translator.swap(className + ".description");
    }

    public String getType() {
        return CapsisExtensionManager.DATA_EXTRACTOR;
    }

    public String getClassName() {
        return this.getClass().getName();
    }
}
