#!python2
###############################################################################
#-----------#
# lidar.py  #
#-----------#

"""
This module implements all LiDAR processing tasks for STANDFIRE. This
includes creating a fishnet with 64x64m cells to divide the lidar points into
one acre plots, calculating FVS input variables, running FVS for each plot
and collating the results.

**Inputs:**

1. Lidar shapefile. Projected in WGS 1984 UTM (any zone). This shapefile
needs to have the following attributes:

==========  ======= =========== ===========
Field Name  Type    Units       Description
==========  ======= =========== ===========
X_UTM       Float   Meters      X coordinate
Y_UTM       Float   Meters      Y coordinate
Height_m    Float   Meters      Tree height
CBH_m       Float   Meters      Crown Base Height
DBH_cm      Float   Centimeters Diameter at Breast Height
Species     String  Text        Two letter FVS species code
==========  ======= =========== ===========

2. FVS keyword file.

**Outputs:**

1. Fishnet shapefile containing the plots (<example>_fishnet.shp)
2. Lidar shapefile containing the input and calculated attributes (<example>_out.shp)
3. Text file containing the calculated attributes (<example>_export.csv)
4. FVS (<example>.tre and <example>.key) files for each plot
5. Tree list for CAPSIS (<example>_trees.csv)
"""

# meta
__authors__ = "Team STANDFIRE"
__copyright__ = "Copyright 2017, STANDFIRE"
__credits__ = ["Greg Cohn", "Brett Davis", "Matt Jolly", "Russ Parsons", "Lucas Wells"]
__license__ = "GPL"
__maintainer__ = "Brett Davis"
__email__ = "bhdavis@fs.fed.us"
__status__ = "Development"
__version__ = "1.1.3a" # previous version 1.1.2a

# Import some modules
import timeit
import os
#import sys
import shutil
import csv

import pandas as pd
from osgeo import ogr
ogr.UseExceptions()

# Below error handling not necessary if compiled
# try:
    # import pandas as pd
# except: # can't sys.exit. find another way to handle errors
    # sys.exit("ERROR: cannot find Pandas modules. Please ensure Pandas \n"
    # "package for Python is installed")
# try:
    # from osgeo import ogr
    # from osgeo.gdal import __version__ as gdal_ver
# except:
    # sys.exit("ERROR: cannot find GDAL/OGR modules. Please ensure GDAL package\n"
    # " for Python is installed.")

# pd_ver_no = int((pd.__version__).replace(".",""))
# ##if pd_ver_no < 191: # 0.19.1 Development version November 2016
# ##    sys.exit("ERROR: Python bindings of Pandas 0.19.1 or later required")

# gdal_ver_no = int((gdal_ver).replace(".",""))
# # Might be able to get away with a older version, haven't tested.
# ##if gdal_ver_no < 213: # 2.1.3 Devel version January 2017
# ##    sys.exit("ERROR: Python bindings of GDAL 2.1.3 or later required")

