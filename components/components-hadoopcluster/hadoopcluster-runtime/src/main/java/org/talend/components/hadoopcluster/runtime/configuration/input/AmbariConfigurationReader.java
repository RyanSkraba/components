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

package org.talend.components.hadoopcluster.runtime.configuration.input;

import java.io.IOException;
import java.net.URL;

import javax.net.ssl.TrustManager;

import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.BoundedSource;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.hadoopcluster.configuration.input.HadoopClusterConfigurationInputProperties;
import org.talend.components.hadoopcluster.runtime.configuration.HadoopAmbariConfigurator;

public class AmbariConfigurationReader extends HadoopClusterConfigurationReader {

    public AmbariConfigurationReader(BoundedSource source, HadoopClusterConfigurationInputProperties properties) {
        super(source, properties);
    }

    @Override
    void initBuilder() {
        builder = new HadoopAmbariConfigurator.Builder();
    }

}
