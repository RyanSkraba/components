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

import java.util.HashMap;

/**
 *
 */
public class PrimitiveInfo {

    /** The boolean info */
    public static final PrimitiveInfo BOOLEAN = new PrimitiveInfo("boolean", 0, Boolean.TYPE);

    /** The byte info */
    public static final PrimitiveInfo BYTE = new PrimitiveInfo("byte", 1, Byte.TYPE);

    /** The char info */
    public static final PrimitiveInfo CHAR = new PrimitiveInfo("char", 2, Character.TYPE);

    /** The double info */
    public static final PrimitiveInfo DOUBLE = new PrimitiveInfo("double", 3, Double.TYPE);

    /** The float info */
    public static final PrimitiveInfo FLOAT = new PrimitiveInfo("float", 4, Float.TYPE);

    /** The int info */
    public static final PrimitiveInfo INT = new PrimitiveInfo("int", 5, Integer.TYPE);

    /** The long info */
    public static final PrimitiveInfo LONG = new PrimitiveInfo("long", 6, Long.TYPE);

    /** The short info */
    public static final PrimitiveInfo SHORT = new PrimitiveInfo("short", 7, Short.TYPE);

    /** The void info */
    public static final PrimitiveInfo VOID = new PrimitiveInfo("void", 8, Void.TYPE);

    /** The primitives */
    private static final PrimitiveInfo[] values = { BOOLEAN, BYTE, CHAR, DOUBLE, FLOAT, INT, LONG, SHORT, VOID };

    /** The name */
    protected final transient String name;

    /** The ordinal */
    protected final int ordinal;

    /** The type */
    protected final transient Class<? extends Object> type;

    /** The primitive types indexed by name */
    private static final HashMap<String, Class<?>> primitiveTypes = new HashMap<>();

    /** The primitive wrapper types indexed by name */
    private static final HashMap<String, Class<?>> primitiveWrapperTypes = new HashMap<>();

    /** The primitive array types indexed by name */
    private static final HashMap<String, String> primitiveArrayTypes = new HashMap<>();

    /** The primitive array classes indexed by name */
    private static final HashMap<String, Class<?>> primitiveArrayTypesClassMap = new HashMap<>();

    /** The primitives */
    private static final HashMap<String, PrimitiveInfo> map = new HashMap<>();

    static {
        map.put("boolean", BOOLEAN);
        map.put("byte", BYTE);
        map.put("char", CHAR);
        map.put("double", DOUBLE);
        map.put("float", FLOAT);
        map.put("int", INT);
        map.put("long", LONG);
        map.put("short", SHORT);
        map.put("void", VOID);

        primitiveTypes.put(Byte.TYPE.getName(), Byte.TYPE);
        primitiveTypes.put(Boolean.TYPE.getName(), Boolean.TYPE);
        primitiveTypes.put(Character.TYPE.getName(), Character.TYPE);
        primitiveTypes.put(Double.TYPE.getName(), Double.TYPE);
        primitiveTypes.put(Float.TYPE.getName(), Float.TYPE);
        primitiveTypes.put(Integer.TYPE.getName(), Integer.TYPE);
        primitiveTypes.put(Long.TYPE.getName(), Long.TYPE);
        primitiveTypes.put(Short.TYPE.getName(), Short.TYPE);

        primitiveWrapperTypes.put(Byte.TYPE.getName(), Byte.class);
        primitiveWrapperTypes.put(Boolean.TYPE.getName(), Boolean.class);
        primitiveWrapperTypes.put(Character.TYPE.getName(), Character.class);
        primitiveWrapperTypes.put(Double.TYPE.getName(), Double.class);
        primitiveWrapperTypes.put(Float.TYPE.getName(), Float.class);
        primitiveWrapperTypes.put(Integer.TYPE.getName(), Integer.class);
        primitiveWrapperTypes.put(Long.TYPE.getName(), Long.class);
        primitiveWrapperTypes.put(Short.TYPE.getName(), Short.class);

        primitiveArrayTypes.put(Byte.TYPE.getName(), "B");
        primitiveArrayTypes.put(Boolean.TYPE.getName(), "Z");
        primitiveArrayTypes.put(Character.TYPE.getName(), "C");
        primitiveArrayTypes.put(Double.TYPE.getName(), "D");
        primitiveArrayTypes.put(Float.TYPE.getName(), "F");
        primitiveArrayTypes.put(Integer.TYPE.getName(), "I");
        primitiveArrayTypes.put(Long.TYPE.getName(), "J");
        primitiveArrayTypes.put(Short.TYPE.getName(), "S");

        primitiveArrayTypesClassMap.put("B", Byte.TYPE);
        primitiveArrayTypesClassMap.put("Z", Boolean.TYPE);
        primitiveArrayTypesClassMap.put("C", Character.TYPE);
        primitiveArrayTypesClassMap.put("D", Double.TYPE);
        primitiveArrayTypesClassMap.put("F", Float.TYPE);
        primitiveArrayTypesClassMap.put("I", Integer.TYPE);
        primitiveArrayTypesClassMap.put("J", Long.TYPE);
        primitiveArrayTypesClassMap.put("S", Short.TYPE);
    }

    /**
     * Get the primitive info for a type
     *
     * @param name the name
     * @return the info
     */
    public static PrimitiveInfo valueOf(String name) {
        return map.get(name);
    }

    /**
     * Get a primitive array type
     *
     * @param name the primitive type name
     * @return the array type or null if not found
     */
    public static String getPrimitiveArrayType(String name) {
        return primitiveArrayTypes.get(name);
    }

    /**
     * Get the primitive type for a name
     *
     * @param name the primitive type name
     * @return the primitive type
     */
    public static Class<?> getPrimitiveType(String name) {
        return primitiveTypes.get(name);
    }

    /**
     * Get the primitive type for a name
     *
     * @param name the primitive type name
     * @return the primitive type
     */
    public static Class<?> getPrimitiveWrapperType(String name) {
        return primitiveWrapperTypes.get(name);
    }

    /**
     * Get the primitive array type class map for a name
     *
     * @param name the array type name
     * @return the component type
     */
    public static Class<?> getPrimitiveArrayComponentType(String name) {
        return primitiveArrayTypesClassMap.get(name);
    }

    /**
     * Create a new primitive info
     *
     * @param name the name
     * @param ordinal the oridinal
     * @param type the class
     */
    protected PrimitiveInfo(String name, int ordinal, Class<? extends Object> type) {
        this.name = name;
        this.ordinal = ordinal;
        this.type = type;
    }

    /**
     * Get the ordinal
     *
     * @return the oridinal
     */
    public int ordinal() {
        return ordinal;
    }

    public String getName() {
        return name;
    }

    public String getSimpleName() {
        return type.getSimpleName();
    }

    @Deprecated
    public Class<?> getType() {
        return type;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof PrimitiveInfo))
            return false;
        if (!obj.getClass().equals(this.getClass()))
            return false;
        PrimitiveInfo other = (PrimitiveInfo) obj;
        return other.ordinal == this.ordinal;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}