class ConvertLidar(object):
    """
    The ConvertLidar class is used to generate a CAPSIS tree list of lidar
    tree attributes from a shapefile of lidar trees.

    :param lidar_shp: name and path of input lidar shapefile.
    :type lidar_shp: string
    :param fishnet_shp: name and path for output fishnet shapefile.
    :type fishnet_shp: string
    :param new_lidar: name and path for output lidar shapefile.
    :type new_lidar: string

    **Methods (execution order):**

    * verify_projection
    * verify_input_fields
    * calculate_extents
    * create_fishnet
    * copy_shapefile
    * cleanup_lidar_fields
    * fishnet_id
    * cleanup_lidar_features
    * add_attribute_fields
    * calculate_attribute_fields
    * number_trees
    * export_attributes_to_csv
    """

    def __init__(self, lidar_shp, fishnet_shp, new_lidar):
        """
        Constructor

        Defines local variables that will be used by various methods below
        """
        self.lidar_shp = lidar_shp
        self.fishnet_shp = fishnet_shp
        self.new_lidar = new_lidar
        self.set_path = os.path.dirname(lidar_shp)
        # TODO: initialize more commonly used stuff like the shapefile driver...

    def verify_projection(self):
        """
        Verifies that the input shapefile is projected and, if so, whether the
        projection is WGS 1984 UTM. Uses EPSG projection codes to verify.

        :return: projection fitness
        :rtype: boolean
        :return: fitness message
        :rtype: string
        :return: fitness code
        :rtype: integer

        *Fitness Codes:*

        * 0 - projection ok
        * 1 - unable to identify projection
        * 2 - unprojected
        """
        epsg_utm = range(32600, 32661) # EPSG codes for UTM North zones
        epsg_utm.extend(range(32700, 32761)) # append EPSG codes for UTM South zones
        in_data_source = ogr.Open(self.lidar_shp)
        in_layer = in_data_source.GetLayer()
        in_spatial_ref = in_layer.GetSpatialRef()
        if in_spatial_ref is None:
            prj_ok = False
            msg = ("Shapefile appears to be unprojected. Please project to WGS "
                   "1984 UTM and retry.")
            print msg
            code = 2
            in_data_source = None
            print "ERROR: Shapefile unprojected"
            return prj_ok, msg, code
        else:
            err = in_spatial_ref.AutoIdentifyEPSG()
            if err != 0:
                prj_ok = False
                msg = ("Unable to identify projection. (Unexpected results may "
                       "occur if the shapefile is in the wrong projection)")
                print msg
                code = 1
                in_data_source = None
            else:
                epsg = in_spatial_ref.GetAuthorityCode(None)
                print "EPSG Authority code: ", epsg
                epsg = int(epsg)
                in_data_source = None
                if epsg in epsg_utm:
                    prj_ok = True
                    msg = "Projection appears to be WGS 1984 UTM as required"
                    code = 0
                else:
                    prj_ok = False
                    msg = ("This shapefile doesn\'t appear to be in a WGS 1984 "
                           "UTM projection as required. (Unexpected results "
                           "may occur if the shapefile is in the wrong "
                           "projection)")
                    print msg
                    code = 1
            return prj_ok, msg, code

    def verify_input_fields(self):
        """
        Verifies that the required input fields are in the input shapefile.

        **Required fields:**

        ==========  ======= =========== ===========
        Field Name  Type    Units       Description
        ==========  ======= =========== ===========
        X_UTM       Float   Meters      X coordinate
        Y_UTM       Float   Meters      Y coordinate
        Height_m    Float   Meters      Tree height
        CBH_m       Float   Meters      Crown Base Height
        DBH_cm      Float   Centimeters Diameter at Breast Height
        Species     String  Text        Two letter FVS species code
        ==========  ======= =========== ===========

        :return: input field fitness
        :rtype: boolean
        :return: fitness message
        :rtype: string

        .. note:: See FVS variant users guides for definitions of species codes:
                  https://www.fs.fed.us/fvs/documents/guides.shtml
        """
        required_fields = ["X_UTM", "Y_UTM", "Height_m", "CBH_m", "DBH_cm", "Species"]
        missing_fields = []
        in_data_source = ogr.Open(self.lidar_shp)
        in_layer = in_data_source.GetLayer()
        in_layer_defn = in_layer.GetLayerDefn()
        in_fields = [in_layer_defn.GetFieldDefn(i).GetName()
                     for i in range(in_layer_defn.GetFieldCount())]
        in_data_source = None
        for req in required_fields:
            if req not in in_fields:
                missing_fields.append(req)
        if missing_fields:
            fields_ok = False
            msg = ("The following fields seem to be missing from the input lidar"
                   " shapefile. Please ensure these fields exist and are named "
                   "as shown above. Missing fields: \n" + ", ".join(missing_fields))
            print msg
        else:
            fields_ok = True
            msg = "Required input fields appear to be present."
            print msg
        return fields_ok, msg

    def calculate_extents(self):
        """
        Calculates the minimum and maximum x and y extents of the input
        shapefile.

        :return: x min, x max, y min, y max
        :rtype: list of four floats
        """
        in_data_source = ogr.Open(self.lidar_shp, 0)
        in_layer = in_data_source.GetLayer()
        extents = in_layer.GetExtent()
        extents = [round(x, 3) for x in extents]
        in_data_source = None
        return extents

    def create_fishnet(self, extents):
        """
        Creates a fishnet polygon shapefile to be used to assign plot
        numbers to the lidar points. Fishnet cells are 64x64m (~1 acre).
        Feature ID numbers are used to number plots.

        :param extents: x min, x max, y min, y max of the input shapefile
        :type extents: list of four floats

        :return: fishnet creation success
        :rtype: boolean
        :return: fishnet creation message
        :rtype: string
        """
        fish_ok = True
        msg = "\nMethod: create_fishnet\n"
        drv = ogr.GetDriverByName("ESRI Shapefile")
        # dimensions
        x_min, x_max, y_min, y_max = extents
        grid_width = 64
        grid_height = 64
        cols = int((x_max-x_min)/grid_width)
        rows = int((y_max-y_min)/grid_height)
        # start grid cell envelope
        ring_x_left_origin = x_min
        ring_x_right_origin = x_min + grid_width
        ring_y_top_origin = y_min + grid_height
        ring_y_bottom_origin = y_min
        # get projection from in shapefile
        proj_data_source = ogr.Open(self.lidar_shp, 0)
        proj_layer = proj_data_source.GetLayer()
        spatial_ref = proj_layer.GetSpatialRef()
        proj_data_source = None
        # create output file
        if os.path.exists(self.fishnet_shp):
            drv.DeleteDataSource(self.fishnet_shp)
        out_data_source = drv.CreateDataSource(self.fishnet_shp)
        # add projection below
        out_layer = out_data_source.CreateLayer(self.fishnet_shp, srs=spatial_ref,
                                                geom_type=ogr.wkbPolygon)
        out_layer_defn = out_layer.GetLayerDefn()
        # create grid cells
        countcols = 0
        while countcols < cols:
            countcols += 1
            # reset envelope for rows
            ring_y_top = ring_y_top_origin
            ring_y_bottom = ring_y_bottom_origin
            countrows = 0
            while countrows < rows:
                countrows += 1
                ring = ogr.Geometry(ogr.wkbLinearRing)
                ring.AddPoint(ring_x_left_origin, ring_y_top)
                ring.AddPoint(ring_x_right_origin, ring_y_top)
                ring.AddPoint(ring_x_right_origin, ring_y_bottom)
                ring.AddPoint(ring_x_left_origin, ring_y_bottom)
                ring.AddPoint(ring_x_left_origin, ring_y_top)
                poly = ogr.Geometry(ogr.wkbPolygon)
                poly.AddGeometry(ring)
                # add new geom to layer
                out_feature = ogr.Feature(out_layer_defn)
                out_feature.SetGeometry(poly)
                out_layer.CreateFeature(out_feature)
                out_feature = None
                # new envelope for next poly
                ring_y_top = ring_y_top + grid_height
                ring_y_bottom = ring_y_bottom + grid_height
            # new envelope for next poly
            ring_x_left_origin = ring_x_left_origin + grid_width
            ring_x_right_origin = ring_x_right_origin + grid_width
        # Save and close DataSources
        out_data_source = None
        # Check for features in fishnet shapefile
        if os.path.exists(self.fishnet_shp):
            out_data_source = drv.Open(self.fishnet_shp, 0)
            out_layer = out_data_source.GetLayer()
            feat_count = out_layer.GetFeatureCount()
            out_data_source = None
            if feat_count > 0:
                fish_ok = True
                msg += "Fishnet shapefile creation appears to have been successful\n"
            else:
                fish_ok = False
                msg += "ERROR: Fishnet shapefile has no features\n"
        else:
            fish_ok = False
            msg += "ERROR: Creation of the fishnet shapefile failed\n"
        return fish_ok, msg

    def copy_shapefile(self):
        """
        Makes an exact copy of the input lidar shapefile for use in creating
        the output lidar shapefile.

        :return: shapefile copy success
        :rtype: boolean
        :return: shapefile copy message
        :rtype: string
        """
        copy_ok = True
        msg = "\nMethod: copy_shapefile\n"
        # set shapefile names
        in_shp = self.lidar_shp
        out_shp = self.new_lidar

        # start timer
        copy_start = timeit.default_timer()

        # get driver
        drv = ogr.GetDriverByName("ESRI Shapefile")

        # open input shapefile and extract information
        in_ds = drv.Open(in_shp, 0)
        in_lyr = in_ds.GetLayer()
        lyr_def = in_lyr.GetLayerDefn()
        lyr_geom = in_lyr.GetGeomType()
        spatial_ref = in_lyr.GetSpatialRef()

        # delete output shapefile if it exists
        if os.path.exists(out_shp):
            del_ok = drv.DeleteDataSource(out_shp) #returns flag
            if del_ok == 0:
                print "Deleted old LiDAR out shapefile"

        # create out shapefile
        out_ds = drv.CreateDataSource(out_shp)
        out_lyr = out_ds.CreateLayer(out_shp.split(".")[0], spatial_ref, lyr_geom)

        # add fields from input shapefile to output shapefile
        cfld_ok = 0
        field_cnt = lyr_def.GetFieldCount()
        for i in range(field_cnt):
            cfld_ok += out_lyr.CreateField(lyr_def.GetFieldDefn(i)) #returns flag
            if cfld_ok != 0:
                msg += ("ERROR: Problems creating fields in out LiDAR shapefile. OGR "
                        "CreateField error = " + str(cfld_ok) + "\n")
                copy_ok = False
