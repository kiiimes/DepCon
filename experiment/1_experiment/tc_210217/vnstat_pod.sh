#!/bin/bash
for i in $(seq 1 $1)
do
	kubectl exec -it p$i --namespace=default -- vnstat -tr 120 > 120s_300Mbps_20pod_10times/$2/p$i.txt &
done
