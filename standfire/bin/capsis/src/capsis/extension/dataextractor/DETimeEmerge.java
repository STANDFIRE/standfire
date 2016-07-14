/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package capsis.extension.dataextractor;

import capsis.defaulttype.Tree;
import capsis.extension.AbstractDataExtractor;
import capsis.extension.dataextractor.format.DFCurves;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Step;
import capsis.lib.emerge.EmergeDB;
import capsis.lib.emerge.UseEmergeDB;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;

public class DETimeEmerge extends AbstractDataExtractor implements DFCurves {

    static {
        Translator.addBundle("capsis.extension.dataextractor.DETimeEmerge");
    }
    public static final String AUTHOR = "T. Bronner";
    public static final String VERSION = "1.0";
    public static final String NAME = Translator.swap("DETimeEmerge");
    public static final String DESCRIPTION = Translator.swap("DETimeEmerge.description");
    private EmergeDB emergeDb;
    private boolean verboseSQL = true;
    private StringBuilder mainSQL, remainingSQL;
    protected List<List<? extends Number>> curves;
    protected List<List<String>> curvesLabels;

    /**
     * Extension dynamic compatibility mechanism. This matchwith method checks
     * if the extension can deal (i.e. is compatible) with the referent.
     */
    static public boolean matchWith(Object referent) {
        return referent instanceof UseEmergeDB;
    }

    public boolean isVerboseSQL() {
        return verboseSQL;
    }

    public void setVerboseSQL(boolean verboseSQL) {
        this.verboseSQL = verboseSQL;
    }

    @Override
    public void setConfigProperties() { /*TODO translations*/
        /* TODO translations
         */
        GModel m = this.step.getProject().getModel();
        /*
        /*get Emerge database*/


        if (m instanceof UseEmergeDB) {
            emergeDb = ((UseEmergeDB) m).getEmergeDB();
        }
        String[] stringArray;
        List list;
        if (emergeDb != null) {
            /*
             * Get available tree compartments
             */
            HashMap<Object, Object> items = emergeDb.getAvailableItems();
            /* Add tree group feature */
            addConfigProperty(AbstractDataExtractor.TREE_GROUP);
            /*Add boolean features*/
            String[] aliveSet = new String[]{Translator.swap("TRUE"), Translator.swap("FALSE"), Translator.swap("ALL")};
            addSetProperty("ALIVE", aliveSet, aliveSet);

            for (Iterator<Object> it = items.keySet().iterator(); it.hasNext();) {
                String key = (String) it.next();
                stringArray = null;/*
                 * dont forget to reset the destination array !
                 */
                list = (List) items.get(key);
                if (key.equals("COMPONENT")) {
                    list.add("ABOVEGROUND");
                    list.add("CROWN");
                }
                else if (key.equals("SUB_COMPONENT")) {
                    /* remove "_" causing traduction bug  */
                    key = key.replace("_", "");
                    /* dont show "NA"   */
                    list.removeAll(Arrays.asList(new String[]{"NA"}));
                    /* Add "all" criterium */
                    list.add("ALL");
                }
                else if (key.equals("ROOT_SUB_COMPONENT")) {
                    /* remove "_" causing traduction bug  */
                    key = key.replace("_", "");
                    /* dont show "NA"   */
                    list.removeAll(Arrays.asList(new String[]{"NA"}));
                    /* Add "all" criterium */
                    list.add("ALL");
                }
                else if (key.equals("COMMERCIAL_WOOD")) {
                    /* remove "_" causing traduction bug  */
                    key = key.replace("_", "");
                    /* dont show null */
                    list.removeAll(Arrays.asList(new String[]{"NULL"}));
                    /* Add "all" criterium */
                    list.add("ALL");
                }
                else if (key.equals("GROWTH_UNIT") || key.equals("RING") || key.equals("COHORT_AGE")) {
                    /* remove "_" causing traduction bug  */
                    key = key.replace("_", "");
                    /* dont show -1  */
                    list.removeAll(Arrays.asList(new String[]{"-1"}));
                    /* Add "all" criterium */
                    list.add("ALL");
                }
                else if (key.equals("ROOT_DIAMETER_CLASS") || key.equals("BRANCH_DIAMETER_CLASS") || key.equals("CROWN_HEIGHT") || key.equals("SOIL_DEPTH")) {
                    /* remove "_" causing traduction bug  */
                    key = key.replace("_", "");
                    /* these values must be taken 2 by 2*/
                    if (list.size() % 2 == 0) {/* list size should be multiple of 2*/
                        ArrayList<String> values = new ArrayList<String>();
                        for (Iterator it1 = list.iterator(); it1.hasNext();) {
                            String lowerBound = (String) it1.next();
                            String upperBound = (String) it1.next();
                            if (!lowerBound.equals("-1") || !upperBound.equals("-1")) {
                                if (lowerBound.equals("-1")) {
                                    values.add("<=" + upperBound);
                                }
                                else if (upperBound.equals("-1")) {
                                    values.add(">=" + lowerBound);
                                }
                                else {
                                    values.add(lowerBound + " - " + upperBound);
                                }
                            }
                        }
                        list = values;
                    }
                    list.add("ALL");
                }
                if (!list.isEmpty()) {
                    stringArray = Arrays.copyOf(list.toArray(), list.toArray().length, String[].class);
                    /*translate*/
                    for (int i = 0; i < stringArray.length; i++) {
                        stringArray[i] = Translator.swap(stringArray[i]);
                    }
                    addSetProperty(key, stringArray, stringArray);
                }
            }
        }
    }

