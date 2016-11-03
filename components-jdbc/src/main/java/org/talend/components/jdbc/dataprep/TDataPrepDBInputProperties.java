package org.talend.components.jdbc.dataprep;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

public class TDataPrepDBInputProperties extends FixedConnectorsComponentProperties implements RuntimeSettingProvider {

    public TDataPrepDBInputProperties(String name) {
        super(name);
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

    // basic setting
    public enum DBType {
        MYSQL,
        DERBY
    }

    public Property<DBType> dbTypes = PropertyFactory.newEnum("dbTypes", DBType.class).setRequired();

    public Property<String> jdbcUrl = PropertyFactory.newProperty("jdbcUrl").setRequired();

    public UserPasswordProperties userPassword = new UserPasswordProperties("userPassword");

    public Property<String> sql = PropertyFactory.newString("sql").setRequired(true);

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = CommonUtils.addForm(this, Form.MAIN);

        mainForm.addRow(dbTypes);
        mainForm.addRow(jdbcUrl);
        mainForm.addRow(userPassword.getForm(Form.MAIN));

        mainForm.addRow(main.getForm(Form.REFERENCE));

        mainForm.addRow(sql);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();

        dbTypes.setValue(DBType.MYSQL);
    }

    @Override
    public AllSetting getRuntimeSetting() {
        AllSetting setting = new AllSetting();

        setDriverAndClass(setting);

        setting.setJdbcUrl(this.jdbcUrl.getValue());
        setting.setUsername(this.userPassword.userId.getValue());
        setting.setPassword(this.userPassword.password.getValue());

        setting.setSql(this.sql.getValue());

        return setting;
    }

    private void setDriverAndClass(AllSetting setting) {
        // TODO this is only a simple implement
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("db_type_config.json");
        JSONParser parser = new JSONParser();
        JSONArray ja = null;
        try {
            ja = (JSONArray) parser.parse(new InputStreamReader(is));
        } catch (IOException | ParseException e) {
            throw new ComponentException(e);
        }

        List<String> mavenPaths = new ArrayList<String>();
        String driverClass = null;

        for (Object o : ja) {
            JSONObject jo = (JSONObject) o;
            String id = (String) jo.get("id");
            if (dbTypes.getValue().name().equals(id)) {
                driverClass = (String) jo.get("class");
                JSONArray paths = (JSONArray) jo.get("paths");
                for (Object path : paths) {
                    JSONObject jo_path = (JSONObject) path;
                    mavenPaths.add((String) jo_path.get("path"));
                }
                break;
            }
        }

        setting.setDriverPaths(mavenPaths);
        setting.setDriverClass(driverClass);
    }
}
