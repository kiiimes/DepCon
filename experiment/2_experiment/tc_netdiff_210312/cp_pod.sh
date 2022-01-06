#!/bin/bash
i=1
while [ ${i} -le 24 ]
do
	i=$((i+1))
	cp p1.yaml p$i.yaml
done
