// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.schema.type;


import org.talend.components.cassandra.type.BaseType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TypeMapping {

    public static List<Class<? extends TBaseType>> getTalendTypes(Class<? extends BaseType> app_type) {
        try {
            Class<TBaseType> talend_type_class = app_type.newInstance().getDefaultTalendType();
            List optionalTalendTypes = talend_type_class.newInstance().getOptionalTalendTypes();
            optionalTalendTypes.add(0, talend_type_class);
            return optionalTalendTypes;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return new ArrayList<Class<? extends TBaseType>>();
    }

    public static List<Class<? extends BaseType>> getAppTypes(String app_family, Class<? extends TBaseType> talend_type) {
        // TODO init only once
        Map<Class<? extends TBaseType>, List<Class<? extends BaseType>>> mapping = new HashMap<Class<? extends TBaseType>, List<Class<? extends BaseType>>>();
        if (BaseType.familyName == app_family) {
            for (Class<? extends BaseType> atype : BaseType.types) {
                for (Class<? extends TBaseType> ttype : getTalendTypes(atype)) {
                    List<Class<? extends BaseType>> atypes = mapping.get(ttype);
                    if (atypes == null || atypes.isEmpty()) {
                        atypes = new ArrayList<Class<? extends BaseType>>();
                        mapping.put(ttype, atypes);
                    }
                    atypes.add(atype);
                }
            }
        }
        List<Class<? extends BaseType>> possibleATypes = mapping.get(talend_type);
        if (possibleATypes == null || possibleATypes.isEmpty()) {
            // mapping.get(talend_type.newInstance().op)
        }
        return null;
    }

    public static TBaseType convert(Class<? extends TBaseType> inType, Class<? extends TBaseType> outType, TBaseType inValue) {
        if (inType == outType) {
            return inValue;
        } else if (inType == TInt.class && outType == TLong.class) {
            TLong outValue = new TLong();
            outValue.setValue(((TInt) inValue).getValue().longValue());
            return outValue;
        }
        return null;
    }
}
