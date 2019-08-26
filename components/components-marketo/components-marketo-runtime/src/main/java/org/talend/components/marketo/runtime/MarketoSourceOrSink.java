// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marketo.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.IndexedRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.runtime.SourceOrSink;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.MarketoProvideConnectionProperties;
import org.talend.components.marketo.MarketoUtils;
import org.talend.components.marketo.runtime.client.MarketoClientService;
import org.talend.components.marketo.runtime.client.MarketoClientServiceExtended;
import org.talend.components.marketo.runtime.client.MarketoRESTClient;
import org.talend.components.marketo.runtime.client.MarketoSOAPClient;
import org.talend.components.marketo.runtime.client.rest.type.FieldDescription;
import org.talend.components.marketo.runtime.client.type.MarketoException;
import org.talend.components.marketo.runtime.client.type.MarketoRecordResult;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.StandardAction;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.CustomObjectAction;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.ValidationResultMutable;

import com.google.gson.Gson;

public class MarketoSourceOrSink implements SourceOrSink, MarketoSourceOrSinkRuntime, MarketoSourceOrSinkSchemaProvider {

    public static final String TALEND6_DYNAMIC_COLUMN_POSITION = "di.dynamic.column.position";

    protected MarketoProvideConnectionProperties properties;

    protected MarketoClientService client;

    protected static final String KEY_CONNECTION_PROPERTIES = "connection";

