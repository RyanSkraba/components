// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marketo.runtime.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.talend.components.marketo.MarketoConstants.DATETIME_PATTERN_PARAM_UTC;
import static org.talend.components.marketo.MarketoConstants.DATETIME_PATTERN_REST;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.junit.Before;
import org.junit.Test;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

public class MarketoClientUtilsTest {

    Field fValidDate;

    Field fInvalidDate;

    Field fString;

    @Before
    public void setUp() throws Exception {
        fValidDate = new Schema.Field("date", AvroUtils._logicalTimestamp(), "", (Object) null);
        fValidDate.addProp(SchemaConstants.JAVA_CLASS_FLAG, Date.class.getCanonicalName());
        fValidDate.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, DATETIME_PATTERN_REST);

        fInvalidDate = new Schema.Field("date", AvroUtils._logicalTimestamp(), "", (Object) null);
    }

    public Field generateFieldType(Type type) {
        return new Schema.Field("generated", Schema.create(type), "", (Object) null);
    }

    @Test
    public void testIsDateTypeField() throws Exception {
        assertTrue(MarketoClientUtils.isDateTypeField(fValidDate));
        assertFalse(MarketoClientUtils.isDateTypeField(null));
        assertFalse(MarketoClientUtils.isDateTypeField(fInvalidDate));
        assertFalse(MarketoClientUtils.isDateTypeField(generateFieldType(Type.LONG)));
    }

    @Test
    public void testFormatLongToDateString() throws Exception {
        Date dt = new Date(71, 9, 8);
        String expected = new SimpleDateFormat(DATETIME_PATTERN_PARAM_UTC).format(dt);
        assertEquals(expected, MarketoClientUtils.formatLongToDateString(dt.getTime()));
        assertNull(MarketoClientUtils.formatLongToDateString(null));
    }

}
