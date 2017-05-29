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

package org.talend.components.netsuite.client.tools;

import static org.talend.components.netsuite.client.model.beans.Beans.toInitialLower;
import static org.talend.components.netsuite.client.model.beans.Beans.toInitialUpper;
import static org.talend.components.netsuite.client.model.TypeUtils.collectXmlTypes;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlSeeAlso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.netsuite.client.model.beans.EnumAccessor;
import org.talend.components.netsuite.client.model.beans.Beans;
import org.talend.components.netsuite.client.model.RecordTypeDesc;
import org.talend.components.netsuite.client.model.SearchRecordTypeDesc;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

/**
 * Generates descriptors for NetSuite record types and search record types.
 *
 * <p>MetaDataModelGen class is not used in runtime, this tool is intended for development purposes only.
 */
public class MetaDataModelGen {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected Class<?> recordBaseClass;
    protected Class<? extends Enum> recordTypeEnumClass;
    protected EnumAccessor recordTypeEnumAccessor;

    protected Set<Class<?>> searchRecordBaseClasses = new HashSet<>();
    protected Class<? extends Enum> searchRecordTypeEnumClass;
    protected EnumAccessor searchRecordTypeEnumAccessor;

    protected Class<?> recordRefClass;

    protected Set<String> standardEntityTypes = new HashSet<>();
    protected Set<String> standardTransactionTypes = new HashSet<>();
    protected Set<String> standardItemTypes = new HashSet<>();

    protected Map<String, String> additionalRecordTypes = new HashMap<>();
    protected Map<String, RecordTypeSpec> recordTypeMap = new HashMap<>();

    protected Map<String, String> additionalRecordTypeSearchMappings = new HashMap<>();

    protected Map<String, String> additionalSearchRecordTypes = new HashMap<>();
    protected Map<String, SearchRecordTypeSpec> searchRecordTypeMap = new HashMap<>();

    protected ClassName recordTypeEnumClassName;
    protected ClassName searchRecordTypeEnumClassName;

    protected File outputFolder;

    public MetaDataModelGen() {
    }

    public void setRecordBaseClass(Class<?> recordBaseClass) {
        this.recordBaseClass = recordBaseClass;
    }

    public void setRecordTypeEnumClass(Class<? extends Enum> recordTypeEnumClass) {
        this.recordTypeEnumClass = recordTypeEnumClass;
        this.recordTypeEnumAccessor = Beans.getEnumAccessor(recordTypeEnumClass);
    }

    public void setSearchRecordBaseClasses(Collection<Class<?>> searchRecordBaseClasses) {
        this.searchRecordBaseClasses.addAll(searchRecordBaseClasses);
    }

    public void setSearchRecordTypeEnumClass(Class<? extends Enum> searchRecordTypeEnumClass) {
        this.searchRecordTypeEnumClass = searchRecordTypeEnumClass;
        this.searchRecordTypeEnumAccessor = Beans.getEnumAccessor(searchRecordTypeEnumClass);
    }

    public void setRecordRefClass(Class<?> recordRefClass) {
        this.recordRefClass = recordRefClass;
    }

