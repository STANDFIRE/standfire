/* 
 * Capsis - Computer-Aided Projections of Strategies in Silviculture
 * 
 * Copyright (C) 2015 LERFoB AgroParisTech/INRA 
 * 
 * Authors: M. Fortin, 
 * 
 * This file is part of Capsis
 * Capsis is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * Capsis is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU lesser General Public License
 * along with Capsis.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package capsis.util.extendeddefaulttype.dataextractor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jeeb.lib.util.Log;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;
import capsis.extension.dataextractor.DEMultiTimeX;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.MethodProvider;
import capsis.util.extendeddefaulttype.ExtModel;
import capsis.util.extendeddefaulttype.methodprovider.PeriodicAnnualIncrementComponentsProvider;
import capsis.util.extendeddefaulttype.methodprovider.PeriodicAnnualIncrementComponentsProvider.GrowthComponent;
import capsis.util.extendeddefaulttype.methodprovider.PeriodicAnnualIncrementComponentsProvider.Variable;

/**
 * This data extractor provides the periodic annual increment component of the basal area growth.
 * @author Mathieu Fortin - July 2010
 */
public class DEIncrementComponents extends DEMultiTimeX {

	private static enum MessageID implements TextableEnum {
		yLabelG("PAI (m2/", "AAP (m2/"),
		yLabelN("PAI (stems/", "AAP (arbres/"),
		yLabelV("PAI (m3/", "AAP (m3/"),
		xLabel("Time (yr)", "Temps (ann\u00E9es)"),
		yLabel2("yr)","an)"),
		ExtractorName("Periodic annual increment / Time", 
				"Accroissement annuel p\u00E9riodique / Temps"),
		Description("Periodic annual increment in basal area in terms of mortality, survivor growth and recruitment",
				"Accroissement annuel p\u00E9riodique en surface terri\u00E8re provenant de la mortalit\u00E9, de l'accroissement des survivants et du recrutement"),
		Variable("Variable", "Variable");

		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
	}
	
	public static final String AUTHOR = "M. Fortin";
	public static final String VERSION = "1.0";

	static public boolean matchWith (Object referent) {
		try {
			if (!(referent instanceof ExtModel)) {return false;}
			ExtModel m = (ExtModel) referent;
			MethodProvider mp = m.getMethodProvider ();
			if (!(mp instanceof PeriodicAnnualIncrementComponentsProvider)) {return false;}

		} catch (Exception e) {
			Log.println (Log.ERROR, "DEBasalAreaGrowthComponents.matchWith ()", "Error in matchWith () (returned false)", e);
			return false;
		}

		return true;
	}

	@Override
	protected String getYLabel() {
		Variable var = findSelectedVariable();
		switch(var) {
		case N:
			return REpiceaTranslator.getString(MessageID.yLabelN);
		case G:
			return REpiceaTranslator.getString(MessageID.yLabelG);
		case V:
			return REpiceaTranslator.getString(MessageID.yLabelV);
		default:
			return "";
		}
	}

	public String getDescription() {
		return REpiceaTranslator.getString(MessageID.Description);
	}
	
	@Override
	public void setConfigProperties () {
		addConfigProperty(HECTARE);
		LinkedList<String> list = new LinkedList<String>();
		for (Variable var : Variable.values()) {
			list.add(var.toString());
		}
		addRadioProperty(list.toArray(new String[]{}));
	}

	@Override
	protected List<Object> getTypes(GScene stand) {
		List<Object> outputList = new ArrayList<Object>();
		for (GrowthComponent component : GrowthComponent.values()) {
			outputList.add(component);
		}
		return outputList;
	}

	
	@Override
	public List<String> getAxesNames () {
		List<String> v = new ArrayList<String> ();
		v.add(REpiceaTranslator.getString(MessageID.xLabel));
		
		if (settings.perHa) {
			v.add (getYLabel() + "ha/" + REpiceaTranslator.getString(MessageID.yLabel2));
		} else {
			v.add (getYLabel() + REpiceaTranslator.getString(MessageID.yLabel2));
		}
		return v;
	}

	@Override
	protected Number getValue(GModel m, GScene stand, int typeId, Object type) {
		double areaFactor = 1d;
		if (settings.perHa) {
			areaFactor = 10000d / stand.getArea();
		}

		Variable var = findSelectedVariable();
		
		PeriodicAnnualIncrementComponentsProvider provider = (PeriodicAnnualIncrementComponentsProvider) m.getMethodProvider();
		
		return provider.getPAIComponents(stand, (GrowthComponent) type, var) * areaFactor;
	}

	@Override
	public String getName() {
		return REpiceaTranslator.getString(MessageID.ExtractorName);
	}

	
	private Variable findSelectedVariable() {
		Variable var;
		if (isSet(Variable.N.toString())) {
			var = Variable.N;
		} else if (isSet(Variable.G.toString())) {
			var = Variable.G;
		} else {
			var = Variable.V;
		}
		return var;
	}
	
}
