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
    treatemnt_collection = {}
    
    def __init__(self, trees):
        """Constructor"""
        
        # type check and handle accordingly
        if isinstance(trees, pd.DateFrame):
            self.trees = trees
        if type(trees) == str:
            self.trees = pd.read_csv(trees)
        else:
            print "ERROR: input trees must be either a .csv file or an instacne of Pandas.DataFrame()"
    
    def add_to_treatment_collection(self, treatment):
        """
        Adds treatment to static class attribute in intervene.BaseSilv()
        """
        self.treatment_collection['a'] = treatment


class SpaceCrowns(BaseSilv):
    """
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
        crad_a = self.trees['cradius'][tree_a]
        crad_b = self.trees['cradius'][tree_b]
        
        # return the distance between trees (Pythagoras' theorem)
        return math.sqrt(pow(x2 - x1, 2) + pow(y2 - y1, 2)) - (crad_a + crad_b)
        