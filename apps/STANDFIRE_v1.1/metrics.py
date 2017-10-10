#------------#
# metrics.py #
#------------#

"""
This module contains class for calculating various metrics. These metrics are
specific to the WFDS configurations used in Standfire, i.e. They probably won't
work on all WFDS output direcotries.

Lots of work to do here. Work in progress.
"""

# meta
__authors__ = "Team STANDFIRE"
__copyright__ = "Copyright 2015, STANDFIRE"
__credits__ = ["Greg Cohn", "Matt Jolly", "Russ Parsons", "Lucas Wells"]
__license__ = "GPL"
__maintainer__ = "Lucas Wells"
__email__ = "bluegrassforestry@gmail.com"
__status__ = "Development"
__version__ = '1.0.0a'

# module imports
import numpy as np
import os
import glob
import re

# global defs
def atoi(text):
    return int(text) if text.isdigit() else text

def natural_keys(text):
    return [ atoi(c) for c in re.split('(\d+)', text) ]

def jump(bin_file):
    """ Convenience function to jump bytes for binary readers """
    np.fromfile(bin_file, dtype=np.dtype(np.int8), count=4)

# global vars
SEP = os.sep


class ROS(object):
    """
    Calculates rate of spread (m/s)
    
    # TODO: make subclass that reads the vegout files. Current each class reads them.
    """

    def __init__(self, wdir, fuel_1, fuel_2, x_diff):
        """
        Constructor
        """

        if (len(fuel_1.split('.')) == 2) and (len(fuel_2.split('.')) == 2):
            if (fuel_1.split('.')[-1] == 'csv') and (fuel_2.split('.')[-1] == 'csv'):
                self.data_1 = np.genfromtxt(wdir + SEP + fuel_1, dtype=np.float, delimiter=',', skip_header=2)
                self.data_2 = np.genfromtxt(wdir + SEP + fuel_2, dtype=np.float, delimiter=',', skip_header=2)
            else:
                print "fuel_1 and fuel_2 arguments must have a .csv extension"
        else:
            print "fuel_1 and fuel_2 arguments must have a .csv extension"

        self.x_diff = x_diff

    def get_first_burn_time(self, fuel):
        """
        Returns the time when the fuel begins to burn

        :param fuel: The fuel that burns first
        """

        # column 3 is total dry mass
        mass = fuel[:,3]

        # find where mass first decreases
        pre_burn_mass = mass[0]
        for i in range(1, len(mass)):
            if mass[i] < pre_burn_mass:
                return fuel[i,0]

        return None

    def get_ros(self):
        """
        Returns the rate of spread in meters per second
        """

        time_1 = self.get_first_burn_time(self.data_1)
        time_2 = self.get_first_burn_time(self.data_2)

        if time_1 and time_2:
            return self.x_diff / (time_2 - time_1)
        else:
            return 0.0


class MassLoss(object):
    """
    Calculates dry mass consumption
    """

    def __init__(self, wdir):

        self.wdir = wdir
        self.tree_files = None

    def get_tree_files(self):

        file_list = os.listdir(self.wdir)

        tree_files = []

        filt = ['SHRUB', 'HERB', 'hrr']
        for f in file_list:
            add = True
            if f.split('.')[-1] == 'csv':
                for e in filt:
                    if e in f:
                        add = False
                if add:
                    tree_files.append(f)

        self.tree_files = tree_files

    def read_tree_mass(self):

        # read first tree file dry mass column
        mass_sum = np.genfromtxt(self.wdir + SEP + self.tree_files[0], dtype=np.float, delimiter=',', skip_header=2)[:,2]

        # sum the rest
        for i in range(1, len(self.tree_files)):
            mass_sum += np.genfromtxt(self.wdir + SEP + self.tree_files[i], dtype=np.float, delimiter=',', skip_header=2)[:,2]

        self.mass_sum = mass_sum

    def get_total_mass_loss(self):

        return (1 - (self.mass_sum[-1] / self.mass_sum[0])) * 100


class WindProfile(object):
    """
    Calculates wind profile
    """

    def __init__(self, wdir, slice_file, t_start, t_end, t_step):

        self.sf = SliceReader(wdir + SEP + slice_file, t_start, t_end, t_step)

    def get_wind_profile(self):

        slice_data = self.sf.slice_data

        # temporal average
        tavg = np.mean(slice_data, axis=0)
        # spatial average
        savg = np.mean(tavg, axis=0)

        return savg

