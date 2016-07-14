"""
test file for FuelCalc class
"""

# ===========================
# relative path module import
# ---------------------------
import os
import sys

mod_path = '/'.join(os.getcwd().split('/')[:-3])
sys.path.append(mod_path)
# ===========================

print 'Testing FuelCalc class...\n'

from standfire import fuels
import os

print "Constructor (csv import)"
test = fuels.FuelCalc(os.path.join(os.getcwd(), "STANDFIRE_example_trees_2010.csv"))
test_df = test.trees
print test_df
test = None
print "\tstatus: passed"
print "\tstatus: failed"


print "Constructor (pd.DataFrame import )"
try:
    test = fuels.FuelCalc(test_df)
    print "\tstatus: passed"
except:
    print "\tstatus: failed"


print "get_species_list()"
try:
    rtn = test.get_species_list()
    print "\tstatus: passed"
    print rtn
except:
    print "\tstatus: failed"


print "set_crown_geometry(sp_geom_dict)"
try:
    test.set_crown_geometry({rtn[0] : "frustum"})
    print "\tstatus: passed"
except:
    print "\tstatus: failed"


print "frustum_vol(1, 10, 5)"
try:
    rtn = test.frustum_vol(1, 10, 5)
    print rtn
    print "\tstatus: passed"
except:
    print "\tstatus: failed"


print "calc_crown_volume()"
test.calc_crown_volume()

test.calc_bulk_density()
print test.trees

test.convert_units()
test.save_trees(os.getcwd() + '/test_trees.csv')


