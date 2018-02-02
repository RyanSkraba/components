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
package org.talend.components.marklogic.data;

import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.components.marklogic.RuntimeInfoProvider;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.definition.I18nDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

public class MarkLogicDatasetDefinition extends I18nDefinition implements DatasetDefinition<MarkLogicDatasetProperties> {

    public static final String NAME = "MarkLogicDataset";

    public static final String DATASET_RUNTIME = "org.talend.components.marklogic.data.MarkLogicDatasetRuntime";

    public MarkLogicDatasetDefinition() {
        super(NAME);
    }

    @Override
    public Class<MarkLogicDatasetProperties> getPropertiesClass() {
        return MarkLogicDatasetProperties.class;
    }

    @Override
    public String getImagePath() {
        return getImagePath(DefinitionImageType.PALETTE_ICON_32X32);
    }

    @Override
    public String getImagePath(DefinitionImageType type) {
        switch (type) {
        case PALETTE_ICON_32X32:
            return NAME + "_icon32.png";
        default:
            return null;
        }
    }

    @Override
    public String getIconKey() {
        return null;
    }

    @Override
    public RuntimeInfo getRuntimeInfo(MarkLogicDatasetProperties properties) {
        return RuntimeInfoProvider.getCommonRuntimeInfo(DATASET_RUNTIME);
    }

}
