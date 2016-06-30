#!/usr/bin/python
"""

"""

import numpy as np
import pandas as pd
import os
import pprint
import platform
import cPickle

__authors__ = "Lucas Wells, Greg Cohn, Russell Parsons"
__copyright__ = "Copyright 2015, STANDFIRE"

# FVS variants
eastern = {'CS', 'LS', 'NE', 'SN'}
western = {'AK', 'BM', 'CA', 'CI', 'CR', 'EC', 'ID', 'NC', 'KT', 'NI', 'PN'
        , 'SO', 'TT', 'UT', 'WC', 'WS'}

class Fvsfuels(object):
    """
    A Fvsfuels object is used to calculate component fuels at the individual
    tree level using the Forest Vegetation Simulator. To create an instance
    of this class you need two items: a keyword file (.key) and tree list file
    (.tre) with the same prefix as the keyword file. If you don't already have
    a tree list file then you can use ```fuels.Inventory``` class to generate
    one.

    :param variant: FVS variant to be imported
    :type variant: string

    **Example:**

    A basic example to extract live canopy biomass for individual trees during
    year of inventory

    >>> from standfire.fuels import Fvsfuels
    >>> stand001 = Fvsfuels("iec")
    >>> stand001.set_keyword("/Users/standfire/test/example.key")
    TIMEINT not found in keyword file, default is 10 years
    >>> stand001.keywords
    {'TIMEINT': 10, 'NUMCYCLE': 10, 'INVYEAR': 2010, 'SVS': 15, 'FUELOUT': 1}

    The keyword file is setup to simulate 100 years at a time interval of 10
    years. Lets change this to only simulate the inventory year.

    >>> stand001.set_num_cycles(0)
    >>> stand001.keywords
    {'TIMEINT': 10, 'NUMCYCLE': 0, 'INVYEAR': 2010, 'SVS': 15, 'FUELOUT': 1}
    >>> stand001.run_fvs()

    Now we can write the trees data frame to disk

    >>> stand001.save_trees_by_year(2010)

    .. note:: The argument must match one of the available variant in the
              PyFVS module. Search through standfire/pyfvs/ to see all
              variants
    """

    def __init__(self, variant):
        """Constructor"""

        # class fields
        self.wdir = ""
        self.keywords = {}
        self.num_cyc = 0
        self.time_int = 0
        self.inv_year = 0
        self.fuels = {"trees" : {}, "snags" : {}}

        # detect operating system
        opt_sys = platform.system()

        # import fvs variant specified by constructor argument
        global fvs
        try:
            if opt_sys == "Linux":
                exec("from pyfvs.linux import pyfvs%s" % variant + " as temp")
        except ValueError:
            print "The PyFVS python module or the specified variant does not exist"
        fvs = temp

    def set_keyword(self, keyfile):
        """
        Sets the keyword file to be used in the FVS simulation

        :Date: 2015-8-12
        :Authors: Lucas Wells

        This method will initalize a FVS simulation by registering the
        specified keyword file (.key) with FVS. The working directory of a
        Fvsfuels object will be set to the folder containing the keyword file.
        You can manually change the working directory with Fvsfuels.set_dir().
        This function will also call private methods in this class to extract
        information from the keyword file and set class fields accordingly for
        use in other methods.

        :param keyfile: path/to/keyword_file. This must have a .key extension
        :type keyfile: string

        **Example:**

        >>> from standfire.fuels import Fvsfuels
        >>> test = Fvsfuels("iec")
        >>> test.set_keyword("/Users/standfire/test/example.key")
        """

        cmd = "--keywordfile=%s" % (keyfile,)

        fvs.fvssetcmdline(cmd)

        # make this folder default output directory
        self.set_dir(os.path.dirname(keyfile))

        # read keywords
        self._read_key(keyfile)

        # set class fields
        self._set_num_cycles()
        self._set_time_int()
        self._set_inv_year()

    def set_dir(self, wdir):
        """
        Sets the working directory of a Fvsfuels object

        This method is called by ``Fvsfuels.set_keyword()``. Thus, the default
        working directory is the folder containing the specified keyword file.
        If you wish to store simulation outputs in a different directory then
        use this methods to do so.

        :param wdir: path/to/desired_directory
        :type wdir: string

        :Example:

        >>> from standfire.fuel import Fvsfuels
        >>> test = Fvsfuels("emc")
        >>> test.set_keyword("/Users/standfire/test/example.key")

        Whoops, I would like to store simulation outputs elsewhere...

        >>> test.set_dir("/Users/standfire/outputs/")
        """

        self.wdir = wdir + "/"
        os.chdir(wdir)

    def _read_key(self, keyfile):

        keys = {}

        f = open(keyfile, 'r')
        lines = [f.readline()]
        while lines[-1] != '':
            lines.append(f.readline())
        lines.pop()

        names = ['INVYEAR', 'NUMCYCLE','TIMEINT','FUELOUT','SVS']
        for i in names:
            for e in lines:
                try:
                    if e[0:len(i)].upper() == i.upper():
                        for s in e.split():
                            if s.isdigit():
                                keys[i.upper()] =int(s)
                            elif i.upper()=='FUELOUT':
                                keys[i.upper()]=1
                except:
                    pass

        self.keywords = keys

    def _set_num_cycles(self):
        """
        Private method

        Sets number of cycles for FVS simulation
        """

        if "NUMCYCLE" in self.keywords.keys():
            self.num_cyc = self.keywords["NUMCYCLE"]
        else:
            self.num_cyc = 10
            self.keywords["NUMCYCLE"] = 10
            print "NUMCYCLE not found in keyword file, default is 10 cycles"

    def _set_time_int(self):
        """
        Private method

        Sets time interval in years for FVS simulation
        """

        if "TIMEINT" in self.keywords.keys():
            self.time_int = self.keywords["TIMEINT"]
        else:
            self.time_int = 10
            self.keywords["TIMEINT"] = 10
            print "TIMEINT not found in keyword file, default is 10 years"

    def _set_inv_year(self):
        """
        Private method

        Sets inventory year for FVS simulation
        """

        if "INVYEAR" in self.keywords.keys():
            self.inv_year = self.keywords["INVYEAR"]
        else:
            self.inv_year = 2015
            self.keywords["INVYEAR"] = 10
            print "INVYEAR not found in keyword file, default is 2015"

    def set_num_cycles(self, num_cyc):
        """
        Sets number of cycles for FVS simulation

        :param num_cyc: number of simulation cycles
        :type num_cyc: int
        """

        self.num_cyc = num_cyc
        self.keywords["NUMCYCLE"] = num_cyc

    def set_time_int(self, time_int):
        """
        Sets time interval for FVS simulation

        :param time_int: length of simulation time step
        :type time_int: int
        """

        self.time_int = time_int
        self.keywords["TIMEINT"] = time_int

    def set_inv_year(self, inv_year):
        """
        Sets inventory year for FVS simulation

        :param inv_year: year of the inventory
        :type inv_year: int
        """

        self.inv_year = inv_year
        self.keywords["INVYEAR"] = inv_year

    def run_fvs(self):
        """
        Runs the FVS simulation

        This method run a FVS simulation using the previously specified keyword
        file. The simulation will be paused at each time interval and the trees
        and snag data collected and appended to the fuels attribute of the
        Fvsfuels object.

        **Example:**

        >>> from standfire.fuels import Fvsfuels
        >>> stand010 = Fvsfuels("iec")
        >>> stand010.set_keyword("/Users/standfire/example/test.key")
        >>> stand010.run_fvs()
        >>> stand010.fuels["trees"][2010]
        xloc    yloc    species   dbh     ht    crd    cratio  crownwt0  crownwt1 ...
        33.49  108.58   PIPO     19.43   68.31  8.77     25    33.46      4.3
        24.3    90.4    PIPO     11.46   56.6   5.63     15     6.55     2.33
        88.84  162.98   PIPO     18.63   67.76  9.48     45    75.88     6.89
        ...

        """
        cnt = 0
        print "Simulating..."
        for i in range(self.inv_year, self.inv_year +
                      (self.num_cyc * self.time_int) +
                       self.time_int, self.time_int):
            print "{0}   {1}".format(cnt, i)
            fvs.fvssetstoppointcodes(6,i)
            fvs.fvs()
            svs_attr = self._get_obj_data()
            spcodes = self._get_spcodes()
            self.fuels["trees"][i] = self._get_trees(svs_attr, spcodes)
            self.fuels["snags"][i] = self._get_snags(svs_attr, spcodes)
            cnt += 1

        # close fvs simulation (call twice)
        fvs.fvs()
        fvs.fvs()

    def _get_obj_data(self):
        """
        Private method
        """

        # fields to query
        svs_names = ['objindex', 'objtype', 'xloc', 'yloc']

        # get dimsize to construct np array to hold values
        nsvsobjs,ndeadobjs,ncwdobjs,mxsvsobjs,mxdeadobjs,mxcwdobjs \
                = fvs.fvssvsdimsizes()
        # size the np array
        svs_attrs = np.zeros(shape = (len(svs_names), nsvsobjs))

        # populate array with fields in svs_names
        for par in range(0, len(svs_names)):
            fvs.fvssvsobjdata(svs_names[par], len(svs_names[par]), \
                'get', svs_attrs[par])

        return svs_attrs

    def _get_spcodes(self):
        """
        Private method
        """

        # get four letter plant codes
        spcd = []
        for i in range(0, fvs.fvsdimsizes()[4]+1):
            spcd.append(fvs.fvsspeciescode(i)[2].split(' ')[0])

        return spcd

    def _get_trees(self, svsobjdata, spcodes):
        """
        Private method
        """

        # headers
        header = ['xloc', 'yloc', 'species', 'dbh', 'ht', 'crd',\
        'cratio', 'crownwt0', 'crownwt1', 'crownwt2', 'crownwt3']

        # fields to query
        tree_names = ['id','species', 'dbh', 'ht', 'crwdth', 'cratio', \
                      'crownwt0', 'crownwt1', 'crownwt2', 'crownwt3']

        # get dimsize to construct np array to hold values
        ntrees, ncycles, nplots, maxtrees, maxspecies, maxplots, \
            maxcycles = fvs.fvsdimsizes()

        # size the np array
        tree_attrs = np.zeros(shape = (len(tree_names), ntrees))

        # populate array with fields in tree_names
        for par in range(0, len(tree_names)):
            fvs.fvstreeattr(tree_names[par], len(tree_names[par]), \
                'get', tree_attrs[par])

        # get number of rows in tree_attrs where objtype == 1 (tree)
        l = len(np.where(svsobjdata[1] == 1)[0])

        # mask array based on objtype == 1
        mask = np.zeros_like(svsobjdata)
        mask[:,np.where(svsobjdata[1] != 1)] = 1
        svsobjdata = np.reshape(np.ma.masked_array(svsobjdata, mask)\
        .compressed(), (len(svsobjdata), l))

        # shape array to hold tree data
        lives = np.zeros(shape = (11, (fvs.fvssvsdimsizes()[0]- \
            fvs.fvssvsdimsizes()[1]-fvs.fvssvsdimsizes()[2])))

        # index trees sequentially
        tree_attrs = np.append(tree_attrs, np.arange(1,ntrees+1))
        tree_attrs = np.reshape(tree_attrs, (len(tree_names)+1, ntrees))

        # merge tree data with svs_attrs
        cnt = 0
        for value in tree_attrs[10]:
            inde = np.where(svsobjdata[0] == value)[0]
            if len(inde) != 0:
                for i in inde:
                    lives[0][cnt] = svsobjdata[2][i]
                    lives[1][cnt] = svsobjdata[3][i]
                    tree_id = np.where(tree_attrs[10] == value)[0]
                    if len(tree_id) != 0:
                        for e in range(2,11,1):
                            lives[e][cnt] = tree_attrs[e-1][tree_id[0]]
                    cnt += 1

        # divide FVS crwdth output by 2 to match SVS 'crd'
        lives[5] = np.divide(lives[5], 2.)

        # convert spcd value to plant_code
        # numpy does not allow multiple dtypes, convert to list of lists
        lives = lives.tolist()
        lives = [ map(lambda x: round(x, 2), elem) for elem in lives ]
        for tre in range(0, len(lives[2])):
            lives[2][tre] = spcodes[int(lives[2][tre])]

        df = pd.DataFrame(lives, header).transpose()

        return df

    def _get_snags(self, svsobjdata, spcodes):
        """
        Private method
        """

        # headers
        header = ['xloc', 'yloc', 'snagspp', 'snagdbh', 'snaglen', 'snagfdir',\
        'snagstat', 'snagyear', 'snagwt0', 'snagwt1', 'snagwt2', 'snagwt3']

        # fields to query
        snag_names = ['snagspp', 'snagdbh',\
        'snaglen', 'snagfdir', 'snagstat', 'snagyear', 'snagwt0',\
        'snagwt1', 'snagwt2', 'snagwt3']

        # get dimsize to construct np array to hold values
        nsvsobjs,ndeadobjs,ncwdobjs,mxsvsobjs,mxdeadobjs,mxcwdobjs \
                = fvs.fvssvsdimsizes()

        # size the np array
        snag_attrs = np.zeros(shape = (len(snag_names), ndeadobjs))

        # populate array with fields in tree_names
        for par in range(0, len(snag_names)):
            fvs.fvssvsobjdata(snag_names[par], len(snag_names[par]), \
                'get', snag_attrs[par])

        # get number of rows in tree_attrs where objtype == 2 (snag)
        l = len(np.where(svsobjdata[1] == 2)[0])

        # mask array based on objtype == 2
        mask = np.zeros_like(svsobjdata)
        mask[:,np.where(svsobjdata[1] != 2)] = 1
        svsobjdata = np.reshape(np.ma.masked_array(svsobjdata, mask)\
        .compressed(), (len(svsobjdata), l))

        # flip array to match svs_attrs
        snag_attrs = np.fliplr(snag_attrs)

        # add svs_attrs
        snags = np.reshape(np.append(svsobjdata[2:],snag_attrs), \
            (12,ndeadobjs))

        # convert spcd value to plant_code
        # numpy does not allow multiple dtypes, convert to list of lists
        snags = snags.tolist()
        snags = [ map(lambda x: round(x, 2), elem) for elem in snags ]
        for sng in range(0, len(snags[2])):
            snags[2][sng] = spcodes[int(snags[2][sng])]

        df = pd.DataFrame(snags, header).transpose()

        return df

    def get_simulation_years(self):
        """
        Returns a list of the simulated years

        :return: simulated year
        :rtype: list of integers
        """

        return self.fuels['trees'].keys()

    def get_trees(self, year):
        """
        Returns pandas data fram of the trees by indexed year

        :param year: simulation year of the data frame to return
        :type year: int
        :return: data frame of trees at indexed year
        :rtype: pandas dataframe

        .. note:: If a data frame for the specified year does not exist then
                  a message will be printed to the console.
        """

        if year in self.fuels["trees"].keys():
            return self.fuels["trees"][year]
        else:
            print "ERROR: the specified year does not exist"

    def get_snags(self, year):
        """
        Returns pandas data fram of the snags by indexed year

        :param year: simulation year of the data frame to return
        :type year: int
        :return: data frame of snags at indexed year
        :rtype: pandas dataframe

        .. note:: If a data frame for the specified year does not exist then
                  a message will be printed to the console.
        """

        if year in self.fuels["snags"].keys():
            return self.fuels["snags"][year]
        else:
            print "ERROR: the specified year does not exist"

    def get_standid(self):
        """
        Returns stand ID as defined in the keyword file of the class instance

        :return: stand ID value
        :rtype: string
        """

        return fvs.fvsstandid()[0].split(' ')[0]

    def save_all(self):
        """
        Writes all data frame in the ``fuels`` attribute of the class to the
        specified working directory. Output file are .csv.
        """

        standid = self.get_standid()

        for i in self.fuels.keys():
            for e in self.fuels[i]:
                self.fuels[i][e].to_csv(self.wdir +
                          "{0}_{1}_{2}.csv".format(standid, i, e), index=False)

    def save_trees_by_year(self, year):
        """
        Writes tree data frame at indexed year to .csv in working directory
        """

        standid = self.get_standid()

        self.fuels["trees"][year].to_csv(self.wdir +
                 "{0}_{1}_{2}.csv".format(standid, "trees", year), index=False)

    def save_snags_by_year(self, year):
        """
        Writes snag data frame at indexed year to .csv in working directory
        """

        standid = self.get_standid()

        self.fuels["snags"][year].to_csv(self.wdir +
                 "{0}_{1}_{2}.csv".format(standid, "snags", year), index=False)


