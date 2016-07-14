"""
Test file for Fvsfuels class
"""

# ===========================
# relative path module import
# ---------------------------
import os
import sys

mod_path = '/'.join(os.getcwd().split('/')[:-3])
sys.path.append(mod_path)
# ===========================

print 'Testing Fvsfuels class...\n'

from standfire import fuels
import os

print "Constructor"
try:
    test = fuels.Fvsfuels('iec')
    print "\tstatus: passed"
except:
    print "\tstatus: failed"


print "\nset_keyword()"
try:
    test.set_keyword(os.path.join(os.getcwd(), 'test.key'))
    print "\tstatus: passed"
except:
    print "\tstatus: failed"


print "\nset_dir(wdir)"
try:
    test.set_dir(os.path.join(os.getcwd(), 'output'))
    print "\tstatus: passed"
except:
    print "\tstatus: failed"


print "\nset_num_cycles(val)"
try:
    test.set_num_cycles(10)
    print "\tstatus: passed"
except:
    print "\tstatus: failed"


print "\nset_time_int(val)"
try:
    test.set_time_int(10)
    print "\tstatus: passed"
except:
    print "\tstatus: failed"


print "\nset_inv_year(val)"
try:
    test.set_inv_year(2010)
    print "\tstatus: passed"
except:
    print "\tstatus: failed"

print "\nset_stop_point(val, val)"
try:
    test.set_stop_point()
    print "\tstatus: passed"
except:
    print "\tstatus: failed"


print "\nrun_fvs()"
try:
    test.run_fvs()
    print "\tstatus: passed"
except:
    print "\tstatus: failed"


print "\nget_simulation_years()"
try:
    rtn = test.get_simulation_years()
    print "\n\t\t", rtn
    print "\n\tstatus: passed"
except:
    print "status: failed"


print "\nget_trees(2010)"
try:
    rtn = test.get_trees(2010)
    print "\n\t\t", rtn.head(5)
    print "\n\tstatus: passed"
except:
    print "status: failed"


print "\nget_snags(2110)"
try:
    rtn = test.get_snags(2110)
    print "\n\t\t", rtn.head(5)
    print "\n\tstatus: passed"
except:
    print "status: failed"


print "\nget_standid()"
try:
    rtn = test.get_standid()
    print "\n\t\t", rtn
    print "\n\tstatus: passed"
except:
    print "status: failed"


print "\nsave_all()"
try:
    test.save_all()
    print "\n\tstatus: passed"
except:
    print "status: failed"


print "\nsave_trees_by_year(2010)"
try:
    test.save_trees_by_year(2010)
    print "\n\tstatus: passed"
except:
    print "status: failed"


print "\nsave_snags_by_year(2110)"
try:
    test.save_snags_by_year(2110)
    print "\n\tstatus: passed"
except:
    print "status: failed"





