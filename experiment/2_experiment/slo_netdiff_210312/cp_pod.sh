#!/bin/bash
i=1
while [ ${i} -le 40 ]
do
	i=$((i+2))
	cp p1.yaml p$i.yaml
done
