#!/bin/bash
for i in $(seq 1 28)
do
	kubectl $1 -f p$i.yaml
	sleep 1
done
