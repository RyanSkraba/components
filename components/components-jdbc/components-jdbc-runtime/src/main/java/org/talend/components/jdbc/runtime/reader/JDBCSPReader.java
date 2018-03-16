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
package org.talend.components.jdbc.runtime.reader;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.IndexedRecord;
import org.talend.components.api.component.runtime.AbstractBoundedReader;
import org.talend.components.api.component.runtime.Result;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.avro.JDBCSPIndexedRecordCreator;
import org.talend.components.jdbc.runtime.JDBCSPSource;
import org.talend.components.jdbc.runtime.setting.AllSetting;

/**
 * JDBC reader for JDBC SP
 *
 */
public class JDBCSPReader extends AbstractBoundedReader<IndexedRecord> {

    protected RuntimeSettingProvider properties;

    protected RuntimeContainer container;

    protected Connection conn;

    private JDBCSPSource source;

    private CallableStatement cs;

    private Result result;

    private boolean useExistedConnection;

    private AllSetting setting;

    public JDBCSPReader(RuntimeContainer container, JDBCSPSource source, RuntimeSettingProvider props) {
        super(source);
        this.container = container;
        this.properties = props;
        this.source = (JDBCSPSource) getCurrentSource();

        this.setting = props.getRuntimeSetting();
        this.useExistedConnection = setting.getReferencedComponentId() != null;
    }

    @Override
    public boolean start() throws IOException {
        result = new Result();

        try {
            conn = source.getConnection(container);
        } catch (ClassNotFoundException | SQLException e) {
            throw CommonUtils.newComponentException(e);
        }

        return true;
    }

    @Override
    public boolean advance() throws IOException {
        return false;// only one row
    }

    private JDBCSPIndexedRecordCreator indexedRecordCreator;

    @Override
    public IndexedRecord getCurrent() {
        result.totalCount++;
        try {
            cs = conn.prepareCall(source.getSPStatement(setting));

            Schema componentSchema = CommonUtils.getMainSchemaFromInputConnector((ComponentProperties) source.properties);
            Schema outputSchema = CommonUtils.getOutputSchema((ComponentProperties) source.properties);

            source.fillParameters(cs, componentSchema, null, null, setting);

            cs.execute();

            if (indexedRecordCreator == null) {
                indexedRecordCreator = new JDBCSPIndexedRecordCreator();
                indexedRecordCreator.init(componentSchema, outputSchema, setting);
            }

            IndexedRecord outputRecord = indexedRecordCreator.createOutputIndexedRecord(cs, null);

            return outputRecord;
        } catch (SQLException e) {
            throw CommonUtils.newComponentException(e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (cs != null) {
                cs.close();
                cs = null;
            }

            if (!useExistedConnection && conn != null) {
                conn.close();
                conn = null;
            }
        } catch (SQLException e) {
            throw CommonUtils.newComponentException(e);
        }
    }

    @Override
    public Map<String, Object> getReturnValues() {
        return result.toMap();
    }

}
