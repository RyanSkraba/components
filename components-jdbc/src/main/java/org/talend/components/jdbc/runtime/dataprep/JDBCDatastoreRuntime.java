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