    private static final Logger LOG = LoggerFactory.getLogger(MarketoSourceOrSink.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(MarketoSourceOrSink.class);

    public MarketoProvideConnectionProperties getProperties() {
        return properties;
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, ComponentProperties properties) {
        this.properties = (MarketoProvideConnectionProperties) properties;
        return ValidationResult.OK;
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        TMarketoConnectionProperties conn = getEffectiveConnection(container);
        String endpoint = conn.endpoint.getValue();
        String clientAccess = conn.clientAccessId.getValue();
        String secretKey = conn.secretKey.getValue();
        ValidationResultMutable vr = new ValidationResultMutable();
        if (endpoint == null || endpoint.isEmpty()) {
            vr.setMessage(messages.getMessage("error.validation.connection.endpoint"));
            vr.setStatus(ValidationResult.Result.ERROR);
            return vr;
        }
        if (clientAccess == null || clientAccess.isEmpty()) {
            vr.setMessage(messages.getMessage("error.validation.connection.clientaccess"));
            vr.setStatus(ValidationResult.Result.ERROR);
            return vr;
        }
        if (secretKey == null || secretKey.isEmpty()) {
            vr.setMessage(messages.getMessage("error.validation.connection.secretkey"));
            vr.setStatus(ValidationResult.Result.ERROR);
            return vr;
        }
        // bug/TDI-38439_MarketoWizardConnection
        return validateConnection(conn);
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        List<NamedThing> customObjects = new ArrayList<>();
        MarketoClientServiceExtended client = (MarketoClientServiceExtended) getClientService(null);
        TMarketoInputProperties ip = new TMarketoInputProperties("COSchemas");
        ip.init();
        ip.connection = properties.getConnectionProperties();
        ip.inputOperation.setValue(InputOperation.CustomObject);
        ip.customObjectAction.setValue(CustomObjectAction.list);
        ip.customObjectNames.setValue("");
        ip.schemaInput.schema.setValue(MarketoConstants.getCustomObjectDescribeSchema());
        MarketoRecordResult r = client.listCustomObjects(ip);
        for (IndexedRecord co : r.getRecords()) {
            String name = co.get(0).toString();// name cannot be null
            Object displayName = co.get(1);
            if (displayName == null) {
                displayName = name;
            }
            customObjects.add(new SimpleNamedThing(name, displayName.toString()));
        }
        //
        return customObjects;
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        MarketoRESTClient client = (MarketoRESTClient) getClientService(null);
        TMarketoInputProperties ip = new TMarketoInputProperties("retrieveSchema");
        Schema describeSchema = MarketoConstants.getCustomObjectDescribeSchema();
        ip.connection = properties.getConnectionProperties();
        ip.schemaInput.schema.setValue(describeSchema);
        ip.standardAction.setValue(StandardAction.describe);
        ip.customObjectAction.setValue(CustomObjectAction.describe);
        MarketoRecordResult r = new MarketoRecordResult();
        switch (schemaName) {
        case RESOURCE_COMPANY:
            ip.inputOperation.setValue(InputOperation.Company);
            r = client.describeCompanies(ip);
            break;
        case RESOURCE_OPPORTUNITY:
            ip.inputOperation.setValue(InputOperation.Opportunity);
            r = client.describeOpportunity(ip);
            break;
        case RESOURCE_OPPORTUNITY_ROLE:
            ip.inputOperation.setValue(InputOperation.OpportunityRole);
            r = client.describeOpportunity(ip);
            break;
        default:
            ip.inputOperation.setValue(InputOperation.CustomObject);
            ip.customObjectName.setValue(schemaName);
            r = client.describeCustomObject(ip);
        }
        if (!r.isSuccess()) {
            return null;
        }
        List<IndexedRecord> records = r.getRecords();
        if (records == null || records.isEmpty()) {
            return null;
        }
        IndexedRecord record = records.get(0);

        return FieldDescription.getSchemaFromJson(schemaName, record.get(describeSchema.getField("fields").pos()).toString(),
                record.get(describeSchema.getField("dedupeFields").pos()).toString());
    }

    @Override
    public Schema getSchemaForCustomObject(String customObjectName) throws IOException {
        if (StringUtils.isEmpty(customObjectName)) {
            return null;
        }
        return getEndpointSchema(null, customObjectName);
    }

    @Override
    public Schema getSchemaForCompany() throws IOException {
        return getEndpointSchema(null, RESOURCE_COMPANY);
    }

    @Override
    public Schema getSchemaForOpportunity() throws IOException {
        return getEndpointSchema(null, RESOURCE_OPPORTUNITY);
    }

    @Override
    public Schema getSchemaForOpportunityRole() throws IOException {
        return getEndpointSchema(null, RESOURCE_OPPORTUNITY_ROLE);
    }

    @Override
    public List<String> getCompoundKeyFields(String resource) throws IOException {
        MarketoRESTClient client = (MarketoRESTClient) getClientService(null);
        TMarketoInputProperties ip = new TMarketoInputProperties("retrieveSchema");
        Schema describeSchema = MarketoConstants.getCustomObjectDescribeSchema();
        ip.connection = properties.getConnectionProperties();
        ip.customObjectAction.setValue(CustomObjectAction.describe);
        ip.standardAction.setValue(StandardAction.describe);
        ip.schemaInput.schema.setValue(describeSchema);
        MarketoRecordResult r;

        switch (resource) {
        case RESOURCE_OPPORTUNITY:
            ip.inputOperation.setValue(InputOperation.Opportunity);
            r = client.describeOpportunity(ip);
            break;
        case RESOURCE_OPPORTUNITY_ROLE:
            ip.inputOperation.setValue(InputOperation.OpportunityRole);
            r = client.describeOpportunity(ip);
            break;
        default:
            ip.inputOperation.setValue(InputOperation.CustomObject);
            ip.customObjectName.setValue(resource);
            r = client.describeCustomObject(ip);
        }
        if (!r.isSuccess()) {
            return null;
        }
        List<IndexedRecord> records = r.getRecords();
        if (records == null || records.isEmpty()) {
            return null;
        }
        IndexedRecord record = records.get(0);
        String[] keys = new Gson().fromJson(record.get(describeSchema.getField("dedupeFields").pos()).toString(), String[].class);
        // quote keys
        for (int i = 0; i < keys.length; i++) {
            keys[i] = "\"" + keys[i] + "\"";
        }

        return Arrays.asList(keys);
    }

    @Override
    public List<Field> getAllLeadFields() throws IOException {
        return ((MarketoRESTClient) getClientService(null)).getAllLeadFields();
    }

    /**
     * Retrieve schema for Leads or CustomObjects.
     *
     * @param the ObjectName to get schema. If blank, assumes it's for Lead fields. Otherwise ObjectName should be the
     * CustomObject API's name.
     * 
     * @return the schema for the given CustomObject or all Lead fields.
     * 
     */
    public Schema getDynamicSchema(String objectName, Schema design) throws IOException {
        List<Field> designFields = new ArrayList<>();
        List<String> existingFieldNames = new ArrayList<>();
        for (Field f : design.getFields()) {
            existingFieldNames.add(f.name());
            Field nf = new Field(f.name(), f.schema(), f.doc(), f.defaultVal());
            nf.getObjectProps().putAll(f.getObjectProps());
            for (Map.Entry<String, Object> entry : f.getObjectProps().entrySet()) {
                nf.addProp(entry.getKey(), entry.getValue());
            }
            designFields.add(nf);
        }
        List<Field> objectFields;
        List<Field> resultFields = new ArrayList<>();
        resultFields.addAll(designFields);
        // will fetch fields...
        MarketoRESTClient client = (MarketoRESTClient) getClientService(null);
        if (StringUtils.isEmpty(objectName)) {
            objectFields = client.getAllLeadFields();
        } else {
            objectFields = getSchemaFieldsList(getEndpointSchema(null, objectName));
        }
        for (Field f : objectFields) {
            // test if field isn't already in the schema
            if (!existingFieldNames.contains(f.name())) {
                resultFields.add(f);
            }
        }
        Schema resultSchema = Schema.createRecord(design.getName(), design.getDoc(), design.getNamespace(), design.isError());
        resultSchema.getObjectProps().putAll(design.getObjectProps());
        resultSchema.setFields(resultFields);

        return resultSchema;
    }

    public static List<Field> getSchemaFieldsList(Schema schema) {
        List<Field> result = new ArrayList<>();
        for (Field f : schema.getFields()) {
            Field nf = new Field(f.name(), f.schema(), f.doc(), f.defaultVal());
            nf.getObjectProps().putAll(f.getObjectProps());
            for (Map.Entry<String, Object> entry : f.getObjectProps().entrySet()) {
                nf.addProp(entry.getKey(), entry.getValue());
            }
            result.add(nf);
        }
        return result;
    }

    public ValidationResult validateConnection(MarketoProvideConnectionProperties properties) {
        ValidationResultMutable vr = new ValidationResultMutable().setStatus(Result.OK);
        try {
            MarketoSourceOrSink sos = new MarketoSourceOrSink();
            sos.initialize(null, (ComponentProperties) properties);
            client = sos.getClientService(null);
            vr.setMessage(messages.getMessage("success.validation.connection"));
        } catch (IOException e) {
            vr.setStatus(Result.ERROR);
            vr.setMessage(messages.getMessage("error.validation.connection.testconnection", e.getLocalizedMessage()));
        }
        return vr;
    }

    public TMarketoConnectionProperties getEffectiveConnection(RuntimeContainer container) {
        TMarketoConnectionProperties connProps = properties.getConnectionProperties();
        String refComponentId = connProps.getReferencedComponentId();
        // Using another component's connection
        if (refComponentId != null) {
            // In a runtime container
            if (container != null) {
                TMarketoConnectionProperties shared = (TMarketoConnectionProperties) container.getComponentData(refComponentId,
                        KEY_CONNECTION_PROPERTIES);
                if (shared != null) {
                    return shared;
                }
            }
            // Design time
            connProps = connProps.getReferencedConnectionProperties();
        }
        if (container != null) {
            container.setComponentData(container.getCurrentComponentId(), KEY_CONNECTION_PROPERTIES, connProps);
        }
        return connProps;
    }

    public TMarketoConnectionProperties getConnectionProperties() {
        return properties.getConnectionProperties();
    }

    public MarketoClientService getClientService(RuntimeContainer container) throws IOException {
        if (client == null) {
            try {
                TMarketoConnectionProperties conn = getEffectiveConnection(container);
                if (APIMode.SOAP.equals(conn.apiMode.getValue())) {
                    client = new MarketoSOAPClient(conn).connect();
                } else {
                    client = new MarketoRESTClient(conn).connect();
                }
            } catch (MarketoException e) {
                LOG.error(e.toString());
                throw new IOException(e);
            }
            LOG.debug("ClientService : {}", client);
        }
        return client;
    }

    public static Schema mergeDynamicSchemas(Schema data, Schema flow) {
        // TODO when https://jira.talendforge.org/browse/TDKN-154 will be resolved, use the new property here!
        String dynamicFieldProperty = flow.getProp(TALEND6_DYNAMIC_COLUMN_POSITION);
        int dynamicFieldPosition = -1;
        if (AvroUtils.isIncludeAllFields(flow) && dynamicFieldProperty != null) {
            dynamicFieldPosition = Integer.valueOf(dynamicFieldProperty);
        }
        List<Field> mergeFields = new ArrayList<>();
        for (Field f : flow.getFields()) {
            if (f.pos() == dynamicFieldPosition) {
                for (Field cf : data.getFields()) {
                    // don't add field in that exists in flow schema
                    if (flow.getField(cf.name()) == null) {
                        mergeFields.add(MarketoUtils.generateNewField(cf));
                    }
                }
                // add field from flow at after dynamic index if exists
                if (flow.getFields().get(f.pos()) != null) {
                    mergeFields.add(MarketoUtils.generateNewField(f));
                }
            } else {
                mergeFields.add(MarketoUtils.generateNewField(f));
            }
        }
        // dynamic column is at the end
        if (dynamicFieldPosition >= flow.getFields().size()) {
            for (Field cf : data.getFields()) {
                // don't add field in that exists in flow schema
                if (flow.getField(cf.name()) == null) {
                    mergeFields.add(MarketoUtils.generateNewField(cf));
                }
            }
        }
        Schema merged = Schema.createRecord("merged", "", "0", false);
        merged.setFields(mergeFields);

        return merged;
    }

}
