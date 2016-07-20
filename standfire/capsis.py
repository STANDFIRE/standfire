"""
Capsis controller
"""

import os


class RunConfig(object):
    """
    """

    def __init__(self):
        """
        Constructor
        """

        this_dir = os.path.dirname(os.path.abspath(__file__))

        with open(this_dir + '/data/capsis/input_template.txt', 'r') as f:
            lines = f.read()

        # default parameters
        self.params = {'path': '',
                       'speciesFile': this_dir + '/data/capsis/speciesFiles.txt',
                       'svsBaseFile': '',
                       'additionalProperties': this_dir + '/data/capsis/additionalProperties.txt',
                       'sceneOriginX': 0.0,
                       'sceneOriginY': 0.0,
                       'sceneSizeX': 160,
                       'sceneSizeY': 90,
                       'show3d': 'false',
                       'extend': 'true',
                       'xOffset': 83.0,
                       'yOffset' : 13.0,
                       'spatialOpt': 0,
                       'respace': 'false',
                       'respaceDistance': 0.0,
                       'prune': 'false',
                       'pruneHeight': 0.0,
                       'format': 64,
                       'litter': 'true',
                       'leaveLive': 'true',
                       'leaveDead': 'true',
                       'twig1Live': 'false',
                       'twig1Dead': 'false',
                       'twig2Live': 'false',
                       'twig2Dead': 'false',
                       'twig3Live': 'false',
                       'twig3Dead': 'false',
                       'canopyGeom': 'Rectangle',
                       'bdBin': 0.01,
                       'firstGridFile': 'grid.xyz',
                       'gridNumber': 1,
                       'vegetation_cdrag': 0.5,
                       'vegetation_char_fraction': 0.2,
                       'emissivity': 0.99,
                       'degrad': 'false',
                       'mlr': 0.05,
                       'init_temp': 20.0,
                       'veg_char_fraction': 0.25,
                       'veg_drag_coefficient': 0.125,
                       'burnRateMax': 0.4,
                       'dehydration': 0.4,
                       'rmChar': 'true',
                       'outDir': '/output/',
                       'fileName': 'wfds.txt',
                       'srf_blocks': {1: [[0,0],[0,0],[0,0],[0,0]],
                                      2: [[0,0],[0,0],[0,0],[0,0]],
                                      3: [[0,0],[0,0],[0,0],[0,0]],
                                      4: [[0,0],[0,0],[0,0],[0,0]],
                                      5: [[0,0],[0,0],[0,0],[0,0]]}}

        self.template = lines.format(d=self.params)

    def set_path(self, path):
        """
        Sets path to Capsis run directory

        :param path: path to Capsis run directory
        :type path: string

        """

        if os.path.isdir(path):
            self.params['path'] = path
        else:
            print "ERROR: " + path + " is not a directory"

    def set_svs_base(self, base_name):
        """
        Sets the base file name for FVS/SVS fuel output files

        :param base_name: base file name for fuel output files
        :type base_name: string

        .. note:: Only the tree.csv file is required. If snags, cwd and scalar
                  files exist in the same directory they will be used by
                  Capsis when writing WFDS fuel inputs.

        """

        if os.path.isfile(os.path.join(self.params['path'], base_name + '_trees.csv')):
            self.params['svsBaseFile'] = base_name
        else:
            print "ERROR: " + base_name + "does not exist in " + self.params['path']

    def set_x_size(self, x_size):
        """
        Sets scene x dimension

        :param x_size: size of scene in the x domain (meters)
        :type x_size: integer

        .. note:: `x_size` must be greater than or equal to 64 meters

        """

        if x_size < 64:
            print "X dimension must be greater than or equal to 64 meters"
            return -1
        else:
            self.params['sceneSizeX'] = x_size

        # update offset
        self._set_x_offset()
        self._set_y_offset()

    def set_y_size(self, y_size):
        """
        Sets scene y dimension

        :param y_size: size of scene in the y domain (meters)
        :type y_size: integer

        .. note:: `y_size` must be greater than or equal to 64 meters

        """

        if y_size < 64:
            print "Y dimension must be greater than or equal to 64 meters"
            return -1
        else:
            self.params['sceneSizeY'] = y_size

        # update offset
        self._set_x_offset()
        self._set_y_offset()

    def _set_x_offset(self):
        """
        Private method
        """

        x = self.params['sceneSizeX']
        y = self.params['sceneSizeY']
        self.params['xOffset'] = x - (64 + ((y - 64)/2))

    def _set_y_offset(self):
        """
        Private method
        """

        self.params['yOffset'] = int((self.params['sceneSizeY'] - 64)/2.0)
