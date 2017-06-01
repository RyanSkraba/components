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
package org.talend.components.api.test.performance;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtils {

    /**
     * Method is used to retrieve the field values using Reflection.
     */
    public static <T> T getObjectByField(Object delegate, Class<?> delegateClass, String fieldName)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field f = delegateClass.getDeclaredField(fieldName);
        f.setAccessible(true);
        T object = (T) f.get(delegate);
        return object;
    }

    /**
     * Check if the method is annotated with some annotation
     * 
     * @param testMethod method to be checked
     * @param annotationClass annotation test method should be annotated with
     * 
     * @return true if the method is annotated with annotationClass annotation
     */
    public static <T extends Annotation> boolean isMethodAnnotated(Method testMethod, Class<T> annotationClass) {
        T classAnnotation = testMethod.getAnnotation(annotationClass);
        return classAnnotation != null;
    }

}
