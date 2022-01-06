package org.cloudbus.cloudsim.container.core;

import org.cloudbus.cloudsim.container.schedulers.ContainerCloudletScheduler;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.util.MathUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by eunsook on 9/07/15.
 */
public class Framework {
	
	private int frameworkId;
	
	private double requestedCpu;
	
	private double requestedMem;
	
    private double cpuShare;
    
    private double memShare;
    
    private double dominantShare;
    
    private List<Container> containerList = new ArrayList<>();
    
    //eunsook
    private List<ContainerVm> vmList = new ArrayList<>();
    
    /**
     * 
     * Creates a new Framework object.
     * @param frameworkId
     * @param requestedCpu;
     * @param requestedMem;
     * @param cpuShare;
     * @param memShare;
     * @param dominantShare;
     */
    public Framework(
            int frameworkId,
            double requestedCpu,
            double requestedMem,
            double cpuShare,
            double memShare,
            double dominantShare) {
        //eunsook
    	setFrameworkId(frameworkId);
    	setRequestedCpu(requestedCpu);
    	setRequestedMem(requestedMem);
        setCpuShare(0);
        setMemShare(0);
        setDominantShare(0);
    }

    public int getFrameworkId() {
    	return frameworkId;
    }
    
    protected void setFrameworkId(int frameworkId) {
    	this.frameworkId = frameworkId;
    }
    
    public double getRequestedCpu() {
		return requestedCpu;
	}

	public void setRequestedCpu(double requestedCpu) {
		this.requestedCpu = requestedCpu;
	}

	public double getRequestedMem() {
		return requestedMem;
	}

	public void setRequestedMem(double requestedMem) {
		this.requestedMem = requestedMem;
	}

	public double getCpuShare() {
    	return cpuShare;
    }
	
	public void setCpuShare(double cpuShare) {
		this.cpuShare = cpuShare;
	}
	
    /*
    public double setCpuShare(double requested, double capacity) {
    	if (requested == 0 && capacity == 0) {
    		this.cpuShare = 0;
    	}else{
    		this.cpuShare += (double) requested / capacity;

    	}
    	return this.cpuShare;
    }
    */
	
    public double getMemShare() {
    	return memShare;
    }
    
    public void setMemShare (double memShare) {
    	this.memShare = memShare;
    }
    /*
    public double setMemShare(double requested, double capacity) {
    	if (requested == 0 && capacity == 0) {
    		this.memShare = 0;
    	}else{
    		this.memShare += (double) requested / capacity;
    	}
    	return this.memShare;
    }
    */
    public double getDominantShare() {
    	return dominantShare;
    }
    
    public void setDominantShare(double dominantShare) {
    	this.dominantShare = dominantShare;
    }
   
    public List<Container> getContainerList(){
    	return containerList;
    }
    
    public void setContainerList(Container container) {
    	this.containerList.add(container);
    }
    
    public List<ContainerVm> getVmList(){
    	return vmList;
    }
    
    public void setVmList(ContainerVm containerVm) {
    	this.vmList.add(containerVm);
    }

}