##                in_ds = None
##                out_ds = None
        out_lyr.ResetReading()

        # create output shapefile features
        for in_feat in in_lyr:
            out_feat = ogr.Feature(lyr_def)
            for i in range(field_cnt):
                out_feat.SetField(lyr_def.GetFieldDefn(i).GetNameRef(), in_feat.GetField(i))
            cftr_ok = out_lyr.CreateFeature(in_feat)
            if cftr_ok != 0:
                msg += ("ERROR: Problem creating features in out LiDAR shapefile. OGR "
                        "CreateFeatrure error = " + str(cftr_ok) + "\n")
                copy_ok = False
##                in_ds = None
##                out_ds = None
            out_feat = None # dereference feature
            in_feat = None

        # ogr cleanup and save shapefile
        in_lyr = None
        in_ds = None
        out_lyr = None
        out_ds = None

        if copy_ok:
            # end timer
            copy_time = timeit.default_timer() - copy_start
            print "\nCreate output shapefile took ", copy_time, " to run."

            # return messages
            msg += "Created initial LiDAR out shapefile\n"
        return copy_ok, msg

    def cleanup_lidar_fields(self):
        """
        Deletes extraneous fields from output shapefile.

        :return: cleanup success
        :rtype: boolean
        :return: cleanup message
        :rtype: string
        """
        cln_fld_ok = True
        msg = "\nMethod: cleanup_lidar_fields\n"
        # define input shapefile
        pt_shp = self.new_lidar

        # required input fields
        pt_req_in_flds = ["FID", "Shape", "X_UTM", "Y_UTM", "Height_m", "CBH_m",
                          "DBH_cm", "Species"]

        # get driver
        drv = ogr.GetDriverByName("ESRI Shapefile")

        # open lidar point shapefile
        pt_ds = drv.Open(pt_shp, 1)
        pt_lyr = pt_ds.GetLayer()

        # get lidar point field names
        pt_lyr_def = pt_lyr.GetLayerDefn()
        pt_nmbr_flds = range(pt_lyr_def.GetFieldCount())
        pt_fld_nms = [pt_lyr_def.GetFieldDefn(i).GetName() for i in pt_nmbr_flds]

        # delete extraneous lidar point fields
        for in_fld in pt_fld_nms:
            if in_fld not in pt_req_in_flds:
                del_fld_idx = pt_lyr_def.GetFieldIndex(in_fld)
                dflds_ok = pt_lyr.DeleteField(del_fld_idx) # returns flag
                if dflds_ok != 0:
                    cln_fld_ok = False
                    msg += ("ERROR: Problem deleting extraneous fields from output "
                            "shapefile. OGR DeleteField error: " + str(dflds_ok) + "\n")
                    return cln_fld_ok, msg

        # save and close data sources, layers and layer definitions
        pt_lyr_def = None
        pt_lyr = None
        pt_ds = None
        if cln_fld_ok:
            msg += "Deleted extraneous fileds from output shapefile\n"
        return cln_fld_ok, msg

    def fishnet_id(self):
        """
        Assigns a fishnet-based plot ID number to each tree in the output
        shapefile.

        :return: plot number assignment success
        :rtype: boolean
        :return: plot number assignment message
        :rtype: string
        """
        fish_id_ok = True
        msg = "\nMethod: fishnet_id\n"
        # define input shapefiles
        pt_shp = self.new_lidar
        poly_shp = self.fishnet_shp

        # start timer
        fish_id_start = timeit.default_timer()

        # get driver
        drv = ogr.GetDriverByName("ESRI Shapefile")

        # open lidar point shapefile
        pt_ds = drv.Open(pt_shp, 1)
        pt_lyr = pt_ds.GetLayer()

        # add fishnet ID field to lidar out shapefile
        fish_field = ogr.FieldDefn("Plot_ID", ogr.OFTInteger)
        fish_field_ok = pt_lyr.CreateField(fish_field)
        if fish_field_ok != 0:
            fish_id_ok = False
            msg += "ERROR: Problem adding fishnet ID field to lidar out shapefile\n"

        # set all initial fishnet ID values to -9999
        feat_init_ok = 0
        for feat in pt_lyr:
            feat.SetField("Plot_ID", -9999)
            feat_init_ok += pt_lyr.SetFeature(feat)
        pt_lyr.ResetReading() # prob not necessary now...
        if feat_init_ok != 0:
            fish_id_ok = False
            msg += "ERROR: Problem initializing fishnet ID field in lidar out shapefile\n"
        # save and refresh data sources and layers
        pt_lyr = None
        pt_ds = None
        pt_ds = drv.Open(pt_shp, 1)
        pt_lyr = pt_ds.GetLayer()

        # open fishnet shapefile
        poly_ds = drv.Open(poly_shp, 0)
        poly_lyr = poly_ds.GetLayer()

        # get field index for fishnet FID field
        fish_id = poly_lyr.GetLayerDefn().GetFieldIndex("FID")
        fish_id_list = []

        feat_ok = 0
        for feat in pt_lyr:
            # get XY coordinates for points in the lidar shapefile
            geom = feat.GetGeometryRef()
            x_coordinate, y_coordinate = geom.GetX(), geom.GetY()
            # create single point to filter fishnet
            point = ogr.Geometry(ogr.wkbPoint)
            point.AddPoint(x_coordinate, y_coordinate)
            # filter fishnet features by point location
            poly_lyr.SetSpatialFilter(point)
            # extract fishnet ID number and add to lidar point attributes
            # loop doesn't execute when the point falls outside fishnet
            for poly_feat in poly_lyr:
                feature_id = int(poly_feat.GetFieldAsInteger(fish_id))
                feat.SetField("Plot_ID", feature_id)
                feat_ok += pt_lyr.SetFeature(feat)
                fish_id_list.append(feature_id) #testing
            poly_lyr.SetSpatialFilter(None)
        pt_lyr.ResetReading() # prob not necessary now...
        if feat_ok != 0:
            fish_id_ok = False
            msg += "ERROR: Problem adding fishnet ID # to lidar out shapefile\n"
        # save and close layers and data sources
        pt_lyr = None
        pt_ds = None
        poly_lyr = None
        poly_ds = None

        if fish_id_ok:
            msg += "Plot ID numbers successfully added to lidar out shapefile\n"
            # print some statistics
            print "\nLength of fish ID list is: " + str(len(fish_id_list))
            print "Unique values in fish ID list: "
            print sorted(set(fish_id_list))

            fish_id_time = timeit.default_timer() - fish_id_start
            print "Fishnet ID function took ", fish_id_time, " to run."
        return fish_id_ok, msg

    def cleanup_lidar_features(self):
        """
        Deletes features outside the fishnet area.

        :return: cleanup success
        :rtype: boolean
        :return: cleanup message
        :rtype: string
        """
        cln_ftr_ok = True
        msg = "\nMethod: cleanup_lidar_features\n"
        # get path and layer name for output shapefile
        pt_shp = self.new_lidar
        lyr_name = os.path.basename(pt_shp)[:-4]

        # get driver
        drv = ogr.GetDriverByName("ESRI Shapefile")

        # open lidar point shapefile
        pt_ds = drv.Open(pt_shp, 1)
        pt_lyr = pt_ds.GetLayer()

        # get number of input features
        pt_nmbr_in_ftrs = pt_lyr.GetFeatureCount()

        # delete features outside the fishnet
        del_feat_ok = 0
        pt_lyr.SetAttributeFilter("Plot_ID = '-9999'")
        for feat in pt_lyr:
            del_feats = feat.GetFID()
            del_feat_ok += pt_lyr.DeleteFeature(del_feats)
            feat = None
        if del_feat_ok != 0:
            cln_ftr_ok = False
            msg += "ERROR: Problem deleting features that fell outside the fishnet boundary\n"
        # shapefile needs to be "repacked" before the features will actually be deleted
        # close and refresh data source
        pt_lyr = None
        pt_ds = None
        pt_ds = drv.Open(pt_shp, 1)
        repack_return = pt_ds.ExecuteSQL("repack " + lyr_name)
        # ExecuteSQL returns "Null" if no error occured
        if repack_return:
            cln_ftr_ok = False
            msg += "ERROR: Problem 'repacking' features in the lidar out shapefile\n"

        if cln_ftr_ok:
            msg += "Features outside the fishnet boundary were successfully deleted\n"
            # calculate the numer of features deleted
            pt_lyr = pt_ds.GetLayer()
            pt_nmbr_out_ftrs = pt_lyr.GetFeatureCount()
            pt_nmbr_del_ftrs = pt_nmbr_in_ftrs - pt_nmbr_out_ftrs
            # print deleted feature count
            print pt_nmbr_del_ftrs, " features fell outside the fishnet and were deleted."
        else: msg += ("ERROR: Problem rebuilding output shapefile after deleting features "
                      "outside the fishnet boundary\n")

        # save and close shapefile
        pt_lyr = None
        pt_ds = None

        return cln_ftr_ok, msg

    def add_attribute_fields(self):
        """
        Adds and defines new attribute fields to the output shapefile.

        **Fields added:**

        ==========  ======= ===========
        Field Name  Type    Description
        ==========  ======= ===========
        POINT_X     Float   X coordinate
        POINT_Y     Float   Y coordinate
        CR_code     Integer FVS crown ratio code
        DBH_in_x10  Integer Diameter at Breast Height in inches x 10
        Height_ft   Integer Tree height in feet
        Tree_ID     Integer Unique tree ID number within each plot
        ==========  ======= ===========

        :return: add attribute fields success
        :rtype: boolean
        :return: add attribute fields message
        :rtype: string
        """
        add_flds_ok = True
        msg = "\nMethod: add_attribute_fields\n"
        new_fields = {
            "POINT_X": ogr.OFTReal,
            "POINT_Y": ogr.OFTReal,
            "CR_code": ogr.OFTInteger,
            "DBH_in_x10": ogr.OFTInteger,
            "Height_ft": ogr.OFTInteger,
            "Tree_ID": ogr.OFTInteger
        }
        in_data_source = ogr.Open(self.new_lidar, update=True)
        in_layer = in_data_source.GetLayer()
        in_layer_defn = in_layer.GetLayerDefn()
        field_names = [in_layer_defn.GetFieldDefn(i).GetName()
                       for i in range(in_layer_defn.GetFieldCount())]
        crt_flds_ok = 0
        for key in new_fields:
            if key not in field_names:
                new = ogr.FieldDefn(key, new_fields[key])
                crt_flds_ok += in_layer.CreateField(new)
            else:
                print key + " field already exists"
        if crt_flds_ok != 0:
            add_flds_ok = False
            msg += "ERROR: Problem adding new fields to lidar out shapefile\n"
        else:
            msg += "New fields successfully added to lidar out shapefile\n"
        in_data_source = None
        return add_flds_ok, msg

    def calculate_attribute_fields(self):
        """
        Calculates values for the attribute fields added in the
        "add_attribute_fields" method.

        **Formula descriptions:**

            * Height- converts from meters to feet and rounds to integer
            * DBH - converts from centimeters to inches*10 and rounds to integer
            * CR_code- calculates crown ratio while accounting for anomolous data
              where Height is <= CBH. Then classifies the crown ratio into FVS
              categories.

              * crown ratio = (height - crown base height)/height).
              * FVS crown ratio codes: 1: 0-10%; 2: 11-20%;...; 9: 81-100%

        :return: calculate attribute fields success
        :rtype: boolean
        :return: calculate attribute fields message
        :rtype: string
        """
        calc_flds_ok = True
        msg = "\nMethod: calculate_attribute_fields\n"
        in_data_source = ogr.Open(self.new_lidar, 1)
        in_layer = in_data_source.GetLayer()
        set_ftr_ok = 0
        for ftr in in_layer:
            # set X-Y coordinates
            ftr.SetField("POINT_X", ftr.GetField("X_UTM"))
            ftr.SetField("POINT_Y", ftr.GetField("Y_UTM"))
            # extract input height, diameter at breast height and crown base height
            in_ht = ftr.GetField("Height_m")
            in_dbh = ftr.GetField("DBH_cm")
            in_cbh = ftr.GetField("CBH_m")
            # calculate and set output height and diameter at breast height
            out_ht = int((in_ht*3.281)+0.5)
            out_dbh = int((in_dbh*3.937)+0.5)
            ftr.SetField("Height_ft", out_ht)
            ftr.SetField("DBH_in_x10", out_dbh)
            # calculate and set FVS crown ratio code
            if in_ht <= in_cbh:
                crown_ratio = 0.0001
            else:
                crown_ratio = ((in_ht - in_cbh) / in_ht)
            if crown_ratio >= 0.8:
                ftr.SetField("CR_code", 9)
            else:
                ftr.SetField("CR_code", int((crown_ratio - 0.000001) * 10) + 1)
            # save feature attribute changes
            set_ftr_ok += in_layer.SetFeature(ftr)
        if set_ftr_ok != 0:
            calc_flds_ok = False
            msg += "ERROR: Problem calculating fields in output shapefile\n"
        else:
            msg += "Calculated attribute values for new fields in output shapefile\n"
        in_data_source = None
        return calc_flds_ok, msg

    def number_trees(self):
        """
        Numbers trees within each 64x64m (~1 acre) plot. The combination of plot ID
        and tree Id constitutes a unique identifier for each tree in the simulation.

        :return: numbering trees success
        :rtype: boolean
        :return: numbering trees message
        :rtype: string
        """
        nmbr_trees_ok = True
        msg = "\nMethod: number_trees\n"
        in_data_source = ogr.Open(self.new_lidar, 1)
        in_layer = in_data_source.GetLayer()
        field_vals = []
        for ftr in in_layer:
            field_vals.append(ftr.GetFieldAsInteger("Plot_ID"))
        plots = list(set(field_vals))
        in_layer.ResetReading()
        tree_counter = 1
        set_ftr_ok = 0
        for plot in plots:
            plot_filter = "Plot_ID = " + str(plot)
            set_ftr_ok += in_layer.SetAttributeFilter(plot_filter)
            for ftr in in_layer:
                ftr.SetField("Tree_ID", tree_counter)
                set_ftr_ok += in_layer.SetFeature(ftr)
                tree_counter += 1
            print str(tree_counter-1) + " trees numbered in plot " + str(plot)
            tree_counter = 1
        if set_ftr_ok != 0:
            nmbr_trees_ok = False
            msg += "ERROR: Problem numbering trees within each plot\n"
        else:
            msg += "Numbered all trees within each plot\n"
        in_data_source = None
        return nmbr_trees_ok, msg

    def export_attributes_to_csv(self, lidar_csv):
        """
        Exports select attributes in the new lidar shapefile to a text (.csv)
        file.

        **Exported attributes:**

        ==========  ===========
        Name        Description
        ==========  ===========
        FID         GIS record ID number
        POINT_X     X coordinate
        POINT_Y     Y coordinate
        Species     Two letter FVS species code
        CR_code     FVS crown ratio code
        DBH_in_x10  Diameter at Breast Height in inches x 10
        Height_ft   Tree height in feet
        Plot_ID     Unique plot ID number
        Tree_ID     Unique tree ID number within each plot
        ==========  ===========

        :param lidar_csv: name and path for output text file
        :type lidar_csv: string

        :return: export success
        :rtype: boolean
        :return: export message
        :rtype: string
        """
        csv_ok = True
        msg = "\nMethod: export_attributes_to_csv\n"
        csv_filename = lidar_csv
        #Export select fields to csv file
        out_fields = ["FID", "POINT_X", "POINT_Y", "Species", "CR_code",
                      "DBH_in_x10", "Height_ft", "Plot_ID", "Tree_ID"]
        with open(lidar_csv, "w") as lidar_csv_file:
            csvwriter = csv.writer(lidar_csv_file, delimiter=",", lineterminator="\n")
            csvwriter.writerow(out_fields)
            out_fields.remove("FID")
            in_data_source = ogr.Open(self.new_lidar, 0)
            in_layer = in_data_source.GetLayer()
            for ftr in in_layer:
                attributes = []
                attributes.append(ftr.GetFID())
                for field in out_fields:
                    attributes.append(ftr.GetField(field))
                csvwriter.writerow(attributes)
            lidar_csv_file.close()
            in_data_source = None
        if os.path.exists(csv_filename):
            msg += "Lidar out shapefile fields exported to csv: " + csv_filename + "\n"
        else:
            csv_ok = False
            msg += "ERROR: Export of lidar out shapefile fields failed.\n"
        return csv_ok, msg
