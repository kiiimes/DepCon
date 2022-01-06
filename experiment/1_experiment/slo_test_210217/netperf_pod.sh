#!/bin/bash
N=$1
mkdir 120s_300Mbps_20pod_10times
for j in 1 2 3 4 5 6 7 8 9 10
do
mkdir 120s_300Mbps_20pod_10times/$j
for i in $(seq 1 $N)
do
	kubectl exec -it p$i --namespace=default -- netperf -H 10.0.0.25 -p $i -l 150 -- -m 64 > a.txt&
	sleep 1
done
sleep 3
sh vnstat_pod.sh $N $j&
pidstat -G netperf 120 1 > 120s_300Mbps_20pod_10times/$j/pidstat.txt & mpstat -P ALL 120 1 > 120s_300Mbps_20pod_10times/$j/mpstat.txt
cp sum.sh 120s_300Mbps_20pod_10times/$j
sleep 3
done
