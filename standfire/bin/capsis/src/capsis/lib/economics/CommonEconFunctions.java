/*
 * Capsis 4 - Computer-Aided Projections of Strategies in Silviculture
 *
 * Copyright (C) 2000-2001  Francois de Coligny
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package capsis.lib.economics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import jeeb.lib.util.Log;
import jeeb.lib.util.MessageDialog;
import jeeb.lib.util.Node;
import jeeb.lib.util.Translator;
import capsis.extension.economicfunction.Expense;
import capsis.extension.economicfunction.Income;
import capsis.extension.intervener.EconomicIntervention;
import capsis.extensiontype.EconomicFunction;
import capsis.kernel.GModel;
import capsis.kernel.GScene;
import capsis.kernel.Project;
import capsis.kernel.Step;
/**
 * Group of functions providing economics indicators associated to steps of econstand
 * (internate rate of return, present net value,...) considering that the date of the step
 * is the end of the economic cycle (the begining is 0 or the starting date of econmodel)
 * @author Ch. Orazio - November 2003
 */
public class CommonEconFunctions {

	     /**
		 * Calculate Internate rate of return
		 * and taking into account the annual operation of econmodel from a stand [can be used with scripts]
		 * @version 1.0
		 * @author Ch. Orazio - March 2003
		 */
		 public static double standIRR (Step s) {

			 int i = 0;
			 double IRR = 0;
			 //m = s.getScenario ().getModel ();
			 while (Math.abs(standPNV (s, IRR/100))>0.00001 && i<10){
			 	if (standPNV (s, IRR/100)>0){
					IRR += Math.pow(10, -i);
				} else {
					IRR -= Math.pow(10, -i);
					i++;
				}
			 	//System.out.println (i+" Return IRR: "+ IRR + " NV : "+standPNV (step, IRR/100));
			}
			 return IRR;
		 }


		 /**
		 * Calculate present net present value of infinite serial  divided by period duration (independent from rotation period) - using  actualization rate
		 * and taking into account the annual operation of econmodel from a stand [can be used with scripts]
		 * @version 1.0
		 * @author Ch. Orazio - March 2003
		 */
		 public static double standPNVISbyD (Step step, double rate) {
			 double period = EndingDate(step)-StartingDate(step);
			 //double rate = ((EconModel) step.getScenario().getModel()).getActualizationRate()/100;
			 double PNVbyD = Double.POSITIVE_INFINITY;
			 if (period !=0) {PNVbyD = standPNVIS (step, rate)*rate;}
			 return PNVbyD;
		 }




		/**
		 * Calculate present net present value of infinite serial (independent from rotation period) - using  actualization rate
		 * and taking into account the annual operation of econmodel from a stand [can be used with scripts]
		 * @version 1.0
		 * @author Ch. Orazio - March 2003
		 */
		 public static double standPNVIS (Step step, double rate) {
			 double period = EndingDate(step)-StartingDate(step);
			 //double rate = ((EconModel) step.getScenario().getModel()).getActualizationRate()/100;
			 double PNVIS = Double.POSITIVE_INFINITY;
			 PNVIS =   standPNV (step, rate)* Math.pow(1+rate, period)/(Math.pow(1+rate, period)-1);
			 //System.out.println ("Return PNV: "+ PNV);
			 return PNVIS;
		 }

