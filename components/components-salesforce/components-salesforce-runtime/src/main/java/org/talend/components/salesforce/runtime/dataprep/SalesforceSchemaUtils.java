package org.talend.components.salesforce.runtime.dataprep;

import java.io.IOException;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.salesforce.dataset.SalesforceDatasetProperties;

public abstract class SalesforceSchemaUtils {

    public static Schema getSchema(SalesforceDatasetProperties dataset, SalesforceDataprepSource sds, RuntimeContainer container)
            throws IOException {
        if (dataset.sourceType.getValue() == SalesforceDatasetProperties.SourceType.MODULE_SELECTION) {
            List<String> fields = dataset.selectColumnIds.getValue();
            if (!fields.isEmpty()) {
                try {
                    return sds.guessSchema(query(sds, dataset, fields));
                } catch (Exception exception) {
                    throw new RuntimeException("Cannot retrieve schema from specified the SOQL query.", exception);
                }
            }
            throw new RuntimeException("Cannot retrieve schema when no field or column is specified.");
        } else {
            return sds.guessSchema(dataset.query.getValue());
        }
    }

    private static String query(SalesforceDataprepSource sds, SalesforceDatasetProperties dataset, List<String> fields)
            throws IOException {
        if (dataset.sourceType.getValue() != SalesforceDatasetProperties.SourceType.MODULE_SELECTION || fields.isEmpty()) {
            throw new IllegalArgumentException("The module selection should be chosen and the specified fields not empty.");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        int count = 0;
        for (String wantedFieldName : fields) {
            if (count++ > 0) {
                sb.append(", ");
            }
            sb.append(wantedFieldName);
        }
        sb.append(" from ");
        sb.append(dataset.moduleName.getValue());
        return sb.toString();
    }

}
