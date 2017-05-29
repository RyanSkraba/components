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
import org.talend.daikon.avro.AvroRegistry;

/**
 * Special converter which converts any value to {@code null}.
 */
public class NullConverter<T> extends AvroRegistry.Unconverted<T> {

    public NullConverter(Class<T> specificClass, Schema schema) {
        super(specificClass, schema);
    }

    @Override
    public T convertToAvro(T value) {
        return null;
    }

    @Override
    public T convertToDatum(T value) {
        return null;
    }
}
