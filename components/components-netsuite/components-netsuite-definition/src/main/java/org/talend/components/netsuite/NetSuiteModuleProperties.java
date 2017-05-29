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
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.StringProperty;

/**
 * Base for properties which hold information about target NetSuite record type
 * which a component will be operating on.
 */
public abstract class NetSuiteModuleProperties extends ComponentPropertiesImpl
        implements NetSuiteProvideConnectionProperties {

    public final NetSuiteConnectionProperties connection;

    /**
     * Holds name of target NetSuite record type.
     */
    public final StringProperty moduleName = newString("moduleName"); //$NON-NLS-1$

    public final MainSchemaProperties main;

    public NetSuiteModuleProperties(String name, NetSuiteConnectionProperties connectionProperties) {
        super(name);

        connection = connectionProperties;

        main = new MainSchemaProperties("main");
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        for (Form childForm : connection.getForms()) {
            connection.refreshLayout(childForm);
        }
    }

    @Override
    public NetSuiteConnectionProperties getConnectionProperties() {
        return connection.getEffectiveConnectionProperties();
    }

    /**
     * Get names of NetSuite record types which are available for a component.
     *
     * @return list of NetSuite record types
     */
    public List<NamedThing> getRecordTypes() {
        return NetSuiteComponentDefinition.withDatasetRuntime(this, new Function<NetSuiteDatasetRuntime, List<NamedThing>>() {

            @Override
            public List<NamedThing> apply(NetSuiteDatasetRuntime dataSetRuntime) {
                return dataSetRuntime.getRecordTypes();
            }
        });
    }

    /**
     * Get names of NetSuite record types which are searchable.
     *
     * <p>Some types of NetSuite record can not be searched.
     *
     * @return list of searchable NetSuite record types
     */
    public List<NamedThing> getSearchableTypes() {
        return NetSuiteComponentDefinition.withDatasetRuntime(this, new Function<NetSuiteDatasetRuntime, List<NamedThing>>() {

            @Override
            public List<NamedThing> apply(NetSuiteDatasetRuntime dataSetRuntime) {
                return dataSetRuntime.getSearchableTypes();
            }
        });
    }

    /**
     * Get a schema for a given record type.
     *
     * @param typeName name of record type
     * @return
     */
    public Schema getSchema(final String typeName) {
        return NetSuiteComponentDefinition.withDatasetRuntime(this, new Function<NetSuiteDatasetRuntime, Schema>() {

            @Override
            public Schema apply(NetSuiteDatasetRuntime dataSetRuntime) {
                return dataSetRuntime.getSchema(typeName);
            }
        });
    }

    /**
     * Get design-time specific information about search data model.
     *
     * @param typeName name of searchable record type
     * @return search info
     */
    public SearchInfo getSearchInfo(final String typeName) {
        return NetSuiteComponentDefinition.withDatasetRuntime(this, new Function<NetSuiteDatasetRuntime, SearchInfo>() {

            @Override
            public SearchInfo apply(NetSuiteDatasetRuntime dataSetRuntime) {
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

            @Override
            public Schema apply(NetSuiteDatasetRuntime dataSetRuntime) {
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

            @Override
            public Schema apply(NetSuiteDatasetRuntime dataSetRuntime) {
                return dataSetRuntime.getSchemaForDelete(typeName);
            }
        });
    }

    /**
     * Get list of available search operators.
     *
     * @return list of search operators' names
     */
    public List<String> getSearchFieldOperators() {
        return withDatasetRuntime(this, new Function<NetSuiteDatasetRuntime, List<String>>() {

            @Override
            public List<String> apply(NetSuiteDatasetRuntime dataSetRuntime) {
                return dataSetRuntime.getSearchFieldOperators();
            }
        });
    }

    /**
     * Assert that record type name is correct.
     */
    protected void assertModuleName() {
        if (StringUtils.isEmpty(moduleName.getStringValue())) {
            throw new ComponentException(
                    new ValidationResult(ValidationResult.Result.ERROR, getI18nMessage("error.recordTypeNotSpecified")));
        }
    }

    /**
     * Holds properties of a component's main schema.
     */
    public static class MainSchemaProperties extends SchemaProperties {

        private transient ISchemaListener schemaListener;

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
