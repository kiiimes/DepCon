#!/bin/bash
N=$1
for i in $(seq 1 $N)
do
	sed -i "s/p1/p$i/g" p$i.yaml
	sleep 1
done
