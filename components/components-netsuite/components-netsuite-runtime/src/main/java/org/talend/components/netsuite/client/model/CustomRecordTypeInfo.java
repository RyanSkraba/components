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

package org.talend.components.netsuite.client.model;

import org.talend.components.netsuite.client.NsRef;

/**
 *
 */
public class CustomRecordTypeInfo extends RecordTypeInfo {
    private NsRef ref;

    public CustomRecordTypeInfo(String name, RecordTypeDesc recordType, NsRef ref) {
        super(name, recordType);
        this.ref = ref;
    }

    public String getDisplayName() {
        return ref.getName();
    }

    public NsRef getRef() {
        return ref;
    }

    public void setRef(NsRef ref) {
        this.ref = ref;
    }

    public RefType getRefType() {
        return RefType.CUSTOMIZATION_REF;
    }

    public NsRef createRef(String internalId, String externalId, String scriptId) {
        NsRef ref = createRef(internalId, externalId);
        ref.setScriptId(scriptId);
        return ref;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CustomRecordTypeInfo{");
        sb.append("name='").append(name).append('\'');
        sb.append(", recordType=").append(recordType);
        sb.append(", ref=").append(ref);
        sb.append(", displayName='").append(getDisplayName()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
