#!python2
################################################################################
#----------#
# fuels.py #
#----------#

"""
This module is the interface to FVS. Given a FVS variant name, a keyword file
and the corresponding tree file, a user can run an FVS simulation
(``Fvsfuels class``) and request various fuels information from individual trees
(``Fuelcalc class``) The Fvsfuels class will also produce the 4 fuels files
needed for the Capsis fuel matrix generator.

Currently FVS MC Access database querying not available on all platforms.
If a user has a keyword file that points to a MS Access database, then the user
can generate a tree file by exporting the FVS_TreeInit data table from the
Access database to an comma-delimited (.csv) file and pass it through the
``Inventory class``. The output will be the same inventory present in the .mdb
file, but formatted to FVS .tre file standards.
"""

# module imports
import os
import pprint
import platform
import cPickle
import math
import csv
import importlib as imp
import numpy as np
import pandas as pd

# meta
__authors__ = "Team STANDFIRE"
__copyright__ = "Copyright 2017, STANDFIRE"
__credits__ = ["Greg Cohn", "Brett Davis", "Matt Jolly", "Russ Parsons", "Lucas Wells"]
__license__ = "GPL"
__maintainer__ = "Lucas Wells"
__email__ = "bluegrassforestry@gmail.com"
__status__ = "Development"
__version__ = "1.1.4a" # Previous version: "1.1.3a"

# FVS variant globals
EASTERN = {"CS", "LS", "NE", "SN"}
WESTERN = {"AK", "BM", "CA", "CI", "CR", "EC", "ID", "NC", "KT", "NI", "PN",
           "SO", "TT", "UT", "WC", "WS"}

