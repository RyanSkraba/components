// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.dataprep.tdatasetoutput;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.Constants;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.RuntimeInfo;
import org.talend.components.api.component.runtime.SimpleRuntimeInfo;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.dataprep.DataPrepDefinition;
import org.talend.components.dataprep.runtime.DataSetSink;
import org.talend.daikon.properties.Properties;

import aQute.bnd.annotation.component.Component;

/**
 * The TDataSetOutputDefinition acts as an entry point for all of services that a component provides to integrate with
 * the Studio (at design-time) and other components (at run-time).
 */
@Component(name = Constants.COMPONENT_BEAN_PREFIX + TDataSetOutputDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TDataSetOutputDefinition extends DataPrepDefinition {

    public static final String COMPONENT_NAME = "tDatasetOutput";

    public TDataSetOutputDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TDataSetOutputProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(Properties properties, ConnectorTopology componentType) {
        if (componentType == ConnectorTopology.INCOMING) {
            return new SimpleRuntimeInfo(this.getClass().getClassLoader(), "org.talend.components", "components-dataprep",
                    DataSetSink.class.getCanonicalName());
        } else {
            return null;
        }
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.INCOMING);
    }

}
