// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.hadoopcluster.runtime.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cloudera.api.DataView;
import com.cloudera.api.model.ApiService;
import com.cloudera.api.model.ApiServiceList;
import com.cloudera.api.v3.ServicesResourceV3;

public class HadoopCMCluster implements HadoopCluster {

    ServicesResourceV3 cluster;
    
    List<String> blacklistParams;

    public HadoopCMCluster(ServicesResourceV3 cluster) {
        this.cluster = cluster;
    }

    @Override
    public Map<HadoopHostedService, HadoopClusterService> getHostedServices() {
        ApiServiceList services = cluster.readServices(DataView.SUMMARY);
        Map<HadoopHostedService, HadoopClusterService> servicesMapping = new HashMap<HadoopHostedService, HadoopClusterService>();
        for (ApiService service : services.getServices()) {
            if (HadoopHostedService.isSupport(service.getType())) {
                HadoopCMClusterService clusterService = new HadoopCMClusterService(service.getName(), cluster, blacklistParams);
                if (clusterService.hasConfigurations()) {
                    servicesMapping.put(HadoopHostedService.fromString(service.getType()), clusterService);
                }
            }
        }
        return servicesMapping;
    }

	@Override
	public void setBlacklistParams(List<String> names) {
		blacklistParams = names;
	}
}
