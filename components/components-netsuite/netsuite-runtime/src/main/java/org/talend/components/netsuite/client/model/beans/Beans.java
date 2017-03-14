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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.expression.DefaultResolver;
import org.apache.commons.beanutils.expression.Resolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.netsuite.util.Mapper;

/**
 *
 */
public abstract class Beans {

    private static final Logger LOG = LoggerFactory.getLogger(Beans.class);

    private static final ConcurrentMap<Class<?>, BeanInfo> beanInfoCache = new ConcurrentHashMap<>();

    private static final Resolver propertyResolver = new DefaultResolver();

    public static PropertyInfo getPropertyInfo(Object target, String name) {
        BeanInfo beanInfo = getBeanInfo(target.getClass());
        return beanInfo != null ? beanInfo.getProperty(name) : null;
    }

    public static BeanInfo getBeanInfo(Class<?> clazz) {
        BeanInfo beanInfo = beanInfoCache.get(clazz);
        if (beanInfo == null) {
            BeanInfo newBeanInfo = loadBeanInfoForClass(clazz);
            if (beanInfoCache.putIfAbsent(clazz, newBeanInfo) == null) {
                beanInfo = newBeanInfo;
            } else {
                beanInfo = beanInfoCache.get(clazz);
            }
        }
        return beanInfo;
    }

