package org.talend.components.webtest;

import java.io.IOException;
import java.util.*;

import javax.inject.Inject;

import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.StringsCompleter;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;

/**
 * Component properties test class
 */
@ComponentScan(basePackages = "org.talend.components", nameGenerator = PropertiesTester.BndToSpringBeanNameGenerator.class, includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = aQute.bnd.annotation.component.Component.class) , excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Osgi") )
@Service
public class PropertiesTester {

    // FIXME - temp
    static final public String BND_ANNOTATION = "aQute.bnd.annotation.component.Component";

    static public class BndToSpringBeanNameGenerator extends AnnotationBeanNameGenerator {

        /**
         * Derive a bean name from one of the annotations on the class.
         *
         * @param annotatedDef the annotation-aware bean definition
         * @return the bean name, or {@code null} if none is found
         */
        @Override
        protected String determineBeanNameFromAnnotation(AnnotatedBeanDefinition annotatedDef) {
            String beanName = super.determineBeanNameFromAnnotation(annotatedDef);
            if (beanName != null) {
                return beanName;
            } // else check for BND annotation
            AnnotationMetadata amd = annotatedDef.getMetadata();
            Set<String> types = amd.getAnnotationTypes();
            for (String type : types) {
                AnnotationAttributes attributes = AnnotationAttributes.fromMap(amd.getAnnotationAttributes(type, false));
                if (isStereotypeWithBndNameValue(type, amd.getMetaAnnotationTypes(type), attributes)) {
                    Object value = attributes.get("name");
                    if (value instanceof String) {
                        String strVal = (String) value;
                        if (StringUtils.hasLength(strVal)) {
                            if (beanName != null && !strVal.equals(beanName)) {
                                throw new IllegalStateException("Stereotype annotations suggest inconsistent "
                                        + "component names: '" + beanName + "' versus '" + strVal + "'");
                            }
                            beanName = strVal;
                        }
                    }
                }
            }
            return beanName;
        }

        /**
         * Check whether the given annotation is a stereotype that is allowed to suggest a component name through its
         * annotation {@code value()}.
         *
         * @param annotationType the name of the annotation class to check
         * @param metaAnnotationTypes the names of meta-annotations on the given annotation
         * @param attributes the map of attributes for the given annotation
         * @return whether the annotation qualifies as a stereotype with component name
         */
        protected boolean isStereotypeWithBndNameValue(String annotationType, Set<String> metaAnnotationTypes,
                Map<String, Object> attributes) {

            boolean isStereotype = annotationType.equals(BND_ANNOTATION)
                    || (metaAnnotationTypes != null && metaAnnotationTypes.contains(BND_ANNOTATION));

            return (isStereotype && attributes != null && attributes.containsKey("name"));
        }

    }

    private class Command {

        String[] names;

        String help;

        void run() {

        }

        void setupCompletor() {

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
            for (String s : names)
                l.add(s);
            return l;
        }

        String helpCommand() {
            StringBuilder sb = new StringBuilder();
            sb.append(names[0]);
            if (names.length == 2)
                sb.append(" (" + names[1] + ")");
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
            if (!firstTime)
                sb.append("\n");
            sb.append(item.toString());
            firstTime = false;
        }
        return sb.toString();
    }

