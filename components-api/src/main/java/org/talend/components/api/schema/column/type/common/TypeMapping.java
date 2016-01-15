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
package org.talend.components.api.schema.column.type.common;


import org.talend.components.api.schema.column.type.*;

import java.util.*;


public class TypeMapping {

    private static Map<String, Map<Class<? extends ExternalBaseType>, Class<? extends TBaseType>>> externalTypesGroup = new HashMap<>();

    public static void registryTypes(TypesRegistry registry) {
        registryExternalTypes(registry.getFamilyName(), registry.getMapping());
    }

    private static void registryExternalTypes(String familyName, Map<Class<? extends ExternalBaseType>, Class<? extends TBaseType>> typeMapping) {
        externalTypesGroup.put(familyName, typeMapping);
    }

    public static Class<? extends TBaseType> getDefaultTalendType(String appFamily, Class<? extends ExternalBaseType> appType) {
        Map<Class<? extends ExternalBaseType>, Class<? extends TBaseType>> appTypesGroup = externalTypesGroup.get(appFamily);
        return appTypesGroup.get(appType);
    }

    //TODO re-implement it, now it's very dirty
    public static List<Class<? extends TBaseType>> getTalendTypes(String appFamily, Class<? extends ExternalBaseType> appType) {
        List<Class> numberClasses = Arrays.asList(new Class[]{TByte.class, TShort.class, TInt.class, TLong.class, TFloat.class, TDouble.class});

        List<Class<? extends TBaseType>> classes = new ArrayList<>();
        Class<? extends TBaseType> bestType = getDefaultTalendType(appFamily, appType);
        classes.add(bestType);
        if (bestType == TCharacter.class) {
            classes.add(TInt.class);
        } else if (bestType == TDate.class) {
            classes.add(TLong.class);
        } else if (bestType == TLong.class) {
            classes.add(TDate.class);
        } else if (numberClasses.contains(bestType)) {
            boolean find = false;
            for (Class numberClass : numberClasses) {
                if (!find) {
                    if (numberClass == bestType)
                        find = true;
                    continue;
                } else {
                    classes.add(numberClass);
                }
            }
        }

        if (!classes.contains(TList.class)) {
            classes.add(TString.class);
            classes.add(TByteArray.class);
        }
        classes.add(TObject.class);

        return classes;
    }


    public static List<Class<? extends ExternalBaseType>> getAppTypes(String appFamily, Class<? extends
            TBaseType> talendType) {
        //TODO improve the mapping init
        Map<Class<? extends TBaseType>, List<Class<? extends ExternalBaseType>>> mapping = new HashMap<>();
        Map<Class<? extends ExternalBaseType>, Class<? extends TBaseType>> appTypes = externalTypesGroup.get(appFamily);
        for (Class<? extends ExternalBaseType> appType : appTypes.keySet()) {
            Class<? extends TBaseType> tType = appTypes.get(appType);
            List<Class<? extends ExternalBaseType>> appTypeList = mapping.get(tType);
            if (appTypeList == null) {
                appTypeList = new ArrayList<>();
                mapping.put(tType, appTypeList);
            }
            appTypeList.add(appType);
        }
        //TODO now it don't support talend internal convert before get App Types, support it
//        List<Class<? extends ExternalBaseType>> result = mapping.get(talendType);
//        if (result == null || result.isEmpty()) {
//            result.addAll(getAppTypes(appFamily, optionalTalendTypes.get(talendType)));
//        }
        return mapping.get(talendType);
    }

    //TODO implement all convert between internal talend type

    public static TBaseType convert(Class<? extends TBaseType> inType, Class<? extends TBaseType> outType, TBaseType inValue) {
        if (inType == outType) {
            return inValue;
        } else {
            TBaseType outValue = null;
            if (outType == TObject.class) {
                outValue = new TObject();
                outValue.setValue(inValue.getValue());
            } else if (outType == TString.class && inType != TList.class) {
                outValue = convertToString(inType, inValue);
            } else if (outType == TByteArray.class && inType != TList.class) {
                outValue = new TBaseType();
                outValue.setValue(convertToString(inType, inValue).getValue().getBytes());//TODO support encoding for each column in future?
            } else if (outType == TBigDecimal.class) {
//                outValue = new TBigDecimal();
//                if (inType == TString.class) {
//                    outValue.setValue(new BigDecimal(((TString) inValue).getValue()));
//                }
            } else if (outType == TDouble.class) {
            } else if (outType == TFloat.class) {
            } else if (outType == TLong.class) {
                outValue = new TLong();
                if (inType == TInt.class) {
                    outValue.setValue(((TInt) inValue).getValue().longValue());
                }
            } else if (outType == TInt.class) {
            } else if (outType == TShort.class) {
            } else if (outType == TByte.class) {
            }
            return outValue;
        }
    }

    private static TString convertToString(Class<? extends TBaseType> inType, TBaseType inValue) {
        TString outValue = new TString();
        if (inType == TByteArray.class) {
            outValue.setValue(new String(((TByteArray) inValue).getValue()));
        } else if (inType == TDate.class) {
            outValue.setValue("");//TODO date pattern
        } else if (inType == TShort.class || inType == TByte.class) {
            outValue.setValue(String.valueOf(Integer.valueOf((int) inValue.getValue())));
        } else {
            outValue.setValue(String.valueOf(inValue.getValue()));
        }
        return outValue;
    }
}
