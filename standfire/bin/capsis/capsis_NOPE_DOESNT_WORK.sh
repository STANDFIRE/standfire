#!/bin/bash


# This script launches Capsis under Linux 32 or 64 bits or MacOSX
# reviewed fc-16.9.2014


# Check wether the splashscreen should be shown (yes, except in script mode)
splashoption="-splash:./etc/splash.png"
string0="$*"
string1=${string0%script*}

echo string0: aa $string0 aa
echo string1: aa $string1 aa

#if [ "$string0" != "$string1" ] # removed an extra ';' here fc-27.11.2014
#then
echo Script: splashscreen was desactivated
splashoption=""
#fi


# Set default memory in Mb (see setmem.sh to change it)
mem="1024"


# Check if 'memory' file exists (created by setmem.sh), read it (1 single line, e.g. 4096)
if [ -f memory ]  # Check if the file named 'memory' exists
then
  read line < "memory"
  mem=$line
fi


# Set max memory, adding 'm' for 'Mega bytes'
memo="$mem"m


# If the javalibrarypath file was not already created, create it
if [ ! -f javalibrarypath ]  # if the file named 'javalibrarypath' does not exist
then
  java -cp ./class:./ext/* capsis.util.JavaLibraryPathDetector
fi


# Read the javalibrarypath file into a variable
read line < "javalibrarypath"
jlp=$line


# Launch Capsis
java $splashoption -Xmx${memo} -cp ./class:./ext/* -Djna.library.path=$jlp -Djava.library.path=$jlp capsis.app.Starter $*
