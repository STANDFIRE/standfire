#!python2
################################################################################
#---------#
# wfds.py #
#---------#

"""
The wfds module is for configuring and running WFDS simulations. Use the WFDS
class to setup the run. The WFDS class inherits the Mesh class, so meshes can
be dealt with there.
"""

# meta
__authors__ = "Team STANDFIRE"
__copyright__ = "Copyright 2015, STANDFIRE"
__credits__ = ["Greg Cohn","Brett Davis","Matt Jolly","Russ Parsons","Lucas Wells"]
__license__ = "GPL"
__maintainer__ = "Lucas Wells"
__email__ = "bluegrassforestry@gmail.com"
__status__ = "Development"
__version__ = "1.1.3a" # Previous version: '1.1.2a'

# module imports
import platform
import subprocess
import os


class Mesh(object):
    """
    The Mesh class can be used to easily and quickly create FDS meshes.

    :param x: X dimension of the simulation domain
    :x type: integer
    :param y: Y dimension of the simulation domain
    :type y: integer
    :param z: Z dimension of the simulation domain
    :type z: integer
    :param res: 3-space resolution prior to mesh stretching
    :type res: integer
    :param n: Number of meshes
    :n type: integer

    *Example:*

    >>> import wfds
    >>> mesh = wfds.mesh(160,90,50,1,9)
    >>> mesh.stretch_mesh([3,33], [1,31], axis='z')
    >>> print mesh.format_mesh()

    """

    def __init__(self, x, y, z, res, n):
        """
        Constructor
        """

        # instance variables
        self.res = res
        self.IJK = []
        self.XB = []
        self.n = n
        self.stretch = None

        # calculate IJK
        self._calcIJK(x, y, z)
        self._calcXB(x, y, z)

    def _calcIJK(self, x, y, z):
        """
        Private method

        Calculates IJK based on xyz and res
        """

        remainder = y % self.n
        interval = (y-remainder)/self.n

        for i in range(self.n):
            if (i == self.n - 1):
                self.IJK.append([x,interval+remainder,z])
            else:
                self.IJK.append([x,interval,z])

    def _calcXB(self, x, y, z):
        """
        Private method

        Calculates XB based on xyz and n
        """

        remainder = y % self.n
        interval = (y-remainder)/self.n

        acumm = 0
        for i in range(self.n):
            if (i == self.n - 1):
                self.XB.append([0, x, acumm, acumm+interval+remainder, 0, z])
            else:
                self.XB.append([0, x, acumm, acumm+interval, 0, z])
            acumm += interval


    def stretch_mesh(self, CC, PC, axis='z'):
        """
        Apply stretching to the mesh along the specified axis

        :param CC: computation coordinates
        :type CC: python list
        :param PC: physical coordinates
        :type PC: python list

        .. note:: CC and PC must have equal number of elements

        :Example:

        >>> mesh = Mesh(200, 150, 100, 1, 1)
        >>> mesh.stretch_mesh([3,33], [1,31], axis='z')
        >>> print mesh.format_mesh()

        """

        if (len(CC) != len(PC)):
            print "CC and PC are not of equal length"
            return -1

        self.stretch = [CC, PC]

    def format_mesh(self):
        """
        Formats mesh for WFDS input file

        :return: formated WFDS mesh
        :rtype: string
        """

        mesh_template = "&MESH IJK={i},{j},{k}, XB={xmin},{xmax},{ymin},{ymax},{zmin},{zmax} /\n"
        stretch_template = "&TRNZ CC={cc}, PC={pc}, MESH_NUMBER={n} /\n"

        mesh_string = ""

        for mesh in range(self.n):
            mesh_string += mesh_template.format(i=self.IJK[mesh][0],
                                                j=self.IJK[mesh][1],
                                                k=self.IJK[mesh][2],
                                                xmin=self.XB[mesh][0],
                                                xmax=self.XB[mesh][1],
                                                ymin=self.XB[mesh][2],
                                                ymax=self.XB[mesh][3],
                                                zmin=self.XB[mesh][4],
                                                zmax=self.XB[mesh][5])
        if self.stretch:
            mesh_string += '\n\n'
            for mesh in range(self.n):
                for i in range(len(self.stretch[0])):
                    mesh_string += stretch_template.format(cc=self.stretch[0][i],
                                                           pc=self.stretch[1][i],
                                                           n=mesh+1)

        return mesh_string


