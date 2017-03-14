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

package org.talend.components.netsuite.client.model.beans;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class BeanIntrospector {

    private static final BeanIntrospector instance = new BeanIntrospector();

    public static BeanIntrospector getInstance() {
        return instance;
    }

    public List<PropertyInfo> getProperties(String className) throws ClassNotFoundException {
        Class clazz = Class.forName(className);
        Collection<PropertyInfo> propertyInfos = getProperties(getMethods(clazz));
        return new ArrayList<>(propertyInfos);
    }

    protected Set<PropertyInfo> getProperties(Set<Method> methods) {
        Map<String, Method> getters = new HashMap<>();
        Map<String, List<Method>> setters = new HashMap<>();
        if (methods.isEmpty() == false) {
            for (Method methodInfo : methods) {
                String name = methodInfo.getName();
                if (isGetter(methodInfo)) {
                    String upperName = getUpperPropertyName(name);
                    getters.put(upperName, methodInfo);
                } else if (isSetter(methodInfo)) {
                    String upperName = getUpperPropertyName(name);
                    List<Method> list = setters.get(upperName);
                    if (list == null) {
                        list = new ArrayList<>();
                        setters.put(upperName, list);
                    }
                    list.add(methodInfo);
                }
            }
        }

        Set<PropertyInfo> properties = new HashSet<>();
        if (getters.isEmpty() == false) {
            for (Map.Entry<String, Method> entry : getters.entrySet()) {
                String name = entry.getKey();
                Method getter = entry.getValue();
                Method setter = null;
                List<Method> setterList = setters.remove(name);
                if (setterList != null && setterList.size() != 0) {
                    for (int j = 0; j < setterList.size(); ++j) {
                        Method thisSetter = setterList.get(j);
                        Class pinfo = thisSetter.getParameterTypes()[0];
                        if (getter.getReturnType().isPrimitive() && !pinfo.isPrimitive() &&
                                PrimitiveInfo.getPrimitiveWrapperType(getter.getReturnType().getName()).getName()
                                        .equals(pinfo.getName())) {
                            setter = thisSetter;
                            break;
                        } else if (!getter.getReturnType().isPrimitive() && pinfo.isPrimitive() &&
                                PrimitiveInfo.getPrimitiveWrapperType(pinfo.getName()).getName()
                                        .equals(getter.getReturnType().getName())) {
                            setter = thisSetter;
                            break;
                        } else if (getter.getReturnType().equals(pinfo) == true) {
                            setter = thisSetter;
                            break;
                        }
                    }
                }
                String lowerName = getLowerPropertyName(name);

                properties.add(new PropertyInfo(lowerName,
                        getPropertyReadType(getter), getPropertyWriteType(setter), getter, setter));
            }
        }
        if (setters.isEmpty() == false) {
            for (Map.Entry<String, List<Method>> entry : setters.entrySet()) {
                String name = entry.getKey();
                List<Method> setterList = entry.getValue();
                for (Method setter : setterList) {
                    Class pinfo = setter.getParameterTypes()[0];
                    String lowerName = getLowerPropertyName(name);
                    properties.add(new PropertyInfo(lowerName, null, pinfo, null, setter));
                }
            }
        }
        return properties;
    }

    protected static boolean isGetter(Method minfo) {
        String name = minfo.getName();
        if ((name.length() > 3 && name.startsWith("get")) || (name.length() > 2 && name.startsWith("is"))) {
            Class returnType = minfo.getReturnType();

            // isBoolean() is not a getter for java.lang.Boolean
            if (name.startsWith("is") && !returnType.isPrimitive())
                return false;

            int params = minfo.getParameterTypes().length;
            if (params == 0 && !PrimitiveInfo.VOID.getName().equals(returnType))
                return true;
        }
        return false;
    }

    protected static boolean isSetter(Method minfo) {
        String name = minfo.getName();
        if ((name.length() > 3 && name.startsWith("set"))) {
            Class returnType = minfo.getReturnType();

            int params = minfo.getParameterTypes().length;

            if (params == 1 && PrimitiveInfo.VOID.getName().equals(returnType.getName()))
                return true;
        }
        return false;
    }

    protected static String getUpperPropertyName(String name) {
        int start = 3;
        if (name.startsWith("is"))
            start = 2;

        return name.substring(start);
    }

    protected static String getLowerPropertyName(String name) {
        // If the second character is upper case then we don't make
        // the first character lower case
        if (name.length() > 1) {
            if (Character.isUpperCase(name.charAt(1)))
                return name;
        }

        StringBuilder buffer = new StringBuilder(name.length());
        buffer.append(Character.toLowerCase(name.charAt(0)));
        if (name.length() > 1)
            buffer.append(name.substring(1));
        return buffer.toString();
    }

    protected Class getPropertyReadType(Method getter) {
        if (getter == null)
            throw new IllegalArgumentException("Getter should not be null!");
        return getter.getReturnType();
    }

    protected Class getPropertyWriteType(Method setter) {
        if (setter == null)
            return null;
        return setter.getParameterTypes()[0];
    }

    protected Set<Method> getMethods(Class classInfo) {
        Set<Method> result = new HashSet<>();

        while (classInfo != null) {
            Method[] minfos = classInfo.getDeclaredMethods();
            if (minfos != null && minfos.length > 0) {
                for (int i = 0; i < minfos.length; ++i) {
                    Method minfo = minfos[i];
                    if (!result.contains(minfo) && Modifier.isPublic(minfo.getModifiers())
                            && Modifier.isAbstract(minfo.getModifiers()) == false
                            && Modifier.isStatic(minfo.getModifiers()) == false
                            && Modifier.isVolatile(minfo.getModifiers()) == false)
                        result.add(minfo);
                }
            }

            classInfo = classInfo.getSuperclass();
        }

        return result;
    }
}
