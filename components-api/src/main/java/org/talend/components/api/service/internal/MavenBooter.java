package org.talend.components.api.service.internal;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilder;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.crypto.DefaultSettingsDecrypter;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import org.codehaus.plexus.util.Os;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
import org.talend.components.api.exception.ComponentException;
import org.talend.daikon.exception.error.CommonErrorCodes;

/**
 * A helper to boot the repository system and a repository system session.
 */
public class MavenBooter {

    public static final String TALEND_MAVEN_REMOTE_REPOSITORY_URL_SYS_PROP = "talend.maven.remote.repository.url"; //$NON-NLS-1$

    public static final String TALEND_MAVEN_REMOTE_REPOSITORY_ID_SYS_PROP = "talend.maven.remote.repository.id"; //$NON-NLS-1$

    static class SecDispatcher extends DefaultSecDispatcher {

        public SecDispatcher() {
            _configurationFile = new File(new File(System.getProperty("user.home"), ".m2"), "settings-security.xml") //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
                    .getAbsolutePath();
            try {
                _cipher = new DefaultPlexusCipher();
            } catch (PlexusCipherException e) {
                e.printStackTrace();
            }
        }

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenBooter.class);

    private static boolean OS_WINDOWS = Os.isFamily("windows");

    private static final String SETTINGS_XML = "settings.xml";

    public DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepo = new LocalRepository(getDefaultLocalRepoDir());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        return session;
    }

    private static DefaultSettingsBuilder settingsBuilder = new DefaultSettingsBuilderFactory().newInstance();

    private Settings settings;

    File getDefaultLocalRepoDir() {
        try {
            Settings settings = getSettings();
            if (settings.getLocalRepository() != null) {
                return new File(settings.getLocalRepository());
            }
        } catch (SettingsBuildingException e) {
            throw new ComponentException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }

        return new File(new File(System.getProperty("user.home"), ".m2"), "repository");
    }

    public synchronized Settings getSettings() throws SettingsBuildingException {
        if (settings == null) {
            DefaultSettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
            request.setUserSettingsFile(getUserSettings());
            request.setGlobalSettingsFile(getGlobalSettings());
            request.setSystemProperties(getSystemProperties());

            settings = settingsBuilder.build(request).getEffectiveSettings();
            SettingsDecryptionResult result = newDefaultSettingsDecrypter()
                    .decrypt(new DefaultSettingsDecryptionRequest(settings));
            settings.setServers(result.getServers());
            settings.setProxies(result.getProxies());
        }
        return settings;
    }

    public File getUserSettings() {
        File userHome = new File(System.getProperty("user.home"));
        return new File(new File(userHome, ".m2"), SETTINGS_XML);
    }

    public File getGlobalSettings() {
        String mavenHome = getMavenHome();
        if (mavenHome != null) {
            return new File(new File(mavenHome, "conf"), SETTINGS_XML);
        }
        return null;
    }

    public String getMavenHome() {
        String mavenHome = System.getProperty("maven.home");
        if (mavenHome != null) {
            return mavenHome;
        }
        return System.getenv("M2_HOME");
    }

    private Properties getSystemProperties() {
        Properties props = new Properties();
        getEnvProperties(props);
        props.putAll(System.getProperties());
        return props;
    }

    private Properties getEnvProperties(Properties props) {
        if (props == null) {
            props = new Properties();
        }
        boolean envCaseInsensitive = OS_WINDOWS;
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            String key = entry.getKey();
            if (envCaseInsensitive) {
                key = key.toUpperCase(Locale.ENGLISH);
            }
            key = "env." + key;
            props.put(key, entry.getValue());
        }
        return props;
    }

    public List<RemoteRepository> getRemoteRepositoriesWithAuthentification(RepositorySystem system,
            RepositorySystemSession session) {
        List<RemoteRepository> repositoryList = getRepositoryList();
        return createAuthentifiedRemoteRepositories(repositoryList);
    }

    /**
     * create a new list of repositories with authentification, if no settings are available no authentication is set.
     * 
     * @param repositoryList list of repos
     * @return a new list with authentitication set found in the settings
     * @throws SettingsBuildingException
     */
    private List<RemoteRepository> createAuthentifiedRemoteRepositories(List<RemoteRepository> repositoryList) {
        ArrayList<RemoteRepository> authentifiedList = new ArrayList<RemoteRepository>(repositoryList.size());
        Settings settings2;
        try {
            settings2 = getSettings();
            for (RemoteRepository repo : repositoryList) {
                String repoId = repo.getId();
                Server server = settings2.getServer(repoId);
                if (server != null) {
                    Authentication authentication = new AuthenticationBuilder().addUsername(server.getUsername())
                            .addPassword(server.getPassword()).build();
                    authentifiedList.add(new RemoteRepository.Builder(repo).setAuthentication(authentication).build());
                } else {// no authentication found so just use the original repo
                    authentifiedList.add(repo);
                }
            }
        } catch (SettingsBuildingException e) {
            // no setings available so log it and return the original list
            LOGGER.error("failed to load maven settings.", e);
            return repositoryList;
        }
        return authentifiedList;
    }

    protected List<RemoteRepository> getRepositoryList() {
        ArrayList<RemoteRepository> repoList = new ArrayList<>(Arrays.asList(newCentralRepository()));
        repoList.addAll(newTalendRepositories());
        return repoList;
    }

    protected RemoteRepository newCentralRepository() {
        return new RemoteRepository.Builder("central", "default", "http://central.maven.org/maven2/").build();
    }

    protected List<RemoteRepository> newTalendRepositories() {
        List<RemoteRepository> talendRepos = new ArrayList<>();
        talendRepos.add(new RemoteRepository.Builder("talend-opensource-release", "default",
                "http://newbuild.talend.com:8081/nexus/content/repositories/TalendOpenSourceRelease/").build());
        talendRepos.add(new RemoteRepository.Builder("talend-opensource-snapshot", "default",
                "http://newbuild.talend.com:8081/nexus/content/repositories/TalendOpenSourceSnapshot/").build());
        String extraRepoId = System.getProperty(TALEND_MAVEN_REMOTE_REPOSITORY_ID_SYS_PROP);
        String extraRepoUrl = System.getProperty(TALEND_MAVEN_REMOTE_REPOSITORY_URL_SYS_PROP);
        if (extraRepoId != null && extraRepoUrl != null) {
            talendRepos.add(new RemoteRepository.Builder(extraRepoId, "default", extraRepoUrl).build());
        } // else no extra repo so ignor it.
        return talendRepos;
    }

    public DefaultSettingsDecrypter newDefaultSettingsDecrypter() {
        SecDispatcher secDispatcher = new SecDispatcher();

        DefaultSettingsDecrypter decrypter = new DefaultSettingsDecrypter();

        try {
            Field field = decrypter.getClass().getDeclaredField("securityDispatcher");
            field.setAccessible(true);
            field.set(decrypter, secDispatcher);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return decrypter;
    }

    public RepositorySystem newRepositorySystem() {
        /*
         * Aether's components implement org.eclipse.aether.spi.locator.Service to ease manual wiring and using the
         * prepopulated DefaultServiceLocator, we only need to register the repository connector and transporter
         * factories.
         */
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        if (false) {
            locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
                @Override
                public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }
        return locator.getService(RepositorySystem.class);
    }
}
