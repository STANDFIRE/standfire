================================
Tutorial 1: Interfacing with FVS
================================

Use Suppose to generate a keyword file. Or use the following example .key::

	NOSCREEN
	RANNSEED           0
	!STATS
	STDIDENT
	STANDFIRE_example
	DESIGN           -10       500         5         9          
	STDINFO          103       140      60.0       0.0       0.0      36.0
	INVYEAR         2010
	NUMCYCLE          10
	TREEDATA
	FMIN
	END
	STATS
	SVS                0                   0         0        15
	FMIn
	Potfire
	FuelOut
	BurnRept
	MortRept
	FuelRept
	SnagSum
	End
	PROCESS
	STOP

If don't have a FVS tree list file, then copy and paste the following text and save  it to the same directory where the keyword file lives, give it the same prefix as the ``.key`` but with a ``.tre`` extension.::

	1   95       9PP 105    35                          0 0         
	1   96       0PP 43     17        1                 0 0         
	1   97       0PP 148    43        2                 0 0         
	1   98       0PP 49     30        1                 0 0         
	1   99       9PP 54     30                          0 0         
	1   100      0PP 100    40        3                 0 0         
	1   101      0PP 42     30        2                 0 0         
	1   102      0PP 53     34        1                 0 0         
	1   103      0PP 97     42        3                 0 0         
	1   104      0PP 61     35        1                 0 0         
	1   105      0PP 81     40        1                 0 0         
	1   106      9PP 80     33                          0 0         
	1   107      0PP 41     32        2                 0 0         
	1   108      9PP 71     40                          0 0         
	1   109      9PP 73     41                          0 0         
	1   110      9PP 94     35                          0 0         
	1   111      9PP 103    32                          0 0