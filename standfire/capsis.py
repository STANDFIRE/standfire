"""
Capsis controller
"""

import os
from shutil import copyfile, rmtree
from wfds import GenerateBinaryGrid
import subprocess
import platform

class RunConfig(object):
    """
    """

    def __init__(self, run_directory):
        """
        Constructor
        """

        self.run_directory = run_directory

        this_dir = os.path.dirname(os.path.abspath(__file__))

        # default parameters
        self.params = {'path': run_directory,
                       'speciesFile': '/speciesFile.txt',
                       'svsBaseFile': '',
                       'additionalProperties': '/additionalProperties.txt',
                       'sceneOriginX': 0.0,
                       'sceneOriginY': 0.0,
                       'sceneSizeX': 160,
                       'sceneSizeY': 90,
                       'sceneSizeZ': 100,
                       'show3d': 'false',
                       'extend': 'true',
                       'xOffset': 83.0,
                       'yOffset' : 13.0,
                       'spatialOpt': 0,
                       'respace': 'false',
                       'respaceDistance': 0.0,
                       'prune': 'false',
                       'pruneHeight': 0.0,
                       'format': 86,
                       'litter': 'true',
                       'leaveLive': 'true',
                       'leaveDead': 'true',
                       'twig1Live': 'false',
                       'twig1Dead': 'false',
                       'twig2Live': 'false',
                       'twig2Dead': 'false',
                       'twig3Live': 'false',
                       'twig3Dead': 'false',
                       'canopyGeom': 'RECTANGLE',
                       'bdBin': 0.1,
                       'firstGridFile': 'grid.xyz',
                       'gridNumber': 1,
                       'gridResolution': 1.0,
                       'vegetation_cdrag': 0.5,
                       'vegetation_char_fraction': 0.2,
                       'emissivity': 0.99,
                       'degrad': 'false',
                       'mlr': 0.35,
                       'init_temp': 30.0,
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
                                      5: [[0,0],[0,0],[0,0],[0,0]],
                                      6: [[0,0],[0,0],[0,0],[0,0]]},
                       'srf_fuels' : {'shrub' : {'ht': 1.0,
                                                 'cbh': 0.0,
                                                 'cover': 1.0,
                                                 'width': 1.0,
                                                 'spat_group': 1,
                                                 'live' : {'load': 0.5,
                                                           'mvr' : 500,
                                                           'svr' : 5000,
                                                           'moisture': 50},
                                                 'dead' : {'load': 0.5,
                                                           'mvr' : 500,
                                                           'svr' : 5000,
                                                           'moisture': 20}},
                                       'herb' : {'ht': 1.0,
                                                 'cbh': 0.0,
                                                 'cover': 1.0,
                                                 'width': 1.0,
                                                 'spat_group': 1,
                                                 'live' : {'load': 0.5,
                                                           'mvr' : 500,
                                                           'svr' : 10000,
                                                           'moisture': 50},
                                                 'dead' : {'load': 0.5,
                                                           'mvr' : 500,
                                                           'svr' : 10000,
                                                           'moisture': 10}},
                                        'litter' : {'ht' : 0.1,
                                                    'cbh': 0.0,
                                                    'cover':1.0,
                                                    'width': -1,
                                                    'spat_group': 0,
                                                    'load': 1.0,
                                                    'mvr' : 500,
                                                    'svr' : 2000,
                                                    'moisture': 10}}}

        self.set_path = run_directory

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

        # update offset and block verts
        self._set_x_offset()
        self._set_y_offset()
        self._set_block_verts()

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

        # update offset and block verts
        self._set_x_offset()
        self._set_y_offset()
        self._set_block_verts()

    def set_z_size(self, z_size):
        """
        Sets scene z dimension

        :param z_size: size of scene in the z domain (meters)
        :type z_size: integer

        .. note:: `z_size` must be greater than or equal to tallest tree in domain

        """

        self.params['sceneSizeZ'] = z_size

    def _set_x_offset(self):
        """
        Private method
        """

        x = self.params['sceneSizeX']
        y = self.params['sceneSizeY']
        self.params['xOffset'] = x - (64 + int((y - 64)/2.0))

    def _set_y_offset(self):
        """
        Private method
        """

        self.params['yOffset'] = int((self.params['sceneSizeY'] - 64)/2.0)

    def _set_block_verts(self):
        """
        Calculates verticies for the 5 surface fuel block in the FDS domain

        |----------------------|
        |           | b3  |    |
        |           -------    |
        |   b2      |     | b4 |
        |           |  b1 |    |
        |           -------    |
        |           | b5  |    |
        |----------------------|
        """

        x = self.params['sceneSizeX']
        y = self.params['sceneSizeY']
        xoff = self.params['xOffset']
        yoff = self.params['yOffset']

        b1 = [[xoff, yoff], [xoff+64, yoff], [xoff+64, yoff+64], [xoff, yoff+64]]
        b2 = [[0, 0], [xoff, 0], [xoff, y], [0, y]]
        b3 = [[xoff, yoff+64], [xoff+64, yoff+64], [xoff+64, y], [xoff, y]]
        b4 = [[xoff+64, 0], [x, 0], [x, y], [xoff+64, y]]
        b5 = [[xoff, 0], [xoff+64, 0], [xoff+64, yoff], [xoff, yoff]]
        b6 = [[0,0], [x,0], [x,y], [0,y]]

        self.params['srf_blocks'][1] = b1
        self.params['srf_blocks'][2] = b2
        self.params['srf_blocks'][3] = b3
        self.params['srf_blocks'][4] = b4
        self.params['srf_blocks'][5] = b5
        self.params['srf_blocks'][6] = b6


    def save_config(self):
        """
        """

        this_dir = os.path.dirname(os.path.abspath(__file__))

        with open(this_dir + '/data/capsis/input_template.txt', 'r') as f:
            input_params = f.read()

        with open(this_dir + '/data/capsis/additionalProperties_template.txt', 'r') as f:
            properties = f.read()

        # copy species file from standfire directory to capsis run directory
        copyfile(this_dir + '/data/capsis/speciesFile.txt', self.run_directory + '/speciesFile.txt')

        input_params = input_params.format(d=self.params)
        properties = properties.format(d=self.params)

        with open(self.run_directory + '/capsis_run_file.txt', 'w') as f:
            f.write(input_params)

        with open(self.run_directory + '/additionalProperties.txt', 'w') as f:
            f.write(properties)

        with open(self.run_directory + '/' + self.params['svsBaseFile'] + '_scalars.csv', 'w') as f:
            f.write('"shrubwt", "herbwt", "litter", "duff"')

        if os.path.isdir(self.run_directory + '/output/'):
            rmtree(self.run_directory + '/output')
            os.mkdir(self.run_directory + '/output/')
        else:
            os.mkdir(self.run_directory + '/output/')

        # generate binary grid
        binGrid = GenerateBinaryGrid(self.params['sceneSizeX'],
                                     self.params['sceneSizeY'],
                                     self.params['sceneSizeZ'],
                                     self.params['gridResolution'],
                                     self.params['gridNumber'],
                                     self.run_directory + '/grid.txt')

class Execute(object):
    """
    """

    def __init__(self, path_to_run_file):

        self.capsis_dir = os.path.dirname(os.path.abspath(__file__)) + '/bin/capsis/'

        if platform.system().lower() == 'linux':
            self._exec_capsis_linux(path_to_run_file)
            self._read_fuels(path_to_run_file)
        if platform.system().lower() == 'windows':
            self._exec_capsis_win(path_to_run_file)
            self._read_fuels(path_to_run_file)

    def _exec_capsis_linux(self, path_to_run_file):
        """
        """

        subprocess.call(['sh', self.capsis_dir + '/capsis.sh', '-p', 'script','standfire.myscripts.SFScript', path_to_run_file])

    def _exec_capsis_win(self, path_to_run_file):
        """
        """

        subprocess.call([self.capsis_dir + '/capsis.bat', '-p', 'script','standfire.myscripts.SFScript', path_to_run_file])

    def _read_fuels(self, path_to_run_file):
        """
        """
        with open('/'.join(path_to_run_file.split('/')[:-1]) + '/output/wfds.txt', 'r') as f:
            lines = f.read()

        self.fuels = lines
