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
package org.talend.components.jdbc.dataset;

import org.talend.components.common.dataset.DatasetDefinition;
import org.talend.components.jdbc.runtime.JDBCTemplate;
import org.talend.components.jdbc.runtime.dataprep.JDBCDatasetRuntime;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * the database data set work for dataprep
 *
 */
public class JDBCDatasetDefinition extends SimpleNamedThing implements DatasetDefinition<JDBCDatasetProperties> {

    public static final String NAME = "JDBCDataset";

    public JDBCDatasetDefinition() {
        super(NAME);
    }

    @Override
    public RuntimeInfo getRuntimeInfo(JDBCDatasetProperties properties) {
        return JDBCTemplate.createCommonRuntime(this.getClass().getClassLoader(), properties,
                JDBCDatasetRuntime.class.getCanonicalName());
    }

    @Override
    public String getImagePath() {
        return NAME + "_icon32.png";
    }

    @Override
    public Class<JDBCDatasetProperties> getPropertiesClass() {
        return JDBCDatasetProperties.class;
    }
}
