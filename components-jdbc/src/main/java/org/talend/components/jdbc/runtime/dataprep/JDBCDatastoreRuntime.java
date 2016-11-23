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
package org.talend.components.jdbc.runtime.dataprep;

import java.util.Arrays;

import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.components.jdbc.datastore.JDBCDatastoreProperties;
import org.talend.components.jdbc.runtime.JDBCSourceOrSink;
import org.talend.daikon.properties.ValidationResult;

public class JDBCDatastoreRuntime implements DatastoreRuntime<JDBCDatastoreProperties> {

    /**
     * 
     */
    private static final long serialVersionUID = -2341607291965168491L;

    protected JDBCDatastoreProperties datastore;

    @Override
    public Iterable<ValidationResult> doHealthChecks(RuntimeContainer container) {
        JDBCSourceOrSink jss = new JDBCSourceOrSink();
        jss.initialize(container, datastore);
        ValidationResult result = jss.validate(container);
        return Arrays.asList(result);
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, JDBCDatastoreProperties properties) {
        this.datastore = properties;
        return ValidationResult.OK;
    }
}