class Fvsfuels(object):
    """
    A Fvsfuels object is used to calculate component fuels at the individual
    tree level using the Forest Vegetation Simulator. To create an instance
    of this class you need two items: a keyword file (.key) and tree list file
    (.tre) with the same prefix as the keyword file. If you don't already have
    a tree list file then you can use ``fuels.Inventory`` class to generate
    one from a .csv file exported from an FVS database.

    :param variant: FVS variant to be imported
    :type variant: string

    **Example:**

    A basic example to extract live canopy biomass for individual trees during
    year of inventory

    >>> from fuels import Fvsfuels
    >>> stand001 = Fvsfuels("iec")
    >>> stand001.set_keyword("/Users/standfire/test/example.key")
    TIMEINT not found in keyword file, default is 10 years
    >>> stand001.keywords
    {"TIMEINT": 10, "NUMCYCLE": 10, "INVYEAR": 2010, "SVS": 15, "FUELOUT": 1}
    >>> stand001.run_fvs()

    Now we can write the trees data frame to disk

    >>> stand001.save_trees_by_year(2010)

    .. note:: The argument must match one of the available variants in the
              PyFVS module. See below for a list of all available variants.

    **Available FVS variants:**

    =============================================   ============
    Variant                                         Abbreviation
    =============================================   ============
    Southeast Alaska and Coastal British Columbia   ak
    Blue Mountains                                  bmc
    Inland California and Southern Cascades         cac
    Central Idaho                                   cic
    Central Rockies                                 crc
    Central States                                  cs
    East Cascades                                   ecc
    Eastern Montana                                 emc
    Inland Empire                                   iec
    Klamath Mountains                               ncc
    Kootenai, Kaniksu, and Tally Lake               ktc
    Lake States                                     ls
    Northeast                                       ne
    Pacific Northwest Coast                         pnc
    Southern                                        sn
    South Central Oregon and Northeast California   soc
    Tetons                                          ttc
    Utah                                            utc
    Westside Cascades                               wcc
    Western Sierra Nevada                           wsc
    =============================================   ============
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

        # instantiate fvs object using variant specified by constructor argument
        try:
            if opt_sys == "Linux":
                var_file = "pyfvs.linux.pyfvs%s" % variant
                self.fvs = imp.import_module(var_file)
            if opt_sys == "Windows":
                var_file = "pyfvs.win.pyfvs%s" % variant
                self.fvs = imp.import_module(var_file)
            if opt_sys == "Osx":
                var_file = "pyfvs.osx.pyfvs%s" % variant
                self.fvs = imp.import_module(var_file)
        except ValueError:
            print "The PyFVS python module or the specified variant does not exist"

    def set_keyword(self, keyfile):
        """
        Sets the keyword file to be used in the FVS simulation

        This method will initalize an FVS simulation by registering the
        specified keyword file (.key) with FVS. The working directory of a
        Fvsfuels object will be set to the folder containing the keyword file.
        You can manually change the working directory with ``Fvsfuels.set_dir()``.
        This function will also call private methods in this class to extract
        information from the keyword file and set class fields accordingly for
        use in other methods.

        :param keyfile: path/to/keyword_file. This must have a .key extension
        :type keyfile: string

        **Example:**

        >>> from fuels import Fvsfuels
        >>> test = Fvsfuels("iec")
        >>> test.set_keyword("/Users/standfire/test/example.key")
        """

        # pass keyword file to fvs object
        cmd = "--keywordfile=%s" % (keyfile,)
        self.fvs.fvssetcmdline(cmd)

        # make this folder default output directory
        self.set_dir(os.path.dirname(keyfile))

        # read keywords
        self._read_key(keyfile)

        # set class fields (from keyword file)
        self._set_num_cycles()
        self._set_time_int()
        self._set_inv_year()

    def set_dir(self, wdir):
        """
        Sets the working directory of a Fvsfuels object

        This method is called by ``Fvsfuels.set_keyword()``. Thus, the default
        working directory is the folder containing the specified keyword file.
        If you wish to store simulation outputs in a different directory then
        use this method to do so.

        :param wdir: path to desired directory
        :type wdir: string

        **Example:**

        >>> from fuels import Fvsfuels
        >>> test = Fvsfuels("emc")
        >>> test.set_keyword("/Users/standfire/test/example.key")
        >>> test.set_dir("/Users/standfire/outputs/")
        """

        self.wdir = wdir + "/"
        os.chdir(wdir)

    def _read_key(self, keyfile):
        """
        Pseudo-private method
        Parses keyword file and extracts inventory year, number of cycles,
        time interval, Stand Visualization System (SVS) parameters and the
        generation of an FFE fuel report.
        """

        # read keyword file
        keys = {}
        key_file = open(keyfile, "r")
        lines = [key_file.readline()]
        while lines[-1] != "":
            lines.append(key_file.readline())
        lines.pop()

        # extract certain keyword values
        names = ["INVYEAR", "NUMCYCLE", "TIMEINT", "FUELOUT", "SVS"]
        for name in names:
            for line in lines:
                try:
                    if line[0:len(name)].upper() == name.upper():
                        for element in line.split():
                            if element.isdigit():
                                keys[name.upper()] = int(element)
                            elif name.upper() == "FUELOUT":
                                keys[name.upper()] = 1
                except StandardError:
                    pass

        self.keywords = keys

    def _set_num_cycles(self):
        """
        Pseudo-private method

        Sets the initial number of cycles for an FVS simulation based on the
        keyword file.
        """

        if "NUMCYCLE" in self.keywords.keys():
            self.num_cyc = self.keywords["NUMCYCLE"]
        else:
            self.num_cyc = 10
            self.keywords["NUMCYCLE"] = 10
            print "NUMCYCLE not found in keyword file, default is 10 cycles"

    def _set_time_int(self):
        """
        Pseudo-private method

        Sets the initial time interval in years for FVS simulation based on the
        keyword file.
        """

        if "TIMEINT" in self.keywords.keys():
            self.time_int = self.keywords["TIMEINT"]
        else:
            self.time_int = 10
            self.keywords["TIMEINT"] = 10
            print "TIMEINT not found in keyword file, default is 10 years"

    def _set_inv_year(self):
        """
        Pseudo-private method

        Sets the initial inventory year for FVS simulation based on the
        keyword file.
        """

        if "INVYEAR" in self.keywords.keys():
            self.inv_year = self.keywords["INVYEAR"]
        else:
            #B should there be a default here?
            self.inv_year = 2015
            self.keywords["INVYEAR"] = 10
            print "INVYEAR not found in keyword file, default is 2015"

# B support for modifying these parameters in FVS has not yet been implemented...
##    def set_num_cycles(self, num_cyc):
##        """
##        Allows the user to reset the number of cycles for FVS simulation
##
##        :param num_cyc: number of simulation cycles
##        :type num_cyc: integer
##        """
##
##        self.num_cyc = num_cyc
##        self.keywords["NUMCYCLE"] = num_cyc
##
##    def set_time_int(self, time_int):
##        """
##        Allows the user to reset the time interval for FVS simulation
##
##        :param time_int: length of simulation time step
##        :type time_int: integer
##        """
##
##        self.time_int = time_int
##        self.keywords["TIMEINT"] = time_int
##
##    def set_inv_year(self, inv_year):
##        """
##        Allows the user to reset the inventory year for FVS simulation
##
##        :param inv_year: year of the inventory
##        :type inv_year: integer
##        """
##
##        self.inv_year = inv_year
##        self.keywords["INVYEAR"] = inv_year

    def set_stop_point(self, code=1, year=-1):
        """
        Function to set the FVS stop point code and year. This causes FVS to
        pause at points defined by the stop point code and year.

        :param code: stop point code (default=1)
        :type code: integer
        :param year: stop point year (default=-1)
        :type year: integer

        .. note:: year=0 means never stop and year=-1 means stop every cycle

        ===============    ==========
        stop point code    Definition
        ===============    ==========
        0                  Never stop
        -1                 Stop at every location
        1                  Stop just before first call to Event Monitor
        2                  Stop just after first call to Event Monitor
        3                  Stop just before second call to Event Monitor
        4                  Stop just after second call to Event Monitor
        5                  Stop after growth and mort has been computed, but before applied
        6                  Stop just before the ESTAB routine is called
        ===============    ==========
        """

        self.fvs.fvssetstoppointcodes(code, year)

    def run_fvs(self):
        """
        This method runs an FVS simulation using the specified keyword file.
        The simulation will be paused at each time cycle as defined by the stop
        point codes and the time interval (TIMEINT from the keyword file).
        During the pause tree and snag data are collected and appended to the
        fuels attribute of the Fvsfuels object.

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

        FVS return codes from open-fvs wiki. Printed to the console:

        * -1: indicates that FVS has not been started
        *  0: indicates that FVS is in a good running state
        *  1: indicates that FVS has detected an error of some kind and should
           not be used until reset by specifying new input
        *  2: indicates that FVS has finished processing all the stands; new
           input can be specified
        """

        # set fvs stop point codes
        # Currently hardwired. Future: pass values to run_fvs based on type of
        # run or set them in mini prior to calling run_fvs
        self.set_stop_point(1, -1)

        cnt = 0
        print "Simulating..."
        for year in range(self.inv_year, self.inv_year +
                          (self.num_cyc * self.time_int) +
                          self.time_int, self.time_int):
            print "{0}   {1}".format(cnt, year)
            return_code = self.fvs.fvs() # return_code reflects FVS status
            print "Return code: ", return_code
            svs_attr = self._get_obj_data() # numpy array
            spcodes = self._get_spcodes()
            # collect interval tree and snag data and add to fuels dictionary
            self.fuels["trees"][year] = self._get_trees(svs_attr, spcodes)
            self.fuels["snags"][year] = self._get_snags(svs_attr, spcodes)
            cnt += 1

        # close fvs simulation (call twice)
        self.fvs.fvs()
        self.fvs.fvs()

    def _get_obj_data(self):
        """
        Pseudo-private method

        Sets up a 4 x [total objects in SVS storage] numpy array and returns it
        to run_fvs method. Initially populated with zeros.
        """

        # fields to query
        svs_names = ["objindex", "objtype", "xloc", "yloc"]

        # get dimsize to construct np array to hold values
        num_svs_objs = self.fvs.fvssvsdimsizes()[0]
        # size the np array
        svs_attrs = np.zeros(shape=(len(svs_names), num_svs_objs))

        # populate array with fields in svs_names
        for par in range(0, len(svs_names)):
            self.fvs.fvssvsobjdata(svs_names[par], len(svs_names[par]), \
                "get", svs_attrs[par])

        return svs_attrs

    def _get_spcodes(self):
        """
        Pseudo-private method
        Generates a list of USDA plant codes (up to six digits, e.g . PSME, PIMO3)
        that occur in the fvs object and returns the result to the run_fvs method.

        :return: USDA plant codes
        :rtype: list
        """

        # get four letter plant codes #B not 4, check if 4 is expected down the road
        spcd = []
        for i in range(0, self.fvs.fvsdimsizes()[4]+1):
            spcd.append(self.fvs.fvsspeciescode(i)[2].split(" ")[0])

        return spcd

    def _get_trees(self, svsobjdata, spcodes):
        """
        Pseudo-private method
        Populates a 11 x [ntrees] numpy array (tree_attrs) with tree attributes.
        Returns a pandas data frame.

        :param svsobjdata: tree and snag attributes
        :type svsobjdata: numpy array
        :param spcodes: species codes
        :type spcodes: list

        :return: tree attributes
        :rtype: pandas data frame
        """

        # headers
        header = ["xloc", "yloc", "species", "dbh", "ht", "crd",\
        "cratio", "crownwt0", "crownwt1", "crownwt2", "crownwt3"]

        # fields to query
        tree_names = ["id", "species", "dbh", "ht", "crwdth", "cratio", \
                      "crownwt0", "crownwt1", "crownwt2", "crownwt3"]

        # get dimsize to construct np array to hold values
        ntrees = self.fvs.fvsdimsizes()[0]

        # size the np array
        tree_attrs = np.zeros(shape=(len(tree_names), ntrees))

        # populate array with fields in tree_names
        for par in range(0, len(tree_names)):
            self.fvs.fvstreeattr(tree_names[par], len(tree_names[par]), \
                "get", tree_attrs[par])

        # get number of rows in tree_attrs where objtype == 1 (tree)
        num_tree_rows = len(np.where(svsobjdata[1] == 1)[0])

        # mask array based on objtype == 1
        mask = np.zeros_like(svsobjdata)
        mask[:, np.where(svsobjdata[1] != 1)] = 1
        svsobjdata = np.reshape(np.ma.masked_array(svsobjdata, mask)\
        .compressed(), (len(svsobjdata), num_tree_rows))

        # shape array to hold tree data
        lives = np.zeros(shape=(11, (self.fvs.fvssvsdimsizes()[0]- \
            self.fvs.fvssvsdimsizes()[1]-self.fvs.fvssvsdimsizes()[2])))

        # index trees sequentially
        tree_attrs = np.append(tree_attrs, np.arange(1, ntrees+1))
        tree_attrs = np.reshape(tree_attrs, (len(tree_names)+1, ntrees))

        # merge tree data with svs_attrs
        cnt = 0
        for value in tree_attrs[10]:
            inde = np.where(svsobjdata[0] == value)[0]
            #B: need to use len(SEQ) for numpy arrays
            if len(inde) != 0: #pylint: disable=len-as-condition
                for i in inde:
                    lives[0][cnt] = svsobjdata[2][i]
                    lives[1][cnt] = svsobjdata[3][i]
                    tree_id = np.where(tree_attrs[10] == value)[0]
                    if len(tree_id) != 0:#pylint: disable=len-as-condition
                        for attr in range(2, 11, 1):
                            lives[attr][cnt] = tree_attrs[attr-1][tree_id[0]]
                    cnt += 1

        # divide FVS crwdth output by 2 to match SVS "crd"
        lives[5] = np.divide(lives[5], 2.) # pylint: disable=no-member

        # convert spcd value to plant_code
        # numpy does not allow multiple dtypes, convert to list of lists
        lives = lives.tolist()
        lives = [[round(x, 2) for x in elem] for elem in lives]
        for tre in range(0, len(lives[2])):
            lives[2][tre] = spcodes[int(lives[2][tre])]

        df_lives = pd.DataFrame(lives, header).transpose()

        return df_lives

    def _get_snags(self, svsobjdata, spcodes):
        """
        Pseudo-private method
        Populates a 12 x [ndeadobjs] numpy array (snag_attrs) with snag
        attributes. Returns a pandas data frame.

        :param svsobjdata: tree and snag attributes
        :type svsobjdata: numpy array
        :param spcodes: species codes
        :type spcodes: list

        :return: snag attributes
        :rtype: pandas data frame
        """

        # headers
        header = ["xloc", "yloc", "snagspp", "snagdbh", "snaglen", "snagfdir",\
        "snagstat", "snagyear", "snagwt0", "snagwt1", "snagwt2", "snagwt3"]

        # fields to query
        snag_names = ["snagspp", "snagdbh",\
        "snaglen", "snagfdir", "snagstat", "snagyear", "snagwt0",\
        "snagwt1", "snagwt2", "snagwt3"]

        # get dimsize to construct np array to hold values
        ndeadobjs = self.fvs.fvssvsdimsizes()[1]

        # size the np array
        snag_attrs = np.zeros(shape=(len(snag_names), ndeadobjs))

        # populate array with fields in tree_names
        for par in range(0, len(snag_names)):
            self.fvs.fvssvsobjdata(snag_names[par], len(snag_names[par]), \
                "get", snag_attrs[par])

        # get number of rows in tree_attrs where objtype == 2 (snag)
        num_snag_rows = len(np.where(svsobjdata[1] == 2)[0])

        # mask array based on objtype == 2
        mask = np.zeros_like(svsobjdata)
        mask[:, np.where(svsobjdata[1] != 2)] = 1
        svsobjdata = np.reshape(np.ma.masked_array(svsobjdata, mask)\
        .compressed(), (len(svsobjdata), num_snag_rows))

        # flip array to match svs_attrs
        snag_attrs = np.fliplr(snag_attrs)

        # add svs_attrs
        snags = np.reshape(np.append(svsobjdata[2:], snag_attrs), \
            (12, ndeadobjs))

        # convert spcd value to plant_code
        # numpy does not allow multiple dtypes, convert to list of lists
        snags = snags.tolist()
        snags = [[round(x, 2) for x in elem] for elem in snags]
        for sng in range(0, len(snags[2])):
            snags[2][sng] = spcodes[int(snags[2][sng])]

        df_snags = pd.DataFrame(snags, header).transpose()

        return df_snags

    def get_simulation_years(self):
        """
        Returns a list of the simulated years

        :return: simulated year
        :rtype: list of integers
        """

        return self.fuels["trees"].keys()

    def get_trees(self, year):
        """
        Returns pandas data frame of the trees by indexed year

        :param year: simulation year of the data frame to return
        :type year: integer
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
        Returns pandas data frame of the snags by indexed year

        :param year: simulation year of the data frame to return
        :type year: integer
        :return: data frame of snags at indexed year
        :rtype: pandas dataframe

        .. note:: If a data frame for the specified year does not exist then
                  a message will be printed to the console.
        """

        if year in self.fuels["snags"].keys():
            return self.fuels["snags"][year]
        else:
            print "ERROR: the specified year does not exist"

    def get_standid(self): #if fvs is global why does this exist?
        """
        Returns stand ID as defined in the keyword file of the class instance

        :return: stand ID value
        :rtype: string
        """

        return self.fvs.fvsstandid()[0].split(" ")[0]

    def save_all(self):
        """
        Allows the user to create output files of trees and snags by year for
        all years simulated. Output file are in the .csv format and are named:
        <standid>_<trees/snags>_<year>.csv
        """

        standid = self.get_standid()

        for i in self.fuels: # trees and snags
            for year in self.fuels[i]:
                self.fuels[i][year].to_csv(self.wdir + "{0}_{1}_{2}.csv".format\
                    (standid, year, i), quoting=3, index=False)

    def save_trees_by_year(self, year):
        """
        Writes tree data frame at indexed year to .csv in working directory

        :param year: simulated year
        :type year: integer
        """

        standid = self.get_standid()
        out_file = "{0}_{1}_{2}.csv".format(standid, year, "trees")

        self.fuels["trees"][year].to_csv(self.wdir + out_file,
                                         quoting=csv.QUOTE_NONNUMERIC,
                                         index=False)

    def save_snags_by_year(self, year):
        """
        Writes snag data frame at indexed year to .csv in working directory

        :param year: simulated year
        :type year: integer
        """

        standid = self.get_standid()
        out_file = "{0}_{1}_{2}.csv".format(standid, year, "snags")

        self.fuels["snags"][year].to_csv(self.wdir + out_file, quoting=3,
                                         index=False)


