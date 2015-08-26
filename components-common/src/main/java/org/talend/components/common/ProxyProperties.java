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

import org.talend.component.ComponentProperties;
import org.talend.component.properties.Property;
import org.talend.component.properties.presentation.Layout;

public class ProxyProperties extends ComponentProperties {

    public Property<Boolean> useProxy = new Property<Boolean>("useProxy", "Use Proxy").setRequired(true);

    public Property<String> host = new Property<String>("host", "Host").setRequired(true);

    public Property<String> userName = new Property<String>("userName", "User name").setRequired(true);

    public Property<String> password = new Property<String>("password", "Password").setRequired(true);

    public ProxyProperties() {
        useProxy.setLayout(Layout.create().setRow(0));
        host.setLayout(Layout.create().setRow(1));
        userName.setLayout(Layout.create().setRow(2));
        password.setLayout(Layout.create().setRow(3));
    }

}
