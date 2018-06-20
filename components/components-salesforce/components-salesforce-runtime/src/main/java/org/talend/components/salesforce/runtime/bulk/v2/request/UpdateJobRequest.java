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

package org.talend.components.salesforce.runtime.bulk.v2.request;

import com.sforce.async.JobStateEnum;

/**
 * Store update information
 */
public class UpdateJobRequest {

    /**
     * The state to update the job to. Use UploadComplete to close a job, or Aborted to abort a job.
     */
    private final JobStateEnum state;

    private UpdateJobRequest(Builder builder) {
        this.state = builder.state;
    }

    public JobStateEnum getState() {
        return state;
    }

    public static class Builder {

        private JobStateEnum state;

        public Builder(JobStateEnum state) {
            this.state = state;
        }

        public UpdateJobRequest build() {
            return new UpdateJobRequest(this);
        }
    }
}
