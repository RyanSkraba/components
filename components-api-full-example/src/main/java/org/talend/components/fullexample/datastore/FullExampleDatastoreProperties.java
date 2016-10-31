package org.talend.components.fullexample.datastore;

import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

/**
 * Properties that can be used to configure a FullExampleDatastore.
 */
public class FullExampleDatastoreProperties extends PropertiesImpl implements DatastoreProperties {

    public Property<String> tag = PropertyFactory.newString("tag");

    public Property<Integer> tagId = PropertyFactory.newInteger("tagId");

    FullExampleDatastoreProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addColumn(tag);
        mainForm.addColumn(tagId);
    }

}
