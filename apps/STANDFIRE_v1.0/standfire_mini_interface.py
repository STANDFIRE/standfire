#!python2
################################################################################
#-----------------------------#
# standfire_mini_interface.py #
#-----------------------------#

'''
STANDFIRE main module. This GUI orchestrates the standfire program. It
solicites inputs from the user, calls various submodules and generates outputs
to be used in other programs in the standfire modeling process. It
differentiates between a standard 64x64m (~1 acre) simulation and a lidar
simulation, which can be any size.

STANDFIRE submodules called by this module (see individual script comments for
    operational descriptions and requirments):
1) fuels.py
2) lidar.py
3) capsis.py
4) wfds.py

Required modules / Python packages (for main and submodules):
1) Main (mini-interface): STANDFIRE submodules (above), os, sys, time, timeit,
                            shutil, platform, Tkinter
2) Fuels: numpy, pandas, os, pprint, platform, cPickle, math, csv
3) Lidar: os, sys, shutil, csv, pandas, gdal
4) Capsis: os, shutil, wfds, subprocess, platform
5) WFDS: os, subprocess, platform

All) 1) Python standard library: cPickle, csv, math, os, platform, pprint,
                                shutil, subprocess, sys, time, timeit, tkinter
     2) Outside modules: gdal, numpy, pandas (there may be other prerequisites
                        for these modules)

Methods:
main
__init__
update_status
get_lidar_shapefile
get_keyword_file
get_output_dir
toggle_ldr_check
toggle_trt_check
toggle_unlock
update_domain_change
create_wfds_run_script
create_smv_run_script
run_button
run_lidar
run_standard
capsis_etal
create_variables
reset
create_widgets
grid_widgets
end

See the following for more information on FVS:
Gary E. Dixon, Essential FVS: A User's Guide to the Forest
        Vegetation Simulator Tech. Rep., U.S. Department of Agriculture , Forest
        Service, Forest Management Service Center, Fort Collins, Colo, USA, 2003.
'''

# meta
__authors__ = "Team STANDFIRE"
__copyright__ = "Copyright 2015, STANDFIRE"
__credits__ = ["Greg Cohn", "Brett Davis", "Matt Jolly", "Russ Parsons", "Lucas Wells"]
__license__ = "GPL"
__maintainer__ = "Lucas Wells"
__email__ = "bluegrassforestry@gmail.com"
__status__ = "Development"
__version__ = "1.1.1a" # Previous version: '1.1.0a'

# module imports #pyinstaller doesn't recognize 2nd level imports (e.g.
#   import os, timeit). must be on seperate lines
import os
import sys
import time
import timeit
import shutil
import tkFileDialog
import tkMessageBox
import ttk
import Tkinter as tk
import platform

# relative path import for standfire modules
sep = os.sep

# mod_path(s) for running from python - shouldn't interfere with executable now
mod_path = sep.join(os.getcwd().split(sep)[:-2]) +  sep + 'standfire' + sep
sys.path.append(mod_path)

# import standfire modules - works in both the interpreter and the compiler
#  as long as the path to these files is in the pyinstaller command or to the
# .spec file.
import fuels
import capsis
import wfds
import lidar

# globals
VARIANTS = {'Southeast Alaska and Costal BC (ak)' : 'ak',
            'Blue Mountains (bmc)' : 'bmc',
            'Inland CA and Southern Cascades (cac)' : 'cac',
            'Central Idaho (cic)' : 'cic',
            'Central Rockies (crc)' : 'crc',
            'Central States (cs)' : 'cs',
            'Eastern Cascades (ecc)' : 'ecc',
            'Eastern Montana (emc)' : 'emc',
            'Inland Empire (iec)' : 'iec',
            'Klamath Mountains (ncc)' : 'ncc',
            'KooKanTL (ktc)' : 'ktc',
            'Lake States (ls)' : 'ls',
            'Northeast (ne)' : 'ne',
            'Pacific Northwest Coast (pnc)' : 'pnc',
            'Southern (sn)' : 'sn',
            'SORNEC (soc)' : 'soc',
            'Tetons (ttc)' : 'ttc',
            'Utah (utc)' : 'utc',
            'Westside Cascades (wcc)' : 'wcc',
            'Western Sierra Nevada (wsc)' : 'wsc'}


