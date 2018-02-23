// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.google.drive.runtime.client;

import java.awt.*;
import java.io.IOException;

import org.eclipse.swt.program.Program;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.util.Preconditions;

public class AuthorizationCodeInstalledAppTalend extends AuthorizationCodeInstalledApp {

    private transient static final Logger LOG = LoggerFactory.getLogger(AuthorizationCodeInstalledAppTalend.class);

    public AuthorizationCodeInstalledAppTalend(AuthorizationCodeFlow flow, VerificationCodeReceiver receiver) {
        super(flow, receiver);
    }

    public static void browseWithProgramLauncher(String url) {
        Preconditions.checkNotNull(url);
        LOG.warn("Trying to open '{}'...", url);
        String osName = System.getProperty("os.name");
        String osNameMatch = osName.toLowerCase();
        if (osNameMatch.contains("linux")) {
            if (Program.findProgram("hmtl") == null) {
                try {
                    Runtime.getRuntime().exec("xdg-open " + url);
                    return;
                } catch (IOException e) {
                    LOG.error("Failed to open url via xdg-open: {}.", e.getMessage());
                }
            }
        }
        Program.launch(url);
    }

    @Override
    protected void onAuthorization(AuthorizationCodeRequestUrl authorizationUrl) throws IOException {
        try {
            Desktop.isDesktopSupported();
            browse(authorizationUrl.build());
        } catch (Exception e) {
            LOG.warn("Failed to open browser with Desktop browse method.");
            browseWithProgramLauncher(authorizationUrl.build());
        }
    }
}
