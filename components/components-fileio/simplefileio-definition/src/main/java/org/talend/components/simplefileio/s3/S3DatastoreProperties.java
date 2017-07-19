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

package org.talend.components.simplefileio.s3;

import static org.talend.daikon.properties.presentation.Widget.widget;

import java.util.EnumSet;

import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class S3DatastoreProperties extends PropertiesImpl implements DatastoreProperties {

    public Property<Boolean> specifyCredentials = PropertyFactory.newBoolean("specifyCredentials", true).setRequired();

    public Property<String> accessKey = PropertyFactory.newString("accessKey");

    public Property<String> secretKey = PropertyFactory.newString("secretKey").setFlags(
            EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    public S3DatastoreProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(specifyCredentials);
        mainForm.addRow(accessKey);
        mainForm.addRow(widget(secretKey).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        // Main properties
        if (form.getName().equals(Form.MAIN)) {
            // handle S3
            final boolean isSpecifyCredentialsEnabled = specifyCredentials.getValue();
            accessKey.setRequired(isSpecifyCredentialsEnabled);
            secretKey.setRequired(isSpecifyCredentialsEnabled);
            form.getWidget(accessKey.getName()).setVisible(isSpecifyCredentialsEnabled);
            form.getWidget(secretKey.getName()).setVisible(isSpecifyCredentialsEnabled);
        }
    }

    public void afterSpecifyCredentials() {
        refreshLayout(getForm(Form.MAIN));
    }

}
