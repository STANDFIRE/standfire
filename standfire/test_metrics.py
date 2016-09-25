
import metrics

wdir = '/home/lawells/Projects/JUNK/output/'

fuel_1 = 'yup_FUEL2_Herb_Leave_Dead_vegout.csv'
fuel_2 = 'yup_FUEL8_Herb_Leave_Dead_vegout.csv'

ros = metrics.ROS(wdir, fuel_1, fuel_2, 64)

print ros.get_first_burn_time(ros.data_1)

massloss = metrics.MassLoss(wdir)
massloss.get_tree_files()
massloss.read_tree_mass()
print massloss.get_total_mass_loss()

wind = metrics.WindProfile(wdir, 'yup_01.sf', 20, 30, 1)
print wind.get_wind_profile()


heat = metrics.HeatTransfer(wdir)
heat.get_tree_files()
heat.read_tree_conv()
print heat.conv_sum
