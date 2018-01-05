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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;

public class AnonymousAWSCredentialsProvider implements AWSCredentialsProvider {

    public AnonymousAWSCredentialsProvider() {
    }

    public AWSCredentials getCredentials() {
        return new AnonymousAWSCredentials();
    }

    public void refresh() {
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }
}
