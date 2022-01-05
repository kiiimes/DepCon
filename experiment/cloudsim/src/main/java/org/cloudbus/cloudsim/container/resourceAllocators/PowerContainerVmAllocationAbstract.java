package org.cloudbus.cloudsim.container.resourceAllocators;


import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.core.ContainerHost;
import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Created by sareh on 14/07/15.
 */
public abstract  class PowerContainerVmAllocationAbstract extends ContainerVmAllocationPolicy{

        /** The vm table. */
        private final Map<String, ContainerHost> vmTable = new HashMap<String, ContainerHost>();

        /**
         * Instantiates a new power vm allocation policy abstract.
         *
         * @param list the list
         */
        public PowerContainerVmAllocationAbstract(List<? extends ContainerHost> list) {
            super(list);
        }

        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.VmAllocationPolicy#allocateHostForVm(org.cloudbus.cloudsim.Vm)
         */
        @Override
        public boolean allocateHostForVm(ContainerVm containerVm) {
        	
            return allocateHostForVm(containerVm, findHostForVm(containerVm));
        }

        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.VmAllocationPolicy#allocateHostForVm(org.cloudbus.cloudsim.Vm,
         * org.cloudbus.cloudsim.Host)
         */
        @Override
        public boolean allocateHostForVm(ContainerVm containerVm, ContainerHost host) {
            if (host == null) {
                Log.formatLine("%.2f: No suitable host found for VM #" + containerVm.getId() + "\n", CloudSim.clock());
                return false;
            }
            if (host.containerVmCreate(containerVm)) { // if vm has been succesfully created in the host
                getVmTable().put(containerVm.getUid(), host);
                Log.formatLine(
                        "%.2f: VM #" + containerVm.getId() + " has been allocated to the host #" + host.getId(),
                        CloudSim.clock());
                return true;
            }
            Log.formatLine(
                    "%.2f: Creation of VM #" + containerVm.getId() + " on the host #" + host.getId() + " failed\n",
                    CloudSim.clock());
            return false;
        }

        /**
         * Find host for vm.
         *
         * @param containerVm the vm
         * @return the power host
         */
        /*
        public ContainerHost findHostForVm(ContainerVm containerVm) {
            for (ContainerHost host : this.<ContainerHost> getContainerHostList()) {
                if (host.isSuitableForContainerVm(containerVm)) {
                    return host;
                }
            }
            return null;
        }
		*/
        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.VmAllocationPolicy#deallocateHostForVm(org.cloudbus.cloudsim.Vm)
         */
        @Override
        public void deallocateHostForVm(ContainerVm containerVm) {
        	containerVm.getHost().setAvailableBw(containerVm.getHost().getAvailableBw()+containerVm.getBw()); 
        	containerVm.getHost().setAvailableCpu(containerVm.getHost().getAvailableCpu()+containerVm.getNumberOfPes()); 
        	containerVm.getHost().setAvailableMem(containerVm.getHost().getAvailableMem()+containerVm.getRam()); 
        	//containerVm.getVm().setAvailableCpu(container.getVm().getAvailableCpu()+container.getNumberOfPes());
        	//containerVm.getVm().setAvailableMem(container.getVm().getAvailableMem()+container.getRam());  
            ContainerHost host = getVmTable().remove(containerVm.getUid());
            if (host != null) {
                host.containerVmDestroy(containerVm);
            }
        }

        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.VmAllocationPolicy#getHost(org.cloudbus.cloudsim.Vm)
         */
        @Override
        public ContainerHost getHost(ContainerVm vm) {
            return getVmTable().get(vm.getUid());
        }

        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.VmAllocationPolicy#getHost(int, int)
         */
        @Override
        public ContainerHost getHost(int vmId, int userId) {
            return getVmTable().get(ContainerVm.getUid(userId, vmId));
        }

        /**
         * Gets the vm table.
         *
         * @return the vm table
         */
        public Map<String, ContainerHost> getVmTable() {
            return vmTable;
        }

    public List<ContainerVm> getOverUtilizedVms() {
        List<ContainerVm> vmList = new ArrayList<ContainerVm>();
        for (ContainerHost host : getContainerHostList()) {
            for (ContainerVm vm : host.getVmList()) {
                if (vm.getTotalUtilizationOfCpuMips(CloudSim.clock()) > vm.getTotalMips()) {
                    vmList.add(vm);

                }

            }

        }
        return vmList;
    }
    
  //[DepCon] 
    //eunsook
   
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
   
    ////

}