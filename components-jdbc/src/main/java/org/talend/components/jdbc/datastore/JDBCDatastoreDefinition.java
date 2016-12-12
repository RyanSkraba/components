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
package org.talend.components.jdbc.datastore;

import org.talend.components.common.dataset.DatasetProperties;
import org.talend.components.common.datastore.DatastoreDefinition;
import org.talend.components.jdbc.dataprep.JDBCInputDefinition;
import org.talend.components.jdbc.dataset.JDBCDatasetProperties;
import org.talend.components.jdbc.runtime.JDBCTemplate;
import org.talend.components.jdbc.runtime.dataprep.JDBCDatastoreRuntime;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.runtime.RuntimeInfo;

/**
 * the database data store work for dataprep
 *
 */
public class JDBCDatastoreDefinition extends SimpleNamedThing implements DatastoreDefinition<JDBCDatastoreProperties> {

    public static final String NAME = "JDBCDatastore";

    public JDBCDatastoreDefinition() {
        super(NAME);
    }

    @Override
    public RuntimeInfo getRuntimeInfo(JDBCDatastoreProperties properties) {
        return JDBCTemplate.createCommonRuntime(this.getClass().getClassLoader(), properties,
                JDBCDatastoreRuntime.class.getCanonicalName());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public DatasetProperties createDatasetProperties(JDBCDatastoreProperties storeProp) {
        JDBCDatasetProperties datasetProperties = new JDBCDatasetProperties("dataset");
        datasetProperties.init();
        datasetProperties.setDatastoreProperties(storeProp);
        return datasetProperties;
    }

    @Override
    public String getInputCompDefinitionName() {
        return JDBCInputDefinition.NAME;
    }

    @Override
    public String getOutputCompDefinitionName() {
        // no output component now
        return null;
    }

    @Override
    public String getImagePath() {
        return NAME + "_icon32.png";
    }

    @Override
    public Class<JDBCDatastoreProperties> getPropertiesClass() {
        return JDBCDatastoreProperties.class;
    }

    @Override
    public String getDisplayName() {
        return getI18nMessage("datastore." + getName() + I18N_DISPLAY_NAME_SUFFIX);
    }

}
