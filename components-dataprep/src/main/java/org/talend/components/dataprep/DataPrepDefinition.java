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
package org.talend.components.dataprep;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.daikon.properties.property.Property;

import static org.talend.daikon.properties.property.PropertyFactory.newInteger;

/**
 * DataPrep definition common class. It contains some common definition implementation for all DataPrep components
 */
public abstract class DataPrepDefinition extends AbstractComponentDefinition {

    public DataPrepDefinition(String componentName) {
        super(componentName);
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Talend Data Preparation" };
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
    public boolean isSchemaAutoPropagate() {
        return true;
    }

    @Override
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_TOTAL_RECORD_COUNT_PROP };
    }
}
