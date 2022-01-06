#!/bin/bash
#num1=20
#num2=15
count=0
num1=1
list=(`kubectl get pods --output=wide | grep oslab2 | awk -F' ' '{print $1}'`)
for i in "${list[@]}"
do
        count=`expr $count + $num1`
        echo $i
	kubectl exec -it $i --namespace=default -- vnstat -tr 120 > $i.txt &
done
