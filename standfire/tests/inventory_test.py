
from standfire import fuels
import os

def test():
    path = os.path.dirname(__file__)
    fname = "FVS_TreeInit.csv"
    test = fuels.Inventory()
    test.set_FVS_variant('iec')
    test.read_inventory(os.path.join(path, fname))
    print test.get_stands()
    test.filter_by_stand(['01160805050024'])
    print test.get_stands()
    test.convert_sp_codes()
    test.format_fvs_tree_file()
    test.save(path + '/Inventory/')
