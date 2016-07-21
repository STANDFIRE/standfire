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

import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.ExpansionFactorProvider;
import capsis.defaulttype.Numberable;
import capsis.defaulttype.Tree;

/**
 * The RdiAutoThinnerScoredTree class is a wrapper for a Tree instance. It provides
 * an expansion factor for each instance as well as a score, which serves to determine
 * whether or not the tree is going to harvested.
 * @author F. Mothe, G. LeMoguedec - May 2010
 * @author Mathieu Fortin (refactoring) - May 2014
 */
public class RdiAutoThinnerScoredTree implements DbhCmProvider, ExpansionFactorProvider {
	
    protected Tree t;
    protected boolean toCut;
    private double score;
    private double uniform;
    private double number;

    protected RdiAutoThinnerScoredTree(Tree t, double uniform) {
        this.t = t;
        this.uniform = uniform;
        this.number = (t instanceof Numberable) ? ((Numberable) t).getNumber() : 1.;
        // score and toCut initialised externally
    }

    @Override
    public double getNumber() {return number;}

    @Override
    public double getDbhCm() {return t.getDbh();}

    protected double getUniform() {return uniform;}

    protected double getCuttingScore() {return score;}

    protected double getExpectedRemainingNumber(double slope, double intercept) {
        return getRemainingProba(slope, intercept) * number;
    }

    private double getCuttingProba(double slope, double intercept) {
        double score = intercept + slope * getDbhCm();
        return Math.min(Math.max(score, 0.), 1.);
    }

    private double getRemainingProba(double slope, double intercept) {
        return 1. - getCuttingProba(slope, intercept);
    }

    /**
     * Set the score to be cut in the range [-1, +1] (+1 = always cut, -1 = neither cut)
     */
    protected void setCuttingScore(double slope, double intercept) {
        double p = getCuttingProba(slope, intercept);
        if (p <= 0.) {
            score = -1.;
        } else if (p >= 1.) {
            score = 1.;
        } else {
            score = p - uniform;
        }
    }

    /**
     * Set the score for infinite or null slope
     */
    protected void setCuttingScore(double score) {
        this.score = score;
    }

}
