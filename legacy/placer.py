

import pandas as pd
import random

class Extender(object):
    """
    Base class for Stamper
    """

    def __init__(self, domain, aoa):
        """
        Constructor

        :param domain: origin and spatial bounds for domain
        :type domain: list
        :param aoa: origin and spatial bound for aoa
        :type aoa: list
        """

        self.aoa = aoa
        self.domain = domain
        self.aoa_vertices = []
        self.domain_vertices = []
        self.aoa_extent = {}
        self.domain_extent = {}

        # extract vertices and extent of shapes on object instantiation
        self._extract_vertices()
        self._extract_extent()

    def _extract_vertices(self):
        """
        Private method

        extracts vertices from domain and aoa instance variables
        """

        self.aoa_vertices = [(self.aoa[0], self.aoa[1]), (self.aoa[0] +
                              self.aoa[2], self.aoa[1]), (self.aoa[0],
                              self.aoa[1] + self.aoa[3]), (self.aoa[0] +
                              self.aoa[2], self.aoa[1] + self.aoa[3])]

        self.domain_vertices = [(self.domain[0], self.domain[1]),
                                (self.domain[0] + self.domain[2],
                                 self.domain[1]), (self.domain[1],
                                 self.domain[1] + self.domain[3]),
                                (self.domain[1] + self.domain[2],
                                 self.domain[1] + self.domain[3])]

    def _extract_extent(self):
        """
        Private method

        extracts extent from domain and aoa instance variables
        """

        self.aoa_extent = {'xmin': min([x[0] for x in self.aoa_vertices])
                         , 'xmax': max([x[0] for x in self.aoa_vertices])
                         , 'ymin': min([y[1] for y in self.aoa_vertices])
                         , 'ymax': max([y[1] for y in self.aoa_vertices])}

        self.domain_extent = {'xmin': min([x[0] for x in self.domain_vertices])
                            , 'xmax': max([x[0] for x in self.domain_vertices])
                            , 'ymin': min([y[1] for y in self.domain_vertices])
                            , 'ymax': max([y[1] for y in self.domain_vertices])}

    @classmethod
    def get_extent(cls, shape_verts):
        """
        This method is useless now
        """

        xmin = min([v[0] for v in shape_verts])
        xmax = max([v[0] for v in shape_verts])
        ymin = min([v[1] for v in shape_verts])
        ymax = max([v[1] for v in shape_verts])

        extent = {'xmin': xmin, 'xmax': xmax, 'ymin': ymin, 'ymax': ymax}

        return extent

    def get_adj_order(self):
        """
        Returns the adjacency order required to fill the requested domain size.
        Adjacency order refers to the number of nested adjacency surrounding the
        aoa required to cover the domain.

        :return: adjacency order
        :rtype: integer
        """

        shape1 = self.domain_extent
        shape2 = self.aoa_extent

        order = 0
        while not self._is_within(shape1, shape2):
            shape2['xmin'] = shape2['xmin'] - self.aoa[2]
            shape2['xmax'] = shape2['xmax'] + self.aoa[2]
            shape2['ymin'] = shape2['ymin'] - self.aoa[3]
            shape2['ymax'] = shape2['ymax'] + self.aoa[3]
            order += 1

        return order

    @classmethod
    def _is_within(cls, shape1, shape2):
        """
        Private method

        Is the extent of shape 1 within the extent of shape 2
        """

        if ((shape1['xmin'] > shape2['xmin']) and
                   (shape1['xmax'] < shape2['xmax']) and
                   (shape1['ymin'] > shape2['ymin']) and
                   (shape1['ymax'] < shape2['ymax'])):
            return True
        else:
            return False

    @classmethod
    def permutate_order(cls, order):
        """
        Calculate all translation positions for extending SVS acre
        to the extent of the domain

        :param order: adjacency order required to extend SVS acre to domain
        :type order: integer
        :return: coordinates indicating position of translation
        :rtype: list of tuples
        """

        init_lst = [0]
        trans_map = []
        for i in range(1, order + 1):
            init_lst.append(i)
            init_lst.append(i * -1)

        for i in init_lst:
            for j in init_lst:
                if (i != 0) or (j != 0):
                    trans_map.append((i, j))

        trans_map.sort()

        return trans_map

    def translate_geom(self, trans_map):
        """
        Calculates geometric translations for all SVS tiles

        :param trans_map: coordinates indicating positions of translations
        :type trans_map: list of tuples
        :return: translation coordinates
        :rtype: dictionary of tile extents
        """
        x_side = self.aoa[2]
        y_side = self.aoa[3]

        base_verts = self.aoa_vertices

        trans_coords = {0: base_verts}
        cnt = 1
        for i in trans_map:
            trans_coords[cnt] = [(base_verts[0][0] + x_side * i[0],
                                  base_verts[0][1] + y_side * i[1]),
                                 (base_verts[1][0] + x_side * i[0],
                                  base_verts[1][1] + y_side * i[1]),
                                 (base_verts[2][0] + x_side * i[0],
                                  base_verts[2][1] + y_side * i[1]),
                                 (base_verts[3][0] + x_side * i[0],
                                  base_verts[3][1] + y_side * i[1])]
            cnt += 1

        return trans_coords

    def trim_trans(self, trans_coords):
        """
        Method trims any tiles that are completely outside of the domain

        :param trans_coords: dictionary of tile extents
        :return: final tile for tree placement
        """
        for i in trans_coords.keys():
            extent = self.get_extent(trans_coords[i])
            if ((extent['xmin'] > self.domain_extent['xmax']) or
                    (extent['xmax'] < self.domain_extent['xmin']) or
                    (extent['ymin'] > self.domain_extent['ymax']) or
                    (extent['ymax'] < self.domain_extent['ymin'])):
                trans_coords.pop(i)

        return trans_coords


