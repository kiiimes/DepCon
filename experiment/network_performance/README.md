# 네트워크 성능 평가 실험 
## 1. k8s와 DepCon의 Pod 수 증가에 따른 네트워크 성능 평가 및 비교
* 2대의 서버에서 300Mbps의 SLO를 요구하는 pod 수를 5, 10, 15, 20개로 증가하여 network SLO 달성률을 실험하였을 때 기존 Kubernetes와 DepCon의 성능을 비교한다. 
* Kubernetes에서 제공하는 네트워크 트래픽 셰이핑 플러그인은 tc를 이용하여 네트워크 트래픽을 조절한다. tc는 각 컨테이너에 대한 패킷 큐를 유지하고 패킷 헤더 정보를 기반으로 들어오는 패킷을 분류하는데, 이 때, en/de-queueing을 위한 lock operation을 포함하여 패킷 검사 및 분류 작업이 필요하기 때문에 패킷 처리에 대한 softirq 오버헤드를 증가시키고 전체 네트워크 처리량 감소로 이어지게 된다. 
* Kubernetes에 tc를 적용했을 때의 성능 감소를 DepCon을 이용하여 해결 가능한지 확인 및 비교한다. -> 자세한 내용은 DepCon 논문에 나와있음.

### 실험 환경
* 1번 서버 : Intel E5-2650 v3 10 cores CPU, 256GB memory 
* 2번 서버 : Intel E5-2650 v3 10 cores CPU, 128GB memory 
* 10GbE network interface로 구성된 2대의 서버
* Linux kernel version : 5.13.18 , Ubuntu version : 18.04, Kubernetes version : 1.18.3, Docker version : 18.09
### 실험 방법
* 1번 서버를 master node, 2번 서버를 worker node로 사용하였고 pod들의 생성은 2번 서버에서 진행하였다.
#### tc 실험 방법 (기존 Kubernetes의 네트워크 트래픽 셰이핑 플러그인 이용) 
* 1번 서버(Master node)에서 netserver 실행 -> pod 생성 수와 맞게 실행
`netserver -p 1 && netserver -p 2 && netserver -p [pod를 5개 실행하면 5개의 netserver 실행]`
* 2번 서버 (Worker node) 에서 Kubernetes의 네트워크 트래픽 셰이핑 플러그인을 적용
	* kubelet을 정지한 후 플러그인 추가를 위해 설정파일 열기
```
systemctl stop kubelet
vi /etc/cni/net.d/10-flannel.conflist
```
* 아래와 같은 설정 파일에 type: bandwidth의 플러그인을 추가
```
{
  "name": "cbr0",
  "cniVersion": "0.3.1",
  "plugins": [
    {
      "type": "flannel",
      "delegate": {
        "hairpinMode": true,
        "isDefaultGateway": true
      }
    },
    {
      "type": "portmap",
      "capabilities": {
        "portMappings": true
      }
    }, //아래부분이 추가 부분
	  {
      "type": "bandwidth",
      "capabilities": {"bandwidth": true}
	  }
  ]
}
```
	* 아래의 커맨드로 kubelet을 재시작한 후 플러그인이 제대로 적용되었는지 확인
```
systemctl restart kubelet
cat /etc/cni/net.d/10-flannel.conflist
```

* tc 실험 디렉토리 : /home/oslab2/eskim/depcon/tc_210217
* 스크립트별 설명 [필요에 맞게 수정해서 사용] -> 2번 서버의 위 디렉토리에 존재 
	* cp_pod.sh : 5, 10, 15, 20 개의 pod 생성을 위해서는 yaml의 갯수도 그에 맞게 있어야 함. pod를 쉽게 복사하기 위한 스크립트. 
	* edit.sh : pod를 복사 후에 다 같은 pod name으로 설정되어 있을 때 pod 이름을 바꿔주는 용도 또는 network SLO 값을 변경하는 용도로 사용 
`sed -i "s/p1/p$i/g" p$i.yaml` or `sed -i 's/160M/300M p$i.yaml`
* create_pod.sh : pod를 생성하는 스크립트
* netperf_pod.sh : netperf, vnstat, pidstat을 실행하는 스크립트, 10번 실험한 평균을 낼 수 있도록 10번 실험 값 측정 
```
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
```
* 실험 결과를 저장할 디렉토리 생성 후 실험을 10번 반복
* 한 번의 실험에서 netperf를 150초 동안 64bytes 메시지 크기로 packet을 전송
	* 150초 실험을 진행하는 이유는 vnstat, pidstat 등 측정을 하는 동안 netperf 실행이 중지되는 일이 없도록 조정

