#!/bin/bash
list=(`kubectl get pods --output=wide | grep oslab2 | awk -F' ' '{print $1}'`)
for i in "${list[@]}"
do
	echo $i
	grep -r tx $i.txt
done
