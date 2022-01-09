# DepCon 실험 관련 정리
* DepCon 실험은 기존 Kubernetes와 DepCon 기법을 적용한 Kubernetes 환경에서 네트워크 성능 평가를 진행 
* 추가로 다른 multi-resource scheduling 기법인 DRF와 DepCon의 스케줄링 오버헤드를 비교하는 실험을 진행
* DepCon 실험은 총 4가지 실험을 진행하였음. 
	* 네트워크 성능 평가 
		* [링크]
		1. k8s와 DepCon의 Pod 수 증가에 따른 네트워크 성능 평가 및 비교
			* 2대의 서버에서 300Mbps의 SLO를 요구하는 pod 수를 5, 10, 15, 20개로 증가하여 네트워크 성능 측정
		2. k8s와 DepCon에서 여러 network SLO를 요구하는 pod들의 네트워크 성능 평가 및 비교
			* 4대의 서버에서 100Mbps와 300Mbps의 SLO를 요구하는 Pod를 함께 실행하여 네트워크 성능 측정
		3. k8s와 DepCon에서 더 많은 서버에서 더 다양한 network SLO를 요구하는 pod들의 네트워크 성능 평가 및 비교 
			* 10대의 서버에서 100Mbps, 200Mbps, 300Mbps, 400Mbps의 network SLO를 요구하는 pod를 100개 생성하여 네트워크 성능 측정
	* 스케줄링 오버헤드 평가 
		* [링크]
		1. DepCon과 대표적인 multi-resource scheduling 기법인 DRF의 스케줄링 오버헤드 평가 및 비교
			* 스케줄링 기법 간 비교 진행 

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
### 4번 실험 결과 
#### CloudSim 실험 결과
* CloudSim에서 알고리즘 구현 후 System.nanotime이라는 함수를 이용하여 시뮬레이션 시간 측정
* 생성까지의 시간은 합치지 않고 컨테이너 생성 요청 후 Host에 VM을 배치시키는 과정만 배치 시간으로 측정
* 10번씩 실험한 결과의 평균으로 값 측정
* DepCon이 DRF보다 최대 34%의 복잡도 개선을 보여주고, framework의 수 (40, 100, 400)에 따라 34%, 17%, 12%의 스케줄링 오버헤드 개선을 보여줌. ![21EECCE2-47E2-48AB-9F02-A59AFBADD2F3](https://user-images.githubusercontent.com/28219985/148675661-863c92f7-377f-4920-9d05-2c0f0a9057b0.png)


## DepCon 논문 관련 (자세한 실험 결과 및 분석 있음)
* DepCon 논문 : Paramo workshop 제출 및 accept [학회지 출판 예정]
	* 내년에 revision 후 SCI 출판 예정
* 졸업논문 처리 완료 후 공개 될 예정
* 혹시, 논문의 실험 결과 관련하여 궁금하신 부분이 있으시면 아래의 메일로 연락 부탁드립니다.
	* 이메일 : dkdla58@gmail.com 

## 레퍼런스
* Kubernetes : https://github.com/kubernetes/kubernetes
* DRF : https://cs.stanford.edu/~matei/papers/2011/nsdi_drf.pdf





