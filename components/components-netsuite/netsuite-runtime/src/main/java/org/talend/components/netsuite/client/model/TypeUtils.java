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

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlSeeAlso;

import org.talend.components.netsuite.client.NetSuiteException;

/**
 *
 */
public abstract class TypeUtils {

    public static void collectXmlTypes(Class<?> rootClass, Class<?> clazz, Set<Class<?>> classes) {
        if (classes.contains(clazz)) {
            return;
        }

        if (clazz != rootClass && rootClass.isAssignableFrom(clazz) && !Modifier.isAbstract(clazz.getModifiers())) {
            classes.add(clazz);
        }

        XmlSeeAlso xmlSeeAlso = clazz.getAnnotation(XmlSeeAlso.class);
        if (xmlSeeAlso != null) {
            Collection<Class<?>> referencedClasses = new HashSet<>(Arrays.<Class<?>>asList(xmlSeeAlso.value()));
            for (Class<?> referencedClass : referencedClasses) {
                collectXmlTypes(rootClass, referencedClass, classes);
            }
        }
    }

    public static <T> T createInstance(Class<T> clazz) throws NetSuiteException {
        try {
            T target = clazz.cast(clazz.newInstance());
            return target;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new NetSuiteException("Failed to instantiate object: " + clazz, e);
        }
    }

}