    public void genRecordTypeMetaDataModel() {

        Set<Class<?>> recordClasses = new HashSet<>();
        collectXmlTypes(recordBaseClass, recordBaseClass, recordClasses);

        Set<String> unresolvedTypeNames = new HashSet<>();

        for (Class<?> clazz : recordClasses) {
            if (clazz == recordBaseClass
                    || !recordBaseClass.isAssignableFrom(clazz)
                    || Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }

            RecordTypeSpec spec = null;

            String recordTypeClassSimpleName = clazz.getSimpleName();
            String recordTypeName = null;
            Enum<?> recordTypeEnumValue = null;
            String recordTypeEnumConstantName = null;

            if (additionalRecordTypes.containsKey(recordTypeClassSimpleName)) {
                recordTypeName = toInitialLower(recordTypeClassSimpleName);
                recordTypeEnumConstantName = additionalRecordTypes.get(recordTypeClassSimpleName);
                spec = new RecordTypeSpec();

            } else {
                try {
                    recordTypeEnumValue = recordTypeEnumAccessor.getEnumValue(
                            toInitialLower(recordTypeClassSimpleName));
                    recordTypeEnumConstantName = recordTypeEnumValue.name();
                    recordTypeName = recordTypeEnumAccessor.getStringValue(recordTypeEnumValue);

                    spec = new RecordTypeSpec();
                } catch (IllegalArgumentException e) {
                    unresolvedTypeNames.add(recordTypeClassSimpleName);
                }
            }

            if (spec != null) {
                if (recordTypeMap.containsKey(recordTypeName)) {
                    throw new IllegalArgumentException("Record type already registered: " + recordTypeClassSimpleName);
                }

                String searchRecordTypeName = null;
                String recordTypeNameCapitalized = toInitialUpper(recordTypeName);
                if (standardEntityTypes.contains(recordTypeNameCapitalized)) {
                    try {
                        Enum<?> searchRecordType = searchRecordTypeEnumAccessor.getEnumValue(recordTypeName);
                        searchRecordTypeName = searchRecordTypeEnumAccessor.getStringValue(searchRecordType);
                    } catch (IllegalArgumentException e) {
                        logger.warn("Couldn't automatically determine search record type: '" + recordTypeName + "'");
                    }
                } else if (standardTransactionTypes.contains(recordTypeNameCapitalized)) {
                    searchRecordTypeName = "transaction";
                } else if (standardItemTypes.contains(recordTypeNameCapitalized)) {
                    searchRecordTypeName = "item";
                }

                if (additionalSearchRecordTypes.containsKey(recordTypeName)) {
                    searchRecordTypeName = recordTypeName;
                } else if (additionalRecordTypeSearchMappings.containsKey(recordTypeNameCapitalized)) {
                    searchRecordTypeName = additionalRecordTypeSearchMappings.get(recordTypeNameCapitalized);
                }

                spec.setName(recordTypeName);
                spec.setTypeName(recordTypeClassSimpleName);
                spec.setEnumValue(recordTypeEnumValue);
                spec.setEnumConstantName(recordTypeEnumConstantName);
                spec.setRecordClass(clazz);
                spec.setSearchRecordTypeName(searchRecordTypeName);

                recordTypeMap.put(recordTypeName, spec);
            }
        }

        if (!unresolvedTypeNames.isEmpty()) {
            logger.warn("Unresolved record types detected: {}", unresolvedTypeNames);
            System.out.println("[WARNING] Unresolved record types detected: " + unresolvedTypeNames);
        }
    }

    public void genSearchRecordTypeMetaDataModel() {
        Collection<Class<?>> searchRecordClasses = new HashSet<>();

        for (Class<?> searchRecordBaseClass : searchRecordBaseClasses) {
            XmlSeeAlso xmlSeeAlso = searchRecordBaseClass.getAnnotation(XmlSeeAlso.class);
            for (Class<?> clazz : xmlSeeAlso.value()) {
                if (clazz == searchRecordBaseClass
                        || !searchRecordBaseClass.isAssignableFrom(clazz)
                        || Modifier.isAbstract(clazz.getModifiers())) {
                    continue;
                }
                searchRecordClasses.add(clazz);
            }
        }

        Set<String> searchRecordTypeSet = new HashSet<>();
        for (Enum value : searchRecordTypeEnumClass.getEnumConstants()) {
            String searchRecordTypeName = searchRecordTypeEnumAccessor.getStringValue(value);
            searchRecordTypeSet.add(searchRecordTypeName);
        }
        searchRecordTypeSet.addAll(additionalSearchRecordTypes.keySet());

        Map<String, Class<?>> searchRecordClassMap = new HashMap<>();
        for (Class<?> clazz : searchRecordClasses) {
            String searchRecordTypeName = clazz.getSimpleName();
            if (searchRecordClassMap.containsKey(searchRecordTypeName)) {
                throw new IllegalStateException("Search record class already registered: " + searchRecordTypeName + ", " + clazz);
            }
            searchRecordClassMap.put(searchRecordTypeName, clazz);
        }

        Set<Class<?>> unresolvedSearchRecords = new HashSet<>(searchRecordClassMap.values());

        for (String searchRecordType : searchRecordTypeSet) {
            String searchRecordTypeName = toInitialUpper(searchRecordType);

            Class<?> searchClass;
            Class<?> searchBasicClass;
            Class<?> searchAdvancedClass;

            searchClass = searchRecordClassMap.get(searchRecordTypeName + "Search");
            unresolvedSearchRecords.remove(searchClass);

            searchBasicClass = searchRecordClassMap.get(searchRecordTypeName + "SearchBasic");
            unresolvedSearchRecords.remove(searchBasicClass);

            searchAdvancedClass = searchRecordClassMap.get(searchRecordTypeName + "SearchAdvanced");
            unresolvedSearchRecords.remove(searchAdvancedClass);

            if (searchBasicClass != null) {
                Enum<?> searchRecordEnumValue = null;
                String searchRecordTypeEnumConstantName = null;
                try {
                    searchRecordEnumValue = searchRecordTypeEnumAccessor.getEnumValue(searchRecordType);
                    searchRecordTypeEnumConstantName = searchRecordEnumValue.name();
                } catch (IllegalArgumentException e) {
                    searchRecordTypeEnumConstantName = additionalSearchRecordTypes.get(searchRecordType);
                }

                SearchRecordTypeSpec spec = new SearchRecordTypeSpec();
                spec.setName(searchRecordType);
                spec.setTypeName(searchRecordTypeName);
                spec.setEnumConstantName(searchRecordTypeEnumConstantName);
                spec.setSearchClass(searchClass);
                spec.setSearchBasicClass(searchBasicClass);
                spec.setSearchAdvancedClass(searchAdvancedClass);

                searchRecordTypeMap.put(spec.getName(), spec);
            }
        }

        if (!unresolvedSearchRecords.isEmpty()) {
            logger.warn("Unresolved search record types detected: {}", unresolvedSearchRecords);
            System.out.println("[WARNING] Unresolved search record types detected: " + unresolvedSearchRecords);
        }
    }

