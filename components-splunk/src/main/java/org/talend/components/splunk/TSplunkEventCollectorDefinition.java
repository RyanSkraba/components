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

import static org.talend.daikon.properties.property.PropertyFactory.newProperty;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;

import org.talend.components.api.Constants;
import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.OutputComponentDefinition;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.splunk.runtime.TSplunkEventCollectorSink;
import org.talend.daikon.properties.property.Property;

import aQute.bnd.annotation.component.Component;

/**
 * The tSplunkEventCollectorDefinition acts as an entry point for all of services that a component provides to integrate
 * with the Studio (at design-time) and other components (at run-time).
 */
@Component(name = Constants.COMPONENT_BEAN_PREFIX
        + TSplunkEventCollectorDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TSplunkEventCollectorDefinition extends AbstractComponentDefinition implements OutputComponentDefinition {

    public static String RETURN_RESPONSE_CODE = "responseCode";

    public static final String COMPONENT_NAME = "tSplunkEventCollector"; //$NON-NLS-1$

    public TSplunkEventCollectorDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public String[] getFamilies() {
        return new String[]{"Business Intelligence/Splunk"}; //$NON-NLS-1$
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[]{newInteger(RETURN_RESPONSE_CODE), newProperty(RETURN_ERROR_MESSAGE), newInteger(RETURN_TOTAL_RECORD_COUNT)};
    }

    public String getMavenGroupId() {
        return "org.talend.components";
    }

    @Override
    public String getMavenArtifactId() {
        return "components-splunk";
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
    public Sink getRuntime() {
        return new TSplunkEventCollectorSink();
    }
}
