================================
Tutorial 1: Interfacing with FVS
================================

Use Suppose to generate a keyword file. Or use the following example .key

.. code-block:: none

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

If don't have a FVS tree list file, then copy and paste the following text and save  it to the same directory where the keyword file lives, give it the same prefix as the ``.key`` but with a ``.tre`` extension.

.. code-block:: none

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

Once you have a keyword file and a tree list file in the same directory we can start to build a script to do some work.

.. code-block::

	$ cd /Users/standfire/fvs_exp
	$ ls
	example.key    example.tre


First we import the Fvsfuels class from the fuels module.

.. code-block:: python

	>>> from standfire.fuels import Fvsfuels

Next create an instance of the class passes the desired variant as an argument and register the keyword file.

.. code-block:: python

	>>> stand_1 = Fvsfuel("iec")
	>>> stand_1.set_keyword("/Users/standfire/fvs_exp/example.key")
	TIMEINT not found in keyword file, default is 10 years

We get a message telling us that the TIMEINT keyword was not found in the keyword file. No problem, STANDFIRE automatically sets this value to 10 years.

.. code-block:: python

	>>> stand_1.keywords
	{'TIMEINT': 10, 'NUMCYCLE': 10, 'INVYEAR': 2010, 'SVS': 15, 'FUELOUT': 1}

Notice the keys in the keywords dictionary.  ``TIMEINT`` is the time interval of the FVS simulation in year, ``NUMCYCLE`` is the number of cycles, ``INVYEAR`` is the year of the inventory, and ``SVS`` and ``FUELOUT`` are there to check if these keywords are in the keyword file. If the ``SVS`` and ``FUELOUT`` keywords are not defined the keyword file then FVS will not calculate tree positions or fuel attributes. So be sure you add these to your keyword file before registering the .key with FVS. You can use *post processors** in Suppose to do so.  ``TIMEINT``, ``NUMCYCLE``, and ``INVYEAR`` can be manually changed by calling setters for each. For instance, if you only want to calculate fuel attributes for trees during the year of the inventory then simply change the ``NUMCYCLE`` value in the keyword dictionary.

.. code-block:: python

	>>> stand_1.set_num_cycle(0)
	>>> stand_1.keywords
	{'TIMEINT': 10, 'NUMCYCLE': 0, 'INVYEAR': 2010, 'SVS': 15, 'FUELOUT': 1}

Now that we have our simulation parameters established, we startup FVS.

.. code-block:: python

	>>> stand_1.run_fvs()
