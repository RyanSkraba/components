package org.talend.components.api.service.testcomponent;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;

/**
 * Component properties test class
 */
@Service
public class PropertiesTester {

    ComponentProperties testProps;

    ComponentDefinition testDef;

    @Inject
    public ComponentService componentService;

    public void processCommand(String argString) {
        String[] args = new String(argString).split(" ");
        if (args.length == 0) {
            throw new IllegalArgumentException("Empty command");
        }
        int index = 0;
        String command = args[index++];
        if (command.equalsIgnoreCase("createProps")) {
            String comp = args[index++];
            testDef = componentService.getComponentDefinition(comp);
            testProps = componentService.getComponentProperties(comp);
        } else if (command.equalsIgnoreCase("showProps")) {
            System.out.println("Properties: " + testProps);
        }
    }

    public void processCommands(String lines) {
        String[] lineArray = lines.split("\n");
        for (String line : lineArray) {
            processCommand(line);
        }
    }

    public static void main(String[] args) {
        PropertiesTester pt = new PropertiesTester();
        pt.processCommands("createProps tSalesforceConnect\nshowProps\n");
    }

}
