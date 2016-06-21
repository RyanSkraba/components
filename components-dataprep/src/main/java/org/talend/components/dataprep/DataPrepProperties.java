// ============================================================================ 
// 
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com 
// 
// This source code is available under agreement available at 
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt 
// 
// You should have received a copy of the agreement 
// along with this program; if not, write to Talend SA 
// 9 rue Pages 92150 Suresnes, France 
// 
// ============================================================================ 
package org.talend.components.dataprep;

import org.apache.avro.Schema;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.components.common.SchemaProperties;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.EnumSet;

public abstract class DataPrepProperties extends FixedConnectorsComponentProperties {

    private static final Logger LOG = LoggerFactory.getLogger(DataPrepProperties.class);

    public final SchemaProperties schema = new SchemaProperties("schema");

    public final PropertyPathConnector mainConnector = new PropertyPathConnector(Connector.MAIN_NAME, "schema");

    public final Property<String> url = PropertyFactory.newString("url").setRequired();

    public Property<String> login = PropertyFactory.newString("login").setRequired();

    public Property<String> pass = PropertyFactory.newString("pass").setRequired()
            .setFlags(EnumSet.of(Property.Flags.ENCRYPT, Property.Flags.SUPPRESS_LOGGING));

    public DataPrepProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();
        Form form = new Form(this, Form.MAIN);
        form.addRow(schema.getForm(Form.REFERENCE));
        form.addRow(url);
        form.addRow(login);
        form.addRow(Widget.widget(pass).setWidgetType(Widget.HIDDEN_TEXT_WIDGET_TYPE));
    }

    public ValidationResult validateUrl() throws IOException {
        UrlValidator urlValidator = new UrlValidator(UrlValidator.ALLOW_LOCAL_URLS);

        if (!urlValidator.isValid(url.getStringValue())) {
            LOG.debug(getI18nMessage("error.wrongUrlSyntax"));
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR)
                    .setMessage(getI18nMessage("error.wrongUrlSyntax"));
        }

        Request request = Request.Head(url.getStringValue());
        int statusCode = 0;
        try {
            statusCode = request.execute().returnResponse().getStatusLine().getStatusCode();
        } catch (IOException e) {
            LOG.debug(getI18nMessage("error.urlIsNotAccessible"), e.getMessage());
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR)
                    .setMessage(getI18nMessage("error.urlIsNotAccessible", e));
        }
        if (statusCode == 0 || statusCode != HttpServletResponse.SC_OK) {
            LOG.debug(getI18nMessage("error.urlIsNotAccessible"));
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR)
                    .setMessage(getI18nMessage("error.urlIsNotAccessible"));
        }
        return ValidationResult.OK;
    }

    public ValidationResult validateLogin() throws IOException {
        EmailValidator emailValidator = EmailValidator.getInstance();

        if (!emailValidator.isValid(login.getStringValue())) {
            LOG.debug(getI18nMessage("error.wrongLogin"));
            return new ValidationResult().setStatus(ValidationResult.Result.ERROR)
                    .setMessage(getI18nMessage("error.wrongLogin"));
        }
        return ValidationResult.OK;
    }

    protected boolean isNotNullAndNotEmpty(String propertyStringValue) {
        return propertyStringValue == null || propertyStringValue.isEmpty();
    }

    public Schema getSchema() {
        return schema.schema.getValue();
    }
}