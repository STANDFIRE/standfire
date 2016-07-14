package	capsis.lib.numerics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
Author				AF
Date				December, 29th, 2001
Location			Dreux
Version				2.0
last revision		December, 23rd, 2002
//
	Fields			int			nRow, nCol		;
					int			valMax			;
					byte[][]	tab				;

	Constructors	MatrixOfBytes(int nRow, int nCol, int valMax)
					MatrixOfBytes(int nRow, int nCol)

	Public Methods	int 	getNRow()
					int		getNCol()
					byte	getValueAt(int i, int j)


*/
public class MatrixOfBytes{

	/*

	 /=========================================================================================\
	|																							|
	|									Fields													|
	|																							|
	 \=========================================================================================/

	*/


	public 	int			nRow, nCol		;
	public 	int			valMax			;
	public	byte[][]	tab				;


	// =========================================================================================//
	//																							//
	//									Constructors											//
	//															 								//
	// =========================================================================================//

	public MatrixOfBytes(){
		nRow	= -1	;
		nCol	= -1	;
		valMax	= -1	;
		tab		= null	;
	}

	/** Constructs a Matrix of Bytes of size nRow by nCol full of zeros
	@ param	nRow		Number of rows
	@ param nCol		Number of columns
	@ param	valMax		Maximum value of an element
	*/
	public MatrixOfBytes(int nRow, int nCol, int valMax){
		this.nRow		= nRow					;
		this.nCol		= nCol					;
		this.valMax		= valMax				;
		tab				= new byte [nRow][nCol]	;
	}
	//
	/** Constructs a Matrix of Bytes of size nRow by nCol full of zeros.
	The number of rows is equal to the number of columns
	@ param	dim			Number of rows and number of columns
	@ param	valMax		Maximum value of an element
	*/
	public MatrixOfBytes(int nRow, int nCol){
		this.nRow		= nRow						;
		this.nCol		= nCol						;
		valMax			= (byte)1					;
		tab				= new byte [nRow][nCol]		;
	}

	/*
	 /=========================================================================================\
	|																							|
	|									Public Methods											|
	|																							|
	 \=========================================================================================/

	 /-----------------------------------------------------------------------------------------\
	|																							|
	|									Getting fields											|
	|																							|
	 \ ----------------------------------------------------------------------------------------/

	*/


	public 	int 	getNRow()					{return nRow		;}
	public	int		getNCol()					{return	nCol		;}
	public	int		getValMax()					{return	valMax		;}

	public	byte	getValueAt(int i, int j)	{return	tab[i][j]	;}

	public	byte	getValueAt(int[] iPoint){

		byte	value				;

		int	i, j					;

		i		= iPoint[0]			;
		j		= iPoint[1]			;

		value	= getValueAt(i,j)	;

		return 	value	;
	}


	// -----------------------------------------------------------------------------------------\
	//																							|
	//									Setting fields											|
	//																							|
	// -----------------------------------------------------------------------------------------/

	public void setValMax(int valMax)	{this.valMax	= valMax	;}

	public	void dimensionInput(){

		String		latticeDimension 	;
		int			dim					;
		int			nRow,nCol			;

		latticeDimension 	= JOptionPane.showInputDialog("Dimension of the Lattice")	;
		dim					= Integer.parseInt(latticeDimension)						;
		nRow				= dim														;
		nCol				= dim														;

		this.nRow			= nRow					;
		this.nCol			= nCol					;

		tab					= new byte[nRow][nCol]	;
	}


	public	void	nStateInput(){

		String		stateNumber		;
		int			valMax			;

		stateNumber			= JOptionPane.showInputDialog("Number of states")			;
		valMax				= Integer.parseInt(stateNumber)								;

		valMax	= valMax - 1	;

		setValMax(valMax)		;

	}



