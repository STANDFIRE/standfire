#!python2
################################################################################
#---------#
# wfds.py #
#---------#

"""
The wfds module is for configuring and running the Wildland-urban interface Fire
Dynamics Simulator (WFDS). The ``WFDS``class is used to setup the run. This
class inherits the ``Mesh`` class, so meshes can be dealt with there. The main
purpose of the WFDS class is to create a WFDS input file. The ``Execute`` class
runs a WFDS simulation. The ``GenerateBinaryGrid`` class is used to generate
grids for the capsis module.

See the FDS Users Guide: https://pages.nist.gov/fds-smv/manuals.html and the
WFDS Users Guide: https://www.fs.fed.us/pnw/fera/wfds/wfds_user_guide.pdf
for further information.
"""

# meta
__authors__ = "Team STANDFIRE"
__copyright__ = "Copyright 2017, STANDFIRE"
__credits__ = ["Greg Cohn", "Brett Davis", "Matt Jolly", "Russ Parsons", "Lucas Wells"]
__license__ = "GPL"
__maintainer__ = "Lucas Wells"
__email__ = "bluegrassforestry@gmail.com"
__status__ = "Development"
__version__ = "1.1.4a" # Previous version: "1.1.3a"

# module imports
import platform
import subprocess
import os

class Mesh(object):
    """
    The Mesh class can be used to easily and quickly calculate the input
    parameters needed to define FDS meshes. It is inherited by the ``WFDS`` and
    ``GenerateBinaryGrid`` classes. It has two callable (non-private) methods:
    ``stretch_mesh()`` and ``format_mesh()``

    :param x_dom: X dimension of the simulation domain
    :type x_dom: integer
    :param y_dom: Y dimension of the simulation domain
    :type y_dom: integer
    :param z_dom: Z dimension of the simulation domain
    :type z_dom: integer
    :param res: 3-space resolution prior to mesh stretching
    :type res: float
    :param num_meshes: Number of meshes
    :type num_meshes: integer

    :Example:

    >>> import wfds
    >>> mesh = wfds.Mesh(160,90,50,1,9)
    >>> mesh.stretch_mesh([3,33], [1,31])
    >>> print mesh.format_mesh()

    """

    def __init__(self, x_dom, y_dom, z_dom, res, num_meshes):
        """
        Constructor
        """

        # instance variables
        self.res = res # unused as of v 1.1.4a
        self.num_cells = []
        self.mesh_domain = []
        self.num_meshes = num_meshes
        self.stretch = None

        # calculate cell size and mesh domain
        self._calc_num_cells(x_dom, y_dom, z_dom)
        self._calc_mesh_domain(x_dom, y_dom, z_dom)

    def _calc_num_cells(self, x_dom, y_dom, z_dom):
        """
        Pseudo-private method. Called when a Mesh object is instantiated.

        Calculates the number of cells in the x, y and z directions for each
        mesh using x, y and z domains and the number of meshes. When multiple
        meshes are requested the domain is divided along the y axis. For example,
        in a mesh domain where x=160, y=90 and z=50, and only one mesh is
        requested the the number of cells would be 160, 90 and 50. If two meshes
        are requested, the number of cells for each would be 160, 45 and 50.
        These numbers act as divisors for the mesh domain dimensions to define
        cell sizes. These three numbers populate the IJK parameter in the WFDS
        input file.
        """

        remainder = y_dom % self.num_meshes
        interval = (y_dom-remainder)/self.num_meshes

        for i in range(self.num_meshes):
            if i == self.num_meshes - 1:
                self.num_cells.append([x_dom, interval+remainder, z_dom])
            else:
                self.num_cells.append([x_dom, interval, z_dom])

    def _calc_mesh_domain(self, x_dom, y_dom, z_dom):
        """
        Pseudo-private method. Called when a Mesh object is instantiated.

        Calculates mesh domains based on the x,y,z domain and the number of
        meshes.

		A mesh domain is defined by a set of six coordinates. The first,
        third and fifth coordinates define the origin point of the mesh and the
        second, fourth and sixth coordinates define the opposite corner of the
        mesh. For example, coordinates (0,160,0,90,0,50) define a mesh that
        extends 160 units in the x direction, 90 units in the y direction and 50
        units in the z direction. These six numbers populate the XB parameter in
        the WFDS input file.
        """

        remainder = y_dom % self.num_meshes
        interval = (y_dom-remainder)/self.num_meshes

        acumm = 0
        for i in range(self.num_meshes):
            if i == self.num_meshes - 1:
                self.mesh_domain.append([0, x_dom, acumm, acumm+interval+remainder, 0, z_dom])
            else:
                self.mesh_domain.append([0, x_dom, acumm, acumm+interval, 0, z_dom])
            acumm += interval


    def stretch_mesh(self, comp_coord, phys_coord):
        """
        Defines the parameters for stretching a the mesh along the z axis,
        distorting the cell sizes.  Passes the two sets of coordinates to the
        self.stretch variable which will be accessed by the
        ``format_mesh()`` method to format the WFDS input file.

        :param comp_coord: computation coordinates
        :type comp_coord: python list
        :param phys_coord: physical coordinates
        :type phys_coord: python list

        .. note:: comp_coord and phys_coord must have an equal number of
                  elements.See the FDS users guide for more information on
                  stretching:
                  https://pages.nist.gov/fds-smv/manuals.html

        :Example:

        >>> mesh = wfds.Mesh(200, 150, 100, 1, 1)
        >>> mesh.stretch_mesh([3,33], [1,31])
        >>> print mesh.format_mesh()
        """

        if len(comp_coord) != len(phys_coord):
            print "Computational coordinates and physical coordinates are not \
                   of equal length"
            return -1

        self.stretch = [comp_coord, phys_coord]

    def format_mesh(self):
        """
        Formats a mesh for the WFDS input file.

        :return: formated WFDS mesh
        :rtype: string
        """

        # Below, XB defines the mesh domain, IJK defines the number of cells
        #  within a mesh, TRNZ defines a mesh stretch in the Z direction, CC is
        #  comutaional coordinates and PC is physical coordinates.
        mesh_template = "&MESH IJK={i},{j},{k}, XB={xmin},{xmax},{ymin},{ymax},{zmin},{zmax} /\n"
        stretch_template = "&TRNZ CC={cc}, PC={pc}, MESH_NUMBER={n} /\n"

        mesh_string = ""

        for mesh in range(self.num_meshes):
            mesh_string += mesh_template.format(i=self.num_cells[mesh][0],
                                                j=self.num_cells[mesh][1],
                                                k=self.num_cells[mesh][2],
                                                xmin=self.mesh_domain[mesh][0],
                                                xmax=self.mesh_domain[mesh][1],
                                                ymin=self.mesh_domain[mesh][2],
                                                ymax=self.mesh_domain[mesh][3],
                                                zmin=self.mesh_domain[mesh][4],
                                                zmax=self.mesh_domain[mesh][5])
        if self.stretch:
            mesh_string += "\n\n"
            for mesh in range(self.num_meshes):
                for i in range(len(self.stretch[0])):
                    mesh_string += stretch_template.format(cc=self.stretch[0][i],
                                                           pc=self.stretch[1][i],
                                                           n=mesh+1)

        return mesh_string


