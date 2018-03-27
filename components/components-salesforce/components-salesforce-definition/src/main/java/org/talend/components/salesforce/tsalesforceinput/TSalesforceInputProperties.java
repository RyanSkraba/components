// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import static org.talend.components.salesforce.SalesforceDefinition.SOURCE_OR_SINK_CLASS;
import static org.talend.components.salesforce.SalesforceDefinition.getSandboxedInstance;
import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;
import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.apache.avro.Schema;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.ComponentConstants;
import org.talend.components.salesforce.SalesforceConnectionModuleProperties;
import org.talend.components.salesforce.common.SalesforceRuntimeSourceOrSink;
import org.talend.components.salesforce.schema.SalesforceSchemaHelper;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResultMutable;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.sandbox.SandboxedInstance;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.talend.daikon.serialize.PostDeserializeSetup;
import org.talend.daikon.serialize.migration.SerializeSetVersion;

public class TSalesforceInputProperties extends SalesforceConnectionModuleProperties implements SerializeSetVersion {



    public enum QueryMode {
        Query,
        Bulk;
    }
    public Property<QueryMode> queryMode = newEnum("queryMode", QueryMode.class);

    public Property<String> condition = newProperty("condition"); //$NON-NLS-1$

    public Property<Boolean> manualQuery = newBoolean("manualQuery"); //$NON-NLS-1$

    public Property<String> query = newProperty("query"); //$NON-NLS-1$

    public static final String DEFAULT_QUERY = "\"SELECT Id, Name, IsDeleted FROM Account\"";

    public transient PresentationItem guessSchema = new PresentationItem("guessSchema", "Guess schema");

    public transient PresentationItem guessQuery = new PresentationItem("guessQuery", "Guess query");

    public Property<Boolean> includeDeleted = newBoolean("includeDeleted"); //$NON-NLS-1$

    //

    // Advanced
    public Property<Integer> batchSize = newInteger("batchSize"); //$NON-NLS-1$
    //
    public Property<String> normalizeDelimiter = newProperty("normalizeDelimiter"); //$NON-NLS-1$

    public Property<String> columnNameDelimiter = newProperty("columnNameDelimiter"); //$NON-NLS-1$

    // chunk size must be less than 250000.

    public static final int MAX_CHUNK_SIZE = 250_000;
    public static final int DEFAULT_CHUNK_SIZE = 100_000;

    public static final int DEFAULT_CHUNK_SLEEP_TIME = 15;

    public static final int DEFAULT_JOB_TIME_OUT = 0; // Default : no timeout to wait until the job fails or is in success

    public Property<Boolean> safetySwitch = newBoolean("safetySwitch", true);

    public Property<Integer> jobTimeOut = newInteger("jobTimeOut");

    public Property<Boolean> pkChunking = newBoolean("pkChunking", false);

    public Property<Integer> chunkSize = newInteger("chunkSize", DEFAULT_CHUNK_SIZE);

    public Property<Integer> chunkSleepTime = newInteger("chunkSleepTime", DEFAULT_CHUNK_SLEEP_TIME);

    public TSalesforceInputProperties(@JsonProperty("name") String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        jobTimeOut.setValue(DEFAULT_JOB_TIME_OUT);
        batchSize.setValue(250);
        queryMode.setValue(QueryMode.Query);
        normalizeDelimiter.setValue(";");
        columnNameDelimiter.setValue("_");
        query.setTaggedValue(ComponentConstants.LINE_SEPARATOR_REPLACED_TO, " ");
        query.setValue(DEFAULT_QUERY);
    }

    @Override
    public int getVersionNumber() {
        return 1;
    }

    @Override
    public boolean postDeserialize(int version, PostDeserializeSetup setup, boolean persistent) {
        boolean deserialized = super.postDeserialize(version, setup, persistent);

        Integer timeout = jobTimeOut.getValue();
        if(timeout == null) {
            deserialized = true;
            jobTimeOut.setValue(DEFAULT_JOB_TIME_OUT);
        }

        return deserialized;
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(queryMode);
        mainForm.addRow(condition);
        mainForm.addRow(manualQuery);

        mainForm.addColumn(Widget.widget(guessSchema).setWidgetType(Widget.BUTTON_WIDGET_TYPE));
        mainForm.addColumn(Widget.widget(guessQuery).setWidgetType(Widget.BUTTON_WIDGET_TYPE));
        mainForm.addRow(Widget.widget(query).setWidgetType(Widget.TEXT_AREA_WIDGET_TYPE));

        mainForm.addRow(includeDeleted);

        Form advancedForm = getForm(Form.ADVANCED);
        advancedForm.addRow(safetySwitch);
        advancedForm.addRow(jobTimeOut);
        advancedForm.addRow(pkChunking);
        advancedForm.addRow(chunkSize);
        advancedForm.addRow(chunkSleepTime);
        advancedForm.addRow(batchSize);
        advancedForm.addRow(normalizeDelimiter);
        advancedForm.addRow(columnNameDelimiter);
    }