class Application(ttk.Frame, object):
    '''
    '''

    @classmethod
    def main(cls):
        """
        Main method for running application
        """

        tk.NoDefaultRoot()
        root = tk.Tk()
        app = cls(root)

        # auto frame resize
        app.grid(sticky='NSEW')
        root.grid_columnconfigure(0, weight=1)
        root.grid_rowconfigure(0, weight=1)
        root.grid_columnconfigure(1, weight=1)
        root.grid_rowconfigure(1, weight=1)
        root.grid_columnconfigure(2, weight=1)
        root.grid_rowconfigure(2, weight=1)

        # make adjustable in x not y
        root.resizable(True, False)

        # run application until user exit
        root.mainloop()


    def __init__(self, root):
        """
        Class constructor
        """

        # boolean for GUI initialization
        self.is_initialized = False

        # call super class constructor
        super(self.__class__, self).__init__(root)

        # set window title
        root.title("STANDFIRE v1.1a")

        # create widgets
        self.root = root
        self.create_variables()
        self.create_widgets()
        self.grid_widgets()
        self.reset()

        self.status.set(" Status: Ready")


    def update_status(self, text):
        """
        Update status in GUI gutter
        """
        self.status.set(" Status: %s" % text)


    def get_lidar_shapefile(self):
        """
        Opens file dialog and stores user selected filename in lidar_entry box
        """
        tkMessageBox.showinfo("Lidar Shapefile Requirements", "The lidar shapefile projection "
                              "must be WGS 1984 UTM. \n\nAttributes/Fields must include the "
                              "following: \nX_UTM (meters), \nY_UTM (meters), \nHeight_m (meters),"
                              " \nCBH_m (meters), \nDBH_cm (centimeters), \nSpecies (two letter "
                              "FVS code).\n", parent=self.root)
        self.update_status("Select LiDAR shapefile...")
        file_opt = options = {}
        options['parent'] = self.root
        options['title'] = "Select LiDAR shapefile"
        # file type order matters: interp 2nd is shown 1st, exe is the opposite
        options['filetypes'] = (('shapefiles', '*.shp'), ('all_files', '*.*'))
        filename = str(tkFileDialog.askopenfilename(**file_opt)) # str to convert from unicode
        # Check for spaces in path or file name
        if " " in filename:
            tkMessageBox.showerror("", "Spaces not allowed in path or file name: \n" + filename
                                   + " \nPlease move or rename the shapefile", parent=self.root)
            self.ldr_check_var.set(0)
            self.toggle_ldr_check()
            self.update_status("Ready")
            return
        if filename:
            if filename.split('.')[-1] == 'shp':
                self.lidar_shapefile.set(filename)
                lidarShp = os.path.abspath(filename)
                # set intermediate and output file names
                fishnetShp = lidarShp[:-4]+"_fishnet.shp"
                newLidar = lidarShp[:-4] + "_out.shp"
                # instantiate lidar objects
                ldr = lidar.ConvertLidar(lidarShp, fishnetShp, newLidar)
                # verify shapefile projection
                prjOk, pMsg, code = ldr.verify_projection()
                if not prjOk:
                    if code == 2:
                        tkMessageBox.showerror("Projection error", pMsg, parent=self.root)
                        self.ldr_check_var.set(0)
                        self.toggle_ldr_check()
                    if code == 1:
                        pMsg += "\n\n OK to continue anyway and CANCEL to abort simulation"
                        if tkMessageBox.askokcancel("Possible problem", pMsg, parent=self.root):
                            tkMessageBox.showwarning("Continuing", "Continuing LiDAR "
                                                     "shapefile processing", parent=self.root)
                            prjOk = True
                        else:
                            tkMessageBox.showerror("Projection error", "Cancelling LiDAR "
                                                   "shapefile processing", parent=self.root)
                            self.ldr_check_var.set(0)
                            self.toggle_ldr_check()
                else: print pMsg
                # verify shapefile input fields
                fieldsOk, fMsg = ldr.verify_input_fields()
                if prjOk and not fieldsOk:
                    tkMessageBox.showerror("Terminal error", fMsg, parent=self.root)
                    self.ldr_check_var.set(0)
                    self.toggle_ldr_check()
                # calc dimensions and set associated variables
                if prjOk and fieldsOk:
                    xMin, xMax, yMin, yMax = ldr.calculate_extents()
                    xSceneSize = x_AOI_size = int((xMax-xMin)/64)*64
                    ySceneSize = y_AOI_size = int((yMax-yMin)/64)*64
                    self.x_size.set(xSceneSize)
                    self.y_size.set(ySceneSize)
                    self.x_AOI_size.set(x_AOI_size)
                    self.y_AOI_size.set(y_AOI_size)
                    #print "HEY!!!, x,y,xA,yA"
                    #print xSceneSize, ySceneSize, x_AOI_size, y_AOI_size
                    self.svs_offset.set(0)
            else:
                tkMessageBox.showerror("", "The specified file must have a '.shp' extension",
                                       parent=self.root)

        self.update_status("Ready")


    def get_keyword_file(self):
        """
        Opens file dialog and stores user selected filename in keyword_entry box
        """
        self.update_status("Select FVS keyword file...")

        file_opt = options = {}
        options['parent'] = self.root
        options['title'] = "Select FVS keyword file"
        # file type order matters: interp 2nd is shown 1st, exe is the opposite
        options['filetypes'] = (('keyword files', '*.key'), ('all files', '*.*'))
        filename = str(tkFileDialog.askopenfilename(**file_opt)) # str to convert from unicode
        # Check for spaces in path or file name
        if " " in filename:
            tkMessageBox.showerror("", "Spaces not allowed in path or file name: \n" + filename
                                   + " \nPlease move or rename *.key file", parent=self.root)
            self.update_status("Ready")
            return
            # if filename exists, continue
        if filename:
            # if filename extension is not '.key', report error to user
            if filename.split('.')[-1] == 'key':
                self.keyword_filename.set(filename)
                self.output_dir.set('/'.join(filename.split('/')[:-1]))
                key = fuels.Fvsfuels('iec') #B change?
                key.set_keyword(filename) #B set only to get sim years?
                self.sim_years = range(key.inv_year, key.inv_year + (key.num_cyc * key.time_int)
                                       + 1, key.time_int)
                self.sim_years_cb['values'] = self.sim_years
            else:
                tkMessageBox.showerror("", "The specified file must have a '.key' extension",
                                       parent=self.root)

        self.update_status("Ready")

    def get_output_dir(self):
        """
        Opens file dialog and returns user specified directory
        """

        file_opt = options = {}
        options['parent'] = self.root
        options['title'] = "Select Output Directory"
        output_dir = str(tkFileDialog.askdirectory(**file_opt)) # str to convert from unicode
        # Check for spaces in path or file name
        if " " in output_dir:
            tkMessageBox.showerror("", "Spaces not allowed in path or file name: \n"
                                   + output_dir + " \nPlease move or rename the directory",
                                   parent=self.root)
            self.update_status("Ready")
            return
        if output_dir:
            if os.path.isdir(output_dir):
                self.output_dir.set(output_dir)
            else:
                tkMessageBox.showerror("", "The directory does not exist", parent=self.root)

    def toggle_ldr_check(self):
        """
        Toggle logic for LiDAR vs standard STANDFIRE run
        """
        if self.ldr_check_var.get() == 0:
            self.lidar_shapefile.set("")
            self.shape_entry.configure(state='disabled')
            self.shp_brws_btn.configure(state='disabled')
            self.x_entry.configure(state='enabled')
            self.y_entry.configure(state='enabled')
            self.is_initialized = False # suppress 'are you sure' message
            self.reset()
        else:
            self.shape_entry.configure(state='enabled')
            self.shp_brws_btn.configure(state='enabled')
            self.x_size.set("")
            self.x_entry.configure(state='disabled')
            self.y_size.set("")
            self.y_entry.configure(state='disabled')
            self.svs_offset.set("")
            self.origin_x.set("") # igniter
            self.origin_y.set("") # igniter
            self.width.set("") # igniter
            self.length.set("") # igniter

    def toggle_trt_check(self):
        """
        Toggle logic for treatment parameter entry fields
        """

        if self.trt_check_var.get() == 0:
            self.crown_spacing.set(0.0)
            self.prune_height.set(0.0)
            self.crown_entry.configure(state='disabled')
            self.prune_entry.configure(state='disabled')
        else:
            self.crown_entry.configure(state='enabled')
            self.prune_entry.configure(state='enabled')
            self.crown_spacing.set(1.5)
            self.prune_height.set(1.5)

    def toggle_unlock(self):
        """
        Toggle logic for igniter dimensions
        """

        if self.unlock_check_var.get() == 0:
            self.origin_x_entry.configure(state='disabled')
            self.origin_y_entry.configure(state='disabled')
            self.width_entry.configure(state='disabled')
            self.length_entry.configure(state='disabled')
        else:
            self.origin_x_entry.configure(state='enabled')
            self.origin_y_entry.configure(state='enabled')
            self.width_entry.configure(state='enabled')
            self.length_entry.configure(state='enabled')

    def update_domain_change(self, a, b, c): # a,b,c ???
        try: # in case x or y is an empty string (no value in box)
            x = self.x_size.get()
            y = self.y_size.get()
            if self.ldr_check_var.get() == 0:
                # calculate x & y AOI dimensions and offset locations
                x_AOI_size = 64
                y_AOI_size = 64
                y_offset = max(1, int((y - y_AOI_size)/2.0))
                x_offset = max(1, x - (x_AOI_size + y_offset))
                # calculate WFDS igniter dimensions
                x_ign = max(1, int(0.3*float(x_offset)))
                y_ign = y_offset
                length = max(1, int(y - (y_ign *2)))
            elif self.ldr_check_var.get() == 1:
                # calculate x & y AOI dimensions and offset locations
                x_AOI_size = x # change when we implement diff AOI size in lidar
                y_AOI_size = y
                x_offset = max(1, int(x - x_AOI_size))
                #y_offset = max(1,int((y - y_AOI_size)/2.0))
                # calculate WFDS igniter dimensions
                x_ign = max(1, int(x * 0.2))
                y_ign = max(1, int(y * 0.2))
                length = max(1, int(y * 0.6))
            else:
                print "Something ain't right in update_domain_change"
            # set calculated variables
            self.svs_offset.set(x_offset)
            self.x_AOI_size.set(x_AOI_size)
            self.y_AOI_size.set(y_AOI_size)
            self.origin_x.set(x_ign)
            self.origin_y.set(y_ign)
            self.length.set(length)
            self.width.set(5)
        except: # x or y is an empty string
            # reset varibles in case of an error in the above calculations
            self.svs_offset.set("")
            self.x_AOI_size.set("")
            self.y_AOI_size.set("")
            self.origin_x.set("")
            self.origin_y.set("")
            self.length.set("")
            self.width.set("")

    def create_wfds_run_script(self):
        """
        """
        out_dir = self.output_dir.get()

        if platform.system().lower() == 'linux':
            if self.n_mesh.get() == 1:
                with open(out_dir + '/output/runFDS.sh', 'w') as f:
                    f.write(mod_path + '/bin/fds_linux/wfds ' + self.run_name.get() + '.txt')
            else:
                with open(out_dir + '/output/runFDS.sh', 'w') as f:
                    f.write('module load mpi/openmpi-x86_64')
                    f.write('mpiexec -np ' + self.n_mesh.get() + ' ' + mod_path +
                            '/bin/fds_linux/wfds_mpi ' + self.run_name.get() + '.txt')

        if platform.system().lower() == 'windows':
            if self.n_mesh.get() == 1:
                with open(out_dir + '/output/runFDS.bat', 'w') as f:
                    f.write(mod_path + '/bin/fds_win/wfds.exe ' + self.run_name.get() + '.txt')
            else:
                with open(out_dir + '/output/runFDS.bat', 'w') as f:
                    f.write('mpiexec -np ' + self.n_mesh_get() + ' ' + mod_path +
                            '/bin/fds_win/wfds_mpi.exe ' + self.run_name.get() + '.txt')

    def create_smv_run_script(self):
        """
        """
        out_dir = self.output_dir.get()

        if platform.system().lower() == 'linux':
            with open(out_dir + '/output/runSMV.sh', 'w') as f:
                f.write(mod_path + '/bin/fds_linux/smokeview ' + self.run_name.get() + '.smv')

        if platform.system().lower() == 'windows':
            with open(out_dir + '/output/runSMV.bat', 'w') as f:
                f.write(mod_path + '/bin/fds_win/smokeview.exe ' + self.run_name.get() + '.smv')

    def run_button(self):
        """
        Calls either a standard or LiDAR run depending on the status of the
        lidar check box (ldr_check_var)
        """
        if self.ldr_check_var.get() == 1:
            ldrOk = self.run_lidar()
            if not ldrOk:
                tkMessageBox.showerror("Lidar processing Error", "LiDAR "
                                       "processing failed. Verify LiDAR shapefile "
                                       "input requirements and try again.", parent=self.root)
                self.status.set(" Status: Ready")
                return
        elif self.ldr_check_var.get() == 0:
            self.run_standard()
        else: print "Something wrong with ldr_check_var variable"
        # run the new capsis/wfds/smv method for both.
        self.capsis_etal()

    def run_lidar(self):
        """
        This method processes the input lidar shapefile, runs multiple
        FVS simulations and collates the FVS simulation outputs for a
        lidar based STANDFIRE simulation
        """
        # Lidar processing
        # ================
        lidar_start = timeit.default_timer()
        ldrOk = True
        self.update_status("Processing LiDAR shapefile...")
        self.root.update()
        filename = self.lidar_shapefile.get()
        lidarShp = os.path.abspath(filename)

        # set intermediate and output file names
        fishnetShp = lidarShp[:-4]+"_fishnet.shp"
        newLidar = lidarShp[:-4] + "_out.shp"
        lidarCsv = newLidar[:-4]+'_export.csv'

        # instantiate fuels object
        fuel = fuels.Fvsfuels(VARIANTS[self.variant_cb.get()])
        fuel.set_keyword(self.keyword_filename.get())
        # instantiate lidar objects
        ldr = lidar.ConvertLidar(lidarShp, fishnetShp, newLidar)
        fvs = lidar.FVSFromLidar(fuel, lidarCsv, self.keyword_filename.get())
        extents = ldr.calculate_extents()

        # create fishnet. returns fishnet dimensions
        xySize = ldr.create_fishnet(extents) # no longer used?

        # set some coordinate variables
        xyOrig = [extents[0], extents[2]]

        # lidar shapefile processing
        # make a copy of the input shapefile
        copy_ok, msg = ldr.copy_shapefile()
        if not copy_ok:
            tkMessageBox.showerror("Terminal error", msg)
            sys.exit("ERROR: Unable to create output shapefile")
        # Delete extraneous fields from output shapefile
        clf_ok, msg = ldr.cleanup_lidar_fields()
        if not clf_ok:
            tkMessageBox.showerror("Warning", msg)
        # Assigns a fishnet based plot ID numbers
        try:
            ldr.fishnet_id()
        except:
            ldrOk = False
            return ldrOk
        # Delete features outside the fishnet area
        try:
            ldr.cleanup_lidar_features()
        except:
            ldrOk = False
            return ldrOk
        try:
            ldr.add_attribute_fields()
        except:
            ldrOk = False
            return ldrOk
        try:
            ldr.calculate_attribute_fields()
        except:
            ldrOk = False
            return ldrOk
        try:
            ldr.number_trees()
        except:
            ldrOk = False
            return ldrOk
        try:
            ldr.export_attributes_to_csv(lidarCsv)
        except:
            ldrOk = False
            return ldrOk

        #FVS simulation
        self.update_status("FVS simulation...")
        self.root.update()
        time.sleep(1)
        # run the FVS simulations
        fvsCsv = fvs.run_FVS_lidar()
        # collate into CAPSIS input file
        fvs.create_capsis_csv(xyOrig, fvsCsv)
        lidar_elapsed = timeit.default_timer() - lidar_start
        print "Converting lidar data took: "+str(round(lidar_elapsed, 3))+" seconds."
        return ldrOk

    def run_standard(self):
        """
        This method uses the user input and run the FVS simulation for a
        standard STANDFIRE simulation
        """
        # FVS simulation
        # ==============
        self.update_status("FVS simulation...")
        self.root.update()
        fuel = fuels.Fvsfuels(VARIANTS[self.variant_cb.get()])
        fuel.set_keyword(self.keyword_filename.get())
        fuel.run_fvs()

        self.update_status("Saving FVS fuels output...")
        self.root.update()
        time.sleep(1)
        fuel.save_trees_by_year(int(self.sim_years_cb.get()))

    def capsis_etal(self):
        """
        This method runs CAPSIS, generates WFDS inputs and potentially runs WFDS
        """
        fuel = fuels.Fvsfuels(VARIANTS[self.variant_cb.get()])
        fuel.set_keyword(self.keyword_filename.get())
        svs_base = fuel.get_standid()

        self.update_status("Configuring Capsis...")
        self.root.update()
        time.sleep(1)

        # configure the capsis run
        cap = capsis.RunConfig(fuel.wdir)

        # spatial domain - also calculates x & y offsets and surface fuel block dimensions
        cap.set_xy_size(self.x_size.get(), self.y_size.get(), self.x_AOI_size.get(),
                        self.y_AOI_size.get())
        cap.set_z_size(self.z_size.get())

        # set surface fuels
        cap.set_srf_height(self.shrub_ht.get(), self.herb_ht.get(), self.litter_ht.get())
        load_shrub_dead = self.shrub_load.get() * (self.shrub_percent_dead.get()/100.)
        load_shrub_live = self.shrub_load.get() * (1 - (self.shrub_percent_dead.get()/100.))
        load_herb_dead = self.herb_load.get() * (self.herb_percent_dead.get()/100.)
        load_herb_live = self.herb_load.get() * (1 - (self.herb_percent_dead.get()/100.))
        cap.set_srf_live_load(load_shrub_live, load_herb_live)
        cap.set_srf_dead_load(load_shrub_dead, load_herb_dead, self.litter_load.get())
        cap.set_srf_live_svr(self.shrub_sav.get(), self.herb_sav.get())
        cap.set_srf_dead_svr(self.shrub_sav.get(), self.herb_sav.get(), self.litter_sav.get())
        cap.set_srf_cover(self.shrub_cover.get()/100., self.herb_cover.get()/100.)
        cap.set_srf_live_mc(self.shrub_live_mc.get(), self.herb_live_mc.get())
        cap.set_srf_dead_mc(self.shrub_dead_mc.get(), self.herb_dead_mc.get(),
                            self.litter_dead_mc.get())

        # treatments
        if self.trt_check_var.get() == 1:
            cap.set_crown_space(self.crown_spacing.get())
            cap.set_prune_height(self.prune_height.get())

        # show 3D
        if self.viewer_check_var.get() == 1:
            cap.set_show3D('true')

        # extend tree AOI sample to entire scene for standard runs
        if self.ldr_check_var.get() == 0:
            bExtend = True
        elif self.ldr_check_var.get() == 1:
            bExtend = False
        cap.set_extend_FVS_sample(bExtend)

        # name of FVS output fuels file
        cap.set_svs_base(svs_base + "_" + self.sim_years_cb.get())

        # Save configuration and generate binary grid
        self.update_status("Generating binary grid for Capsis fuels...")
        self.root.update()
        print cap.params
        cap.save_config()

        # now run capsis
        self.update_status("Running Capsis...")
        self.root.update()
        exeCap = capsis.Execute(cap.params['path'] + '/capsis_run_file.txt')

        # instantiate a WFDS object
        self.update_status("Configuring WFDS...")
        self.root.update()
        time.sleep(1)
        fds = wfds.WFDS(self.x_size.get(), self.y_size.get(), self.z_size.get(),
                        self.x_AOI_size.get(), self.svs_offset.get(), self.res.get(),
                        self.n_mesh.get(), exeCap.fuels)
        # stretch the mesh
        fds.create_mesh(stretch={'CC':[3, 33], 'PC':[1, 31], 'axis':'z'})
        # set the ignition strip
        fds.create_ignition(self.start_time.get(), self.end_time.get(), self.origin_x.get(),
                            self.origin_x.get() + self.width.get(), self.origin_y.get(),
                            self.origin_y.get() + self.length.get())
        fds.set_hrrpua(self.hrr.get())
        # other parameters
        fds.set_wind_speed(self.wind_speed.get())
        fds.set_init_temp(self.temp.get())
        fds.set_simulation_time(self.sim_time.get())
        fds.set_run_name(self.run_name.get())
        # write the WFDS input file
        self.update_status("Writing WFDS input file...")
        self.root.update()
        time.sleep(1)
        fds.save_input(fuel.wdir + 'output/' + self.run_name.get() + '.txt')

        # clean up directory
        self.update_status("Cleaning up directory...")
        self.root.update()
        time.sleep(1)
        cur_dir = '/'.join(self.keyword_filename.get().split('/')[:-1])
        if self.output_dir.get() != cur_dir: ###
            if os.path.isdir(self.output_dir.get() + '/output'):
                shutil.rmtree(self.output_dir.get() + '/output')
            shutil.copytree(cur_dir + '/output', self.output_dir.get() + '/output')
            shutil.rmtree(cur_dir + '/output')
            files = os.listdir('/'.join(self.keyword_filename.get().split('/')[:-1]))
            keep = ['key', 'tre']
            for i in files:
                if i.split('.')[-1] not in keep:
                    shutil.copy(cur_dir + '/' + i, self.output_dir.get())
                    os.remove(cur_dir + '/' + i)

        # create wfds and smv run scripts
        self.create_wfds_run_script()
        self.create_smv_run_script()

        # check if execute WFDS is on
        if self.fds_check_var.get() == 1:
            self.update_status("Executing WFDS...")
            self.root.update()
            time.sleep(1)
            os.chdir(self.output_dir.get() + '/output/')
            if platform.system().lower() == 'linux':
                os.system("gnome-terminal -e 'sh runFDS.sh'")
            if platform.system().lower() == 'windows':
                os.system('start cmd /K "runFDS.bat"')

        self.update_status("Done")
        self.root.update()
        time.sleep(2)
        self.end()
        return # test bug
        # kill root
        #self.root.quit()
        #self.root.destroy() # root.quit wasn't working in the interpreter
        #sys.exit()
        # none of the above prevent the termination bug

    def create_variables(self):
        """
        Application variable definitions
        """

        # boolean check variables
        self.trt_check_var = tk.IntVar(self.root)
        self.viewer_check_var = tk.IntVar(self.root)
        self.fds_check_var = tk.IntVar(self.root)
        self.unlock_check_var = tk.IntVar(self.root)
        self.ldr_check_var = tk.IntVar(self.root) #B

        # entry field variables
        # =====================
        # lidar run
        self.lidar_shapefile = tk.StringVar(self.root) #B
        # canopy fuels
        self.keyword_filename = tk.StringVar(self.root)
        # surface fuels
        self.shrub_ht = tk.DoubleVar(self.root)
        self.shrub_cbh = tk.DoubleVar(self.root)
        self.shrub_load = tk.DoubleVar(self.root)
        self.shrub_sav = tk.IntVar(self.root)
        self.shrub_cover = tk.IntVar(self.root)
        self.shrub_live_mc = tk.IntVar(self.root)
        self.shrub_dead_mc = tk.IntVar(self.root)
        self.shrub_percent_dead = tk.IntVar(self.root)
        self.herb_ht = tk.DoubleVar(self.root)
        self.herb_cbh = tk.DoubleVar(self.root)
        self.herb_load = tk.DoubleVar(self.root)
        self.herb_sav = tk.IntVar(self.root)
        self.herb_cover = tk.IntVar(self.root)
        self.herb_live_mc = tk.IntVar(self.root)
        self.herb_dead_mc = tk.IntVar(self.root)
        self.herb_percent_dead = tk.IntVar(self.root)
        self.litter_ht = tk.DoubleVar(self.root)
        self.litter_load = tk.DoubleVar(self.root)
        self.litter_sav = tk.IntVar(self.root)
        self.litter_dead_mc = tk.IntVar(self.root)
        # simulation area
        self.x_size = tk.IntVar(self.root)
        self.y_size = tk.IntVar(self.root)
        self.z_size = tk.IntVar(self.root)
        self.x_AOI_size = tk.IntVar(self.root)
        self.y_AOI_size = tk.IntVar(self.root)
        self.svs_offset = tk.IntVar(self.root)
        self.res = tk.DoubleVar(self.root)
        self.n_mesh = tk.IntVar(self.root)
        # weather
        self.wind_speed = tk.DoubleVar(self.root)
        self.temp = tk.DoubleVar(self.root)
        # igniter fire
        self.origin_x = tk.IntVar(self.root)
        self.origin_y = tk.IntVar(self.root)
        self.width = tk.IntVar(self.root)
        self.length = tk.IntVar(self.root)
        self.hrr = tk.IntVar(self.root)
        self.start_time = tk.IntVar(self.root)
        self.end_time = tk.IntVar(self.root)
        # treatments
        self.crown_spacing = tk.DoubleVar(self.root)
        self.prune_height = tk.DoubleVar(self.root)
        # run settings
        self.run_name = tk.StringVar(self.root)
        self.sim_time = tk.IntVar(self.root)
        self.output_dir = tk.StringVar(self.root)

        # trace variable
        self.x_size.trace('w', self.update_domain_change)
        self.y_size.trace('w', self.update_domain_change)

        # status bar
        self.status = tk.StringVar(self.root)


    def reset(self):
        """
        Resets values to default
        ..note:: This method is called when GUI is initialized
        """

        if self.is_initialized: ###
            result = tkMessageBox.askquestion("Reset Values",
                                              "Are you sure you want to reset values to default?",
                                              icon='warning', parent=self.root)
        else:
            result = 'yes'

        self.is_initialized = True

        if result == 'yes':
