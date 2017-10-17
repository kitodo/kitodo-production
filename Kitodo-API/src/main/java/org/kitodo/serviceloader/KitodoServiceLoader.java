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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
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
     * Frontend files of all modules will be loaded into the core module.
     *
     * @return A module with type T.
     */
    @SuppressWarnings("unchecked")
    public T loadModule() {

        loadModulesIntoClasspath();
        loadBeans();
        loadFrontendFilesIntoCore();

        ServiceLoader<T> loader = ServiceLoader.load(clazz);

        return loader.iterator().next();
    }

    /**
     * Loads bean classes and registers them to the FacesContext. Afterwards they can be used in all
     * frontend files
     */
    private void loadBeans() {
        try {

            Path moduleFolder = FileSystems.getDefault().getPath(modulePath);
            DirectoryStream<Path> stream = Files.newDirectoryStream(moduleFolder, "*.jar");

            for (Path f : stream) {
                JarFile jarFile = new JarFile(f.toString());

                if (hasFrontendFiles(jarFile)) {

                    Enumeration<JarEntry> e = jarFile.entries();

                    URL[] urls = {new URL("jar:file:" + f.toString() + "!/")};
                    URLClassLoader cl = URLClassLoader.newInstance(urls);

                    while (e.hasMoreElements()) {
                        JarEntry je = e.nextElement();

                        // TODO: konvention: name der xhtml datei + Form, bspw.: sample.xhtml -> SampleForm.java
                        // deshalb wird hier auf "Form.class" gesucht

                        if (je.isDirectory() || !je.getName().endsWith("Form.class")) {
                            continue;
                        }

                        String className = je.getName().substring(0, je.getName().length() - 6);
                        className = className.replace('/', '.');
                        Class c = cl.loadClass(className);

                        String beanName = className.substring(className.lastIndexOf(".") + 1).trim();

                        FacesContext facesContext = FacesContext.getCurrentInstance();
                        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);

                        session.getServletContext().setAttribute(beanName, c.newInstance());
                    }
                }
            }
        }
        catch (Exception e) {
            logger.error("Classpath could not be accessed", e.getMessage());
        }
    }

    /**
     * If the found jar files have frontend files, they will be extracted and copied into the frontend folder
     * of the core module. Before copying, existing frontend files of the same module will be deleted from the
     * core module. Afterwards the created temporary folder will be deleted as well.
     */
    // TODO: Relative Pfade müssen zu absoluten Pfaden angepasst werden
    private void loadFrontendFilesIntoCore() {
        Path moduleFolder = FileSystems.getDefault().getPath(modulePath);

        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(moduleFolder, "*.jar");

            for (Path f : stream) {

                File loc = new File(f.toString());
                JarFile jarFile = new JarFile(loc);

                if (hasFrontendFiles(jarFile)) {
                    String temporaryFolderName = generateRandomString(10);
                    extractFrontEndFiles(loc.getAbsolutePath(), temporaryFolderName);
                    String moduleName = extractModuleName(temporaryFolderName);
                    FileUtils.deleteDirectory(new File("Kitodo/src/main/webapp/pages/" + moduleName));
                    copyFrontEndFiles(temporaryFolderName + "/META-INF/resources", "Kitodo/src/main/webapp/pages/" + moduleName);
                    FileUtils.deleteDirectory(new File(temporaryFolderName));
                }
            }
        }
        catch (Exception e) {
            logger.error("Classpath could not be accessed", e.getMessage());
        }
    }

    /**
     * Extracts the module name of the current module by finding the pom.properties
     * in the folder given by the temporary folder name
     *
     * @param temporaryFolderName
     *            folder name in which the pom.properties file will be searched for
     *
     * @return boolean
     */
    protected String extractModuleName(String temporaryFolderName) throws IOException {
        Properties prop = new Properties();
        InputStream input = null;
        String moduleName = "";
        File properties = findFile("pom.properties", temporaryFolderName);

        try {
            input = new FileInputStream(properties);
            prop.load(input);
            return moduleName = prop.getProperty("artifactId");

        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * This method generates random string
     *
     * @return
     */
    private String generateRandomString(int randomStringLength){
        final String charList = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuffer randomString = new StringBuffer();
        for(int i=0; i<randomStringLength; i++){
            int number = getRandomNumber();
            randomString.append(charList.charAt(number));
        }
        return randomString.toString();
    }

    /**
     * This method generates random numbers
     *
     * @return int
     */
    private int getRandomNumber() {
        final int lenght = 50;
        int randomInt;
        Random randomGenerator = new Random();
        randomInt = randomGenerator.nextInt(lenght);
        if (randomInt - 1 == -1) {
            return randomInt;
        } else {
            return randomInt - 1;
        }
    }

    /**
     * Copies extracted frontend files by a given source folder name to the destination Folder
     * given by a destination folder name
     *
     * @param sourceFolder
     *            copies all extracted frontend files
     * @param destinationFolder
     *            jarFile that will be checked for frontend files
     */
    private void copyFrontEndFiles(String sourceFolder, String destinationFolder) throws IOException {
        FileUtils.copyDirectory(new File(sourceFolder), new File(destinationFolder));
    }

    /**
     * Checks, whether a passed jarFile has frontend files or not. Returns true, when the jar contains
     * a folder with the name "resources"
     *
     * @param jarPath
     *            jarFile that will be checked for frontend files
     * @param destinationPath
     *            destination path, where the frontend files will be extracted to
     *
     */
    private void extractFrontEndFiles(String jarPath, String destinationPath) throws IOException {
        File destinationFolder = new File(destinationPath);
        destinationFolder.mkdir();

        java.util.jar.JarFile jar = new java.util.jar.JarFile(jarPath);
        java.util.Enumeration enumEntries = jar.entries();
        while (enumEntries.hasMoreElements()) {
            java.util.jar.JarEntry file2 = (java.util.jar.JarEntry) enumEntries.nextElement();

            if (file2.getName().contains("resources") || file2.getName().contains("pom.properties")) {
                java.io.File f = new java.io.File(destinationPath + java.io.File.separator + file2.getName());
                if (file2.isDirectory()) {
                    f.mkdirs();
                    continue;
                }
                if (file2.getName().contains("pom.properties")) {
                    f.getParentFile().mkdirs();
                }

                java.io.InputStream is = jar.getInputStream(file2);
                java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
                while (is.available() > 0) {
                    fos.write(is.read());
                }
                fos.close();
                is.close();
            }
        }
        jar.close();
    }

    /**
     * Checks, whether a passed jarFile has frontend files or not. Returns true, when the jar contains
     * a folder with the name "resources"
     *
     * @param jarFile
     *            jarFile that will be checked for frontend files
     *
     * @return boolean
     */
    private boolean hasFrontendFiles(JarFile jarFile) {
        Enumeration enums = jarFile.entries();
        while (enums.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry)enums.nextElement();
            if (jarEntry.getName().contains("resources") && jarEntry.isDirectory()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tries to find a file by a given file name in a folder by folder name
     *
     * @param name
     *            file name that will be searched for
     * @param folderName
     *            folder that will be searched
     *
     * @return File, null
     */
    public File findFile(String name, String folderName)
    {
        Collection<File> s = FileUtils.listFiles(new File(folderName), null, true);

        for (File f:
                s) {
            if (f.getName().equals(name))
                return f;
        }
        return null;
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