		/**
		 * Calculate net present value using actualization rate
		 * and taking into account the annual operation of econmodel from a stand [can be used with scripts]
		 * @version 1.1
		 * @author Ch. Orazio - March 2003 - 2005
		 */
		 public static double standPNV (Step step, double rate) {
			double sum = 0;
			//double rate = ((EconModel) step.getScenario().getModel()).getActualizationRate()/100;
			Vector steps = step.getProject ().getStepsFromRoot (step);
			//Check economic functions acssociated to EconStand and previous
			for (Iterator i = steps.iterator (); i.hasNext ();) {
				Step stp = (Step) i.next ();
				GScene std = stp.getScene ();

				if (std.isInitialScene () || std.isInterventionResult ()) {
					Expense expF = null;
					Income incF = null;
					EconStand es = (EconStand) std;
					Collection functions = es.getEconomicFunctions ();
					if (functions != null) {
						for (Iterator j = functions.iterator (); j.hasNext ();) {
							EconomicFunction f = (EconomicFunction) j.next ();
							double d = Math.pow(1+rate,Math.round(std.getDate()-StartingDate(step)));
							if (f instanceof Expense && std.getDate()>= StartingDate(step) && std.getDate()<=EndingDate(step)) {sum -= ((Expense) f).getResult ()/d;} //substrac expenses
							if (f instanceof Income && std.getDate()>= StartingDate(step) && std.getDate()<=EndingDate(step)) {sum += ((Income) f).getResult ()/d;}   // add incomes
						}
					}
				}

			}
			//Check expenses associated to EconModel
			GModel m = step.getProject ().getModel ();
			Collection lines = ((EconModel) m).getRegularExpenseOrIncomes ();
			if (lines != null) {
				for (Iterator i = lines.iterator (); i.hasNext ();) {
					RegularExpenseOrIncome r = (RegularExpenseOrIncome) i.next ();
					double date1 =Math.max(r.getFromDate (), StartingDate(step));
					double date2 = Math.min(r.getToDate(), EndingDate(step));
					for (int a= (int) Math.round(date1-StartingDate(step)); a<=Math.round(date2-StartingDate(step)); a++){
						sum += (r.getIncome()-r.getExpense())/Math.pow(1+rate,a) ;
					}
				}
			}
			//System.out.println ("Return NV: "+sum);
			return sum;
		}
	 	/**
		 * Calculate current benefit (Incomes- expenses) without using actualization rate
		 * and taking into account the annual operation of econmodel from a stand [can be used with scripts]
		 * @version 1.1
		 * @author Ch. Orazio - March 2003 - 2005
		 */
		public static double standBenefit (Step step) {
				double sum = 0;
				String line = "";
				Vector steps = step.getProject ().getStepsFromRoot (step);
				//Check economic functions acssociated to EconStand and previous
				for (Iterator i = steps.iterator (); i.hasNext ();) {
					Step stp = (Step) i.next ();
					GScene std = stp.getScene ();

					if (std.isInitialScene () || std.isInterventionResult ()) {
						Expense expF = null;
						Income incF = null;
						EconStand es = (EconStand) std;
						Collection functions = es.getEconomicFunctions ();
						if (functions != null) {
							for (Iterator j = functions.iterator (); j.hasNext ();) {
								EconomicFunction f = (EconomicFunction) j.next ();
								if (f instanceof Expense && std.getDate()>= StartingDate(step) && std.getDate()<=EndingDate(step)) {sum -= ((Expense) f).getResult ();} //substrac expenses
								if (f instanceof Income && std.getDate()>= StartingDate(step) && std.getDate()<=EndingDate(step)) {sum += ((Income) f).getResult ();}   // add incomes
							}
						}
					}

				}
				//Check expenses associated to EconModel
				GModel m = step.getProject ().getModel ();
				Collection lines = ((EconModel) m).getRegularExpenseOrIncomes ();
				if (lines != null) {
					for (Iterator i = lines.iterator (); i.hasNext ();) {
						RegularExpenseOrIncome r = (RegularExpenseOrIncome) i.next ();
						double date1 =Math.max(r.getFromDate (), StartingDate(step));
						double date2 = Math.min(r.getToDate(), EndingDate(step));
						for (int a= (int) Math.round(date1); a<=Math.round(date2); a++){
							sum += r.getIncome()-r.getExpense() ;
						}
						System.out.println (r.getLabel ());
					}
				}
				System.out.print ("  Return sum : "+sum);
				return sum;
		}
		
