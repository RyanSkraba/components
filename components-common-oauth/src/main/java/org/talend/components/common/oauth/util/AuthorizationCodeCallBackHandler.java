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
package org.talend.components.common.oauth.util;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class AuthorizationCodeCallBackHandler extends AbstractHandler {

    String code;

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        if (code == null) {
            try {
                OAuthAuthzResponse oauthCodeAuthzResponse = OAuthAuthzResponse.oauthCodeAuthzResponse(request);
                code = oauthCodeAuthzResponse.getCode();
            } catch (OAuthProblemException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (code != null) {
                response.getWriter().println("<p>Successful to get authorization code:" + code + "</p>");
            }
        }
        response.flushBuffer();
    }

    public String getCode() {
        return code;
    }
}
