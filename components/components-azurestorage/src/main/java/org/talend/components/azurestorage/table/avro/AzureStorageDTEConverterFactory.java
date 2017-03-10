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
package org.talend.components.azurestorage.table.avro;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.talend.components.azurestorage.table.avro.AzureStorageDTEConverters.DTEConverter;
import org.talend.daikon.avro.AvroUtils;

public class AzureStorageDTEConverterFactory {

    public static DTEConverter createConverter(final Field f, final String mappedName) {

        Schema basicSchema = AvroUtils.unwrapIfNullable(f.schema());
        AzureStorageDTEConverters converters = new AzureStorageDTEConverters();

        DTEConverter dteConverter;
        if (AvroUtils.isSameType(basicSchema, AvroUtils._string()) || AvroUtils.isSameType(basicSchema, AvroUtils._character())) {

            dteConverter = converters.new StringDTEConverter(f, mappedName);

        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._int()) || AvroUtils.isSameType(basicSchema, AvroUtils._short())
                || AvroUtils.isSameType(basicSchema, AvroUtils._byte())) {

            dteConverter = converters.new IntegerDTEConverter(mappedName);

        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._date())) {

            dteConverter = converters.new DateDTEConverter(f, mappedName);

        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._decimal())
                || AvroUtils.isSameType(basicSchema, AvroUtils._double())
                || AvroUtils.isSameType(basicSchema, AvroUtils._float())) {

            dteConverter = converters.new DoubleDTEConverter(mappedName);

        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._long())) {

            dteConverter = converters.new LongDTEConverter(f, mappedName);

        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._boolean())) {

            dteConverter = converters.new BooleanDTEConverter(mappedName);

        } else if (AvroUtils.isSameType(basicSchema, AvroUtils._bytes())) {

            dteConverter = converters.new ByteArrayDTEConverter(mappedName);

        } else {
            dteConverter = converters.new StringDTEConverter(f, mappedName);
        }

        return dteConverter;

    }

}
