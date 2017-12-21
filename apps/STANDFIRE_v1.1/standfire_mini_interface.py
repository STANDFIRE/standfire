#!/usr/bin/env python2
################################################################################
#-----------------------------#
# standfire_mini_interface.py #
#-----------------------------#

"""
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
3) Lidar: timeit, os, sys, shutil, csv, pandas, gdal
4) Capsis: os, shutil, wfds, subprocess, platform, random
5) WFDS: os, subprocess, platform

All) 1) Python standard library: cPickle, csv, math, os, platform, pprint,
                                 shutil, subprocess, sys, time, timeit, tkinter
     2) Outside modules: gdal, pandas, numpy (installed as pre-requisite for
                         pandas) (there may be other prerequisites for these
                         modules)

See the following for more information on FVS:
Gary E. Dixon, Essential FVS: A User's Guide to the Forest
        Vegetation Simulator Tech. Rep., U.S. Department of Agriculture , Forest
        Service, Forest Management Service Center, Fort Collins, Colo, USA, 2003.
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

# module imports #pyinstaller doesn't recognize 2nd level imports (e.g.
#   import os, timeit). must be on seperate lines
import os
#import sys
import time
import timeit
import shutil
import tkFileDialog
import tkMessageBox
import ttk
import Tkinter as tk
import platform

# import standfire modules - works in both the interpreter and the compiler
#  as long as the path to these files is in the pyinstaller command or in the
# .spec file.
import fuels
import capsis
import wfds
import lidar

MOD_PATH = os.getcwd() # necessary?

# FVS variant abbreviations
VARIANTS = {"Southeast Alaska and Costal BC (ak)" : "ak",
            "Blue Mountains (bmc)" : "bmc",
            "Inland CA and Southern Cascades (cac)" : "cac",
            "Central Idaho (cic)" : "cic",
            "Central Rockies (crc)" : "crc",
            "Central States (cs)" : "cs",
            "Eastern Cascades (ecc)" : "ecc",
            "Eastern Montana (emc)" : "emc",
            "Inland Empire (iec)" : "iec",
            "Klamath Mountains (ncc)" : "ncc",
            "KooKanTL (ktc)" : "ktc",
            "Lake States (ls)" : "ls",
            "Northeast (ne)" : "ne",
            "Pacific Northwest Coast (pnc)" : "pnc",
            "Southern (sn)" : "sn",
            "SORNEC (soc)" : "soc",
            "Tetons (ttc)" : "ttc",
            "Utah (utc)" : "utc",
            "Westside Cascades (wcc)" : "wcc",
            "Western Sierra Nevada (wsc)" : "wsc"}

class Application(ttk.Frame, object):
    """ STANDFIRE application """

    @classmethod
    def main(cls):
        """ Main method for running application """

        tk.NoDefaultRoot()
        root = tk.Tk()
        app = cls(root)
        # auto frame resize
        app.grid(sticky="NSEW")
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
        """ Class constructor """

        # boolean for GUI initialization
        self.is_initialized = False
        super(Application, self).__init__(root)
        # set window title
        root.title("STANDFIRE v1.1a")
        # create widgets
        self.root = root
        self.create_variables()
        self.create_widgets()
        self.grid_widgets()
        self.reset()
        self.execute_capsis = None # will be defined in run_capsis method
        self.fuel_wdir = None # will be defined in get_keyword_file method
        # idea: create a testing method with default inputs and run it here
        #key_path = r""
        #out_name = ""
        #self.test(key_path, out_name)
        self.status.set(" Status: Ready")

    def update_status(self, text):
        """
        Update applicaion status in GUI gutter

        :param text: text to add to the GUI gutter
        :type text: string
        """

        self.status.set(" Status: %s" % text)

    def get_lidar_shapefile(self):
        """
        Opens file dialog and stores user selected lidar shapefile name and
        directory path in lidar_entry box.
        """

        tkMessageBox.showinfo("Lidar Shapefile Requirements", "The lidar shapefile projection "
                              "must be WGS 1984 UTM. \n\nAttributes/Fields must include the "
                              "following: \nX_UTM (meters), \nY_UTM (meters), \nHeight_m (meters),"
                              " \nCBH_m (meters), \nDBH_cm (centimeters), \nSpecies (two letter "
                              "FVS code).\n", parent=self.root)
        self.update_status("Select LiDAR shapefile...")
        file_opt = options = {}
        options["parent"] = self.root
        options["title"] = "Select LiDAR shapefile"
        # file type order matters. interp: 2nd is shown 1st, exe is the opposite
        options["filetypes"] = (("shapefiles", "*.shp"), ("all_files", "*.*"))
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
            if filename.split(".")[-1] == "shp":
                self.lidar_shapefile.set(filename)
                lidar_shp = os.path.abspath(filename)
                # set intermediate and output file names
                fishnet_shp = lidar_shp[:-4]+"_fishnet.shp"
                new_lidar_shp = lidar_shp[:-4] + "_out.shp"
                # instantiate lidar object
                ldr = lidar.ConvertLidar(lidar_shp, fishnet_shp, new_lidar_shp)
                # verify shapefile projection
                projection_ok, projection_msg, code = ldr.verify_projection()
                if not projection_ok:
                    if code == 2:
                        tkMessageBox.showerror("Projection error", projection_msg, parent=self.root)
                        self.ldr_check_var.set(0)
                        self.toggle_ldr_check()
                    if code == 1:
                        projection_msg += ("\n\n OK to continue anyway and CANCEL to abort "
                                           "simulation")
                        if tkMessageBox.askokcancel("Possible problem", projection_msg,
                                                    parent=self.root):
                            tkMessageBox.showwarning("Continuing", "Continuing LiDAR "
                                                     "shapefile processing", parent=self.root)
                            projection_ok = True
                        else:
                            tkMessageBox.showerror("Projection error", "Cancelling LiDAR "
                                                   "shapefile processing", parent=self.root)
                            self.ldr_check_var.set(0)
                            self.toggle_ldr_check()
                else: print projection_msg
                # verify shapefile input fields
                fields_ok, fields_msg = ldr.verify_input_fields()
                if projection_ok and not fields_ok:
                    tkMessageBox.showerror("Terminal error", fields_msg, parent=self.root)
                    self.ldr_check_var.set(0)
                    self.toggle_ldr_check()
                # calculate dimensions and set associated variables
                if projection_ok and fields_ok:
                    x_min, x_max, y_min, y_max = ldr.calculate_extents()
                    x_scene_size = x_aoi_size = int((x_max-x_min)/64)*64
                    y_scene_size = y_aoi_size = int((y_max-y_min)/64)*64
                    self.x_size.set(x_scene_size)
                    self.y_size.set(y_scene_size)
                    self.x_aoi_size.set(x_aoi_size)
                    self.y_aoi_size.set(y_aoi_size)
                    self.svs_offset.set(0)
            else:
                tkMessageBox.showerror("", "The specified file must have a '.shp' extension",
                                       parent=self.root)
        self.update_status("Ready")

    def get_keyword_file(self):
        """
        Opens file dialog and stores user selected filename in keyword_entry
        box. Calls fuels submodule to extract inventory year, time interval and
        number of cycles from the keyword file, calculates potential simulation
        years and populates the simulation year combobox with these years.
        """

        self.update_status("Select FVS keyword file...")
        file_opt = options = {}
        options["parent"] = self.root
        options["title"] = "Select FVS keyword file"
        # file type order matters: interp 2nd is shown 1st, exe is the opposite
        options["filetypes"] = (("keyword files", "*.key"), ("all files", "*.*"))
        filename = str(tkFileDialog.askopenfilename(**file_opt))
        # Check for spaces in path or file name
        if " " in filename:
            tkMessageBox.showerror("", "Spaces not allowed in path or file name: \n" + filename
                                   + " \nPlease move or rename *.key file", parent=self.root)
            self.update_status("Ready")
            return
        # if filename exists, continue
        if filename:
            # if filename extension is not ".key", report error to user
            if filename.split(".")[-1] == "key":
                self.keyword_filename.set(filename)
                self.output_dir.set("/".join(filename.split("/")[:-1]))
                # calculate potential simulation years
                key = fuels.Fvsfuels("iec") # variant irrelevant for this operation
                key.set_keyword(filename)
                self.fuel_wdir = key.wdir
                sim_years = range(key.inv_year, key.inv_year + (key.num_cyc * key.time_int)
                                  + 1, key.time_int)
                self.sim_years_cb["values"] = sim_years
            else:
                tkMessageBox.showerror("", "The specified file must have a '.key' extension",
                                       parent=self.root)
        self.update_status("Ready")

    def get_output_dir(self):
        """ Opens file dialog and returns user specified directory """

        file_opt = options = {}
        options["parent"] = self.root
        options["title"] = "Select Output Directory"
        output_dir = str(tkFileDialog.askdirectory(**file_opt))
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
        Toggle logic for LiDAR vs standard STANDFIRE run. Resets lidar and
        related input variables when the lidar check box is toggled.
        """

        if self.ldr_check_var.get() == 0:
            self.lidar_shapefile.set("")
            self.shape_entry.configure(state="disabled")
            self.shp_brws_btn.configure(state="disabled")
            self.x_entry.configure(state="enabled")
            self.y_entry.configure(state="enabled")
            self.is_initialized = False # suppress "are you sure" message
            self.reset()
        else:
            self.shape_entry.configure(state="enabled")
            self.shp_brws_btn.configure(state="enabled")
            self.x_size.set("")
            self.x_entry.configure(state="disabled")
            self.y_size.set("")
            self.y_entry.configure(state="disabled")
            self.svs_offset.set("")
            self.origin_x.set("") # igniter
            self.origin_y.set("") # igniter
            self.width.set("") # igniter
            self.length.set("") # igniter

    def toggle_trt_check(self):
        """
        Toggle logic for treatment parameter entry fields. Resets treatment
        and related input variables when the treatment check box is toggled.
        """

        if self.trt_check_var.get() == 0:
            self.crown_spacing.set(0.0)
            self.prune_height.set(0.0)
            self.crown_entry.configure(state="disabled")
            self.prune_entry.configure(state="disabled")
        else:
            self.crown_entry.configure(state="enabled")
            self.prune_entry.configure(state="enabled")
            self.crown_spacing.set(1.5)
            self.prune_height.set(1.5)

    def toggle_unlock(self):
        """
        Toggle logic for igniter dimensions. Resets igniter dimension
        variables when the unlock values check box is toggled.
        """

        if self.unlock_check_var.get() == 0:
            self.origin_x_entry.configure(state="disabled")
            self.origin_y_entry.configure(state="disabled")
            self.width_entry.configure(state="disabled")
            self.length_entry.configure(state="disabled")
        else:
            self.origin_x_entry.configure(state="enabled")
            self.origin_y_entry.configure(state="enabled")
            self.width_entry.configure(state="enabled")
            self.length_entry.configure(state="enabled")

    def update_domain_change(self, internal_name, index, operation):
        """
        Reset calculated variables when the x or y dimensions are changed in
        the GUI. Tkinter return arguments:
                    Internal variable name
                    Index (only if the variable is a list)
                    Operation: w - write, r - read, u - unset (delete)
        """

        trace_return = ("Trace return: \nInternal variable name: " + internal_name +
                        "\n Index: " + index + "\n Operation: " + operation)
        try: # in case x or y is an empty string (no value in box)
            x_size = self.x_size.get()
            y_size = self.y_size.get()
            if self.ldr_check_var.get() == 0:
                # calculate x & y AOI dimensions and offset locations
                x_aoi_size = 64
                y_aoi_size = 64
                y_offset = max(1, int((y_size - y_aoi_size)/2.0))
                x_offset = max(1, x_size - (x_aoi_size + y_offset))
                # calculate WFDS igniter dimensions
                x_ign = max(1, int(0.3*float(x_offset)))
                y_ign = y_offset
                length = max(1, int(y_size - (y_ign *2)))
            elif self.ldr_check_var.get() == 1:
                # calculate x & y AOI dimensions and offset locations
                x_aoi_size = x_size # change when we implement diff AOI size in lidar
                y_aoi_size = y_size
                x_offset = max(1, int(x_size - x_aoi_size))
                # calculate WFDS igniter dimensions
                x_ign = max(1, int(x_size * 0.2))
                y_ign = max(1, int(y_size * 0.2))
                length = max(1, int(y_size * 0.6))
            else:
                print "Possible lidar check variable problem. See \
                        update_domain_change method"
            # set calculated variables
            self.svs_offset.set(x_offset)
            self.x_aoi_size.set(x_aoi_size)
            self.y_aoi_size.set(y_aoi_size)
            self.origin_x.set(x_ign)
            self.origin_y.set(y_ign)
            self.length.set(length)
            self.width.set(5)
        except ValueError: # x or y is an empty string
            # reset varibles in case of an error in the above calculations
            self.svs_offset.set("")
            self.x_aoi_size.set("")
            self.y_aoi_size.set("")
            self.origin_x.set("")
            self.origin_y.set("")
            self.length.set("")
            self.width.set("")

    def create_wfds_run_script(self):
        """
        Create an OS specific batch script that can be used to execute WFDS.
        Only linux and Windows are currently supported. Multiprocesing is not
        directly supported in Windows at the moment.
        """

        out_dir = self.output_dir.get()
        # linux bash file
        if platform.system().lower() == "linux":
            if self.n_mesh.get() == 1:
                with open(out_dir + "/output/runFDS.sh", "w") as script:
                    script.write(MOD_PATH + "/bin/fds_linux/wfds " + self.run_name.get() + ".txt")
            else:
                with open(out_dir + "/output/runFDS.sh", "w") as script:
                    script.write("module load mpi/openmpi-x86_64")
                    script.write("mpiexec -np " + str(self.n_mesh.get()) + " " + MOD_PATH +
                                 "/bin/fds_linux/wfds_mpi " + self.run_name.get() + ".txt")
        # windows batch file
        if platform.system().lower() == "windows":
            with open(out_dir + "/output/runFDS.bat", "w") as script:
                script.write(MOD_PATH + "/bin/fds_win/wfds.exe " +
                             self.run_name.get() + ".txt")
