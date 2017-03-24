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

import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.container.RuntimeContainer;

public class ClouderaManagerConfigurationSource extends HadoopClusterConfigurationSource {

    @Override
    public BoundedReader createReader(RuntimeContainer adaptor) {
        return new ClouderaManagerConfigurationReader(this, properties);
    }
}
