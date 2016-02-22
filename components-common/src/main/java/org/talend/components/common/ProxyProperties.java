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

import static org.talend.daikon.properties.PropertyFactory.*;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.daikon.properties.Property;
import org.talend.daikon.properties.Property.Type;
import org.talend.daikon.properties.presentation.Form;

public class ProxyProperties extends ComponentProperties {

    public Property useProxy = (Property) newProperty(Type.BOOLEAN, "useProxy").setRequired(true); //$NON-NLS-1$

    private static final String HOST = "host";

    public Property host = (Property) newProperty(HOST).setRequired(true);

    private static final String PORT = "port";

    public Property port = (Property) newInteger(PORT).setRequired(true);

    private static final String USERPASSWORD = "userPassword";

    public UserPasswordProperties userPassword = new UserPasswordProperties(USERPASSWORD);

    public ProxyProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = Form.create(this, Form.MAIN, "Proxy Parameters");
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
            boolean isUseProxy = useProxy.getBooleanValue();
            form.getWidget(HOST).setVisible(isUseProxy);
            form.getWidget(PORT).setVisible(isUseProxy);
            form.getWidget(USERPASSWORD).setVisible(isUseProxy);
        }
    }

}
