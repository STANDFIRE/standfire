
import os
import sys
import platform

# relative path import for standfire modules
sep = os.sep

# mod_path for python run
#mod_path = sep.join(os.getcwd().split(sep)[:-2]) +  sep + 'standfire' + sep
#sys.path.append(mod_path)

# mod path for executable
mod_path = os.getcwd() + sep + 'standfire' + sep

# import for python run
#import metrics

# import for executable
from standfire import metrics

# simulation output directory
print ''
wdir = input("Enter simulation output directory:  ")

# get the run name from the .smv file
f = os.listdir(wdir)
for i in f:
    if i.split('.')[-1] == 'smv':
        run_name = i.split('.')[0]

print '\nCalculating...'

# mass loss
massloss = metrics.MassLoss(wdir)
massloss.get_tree_files()
massloss.read_tree_mass()
massloss_percent = massloss.get_total_mass_loss()

# rate of spread
fuel_1 = run_name + '_FUEL2_Herb_Leave_Dead_vegout.csv'
fuel_2 = run_name + '_FUEL8_Herb_Leave_Dead_vegout.csv'
ros = metrics.ROS(wdir, fuel_1, fuel_2, 64)
ros_val = ros.get_ros()

# get first slice file (VELOCITY)
files = os.listdir(wdir)
slices = []
for i in files:
    if i.split('.')[-1] == 'sf':
        slices.append(i)

slices.sort()
# wind profile
wind = metrics.WindProfile(wdir, slices[0], 29, 30, 1)
wind_prof =  wind.get_wind_profile()

# heat
heat = metrics.HeatTransfer(wdir)
heat.get_tree_files()
heat.read_tree_conv()
heat.read_tree_rad()

out_file = '\n'
out_file += '='*42
mass_title = 'Dry Mass Consumption'
buf = (42-len(mass_title))/2
out_file += '\n'
out_file += ' '*buf + mass_title
out_file += '\n'
out_file += '='*42
out_file += '\n'
out_file += '{:30s} {:10.2f}'.format('Total crown biomass (kg):', massloss.mass_sum[0])
out_file += '\n'
out_file += '{:30s} {:10.2f}'.format('Total consumed biomass (kg):', massloss.mass_sum[-1])
out_file += '\n'
out_file += '{:30s} {:10.2f}'.format('Percent consumed (%):', massloss_percent)

out_file += '\n'
out_file += '='*42
ros_title = 'Surface Fire Rate of Spread'
buf = (42-len(ros_title))/2
out_file += '\n'
out_file += ' '*buf + ros_title
out_file += '\n'
out_file += '='*42
out_file += '\n'
out_file += '{:30s} {:10.2f}'.format('Rate of spread (m/s):', ros_val)

out_file += '\n'
out_file += '='*42
wind_title = 'Wind Speed Profile'
buf = (42-len(wind_title))/2
out_file += '\n'
out_file += ' '*buf + wind_title
out_file += '\n'
out_file += '='*42
out_file += '\n'
out_file += '{:30s} {:10.2f}'.format('Wind speed at z=1m (m/s):', wind_prof[1])
out_file += '\n'
out_file += '{:30s} {:10.2f}'.format('Wind speed at z=5m (m/s):', wind_prof[5])
out_file += '\n'
out_file += '{:30s} {:10.2f}'.format('Wind speed at z=10m (m/s):', wind_prof[10])
out_file += '\n'
out_file += '{:30s} {:10.2f}'.format('Wind speed at z=15m (m/s):', wind_prof[15])
out_file += '\n'
out_file += '{:30s} {:10.2f}'.format('Wind speed at z=20m (m/s):', wind_prof[20])

out_file += '\n'
out_file += '='*42
heat_title = 'Crown Heat Transfer'
buf = (42-len(heat_title))/2
out_file += '\n'
out_file += ' '*buf + heat_title
out_file += '\n'
out_file += '='*42
out_file += '\n'
out_file += '{:30s} {:10.2f}'.format('Total convection (kW/m^2):', sum(heat.conv_sum)/heat.sim_area)
out_file += '\n'
out_file += '{:30s} {:10.2f}'.format('Total radiation (kW/m^2):', sum(heat.rad_sum)/heat.sim_area)
out_file += '\n'
out_file += '{:30s} {:10.2f}'.format('Peak convection (kW):', max(heat.conv_sum))
out_file += '\n'
out_file += '{:30s} {:10.2f}'.format('Peak radiation (kW):', max(heat.rad_sum))
out_file += '\n\n'
out_file += 'Initializing smokeview...'
out_file += '\n'

print out_file

with open(wdir + '/sf_metrics.txt', 'w') as f:
    f.write(out_file)

os.chdir(wdir)
if platform.system().lower() == 'linux':
    os.system("gnome-terminal -e 'sh runSMV.sh'")
if platform.system().lower() == 'windows':
    os.system('start cmd /K "runSMV.bat"')

hold = input('')



