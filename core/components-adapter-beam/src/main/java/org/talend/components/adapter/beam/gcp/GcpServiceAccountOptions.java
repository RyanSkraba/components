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

package org.talend.components.adapter.beam.gcp;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.GcpOptions;
import org.apache.beam.sdk.options.PipelineOptions;

/**
 * Options used to configure Google Cloud Platform service account credentials
 *
 * <p>
 * These options defer to the <a href="https://developers.google.com/identity/protocols/OAuth2ServiceAccount"> Service
 * account credentials</a> for authentication.
 */
@Description("Options used to configure Google Cloud Platform service account credentials.")
public interface GcpServiceAccountOptions extends GcpOptions, PipelineOptions {

    /**
     * Service account token file path
     */
    @Description("Service account token file path.")
    String getServiceAccountFile();
    void setServiceAccountFile(String value);
}