    public PropertiesTester() throws IOException {
        instance = this;

        commands = new Command[] { //
                new Command(new String[] { "createProps", "cp" }, "Create properties, specify component name") {

                    void run() {
                        if (argIndex >= args.length) {
                            System.out.println("Specify the component name");
                            return;
                        }
                        String comp = args[argIndex++];
                        testDef = componentService.getComponentDefinition(comp);
                        testProps = componentService.getComponentProperties(comp);
                    }

                    void setupCompletor() {
                        StringsCompleter sc = new StringsCompleter(getNameList());
                        console.addCompleter(
                                new ArgumentCompleter(sc, new StringsCompleter(componentService.getAllComponentNames())));
                    }
                }, //
                new Command(new String[] { "showProps", "sp" }, "Show previously created properties") {

                    void run() {
                        System.out.println(testProps);
                    }
                }, //
                new Command(new String[] { "showComps", "sc" }, "Show all component definitions") {

                    void run() {
                        System.out.println(arrayLines(componentService.getAllComponents()));
                    }
                }, //
                new Command(new String[] { "showCompNames", "scn" }, "Show all component names") {

                    void run() {
                        System.out.println(arrayLines(componentService.getAllComponentNames()));
                    }
                }, //
                new Command(new String[] { "setValue", "sv" }, "Sets the value of the specified property") {

                    void run() {
                        if (argIndex >= args.length) {
                            System.out.println("Specify the property name (which can be qualified)");
                            return;
                        }
                        String prop = args[argIndex++];
                        if (argIndex >= args.length) {
                            System.out.println("Specify the value");
                            return;
                        }
                        String value = args[argIndex++];

                        testProps.setValue(testProps.getProperty(prop), value);
                    }
                }, //
                new Command(new String[] { "beforePresent", "bp" },
                        "Call the beforePresent service with the specified property") {

                    void run() {
                        if (argIndex >= args.length) {
                            System.out.println("Specify the property name (which can be qualified)");
                            return;
                        }
                        String prop = args[argIndex++];

                        try {
                            ComponentProperties props = componentService.beforePropertyPresent(prop, testProps);
                            testProps = props;
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                }, //
                new Command(new String[] { "beforeActivate", "ba" },
                        "Call the beforeActivate service with the specified property") {

                    void run() {
                        if (argIndex >= args.length) {
                            System.out.println("Specify the property name (which can be qualified)");
                            return;
                        }
                        String prop = args[argIndex++];

                        try {
                            ComponentProperties props = componentService.beforePropertyActivate(prop, testProps);
                            testProps = props;
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                }, //
                new Command(new String[] { "after", "a" }, "Call the afterProperty service with the specified property") {

                    void run() {
                        if (argIndex >= args.length) {
                            System.out.println("Specify the property name (which can be qualified)");
                            return;
                        }
                        String prop = args[argIndex++];

                        try {
                            ComponentProperties props = componentService.afterProperty(prop, testProps);
                            testProps = props;
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                }, //
                new Command(new String[] { "validate", "v" }, "Call the validateProperty service with the specified property") {

                    void run() {
                        if (argIndex >= args.length) {
                            System.out.println("Specify the property name (which can be qualified)");
                            return;
                        }
                        String prop = args[argIndex++];

                        try {
                            ComponentProperties props = componentService.validateProperty(prop, testProps);
                            System.out.println(props.getValidationResult());
                            testProps = props;
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                }, //
                new Command(new String[] { "help", "h" }, "Show this help") {

                    void run() {
                        for (Command c : commands) {
                            System.out.printf("%-20s %s\n", c.helpCommand(), c.help);
                        }
                    }
                }, //
                new Command(new String[] { "exit" }, "Exit") {

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
        if (argString.isEmpty())
            return;
        if (argString.startsWith("#"))
            return;

        args = new String(argString).split(" ");
        if (args.length == 0) {
            return;
        }
        argIndex = 0;
        currentCommand = args[argIndex++];
        for (Command cmd : commands) {
            if (cmd.executeIfMatch())
                return;
        }
        System.out.println("Unknown command: " + currentCommand);
    }

    public void processCommands() {
        try {
            console = new ConsoleReader();
            List commandNames = new ArrayList();
            for (Command c : commands) {
                c.setupCompletor();
                commandNames.add(c.names[0]);
            }
            console.addCompleter(new StringsCompleter(commandNames));
            console.setPrompt("comptest> ");
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
        if (true)
            app.run(PropertiesTester.class, args);
        else
            app.run(args);
        PropertiesTester pt = instance;
        pt.processCommands();
    }

}
