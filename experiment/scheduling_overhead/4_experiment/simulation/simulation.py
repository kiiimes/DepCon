import depcon
import random
import drf
import time

CPU = 0
BW = 1
#요청 리소스 랜덤, 노드도 랜덤
# CPU:5~10,BW:

numOfNode = int(input("number of node : "))
numOfContainer = int(input("number of container : "))
schedulingType = input("scheduling type (1.DepCon, 2. DRF) : ")

nodeList = {}
containerList = []
capacityList = [0,0]

frameworkList = {}

#node 리소스 설정
for i in range(numOfNode):
    #node에도 랜덤으로 값 설정?
    '''
    availableCpu = random.randrange(5,10)
    availableBw = random.randrange(5000,10000)
    '''
    #CPU : 10, BW : 10Gbit 설정
    nodeList[i] = [10,10000]

#랜덤으로 요청 CPU, BW 설정
for i in range(numOfContainer):
    availableCpu = random.randrange(1,3)
    availableBw = random.randrange(50,101)
    containerList.append([availableCpu,availableBw])

#print("nodeList : ",nodeList)
#print("containerList : ",containerList)

if schedulingType == "1":    
    total = 0
    for container in containerList:
        start = time.time() 
        selectedNode = depcon.depcon(container,nodeList)
        total += (time.time()-start)
        if not selectedNode:
            print("no more availableList")
            break
        key = list(selectedNode)[0]
        nodeList[key][CPU] -= container[CPU]
        nodeList[key][BW] -= container[BW]
        #print("nodeList! : ",nodeList)
        #print("total time : ",(time.time()-start)*1000)
    print("total time : ",total)
    print("average time : ",total/numOfContainer*1000)
    
        
if schedulingType == "2":
    tmp = -1
    numOfFramework = int(input("number of framework : "))

    for node in nodeList:
        for i in range(2):
            capacityList[i] += nodeList[node][i]
    print("capacity : ",capacityList)

    for i in range(numOfFramework):
        frameworkList[i] = [[0,0,0],[0,0],[]]
        for j in range(int(numOfContainer/numOfFramework)):
            frameworkList[i][2].append(containerList[j+(i*int(numOfContainer/numOfFramework))])
        #print("frameworkList : ", frameworkList[i])
    total = 0
    for container in containerList: 
        for i in frameworkList:
            if frameworkList[i][2]:
                frameworkList[i][1]=frameworkList[i][2][0]
            else:
                frameworkList[i][1]=[]
            #if not frameworkList[i][2]:
            #    tmp = i
            #else:
            #    frameworkList[i][1] = frameworkList[i][2][0]
                #print("frameworkList1!!! : ",frameworkList[i])
        #if tmp != -1:
        #    del frameworkList[tmp]
        #    tmp = -1
        
        start = time.time()    
        [selectedNode,selectedContainer] = drf.drf(frameworkList,nodeList,capacityList)
        total += (time.time()-start)
        if not selectedNode:
            print("no more availableList")
            break    
        key = list(selectedNode.keys())[0]
        nodeList[key][CPU] -= selectedContainer[CPU]
        nodeList[key][BW] -= selectedContainer[BW]
        #print("nodeList! : ",nodeList)
        #print("interval : ",(time.time()-start)*1000)
    print("total time : ",total)
    print("average time : ",total/numOfContainer*1000)
       


'''
for i in range(num_of_container):

    availableList = filtering(num_of_node)

    if availableList == null:
        return null
    else
        selectedHost = scoring(i,availableList)

''' 

    
        
    

'''
List<ContainerHost> availableHostList=filteringHost(containerVm);
    	ContainerHost selectedHost;
    	
    	if (availableHostList == null) {
    		return null;
    	}else {
    		selectedHost = scoringHost(containerVm, availableHostList);
    	}
    	
    	return selectedHost;




a[num_of_container]=[0]

def filtering(num_of_node):
    for j in range(num_of_node):
        if container[j][1] < node[j][1]:
            fileredList[j]=node[j]

        if filteredList.isEmpty():
            return null

        return filteredList

def scoring(filteredList):
    C



ContainerHost selectedHost = availableHostList.get(0);
    	
        for (ContainerHost containerHost : availableHostList) {
        	if (containerHost.getAvailableCpu() > selectedHost.getAvailableCpu()){
        		selectedHost = containerHost;
        	}else if (containerHost.getAvailableCpu() == selectedHost.getAvailableCpu()) {
        		if (containerHost.getAvailableMem() > selectedHost.getAvailableMem()) {
        			selectedHost = containerHost;
        		}
        	}
        }
        
        return selectedHost;    

for i in range(num_of_container):

    #filtering
    filtering(num_of_node)
    
    #scoring
    for k in range(fileringList.size()):
        
    
public ContainerHost findHostForVm(ContainerVm containerVm) {
    	List<ContainerHost> availableHostList=filteringHost(containerVm);
    	ContainerHost selectedHost;
    	
    	if (availableHostList == null) {
    		return null;
    	}else {
    		selectedHost = scoringHost(containerVm, availableHostList);
    	}
    	
    	return selectedHost;
    	
    }
  
   ///
    
  //eunsook
    public List<ContainerHost> filteringHost(ContainerVm containerVm) {
    	List<ContainerHost> filteredHostList=new ArrayList<ContainerHost>();
    	
    	
        for (ContainerHost containerHost : getContainerHostList()) {
        	//Log.printLine("containerVmUiD : " + containerVm.getUid()+"bw! : " + containerVm.getAvailableBw());
        	//Log.printLine("containerVm : " + containerVm.getAvailableBw());
        	if (containerHost.isSuitableForContainerVm(containerVm)&&(containerHost.getAvailableBw() > containerVm.getBw())){
        		filteredHostList.add(containerHost);
        	}
        }
       // Log.printLine("filtering" + filteredVmList);
        if (filteredHostList.isEmpty()) {
        	return null;
        }
        return filteredHostList;
    }
    /////

  //eunsook
    public ContainerHost scoringHost(ContainerVm containerVm, List<ContainerHost> availableHostList) {
    	ContainerHost selectedHost = availableHostList.get(0);
    	
        for (ContainerHost containerHost : availableHostList) {
        	if (containerHost.getAvailableCpu() > selectedHost.getAvailableCpu()){
        		selectedHost = containerHost;
        	}else if (containerHost.getAvailableCpu() == selectedHost.getAvailableCpu()) {
        		if (containerHost.getAvailableMem() > selectedHost.getAvailableMem()) {
        			selectedHost = containerHost;
        		}
        	}
        }
        
        return selectedHost;
    }
'''

"""
#container creation
for i in range(container_creation):
    #DepCon
    if num_scheduling == 1:
        for j in range(num_of_node):
            if 
            

    #DRF
    elif num_scheduling == 2:
"""
