/*
 * This file is part of the LERFoB modules for Capsis4.
 *
 * Copyright (C) 2009-2014 UMR 1092 (AgroParisTech/INRA) 
 * Contributors Jean-Francois Dhote, Patrick Vallet,
 * Jean-Daniel Bontemps, Fleur Longuetaud, Frederic Mothe,
 * Laurent Saint-Andre, Ingrid Seynave, Mathieu Fortin.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package capsis.extension.intervener;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Random;

import jeeb.lib.util.Log;
import jeeb.lib.util.Settings;
import jeeb.lib.util.Translator;
import jeeb.lib.util.annotation.Ignore;
import jeeb.lib.util.annotation.RecursiveParam;
import repicea.gui.REpiceaShowableUI;
import capsis.defaulttype.TreeCollection;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.kernel.Step;
import capsis.kernel.automation.Automatable;
import capsis.kernel.extensiontype.Intervener;
import capsis.util.methodprovider.GCoppiceProvider;
import capsis.util.methodprovider.RDIProvider;
import capsis.util.methodprovider.RandomGeneratorProvider;

/**
 * Create a Thinner using the rdi, and a thinning coefficient using an algorithm by G. LeMoguedec
 * @author F. Mothe, G. LeMoguedec - may 2010
 */
public class RdiAutoThinner implements Intervener, Automatable, REpiceaShowableUI {

    static public String NAME = "RdiAutoThinner";
    static public String VERSION = "1.0";
    static public String AUTHOR = "F. Mothe, G. LeMoguedec";
    static public String DESCRIPTION = "RdiAutoThinner.description";
    static public String SUBTYPE = "SelectiveThinner";
    
	public static final double DEFAULT_THINNING_COEF = -0.5;


    static {
        Translator.addBundle("capsis.extension.intervener.RdiAutoThinner");
    }

    public static class RDIAutoThinnerSettings implements Serializable, Automatable {

        private static final long serialVersionUID = 20100223L;
        
        private double objectiveRdi;
        private double thinningCoef;
        private double coppiceBasalAreaM2; //TB 2012 coppice basal area support for FTChene

        public RDIAutoThinnerSettings(double objectiveRdi) {
            this(objectiveRdi, DEFAULT_THINNING_COEF);
        }

        public RDIAutoThinnerSettings(double objectiveRdi, double thinningCoef) {
            this.objectiveRdi = objectiveRdi;
            this.thinningCoef = thinningCoef;
        }
        
        public double getObjectiveRdi() {return objectiveRdi;}
        public double getThinningCoefficient() {return thinningCoef;}
        
        public void setObjectiveRdi(double objectiveRdi) {this.objectiveRdi = objectiveRdi;}
        public void setThinningCoefficient(double thinningCoef) {this.thinningCoef = thinningCoef;}

		public void setCoppiceBasalAreaM2(double value) {this.coppiceBasalAreaM2 = value;}

		protected double getCoppiceBasalAreaM2() {return coppiceBasalAreaM2;}

    }
    
    @Ignore
    protected GModel model;
    @Ignore
    protected GScene stand;
    @Ignore
    protected boolean ready = false;
    
    /**
     * Thinning parameters (public for Automation only, should be private).
     */
    @RecursiveParam
    private RDIAutoThinnerSettings settings = null;	// should be private
    @Ignore
    private Random m_random;
    
    private final boolean scriptMode;

    private transient RdiAutoThinnerDialog guiInterface;
    
    
    protected RdiAutoThinnerScoredTreeCollection scoredTreeCollection;

    /**
     * Phantom constructor for GUI implementation.
     * Only to ask for extension properties (authorName, version...).
     */
    public RdiAutoThinner () {
    	scriptMode = false;
    }
    
