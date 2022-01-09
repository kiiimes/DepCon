# CloudSim 관련 내용 및 실험[DepCon scheduling overhead] 정리
## 실험 목표
* DepCon과 DRF의 컨테이너 스케줄링 오버헤드 비교를 위해 대규모 클라우드 환경에서 시뮬레이션이 필요
* 이를 위해 CloudSim을 이용하여 컨테이너 스케줄링 완료 시간을 측정
* CloudSim에서 스케줄링 시간에 대한 코드는 제공하지 않고 있어 실제 시뮬레이션 시간을 측정하여 normalize된 값을 스케줄링 시간으로 사용

## CloudSim 이란?
* CloudSim은 클라우드 컴퓨팅 인프라 및 서비스의 모델링 및 시뮬레이션을 위한 프레임워크
	* 실제로 자원을 주거나 받는것이 아니라 mips, clock 수 등을 이용해서 실제 환경에서 어느정도의 컨테이너 생성 혹은 자원을 사용할지 생성되는데 얼마나 걸릴지 등을 계산함
* CloudSim plus, CloudSimEx 등의 확장된 버전도 제공
* CloudSim 주요 기능
	* 대규모 클라우드 컴퓨팅 데이터 센터의 모델링 및 시뮬레이션 지원 
	* 어플리케이션 컨테이너의 모델링 및 시뮬레이션 지원
	* 데이터 센터 네트워크 토폴로지 및 메시지 전달 애플리케이션의 모델링 및 시뮬레이션 지원
	* 가상 머신에 대한 호스트 할당을 위한 사용자 정의 정책 및 가상 머신에 대한 호스트 리소스 할당 정책 지원

## CloudSim 성능 지표 메트릭
* 각 VM에서 할당된 CPU와 요청된 CPU의 비율로 SLA metric을 확인
* CloudSim에서 기본으로 출력되는 항목은 container마다의 time, start time, finish time 확인 가능
	* start time : cloudlet의 start time, finish time : cloudlet의 종료 시간, time : 시뮬레이션 동안 걸린 실제 CPU time
	* [참고] Silo에서는 admittance ratio를 사용 -> 75% 또는 90%의 데이터 센터 점유율을 가질 때, tenant의 admitted request(승인된 요청)의 비율을 의미
* CloudSim에서 알고리즘의 효율성을 비교하기 위해 전력 소비, SLA 위반 비율, SLA 위반으로 인한 성능 저하, 활성 호스트 당 SLA 위반 및 가상 머신 마이그레이션 수를 사용함.
### CloudSim 코드 상의 성능 메트릭
#### vm
* TotalUtilization : 컨테이너에서 실행되는 모든 cloudlet(container)의 utilization
#### cloudlet
* cloudletTotalLength : 모든 PE(Processing Element의 약자로 physical machine의 CPU core를 나타냄. 여기에서 CPU core들은 MIPS rating으로 정의됨)들에서의 cloudlet의 길이를 얻음. 각 PE에서 실행되는 cloudletLength를 고려함
* CostPerSec : cloud resource에 대한 초당 비용
* NetServiceLevel : network로 이 cloudlet을 보내는것을 위한 network service level (ToS[이용약관])
* ProcessingCost : cloudlet을 실행하거나 처리하는 전체 cost, Processing cost = input data transfer + processing cost + output transfer data
* SubmissionTime : 최근 CloudResource로부터 cloudlet의 submission(arrival) time
*  Utilization : Ram, bandwidth, CPU의 utilization
	* UtilizationModel : Ram, bandwidth, CPU의 utilization model
* WallClockTime : cloudlet이 최신 cloud resource에 있는 시간을 가져온다. (도착시간부터 출발시간까지)