class WFDS(Mesh):
    """
    This class configures a WFDS simulation. It gathers and calculates WFDS
    paramters and writes them to a WFDS input file.

    ``WFDS()`` is a subclass of the ``Mesh()`` class and meshes are dealt with
    implicitly.

    :param x: scene size in the x dimension
    :type x: integer
    :param y: scene size in the y dimension
    :type y: integer
    :param z: scene size in the z dimension
    :type z: integer
    :param xA: x dimension of the Area Of Interest
    :type xA: integer
    :param xO: x offset
    :type xO: integer
    :param res: simulation resolution
    :type res: double
    :param n: number of WFDS meshes
    :type n: integer
    :param fuels: fuels object (fuels.py, class: FVSfuels)
    :type fuels: object

    The following example assumes that a fuels object has been created using
    the fuels module.

    :Example:

    >>> fds = wfds.WFDS(160, 90, 50, 64, 83, 1, 1, fuels)
    >>> fds.create_mesh(stretch={"CC":[3, 33], "PC":[1, 31]})
    >>> fds.create_ignition(30, 50, 24, 29, 13, 77)
    >>> fds.set_hrrpua(1000)
    >>> fds.set_wind_speed(9)
    >>> fds.set_init_temp(30)
    >>> fds.set_simulation_time(300)
    >>> fds.set_run_name("standard_run")
    >>> fds.save_input("C:/temp/standard_run.txt")
    """

    def __init__(self, x, y, z, xA, xO, res, n, fuels):
        """
        Constructor

        :param x: scene size in the x dimension
        :type x: integer
        :param y: scene size in the y dimension
        :type y: integer
        :param z: scene size in the z dimension
        :type z: integer
        :param xA: x dimension of the Area Of Interest
        :type xA: integer
        :param xO: x offset
        :type xO: integer
        :param res: simulation resolution
        :type res: double
        :param n: number of WFDS meshes
        :type n: integer
        :param fuels: fuels object (fuels.py, class: FVSfuels)
        :type fuels: object
        """

        # call the super class constructor
        super(WFDS, self).__init__(x, y, z, res, n)

        # auto-calculate the position of the "area of interest"
        aoi_x_center = (xO+(xA/2))

        # WFDS run configuration parameters
        self.params = {"run_name"   : "Default",
                       "mesh"       : None,
                       "time"       : 0,
                       "init_temp"  : 0,
                       "wind_speed" : 0,
                       "ign"        : {"hrrpua" : 0,
                                       "coords" : [0, 0, 0, 0],
                                       "ramp"   : [0, 0, 0, 0, 0]},
                       "bounds"     : {"x" : x,
                                       "y" : y,
                                       "z" : z},
                       "fuels"      : fuels,
                       "dump"       : {"y_center" : y/2.0,
                                       "aoa_x_center" : aoi_x_center,
                                       "aoa_x_front" : aoi_x_center - 10,
                                       "aoa_x_back" : aoi_x_center + 10}}
        self.mesh = None # will be defined in create_mesh()

    def create_mesh(self, stretch=False):
        """
        Collates and adds mesh parameters to the params dictionary.
        Conditionally calls the ``stretch_mesh()`` method of the Mesh class
        depending on the state of the stretch argument. Calls the
        ``format_mesh()`` method of the Mesh class.

        :param stretch: to stretch or not to stretch
        :type stretch: False or dictionary of stretch parameters

        Example dictionary of stretch parameters:
            {"CC":[3, 33], "PC":[1, 31]}

        .. note:: See ``Mesh.stretch_mesh()`` for setting the stretch arguments
        """

        if stretch:
            self.stretch_mesh(stretch["CC"], stretch["PC"])

        self.mesh = self.format_mesh()
        self.params["mesh"] = self.mesh

    def create_ignition(self, start_time, end_time, x_strt, x_end, y_strt, y_end):
        """
        Places an ignition strip at the specified location and generates fire
        at the specified Heat Release Rate Per Unit Area (HRRPUA) for the
        specified duration.

        :param start_time: start time of the igniter fire
        :type start_time: integer
        :param end_time: finish time of the igniter fire
        :type end_time: integer
        :param x_strt: starting x position of the ignition strip
        :type x_strt: float
        :param x_end: ending x position of the ignition strip
        :type x_end: float
        :param y_strt: starting y position of the ignition strip
        :type y_strt: float
        :param y_end: ending y position of the ignition strip
        :type y_end: float

        .. note:: Ignition ramping is dealt with explicitly to avoid "explosions"
        """

        self.params["ign"]["coords"][0] = x_strt
        self.params["ign"]["coords"][1] = x_end
        self.params["ign"]["coords"][2] = y_strt
        self.params["ign"]["coords"][3] = y_end

        delta = end_time - start_time
        interval = delta/4.0
        self.params["ign"]["ramp"][0] = start_time
        time_step = start_time + interval
        for i in range(1, 4):
            self.params["ign"]["ramp"][i] = time_step
            time_step += interval
        self.params["ign"]["ramp"][4] = end_time

    def set_wind_speed(self, inflow_spd):
        """
        Set the inflow wind speed

        :param inflow_spd: inflow wind speed (m/s)
        :type inflow_spd: float
        """

        self.params["wind_speed"] = inflow_spd

    def set_init_temp(self, temp):
        """
        Set the initial temperature of the simulation

        :param temp: temperature (celcius)
        :type temp: float
        """

        self.params["init_temp"] = temp

    def set_simulation_time(self, sim_time):
        """
        Set the duration of the simulation

        :param sim_time: duration of the simulation (s)
        :type sim_time: float
        """

        self.params["time"] = sim_time

    def set_hrrpua(self, hrr):
        """
        Set the heat release rate per unit area for the ignition strip

        :param hrr: heat release rate per unit area (kW/m^2)
        :type hrr: integer
        """

        self.params["ign"]["hrrpua"] = hrr

    def set_run_name(self, name):
        """
        Adds the run name to `params` dictionary

        :params name: run name
        :type name: string
        """

        self.params["run_name"] = name

    def save_input(self, file_name):
        """
        Creates and saves the WFDS input file using a template file. Include the
        desired directory in the string.

        :param file_name: path to and name of WFDS input file (.txt NOT .fds)
        :type file_name: string
        """

        this_dir = os.path.dirname(os.path.abspath(__file__))

        # open and read the fds input file template from the standfire directory
        with open(this_dir + "/data/wfds/wfds_template.txt", "r") as tmplt_file:
            file_template = tmplt_file.read()

        # set the parameters
        fds_file = file_template.format(d=self.params)

        # write it to disk
        with open(file_name, "w") as fds_file_out:
            fds_file_out.write(fds_file)

