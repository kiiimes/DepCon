#!/bin/bash
LIST=`kubectl get pods | grep deployment | awk '{print $1}'`
COUNT=1

for i in $LIST;
do
	kubectl exec -it $i --namespace=default -- netperf -H 10.0.0.25 -p $COUNT -l 150 -- -m 64 &
	sleep 3
	let COUNT=$COUNT+1
done

sh vnstat_pod.sh &
pidstat -G netperf 60 1 > pidstat.txt & mpstat -P ALL 60 1 > mpstat.txt
