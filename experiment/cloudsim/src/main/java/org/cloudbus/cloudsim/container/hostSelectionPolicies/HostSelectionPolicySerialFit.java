package org.cloudbus.cloudsim.container.hostSelectionPolicies;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.container.core.ContainerHost;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh on 12/08/15.
 */
public class HostSelectionPolicySerialFit extends HostSelectionPolicy {
	int count=0;
    @Override
    public ContainerHost getHost(List<ContainerHost> hostList, Object obj, Set<? extends ContainerHost> excludedHostList) {
        ContainerHost host = null;
        for (ContainerHost host1 : hostList) {
        	host = hostList.get(count);
            //Log.printLine(host);
            if (excludedHostList.contains(host)) {
            	count++;
                continue;
            }
            break;
        }
        count++;
    return host;
    }
}
