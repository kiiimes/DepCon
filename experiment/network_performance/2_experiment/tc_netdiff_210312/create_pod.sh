#!/bin/bash
for i in $(seq 1 30)
do
	kubectl $1 -f p$i.yaml
done
