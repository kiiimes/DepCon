package org.cloudbus.cloudsim.container.resourceAllocators;

import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
//eunsook
import org.cloudbus.cloudsim.container.core.Framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sareh on 16/07/15.
 */
public abstract class PowerContainerAllocationPolicy extends ContainerAllocationPolicy{

        /** The container table. */
        private final Map<String, ContainerVm> containerTable = new HashMap<>();
        

        /**
         * Instantiates a new power vm allocation policy abstract.
         *
         */
        public PowerContainerAllocationPolicy() {
            super();
        }
        
        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.VmAllocationPolicy#allocateHostForVm(org.cloudbus.cloudsim.Vm)
         */
        
        @Override
        //DepCon 용
        public boolean allocateVmForContainer(Container container, List<ContainerVm> containerVmList) {
            setContainerVmList(containerVmList); 
            return allocateVmForContainer(container, findVmForContainer(container));
        }
        
        /* eunsook
         * DRF 용
        @Override
        public boolean allocateVmForContainer(List<ContainerVm> containerVmList, List<Framework> frameworkList) {
        	//Log.printLine("eunsookkkkkkkk");
        	Container container;
        	
        	container = calculateDrf(frameworkList,10000,10000);
        	setContainerVmList(containerVmList); 
            return allocateVmForContainer(container, findVmForContainer(container));
        }
        */
        
        /*
        @Override
        public boolean allocateVmForContainer(List<ContainerVm> containerVmList,List<Container> containerList) {
        	//Log.printLine("eunsookkkkkkkk");
            setContainerVmList(containerVmList);
            getContainerVmList();
            getContainerList();
            return allocateVmForContainer(dominantContainer, findVmForContainer(containerList));
        }
        */

        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.VmAllocationPolicy#allocateHostForVm(org.cloudbus.cloudsim.Vm,
         * org.cloudbus.cloudsim.Host)
         */
        //[eunsook]isSuitableForContainer에 알고리즘 작성?혹은 이 allocateVmForContainer?
        @Override
        public boolean allocateVmForContainer(Container container, ContainerVm containerVm) {
            if (containerVm == null) {
                Log.formatLine("%.2f: No suitable VM found for Container#" + container.getId() + "\n", CloudSim.clock());
                return false;
            }
            if (containerVm.containerCreate(container)) { // if vm has been succesfully created in the host
                getContainerTable().put(container.getUid(), containerVm);
//                container.setVm(containerVm);
                Log.formatLine(
                        "%.2f: Container #" + container.getId() + " has been allocated to the VM #" + containerVm.getId(),
                        CloudSim.clock());
                return true;
            }
            Log.formatLine(
                    "%.2f: Creation of Container #" + container.getId() + " on the Vm #" + containerVm.getId() + " failed\n",
                    CloudSim.clock());
            return false;
        }
 
        /**
         * Find host for vm.
         *
         * @param container the vm
         * @return the power host
         */
        
        public ContainerVm findVmForContainer(Container container) {
        	for (ContainerVm containerVm : getContainerVmList()) { 
                //Log.printConcatLine("Trying vm #",containerVm.getId(),"For container #", container.getId());
                if (containerVm.isSuitableForContainer(container)) {
                    return containerVm;
                }
            }
            return null;
        }
       
        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.VmAllocationPolicy#deallocateHostForVm(org.cloudbus.cloudsim.Vm)
         */
        @Override
        public void deallocateVmForContainer(Container container) {
        	//eunsook
        	//container.getVm().setAvailableBw(container.getVm().getAvailableBw()+container.getBw());
        	//container.getVm().setAvailableCpu(container.getVm().getAvailableCpu()+container.getNumberOfPes());
        	//container.getVm().setAvailableMem(container.getVm().getAvailableMem()+container.getRam());        	
        	ContainerVm containerVm = getContainerTable().remove(container.getUid());
            if (containerVm != null) {
                containerVm.containerDestroy(container);
            }
        }

        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.VmAllocationPolicy#getHost(org.cloudbus.cloudsim.Vm)
         */
        @Override
        public ContainerVm getContainerVm(Container container) {
            return getContainerTable().get(container.getUid());
        }

        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.VmAllocationPolicy#getHost(int, int)
         */
        @Override
        public ContainerVm getContainerVm(int containerId, int userId) {
            return getContainerTable().get(Container.getUid(userId, containerId));
        }

        /**
         * Gets the vm table.
         *
         * @return the vm table
         */
        public Map<String, ContainerVm> getContainerTable() {
            return containerTable;
        }

        //[new DRF] - 최종버
        //eunsook
       public Container calculateDrf(List<Framework> frameworkList,double totalCpu, double totalRam) throws InterruptedException {
    	   Container container = null;
    	   int dominantFramework = 0;
    	   double dominantShare = frameworkList.get(0).getDominantShare(), cpuShare = 0, memShare = 0;
    	   
    	   for (Framework framework : frameworkList) {
    		   if (framework.getDominantShare() < dominantShare) {
    			   dominantFramework = framework.getFrameworkId();
    			   dominantShare = framework.getDominantShare();
    		   }
    	   }   
    	   cpuShare = frameworkList.get(dominantFramework).getCpuShare() + (frameworkList.get(dominantFramework).getRequestedCpu()/totalCpu);
    	   memShare = frameworkList.get(dominantFramework).getMemShare() + (frameworkList.get(dominantFramework).getRequestedMem()/totalRam);
    	   
    	   Log.printLine("cpuSHare : " + cpuShare);
    	   Log.printLine("memSHare : " + memShare);
    	   
    	   frameworkList.get(dominantFramework).setCpuShare(cpuShare);
    	   frameworkList.get(dominantFramework).setMemShare(memShare);
    	   
    	   if (cpuShare <= memShare) {
    		   frameworkList.get(dominantFramework).setDominantShare(cpuShare);
    	   }else {
    		   frameworkList.get(dominantFramework).setDominantShare(memShare);
    	   }
    	   
    	   container = frameworkList.get(dominantFramework).getContainerList().get(0);
    	   frameworkList.get(dominantFramework).getContainerList().remove(0);
    	   
    	   return container;
    	   
    		 /*  
    		   
    		   if (framework.getDominantShare()==0) {
    			   dominantFramework = framework.getFrameworkId();
    			   cpuShare = framework.getCpuShare() + (framework.getRequestedCpu()/totalCpu);
    			   memShare = framework.getMemShare() + (framework.getRequestedMem()/totalRam);
    			   
    			   if(cpuShare >= memShare) {
        			   dominantShare = cpuShare;
    			   }else {
    				   dominantShare = memShare;
    			   }

    			   break;
    		   }else {
    			   if (framework.getFrameworkId()!=dominantFramework)
    			   cpuShare = framework.getCpuShare() + (framework.getRequestedCpu()/totalCpu);
    			   memShare = framework.getMemShare() + (framework.getRequestedMem()/totalRam);
    			   
    			   if(cpuShare >= memShare) {
        			   tempShare = cpuShare;
    			   }else {
    				   tempShare = memShare;
    			   }
    			   
    			   if (tempShare < dominant) {
    				   dominant = tempShare;
    			   }
    			   
    		   }
    		   
    	   }
    	   
    	   
    	   frameworkList.get(dominantFramework).setCpuShare(cpuShare);
    	   frameworkList.get(dominantFramework).setMemShare(memShare);
    	   frameworkList.get(dominantFramework).setDominantShare(dominant);
    	   
    	   container = frameworkList.get(dominantFramework).getContainerList().get(0);
    	   frameworkList.get(dominantFramework).getContainerList().remove(0);
    	   
    	   return container;
    	   */
       }
        
        
