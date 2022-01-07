#!/bin/bash
N=$1

for i in $(seq 1 83)
do
	sed -i "s/#        kubernetes.io/        kubernetes.io/g" p$i.yaml
	#sed -i "s/00m/00/g" p$i.yaml
done