class SvsStamper(Extender):
    """
    """

    def __init__(self, domain, xoffset, treelist):
        """
        Constructor

        :param domain:
        :param aoa:
        :param treelist:
        :return:
        """

        self.flip_options = {0: self._flip_a,
                             1: self._flip_b,
                             2: self._flip_c,
                             3: self._flip_d,
                             4: self._flip_e,
                             5: self._flip_f,
                             6: self._flip_g,
                             7: self._flip_h}

        # calculate aoa extent from xoffset
        aoa = [xoffset, (domain[3] - 64) / 2, 64, 64]

        # call super class constructor
        super(self.__class__, self).__init__(domain, aoa)

        # type check the tree list and handle accordingly
        if isinstance(treelist, pd.DataFrame):
            self.trees = treelist
        elif type(treelist) == str:
            try:
                self.trees = pd.read_csv(treelist)
            except:
                raise TypeError("String argument must point to .csv file")
        else:
            raise TypeError("argument type must be either an instance of "
                            "Pandas.DataFrame() or a stirng indicating a path "
                            "to a comma-delimted file")

        self.tiles = self.extend()

    def extend(self):
        """
        Calls methods from 'Extender' inheirited class to build domain

        :return: tree placement tiles
        :rtype: dictionary of lists of 2-tuple coordinates
        """

        order = super(self.__class__, self).get_adj_order()
        trans_map = super(self.__class__, self).permutate_order(order)
        tile_coords = super(self.__class__, self).translate_geom(trans_map)
        trimed_tiles = super(self.__class__, self).trim_trans(tile_coords)

        return trimed_tiles

    @classmethod
    def _flip_a(cls, x, y):
        xf = x * 1
        yf = y * 1

        return xf, yf

    @classmethod
    def _flip_b(cls, x, y):
        xf = x * -1 + max(x)
        yf = y * -1 + max(y)

        return xf, yf

    @classmethod
    def _flip_c(cls, x, y):
        xf = y * 1
        yf = x * -1 + max(x)

        return xf, yf

    @classmethod
    def _flip_d(cls, x, y):
        xf = x * 1
        yf = y * -1 + max(y)

        return xf, yf

    @classmethod
    def _flip_e(cls, x, y):
        xf = x * -1 + max(x)
        yf = y * 1

        return xf, yf

    @classmethod
    def _flip_f(cls, x, y):
        xf = y
        yf = x * 1

        return xf, yf

    @classmethod
    def _flip_g(cls, x, y):
        xf = y * -1 + max(y)
        yf = x * -1 + max(x)

        return xf, yf

    @classmethod
    def _flip_h(cls, x, y):
        xf = y * -1 + max(y)
        yf = x * 1

        return xf, yf

    def place_trees(self, rand_seed=None):
        """
        Places treelist though the extented domain. Trees are placed by repeated
        the SVS square. Use the `rand_seed` parameter for reproducible simulations

        :param rand_seed: sets a random seed to reproducible runs
        :type rand_seed: integer
        """

        if rand_seed:
            random.seed(rand_seed)

        # add column to designate trees as in or out of AOA
        self.trees['svs'] = 1

        trees = self.trees.copy()
        trees['xloc'] += self.tiles[0][0][0]
        trees['yloc'] += self.tiles[0][0][1]

        tree_frames = [trees]

        for i in self.tiles.keys():
            if i != 0:
                tmp_frame = self.trees.copy()
                flip_equ = random.randint(0, len(self.flip_options) - 1)
                tmp_frame['xloc'], tmp_frame['yloc'] = self.flip_options[flip_equ](tmp_frame['xloc'], tmp_frame['yloc'])
                tmp_frame['xloc'] += self.tiles[i][0][0]
                tmp_frame['yloc'] += self.tiles[i][0][1]
                tmp_frame['svs'] = 0
                tree_frames.append(tmp_frame)

        print len(tree_frames)

        self.trees = pd.concat(tree_frames)

    def trim_trees(self):
        """
        Trims the trees to the specified domain extent
        """

        # trim x values
        self.trees = self.trees[(self.trees['xloc'] > self.domain_extent['xmin'])
                                & (self.trees['xloc'] < self.domain_extent['xmax'])]

        # trim y values
        self.trees = self.trees[(self.trees['yloc'] < self.domain_extent['ymax'])
                                & (self.trees['yloc'] > self.domain_extent['ymin'])]

    def write_trees(self, save_to):
        """

        :param fname:
        :return:
        """

        self.trees.to_csv(save_to, ',')