##            #test termination bug - default .key file for convenience
##            kwfn = r'C:/Users/bhdavis/Documents/STANDFIRE/source/apps/STANDFIRE_v1.0/test_dir/test.key'
##            self.keyword_filename.set(kwfn)
##            self.output_dir.set('/'.join(kwfn.split('/')[:-1]))
##            key = fuels.Fvsfuels('iec')
##            key = None
##            #del key
##            #key.set_keyword(kwfn)
##            self.sim_years = range(2010,2110,10)
##            #self.sim_years = range(key.inv_year, key.inv_year + (key.num_cyc * key.time_int) + 1, key.time_int)
##            self.sim_years_cb['values'] = self.sim_years

            self.viewer_check_var.set(1)
            # reset lidar
            self.ldr_check_var.set(0)
            self.lidar_shapefile.set("")
            self.shape_entry.configure(state='disabled')
            self.shp_brws_btn.configure(state='disabled')
            self.x_entry.configure(state='enabled')
            self.y_entry.configure(state='enabled')
            # surface fuels
            self.shrub_ht.set(0.35)
            self.shrub_cbh.set(0.0)
            self.shrub_load.set(0.8)
            self.shrub_sav.set(5000)
            self.shrub_cover.set(50)
            self.shrub_live_mc.set(100)
            self.shrub_dead_mc.set(40)
            self.shrub_percent_dead.set(10)
            self.herb_ht.set(0.35)
            self.herb_cbh.set(0.0)
            self.herb_load.set(0.8)
            self.herb_sav.set(5000)
            self.herb_cover.set(80)
            self.herb_live_mc.set(100)
            self.herb_dead_mc.set(5)
            self.herb_percent_dead.set(100)
            self.litter_ht.set(0.1)
            self.litter_load.set(0.5)
            self.litter_sav.set(2000)
            self.litter_dead_mc.set(10)
            # simulation area
            self.x_size.set(160)
            self.y_size.set(90)
            self.z_size.set(50)
            self.x_AOI_size.set(64)
            self.y_AOI_size.set(64)
            self.svs_offset.set(83)
            self.res.set(1.0)
            # self.n_mesh.set(multiprocessing.cpu_count()) Multiprocessing not
            # used at this point
            self.n_mesh.set(1)
            # weather
            self.wind_speed.set(8.94)
            self.temp.set(30)
            # igniter fire
            self.origin_x.set(24)
            self.origin_y.set(13)
            self.width.set(5)
            self.length.set(64)
            self.hrr.set(1000)
            self.start_time.set(30)
            self.end_time.set(50)
            # run settings
            self.sim_time.set(300)


    def create_widgets(self):
        """
        Widget definitions and configuration
        """

        entry_opts = dict(width='5')

        # Lidar panel
        # ===========
        self.groupLR = ttk.LabelFrame(self.root, text="LiDAR Run")
        self.lidar_check = ttk.Checkbutton(self.groupLR, text="Enable LiDAR Run",
                                           variable=self.ldr_check_var,
                                           command=self.toggle_ldr_check)
        self.shape_lbl = ttk.Label(self.groupLR, text="Specify LiDAR shapefile:")
        self.shape_entry = ttk.Entry(self.groupLR, textvariable=self.lidar_shapefile)
        self.shape_entry.configure(state='disabled')
        self.shp_brws_btn = ttk.Button(self.groupLR, text="Browse...",
                                       command=self.get_lidar_shapefile)
        self.shp_brws_btn.configure(state='disabled')

        # Canopy fuels panel
        # ==================
        self.groupCF = ttk.LabelFrame(self.root, text="Canopy Fuels")
        self.keyword_lbl = ttk.Label(self.groupCF, text="Specify FVS keyword file:")
        self.keyword_entry = ttk.Entry(self.groupCF, textvariable=self.keyword_filename)
        self.browse_bnt = ttk.Button(self.groupCF, text="Browse...", command=self.get_keyword_file)
        self.variant_lbl = ttk.Label(self.groupCF, text="Select FVS variant:")
        self.variant_cb = ttk.Combobox(self.groupCF, values=VARIANTS.keys(), state='readonly')
        self.variant_cb.current(2)
        self.sim_years_lbl = ttk.Label(self.groupCF, text="Simulation year:")
        self.sim_years_cb = ttk.Combobox(self.groupCF, values="", state='readonly')


        # Surface fuels panel
        # ===================
        self.groupSF = ttk.LabelFrame(self.root, text="Surface Fuels")
        # Labels
        self.ht_lbl = ttk.Label(self.groupSF, text="height (m):")
        self.load_lbl = ttk.Label(self.groupSF, text=u"load (kg/m\u00B2):")
        self.sav_lbl = ttk.Label(self.groupSF, text="SAV:")
        self.cover_lbl = ttk.Label(self.groupSF, text="cover (%):")
        self.live_mc_lbl = ttk.Label(self.groupSF, text="live mc (%):")
        self.dead_mc_lbl = ttk.Label(self.groupSF, text="dead mc (%):")
        self.percent_dead_lbl = ttk.Label(self.groupSF, text="percent dead:")
        # Shrubs
        self.shrub_lbl = ttk.Label(self.groupSF, text="Shrubs")
        self.shrub_ht_entry = ttk.Entry(self.groupSF, textvariable=self.shrub_ht, **entry_opts)
        self.shrub_load_entry = ttk.Entry(self.groupSF, textvariable=self.shrub_load, **entry_opts)
        self.shrub_sav_entry = ttk.Entry(self.groupSF, textvariable=self.shrub_sav, **entry_opts)
        self.shrub_cover_entry = ttk.Entry(self.groupSF, textvariable=self.shrub_cover,
                                           **entry_opts)
        self.shrub_live_mc_entry = ttk.Entry(self.groupSF, textvariable=self.shrub_live_mc,
                                             **entry_opts)
        self.shrub_dead_mc_entry = ttk.Entry(self.groupSF, textvariable=self.shrub_dead_mc,
                                             **entry_opts)
        self.shrub_percent_dead_entry = ttk.Entry(self.groupSF, textvariable=self.shrub_percent_dead,
                                                  **entry_opts)
        # Herbs
        self.herb_lbl = ttk.Label(self.groupSF, text="Herbs")
        self.herb_ht_entry = ttk.Entry(self.groupSF, textvariable=self.herb_ht, **entry_opts)
        self.herb_load_entry = ttk.Entry(self.groupSF, textvariable=self.herb_load, **entry_opts)
        self.herb_sav_entry = ttk.Entry(self.groupSF, textvariable=self.herb_sav, **entry_opts)
        self.herb_cover_entry = ttk.Entry(self.groupSF, textvariable=self.herb_cover, **entry_opts)
        self.herb_live_mc_entry = ttk.Entry(self.groupSF, textvariable=self.herb_live_mc,
                                            **entry_opts)
        self.herb_dead_mc_entry = ttk.Entry(self.groupSF, textvariable=self.herb_dead_mc,
                                            **entry_opts)
        self.herb_percent_dead_entry = ttk.Entry(self.groupSF, textvariable=self.herb_percent_dead,
                                                 **entry_opts)
        # Litter
        self.litter_lbl = ttk.Label(self.groupSF, text="Litter")
        self.litter_ht_entry = ttk.Entry(self.groupSF, textvariable=self.litter_ht, **entry_opts)
        self.litter_load_entry = ttk.Entry(self.groupSF, textvariable=self.litter_load,
                                           **entry_opts)
        self.litter_sav_entry = ttk.Entry(self.groupSF, textvariable=self.litter_sav, **entry_opts)
        self.litter_cover_entry = ttk.Entry(self.groupSF, **entry_opts)
        self.litter_cover_entry.configure(state='disabled')
        self.litter_live_mc_entry = ttk.Entry(self.groupSF, **entry_opts)
        self.litter_live_mc_entry.configure(state='disabled')
        self.litter_dead_mc_entry = ttk.Entry(self.groupSF, textvariable=self.litter_dead_mc,
                                              **entry_opts)
        self.litter_percent_dead_entry = ttk.Entry(self.groupSF, **entry_opts)
        self.litter_percent_dead_entry.configure(state='disabled')

        # Simulation Area
        # ===============
        self.groupSD = ttk.LabelFrame(self.root, text="Simulation Area")
        self.x_lbl = ttk.Label(self.groupSD, text="x size (m):")
        self.x_entry = ttk.Entry(self.groupSD, textvariable=self.x_size, **entry_opts)
        self.y_lbl = ttk.Label(self.groupSD, text="y size (m):")
        self.y_entry = ttk.Entry(self.groupSD, textvariable=self.y_size, **entry_opts)
        self.z_lbl = ttk.Label(self.groupSD, text="z size (m):")
        self.z_entry = ttk.Entry(self.groupSD, textvariable=self.z_size, **entry_opts)
        self.res_lbl = ttk.Label(self.groupSD, text="resolution (m):")
        self.res_entry = ttk.Entry(self.groupSD, textvariable=self.res, **entry_opts)
        self.meshes_lbl = ttk.Label(self.groupSD, text="number of meshes:")
        self.meshes_entry = ttk.Entry(self.groupSD, textvariable=self.n_mesh, **entry_opts)

        # Weather
        # =======
        self.groupWE = ttk.LabelFrame(self.root, text="Weather")
        self.wind_lbl = ttk.Label(self.groupWE, text="wind speed (m/s):")
        self.wind_entry = ttk.Entry(self.groupWE, textvariable=self.wind_speed, **entry_opts)
        self.temp_lbl = ttk.Label(self.groupWE, text=u"temperature (\u00B0C):")
        self.temp_entry = ttk.Entry(self.groupWE, textvariable=self.temp, **entry_opts)

        # Ignitor Fire
        # ============
        self.groupIF = ttk.LabelFrame(self.root, text="Igniter Fire")
        self.origin_x_lbl = ttk.Label(self.groupIF, text="origin x (m):")
        self.origin_x_entry = ttk.Entry(self.groupIF, textvariable=self.origin_x, **entry_opts)
        self.origin_x_entry.configure(state='disabled')
        self.origin_y_lbl = ttk.Label(self.groupIF, text="origin y (m):")
        self.origin_y_entry = ttk.Entry(self.groupIF, textvariable=self.origin_y, **entry_opts)
        self.origin_y_entry.configure(state='disabled')
        self.width_lbl = ttk.Label(self.groupIF, text="width (m):")
        self.width_entry = ttk.Entry(self.groupIF, textvariable=self.width, **entry_opts)
        self.width_entry.configure(state='disabled')
        self.length_lbl = ttk.Label(self.groupIF, text="length (m):")
        self.length_entry = ttk.Entry(self.groupIF, textvariable=self.length, **entry_opts)
        self.length_entry.configure(state='disabled')
        self.hrr_lbl = ttk.Label(self.groupIF, text=u"hrr (kW/m\u00B2):")
        self.hrr_entry = ttk.Entry(self.groupIF, textvariable=self.hrr, **entry_opts)
        self.start_time_lbl = ttk.Label(self.groupIF, text="start time (s):")
        self.start_time_entry = ttk.Entry(self.groupIF, textvariable=self.start_time, **entry_opts)
        self.end_time_lbl = ttk.Label(self.groupIF, text="end time (s):")
        self.end_time_entry = ttk.Entry(self.groupIF, textvariable=self.end_time, **entry_opts)
        self.auto_calc_bnt = ttk.Checkbutton(self.groupIF, text="Unlock values",
                                             variable=self.unlock_check_var,
                                             command=self.toggle_unlock)

        # Treatments
        # ==========
        self.groupTR = ttk.LabelFrame(self.root, text="Treatments")
        self.trt_check = ttk.Checkbutton(self.groupTR, text="Apply fuel treatment",
                                         variable=self.trt_check_var, command=self.toggle_trt_check)
        self.crown_lbl = ttk.Label(self.groupTR, text="crown spacing (m):")
        self.crown_entry = ttk.Entry(self.groupTR, textvariable=self.crown_spacing, **entry_opts)
        self.crown_entry.configure(state='disabled')
        self.prune_lbl = ttk.Label(self.groupTR, text="prune height (m):")
        self.prune_entry = ttk.Entry(self.groupTR, textvariable=self.prune_height, **entry_opts)
        self.prune_entry.configure(state='disabled')

        # Run Settings
        # ============
        self.groupRS = ttk.LabelFrame(self.root, text="Run Settings")
        self.viewer_check = ttk.Checkbutton(self.groupRS, text="Show 3D stand viewer",
                                            variable=self.viewer_check_var)
        self.fds_check = ttk.Checkbutton(self.groupRS, text="Execute WFDS",
                                         variable=self.fds_check_var)
        self.sim_time_lbl = ttk.Label(self.groupRS, text="simulation time (s):")
        self.sim_time_entry = ttk.Entry(self.groupRS, textvariable=self.sim_time, **entry_opts)
        self.run_name_lbl = ttk.Label(self.groupRS, text="run name:")
        self.run_name_entry = ttk.Entry(self.groupRS, textvariable=self.run_name, **entry_opts)
        self.output_dir_lbl = ttk.Label(self.groupRS, text="output directory:")
        self.output_dir_entry = ttk.Entry(self.groupRS, textvariable=self.output_dir, **entry_opts)
        self.output_dir_bnt = ttk.Button(self.groupRS, text="Browse...",
                                         command=self.get_output_dir)

        # Control Buttons
        # ===============
        self.reset_form_bnt = ttk.Button(self.root, text="Reset Values", command=self.reset)
        self.gen_bnt = ttk.Button(self.root, text="Run", command=self.run_button)

        # Status Bar
        # ==========
        # Change font from "" to "Arial" to try to fix a runtime error - didn't matter
        self.status_lbl = ttk.Label(self.root, relief=tk.SUNKEN, anchor=tk.W,
                                    textvariable=self.status, font=("Arial", 10))


    def grid_widgets(self):
        """
        Widget positioning and auto-resize configuration
        """

        options = dict(sticky='NSEW', padx=5, pady=5)

        # Lidar panel
        # ===========
        self.groupLR.grid(row=0, columnspan=5, **options)
        self.groupLR.columnconfigure(2, weight=1)
        self.lidar_check.grid(row=0, column=0, **options)
        self.shape_lbl.grid(row=0, column=1, **options)
        self.shape_entry.grid(row=0, column=2, columnspan=2, **options)
        self.shp_brws_btn.grid(row=0, column=4, **options)

        # Canopy fuels panel
        # ==================
        self.groupCF.grid(row=1, columnspan=4, **options)
        self.groupCF.columnconfigure(1, weight=1)
        self.keyword_lbl.grid(row=0, column=0, **options)
        self.keyword_entry.grid(row=0, column=1, columnspan=2, **options)
        self.browse_bnt.grid(row=0, column=3, **options)
        self.variant_lbl.grid(row=1, column=0, **options)
        self.variant_cb.grid(row=1, column=1, **options)
        self.sim_years_lbl.grid(row=1, column=2, **options)
        self.sim_years_cb.grid(row=1, column=3, **options)

        # Surface fuels panel
        # ===================
        self.groupSF.grid(row=2, rowspan=2, column=0, **options)
        self.groupSF.columnconfigure(1, weight=1)
        self.groupSF.columnconfigure(2, weight=1)
        self.groupSF.columnconfigure(3, weight=1)
        # Labels
        self.ht_lbl.grid(row=1, column=0, **options)
        self.load_lbl.grid(row=2, column=0, **options)
        self.sav_lbl.grid(row=3, column=0, **options)
        self.cover_lbl.grid(row=4, column=0, **options)
        self.live_mc_lbl.grid(row=5, column=0, **options)
        self.dead_mc_lbl.grid(row=6, column=0, **options)
        self.percent_dead_lbl.grid(row=7, column=0, **options)
        # Shrubs
        self.shrub_lbl.grid(row=0, column=1, **options)
        self.shrub_ht_entry.grid(row=1, column=1, **options)
        self.shrub_load_entry.grid(row=2, column=1, **options)
        self.shrub_sav_entry.grid(row=3, column=1, **options)
        self.shrub_cover_entry.grid(row=4, column=1, **options)
        self.shrub_live_mc_entry.grid(row=5, column=1, **options)
        self.shrub_dead_mc_entry.grid(row=6, column=1, **options)
        self.shrub_percent_dead_entry.grid(row=7, column=1, **options)
        # Herbs
        self.herb_lbl.grid(row=0, column=2, **options)
        self.herb_ht_entry.grid(row=1, column=2, **options)
        self.herb_load_entry.grid(row=2, column=2, **options)
        self.herb_sav_entry.grid(row=3, column=2, **options)
        self.herb_cover_entry.grid(row=4, column=2, **options)
        self.herb_live_mc_entry.grid(row=5, column=2, **options)
        self.herb_dead_mc_entry.grid(row=6, column=2, **options)
        self.herb_percent_dead_entry.grid(row=7, column=2, **options)
        # Litter
        self.litter_lbl.grid(row=0, column=3, **options)
        self.litter_ht_entry.grid(row=1, column=3, **options)
        self.litter_load_entry.grid(row=2, column=3, **options)
        self.litter_sav_entry.grid(row=3, column=3, **options)
        self.litter_cover_entry.grid(row=4, column=3, **options)
        self.litter_live_mc_entry.grid(row=5, column=3, **options)
        self.litter_dead_mc_entry.grid(row=6, column=3, **options)
        self.litter_percent_dead_entry.grid(row=7, column=3, **options)

        # Simulation Area
        # ===============
        self.groupSD.grid(row=2, column=1, **options)
        self.groupSD.columnconfigure(1, weight=1)
        self.x_lbl.grid(row=0, column=0, **options)
        self.x_entry.grid(row=0, column=1, **options)
        self.y_lbl.grid(row=1, column=0, **options)
        self.y_entry.grid(row=1, column=1, **options)
        self.z_lbl.grid(row=2, column=0, **options)
        self.z_entry.grid(row=2, column=1, **options)
        self.res_lbl.grid(row=3, column=0, **options)
        self.res_entry.grid(row=3, column=1, **options)
        self.meshes_lbl.grid(row=4, column=0, **options)
        self.meshes_entry.grid(row=4, column=1, **options)

        # Weather
        # =======
        self.groupWE.grid(row=3, column=1, **options)
        self.groupWE.columnconfigure(1, weight=1)
        self.wind_lbl.grid(row=0, column=0, **options)
        self.wind_entry.grid(row=0, column=1, **options)
        self.temp_lbl.grid(row=1, column=0, **options)
        self.temp_entry.grid(row=1, column=1, **options)

        # Ignitor Fire
        # ============
        self.groupIF.grid(row=2, rowspan=2, column=2, **options)
        self.groupIF.columnconfigure(1, weight=1)
        self.origin_x_lbl.grid(row=0, column=0, **options)
        self.origin_x_entry.grid(row=0, column=1, **options)
        self.origin_y_lbl.grid(row=1, column=0, **options)
        self.origin_y_entry.grid(row=1, column=1, **options)
        self.width_lbl.grid(row=2, column=0, **options)
        self.width_entry.grid(row=2, column=1, **options)
        self.length_lbl.grid(row=3, column=0, **options)
        self.length_entry.grid(row=3, column=1, **options)
        self.hrr_lbl.grid(row=4, column=0, **options)
        self.hrr_entry.grid(row=4, column=1, **options)
        self.start_time_lbl.grid(row=5, column=0, **options)
        self.start_time_entry.grid(row=5, column=1, **options)
        self.end_time_lbl.grid(row=6, column=0, **options)
        self.end_time_entry.grid(row=6, column=1, **options)
        self.auto_calc_bnt.grid(row=7, column=0, columnspan=2, **options)

        # Treatment
        # =========
        self.groupTR.grid(row=4, column=0, columnspan=3, **options)
        self.groupTR.columnconfigure(2, weight=1)
        self.groupTR.columnconfigure(4, weight=1)
        self.trt_check.grid(row=0, column=0, **options)
        self.crown_lbl.grid(row=0, column=1, **options)
        self.crown_entry.grid(row=0, column=2, **options)
        self.prune_lbl.grid(row=0, column=3, **options)
        self.prune_entry.grid(row=0, column=4, **options)

        # Run Settings
        # ============
        self.groupRS.grid(row=5, rowspan=3, column=0, columnspan=2, **options)
        self.groupRS.columnconfigure(1, weight=1)
        self.sim_time_lbl.grid(row=0, column=2, **options)
        self.sim_time_entry.grid(row=0, column=3, **options)
        self.run_name_lbl.grid(row=0, column=0, **options)
        self.run_name_entry.grid(row=0, column=1, **options)
        self.output_dir_lbl.grid(row=1, column=0, **options)
        self.output_dir_entry.grid(row=1, column=1, columnspan=2, **options)
        self.output_dir_bnt.grid(row=1, column=3, **options)
        self.viewer_check.grid(row=2, column=0, **options)
        self.fds_check.grid(row=2, column=1, columnspan=2, **options)

        # Control Buttons
        # ===============
        self.reset_form_bnt.grid(row=6, column=2, **options)
        self.gen_bnt.grid(row=7, column=2, **options)

        # Status bar
        # ==========
        self.status_lbl.grid(row=8, column=0, columnspan=4, sticky='NSEW', padx=0)

    def end(self):
        """
        End the STANDFIRE program and close the GUI
        """
        print "the end method was called"
        return #temp
        # test termination error - none of the below helped...
        #self.root.destroy()
##        try:
##            self.root.destroy()
##            print "destroyed"
##        except:
##            print "destroy exception"
##        try:
##          print "here goes sys.exit"
##          sys.exit(0)
##          print "sys.exit'ed"
##        except:
##            print "sys.exit exception (there always is)"
##        return

if __name__ == '__main__':
    Application.main()
    print "made it through app"