	/** Fills a Matrix of Bytes with random values between 0 and valMax
	*/
	public void setAlea(){
		int 	i,j	;
		double	r	;
		byte	b	;
		//
		for (i=0; i < nRow; i++){
			for (j=0; j<nCol; j++){
				r			= Math.random()						;
				b			= (byte)Math.floor(r*(1+valMax))	;
				tab[i][j]	= b									;
			}
		}
	}

	public void setAlea(double[] frac){
		int			i, j, k		;
		byte		value		;
		double		x, sum		;
		double		xRandom		;
		int			lFrac		;
		//
		sum		= 0				;
		lFrac	= frac.length	;
		//
		for (i=0; i<lFrac;i++){
			x		= frac[i]	;
			sum		= sum + x	;
			frac[i]	= sum		;
		}
		for (i=0; i < nRow; i++){
			for (j=0; j < nCol; j++){
				xRandom	= Math.random()			;
				k		= 0						;
				while (xRandom > frac[k]){k++	;}
				value		= (byte)k			;
				tab[i][j]	= value				;
			}
		}
	}


	public void setAlea(RealVector frac){
		double[]	vec				;
		vec			= frac.getVec()	;
		setAlea(vec)				;
	}
	/**	sets values in matrix with uniform distribution.
		Each element is expected to present with probability prob = 1/(valMax + 1)
	*/
	public void setUniformDistribution(){
		//
		int			iVal	;
		double		prob	;
		double		one		;
		double		val		;
		RealVector	rv		;
		//
		iVal	= valMax + 1					;
		val		= (double)iVal					;
		one		= (double)1						;
		prob	= one/val						;
		rv		= new RealVector(valMax+1,prob)	;
		//
		setAlea(rv)								;
	}



	/** Fills a binary Matrix of Bytes with ones with a given density
	*/
	public void setAleaDens(double dens){
		int 	i,j	;
		double	r	;
		byte	b	;
		//
		for (i=0; i < nRow; i++){
			for (j=0; j<nCol; j++){
				r			= Math.random()						;
				if (r < dens)	{tab[i][j] 	= 1	;}
				else			{tab[i][j]	= 0	;}
			}
		}
	}


	/** Fills a Matrix of Bytes with zeros
	*/

	public void setZeros(){
		int	i, j	;
		for (i=0; i < nRow; i++){
			for (j=0; j < nCol; j++){
				tab[i][j]	= 0	;
			}
		}
	}

	/** Fills a Matrix of Bytes with ones
	*/

	public void setOnes(){
		int	i, j	;
		for (i=0; i < nRow; i++){
			for (j=0; j < nCol; j++){
				tab[i][j]	= 1	;
			}
		}
	}

	/** Fills a Matrix of Bytes with value b
	*/

	public void fillWithValue(byte b){
		int	i, j	;
		for (i=0; i < nRow; i++){
			for (j=0; j < nCol; j++){
				tab[i][j]	= b	;
			}
		}
	}

	public void allButOneAtRandom(byte valueBackGround, byte valueSinglePixel){
		//
		int[]	iPoint							;
		//
		fillWithValue(valueBackGround)			;
		iPoint	= selectRandomPoint()			;
		setValueAt(valueSinglePixel, iPoint)	;
		//
	}

	// ----------------------------------------------------------------------------------------------------

	/**
	*/

	public int[] selectRandomPoint(){

		int[]	iPoint					;
		double	xRandom, yRandom		;
		int		i, j					;

		iPoint	= new int[2]			;

		xRandom	= Math.random()					;
		yRandom	= Math.random()					;
		i		= (int)Math.floor(xRandom*nRow)	;
		j		= (int)Math.floor(yRandom*nCol)	;

		iPoint[0]	= i							;
		iPoint[1]	= j							;

		return		iPoint						;
	}


	/** Sets a given value
	*/
	public void setValueAt(byte b, int i, int j){tab[i][j] = b	;}


