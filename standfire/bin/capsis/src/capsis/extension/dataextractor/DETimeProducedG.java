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
package capsis.extension.dataextractor;

import capsis.app.CapsisExtensionManager;
import capsis.extension.AbstractDataExtractor;
import capsis.extension.dataextractor.format.DFCurves;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.util.methodprovider.GProvider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jeeb.lib.defaulttype.PaleoExtension;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;

public class DETimeProducedG extends AbstractDataExtractor implements DFCurves, PaleoExtension {

    private static String className = "DETimeProducedG";
    private List<List<String>> labels;
    protected List<List<? extends Number>> curves;
    private GModel model;

    static {
        Translator.addBundle("capsis.extension.dataextractor." + className);
    }

    /**
     * Phantom constructor. Only to ask for extension properties (authorName, version...).
     */
    public DETimeProducedG() {
    }

    /**
     * Official constructor. It uses the standard Extension starter.
     */
    public DETimeProducedG(GenericExtensionStarter s) {
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
            if (!(mp instanceof GProvider)) {
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
        MethodProvider mp = model.getMethodProvider();

        try {
            // Retrieve Steps from root to this step
            ArrayList<Step> steps = new ArrayList<Step>(step.getProject().getStepsFromRoot(step));
            ArrayList<Number> x = new ArrayList();					// x coordinates (years)
            ArrayList<Number> y1 = new ArrayList();					// y coordinates (Rdi)
            // Data extraction : points with (Integer, Double) coordinates
            double totalG = 0d;
            double thinnedG = 0d;
            for (Iterator i = steps.iterator(); i.hasNext();) {
                Step s = (Step) i.next();
                GScene stand = s.getScene();
                int year = stand.getDate();
                x.add(new Integer(year));
                if (stand.isInterventionResult()) {
                    thinnedG += totalG - ((GProvider) mp).getG(stand, null);
                }
                totalG = ((GProvider) mp).getG(stand, null);
                y1.add(new Double(thinnedG));
            }
            curves.clear();
            curves.add(x);
            curves.add(y1);
            labels.clear();
            labels.add(new ArrayList());		// no x labels
            ArrayList y1Labels = new ArrayList();
            y1Labels.add("G");
            labels.add(y1Labels);			// y1 : label "G"

        }
        catch (Exception exc) {
            Log.println(Log.ERROR, className + ".doExtraction ()", "Exception caught : ", exc);
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
