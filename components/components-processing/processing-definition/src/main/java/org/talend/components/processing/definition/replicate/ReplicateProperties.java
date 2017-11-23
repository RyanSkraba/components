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
package org.talend.components.processing.definition.replicate;

import java.util.HashSet;
import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.properties.presentation.Form;

public class ReplicateProperties extends FixedConnectorsComponentProperties {

    // input schema
    public transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "main");

    public SchemaProperties main = new SchemaProperties("main") {

        @SuppressWarnings("unused")
        public void afterSchema() {
            updateOutputSchemas();
        }
    };

    // output schema
    public transient PropertyPathConnector FLOW_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow");

    public transient PropertyPathConnector SECOND_FLOW_CONNECTOR = new ReplicatePropertyPathConnector(Connector.MAIN_NAME,
            "schemaSecondFlow");

    public SchemaProperties schemaFlow = new SchemaProperties("schemaFlow");

    public SchemaProperties schemaSecondFlow = new SchemaProperties("schemaSecondFlow");

    /**
     * FixedSchemaComponentProperties constructor comment.
     */
    public ReplicateProperties(String name) {
        super(name);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        HashSet<PropertyPathConnector> connectors = new HashSet<PropertyPathConnector>();
        if (isOutputConnection) {
            // output schemas
            connectors.add(FLOW_CONNECTOR);
            connectors.add(SECOND_FLOW_CONNECTOR);
        } else {
            // input schema
            connectors.add(MAIN_CONNECTOR);
        }
        return connectors;
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
    }

    public void updateOutputSchemas() {
        // Copy the "main" schema into the "flow" schema
        Schema inputSchema = main.schema.getValue();
        schemaFlow.schema.setValue(inputSchema);

        // Add the "main" schema into the "second flow" schema
        schemaSecondFlow.schema.setValue(inputSchema);
    }

    /**
     * Internal {@link PropertyPathConnector} that permits more than one MAIN in the output set.
     *
     * The default behaviour only hashes on the flow type (aka name).  This implementation prevents
     * one MAIN_NAME from overwriting another when they are placed in a Set.
     */
    private static class ReplicatePropertyPathConnector extends PropertyPathConnector {

        /**
         * @param name
         * @param propertyPath
         */
        public ReplicatePropertyPathConnector(String name, String propertyPath) {
            super(name, propertyPath);
        }

        @Override
        public boolean equals(Object obj) {
            // instances are only equal when they are the same instance.
            return this == obj;
        }
    }

}
