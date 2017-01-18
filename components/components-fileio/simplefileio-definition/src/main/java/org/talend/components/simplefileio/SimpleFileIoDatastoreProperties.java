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

package org.talend.components.simplefileio;

import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class SimpleFileIoDatastoreProperties extends PropertiesImpl implements DatastoreProperties {

    public Property<String> userName = PropertyFactory.newString("userName");

    public Property<Boolean> useKerberos = PropertyFactory.newBoolean("useKerberos", false);

    public Property<String> kerberosPrincipal = PropertyFactory.newString("kerberosPrincipal", "username@EXAMPLE.COM");

    public Property<String> kerberosKeytab = PropertyFactory.newString("kerberosKeytab", "/home/username/username.keytab");

    public SimpleFileIoDatastoreProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form mainForm = new Form(this, Form.MAIN);
        mainForm.addRow(useKerberos);
        mainForm.addRow(kerberosPrincipal);
        mainForm.addRow(kerberosKeytab);
        mainForm.addRow(userName);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        // Main properties
        if (form.getName().equals(Form.MAIN)) {
            form.getWidget(kerberosPrincipal.getName()).setVisible(useKerberos);
            form.getWidget(kerberosKeytab.getName()).setVisible(useKerberos);
            form.getWidget(userName.getName()).setHidden(useKerberos);
        }
    }

    public void afterUseKerberos() {
        refreshLayout(getForm(Form.MAIN));
    }
}
