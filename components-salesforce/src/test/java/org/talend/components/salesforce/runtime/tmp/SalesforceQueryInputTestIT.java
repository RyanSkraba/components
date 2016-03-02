package org.talend.components.salesforce.runtime.tmp;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.reflect.ReflectData;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.salesforce.SalesforceConnectionProperties;
import org.talend.components.salesforce.runtime.SalesforceSourceOrSink;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Property;

import com.sforce.soap.partner.sobject.SObject;

/**
 * Unit tests for the {@link SalesforceQueryInput}.
 */
@SuppressWarnings("nls")
public class SalesforceQueryInputTestIT {

    public static final String userId = System.getProperty("salesforce.user");

    public static final String password = System.getProperty("salesforce.password");

    public static final String securityKey = System.getProperty("salesforce.key");

    public static final boolean ADD_QUOTES = true;

    public static final String EXISTING_MODULE_NAME = "Account";

    public static final String NOT_EXISTING_MODULE_NAME = "foobar";

    public static SalesforceConnectionProperties setupProps(SalesforceConnectionProperties props, boolean addQuotes) {
        ComponentProperties userPassword = (ComponentProperties) props.getProperty("userPassword");
        ((Property) userPassword.getProperty("userId")).setValue(addQuotes ? "\"" + userId + "\"" : userId);
        ((Property) userPassword.getProperty("password")).setValue(addQuotes ? "\"" + password + "\"" : password);
        ((Property) userPassword.getProperty("securityKey")).setValue(addQuotes ? "\"" + securityKey + "\"" : securityKey);
        return props;
    }

    /**
     * Tests the simplest case of this input.
     */
    @Test
    public void testBasic() throws IOException {
        // Setup.
        TSalesforceInputProperties tsip = (TSalesforceInputProperties) new TSalesforceInputProperties("foo").init(); //$NON-NLS-1$
        setupProps(tsip.connection, !ADD_QUOTES);
        tsip.module.moduleName.setValue(EXISTING_MODULE_NAME);

        // Get the schema for the module.
        SalesforceSourceOrSink ss = new SalesforceSourceOrSink();
        ss.initialize(null, tsip);
        Schema querySchema = ss.getSchema((RuntimeContainer) null, EXISTING_MODULE_NAME);

        // Run and validate one record.
        try (SalesforceQueryInput in = new SalesforceQueryInput(tsip, querySchema)) {
            in.setup();

            assertThat(in.hasNext(), is(true));
            IndexedRecord record = in.next();

            assertThat(record, not(nullValue()));

            Schema.Field stringField = querySchema.getField("Id");
            Object stringValue = record.get(stringField.pos());
            assertThat(stringValue, instanceOf(String.class));

            Schema.Field booleanField = querySchema.getField("IsDeleted");
            Object booleanValue = record.get(booleanField.pos());
            assertThat(booleanValue, instanceOf(Boolean.class));

            Schema.Field doubleField = querySchema.getField("BillingLatitude");
            Object doubleValue = record.get(doubleField.pos());
            // TODO(rskraba): find a non-null double someplace.
            // assertThat(doubleValue, instanceOf(Double.class));

            Schema.Field decimalField = querySchema.getField("AnnualRevenue");
            Object decimalValue = record.get(decimalField.pos());
            // TODO(rskraba): find a non-null decimal someplace.

            Schema.Field datetimeField = querySchema.getField("CreatedDate");
            Object datetimeValue = record.get(datetimeField.pos());
            assertThat(datetimeValue, instanceOf(Long.class));
            // TODO(rskraba): we probably don't want this to be a long.

            // TODO(rskraba): find a date field somewhere.

            // TODO(rskraba): find an integer field someplace.

            // Make sure the record is a valid IndexedRecord for its Schema.
            ReflectData.get().validate(record.getSchema(), record);

            // Dump the entire object to the screen.
            // for (Schema.Field f : querySchema.getFields()) {
            // Object x = record.get(f.pos());
            // if (x != null) {
            // System.out.println(f.name() + " (" + x.getClass() + "): " + x);
            // } else {
            // System.out.println(f.name() + " NULL : " + x);
            // }
            // }
        }
    }

    /**
     * Not really a test, used to explore the data returned from the Salesforce connection during an integration test.
     * 
     * @throws IOException
     */
    @Ignore
    @Test
    public void testAllModules() throws IOException {
        // Setup.
        TSalesforceInputProperties tsip = (TSalesforceInputProperties) new TSalesforceInputProperties("foo").init(); //$NON-NLS-1$
        setupProps(tsip.connection, !ADD_QUOTES);

        // Get the schema for the module.
        SalesforceSourceOrSink ss = new SalesforceSourceOrSink();
        ss.initialize(null, tsip);
        List<NamedThing> moduleNames = ss.getSchemaNames((RuntimeContainer) null);

        Map<String, Schema> moduleSchema = new HashMap<>();
        Map<String, List<SObject>> moduleData = new HashMap<>();
        Map<String, Exception> moduleCantQuery = new HashMap<>();

        // Get a bunch of test data from each module.
        for (NamedThing module : moduleNames) {
            tsip.module.moduleName.setValue(module.getName());
            Schema querySchema = ss.getSchema((RuntimeContainer) null, module.getName());
            moduleSchema.put(module.getName(), querySchema);
            try (SalesforceQueryInput in = new SalesforceQueryInput(tsip, querySchema)) {
                in.setup();
                List<SObject> testSO = new ArrayList<>();
                for (int i = 0; in.hasNext() && i < 10; i++) {
                    testSO.add(in.nextSObjectToWrap());
                }
                moduleData.put(module.getName(), testSO);
            } catch (RuntimeException e) {
                moduleCantQuery.put(module.getName(), e);
            }
        }

        // Output the data.
        System.out.println("Hello");

    }
}