* vnstat_pod.sh : vnstat을 이용하여 120초 동안 실험 진행 -> netperf_pod.sh 에서 호출됨
* sum.sh : vnstat의 결과 파일로 생성되는 텍스트 파일을 모아서 한 번에 보여줌. 
* p1.yaml 
```
kind: Pod
apiVersion: v1
metadata:
  name: p1
  #namespace: qos
  annotations:
          kubernetes.io/ingress-bandwidth: 300M
          kubernetes.io/egress-bandwidth: 300M
spec:
  containers:
    - name: p1
      image: ubuntu:netperf
      command: ["/bin/bash", "-ec", "while :; do echo '.'; sleep 5 ; done"]
  restartPolicy: Never
```
* Kubernetes의 bandwidth 플러그인을 활성화하기 위해서 annotations를 추가해줘야 함. 
	* ingress와 egress 모두 달성하려는 목표 성능으로 설정 -> ingress 들어오는 트래픽, egress 나가는 트래픽
* 이미지는 netperf와 vnstat 등이 설치된 이미지를 사용하는데 이 디렉토리에 설정된 ubuntu:netperf 이미지는 직접 만들어야 하는 이미지이기 때문에 같은 기능을 제공하는 dkdla58/ubuntu:netperf 이미지를 사용하시는게 좋을 듯.

* 실험 실행 순서 
1. yaml 파일 및 스크립트 필요에 맞게 수정
2. ./create_pod.sh -> 지정된 수의 pod 생성
3. ./netperf_pod.sh [생성할 pod 수] 
4. ./sum.sh [출력할 pod 결과 수] > vnstat.txt 

* raw_data 위치
	* 5 pod : 120s_300Mbps_5pod
	* 10 pod : 120s_300Mbps_10pod
	* 15 pod : 120s_300Mbps_15pod_10times
	* 20 pod : 120s_300Mbps_20pod_10times

#### slo 실험 방법 (Kubernetes에 DepCon 기법 적용한 실험) 
* slo 실험 디렉토리 : /home/oslab2/depcon/slo_test_210217
* 스크립트 파일은 tc와 동일 
```
kind: Pod
apiVersion: v1
metadata:
  name: p1
spec:
  containers:
    - name: p1
      image: ubuntu:netperf
      resources:
        limits:
             example.com/SLO: 300
      command: ["/bin/bash", "-ec", "while :; do echo '.'; sleep 5 ; done"]
  restartPolicy: Never
```
* annotations 대신 example.com/SLO 를 지정하여 실험 진행 
* 이미지는 netperf와 vnstat 등이 설치된 이미지를 사용하는데 이 디렉토리에 설정된 ubuntu:netperf 이미지는 직접 만들어야 하는 이미지이기 때문에 같은 기능을 제공하는 dkdla58/ubuntu:netperf 이미지를 사용하시는게 좋을 듯.

* 실험 실행 순서 
1. DepCon scheduler 및 agent 실행
2. yaml 파일 및 스크립트 필요에 맞게 수정
3. ./create_pod.sh -> 지정된 수의 pod 생성
4. ./netperf_pod.sh [생성할 pod 수] 
5. ./sum.sh [출력할 pod 결과 수] > vnstat.txt 

* raw_data 위치
	* 5 pod : 120s_300Mbps_5pod
	* 10 pod : 120s_300Mbps_10pod
	* 15 pod : 120s_300Mbps_15pod_10times
	* 20 pod : 120s_300Mbps_20pod_10times

## 2. k8s와 DepCon에서 여러 network SLO를 요구하는 pod들의 네트워크 성능 평가 및 비교
* 4대의 서버에서 100Mbps와 300Mbps의 SLO를 요구하는 Pod를 함께 생성하였을 때 network SLO의 달성률에 대해 기존 Kubernetes와 DepCon의 성능을 비교한다. 
* 다양한 SLO와 더 많은 서버를 이용하였을 때의 상황에서 성능 평가 진행