		/**
		 * Calculate an actualized value on a step (take the min and max date from the step) actualization rate
		 * and taking into account the annual operation of econmodel from a stand [can be used with scripts]
		 * @version 1.0
		 * @author Ch. Orazio - December 2005
		 */
		public static double actualizedValueOnAStep (double value, double fromDate, double toDate, Step step) {
			double rate = ((EconModel) step.getProject().getModel()).getActualizationRate()/100;
			double sum = 0;
			double date1 =Math.max(fromDate, StartingDate(step));
			double date2 = Math.min(toDate, EndingDate(step));
			
			for (int a= (int) Math.round(date1-StartingDate(step)); a<=Math.round(date2-StartingDate(step)); a++){
				sum += (value)/Math.pow(1+rate,a-StartingDate(step)) ;
			}
			return sum;
		}



		/**
		* look for starting date in the project in stand and in regular charges
		*/
		public static  int  StartingDate (Step s) {
			GModel m = s.getProject().getModel();
			return  ((EconModel) m).getEconomicModelStartingDate();
		}

		/**
		* look for endingdate in the project in stand and in regular charges
		*/
		public static  int  EndingDate (Step s) {
			return  s.getScene().getDate();
		}

		// Insert a new step (n) between referent (r) and user selected one (a)
		// r - a -> r - n - a
		// n is the result of an EconomicIntervention on step r
		public static  void insertEconomicIntervention (Step r, Step a) {
			boolean insertingBeforeRoot = false;
			GScene scene;
			Step step;

			if (r == null) {	// intervention before root : replace root (a is root)
				insertingBeforeRoot = true;
				scene = a.getScene ().getInterventionBase ();
				step = a;
			} else {
				scene = r.getScene ().getInterventionBase ();
				step = r;
			}
			GModel m = a.getProject ().getModel ();
			

			EconomicIntervention i = null;
			try {
				i = new EconomicIntervention ();
				i.init(m, step, scene, null);
				
				// fc-8.11.2011 added the line below, seems to have been forgotten when refactoring the modeltools
				i.initGUI ();
				
				if (!i.isReadyToApply ()) {
					Log.println (Log.ERROR, "CommonEconFunctions.insertEconomicIntervention ()", "The EconomicIntervention is not readyToApply (): "+i);
					MessageDialog.print (null, Translator.swap ("EconomicBalance.intervenerCannotBeAppliedSeeLog"));
					return;
				}
			} catch (Exception e) {
				Log.println (Log.ERROR, "CommonEconFunctions.insertEconomicIntervention ()", "Error", e);
				MessageDialog.print (null, Translator.swap ("EconomicBalance.exceptionWhileConstructingEconomicInterventionSeeLog"));
				//~ status.setText (Translator.swap ("EconomicBalance.exceptionWhileConstructingEconomicInterventionSeeLog"));
				return;
			}

			Step n = null;
			try {
				GScene newStand = (GScene) i.apply ();
				n = a.getProject ().createStep(newStand);
				n.setReason(i.toString ());
				n.setVisible(true);
				
			} catch (Exception e) {
				Log.println (Log.ERROR, "CommonEconFunctions.insertEconomicIntervention ()", "Error (2)", e);
				MessageDialog.print (null, Translator.swap ("EconomicBalance.exceptionWhileApplyingEconomicInterventionSeeLog"));
				return;
			}

			if (insertingBeforeRoot) {
				a.setFather (n);
				n.setLeftSon (a);
				Project sc = a.getProject ();
				sc.setRoot (n);
			} else {
				a.setFather (n);
				n.setFather (r);
				n.setLeftSon (a);
				if (r.hasOneAndOnlyOneSon ()) {
					r.setLeftSon (n);
				} else {
					Node aux = r.getLeftSon ();
					while (aux != null && aux.getRightBrother () != a) {
						aux = aux.getRightBrother ();
					}
					if (aux != null) {
						aux.setRightBrother (n);
					}
				}
				n.setRightBrother (a.getRightBrother ());
				a.setRightBrother (null);
			}

			Project sc = a.getProject ();
			sc.setSaved (false);	// scenario was modified
		}

