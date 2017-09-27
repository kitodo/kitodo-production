/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.serviceloader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KitodoServiceLoader<T> {
    private Class clazz;
    private String modulePath;
    private static final Logger logger = LogManager.getLogger(KitodoServiceLoader.class);

    /**
     * Constructor for KitodoServiceLoader.
     * 
     * @param clazz
     *            interface class of module to load
     * @param modulePath
     *            path to module folder
     */
    public KitodoServiceLoader(Class clazz, String modulePath) {
        this.clazz = clazz;
        if (!new File(modulePath).exists()) {
                logger.error("Specified module folder does not exist: " + modulePath);
        } else {
            this.modulePath = modulePath;
        }
    }

    /**
     * Loads a module from the classpath which implements the constructed clazz.
     * 
     * @return A module with type T.
     */
    @SuppressWarnings("unchecked")
    public T loadModule() {

        loadModulesIntoClasspath();

        ServiceLoader<T> loader = ServiceLoader.load(clazz);

        return loader.iterator().next();
    }

    /**
     * Loads jars from the pluginsFolder to the classpath, so the ServiceLoader can
     * find them.
     */
    private void loadModulesIntoClasspath() {
        Path moduleFolder = FileSystems.getDefault().getPath(modulePath);

        URLClassLoader sysLoader;
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(moduleFolder, "*.jar");
            for (Path f : stream) {
                File loc = new File(f.toString());
                sysLoader = (URLClassLoader) this.getClass().getClassLoader();
                ArrayList<URL> urls = new ArrayList<>(Arrays.asList(sysLoader.getURLs()));
                URL udir = loc.toURI().toURL();

                if (!urls.contains(udir)) {
                    Class<URLClassLoader> sysClass = URLClassLoader.class;
                    Method method = sysClass.getDeclaredMethod("addURL", URL.class);
                    method.setAccessible(true);
                    method.invoke(sysLoader, udir);
                }
            }
        } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.error("Classpath could not be accessed", e.getMessage());
        }
    }

}
