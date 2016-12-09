//==============================================================================
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
//==============================================================================
package org.talend.components.service.rest.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.service.common.ComponentServiceImpl;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.components.service.rest.ComponentController;
import org.talend.components.service.rest.serialization.JsonSerializationHelper;
import org.talend.daikon.annotation.ServiceImplementation;
import org.talend.daikon.properties.Properties;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.talend.daikon.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

/**
 * Default implementation of the ComponentController.
 */
@ServiceImplementation
public class ComponentControllerImpl implements ComponentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentServiceImpl.class);

    @Autowired
    private ComponentService componentServiceDelegate;

    private JsonSerializationHelper jsonSerializer = new JsonSerializationHelper();

    @Override
    public StreamingResponseBody getComponentProperties(@PathVariable String name) {
        return outputStream -> {
            try (Writer w = new OutputStreamWriter(outputStream)) {
                w.write(jsonSerializer.toJson(componentServiceDelegate.getComponentProperties(name), name));
            }
        };
    }

    @Override
    public ComponentDefinition getComponentDefinition(String name) {
        return componentServiceDelegate.getComponentDefinition(name);
    }

    @Override
    public ComponentWizard getComponentWizard(String name, String repositoryLocation) {
        return componentServiceDelegate.getComponentWizard(name, repositoryLocation);
    }

    @Override
    public List<ComponentWizard> getComponentWizardsForProperties(ComponentProperties properties, String repositoryLocation) {
        return componentServiceDelegate.getComponentWizardsForProperties(properties, repositoryLocation);
    }

    @Override
    public List<ComponentDefinition> getPossibleComponents(ComponentProperties... properties) throws Throwable {
        return componentServiceDelegate.getPossibleComponents(properties);
    }

    @Override
    public Properties makeFormCancelable(Properties properties, String formName) {
        return componentServiceDelegate.makeFormCancelable(properties, formName);
    }

    @Override
    public Properties cancelFormValues(Properties properties, String formName) {
        return componentServiceDelegate.cancelFormValues(properties, formName);
    }

    @Override
    public Properties validateProperty(String propName, Properties properties) throws Throwable {
        componentServiceDelegate.validateProperty(propName, properties);
        return properties;
    }

    @Override
    public Properties beforePropertyActivate(String propName, Properties properties) throws Throwable {
        componentServiceDelegate.beforePropertyActivate(propName, properties);
        return properties;
    }

    @Override
    public Properties beforePropertyPresent(String propName, Properties properties) throws Throwable {
        componentServiceDelegate.beforePropertyPresent(propName, properties);
        return properties;
    }

    @Override
    public Properties afterProperty(String propName, Properties properties) throws Throwable {
        componentServiceDelegate.afterProperty(propName, properties);
        return properties;
    }

    @Override
    public Properties beforeFormPresent(String formName, Properties properties) throws Throwable {
        componentServiceDelegate.beforeFormPresent(formName, properties);
        return properties;
    }

    @Override
    public Properties afterFormNext(String formName, Properties properties) throws Throwable {
        componentServiceDelegate.afterFormNext(formName, properties);
        return properties;
    }

    @Override
    public Properties afterFormBack(String formName, Properties properties) throws Throwable {
        componentServiceDelegate.afterFormBack(formName, properties);
        return properties;
    }

    @Override
    public Properties afterFormFinish(String formName, Properties properties) throws Throwable {
        componentServiceDelegate.afterFormFinish(formName, properties);
        return properties;
    }

    @Override
    public Set<String> getAllComponentNames() {
        return componentServiceDelegate.getAllComponentNames();
    }

    @Override
    public Set<ComponentDefinition> getAllComponents() {
        return componentServiceDelegate.getAllComponents();
    }

    public Set<ComponentWizardDefinition> getTopLevelComponentWizards() {
        return componentServiceDelegate.getTopLevelComponentWizards();
    }

    @Override
    public void getWizardImageRest(String name, WizardImageType type, HttpServletResponse response) {
        InputStream wizardPngImageStream = componentServiceDelegate.getWizardPngImage(name, type);
        sendStreamBack(response, wizardPngImageStream);
    }

    private void sendStreamBack(final HttpServletResponse response, InputStream inputStream) throws ComponentException {
        try {
            if (inputStream != null) {
                try {
                    IOUtils.copy(inputStream, response.getOutputStream());
                } catch (IOException e) {
                    throw new ComponentException(UNEXPECTED_EXCEPTION, e);
                } finally {
                    inputStream.close();
                }
            } else {// could not get icon so respond a resource_not_found : 404
                response.sendError(SC_NOT_FOUND);
            }
        } catch (IOException e) {// is sendError fails or input stream fails when closing
            LOGGER.error("sendError failed or input stream failed when closing.", e); //$NON-NLS-1$
            throw new ComponentException(UNEXPECTED_EXCEPTION, e);
        }
    }

    @Override
    public void getComponentsImageRest(String name, ComponentImageType type, HttpServletResponse response) {
        InputStream componentPngImageStream = componentServiceDelegate.getComponentPngImage(name, type);
        sendStreamBack(response, componentPngImageStream);
    }

}