	// Remove step a and link its predecessor to its successors
	public static void removeEconomicIntervention (Step a) {
		Project sc = a.getProject ();

		Step f = (Step) a.getFather ();

		Collection sons = new ArrayList ();
		Step aux = (Step) a.getLeftSon ();
		while (aux != null) {
			sons.add (aux);
			aux = (Step) aux.getRightBrother ();
		}

		if (f == null && sons.size () > 1) {
			MessageDialog.print (null, Translator.swap (
					"EconomicBalance.canNotRemoveInterventionBecauseItIsRootAndHasSeveralSons"));
			return;
		}

		for (Iterator i = sons.iterator (); i.hasNext ();) {
			Step s = (Step) i.next ();
			s.setFather (f);
		}

		if (f != null) {
			f.setLeftSon (a.getLeftSon ());
		}

		if (a.isRoot ()) {
			sc.setRoot (a.getLeftSon ());
		}

		sc.setSaved (false);	// scenario was modified


	}

	/**
	* extrait une liste de variable d'une chaine de caractères dont on connait le séparateur
	*/
	public static String getValueFromString (String stringSource, int Index, String separator) {
		HashMap parameterList = new HashMap();
		int i = 0;
		int ind = 1;
		while (i < stringSource.lastIndexOf(separator)){
			String v = stringSource.substring(i+1, stringSource.indexOf(separator, i+1));
			if (i == 0){
				v = stringSource.substring(i, stringSource.indexOf(separator, i+1));
			}
			//System.out.println ("ind:"+ind+" i:"+i+"-"+stringSource.indexOf(separator, i+1)+" getValueFromString "+stringSource+":"+v);
			parameterList.put(""+ind , v);
			i=stringSource.indexOf(separator, i+1);
			ind++;
		}
		if (parameterList.containsKey(""+Index)) {
			return (String)parameterList.get(""+Index);
		} else {
			return null;
		}
	}
	
	public static String createStringFromArray(String[] a, int l){
		String s="";
		for (int i = 1; i <= l; i++ ){
			try {s = s + a[i] + separator;}
			catch (Exception e) {
		            MessageDialog.print (null, Translator.swap ( "CommonEconFunctions.createStringFromArray "+i+" causes error : "+e.toString ()));
		        }
		} 
		return s;
	}
	
	public static String getEconLabel (Step stp){
		String line = stp.getReason ();
		int begin = line.indexOf ("econLabel=\"");
	
		if (begin != -1) {
			begin+=11;
			line = line.substring (begin).trim ();
			int end = line.indexOf ("\"");
			if (end != -1) {
				line = line.substring (0, end );
			}
		} else { // if EconLabel doesn't exist import Name
			begin = line.indexOf ("name=\"");
			if (begin != -1) {
				begin+=6;
				line = line.substring (begin).trim ();
				int end = line.indexOf ("\"");
				if (end != -1) {
					line = line.substring (0, end);
				}
			} else {
			int begin2 = line.indexOf ("name=")-1;
				if (begin2 != -1) {
				begin2+=6;
				line = line.substring (begin2).trim ();
				int end2 = line.indexOf ("constructionCompleted");
					if (end2 != -1) {
						line = line.substring (0, end2);
					}
				}
			}
		}
		return line;
	}

	/*****************************
	* EconParametersFiles Management
	* Based on XML standarts
	*****************************/

	public static String version = "1.5";
	public static String separator = ";";
	public static String econStarter = "CAPSISEconomicParametersFile";
	public static int possibleTreesStatus = 2 ; // cut or dead

		/**
		* Create an econ parameters file
		*/



