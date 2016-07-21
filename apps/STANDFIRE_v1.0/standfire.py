

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

cap.params['gridNumber'] = 1

cap.set_svs_base(svs_base + "_2010")

cap.save_config()

# now run capsis
exeCap = capsis.Execute(cap.params['path'] + '/capsis_run_file.txt')