###End class "ConvertLidar"###

class FVSFromLidar(object):
    """
    Creates FVS tree lists (<example>.tre) and keyword files (<example>.key),
    runs FVS for each 64x64m (~1 acre) lidar plot and creates a tree list for CAPSIS.

    :param fuel: FVS fuels object. Specific to a single FVS variant.
    :type fuel: object
    :param lidar_csv: path and file name of input text file (generated by the
                    ConvertLidar class above (export_attributes_to_csv method)
    :type lidar_csv: string
    :param keyword_file: path and file name of the "master" FVS keyword file.
    :type keyword_file: string

    **Methods:**

    * run_fvs_lidar
    * create_capsis_csv
    """

    def __init__(self, fuel, lidar_csv, keyword_file):
        """
        Constructor

        Defines local variables and objects that will be used by methods below
        """
        self.fuel = fuel # fuels object
        self.lidar_csv = lidar_csv
        self.keyword_file = keyword_file
        self.set_path = os.path.dirname(lidar_csv)

    def run_fvs_lidar(self):
        """
        Creates FVS input files (.key and .tre) from lidar tree list generated
        in the export_attributes_to_csv method in the ConvertLidar class. Uses
        these files to run FVS for each lidar subset/plot.

        :returns: filename and path for a collated (from subsets/plots)
            FVS results file. Only the filename is generated in this method.
            The file itself will be collated in the create_capsis_csv method.
        :rtype: string
        """
        fvs_start = timeit.default_timer()
        work_dir = os.path.dirname(self.keyword_file)
        # Read lidar data into pandas data frame
        df_lidar_fvs = pd.read_csv(self.lidar_csv)
        plots = sorted(df_lidar_fvs.Plot_ID.unique())
        # Empty or unchanging .tre variables
        tree_cnt = "".ljust(6) # Tree_count
        tree_hist = "0" # Tree_history
        dbh_inc = "".ljust(3) # DBH_increment
        top_kill_ht = "".ljust(3) # Height_to_top-kill
        ht_inc = "".ljust(4) # Height_increment
        the_rest = "".ljust(27) # All variables after Crown_ratio_code
        # Get column index by name. Safer than by index order if the order in
        #   the file changes...
        plot_id_col = df_lidar_fvs.columns.get_loc("Plot_ID")
        tree_id_col = df_lidar_fvs.columns.get_loc("Tree_ID")
        species_col = df_lidar_fvs.columns.get_loc("Species")
        dbh_col = df_lidar_fvs.columns.get_loc("DBH_in_x10")
        ht_col = df_lidar_fvs.columns.get_loc("Height_ft")
        cr_code_col = df_lidar_fvs.columns.get_loc("CR_code")
        # Loop through each subset/plot to create .tre files and run FVS
        for plot in plots:
            df_lidar_fvs_subplot = df_lidar_fvs[df_lidar_fvs.Plot_ID == plot]
            num_records = len(df_lidar_fvs_subplot.index)
            tre_lines = [] # Initialize list variable to store FVS tree data
            tre_file_name = work_dir+"/subset"+str(plot)+".tre" # FVS subset tree file
            tre_file = open(tre_file_name, "w")
            # Loop through records in subset's data frame and extract variables
            for rec in range(0, num_records):
                plot_id = str(df_lidar_fvs_subplot.iat[rec, plot_id_col]).ljust(4) # Plot_ID
                tree_id = str(df_lidar_fvs_subplot.iat[rec, tree_id_col]).ljust(3) # Tree_ID
                species = str(df_lidar_fvs_subplot.iat[rec, species_col]).ljust(3) # Species
                dbh = str(df_lidar_fvs_subplot.iat[rec, dbh_col]).ljust(4) # DBH
                height = str(df_lidar_fvs_subplot.iat[rec, ht_col]).ljust(3) # Live_height
                cr_code = str(df_lidar_fvs_subplot.iat[rec, cr_code_col]) # Crown_ratio_code
                # Merge variables and spaces into one long string
                tre_line = (plot_id + tree_id + tree_cnt + tree_hist + species + dbh + \
						    dbh_inc + height + top_kill_ht + ht_inc + cr_code + the_rest + "\n")
                tre_lines.append(tre_line) # Add to list variable
            tre_file.writelines(tre_lines) # Write all lines to subset's .tre file
            tre_file.close() # .tre file for each subset
            # .key file for each subset (identical to master key)
            shutil.copy(self.keyword_file, tre_file_name[0:-4]+".key")
            key_file = tre_file_name[0:-4]+".key"
            self.fuel.set_keyword(key_file)
            # Start FVS simulation
            self.fuel.run_fvs() # FVS for each subset
            # Base name for output .csv file. e.g. STANDFIRE_ex. From .key file
            svs_base = self.fuel.get_standid()
            # Writes subset .csv file containg tree variables for CAPSIS
            self.fuel.save_trees_by_year(2010)
            fvs_csv = self.fuel.wdir+svs_base+"_2010_trees.csv"
            subplot_out = fvs_csv[0:-4]+"_subset"+str(plot)+".csv"
            if os.path.exists(subplot_out):
                os.remove(subplot_out)
            os.rename(fvs_csv, subplot_out) # Renames .csv file with the subset name.
        fvs_elapsed = timeit.default_timer() - fvs_start
        print "FVS runs took: "+str(round(fvs_elapsed, 3))+" seconds."
        return fvs_csv

    def create_capsis_csv(self, xy_orig, fvs_csv):
        """
        Creates a capsis input file from FVS subset/plot output files generated
        by the run_fvs_lidar method. Calculates adjusted xy coordinates for
        each tree (i.e. each plot's coordinates need to be adjusted depending on
        its position amoungst all plots).

        :param xy_orig: xy origin of the original input lidar shapefile in UTM
            coordinates.
        :type xy_orig: list of two floats
        :param fvs_csv: filename and path for the collated (from subsets/plots)
            FVS results file.
        :type fvs_csv: string

        Each of the six 64x64m plots illustrated below were modeled seperately
        in FVS. As a consequence, they each have coordinates with a 0,0 origin
        in their lower left corners. This method adjusts the coordinates of
        plots 1-5 so their origin is now the lower left corner of plot 0 \
        (original input lidar shapefile's origin location).

        Example::

                _____________________________
            128|         |         |         |
               |    3    |    4    |    5    |
               |         |         |         |
               |_________|_________|_________|
             64|         |         |         |
               |    0    |    1    |    2    |
               |         |         |         |
               |_________|_________|_________|
               0        64        128       192

        """
        # Create merged _trees.csv with adjusted coordinates for input into CAPSIS
        col_names = ["xloc", "yloc", "species", "dbh", "ht", "crd", "cratio",
                     "crownwt0", "crownwt1", "crownwt2", "crownwt3"]
        #Create data frame from lidar data to obtain original UTM xy coordinates
        df_lidar = pd.read_csv(self.lidar_csv)
        df_fvs_csv = pd.DataFrame(columns=col_names) # Initialize empty data frame
        plots = sorted(df_lidar.Plot_ID.unique())
        for plot in plots:
            subplot_file = fvs_csv[:-4]+"_subset"+str(plot)+".csv"
            df_trees = pd.read_csv(subplot_file) # Data frame for subset
            df_lidar_subset = df_lidar[df_lidar.Plot_ID == plot]
            # Need index numbers for both data frames to match up
            df_lidar_subset.reset_index(drop=True, inplace=True)
            # Subtract original UTM origin from original tree location to get xy
            # coordinate system whose origin is 0,0 and whose units are now feet
            df_trees.xloc = ((df_lidar_subset.POINT_X - xy_orig[0])*3.28)
            df_trees.yloc = ((df_lidar_subset.POINT_Y - xy_orig[1])*3.28)
            df_trees.xloc = df_trees.xloc.apply(lambda x: round(x, 3))
            df_trees.yloc = df_trees.yloc.apply(lambda x: round(x, 3))
            # Append subset data frame to set data frame
            df_fvs_csv = df_fvs_csv.append(df_trees, ignore_index=True)
            # Convert set data frame to set *_trees.csv
        df_fvs_csv.to_csv(fvs_csv, index=False, quoting=csv.QUOTE_NONNUMERIC)
