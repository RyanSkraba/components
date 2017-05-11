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

import static org.talend.components.netsuite.client.model.beans.Beans.getSimpleProperty;
import static org.talend.components.netsuite.client.model.beans.Beans.setSimpleProperty;

import java.util.Objects;

import org.talend.components.netsuite.client.model.BasicMetaData;
import org.talend.components.netsuite.client.model.RefType;
import org.talend.components.netsuite.client.model.beans.BeanInfo;
import org.talend.components.netsuite.client.model.beans.Beans;

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
    private String typeId;

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

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public Object toNativeRef(BasicMetaData basicMetaData) {
        Object ref = basicMetaData.createInstance(refType.getTypeName());
        BeanInfo beanInfo = Beans.getBeanInfo(ref.getClass());
        setSimpleProperty(ref, "internalId", internalId);
        if (refType == RefType.CUSTOMIZATION_REF || refType == RefType.CUSTOM_RECORD_REF) {
            setSimpleProperty(ref, "scriptId", scriptId);
        }
        if (refType == RefType.CUSTOM_RECORD_REF) {
            setSimpleProperty(ref, "typeId", typeId);
        } else {
            setSimpleProperty(ref, "type", Beans.getEnumAccessor(
                    (Class<Enum>) beanInfo.getProperty("type").getWriteType()).getEnumValue(type));
        }
        return ref;
    }

    public static NsRef fromNativeRef(Object ref) {
        String typeName = ref.getClass().getSimpleName();
        RefType refType = RefType.getByTypeName(typeName);
        NsRef nsRef = new NsRef();
        nsRef.setRefType(refType);
        BeanInfo beanInfo = Beans.getBeanInfo(ref.getClass());
        nsRef.setInternalId((String) getSimpleProperty(ref, "internalId"));
        nsRef.setExternalId((String) getSimpleProperty(ref, "externalId"));
        if (refType == RefType.RECORD_REF) {
            nsRef.setType(Beans.getEnumAccessor((Class<Enum>) beanInfo.getProperty("type").getReadType())
                    .getStringValue((Enum) getSimpleProperty(ref, "type")));
        } else if (refType == RefType.CUSTOM_RECORD_REF) {
            nsRef.setTypeId((String) getSimpleProperty(ref, "typeId"));
        } else if (refType == RefType.CUSTOMIZATION_REF) {
            nsRef.setScriptId((String) getSimpleProperty(ref, "scriptId"));
        }
        return nsRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        NsRef ref = (NsRef) o;
        return refType == ref.refType && Objects.equals(internalId, ref.internalId) &&
                Objects.equals(externalId, ref.externalId) && Objects.equals(scriptId, ref.scriptId) &&
                Objects.equals(typeId, ref.typeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(refType, internalId, externalId, scriptId, typeId);
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
        sb.append(", typeId='").append(typeId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
