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
package org.talend.components.common.runtime;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.talend.components.api.exception.ComponentException;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;

public class DynamicSchemaUtilsTest {

    private static final I18nMessages I18N_MESSAGES = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(DynamicSchemaUtils.class);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testGetCommonFieldsWithNotSetRemoteSchema() {
        exception.expect(ComponentException.class);
        exception.expectMessage(I18N_MESSAGES.getMessage("error.message.remoteSchemaNotSet"));
        Schema inputSchema = SchemaBuilder.builder().record("record").fields().requiredInt("age").endRecord();

        DynamicSchemaUtils.getCommonFieldsForDynamicSchema(null, inputSchema);
    }

    @Test
    public void testGetCommonFieldsWithEmptyRemoteSchema() {
        exception.expect(ComponentException.class);
        exception.expectMessage(I18N_MESSAGES.getMessage("error.message.remoteSchemaNotSet"));
        Schema remoteSchema = SchemaBuilder.builder().record("record").fields().endRecord();
        Schema inputSchema = SchemaBuilder.builder().record("record").fields().requiredInt("age").endRecord();

        DynamicSchemaUtils.getCommonFieldsForDynamicSchema(remoteSchema, inputSchema);
    }

    @Test
    public void testGetCommonFieldsWithNotSetInputSchema() {
        exception.expect(ComponentException.class);
        exception.expectMessage(I18N_MESSAGES.getMessage("error.message.inputSchemaNotSet"));
        Schema remoteSchema = SchemaBuilder.builder().record("record").fields().requiredInt("age").endRecord();

        DynamicSchemaUtils.getCommonFieldsForDynamicSchema(remoteSchema, null);
    }

    @Test
    public void testGetCommonFieldsWithEmptyInputSchema() {
        exception.expect(ComponentException.class);
        exception.expectMessage(I18N_MESSAGES.getMessage("error.message.inputSchemaNotSet"));
        Schema remoteSchema = SchemaBuilder.builder().record("record").fields().requiredInt("age").endRecord();
        Schema inputSchema = SchemaBuilder.builder().record("record").fields().endRecord();

        DynamicSchemaUtils.getCommonFieldsForDynamicSchema(remoteSchema, inputSchema);
    }

    @Test
    public void testGetCommonFieldsWithCompletlyDifferentSchemas() {
        exception.expect(ComponentException.class);
        exception.expectMessage(I18N_MESSAGES.getMessage("error.message.differentSchema"));
        Schema remoteSchema = SchemaBuilder.builder().record("record").fields().requiredInt("age").endRecord();
        Schema inputSchema = SchemaBuilder.builder().record("record").fields().requiredInt("year").endRecord();

        DynamicSchemaUtils.getCommonFieldsForDynamicSchema(remoteSchema, inputSchema);
    }

    @Test
    public void testGetColumnsWhenRemoteSchemaHasMoreColumns() {
        Schema remoteSchema = SchemaBuilder.builder().record("record").fields().requiredString("name").requiredInt("age")
                .requiredInt("year").endRecord();
        Schema inputSchema = SchemaBuilder.builder().record("record").fields().requiredString("name").requiredInt("year")
                .endRecord();

        List<Field> resultFieldList = DynamicSchemaUtils.getCommonFieldsForDynamicSchema(remoteSchema, inputSchema);

        Assert.assertEquals(remoteSchema.getFields().size(), resultFieldList.size());
        Set<String> exspectedFieldsNames = new HashSet<>(Arrays.asList(null, "name", "year"));
        for (Field field : resultFieldList) {
            if (field == null) {
                Assert.assertTrue(exspectedFieldsNames.contains(null));
                continue;
            }
            Assert.assertTrue(exspectedFieldsNames.contains(field.name()));
        }
    }

    @Test
    public void testGetColumnsWhenRemoteSchemaHasLessColumns() {
        Schema remoteSchema = SchemaBuilder.builder().record("record").fields().requiredString("name").requiredInt("year")
                .endRecord();
        Schema inputSchema = SchemaBuilder.builder().record("record").fields().requiredString("name").requiredInt("age")
                .requiredInt("year").endRecord();

        List<Field> resultFieldList = DynamicSchemaUtils.getCommonFieldsForDynamicSchema(remoteSchema, inputSchema);

        Assert.assertTrue(inputSchema.getFields().size() > resultFieldList.size());
        Assert.assertThat(resultFieldList, CoreMatchers.is(remoteSchema.getFields()));
    }

    @Test
    public void testGetColumnsWhenSchemaHaveSameColumns() {
        Schema remoteSchema = SchemaBuilder.builder().record("record").fields().requiredString("name").requiredInt("age")
                .requiredInt("year").endRecord();
        Schema inputSchema = SchemaBuilder.builder().record("record").fields().requiredString("name").requiredInt("age")
                .requiredInt("year").endRecord();

        List<Field> resultFieldList = DynamicSchemaUtils.getCommonFieldsForDynamicSchema(remoteSchema, inputSchema);

        Assert.assertEquals(remoteSchema.getFields().size(), resultFieldList.size());
        Assert.assertEquals(inputSchema.getFields().size(), resultFieldList.size());
        Assert.assertThat(resultFieldList, CoreMatchers.is(remoteSchema.getFields()));
    }
}
