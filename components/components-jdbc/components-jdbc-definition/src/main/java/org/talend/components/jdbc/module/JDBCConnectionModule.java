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
package org.talend.components.jdbc.module;

import static org.talend.daikon.properties.presentation.Widget.widget;

import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.talend.components.api.properties.ComponentPropertiesImpl;
import org.talend.components.common.UserPasswordProperties;
import org.talend.components.jdbc.CommonUtils;
import org.talend.components.jdbc.runtime.setting.AllSetting;
import org.talend.daikon.properties.PresentationItem;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

/**
 * common JDBC connection information properties
 *
 */
// have to implement ComponentProperties for the wizard part, not good
public class JDBCConnectionModule extends ComponentPropertiesImpl {

    public Property<String> jdbcUrl = PropertyFactory.newProperty("jdbcUrl").setRequired();

    public DriverTable driverTable = new DriverTable("driverTable");

    public Property<String> driverClass = PropertyFactory.newProperty("driverClass").setRequired();

    public transient PresentationItem selectClass = new PresentationItem("selectClass");

    public UserPasswordProperties userPassword = new UserPasswordProperties("userPassword");

    private boolean useInWizard = false;

    public JDBCConnectionModule(String name) {
        super(name);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        jdbcUrl.setValue("jdbc:");
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form form = CommonUtils.addForm(this, Form.MAIN);
        form.addRow(jdbcUrl);
        form.addRow(widget(driverTable).setWidgetType(Widget.TABLE_WIDGET_TYPE));

        if (useInWizard) {
            form.addRow(Widget.widget(driverClass).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
            form.addColumn(widget(selectClass).setWidgetType(Widget.BUTTON_WIDGET_TYPE));
        } else {
            form.addRow(driverClass);
        }

        form.addRow(userPassword.getForm(Form.MAIN));
    }

    public ValidationResult afterSelectClass() {
        List<String> driverClasses = new ArrayList<>();

        AllSetting setting = new AllSetting();
        setting.setDriverPaths(driverTable.drivers.getValue());
        List<String> jar_maven_paths = setting.getDriverPaths();

        try {
            List<URL> urls = new ArrayList<>();
            for (String maven_path : jar_maven_paths) {
                URL url = new URL(removeQuote(maven_path));
                urls.add(url);
            }

            URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0]), this.getClass().getClassLoader());

            for (URL jarUrl : urls) {
                try (JarInputStream jarInputStream = new JarInputStream(jarUrl.openStream())) {
                    JarEntry nextJarEntry = jarInputStream.getNextJarEntry();
                    while (nextJarEntry != null) {
                        boolean isFile = !nextJarEntry.isDirectory();
                        if (isFile) {
                            String name = nextJarEntry.getName();
                            if (name != null && name.toLowerCase().endsWith(".class")) {
                                String className = changeFileNameToClassName(name);
                                try {
                                    Class clazz = classLoader.loadClass(className);
                                    if (Driver.class.isAssignableFrom(clazz)) {
                                        driverClasses.add(clazz.getName());
                                    }
                                } catch (Throwable th) {
                                    // ignore all the exceptions, especially the class not found exception when look up a class
                                    // outside the jar
                                }
                            }
                        }

                        nextJarEntry = jarInputStream.getNextJarEntry();
                    }
                }
            }
        } catch (Exception ex) {
            return new ValidationResult(ValidationResult.Result.ERROR, ex.getMessage());
        }

        if (driverClasses.isEmpty()) {
            return new ValidationResult(ValidationResult.Result.ERROR,
                    "not found any Driver class, please make sure the jar is right");
        }

        driverClass.setPossibleValues(driverClasses);
        driverClass.setValue(driverClasses.get(0));

        return ValidationResult.OK;
    }

    private String changeFileNameToClassName(String name) {
        name = name.replace('/', '.');
        name = name.replace('\\', '.');
        name = name.substring(0, name.length() - 6);
        return name;
    }

    private String removeQuote(String content) {
        if (content.startsWith("\"") && content.endsWith("\"")) {
            return content.substring(1, content.length() - 1);
        }

        return content;
    }

    public void setNotRequired() {
        this.jdbcUrl.setRequired(false);
        this.driverClass.setRequired(false);
        this.userPassword.userId.setRequired(false);
        this.userPassword.password.setRequired(false);
    }

    public JDBCConnectionModule useInWizard() {// the ui layout is different for component and wizard part
        useInWizard = true;
        return this;
    }

}
