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
package org.talend.components.jira.tjirainput;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field.Order;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.jira.JiraProperties;
import org.talend.components.jira.Resource;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

import java.util.Collections;
import java.util.Set;

import static org.talend.daikon.avro.SchemaConstants.TALEND_IS_LOCKED;

/**
 * {@link Properties} for Jira input component.
 */
public class TJiraInputProperties extends JiraProperties {

    /**
     * Jira Query language request property
     */
    public Property<String> jql = PropertyFactory.newString("jql");

    /**
     * Jira project ID property
     */
    public Property<String> projectId = PropertyFactory.newString("projectId");

    /**
     * Batch size property, which specifies how many Jira entities should be requested per request
     */
    public Property<Integer> batchSize = PropertyFactory.newInteger("batchSize");

    /**
     * Constructor sets {@link Properties} name
     * 
     * @param name {@link Properties} name
     */
    public TJiraInputProperties(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupProperties() {
        super.setupProperties();
        setupSchema();
        jql.setValue("summary ~ \\\"some word\\\" AND project=PROJECT_ID");
        projectId.setValue("");
        batchSize.setValue(50);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(resource);
        mainForm.addRow(jql);
        mainForm.addRow(projectId);

        Form advancedForm = new Form(this, Form.ADVANCED);
        advancedForm.addRow(batchSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        if (form.getName().equals(Form.MAIN)) {

            // refresh after resource changed
            Resource resourceValue = resource.getValue();
            switch (resourceValue) {
            case PROJECT: {
                form.getWidget(jql.getName()).setHidden(true);
                form.getWidget(projectId.getName()).setHidden(false);
                break;
            }
            case ISSUE: {
                form.getWidget(jql.getName()).setHidden(false);
                form.getWidget(projectId.getName()).setHidden(true);
                break;
            }
            }
        }
    }

    /**
     * Sets initial value of schema property
     */
    void setupSchema() {

        // get Schema for String class
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();

        // create Schema for JSON
        Schema.Field jsonField = new Schema.Field("json", stringSchema, null, null, Order.ASCENDING);
        Schema initialSchema = Schema.createRecord("jira", null, null, false, Collections.singletonList(jsonField));
        initialSchema.addProp(TALEND_IS_LOCKED, "true");

        schema.schema.setValue(initialSchema);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputComponent) {
        if (isOutputComponent) {
            return Collections.singleton(MAIN_CONNECTOR);
        }
        return Collections.emptySet();
    }

}