class HeatTransfer(object):
    """
    Calculates crown heat transfer
    """

    def __init__(self, wdir):

        self.wdir = wdir
        self.tree_files = None
        with open(wdir + '/sim_area.txt', 'r') as f:
            self.sim_area = float(f.read())

    def get_tree_files(self):

        file_list = os.listdir(self.wdir)

        tree_files = []

        filt = ['SHRUB', 'HERB', 'hrr']
        for f in file_list:
            add = True
            if f.split('.')[-1] == 'csv':
                for e in filt:
                    if e in f:
                        add = False
                if add:
                    tree_files.append(f)

        self.tree_files = tree_files

    def read_tree_conv(self):

        # read first tree file radiative heat column (depending of the version
        # of WFDS you are using, these indicies may be different)
        conv_sum = np.genfromtxt(self.wdir + SEP + self.tree_files[0], dtype=np.float, delimiter=',', skip_header=2)[:,6]
        conv_sum = np.nan_to_num(conv_sum)

        # sum the rest
        for i in range(1, len(self.tree_files)):
            tmp = np.genfromtxt(self.wdir + SEP + self.tree_files[i], dtype=np.float, delimiter=',', skip_header=2)[:,6]
            conv_sum += np.nan_to_num(tmp)

        self.conv_sum = conv_sum

    def read_tree_rad(self):

        # read first tree file radiative heat column
        rad_sum = np.genfromtxt(self.wdir + SEP + self.tree_files[0], dtype=np.float, delimiter=',', skip_header=2)[:,7]
        rad_sum = np.nan_to_num(rad_sum)

        # sum the rest
        for i in range(1, len(self.tree_files)):
            tmp = np.genfromtxt(self.wdir + SEP + self.tree_files[i], dtype=np.float, delimiter=',', skip_header=2)[:,7]
            rad_sum += np.nan_to_num(tmp)

        self.rad_sum = rad_sum


""" Binary file readers """


class SliceReader(object):
    """
    Reads slice file
    """

    def __init__(self, sf_name, t_start, t_end, t_step):

        # get list of slice meshes
        sfiles = glob.glob(sf_name)
        sfiles.sort(key=natural_keys)

        self.slice_data = self.binary_slice_reader(sfiles[0], t_start, t_end, t_step)

        # loop through each slice and assemble
        for sf in sfiles[1:]:
            self.slice_data = np.concatenate([self.slice_data,
                    self.binary_slice_reader(sf, t_start, t_end, t_step)], axis=2)

    def binary_slice_reader(self, fname, t_start, t_end, t_step):

        # data types
        d8 = np.dtype(np.int8)
        d32 = np.dtype(np.int32)
        f32 = np.dtype(np.float32)

        # open binary slice file
        bf = open(fname, 'rb')

        # read the header and get variable name and units
        jump(bf)
        var_long = str(bf.read(30))
        jump(bf)

        jump(bf)
        var_short = str(bf.read(30))
        jump(bf)

        jump(bf)
        var_units = str(bf.read(30))
        jump(bf)

        jump(bf)
        domain = np.fromfile(bf, d32, count=6)
        jump(bf)

        # get dimensions of the slice [x0, x1, y0, y1, z0, z1]
        x_size = domain[1] - domain[0] + 1
        y_size = domain[3] - domain[2] + 1
        z_size = domain[5] - domain[4] + 1
        self.sim_area = x_size * y_size

        # determine which axis the plane spans (orientation of the slice)
        if x_size == 1: M = y_size; N = z_size
        elif y_size == 1: M = x_size; N = z_size
        else: M = x_size; N = y_size

        # allocate arrays to store slice for each dicreate time interval
        time_d = max(1, round((t_end - t_start) / t_step))
        cells = N * M
        S = np.zeros((N, M, time_d))
        time_stamp = []

        t = 0 # current position in S
        while bf:
            t_val = np.fromfile(bf, d8, count=4)
            if len(t_val) == 4:

                # get the current time step
                cur_time = np.fromfile(bf, dtype=f32, count=1)[0]

                jump(bf)
                jump(bf)

                # if the current time step is between user defined bounds
                if (cur_time >= t_start) and (cur_time <= t_end):
                    time_stamp.append(cur_time)

                    # add slice at time t to matrix S
                    S[:, :, t] = np.fromfile(bf, dtype=f32, count=cells).reshape((N, M))
                    t += 1
                else:

                    # else read a tmp slice to move through the file
                    jump_slice = np.fromfile(bf, dtype=f32, count=cells)

                jump(bf)

            else:
                break

        bf.close()

        # return a transposed S such that S[time : N : M]
        return S.T


