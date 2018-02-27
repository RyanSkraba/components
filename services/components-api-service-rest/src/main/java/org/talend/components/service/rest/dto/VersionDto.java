// ==============================================================================
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
// ==============================================================================

package org.talend.components.service.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO used for the version information..
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VersionDto {

    public final static String N_A = "N/A";

    /** Set to the version of this artifact at the time of build. */
    private String version = N_A;

    /** Set to the GIT commit id of this artifact at the time of build. */
    private String commit = N_A;

    /** Set to the time of build. */
    private String time = N_A;

    /**
     * Default empty constructor.
     */
    public VersionDto() {
    }

    public VersionDto(String version, String commit, String time) {
        setVersion(version);
        setCommit(commit);
        setTime(time);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "VersionDto{" + //
                "version='" + version + '\'' + //
                ", commit='" + commit + '\'' + //
                ", time=" + time + //
                '}';
    }
}
