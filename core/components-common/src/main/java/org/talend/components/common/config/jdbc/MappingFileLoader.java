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
package org.talend.components.common.config.jdbc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * JDBC configuration parser (mapping_*.xml)
 */
public class MappingFileLoader {

    /**
     * Parses configuration mapping files and returns a list of {@link Dbms}
     * 
     * @param path path to configuration file
     * @return list of {@link Dbms}
     */
    public List<Dbms> load(String path) {
        return load(new File(path));
    }

    /**
     * Parses configuration mapping files and returns a list of {@link Dbms}
     * 
     * @param file configuration file
     * @return list of {@link Dbms}
     */
    public List<Dbms> load(File file) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder analyser = documentBuilderFactory.newDocumentBuilder();
            Document document = analyser.parse(file);
            NodeList dbmsNodes = document.getElementsByTagName("dbms");
            return constructAllDbms(dbmsNodes);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Constructs all DBMS from DOM
     * 
     * @param dbmsNodes
     * @return
     */
    private List<Dbms> constructAllDbms(NodeList dbmsNodes) {
        ArrayList<Dbms> dbmsList = new ArrayList<>();
        for (int i = 0; i < dbmsNodes.getLength(); i++) {
            Dbms dbms = constructDbms((Element) dbmsNodes.item(i));
            dbmsList.add(dbms);
        }
        return dbmsList;
    }

    /**
     * Construct single DBMS from DOM Element
     * 
     * @param dbmsNode
     * @return
     */
    private Dbms constructDbms(Element dbmsNode) {
        String productName = dbmsNode.getAttribute("product");
        String id = dbmsNode.getAttribute("id");
        String label = dbmsNode.getAttribute("label");
        boolean isDefault = Boolean.parseBoolean(dbmsNode.getAttribute("default"));
        Dbms dbms = new Dbms(id, productName, label, isDefault);

        // parse db types
        Element dbTypesNode = getChildElement(dbmsNode, "dbTypes");
        NodeList dbTypes = dbTypesNode.getElementsByTagName("dbType");
        for (int i = 0; i < dbTypes.getLength(); i++) {
            constructDbmsType((Element) dbTypes.item(i), dbms);
        }

        // parse type mappings
        Element javaLanguageNode = findJavaLanguage(dbmsNode.getElementsByTagName("language"));
        // process talend to db mappings
        Element talendToDbmsTypes = getChildElement(javaLanguageNode, "talendToDbTypes");
        List<Element> talendMappings = getChildren(talendToDbmsTypes);
        for (Element el : talendMappings) {
            constructTalendMapping(el, dbms);
        }

        Element dbTypesToTalend = getChildElement(javaLanguageNode, "dbToTalendTypes");
        List<Element> dbMappings = getChildren(dbTypesToTalend);
        for (Element el : dbMappings) {
            constructDbMapping(el, dbms);
        }

        return dbms;
    }

    private void constructDbMapping(Element dbMapping, Dbms dbms) {
        String dbTypeName = dbMapping.getAttribute("type");
        DbmsType dbType = dbms.getDbmsType(dbTypeName);

        List<Element> correspondingTalendTypes = getChildren(dbMapping);
        Set<TalendType> targetTypes = new HashSet<>();
        TalendType defaultType = null;
        for (Element talendTypeNode : correspondingTalendTypes) {
            String talendTypeName = talendTypeNode.getAttribute("type");
            targetTypes.add(TalendType.get(talendTypeName));
            String defaultAttrValue = talendTypeNode.getAttribute("default");
            boolean isDefault = defaultAttrValue.isEmpty() ? false : Boolean.parseBoolean(defaultAttrValue);
            if (isDefault) {
                defaultType = TalendType.get(talendTypeName);
            }
        }
        MappingType<DbmsType, TalendType> typeMapping = new MappingType<>(dbType, defaultType, targetTypes);
        dbms.addDbMapping(dbTypeName, typeMapping);
    }

