sudo sh netperf_pod.sh $1 &
sleep 30
kubectl exec -it p$1.txt --namespace=default -- sysbench cpu --max-time=60 --num-threads=4 run &
sleep 3
PID=`pgrep sysbench`
sudo pidstat -p $PID -t 30 2 > sysbench.txt &
sudo mpstat 30 2 > mpstat.txt