class Execute(object):
    """
    The Execute class runs the WFDS input file (platform agnostic)

    :param input_file: path to and name of input file (e.g. grid.txt)
    :type input_file: string
    :param n_proc: number of processors
    :type n_proc: integer

    .. note:: multiple processors not currently supported. Planned for a future
              version.
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
##        elif platform.system().lower() == "darwin":
##            self._exec_mac()
        else:
            print "OS of type {0} is not recognized by STANDFIRE".format(platform.system())

    def _exec_linux(self, input_file, n_proc):
        """
        Pseudo-private method
        """

        if n_proc > 1:
            print "Multiple processors not currently supported for standfire \
                   WFDS execution"

        log = subprocess.check_output([self.fds_bin + "fds_linux/wfds", input_file],
                                      cwd="/".join(input_file.split("/")[:-1]))

        print log

    def _exec_win(self, input_file, n_proc):
        """
        Pseudo-private method
        """

        if n_proc > 1:
            print "Multiple processors not currently supported for standfire \
                   WFDS execution"

        log = subprocess.check_output([self.fds_bin + "fds_win/wfds.exe", input_file],
                                      cwd="/".join(input_file.split("/")[:-1]))

        print log


class GenerateBinaryGrid(Mesh):
    """
    This subclass of ``Mesh`` is used to generate a binary FDS grid for Capsis.
    Inherits the mesh class.

    .. note:: See the ``wfds.Mesh()`` class for details
    """

    def __init__(self, x, y, z, res, n, f_name, stretch=False):
        """
        Constructor
        """

        # call super constructor (GBG inherits Mesh)
        super(GenerateBinaryGrid, self).__init__(x, y, z, res, n)

        if stretch:
            self.stretch_mesh(stretch["CC"], stretch["PC"])

        self.f_name = f_name
        self.mesh = self.format_mesh()

        self._write()
        self._generate()

    def _write(self):
        """
        Pseudo-private method
        Create WFDS input file (e.g. grid.txt)
        """

        grid_name = self.f_name.split("/")[-1].split(".")[0]
        grid_input = "&HEAD CHID='{0}', TITLE='{0}' /\n\n\n".format(grid_name)

        grid_input += self.mesh

        grid_input += "\n&TIME T_END=0.1 /\n"
        grid_input += "\n&DUMP WRITE_XYZ=.TRUE. /\n"
        grid_input += "\n&TAIL /"

        with open(self.f_name, "w") as cap_grid_file:
            cap_grid_file.write(grid_input)

    def _generate(self):
        """
        Pseudo-private method

        f_name is the name of the WFDS input file (e.g. grid.txt)
        """
        Execute(self.f_name, 1)
