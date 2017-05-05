package org.talend.components.service.spi;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Just a simple URLClassLoader to which you can add new url to extends the classpath
 */
public class ExtensibleUrlClassLoader extends URLClassLoader {

    public ExtensibleUrlClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    public ExtensibleUrlClassLoader(URL[] urls) {
        super(urls);
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }
}