class ParticleReader(object):

    def __init__(self, prt5_name, t_end, precision):

        # get list of prt5 files
        prtfiles = glob.glob(prt5_name)
        prtfiles.sort(key=natural_keys)
        print prtfiles

        mesh = 1
        xp = {}
        yp = {}
        zp = {}

        for prt in prtfiles:
            xp[mesh], yp[mesh], zp[mesh] = self.binary_prt5_reader(prt, t_end, precision)
            mesh += 1

        self.xp = xp
        self.yp = yp
        self.zp = zp

    def binary_prt5_reader(self, prt5_name, t_end, precision):

        # data types
        d8 = np.dtype(np.int8)
        d16 = np.dtype(np.int16)
        d32 = np.dtype(np.int32)
        f32 = np.dtype(np.float32)
        f64 = np.dtype(np.float64)

        # open binary slice file
        bf = open(prt5_name, 'rb')

        jump = np.fromfile(bf, d32, count=1)
        one_int = np.fromfile(bf, d32, count=1)
        jump = np.fromfile(bf, d32, count=1)

        jump = np.fromfile(bf, d32, count=1)
        int_version = np.fromfile(bf, d32, count=1)
        jump = np.fromfile(bf, d32, count=1)

        jump = np.fromfile(bf, d32, count=1)
        n_part = np.fromfile(bf, d32, count=1)[0]
        jump = np.fromfile(bf, d32, count=1)

        n_quantities = {}
        smv_label = {}
        units = {}

        for npc in range(0, n_part):

            jump = np.fromfile(bf, d32, count=1)
            pc = np.fromfile(bf, d32, count=2)
            n_quantities[npc] = pc[0]
            jump = np.fromfile(bf, d32, count=1)

            for nq in range(0, n_quantities[npc]):
                jump = np.fromfile(bf, d32, count=1)
                smv_label[nq] = str(bf.read(30))
                jump = np.fromfile(bf, d32, count=1)

                jump = np.fromfile(bf, d32, count=1)
                units[nq] = str(bf.read(30))
                jump = np.fromfile(bf, d32, count=1)

        n = 0
        STIME = {}
        XP = {}
        YP = {}
        ZP = {}
        TG = {}
        QP = {}
        while bf:
            n = n + 1
            jump = np.fromfile(bf, d32, count=1)
            stime_tmp = np.fromfile(bf, precision, count=1)
            jump = np.fromfile(bf, d32, count=1)

            if (len(stime_tmp) == 0) or (stime_tmp > t_end):
                break
            else:
                STIME[n] = stime_tmp

            XP[n] = {}
            YP[n] = {}
            ZP[n] = {}

            for npc in range(0, n_part):

                jump = np.fromfile(bf, d32, count=1)
                nplim = np.fromfile(bf, d32, count=1)
                jump = np.fromfile(bf, d32, count=1)

                if nplim[0] == 0:
                    skp = np.fromfile(bf, d32, count=6)
                    continue

                jump = np.fromfile(bf, d32, count=1)
                xp = np.fromfile(bf, precision, nplim)
                yp = np.fromfile(bf, precision, nplim)
                zp = np.fromfile(bf, precision, nplim)
                jump = np.fromfile(bf, d32, count=1)

                # store positions
                XP[n][npc] = xp
                YP[n][npc] = yp
                ZP[n][npc] = zp

                jump = np.fromfile(bf, d32, count=1)
                TG[n] = np.fromfile(bf, d32, nplim)
                jump = np.fromfile(bf, d32, count=1)

                if n_quantities[npc] > 0:
                    jump = np.fromfile(bf, d32, 1)
                    for nq in range(0, n_quantities[npc]):
                        qp = np.fromfile(bf, precision, nplim)
                        # store in for loop
                    jump = np.fromfile(bf, d32, 1)

        bf.close()
        return XP, YP, ZP


