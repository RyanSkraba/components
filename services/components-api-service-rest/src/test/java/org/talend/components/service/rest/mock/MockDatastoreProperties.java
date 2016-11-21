package org.talend.components.service.rest.mock;

import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.daikon.properties.PropertiesImpl;

/**
 * Mock catastore properties for tests.
 */
public class MockDatastoreProperties extends PropertiesImpl implements DatastoreProperties {

    /**
     * Default constructor.
     * @param name the properties name.
     */
    public MockDatastoreProperties(String name) {
        super(name);
    }

}
