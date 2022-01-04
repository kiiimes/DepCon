CPU = 0
BW = 1
NUM_RESOURCE = 2

#framework 요청 리소스 포함 필요 

def drf(frameworkList,nodeList,capacityList):    
    selectedContainer = calculateShare(frameworkList,nodeList,capacityList)

    if not selectedContainer:
        print("There is no selectedContainer.")
        return None
    else:
        selectedNode = selectNode(selectedContainer,nodeList)

    #print("? : ",selectedNode)
    if selectedNode is None:
        print("There is no available Node.")
        return [None,selectedContainer]
    return [selectedNode,selectedContainer]
    

def calculateShare(frameworkList,nodeList,capacityList):
    dominantFramework = 0
    dominantShare = 1

    for framework in frameworkList:
        #print("frameworkList[framework] : ",frameworkList[framework])
        if (frameworkList[framework][0][0] <= dominantShare) and frameworkList[framework][1]:
            dominantFramework = framework
            dominantShare = frameworkList[framework][0][0]

    #print("------------------------------------------------")

    #dominantShare 구하기
    maxShare = 0
    for i in range(0,NUM_RESOURCE):
        #print("resource : ",float(frameworkList[dominantFramework][1][i]))
        #print("before : ",float(frameworkList[dominantFramework][0][0]))
        
        #print("frameworkList[dominantFramework] : ",frameworkList[dominantFramework])
        #print("i : ",i)        
        share = float(frameworkList[dominantFramework][0][i+1]) + float(frameworkList[dominantFramework][1][i])/float(capacityList[i])
        frameworkList[dominantFramework][0][i+1] = share
        if maxShare < share:
            maxShare = share

    frameworkList[dominantFramework][0][0] = maxShare


    #dominantResource 결정
    '''
    if frameworkList[dominantFramework][0][1] >= frameworkList[dominantFramework][0][2]:
        frameworkList[dominantFramework][0][0] = frameworkList[dominantFramework][0][1]
    else:
        frameworkList[dominantFramework][0][0] = frameworkList[dominantFramework][0][2]
    '''

    #print("update : ",frameworkList[dominantFramework][0])

    selectedContainer = frameworkList[dominantFramework][2][0]
    #print("selectedContainer : ",selectedContainer)

    del frameworkList[dominantFramework][2][0]
    #print("selectedContainer : ",frameworkList[dominantFramework][2])
    return selectedContainer


def selectNode(container,nodeList):
    selectedNode = {}
    count = 0
    #print("selectNode의 selectedContainer : ",container)
    for node in nodeList:
        if (nodeList[node][CPU] >= container[CPU]) and (nodeList[node][BW] >= container[BW]):
            selectedNode[node] = nodeList[node]
    if not selectedNode:
        return None
    #print("select? : ",selectedNode)
    return selectedNode 

    


if __name__ == '__main__':
    selected = drf({0:[[0,0,0],[5,10],[[5,10],[1,100],[3,1000]]],1:[[0,0,0],[5,10],[[5,10],[1,100],[3,1000]]]},{0:[10,10000],1:[10,10000],2:[10,10000],3:[10,10000]},[40,40000])
    print("selected : ",selected)
