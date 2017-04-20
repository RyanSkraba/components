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

package org.talend.components.netsuite.avro.converter;

import org.apache.avro.Schema;
import org.talend.components.netsuite.NsObjectTransducer;
import org.talend.components.netsuite.client.model.beans.EnumAccessor;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.converter.AvroConverter;

/**
 * Responsible for conversion of NetSuite <code>Enum Constant</code> from/to <code>string</code>.
 */
public class EnumToStringConverter<T extends Enum<T>> implements AvroConverter<T, String> {

    private final Class<T> clazz;

    private final EnumAccessor enumAccessor;

    public EnumToStringConverter(Class<T> clazz, EnumAccessor enumAccessor) {
        this.clazz = clazz;
        this.enumAccessor = enumAccessor;
    }

    @Override
    public Schema getSchema() {
        return AvroUtils._string();
    }

    @Override
    public Class<T> getDatumClass() {
        return clazz;
    }

    @Override
    public T convertToDatum(String value) {
        if (value == null) {
            return null;
        }
        try {
            return (T) enumAccessor.getEnumValue(value);
        } catch (IllegalArgumentException ex) {
            // Fallback to .valueOf(String)
            return Enum.valueOf(clazz, value);
        }
    }

    @Override
    public String convertToAvro(Enum enumValue) {
        if (enumValue == null) {
            return null;
        }
        try {
            return enumAccessor.getStringValue(enumValue);
        } catch (IllegalArgumentException ex) {
            // Fallback to .name()
            return enumValue.name();
        }
    }
}
