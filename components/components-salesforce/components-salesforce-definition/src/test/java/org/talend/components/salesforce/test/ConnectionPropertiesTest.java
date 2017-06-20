package org.talend.components.salesforce.test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.salesforce.TestUtils;
import org.talend.components.salesforce.tsalesforceinput.TSalesforceInputProperties;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.serialize.SerializerDeserializer;

public class ConnectionPropertiesTest {

    @Test
    public void testDeserializeOldReferenceProps() throws IOException {
        String oldPropsStr = TestUtils.getResourceAsString(getClass(),"tSalesforceInputConnectionProperties_old.json");
        SerializerDeserializer.Deserialized<TSalesforceInputProperties> fromSerializedPersistent = Properties.Helper
                .fromSerializedPersistent(oldPropsStr, TSalesforceInputProperties.class);
        TSalesforceInputProperties deserializedProps = fromSerializedPersistent.object;
        ComponentReferenceProperties deSerRefProps = (ComponentReferenceProperties) deserializedProps
                .getProperty("connection.referencedComponent");
        assertEquals("tSalesforceConnection", deSerRefProps.referenceDefinitionName.getValue());
        assertEquals("tSalesforceConnection_1", deSerRefProps.componentInstanceId.getValue());
        assertEquals(ComponentReferenceProperties.ReferenceType.COMPONENT_INSTANCE, deSerRefProps.referenceType.getValue());
    }

}
