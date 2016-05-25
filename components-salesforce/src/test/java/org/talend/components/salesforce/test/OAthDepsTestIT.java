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
package org.talend.components.salesforce.test;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.talend.components.api.service.ComponentService;
import org.talend.components.common.oauth.OauthProperties;
import org.talend.components.salesforce.connection.oauth.SalesforceOAuthConnection;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OAthDepsTestIT {

    @Inject
    ComponentService osgiCompService;

    @Configuration
    public Option[] config() {
        return OsgiSalesfoceComponentTestIT.getSalesforcePaxExamOption();
    }

    @Test(expected = NullPointerException.class)
    public void setypComponentService() {
        // this the only checks that all import and export for OAuth ahtentication are correctly set for OSGI.
        SalesforceOAuthConnection salesforceOAuthConnection = new SalesforceOAuthConnection(new OauthProperties("foo"),
                "http://localhost", null);
        salesforceOAuthConnection.login(null);// this should not throw ClassNotFoundException
    }

}