//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //[DRF 1] VM을 tenant라고 생각하는 경우 
        //eunsook
        /*
        public ContainerVm findVmForContainer(Container container) {
        	double cpuShare, memShare;
        	
        	for (ContainerVm containerVm : getContainerVmList()) {
        		Log.printLine("vm : "+ containerVm.getUid());
        		
        		if (containerVm.getDominantShare() == 0 && containerVm.getId()==1) {
        			cpuShare = containerVm.getCpuShare() + (containerVm.getAvailableCpu()/(double)containerVm.getNumberOfPes()); 
            		memShare = containerVm.getMemShare() + ((double)container.getRam()/ (double)containerVm.getRam());            		
            		if (cpuShare >= memShare) {
            			containerVm.setDominantShare(cpuShare);
            		}else {
            			containerVm.setDominantShare(memShare);
            		}
            		
            		dominantShare = containerVm.getDominantShare();
        			dominantVm = containerVm;
        			
        			Log.printLine("cpuShare : " + cpuShare);
        			Log.printLine("memShare : " + memShare);
        			Log.printLine("dominantShare : " + dominantShare);
        			Log.printLine("--------------------------------");
        			
        			break;
        		}else{
        			cpuShare = containerVm.setCpuShare(container.getNumberOfPes(),containerVm.getNumberOfPes()); // 70000이 부분에 vm들의 capacity합 들어가야됨
            		memShare = containerVm.setMemShare(container.getRam(), containerVm.getRam()); // 512 이 부분에 vm들의 capacity합 들어가야됨
            		
            		if (cpuShare >= memShare) {
            			containerVm.setDominantShare(cpuShare);
            		}else {
            			containerVm.setDominantShare(memShare);
            		}
            		Log.printLine("cpuShare : " + cpuShare);
        			Log.printLine("memShare : " + memShare);
            		if (containerVm.getDominantShare() < dominantShare ) {
            			dominantShare = containerVm.getDominantShare();
            			dominantVm = containerVm;
            		}

        			Log.printLine("dominantShare : " + dominantShare);
        			Log.printLine("--------------------------------");
        		}
        		
        		
        		if (dominantVm.isSuitableForContainer(container)) {
                	Log.printLine(containerVm);
                    return containerVm;
                }
                
            }
            return dominantVm;
        }
        */
        
        //[DRF 2] kube-batch와 같은 방식 
        //eunsook
        /*
        public ContainerVm findVmForContainer(List<Container> containerList) {
        	//cpu share, memory share
        	double cpuShare, memShare;
        	
        	//vm 전체 capacity 
        	for (Container container : containerList) {
        		cpuShare = container.setCpuShare(container.getNumberOfPes(),700000); // 70000이 부분에 vm들의 capacity합 들어가야됨
        		memShare = container.setMemShare(container.getRam(), 512); // 512 이 부분에 vm들의 capacity합 들어가야됨
        		
        		if (cpuShare >= memShare) {
        			container.setDominantShare(cpuShare);
        		}else {
        			container.setDominantShare(memShare);
        		}
        		
            }
        	
        	Collections.sort(containerList, new Comparator<Container>() {
                public int compare(Container o1, Container o2) {
                    if(o1.getDominantShare() > o2.getDominantShare()) {
                        return 1;
                    }else if(o1.getDominantShare() < o2.getDominantShare()) {
                        return -1;
                    }else {
                        return 0;
                    }
                }
            });
       
        	for (Container container : containerList) {
	        	for (ContainerVm containerVm : getContainerVmList()) {
	                //Log.printConcatLine("Trying vm #",containerVm.getId(),"For container #", container.getId());
	                if (containerVm.isSuitableForContainer(container)) {
	                	Log.printLine(containerVm);
	                    return containerVm;
	                }
	            }
        	}
            return null;
        	
        }
        */
        /////
        
      //[DRF 3] kube-batch와 같은 방식은 맞지만 containerList를 한번씩 더 돌면서 실행 
        //eunsook
        /*
        public ContainerVm findVmForContainer(List<Container> containerList) {
        	//cpu share, memory share
        	double cpuShare, memShare;
        	List<Container> availableList = containerList;
        	
        	
        	//vm 전체 capacity 
        	for (Container container : availableList) {
        		cpuShare = container.setCpuShare(container.getNumberOfPes(),700000); // 70000이 부분에 vm들의 capacity합 들어가야됨
        		memShare = container.setMemShare(container.getRam(), 512); // 512 이 부분에 vm들의 capacity합 들어가야됨
        		
        		if (cpuShare >= memShare) {
        			container.setDominantShare(cpuShare);
        		}else {
        			container.setDominantShare(memShare);
        		}
        		
        		if (container.getDominantShare() < dominantShare || dominantShare==0) {
        			dominantShare = container.getDominantShare();
        		}
            }
        	
        	
	        	for (ContainerVm containerVm : getContainerVmList()) {
	                //Log.printConcatLine("Trying vm #",containerVm.getId(),"For container #", container.getId());
	                if (containerVm.isSuitableForContainer(dominantContainer)) {
	                	Log.printLine(containerVm);
	                    return containerVm;
	                }
	            }
        	
            return null;
        	
        }
        */
        /////
        
        //[DepCon] 
        //eunsook
       /*
        public ContainerVm findVmForContainer(Container container) {
        	List<ContainerVm> availableVmList=filteringVm(container);
        	ContainerVm selectedVm;
        	
        	if (availableVmList == null) {
        		return null;
        	}else {
        		selectedVm=scoringVm(container,availableVmList);
        	}
        	
        	return selectedVm;
        }
        /////
        
        //eunsook
        public List<ContainerVm> filteringVm(Container container) {
        	List<ContainerVm> filteredVmList=new ArrayList<ContainerVm>();
        	
        	
            for (ContainerVm containerVm : getContainerVmList()) {
            	//Log.printLine("containerVmUiD : " + containerVm.getUid()+"bw! : " + containerVm.getAvailableBw());
            	//Log.printLine("containerVm : " + containerVm.getAvailableBw());
            	if (containerVm.isSuitableForContainer(container)&&(containerVm.getAvailableBw() > container.getBw())){
            		filteredVmList.add(containerVm);
            	}
            }
           // Log.printLine("filtering" + filteredVmList);
            if (filteredVmList.isEmpty()) {
            	return null;
            }
            return filteredVmList;
        }
        /////
        
        //eunsook
        public ContainerVm scoringVm(Container container, List<ContainerVm> availableVmList) {
        	ContainerVm selectedVm = availableVmList.get(0);
        	
            for (ContainerVm containerVm : availableVmList) {
            	//Log.printLine("vm cpu : " + containerVm.getAvailableCpu());
            	//Log.printLine("vm Ram : " + containerVm.getAvailableBw());
            	//Log.printLine("container cpu " + selectedVm.getAvailableCpu());
            	if (containerVm.getAvailableCpu() > selectedVm.getAvailableCpu()){
            		selectedVm = containerVm;
            	}else if (containerVm.getAvailableCpu() == selectedVm.getAvailableCpu()) {
            		if (containerVm.getAvailableMem() > selectedVm.getAvailableMem()) {
            			selectedVm = containerVm;
            		}
            	}
            }
            
            return selectedVm;
        }
       */
        ////
	}





