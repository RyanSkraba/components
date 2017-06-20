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
package org.talend.components.salesforce.dataset;

import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.components.salesforce.SalesforceDefinition;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.definition.I18nDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * the salesforce data set work for dataprep
 *
 */
public class SalesforceDatasetDefinition extends I18nDefinition implements DatasetDefinition<SalesforceDatasetProperties> {

    public static final String NAME = "SalesforceDataset";

    public SalesforceDatasetDefinition() {
        super(NAME);
    }

    @Override
    public RuntimeInfo getRuntimeInfo(SalesforceDatasetProperties properties) {
        return SalesforceDefinition.getCommonRuntimeInfo(SalesforceDefinition.DATASET_RUNTIME_CLASS);
    }

    @Deprecated
    @Override
    public String getImagePath() {
        return NAME + "_icon32.png";
    }

    @Override
    public String getImagePath(DefinitionImageType type) {
        switch (type) {
        case PALETTE_ICON_32X32:
            return NAME + "_icon32.png";
        case SVG_ICON:
            return null;
        }
        return null;
    }

    @Override
    public String getIconKey() {
        return null;
    }

    @Override
    public Class<SalesforceDatasetProperties> getPropertiesClass() {
        return SalesforceDatasetProperties.class;
    }
}
