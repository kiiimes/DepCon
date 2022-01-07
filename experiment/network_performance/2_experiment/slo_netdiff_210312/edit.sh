#!/bin/bash
i=2
while [ ${i} -le 40 ]
do	
	sed -i 's/300/100/' p$i.yaml
	i=$((i+2))
	sleep 1
done
