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

package org.apache.beam.sdk.io.kinesis.auth;

import org.apache.beam.sdk.repackaged.org.apache.commons.lang3.StringUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;

public class BasicAWSCredentialsProvider implements AWSCredentialsProvider {

    private final String accessKey;

    private final String secretKey;

    public BasicAWSCredentialsProvider(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public AWSCredentials getCredentials() {
        if (!StringUtils.isEmpty(this.accessKey) && !StringUtils.isEmpty(this.secretKey)) {
            return new BasicAWSCredentials(this.accessKey, this.secretKey);
        } else {
            throw new AmazonClientException("Access key or secret key is null");
        }
    }

    public void refresh() {
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }
}
