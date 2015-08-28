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

import org.talend.components.base.ComponentProperties;
import org.talend.components.base.properties.Property;
import org.talend.components.base.properties.presentation.Layout;

public class OauthProperties extends ComponentProperties {

    public Property<String> clientId = new Property<String>("clientId", "Client Id").setRequired(true);

    public Property<String> clientSecret = new Property<String>("clientSecret", "Client Secret").setRequired(true);

    public Property<String> callbackHost = new Property<String>("callbackHost", "Callback Host").setRequired(true);

    public Property<Integer> callbackPort = new Property<Integer>("callbackPort", "Callback Port").setRequired(true);

    public Property<String> tokenFile = new Property<String>("tokenFile", "Token File").setRequired(true);

    public OauthProperties() {
        clientId.setLayout(Layout.create().setRow(0));
        clientSecret.setLayout(Layout.create().setRow(1));
        callbackHost.setLayout(Layout.create().setRow(2));
        callbackPort.setLayout(Layout.create().setRow(3));
        tokenFile.setLayout(Layout.create().setRow(4));
    }

}
