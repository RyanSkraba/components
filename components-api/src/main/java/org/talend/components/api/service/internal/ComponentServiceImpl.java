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
package org.talend.components.api.service.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectModelResolver;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.internal.impl.DefaultRemoteRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.graph.selector.ExclusionDependencySelector;
import org.eclipse.aether.util.graph.selector.OptionalDependencySelector;
import org.eclipse.aether.util.graph.selector.ScopeDependencySelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.components.api.Constants;
import org.talend.components.api.TopLevelDefinition;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.component.ComponentImageType;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.exception.error.ComponentsErrorCode;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.components.api.wizard.ComponentWizard;
import org.talend.components.api.wizard.ComponentWizardDefinition;
import org.talend.components.api.wizard.WizardImageType;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.properties.service.PropertiesServiceImpl;

/**
 * Main Component Service implementation that is not related to any framework (neither OSGI, nor Spring) it uses a
 * ComponentRegistry implementation that will be provided by framework specific Service classes
 */
public class ComponentServiceImpl extends PropertiesServiceImpl<ComponentProperties>implements ComponentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentServiceImpl.class);

    private Map<Artifact, Set<Dependency>> dependenciesCache = new HashMap<>();

    private ComponentRegistry componentRegistry;

    private ModelBuilder modelBuilder;

    public ComponentServiceImpl(ComponentRegistry componentRegistry) {
        this.componentRegistry = componentRegistry;
    }

    @Override
    public Set<String> getAllComponentNames() {
        // remove the components# internal prefix to return the simple name
        Collection<String> componentsInternalNames = componentRegistry.getComponents().keySet();
        Set<String> compNames = new HashSet<>(componentsInternalNames.size());
        for (String name : componentsInternalNames) {
            compNames.add(name.substring(Constants.COMPONENT_BEAN_PREFIX.length()));
        }
        return compNames;
    }

    @Override
    public Set<ComponentDefinition> getAllComponents() {
        return new HashSet<>(componentRegistry.getComponents().values());
    }

    @Override
    public Set<ComponentWizardDefinition> getTopLevelComponentWizards() {
        Set<ComponentWizardDefinition> defs = new HashSet<>();
        for (ComponentWizardDefinition def : componentRegistry.getComponentWizards().values()) {
            if (def.isTopLevel()) {
                defs.add(def);
            }
        }
        return defs;
    }

    @Override
    public ComponentProperties getComponentProperties(String name) {
        ComponentDefinition compDef = getComponentDefinition(name);
        ComponentProperties properties = compDef.createProperties();
        return properties;
    }

    @Override
    public ComponentDefinition getComponentDefinition(String name) {
        final String beanName = Constants.COMPONENT_BEAN_PREFIX + name;
        ComponentDefinition compDef = componentRegistry.getComponents().get(beanName);
        if (compDef == null) {
            throw new ComponentException(ComponentsErrorCode.WRONG_COMPONENT_NAME, ExceptionContext.build().put("name", name)); //$NON-NLS-1$
        } // else got the def so use it
        return compDef;
    }

    @Override
    public ComponentWizard getComponentWizard(String name, String location) {
        final String beanName = Constants.COMPONENT_WIZARD_BEAN_PREFIX + name;
        ComponentWizardDefinition wizardDefinition = componentRegistry.getComponentWizards().get(beanName);
        if (wizardDefinition == null) {
            throw new ComponentException(ComponentsErrorCode.WRONG_WIZARD_NAME, ExceptionContext.build().put("name", name)); //$NON-NLS-1$
        }
        ComponentWizard wizard = wizardDefinition.createWizard(location);
        return wizard;
    }

    @Override
    public List<ComponentWizard> getComponentWizardsForProperties(ComponentProperties properties, String location) {
        List<ComponentWizard> wizards = new ArrayList<>();
        for (ComponentWizardDefinition wizardDefinition : componentRegistry.getComponentWizards().values()) {
            if (wizardDefinition.supportsProperties(properties.getClass())) {
                ComponentWizard wizard = wizardDefinition.createWizard(properties, location);
                wizards.add(wizard);
            }
        }
        return wizards;
    }

    @Override
    public List<ComponentDefinition> getPossibleComponents(ComponentProperties properties) {
        List<ComponentDefinition> returnList = new ArrayList<>();
        for (ComponentDefinition cd : componentRegistry.getComponents().values()) {
            if (cd.supportsProperties(properties)) {
                returnList.add(cd);
            }
        }
        return returnList;
    }

    @Override
    public InputStream getWizardPngImage(String wizardName, WizardImageType imageType) {
        ComponentWizardDefinition wizardDefinition = componentRegistry.getComponentWizards()
                .get(Constants.COMPONENT_WIZARD_BEAN_PREFIX + wizardName);
        if (wizardDefinition != null) {
            return getImageStream(wizardDefinition, wizardDefinition.getPngImagePath(imageType));
        } else {
            throw new ComponentException(ComponentsErrorCode.WRONG_WIZARD_NAME, ExceptionContext.build().put("name", wizardName)); //$NON-NLS-1$
        }

    }

    @Override
    public InputStream getComponentPngImage(String componentName, ComponentImageType imageType) {
        ComponentDefinition componentDefinition = componentRegistry.getComponents()
                .get(Constants.COMPONENT_BEAN_PREFIX + componentName);
        if (componentDefinition != null) {
            return getImageStream(componentDefinition, componentDefinition.getPngImagePath(imageType));
        } else {
            throw new ComponentException(ComponentsErrorCode.WRONG_COMPONENT_NAME,
                    ExceptionContext.build().put("name", componentName)); //$NON-NLS-1$
        }
    }

    /**
     * get the image stream or null
     * 
     * @param definition, must not be null
     * @return the stream or null if no image was defined for th component or the path is wrong
     */
    private InputStream getImageStream(TopLevelDefinition definition, String pngIconPath) {
        InputStream result = null;
        if (pngIconPath != null && !"".equals(pngIconPath)) { //$NON-NLS-1$
            InputStream resourceAsStream = definition.getClass().getResourceAsStream(pngIconPath);
            if (resourceAsStream == null) {// no resource found so this is an component error, so log it and return
                                           // null
                LOGGER.error("Failed to load the Wizard icon [" + definition.getName() + "," + pngIconPath + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } else {
                result = resourceAsStream;
            }
        } else {// no path provided so will return null but log it.
            LOGGER.warn("The defintion of [" + definition.getName() + "] did not specify any icon"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return result;
    }

    @Override
    public Set<String> getMavenUriDependencies(String componentName) {
        ComponentDefinition componentDef = getComponentDefinition(componentName);
        InputStream mavenPomStream = componentDef.getMavenPom();
        try {
            return computeDependenciesFromPom(mavenPomStream, "test", "provided"); //$NON-NLS-1$
        } catch (IOException | XmlPullParserException | DependencyCollectionException
                | org.eclipse.aether.resolution.DependencyResolutionException | ModelBuildingException e) {
            throw new ComponentException(ComponentsErrorCode.COMPUTE_DEPENDENCIES_FAILED, e);
        }
    }

    /**
     * DOC sgandon Comment method "computeDependenciesFromPom".
     * 
     * @param mavenPomStream
     * @param componentDef will only be used to for its name in case of errors.
     * @return
     * @throws org.eclipse.aether.resolution.DependencyResolutionException
     * @throws DependencyCollectionException
     * @throws XmlPullParserException
     * @throws IOException
     * @throws ModelBuildingException
     * @throws Exception
     */
    Set<String> computeDependenciesFromPom(InputStream mavenPomStream, String... excludedScopes)
            throws DependencyCollectionException, org.eclipse.aether.resolution.DependencyResolutionException, IOException,
            XmlPullParserException, ModelBuildingException {
        MavenBooter booter = new MavenBooter();
        // FIXME we may not have to load the model and resolve it
        MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
        mavenXpp3Reader.setAddDefaultEntities(false);
        Model pomModel = mavenXpp3Reader.read(mavenPomStream);

        // Model pomModel = loadPom(mavenPomStream, booter, Collections.EMPTY_LIST);

        // List<org.apache.maven.model.Dependency> dependencies = pomModel.getDependencies();
        MavenProject mavenProject = new MavenProject(pomModel);
        Set<Dependency> dependencies = getArtifactsDependencies(mavenProject, booter, excludedScopes);
        Set<String> depsStrings = new HashSet<>(dependencies.size());
        // depsStrings.add("mvn:" + pomModel.getGroupId() + "/" + pomModel.getArtifactId() + "/" +
        // pomModel.getVersion());
        for (Dependency dep : dependencies) {
            depsStrings.add("mvn:" + dep.getArtifact().getGroupId() + "/" + dep.getArtifact().getArtifactId() + "/" //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                    + dep.getArtifact().getVersion() + "/"
                    + (dep.getArtifact().getExtension().equals("") ? "" : dep.getArtifact().getExtension())
                    + (dep.getArtifact().getClassifier().equals("") ? "" : ("/" + dep.getArtifact().getClassifier())));
        }
        return depsStrings;
    }

    public Set<Dependency> getArtifactsDependencies(MavenProject project, MavenBooter booter, String... excludedScopes)
            throws DependencyCollectionException, org.eclipse.aether.resolution.DependencyResolutionException {
        DefaultArtifact pomArtifact = new DefaultArtifact(project.getGroupId(), project.getArtifactId(), project.getPackaging(),
                null, project.getVersion());
        // check the cache if we already have computed the dependencies for this pom.
        if (dependenciesCache.containsKey(pomArtifact)) {
            return dependenciesCache.get(pomArtifact);
        }
        RepositorySystem repoSystem = booter.newRepositorySystem();
        DefaultRepositorySystemSession repoSession = booter.newRepositorySystemSession(repoSystem);
        DependencySelector depFilter = new AndDependencySelector(new ScopeDependencySelector(null, Arrays.asList(excludedScopes)),
                new OptionalDependencySelector(), new ExclusionDependencySelector());
        repoSession.setDependencySelector(depFilter);

        List<RemoteRepository> remoteRepos = booter.getRemoteRepositoriesWithAuthentification(repoSystem, repoSession);

        CollectRequest collectRequest = new CollectRequest(new Dependency(pomArtifact, "runtime"), remoteRepos);
        // collectRequest.setRequestContext(scope);
        CollectResult collectResult = repoSystem.collectDependencies(repoSession, collectRequest);
        DependencyNode root = collectResult.getRoot();
        Set<Dependency> ret = new HashSet<>();
        ret.add(root.getDependency());
        flattenDeps(root, ret);
        dependenciesCache.put(pomArtifact, ret);
        return ret;
    }

    /**
     * DOC sgandon Comment method "flattenDeps".
     * 
     * @param node
     * @param ret
     */
    private static void flattenDeps(DependencyNode node, Set<Dependency> ret) {
        List<DependencyNode> children = node.getChildren();
        for (DependencyNode dn : children) {
            Dependency dep = dn.getDependency();
            ret.add(dep);
            if (!dn.getChildren().isEmpty()) {
                flattenDeps(dn, ret);
            }
        }
    }

    Model loadPom(final InputStream pomStream, MavenBooter booter, List<String> profilesList) throws ModelBuildingException {

        RepositorySystem system = booter.newRepositorySystem();
        RepositorySystemSession session = booter.newRepositorySystemSession(system);
        ModelBuildingRequest modelRequest = new DefaultModelBuildingRequest();
        modelRequest.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        modelRequest.setProcessPlugins(false);
        modelRequest.setTwoPhaseBuilding(false);
        modelRequest.setSystemProperties(toProperties(session.getUserProperties(), session.getSystemProperties()));
        // modelRequest.setModelCache( DefaultModelCache.newInstance( session ) );
        ProjectModelResolver projectModelResolver = new ProjectModelResolver(session, null, system,
                new DefaultRemoteRepositoryManager(), booter.getRemoteRepositoriesWithAuthentification(system, session), null,
                null);
        modelRequest.setModelResolver(projectModelResolver);
        modelRequest.setActiveProfileIds(profilesList);
        modelRequest.setModelSource(new ModelSource() {

            @Override
            public InputStream getInputStream() throws IOException {
                return pomStream;
            }

            @Override
            public String getLocation() {
                return "";// FIXME return the component name
            }
        });
        if (modelBuilder == null) {
            modelBuilder = new DefaultModelBuilderFactory().newInstance();
        }
        ModelBuildingResult builtModel = modelBuilder.build(modelRequest);
        LOGGER.debug("built problems:" + builtModel.getProblems());
        return builtModel.getEffectiveModel();
    }

    private Properties toProperties(Map<String, String> dominant, Map<String, String> recessive) {
        Properties props = new Properties();
        if (recessive != null) {
            props.putAll(recessive);
        }
        if (dominant != null) {
            props.putAll(dominant);
        }
        return props;
    }

}
