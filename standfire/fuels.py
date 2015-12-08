# -*- coding: utf-8 -*-
"""
Created on Mon Dec  7 17:49:52 2015

@author: lucas wells
"""

import numpy as np
import pandas as pd
import os

class Fvsfuels(object):
    
    def __init__(self, variant):
        
        # class fields
        self.wdir = ""
        self.keys = {}
        self.num_cyc = 0
        self.time_int = 0
        self.inv_year = 0
        self.fuels = {"trees" : {}, "snags" : {}}
        
        # import fvs variant specified by constructor argument
        global fvs
        try:
            exec("from pyfvs import pyfvs%s" % variant + " as temp")
        except ValueError:
            print "The PyFVS python module or the specified variant does not exist"
        fvs = temp
        
    def set_keyword(self, keyfile):
        """
        Sets the keyword file to be used in the FVS simulation
        
        This method will initalize a FVS simulation by registering the
        specified keyword file (.key) with FVS. The working directory of a
        Fvsfuels object will be set to the folder containing the keyword file. 
        You can manually change the working directory with Fvsfuels.set_dir(). 
        This function will also call private methods in this class to extract
        information from the keyword file and set class fields accordingly for
        use in other methods.
        
        :param keyfile: path/to/keyword_file. This must have a .key extension
        :type keyfile: string
        
        :Example:
        
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
        
        This methods is called by Fvsfuels.set_keyword(). Thus, the default
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
        
        self.keys = keys
    
    def _set_num_cycles(self):
        
        if "NUMCYCLE" in self.keys.keys():
            self.num_cyc = self.keys["NUMCYCLE"]
        else:
            self.num_cyc = 10
            print "NUMCYCLE not found in keyword file, default is 10 cycles"
        
    def _set_time_int(self):
        
        if "TIMEINT" in self.keys.keys():
            self.time_int = self.keys["TIMEINT"]
        else:
            self.time_int = 10
            print "TIMEINT not found in keyword file, default is 10 years"
        
    def _set_inv_year(self):
        
        if "INVYEAR" in self.keys.keys():
            self.inv_year = self.keys["INVYEAR"]
        else:
            self.inv_year = 2015
            print "INVYEAR not found in keyword file, default is 2015"
            
    def set_num_cycles(self, num_cyc):
        
        self.num_cyc = num_cyc
        
    def set_time_int(self, time_int):
        
        self.time_int = time_int
        
    def set_inv_year(self, inv_year):
        
        self.inv_year = inv_year
        
    def run_fvs(self):
        
        for i in range(self.inv_year, self.inv_year + 
                      (self.num_cyc * self.time_int) + 
                       self.time_int, self.time_int):
            fvs.fvssetstoppointcodes(6,i)
            fvs.fvs()
            svs_attr = self.get_obj_data()
            spcodes = self.get_spcodes()
            self.fuels["trees"][i] = self._get_trees(svs_attr, spcodes)
            self.fuels["snags"][i] = self._get_snags(svs_attr, spcodes)
        
        # close fvs simulation (call twice)
        fvs.fvs()
        fvs.fvs()
    
    def get_obj_data(self):
        
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
        
    def get_spcodes(self):
        
        # get four letter plant codes
        spcd = []
        for i in range(0, fvs.fvsdimsizes()[4]+1):
            spcd.append(fvs.fvsspeciescode(i)[2].split(' ')[0])
        
        return spcd

    def _get_trees(self, svsobjdata, spcodes):
                
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
        
        return self.fuels['trees'].keys()
        
    def get_trees(self, year):
        
        if year in self.fuels["trees"].keys():
            return self.fuels["trees"][year]
        else:
            print "ERROR: the specified year does not exist"
    
    def get_snags(self, year):
        if year in self.fuels["snags"].keys():
            return self.fuels["snags"][year]
        else:
            print "ERROR: the specified year does not exist"
            
    def get_standid(self):
        
        return fvs.fvsstandid()[0].split(' ')[0]
        
    def save_all(self):
        
        standid = self.get_standid()
        
        for i in self.fuels.keys():
            for e in self.fuels[i]:
                self.fuels[i][e].to_csv(self.wdir + 
                          "{0}_{1}_{2}.csv".format(standid, i, e), index=False)
    
    def save_trees_by_year(self, year):
        
        standid = self.get_standid()
        
        self.fuels["trees"][year].to_csv(self.wdir +
                 "{0}_{1}_{2}.csv".format(standid, "trees", year), index=False)
    
    def save_snags_by_year(self, year):
        
        standid = self.get_standid()
        
        self.fuels["snags"][year].to_csv(self.wdir +
                 "{0}_{1}_{2}.csv".format(standid, "snags", year), index=False)