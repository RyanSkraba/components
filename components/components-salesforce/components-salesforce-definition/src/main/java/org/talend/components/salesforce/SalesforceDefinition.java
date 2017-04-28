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
package org.talend.components.salesforce;

import java.util.Arrays;
import java.util.List;

import org.talend.components.api.component.AbstractComponentDefinition;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.ExecutionEngine;
import org.talend.components.api.component.runtime.JarRuntimeInfo;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.CommonTags;
import org.talend.daikon.i18n.tag.TagImpl;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeInfo;

public abstract class SalesforceDefinition extends AbstractComponentDefinition {

    protected static final TagImpl SALESFORCE_CLOUD_TAG = new TagImpl("salesforce", CommonTags.CLOUD_TAG);

    protected static final TagImpl SALESFORCE_BUSINESS_TAG = new TagImpl("salesforce", CommonTags.BUSINESS_TAG);

    public SalesforceDefinition(String componentName, ExecutionEngine engine1, ExecutionEngine... engines) {
        super(componentName, engine1, engines);
    }

    @Override
    public String[] getFamilies() {
        return new String[] { "Business/Salesforce", "Cloud/Salesforce" };
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ComponentProperties>[] getNestedCompatibleComponentPropertiesClass() {
        return new Class[] { SalesforceConnectionProperties.class };
    }

    @Override
    // Most of the components are on the input side, so put this here, the output definition will override this
    public Property[] getReturnProperties() {
        return new Property[] { RETURN_ERROR_MESSAGE_PROP, RETURN_TOTAL_RECORD_COUNT_PROP };
    }

    public static RuntimeInfo getCommonRuntimeInfo(String clazzFullName) {
        return new JarRuntimeInfo("mvn:org.talend.components/components-salesforce-runtime",
                DependenciesReader.computeDependenciesFilePath("org.talend.components", "components-salesforce-runtime"),
                clazzFullName);
    }

    public List<TagImpl> doGetTags() {
        return Arrays.asList(SALESFORCE_CLOUD_TAG, SALESFORCE_BUSINESS_TAG);
    }

}
