#!/bin/bash
# build_pyfvs_linus.sh

# =============================================================================
# edit FVS_ROOT variable to reflect path to open-fvs directory
# -----------------------------------------------------------------------------
FVS_ROOT=$HOME/Projects/standfire/open-fvs
# =============================================================================

# ./standfire/apps/STANDFIRE_vx.x/pyfvs/linux
THIS_FILE=$(readlink -f "$0")
THIS_DIR=$(dirname "$THIS_FILE")

# FVS variant list
VARIANTS="ak bmc cac cic crc cs ecc emc iec ktc ls ncc ne oc op pnc sn soc ttc
          utc wcc wsc"

# python branch directory
PYFVS_DIR=$FVS_ROOT/branches/PyFVS/bin

cd $PYFVS_DIR

# check if build directory exists and delete if true
CHECK_DIR=$PYFVS_DIR/build
if [ -d "$CHECK_DIR" ]; then
    rm -rf "$CHECK_DIR"
fi

# make build directory and step into
mkdir build
cd ./build

# generate makefiles for all variants with pymod on
cmake .. -G"Unix Makefiles" -DWITH_PYMOD=ON -DRELEASE=ON

# build each variant: change -j flag for more or less CPUs
for i in $VARIANTS; do
    echo building "$i"
    make -j4 "$i"
done

# step into release directory
cd Open-FVS/bin

# copy necessary files to standfire pyfvs directory
echo "\ncopying files to standfire pyfvs directory\n"
cp -rf mkdbsTypeDefs $THIS_DIR
for i in $VARIANTS; do
    cp -rf "FVS$i" $THIS_DIR
    cp -rf "libFVS$i.so" $THIS_DIR
    cp -rf "pyfvs$i.so" $THIS_DIR
done

echo "\nComplete: new pyfvs build in $THIS_DIR\n"

# end of build_pyfvs_linux.sh
