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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.commons.codec.binary.Base64;

/**
 * A 509 certificate handler to load certificate from a key store and use it to sign data
 */
public class X509Key {

    public enum Algorithm {

        SHA256withRSA,
    }

    private static final String storeTypeKJS = "JKS";

    private static final String charSetUtf8 = "UTF-8";

    private PrivateKey privateKey;

    private X509Certificate publicKey;

    private X509Key(Builder b) {

        try (InputStream keyStoreIS = new FileInputStream(b.keyStorePath)) {

            KeyStore keystore = KeyStore.getInstance(storeTypeKJS);
            keystore.load(keyStoreIS, b.keyStorePassword.toCharArray());
            this.privateKey = (PrivateKey) keystore.getKey(b.certificateAlias, b.keyStorePassword.toCharArray());
            this.publicKey = (X509Certificate) keystore.getCertificate(b.certificateAlias);

            if (privateKey == null || publicKey == null) {
                throw new RuntimeException("Certificate " + b.certificateAlias + " can't be found in the store" + b.keyStorePath);
            }

        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException
                | UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * sign data with private key using algo
     */
    public byte[] sign(String data, Algorithm algo) {

        try {
            // Sign the JWT Header + "." + JWT Claims Object
            Signature signature = Signature.getInstance(algo.name());
            signature.initSign(privateKey);
            signature.update(data.getBytes(charSetUtf8));
            return signature.sign();

        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Base64 encoded hash of the the public certificate.
     * 
     * @return base64 endoded string
     * @throws CertificateEncodingException
     * @throws NoSuchAlgorithmException
     */
    public String getPublicCertificateHash() throws CertificateEncodingException, NoSuchAlgorithmException {
        return Base64.encodeBase64String(getHash(this.publicKey.getEncoded()));
    }

    /**
     * Base64 encoded public certificate.
     * 
     * @return base64 endoded string
     * @throws CertificateEncodingException
     * @throws NoSuchAlgorithmException
     */
    public String getPublicCertificate() throws CertificateEncodingException, NoSuchAlgorithmException {
        return Base64.encodeBase64String(this.publicKey.getEncoded());
    }

    private static byte[] getHash(final byte[] inputBytes) throws NoSuchAlgorithmException, CertificateEncodingException {
        final MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(inputBytes);
        return md.digest();

    }

    // Builder
    public static KeyStorePath builder() {
        return new Builder();
    }

    private static class Builder implements Build, KeyStorePath, KeyStorePasswd, CertificateAlias {

        private String keyStorePath;

        private String keyStorePassword;

        private String certificateAlias;

        @Override
        public KeyStorePasswd keyStorePath(String keyStorePath) {
            this.keyStorePath = keyStorePath;
            return this;
        }

        @Override
        public CertificateAlias keyStorePassword(String keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
            return this;
        }

        @Override
        public Build certificateAlias(String certificateAlias) {
            this.certificateAlias = certificateAlias;
            return this;
        }

        @Override
        public X509Key build() {
            return new X509Key(this);
        }
    }

    public interface KeyStorePath {

        public KeyStorePasswd keyStorePath(String certificateStorePath);
    }

    public interface KeyStorePasswd {

        public CertificateAlias keyStorePassword(String certificateStorePassword);
    }

    public interface CertificateAlias {

        /**
         * The certificate alias in the store
         */
        public Build certificateAlias(String certificateName);
    }

    public interface Build {

        public X509Key build();
    }

}
