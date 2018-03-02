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
package org.talend.components.jdbc.datastore;

import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newProperty;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.dataprep.DBType;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.properties.PropertiesImpl;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JDBCDatastoreProperties extends PropertiesImpl implements DatastoreProperties, RuntimeSettingProvider {

    public static final Logger LOG = LoggerFactory.getLogger(JDBCDatastoreProperties.class);

    private transient Map<String, DBType> dyTypesInfo = new HashMap<String, DBType>();

    private final static String CONFIG_FILE_lOCATION_KEY = "org.talend.component.jdbc.config.file";

    public JDBCDatastoreProperties(String name) {
        super(name);

        String config_file = System.getProperty(CONFIG_FILE_lOCATION_KEY);
        InputStream is;
        if (config_file != null) {// priority to the system property
            try {
                is = new FileInputStream(config_file);
            } catch (FileNotFoundException e) {
                throw new ComponentException(CommonErrorCodes.UNABLE_TO_READ_CONTENT, e);
            }
        } else {// then look in the classpath
            is = this.getClass().getClassLoader().getResourceAsStream("jdbc_config.json");
            if (is == null) {// use the default provided configuration file
                is = this.getClass().getClassLoader().getResourceAsStream("db_type_config.json");
            } // else already set.
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = null;
            jsonNode = objectMapper.readTree(new InputStreamReader(is));

            for (JsonNode jo : jsonNode) {
                DBType type = new DBType();
                type.id = jo.get("id").asText();
                type.clazz = jo.get("class").asText();
                type.url = jo.get("url").asText();
                JsonNode paths = jo.get("paths");
                for (JsonNode path : paths) {
                    JsonNode jo_path = (JsonNode) path;
                    type.paths.add(jo_path.get("path").asText());
                }

                dyTypesInfo.put(type.id, type);
            }

        } catch (IOException e) {
            throw new ComponentException(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOG.error("failed to close JDBC config file", e);
                }
            } // else nothing to close
        }
    }

    public Property<String> dbTypes = PropertyFactory.newString("dbTypes").setRequired();

    public Property<String> jdbcUrl = PropertyFactory.newProperty("jdbcUrl").setRequired();

    public Property<String> userId = newProperty("userId").setRequired();

    public Property<String> password = newProperty("password").setRequired()
            .setFlags(EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    @Override
    public void setupProperties() {
        super.setupProperties();

        List<String> dbTypesId = new ArrayList<String>();
        for (String id : dyTypesInfo.keySet()) {
            dbTypesId.add(id);
        }

        DBType defaultDBType = dyTypesInfo.get(dbTypesId.get(0));

        dbTypes.setPossibleValues(dbTypesId);
        dbTypes.setValue(defaultDBType.id);

        jdbcUrl.setValue(defaultDBType.url);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = CommonUtils.addForm(this, Form.MAIN);

        mainForm.addRow(Widget.widget(dbTypes).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addRow(Widget.widget(jdbcUrl).setWidgetType(Widget.TEXT_AREA_WIDGET_TYPE));

        mainForm.addRow(userId);
        mainForm.addRow(widget(password).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
    }

    public void afterDbTypes() {
        DBType currentDBType = this.getCurrentDBType();
        jdbcUrl.setValue(currentDBType.url);
    }

    @Override
    public AllSetting getRuntimeSetting() {
        AllSetting setting = new AllSetting();

        setting.setDriverPaths(getCurrentDriverPaths());
        setting.setDriverClass(getCurrentDriverClass());
        setting.setJdbcUrl(jdbcUrl.getValue());

        setting.setUsername(userId.getValue());
        setting.setPassword(password.getValue());

        return setting;
    }

    private DBType getCurrentDBType() {
        return dyTypesInfo.get(dbTypes.getValue());
    }

    public List<String> getCurrentDriverPaths() {
        List<String> mavenPaths = new ArrayList<String>();

        DBType currentDBType = dyTypesInfo.get(dbTypes.getValue());

        if (currentDBType != null) {
            mavenPaths.addAll(currentDBType.paths);
        }

        return mavenPaths;
    }

    public String getCurrentDriverClass() {
        DBType currentDBType = dyTypesInfo.get(dbTypes.getValue());

        if (currentDBType == null) {
            return null;
        }

        return currentDBType.clazz;
    }

}
