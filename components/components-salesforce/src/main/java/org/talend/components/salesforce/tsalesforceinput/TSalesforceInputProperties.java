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
package org.talend.components.salesforce.tsalesforceinput;

import static org.talend.daikon.properties.property.PropertyFactory.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import com.sforce.ws.ConnectionException;
import org.apache.avro.Schema;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.component.runtime.DependenciesReader;
import org.talend.components.api.component.runtime.SimpleRuntimeInfo;
import org.talend.components.common.ComponentConstants;
import org.talend.components.salesforce.SalesforceConnectionModuleProperties;
import org.talend.components.salesforce.runtime.SalesforceSourceOrSink;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.runtime.RuntimeUtil;
import org.talend.daikon.sandbox.SandboxedInstance;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TSalesforceInputProperties extends SalesforceConnectionModuleProperties {

    public enum QueryMode {
        Query,
        Bulk
    }

    public Property<QueryMode> queryMode = newEnum("queryMode", QueryMode.class);

    public Property<String> condition = newProperty("condition"); //$NON-NLS-1$

    public Property<Boolean> manualQuery = newBoolean("manualQuery"); //$NON-NLS-1$

    public Property<String> query = newProperty("query"); //$NON-NLS-1$

    public transient PresentationItem guessSchema = new PresentationItem("guessSchema", "Guess schema");

    public Property<Boolean> includeDeleted = newBoolean("includeDeleted"); //$NON-NLS-1$

    //
    // Advanced
    //
    public Property<Integer> batchSize = newInteger("batchSize"); //$NON-NLS-1$

    public Property<String> normalizeDelimiter = newProperty("normalizeDelimiter"); //$NON-NLS-1$

    public Property<String> columnNameDelimiter = newProperty("columnNameDelimiter"); //$NON-NLS-1$

    public TSalesforceInputProperties(@JsonProperty("name") String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        batchSize.setValue(250);
        queryMode.setValue(QueryMode.Query);
        normalizeDelimiter.setValue(";");
        columnNameDelimiter.setValue("_");
        query.setTaggedValue(ComponentConstants.LINE_SEPARATOR_REPLACED_TO, " ");
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(queryMode);
        mainForm.addRow(condition);
        mainForm.addRow(manualQuery);

        mainForm.addColumn(Widget.widget(guessSchema).setWidgetType(Widget.BUTTON_WIDGET_TYPE));

        mainForm.addRow(Widget.widget(query).setWidgetType(Widget.TEXT_AREA_WIDGET_TYPE));
        query.setValue("\"SELECT Id, Name, IsDeleted FROM Account\"");
        mainForm.addRow(includeDeleted);

        Form advancedForm = getForm(Form.ADVANCED);
        advancedForm.addRow(batchSize);
        advancedForm.addRow(normalizeDelimiter);
        advancedForm.addRow(columnNameDelimiter);
    }

    public ValidationResult validateGuessSchema() {
        ValidationResult validationResult = new ValidationResult();

        try (SandboxedInstance sandboxISalesforceSourceOrSink = RuntimeUtil.createRuntimeClass(
                new SimpleRuntimeInfo(this.getClass().getClassLoader(),
                        DependenciesReader.computeDependenciesFilePath("org.talend.components", "components-salesforce"),
                        "org.talend.components.salesforce.runtime.SalesforceSourceOrSink"),
                connection.getClass().getClassLoader())) {

            SalesforceSourceOrSink salesforceSourceOrSink = (SalesforceSourceOrSink) sandboxISalesforceSourceOrSink.getInstance();
            salesforceSourceOrSink.initialize(null, this);

            Schema schema = salesforceSourceOrSink.guessSchema(query.getValue());

            module.main.schema.setValue(schema);
            validationResult.setStatus(ValidationResult.Result.OK);

        } catch (ConnectionException e1) {
            validationResult.setStatus(ValidationResult.Result.ERROR).setMessage("Could not call Salesforce API. Schema cannot be guessed.");
        } catch (IOException e2) {
            validationResult.setStatus(ValidationResult.Result.ERROR).setMessage("Could not connect to Salesforce server. Schema cannot be guessed.");
        }
        return validationResult;
    }

    public void afterGuessSchema() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterQueryMode() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterManualQuery() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(includeDeleted.getName())
                    .setHidden(!(queryMode.getValue() != null && queryMode.getValue().equals(QueryMode.Query)));

            form.getWidget(query.getName()).setHidden(!manualQuery.getValue());
            form.getWidget(condition.getName()).setHidden(manualQuery.getValue());
            form.getWidget(guessSchema.getName()).setHidden(!manualQuery.getValue());
        }
        if (Form.ADVANCED.equals(form.getName())) {
            boolean isBulkQuery = queryMode.getValue().equals(QueryMode.Bulk);
            form.getWidget(normalizeDelimiter.getName()).setHidden(isBulkQuery);
            form.getWidget(columnNameDelimiter.getName()).setHidden(isBulkQuery);
            form.getWidget(batchSize.getName()).setHidden(isBulkQuery);

            connection.bulkConnection.setValue(isBulkQuery);
            connection.afterBulkConnection();
            form.getChildForm(connection.getName()).getWidget(connection.bulkConnection.getName()).setHidden(true);
        }
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.singleton(MAIN_CONNECTOR);
        } else {
            return Collections.EMPTY_SET;
        }
    }

}
