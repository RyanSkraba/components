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
package org.talend.components.marketo.runtime.client.rest.type;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.marketo.MarketoConstants;
import org.talend.daikon.avro.AvroUtils;
import org.talend.daikon.avro.SchemaConstants;

import com.google.gson.Gson;

/**
 * Generic Marketo Field Description.
 *
 * This should encapsulate Lead fields description but also CustomObjects fields.
 *
 *
 * For lead fields : {"id":2,"displayName":"Company Name","dataType":"string","length":255,"rest":{"name":"company",
 * "readOnly":false},"soap":{"name":"Company","readOnly":false}}
 *
 * For CustomObject fields : {name='marketoGUID', displayName='Marketo GUID', dataType='string', length=36,
 * updateable=false}
 * 
 */
public class FieldDescription {

    private transient static final Logger LOG = LoggerFactory.getLogger(FieldDescription.class);

    //
    private Integer id;

    private String displayName;

    private String dataType;

    private Integer length;

    private ApiFieldName rest;

    private ApiFieldName soap;

    private String name;

    private Boolean updateable;

    public class ApiFieldName {

        private String name;

        private Boolean readOnly;

        public String getName() {
            return name;
        }

        public ApiFieldName() {
        }

        public ApiFieldName(String name, Boolean readOnly) {
            this.name = name;
            this.readOnly = readOnly;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Boolean getReadOnly() {
            return readOnly;
        }

        public void setReadOnly(Boolean readOnly) {
            this.readOnly = readOnly;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("ApiFieldName{");
            sb.append("name='").append(name).append('\'');
            sb.append(", readOnly=").append(readOnly);
            sb.append('}');
            return sb.toString();
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public ApiFieldName getRest() {
        return rest;
    }

    public void setRest(ApiFieldName rest) {
        this.rest = rest;
    }

    public ApiFieldName getSoap() {
        return soap;
    }

    public void setSoap(ApiFieldName soap) {
        this.soap = soap;
    }

    public String getName() {
        if (name != null) {
            return name;
        }
        return rest.getName();
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getUpdateable() {
        return updateable;
    }

    public void setUpdateable(Boolean updateable) {
        this.updateable = updateable;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("FieldDescription{");
        sb.append("id=").append(id);
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", dataType='").append(dataType).append('\'');
        sb.append(", length=").append(length);
        sb.append(", rest=").append(rest);
        sb.append(", soap=").append(soap);
        sb.append('}');
        return sb.toString();
    }

    public Field toAvroField() {
        Schema fs = null;
        String fname = getName().replaceAll("-", "_");
        //
        fs = SchemaBuilder.builder().unionOf().nullType().and().stringType().endUnion();
        switch (getDataType()) {
        case ("string"):
        case ("text"):
        case ("phone"):
        case ("email"):
        case ("url"):
        case ("lead_function"):
        case ("reference"):
            fs = SchemaBuilder.builder().unionOf().nullType().and().stringType().endUnion();
            break;
        case ("integer"):
            fs = SchemaBuilder.builder().unionOf().nullType().and().intType().endUnion();
            break;
        case ("boolean"):
            fs = SchemaBuilder.builder().unionOf().nullType().and().booleanType().endUnion();
            break;
        case ("float"):
        case ("currency"):
            fs = SchemaBuilder.builder().unionOf().nullType().and().floatType().endUnion();
            break;
        case ("date"):
        case ("datetime"):
            fs = SchemaBuilder.builder().unionOf().nullType().and().longType().endUnion();
            break;
        default:
            LOG.warn("Non managed type : {}. for {}. Defaulting to String.", getDataType(), this);
        }
        Field f = new Field(fname, fs, getDisplayName(), (Object) null);
        f.addProp(SchemaConstants.TALEND_COLUMN_DB_COLUMN_NAME, getName());
        if (getLength() != null) {
            f.addProp(SchemaConstants.TALEND_COLUMN_DB_LENGTH, getLength().toString());
        }
        if (AvroUtils._long().equals(fs.getTypes().get(1))) {
            f.addProp(SchemaConstants.TALEND_COLUMN_PATTERN, MarketoConstants.DATETIME_PATTERN_REST);
            f.addProp(SchemaConstants.JAVA_CLASS_FLAG, "java.util.Date");
        }
        if (updateable != null && !updateable) {
            f.addProp(SchemaConstants.TALEND_IS_LOCKED, "true");
        }
        //
        if (getId() != null) {
            f.addProp("mktoId", getId().toString());
        }
        f.addProp("mktoType", getDataType());
        return f;
    }

    public static Schema getSchemaFromJson(String schemaName, String jsonFields, String jsonKeys) {
        Gson gson = new Gson();
        FieldDescription[] fields = gson.fromJson(jsonFields, FieldDescription[].class);
        String[] keys = gson.fromJson(jsonKeys, String[].class);
        return getSchemaForThisFields(schemaName, fields, keys);
    }

    public static Schema getSchemaForThisFields(String schemaName, FieldDescription[] fields, String[] keys) {
        Schema schema = Schema.createRecord(schemaName, "", "", false);
        List<Field> fieldList = new ArrayList<>();
        if (fields == null) {
            return null;
        }
        for (FieldDescription field : fields) {
            Field f = field.toAvroField();
            for (String key : keys) {
                if (field.getName().equals(key)) {
                    f.addProp(SchemaConstants.TALEND_COLUMN_IS_KEY, "true");
                }
            }
            fieldList.add(f);
        }
        schema.setFields(fieldList);

        return schema;
    }

}