### 실험 환경
* 1번 서버 : Intel E5-2650 v3 10 cores CPU, 256GB memory 
* 2번 서버 : Intel E5-2650 v3 10 cores CPU, 128GB memory 
* 3번 서버 : Intel E5-2650 v3 10 cores CPU, 256GB memory 
* 6번 서버 : Intel E5-2650 v3 10 cores CPU, 256GB memory 
* 10GbE network interface로 구성된 4대의 서버
* Linux kernel version : 5.13.18 , Ubuntu version : 18.04, Kubernetes version : 1.18.3, Docker version : 18.09
### 실험 방법
* 1번 서버를 master node, 2,6번 서버를 worker node로 사용하였고 pod들의 생성은 worker 노드에서 진행
* 1번 (Master node), 3번 (Evaluation machine)에서 netserver를 실행하여 네트워크에서 bottleneck이 발생하지 않도록 하고, 랜덤으로 100Mbps, 300Mbps로 구성된 30개의 컨테이너를 생성하여 실험 진행 
#### tc 실험 방법 (기존 Kubernetes의 네트워크 트래픽 셰이핑 플러그인 이용) 
* 1번 서버(Master node)에서 netserver.sh 실행 -> pod 생성 수와 맞게 스크립트 수정
`netserver -p 1 && netserver -p 2 && netserver -p [pod를 5개 실행하면 5개의 netserver 실행]`
* 2,4 번 서버 (Worker node) 에서 Kubernetes의 네트워크 트래픽 셰이핑 플러그인을 적용
	* 1번 실험 방법과 동일
* tc 실험 디렉토리 : [1번 서버] /home/oslab/eskim/depcon, [2번 서버] /home/oslab2/eskim/depcon/tc_netdiff_210312, [6번 서버] /home/test/eskim/depcon/tc_netdiff_210312 (raw data는 dropbox에 존재)
* 스크립트별 설명 [필요에 맞게 수정해서 사용] -> 2번 서버의 위 디렉토리에 존재 , 6번 서버는 /home/test/eskim/depcon/tc_netdiff_210312에 스크립트 존재
* * 스크립트별 설명은 1번 실험과 같고, 다른 스크립트는 아래와 같음
	* edit_pnum.sh : pod를 복사 후에 다 같은 pod name으로 설정되어 있을 때 pod 이름을 바꿔주는 용도
	* edit.sh : network SLO 값을 변경하는 용도로 사용 
	* netperf_pod.sh : netperf, vnstat, pidstat을 실행하는 스크립트
```
#!/bin/bash
count=0
num1=1
list=(`kubectl get pods --output=wide | grep oslab2 | awk -F' ' '{print $1}'`)
for i in "${list[@]}"
do
        count=`expr $count + $num1`
        echo $i
        kubectl exec -it $i --namespace=default -- netperf -H 10.0.0.25 -p $count -l 150 -- -m 64 &
        sleep 1
done
sleep 1
./vnstat_pod.sh &
pidstat -G netperf 120 1 > pidstat.txt & mpstat -P ALL 120 1 > mpstat.txt
```
* 2번 서버의 netperf 스크립트
* netperf를 실행할 때 이 스크립트를 실행하는 서버에 있는 pod만 결과 값을 측정할 수 있으므로, 이 스크립트를 실행하는 서버의 이름인 oslab2를 node name으로 가지는 pod만 netperf를 실행한다. 
* 위 스크립트를 6번 서버에서도 동일하게 수정하여 실험 진행하면 됨.
	* 6번 서버의 경우에는 node name을 oslab4로 grep 하면 됨.
  
* vnstat_pod.sh : vnstat을 이용하여 120초 동안 실험 진행 -> netperf_pod.sh 에서 호출됨
	* 이 스크립트도 vnstat을 실행할 pod가 이 스크립트를 실행하는 서버에 존재해야 하므로 node name을 grep하여 vnstat 측정
* p1.yaml : 랜덤으로 9개의 pod는 100Mbps, 21개의 pod는 300Mbps로 pod 생성을 하였고, 1번 실험처럼 annotations의 ingress, egress 수정해주면 됨. 

* 실험 실행 순서 
1. yaml 파일 및 스크립트 필요에 맞게 수정
2. ./create_pod.sh -> 30개의 pod 생성
3. ./netperf_pod.sh 
4. ./sum.sh > vnstat.txt 

* raw_data 위치
	* tc raw data : dropbox -> 2020 k8s analysis /depcon /netdiff/tc_netdiff_210312_oslab2,oslab4/120s_21pod_100_9pod

