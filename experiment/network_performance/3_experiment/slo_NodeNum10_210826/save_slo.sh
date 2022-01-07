#!/bin/bash
N=$1
for i in $(seq 1 $N)
do
	a=`cat p$i.yaml | grep "example.com"`
	echo $a
done
