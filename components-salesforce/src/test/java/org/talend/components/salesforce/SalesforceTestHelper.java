// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.salesforce;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.talend.components.api.adaptor.Adaptor;
import org.talend.components.api.adaptor.ComponentDynamicHolder;
import org.talend.components.api.component.runtime.BoundedReader;
import org.talend.components.api.component.runtime.Writer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.salesforce.runtime.SalesforceSource;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.schema.Schema;
import org.talend.daikon.schema.SchemaElement;
import org.talend.daikon.schema.SchemaFactory;

public class SalesforceTestHelper {

    public static final boolean ADD_QUOTES = true;

    public static final String userId = System.getProperty("salesforce.user");

    public static final String password = System.getProperty("salesforce.password");

    public static final String securityKey = System.getProperty("salesforce.key");

    public static SalesforceConnectionProperties setupProps(SalesforceConnectionProperties props, boolean addQuotes) {
        if (props == null) {
            props = (SalesforceConnectionProperties) new SalesforceConnectionProperties("foo").init();
        }
        ComponentProperties userPassword = (ComponentProperties) props.getProperty("userPassword");
        ((Property) userPassword.getProperty("userId")).setValue(addQuotes ? "\"" + userId + "\"" : userId);
        ((Property) userPassword.getProperty("password")).setValue(addQuotes ? "\"" + password + "\"" : password);
        ((Property) userPassword.getProperty("securityKey")).setValue(addQuotes ? "\"" + securityKey + "\"" : securityKey);
        return props;
    }

    public static final String EXISTING_MODULE_NAME = "Account";

    public static final String NOT_EXISTING_MODULE_NAME = "foobar";

    public static final String TEST_KEY = "Address2 456";

    protected static ComponentDynamicHolder setupDynamic(SchemaElement schemaElement, Adaptor adaptor) {
        for (SchemaElement se : schemaElement.getChildren()) {
            if (se.getType() == SchemaElement.Type.DYNAMIC) {
                ComponentDynamicHolder dynamic = adaptor.createDynamicHolder();
                Schema dynSchema = SchemaFactory.newSchema();
                dynSchema.setRoot(SchemaFactory.newSchemaElement(SchemaElement.Type.STRING, "Root"));
                dynSchema.getRoot().addChild(SchemaFactory.newSchemaElement(SchemaElement.Type.STRING, "ShippingState"));
                dynamic.setSchemaElements(dynSchema.getRoot().getChildren());
                return dynamic;
            }
        }
        return null;
    }

    public static void fixSchemaForDynamic(SchemaElement schemaElement) {
        SchemaElement dynElement = SchemaFactory.newSchemaElement(SchemaElement.Type.DYNAMIC, "dynamic");
        schemaElement.addChild(dynElement);
        Iterator<SchemaElement> it = schemaElement.getChildren().iterator();
        while (it.hasNext()) {
            SchemaElement se = it.next();
            if (se.getName().equals("ShippingState")) {
                it.remove();
                break;
            }
        }
    }

    public static void addDynamicColumn(Map<String, Object> row, Schema schema, boolean isDynamic, Adaptor adaptator) {
        if (isDynamic) {
            ComponentDynamicHolder dynamic = setupDynamic(schema.getRoot(), adaptator);
            dynamic.addFieldValue("ShippingState", "CA");
            row.put("dynamic", dynamic);
        }
    }

     public static <T> T writeRows(Writer<T> writer, List<Map<String, Object>> outputRows) throws IOException {
        T result = null;
        writer.open("foo");
        try {
            for (Map<String, Object> row : outputRows) {
                writer.write(row);
            }
        } finally {
            result = writer.close();
        }
        return result;
    }

