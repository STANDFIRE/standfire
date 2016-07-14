package capsis.lib.numerics;

//~ import general.*	;
//~ import functions.*	;
import java.util.ArrayList;
import java.util.Arrays;

/** Class for Genetic Algorithm
	Author			: Alain Franc
	Version			: 1.1b
	Date			: Dreux, May 12th, 2002
	Last revision	: June 9th, 2002

	Contains two proivate classes where all computations are made
	-	class	gene
	-	class	PopulationOfGenes


*/
public class OptimGene{
	//
	int							nLoci	;	// Number of loci per gene
	int							nGene	; 	// Number of genes in the population
	int							nIter	;	// Number of generations
	double						tMut	;	// Mutation rate
	double						T		;	// Discrimination for reproduction
	PopulationOfGenes			popGen	;	// Population (Array of genes)
	RealMatrix					M		;	// in line (i), min, mean and max of fitness in gene (i)
	FunctionBinaryVector2Real	f		;	// function for fitness
	//
	//	----------------- Constructor -----------------------------------------------------------
	//
	public OptimGene(FunctionBinaryVector2Real f, int nLoci, int nGene, int nIter, double tMut, double T){
		//
		this.nLoci	= nLoci				;
		this.nGene	= nGene				;
		this.nIter	= nIter				;
		this.f		= f					;
		this.tMut	= tMut				;
		this.T		= T					;
		//
		popGen		= new PopulationOfGenes(nLoci, nGene)	;
		M			= new RealMatrix(nIter,3)				;
	}
	//
	//	---------------- Getting fields and displays results -------------------------------------
	//
	public RealMatrix 	getM()		{return M	;}
	//
	public void popToScreen(){popGen.toScreen();}
	//
	//	============================================================================================
	//
	//							Iteration Procedures
	//
	//	=============================================================================================
	//
	/**	performs $nIter$ elementary iterations.
		at each step $i$, fills line $i$ of matrix $M$ with minimum, mean and maximum
			value of the fitness in tyhe population.
	*/
	public void run(){
		int			i					;
		double[]	a					;
		a	= popGen.minMeanMax()		;
		M.setVectorAtLine(a,0)			;
		for (i=1;i<nIter;i++){
			iter()						;
			a	= popGen.minMeanMax()	;
			M.setVectorAtLine(a,i)		;
		}
	}
	//
	/**	performs an elementary step of iteration, by a sequence of
		- random mutation
		- reproduction according to fitness for each gene
		- selection of the fittest genes
	*/
	public void iter(){
		int	k							;
		//
		popGen.mutate(tMut)				;
		popGen.reproduce(T)				;
		popGen.selectByFitness()		;
	}
	//	============================================================================================
	//
	//							Private classes Gene
	//
	//	=============================================================================================
	//
	/**	Private class gene contains as fields
			-	a binary vector g
			-	a real			phi
		the real phi is the fitness of g, computed with function f defined as a field of OptimGene
		When initialized by constructor Gene(nLoci), gene g is random, and fitness is automatically computed.
	*/
	private class Gene implements Comparable{
		BinaryVector	g	;
		double			phi	;
		//
		public Gene(int nLoci){
			g	= new BinaryVector(nLoci)	;
			g.setAlea()						;
			phi	= f.compute(g)				;
		}
		//
		public Gene(BinaryVector v){
			g	= v				;
			phi	= f.compute(g)	;
		}
		//
		public BinaryVector getBinaryVector(){return g;}
		//
		public double getFitness(){return phi;}
		//
		public void computeFitness(){phi = f.compute(g);}
		//
		public void toScreen(){
			String	s						;
			double	x						;
			s		= g.toString()			;
			x		= getFitness()			;
			//
			System.out.println(s + " -> " + x)		;
		}
		//
		public void mutate(double tMut){
			g.mutate(tMut)		;
			computeFitness()	;
		}
		//
		public Gene toClone(){
			BinaryVector	bv				;
			Gene			gClone			;
			//
			bv				= g.toClone()	;
			gClone			= new Gene(bv)	;
			return			gClone			;
		}
		//
		public int compareTo(Object otherGene){
			Gene	other	= (Gene)otherGene	;
			if (phi < other.phi)	return 1	;
			if (phi > other.phi)	return -1	;
			return	0							;
		}
	}
	//	============================================================================================
	//
	//							Private classes PopulationOfGene
	//
	//	=============================================================================================
	//
	private class PopulationOfGenes{
		Gene[]		geneArray	;
		ArrayList	geneList	;
		//
		public PopulationOfGenes(int nLoci, int nGene){
			int		i	;
			//
			geneArray	= new Gene[nGene]			;
			for (i=0; i < nGene; i++){
				geneArray[i]	= new Gene(nLoci)	;
			}
			geneList	= null						;
		}
		//
		//	------------------------------	Gets the minimum, mean and maximum fitness in population pop
		//
		public double[] minMeanMax(){
			int			i	;
			double		y	;
			RealVector	x	;
			double[]	a	;
			//
			x			= new RealVector(nGene)	;
			a			= new double[3]			;
			//
			for (i=0; i < nGene; i++){
				y	= geneArray[i].getFitness()	;
				x.setValueAt(y,i)				;
			}
			//
			a[0]	= x.min()	;
			a[1]	= x.mean()	;
			a[2]	= x.max()	;
			//
			return a		;
		}
		//
		//	-------------------------------- Mutates all genes with probability tMut at each locus
		//
		public void mutate(double tMut){
			int		i	;
			//
			for (i=0; i<nGene; i++){
				geneArray[i].mutate(tMut)	;
			}
		}
		//	----------------------------------- Reproduces genes
		//
		public void reproduce(double T){
			int		i			;
			double	x,z			;
			Gene	gRep		;
			//
			geneList	= new ArrayList()	;
			//
			for (i=0; i<nGene; i++){
				geneList.add(geneArray[i])		;
				z	= geneArray[i].getFitness()	;
				z	= 1-Math.exp(-z/T)			;
				x	= Math.random()				;
				//
				if (x < z){
					gRep	= geneArray[i].toClone()	;
					geneList.add(gRep)					;										;
				}
			}
		}
		//
		//	------------------------------------- Selection at random
		//
		public void selectAlea(){
			int			i 					;
			double		x					;
			Gene		gen					;
			Object		o					;
			//
			while (geneList.size() > nGene){
				x		= Math.random()			;
				i		= (int)Math.floor(geneList.size()*x)	;
				geneList.remove(i)			;
			}
			for (i=0; i<nLoci; i++){
				o				= geneList.get(i)		;
				geneArray[i]	= (Gene)o				;
			}
		}
		//	------------------------------------- Selection by fitness
		//
		public void selectByFitness(){
			int		i, n				;
			Gene[]	a				;
			//
			n	= geneList.size()	;
			a	= new Gene[n]		;
			//
			for (i=0; i<n; i++)		{a[i]=(Gene)geneList.get(i)	;}
			Arrays.sort(a);
			for (i=0; i<nLoci; i++)	{geneArray[i]=a[i]	;}
		}
		//
		//	------------------------------------- Displays the population on the screen
		//
		public void toScreen(){
			int	i	;
			for (i=0; i<nGene; i++){
				geneArray[i].toScreen()	;
			}
		}
		//
		//	------------------------------------ Displays population after reproduction
		//
		public void reproductionToScreen(){
			int		i, n			;
			Object	o				;
			Gene	gen				;
			//
			n	= geneList.size()	;
			//
			for (i=0; i<n; i++){
				o	= geneList.get(i)	;
				gen	= (Gene)o			;
				gen.toScreen()			;
			}
		}
	}// -> end of PopulationOfGenes
}