    public ValidationResult validateGuessSchema() {
        ValidationResultMutable validationResult = new ValidationResultMutable();

        try (SandboxedInstance sandboxedInstance = getSandboxedInstance(SOURCE_OR_SINK_CLASS)) {

            SalesforceRuntimeSourceOrSink salesforceSourceOrSink = (SalesforceRuntimeSourceOrSink) sandboxedInstance
                    .getInstance();
            salesforceSourceOrSink.initialize(null, this);

            Schema schema = ((SalesforceSchemaHelper<Schema>) salesforceSourceOrSink).guessSchema(query.getValue());

            module.main.schema.setValue(schema);
            validationResult.setStatus(ValidationResult.Result.OK);
        } catch (TalendRuntimeException tre) {
            String errorMessage = getI18nMessage("errorMessage.validateGuessSchemaSoqlError", tre.getMessage());
            validationResult.setStatus(ValidationResult.Result.ERROR).setMessage(errorMessage);
        } catch (RuntimeException e1) {
            String errorMessage = getI18nMessage("errorMessage.validateGuessSchemaRuntimeError", e1.getMessage());
            validationResult.setStatus(ValidationResult.Result.ERROR).setMessage(errorMessage);
        } catch (IOException e2) {
            String errorMessage = getI18nMessage("errorMessage.validateGuessSchemaConnectionError", e2.getMessage());
            validationResult.setStatus(ValidationResult.Result.ERROR).setMessage(errorMessage);
        }
        return validationResult;
    }

    public ValidationResult validateGuessQuery() {
        ValidationResultMutable validationResult = new ValidationResultMutable();

        try (SandboxedInstance sandboxedInstance = getSandboxedInstance(SOURCE_OR_SINK_CLASS)) {

            SalesforceRuntimeSourceOrSink salesforceSourceOrSink = (SalesforceRuntimeSourceOrSink) sandboxedInstance
                    .getInstance();
            salesforceSourceOrSink.initialize(null, this);

            Schema schema = module.main.schema.getValue();
            String moduleName = module.moduleName.getValue();

            if (!schema.getFields().isEmpty()) {
                String soqlQuery = ((SalesforceSchemaHelper<Schema>)salesforceSourceOrSink).guessQuery(schema, moduleName);
                query.setValue(soqlQuery);

                validationResult.setStatus(ValidationResult.Result.OK);
            } else {
                String errorMessage = getI18nMessage("errorMessage.validateGuessQueryError");
                validationResult.setStatus(ValidationResult.Result.ERROR).setMessage(errorMessage);
                query.setValue("");
            }
        } catch (TalendRuntimeException tre) {
            validationResult.setStatus(ValidationResult.Result.ERROR);
            validationResult.setMessage(getI18nMessage("errorMessage.validateGuessQuerySoqlError", tre.getMessage()));
        }

        return validationResult;
    }

    public void afterGuessSchema() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterGuessQuery() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterQueryMode() {
        refreshLayout(getForm(Form.MAIN));
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterManualQuery() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterPkChunking() {
        refreshLayout(getForm(Form.ADVANCED));
    }

    public void afterPkChunkingSleepTime() {
        refreshLayout(getForm(Form.ADVANCED));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(includeDeleted.getName())
                    .setHidden(!((queryMode.getValue() != null) && queryMode.getValue().equals(QueryMode.Query)));

            form.getWidget(query.getName()).setHidden(!manualQuery.getValue());
            form.getWidget(condition.getName()).setHidden(manualQuery.getValue());
            form.getWidget(guessSchema.getName()).setHidden(!manualQuery.getValue());
            form.getWidget(guessQuery.getName()).setHidden(!manualQuery.getValue());
        }
        if (Form.ADVANCED.equals(form.getName())) {
            boolean isBulkQuery = queryMode.getValue().equals(QueryMode.Bulk);
            form.getWidget(safetySwitch.getName()).setVisible(isBulkQuery);
            form.getWidget(jobTimeOut.getName()).setVisible(isBulkQuery);
            form.getWidget(pkChunking.getName()).setVisible(isBulkQuery);
            form.getWidget(chunkSize.getName()).setVisible(isBulkQuery && pkChunking.getValue());
            form.getWidget(chunkSleepTime.getName()).setVisible(isBulkQuery && pkChunking.getValue());
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
        return isOutputConnection ? Collections.singleton(MAIN_CONNECTOR) : Collections.<PropertyPathConnector> emptySet();
    }
}
