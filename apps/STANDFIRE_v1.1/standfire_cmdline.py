#!python2
################################################################################
#----------------------#
# standfire_cmdline.py #
#----------------------#

'''
STANDFIRE main module. This module orchestrates the standfire program. It
solicites inputs from the user, calls various submodules and generates outputs
to be used in other programs in the standfire modeling process. It
differentiates between a standard 64x64m (~1 acre) simulation and a lidar
simulation, which can be any size.

Inputs:
1) FVS keyword file (.key)
2) Lidar shapefile (lidar run only - see 'lidar.py' comments for requirements)
3) FVS tree list (.tre) (standard run only). Must have same prefix as keyword
    file (e.g. test.key and test.tre)

Queried Inputs:
1) Type of simulation: lidar or standard
2) FVS variant (e.g. Inland Empire 'iec') See below for FVS guide.

STANDFIRE submodules called by this module (see individual script comments for
    operational descriptions and requirments):
1) fuels.py
2) lidar.py
3) capsis.py
4) wfds.py

Required modules / Python packages (for main and submodules):
1) Main: STANDFIRE submodules (above), os, sys, Tkinter, timeit
2) Fuels: numpy, pandas, os, pprint, platform, cPickle, math, csv
3) Lidar: os, sys, shutil, csv, pandas, gdal
4) Capsis: os, shutil, wfds, subprocess, platform
5) WFDS: os, subprocess, platform

All) 1) Python standard library: cPickle, csv, math, os, platform, pprint,
                                shutil, subprocess, sys, timeit, tkinter
     2) Outside modules: gdal, numpy, pandas


See the following for more information on FVS:
Gary E. Dixon, Essential FVS: A User's Guide to the Forest
        Vegetation Simulator Tech. Rep., U.S. Department of Agriculture , Forest
        Service, Forest Management Service Center, Fort Collins, Colo, USA, 2003.
'''

# meta
__authors__ = "Team STANDFIRE"
__copyright__ = "Copyright 2017, STANDFIRE"
__credits__ = ["Greg Cohn", "Brett Davis", "Matt Jolly", "Russ Parsons", "Lucas Wells"]
__license__ = "GPL"
__maintainer__ = "Brett Davis"
__email__ = "bhdavis@fs.fed.us"
__status__ = "Development"
__version__ = "1.1.1a"

# import some system modules.
print "Importing modules..."
import os
import sys
#import platform
from os.path import dirname as dn
import Tkinter as tk
import tkFileDialog
import tkMessageBox
import timeit

# relative path module import
#mod_path = os.path.abspath(r"C:\Users\bhdavis\Documents\STANDFIRE\source\standfire")
mod_path = os.path.join(dn(dn(dn(os.path.abspath(__file__)))), "standfire")
sys.path.append(mod_path)
print "Standfire module path is:", mod_path

# import standfire modules
import fuels
import capsis
import wfds
import lidar
print "Finished importing modules"

# set some variables
zSceneSize = 50 #Default. Later calc a multiple of tree height + terrain.
variants = ["iec", "ak", "cs", "nc", "ttc"] # currently present in pyFVS
# WFDS variables
res = 1.0 # (m)
nMesh = 1
hrr = 1000 # heat release rate (kW/m^2)
igStartTime = 30 # seconds
igEndTime = 50 # seconds
simTime = 300 # seconds
windSpeed = 8.94 # (m/s)
tempC = 30 # (Celcius)

# initialize tkinter
root = tk.Tk()
root.withdraw()

### User Inputs ###
# ask user for path to keyword file
keywordFile = str(tkFileDialog.askopenfilename(parent=root, initialdir=mod_path,
                                               title="Please select an FVS "
                                               "keyword file (.key)", filetypes=
                                               (("all files", "*.*"),
                                                ("key files", "*.key"))))
keywordFile = os.path.abspath(keywordFile)
# ask user if this is a lidar run
mode = raw_input("Will this simulation be initialized with a LiDAR shapefile?"
                 " (y/n)")
# ask user for FVS variant
variant = raw_input("Enter variant: 'iec' (Inland Empire), 'ak' (Alaska), 'cs' "
                    "(Central States), 'nc' (Klamath Mountains) or 'ttc' "
                    "(Tetons)")
if variant not in variants:
    sys.exit("Variant must be one of these: %s" % (variants))

# instantiate a Fvsfuels object
fuel = fuels.Fvsfuels(variant)
# keyword needs to be set to set capsis run_directory below (with fuel.wdir)
fuel.set_keyword(keywordFile)

# instantiate a capsis config object
cap = capsis.RunConfig(os.path.abspath(fuel.wdir))

