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
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.CustomObjectAction;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.InputOperation;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.StandardAction;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.ValidationResultMutable;

import com.google.gson.Gson;

public class MarketoSourceOrSink implements SourceOrSink, MarketoSourceOrSinkSchemaProvider {

    public static final String RESOURCE_COMPANY = "resourceCompany";

    public static final String RESOURCE_OPPORTUNITY = "resourceOpportunity";

    public static final String RESOURCE_OPPORTUNITY_ROLE = "resourceOpportunityRole";

    protected MarketoProvideConnectionProperties properties;

    protected MarketoClientService client;

    protected static final String KEY_CONNECTION_PROPERTIES = "connection";

    private static final Logger LOG = LoggerFactory.getLogger(MarketoSourceOrSink.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(MarketoSourceOrSink.class);

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

    public static List<NamedThing> getSchemaNames(RuntimeContainer container, TMarketoConnectionProperties connection)
            throws IOException {
        MarketoSourceOrSink sos = new MarketoSourceOrSink();
        sos.initialize(container, connection);
        return sos.getSchemaNames(container);
    }

    @Override
    public List<NamedThing> getSchemaNames(RuntimeContainer container) throws IOException {
        List<NamedThing> customObjects = new ArrayList<>();
        MarketoClientServiceExtended client = (MarketoClientServiceExtended) getClientService(null);
        TMarketoInputProperties ip = new TMarketoInputProperties("COSchemas");
        ip.connection = properties.getConnectionProperties();
        ip.inputOperation.setValue(InputOperation.CustomObject);
        ip.customObjectAction.setValue(CustomObjectAction.list);
        ip.customObjectNames.setValue("");
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

    public static Schema getEndpointSchema(RuntimeContainer container, String schemaName, TMarketoConnectionProperties connection)
            throws IOException {
        MarketoSourceOrSink sos = new MarketoSourceOrSink();
        sos.initialize(container, connection);
        return sos.getEndpointSchema(container, schemaName);
    }

    @Override
    public Schema getEndpointSchema(RuntimeContainer container, String schemaName) throws IOException {
        MarketoRESTClient client = (MarketoRESTClient) getClientService(null);
        TMarketoInputProperties ip = new TMarketoInputProperties("retrieveSchema");
        MarketoRecordResult r = new MarketoRecordResult();
        Schema describeSchema = MarketoConstants.getCustomObjectDescribeSchema();
        ip.connection = properties.getConnectionProperties();
        switch (schemaName) {
        case RESOURCE_COMPANY:
            ip.inputOperation.setValue(InputOperation.Company);
            ip.standardAction.setValue(StandardAction.describe);
            r = client.describeCompanies(ip);
            break;
        case RESOURCE_OPPORTUNITY:
            ip.inputOperation.setValue(InputOperation.Opportunity);
            ip.standardAction.setValue(StandardAction.describe);
            r = client.describeOpportunity(ip);
            break;
        case RESOURCE_OPPORTUNITY_ROLE:
            ip.inputOperation.setValue(InputOperation.OpportunityRole);
            ip.standardAction.setValue(StandardAction.describe);
            r = client.describeOpportunity(ip);
            break;
        default:
            ip.inputOperation.setValue(InputOperation.CustomObject);
            ip.customObjectAction.setValue(CustomObjectAction.describe);
            ip.customObjectName.setValue(schemaName);
            r = client.describeCustomObject(ip);
            break;
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
        MarketoRecordResult r = new MarketoRecordResult();
        Schema describeSchema = MarketoConstants.getCustomObjectDescribeSchema();
        ip.connection = properties.getConnectionProperties();
        switch (resource) {
        case RESOURCE_COMPANY:
            ip.inputOperation.setValue(InputOperation.Company);
            ip.standardAction.setValue(StandardAction.describe);
            r = client.describeCompanies(ip);
            break;
        case RESOURCE_OPPORTUNITY:
            ip.inputOperation.setValue(InputOperation.Opportunity);
            ip.standardAction.setValue(StandardAction.describe);
            r = client.describeOpportunity(ip);
            break;
        case RESOURCE_OPPORTUNITY_ROLE:
            ip.inputOperation.setValue(InputOperation.OpportunityRole);
            ip.standardAction.setValue(StandardAction.describe);
            r = client.describeOpportunity(ip);
            break;
        default:
            ip.inputOperation.setValue(InputOperation.CustomObject);
            ip.customObjectAction.setValue(CustomObjectAction.describe);
            ip.customObjectName.setValue(resource);
            r = client.describeCustomObject(ip);
            break;
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

        return Arrays.asList(keys);
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

    public static ValidationResult validateConnection(MarketoProvideConnectionProperties properties) {
        ValidationResultMutable vr = new ValidationResultMutable();
        try {
            MarketoSourceOrSink sos = new MarketoSourceOrSink();
            sos.initialize(null, (ComponentProperties) properties);
            sos.getClientService(null);
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
                if (conn.apiMode.getValue().equals(APIMode.SOAP)) {
                    client = new MarketoSOAPClient(conn);
                } else {
                    client = new MarketoRESTClient(conn);
                }
            } catch (MarketoException e) {
                LOG.error(e.toString());
                throw new IOException(e);
            }
            LOG.debug("ClientService : {}", client);
        }
        return client;
    }

}