		public static void createEconParametersFile (String fileName) throws Exception {
		        BufferedWriter out = null;
		        //OutputStreamWriter out2 = null;
		        try {
		            out = new BufferedWriter (new FileWriter (fileName));
		            //out2 = new OutputStreamWriter (out);
		        } catch (Exception e) {
		            throw new Exception ("File name "+fileName+" causes error : "+e.toString ());
		        }
		        out.write ("<?xml version='1.0'?>");
				out.newLine ();
		        out.write ("<"+econStarter+" Version='"+version+"' Date='"+new Date().toString() +"'>");
		    	out.newLine ();
		        out.write ("</"+econStarter+" >");
		        try {out.close ();} catch (Exception e) {}    // no exception reported if trouble while closing
		}





		/**
		* put vector of string in an XML type file with tag and subtag
		* @Version 1.2
		*/

		public static void updateEconParametersFile(String fileName, String tag, String subTag, Vector Values) throws Exception {
			BufferedReader in = null;
			Vector sub = new Vector();
			Vector line = new Vector ();
			line.add(0,"");
			int l=0;
			int econ_start = -1;
			int econ_end = -1;
			int tag_start = -1;
			int tag_end = -1;
			int subTag_start = -1;
			int subTag_end = -1;
			String startItemTag="<item>";
			String endItemTag = "</item>";
			Vector taggedValues = new Vector ();
				try {
					in = new BufferedReader (new FileReader (fileName));
				} catch (Exception e) {
					throw new Exception ("Reading file named "+fileName+" causes error : "+e.toString ());
				}
				// tag the values

				for (Iterator i = Values.iterator (); i.hasNext ();) {
					taggedValues.add(startItemTag+i.next()+endItemTag);
				}

				// get the file in the vector line
				while (in.ready()) {
					String s = in.readLine();
					line.add(l,s);
					//System.out.println ("reading: "+ s+" at line " +l);
					if (s.trim().startsWith("<"+econStarter+" ") || s.trim().startsWith("<"+econStarter+">"))							{econ_start = l;}
					if (s.trim().startsWith("</"+econStarter+" ") || s.trim().startsWith("</"+econStarter+">"))							{econ_end = l;}
					if ((s.trim().startsWith("<"+tag+" ") || s.trim().startsWith("<"+tag+">")) && econ_start!=-1)						{tag_start = l;}
					if ((s.trim().startsWith("</"+tag+" ") || s.trim().startsWith("</"+tag+">")) && econ_end == -1)						{tag_end = l;}
					if ((s.trim().startsWith("<"+subTag+" ") || s.trim().startsWith("<"+subTag+">")) && tag_end==-1 && tag_start!=-1)	{subTag_start = l;}
					if ((s.trim().startsWith("</"+subTag+" ") || s.trim().startsWith("</"+subTag+">")) && tag_end==-1 && tag_start!=-1)	{subTag_end = l;}
					l++;
				}
				//System.out.println ("econ :"+econ_start+" "+ econ_end + "tag : "+tag_start+" "+tag_end+" subtag :"+subTag_start +" "+ subTag_end);
				try {in.close ();} catch (Exception e) {}    // no exception reported if trouble while closing

				// check if tag exist
				if (tag_start==-1){
					sub.clear();
					sub.add("<"+tag+">");
					sub.add("<"+subTag+">");
					sub.addAll (taggedValues);
					sub.add("</"+subTag+">");
					sub.add("</"+tag+" >");
					line.addAll(econ_start+1,sub);
				} else {
					if (tag_end < tag_start || tag_end==-1){
						throw new Exception ("Reading file named "+fileName+" causes error : tag integrity of "+tag);
					}
					//check test if subtask exist
					sub.clear();
					sub.add("<"+subTag+">");
					sub.addAll (taggedValues);
					sub.add("</"+subTag+">");
					if (subTag_start==-1){
						line.addAll(tag_start+1,sub);
						//System.out.println ("Création subtag après :"+line.get(tag_start));
					} else {
						//case if both tag and subtask exist
						if (subTag_end < subTag_start || subTag_end==-1 || subTag_start < tag_start || subTag_end > tag_end  ){
							throw new Exception ("Reading file named "+fileName+" causes error : Subtag integrity of "+subTag+ " tag_start:"+tag_start+ " tag_end:"+tag_end+ " subTag_start:"+subTag_start+ " subTag_end:"+subTag_end);
						}
						//destruction des valeurs périmées
						for (int j=subTag_start; j<=subTag_end; j++){
							//System.out.println (subTag_start+"Removed:"+line.get(subTag_start));
							line.remove(subTag_start);
							//line.removeElementAt(j);
						}
						//Vector li =  line.subList(subTag_start, subTag_end).clone();
						//line.removeAll(li);

						//inscription des valeurs actuelles
						line.addAll(tag_start+1,sub);
						//System.out.println ("ajout après :"+line.get(tag_start));
					}
				}

				// write line in a file
				BufferedWriter out = null;
				try {
					out = new BufferedWriter (new FileWriter (fileName));
				} catch (Exception e) {
					throw new Exception ("File name "+fileName+" causes error : "+e.toString ());
				}
				for (Enumeration e = line.elements() ; e.hasMoreElements() ;) {
				    String s = e.nextElement().toString();
					if (s!=""){
						out.write(s);
						out.newLine ();
					}
	     		}
				try {out.close ();} catch (Exception e) {}    // no exception reported if trouble while closing
		}
		/**********
		/*Delete data associated to a tag and a subtag in a file
		/***********/
		public static void deletesubTagEconParametersFile(String fileName, String tag, String subTag) throws Exception {
			BufferedReader in = null;
			Vector sub = new Vector();
			Vector line = new Vector ();
			line.add(0,"");
			int l=0;
			int econ_start = -1;
			int econ_end = -1;
			int tag_start = -1;
			int tag_end = -1;
			int subTag_start = -1;
			int subTag_end = -1;
				try {
					in = new BufferedReader (new FileReader (fileName));
				} catch (Exception e) {
					throw new Exception ("Reading file named "+fileName+" causes error : "+e.toString ());
				}


				// get the file in the vector line
				while (in.ready()) {
					String s = in.readLine();
					line.add(l,s);
					System.out.println ("reading: "+ s+" at line " +l);
					if (s.trim().startsWith("<"+econStarter+" ") || s.trim().startsWith("<"+econStarter+">"))							{econ_start = l;}
					if (s.trim().startsWith("</"+econStarter+" ") || s.trim().startsWith("</"+econStarter+">"))							{econ_end = l;}
					if ((s.trim().startsWith("<"+tag+" ") || s.trim().startsWith("<"+tag+">")) && econ_start!=-1)						{tag_start = l;}
					if ((s.trim().startsWith("</"+tag+" ") || s.trim().startsWith("</"+tag+">")) && econ_end == -1)						{tag_end = l;}
					if ((s.trim().startsWith("<"+subTag+" ") || s.trim().startsWith("<"+subTag+">")) && tag_end==-1 && tag_start!=-1)	{subTag_start = l;}
					if ((s.trim().startsWith("</"+subTag+" ") || s.trim().startsWith("</"+subTag+">")) && tag_end==-1 && tag_start!=-1)	{subTag_end = l;}
					l++;
				}
				//System.out.println ("econ :"+econ_start+" "+ econ_end + "tag : "+tag_start+" "+tag_end+" subtag :"+subTag_start +" "+ subTag_end);
				try {in.close ();} catch (Exception e) {}    // no exception reported if trouble while closing

				// check if tag exist
				if (tag_start==-1){
					//System.out.println ("Cannot delete a tag that doesn't exist");
				} else {
					if (tag_end < tag_start || tag_end==-1){
						throw new Exception ("Reading file named "+fileName+" causes error : tag integrity of "+tag);
					}
					//check test if subtask exist
					if (subTag_start==-1){
						System.out.println ("Cannot delete a subtag that doesn't exist");
					} else {
						//case if both tag and subtask exist
						if (subTag_end < subTag_start || subTag_end==-1 || subTag_start < tag_start || subTag_end > tag_end  ){
							throw new Exception ("Reading file named "+fileName+" causes error : Subtag integrity of "+subTag+ " tag_start:"+tag_start+ " tag_end:"+tag_end+ " subTag_start:"+subTag_start+ " subTag_end:"+subTag_end);
						}
						//destruction des valeurs périmées
						for (int j=subTag_start; j<=subTag_end; j++){
							System.out.println (subTag_start+"Removed:"+line.get(subTag_start));
							line.remove(subTag_start);
							//line.removeElementAt(j);
						}
					}
				}

				// write line in a file
				BufferedWriter out = null;
				try {
					out = new BufferedWriter (new FileWriter (fileName));
				} catch (Exception e) {
					throw new Exception ("File name "+fileName+" causes error : "+e.toString ());
				}
				for (Enumeration e = line.elements() ; e.hasMoreElements() ;) {
				    String s = e.nextElement().toString();
					if (s!=""){
						out.write(s);
						out.newLine ();
					}
	     		}
				try {out.close ();} catch (Exception e) {}    // no exception reported if trouble while closing
		}
		/******
		/Delete data associated to a tag in a file
		/******/

