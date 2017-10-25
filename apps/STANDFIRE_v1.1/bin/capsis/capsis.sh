#!/bin/bash

cd `dirname "$0"`
EXTDIR=`cd ./ext; pwd`

# Check wether the splashscreen should be shown (yes, except in script mode) fc-21.5.2013
splashoption="-splash:./etc/splash.png"
string0="$*"
string1=${string0%script*}

if [ "$string0" != "$string1" ]; then
  echo Script: splashscreen was desactivated
  splashoption=""
fi

# Check architecture, set default memory and libraries path
architecture=`uname -m`
if [ "$architecture" != "x86_64" ] && [ "$architecture" != "ia64" ]; then
    
  msgarchi='32 bits architecture'
  mem="4096"   # 32 bits default value / see setmem.sh to change it
  export LD_LIBRARY_PATH=$EXTDIR/linux:$LD_LIBRARY_PATH

else

  msgarchi='64 bits architecture'
  mem="4096"   # 64 bits default value / see setmem.sh to change it
  export LD_LIBRARY_PATH=$EXTDIR/linux64:$LD_LIBRARY_PATH

fi

# MacOSX
export DYLD_LIBRARY_PATH=$EXTDIR/macosx:${DYLD_LIBRARY_PATH}

# Chek if 'memory' file exists, read it (1 single line, e.g. 4096)
if [ -f memory ]  # Check if the file named 'memory' exists
then
  read line < "memory"
  mem=$line
fi

# Setting max memory, adding 'm' for 'Mega bytes'
memo="$mem"m
msgmemory="max memory: $mem mega bytes"


# Print archi - memory message
echo $msgarchi - $msgmemory


java $splashoption -Xmx${memo} -cp ./class:./ext/* capsis.app.Starter $*

