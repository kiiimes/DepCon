# DepCon 구현 및 실행 내용 정리
## DepCon scheduler 구현
* DepCon scheduler는 network SLO를 충족시키기에 가장 적합헌 서버를 찾는 역할을 한다. 먼저, Filtering과정을 거치는데 이는 Pod에 설정한 Network SLO 달성을 위해 network bandwidth가 충분한 서버들의 리스트를 생성한다. 그 다음, Scoring 과정을 거치는데 앞에서 만들어진 서버 리스트에서 사용 가능한 CPU가 가장 많은 서버를 선택한다. 이는, DepCon agent가 각 서버에서 CPU를 동적으로 조절하여 network SLO를 달성할 수 있도록 적합환 환경을 만들어주는 과정이다. 각 기능에 대한 구현 방법은 아래와 같다. 

### Kubernetes cluster 구성
* 아래의 링크대로 만들어 구성함. kubernetes의 버전은 1.18.15 버전 사용
* [우분투 Kubernetes 설치 방법 - HiSEON](https://hiseon.me/linux/ubuntu/ubuntu-kubernetes-install/)
* 설치하는 부분만 아래의 커맨드로 바꿔줌
`sudo apt-get install kubectl=1.18.3-00 kubelet=1.18.3-00 kubeadm=1.18.3-00 kubernetes-cni=0.8.6-00`

### Filtering 
* Filtering 과정에서는 사용하려는 추가 리소스만 api server에 추가하면, 해당 리소스를 요청하는 pod가 각 서버들에 할당이 가능한지 아닌지를 판단해준다. 
* 기존 Kubernetes에서는 network 자원에 대한 할당 가능 여부를 filtering 과정에서 제공하지 않기 때문에 각 서버의 network capacity를 API Server에 알려준다. 
### Filtering 구현
* 디렉토리 : 1번 서버) / home/oslab/eskim/depcon
* sh slo_setting.sh 를 실행하여 network SLO 셋팅
	* sh slo_setting.sh [네트워크 인터페이스] 
```
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
        --data '[{"op": "add", "path": "/status/capacity/example.com~1SLO", "value": '`expr $value`'}]' \
        http://localhost:8001/api/v1/nodes/$node/status
done
```
	1. Ethtool 명령어를 이용하여 각 서버의 network interface의 정보를 얻어 온다. 그 중 Ethernet 속도를 나타내는 Speed를 파싱하여 지원하는 ethernet 속도를 저장한다. 
	2. 모든 노드들의 이름 리스트를 얻어오고, API server에 리소스 정보 전달을 위해 kubernetes proxy를 실행한다. -> 켜져 있지 않으면 켜는 과정을 거쳐야 함
	3. 노드들의 리스트에서 반복문을 통해 각 노드의 이름을 가져오고 curl 명령어를 이용해 API server에 ethernet 지원 속도를 SLO의 capacity로 설정한다. -> Mbps 단위가 고려 대상이기 때문에 capacity를 Mbps단위로 지정하였음. 
* 위의 내용처럼 네트워크 자원을 등록하고 나면 kubernetes에서는 각 노드의 capacity를 확인하고 pod에 설정된 네트워크를 할당할 수 있는지 확인한다. 할당가능하다면 pod를 생성할 노드 리스트에 추가한다. 특정 서버에 pod 생성이 완료되고 나면 pod에 할당된 네트워크 자원만큼 Kubernetes에서 서버의 할당 가능한 네트워크 자원 양을 제외한다. 

### Scoring
* Scoring과정에서는 network SLO를 달성하기 위해 필요한 CPU가 가장 많은 서버에 pod를 배치하도록 노드들의 가중치를 계산하는 역할을 한다. Kubernetes에서 제공하는 여러 플러그인들이 있지만, DepCon에서는 플러그인을 추가하여 CPU가 가장 많은 서버에 더 많은 가중치를 주도록 한다. 
* CPU가 가장 많은 서버에 pod를 배치하는 이유는 첫 번째로, Kubernetes scheduler 쪽에서 pod의 네트워크 성능을 달성하기 위해 얼만큼의 CPU가 필요한지 알 수 없기 때문이다. 두 번째로는 CPU가 가장 많은 서버에서도 네트워크 성능을 달성하지 못하면 다른 서버들 또한, 네트워크 성능 달성이 어렵기 때문에 CPU가 가장 많은 서버에 pod를 생성한다. 
* 이 과정에서 cpu 자원에 대한 fragmentation이 발생할 수 있는 문제점이 있었고, 미리 network 자원에 대해 필요한 CPU의 양을 예측할 수 있으면 이러한 문제점들을 해결할 수 있다는 아이디어로 이를 DepCon2에서 해결하려고 하였다.
### Scoring 구현
* 디렉토리 : /home/oslab/eskim/depcon
1. 먼저, depcon.conf 파일을 /etc/kubernetes/ 디렉토리로 copy한다. 
`cp depcon.conf /etc/kubernetes`
	* depcon.conf는 추가하려는 플러그인의 설정파일이다. 
	* 사용한 플러그인으로는 RequestedToCapacityRatioPriority 플러그인으로 각 리소스에 대해 가중치를 지정하여 각 자원의 capacity 대비 requested 비율(pod의 요청 자원 + 사용되고 있는 자원)을 계산하여 노드의 가중치를 결정한다. -> 가장 가중치가 큰 서버가 pod를 생성할 서버로 선택됨.