# multiprocessing not currently supported in Windows.
##            if self.n_mesh.get() == 1:
##                with open(out_dir + "/output/runFDS.bat", "w") as script:
##                    script.write(MOD_PATH + "/bin/fds_win/wfds.exe " + self.run_name.get() +
##                                 ".txt")
##            else:
##                with open(out_dir + "/output/runFDS.bat", "w") as script:
##                    script.write("mpiexec -np " + str(self.n_mesh.get()) + " " + MOD_PATH +
##                                 "/bin/fds_win/wfds_mpi.exe " + self.run_name.get() + ".txt")

    def create_smv_run_script(self):
        """
        Create an OS specific batch script that can be used to execute Smoke
        View. Only linux and Windows are supported at the moment.
        """

        out_dir = self.output_dir.get()
        # linux bash file
        if platform.system().lower() == "linux":
            with open(out_dir + "/output/runSMV.sh", "w") as script:
                script.write(MOD_PATH + "/bin/fds_linux/smokeview " + self.run_name.get() + ".smv")
        # windows batch file
        if platform.system().lower() == "windows":
            with open(out_dir + "/output/runSMV.bat", "w") as script:
                script.write(MOD_PATH + "/bin/fds_win/smokeview.exe " + self.run_name.get() +
                             ".smv")

    def run_button(self):
        """
        Calls either a standard or LiDAR run depending on the status of the
        lidar check box (ldr_check_var).
        """

        if self.ldr_check_var.get() == 1:
            lidar_run_ok, all_msg = self.run_lidar()
            if not lidar_run_ok:
                tkMessageBox.showerror("Lidar processing Error", "LiDAR "
                                       "processing failed. Verify LiDAR shapefile "
                                       "input requirements and try again.", parent=self.root)
                print all_msg
                self.status.set(" Status: Ready")
                return
        elif self.ldr_check_var.get() == 0:
            self.run_standard()
        else: print "Something wrong with ldr_check_var variable"

        # run the capsis/wfds/smv methods for both.
        self.run_capsis()
        self.config_wfds()
        # check if execute WFDS is selected
        if self.fds_check_var.get() == 1:
            self.run_wfds()
        self.update_status("Done")
        self.root.update()
        time.sleep(2)
        self.end("STANDFIRE simulation finished")

    def run_lidar(self):
        """
        This method processes the input lidar shapefile, runs multiple FVS
        simulations and collates the FVS simulation outputs for a lidar based
        STANDFIRE simulation
        """

        # Lidar processing
        lidar_start = timeit.default_timer()
        lidar_run_ok = True
        all_msg = "\nLIDAR PROCESSING MESSAGES: \n"
        self.update_status("Processing LiDAR shapefile...")
        self.root.update()
        lidar_shp = os.path.abspath(self.lidar_shapefile.get())
        # set intermediate and output file names
        fishnet_shp = lidar_shp[:-4]+"_fishnet.shp"
        new_lidar_shp = lidar_shp[:-4] + "_out.shp"
        lidar_csv = new_lidar_shp[:-4]+"_export.csv"
        # instantiate fuels object
        fuel = fuels.Fvsfuels(VARIANTS[self.variant_cb.get()])
        fuel.set_keyword(self.keyword_filename.get())
        # instantiate lidar objects
        ldr = lidar.ConvertLidar(lidar_shp, fishnet_shp, new_lidar_shp)
        fvs = lidar.FVSFromLidar(fuel, lidar_csv, self.keyword_filename.get())
        # Calculate extents of input shapefile
        extents = ldr.calculate_extents()
        # set some coordinate variables
        xy_origin = [extents[0], extents[2]]
        # LiDAR processing steps
        processing_steps = {
            "create_fishnet": extents,
            "copy_shapefile": "",
            "cleanup_lidar_fields": "",
            "fishnet_id": "",
            "cleanup_lidar_features": "",
            "add_attribute_fields": "",
            "calculate_attribute_fields": "",
            "number_trees": "",
            "export_attributes_to_csv": "lidar_csv" # lidar_csv in "" necessary for eval()
            }
        # Vanilla python dictionaries don't respect order. Therefore:
        ordered_key = ["create_fishnet", "copy_shapefile", "cleanup_lidar_fields",
                       "fishnet_id", "cleanup_lidar_features", "add_attribute_fields",
                       "calculate_attribute_fields", "number_trees", "export_attributes_to_csv"]
        for key in ordered_key:
            print "\nRunning: ", key
            # eval usage- as formulated it may not be too much of a security
            #  risk (processing_steps keys are hard-wired) but it's probably
            #  worthwhile to figure a way around its usage in the future...
            lidar_step_ok, msg = eval("ldr.%s(%s)" % (key, processing_steps[key]))
            if not lidar_step_ok:
                lidar_run_ok = False
                all_msg += msg + "Step ok: " + str(lidar_step_ok) + "\n"
                return lidar_run_ok, all_msg
            else:
                all_msg += msg + "Step ok: " + str(lidar_step_ok) + "\n"
        # Runs FVS simulation for each plot
        if lidar_run_ok:
            print all_msg
            self._lidar_fvs(fvs, xy_origin)
            lidar_elapsed = timeit.default_timer() - lidar_start
            print "Converting lidar data took: "+str(round(lidar_elapsed, 3))+" seconds."
        return lidar_run_ok, all_msg

    def _lidar_fvs(self, fvs, xy_origin):
        """
        Pseudo-private method.
        Run an FVS simulation from lidar data.
        :param fvs: FVSFromLidar class from lidar submodule
        :type fvs: object
        :param xy_origin: pair of coordinates describing the origin of the lidar
             shapefile
        :type xy_origin: list
        """

        self.update_status("FVS simulation...")
        self.root.update()
        time.sleep(1)
        # run the FVS simulations
        fvs_csv = fvs.run_fvs_lidar()
        # collate into CAPSIS input file
        fvs.create_capsis_csv(xy_origin, fvs_csv)

    def run_standard(self):
        """
        This method takes user input and runs the FVS simulation for a standard
        STANDFIRE simulation.
        """

        self.update_status("FVS simulation...")
        self.root.update()
        # instantiate fuels object
        fuel = fuels.Fvsfuels(VARIANTS[self.variant_cb.get()])
        fuel.set_keyword(self.keyword_filename.get())
        fuel.run_fvs()
        self.update_status("Saving FVS fuels output...")
        self.root.update()
        time.sleep(1)
        # write capsis input file (csv)
        fuel.save_trees_by_year(int(self.sim_years_cb.get()))

    def run_capsis(self):
        """ This method configures and runs CAPSIS """

        # copy orginal fvs report before it gets over-written ?temp solution?
        fvs_report = self.keyword_filename.get()[:-4] + ".out"
        shutil.copyfile(fvs_report, fvs_report[:-4] + "_fvs_report.out")
        # instantiate fuels object
        fuel = fuels.Fvsfuels(VARIANTS[self.variant_cb.get()])
        fuel.set_keyword(self.keyword_filename.get())
        svs_base = fuel.get_standid()
        self.update_status("Configuring Capsis...")
        self.root.update()
        time.sleep(1)
        # configure the capsis run
        cap = capsis.RunConfig(self.fuel_wdir[:-1]) # removes trailing "/" in fuel_wdir
        # spatial domain - also calculates x & y offsets and surface fuel block dimensions
        cap.set_xy_size(self.x_size.get(), self.y_size.get(), self.x_aoi_size.get(),
                        self.y_aoi_size.get())
        cap.set_z_size(self.z_size.get())
        # set surface fuels
        cap.set_srf_height(self.shrub_ht.get(), self.herb_ht.get(), self.litter_ht.get())
        load_shrub_dead = round(self.shrub_load.get() * (self.shrub_percent_dead.get()/100.), 4)
        load_shrub_live = round(self.shrub_load.get() *
                                (1 - (self.shrub_percent_dead.get()/100.)), 4)
        load_herb_dead = round(self.herb_load.get() * (self.herb_percent_dead.get()/100.), 4)
        load_herb_live = round(self.herb_load.get() * (1 - (self.herb_percent_dead.get()/100.)), 4)
        cap.set_srf_live_load(load_shrub_live, load_herb_live)
        cap.set_srf_dead_load(load_shrub_dead, load_herb_dead, self.litter_load.get())
        cap.set_srf_live_svr(self.shrub_sav.get(), self.herb_sav.get())
        cap.set_srf_dead_svr(self.shrub_sav.get(), self.herb_sav.get(), self.litter_sav.get())
        cap.set_srf_cover(self.shrub_cover.get()/100., self.herb_cover.get()/100.,
                          self.litter_cover.get()/100.)
        cap.set_srf_patch(self.shrub_patch.get(), self.herb_patch.get(), self.litter_patch.get()) #B
        cap.set_srf_live_mc(self.shrub_live_mc.get(), self.herb_live_mc.get())
        cap.set_srf_dead_mc(self.shrub_dead_mc.get(), self.herb_dead_mc.get(),
                            self.litter_dead_mc.get())
        # treatments
        if self.trt_check_var.get() == 1:
            cap.set_crown_space(self.crown_spacing.get())
            cap.set_prune_height(self.prune_height.get())
        # show 3D
        if self.viewer_check_var.get() == 1:
            cap.set_show_3d("true")
        # extrapolate tree AOI sample to entire scene for standard runs
        if self.ldr_check_var.get() == 0:
            b_extend = True
        elif self.ldr_check_var.get() == 1:
            b_extend = False
        cap.set_extend_fvs_sample(b_extend)
        # set subset fds percentage
        if self.subset_fds_check_var.get() == 1:
            # when user percent entry is added, get and set the value here
            subset_percent = 0.01
        else:
            subset_percent = 1.0
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
        self.execute_capsis = capsis.Execute(cap.params["path"] + "/capsis_run_file.txt",#B
                                             subset_percent)#B

    def config_wfds(self):
        """
        Configures a WFDS simulation and creates WFDS and SMOKEVIEW run scripts.
        CC - computational coordinates, PC - physical coordinates
        """

        # instantiate a WFDS object
        self.update_status("Configuring WFDS...")
        self.root.update()
        time.sleep(1)
        fds = wfds.WFDS(self.x_size.get(), self.y_size.get(), self.z_size.get(),
                        self.x_aoi_size.get(), self.svs_offset.get(), self.res.get(),
                        self.n_mesh.get(), self.execute_capsis.fuels)
        # stretch the mesh. CC and PC are currently hard-wired
        fds.create_mesh(stretch={"CC":[3, 33], "PC":[1, 31]})
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
        fds.save_input(self.fuel_wdir + "output/" + self.run_name.get() + ".txt")
        # clean up directory
        self.update_status("Cleaning up directory...")
        self.root.update()
        time.sleep(1)
        cur_dir = "/".join(self.keyword_filename.get().split("/")[:-1])
        if self.output_dir.get() != cur_dir: ###
            if os.path.isdir(self.output_dir.get() + "/output"):
                shutil.rmtree(self.output_dir.get() + "/output")
            shutil.copytree(cur_dir + "/output", self.output_dir.get() + "/output")
            shutil.rmtree(cur_dir + "/output")
            files = os.listdir("/".join(self.keyword_filename.get().split("/")[:-1]))
            keep = ["key", "tre"]
            for i in files:
                if i.split(".")[-1] not in keep:
                    shutil.copy(cur_dir + "/" + i, self.output_dir.get())
                    #Note: problem here if user selects a different output directory
                    os.remove(cur_dir + "/" + i)
        # create wfds and smv run scripts
        self.create_wfds_run_script()
        self.create_smv_run_script()

    def run_wfds(self):
        """ Executes WFDS simulation """

        self.update_status("Executing WFDS...")
        self.root.update()
        time.sleep(1)
        os.chdir(self.output_dir.get() + "/output/")
        if platform.system().lower() == "linux":
            os.system("gnome-terminal -e sh runFDS.sh")
        if platform.system().lower() == "windows":
            os.system("start cmd /K runFDS.bat")

    def create_variables(self):
        """ Application variable definitions """

        # boolean check variables
        self.trt_check_var = tk.IntVar(self.root)
        self.viewer_check_var = tk.IntVar(self.root)
        self.fds_check_var = tk.IntVar(self.root)
        self.unlock_check_var = tk.IntVar(self.root)
        self.ldr_check_var = tk.IntVar(self.root)
        self.subset_fds_check_var = tk.IntVar(self.root)
        # entry field variables
        # =====================
        # lidar run
        self.lidar_shapefile = tk.StringVar(self.root)
        # canopy fuels
        self.keyword_filename = tk.StringVar(self.root)
        # surface fuels
        self.shrub_ht = tk.DoubleVar(self.root)
        self.shrub_cbh = tk.DoubleVar(self.root)
        self.shrub_load = tk.DoubleVar(self.root)
        self.shrub_sav = tk.IntVar(self.root)
        self.shrub_cover = tk.IntVar(self.root)
        self.shrub_patch = tk.DoubleVar(self.root)
        self.shrub_live_mc = tk.IntVar(self.root)
        self.shrub_dead_mc = tk.IntVar(self.root)
        self.shrub_percent_dead = tk.IntVar(self.root)
        self.herb_ht = tk.DoubleVar(self.root)
        self.herb_cbh = tk.DoubleVar(self.root)
        self.herb_load = tk.DoubleVar(self.root)
        self.herb_sav = tk.IntVar(self.root)
        self.herb_cover = tk.IntVar(self.root)
        self.herb_patch = tk.DoubleVar(self.root)
        self.herb_live_mc = tk.IntVar(self.root)
        self.herb_dead_mc = tk.IntVar(self.root)
        self.herb_percent_dead = tk.IntVar(self.root)
        self.litter_ht = tk.DoubleVar(self.root)
        self.litter_load = tk.DoubleVar(self.root)
        self.litter_sav = tk.IntVar(self.root)
        self.litter_cover = tk.IntVar(self.root)
        self.litter_patch = tk.DoubleVar(self.root)
        self.litter_dead_mc = tk.IntVar(self.root)
        # simulation area
        self.x_size = tk.IntVar(self.root)
        self.y_size = tk.IntVar(self.root)
        self.z_size = tk.IntVar(self.root)
        self.x_aoi_size = tk.IntVar(self.root)
        self.y_aoi_size = tk.IntVar(self.root)
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
        self.x_size.trace("w", self.update_domain_change)
        self.y_size.trace("w", self.update_domain_change)
        # status bar
        self.status = tk.StringVar(self.root)

    def reset(self):
        """
        Resets values to default
        ..note:: This method is called when GUI is initialized
        """

        # confirm reset
        if self.is_initialized:
            result = tkMessageBox.askquestion("Reset Values",
                                              "Are you sure you want to reset values to default?",
                                              icon="warning", parent=self.root)
        else:
            result = "yes"
        self.is_initialized = True
        # reset values
        if result == "yes":
            self.viewer_check_var.set(1)
            # reset lidar
            self.ldr_check_var.set(0)
            self.lidar_shapefile.set("")
            self.shape_entry.configure(state="disabled")
            self.shp_brws_btn.configure(state="disabled")
            self.x_entry.configure(state="enabled")
            self.y_entry.configure(state="enabled")
            # surface fuels
            self.shrub_ht.set(0.35)
            self.shrub_cbh.set(0.0)
            self.shrub_load.set(0.8)
            self.shrub_sav.set(5000)
            self.shrub_cover.set(50)
            self.shrub_patch.set(5.0)
            self.shrub_live_mc.set(100)
            self.shrub_dead_mc.set(40)
            self.shrub_percent_dead.set(10)
            self.herb_ht.set(0.35)
            self.herb_cbh.set(0.0)
            self.herb_load.set(0.8)
            self.herb_sav.set(5000)
            self.herb_cover.set(80)
            self.herb_patch.set(1.0)
            self.herb_live_mc.set(100)
            self.herb_dead_mc.set(5)
            self.herb_percent_dead.set(100)
            self.litter_ht.set(0.1)
            self.litter_load.set(0.5)
            self.litter_sav.set(2000)
            self.litter_cover.set(100)
            self.litter_patch.set(-1)
            self.litter_dead_mc.set(10)
            # simulation area
            self.x_size.set(160)
            self.y_size.set(90)
            self.z_size.set(50)
            self.x_aoi_size.set(64)
            self.y_aoi_size.set(64)
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
        """ Widget definitions and configuration """

        entry_opts = dict(width="5")
        # Lidar panel
        # ===========
        self.group_lidar = ttk.LabelFrame(self.root, text="LiDAR Run")
        self.lidar_check = ttk.Checkbutton(self.group_lidar, text="Enable LiDAR Run",
                                           variable=self.ldr_check_var,
                                           command=self.toggle_ldr_check)
        self.shape_lbl = ttk.Label(self.group_lidar, text="Specify LiDAR shapefile:")
        self.shape_entry = ttk.Entry(self.group_lidar, textvariable=self.lidar_shapefile)
        self.shape_entry.configure(state="disabled")
        self.shp_brws_btn = ttk.Button(self.group_lidar, text="Browse...",
                                       command=self.get_lidar_shapefile)
        self.shp_brws_btn.configure(state="disabled")
        # Canopy fuels panel
        # ==================
        self.group_canopy_fuels = ttk.LabelFrame(self.root, text="Canopy Fuels")
        self.keyword_lbl = ttk.Label(self.group_canopy_fuels, text="Specify FVS keyword file:")
        self.keyword_entry = ttk.Entry(self.group_canopy_fuels, textvariable=self.keyword_filename)
        self.browse_bnt = ttk.Button(self.group_canopy_fuels, text="Browse...",
                                     command=self.get_keyword_file)
        self.variant_lbl = ttk.Label(self.group_canopy_fuels, text="Select FVS variant:")
        self.variant_cb = ttk.Combobox(self.group_canopy_fuels, values=VARIANTS.keys(),
                                       state="readonly")
        self.variant_cb.current(2)
        self.sim_years_lbl = ttk.Label(self.group_canopy_fuels, text="Simulation year:")
        self.sim_years_cb = ttk.Combobox(self.group_canopy_fuels, values="", state="readonly")
        # Surface fuels panel
        # ===================
        self.group_surface_fuels = ttk.LabelFrame(self.root, text="Surface Fuels")
        # Labels
        self.ht_lbl = ttk.Label(self.group_surface_fuels, text="height (m):")
        self.load_lbl = ttk.Label(self.group_surface_fuels, text=u"load (kg/m\u00B2):")
        self.sav_lbl = ttk.Label(self.group_surface_fuels, text="SAV:")
        self.cover_lbl = ttk.Label(self.group_surface_fuels, text="cover (%):")
        self.patch_lbl = ttk.Label(self.group_surface_fuels, text="patch size (m):")
        self.live_mc_lbl = ttk.Label(self.group_surface_fuels, text="live mc (%):")
        self.dead_mc_lbl = ttk.Label(self.group_surface_fuels, text="dead mc (%):")
        self.percent_dead_lbl = ttk.Label(self.group_surface_fuels, text="percent dead:")
        # Shrubs
        self.shrub_lbl = ttk.Label(self.group_surface_fuels, text="Shrubs")
        self.shrub_ht_entry = ttk.Entry(self.group_surface_fuels, textvariable=self.shrub_ht,
                                        **entry_opts)
        self.shrub_load_entry = ttk.Entry(self.group_surface_fuels, textvariable=self.shrub_load,
                                          **entry_opts)
        self.shrub_sav_entry = ttk.Entry(self.group_surface_fuels, textvariable=self.shrub_sav,
                                         **entry_opts)
        self.shrub_cover_entry = ttk.Entry(self.group_surface_fuels, textvariable=self.shrub_cover,
                                           **entry_opts)
        self.shrub_patch_entry = ttk.Entry(self.group_surface_fuels, textvariable=self.shrub_patch,
                                           **entry_opts)
        self.shrub_live_mc_entry = ttk.Entry(self.group_surface_fuels,
                                             textvariable=self.shrub_live_mc, **entry_opts)
        self.shrub_dead_mc_entry = ttk.Entry(self.group_surface_fuels,
                                             textvariable=self.shrub_dead_mc, **entry_opts)
        self.shrub_percent_dead_entry = ttk.Entry(self.group_surface_fuels,
                                                  textvariable=self.shrub_percent_dead,
                                                  **entry_opts)
        # Herbs
        self.herb_lbl = ttk.Label(self.group_surface_fuels, text="Herbs")
        self.herb_ht_entry = ttk.Entry(self.group_surface_fuels, textvariable=self.herb_ht,
                                       **entry_opts)
        self.herb_load_entry = ttk.Entry(self.group_surface_fuels, textvariable=self.herb_load,
                                         **entry_opts)
        self.herb_sav_entry = ttk.Entry(self.group_surface_fuels, textvariable=self.herb_sav,
                                        **entry_opts)
        self.herb_cover_entry = ttk.Entry(self.group_surface_fuels, textvariable=self.herb_cover,
                                          **entry_opts)
        self.herb_patch_entry = ttk.Entry(self.group_surface_fuels, textvariable=self.herb_patch,
                                          **entry_opts)
        self.herb_live_mc_entry = ttk.Entry(self.group_surface_fuels,
                                            textvariable=self.herb_live_mc, **entry_opts)
        self.herb_dead_mc_entry = ttk.Entry(self.group_surface_fuels,
                                            textvariable=self.herb_dead_mc, **entry_opts)
        self.herb_percent_dead_entry = ttk.Entry(self.group_surface_fuels,
                                                 textvariable=self.herb_percent_dead, **entry_opts)
        # Litter
        self.litter_lbl = ttk.Label(self.group_surface_fuels, text="Litter")
        self.litter_ht_entry = ttk.Entry(self.group_surface_fuels, textvariable=self.litter_ht,
                                         **entry_opts)
        self.litter_load_entry = ttk.Entry(self.group_surface_fuels, textvariable=self.litter_load,
                                           **entry_opts)
        self.litter_sav_entry = ttk.Entry(self.group_surface_fuels, textvariable=self.litter_sav,
                                          **entry_opts)
        self.litter_cover_entry = ttk.Entry(self.group_surface_fuels,
                                            textvariable=self.litter_cover,
                                            **entry_opts)
        self.litter_patch_entry = ttk.Entry(self.group_surface_fuels,
                                            textvariable=self.litter_patch,
                                            **entry_opts)
        self.litter_live_mc_entry = ttk.Entry(self.group_surface_fuels, **entry_opts)
        self.litter_live_mc_entry.configure(state="disabled")
        self.litter_dead_mc_entry = ttk.Entry(self.group_surface_fuels,
                                              textvariable=self.litter_dead_mc, **entry_opts)
        self.litter_percent_dead_entry = ttk.Entry(self.group_surface_fuels, **entry_opts)
        self.litter_percent_dead_entry.configure(state="disabled")
        # Simulation Area
        # ===============
        self.group_sim_area = ttk.LabelFrame(self.root, text="Simulation Area")
        self.x_lbl = ttk.Label(self.group_sim_area, text="x size (m):")
        self.x_entry = ttk.Entry(self.group_sim_area, textvariable=self.x_size, **entry_opts)
        self.y_lbl = ttk.Label(self.group_sim_area, text="y size (m):")
        self.y_entry = ttk.Entry(self.group_sim_area, textvariable=self.y_size, **entry_opts)
        self.z_lbl = ttk.Label(self.group_sim_area, text="z size (m):")
        self.z_entry = ttk.Entry(self.group_sim_area, textvariable=self.z_size, **entry_opts)
        self.res_lbl = ttk.Label(self.group_sim_area, text="resolution (m):")
        self.res_entry = ttk.Entry(self.group_sim_area, textvariable=self.res, **entry_opts)
        self.meshes_lbl = ttk.Label(self.group_sim_area, text="number of meshes:")
        self.meshes_entry = ttk.Entry(self.group_sim_area, textvariable=self.n_mesh, **entry_opts)
        # Weather
        # =======
        self.group_weather = ttk.LabelFrame(self.root, text="Weather")
        self.wind_lbl = ttk.Label(self.group_weather, text="wind speed (m/s):")
        self.wind_entry = ttk.Entry(self.group_weather, textvariable=self.wind_speed, **entry_opts)
        self.temp_lbl = ttk.Label(self.group_weather, text=u"temperature (\u00B0C):")
        self.temp_entry = ttk.Entry(self.group_weather, textvariable=self.temp, **entry_opts)
        # Ignitor Fire
        # ============
        self.group_ignitor_fire = ttk.LabelFrame(self.root, text="Igniter Fire")
        self.origin_x_lbl = ttk.Label(self.group_ignitor_fire, text="origin x (m):")
        self.origin_x_entry = ttk.Entry(self.group_ignitor_fire, textvariable=self.origin_x,
                                        **entry_opts)
        self.origin_x_entry.configure(state="disabled")
        self.origin_y_lbl = ttk.Label(self.group_ignitor_fire, text="origin y (m):")
        self.origin_y_entry = ttk.Entry(self.group_ignitor_fire, textvariable=self.origin_y,
                                        **entry_opts)
        self.origin_y_entry.configure(state="disabled")
        self.width_lbl = ttk.Label(self.group_ignitor_fire, text="width (m):")
        self.width_entry = ttk.Entry(self.group_ignitor_fire, textvariable=self.width, **entry_opts)
        self.width_entry.configure(state="disabled")
        self.length_lbl = ttk.Label(self.group_ignitor_fire, text="length (m):")
        self.length_entry = ttk.Entry(self.group_ignitor_fire, textvariable=self.length,
                                      **entry_opts)
        self.length_entry.configure(state="disabled")
        self.hrr_lbl = ttk.Label(self.group_ignitor_fire, text=u"hrr (kW/m\u00B2):")
        self.hrr_entry = ttk.Entry(self.group_ignitor_fire, textvariable=self.hrr, **entry_opts)
        self.start_time_lbl = ttk.Label(self.group_ignitor_fire, text="start time (s):")
        self.start_time_entry = ttk.Entry(self.group_ignitor_fire, textvariable=self.start_time,
                                          **entry_opts)
        self.end_time_lbl = ttk.Label(self.group_ignitor_fire, text="end time (s):")
        self.end_time_entry = ttk.Entry(self.group_ignitor_fire, textvariable=self.end_time,
                                        **entry_opts)
        self.auto_calc_bnt = ttk.Checkbutton(self.group_ignitor_fire, text="Unlock values",
                                             variable=self.unlock_check_var,
                                             command=self.toggle_unlock)
        # Treatments
        # ==========
        self.group_treatments = ttk.LabelFrame(self.root, text="Treatments")
        self.trt_check = ttk.Checkbutton(self.group_treatments, text="Apply fuel treatment",
                                         variable=self.trt_check_var, command=self.toggle_trt_check)
        self.crown_lbl = ttk.Label(self.group_treatments, text="crown spacing (m):")
        self.crown_entry = ttk.Entry(self.group_treatments, textvariable=self.crown_spacing,
                                     **entry_opts)
        self.crown_entry.configure(state="disabled")
        self.prune_lbl = ttk.Label(self.group_treatments, text="prune height (m):")
        self.prune_entry = ttk.Entry(self.group_treatments, textvariable=self.prune_height,
                                     **entry_opts)
        self.prune_entry.configure(state="disabled")
        # Run Settings
        # ============
        self.group_run_settings = ttk.LabelFrame(self.root, text="Run Settings")
        self.viewer_check = ttk.Checkbutton(self.group_run_settings, text="Show 3D stand viewer",
                                            variable=self.viewer_check_var)
        self.fds_check = ttk.Checkbutton(self.group_run_settings, text="Execute WFDS",
                                         variable=self.fds_check_var)
        self.subset_fds_check = ttk.Checkbutton(self.group_run_settings,
                                                text="Subset WFDS fuels tracking",
                                                variable=self.subset_fds_check_var)
        self.sim_time_lbl = ttk.Label(self.group_run_settings, text="simulation time (s):")
        self.sim_time_entry = ttk.Entry(self.group_run_settings, textvariable=self.sim_time,
                                        **entry_opts)
        self.run_name_lbl = ttk.Label(self.group_run_settings, text="run name:")
        self.run_name_entry = ttk.Entry(self.group_run_settings, textvariable=self.run_name,
                                        **entry_opts)
        self.output_dir_lbl = ttk.Label(self.group_run_settings, text="output directory:")
        self.output_dir_entry = ttk.Entry(self.group_run_settings, textvariable=self.output_dir,
                                          **entry_opts)
        self.output_dir_bnt = ttk.Button(self.group_run_settings, text="Browse...",
                                         command=self.get_output_dir)
        # Control Buttons
        # ===============
        self.reset_form_bnt = ttk.Button(self.root, text="Reset Values", command=self.reset)
        self.gen_bnt = ttk.Button(self.root, text="Run", command=self.run_button)
        # Status Bar
        # ==========
        self.status_lbl = ttk.Label(self.root, relief=tk.SUNKEN, anchor=tk.W,
                                    textvariable=self.status, font=("Arial", 10))

    def grid_widgets(self):
        """ Widget positioning and auto-resize configuration """

        options = dict(sticky="NSEW", padx=5, pady=5)
        # Lidar panel
        # ===========
        self.group_lidar.grid(row=0, columnspan=5, **options)
        self.group_lidar.columnconfigure(2, weight=1)
        self.lidar_check.grid(row=0, column=0, **options)
        self.shape_lbl.grid(row=0, column=1, **options)
        self.shape_entry.grid(row=0, column=2, columnspan=2, **options)
        self.shp_brws_btn.grid(row=0, column=4, **options)
        # Canopy fuels panel
        # ==================
        self.group_canopy_fuels.grid(row=1, columnspan=4, **options)
        self.group_canopy_fuels.columnconfigure(1, weight=1)
        self.keyword_lbl.grid(row=0, column=0, **options)
        self.keyword_entry.grid(row=0, column=1, columnspan=2, **options)
        self.browse_bnt.grid(row=0, column=3, **options)
        self.variant_lbl.grid(row=1, column=0, **options)
        self.variant_cb.grid(row=1, column=1, **options)
        self.sim_years_lbl.grid(row=1, column=2, **options)
        self.sim_years_cb.grid(row=1, column=3, **options)
        # Surface fuels panel
        # ===================
        self.group_surface_fuels.grid(row=2, rowspan=2, column=0, **options)
        self.group_surface_fuels.columnconfigure(1, weight=1)
        self.group_surface_fuels.columnconfigure(2, weight=1)
        self.group_surface_fuels.columnconfigure(3, weight=1)
        # Labels
        self.ht_lbl.grid(row=1, column=0, **options)
        self.load_lbl.grid(row=2, column=0, **options)
        self.sav_lbl.grid(row=3, column=0, **options)
        self.cover_lbl.grid(row=4, column=0, **options)
        self.patch_lbl.grid(row=5, column=0, **options)
        self.live_mc_lbl.grid(row=6, column=0, **options)
        self.dead_mc_lbl.grid(row=7, column=0, **options)
        self.percent_dead_lbl.grid(row=8, column=0, **options)
        # Shrubs
        self.shrub_lbl.grid(row=0, column=1, **options)
        self.shrub_ht_entry.grid(row=1, column=1, **options)
        self.shrub_load_entry.grid(row=2, column=1, **options)
        self.shrub_sav_entry.grid(row=3, column=1, **options)
        self.shrub_cover_entry.grid(row=4, column=1, **options)
        self.shrub_patch_entry.grid(row=5, column=1, **options)
        self.shrub_live_mc_entry.grid(row=6, column=1, **options)
        self.shrub_dead_mc_entry.grid(row=7, column=1, **options)
        self.shrub_percent_dead_entry.grid(row=8, column=1, **options)
        # Herbs
        self.herb_lbl.grid(row=0, column=2, **options)
        self.herb_ht_entry.grid(row=1, column=2, **options)
        self.herb_load_entry.grid(row=2, column=2, **options)
        self.herb_sav_entry.grid(row=3, column=2, **options)
        self.herb_cover_entry.grid(row=4, column=2, **options)
        self.herb_patch_entry.grid(row=5, column=2, **options)
        self.herb_live_mc_entry.grid(row=6, column=2, **options)
        self.herb_dead_mc_entry.grid(row=7, column=2, **options)
        self.herb_percent_dead_entry.grid(row=8, column=2, **options)
        # Litter
        self.litter_lbl.grid(row=0, column=3, **options)
        self.litter_ht_entry.grid(row=1, column=3, **options)
        self.litter_load_entry.grid(row=2, column=3, **options)
        self.litter_sav_entry.grid(row=3, column=3, **options)
        self.litter_cover_entry.grid(row=4, column=3, **options)
        self.litter_patch_entry.grid(row=5, column=3, **options)
        self.litter_live_mc_entry.grid(row=6, column=3, **options)
        self.litter_dead_mc_entry.grid(row=7, column=3, **options)
        self.litter_percent_dead_entry.grid(row=8, column=3, **options)
        # Simulation Area
        # ===============
        self.group_sim_area.grid(row=2, column=1, **options)
        self.group_sim_area.columnconfigure(1, weight=1)
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
        self.group_weather.grid(row=3, column=1, **options)
        self.group_weather.columnconfigure(1, weight=1)
        self.wind_lbl.grid(row=0, column=0, **options)
        self.wind_entry.grid(row=0, column=1, **options)
        self.temp_lbl.grid(row=1, column=0, **options)
        self.temp_entry.grid(row=1, column=1, **options)
        # Ignitor Fire
        # ============
        self.group_ignitor_fire.grid(row=2, rowspan=2, column=2, **options)
        self.group_ignitor_fire.columnconfigure(1, weight=1)
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
        self.group_treatments.grid(row=4, column=0, columnspan=3, **options)
        self.group_treatments.columnconfigure(2, weight=1)
        self.group_treatments.columnconfigure(4, weight=1)
        self.trt_check.grid(row=0, column=0, **options)
        self.crown_lbl.grid(row=0, column=1, **options)
        self.crown_entry.grid(row=0, column=2, **options)
        self.prune_lbl.grid(row=0, column=3, **options)
        self.prune_entry.grid(row=0, column=4, **options)
        # Run Settings
        # ============
        self.group_run_settings.grid(row=5, rowspan=3, column=0, columnspan=2, **options)
        self.group_run_settings.columnconfigure(1, weight=1)
        self.sim_time_lbl.grid(row=0, column=2, **options)
        self.sim_time_entry.grid(row=0, column=3, **options)
        self.run_name_lbl.grid(row=0, column=0, **options)
        self.run_name_entry.grid(row=0, column=1, **options)
        self.output_dir_lbl.grid(row=1, column=0, **options)
        self.output_dir_entry.grid(row=1, column=1, columnspan=2, **options)
        self.output_dir_bnt.grid(row=1, column=3, **options)
        self.viewer_check.grid(row=2, column=0, **options)
        self.fds_check.grid(row=2, column=1, columnspan=1, **options)
        self.subset_fds_check.grid(row=2, column=2, columnspan=2, **options)
        # Control Buttons
        # ===============
        self.reset_form_bnt.grid(row=6, column=2, **options)
        self.gen_bnt.grid(row=7, column=2, **options)
        # Status bar
        # ==========
        self.status_lbl.grid(row=8, column=0, columnspan=4, sticky="NSEW", padx=0)

    def end(self, msg):
        """ End the STANDFIRE program. Currently this leaves the GUI open. """

        self.update_status(msg)
        self.root.update()
        print "The end method was called"
        return #temp
        #self.root.destroy()

    def test(self, key_path, out_name):
        """ Preset input variables to speed testing. Not finished """

##        self.ldr_check_var.set(1)
##        ldr_shp = <path to lidar shapefile>
##        self.lidar_shapefile.set(ldr_shp)

        self.keyword_filename.set(key_path)
        self.output_dir.set("/".join(key_path.split("/")[:-1]))
        # calculate potential simulation years
        key = fuels.Fvsfuels("iec") # variant irrelevant for this operation
        key.set_keyword(key_path)
        sim_years = range(key.inv_year, key.inv_year + (key.num_cyc * key.time_int)
                          + 1, key.time_int)
        self.sim_years_cb["values"] = sim_years
        self.run_name.set(out_name)
        self.viewer_check_var.set(0)

if __name__ == "__main__":
    Application.main()
    print "Done"
