package org.talend.components.service.spi;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ServiceLoader;

import org.hamcrest.collection.IsIterableWithSize;
import org.junit.Test;
import org.talend.components.api.ComponentInstaller;
import org.talend.daikon.runtime.RuntimeUtil;


public class ExtensibleUrlClassLoaderTest {

    @Test
    public void testDynamicClassLoaderService() throws MalformedURLException {
        // this will check that the java service loader works on a classloader that is mutable
        RuntimeUtil.registerMavenUrlHandler();
        // given
        ExtensibleUrlClassLoader urlClassLoader = new ExtensibleUrlClassLoader(new URL[0]);
        // 2 comp installer
        assertThat(ServiceLoader.load(ComponentInstaller.class, urlClassLoader),
                IsIterableWithSize.<ComponentInstaller> iterableWithSize(2));

        // when
        urlClassLoader.addURL(new URL("mvn:org.talend.components/multiple-runtime-comp"));

        // then
        // 3 comp installer
        assertThat(ServiceLoader.load(ComponentInstaller.class, urlClassLoader),
                IsIterableWithSize.<ComponentInstaller> iterableWithSize(3));

    }


}
