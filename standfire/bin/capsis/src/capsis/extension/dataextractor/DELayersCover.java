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

import java.util.List;
import java.util.Vector;

import jeeb.lib.util.Log;
import jeeb.lib.util.Translator;
import capsis.extension.PaleoDataExtractor;
import capsis.kernel.GModel;
import capsis.kernel.MethodProvider;
import capsis.kernel.extensiontype.GenericExtensionStarter;
import capsis.util.methodprovider.CoverProvider;


/**
 * Cover for each canopy layer and total plot.
 *
 * @author B. Courbaud - june 2001
 */
public class DELayersCover extends DETimeG {
	private Vector labels;

	static {
		Translator.addBundle("capsis.extension.dataextractor.DELayersCover");
	}


	/**
	 * Phantom constructor.
	 * Only to ask for extension properties (authorName, version...).
	 */
	public DELayersCover () {}


	/**
	 * Official constructor. It uses the standard Extension starter.
	 */
	public DELayersCover (GenericExtensionStarter s) {
		//~ this (s.getStep ());
	//~ }


	//~ /**
	 //~ * Functional constructor.
	 //~ */
	//~ protected DELayersCover (Step stp) {
		super (s);
		labels = new Vector ();
	}


	/**
	 * Extension dynamic compatibility mechanism.
	 * This matchwith method checks if the extension can deal (i.e. is compatible) with the referent.
	 */
	public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof GModel)) {return false;}
			GModel m = (GModel) referent;

			MethodProvider mp = m.getMethodProvider ();
			if (!(mp instanceof CoverProvider)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "DELayersCover.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}


	/**
	 * This method is called by superclass DataExtractor.
	 */
	public void setConfigProperties () {
		// Choose configuration properties
		addConfigProperty (PaleoDataExtractor.HECTARE);
		addConfigProperty (PaleoDataExtractor.PERCENTAGE);
	}


	/**
	 * From DataExtractor SuperClass.
	 *
	 * Computes the data series. This is the real output building.
	 * It needs a particular Step.
	 *
	 * Return false if trouble while extracting.
	 */
	public boolean doExtraction () {
		if (upToDate) {return true;}
		if (step == null) {return false;}

		//System.out.println ("DELayersCover : extraction being made");

		try {
			// per Ha computation
			double coefHa = 1;
			if (!settings.percentage && settings.perHa) {
				coefHa = 10000 / step.getScene ().getArea ();
			}

			Vector c1 = new Vector ();		// x coordinates
			Vector c2 = new Vector ();		// y coordinates
			Vector l1 = new Vector ();		// labels for x axis (ex: 0-10, 10-20...)

			methodProvider = step.getProject ().getModel ().getMethodProvider ();
			Vector coverStatus = ((CoverProvider) methodProvider).getCover (step.getScene ());

			double totalCover = ((Double) coverStatus.get (0)).doubleValue ();
			double layer4Cover = ((Double) coverStatus.get (1)).doubleValue ();
			double layer3Cover = ((Double) coverStatus.get (2)).doubleValue ();
			double layer2Cover = ((Double) coverStatus.get (3)).doubleValue ();
			double layer1Cover = ((Double) coverStatus.get (4)).doubleValue ();

			if (settings.percentage) {
				totalCover = totalCover*100/step.getScene ().getArea ();
				layer4Cover = layer4Cover*100/step.getScene ().getArea ();
				layer3Cover = layer3Cover*100/step.getScene ().getArea ();
				layer2Cover = layer2Cover*100/step.getScene ().getArea ();
				layer1Cover = layer1Cover*100/step.getScene ().getArea ();
			}

			// Consider restriction to one particular group if needed
			//GPlot plot = step.getStand ().getPlot ();

			c1.add (new Integer (1));
			c1.add (new Integer (2));
			c1.add (new Integer (3));
			c1.add (new Integer (4));
			c1.add (new Integer (5));

			c2.add (new Double (totalCover*coefHa));
			c2.add (new Double (layer4Cover*coefHa));
			c2.add (new Double (layer3Cover*coefHa));
			c2.add (new Double (layer2Cover*coefHa));
			c2.add (new Double (layer1Cover*coefHa));

			l1.add (Translator.swap ("DELayersCover.total"));
			l1.add (Translator.swap ("DELayersCover.layer4"));
			l1.add (Translator.swap ("DELayersCover.layer3"));
			l1.add (Translator.swap ("DELayersCover.layer2"));
			l1.add (Translator.swap ("DELayersCover.layer1"));

			curves.clear ();
			curves.add (c1);
			curves.add (c2);

			labels.clear ();
			labels.add (l1);

		} catch (Exception exc) {
			Log.println (Log.ERROR, "DELayersCover.doExtraction ()", "Exception caught : ",exc);
			return false;
		}

		upToDate = true;
		return true;
	}


	/**
	 * From DataFormat interface.
	 */
	public String getName() {
		return getNamePrefix ()+Translator.swap ("DELayersCover");
	}


	/**
	 * From DFCurves interface.
	 */
	public List<String> getAxesNames () {
		Vector v = new Vector ();
		v.add (Translator.swap ("DELayersCover.xLabel"));
		if (settings.percentage) {
			v.add (Translator.swap ("DELayersCover.yLabel")+" (% "+Translator.swap ("DELayersCover.surface")+")");
		} else if (settings.perHa) {
			v.add (Translator.swap ("DELayersCover.yLabel")+" (ha)");
		} else {
			v.add (Translator.swap ("DELayersCover.yLabel"));
		}
		return v;
	}


	/**
	 * From DFCurves interface.
	 */
	public List<List<String>> getLabels () {
		return labels;
	}


	/**
	 * From Extension interface.
	 */
	public String getVersion () {return VERSION;}

	public static final String VERSION = "1.0";


	/**
	 * From Extension interface.
	 */
	public String getAuthor () {return "B. Courbaud";}


	/**
	 * From Extension interface.
	 */
	public String getDescription () {return Translator.swap ("DELayersCover.description");}


}