class WFDS(Mesh):
    """
    This class configures a WFDS simulation. This is a subclass of Mesh and
    meshes are dealt with implicitly

    *Example:*

    >>> import wfds
    >>> fds = wfds.WFDS(160, 90, 50, 64, 83, 1, 9, fuels)
    """

    def __init__(self, x, y, z, xA, xO, res, n, fuels):
        """
        Constructor
        x: scene size in the x dimension
        y: scene size in the y dimension
        z: scene size in the z dimension
        xA: Area Of Interest in the x dimension
        xO: x offset
        res: simulation resolution
        n: number of WFDS meshes
        fuels: fuels object (fuels.py, class: FVSfuels)
        """

        # call the super class constructor
        #super(self.__class__, self).__init__(x, y, z, res, n)
        super(WFDS, self).__init__(x, y, z, res, n)

        # auto-calculate the position of the aoa
        aoa_x_center = (xO+(xA/2))
        #aoa_x_center = (x-((y-64)/2))-(64/2)

        # run configuration parameters
        self.params = {'run_name'   : 'Default',
                  'mesh'       : None,
                  'time'       : 0,
                  'init_temp'  : 0,
                  'wind_speed' : 0,
                  'ign'        : {'hrrpua' : 0,
                                  'coords' : [0,0,0,0],
                                  'ramp'   : [0,0,0,0,0]},
                  'bounds'     : {'x' : x,
                                  'y' : y,
                                  'z' : z},
                  'fuels'      : fuels,
                  'dump'       : {'y_center' : y/2.0,
                                  'aoa_x_center' : aoa_x_center,
                                  'aoa_x_front' : aoa_x_center - 10,
                                  'aoa_x_back' : aoa_x_center + 10}}

    def create_mesh(self, stretch=False):
        """
        Call the `stretch_mesh()` method of the Mesh class

        :param stretch: to stretch or not to strecth
        :stretch type: False or stretch arguments

        .. note:: See `Mesh.stretch_mesh()` for setting the mesh arguments
        """

        if stretch:
            self.stretch_mesh(stretch['CC'], stretch['PC'], stretch['axis'])

        self.mesh = self.format_mesh()
        self.params['mesh'] = self.mesh

    def create_ignition(self, start_time, end_time, x0, x1, y0, y1):
        """
        Places an ignition strip at the specified location and generates fire
        at the specified HRR for the specified duration

        :param start_time: start time of the igniter fire
        :type start_time: integer
        :param end_time: finish time of teh igniter fire
        :type end_time: integer
        :param x0: starting x position of the ignition strip
        :type x0: float
        :param x1: ending x position of the ignition strip
        :type x1: float
        :param y0: starting y position of the ignition strip
        :type y0: float
        :param y1: ending y position of the ignition strip
        :type y1: float

        .. note:: Ignition ramping is dealt with implicitly to avoid 'explosions'
        """

        self.params['ign']['coords'][0] = x0
        self.params['ign']['coords'][1] = x1
        self.params['ign']['coords'][2] = y0
        self.params['ign']['coords'][3] = y1

        delta = end_time - start_time
        interval = delta/4.0
        self.params['ign']['ramp'][0] = start_time
        cT = start_time + interval
        for i in range(1,4):
            self.params['ign']['ramp'][i] = cT
            cT += interval
        self.params['ign']['ramp'][4] = end_time

    def set_wind_speed(self, U0):
        """
        Set the inflow wind speed

        :param U0: inflow wind speed (m/s)
        :type U0: float
        """

        self.params['wind_speed'] = U0

    def set_init_temp(self, temp):
        """
        Set the initial temperature of the simulation

        :param temp: temperature (celcius)
        :type temp: float
        """

        self.params['init_temp'] = temp

    def set_simulation_time(self, sim_time):
        """
        Set the duration of the simulation

        :param sim_time: duration of the simulation (s)
        :type sim_time: float
        """

        self.params['time'] = sim_time

    def set_hrrpua(self, hrr):
        """
        Set the heat release rate per unit area for the ignition strip

        :param hrr: heat release rate per unit area (kW/m^2)
        :type hrr: integer
        """

        self.params['ign']['hrrpua'] = hrr

    def set_run_name(self, name):
        """
        """

        self.params['run_name'] = name

    def save_input(self, file_name):
        """
        Save the input file. Include the desired directory in the string

        :param file_name: path to and name of input file (.txt not .fds please)
        :type file_name: string
        """

        this_dir = os.path.dirname(os.path.abspath(__file__))

        # open and read the fds input file template from the standfire directory
        with open(this_dir + '/data/wfds/wfds_template.txt', 'r') as f:
            file_template = f.read()

        # set the parameters
        fds_file = file_template.format(d=self.params)

        # write it to disk
        with open(file_name, 'w') as f:
            f.write(fds_file)

