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

package capsis.extension.treelogger.geolog.species;

import java.util.HashMap;
import java.util.Map;

import capsis.extension.treelogger.geolog.util.LogPriceModel;

/**	FgOakLogPriceModel :
*	Log prices in Euro / m3 according to Sébastien Cavaignac 2003 model :
*	CAVAIGNAC S., 2003 : Elaboration d'un modèle de prix du bois de Chêne (Quercus petraea Liebl. et Quercus
*	robur L.) à partir de ventes en régie et comparaison à  d'autres méthodes d'estimation. D.E.A. Sciences Agronomiques,
*	ENSAI Nancy, Equipe de Recherches sur la Qualité des Bois, INRA-CRF, Nancy - Champenoux, Septembre,
*	42pp (document ERQB 2003/6).
*
*	@author F. Mothe - july 2006
*/
public class FgOakLogPriceModel {

	private static Map <String, LogPriceModel> models;		// by log class name "A", "B"...
	private static FgOakLogPriceModel instance = new FgOakLogPriceModel ();

	private FgOakLogPriceModel () {
		models = new HashMap <String, LogPriceModel> ();
		models.put ("A", new LogPriceModel ( -143.5791565,	12.530968078));
		models.put ("B", new LogPriceModel (-115.64215,	8.4330081901));
		models.put ("C", new LogPriceModel (-43.63774817,	4.407878472));
		models.put ("D", new LogPriceModel ( 10.866264144,	0.845996492));
		// unused : models.put ("E", new LogPriceModel (7,	0));
		models.put ("F", new LogPriceModel (1.5,	0));
		// unused : models.put ("Z", new LogPriceModel (0,	0));
	}

	public static LogPriceModel getModel (String className) {
		return models.get (className);
	}

/*
	private Map < Integer, GeoLogProduct > products;	// by id
	private Map <String, LogPriceModel> models;		// by log class name "A", "B"...
	private Map <String, LogPriceModel> productModels;	// by product name "stump", "slicing"...

	public FgOakLogPriceModel (Map < Integer, GeoLogProduct > products) {
		this.products = products;
		createModels ();
	}

	void createModels () {
		models = new HashMap <String, LogPriceModel> ();
		models.put ("A", new LogPriceModel ( -143.5791565,	12.530968078));
		models.put ("B", new LogPriceModel (-115.64215,	8.4330081901));
		models.put ("C", new LogPriceModel (-43.63774817,	4.407878472));
		models.put ("D", new LogPriceModel ( 10.866264144,	0.845996492));
		// unused : models.put ("E", new LogPriceModel (7,	0));
		models.put ("F", new LogPriceModel (1.5,	0));
		// unused : models.put ("Z", new LogPriceModel (0,	0));

		productModels = new HashMap <String, LogPriceModel> ();
		associates (FagaceesLoggingStarter.SLICING, "A");
		associates (FagaceesLoggingStarter.STAVE, "A");
		associates (FagaceesLoggingStarter.FURNITURE, "B");
		associates (FagaceesLoggingStarter.SAWING, "C");
		associates (FagaceesLoggingStarter.LVL, "D");
		associates (FagaceesLoggingStarter.PARTICLE, "D");
		associates (FagaceesLoggingStarter.FIRE, "F");

		// TODO : models should be local
	}

	void associates (int productId, String modelName) {
		GeoLogProduct prod = products.get (productId);
		String n = ( prod == null ? ("?" + productId) : prod.getName () );
		LogPriceModel m = models.get (modelName);
		// System.out.println ("pid=" + productId + " prod=" + prod + " n=" + n + " m=" + m);
		productModels.put (n, m);

		//productModels.put (products.get (productId).getName (),
		//		models.get (modelName));
	}

	double getPrice_Epm3 (String productName, double medianDiam_cm) {
		LogPriceModel model = productModels.get (productName);
		return model == null ? 0. : model.getPrice_Epm3 (medianDiam_cm);
	}

	public double getPrice_Epm3 (GPiece piece)
	{
		return getPrice_Epm3 (piece.pieceProduct,
				PieceUtil.getmedianDiameter_cm (piece));
	}
*/
}
