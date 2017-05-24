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
package org.talend.components.azurestorage.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.security.InvalidKeyException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SharedAccessSignatureUtilsTest {

    private SharedAccessSignatureUtils sharedAccessSignatureUtils;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    String account = "undx";

    String protocol = "https";

    String sap = "?sv=2015-12-11&ss=bfqt&srt=sco&sp=rwdlacup&se=2017-01-04T17:57:27Z&st=2017-01-04T09:57:27Z&spr=https&sig=E730Wcw4pz9OPDZnlw5sCOCGxHbPX4YaXMv9%2F4JinUk%3D";

    String sas = "https://undx.blob.core.windows.net/?sv=2015-12-11&ss=bfqt&srt=sco&sp=rwdlacup&se=2017-01-04T17:57:27Z&st=2017-01-04T09:57:27Z&spr=https&sig=E730Wcw4pz9OPDZnlw5sCOCGxHbPX4YaXMv9%2F4JinUk%3D";

    String service = "blob";

    /**
     *
     * @see org.talend.components.azurestorage.utils.SharedAccessSignatureUtils#getService()
     */
    @Test
    public void getServiceOther() {
        String service = sharedAccessSignatureUtils.getService();
        assertTrue("service should be not null and not empty", service != null && !service.equals(""));
    }

    /**
     *
     * @see org.talend.components.azurestorage.utils.SharedAccessSignatureUtils#getSharedAccessSignature()
     */
    @Test
    public void getSharedAccessSignatureOther() {
        String sharedaccesssignature = sharedAccessSignatureUtils.getSharedAccessSignature();
        assertEquals(sas, sharedaccesssignature);
    }

    /**
     *
     * @throws InvalidKeyException
     * @see org.talend.components.azurestorage.utils.SharedAccessSignatureUtils#getSharedAccessSignatureUtils(String)
     */
    @Test(expected = InvalidKeyException.class)
    public void getSharedAccessSignatureUtilsInvalid() throws InvalidKeyException {
        SharedAccessSignatureUtils.getSharedAccessSignatureUtils("");
    }

    public void getSharedAccessSignatureUtilsValid() throws InvalidKeyException {
        SharedAccessSignatureUtils sharedaccesssignatureutils = SharedAccessSignatureUtils.getSharedAccessSignatureUtils(sas);
        assertNotNull("sharedaccesssignatureutils cannot be null", sharedaccesssignatureutils);
    }

    @Before
    public void setUp() throws Exception {
        sharedAccessSignatureUtils = new SharedAccessSignatureUtils(sas, protocol, account, service, sap);
    }

    /**
     *
     * @see org.talend.components.azurestorage.utils.SharedAccessSignatureUtils#getAccount()
     */
    @Test
    public void testGetAccount() {
        String account = sharedAccessSignatureUtils.getAccount();
        assertEquals("undx", account);
    }

    /**
     *
     * @see org.talend.components.azurestorage.utils.SharedAccessSignatureUtils#getProtocol()
     */
    @Test
    public void testGetProtocol() {
        String protocol = sharedAccessSignatureUtils.getProtocol();
        assertEquals("https", protocol);
    }

    /**
     *
     * @see org.talend.components.azurestorage.utils.SharedAccessSignatureUtils#getService()
     */
    @Test
    public void testGetService() {
        String service = sharedAccessSignatureUtils.getService();
        assertTrue("service should be not null and equal to blob", service != null && service.equals("blob"));
    }

    /**
     *
     * @see org.talend.components.azurestorage.utils.SharedAccessSignatureUtils#getSharedAccessSignature()
     */
    @Test
    public void testGetSharedAccessSignature() {
        String sharedaccesssignature = sharedAccessSignatureUtils.getSharedAccessSignature();
        assertTrue("sharedaccesssignature should be not null and not empty",
                sharedaccesssignature != null && !sharedaccesssignature.equals(""));
    }

    /**
     *
     * @see org.talend.components.azurestorage.utils.SharedAccessSignatureUtils#getToken()
     */
    @Test
    public void testGetToken() {
        String token = sharedAccessSignatureUtils.getToken();
        assertEquals("token should be not null and not empty", sap, token);
    }

    /**
     *
     * @see org.talend.components.azurestorage.utils.SharedAccessSignatureUtils#getURI()
     */
    @Test
    public void testGetURI() throws Throwable {
        URI uri = sharedAccessSignatureUtils.getURI();
        assertNotNull("uri cannot be null", uri);
    }

    /**
     *
     * @see org.talend.components.azurestorage.utils.SharedAccessSignatureUtils#toString()
     */
    @Test
    public void testToString() {
        String result = sharedAccessSignatureUtils.toString();
        assertEquals("protocol=https; service=blob; account=undx", result);
    }

}
