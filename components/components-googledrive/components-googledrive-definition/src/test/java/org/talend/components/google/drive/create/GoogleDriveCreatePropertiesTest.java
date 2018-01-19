package org.talend.components.google.drive.create;

import org.junit.Before;
import org.talend.daikon.properties.presentation.Form;

public class GoogleDriveCreatePropertiesTest {

    private GoogleDriveCreateProperties properties;

    @Before
    public void setUp() throws Exception {
        properties = new GoogleDriveCreateProperties("test");
        properties.schemaMain.setupProperties();
        properties.schemaMain.setupLayout();
        properties.connection.setupProperties();
        properties.connection.setupLayout();
        properties.setupProperties();
        properties.setupLayout();
        properties.refreshLayout(properties.getForm(Form.MAIN));
    }
}