#### slo 실험 방법 (기존 Kubernetes의 네트워크 트래픽 셰이핑 플러그인 이용) 
* 1번 서버(Master node)에서 netserver.sh 실행 -> pod 생성 수와 맞게 스크립트 수정
`netserver -p 1 && netserver -p 2 && netserver -p [pod를 5개 실행하면 5개의 netserver 실행]`
* 2,6 번 서버 (Worker node) 에서 Kubernetes의 네트워크 트래픽 셰이핑 플러그인을 적용
	* 1번 실험 방법과 동일
* slo 실험 디렉토리 : [1번 서버] /home/oslab/eskim/depcon, [2번 서버] /home/oslab2/eskim/depcon/slo_netdiff_210312, [6번 서버] /home/test/eskim/depcon/slo_netdiff_210312 (raw data는 dropbox에 존재)
* 스크립트별 설명 [필요에 맞게 수정해서 사용] -> 2번 서버의 위 디렉토리에 존재 , 6번 서버는 /home/test/eskim/depcon/slo_netdiff_210312 에 스크립트 존재
* 스크립트별 설명은 2번의 tc 실험과 같음. (p1.yaml은 예외)
* p1.yaml : 랜덤으로 9개의 pod는 100Mbps, 21개의 pod는 300Mbps로 pod 생성을 하였고, example.com/SLO를 각각 설정하려는 SLO에 맞게 100 또는 300 으로 설정

* 실험 실행 순서 
1. DepCon scheduler 및 agent 실행
2. yaml 파일 및 스크립트 필요에 맞게 수정
3. ./create_pod.sh -> 30개의 pod 생성
4. ./netperf_pod.sh 
5. ./sum.sh > vnstat.txt 

* raw_data 위치
	* 	slo raw data : dropbox -> 2020 k8s analysis /depcon /netdiff/slo_netdiff_210312_oslab2,oslab4/120s_21pod_100_9pod


##  3. k8s와 DepCon에서 더 많은 서버에서 더 다양한 network SLO를 요구하는 pod들의 네트워크 성능 평가 및 비교 
* 4대의 서버로 실제 클러스터 환경에서의 동작을 보여주기 힘들다고 판단하여 10대의 서버에서 더 다양한 SLO(100, 200, 300, 400 Mbps)를 설정한 pod를 생성하여 각 pod의 네트워크 목표 성능의 달성률을 확인
* 기존 Kubernetes의 네트워크 성능과 DepCon의 성능을 비교
### 실험 환경
* 1번 서버 : Intel E5-2650 v3 10 cores CPU, 256GB memory 
* 2번 서버 : Intel E5-2650 v3 10 cores CPU, 128GB memory 
* 3번 서버 : Intel E5-2650 v3 10 cores CPU, 256GB memory 
* 4번 서버 : Intel E5-2650 v3 10 cores CPU, 256GB memory 
* 6번 서버 : Intel E5-2650 v3 10 cores CPU, 256GB memory 
* 7번 서버 : Intel E5-2650 v3 10 cores CPU, 256GB memory 
* 8번 서버 : Intel E5-2650 v3 10 cores CPU, 256GB memory 
* 10번 서버 : Intel E5-2650 v3 10 cores CPU, 256GB memory 
* 11번 서버 : Intel E5-2650 v4 24 cores CPU, 128GB memory 
* 12번 서버 : Intel E5-2650 v4 24 cores CPU, 128GB memory 
* 10GbE network interface로 구성된 10대의 서버
* Linux kernel version : 5.13.18 , Ubuntu version : 18.04, Kubernetes version : 1.18.3, Docker version : 18.09

### 실험 방법
* 1번 서버를 master node, 2,3,4,6,8,10번 서버를 worker node로 사용하였고 pod들의 생성은 worker 노드에서 진행
* 1번 (Master node), 7,11,12번 (Evaluation machine)에서 netserver를 실행하여 netperf 패킷을 netserver 하나 당 2개의 서버에서 보내어 네트워크에서 bottleneck이 발생하지 않도록 하고, 랜덤으로 100Mbps, 200Mbps, 300Mbps, 400Mbps로 구성된 100개의 컨테이너를 생성하여 실험 진행 

