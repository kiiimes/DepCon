#!/bin/sh
i=$1
CON=$2
Q=$3
P=$4

echo "Setting a container's PID in the scheduling module."
PID=`docker inspect --format="{{.State.Pid}}" $CON`
echo $PID > /proc/oslab/vif$i/pid

echo "Setting the class of the container."
echo $Q > /proc/oslab/vif$i/weight

echo "Setting the number of processes."
echo $P > /proc/oslab/vif$i/min_credit
