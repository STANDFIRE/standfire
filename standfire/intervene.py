# -*- coding: utf-8 -*-
"""
The intervene module is a collection of treatment algorithms
"""

import pandas as pd
import math

__authors__ = "Lucas Wells"
__Copyright__ = "Copyright 2015, STANDFIRE"


class BaseSilv(object):
    """
    Collector class for treatments.

    :ivar trees: Pandas data frame of trees
    :ivar extent: min_x, min_y, max_x, max_y coordinates of trees data frame

    ..note:: All treatment class should inherit BaseSilv
    """

    # class attributes
    treatment_collection = {}

    def __init__(self, trees):
        """Constructor"""

        # instance variables
        self.trees = ""
        self.extent = {"min_x": 0, "min_y": 0, "max_x": 0, "max_y": 0}

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

        # set extent
        self.set_extent(min(self.trees['xloc']), min(self.trees['yloc']),
                        max(self.trees['xloc']), max(self.trees['yloc']))

    def get_trees(self):
        """
        Returns the trees data frame of the object

        :return: trees data frame
        :rtype: Pandas.DataFrame
        """

        return self.trees

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

    def set_extent(self, min_x, min_y, max_x, max_y):
        """
        Sets extent instance variable

        :param min_x: minimum x coordinate
        :type min_x: float
        :param min_y: minimum y coordinate
        :type min_y: float
        :param max_x: maximum x coordinate
        :type max_x: float
        :param max_y: maximum y coordinate
        :type max_y: float

        .. note:: ``set_extent`` is automatically called by ``BaseSilv()``
                  constructor
        """

        self.extent["min_x"] = min_x
        self.extent["min_y"] = min_y
        self.extent["max_x"] = max_x
        self.extent["max_y"] = max_y

    def get_extent(self):
        """
        Returns bounding box of tree coordinates

        :return: [min_x, min_y, max_x, max_y]
        :rtype: list of floats

        :Examples:

            >>> from standfire.intervene import SpaceCrowns
            >>> space = SpaceCrowns("/Users/standfire/test_trees.csv")
            >>> bbox = space.get_extent()
            >>> bbox
            [1.3, 3.5, 63.1, 61.4]

        """
        return self.extent


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
        self.treatment_options = {1: "thin from below to crown spacing",
                                  2: "thin from above to crown spacing",
                                  3: "random thin to crown spacing"}

    def set_crown_space(self, crown_space):
        """
        Sets spacing between crowns for the treatment

        :param crown_space: crown spacing in units of input data frame
        :type crown_space: float
        """

        self.crown_space = crown_space

    def get_treatment_options(self):
        """
        Returns dictionary of treatment options

        :return: treatment option codes and description
        :rtype: dictionary
        """

        return self.treatment_options

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
        x1, x2 = self.trees['xloc'][tree_a], self.trees['xloc'][tree_b]
        y1, y2 = self.trees['yloc'][tree_a], self.trees['yloc'][tree_b]

        # get crown radii of tree_a and tree_b
        crad_a, crad_b = self.trees['crd'][tree_a], self.trees['crd'][tree_b]

        # return the distance between trees (Pythagoras' theorem)
        return math.sqrt(pow(x2 - x1, 2) + pow(y2 - y1, 2)) - (crad_a + crad_b)

    def treat(self):
        """
        Treatment algorithm for removing trees based on input crown spacing

        .. todo:: Optimize algorithm by incorporating ``search_rad``.
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
