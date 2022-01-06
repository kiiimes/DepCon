#!/bin/bash
N=$1
for i in $(seq 1 $N)
do
	sed -i 's/450m/600m/' p$i.yaml
	sleep 1
done