    @Override
    public boolean doExtraction() {
        final String methodName = "DETEmerge.doExtraction()";
        if (upToDate) {
            return true;
        }
        if (step == null) {
            return false;
        }
        /*
         * init data and label collections
         */
        curves = new ArrayList<List<? extends Number>>();
        curvesLabels = new ArrayList<List<String>>();
        String diameterLB = null, diameterUB = null, depthLB = null, depthUB = null, heightLB = null, heightUB = null, aliveString = null;

        if (emergeDb != null) {
            /*
             * prepare x labels
             */
            ArrayList<Integer> dates = new ArrayList<Integer>();
            /*
             * Get steps from current step
             */
            Collection steps = step.getProject().getStepsFromRoot(step);
            Set masses = getSetProperty("MASS");
            Set components = getSetProperty("COMPONENT");
            /*
             * prepare main SQL statement
             */
            mainSQL = new StringBuilder("SELECT ");
            /*
             * Prepare masses sum
             */
            for (Iterator it = masses.iterator(); it.hasNext();) {
                /* iterate masses */
                String mass = (String) it.next();
                if (mass.equals(Translator.swap("BIOMASS"))) {
                    mass = "BIOMASS";
                }
                mainSQL.append(" SUM(").append(mass.toUpperCase()).append(")/1000 AS ").append(mass.toUpperCase()); /*mass in tons*/
                if (it.hasNext()) {
                    mainSQL.append(",");
                }
            }
            /*
             * set table name
             */
            mainSQL.append(" FROM ").append(emergeDb.getTableName());
            mainSQL.append(" WHERE TRUE");
            /*
             * manage alive criteria
             */
            Set alive = getSetProperty("ALIVE");
            if (alive.contains(Translator.swap("TRUE")) && !alive.contains(Translator.swap("FALSE"))) {
                mainSQL.append(" AND ALIVE=TRUE");
                aliveString = Translator.swap("ALIVE");
            }
            else if (alive.contains(Translator.swap("FALSE")) && !alive.contains(Translator.swap("TRUE"))) {
                mainSQL.append(" AND ALIVE=FALSE");
                aliveString = Translator.swap("DEAD");
            }

            mainSQL.append(" AND (");
            for (Iterator it = steps.iterator(); it.hasNext();) {
                Step stp = (Step) it.next();
                /*get step date*/
                dates.add(stp.getScene().getDate());
                /*get step name*/
                mainSQL.append(" ( STEP_NAME ='").append(stp.getName()).append("' AND TREE_ID IN (");
                /*get filtered tree ids for this step*/
                for (Iterator it1 = doFilter(stp.getScene()).iterator(); it1.hasNext();) {
                    Tree tree = (Tree) it1.next();
                    mainSQL.append(tree.getId());
                    if (it1.hasNext()) {
                        mainSQL.append(",");
                    }
                }
                mainSQL.append("))");
                if (it.hasNext()) {
                    mainSQL.append(" OR ");
                }
            }
            mainSQL.append(" )");


            /*
             * setup X axis
             */
            curves.add(dates);
            curvesLabels.add(new ArrayList<String>());

            /*
             * Get ROOT items corresponding to criteria
             */
            if (components.contains(Translator.swap("ROOT"))) {
                /* Get elements to retrieve */
                Set setSubComponent = getSetProperty("ROOTSUBCOMPONENT");
                Set diameter = getSetProperty("ROOTDIAMETERCLASS");
                Set depth = getSetProperty("SOILDEPTH");
                /*
                 * prepare component specific criteria
                 */
                for (Iterator it2 = setSubComponent.iterator(); it2.hasNext();) {/*
                     * iterate sub components
                     */
                    String subCString = (String) it2.next();
                    for (Iterator it1 = diameter.iterator(); it1.hasNext();) {/*
                         * iterate cohortAge classes
                         */
                        String diamString = (String) it1.next();
                        if (diamString.startsWith(">=")) {
                            diameterLB = diamString.replace(">=", "").trim();
                            diameterUB = "-1";
                        }
                        else if (diamString.startsWith("<=")) {
                            diameterLB = "-1";
                            diameterUB = diamString.replace("<=", "").trim();
                        }
                        else if (diamString.contains("-")) {
                            String[] s = diamString.split("-");
                            if (s.length == 2) {
                                diameterLB = s[0].trim();
                                diameterUB = s[1].trim();
                            }
                        }
                        for (Iterator it = depth.iterator(); it.hasNext();) {
                            String depthString = (String) it.next();
                            if (depthString.startsWith(">=")) {
                                depthLB = depthString.replace(">=", "").trim();
                                depthUB = "-1";
                            }
                            else if (depthString.startsWith("<=")) {
                                depthLB = "-1";
                                depthUB = depthString.replace("<=", "").trim();
                            }
                            else if (depthString.contains("-")) {
                                String[] s = depthString.split("-");
                                if (s.length == 2) {
                                    depthLB = s[0].trim();
                                    depthUB = s[1].trim();
                                }
                            }
                            /*
                             * Construct remaining of the query
                             */
                            remainingSQL = new StringBuilder();
                            remainingSQL.append(" AND COMPONENT='ROOT' ");
                            if (!subCString.equals(Translator.swap("ALL"))) {
                                remainingSQL.append(" AND SUB_COMPONENT='").append(subCString).append("'");
                            }
                            if (!diamString.equals(Translator.swap("ALL"))) {
                                remainingSQL.append(" AND DIAMETER_CLASS_LOWER_BOUND=").append(diameterLB);
                                remainingSQL.append(" AND DIAMETER_CLASS_UPPER_BOUND=").append(diameterUB);
                            }
                            if (!depthString.equals(Translator.swap("ALL"))) {
                                remainingSQL.append(" AND SOIL_DEPTH_LOWER_BOUND=").append(depthLB);
                                remainingSQL.append(" AND SOIL_DEPTH_UPPER_BOUND=").append(depthUB);
                            }
                            completeExtraction("Root " + (aliveString != null ? aliveString : "") + " sub=" + subCString + " diam=" + diamString + " depth=" + depthString);
                        }
                    }
                }
            }
            /*
             * Get TREE items corresponding to criteria
             */
            if (components.contains(Translator.swap("TREE"))) {
                /*
                 * Get elements to retrieve
                 */
                Set commercialWood = getSetProperty("COMMERCIALWOOD");
                Set setSubComponent = getSetProperty("SUBCOMPONENT");
                /*
                 * prepare component specific criteria
                 */
                for (Iterator it2 = setSubComponent.iterator(); it2.hasNext();) {/*iterate sub components                     */
                    String subCString = (String) it2.next();
                    for (Iterator it = commercialWood.iterator(); it.hasNext();) {/*iterate commercial wood*/
                        String comWoodString = (String) it.next();
                        remainingSQL = new StringBuilder();
                        remainingSQL.append(" AND COMPONENT='TREE' ");
                        if (!subCString.equals(Translator.swap("ALL"))) {
                            remainingSQL.append(" AND SUB_COMPONENT='").append(subCString).append("'");
                        }
                        if (!comWoodString.equals(Translator.swap("ALL"))) {
                            remainingSQL.append(" AND COMMERCIAL_WOOD='").append(comWoodString).append("'");
                        }
                        completeExtraction("Tree " + (aliveString != null ? aliveString : "") + " SC=" + subCString + " comWood=" + comWoodString);
                    }
                }
            }
            /*
             * Get STEM items corresponding to criteria
             */
            if (components.contains(Translator.swap("STEM"))) {
                /*
                 * Get elements to retrieve
                 */
                Set commercialWood = getSetProperty("COMMERCIALWOOD");
                Set setSubComponent = getSetProperty("SUBCOMPONENT");
                Set growthUnit = getSetProperty("GROWTHUNIT");
                Set ring = getSetProperty("RING");
                /*
                 * prepare component specific criteria
                 */
                for (Iterator it2 = setSubComponent.iterator(); it2.hasNext();) {/*iterate sub components*/
                    String subCString = (String) it2.next();
                    for (Iterator it1 = growthUnit.iterator(); it1.hasNext();) {/*iterate growthUnit classes*/
                        String guString = (String) it1.next();
                        for (Iterator it = ring.iterator(); it.hasNext();) {
                            String ringString = (String) it.next();
                            for (Iterator it3 = commercialWood.iterator(); it3.hasNext();) {/*iterate commercial wood*/
                                String comWoodString = (String) it3.next();
                                /*
                                 * Construct remaining of the query
                                 */
                                remainingSQL = new StringBuilder();
                                remainingSQL.append(" AND COMPONENT='STEM' ");
                                if (!subCString.equals(Translator.swap("ALL"))) {
                                    remainingSQL.append(" AND SUB_COMPONENT='").append(subCString).append("'");
                                }
                                if (!guString.equals(Translator.swap("ALL"))) {
                                    remainingSQL.append(" AND GROWTH_UNIT=").append(guString);
                                }
                                if (!ringString.equals(Translator.swap("ALL"))) {
                                    remainingSQL.append(" AND RING=").append(ringString);
                                }
                                else {
                                    /*explicitely avoid ring data to prevent double count*/
                                    remainingSQL.append(" AND RING=-1");
                                }
                                if (!comWoodString.equals(Translator.swap("ALL"))) {
                                    remainingSQL.append(" AND COMMERCIAL_WOOD='").append(comWoodString).append("'");
                                }
                                completeExtraction("Stem " + (aliveString != null ? aliveString : "") + " sub=" + subCString + " gu=" + guString + " ring=" + ringString + " comWood=" + comWoodString);
                            }
                        }
                    }
                }
            }
            /*
             * Get BRANCH items corresponding to criteria
             */
            if (components.contains(Translator.swap("BRANCH"))) {
                /*
                 * Get elements to retrieve
                 */
                Set commercialWood = getSetProperty("COMMERCIALWOOD");
                Set setSubComponent = getSetProperty("SUBCOMPONENT");
                Set diameter = getSetProperty("BRANCH_DIAMETERCLASS");
                Set height = getSetProperty("CROWNHEIGHT");
                /*
                 * prepare component specific criteria
                 */
                for (Iterator it2 = setSubComponent.iterator(); it2.hasNext();) {/*
                     * iterate sub components
                     */
                    String subCString = (String) it2.next();
                    for (Iterator it1 = diameter.iterator(); it1.hasNext();) {/* iterate diameter classes*/
                        String diamString = (String) it1.next();
                        for (Iterator it3 = commercialWood.iterator(); it3.hasNext();) {/*iterate commercial wood*/
                            String comWoodString = (String) it3.next();
                            if (diamString.startsWith(">=")) {
                                diameterLB = diamString.replace(">=", "").trim();
                                diameterUB = "-1";
                            }
                            else if (diamString.startsWith("<=")) {
                                diameterLB = "-1";
                                diameterUB = diamString.replace("<=", "").trim();
                            }
                            else if (diamString.contains("-")) {
                                String[] s = diamString.split("-");
                                if (s.length == 2) {
                                    diameterLB = s[0].trim();
                                    diameterUB = s[1].trim();
                                }
                            }
                            for (Iterator it = height.iterator(); it.hasNext();) {
                                String heightString = (String) it.next();
                                if (heightString.startsWith(">=")) {
                                    heightLB = heightString.replace(">=", "").trim();
                                    heightUB = "-1";
                                }
                                else if (heightString.startsWith("<=")) {
                                    heightLB = "-1";
                                    heightUB = heightString.replace("<=", "").trim();
                                }
                                else if (heightString.contains("-")) {
                                    String[] s = heightString.split("-");
                                    if (s.length == 2) {
                                        heightLB = s[0].trim();
                                        heightUB = s[1].trim();
                                    }
                                }
                                /*
                                 * Construct remaining of the query
                                 */
                                remainingSQL = new StringBuilder();
                                remainingSQL.append(" AND COMPONENT='BRANCH' ");
                                if (!subCString.equals(Translator.swap("ALL"))) {
                                    remainingSQL.append(" AND SUB_COMPONENT='").append(subCString).append("'");
                                }
                                if (!diamString.equals(Translator.swap("ALL"))) {
                                    remainingSQL.append(" AND DIAMETER_CLASS_LOWER_BOUND=").append(diameterLB);
                                    remainingSQL.append(" AND DIAMETER_CLASS_UPPER_BOUND=").append(diameterUB);
                                }
                                if (!heightString.equals(Translator.swap("ALL"))) {
                                    remainingSQL.append(" AND CROWN_HEIGHT_RANGE_LOWER_BOUND=").append(heightLB);
                                    remainingSQL.append(" AND CROWN_HEIGTH_RANGE_UPPER_BOUND=").append(heightUB);
                                }
                                if (!comWoodString.equals(Translator.swap("ALL"))) {
                                    remainingSQL.append(" AND COMMERCIAL_WOOD='").append(comWoodString).append("'");
                                }
                                completeExtraction("Branch " + (aliveString != null ? aliveString : "") + " sub:" + subCString + " diam:" + diamString + " height:" + heightString + " comWood="
                                        + comWoodString);
                            }
                        }
                    }
                }
            }
            /*
             * Get STUMP items corresponding to criteria
             */
            if (components.contains(Translator.swap("STUMP"))) {
                /*
                 * Get elements to retrieve, adding a total
                 */
                Set setSubComponent = getSetProperty("SUBCOMPONENT");
                /*
                 * prepare component specific criteria
                 */
                for (Iterator it2 = setSubComponent.iterator(); it2.hasNext();) {/*
                     * iterate sub components
                     */
                    String subCString = (String) it2.next();
                    remainingSQL = new StringBuilder();
                    remainingSQL.append(" AND COMPONENT='STUMP' ");
                    if (!subCString.equals(Translator.swap("ALL"))) {
                        remainingSQL.append(" AND SUB_COMPONENT='").append(subCString).append("'");
                    }
                    completeExtraction("Stump " + (aliveString != null ? aliveString : "") + " SC=" + subCString);
                }
            }
            /*
             * Get LEAF items corresponding to criteria
             */
            if (components.contains(Translator.swap("LEAF"))) {
                /*
                 * Get elements to retrieve
                 */
                Set setSubComponent = getSetProperty("SUBCOMPONENT");
                Set cohortAge = getSetProperty("COHORTAGE");
                Set height = getSetProperty("CROWNHEIGHT");
                /*
                 * prepare component specific criteria
                 */
                for (Iterator it2 = setSubComponent.iterator(); it2.hasNext();) {/*
                     * iterate sub components
                     */
                    String subCString = (String) it2.next();
                    for (Iterator it1 = cohortAge.iterator(); it1.hasNext();) {/*
                         * iterate cohortAge classes
                         */
                        String cohortString = (String) it1.next();
                        for (Iterator it = height.iterator(); it.hasNext();) {
                            String heightString = (String) it.next();
                            if (heightString.startsWith(">=")) {
                                heightLB = heightString.replace(">=", "").trim();
                                heightUB = "-1";
                            }
                            else if (heightString.startsWith("<=")) {
                                heightLB = "-1";
                                heightUB = heightString.replace("<=", "").trim();
                            }
                            else if (heightString.contains("-")) {
                                String[] s = heightString.split("-");
                                if (s.length == 2) {
                                    heightLB = s[0].trim();
                                    heightUB = s[1].trim();
                                }
                            }
                            /*
                             * Construct remaining of the query
                             */
                            remainingSQL = new StringBuilder();
                            remainingSQL.append(" AND COMPONENT='LEAF' ");
                            if (!subCString.equals(Translator.swap("ALL"))) {
                                remainingSQL.append(" AND SUB_COMPONENT='").append(subCString).append("'");
                            }
                            if (!cohortString.equals(Translator.swap("ALL"))) {
                                remainingSQL.append(" AND COHORT_AGE=").append(cohortString);
                            }
                            if (!heightString.equals(Translator.swap("ALL"))) {
                                remainingSQL.append(" AND CROWN_HEIGHT_RANGE_LOWER_BOUND=").append(heightLB);
                                remainingSQL.append(" AND CROWN_HEIGHT_RANGE_UPPER_BOUND=").append(heightUB);
                            }
                            completeExtraction("Leaf " + (aliveString != null ? aliveString : "") + " sub:" + subCString + " cohort:" + cohortString + " height:" + heightString);
                        }
                    }
                }
            }
            /*
             * Get FRUIT items corresponding to criteria
             */
            if (components.contains(Translator.swap("FRUIT"))) {
                remainingSQL = new StringBuilder();
                remainingSQL.append(" AND COMPONENT='FRUIT' ");
                completeExtraction("Fruit " + (aliveString != null ? aliveString : ""));
            }
            /*
             * Get above ground items
             */
            if (components.contains(Translator.swap("ABOVE_GROUND"))) {
                /*
                 * Get elements to retrieve, adding a total
                 */
                Set setSubComponent = getSetProperty("SUBCOMPONENT");
                /*
                 * prepare component specific criteria
                 */
                for (Iterator it2 = setSubComponent.iterator(); it2.hasNext();) {/*
                     * iterate sub components
                     */
                    String subCString = (String) it2.next();
                    remainingSQL = new StringBuilder();
                    remainingSQL.append(" AND ABOVE_GROUND=TRUE ");
                    if (!subCString.equals(Translator.swap("ALL"))) {
                        remainingSQL.append(" AND SUB_COMPONENT='").append(subCString).append("'");
                    }
                    completeExtraction("Above Ground " + (aliveString != null ? aliveString : "") + " sub=" + subCString);
                }
            }
            /*
             * Get crown items
             */
            if (components.contains(Translator.swap("CROWN"))) {
                /*
                 * Get elements to retrieve, adding a total
                 */
                Set setSubComponent = getSetProperty("SUBCOMPONENT");
                /*
                 * prepare component specific criteria
                 */
                for (Iterator it2 = setSubComponent.iterator(); it2.hasNext();) {/*
                     * iterate sub components
                     */
                    String subCString = (String) it2.next();
                    remainingSQL = new StringBuilder();
                    remainingSQL.append(" AND CROWN=TRUE ");
                    if (!subCString.equals(Translator.swap("ALL"))) {
                        remainingSQL.append(" AND SUB_COMPONENT='").append(subCString).append("'");
                    }
                    completeExtraction("Crown " + (aliveString != null ? aliveString : "") + " sub=" + subCString);
                }
            }
        }


        return true;
    }

