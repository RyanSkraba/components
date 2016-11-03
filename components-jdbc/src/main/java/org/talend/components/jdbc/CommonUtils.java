package org.talend.components.jdbc;

import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;

public class CommonUtils {

    public static Schema getSchema(SchemaProperties schema) {
        return (Schema) schema.schema.getValue();
    }

    public static Form addForm(Properties props, String formName) {
        return new Form(props, formName);
    }

    // get main schema from the out connector of input components
    public static Schema getMainSchemaFromOutputConnector(ComponentProperties properties) {
        Set<? extends Connector> outputConnectors = properties.getPossibleConnectors(true);

        if (outputConnectors == null) {
            return null;
        }

        for (Connector connector : outputConnectors) {
            if (Connector.MAIN_NAME.equals(connector.getName())) {
                return properties.getSchema(connector, true);
            }
        }

        return null;
    }

    // get main schema from the input connector of output components
    public static Schema getMainSchemaFromInputConnector(ComponentProperties properties) {
        Set<? extends Connector> inputConnectors = properties.getPossibleConnectors(false);

        if (inputConnectors == null) {
            return null;
        }

        for (Connector connector : inputConnectors) {
            if (Connector.MAIN_NAME.equals(connector.getName())) {
                return properties.getSchema(connector, false);
            }
        }

        return null;
    }

    public static Schema getOutputSchema(ComponentProperties properties) {
        Set<? extends Connector> outputConnectors = properties.getPossibleConnectors(true);

        if (outputConnectors == null) {
            return null;
        }

        for (Connector connector : outputConnectors) {
            if (Connector.MAIN_NAME.equals(connector.getName())) {
                return properties.getSchema(connector, true);
            }
        }

        return null;
    }

    public static Schema getRejectSchema(ComponentProperties properties) {
        Set<? extends Connector> outputConnectors = properties.getPossibleConnectors(true);

        if (outputConnectors == null) {
            return null;
        }

        for (Connector connector : outputConnectors) {
            if (Connector.REJECT_NAME.equals(connector.getName())) {
                return properties.getSchema(connector, true);
            }
        }

        return null;
    }
}
