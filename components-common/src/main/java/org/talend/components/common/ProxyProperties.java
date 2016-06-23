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
package org.talend.components.common;

import static org.talend.daikon.properties.property.PropertyFactory.*;

import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;

public class ProxyProperties extends PropertiesImpl {

    public enum ProxyType {
        HTTP,
        HTTPS,
        SOCKS,
        FTP
    }

    public Property<Boolean> useProxy = newBoolean("useProxy").setRequired(); //$NON-NLS-1$

    private static final String HOST = "host";

    public Property<String> host = newProperty(HOST).setRequired();

    private static final String PORT = "port";

    public Property<Integer> port = newInteger(PORT).setRequired();

    private static final String USERPASSWORD = "userPassword";

    public UserPasswordProperties userPassword = new UserPasswordProperties(USERPASSWORD);

    public ProxyProperties(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        userPassword.userId.setRequired(false);
        userPassword.password.setRequired(false);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = Form.create(this, Form.MAIN);
        form.addRow(useProxy);
        form.addRow(host);
        form.addRow(port);
        form.addRow(userPassword.getForm(Form.MAIN));
    }

    public void afterUseProxy() {
        refreshLayout(getForm(Form.MAIN));
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);
        if (form.getName().equals(Form.MAIN)) {
            boolean isUseProxy = useProxy.getValue();
            form.getWidget(HOST).setHidden(!isUseProxy);
            form.getWidget(PORT).setHidden(!isUseProxy);
            form.getWidget(USERPASSWORD).setHidden(!isUseProxy);
        }
    }

}