	public void	setValueAt(byte value, int[] iPoint){
		//
		int	i,j					;
		int	li					;
		//
		li	= iPoint.length		;
		if (li > 2){
			System.out.println("Class MatrixOfBytes // Method setValueAt() // iPoint not of length 2 : " + li);
		}
		//
		i	= iPoint[0]			;
		j	= iPoint[1]			;
		//
		setValueAt(value, i, j)	;
	}

	/** Sets a given line
	*/
	public void setVecAtLine(BinaryVector vec, int i){
		int		j	;
		byte	b	;
		//
		if (vec.getDim() == nCol){
			for(j=0; j<nCol;j++){
				b	= vec.getValueAt(j)	;
				setValueAt(b,i,j)		;
			}
		}
		else	{
			System.out.println("Error: MatrixOfBytes.setLineAt()  --> dimension do not fit!")	;
			System.out.println("vec.dim = " + vec.getDim() + " -- nCol = " + nCol)				;
		}
	}

	/**	Select the lines and columns of a neighbor of cell at (l,c) depending on a byte b with
		b	= 	0	:	north
				1	:	east
				2	:	south
				3	:	west
	*/
	public int[] selectNeighbor (int l, int c, byte b){
		//
		int[]		neighbor		;
		neighbor	= new int[2]	;
		neighbor[0]	= -1			;
		neighbor[1]	= -1			;
		//
		switch(b){
			case 0:
				if  (l > 0)			{neighbor[0]	= l - 1		;}
				if	(l ==0)			{neighbor[0]	= nRow -1	;}
				neighbor[1]							= c			;
				break											;
			case 1:
				neighbor[0]							= l			;
				if (c < nCol-1)		{neighbor[1]	= c +1		;}
				if (c == nCol-1)	{neighbor[1]	= 0			;}
				break									;
			case 2:
				if	(l < nRow-1)	{neighbor[0]	= l + 1		;}
				if	(l == nRow-1)	{neighbor[0]	= 0			;}
				neighbor[1]							= c			;
				break											;
			case 3:
				neighbor[0]							= l			;
				if	(c > 0)			{neighbor[1]	= c -1		;}
				if	(c == 0)		{neighbor[1]	= nCol-1	;}
				break											;
		}
		return neighbor	;
	}

	public	int[]	selectNeighbor	(int[] iPoint, byte b){

		int			l, c			;
		int[]		iPointOut		;

		iPointOut	= new int[2]	;


		l	= iPoint[0]	;
		c	= iPoint[1]	;

		iPointOut	= 	selectNeighbor(l,c,b)	;

		return	iPointOut	;
	}

	// -------------------------------------------------------------------------------------------

	/**	Displays a matrix of bytes on the screen
	*/
	public void toScreen(){
		int i,j	;
		//
		for (i=0; i<nRow; i++){
			for (j=0; j<nCol; j++){System.out.print(tab[i][j])	;}
			System.out.println("")					;
		}
	}

// ----------------------------------------------------------------------------------------

	public int	numberOfNeighborInStateAt(byte	b, int[] iPoint){

		int				l, c, ln, ls, ce, cw	;
		int				nNeighbor				;
		byte			kb						;

		l			= iPoint[0]	;
		c			= iPoint[1]	;
		nNeighbor	= 0			;

		if (l >0)		{ln	= l-1 		;}
		else 			{ln = nRow-1 	;}


		if (l <nRow-1)	{ls	= l+1 		;}
		else 			{ls = 0 		;}

		if (c >0)		{cw	= c-1 		;}
		else 			{cw = nCol-1 	;}

		if (c < nCol-1)	{ce	= c+1 		;}
		else 			{ce = 0 		;}

		kb	= tab[ln][c] 		;
		if (kb == b)	{nNeighbor++	;}

		kb	= tab[l][ce] 		;
		if (kb == b)	{nNeighbor++	;}

		kb	= tab[ls][c] 		;
		if (kb == b)	{nNeighbor++	;}

		kb	= tab[l][cw] 		;
		if (kb == b)	{nNeighbor++	;}

		return nNeighbor	;
	}

