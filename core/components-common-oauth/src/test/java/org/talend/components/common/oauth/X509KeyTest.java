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
package org.talend.components.common.oauth;

import org.junit.Test;

public class X509KeyTest {

    @Test(expected = RuntimeException.class)
    public void testWrongPassword() {
        X509Key.builder()//
                .keyStorePath(getClass().getClassLoader().getResource("00D0Y000001dveq.jks").getPath())//
                .keyStorePassword("wrongPass")// store pwd
                .certificateAlias("jobcert")// certificate alias
                .build();
    }

    @Test(expected = RuntimeException.class)
    public void testWrongCertificateAlias() {
        X509Key.builder()//
                .keyStorePath(getClass().getClassLoader().getResource("00D0Y000001dveq.jks").getPath())//
                .keyStorePassword("talend2017")// store pwd
                .certificateAlias("wrongAlias")// certificate alias
                .build();
    }

}
