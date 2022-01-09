# 스케줄링 오버헤드 실험 
## 4. DepCon과 DRF의 스케줄링 오버헤드를 비교하는 실험 
### CloudSim 실험
* [CloudSim 관련 내용 및 실험[DepCon scheduling overhead] 정리](https://github.com/kiiimes/DepCon/tree/master/experiment/scheduling_overhead/4_experiment/cloudsim_experiment)

### DepCon 자체 시뮬레이터 실험 
* [DepCon 자체 시뮬레이터 실험 관련 정리](https://github.com/kiiimes/DepCon/tree/master/experiment/scheduling_overhead/4_experiment/simulation)

### 스케줄링 오버헤드 실험 결과 
#### CloudSim 실험 결과
* CloudSim에서 알고리즘 구현 후 System.nanotime이라는 함수를 이용하여 시뮬레이션 시간 측정
* 생성까지의 시간은 합치지 않고 컨테이너 생성 요청 후 Host에 VM을 배치시키는 과정만 배치 시간으로 측정
* 10번씩 실험한 결과의 평균으로 값 측정
* DepCon이 DRF보다 최대 34%의 복잡도 개선을 보여주고, framework의 수 (40, 100, 400)에 따라 34%, 17%, 12%의 스케줄링 오버헤드 개선을 보여줌.
#### 자체 시뮬레이터 실험 결과
* DRF의 스케줄링 오버헤드는 테넌트 수가 200개에서 2000개로 증가함에 따라 스케줄링 오버헤드가 15배 증가
* 반면, DepCon의 스케줄링 오버헤드는 테넌트 수가 오버헤드에 영향을 주지 않음
* DepCon은 서버 수가 100에서 300으로 증가함에 따라 0.072ms에서 0.135ms으로 스케줄링 오버헤드가 1.8배만 증가
	* 서버 수가 300개일 경우에는 DRF가 테넌트 수 2000개, 400개 일 때의 스케줄링 오버헤드보다 DepCon이 스케줄링 오버헤드를 각각 640%, 45% 단축 시킴