	public	MatrixOfBytes	numberOfNeighborInState(byte	b){

		int				i, j			;
		int[]			iPoint			;
		int				nNeighbors		;
		MatrixOfBytes	result			;

		iPoint	= new int[2]									;
		result	= new MatrixOfBytes (nRow, nCol, 4)				;

		for (i=0; i<nRow; i++){
			iPoint[0]	= i			;
			for(j=0; j<nCol; j++){
				iPoint[1]	= j										;
				nNeighbors	= numberOfNeighborInStateAt(b, iPoint)	;
				result.setValueAt((byte)nNeighbors, iPoint)			;
			 }
		}

		return result			;
	}


	/**	Counts the number of neighbors in state s, with neighborhood of 4 neighbors
	puts the result into a matrixOfBytes of same dimension, indicating the value for each cell.
	Provisionally, s=1 and on binary CA
	AF, Dreux, 02/04/06
	*/
	public MatrixOfBytes countNVois4(){
		int				l, c, ln, ls, ce, cw	;
		int				k						;
		byte			kb						;
		MatrixOfBytes	nVois					;
		//
		nVois	= new MatrixOfBytes(nRow,nCol,4)	;
		//
		for(l=0 ; l < nRow; l++){
			if (l >0)		{ln	= l-1 		;}
			else 			{ln = nRow-1 	;}
			if (l <nRow-1)	{ls	= l+1 		;}
			else 			{ls = 0 		;}
			for (c=0; c < nCol; c++){
				if (c >0)		{cw	= c-1 		;}
				else 			{cw = nCol-1 	;}
				if (c < nCol-1)	{ce	= c+1 		;}
				else 			{ce = 0 		;}
				//
				k 	= 0	;
				k 	= k + tab[ln][c] + tab[l][ce] + tab[ls][c]  + tab[l][cw] 		;
				//
				kb	= (byte)k			;
				nVois.tab[l][c]	= kb	;
			}
		}
		return nVois	;
	}

	// ----------------------------------------------------------------------------------------

	/**	Counts the number of neighbors in state s, with neighborhood of 8 neighbors
	Provisionally, s=1 and on binary CA
	AF, Dreux, 02/04/06
	*/
	public MatrixOfBytes countNVois8(){
		int				l, c, ln, ls, ce, cw	;
		int				k						;
		byte			kb						;
		MatrixOfBytes	nVois					;
		//
		nVois	= new MatrixOfBytes(nRow,nCol,8)	;
		//
		for(l=0 ; l < nRow; l++){
			if (l >0)		{ln	= l-1 		;}
			else 			{ln = nRow-1 	;}
			if (l <nRow-1)	{ls	= l+1 		;}
			else 			{ls = 0 		;}
			for (c=0; c < nCol; c++){
				if (c >0)		{cw	= c-1 		;}
				else 			{cw = nCol-1 	;}
				if (c < nCol-1)	{ce	= c+1 		;}
				else 			{ce = 0 		;}
				//
				k 	= 0	;
				k 	= k + tab[ln][c] + tab[ln][ce] + tab[l][ce]		;
				k	= k + tab[ls][ce]+ tab[ls][c]  + tab[ls][cw]	;
				k	= k + tab[l][cw] + tab[ln][cw]					;
				//
				kb	= (byte)k			;
				nVois.tab[l][c]	= kb	;
			}
		}
		return nVois	;
	}

	/*

	 /-----------------------------------------------------------------------------------------\
	|																							|
	|									Graphics											|
	|																							|
	 \ ----------------------------------------------------------------------------------------/

	*/

	public void toWindow(){
		//
		CaGraphWindow	caWin	;
		CaPanel			caPane	;
		//
		caWin	= new CaGraphWindow()		;
		caPane	= new CaPanel()				;
		//
		caWin.getContentPane().add(caPane)	;
		caWin.show()						;
	}


