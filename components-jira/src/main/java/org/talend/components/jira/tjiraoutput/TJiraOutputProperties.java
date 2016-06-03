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
package org.talend.components.jira.tjiraoutput;

import static org.talend.components.jira.Action.*;
import static org.talend.components.jira.Mode.*;
import static org.talend.components.jira.Resource.*;
import static org.talend.daikon.avro.SchemaConstants.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field.Order;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.properties.ComponentPropertyFactory;
import org.talend.components.jira.Action;
import org.talend.components.jira.JiraProperties;
import org.talend.components.jira.Mode;
import org.talend.components.jira.Resource;
import org.talend.daikon.avro.AvroRegistry;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

/**
 * {@link Properties} for Jira output component.
 */
public class TJiraOutputProperties extends JiraProperties {

    /**
     * Output result properties names
     */
    public static final String NB_LINE = "NB_LINE";

    public static final String NB_SUCCESS = "NB_SUCCESS";

    public static final String NB_REJECT = "NB_REJECT";

    /**
     * Corresponding schemas for each Action
     */
    private static final Schema deleteSchema;

    private static final Schema insertSchema;

    private static final Schema updateSchema;

    /**
     * Initializes schema constants
     */
    static {
        // get Schema for String class
        AvroRegistry registry = new AvroRegistry();
        Schema stringSchema = registry.getConverter(String.class).getSchema();

        Schema.Field deleteIdField = new Schema.Field("id", stringSchema, null, null, Order.ASCENDING);
        deleteSchema = Schema.createRecord("jira", null, null, false, Collections.singletonList(deleteIdField));
        deleteSchema.addProp(TALEND_IS_LOCKED, "true");

        Schema.Field insertJsonField = new Schema.Field("json", stringSchema, null, null, Order.ASCENDING);
        insertSchema = Schema.createRecord("jira", null, null, false, Collections.singletonList(insertJsonField));
        insertSchema.addProp(TALEND_IS_LOCKED, "true");

        Schema.Field updateIdField = new Schema.Field("id", stringSchema, null, null, Order.ASCENDING);
        Schema.Field updateJsonField = new Schema.Field("json", stringSchema, null, null, Order.ASCENDING);
        List<Schema.Field> fields = Arrays.asList(updateIdField, updateJsonField);
        updateSchema = Schema.createRecord("jira", null, null, false, fields);
        updateSchema.addProp(TALEND_IS_LOCKED, "true");
    }

    /**
     * Combo-box action, which defines what output action to perform for data
     */
    public Property<Action> action = PropertyFactory.newEnum("action", Action.class);

    /**
     * Check-box, which defines whether to delete issue subtasks in Delete action
     */
    public Property<Boolean> deleteSubtasks = PropertyFactory.newBoolean("deleteSubtasks");

    /**
     * Combo-box mode, which defines mode of output operation. Possible values are "Basic" and "Advanced"
     */
    public Property<Mode> mode = PropertyFactory.newEnum("mode", Mode.class);

    /**
     * Constructor sets {@link Properties} name
     * 
     * @param name {@link Properties} name
     */
    public TJiraOutputProperties(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupProperties() {
        super.setupProperties();
        schema.schema.setValue(insertSchema);
        action.setValue(INSERT);
        deleteSubtasks.setValue(true);
        mode.setValue(ADVANCED);

        ComponentPropertyFactory.newReturnProperty(getReturns(), PropertyFactory.newInteger(NB_LINE));
        ComponentPropertyFactory.newReturnProperty(getReturns(), PropertyFactory.newInteger(NB_SUCCESS));
        ComponentPropertyFactory.newReturnProperty(getReturns(), PropertyFactory.newInteger(NB_REJECT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(action);

        Form advancedForm = new Form(this, Form.ADVANCED);
        advancedForm.addRow(deleteSubtasks);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        Action actionValue = action.getValue();
        Resource resourceValue = resource.getValue();

        if (form.getName().equals(Form.MAIN)) {

            // sets corresponding schema property for chosen action
            switch (actionValue) {
            case DELETE: {
                schema.schema.setValue(deleteSchema);
                break;
            }
            case INSERT: {
                schema.schema.setValue(insertSchema);
                break;
            }
            case UPDATE: {
                schema.schema.setValue(updateSchema);
                break;
            }
            }

        }

        if (form.getName().equals(Form.ADVANCED)) {

            // deleteSubtasks property visibility
            if (DELETE.equals(actionValue) && ISSUE.equals(resourceValue)) {
                form.getWidget(deleteSubtasks.getName()).setHidden(false);
            } else {
                form.getWidget(deleteSubtasks.getName()).setHidden(true);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputComponent) {
        if (isOutputComponent) {
            return Collections.EMPTY_SET;
        } else {
            return Collections.singleton(MAIN_CONNECTOR);
        }
    }

    /**
     * Refreshes form layout after action is changed
     */
    public void afterAction() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
    }

    /**
     * Refreshes form layout after resource is changed
     */
    @Override
    public void afterResource() {
        refreshLayout(getForm(Form.ADVANCED));
    }

}
