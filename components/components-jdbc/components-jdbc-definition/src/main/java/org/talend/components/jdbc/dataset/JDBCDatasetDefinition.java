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
package org.talend.components.jdbc.dataset;

import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.components.jdbc.JdbcRuntimeInfo;
import org.talend.daikon.definition.DefinitionImageType;
import org.talend.daikon.definition.I18nDefinition;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * the database data set work for dataprep
 *
 */
public class JDBCDatasetDefinition extends I18nDefinition implements DatasetDefinition<JDBCDatasetProperties> {

    public static final String NAME = "JDBCDataset";

    public JDBCDatasetDefinition() {
        super(NAME);
    }

    @Override
    public RuntimeInfo getRuntimeInfo(JDBCDatasetProperties properties) {
        return new JdbcRuntimeInfo(properties, "org.talend.components.jdbc.runtime.dataprep.JDBCDatasetRuntime");
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
    public Class<JDBCDatasetProperties> getPropertiesClass() {
        return JDBCDatasetProperties.class;
    }
}
