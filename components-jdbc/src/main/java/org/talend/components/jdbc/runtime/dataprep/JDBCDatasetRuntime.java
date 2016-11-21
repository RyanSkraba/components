package org.talend.components.jdbc.runtime.dataprep;

import org.apache.avro.Schema;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.jdbc.dataset.JDBCDatasetProperties;
import org.talend.components.jdbc.runtime.JDBCSourceOrSink;
import org.talend.daikon.properties.ValidationResult;

public class JDBCDatasetRuntime implements RuntimableRuntime<JDBCDatasetProperties> {

    /**
     * 
     */
    private static final long serialVersionUID = 5829335010543623248L;

    private JDBCDatasetProperties dataset;

    @Override
    public ValidationResult initialize(RuntimeContainer container, JDBCDatasetProperties properties) {
        this.dataset = properties;
        return ValidationResult.OK;
    }

    public Schema getSchemaFromQuery(RuntimeContainer container, String query) {
        JDBCSourceOrSink jss = new JDBCSourceOrSink();
        jss.initialize(container, dataset);
        return jss.getSchemaFromQuery(container, query);
    }

}
