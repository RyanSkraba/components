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


import org.talend.components.api.schema.SchemaElement;

import java.util.*;


public class TypeMapping {

    private static Map<String, Map<Class<? extends ExternalBaseType>, SchemaElement.Type>> externalTypesGroup = new HashMap<>();

    public static void registryTypes(TypesRegistry registry) {
        registryExternalTypes(registry.getFamilyName(), registry.getMapping());
    }

    private static void registryExternalTypes(String familyName, Map<Class<? extends ExternalBaseType>, SchemaElement.Type> typeMapping) {
        externalTypesGroup.put(familyName, typeMapping);
    }

    public static SchemaElement.Type getDefaultTalendType(String appFamily, Class<? extends ExternalBaseType> appType) {
        Map<Class<? extends ExternalBaseType>, SchemaElement.Type> appTypesGroup = externalTypesGroup.get(appFamily);
        return appTypesGroup.get(appType);
    }

    //TODO re-implement it, now it's very dirty
    public static List<SchemaElement.Type> getTalendTypes(String appFamily, Class<? extends ExternalBaseType> appType) {
        List<SchemaElement.Type> numberTypes = Arrays.asList(new SchemaElement.Type[]{SchemaElement.Type.BYTE, SchemaElement.Type.SHORT, SchemaElement.Type.INT, SchemaElement.Type.LONG, SchemaElement.Type.FLOAT, SchemaElement.Type.DOUBLE});

        List<SchemaElement.Type> classes = new ArrayList<>();
        SchemaElement.Type bestType = getDefaultTalendType(appFamily, appType);
        classes.add(bestType);
        if (bestType == SchemaElement.Type.CHARACTER) {
            classes.add(SchemaElement.Type.INT);
        } else if (bestType == SchemaElement.Type.DATE) {
            classes.add(SchemaElement.Type.LONG);
        } else if (bestType == SchemaElement.Type.LONG) {
            classes.add(SchemaElement.Type.DATE);
        } else if (numberTypes.contains(bestType)) {
            boolean find = false;
            for (SchemaElement.Type numberType : numberTypes) {
                if (!find) {
                    if (numberType == bestType)
                        find = true;
                    continue;
                } else {
                    classes.add(numberType);
                }
            }
        }

        if (!classes.contains(SchemaElement.Type.LIST)) {
            classes.add(SchemaElement.Type.STRING);
            classes.add(SchemaElement.Type.BYTE_ARRAY);
        }
        classes.add(SchemaElement.Type.OBJECT);

        return classes;
    }


    public static List<Class<? extends ExternalBaseType>> getAppTypes(String appFamily, SchemaElement.Type talendType) {
        //TODO improve the mapping init
        Map<SchemaElement.Type, List<Class<? extends ExternalBaseType>>> mapping = new HashMap<>();
        Map<Class<? extends ExternalBaseType>, SchemaElement.Type> appTypes = externalTypesGroup.get(appFamily);
        for (Class<? extends ExternalBaseType> appType : appTypes.keySet()) {
            SchemaElement.Type tType = appTypes.get(appType);
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
    public static Object convert(SchemaElement.Type inType, SchemaElement.Type outType, Object inValue) {
        if (inType == outType) {
            return inValue;
        } else {
            if (outType == SchemaElement.Type.OBJECT) {
                return inValue;
            } else if (outType == SchemaElement.Type.STRING && inType != SchemaElement.Type.LIST) {
                return convertToString(inType, inValue);
            } else if (outType == SchemaElement.Type.BYTE_ARRAY && inType != SchemaElement.Type.LIST) {
                return (convertToString(inType, inValue).getBytes());//TODO support encoding for each column in future?
            } else if (outType == SchemaElement.Type.DECIMAL) {
//                outValue = new TBigDecimal();
//                if (inType == TString.class) {
//                    outValue.setValue(new BigDecimal(((TString) inValue).getValue()));
//                }
            } else if (outType == SchemaElement.Type.DOUBLE) {
            } else if (outType == SchemaElement.Type.FLOAT) {
            } else if (outType == SchemaElement.Type.LONG) {
                if (inType == SchemaElement.Type.INT) {
                    return ((Integer) inValue).longValue();
                }
            } else if (outType == SchemaElement.Type.INT) {
            } else if (outType == SchemaElement.Type.SHORT) {
            } else if (outType == SchemaElement.Type.BYTE) {
            }
        }
        return null;
    }

    private static String convertToString(SchemaElement.Type inType, Object inValue) {
        if (inType == SchemaElement.Type.BYTE_ARRAY) {
            return new String(((byte[]) inValue));
        } else if (inType == SchemaElement.Type.DATE) {
            //TODO date pattern
            return null;
        } else if (inType == SchemaElement.Type.SHORT || inType == SchemaElement.Type.BYTE) {
            return String.valueOf(Integer.valueOf((int) inValue));
        } else {
            return String.valueOf(inValue);
        }
    }
}
