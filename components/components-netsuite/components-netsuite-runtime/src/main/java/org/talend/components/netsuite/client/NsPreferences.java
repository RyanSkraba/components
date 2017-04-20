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

/**
 *
 */
public class NsPreferences {
    protected Boolean warningAsError;
    protected Boolean disableMandatoryCustomFieldValidation;
    protected Boolean disableSystemNotesForCustomFields;
    protected Boolean ignoreReadOnlyFields;
    protected Boolean runServerSuiteScriptAndTriggerWorkflows;

    public Boolean getWarningAsError() {
        return warningAsError;
    }

    public void setWarningAsError(Boolean value) {
        this.warningAsError = value;
    }

    public Boolean getDisableMandatoryCustomFieldValidation() {
        return disableMandatoryCustomFieldValidation;
    }

    public void setDisableMandatoryCustomFieldValidation(Boolean value) {
        this.disableMandatoryCustomFieldValidation = value;
    }

    public Boolean getDisableSystemNotesForCustomFields() {
        return disableSystemNotesForCustomFields;
    }

    public void setDisableSystemNotesForCustomFields(Boolean value) {
        this.disableSystemNotesForCustomFields = value;
    }

    public Boolean getIgnoreReadOnlyFields() {
        return ignoreReadOnlyFields;
    }

    public void setIgnoreReadOnlyFields(Boolean value) {
        this.ignoreReadOnlyFields = value;
    }

    public Boolean getRunServerSuiteScriptAndTriggerWorkflows() {
        return runServerSuiteScriptAndTriggerWorkflows;
    }

    public void setRunServerSuiteScriptAndTriggerWorkflows(Boolean value) {
        this.runServerSuiteScriptAndTriggerWorkflows = value;
    }

}