class FuelCalc(object):
    """
    This class calculates several fuel attributes and appends them to the FVS
    output tree list generated by the Fvsfuels class, generating a new .csv file.

    :param trees: FVS output tree list
    :type trees: .csv file or pandas data frame
    """

    def __init__(self, trees):
        """
        Constructor
        """

        # type check and handle accordingly
        if isinstance(trees, pd.DataFrame):
            self.trees = trees
        elif isinstance(trees, str):
            try:
                self.trees = pd.read_csv(trees)
            except:
                raise TypeError("String argument must point to .csv file")
        else:
            raise TypeError("argument type must be either an instance of "
                            "Pandas.DataFrame() or a string indicating a path "
                            "to a comma-delimted file")

        # calculate crown base height and crown height
        self.get_crown_base_ht()
        self.get_crown_ht()

        # conversion code for double unit conversion check
        self.cvt_code = None

    def get_crown_ht(self):
        """
        Calculates crown height for each trees based on crown ratio. This
        value is added to the data frame

        .. math:: c_{ratio}*h

        """

        if "crown_ht" not in self.trees:
            self.trees["crown_ht"] = ""

        self.trees["crown_ht"] = self.trees["cratio"]/100. * self.trees["ht"]


    def get_crown_base_ht(self):
        """
        Calculates crown base height for each tree based on crown ratio and
        tree height. This value is added to the data frame

        .. math:: h - (c_{ratio}*h)

        """

        if "base_ht" not in self.trees:
            self.trees["base_ht"] = ""

        self.trees["base_ht"] = (self.trees["ht"] - (self.trees["cratio"]/100.)
                                 * self.trees["ht"])

    def set_crown_geometry(self, sp_geom_dict):
        """
        Appends crown geometry to each tree in the data frame conditional on
        species.

        :param sp_geom_dict: dictionary of species-specific crown geometries
        :type sp_geom_dict: python dictionary

        :Example:

            >>> sp_dict = {"PIPO" : "cylinder", "PSME" : "frustum"}
            >>> fuels.set_crown_geometry(sp_dict)

        """

        if "geom" not in self.trees:
            self.trees["geom"] = ""

        for i in self.get_species_list():
            if sp_geom_dict[i] not in ["rectangle", "cylinder", "cone", "frustum"]:
                val_err = "The specified geometry '{0}' is not valid".format(sp_geom_dict[i])
                raise ValueError(val_err)
            self.trees["geom"][self.trees["species"] == i] = sp_geom_dict[i]


    def get_species_list(self):
        """
        Return set of species existing in trees file supplied to constructor

        :return: unique list of species
        :rtype: list

        .. note:: This methods is useful when assigning geometries by species.
                  A user can first retrieve the species list then use it to
                  assigne crown geometries
        """

        return pd.unique(self.trees["species"])

    def calc_crown_volume(self):
        """
        Calculates crown volume based on geometry and crown dimensions
        """

        if "vol" not in self.trees:
            self.trees["vol"] = ""

        self.trees["vol"][self.trees["geom"] == "rectangle"] = \
            self.rectangle_volume(self.trees["crd"]*2, self.trees["crown_ht"])
        self.trees["vol"][self.trees["geom"] == "cylinder"] = \
            self.cylinder_volume(self.trees["crd"], self.trees["crown_ht"])
        self.trees["vol"][self.trees["geom"] == "cone"] = \
            self.cone_volume(self.trees["crd"], self.trees["crown_ht"])
        self.trees["vol"][self.trees["geom"] == "frustum"] = \
            self.frustum_volume(self.trees["crd"], self.trees["crown_ht"])

    def calc_bulk_density(self):
        """
        Calculates crown bulk density based on crown volume and biomass weight
        (weight/vol)
        """

        fields = ["bd_foliage", "bd_1hr", "bd_10hr", "bd_100hr"]

        for i in fields:
            if i not in self.trees:
                self.trees[i] = ""

        self.trees["bd_foliage"] = self.trees["crownwt0"] / self.trees["vol"]
        self.trees["bd_1hr"] = self.trees["crownwt1"] / self.trees["vol"]
        self.trees["bd_10hr"] = self.trees["crownwt2"] / self.trees["vol"]
        self.trees["bd_100hr"] = self.trees["crownwt3"] / self.trees["vol"]

    @staticmethod
    def frustum_volume(big_r, height, small_r=0.5):
        """
        Returns the volume of a frustum

        :param small_r: small (top) radius
        :type small_r: float
        :param height: height
        :type height: float
        :param big_r: big (bottom) radius
        :type big_r: float
        :return: volume
        :rtype: float

        .. math:: \\frac{\\Pi*height}{3}(big_r^2+small_r*big_r+small_r^2)

        """

        return (1.0/3.0)*math.pi*height*(big_r**2 + small_r*big_r + small_r**2)

    @staticmethod
    def cone_volume(radius, height):
        """
        Returns the volume of a cone

        :param radius: radius
        :type radius: float
        :param height: height
        :type height: float
        :return: volume
        :rtype: float

        .. math:: \\Pi*radius^2 \\frac{height}{3}

        """

        return (math.pi * radius**2) * (height/3)

    @staticmethod
    def cylinder_volume(radius, height):
        """
        Returns the volume of a cylinder

        :param radius: radius
        :type radius: float
        :param height: height
        :type height: float
        :return: volume
        :rtype: float

        .. math:: \\Pi*radius^2*height

        """

        return math.pi * radius**2 * height

    @staticmethod
    def rectangle_volume(width, height):
        """
        Returns the volume of a rectangle

        :param width: width
        :type width: float
        :param height: height
        :type height: float
        :return: volume
        :rtype: float

        .. math:: width^2*height

        """

        return width*width*height

    def convert_units(self, from_to=1):
        """
        Convert all units in data frame

        :param from_to: 1 = english to metric; 2 = metric to english (default=1)
        :type from_to: integer

        .. note:: if this method is called more than once on the same instance
                  of a data frame with the same conversion code a warning will
                  be printed to the console
        """

        if self.cvt_code == from_to:
            print "This data was previously converted using the same coversion code"
        else:
            self.cvt_code = from_to

        # setup conversion dictionary
        if from_to == 1:
            tree_attrib = {"dbh": 2.54, "ht": 0.3048, "crd": 0.3048, "crownwt0": 0.453592,
                           "crownwt1": 0.453592, "crownwt2": 0.453592, "crownwt3": 0.453592,
                           "base_ht": 0.3048, "crown_ht": 0.3048, "vol": 0.0283168,
                           "bd_foliage": 16.1085, "bd_1hr": 16.1085, "bd_10hr": 16.1085,
                           "bd_100hr": 16.1085}

        elif from_to == 2:
            tree_attrib = {"dbh": 0.0328084, "ht": 3.28084, "crd": 3.28084, "crownwt0": 2.20462,
                           "crownwt1": 2.20462, "crownwt2": 2.20462, "crownwt3": 2.20462,
                           "base_ht": 3.28084, "crown_ht": 3.28084, "vol": 35.3147,
                           "bd_foliage": 0.062428, "bd_1hr": 0.062428, "bd_10hr": 0.062428,
                           "bd_100hr": 0.062428}
        else:
            print "The conversion code '{0}' is not recognized".format(from_to)
            return

        # convert units
        for attrib in tree_attrib:
            if attrib in self.trees:
                self.trees[attrib] = self.trees[attrib] * tree_attrib[attrib]


    def save_trees(self, save_to):
        """
        Write trees data frame to specified directory

        :param save_to: directory and filename of file to save
        :type save_to: string

        """

        self.trees.to_csv(save_to, index=False)

