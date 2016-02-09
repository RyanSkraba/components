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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.talend.daikon.schema.SchemaElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.models.dto.ApiInfo;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;

@Configuration
@ConditionalOnProperty(name = "service.documentation", havingValue = "true", matchIfMissing = true)
@EnableSwagger
public class Documentation {

    @Value("${service.documentation.name}")
    private String serviceDisplayName;

    @Value("${service.documentation.description}")
    private String serviceDescription;

    @Value("#{'${service.documentation.path}'.split(',')}")
    private String[] servicePaths;

    private SpringSwaggerConfig springSwaggerConfig;

    @Autowired
    public void setSpringSwaggerConfig(SpringSwaggerConfig springSwaggerConfig) {
        this.springSwaggerConfig = springSwaggerConfig;
    }

    @Bean
    public SwaggerSpringMvcPlugin customImplementation() {
        ApiInfo apiInfo = new ApiInfo(serviceDisplayName, serviceDescription, StringUtils.EMPTY, StringUtils.EMPTY,
                StringUtils.EMPTY, StringUtils.EMPTY);
        return new SwaggerSpringMvcPlugin(springSwaggerConfig).apiInfo(apiInfo).includePatterns(servicePaths);
    }

    // @Bean
    // public ObjectMapper objectMapper() {
    // final ObjectMapper mapper = new ObjectMapper();
    // mapper.addMixInAnnotations(org.talend.daikon.schema.AbstractSchemaElement.class,
    // AbstractSchemaElementMixIn.class);
    // return mapper;
    // }

    @Bean
    @Primary
    public Jackson2ObjectMapperBuilder objectMapperBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.mixIn(org.talend.daikon.schema.AbstractSchemaElement.class, AbstractSchemaElementMixIn.class);
        return builder;
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        objectMapper.addMixIn(org.talend.daikon.schema.AbstractSchemaElement.class, AbstractSchemaElementMixIn.class);
        objectMapper.addMixInAnnotations(org.talend.daikon.schema.AbstractSchemaElement.class, AbstractSchemaElementMixIn.class);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        return converter;
    }

    interface AbstractSchemaElementMixIn {

        @JsonIgnore
        public SchemaElement setPossibleValues(Object... values); // we don't need it!

    }
}
