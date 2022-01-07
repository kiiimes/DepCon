#!/bin/bash
#num1=20
#num2=15
count=0
num1=1
list=(`kubectl get pods --output=wide | grep oslab2 | awk -F' ' '{print $1}'`)
for i in "${list[@]}"
do
#	w=`expr $i + $num1`
#	j=`expr $i + $num2`
	count=`expr $count + $num1`
	echo $i
	kubectl exec -it $i --namespace=default -- netperf -H 10.0.0.25 -p $count -l 150 -- -m 64 &	
	sleep 1
#	kubectl exec -it p$j --namespace=default -- netperf -H 10.0.0.22 -p $w -l 150 -- -m 64 &
#	sleep 1
done
sleep 1
./vnstat_pod.sh &
pidstat -G netperf 120 1 > pidstat.txt & mpstat -P ALL 120 1 > mpstat.txt
