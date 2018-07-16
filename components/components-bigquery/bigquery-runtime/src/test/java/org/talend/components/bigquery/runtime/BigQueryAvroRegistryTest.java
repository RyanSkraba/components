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

package org.talend.components.bigquery.runtime;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.apache.avro.Schema;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.talend.daikon.avro.converter.AvroConverter;

public class BigQueryAvroRegistryTest {

    @Test
    public void testGetConverter_double() {
        BigQueryBaseIndexedRecordConverter indexedRecordConverter = new BigQueryTableRowIndexedRecordConverter();
        AvroConverter converter = indexedRecordConverter.getConverter(Schema.create(Schema.Type.DOUBLE));
        assertThat(converter.convertToAvro(123L), Matchers.<Object>is(123d));
        assertThat(converter.convertToAvro(123), Matchers.<Object>is(123d));
        assertThat(converter.convertToAvro((short)123), Matchers.<Object>is(123d));
        assertThat(converter.convertToAvro((byte)123), Matchers.<Object>is(123d));
        assertThat(converter.convertToAvro(123d), Matchers.<Object>is(123d));
        assertThat(converter.convertToAvro(123f), Matchers.<Object>is(123d));
        assertThat(converter.convertToAvro(null), nullValue());
    }

    @Test
    public void testGetConverter_long() {
        BigQueryBaseIndexedRecordConverter indexedRecordConverter = new BigQueryTableRowIndexedRecordConverter();
        AvroConverter converter = indexedRecordConverter.getConverter(Schema.create(Schema.Type.LONG));
        assertThat(converter.convertToAvro(123L), Matchers.<Object>is(123L));
        assertThat(converter.convertToAvro(123), Matchers.<Object>is(123L));
        assertThat(converter.convertToAvro((short)123), Matchers.<Object>is(123L));
        assertThat(converter.convertToAvro((byte)123), Matchers.<Object>is(123L));
        assertThat(converter.convertToAvro(123d), Matchers.<Object>is(123L));
        assertThat(converter.convertToAvro(123f), Matchers.<Object>is(123L));
        assertThat(converter.convertToAvro(null), nullValue());
    }
}