    /**
     * Constructor for script implementation.
     * @param settings a RDIAutoTHinnerSettings instance
     */
    public RdiAutoThinner(RDIAutoThinnerSettings settings) {
    	scriptMode = true;
        this.settings = settings;
    }

    
    /**
     * Extension dynamic compatibility mechanism. This matchWith() method checks if the extension can deal (i.e. is compatible) with the referent.
     */
    static public boolean matchWith(Object referent) {
        try {
            if (!(referent instanceof GModel)) {
                return false;
            }
            GModel m = (GModel) referent;
            GScene s = ((Step) m.getProject().getRoot()).getScene();
            if (!(s instanceof TreeCollection)) {
                return false;
            }
            MethodProvider mp = m.getMethodProvider();
            if (!(mp instanceof RDIProvider)) {
                return false;
            }
        }
        catch (Exception e) {
            Log.println(Log.ERROR, "RdiAutoThinner.matchWith ()", "Error in matchWith () (returned false)", e);
            return false;
        }
        return true;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void init(GModel m, Step s, GScene scene, Collection c) {

        model = m;
        stand = scene;
        
        MethodProvider mp = model.getMethodProvider();
        if (mp instanceof RandomGeneratorProvider) {
        	m_random = ((RandomGeneratorProvider) mp).getRandomGenerator(model);
        } else {
        	m_random = new Random();
        }
        
        if (settings == null) {
        	double thinningCoefficient = Settings.getProperty(getClass().getName() + ".thinningCoef", DEFAULT_THINNING_COEF);
            settings = new RDIAutoThinnerSettings(1d, thinningCoefficient);
        }
        
        scoredTreeCollection = new RdiAutoThinnerScoredTreeCollection(model, stand, m_random);
        if (scriptMode) {		// scores must be set no further possibilities of changing the parameters
            getFutureStandCharacteristics(settings);
        }
    }

    /**
     * From Intervener.
     */
    @Override
    public boolean initGUI() {
    	showUI();
        ready = getUI().isValidDialog();
        if (ready) {
            Settings.setProperty(getClass().getName() + ".thinningCoef", settings.thinningCoef);
        }
        getUI().dispose();
        return ready;
    }

    @Override
    public void activate() {}

    /**
     * From Intervener. Control input parameters.
     */
    @Override
    public boolean isReadyToApply() {return ready;}

    /**
     * From Intervener. Makes the action : thinning.
     */
    @Override
    public Object apply() throws Exception {
        boolean thinned = scoredTreeCollection.thinForRdi();
        //TB 2012 coppice basal area support for models with non individualized coppice G like FTChene
        MethodProvider mp = model.getMethodProvider();
        if (mp instanceof GCoppiceProvider && settings.getCoppiceBasalAreaM2() >= 0d) {
            ((GCoppiceProvider) mp).setCoppiceG(stand, settings.getCoppiceBasalAreaM2());
            thinned = true;
        }
        if (thinned) {
            stand.setInterventionResult(true);
        }
        return stand;
    }

    /**
     * This method is a script implementation of RdiAutoThinner. It creates a
     * RdiAutoThinner instance, determines the trees to be harvested and then proceeds
     * to the harvest. 
     * @param stand a GScene instance
     * @param model a GModel instance 
     * @param data a Data instance
     * @return true if trees were harvested or false otherwise
     */
    public static boolean thin(GScene stand, GModel model, RDIAutoThinnerSettings data) {
    	RdiAutoThinner rat = new RdiAutoThinner(data);
    	rat.init(model, null, stand, null);
    	boolean thinned = rat.isReadyToApply();
    	if (thinned) {
        	try {
        		rat.apply();
    		} catch (Exception e) {
    			e.printStackTrace();
    			throw new InvalidParameterException("The apply method in the RdiAutoThinner has failed!");
    		}
    	}
		return thinned;
    }
    
    /**
     * This method determines all the trees to be harvested according to the target rdi and the thinning coefficient as specified
     * in the Data instance. If no tree is to be harvested, the isReadyToApply method will return false.
     * @return a RdiAutoThinnerStandData instance that contains the future stand characteristics
     */
    protected RdiAutoThinnerStandData getFutureStandCharacteristics(RDIAutoThinnerSettings settings) {
    	RdiAutoThinnerStandData futureStand = scoredTreeCollection.getStandCharacteristics(settings);
    	if (futureStand.getN() < scoredTreeCollection.getInitialStandCharacteristics().getN()) {
    		ready = true;
    	} else {
    		ready = false;
    	}
    	return futureStand;
    }

    /**
     * Return a clone of random by serialising/deserialising
     */
    // TODO : il y a surement plus simple...
    public static Random cloneRandomGenerator(Random random) {
        Random newRandom;
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
            oos.writeObject(random);

            java.io.ByteArrayInputStream bais =
                    new java.io.ByteArrayInputStream(baos.toByteArray());
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
            newRandom = (Random) ois.readObject();
        }
        catch (Exception e) {
            newRandom = null;
            Log.println(Log.ERROR, "RdiAutoThinner.cloneRandomGenerator ()", "Clonage failed", e);
        }
        return newRandom;
    }

    /**
     * Human readable text info.
     */
    public String getInfo() {
        String s = "RdiAutoThinner";
        if (settings != null) {
            s += " to rdi=" + settings.objectiveRdi + " coef=" + settings.thinningCoef;
        }
        else {
            s += "(null data)";
        }
        return s;
    }

	@Override
	public RdiAutoThinnerDialog getUI() {
		if (guiInterface == null) {
			guiInterface = new RdiAutoThinnerDialog(this);
		}
		return guiInterface;
	}

	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

	@Override
	public void showUI() {
		getUI().show();
	}

	protected void setSettings(RDIAutoThinnerSettings newSettings) {
		this.settings = newSettings;
	}
	
	/**
	 * This method returns the settings of the thinner, namely the target rdi and the thinning coefficient.
	 * @return a RDIAutoThinnerSettings instance
	 */
	public RDIAutoThinnerSettings getSettings() {return settings;}
	
}