		public static void deleteTagEconParametersFile(String fileName, String tag) throws Exception {
			BufferedReader in = null;
			Vector sub = new Vector();
			Vector line = new Vector ();
			line.add(0,"");
			int l=0;
			int econ_start = -1;
			int econ_end = -1;
			int tag_start = -1;
			int tag_end = -1;
			int subTag_start = -1;
			int subTag_end = -1;
				try {
					in = new BufferedReader (new FileReader (fileName));
				} catch (Exception e) {
					throw new Exception ("Reading file named "+fileName+" causes error : "+e.toString ());
				}
				// get the file in the vector line
				while (in.ready()) {
					String s = in.readLine();
					line.add(l,s);
					//System.out.println ("reading: "+ s+" at line " +l);
					if (s.trim().startsWith("<"+econStarter+" ") || s.trim().startsWith("<"+econStarter+">"))						{econ_start = l;}
					if (s.trim().startsWith("</"+econStarter+" ") || s.trim().startsWith("</"+econStarter+">"))						{econ_end = l;}
					if ((s.trim().startsWith("<"+tag+" ") || s.trim().startsWith("<"+tag+">")) && econ_start!=-1)						{tag_start = l;}
					if ((s.trim().startsWith("</"+tag+" ") || s.trim().startsWith("</"+tag+">")) && econ_end == -1)						{tag_end = l;}
					//if ((s.trim().startsWith("<"+subTag+" ") || s.trim().startsWith("<"+subTag+">")) && tag_start!=-1)					{subTag_start = l;}
					//if ((s.trim().startsWith("</"+subTag+" ") || s.trim().startsWith("</"+subTag+">")) && tag_end==-1 && tag_start!=-1)	{subTag_end = l;}
					l++;
				}
				//System.out.println ("econ :"+econ_start+" "+ econ_end + "tag : "+tag_start+" "+tag_end+" subtag :"+subTag_start +" "+ subTag_end);
				try {in.close ();} catch (Exception e) {}    // no exception reported if trouble while closing

				// check if tag exist
				if (tag_start==-1){
					System.out.println ("Cannot delete a tag that doesn't exist");
				} else {
					if (tag_end < tag_start || tag_end==-1){
						throw new Exception ("Reading file named "+fileName+" causes error : tag integrity of "+tag);
					}
					//destruction des valeurs périmées
					for (int j=tag_start; j<=tag_end; j++){
						//System.out.println (tag_start+"Removed:"+line.get(tag_start));
						line.remove(tag_start);
						//line.removeElementAt(j);
					}
				}

				// write line in a file
				BufferedWriter out = null;
				try {
					out = new BufferedWriter (new FileWriter (fileName));
				} catch (Exception e) {
					throw new Exception ("File name "+fileName+" causes error : "+e.toString ());
				}
				for (Enumeration e = line.elements() ; e.hasMoreElements() ;) {
				    String s = e.nextElement().toString();
					if (s!=""){
						out.write(s);
						out.newLine ();
					}
	     		}
				try {out.close ();} catch (Exception e) {}    // no exception reported if trouble while closing
		}
	public static Vector getValueSubTagEconParametersFile(String fileName, String tag, String subTag) throws Exception {
		BufferedReader in = null;
		Vector sub = new Vector();
		Vector line = new Vector ();
		line.add(0,"");
		int l=0;
		int econ_start = -1;
		int econ_end = -1;
		int tag_start = -1;
		int tag_end = -1;
		int subTag_start = -1;
		int subTag_end = -1;
		String startItemTag="<item>";
		String endItemTag = "</item>";
			try {
				in = new BufferedReader (new FileReader (fileName));
			} catch (Exception e) {
				throw new Exception ("Reading file named "+fileName+" causes error : "+e.toString ());
			}
			// get the file in the vector line
			while (in.ready()) {
				String s = in.readLine();
				line.add(l,s);
				//System.out.println ("reading: "+ s+" at line " +l);
				if (s.trim().startsWith("<"+econStarter+" ") || s.trim().startsWith("<"+econStarter+">"))							{econ_start = l;}
				if (s.trim().startsWith("</"+econStarter+" ") || s.trim().startsWith("</"+econStarter+">"))							{econ_end = l;}
				if ((s.trim().startsWith("<"+tag+" ") || s.trim().startsWith("<"+tag+">")) && econ_start!=-1)						{tag_start = l;}
				if ((s.trim().startsWith("</"+tag+" ") || s.trim().startsWith("</"+tag+">")) && econ_end == -1)						{tag_end = l;}
				if ((s.trim().startsWith("<"+subTag+" ") || s.trim().startsWith("<"+subTag+">")) && tag_end==-1 && tag_start!=-1)	{subTag_start = l;}
				if ((s.trim().startsWith("</"+subTag+" ") || s.trim().startsWith("</"+subTag+">")) && tag_end==-1 && tag_start!=-1)	{subTag_end = l;}
				l++;
			}
			//System.out.println ("econ :"+econ_start+" "+ econ_end + "tag : "+tag_start+" "+tag_end+" subtag :"+subTag_start +" "+ subTag_end);
			try {in.close ();} catch (Exception e) {}    // no exception reported if trouble while closing

			// check if tag exist
			if (tag_start==-1){
				System.out.println ("tag "+tag+" doesn't exist");
			} else {
				if (tag_end < tag_start || tag_end==-1){
					throw new Exception ("Reading file named "+fileName+" causes error : tag integrity of "+tag);
				}
				//check test if subtask exist
				if (subTag_start==-1){
					System.out.println ("subtag "+subTag+" doesn't exist");
				} else {
					//case if both tag and subtask exist
					if (subTag_end < subTag_start || subTag_end==-1 || subTag_start < tag_start || subTag_end > tag_end  ){
						throw new Exception ("Reading file named "+fileName+" causes error : Subtag integrity of "+subTag+ " tag_start:"+tag_start+ " tag_end:"+tag_end+ " subTag_start:"+subTag_start+ " subTag_end:"+subTag_end);
					}
					//destruction des valeurs périmées
					for (int j=subTag_start+1; j<=subTag_end-1; j++){
						String item= ((String) line.get(j)).trim();
						if (item.startsWith(startItemTag)){
							item = item.substring(startItemTag.length(), item.length());
						}
						if (item.endsWith(endItemTag)){
							item = item.substring(0, item.length()-endItemTag.length());
						}

						sub.add(item);
					}
				}
			}
			return sub;
	}

}
