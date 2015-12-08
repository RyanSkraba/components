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
package org.talend.components.proptester;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Service;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.properties.Property;
import org.talend.components.api.schema.SchemaElement;
import org.talend.components.api.service.ComponentService;
import org.talend.daikon.spring.BndToSpringBeanNameGenerator;

import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.StringsCompleter;

/**
 * Component properties test class
 */
@ComponentScan(basePackages = "org.talend.components", nameGenerator = BndToSpringBeanNameGenerator.class, includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = aQute.bnd.annotation.component.Component.class) , excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Osgi") )
@Service
public class PropertiesTester {

    private class Command {

        String[] names;

        String help;

        void run() {
        }

        void setupCompleter() {
        }

        boolean executeIfMatch() {
            for (String name : names) {
                if (currentCommand.equalsIgnoreCase(name)) {
                    try {
                        run();
                    } catch (Exception ex) {
                        System.out.println("Error: " + ex);
                    }
                    return true;
                }
            }
            return false;
        }

        List<String> getNameList() {
            List<String> l = new ArrayList();
            for (String s : names) {
                l.add(s);
            }
            return l;
        }

        String helpCommand() {
            StringBuilder sb = new StringBuilder();
            sb.append(names[0]);
            if (names.length == 2) {
                sb.append(" (" + names[1] + ")");
            }
            return sb.toString();
        }

        Command(String[] names, String help) {
            this.names = names;
            this.help = help;
        }
    }

    private static PropertiesTester instance;

    @Inject
    public ComponentService componentService;

    ComponentProperties testProps;

    ComponentDefinition testDef;

    ConsoleReader console;

    private String arrayLines(Collection c) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object item : c) {
            if (!firstTime) {
                sb.append("\n");
            }
            sb.append(item.toString());
            firstTime = false;
        }
        return sb.toString();
    }

    private Property resolveProperty() {
        if (argIndex >= args.length) {
            System.out.println("Please specify the property name (which can be qualified)");
            throw new IllegalArgumentException();
        }
        String prop = args[argIndex++];
        SchemaElement se = testProps.getProperty(prop);
        if (se == null) {
            System.out.println("Property: " + prop + " not found");
            throw new IllegalArgumentException();
        }
        if (!(se instanceof Property)) {
            System.out.println("Property: " + prop + " must be a leaf property");
            throw new IllegalArgumentException();
        }
        Property p = (Property) se;
        return p;
    }

    public PropertiesTester() throws IOException {
        instance = this;

        commands = new Command[] { //
                new Command(new String[] { "createProps", "cp" }, "Create properties, specify component name") {

                    @Override
                    void run() {
                        if (argIndex >= args.length) {
                            System.out.println("Specify the component name");
                            return;
                        }
                        String comp = args[argIndex++];
                        testDef = componentService.getComponentDefinition(comp);
                        testProps = componentService.getComponentProperties(comp);
                    }

                    @Override
                    void setupCompleter() {
                        StringsCompleter sc = new StringsCompleter(getNameList());
                        console.addCompleter(
                                new ArgumentCompleter(sc, new StringsCompleter(componentService.getAllComponentNames())));
                    }
                }, //
                new Command(new String[] { "showProps", "sp" }, "Show previously created properties") {

                    @Override
                    void run() {
                        System.out.println(testProps);
                    }
                }, //
                new Command(new String[] { "showComps", "sc" }, "Show all component definitions") {

                    @Override
                    void run() {
                        System.out.println(arrayLines(componentService.getAllComponents()));
                    }
                }, //
                new Command(new String[] { "showCompNames", "scn" }, "Show all component names") {

                    @Override
                    void run() {
                        System.out.println(arrayLines(componentService.getAllComponentNames()));
                    }
                }, //
                new Command(new String[] { "setValue", "sv" }, "Sets the value of the specified property") {

                    @Override
                    void run() {
                        Property p = resolveProperty();
                        if (argIndex >= args.length) {
                            System.out.println("Please specify the value as the second argument");
                            return;
                        }
                        String value = args[argIndex++];
                        p.setValue(value);
                    }
                }, //
                new Command(new String[] { "beforePresent", "bp" },
                        "Call the beforePresent service with the specified property") {

                    @Override
                    void run() {
                        Property p = resolveProperty();
                        try {
                            ComponentProperties props = componentService.beforePropertyPresent(p.getName(), testProps);
                            testProps = props;
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                }, //
                new Command(new String[] { "beforeActivate", "ba" },
                        "Call the beforeActivate service with the specified property") {

                    @Override
                    void run() {
                        Property p = resolveProperty();
                        try {
                            ComponentProperties props = componentService.beforePropertyActivate(p.getName(), testProps);
                            testProps = props;
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                }, //
                new Command(new String[] { "after", "a" }, "Call the afterProperty service with the specified property") {

                    @Override
                    void run() {
                        Property p = resolveProperty();
                        try {
                            ComponentProperties props = componentService.afterProperty(p.getName(), testProps);
                            testProps = props;
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                }, //
                new Command(new String[] { "validate", "v" }, "Call the validateProperty service with the specified property") {

                    @Override
                    void run() {
                        Property p = resolveProperty();
                        try {
                            ComponentProperties props = componentService.validateProperty(p.getName(), testProps);
                            System.out.println(props.getValidationResult());
                            testProps = props;
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                }, //
                new Command(new String[] { "help", "h" }, "Show this help") {

                    @Override
                    void run() {
                        for (Command c : commands) {
                            System.out.printf("%-20s %s\n", c.helpCommand(), c.help);
                        }
                    }
                }, //
                new Command(new String[] { "exit" }, "Exit") {

                    @Override
                    void run() {
                        System.exit(0);
                    }
                }, //
        };
    }

    private Command[] commands;

    private String currentCommand;

    private String args[];

    private int argIndex;

    public void processCommand(String argString) {
        if (argString.isEmpty()) {
            return;
        }
        if (argString.startsWith("#")) {
            return;
        }

        args = new String(argString).split(" ");
        if (args.length == 0) {
            return;
        }
        argIndex = 0;
        currentCommand = args[argIndex++];
        for (Command cmd : commands) {
            if (cmd.executeIfMatch()) {
                return;
            }
        }
        System.out.println("Unknown command: " + currentCommand);
    }

    public void processCommandLines(String commandLines) {
        String lines[] = commandLines.split("\n");
        for (String line : lines) {
            System.out.println(line);
            processCommand(line);
        }
    }

    public void readCommands() {
        try {
            console = new ConsoleReader();
            List commandNames = new ArrayList();
            for (Command c : commands) {
                c.setupCompleter();
                commandNames.add(c.names[0]);
            }
            console.addCompleter(new StringsCompleter(commandNames));
            console.setPrompt("proptester> ");
            String line;
            while ((line = console.readLine()) != null) {
                processCommand(line);
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PropertiesTester.class);
        app.setWebEnvironment(false);
        app.setShowBanner(false);
        app.setHeadless(true);
        app.setLogStartupInfo(false);
        app.run(args);
        PropertiesTester pt = instance;
        pt.readCommands();
    }

}
