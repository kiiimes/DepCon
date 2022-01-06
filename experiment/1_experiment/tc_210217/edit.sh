#!/bin/bash
N=$1
for i in $(seq 1 $N)
do
	sed -i 's/160M/300M/' p$i.yaml
	sleep 1
done