    public void genRecordTypeEnumClass() throws IOException {
        List<RecordTypeSpec> specs = new ArrayList<>(recordTypeMap.values());
        Collections.sort(specs, new Comparator<RecordTypeSpec>() {
            @Override public int compare(RecordTypeSpec o1, RecordTypeSpec o2) {
                return o1.getEnumConstantName().compareTo(o2.getEnumConstantName());
            }
        });

        TypeSpec.Builder builder = TypeSpec.enumBuilder(recordTypeEnumClassName.simpleName())
                .addSuperinterface(RecordTypeDesc.class)
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
                .addField(String.class, "type",
                        javax.lang.model.element.Modifier.PRIVATE, javax.lang.model.element.Modifier.FINAL)
                .addField(String.class, "typeName",
                        javax.lang.model.element.Modifier.PRIVATE, javax.lang.model.element.Modifier.FINAL)
                .addField(Class.class, "recordClass",
                        javax.lang.model.element.Modifier.PRIVATE, javax.lang.model.element.Modifier.FINAL)
                .addField(String.class, "searchRecordType",
                        javax.lang.model.element.Modifier.PRIVATE, javax.lang.model.element.Modifier.FINAL);

        for (RecordTypeSpec spec : specs) {
            builder.addEnumConstant(spec.getEnumConstantName(), TypeSpec.anonymousClassBuilder(
                    "$S, $S, $T.class, $S", spec.getName(), spec.getTypeName(), spec.getRecordClass(),
                    spec.getSearchRecordTypeName()).build());
        }

        builder.addMethod(MethodSpec.constructorBuilder()
                .addParameter(String.class, "type")
                .addParameter(String.class, "typeName")
                .addParameter(Class.class, "recordClass")
                .addParameter(String.class, "searchRecordType")
                .addStatement("this.$N = $N", "type", "type")
                .addStatement("this.$N = $N", "typeName", "typeName")
                .addStatement("this.$N = $N", "recordClass", "recordClass")
                .addStatement("this.$N = $N", "searchRecordType", "searchRecordType")
                .build());

        builder.addMethod(MethodSpec.methodBuilder("getType")
                .addAnnotation(Override.class)
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("return this.$N", "type")
                .build());
        builder.addMethod(MethodSpec.methodBuilder("getTypeName")
                .addAnnotation(Override.class)
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("return this.$N", "typeName")
                .build());
        builder.addMethod(MethodSpec.methodBuilder("getRecordClass")
                .addAnnotation(Override.class)
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
                .returns(Class.class)
                .addStatement("return this.$N", "recordClass")
                .build());
        builder.addMethod(MethodSpec.methodBuilder("getSearchRecordType")
                .addAnnotation(Override.class)
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("return this.$N", "searchRecordType")
                .build());

        builder.addMethod(MethodSpec.methodBuilder("getByTypeName")
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC, javax.lang.model.element.Modifier.STATIC)
                .returns(recordTypeEnumClassName)
                .addParameter(String.class, "typeName")
                .addCode(CodeBlock.builder()
                        .beginControlFlow("for ($T value : values())", recordTypeEnumClassName)
                        .beginControlFlow("if (value.typeName.equals($N))", "typeName")
                        .addStatement("return value")
                        .endControlFlow()
                        .endControlFlow()
                        .addStatement("return null")
                        .build())
                .build());

