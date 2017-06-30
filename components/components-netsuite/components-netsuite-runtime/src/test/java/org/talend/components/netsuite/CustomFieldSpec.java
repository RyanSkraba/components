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

package org.talend.components.netsuite;

import java.util.Collection;

import org.talend.components.netsuite.client.model.RecordTypeDesc;
import org.talend.components.netsuite.client.model.customfield.CustomFieldRefType;

/**
 *
 */
public class CustomFieldSpec<R, C> {

    protected Class<?> fieldTypeClass;

    protected String scriptId;

    protected String internalId;

    protected R recordType;

    protected C fieldType;

    protected CustomFieldRefType fieldRefType;

    protected Collection<String> appliesTo;

    public CustomFieldSpec() {
    }

    public CustomFieldSpec(String scriptId, String internalId, R recordType, Class<?> fieldTypeClass, C fieldType,
            CustomFieldRefType fieldRefType, Collection<String> appliesTo) {
        this.setFieldTypeClass(fieldTypeClass);
        this.setScriptId(scriptId);
        this.setInternalId(internalId);
        this.setRecordType(recordType);
        this.setFieldType(fieldType);
        this.setFieldRefType(fieldRefType);
        this.setAppliesTo(appliesTo);
    }

    public Class<?> getFieldTypeClass() {
        return fieldTypeClass;
    }

    public void setFieldTypeClass(Class<?> fieldTypeClass) {
        this.fieldTypeClass = fieldTypeClass;
    }

    public String getScriptId() {
        return scriptId;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public R getRecordType() {
        return recordType;
    }

    public void setRecordType(R recordType) {
        this.recordType = recordType;
    }

    public C getFieldType() {
        return fieldType;
    }

    public void setFieldType(C fieldType) {
        this.fieldType = fieldType;
    }

    public CustomFieldRefType getFieldRefType() {
        return fieldRefType;
    }

    public void setFieldRefType(CustomFieldRefType fieldRefType) {
        this.fieldRefType = fieldRefType;
    }

    public Collection<String> getAppliesTo() {
        return appliesTo;
    }

    public void setAppliesTo(Collection<String> appliesTo) {
        this.appliesTo = appliesTo;
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("CustomFieldSpec{");
        sb.append("fieldTypeClass=").append(fieldTypeClass);
        sb.append(", scriptId='").append(scriptId).append('\'');
        sb.append(", internalId='").append(internalId).append('\'');
        sb.append(", recordType=").append(recordType);
        sb.append(", fieldType=").append(fieldType);
        sb.append(", fieldRefType=").append(fieldRefType);
        sb.append(", appliesTo=").append(appliesTo);
        sb.append('}');
        return sb.toString();
    }
}