```
apiVersion: v1
kind: Policy
priorities:
  - name: RequestedToCapacityRatioPriority
    weight: 2
    argument:
      requestedToCapacityRatioArguments:
        shape:
          - utilization: 0
            score: 10
          - utilization: 100
            score: 0
        resources:
          - name: CPU
            weight: 10
          - name: Memory
            weight: 1
```
	* 위와 같이 depcon.conf 파일을 작성한다. 플러그인의 weight을 2로 준 이유는 기존의 weight가 1로 설정된 플러그인들보다는 우선순위가 높아야 하기 때문이다. shape의 경우에는 최소 요청인 경우에 더 높은 우선순위를 부여하였다. -> 여기에서 최소 요청이란 [사용된 양 + 요청된 양]이 최소인 즉, utilization이 가장 낮은 (사용 가능한 자원이 가장 많은) 노드에 가중치를 더 주겠다는 의미. 그러므로 utilization이 0일 때 score를 10으로 주고 utilization이 100일 때 score를 0으로 설정
	* resources에서는 CPU와 memory 중 CPU에 더 비중을 두어야 함. 
		* weight의 비율을 CPU: memory = 10:1로 준 이유는 CPU와 Memory 둘 다 최대가 될 수 있는 값이 10인데 CPU가 memory의 영향을 받지 않고 가장 높은 우선순위를 가지려면 10:1의 비율로 설정해야함. 
	* [참고] 수식 계산 방법 : [확장된 리소스를 위한 리소스 빈 패킹(bin packing) | Kubernetes](https://kubernetes.io/ko/docs/concepts/scheduling-eviction/resource-bin-packing/)
2. 그 다음, kube-scheduler.conf 파일을 /etc/kubernetes/manifests 로 copy한다. 
`cp kube-scheduler.conf /etc/kubernetes/manifests`
* kube-scheduler.conf는 kubernetes의 default scheduler에 대한 설정파일이다. 
* scheduler를 따로 구성하여 추가하는 방법도 있는데, 여기에서는 다루지 않고 depcon2에서 구성하는 방법에 대해 설명한다. 
```
apiVersion: v1
kind: Pod
metadata:
  creationTimestamp: null
  labels:
    component: kube-scheduler
    tier: control-plane
  name: kube-scheduler
  namespace: kube-system
spec:
  containers:
  - command:
    - kube-scheduler
    - --authentication-kubeconfig=/etc/kubernetes/scheduler.conf
    - --authorization-kubeconfig=/etc/kubernetes/scheduler.conf
    - --bind-address=127.0.0.1
    - --kubeconfig=/etc/kubernetes/scheduler.conf
    - --leader-elect=true
    image: k8s.gcr.io/kube-scheduler:v1.18.15
    imagePullPolicy: IfNotPresent
    livenessProbe:
      failureThreshold: 8
      httpGet:
        host: 127.0.0.1
        path: /healthz
        port: 10259
        scheme: HTTPS
      initialDelaySeconds: 15
      timeoutSeconds: 15
    name: kube-scheduler
    resources:
      requests:
        cpu: 100m
    volumeMounts:
    - mountPath: /etc/kubernetes/scheduler.conf
      name: kubeconfig
      readOnly: true
    - mountPath: /etc/kubernetes/depcon.conf
      name: depconfig
      readOnly: true
  hostNetwork: true
  priorityClassName: system-cluster-critical
  volumes:
  - hostPath:
      path: /etc/kubernetes/scheduler.conf
      type: FileOrCreate
    name: kubeconfig
  - hostPath:
      path: /etc/kubernetes/depcon.conf
      type: FileOrCreate
    name: depconfig
status: {}
```
	* kube-scheduler.conf의 경우에는 수정하게 되면 따로 kube-scheduler pod를 삭제하고 생성하는 과정을 하지 않아도 수정한 설정으로 다시 시작된다. 
	* 앞에 설명한 플러그인 파일인 depcon.conf 파일을 kube-scheduler pod에 마운트 해주고, Kube-scheduler 명령어의 옵션으로 --policy-config-file=/etc/kubernetes/depcon.conf (depcon.conf가 있는 디렉토리)를 추가 해주면 depcon.conf에 설정한 플러그인들이 kube-scheculer에 적용된다. 

## DepCon agent 구현
* DepCon agent는 각 서버에서 동적으로 CPU를 조절하여 network SLO를 달성하는 모듈이다. pod에서 보이는 network bandwidth가 network SLO보다 낮으면 network SLO 달성을 위해 CPU quota 값을 늘려주고, pod에서 보이는 network bandwidth가 network SLO보다 높으면 CPU quota 값을 줄여 다른 pod들도 함께 network SLO를 달성할 수 있도록 한다. 
* DepCon scheduler는 network SLO를 달성하기에 적합한 환경을 만들어주는 기능을 했다면, DepCon agent는 각 서버에서 실질적으로 CPU를 조절하여 network SLO를 달성하는 기능을 함.

### Kubelet에 DepCon agent의 커널 모듈 적용 (Kubelet 코드 수정)
* DepCon agent의 커널 모듈에 대한 설정 파일은 /proc/oslab/vif1, vif2, vif3… 이런식으로 컨테이너마다 디렉토리가 생성된다. 각 vif 디렉토리에는 pid, weight, min_credit 파일이 생성된다. 
	* pid : pid를 기입
	* goal : 컨테이너의 network SLO 값
	* weight : weight의 기본 값은 1이고, container의 가중치 -> 다른 컨테이너보다 우선순위를 두고 싶을 때 사용
	* min_credit : min credit의 기본 값은 0, bandwidth capacity를 percentage로 나타내고 컨테이너의 최소 bandwidth를 설정	
#### DepCon 소스코드 빌드
* 디렉토리 : /home/oslab2/eskim/kubgo/src/NeCon_k8s
	* 또는 GitHub.com/kiiimes/NeCon_k8s clone하여 사용
* make 실행하여 build
	* depcon Kubelet 실행 파일은 _output/bin/에 생성됨
* build 없이 depcon Kubelet 실행파일만 사용하고 싶으면 /home/oslab/eskim/depcon(1번 서버)에 Kubelet 실행파일을 /usr/bin/으로 copy하면 depcon Kubelet 적용됨
```
systemctl stop kubelet
cp /home/oslab/eskim/depcon/kubelet /usr/bin
systemctl restart kubelet
```
	* kubelet 데몬을 멈춘 후 depcon kubelet 실행파일을 복사 한 후에 다시 kubelet 데몬을 실행시킴

#### DepCon 소스코드 수정 관련
* DepCon agent를 실행하기 위해서는 /proc/oslab/vif{n}의 pid, goal(Network SLO)을 추가하는 과정이 필요함. 이를 스크립트로 추가할 수도 있지만, kubelet에서 pod가 생성될 때 바로 pid와 SLO 값을 설정할 수 있도록 하기 위해 kubelet을 수정
* 디렉토리 : /home/oslab2/eskim/kubgo/src/NeCon_k8s/pkg/kubelet
	* BUILD 파일 : kubernetes 소스코드 빌드할 때 수정된 kubelet 코드가 반영될 수 있도록 BUILD 파일 수정
	* kubelet.go : syncPod 함수에서 pod의 Cgroup을 생성하고 update하는 부분이 있는데 이 부분에서 pod에 대한 instance를 얻어오고 이 instance에 대한 값을 DepCon agent 소스코드 파일로 넘겨 줄 수 있도록 함. 
* 디렉토리 : /home/oslab2/eskim/kubgo/src/NeCon_k8s/pkg/kubelet/necon
	* BUILD 파일 : kubelet 코드 빌드 시 DepCon agent 소스 코드가 반영될 수 있도록 BUILD 파일 수정
	* necon.go : /proc/oslab/vif{n}/goal, pid를 설정하는 코드와 pod가 생성될 때 instance를 얻어오는 부분, CgroupPath를 얻어오는 부분 등의 DepCon agent를 구동시키기 위한 코드로 구성이 되어 있음. 
* 디렉토리 : /home/oslab2/eskim/kubgo/src/NeCon_k8s/pkg/kubelet/kuberuntime/
	* kuberuntime_manager.go : SyncPod 함수에서 Network와 관련된 설정을 다루기 때문에 여기에서 GetInstance를 하여 pod에 설정된 SLO를 얻어와 DepCon agent 설정 파일에 전달
	* kuberuntime_container.go : startContainer 함수에서 container 생성을 위한 설정을 마치고 pod 내부에 생성된 container의 ID를 얻어와서 necon.go 파일에 넘겨줌 
* 디렉토리 : /home/oslab2/eskim/kubgo/src/NeCon_k8s/pkg/kubelet/cm/cgroup_manager_linux.go
	* CgroupPath와 container ID가 결합된 디렉토리에서 container에 대한 PID를 구해올 수 있기 때문에 Croup configuration을 update하는 함수에서 Cgroup path를 얻어오고 이 cgroup path와 container ID를 결합하여 necon.go 파일에서 DepCon agent의 설정파일에 PID를 전달한다. 

### DepCon agent 실행 방법
* 디렉토리 : /home/oslab2/eskim/kubgo/src/Stella_con
* 커널 버전 : Linux 5.3.18
* [참고] 커널 모듈 실행 시 kubernetes 관련 컨테이너 외의 다른 컨테이너들은 생성되어 있지 않아야함. -> 커널 모듈 내릴 때도 생성된 컨테이너들을 삭제 후 모듈을 내려야함. 
1. DepCon agent를 실행하기 위한 커널 모듈을 생성하기 위해 디렉토리에서 make를 하여 vif.ko 파일 생성
2. 아래의 커맨드로 DepCon agent의 커널 모듈을 올림 
`insmod vif.ko`
3. 아래의 커맨드로 DepCon agent의 커널 모듈이 제대로 올라갔는지 확인
`lsmod | grep vif`

* DepCon agent가 제대로 적용됐는지 확인하기 위해 pod 생성 후 테스트 진행 
	* DepCon scheduler 실행 후 진행
* 디렉토리 : /home/oslab2/eskim/slo_test/
* 먼저, netperf, vnstat 등이 설치되어 있는 ubuntu 이미지를 다운로드
`docker pull dkdla58/ubuntu:netperf`
* 아래의 커맨드로 pod 생성
`kubectl create -f test.yaml`
```
kind: Pod
apiVersion: v1
metadata:
kind: Pod
apiVersion: v1
metadata:
  name: p1
spec:
  containers:
    - name: p1
      image: dkdla58/ubuntu:netperf
      resources:
        limits:
             example.com/SLO: 240
      command: ["/bin/bash", "-ec", "while :; do echo '.'; sleep 5 ; done"]
  restartPolicy: Never
  nodeName: oslab2
```
* image는 테스트를 위해 위에서 다운로드 받은 이미지를 사용
* resource limit으로 example.com/SLO 항목에 Mbps 단위로 값을 입력

* Pod 생성까지 완료 후 pid와 goal이 정상적으로 각각 /proc/oslab/vif1/pid, goal 에 입력되었는지 확인 후 테스트 실험 진행
	* ./netperf_pod.sh 1 (1개의 pod에 대해서 netperf 스크립트 실행, vnstat, pidstat도 함께 실행)
	* vnstat 결과는 p1.txt 와 같은 형식의 파일로 생성됨
	* p1.txt의 결과가 240Mbps를 달성 가능한 지 확인

### DepCon agent 실행 중지 방법
* 모든 Pod들을 먼저 삭제 함. 
`kubectl delete pods --all`
* 모든 pod들이 삭제한 것을 확인 후 DepCon 모듈을 커널에서 제거 
```
kubectl get pods #생성한 pod들이 더 이상 list에 뜨지 않으면 아래의 커맨드 실행
rmmod vif.ko
```
* kubelet도 기존에 DepCon이 적용되지 않은 kubelet을 백업하고 있다가 (각자 백업 필요) 다시 /usr/bin으로 복사하여 사용 
`cp kubelet /usr/bin`
* 혹은 DepCon이 적용된 kubelet을 계속 사용하고 싶다면, 아래의 커맨드로 kubelet 재시작
`systemctl restart kubelet`

## 레퍼런스
* Stella_con : https://github.com/KUoslab/Stella_con
* Kubernetes : https://github.com/kubernetes/kubernetes
