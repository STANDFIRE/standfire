"""
wfds.py
"""

import platform
import subprocess
import os


class Mesh(object):
    """
    """

    def __init__(self, x, y, z, res, n):

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

        acum = 0
        for i in range(self.n):
            if (i == self.n - 1):
                self.XB.append([0, x, acum, acum+interval+remainder, 0, z])
            else:
                self.XB.append([0, x, acum, acum+interval, 0, z])
            acum += interval


    def stretch_mesh(self, CC, PC, axis='z'):
        """
        Apply stretching to the mesh along the specified axis

        :param CC: computation coordinates
        :type CC: python list
        :param PC: physical coordinates
        :type PC: python list

        .. note:: CC and PC must have equal number of elements

        :Example:

        >>> mesh = CalcMesh(200, 150, 100, 1, 1)
        >>> mesh.stretch([3,33], [1,31], axis='z')
        >>> print mesh.write(method='virt')

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
        stretch_template = "&TRANZ CC={cc}, PC={pc}, MESH_NUMBER={n} /\n"

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
    """
    def __init__(self, x, y, z, res, n, fuels):

        super(self.__class__, self).__init__(x, y, z, res, n)

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
                  'fuels'      : fuels}

    def create_mesh(self, stretch=False):
        """
        """

        if stretch:
            self.stretch_mesh(stretch['CC'], stretch['PC'], stretch['axis'])

        self.mesh = self.format_mesh()
        self.params['mesh'] = self.mesh

    def create_ignition(self, start_time, end_time, x0, x1, y0, y1):
        """
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
        """

        self.params['wind_speed'] = U0

    def set_init_temp(self, temp):
        """
        """

        self.params['init_temp'] = temp

    def set_simulation_time(self, sim_time):
        """
        """

        self.params['time'] = sim_time

    def set_hrrpua(self, hrr):
        """
        """

        self.params['ign']['hrrpua'] = hrr

    def save_input(self, file_name):
        """
        """

        this_dir = os.path.dirname(os.path.abspath(__file__))

        with open(this_dir + '/data/wfds/wfds_template.txt', 'r') as f:
            file_template = f.read()

        fds_file = file_template.format(d=self.params)

        with open(file_name, 'w') as f:
            f.write(fds_file)


class Execute(object):

    def __init__(self, input_file, n_proc):

        # get path to STANDFIRE directory
        self.fds_bin = os.path.dirname(os.path.abspath(__file__)) + "/bin/"

        if platform.system().lower() == "linux":
            self._exec_linux(input_file, n_proc)
        elif platform.system().lower() == "windows":
            self.exec_win(input_file, n_proc)
        elif platform.system().lower() == "darwin":
            self.exec_mac()
        else:
            print "OS of type {0} is not recognized by STANDFIRE".format(platform.system())

    def _exec_linux(self, input_file, n_proc):
        """
        """

        log = subprocess.check_output([self.fds_bin + "fds_linux/wfds", input_file],
                cwd='/'.join(input_file.split('/')[:-1]))

    def _exec_win(self, input_file, n_proc):
        """
        """

        log = subprocess.check_output([self.fds_bin + "fds_win/wfds.exe", input_file],
                cwd='/'.join(input_file.split('/')[:-1]))

class GenerateBinaryGrid(Mesh):
    """
    """

    def __init__(self, x, y, z, res, n, f_name, stretch=False):

        # call super constructor
        super(self.__class__, self).__init__(x, y, z, res, n)

        if stretch:
            self.stretch_mesh(stretch['CC'], stretch['PC'], stretch['axis'])

        self.f_name = f_name
        self.mesh = self.format_mesh()

        self._write()
        self._generate()

    def _write(self):

        grid_name = self.f_name.split('/')[-1].split('.')[0]
        grid_input = "&HEAD CHID='{0}', TITLE='{0}' /\n\n\n".format(grid_name)

        grid_input += self.mesh

        grid_input += "\n&TIME T_END=0.1 /\n"
        grid_input += "\n&DUMP WRITE_XYZ=.TRUE. /\n"
        grid_input += "\n&TAIL /"

        with open(self.f_name, 'w') as f:
            f.write(grid_input)

    def _generate(self):

        proc = Execute(self.f_name, 1)