        builder.addJavadoc("Generated by $T\n", getClass());

        TypeSpec typeSpec = builder.build();

        JavaFile jfile = JavaFile.builder(recordTypeEnumClassName.packageName(), typeSpec)
                .indent("    ").build();

        jfile.writeTo(outputFolder);
    }

    public void genSearchRecordTypeEnumClass() throws IOException {
        List<SearchRecordTypeSpec> specs = new ArrayList<>(searchRecordTypeMap.values());
        Collections.sort(specs, new Comparator<SearchRecordTypeSpec>() {
            @Override public int compare(SearchRecordTypeSpec o1, SearchRecordTypeSpec o2) {
                return o1.getEnumConstantName().compareTo(o2.getEnumConstantName());
            }
        });

        TypeSpec.Builder builder = TypeSpec.enumBuilder(searchRecordTypeEnumClassName.simpleName())
                .addSuperinterface(SearchRecordTypeDesc.class)
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
                .addField(String.class, "type",
                        javax.lang.model.element.Modifier.PRIVATE, javax.lang.model.element.Modifier.FINAL)
                .addField(String.class, "typeName",
                        javax.lang.model.element.Modifier.PRIVATE, javax.lang.model.element.Modifier.FINAL)
                .addField(Class.class, "searchClass",
                        javax.lang.model.element.Modifier.PRIVATE, javax.lang.model.element.Modifier.FINAL)
                .addField(Class.class, "searchBasicClass",
                        javax.lang.model.element.Modifier.PRIVATE, javax.lang.model.element.Modifier.FINAL)
                .addField(Class.class, "searchAdvancedClass",
                        javax.lang.model.element.Modifier.PRIVATE, javax.lang.model.element.Modifier.FINAL);

        for (SearchRecordTypeSpec spec : specs) {
            TypeSpec enumTypeSpec;
            if (spec.getSearchClass() != null && spec.getSearchAdvancedClass() != null) {
                enumTypeSpec = TypeSpec.anonymousClassBuilder("$S, $S, $T.class, $T.class, $T.class", spec.getName(),
                        spec.getTypeName(), spec.getSearchClass(), spec.getSearchBasicClass(), spec.getSearchAdvancedClass()).build();
            } else {
                enumTypeSpec = TypeSpec.anonymousClassBuilder("$S, $S, null, $T.class, null", spec.getName(), spec.getTypeName(),
                        spec.getSearchBasicClass()).build();
            }
            builder.addEnumConstant(spec.getEnumConstantName(), enumTypeSpec);
        }

        builder.addMethod(MethodSpec.constructorBuilder()
                .addParameter(String.class, "type")
                .addParameter(String.class, "typeName")
                .addParameter(Class.class, "searchClass")
                .addParameter(Class.class, "searchBasicClass")
                .addParameter(Class.class, "searchAdvancedClass")
                .addStatement("this.$N = $N", "type", "type")
                .addStatement("this.$N = $N", "typeName", "typeName")
                .addStatement("this.$N = $N", "searchClass", "searchClass")
                .addStatement("this.$N = $N", "searchBasicClass", "searchBasicClass")
                .addStatement("this.$N = $N", "searchAdvancedClass", "searchAdvancedClass")
                .build());

        builder.addMethod(MethodSpec.methodBuilder("getType")
                .addAnnotation(Override.class)
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("return this.type")
                .build());
        builder.addMethod(MethodSpec.methodBuilder("getTypeName")
                .addAnnotation(Override.class)
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
                .returns(String.class)
                .addStatement("return this.typeName")
                .build());
        builder.addMethod(MethodSpec.methodBuilder("getSearchClass")
                .addAnnotation(Override.class)
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
                .returns(Class.class)
                .addStatement("return this.searchClass")
                .build());
        builder.addMethod(MethodSpec.methodBuilder("getSearchBasicClass")
                .addAnnotation(Override.class)
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
                .returns(Class.class)
                .addStatement("return this.searchBasicClass")
                .build());
        builder.addMethod(MethodSpec.methodBuilder("getSearchAdvancedClass")
                .addAnnotation(Override.class)
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC)
                .returns(Class.class)
                .addStatement("return this.searchAdvancedClass")
                .build());

        builder.addMethod(MethodSpec.methodBuilder("getByTypeName")
                .addModifiers(javax.lang.model.element.Modifier.PUBLIC, javax.lang.model.element.Modifier.STATIC)
                .returns(searchRecordTypeEnumClassName)
                .addParameter(String.class, "typeName")
                .addCode(CodeBlock.builder()
                        .beginControlFlow("for ($T value : values())", searchRecordTypeEnumClassName)
                        .beginControlFlow("if (value.typeName.equals($N))", "typeName")
                        .addStatement("return value")
                        .endControlFlow()
                        .endControlFlow()
                        .addStatement("return null")
                        .build())
                .build());

        builder.addJavadoc("Generated by $T\n", getClass());

        TypeSpec typeSpec = builder.build();

        JavaFile jfile = JavaFile.builder(searchRecordTypeEnumClassName.packageName(), typeSpec)
                .indent("    ").build();

        jfile.writeTo(outputFolder);

    }

    protected static class RecordTypeSpec {
        protected String name;
        protected String typeName;
        protected Enum<?> enumValue;
        protected String enumConstantName;
        protected Class<?> recordClass;
        protected String searchRecordTypeName;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public Enum<?> getEnumValue() {
            return enumValue;
        }

        public void setEnumValue(Enum<?> enumValue) {
            this.enumValue = enumValue;
        }

        public String getEnumConstantName() {
            return enumConstantName;
        }

        public void setEnumConstantName(String enumConstantName) {
            this.enumConstantName = enumConstantName;
        }

        public Class<?> getRecordClass() {
            return recordClass;
        }

        public void setRecordClass(Class<?> recordClass) {
            this.recordClass = recordClass;
        }

        public String getSearchRecordTypeName() {
            return searchRecordTypeName;
        }

        public void setSearchRecordTypeName(String searchRecordTypeName) {
            this.searchRecordTypeName = searchRecordTypeName;
        }
    }

    protected static class SearchRecordTypeSpec {
        protected String name;
        protected String typeName;
        protected Enum<?> enumValue;
        protected String enumConstantName;
        protected Class<?> searchClass;
        protected Class<?> searchBasicClass;
        protected Class<?> searchAdvancedClass;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public Enum<?> getEnumValue() {
            return enumValue;
        }

        public void setEnumValue(Enum<?> enumValue) {
            this.enumValue = enumValue;
        }

        public String getEnumConstantName() {
            return enumConstantName;
        }

        public void setEnumConstantName(String enumConstantName) {
            this.enumConstantName = enumConstantName;
        }

        public Class<?> getSearchClass() {
            return searchClass;
        }

        public void setSearchClass(Class<?> searchClass) {
            this.searchClass = searchClass;
        }

        public Class<?> getSearchBasicClass() {
            return searchBasicClass;
        }

        public void setSearchBasicClass(Class<?> searchBasicClass) {
            this.searchBasicClass = searchBasicClass;
        }

        public Class<?> getSearchAdvancedClass() {
            return searchAdvancedClass;
        }

        public void setSearchAdvancedClass(Class<?> searchAdvancedClass) {
            this.searchAdvancedClass = searchAdvancedClass;
        }
    }

    public void run(String...args) throws Exception {
        genRecordTypeMetaDataModel();
        genSearchRecordTypeMetaDataModel();

        genRecordTypeEnumClass();
        genSearchRecordTypeEnumClass();
    }

}
