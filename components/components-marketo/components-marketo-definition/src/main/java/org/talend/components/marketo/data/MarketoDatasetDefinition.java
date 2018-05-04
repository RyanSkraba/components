// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marketo.data;

import static org.talend.components.marketo.MarketoComponentDefinition.getCommonRuntimeInfo;

import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.components.marketo.MarketoComponentDefinition;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.definition.I18nDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

public class MarketoDatasetDefinition extends I18nDefinition implements DatasetDefinition<MarketoDatasetProperties> {

    public static final String COMPONENT_NAME = "MarketoDataset";

    public MarketoDatasetDefinition() {
        super(COMPONENT_NAME);
    }

    @Override
    public RuntimeInfo getRuntimeInfo(MarketoDatasetProperties properties) {
        return getCommonRuntimeInfo(MarketoComponentDefinition.RUNTIME_DATASET);
    }

    @Override
    public Class<MarketoDatasetProperties> getPropertiesClass() {
        return MarketoDatasetProperties.class;
    }

    @Override
    public String getImagePath() {
        return getImagePath(DefinitionImageType.PALETTE_ICON_32X32);
    }

    @Override
    public String getImagePath(DefinitionImageType type) {
        switch (type) {
        case PALETTE_ICON_32X32:
            return COMPONENT_NAME + "_icon32.png";
        default:
            return null;
        }
    }

    @Override
    public String getIconKey() {
        return null;
    }

}
