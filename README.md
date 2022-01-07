# DepCon 관련 내용 정리
## DepCon이란?
* DepCon은 컨테이너 환경에서 network SLO를 달성하기 위해 컨테이너의 CPU 자원을 제어하는 기법이다.
* 컨테이너는 클라우드에서 분산 방식으로 실행되고, 컨테이너에 대한 네트워크 SLO를 충족하는 것이 중요하다. 또한, 컨테이너의 네트워킹 스택이 길기 때문에 일반 프로세스보다 네트워크 처리에 더 많은 CPU 자원을 사용한다. 따라서, 네트워크 SLO를 달성하기 위해서는 충분한 CPU, network 자원이 필요하며 이를 자원 간 종속성이라고한다. 하지만, 기존 클라우드 스케줄러는 네트워크 SLO에 대한 CPU를 고려하지 않는다는 점에서 한계가 있어 이를 만족하기 위한 기법을 고안하였다. 
* DepCon은 클라우드 수준에서 작동하는 DepCon scheduler와 각 노드의 DepCon agent로 구성되고, 이를 가장 인기 있는 컨테이너 오케스트레이션 플랫폼인 Kubernetes에서 구현하였다. 

## DepCon 구현 및 실행 내용 정리
[DepCon 구현 및 실행 내용 정리](https://github.com/kiiimes/DepCon/tree/master/implementation#depcon-%EA%B5%AC%ED%98%84-%EB%B0%8F-%EC%8B%A4%ED%96%89-%EB%82%B4%EC%9A%A9-%EC%A0%95%EB%A6%AC)

## DepCon 실험 방법 및 결과 요약
* [[DepCon 실험 관련 정리]]



