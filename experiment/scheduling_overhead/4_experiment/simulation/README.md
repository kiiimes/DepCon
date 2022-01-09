# DepCon 자체 시뮬레이터 실험 관련 정리
* DepCon과 멀티 리소스 스케줄링의 대표적인 기법인 DRF의 컨테이너 스케줄링 오버헤드 비교를 위해 대규모 클라우드 환경에서 시뮬레이션이 필요
* CloudSim에서 생성할 수 있는 컨테이너 수가 한계가 있어 더 많은 수의 컨테이너 환경에서 오버헤드 측정을 위해 자체 시뮬레이터를 이용하여 실험 진행
* Python으로 DepCon과 DRF의 스케줄링 알고리즘을 구성하고 두 알고리즘의 스케줄링 완료 시간의 차이가 얼마나 나는지 실행 시간을 측정함.

## 시뮬레이터 설명
* 시뮬레이터는 서버들의 CPU와 network capacity를 코드 상에서 정하고 Node 수, Container 수, scheduling Type (DepCon, DRF)을 입력 받아 스케줄링 오버헤드를 출력하는 프로그램.

### 시뮬레이터 실행 방법
* 디렉토리 : simulation/simulation.py
* 기본적인 서버들의 CPU와 network capacity는 10 cores CPU, 10Gbit Ethernet으로 설정되어 있음.
	* 설정 값을 바꾸고 싶으면 아래의 파일별 설명을 참고
* simulation.py를 실행시키면 아래와 같이 프롬프트가 뜸
![image](https://user-images.githubusercontent.com/28219985/148676491-fae731cc-1eaa-4d12-ab95-4659fe940d47.png)
* 입력
	* number of node : node들의 수 
	* number of container : container들의 수
	* scheduling type : 1번은 DepCon에 대한 scheduling overhead, 2번은 DRF에 대한 scheduling overhead를 계산
	* number of framework : (scheduling type으로 2. DRF를 선택한 경우) DRF는 컨테이너에 필요한 자원을 각 framework 에서 요청하므로 framework의 수 입력 필요
* 출력
	* total time : 전체 스케줄링 오버헤드
	* average time : 평균 스케줄링 오버헤드

### 시뮬레이터 파일별 설명
* simulation.py : 전체 simulation을 실행하기 위한 main 코드
	* 수정하여 사용할 수 있는 부분은 다음과 같다. 
```
for i in range(numOfContainer):
    availableCpu = random.randrange(1,3)
    availableBw = random.randrange(50,101)
    containerList.append([availableCpu,availableBw])
```
* 각 컨테이너에서 요청하는 자원은 1이상 3 미만의 CPU와 50이상 101 미만의 network로 랜덤하게 요청하도록 설정하였는데 이 범위를 필요에 따라 수정해서 사용 가능
	
```
for i in range(numOfNode):
	nodeList[i]=[10,10000]
```
* 모든 서버를 10cores CPU, 10Gbit network 로 설정하였는 데 필요에 따라 수정 가능

* drf.py : drf의 스케줄링 알고리즘과 관련된 코드
* depcon.py : depcon의 스케줄링 알고리즘과 관련된 코드

## 실험 방법
* 시뮬레이터를 이용하여 컨테이너 수를 2000개로 고정하고, 노드 수를 100, 200, 300 개로 증가를 하여 실험 진행
	* DRF의 경우에는 framework(테넌트) 수를 200개, 400개, 2000개로 증가하여 실험 진행

## 간략한 실험 결과
* DRF의 스케줄링 오버헤드는 테넌트 수가 200개에서 2000개로 증가함에 따라 스케줄링 오버헤드가 15배 증가
* 반면, DepCon의 스케줄링 오버헤드는 테넌트 수가 오버헤드에 영향을 주지 않음
* DepCon은 서버 수가 100에서 300으로 증가함에 따라 0.072ms에서 0.135ms으로 스케줄링 오버헤드가 1.8배만 증가
	* 서버 수가 300개일 경우에는 DRF가 테넌트 수 2000개, 400개 일 때의 스케줄링 오버헤드보다 DepCon이 스케줄링 오버헤드를 각각 640%, 45% 단축 시킴

## DepCon 논문 관련 (자세한 실험 결과 및 분석 있음)
* DepCon 논문 : Paramo workshop 제출 및 accept [학회지 출판 예정]
	* 내년에 revision 후 SCI 출판 예정
* 졸업논문 처리 완료 후 공개 될 예정
* 혹시, 논문의 실험 결과 관련하여 궁금하신 부분이 있으시면 아래의 메일로 연락 부탁드립니다.
	* 이메일 : dkdla58@gmail.com 

## 레퍼런스
* Kubernetes : https://github.com/kubernetes/kubernetes
* DRF : https://cs.stanford.edu/~matei/papers/2011/nsdi_drf.pdf
