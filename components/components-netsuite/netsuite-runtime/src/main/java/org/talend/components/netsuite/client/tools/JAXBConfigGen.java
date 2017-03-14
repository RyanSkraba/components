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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

/**
 *
 */
public class JAXBConfigGen {

    public static void main(String... args) throws Exception {
        JAXBConfigGen tool = new JAXBConfigGen();
        // "file:///D:/ws/talend/dev/components/components/components-netsuite/netsuite-runtime_2014_2/src/main/resources/wsdl/2014.2/netsuite.wsdl", "_2016_2", "v2016_2"
        tool.run(args);
    }

    public void run(String... args) throws Exception {
        String wsdlUrl = args[0];
        String apiVersionPackageSuffix = args[1];
        String apiVersionPackage = args[2];

        WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();

        Definition def = wsdlReader.readWSDL(wsdlUrl);

        Collection<String> namespaceList = new HashSet<>();
        processSchemas(def, namespaceList);

        System.out.println("Found namespaces: " + namespaceList.size());
        for (String namespaceUri : namespaceList) {
            System.out.println(namespaceUri);
        }

        List<Map<String, String>> entries = new ArrayList<>();
        for (String namespaceUri : namespaceList) {
            String[] parts1 = namespaceUri.split("\\:"); // split "urn:<ns>"
            String namespace = parts1[1].replace(apiVersionPackageSuffix, "");
            String[] parts2 = namespace.split("\\.");
            List<String> nameParts = Arrays.asList(parts2);
            Collections.reverse(nameParts);

            StringBuilder sb = new StringBuilder();
            for (String namePart : nameParts) {
                if (sb.length() != 0) {
                    sb.append(".");
                }
                sb.append(namePart);
            }

            String packageName = sb.toString().replace(
                    "com.netsuite.webservices.", "com.netsuite.webservices." + apiVersionPackage + ".");

            Map<String, String> entry = new HashMap<>();
            entry.put("uri", namespaceUri);
            entry.put("package", packageName);
            entries.add(entry);
        }

        System.out.println();
        System.out.println("JAXB binding customization samples: ");
        System.out.println();

        String template =
                "<bindings scd=\"x-schema::tns\" xmlns:tns=\"${namespace}\">\n" +
                "    <schemaBindings>\n" +
                "        <package name=\"${package}\"/>\n" +
                "    </schemaBindings>\n" +
                "</bindings>";

        for (Map<String, String> nsEntry : entries) {
            String namespaceUri = nsEntry.get("uri");
            String packageName = nsEntry.get("package");
            String result = template
                    .replace("${namespace}", namespaceUri)
                    .replace("${package}", packageName);
            System.out.println(result);
        }

        System.out.println();
        System.out.println("Done.");
    }

    private void processSchemas(Definition def, Collection<String> namespaceList) throws Exception {
//        namespaceList.add(def.getTargetNamespace());

        if (def.getTypes() != null && def.getTypes().getExtensibilityElements() != null) {
            for (Iterator iter = def.getTypes().getExtensibilityElements().iterator(); iter.hasNext();) {
                ExtensibilityElement element = (ExtensibilityElement) iter.next();
                if (element instanceof javax.wsdl.extensions.schema.Schema) {
                    javax.wsdl.extensions.schema.Schema schema = (javax.wsdl.extensions.schema.Schema) element;
                    processSchema(schema, namespaceList);
                }
            }
        }

        if (def.getImports() != null) {
            for (Iterator itImp = def.getImports().values().iterator(); itImp.hasNext();) {
                Collection imps = (Collection) itImp.next();
                for (Iterator iter = imps.iterator(); iter.hasNext();) {
                    Import imp = (Import) iter.next();
                    processSchemas(imp.getDefinition(), namespaceList);
                }
            }
        }

    }

    private void processSchema(javax.wsdl.extensions.schema.Schema schema, Collection<String> namespaceList) throws Exception {
        for (Iterator itImp = schema.getImports().values().iterator(); itImp.hasNext();) {
            Collection imps = (Collection) itImp.next();
            for (Iterator itSi = imps.iterator(); itSi.hasNext();) {
                SchemaImport imp = (SchemaImport) itSi.next();
                String namespaceUri = imp.getNamespaceURI();
                namespaceList.add(namespaceUri);

                processSchema(imp.getReferencedSchema(), namespaceList);
            }
        }
    }
}