class Execute(object):
    """
    The Execute class runs the WFDS input file (platform agnostic)

    :param input_file: path to and name of input file
    :type input_file: string
    :param n_proc: number of processors
    :type n_proc: integer
    """

    def __init__(self, input_file, n_proc):
        """
        Constructor
        """

        # get path to STANDFIRE directory
        self.fds_bin = os.path.dirname(os.path.abspath(__file__)) + "/bin/"

        if platform.system().lower() == "linux":
            self._exec_linux(input_file, n_proc)
        elif platform.system().lower() == "windows":
            self._exec_win(input_file, n_proc)
        elif platform.system().lower() == "darwin":
            self._exec_mac()
        else:
            print "OS of type {0} is not recognized by STANDFIRE".format(platform.system())

    def _exec_linux(self, input_file, n_proc):
        """
        Private method
        """

        log = subprocess.check_output([self.fds_bin + "fds_linux/wfds", input_file],
                cwd='/'.join(input_file.split('/')[:-1]))

    def _exec_win(self, input_file, n_proc):
        """
        Private method
        """

        log = subprocess.check_output([self.fds_bin + "fds_win/wfds.exe", input_file],
                cwd='/'.join(input_file.split('/')[:-1]))


class GenerateBinaryGrid(Mesh):
    """
    This class is for generating binary FDS grid. Useful for Capsis. Inherits
    the mesh class.

    .. note:: See the wfds.Mesh() class for details
    """

    def __init__(self, x, y, z, res, n, f_name, stretch=False):
        """
        Constructor
        """

        # call super constructor
        #super(self.__class__, self).__init__(x, y, z, res, n)
        super(GenerateBinaryGrid, self).__init__(x, y, z, res, n)

        if stretch:
            self.stretch_mesh(stretch['CC'], stretch['PC'], stretch['axis'])

        self.f_name = f_name
        self.mesh = self.format_mesh()

        self._write()
        self._generate()

    def _write(self):
        """
        Private method
        """

        grid_name = self.f_name.split('/')[-1].split('.')[0]
        grid_input = "&HEAD CHID='{0}', TITLE='{0}' /\n\n\n".format(grid_name)

        grid_input += self.mesh

        grid_input += "\n&TIME T_END=0.1 /\n"
        grid_input += "\n&DUMP WRITE_XYZ=.TRUE. /\n"
        grid_input += "\n&TAIL /"

        with open(self.f_name, 'w') as f:
            f.write(grid_input)

    def _generate(self):
        """
        Private method
        """

        proc = Execute(self.f_name, 1)