    /**
     * *
     * Finish extraction from the Emerge database
     *
     * @param serieNameString
     */
    private void completeExtraction(String serieNameString) {
        ArrayList<String> serieName;
        final String methodName = "DETEmerge.completeExtraction()";
        /*
         * collection for storing curve values
         */
        HashMap<String, ArrayList<Double>> values = null;
        ResultSet rs;
        /*
         * convert step name to number for ordering use also scenario letter to
         * avoid grouping of steps like 5a,*5b,*5c
         */
        remainingSQL.append(" GROUP BY CONVERT(REPLACE(LEFT(STEP_NAME,LENGTH(STEP_NAME)-1),'*'),SQL_SMALLINT),RIGHT(STEP_NAME,1)");
        remainingSQL.append(" ORDER BY CONVERT(REPLACE(LEFT(STEP_NAME,LENGTH(STEP_NAME)-1),'*'),SQL_SMALLINT),RIGHT(STEP_NAME,1)");
        /*
         * Run the contructed query
         */
        if (verboseSQL) {
            System.out.println(mainSQL.toString() + remainingSQL.toString());
        }
        rs = emergeDb.executeDQLQuery(mainSQL.toString() + remainingSQL.toString());
        /*
         * process results
         */
        if (rs != null) {
            try {
                /*
                 * Prepare destination collection with mass names
                 */
                values = new HashMap<String, ArrayList<Double>>();
                for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                    values.put(rs.getMetaData().getColumnName(i + 1), new ArrayList<Double>());
                }
                /*
                 * retrieve data
                 */
                while (rs.next()) {
                    for (Iterator<String> it3 = values.keySet().iterator(); it3.hasNext();) {
                        String massName = it3.next();
                        values.get(massName).add(rs.getDouble(massName));
                    }
                }
            }
            catch (SQLException e) {
                Log.println(Log.ERROR, methodName, e.getMessage(), e);
            }
            for (Iterator<String> it3 = values.keySet().iterator(); it3.hasNext();) {
                String massName = it3.next();
                serieName = new ArrayList<String>();
                serieName.add(serieNameString + " mass:" + massName);/*
                 * TODO TRANSLATE
                 */
                curvesLabels.add(serieName);
                if (values.containsKey(massName) && values.get(massName).size() > 0) {
                    curves.add(values.get(massName));
                }
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<List<? extends Number>> getCurves() {
        return curves;
    }

    @Override
    public List<List<String>> getLabels() {
        return curvesLabels;
    }

    @Override
    public List<String> getAxesNames() {
        return Arrays.asList(new String[]{"Date", "T/date"}); /*
         * TODO unites et translate
         */
    }

    @Override
    public int getNY() {
        return curves.size() - 1;
    }
}
