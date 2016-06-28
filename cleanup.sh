#!/bin/bash


# Change this to your netid
netid=sxk141931

#
# Root directory of your project
PROJDIR=$HOME/CS6378/Project1

#
# This assumes your config file is named "config.txt"
# and is located in your project directory
#
CONFIG=$PROJDIR/config.txt

#
# Directory your java classes are in
#
BINDIR=$PROJDIR/bin

#
# Your main project class
#
PROG=utd.com.NodeRunner

n=0

cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read firstLine
    echo $firstLine

    numberOfServers=$( echo $firstLine | awk '{ print $1}' )

    while  [ $n -lt $numberOfServers ]
    do
        read line
        host=$( echo $line | awk '{ print $2 }' )
        ssh $netid@$host pkill -9 java -u $netid &
        echo "Killed process running in $host"
        sleep 1

        n=$(( n + 1 ))
    done
   
)

echo "Cleanup complete"
