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
package org.talend.components.dataprep.tdatasetinput;

import aQute.bnd.annotation.component.Component;
import org.talend.components.api.Constants;
import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.InputComponentDefinition;
import org.talend.components.api.component.runtime.Source;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.dataprep.runtime.DataSetSource;

import aQute.bnd.annotation.component.Component;
import org.talend.daikon.properties.property.Property;

import static org.talend.daikon.properties.property.PropertyFactory.newProperty;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;

/**
 * The TDataSetInputDefinition acts as an entry point for all of services that a component provides to integrate with
 * the Studio (at design-time) and other components (at run-time).
 */
@Component(name = Constants.COMPONENT_BEAN_PREFIX + TDataSetInputDefinition.COMPONENT_NAME, provide = ComponentDefinition.class)
public class TDataSetInputDefinition extends AbstractComponentDefinition implements InputComponentDefinition {

    public static final String COMPONENT_NAME = "tDatasetInput";

    public TDataSetInputDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Talend Data Preparation" };
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[] { newInteger(RETURN_TOTAL_RECORD_COUNT) };
    }

    @Override
    public String getMavenGroupId() {
        return "org.talend.components";
    }

    @Override
    public String getMavenArtifactId() {
        return "components-dataprep";
    }

    @Override
    public Class<? extends ComponentProperties> getPropertyClass() {
        return TDataSetInputProperties.class;
    }

    @Override
    public Source getRuntime() {
        return new DataSetSource();
    }

    @Override
    public boolean isSchemaAutoPropagate() {
        return true;
    }
}