class Inventory(object):
    """
    This class contains methods for converting inventory data to FVS .tre
    format

    This class currently does not read inventory data from an FVS access
    database.  The FVS_TreeInit database first needs to be exported as comma
    delimited values. Multiple stands can be exported in the same file, the
    ``formatFvsTreeFile()`` function will format a .tre string for each stand.
    All column headings must be default headings and unaltered during export.
    You can view the default format by importing this class and typing ``FMT``.
    See the FVS guide [1]_ for more information regarding the format of .tre
    files.

    **Example:**

    >>> from standfire import fuels
    >>> toDotTree = fuels.Inventory()
    >>> toDotTree.read_inventory("path/to/FVS_TreeInit.csv")
    >>> toDotTree.format_fvs_tree_file()
    >>> toDotTree.save()

    :References:

    .. [1] Gary E. Dixon, Essential FVS: A User's Guide to the Forest
        Vegetation Simulator Tech. Rep., U.S. Department of Agriculture , Forest
        Service, Forest Management Service Center, Fort Collins, Colo, USA, 2003.
    """

    def __init__(self):
        """Constructor"""

        # FVS variant
        self.variant = None

        # constant
        self.FMT = {'Plot_ID'       : ['ITRE',      'integer',  [0,3],   None,      None],
                    'Tree_ID'       : ['IDTREE2',   'integer',  [4,6],   None,      None],
                    'Tree_Count'    : ['PROB',      'integer',  [7,12],  None,      None],
                    'History'       : ['ITH',       'integer',  [13,13], 'trees',   0   ],
                    'Species'       : ['ISP',       'alphanum', [14,16], None,      None],
                    'DBH'           : ['DBH',       'real',     [17,20], 'inches',  1   ],
                    'DG'            : ['DG',        'real',     [21,23], 'inches',  1   ],
                    'Ht'            : ['HT',        'real',     [24,26], 'feet',    0   ],
                    'HtTopK'        : ['THT',       'real',     [27,29], 'feet',    0   ],
                    'HTG'           : ['HTG',       'real',     [30,33], 'feet',    1   ],
                    'CrRatio'       : ['ICR',       'integer',  [34,34], None,      None],
                    'Damage1'       : ['IDCD(1)',   'integer',  [35,36], None,      None],
                    'Severity1'     : ['IDCD(2)',   'integer',  [37,38], None,      None],
                    'Damage2'       : ['IDCD(3)',   'integer',  [39,40], None,      None],
                    'Severity2'     : ['IDCD(4)',   'integer',  [41,42], None,      None],
                    'Damage3'       : ['IDCD(5)',   'integer',  [43,44], None,      None],
                    'Severity3'     : ['IDCD(6)',   'integer',  [45,46], None,      None],
                    'TreeValue'     : ['IMC',       'integer',  [47,47], None,      None],
                    'Prescription'  : ['IPRSC',     'integer',  [48,48], None,      None],
                    'Slope'         : ['IPVARS(1)', 'integer',  [49,50], 'percent', None],
                    'Aspect'        : ['IPVARS(2)', 'integer',  [51,53], 'code',    None],
                    'PV_Code'       : ['IPVARS(3)', 'integer',  [54,56], 'code',    None],
                    'TopoCode'      : ['IPVARS(4)', 'integer',  [57,59], 'code',    None],
                    'SitePrep'      : ['IPVARS(5)', 'integer',  [58,58], 'code',    None],
                    'Age'           : ['ABIRTH',    'real',     [59,61], 'years',   0   ]}

    def set_FVS_variant(self, var):
        """
        Sets FVS variant. This is need if converting from USDA plant symbols
        (PSME) to alpha codes (DF). If so, then all stand you wish to process
        must be of the same FVS variant

        :param var: FVS variant ('iec', 'emc' ...)
        :type var: string

        """

        self.variant = var.upper()[:2]

    def read_inventory(self, fname):

        """
        Reads a .csv file containing tree records.

        The csv must be in the correct format as described in ``FMT``.  This
        method check the format of the file by calling a private method
        ``_is_correct_format()`` that raises a value error.

        :param fname: path to and file name of the Fvs_TreeInit.csv file
        :type fname: string

        **Example:**

        >>> from standfire import fuels
        >>> toDotTree = fuels.Inventory()
        >>> toDotTree.readInventory("path/to/FVS_TreeInit.csv")
        >>> np.mean(toDotTree.data['DBH'])
        9.0028318584070828

        The ``read_inventory()`` method stores the data in a pandas data frame.
        There are countless operations that can be performed on these objects.
        For example, we can explore the relationship between diameter and
        height by fitting a linear model

        >>> import statsmodels.formula.api as sm
        >>> fit = sm.ols(formula="HT ~ DBH", data=test.data).fit()
        >>> print fit.params
        Intercept    19.688167
        DBH           2.161420
        dtype: float64
        >>> print fit.summary()
        OLS Regression Results
        ==============================================================================
        Dep. Variable:                     Ht   R-squared:                       0.738
        Model:                            OLS   Adj. R-squared:                  0.736
        Method:                 Least Squares   F-statistic:                     351.8
        Date:                Tue, 07 Jul 2015   Prob (F-statistic):           3.77e-38
        Time:                        08:32:02   Log-Likelihood:                -407.10
        No. Observations:                 127   AIC:                             818.2
        Df Residuals:                     125   BIC:                             823.9
        Df Model:                           1
        Covariance Type:            nonrobust
        ==============================================================================
        coef    std err          t      P>|t|      [95.0% Conf. Int.]
        ------------------------------------------------------------------------------
        Intercept     19.6882      1.205     16.338      0.000        17.303    22.073
        DBH            2.1614      0.115     18.757      0.000         1.933     2.389
        ==============================================================================
        Omnibus:                        2.658   Durbin-Watson:                   0.995
        Prob(Omnibus):                  0.265   Jarque-Bera (JB):                2.115
        Skew:                          -0.251   Prob(JB):                        0.347
        Kurtosis:                       3.385   Cond. No.                         23.8
        ==============================================================================

        Read more about pandas at http://pandas.pydata.org/
        """
        self.data = pd.read_csv(fname)

        # check for correct formating
        self._is_correct_format(self.data)

    def print_format_standards(self):
        """
        Print FVS formating standards

        The FVS formating standard for .tre files as described in the Essenital
        FVS Guide is stored in ``FMT`` as a class attribute.  This method
        is for viewing this format.  The keys of the dictionary are the column
        headings and values are as follows: 0 = variable name, 1 = variable
        type, 2 = column location, 3 = units, and 4 = implied decimal place.

        **Example:**

        >>> toDotTree.print_format_standards()
        {'Plot_ID'       : ['ITRE',      'integer',  [0,3],   None,      None],
         'Tree_ID'       : ['IDTREE2',   'integer',  [4,6],   None,      None],
         'Tree_Count'    : ['PROB',      'integer',  [7,12],  None,      None],
         'History'       : ['ITH',       'integer',  [13,13], 'trees',   0   ],
         'Species'       : ['ISP',       'alphanum', [14,16], None,      None],
         'DBH'           : ['DBH',       'real',     [17,20], 'inches',  1   ],
         'DG'            : ['DG',        'real',     [21,23], 'inches',  1   ],
         'Ht'            : ['HT',        'real',     [24,26], 'feet',    0   ],
         'HtTopK'        : ['THT',       'real',     [27,29], 'feet',    0   ],
         'HTG'           : ['HTG',       'real',     [30,33], 'feet',    1   ],
         'CrRatio'       : ['ICR',       'integer',  [34,34], None,      None],
         'Damage1'       : ['IDCD(1)',   'integer',  [35,36], None,      None],
         'Severity1'     : ['IDCD(2)',   'integer',  [37,38], None,      None],
         'Damage2'       : ['IDCD(3)',   'integer',  [39,40], None,      None],
         'Severity2'     : ['IDCD(4)',   'integer',  [41,42], None,      None],
         'Damage3'       : ['IDCD(5)',   'integer',  [43,44], None,      None],
         'Severity3'     : ['IDCD(6)',   'integer',  [45,46], None,      None],
         'TreeValue'     : ['IMC',       'integer',  [47,47], None,      None],
         'Prescription'  : ['IPRSC',     'integer',  [48,48], None,      None],
         'Slope'         : ['IPVARS(1)', 'integer',  [49,50], 'percent', None],
         'Aspect'        : ['IPVARS(2)', 'integer',  [51,53], 'code',    None],
         'PV_Code'       : ['IPVARS(3)', 'integer',  [54,56], 'code',    None],
         'TopoCode'      : ['IPVARS(4)', 'integer',  [57,59], 'code',    None],
         'SitePrep'      : ['IPVARS(5)', 'integer',  [58,58], 'code',    None],
         'Age'           : ['ABIRTH',    'real',     [59,61], 'years',   0   ]}

        See page 61 and 62 in the Essential FVS Guide.
        """
        print "column heading, variable name, varible type, column width \
                , unit, decimal places\n"
        pprint.pprint(self.FMT)

    def get_fvs_cols(self):
        """
        Get list of FVS standard columns

        :return: FVS standard columns
        :rtype: list of strings
        """
        cols = []
        for i in self.FMT.keys():
            cols.append(i)
        return cols

    def _is_correct_format(self, data):
        """
        Private methods to check for correct inventory file formating
        """
        for i in data.columns:
            if i not in self.get_fvs_cols() and i not in ['Stand_CN', 'Stand_ID'
                    , 'StandPlot_CN', 'StandPlot_ID']:
                raise ValueError("Column heading {0} does not match FVS \
                    standard".format(i))

    def get_stands(self):
        """
        Returns unique stand IDs

        :return: stand IDs
        :rtype: list of strings

        **Example:**

        >>> toDotTree.get_stands()
        ['BR', 'TM', 'SW', HB']
        """
        return self.data["Stand_ID"].unique()

    def filter_by_stand(self, stand_list):
        """
        Filters data by a list of stand IDs

        :param stand_list: List of stand ID to retain in the data. All other
                           stands will be removed
        :type stand_list: python list
        """

        mask = self.data["Stand_ID"].isin(stand_list)
        self.data = self.data.loc[mask]

    def crwratio_percent_to_code(self):
        """
        Converts crown ratio from percent to ICR code

        ICR code is described in the Essential FVS Guide on pages 58 and 59.
        This method should only be used if crown ratios values are percentages
        in the FVS_TreeInit.csv.  If you use this method before calling
        `formatFvsTreeFile()` then you must set the optional argument
        ``cratioToCode`` to ``False``.
        """
        self.data.loc[(self.data["CrRatio"] >= 0) & (self.data["CrRatio"]
            <= 10), "CrRatio"] = 1
        self.data.loc[(self.data["CrRatio"] > 10) & (self.data["CrRatio"]
            <= 20), "CrRatio"] = 2
        self.data.loc[(self.data["CrRatio"] > 20) & (self.data["CrRatio"]
            <= 30), "CrRatio"] = 3
        self.data.loc[(self.data["CrRatio"] > 30) & (self.data["CrRatio"]
            <= 40), "CrRatio"] = 4
        self.data.loc[(self.data["CrRatio"] > 40) & (self.data["CrRatio"]
            <= 50), "CrRatio"] = 5
        self.data.loc[(self.data["CrRatio"] > 50) & (self.data["CrRatio"]
            <= 60), "CrRatio"] = 6
        self.data.loc[(self.data["CrRatio"] > 60) & (self.data["CrRatio"]
            <= 70), "CrRatio"] = 7
        self.data.loc[(self.data["CrRatio"] > 70) & (self.data["CrRatio"]
            <= 80), "CrRatio"] = 8
        self.data.loc[(self.data["CrRatio"] > 80) & (self.data["CrRatio"]
            <= 100), "CrRatio"] = 9

    def convert_sp_codes(self, method='2to4'):
        """
        Converts species codes from 4 letter codes to 2 letter codes
        or vise versa

        :param method: must be either "2to4" or "4to2"
        :type method: string
        """

        # check if variant has been set
        if self.variant == None:
            raise("You must set the FVS variant before converting to alpha codes")

        if self.variant in eastern:
            side = 'eastern'
        else:
            side = 'western'

        # get relative path to this module
        this_dir = os.path.dirname(__file__)

        # load species crosswalk database
        crosswalk = cPickle.load(open(this_dir + '/data/species_crosswalk.p', 'rb'))

        # get all species in all stands
        uniq_sp = self.data["Species"].unique()

        for i in uniq_sp:
            if i in crosswalk[side]:
                self.data.loc[self.data["Species"] == i, "Species"] = crosswalk[side][i][self.variant]
            else:
                print("{0} is not recognized by FVS as a {1} species\ndefaulting to unknown species".format(i, side))
                self.data.loc[self.data["Species"] == i, "Species"] = 'OT'

    def format_fvs_tree_file(self, cratio_to_code = True):
        """
        Converts data in FVS_TreeInit.csv to FVS .tre format

        This methods reads entries in the pandas data frame (``self.data``) and
        writes them to a formated text string following FVS .tre data formating
        standards shown in ``FMT``.  If multiple stands exist in ``self.data``
        then each stand will written as a (key,value) pair in
        ``self.fvsTreeFile`` where the key is the stand ID and the value is the
        formated text string.

        :param cratio_to_code: default = True
        :type cratio_to_code: boolean

        .. note:: If the ``crwratio_percent_to_code()`` methods has
                  been called prior to call this methods, then the ``cratio_to_code``
                  optional argument must be set to ``False`` to prevent errors in crown
                  ratio values.

        **Example:**

        >>> toDotTree.format_fvs_tree_file()
        >>> toDotTree.fvsTreeFile['Stand_ID_1']
        5   1  5     0PP 189    65        3                 0 0
        5   2  15    0PP 110    52        2                 0 0
        5   3  5     0PP 180    64        5                 0 0
        5   4  14    0PP 112    56        3                 0 0
        5   5  6     0PP 167    60        4                 0 0
        5   6  5     0PP 190    60        5                 0 0
        5   7  7     0PP 161    62        3                 0 0
        5   8  86    0PP 46     37        1                 0 0
        5   9  10    0PP 130    50        2                 0 0
        5   10 5     0PP 182    60        3                 0 0
        5   11 8     9PP 144    50                          0 0
        6   1  16    0PP 107    42        4                 0 0
        6   2  109   0PP 41     27        2                 0 0
        ...
        """

        self.fvsTreeFile = {}

        # convert crown ratios from percent to int values from 0-9
        if cratio_to_code:
            self.crwratio_percent_to_code()

        # replace nan with empty string
        self.data = self.data.fillna(' ')

        for i in self.get_stands():
            df = self.data.set_index(['Stand_ID']).loc[i]
            # TODO: make this a method
            df["Tree_ID"] = range(1, len(df["Tree_ID"]) + 1)
            tmp = ""
            for j in range(0, len(df)):
                for k in df.columns:
                    if k in self.FMT.keys():
                        tmp += '{0:{width}}'.format([str(df[k][j])
                                .split('.')[0], ''.join(str(df[k][j])
                                .split('.'))][self.FMT[k][4] == 1]
                                , width = (self.FMT[k][2][1] - self.FMT[k][2][0])+1)
                tmp += '\n'
            self.fvsTreeFile[i] = tmp


    def save(self, outputPath):
        """
        Writes formated fvs tree files to specified location

        If multiple stands exist in the FVS_TreeInit then the same number of
        files will be created in the specified directory.  The file names
        will be the same as the Stand_ID with a ``.tre`` extension.

        :param outputPath: directory to store output .tre files
        :type outputPath: string

        .. note:: This method will throw an error if it is called prior to the
                  ``format_fvs_tree_file()`` method.
        """
        for i in self.fvsTreeFile.keys():
            with open(outputPath + i + '.tre', 'w') as f:
                f.write(self.fvsTreeFile[i])
