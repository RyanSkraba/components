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
package org.talend.components.service.rest.fullexample.dataset;

import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.runtime.RuntimeInfo;


public class FullExampleDatasetDefinition extends SimpleNamedThing implements DatasetDefinition<FullExampleDatasetProperties> {

    public static final String NAME = "FullExampleDataset";

    public FullExampleDatasetDefinition() {
        super(NAME);
    }

    @Override
    public RuntimeInfo getRuntimeInfo(FullExampleDatasetProperties properties) {
        return null;
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
    public String getDisplayName() {
        return "Full example dataset";
    }

    @Override
    public String getTitle() {
        return "Full example dataset";
    }

    @Override
    public String getIconKey() {
        return null;
    }

    @Override
    public Class<FullExampleDatasetProperties> getPropertiesClass() {
        return FullExampleDatasetProperties.class;
    }
}
