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

import java.net.URL;
import java.util.List;

import javax.net.ssl.TrustManager;

public interface HadoopConfigurator {

    public List<String> getAllClusters();

    public HadoopCluster getCluster(String name);

    public interface Builder {

        public Builder withUrl(URL url);

        public Builder withUsernamePassword(String user, String password);

        public Builder withTrustManagers(TrustManager[] tms);

        public HadoopConfigurator build();
    }

}
