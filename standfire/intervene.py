# -*- coding: utf-8 -*-
"""
"""

import pandas as pd
import math

__authors__ = "Lucas Wells"
__Copyright__ = "Copyright 2015, STANDFIRE"


class BaseSilv(object):
    """
    
    """
    
    # class attributes
    treatment_collection = {}
    
    def __init__(self, trees):
        """Constructor"""
        
        # type check and handle accordingly
        if isinstance(trees, pd.DataFrame):
            self.trees = trees
        elif type(trees) == str:
            try:
                self.trees = pd.read_csv(trees)
            except:
                raise TypeError("String argument must point to .csv file")
        else:
            raise TypeError("argument type must be either an instance of "
                            "Pandas.DataFrame() or a string indicating a path "
                            "to a comma-delimted file")
    
    def add_to_treatment_collection(self, treatment, ID):
        """
        Adds treatment to static class attribute in intervene.BaseSilv()
        """
        self.treatment_collection[ID] = treatment
        
    def clear_treatment_collection(self):
        """
        Deletes all treatment currently in the treatment collection class
        attribute
        """
        
        self.treatment_collection = {}


class SpaceCrowns(BaseSilv):
    """
    :ivar crown_space: instance variable for crown spacing; initial value = 0
    """
    def __init__(self, trees):
        """Constructor"""
        
        # call parent class constructor
        super(SpaceCrowns, self).__init__(trees)
        
        # instance attributes
        self.crown_space = 0
    
    def set_crown_space(self, crown_space):
        """
        Sets spacing between crowns for the treatment
        
        :param crown_space: crown spacing in units of input data frame
        :type crown_space: float
        """
        
        self.crown_space = crown_space
        
    def get_distance(self, tree_a, tree_b):
        """
        Calculate the distance between two trees
        
        Uses Pythagoras' theorem to calculate distance between two tree crowns
        in units of input data frame
        
        :param tree_a: indexed row of tree a in Pandas data frame
        :type tree_a: int
        :param tree_b: indexed row of tree b in Pandas data frame
        :type tree_b: int
        :return: distance between two crowns in units of input data frame
        :rtype: float
        """
        
        # get x,y coordinates of tree_a and tree_b
        x1 = self.trees['xloc'][tree_a]
        x2 = self.trees['xloc'][tree_b]
        y1 = self.trees['yloc'][tree_a]
        y2 = self.trees['yloc'][tree_b]
        
        # get crown radii of tree_a and tree_b
        crad_a = self.trees['crd'][tree_a]
        crad_b = self.trees['crd'][tree_b]
        
        # return the distance between trees (Pythagoras' theorem)
        return math.sqrt(pow(x2 - x1, 2) + pow(y2 - y1, 2)) - (crad_a + crad_b)
        
    def treat(self):
        """
        Treatment algorithm for removing trees based on input crown spacing
        
        .. todo:: Optimize algorithm by incorporating ``searrch_rad``.
        .. todo:: split this function into 3
        
        """

        connect = {}
        trees = self.trees

        search_rad = self.crown_space + (max(trees['crd']) * 2)

        if self.crown_space == 0:
            return self.trees
            print "WARNING: no trees were remove because crown spacing = 0"
        else:
            for i in trees.index:
                connect[i] = []
                for e in trees.index[(trees['xloc'] < trees['xloc'][i] + 15) &
                                     (trees['xloc'] > trees['xloc'][i] - 15) &
                                     (trees['yloc'] < trees['yloc'][i] + 15) &
                                     (trees['yloc'] > trees['yloc'][i] - 15)]:
                    if e != i:
                        space = self.get_distance(i, e)
                        if space < self.crown_space:
                            connect[i].append(e)

            dbh_dsc = trees.sort(['dbh'], ascending=False)

            indx = []
            for i in dbh_dsc.index:
                if i not in indx:
                    # only thin trees to the right of the burner
                    if dbh_dsc['xloc'][i] > 50:
                        if connect[i]:
                            vals = connect[i]
                            for e in vals:
                                indx.append(e)
                                try:
                                    connect.pop(e)
                                except:
                                    pass

            thinned = dbh_dsc.drop(indx)
            
        return thinned
        