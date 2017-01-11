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
package org.talend.components.jdbc.dataprep.di;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.components.common.UserPasswordProperties;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.RuntimeSettingProvider;
import org.talend.components.jdbc.dataprep.DBType;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

// this component may not be necessary in future, so allow the duplicated code now.
public class TDataPrepDBInputProperties extends FixedConnectorsComponentProperties implements RuntimeSettingProvider {

    private transient Map<String, DBType> dyTypesInfo = new HashMap<String, DBType>();

    private final static String CONFIG_FILE_lOCATION_KEY = "org.talend.component.jdbc.config.file";

    public TDataPrepDBInputProperties(String name) {
        super(name);

        String config_file = System.getProperty(CONFIG_FILE_lOCATION_KEY);
        try (InputStream is = config_file != null ? (new FileInputStream(config_file))
                : this.getClass().getClassLoader().getResourceAsStream("db_type_config.json")) {
            JSONParser parser = new JSONParser();
            JSONArray ja = null;
            try {
                ja = (JSONArray) parser.parse(new InputStreamReader(is));
            } catch (ParseException e) {
                throw new ComponentException(e);
            }

            for (Object o : ja) {
                DBType type = new DBType();
                JSONObject jo = (JSONObject) o;
                type.id = (String) jo.get("id");
                type.clazz = (String) jo.get("class");
                type.url = (String) jo.get("url");
                JSONArray paths = (JSONArray) jo.get("paths");
                for (Object path : paths) {
                    JSONObject jo_path = (JSONObject) path;
                    type.paths.add((String) jo_path.get("path"));
                }

                dyTypesInfo.put(type.id, type);
            }

        } catch (IOException e) {
            throw new ComponentException(e);
        }
    }

    // declare the connector and schema
    public final PropertyPathConnector mainConnector = new PropertyPathConnector(Connector.MAIN_NAME, "main");

    public SchemaProperties main = new SchemaProperties("main");

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.singleton(mainConnector);
        }
        return Collections.emptySet();
    }

    public Property<String> dbTypes = PropertyFactory.newString("dbTypes").setRequired();

    public Property<String> jdbcUrl = PropertyFactory.newProperty("jdbcUrl").setRequired();

    public UserPasswordProperties userPassword = new UserPasswordProperties("userPassword");

    public Property<String> sql = PropertyFactory.newString("sql").setRequired(true);

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = CommonUtils.addForm(this, Form.MAIN);

        mainForm.addRow(Widget.widget(dbTypes).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        mainForm.addRow(jdbcUrl);

        mainForm.addRow(userPassword.getForm(Form.MAIN));

        mainForm.addRow(main.getForm(Form.REFERENCE));

        mainForm.addRow(sql);
    }

    public void afterDbTypes() {
        DBType currentDBType = this.getCurrentDBType();
        jdbcUrl.setValue("\"" + currentDBType.url + "\"");
    }

    private DBType getCurrentDBType() {
        return dyTypesInfo.get(dbTypes.getValue());
    }

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

        jdbcUrl.setValue("\"" + defaultDBType.url + "\"");
    }

    @Override
    public AllSetting getRuntimeSetting() {
        AllSetting setting = new AllSetting();

        setting.setDriverPaths(getCurrentDriverPaths());
        setting.setDriverClass(getCurrentDriverClass());
        setting.setJdbcUrl(jdbcUrl.getValue());

        setting.setUsername(userPassword.userId.getValue());
        setting.setPassword(userPassword.password.getValue());

        setting.setSql(this.sql.getValue());

        setting.setSchema(main.schema.getValue());

        return setting;
    }

    public List<String> getCurrentDriverPaths() {
        List<String> mavenPaths = new ArrayList<String>();

        DBType currentDBType = dyTypesInfo.get(dbTypes.getValue());
        mavenPaths.addAll(currentDBType.paths);

        return mavenPaths;
    }

    public String getCurrentDriverClass() {
        DBType currentDBType = dyTypesInfo.get(dbTypes.getValue());
        return currentDBType.clazz;
    }

}
