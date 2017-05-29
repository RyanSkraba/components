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

package org.talend.components.netsuite.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds information about NetSuite status (read/write operation status etc.).
 *
 * <p>This data object is mirror of NetSuite's native {@code Status} data object.
 */
public class NsStatus {

    /** Indicates whether status is 'success'. */
    private boolean success;

    /** Details of status. */
    private List<Detail> details;

    public NsStatus() {
    }

    public NsStatus(boolean success, List<Detail> details) {
        this.success = success;
        this.details = new ArrayList<>(details);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Detail> getDetails() {
        if (details == null) {
            details = new ArrayList<>();
        }
        return details;
    }

    public void setDetails(List<Detail> details) {
        this.details = details;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NsStatus{");
        sb.append("success=").append(success);
        sb.append(", details=").append(details);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Type of status.
     *
     * <p>This enum is mirror of NetSuite's native {@code StatusDetailType} enum.
     */
    public enum Type {
        ERROR, WARN, INFO
    }

    /**
     * Holds information about NetSuite status detail.
     *
     * <p>This data object is mirror of NetSuite's native {@code StatusDetail} data object.
     */
    public static class Detail {
        private Type type;
        private String code;
        private String message;

        public Detail() {
        }

        public Detail(Type type, String code, String message) {
            this.type = type;
            this.code = code;
            this.message = message;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Detail{");
            sb.append("type=").append(type);
            sb.append(", code='").append(code).append('\'');
            sb.append(", message='").append(message).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
}