#### tc 실험 방법 (기존 Kubernetes의 네트워크 트래픽 셰이핑 플러그인 이용) 
* 7,11,12번 서버(Master node)에서 netserver.sh 실행 -> pod 생성 수와 맞게 스크립트 수정
`netserver -p 1 && netserver -p 2 && netserver -p [pod를 5개 실행하면 5개의 netserver 실행]`
* 2,3,4,6,8,10 번 서버 (Worker node) 에서 Kubernetes의 네트워크 트래픽 셰이핑 플러그인을 적용
	* 1번 실험 방법과 동일
* tc 실험 디렉토리 : 
	* [1번 서버] /home/oslab/eskim/depcon, 
	* [2번 서버] /home/oslab2/eskim/depcon/tc_NodeNum10_210826,
	* [3번 서버] /home/server3/eskim/depcon/tc_NodeNum10_210826,
	* [4번 서버] /home/kimeunsook/eskim/depcon/tc_NodeNum10_210826,
	* [6번 서버] /home/test/eskim/depcon/depcon/tc_NodeNum10_210826,
	* [8번 서버] /home/storage/eskim/depcon/tc_NodeNum10_210826,
	* [10번 서버] /home/fire/eskim/depcon/tc_NodeNum10_210826,
	* [11번 서버] /home/oslab/eskim/depcon
	* [12번 서버] /home/oslab/eskim/depcon
* 스크립트별 설명 [필요에 맞게 수정해서 사용] -> 2번 서버에서 모든 pod 생성하므로 2번 서버 디렉토리에 스크립트 존재, 위 디렉토리에 없으면 상위 디렉토리 확인
* 스크립트별 설명은 2번 실험과 같고, 다른 스크립트는 아래와 같음
	* edit_pnum.sh : pod를 복사 후에 다 같은 pod name으로 설정되어 있을 때 pod 이름을 바꿔주는 용도
		* port 별로 지정되어 있는 포트들은 제외하도록 수정하여 실행함.
	* edit.sh : network SLO 값을 변경하는 용도로 사용 
		* 랜덤으로 network SLO 값을 설정해야 함으로 1부터 100까지 랜덤으로 값을 생성하여 bb.txt에 저장하고 이 bb.txt를 기반으로 pod를 생성하여 랜덤한 SLO 값을 가질 수 있도록 함. 
	* netperf_pod.sh : netperf, vnstat, pidstat을 실행하는 스크립트
		* 2번 실험의 스크립트와 같음 
		* 각 서버별로 Node name을 grep하여 스크립트에 설정해줌. 
	* vnstat_pod.sh : vnstat을 이용하여 120초 동안 실험 진행 -> netperf_pod.sh 에서 호출됨
		* 2번 실험의 스크립트와 같음 
		* 각 서버별로 Node name을 grep하여 스크립트에 설정해줌. 
	* p1.yaml : 랜덤으로 100Mbps, 400Mbps의 network SLO를 가지는 pod를 20개씩, 200Mbps, 300Mbps의 network SLO를 가지는 pod를 30개씩 생성하여 총 100개의 pod 생성, 1번 실험처럼 annotations의 ingress, egress 수정해주면 됨. 

* 실험 실행 순서 
1. yaml 파일 및 스크립트 필요에 맞게 수정
2. ./create_pod.sh -> 100개의 pod 생성
3. ./netperf_pod.sh 
4. ./sum.sh > vnstat.txt 

* raw_data 위치
	* tc raw data : 각 tc 디렉토리에 있음. 

#### slo 실험 방법 (기존 Kubernetes의 네트워크 트래픽 셰이핑 플러그인 이용) 
* 7,11,12번 서버(Master node)에서 netserver.sh 실행 -> pod 생성 수와 맞게 스크립트 수정
`netserver -p 1 && netserver -p 2 && netserver -p [pod를 5개 실행하면 5개의 netserver 실행]`
* 2,3,4,6,8,10 번 서버 (Worker node) 에서 Kubernetes의 네트워크 트래픽 셰이핑 플러그인을 적용
	* 1번 실험 방법과 동일