if mode == 'y':
    tkMessageBox.showinfo("Lidar Shapefile Requirements", "The lidar shapefile "
                          "projection must be WGS 1984 UTM. \n\nAttributes/Fields "
                          "must include the following: \nX_UTM (meters), \nY_UTM "
                          "(meters), \nHeight_m (meters), \nCBH_m (meters), "
                          "\nDBH_cm (centimeters), \nSpecies (two letter FVS "
                          "code).\n")
    initDir = os.path.dirname(keywordFile)
    lidarShp = str(tkFileDialog.askopenfilename(parent=root, initialdir=initDir,
                                                title="Please select the lidar "
                                                "shapefile (.shp)", filetypes=
                                                (("all files", "*.*"),
                                                 ("shapefiles", "*.shp"))))
    if not lidarShp:
        sys.exit("Cancelling simulation")
    lidarShp = os.path.abspath(lidarShp)
    # set intermediate and output file names
    fishnetShp = lidarShp[:-4]+'_fishnet.shp'
    newLidar = lidarShp[:-4] + "_out.shp"
    lidarCsv = newLidar[:-4]+'_export.csv'
    # instantiate lidar objects
    ldr = lidar.ConvertLidar(lidarShp, fishnetShp, newLidar)
    fvs = lidar.FVSFromLidar(fuel, lidarCsv, keywordFile)
    # begin lidar run
    prjOk, msg, code = ldr.verify_projection()
    if not prjOk:
        if code == 2:
            tkMessageBox.showerror("Terminal error", msg)
            sys.exit("Projection problem. Terminating simulation")
        if code == 1:
            msg += "\n\n OK to continue anyway and CANCEL to abort simulation"
            if tkMessageBox.askokcancel("Possible problem", msg):
                tkMessageBox.showwarning("Continuing", "Continuing LiDAR "
                                         "shapefile processing")
            else: sys.exit("Projection problem. Terminating simulation")
    else: print msg
    # check input shapefile for required fields
    fieldsOk, msg = ldr.verify_input_fields()
    if not fieldsOk:
        tkMessageBox.showerror("Terminal error", msg)
        sys.exit("ERROR: Missing fields in the input shapefile")
    lidar_start = timeit.default_timer()
    extents = ldr.calculate_extents()
    # create fishnet. returns fishnet dimensions
    xySize = ldr.create_fishnet(extents)
    # set some coordinate variables
    xSceneSize = x_AOI_size = xySize[0]
    ySceneSize = y_AOI_size = xySize[1]
    xyOrig = [extents[0], extents[2]]
    # continue lidar run
    copy_ok, msg = ldr.copy_shapefile()
    if not copy_ok:
        tkMessageBox.showerror("Terminal error", msg)
        sys.exit("ERROR: Unable to create output shapefile")
    clf_ok, msg = ldr.cleanup_lidar_fields()
    if not clf_ok:
        tkMessageBox.showerror("Warning", msg)
    ldr.fishnet_id()
    ldr.cleanup_lidar_features()
    ldr.add_attribute_fields()
    ldr.calculate_attribute_fields()
    ldr.number_trees()
    ldr.export_attributes_to_csv(lidarCsv)
    fvsCsv = fvs.run_FVS_lidar()
    fvs.create_capsis_csv(xyOrig, fvsCsv)
    # set_xy_size also sets xoffset, xSceneSize, ySceneSize and srf block
    # dimensions
    cap.set_xy_size(xSceneSize, ySceneSize, x_AOI_size, y_AOI_size)
    lidar_elapsed = timeit.default_timer() - lidar_start
    print "Converting lidar data took: "+str(round(lidar_elapsed, 3))+" seconds."

elif mode == 'n':
    # Set some default values (standard 1 acre run)
    xSceneSize = 160
    ySceneSize = 90
    x_AOI_size = 64
    y_AOI_size = 64
    bExtend = True
    # start simulation
    fuel.run_fvs()
    # write fvs fuel files
    fuel.save_trees_by_year(2010)
    # set_xy_size sets xoffset, xSceneSize, ySceneSize and srf block dimensions
    cap.set_xy_size(xSceneSize, ySceneSize, x_AOI_size, y_AOI_size)
    cap.set_extend_FVS_sample(bExtend)
else:
    sys.exit("Error: Answer must be 'y' or 'n'")

root.destroy() # finished with tkinter

# set remaining capsis variables
cap.set_z_size(zSceneSize)
svs_base = fuel.get_standid()
cap.set_svs_base(svs_base + "_2010")
cap.set_show3D('true')
print cap.params
cap.save_config()

# run capsis
print "\nStarting CAPSIS..."
exeCap = capsis.Execute(cap.params['path'] + '/capsis_run_file.txt')

# instantiate a WFDS object
# WFDS(x,y,z,xAOI,xOffset,res,# meshes,fuels info from capsis)
print "\nDeveloping WFDS files..."
fds = wfds.WFDS(xSceneSize, ySceneSize, zSceneSize, x_AOI_size,
                cap.params['xOffset'], res, nMesh, exeCap.fuels)

# because this is a z-axis stretch, I don't believe we need to alter it until
# we change the hard-wired zSceneSize of 50. I'm not sure about the CC
# (Computational Coordinates) and PC (Physical Coordinates) yet...
fds.create_mesh(stretch={'CC':[3, 33], 'PC':[1, 31], 'axis':'z'})

# calculate igniter size and position
if mode == 'y':
    xIgMin = int(xSceneSize*0.2)
    xIgMax = xIgMin+5
    yIgMin = int(ySceneSize*0.2)
    yIgMax = int(ySceneSize*.8)
elif mode == 'n':
    xIgMin = int((cap.params['xOffset']*0.5)-5)
    xIgMax = xIgMin+5
    yIgMin = int(cap.params['yOffset'])
    yIgMax = int(ySceneSize-cap.params['yOffset'])
else:
    sys.exit("Error: Answer must be 'y' or 'n'")

# create_ignition(start time, end time, x min, x max, y min, y max)
fds.create_ignition(igStartTime, igEndTime, xIgMin, xIgMax, yIgMin, yIgMax)

fds.set_wind_speed(windSpeed)
fds.set_init_temp(tempC)
fds.set_simulation_time(simTime)
fds.set_hrrpua(hrr)
#save WFDS configuration file
fds.save_input(fuel.wdir + 'output/test_wfds.txt')

print "Finished STANDFIRE simulation!"