class Inventory(object):
    """
    This class contains methods for converting inventory data to FVS .tre
    format.

    This class currently does not read inventory data directly from an FVS access
    database.  The FVS_TreeInit data table in an FVS database first needs to be
    exported as comma delimited values. Multiple stands can be exported in the
    same file, the ``format_fvs_tree_file()`` method will format a .tre string
    for each stand. All column headings must be default headings and unaltered
    during export. You can view the default format by importing this class and
    and calling ``print_format_standards()``. See the FVS guide [1]_ for more
    information regarding the format of .tre files.

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

        # Initialize data variable
        self.data = None

        # Initialize tree file dictionary
        self.fvs_tree_file = {}

        # constant (spacing increases readability)
        # pylint: disable=bad-whitespace
        self.FMT = {"Plot_ID"       : ["ITRE",      "integer",  [0, 3],   None,      None],
                    "Tree_ID"       : ["IDTREE2",   "integer",  [4, 6],   None,      None],
                    "Tree_Count"    : ["PROB",      "integer",  [7, 12],  None,      None],
                    "History"       : ["ITH",       "integer",  [13, 13], "trees",   0   ],
                    "Species"       : ["ISP",       "alphanum", [14, 16], None,      None],
                    "DBH"           : ["DBH",       "real",     [17, 20], "inches",  1   ],
                    "DG"            : ["DG",        "real",     [21, 23], "inches",  1   ],
                    "Ht"            : ["HT",        "real",     [24, 26], "feet",    0   ],
                    "HtTopK"        : ["THT",       "real",     [27, 29], "feet",    0   ],
                    "HTG"           : ["HTG",       "real",     [30, 33], "feet",    1   ],
                    "CrRatio"       : ["ICR",       "integer",  [34, 34], None,      None],
                    "Damage1"       : ["IDCD(1)",   "integer",  [35, 36], None,      None],
                    "Severity1"     : ["IDCD(2)",   "integer",  [37, 38], None,      None],
                    "Damage2"       : ["IDCD(3)",   "integer",  [39, 40], None,      None],
                    "Severity2"     : ["IDCD(4)",   "integer",  [41, 42], None,      None],
                    "Damage3"       : ["IDCD(5)",   "integer",  [43, 44], None,      None],
                    "Severity3"     : ["IDCD(6)",   "integer",  [45, 46], None,      None],
                    "TreeValue"     : ["IMC",       "integer",  [47, 47], None,      None],
                    "Prescription"  : ["IPRSC",     "integer",  [48, 48], None,      None],
                    "Slope"         : ["IPVARS(1)", "integer",  [49, 50], "percent", None],
                    "Aspect"        : ["IPVARS(2)", "integer",  [51, 53], "code",    None],
                    "PV_Code"       : ["IPVARS(3)", "integer",  [54, 56], "code",    None],
                    "TopoCode"      : ["IPVARS(4)", "integer",  [57, 59], "code",    None],
                    "SitePrep"      : ["IPVARS(5)", "integer",  [58, 58], "code",    None],
                    "Age"           : ["ABIRTH",    "real",     [59, 61], "years",   0   ]}

    def set_fvs_variant(self, var):

        """
        Sets FVS variant. This is needed if converting from USDA plant symbols
        (e.g. PSME) to FVS alpha codes (e.g. DF). If so, then all stands you
        wish to process must be of the same FVS variant

        :param var: FVS variant ("iec", "emc" ...)
        :type var: string

        """

        self.variant = var.upper()[:2]

    def read_inventory(self, fname):

        """
        Reads a .csv file containing tree records.

        The csv must be in the correct format as described in ``FMT``.  This
        method checks the format of the file by calling a private method
        ``_is_correct_format()`` that raises a value error.

        :param fname: path to and file name of the Fvs_TreeInit.csv file
        :type fname: string

        **Example:**

        >>> from standfire import fuels
        >>> toDotTree = fuels.Inventory()
        >>> toDotTree.readInventory("path/to/FVS_TreeInit.csv")
        >>> np.mean(toDotTree.data["DBH"])
        9.0028318584070828

        The ``read_inventory()`` method stores the data in a pandas data frame.
        There are countless operations that can be performed on these objects.
        For example, we can explore the relationship between diameter and
        height by fitting a linear model

        .. note:: The following requires statsmodels python module be installed

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
        is for viewing this format.  The keys of the dictionary are the row
        headings and values are as follows: 0 = variable name, 1 = variable
        type, 2 = column location, 3 = units, and 4 = implied decimal place.

        **Example:**

        >>> toDotTree.print_format_standards()
        {"Plot_ID"       : ["ITRE",      "integer",  [0,3],   None,      None],
         "Tree_ID"       : ["IDTREE2",   "integer",  [4,6],   None,      None],
         "Tree_Count"    : ["PROB",      "integer",  [7,12],  None,      None],
         "History"       : ["ITH",       "integer",  [13,13], "trees",   0   ],
         "Species"       : ["ISP",       "alphanum", [14,16], None,      None],
         "DBH"           : ["DBH",       "real",     [17,20], "inches",  1   ],
         "DG"            : ["DG",        "real",     [21,23], "inches",  1   ],
         "Ht"            : ["HT",        "real",     [24,26], "feet",    0   ],
         "HtTopK"        : ["THT",       "real",     [27,29], "feet",    0   ],
         "HTG"           : ["HTG",       "real",     [30,33], "feet",    1   ],
         "CrRatio"       : ["ICR",       "integer",  [34,34], None,      None],
         "Damage1"       : ["IDCD(1)",   "integer",  [35,36], None,      None],
         "Severity1"     : ["IDCD(2)",   "integer",  [37,38], None,      None],
         "Damage2"       : ["IDCD(3)",   "integer",  [39,40], None,      None],
         "Severity2"     : ["IDCD(4)",   "integer",  [41,42], None,      None],
         "Damage3"       : ["IDCD(5)",   "integer",  [43,44], None,      None],
         "Severity3"     : ["IDCD(6)",   "integer",  [45,46], None,      None],
         "TreeValue"     : ["IMC",       "integer",  [47,47], None,      None],
         "Prescription"  : ["IPRSC",     "integer",  [48,48], None,      None],
         "Slope"         : ["IPVARS(1)", "integer",  [49,50], "percent", None],
         "Aspect"        : ["IPVARS(2)", "integer",  [51,53], "code",    None],
         "PV_Code"       : ["IPVARS(3)", "integer",  [54,56], "code",    None],
         "TopoCode"      : ["IPVARS(4)", "integer",  [57,59], "code",    None],
         "SitePrep"      : ["IPVARS(5)", "integer",  [58,58], "code",    None],
         "Age"           : ["ABIRTH",    "real",     [59,61], "years",   0   ]}

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
        for i in self.FMT:
            cols.append(i)
        return cols

    def _is_correct_format(self, data):
        """
        Pseudo-private methods to check for correct inventory file formating
        """
        headings = ["Stand_CN", "Stand_ID", "StandPlot_CN", "StandPlot_ID"]
        for i in data.columns:
            if i not in self.get_fvs_cols() and i not in headings:
                raise ValueError("Column heading {0} does not match FVS \
                    standard".format(i))

    def get_stands(self):
        """
        Returns unique stand IDs

        :return: stand IDs
        :rtype: list of strings

        **Example:**

        >>> toDotTree.get_stands()
        ["BR", "TM", "SW", "HB"]
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
        ``format_fvs_tree_file()`` then you must set the optional argument
        ``cratioToCode`` in ``format_fvs_tree_file`` to ``False``.
        """
        self.data.loc[(self.data["CrRatio"] >= 0) & (self.data["CrRatio"] \
            <= 10), "CrRatio"] = 1
        self.data.loc[(self.data["CrRatio"] > 10) & (self.data["CrRatio"] \
            <= 20), "CrRatio"] = 2
        self.data.loc[(self.data["CrRatio"] > 20) & (self.data["CrRatio"] \
            <= 30), "CrRatio"] = 3
        self.data.loc[(self.data["CrRatio"] > 30) & (self.data["CrRatio"] \
            <= 40), "CrRatio"] = 4
        self.data.loc[(self.data["CrRatio"] > 40) & (self.data["CrRatio"] \
            <= 50), "CrRatio"] = 5
        self.data.loc[(self.data["CrRatio"] > 50) & (self.data["CrRatio"] \
            <= 60), "CrRatio"] = 6
        self.data.loc[(self.data["CrRatio"] > 60) & (self.data["CrRatio"] \
            <= 70), "CrRatio"] = 7
        self.data.loc[(self.data["CrRatio"] > 70) & (self.data["CrRatio"] \
            <= 80), "CrRatio"] = 8
        self.data.loc[(self.data["CrRatio"] > 80) & (self.data["CrRatio"] \
            <= 100), "CrRatio"] = 9

    def convert_sp_codes(self):
        """
        Converts species codes from USDA plant codes to 2 letter FVS codes.
        """

        # check if variant has been set
        if self.variant is None:
            raise "You must set the FVS variant before converting to FVS codes"

        if self.variant in EASTERN:
            side = "eastern"
        else:
            side = "western"

        # get relative path to this module
        this_dir = os.path.dirname(os.path.abspath(__file__))

        # load species crosswalk database
        crosswalk = cPickle.load(open(this_dir + "/data/species_crosswalk.p", "rb"))

        # get all species in all stands
        uniq_sp = self.data["Species"].unique()

        for i in uniq_sp:
            if i in crosswalk[side]:
                self.data.loc[self.data["Species"] == i,
                              "Species"] = crosswalk[side][i][self.variant]
            else:
                print(i + " is not recognized by FVS as a " + side + " species. " +
                      "Defaulting to unknown species.")
                self.data.loc[self.data["Species"] == i, "Species"] = "OT"

    def format_fvs_tree_file(self, cratio_to_code=True):
        """
        Converts data in FVS_TreeInit.csv to FVS .tre format

        This methods reads entries in the pandas data frame (``self.data``) and
        writes them to a formated text string following FVS .tre data formating
        standards shown in ``FMT``.  If multiple stands exist in ``self.data``
        then each stand will be written as a (key,value) pair in
        ``self.fvs_tree_file`` where the key is the stand ID and the value is
        the formated text string.

        :param cratio_to_code: default = True
        :type cratio_to_code: boolean

        .. note:: If the ``crwratio_percent_to_code()`` methods has
                  been called prior to call this methods, then the
                  ``cratio_to_code`` optional argument must be set to ``False``
                  to prevent errors in crown ratio values.

        **Example:**

        >>> toDotTree.format_fvs_tree_file()
        >>> toDotTree.fvs_tree_file["Stand_ID_1"]
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

        # convert crown ratios from percent to int values from 0-9
        if cratio_to_code:
            self.crwratio_percent_to_code()

        # replace nan with empty string
        self.data = self.data.fillna(" ")

        for i in self.get_stands():
            df_stand = self.data.set_index(["Stand_ID"]).loc[i]
            df_stand["Tree_ID"] = range(1, len(df_stand["Tree_ID"]) + 1)
            tmp = ""
            for j in range(0, len(df_stand)):
                for k in df_stand.columns:
                    if k in self.FMT.keys():
                        tmp += "{0:{width}}".format([str(df_stand[k][j])\
                                .split(".")[0], "".join(str(df_stand[k][j])\
                                .split("."))][self.FMT[k][4] == 1]\
                                , width=(self.FMT[k][2][1] - self.FMT[k][2][0])+1)
                tmp += "\n"
            self.fvs_tree_file[i] = tmp

    def save(self, output_path):
        """
        Writes formated fvs tree files to specified location

        If multiple stands exist in the FVS_TreeInit then the same number of
        files will be created in the specified directory.  The file names
        will be the same as the Stand_ID with a ``.tre`` extension.

        :param output_path: directory to store output .tre files
        :type output_path: string

        .. note:: This method will throw an error if it is called prior to the
                  ``format_fvs_tree_file()`` method.
        """
        for i in self.fvs_tree_file:
            with open(os.path.join(output_path, str(i) + ".tre"), "w") as tre_file:
                tre_file.write(self.fvs_tree_file[i])
