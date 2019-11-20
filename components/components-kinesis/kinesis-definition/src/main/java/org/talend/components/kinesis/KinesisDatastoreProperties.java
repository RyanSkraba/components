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

package org.talend.components.kinesis;

import static org.talend.daikon.properties.presentation.Widget.widget;

import java.util.EnumSet;

import org.talend.components.api.properties.ComponentBasePropertiesImpl;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class KinesisDatastoreProperties extends ComponentBasePropertiesImpl implements DatastoreProperties {

    public Property<Boolean> specifyCredentials = PropertyFactory.newBoolean("specifyCredentials", true);

    public Property<String> accessKey = PropertyFactory.newString("accessKey");

    public Property<String> secretKey = PropertyFactory.newString("secretKey").setFlags(
            EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    public Property<Boolean> specifyEndpoint = PropertyFactory.newBoolean("specifyEndpoint", false);

    // http://docs.aws.amazon.com/general/latest/gr/rande.html#ak_region
    public Property<String> endpoint = PropertyFactory.newString("endpoint", "kinesis.us-east-1.amazonaws.com");

    public Property<Boolean> specifySTS = PropertyFactory.newBoolean("specifySTS", false);

    public Property<String> roleArn = PropertyFactory.newString("roleArn");

    public Property<String> roleSessionName = PropertyFactory.newString("roleSessionName");

    public Property<Boolean> specifyRoleExternalId = PropertyFactory.newBoolean("specifyRoleExternalId", false);

    public Property<String> roleExternalId = PropertyFactory.newString("roleExternalId");

    public Property<Boolean> specifySTSEndpoint = PropertyFactory.newBoolean("specifySTSEndpoint", false);

    // http://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_temp_enable-regions.html
    public Property<String> stsEndpoint = PropertyFactory.newString("stsEndpoint", "sts.amazonaws.com");

    public KinesisDatastoreProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(specifyCredentials);
        mainForm.addRow(accessKey);
        mainForm.addRow(widget(secretKey).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
        mainForm.addRow(specifyEndpoint);
        mainForm.addRow(endpoint);
        mainForm.addRow(specifySTS);
        mainForm.addRow(roleArn);
        mainForm.addRow(roleSessionName);
        mainForm.addRow(specifyRoleExternalId);
        mainForm.addRow(roleExternalId);
        mainForm.addRow(specifySTSEndpoint);
        mainForm.addRow(stsEndpoint);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {
            final boolean isSpecifyCredentialsEnabled = specifyCredentials.getValue();
            accessKey.setRequired(isSpecifyCredentialsEnabled);
            secretKey.setRequired(isSpecifyCredentialsEnabled);
            form.getWidget(accessKey).setVisible(isSpecifyCredentialsEnabled);
            form.getWidget(secretKey).setVisible(isSpecifyCredentialsEnabled);

            endpoint.setRequired(specifyEndpoint.getValue());
            form.getWidget(endpoint).setVisible(specifyEndpoint.getValue());

            final boolean isSpecifySTS = specifySTS.getValue();
            roleArn.setRequired(isSpecifySTS);
            roleSessionName.setRequired(isSpecifySTS);
            form.getWidget(roleArn).setVisible(isSpecifySTS);
            form.getWidget(roleSessionName).setVisible(isSpecifySTS);

            specifyRoleExternalId.setRequired(isSpecifySTS);
            form.getWidget(specifyRoleExternalId).setVisible(isSpecifySTS);
            roleExternalId.setRequired(isSpecifySTS && specifyRoleExternalId.getValue());
            form.getWidget(roleExternalId).setVisible(isSpecifySTS && specifyRoleExternalId.getValue());

            specifySTSEndpoint.setRequired(isSpecifySTS);
            form.getWidget(specifySTSEndpoint).setVisible(isSpecifySTS);
            stsEndpoint.setRequired(isSpecifySTS && specifySTSEndpoint.getValue());
            form.getWidget(stsEndpoint).setVisible(isSpecifySTS && specifySTSEndpoint.getValue());

        }
    }

    public void afterSpecifyCredentials() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterSpecifySTS() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterSpecifyEndpoint() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterSpecifyRoleExternalId() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterSpecifySTSEndpoint() {
        refreshLayout(getForm(Form.MAIN));
    }

}
