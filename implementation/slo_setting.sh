#!/bin/bash
speed=`ethtool $1 | grep Speed`
speedBack="${speed#*Speed: }"
value="${speedBack%%Mb/s*}"

nodes=$(echo "`kubectl get nodes`" | awk '{print $1}' | sed -n "2, \$p")
pid=`ps -ef | grep "kubectl proxy" | grep -v 'grep' | awk '{print $2}'`
if [ -z $pid ];then 
  kubectl proxy &
fi

for node in $nodes
do
	curl --header "Content-Type: application/json-patch+json" \
	--request PATCH \
	--data '[{"op": "add", "path": "/status/capacity/example.com~1SLO", "value": '`expr $value \* 1000000`'}]' \
	http://localhost:8001/api/v1/nodes/$node/status
done
