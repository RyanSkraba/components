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
package org.talend.components.splunk;

import static org.talend.daikon.properties.property.PropertyFactory.*;

import java.util.EnumSet;
import java.util.Set;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ConnectorTopology;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.RuntimeInfo;
import org.talend.components.api.component.runtime.SimpleRuntimeInfo;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.splunk.runtime.TSplunkEventCollectorSink;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;

/**
 * The tSplunkEventCollectorDefinition acts as an entry point for all of services that a component provides to integrate
 * with the Studio (at design-time) and other components (at run-time).
 */
public class TSplunkEventCollectorDefinition extends AbstractComponentDefinition {

    public static String RETURN_RESPONSE_CODE = "responseCode";

    public static Property<Integer> RETURN_RESPONSE_CODE_PROP = newInteger(RETURN_RESPONSE_CODE);

    public static final String COMPONENT_NAME = "tSplunkEventCollector"; //$NON-NLS-1$

    public TSplunkEventCollectorDefinition() {
        super(COMPONENT_NAME);
        setupI18N(new Property<?>[] { RETURN_RESPONSE_CODE_PROP });
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Business Intelligence/Splunk" }; //$NON-NLS-1$
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_RESPONSE_CODE_PROP, RETURN_ERROR_MESSAGE_PROP, RETURN_TOTAL_RECORD_COUNT_PROP };
    }

    @Override
    public String getName() {
        return COMPONENT_NAME;
    }

    @Override
    public boolean isSchemaAutoPropagate() {
        return true;
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TSplunkEventCollectorProperties.class;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(Properties properties, ConnectorTopology componentType) {
        if (componentType == ConnectorTopology.INCOMING) {
            return new SimpleRuntimeInfo(this.getClass().getClassLoader(),
                    DependenciesReader.computeDependenciesFilePath("org.talend.components", "components-splunk"),
                    TSplunkEventCollectorSink.class.getCanonicalName());
        } else {
            return null;
        }
    }

    @Override
    public Set<ConnectorTopology> getSupportedConnectorTopologies() {
        return EnumSet.of(ConnectorTopology.INCOMING);
    }
}
