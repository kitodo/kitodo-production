package org.kitodo.serviceloader;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.ServiceLoader;

public class KitodoServiceLoader<T> {
    private ServiceLoader<T> loader;
    private Class clazz;

    public KitodoServiceLoader(Class clazz){
        this.clazz = clazz;
    }

    public T loadModule() throws MalformedURLException {
        File loc = new File("plugins");

        URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        URL urls[] = sysLoader.getURLs(), udir = loc.toURI().toURL();
        String udirs = udir.toString();
        for (int i = 0; i < urls.length; i++)
            if (urls[i].toString().equalsIgnoreCase(udirs)) {
                return null;
            }
        Class<URLClassLoader> sysClass = URLClassLoader.class;
        try {
            Method method = sysClass.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            method.invoke(sysLoader, new Object[] {udir});
        } catch (Throwable t) {
            t.printStackTrace();
        }

        loader = ServiceLoader.load(clazz);
        Iterator<T> modules = loader.iterator();
        return modules.next();
    }

}
