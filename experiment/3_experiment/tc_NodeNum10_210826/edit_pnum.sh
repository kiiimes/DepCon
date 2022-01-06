#!/bin/bash
N=$1
for i in $(seq 84 $N)
do
	sed -i "s/p1/p$i/g" p$i.yaml
done
