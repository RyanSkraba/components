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

package org.talend.components.netsuite;

import static org.talend.components.netsuite.NetSuiteComponentDefinition.withDatasetRuntime;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.List;

import org.apache.avro.Schema;
import org.apache.commons.lang3.StringUtils;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.common.SchemaProperties;
import org.talend.components.netsuite.connection.NetSuiteConnectionProperties;
import org.talend.components.netsuite.schema.SearchInfo;
import org.talend.daikon.NamedThing;
import org.talend.daikon.java8.Function;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.property.StringProperty;

/**
 *
 */
public abstract class NetSuiteModuleProperties extends ComponentPropertiesImpl
        implements NetSuiteProvideConnectionProperties {

    public final NetSuiteConnectionProperties connection;

    public final StringProperty moduleName = newString("moduleName"); //$NON-NLS-1$

    public final MainSchemaProperties main;

    public NetSuiteModuleProperties(String name, NetSuiteConnectionProperties connectionProperties) {
        super(name);

        connection = connectionProperties;

        main = new MainSchemaProperties("main");
    }

    @Override
    public NetSuiteConnectionProperties getConnectionProperties() {
        return connection.getEffectiveConnectionProperties();
    }

    public List<NamedThing> getRecordTypes() {
        return NetSuiteComponentDefinition.withDatasetRuntime(this, new Function<NetSuiteDatasetRuntime, List<NamedThing>>() {
            @Override public List<NamedThing> apply(NetSuiteDatasetRuntime dataSetRuntime) {
                return dataSetRuntime.getRecordTypes();
            }
        });
    }

    public List<NamedThing> getSearchableTypes() {
        return NetSuiteComponentDefinition.withDatasetRuntime(this, new Function<NetSuiteDatasetRuntime, List<NamedThing>>() {
            @Override public List<NamedThing> apply(NetSuiteDatasetRuntime dataSetRuntime) {
                return dataSetRuntime.getSearchableTypes();
            }
        });
    }

    public Schema getSchema(final String typeName) {
        return NetSuiteComponentDefinition.withDatasetRuntime(this, new Function<NetSuiteDatasetRuntime, Schema>() {
            @Override public Schema apply(NetSuiteDatasetRuntime dataSetRuntime) {
                return dataSetRuntime.getSchema(typeName);
            }
        });
    }

    public SearchInfo getSearchInfo(final String typeName) {
        return NetSuiteComponentDefinition.withDatasetRuntime(this, new Function<NetSuiteDatasetRuntime, SearchInfo>() {
            @Override public SearchInfo apply(NetSuiteDatasetRuntime dataSetRuntime) {
                return dataSetRuntime.getSearchInfo(typeName);
            }
        });
    }

    /**
     * Get schema for update <code>incoming</code> flow.
     *
     * @param typeName name of object's type
     * @return schema
     */
    public Schema getSchemaForUpdate(final String typeName) {
        return NetSuiteComponentDefinition.withDatasetRuntime(this, new Function<NetSuiteDatasetRuntime, Schema>() {
            @Override public Schema apply(NetSuiteDatasetRuntime dataSetRuntime) {
                return dataSetRuntime.getSchemaForUpdate(typeName);
            }
        });
    }

    /**
     * Get schema for delete <code>incoming</code> flow.
     *
     * @param typeName name of object's type
     * @return schema
     */
    public Schema getSchemaForDelete(final String typeName) {
        return NetSuiteComponentDefinition.withDatasetRuntime(this, new Function<NetSuiteDatasetRuntime, Schema>() {
            @Override public Schema apply(NetSuiteDatasetRuntime dataSetRuntime) {
                return dataSetRuntime.getSchemaForDelete(typeName);
            }
        });
    }

    public List<String> getSearchFieldOperators() {
        return withDatasetRuntime(this, new Function<NetSuiteDatasetRuntime, List<String>>() {
            @Override public List<String> apply(NetSuiteDatasetRuntime dataSetRuntime) {
                return dataSetRuntime.getSearchFieldOperators();
            }
        });
    }

    protected void assertModuleName() {
        if (StringUtils.isEmpty(moduleName.getStringValue())) {
            throw new ComponentException(new ValidationResult()
                    .setStatus(ValidationResult.Result.ERROR)
                    .setMessage(getI18nMessage("error.recordTypeNotSpecified")));
        }
    }

    public static class MainSchemaProperties extends SchemaProperties {
        protected transient ISchemaListener schemaListener;

        public MainSchemaProperties(String name) {
            super(name);
        }

        public ISchemaListener getSchemaListener() {
            return schemaListener;
        }

        public void setSchemaListener(ISchemaListener schemaListener) {
            this.schemaListener = schemaListener;
        }

        public void afterSchema() {
            if (schemaListener != null) {
                schemaListener.afterSchema();
            }
        }

    }
}
