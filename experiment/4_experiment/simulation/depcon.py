CPU = 0
BW = 1

def depcon(container,nodeList):
    availableList = filtering(container,nodeList)

    if not availableList:
        print("There is no availableList.")
        return None
    else:
        selectedNode = scoring(container,availableList)

    #print("selectedNode : ",selectedNode)

    return selectedNode


def filtering(container,nodeList):
    filteredList = {}

    for node in nodeList:
        if (container[BW] <= nodeList[node][BW]) and (container[CPU] <= nodeList[node][CPU]):
            filteredList[node] = nodeList[node]

    #print("filteredList : ",filteredList)
    return filteredList
    
def scoring(container,availableList):
    selectedKey = list(availableList)[0]
    selectedValue = availableList[list(availableList)[0]]
    
    for node in availableList:
        if availableList[node][CPU] > selectedValue[CPU]:
            selectedKey = node
            selectedValue = availableList[node]
            
    selectedNode = {selectedKey:selectedValue}

    return selectedNode

if __name__ == '__main__':
    depcon([5,100],{0:[1,10],1:[1,10000],2:[5,10000],3:[3,10000]})

