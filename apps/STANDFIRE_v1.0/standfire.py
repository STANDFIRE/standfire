

# relative path module import

import os
import sys

mod_path = '/'.join(os.path.dirname(os.path.abspath(__file__)).split('/')[:-2]) + '/standfire/'
sys.path.append(mod_path)
print mod_path

# import standfire modules
import fuels
import capsis
import wfds

# instantiate a Fvsfules object
fuel = fuels.Fvsfuels('iec')

# ask user for path to keyword file
key = input("Please specify keyword file: ")
fuel.set_keyword(key)

# start simulation
fuel.run_fvs()

# write fvs fuel files
fuel.save_trees_by_year(2010)

# get the fvs run name for capsis svs base init
svs_base = fuel.get_standid()

# configure the capsis run (pass the wdir from the fuels object to the
# constructor
cap = capsis.RunConfig(fuel.wdir)

cap.set_x_size(160)
cap.set_y_size(90)
cap.set_z_size(50)

#cap.params['gridNumber'] = 1

cap.set_svs_base(svs_base + "_2010")

cap.set_show3D('true')
#cap.params['show3d'] = 'true'
print cap.params

cap.save_config()



# now run capsis
exeCap = capsis.Execute(cap.params['path'] + '/capsis_run_file.txt')

# instantiate a WFDS object
fds = wfds.WFDS(160,90,50,1,9,exeCap.fuels)
print fds.params['fuels']
fds.create_mesh(stretch={'CC':[3,33], 'PC':[1,31], 'axis':'z'})
fds.create_ignition(10, 30, 35, 40, 13, 77)
fds.set_wind_speed(8.94)
fds.set_init_temp(30)
fds.set_simulation_time(300)
fds.set_hrrpua(1000)

fds.save_input(fuel.wdir + 'output/test_wfds.txt')

