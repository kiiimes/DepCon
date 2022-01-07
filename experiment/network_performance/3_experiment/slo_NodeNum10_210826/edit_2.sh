#!/bin/bash
N=$1

for i in $(seq 1 83)
do
	sed -i "s/cpu: \"100\"/cpu: \"100m\"/g" p$i.yaml
	#sed -i "s/00m/00/g" p$i.yaml
done
