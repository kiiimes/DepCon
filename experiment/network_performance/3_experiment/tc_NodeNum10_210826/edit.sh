#!/bin/bash
N=$1

value=$(<bb.txt)

for i in ${value[@]}
do
	sed -i "s/300/200/g" p$i.yaml
done