    private static BeanInfo loadBeanInfoForClass(Class<?> clazz) {
        try {
            List<PropertyInfo> properties = BeanIntrospector.getInstance().getProperties(clazz.getName());
            return new BeanInfo(properties);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setProperty(Object target, String expr, Object value) {
        try {
            Object current = target;
            if (propertyResolver.hasNested(expr)) {
                String currExpr = expr;
                while (propertyResolver.hasNested(currExpr)) {
                    String next = propertyResolver.next(currExpr);
                    Object obj = getSimpleProperty(current, next);
                    if (obj != null) {
                        current = obj;
                        currExpr = propertyResolver.remove(currExpr);
                    }
                }
                if (current != null) {
                    setSimpleProperty(current, currExpr, value);
                }
            } else {
                if (current != null) {
                    setSimpleProperty(current, expr, value);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getProperty(Object target, String expr) {
        try {
            Object current = target;
            if (propertyResolver.hasNested(expr)) {
                String currExpr = expr;
                while (propertyResolver.hasNested(currExpr) && current != null) {
                    String next = propertyResolver.next(currExpr);
                    current = getSimpleProperty(current, next);
                    currExpr = propertyResolver.remove(currExpr);
                }
                return current != null ? getSimpleProperty(current, currExpr) : null;
            } else {
                return getSimpleProperty(current, expr);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getSimpleProperty(Object target, String name) {
        return getPropertyAccessor(target).get(target, name);
    }

    public static void setSimpleProperty(Object target, String name, Object value) {
        getPropertyAccessor(target).set(target, name, value);
    }

    protected static <T> PropertyAccessor<T> getPropertyAccessor(T target) {
        if (target instanceof PropertyAccess) {
            return ((PropertyAccess) target).getPropertyAccessor();
        } else {
            return (PropertyAccessor<T>) ReflectPropertyAccessor.INSTANCE;
        }
    }

    public static EnumAccessor getEnumAccessor(Class<? extends Enum> clazz) {
        return getEnumAccessorImpl(clazz);
    }

    protected static AbstractEnumAccessor getEnumAccessorImpl(Class<? extends Enum> clazz) {
        EnumAccessor accessor = null;
        Method m;
        try {
            m = clazz.getDeclaredMethod("getEnumAccessor", new Class[0]);
            if (!m.getReturnType().equals(EnumAccessor.class)) {
                throw new NoSuchMethodException();
            }
            try {
                accessor = (EnumAccessor) m.invoke(new Object[0]);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOG.error("Failed to get optimized EnumAccessor: enum class " + clazz.getName(), e);
            }
        } catch (NoSuchMethodException e) {
        }
        if (accessor != null) {
            return new OptimizedEnumAccessor(clazz, accessor);
        } else {
            return new ReflectEnumAccessor(clazz);
        }
    }

    public static Mapper<Enum, String> getEnumToStringMapper(Class<Enum> clazz) {
        return getEnumAccessorImpl(clazz).getToStringMapper();
    }

    public static Mapper<String, Enum> getEnumFromStringMapper(Class<Enum> clazz) {
        return getEnumAccessorImpl(clazz).getFromStringMapper();
    }

    public static String toInitialUpper(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    public static String toInitialLower(String value) {
        return value.substring(0, 1).toLowerCase() + value.substring(1);
    }

    public static String toNetSuiteType(String value) {
        return "_" + toInitialLower(value);
    }

    protected static class ReflectPropertyAccessor implements PropertyAccessor<Object> {
        protected static final ReflectPropertyAccessor INSTANCE = new ReflectPropertyAccessor();

        /** An empty class array */
        private static final Class[] EMPTY_CLASS_PARAMETERS = new Class[0];
        /** An empty object array */
        private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

        public Object get(Object target, String name) {
            if (name == null) {
                throw new IllegalArgumentException("No name specified for bean class '" +
                        target.getClass() + "'");
            }

            // Retrieve the property getter method for the specified property
            BeanInfo metaData = Beans.getBeanInfo(target.getClass());
            PropertyInfo descriptor = metaData.getProperty(name);
            if (descriptor == null) {
                throw new IllegalArgumentException("Unknown property '" +
                        name + "' on class '" + target.getClass() + "'");
            }
            Method readMethod = getReadMethod(target.getClass(), descriptor);
            if (readMethod == null) {
                throw new IllegalArgumentException("Property '" + name +
                        "' has no getter method in class '" + target.getClass() + "'");
            }

            // Call the property getter and return the value
            try {
                Object value = invokeMethod(readMethod, target, EMPTY_OBJECT_ARRAY);
                return (value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        public void set(Object target, String name, Object value) {
            if (name == null) {
                throw new IllegalArgumentException("No name specified for bean class '" +
                        target.getClass() + "'");
            }

            // Retrieve the property setter method for the specified property
            BeanInfo metaData = Beans.getBeanInfo(target.getClass());
            PropertyInfo descriptor = metaData.getProperty(name);
            if (descriptor == null) {
                throw new IllegalArgumentException("Unknown property '" +
                        name + "' on class '" + target.getClass() + "'" );
            }
            Method writeMethod = getWriteMethod(target.getClass(), descriptor);
            if (writeMethod == null) {
                throw new IllegalArgumentException("Property '" + name +
                        "' has no setter method in class '" + target.getClass() + "'");
            }

            // Call the property setter method
            Object[] values = new Object[1];
            values[0] = value;

            try {
                invokeMethod(writeMethod, target, values);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        /** This just catches and wraps IllegalArgumentException. */
        private Object invokeMethod(
                Method method,
                Object bean,
                Object[] values)
                throws
                IllegalAccessException,
                InvocationTargetException {

            try {
                return method.invoke(bean, values);
            } catch (IllegalArgumentException cause) {
                if(bean == null) {
                    throw new IllegalArgumentException("No bean specified " +
                            "- this should have been checked before reaching this method");
                }
                String valueString = "";
                if (values != null) {
                    for (int i = 0; i < values.length; i++) {
                        if (i>0) {
                            valueString += ", " ;
                        }
                        valueString += (values[i]).getClass().getName();
                    }
                }
                String expectedString = "";
                Class[] parTypes = method.getParameterTypes();
                if (parTypes != null) {
                    for (int i = 0; i < parTypes.length; i++) {
                        if (i > 0) {
                            expectedString += ", ";
                        }
                        expectedString += parTypes[i].getName();
                    }
                }
                IllegalArgumentException e = new IllegalArgumentException(
                        "Cannot invoke " + method.getDeclaringClass().getName() + "."
                                + method.getName() + " on bean class '" + bean.getClass() +
                                "' - " + cause.getMessage()
                                + " - had objects of type \"" + valueString
                                + "\" but expected signature \""
                                +   expectedString + "\""
                );
                throw e;
            }
        }

        /**
         * <p>Return an accessible property getter method for this property,
         * if there is one; otherwise return <code>null</code>.</p>
         *
         * @param clazz The class of the read method will be invoked on
         * @param descriptor Property descriptor to return a getter for
         * @return The read method
         */
        Method getReadMethod(Class clazz, PropertyInfo descriptor) {
            return (MethodUtils.getAccessibleMethod(clazz, descriptor.getReadMethodName(), EMPTY_CLASS_PARAMETERS));
        }

        /**
         * <p>Return an accessible property setter method for this property,
         * if there is one; otherwise return <code>null</code>.</p>
         *
         * @param clazz The class of the read method will be invoked on
         * @param descriptor Property descriptor to return a setter for
         * @return The write method
         */
        Method getWriteMethod(Class clazz, PropertyInfo descriptor) {
            return (MethodUtils.getAccessibleMethod(clazz, descriptor.getWriteMethodName(),
                    new Class[]{descriptor.getWriteType()}));
        }
    }

    protected static abstract class AbstractEnumAccessor implements EnumAccessor {
        protected Class<?> enumClass;
        protected Mapper<Enum, String> toStringMapper;
        protected Mapper<String, Enum> fromStringMapper;

        protected AbstractEnumAccessor(Class<?> enumClass) {
            this.enumClass = enumClass;
            toStringMapper = new ToStringMapper();
            fromStringMapper = new FromStringMapper();
        }

        public Class<?> getEnumClass() {
            return enumClass;
        }

        public Mapper<Enum, String> getToStringMapper() {
            return toStringMapper;
        }

        public Mapper<String, Enum> getFromStringMapper() {
            return fromStringMapper;
        }

        protected class ToStringMapper implements Mapper<Enum, String> {
            @Override
            public String map(Enum input) {
                return getStringValue(input);
            }
        }

        protected class FromStringMapper implements Mapper<String, Enum> {
            @Override
            public Enum map(String input) {
                return getEnumValue(input);
            }
        }
    }

    public static class ReflectEnumAccessor extends AbstractEnumAccessor {

        public ReflectEnumAccessor(Class<?> enumClass) {
            super(enumClass);
        }

        @Override
        public String getStringValue(Enum enumValue) {
            try {
                return (String) MethodUtils.invokeExactMethod(enumValue, "value", null);
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) e.getTargetException();
                }
                throw new RuntimeException(e.getTargetException());
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Enum getEnumValue(String value) {
            try {
                return (Enum) MethodUtils.invokeExactStaticMethod(enumClass, "fromValue", value);
            } catch (InvocationTargetException e) {
                if (e.getTargetException() instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) e.getTargetException();
                }
                throw new RuntimeException(e.getTargetException());
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class OptimizedEnumAccessor extends AbstractEnumAccessor {
        private EnumAccessor accessor;

        public OptimizedEnumAccessor(Class<?> enumClass, EnumAccessor accessor) {
            super(enumClass);
            this.accessor = accessor;
        }

        @Override
        public String getStringValue(Enum enumValue) {
            return accessor.getStringValue(enumValue);
        }

        @Override
        public Enum getEnumValue(String value) {
            return accessor.getEnumValue(value);
        }
    }
}
