package ${package}.definition.${componentNameLowerCase};

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

import java.util.HashSet;
import java.util.Set;

import org.apache.avro.Schema;
import ${packageTalend}.api.component.Connector;
import ${packageTalend}.api.component.PropertyPathConnector;
import ${packageTalend}.common.FixedConnectorsComponentProperties;
import ${packageTalend}.common.SchemaProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class ${componentNameClass}Properties extends FixedConnectorsComponentProperties {

    // input schema
    public transient PropertyPathConnector INPUT_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "main");

    public SchemaProperties main = new SchemaProperties("main") {
        @SuppressWarnings("unused")
        public void afterSchema() {
                updateOutputSchemas();
        }
    };

    // output schema
    public transient PropertyPathConnector OUTPUT_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schemaFlow");

    public SchemaProperties schemaFlow = new SchemaProperties("schemaFlow");

    /**
    * // reject schema
    * public transient PropertyPathConnector REJECT_CONNECTOR = new PropertyPathConnector(Connector.REJECT_NAME, "schemaReject");
    *
    * public SchemaProperties schemaReject = new SchemaProperties("schemaReject");
    */

    /**
    * Example of a new property
    * public Property<Integer> recordCount = PropertyFactory.newInteger("recordCount").setRequired();
    * public Property<Boolean> showCountRecord = PropertyFactory.newBoolean("showCountRecord").setRequired();
    */

    public ${componentNameClass}Properties(String name) { super(name); }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        HashSet<PropertyPathConnector> connectors = new HashSet<PropertyPathConnector>();
        if (isOutputConnection) {
            // output schemas
            connectors.add(OUTPUT_CONNECTOR);
        } else {
            // input schema
            connectors.add(INPUT_CONNECTOR);
        }
        return connectors;
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        /**
         * Example to add property field in the form :
         * mainForm.addRow(showCountRecord);
         * mainForm.addRow(recordCount);
         */
        }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        /**
         * Example to hide or show a property in a form
         * if (form.getName().equals(Form.MAIN)) {
            form.getWidget(recordCount.getName()).setHidden(showCountRecord);
            }
         */
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        /**
         * Example to set default value to a property in a form :
         * recordCount.setValue(20);
         * showCountRecord.setValue(false);
         */
    }

    protected void updateOutputSchemas() {
        // Copy the "main" schema into the "flow" schema
        Schema inputSchema = main.schema.getValue();
        schemaFlow.schema.setValue(inputSchema);
    }
}
