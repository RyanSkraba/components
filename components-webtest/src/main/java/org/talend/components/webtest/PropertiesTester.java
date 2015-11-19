package org.talend.components.webtest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

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

    ComponentProperties testProps;

    ComponentDefinition testDef;

    private static PropertiesTester instance;

    @Inject
    public ComponentService componentService;

    private static final int CMD_CREATEPROPS = 1;

    private static final int CMD_SHOWPROP = 2;

    private static final int CMD_SHOWCOMPS = 3;

    private static final int CMD_SHOWCOMPNAMES = 4;

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

    public PropertiesTester() {
        instance = this;

        commands = new Command[] { //
                new Command(CMD_CREATEPROPS, new String[] { "cp", "createProps" }, "Create properties", //
                        new Runnable() {

                            @Override
                            public void run() {
                                String comp = args[argIndex++];
                                testDef = componentService.getComponentDefinition(comp);
                                testProps = componentService.getComponentProperties(comp);

                            }
                        }), //
                new Command(CMD_SHOWPROP, new String[] { "sp", "showProps" }, "Show previously created properties", //
                        new Runnable() {

                            @Override
                            public void run() {
                                System.out.println("Properties: " + testProps);
                            }
                        }), //
                new Command(CMD_SHOWCOMPS, new String[] { "sc", "showComps" }, "Show all component definitions", //
                        new Runnable() {

                            @Override
                            public void run() {
                                System.out.println(arrayLines(componentService.getAllComponents()));
                            }
                        }), //
                new Command(CMD_SHOWCOMPNAMES, new String[] { "scn", "showCompNames" }, "Show all component names", //
                        new Runnable() {

                            @Override
                            public void run() {
                                System.out.println(componentService.getAllComponentNames());
                            }
                        }), //
        };

    }

    private class Command {

        int number;

        String[] names;

        String help;

        Runnable run;

        boolean executeIfMatch() {
            for (String name : names) {
                if (currentCommand.equalsIgnoreCase(name)) {
                    try {
                        run.run();
                    } catch (Exception ex) {
                        System.out.println("Error: " + ex);
                    }
                    return true;
                }
            }
            return false;
        }

        Command(int number, String[] names, String help, Runnable run) {
            this.number = number;
            this.names = names;
            this.help = help;
            this.run = run;
        }
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

    public void processCommands(String lines) {
        String[] lineArray = lines.split("\n");
        for (String line : lineArray) {
            try {
                processCommand(line);
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
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

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String input;

            while ((input = br.readLine()) != null) {
                pt.processCommand(input);
            }

        } catch (IOException io) {
            io.printStackTrace();
        }
    }

}
