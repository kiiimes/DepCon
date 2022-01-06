#!/bin/bash

kubectl delete pods --all
cd ../kubgo/src/Stella_con
insmod vif.ko
