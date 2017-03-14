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

import org.talend.components.netsuite.client.model.RefType;

/**
 *
 */
public class NsRef {
    private RefType refType;
    private String name;
    private String type;
    private String internalId;
    private String externalId;
    private String scriptId;

    public RefType getRefType() {
        return refType;
    }

    public void setRefType(RefType refType) {
        this.refType = refType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NsRef{");
        sb.append("refType='").append(refType).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", internalId='").append(internalId).append('\'');
        sb.append(", externalId='").append(externalId).append('\'');
        sb.append(", scriptId='").append(scriptId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