## VM이나 container placement에서 주로 사용되는 성능 메트릭
* [AlloX] : Average job completion time(mins)
* [KubeSphere] : Fairness(Number of tasks per time),  Total, Average waiting time(s)
* [DC-DRF] : Resource utilization (%)
* [PS-DSF] : Resource utilization (%), average processing time to find allocation (s)
* [Carbyne] : Average job completion time, Cluster efficiency (# Running tasks per time(s))
* [TSF] : Job completion time(s), **Job performace** : Job queueing delay(h),job completion time(h), **Tasks performance** : Task queueing delay(h) and Per-task-speedup(h)
* [HUG] : [Prototype] bandwidth consumption of three tenants arriving over time(total allocation (Gbps) per time(S)),  [Simulation1] Progress(Gbps) of each job/shuffle (fraction of shuffle), Utilization (total allocation(Tbps)),
[Simulation2] 95th percentile slowdown and Average shuffle completion time
* [H-DRF] : [Prototype] Resource sharing per time(s), [Prototype2] Median throughput (tasks running simutaneously), % improvement in Job Response time, [Simulation] % improvement in Job Response time
* [DRF] : CPU, memory and dominant share(%) per time(s) , [Comparison] Number of jobs completed, Average response time(s) of job, [Simulation] CPU and memory utilization(%), Average reduction of completion times(%)

## CloudSim에서 사용 중인 워크로드
* PlanetLab

## CloudSim에 정의된 CPU
* The power model of an IBM server x3550 (2 x [_Xeon_ X5675 3067 MHz, 6 cores], 16GB).
* The power model of an IBM server x3550 (2 x [_Xeon_ X5670 2933 MHz, 6 cores], 12GB).
* The power model of an IBM server x3250 (1 x [_Xeon_ X3480 3067 MHz, 4 cores], 8GB).
* The power model of an IBM server x3250 (1 x [_Xeon_ X3470 2933 MHz, 4 cores], 8GB).
* The power model of an HP ProLiant ML110 G5 (1 x [_Xeon_ 3075 2660 MHz, 2 cores], 4GB).
* The power model of an HP ProLiant ML110 G4 (1 x [_Xeon_ 3040 1860 MHz, 2 cores], 4GB).
* The power model of an HP ProLiant ML110 G3 (1 x [_Pentium_ D930 3000 MHz, 2 cores], 4GB).

## CloudSim의 resource 단위
* RAM : MB, MIPS : 각 PE에 대한 mips, BW : Mb/s

## Yarn과 Mesos의 DRF
### Yarn
* Yarn은 Hadoop에서 사용하는 스케줄러로 capacity scheduler와 fair scheduler를 사용
* fair scheduler는 user마다 각각 queue가 존재하고, 각 queue는 가중치에 따라 리소스를 비율적으로 받을 수 있음. 여기에서 Yarn의 DRF는 각 queue 내부의 리소스 관리를 위해 사용되고 job들의 리소스 할당량을 DRF를 이용하여 할당한다. 
* Yarn은 job에 dominant share만큼 resource를 분배하여 할당 및 실행
### Mesos
* Mesos는 클러스터의 자원을 효율적으로 관리하기 위한 프로젝트로 Mesos master의 allocation module에서 DRF를 사용.
* 각 agent에서 남는 리소스가 생기면, allocation module에게 알리고, 이 allocation module에서 DRF를 이용하여 먼저 자원을 할당할 framework를 선정. framework는 전달받은 자원들 중 각 Job에 필요한 자원을 agent에 알려 task를 실행
* Mesos는 framework에 리소스를 전달하고 job에서 필요로 하는 리소스를 만족시켜야 task 실행

## CloudSim의 컨테이너 스케줄링 시간
* CloudSim에서는 CPU나 메모리가 부족하여 생성되지 못한 컨테이너를 중간에 다시 생성하는 과정은 거치지 않음
* 생성 요청하고 기타 CPU 처리 등의 시간은 MIPS로 계산. 
	* 하지만, CloudSim 상에서 container 배치 시간은 0.2로 고정되어 있어서 생성 요청하고 생성 완료까지 시간을 재는 것은 의미가 없음

## ContainerCloudSim - container환경에서의 CloudSim
* hosts, containers, VM의 population을 특정 지음. container의 population은 cloudlet의 population과 같고 각 cloudlet을 각 container에 매핑시킴 
* Datacenter : 클라우드 서비스의 하드웨어 계층은 데이터 센터 클래스를 통해 모델링
* Host : host class는 물리적 컴퓨팅 리소스 (서버)를 나타냄. 구성은 MIPS, memory, storage 로 표현되는 처리 능력으로 정의됨
* VM : host에 의해 host되고 관리되는 virtual machine. vm의 속성은 memory, processor, storage size로 구성.
* Container : VM에 의해 호스트되는 container, container의 속성은 memory, processor, storage size로 구성
* Cloudlet : cloudlet 클래스는 클라우드 데이터센터의 컨테이너에서 호스팅되는 어플리케이션을 모델링함. Cloudlet 길이는 milion instructions로 정의되며, starttime 및 실행상태 (예 : CANCEL, PAUSED 및 REUSED)를 포함하여 CloudSim 패키지의 이전 기능이 있음. 
* Cloudlet에는 input workload 파일 등을 설정하여 container에서 실행할 input workload를 지정할 수 있음. 
* DatacenterBroker는 vm생성, vm에 cloudlet 제출 및 폐기와 같은 vm을 관리하는 broker 역할을 함.
	* Simulation을 실행할 때 broker는 cloudlet, vm, container, host 리스트를 제출하여 시뮬레이션 실행 
### Cloudlet 상태 
* Success :  cloudlet 실행이 성공적으로 완료되고 최종 상태로 브로커에 다시 보낼 준비가 된 경우 정의된다. 
* Failed : cloudlet이 계획된 클라우드 노드에서 실행되지 못한 경우 정의된다. 예를 들어, 클라우드 렛이 할당될 Vm이 어떤 이유로 인해 cloudlet 실행이 실패하여 시작되지 못한 경우에 정의됨. 
* Failed_Resource_unavailable : 실행될 예정인 리소스의 실패로 인해 cloudlet 실행이 실패한 경우 정의된다. 


## ContainerCloudSim으로 DepCon 시뮬레이션 
* ContainerExample1에서 scheduling policy나 allocation policy 등을 지정하여 datacenter를 구성. 
* policy 클래스를 수정하여 DepCon 스케줄러를 적용하고 세부적인 spec을 변경하여 시뮬레이션 진행 

## ContainerCloudSim에서의 policy 수정방법
* https://www.researchgate.net/post/How-to-design-own-vm-allocation-policy

## ContainerCloudSim 코드 수정 관련
* load balancing과 policy 관련 implementation을 시뮬레이션하기 위해, org.cloudbus.cloudsim package에서 “DatacenterBroker.java”, “CloudletScheduler.java”, “VmAllocationPolicy.java” 등의 클래스를 사용할 수 있음. 
### package org.cloudbus.cloudsim.examples.container.ContainerCloudSimExample1.java
* 주요 코드 설명
	* 각 구성요소에 대한 리스트 생성
```
//The cloudlet list
private static List<ContainerCloudlet> cloudletList;
//The vmlist.
private static List<ContainerVm> vmList;
//The containerlist.
private static List<Container> containerList;
//The hostList.
private static List<ContainerHost> hostList;
       
//eunsook
//The frameworkList
private static List<Framework> frameworkList;
```
* container allocation policy, vm allocation policy, host selection policy 설정
		* 각 policy를 수정하고 싶으면 생성하는 policy를 변경하거나 해당 패키지의 파일에서 policy 코드 수정
		
```
/**
* 2-  Defining the container allocation Policy. This policy determines how Containers are
* allocated to VMs in the data center.
*
*/
ContainerAllocationPolicy containerAllocationPolicy = new PowerContainerAllocationPolicySimple();

/**
* 3-  Defining the VM selection Policy. This policy determines which VMs should be selected for migration
* when a host is identified as over-loaded.
*
*/
PowerContainerVmSelectionPolicy vmSelectionPolicy = new PowerContainerVmSelectionPolicyMaximumUsage();


/**
* 4-  Defining the host selection Policy. This policy determines which hosts should be selected as
* migration destination.
*
*/
//HostSelectionPolicy hostSelectionPolicy = new HostSelectionPolicySerialFit();          
HostSelectionPolicy hostSelectionPolicy = new HostSelectionPolicyFirstFit();
```
* underutilization, overutilization의 기준치를 정함

```
/**
* 5- Defining the thresholds for selecting the under-utilized and over-utilized hosts.
*/
double overUtilizationThreshold = 0.80;
double underUtilizationThreshold = 0.70;	
```
* vmAllocationPolicy 설정

```
/**
* 7- The container allocation policy  which defines the allocation of VMs to containers.
*/
ContainerVmAllocationPolicy vmAllocationPolicy = new PowerContainerVmAllocationPolicyMigrationAbstractHostSelection(hostList, vmSelectionPolicy, hostSelectionPolicy, overUtilizationThreshold, underUtilizationThreshold);
```
* Cloudlet, container, vm list를 broker로 보내기위해 brokerID를 매핑시킨 리스트 생성
		* 아래의 frameworkList는 DRF에서 여러 framework가 자원을 요청하기 때문에 추가한 항목

```
/**
* 9- Creating the cloudlet, container and VM lists for submitting to the broker.
*/
cloudletList = createContainerCloudletList(brokerId, ConstantsExamples.NUMBER_CLOUDLETS);
containerList = createContainerList(brokerId, ConstantsExamples.NUMBER_CLOUDLETS);
vmList = createVmList(brokerId, ConstantsExamples.NUMBER_VMS);
//eunsook
frameworkList = createFrameworkList(ConstantsExamples.NUMBER_FRAMEWORK,ConstantsExamples.NUMBER_VMS,vmList);
```
* broker, host, vm, cloudlet, container를 기반으로 Datacenter 생성
* broker에 cloudlet, container, vm list를 제출 (broker가 생성 및 관리)

```
@SuppressWarnings("unused")
PowerContainerDatacenter e = (PowerContainerDatacenter) createDatacenter("datacenter", PowerContainerDatacenterCM.class, hostList, vmAllocationPolicy, containerAllocationPolicy, getExperimentName("ContainerCloudSimExample-1", String.valueOf(overBookingFactor)), ConstantsExamples.SCHEDULING_INTERVAL, logAddress, ConstantsExamples.VM_STARTTUP_DELAY, ConstantsExamples.CONTAINER_STARTTUP_DELAY, frameworkList);
/**
* 11- Submitting the cloudlet's , container's , and VM's lists to the broker.
*/
broker.submitCloudletList(cloudletList.subList(0, containerList.size()));
broker.submitContainerList(containerList);
broker.submitVmList(vmList);
```
* Simulation 종료 시간, 시작, 정지 등을 정하고 simulation이 끝나면 결과 출력
	* simulation 시간을 정해놓고 그 시간 안에 작업이 다 안끝나도 끝냄 -> failed로 처리하는 것으로 알고 있음 
```
/**
* 12- Determining the simulation termination time according to the cloudlet's workload.
*/
CloudSim.terminateSimulation(86400.00);
/**
* 13- Starting the simualtion.
*/
CloudSim.startSimulation();
/**
* 14- Stopping the simualtion.
*/
CloudSim.stopSimulation();
/**
* 15- Printing the results when the simulation is finished.
*/
List<ContainerCloudlet> newList = broker.getCloudletReceivedList();
printCloudletList(newList);
```

### package org.cloudbus.cloudsim.examples.container.ConstantsExamples.java
* 이 파일에서는 각 contatants에 대한 값을 정할 수 있음
	* Cloudlet Length
	* Container_Startup_Delay, VM_startup_delay : VM과 container의 시작 시간 delay
	* VM_TYPES : 정수 값을 적게 되어있는데 이 TYPE들은 몇 개 종류의 TYPE들을 설정할건지를 나타냄
	* VM_PES, VM_MIPS, VM_RAM, VM_BW, VM_SIZE 의 설정 값 배열을 TYPES 크기 만큼 설정 가능 -> 컨테이너, 호스트, VM 등을 설정할 때의 스펙을 설정
		* CONTAINER, HOST 모두 위와 같은 형식으로 설정할 수 있음 
	* NUM_HOSTS, NUM_VMS, NUMBER_CLOUDLETS, NUMBER_FRAMEWORK (DepCon을 위해 추가) : 각 호스트, VM, cloudlet, framework의 수를 설정

### package org.cloudbus.cloudsim.container.core.Container.java
* core 패키지에서는 container, cloudlet, datacenter, host, storage 등 컨테이너 환경에 핵심이 되는 자원이나 구성요소에 대한 코드들로 구성되어 있음
* DRF 구현을 위해 Container.java에서 각 컨테이너 객체에 추가될 frameworkId라는 변수와 getter, setter를 추가한다. 
	* frameworkId를 추가한 이유는 컨테이너가 framework 별로 할당되기 때문

### package org.cloudbus.cloudsim.container.core.ContainerHost.java
* 데이터센터의 호스트의 정보를 정의, 이용가능한 cpu, memory를 정의해놓음 
* DepCon은 이용가능한 bandwidth도 체크를 하기 때문에 availableBw 항목을 추가
* 컨테이너들은 데이터센터에 이용 가능한 자원이 없으면 더이상 컨테이너를 생성하지 않음

### package org.cloudbus.cloudsim.container.core.ContainerVm.java
* VM의 정보를 정의, host 이름, 현재 할당된 Bw, ram, Mips 등을 정의
* availableBw, availableCpu, availableMem, frameworkID를 추가하여 DepCon와 DRF에서 현재 할당가능한 자원의 양을 확인할 수 있도록 함

### package org.cloudbus.cloudsim.container.core.Framework.java
* framework에 대한 정보를 정의 -> cloudsim에서 제공하지 않고 DRF 알고리즘을 구현하기 위해 추가 
* frameworkId, requestedCPU, requestedMem, cpuShare, memShare, dominantShare, containerList, vmList 로 구성된다. 
* 각 vm에는 하나의 container만 할당하고 이 vm을 container와 동일시하여 컨테이너 하나로 생각하여 계산
* DRF는 각 framework에서 자원 (cpu, memory)에 대한 share(requested / capacity)를 계산하고 비교하여 둘 중 큰 값을 dominant share로 정하고 여러 framework의 dominant share를 비교하여 가장 작은 dominant share를 가진 framework에 자원을 할당함 

### package org.cloudbus.cloudsim.container.core.PowerContainerVmAllocationAbstract.java
* vm 할당 정책에 대해 정의
* allocateHostForVm 함수는 Vm을 할당할 Host와 Vm을 인자로 넣어 할당해주는 함수
* findHostForVm 함수는 Vm을 할당할 Host를 찾는 함수 
* DepCon을 위해 findHostForVm 함수를 수정하여 알고리즘 적용
	* filteringHost 함수에서는 vm에서 요청하는 network bandwidth를 할당할 수 없는 host는 리스트에서 제외함
	* scoringHost 함수에서는 filteringHost에서 생성된 list에서 컨테이너에 할당할 수 있는 cpu가 가장 많은 host를 선택함 

### package org.cloudbus.cloudsim.container.core.PowerContainerVmAllocationPolicy.java
* container 할당 정책에 대한 정의
* DepCon과 DRF를 각각 다른 policy에 정의한 이유는 DepCon은 VM 없이 호스트에 컨테이너 환경과 동일하게 만들어야 했기 때문에 컨테이너와 vm을 1:1로 스펙을 맞추어 동일시하여 할당함, DRF는 framework에 들어있는 container에 대한 정보를 얻기 편하라고 allocateVmForContainer에 구현 
* allocateVmForContainer 함수는 Container를 할당할 Vm와 contianer를 인자로 넣어 할당해주는 함수
* findVmForContainer 함수는 container를 할당할 Vm를 찾는 함수 
* calculateDrf [new DRF] 함수는 framework별로 구성되어 있는 리스트의 항목인 서버의 CPU와 memory를 기반으로 framework의 dominant share를 구하고 가장 작은 dominant share를 가진 framework를 선택하여 framework의 vm을 선택하여 컨테이너를 생성

## 실험 방법 및 간략한 정리
* CloudSim에서 알고리즘 구현 후 System.nanotime이라는 함수를 이용하여 시뮬레이션 시간 측정
* 생성까지의 시간은 합치지 않고 컨테이너 생성 요청 후 Host에 VM을 배치시키는 과정만 배치 시간으로 측정
* 10번씩 실험한 결과의 평균으로 값 측정
* DepCon이 DRF보다 최대 34%의 복잡도 개선을 보여주고, framework의 수 (40, 100, 400)에 따라 34%, 17%, 12%의 스케줄링 오버헤드 개선을 보여줌. 

## DepCon 논문 관련 (자세한 실험 결과 및 분석 있음)
* DepCon 논문 : Paramo workshop 제출 및 accept [학회지 출판 예정]
	* 내년에 revision 후 SCI 출판 예정
* 졸업논문 처리 완료 후 공개 될 예정
* 혹시, 논문의 실험 결과 관련하여 궁금하신 부분이 있으시면 아래의 메일로 연락 부탁드립니다.
	* 이메일 : dkdla58@gmail.com

## 레퍼런스
* CloudSim 홈페이지 : [The CLOUDS Lab: Flagship Projects - Gridbus and Cloudbus](http://www.cloudbus.org/cloudsim/)
* CloudSim GitHub : [Releases · Cloudslab/cloudsim · GitHub](https://github.com/Cloudslab/cloudsim/releases)
* 컨테이너 환경의 CloudSim 논문 : http://www.buyya.com/papers/ContainerCloudSim.pdf
* [효원 참고] : https://arxiv.org/ftp/arxiv/papers/1812/1812.00300.pdf