	public void toWindow(String title){
		//
		CaGraphWindow	caWin	;
		CaPanel			caPane	;
		//
		caWin	= new CaGraphWindow(title)	;
		caPane	= new CaPanel()				;
		//
		caWin.getContentPane().add(caPane)	;
		caWin.show()						;
	}




	// ----------------------------------------------------------------------------------------

	public class CaGraphWindow extends JFrame{
		// Champs statiques
		//
		public static final int WIDTH	= 650	;
		public static final int HEIGHT	= 650	;

		public CaGraphWindow(){
			setTitle("Cellular Automata")		;
			setSize(WIDTH,HEIGHT)				;
		}
		//
		public CaGraphWindow(String title){
			setTitle(title)						;
			setSize(WIDTH,HEIGHT)				;
		}
	}

	// ----------------------------------------------------------------------------------------

	public class CaPanel extends JPanel
	{
		public	int					drPaintL, drPaintC	;
		private Point				pRefresh			;	// Pixel à rafraîchir, sinon nul
		private	Image				buffer				;
		//
		public CaPanel(){
			super()							;
			pRefresh			= null		;
		}
		//
		public void setPRefresh(int i, int j){
			pRefresh	= new Point(i,j)	;
		}
		//
		public void setPRefresh(Point p){
			pRefresh	= p	;
		}
		//
		public void paintComponent (Graphics graphics){
			double	top		= 10			;	// margin on top
			double	left	= 10			;	// margin on left
			double	a		= 512/nRow		;	// size of a cell, in pixels
			double	l		= 0				;
			double	c		= 0				;
			//
			int 	i,j	;
			byte	b	;
			//
			if (buffer != null){
				graphics.drawImage(buffer,0,0,null)	;
			}
			else{
				Rectangle bounds 	= getBounds()								;
				buffer				= createImage(bounds.width,bounds.height)	;
			}
			Graphics g		= buffer.getGraphics()	;
			Graphics2D	g2	= (Graphics2D)g			;
			//
			if (pRefresh != null){
				i	= pRefresh.x	;
				j	= pRefresh.y	;
				//
				l	= top + i*a			;
				c	= left + j*a		;
				b	= tab[i][j]		;
				//
				if (b==0)	{g2.setPaint(Color.blue)		;}
				if (b==1)	{g2.setPaint(Color.red)			;}
				if (b==2)	{g2.setPaint(Color.yellow)		;}
				if (b==3)	{g2.setPaint(Color.green)		;}
				if (b==4)	{g2.setPaint(Color.magenta)		;}
				if (b==5)	{g2.setPaint(Color.cyan)		;}
				if (b==6)	{g2.setPaint(Color.orange)		;}
				if (b==7)	{g2.setPaint(Color.pink)		;}
				if (b==8)	{g2.setPaint(Color.black)		;}
				//
				Rectangle2D rect	= new Rectangle2D.Double(l, c, a, a)	;
				g2.fill(rect)		;
			}
			else{
				for (i=0; i<nRow; i++){
					l	= top + i*a	;
					//
					for (j=0; j<nCol; j++){
						c	= left + j*a		;
						b	= tab[i][j]		;
						//
						if (b==0)	{g2.setPaint(Color.blue)		;}
						if (b==1)	{g2.setPaint(Color.red)			;}
						if (b==2)	{g2.setPaint(Color.yellow)		;}
						if (b==3)	{g2.setPaint(Color.green)		;}
						if (b==4)	{g2.setPaint(Color.magenta)		;}
						if (b==5)	{g2.setPaint(Color.cyan)		;}
						if (b==6)	{g2.setPaint(Color.orange)		;}
						if (b==7)	{g2.setPaint(Color.pink)		;}
						if (b==8)	{g2.setPaint(Color.black)		;}
						//
						Rectangle2D rect	= new Rectangle2D.Double(l, c, a, a)	;
						g2.fill(rect)		;
					}
				}
			}
			//
			graphics.drawImage(buffer,0,0,null)	;
		}
	}
}