* tc 실험 디렉토리 : 
	* [1번 서버] /home/oslab/eskim/depcon, 
	* [2번 서버] /home/oslab2/eskim/depcon/slo_NodeNum10_210826/slo_NodeNum10_210827,
	* [3번 서버] /home/server3/eskim/depcon/slo_NodeNum10_210827,
	* [4번 서버] /home/kimeunsook/eskim/depcon/slo_NodeNum10_210827,
	* [6번 서버] /home/test/eskim/depcon/depcon/slo_NodeNum10_210827,
	* [8번 서버] /home/storage/eskim/depcon/slo_NodeNum10_210827,
	* [10번 서버] /home/fire/eskim/depcon/slo_NodeNum10_210827,
	* [11번 서버] /home/oslab/eskim/depcon
	* 	[12번 서버] /home/oslab/eskim/depcon
* 스크립트별 설명 [필요에 맞게 수정해서 사용] -> 2번 서버에서 모든 pod 생성하므로 2번 서버 디렉토리에 스크립트 존재, 위 디렉토리에 없으면 상위 디렉토리 확인
* 스크립트별 설명은 2번 실험과 같고, 다른 스크립트는 아래와 같음
	* edit_pnum.sh : pod를 복사 후에 다 같은 pod name으로 설정되어 있을 때 pod 이름을 바꿔주는 용도
		* port 별로 지정되어 있는 포트들은 제외하도록 수정하여 실행함.
	* edit.sh : network SLO 값을 변경하는 용도로 사용 
		* 랜덤으로 network SLO 값을 설정해야 함으로 1부터 100까지 랜덤으로 값을 생성하여 bb.txt에 저장하고 이 bb.txt를 기반으로 pod를 생성하여 랜덤한 SLO 값을 가질 수 있도록 함. 
	* netperf_pod.sh : netperf, vnstat, pidstat을 실행하는 스크립트
		* 2번 실험의 스크립트와 같음 
		* 각 서버별로 Node name을 grep하여 스크립트에 설정해줌. 
	* vnstat_pod.sh : vnstat을 이용하여 120초 동안 실험 진행 -> netperf_pod.sh 에서 호출됨
		* 2번 실험의 스크립트와 같음 
		* 각 서버별로 Node name을 grep하여 스크립트에 설정해줌. 
	* p1.yaml : 랜덤으로 100Mbps, 400Mbps의 network SLO를 가지는 pod를 20개씩, 200Mbps, 300Mbps의 network SLO를 가지는 pod를 30개씩 생성하여 총 100개의 pod 생성, 1번 실험처럼 example.com/SLO 설정해주면 됨. 

* 실험 실행 순서 
1. DepCon scheduler 및 agent 실행
2. yaml 파일 및 스크립트 필요에 맞게 수정
3. ./create_pod.sh -> 100개의 pod 생성
4. ./netperf_pod.sh 
5. ./sum.sh > vnstat.txt 

* raw_data 위치
	* slo raw data : 각 slo 디렉토리에 있음. 

## 실험별 간략한 결과 정리
* 실험별로 간략하게 정리하면 아래와 같다. 
### 1번 실험 결과
* DepCon 기법이 k8s 대비 
	* 평균 네트워크 처리량을 7% 향상
	* 15개 컨테이너 기준 네트워크 성능 편차가 평균 43% 감소
		* 성능 편차를 감소 시키고 대부분의 컨테이너의 네트워크 목표 성능 달성
### 2번 실험 결과
* DepCon은 100Mbps의 network SLO를 설정한 pod에 대해 모든 pod가 목표 성능을 달성하지만, k8s는 네트워크 성능이 목표 성능 대비 9% 저하, 성능 편차가 10% 증가함을 보여준다. 
* DepCon은 300Mbps의 network SLO를 설정한 pod에 대해 k8s 대비 네트워크 성능을 평균 5% 향상시키고, 성능 편차를 67% 감소시킴.
### 3번 실험 결과
* k8s는 200Mbps의 network SLO를 설정한 pod에 대해 최대 103Mbps의 성능 편차를 보이고, 400Mbps의 network SLO를 설정한 pod에 대해 평균 51%의 네트워크 성능 저하를 보임. 
* DepCon은 100, 200, 300Mbps의 network SLO를 설정한 pod에 대해 네트워크 성능 편차를 줄여 대부분의 pod들이 목표 성능을 달성하는 것을 보임. 
	* 200Mbps의 network SLO를 설정한 pod 기준 네트워크 성능 편차를 95% 감소 시킴 (103Mbps -> 5 Mbps)
	* 400Mbps의 network SLO를 설정한 pod 기준 네트워크 성능을 평균 40% 향상 시킴