    public static List<Map<String, Object>> makeRows(int count, boolean isDynamic, Schema schema, Adaptor adaptator,
            String billingPostalCode) {
        List<Map<String, Object>> outputRows = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("Name", "TestName");
            row.put("ShippingStreet", TEST_KEY);
            row.put("ShippingPostalCode", Integer.toString(i));
            row.put("BillingStreet", "123 Main Street");
            row.put("BillingState", "CA");
            row.put("BillingPostalCode", billingPostalCode);
            addDynamicColumn(row, schema, isDynamic, adaptator);
            System.out.println("out: " + row.get("Name") + " id: " + row.get("Id") + " post: " + row.get("BillingPostalCode")
                    + " st: " + " street: " + row.get("BillingStreet"));
            outputRows.add(row);
        }
        return outputRows;
    }

    protected List<Map<String, Object>> checkRows(List<Map<String, Object>> rows, int count) {
        List<Map<String, Object>> checkedRows = new ArrayList<>();

        int checkCount = 0;
        int checkDynamicCount = 0;
        for (Map<String, Object> row : rows) {
            System.out.println("check: " + row.get("Name") + " id: " + row.get("Id") + " post: " + row.get("BillingPostalCode")
                    + " st: " + " post: " + row.get("BillingStreet"));
            String check = (String) row.get("ShippingStreet");
            if (check == null || !check.equals(SalesforceTestHelper.TEST_KEY)) {
                continue;
            }
            check = (String) row.get("BillingPostalCode");
            if (check == null || !check.equals(random)) {
                continue;
            }
            checkCount++;
            if (dynamic != null) {
                ComponentDynamicHolder d = (ComponentDynamicHolder) row.get("dynamic");
                assertEquals("CA", d.getFieldValue("ShippingState"));
                checkDynamicCount++;
            }
            assertEquals("TestName", row.get("Name"));
            assertEquals("123 Main Street", row.get("BillingStreet"));
            assertEquals("CA", row.get("BillingState"));
            checkedRows.add(row);
        }
        assertEquals(count, checkCount);
        if (dynamic != null) {
            assertEquals(count, checkDynamicCount);
            System.out.println("Check dynamic rows: " + checkDynamicCount);
        }
        return checkedRows;
    }

    static public List<String> getDeleteIds(List<Map<String, Object>> rows) {
        List<String> ids = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            System.out.println("del: " + row.get("Name") + " id: " + row.get("Id") + " post: " + row.get("BillingPostalCode")
                    + " st: " + " post: " + row.get("BillingStreet"));
            String check = (String) row.get("ShippingStreet");
            if (check == null || !check.equals(SalesforceTestHelper.TEST_KEY)) {
                continue;
            }
            ids.add((String) row.get("Id"));
        }
        return ids;
    }

    protected List<Map<String, Object>> readAndCheckRows(SalesforceRuntime runtime, SalesforceConnectionModuleProperties props,
                                                         int count) throws Exception {
        List<Map<String, Object>> inputRows = new ArrayList<>();
        TSalesforceInputProperties inputProps = (TSalesforceInputProperties) getComponentService()
                .getComponentProperties(TSalesforceInputDefinition.COMPONENT_NAME);
        inputProps.connection = props.connection;
        inputProps.module = props.module;
        inputProps.batchSize.setValue(200);
        runtime.input(inputProps, inputRows);
        return checkRows(inputRows, count);
    }

    // Returns the rows written (having been re-read so they have their Ids)
    protected List<Map<String, Object>> writeRows(SalesforceRuntime runtime, SalesforceConnectionModuleProperties props,
                                                  List<Map<String, Object>> outputRows) throws Exception {
        TSalesforceOutputProperties outputProps;
        outputProps = (TSalesforceOutputProperties) getComponentService()
                .getComponentProperties(TSalesforceOutputDefinition.COMPONENT_NAME);
        outputProps.connection = props.connection;
        outputProps.module = props.module;
        outputProps.outputAction.setValue(TSalesforceOutputProperties.OutputAction.INSERT);
        runtime.output(outputProps, outputRows);
        return readAndCheckRows(runtime, props, outputRows.size());
    }

    protected void deleteRows(SalesforceRuntime runtime, List<Map<String, Object>> inputRows) throws Exception {
        List<String> ids = getDeleteIds(inputRows);
        for (String id : ids) {
            runtime.delete(id);
        }
    }

    protected void checkAndDelete(SalesforceRuntime runtime, SalesforceConnectionModuleProperties props, int count)
            throws Exception {
        List<Map<String, Object>> inputRows = readAndCheckRows(runtime, props, count);
        deleteRows(runtime, inputRows);
        readAndCheckRows(runtime, props, 0);
    }

    public static void deleteAllAccountTestRows() throws ConnectionException, AsyncApiException, Exception {
        SalesforceRuntime runtime = new SalesforceRuntime(new DefaultComponentRuntimeContainerImpl());
        SalesforceComponentTestIT salesforceComponentTestIT = new SalesforceComponentTestIT();
        TSalesforceInputProperties props = (TSalesforceInputProperties) new TSalesforceInputProperties("foo").init();
        SalesforceTestHelper.setupProps(props.connection, DO_NOT_ADD_QUOTES);
        props.batchSize.setValue(200);
        props.module.moduleName.setValue("Account");
        // connecting
        runtime.connect(props.connection);
        // getting schema
        props.module.schema.schema.setValue(runtime.getSchema("Account"));
        // getting all rows
        List<Map<String, Object>> rows = new ArrayList<>();
        runtime.input(props, rows);
        // filtering rows
        List<Map<String, Object>> rowToBeDeleted = getAllTestRows(rows);
        // deleting rows
        List<String> ids = salesforceComponentTestIT.getDeleteIds(rowToBeDeleted);
        for (String id : ids) {
            runtime.delete(id);
        }
    }

    static List<Map<String, Object>> getAllTestRows(List<Map<String, Object>> rows) {
        List<Map<String, Object>> checkedRows = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            String check = (String) row.get("ShippingStreet");
            if (check == null || !check.equals(SalesforceTestHelper.TEST_KEY)) {
                continue;
            }
            System.out.println("Test row is: " + row.get("Name") + " id: " + row.get("Id") + " post: "
                    + row.get("BillingPostalCode") + " st: " + " post: " + row.get("BillingStreet"));
            checkedRows.add(row);
        }
        return checkedRows;
    }

    public static BoundedReader createSalesforceInputReaderFromAccount(String moduleName) {
        TSalesforceInputProperties tsip = (TSalesforceInputProperties) new TSalesforceInputProperties("foo").init(); //$NON-NLS-1$
        SalesforceConnectionProperties conProps = setupProps(tsip.connection, !ADD_QUOTES);
        tsip.module.moduleName.setValue(moduleName);
        return createBounderReader(tsip);
    }

    public static BoundedReader createBounderReader(ComponentProperties tsip) {
        SalesforceSource salesforceSource = new SalesforceSource();
        salesforceSource.initialize(null, tsip);
        return salesforceSource.createReader(null);
    }

    static public List<String> getDeleteIds(List<Map<String, Object>> rows) {
        List<String> ids = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            System.out.println("del: " + row.get("Name") + " id: " + row.get("Id") + " post: " + row.get("BillingPostalCode")
                    + " st: " + " post: " + row.get("BillingStreet"));
            String check = (String) row.get("ShippingStreet");
            if (check == null || !check.equals(SalesforceTestHelper.TEST_KEY)) {
                continue;
            }
            ids.add((String) row.get("Id"));
        }
        return ids;
    }

    /**
     * @return the list of row match the TEST_KEY, and if a random values it specified it also filter row against the
     */
    public static List<Map<String, Object>> filterAllTestRows(List<Map<String, Object>> rows, String randomValue) {
        List<Map<String, Object>> checkedRows = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            String check = (String) row.get("ShippingStreet");
            if (check == null || !check.equals(SalesforceTestHelper.TEST_KEY)) {
                continue;
            }
            if (randomValue != null) {// check the random value if specified
                check = (String) row.get("BillingPostalCode");
                if (check == null || !check.equals(randomValue)) {
                    continue;
                }
            }
            System.out.println("Test row is: " + row.get("Name") + " id: " + row.get("Id") + " post: "
                    + row.get("BillingPostalCode") + " st: " + " post: " + row.get("BillingStreet"));
            checkedRows.add(row);
        }
        return checkedRows;
    }

}