    private Element findJavaLanguage(NodeList languagesNodeList) {
        for (int i = 0; i < languagesNodeList.getLength(); i++) {
            Element languageNode = (Element) languagesNodeList.item(0);
            if (languageNode.getAttribute("name").equals("java")) {
                return languageNode;
            }
        }
        return null; // mapping file has no mapping for java
    }

    /**
     * Constructs db type from DOM object
     * 
     * @param dbTypeNode
     */
    private void constructDbmsType(Element dbTypeNode, Dbms dbms) {
        String typeName = dbTypeNode.getAttribute("type");

        boolean isDefault = false;
        String isDefaultAttribute = dbTypeNode.getAttribute("default");
        if (!isDefaultAttribute.isEmpty()) {
            isDefault = Boolean.parseBoolean(isDefaultAttribute);
        }

        int defaultLength = DbmsType.UNDEFINED;
        String defaultLengthAttribute = dbTypeNode.getAttribute("defaultLength");
        if (!defaultLengthAttribute.isEmpty()) {
            defaultLength = Integer.parseInt(defaultLengthAttribute);
        }

        int defaultPrecision = DbmsType.UNDEFINED;
        String defaultPrecisionAttribute = dbTypeNode.getAttribute("defaultPrecision");
        if (!defaultPrecisionAttribute.isEmpty()) {
            defaultPrecision = Integer.parseInt(defaultPrecisionAttribute);
        }

        boolean ignoreLen = false;
        String ignoreLenAttribute = dbTypeNode.getAttribute("ignoreLen");
        if (!ignoreLenAttribute.isEmpty()) {
            ignoreLen = Boolean.parseBoolean(ignoreLenAttribute);
        }

        boolean ignorePre = false;
        String ignorePreAttribute = dbTypeNode.getAttribute("ignorePre");
        if (!ignorePreAttribute.isEmpty()) {
            ignorePre = Boolean.parseBoolean(ignorePreAttribute);
        }

        boolean preBeforeLen = false;
        String preBeforeLenAttribute = dbTypeNode.getAttribute("preBeforeLen");
        if (!preBeforeLenAttribute.isEmpty()) {
            preBeforeLen = Boolean.parseBoolean(preBeforeLenAttribute);
        }
        DbmsType dbType = new DbmsType(typeName, isDefault, defaultLength, defaultPrecision, ignoreLen, ignorePre, preBeforeLen);
        dbms.addType(typeName, dbType);
    }

    private void constructTalendMapping(Element talendMapping, Dbms dbms) {
        String talendTypeName = talendMapping.getAttribute("type");
        TalendType talendType = TalendType.get(talendTypeName);

        List<Element> correspondingDbmsTypes = getChildren(talendMapping);
        Set<DbmsType> targetTypes = new HashSet<>();
        DbmsType defaultType = null;
        for (Element dbTypeNode : correspondingDbmsTypes) {
            String dbType = dbTypeNode.getAttribute("type");
            targetTypes.add(dbms.getDbmsType(dbType));
            String defaultAttrValue = dbTypeNode.getAttribute("default");
            boolean isDefault = defaultAttrValue.isEmpty() ? false : Boolean.parseBoolean(defaultAttrValue);
            if (isDefault) {
                defaultType = dbms.getDbmsType(dbType);
            }
        }
        MappingType<TalendType, DbmsType> typeMapping = new MappingType<>(talendType, defaultType, targetTypes);
        dbms.addTalendMapping(talendTypeName, typeMapping);

    }

    private Element getChildElement(Element parent, String tagName) {
        return (Element) parent.getElementsByTagName(tagName).item(0);
    }

    /**
     * Get children of type ELEMENT_NODE from parent <code>parentNode</code>.
     * 
     * @param parentNode
     * @return
     */
    private List<Element> getChildren(Node parentNode) {
        Node childNode = parentNode.getFirstChild();
        ArrayList<Element> list = new ArrayList<>();
        while (childNode != null) {
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                list.add((Element) childNode);
            }
            childNode = childNode.getNextSibling();
        }
        return list;
    }
}
