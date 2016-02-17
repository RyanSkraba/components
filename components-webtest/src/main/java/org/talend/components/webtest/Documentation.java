// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.webtest;

import org.apache.cxf.jaxrs.swagger.Swagger2Feature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(name = "service.documentation", havingValue = "true", matchIfMissing = true)
@Configuration
public class Documentation {

    @Value("${service.documentation.description}")
    private String serviceDescription;

    @Value("${service.basepath}")
    private String serviceBasePath;

    @Bean
    public Swagger2Feature swaggerFeature(ApplicationContext context) {
        Swagger2Feature feature = new Swagger2Feature();
        feature.setBasePath(serviceBasePath);
        feature.setDescription(serviceDescription);
        feature.setRunAsFilter(true);
        feature.setResourcePackage("org.talend.components");
        return feature;
    }
}
