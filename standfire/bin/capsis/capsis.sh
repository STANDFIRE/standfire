#!/bin/bash

cd `dirname "$0"`
EXTDIR=`cd ./ext; pwd`


# Check wether the splashscreen should be shown (yes, except in script mode) fc-21.5.2013
splashoption="-splash:./etc/splash.png"
string0="$*"
string1=${string0%script*}

#echo string0: aa $string0 aa
#echo string1: aa $string1 aa

if [ "$string0" != "$string1" ]; then
  echo Script: splashscreen was desactivated
  splashoption=""
fi





# Check architecture, set default memory and libraries path
architecture=`uname -m`
if [ "$architecture" != "x86_64" ] && [ "$architecture" != "ia64" ]; then
    
  msgarchi='32 bits architecture'
  mem="10000"   # 32 bits default value / see setmem.sh to change it
  export LD_LIBRARY_PATH=$EXTDIR/linux:$LD_LIBRARY_PATH

else

  msgarchi='64 bits architecture'
  mem="10000"   # 64 bits default value / see setmem.sh to change it
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
#java -splash:./etc/splash.png -Xmx${memo} -cp ./class:./ext/* capsis.app.Starter $*



# Encoding strategy fc-18.11.2011 try to remove all encoding considerations
#java -splash:./etc/splash.png -Dfile.encoding=ISO8859-15 -Xmx${memo}  -cp ./class:./ext/* capsis.app.Starter $1 $2 $3 $4 $5 $6 $7 $8 $9

# Run with Java agent
#java -javaagent:agent.jar -splash:./etc/splash.png -Dfile.encoding=ISO8859-15 -Xmx${memo}  -cp ./class:./ext/* capsis.app.Starter $1 $2 $3 $4 $5 $6 $7 $8 $9


#turned="0"
#for i in $* 
#do
#
#  # Search for a user value on the command line, e.g. mem=4096
#  if [ $i ] && [ `expr substr $i 1 4` = mem= ]
#  then
#    mem=`expr substr $i 5 5`
#    turned="1"
#    echo max memory turned to "$mem" mega bytes... 
#  fi
#
#  # if help requested, document the mem= option
#  if [ $i ] && [ `expr substr $i 1 2` = -h ]
#  then
#    echo " mem=xxxx      : request up to xxxx Mb memory from the OS (default is 1024)"
#  fi
#
#done
#
#  if [ $turned -eq "0" ] 
#  then 
#    echo max memory set to "$mem" mega bytes...
#  fi
