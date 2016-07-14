"""
Test file for SvsStamper class
"""

# ===========================
# relative path module import
# ---------------------------
import os
import sys

mod_path = '/'.join(os.getcwd().split('/')[:-3])
sys.path.append(mod_path)
# ===========================

from standfire import placer
import os

print "Constructor"

test = placer.SvsStamper([0,0,160,90], 86, os.path.join(os.getcwd(), "test_trees.csv"))
test.place_trees()
test.trim_trees()
print test.trees

