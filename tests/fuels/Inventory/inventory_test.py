"""
Test file for Inventory class
"""

# ===========================
# relative path module import
# --------------------------- 
import os
import sys

mod_path = '/'.join(os.getcwd().split('/')[:-3])
sys.path.append(mod_path)
# ===========================

print 'Testing Inventory class,,,\n'

from standfire import fuels
import os

# get relative path to test directory
path = os.path.dirname(__file__)

# example csv tree file
fname = "FVS_TreeInit.csv"

# instantiate Inventory class
test = fuels.Inventory()

# set the variant
test.set_FVS_variant('iec')

# read the inventory csv file
test.read_inventory(os.path.join(path, fname))

# print all stands to console
print test.get_stands()

# filter by 'iec' stand
test.filter_by_stand(['01160805050024'])

# convert USDA plant codes to 2 letter codes
test.convert_sp_codes()

# format the .tre
test.format_fvs_tree_file()

# save .tre
test.save(path)

print 'Inventory class passed testing'

