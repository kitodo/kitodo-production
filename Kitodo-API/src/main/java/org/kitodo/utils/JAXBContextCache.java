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

package org.kitodo.utils;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * Cache for JAXBContexts. Class contains cache map of already created
 * JAXBContext of object classes.
 */
public class JAXBContextCache {

    private static JAXBContextCache instance;

    // ConcurrentHashMap takes care of synchronization in a multi-threaded
    // environment
    private static final Map<Class, JAXBContext> jaxbContextCache = new ConcurrentHashMap();

    private static final Map<ContextDescriptor, Object> contextDescriptorObjectCache = new ConcurrentHashMap();

    /**
     * The synchronized function singleton() must be used to obtain singleton access
     * to the JAXBContextCache instance.
     *
     * @return the singleton JAXBContextCache instance
     */
    public static JAXBContextCache getInstance() {
        JAXBContextCache localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (JAXBContextCache.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new JAXBContextCache();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    /**
     * Get unmarshalled object. This function caches unmarshalled objects of class
     * and file name. If the file is changed, the object is replaced when the
     * function is called again.
     *
     * @param clazz
     *            The class of object to cache.
     * @param file
     *            The file of object to cache.
     * @param <T>
     *            The generic class type.
     * @return The unmarshalled instance of class
     * @throws JAXBException
     *             The exception while unmarshaller is created or unmarshalling is
     *             being in process
     */
    public static <T> T getUnmarshalled(final Class<T> clazz, final File file) throws JAXBException {
        final ContextDescriptor contextDescriptor = new ContextDescriptor(clazz, file);
        final Object value = contextDescriptorObjectCache.get(contextDescriptor);
        if (Objects.nonNull(value)) {
            return (T) value;
        }

        // remove all existing unmarshalled objects for class in conjunction with
        // filename (heap of one unmarshalled object per context descriptor)
        for (ContextDescriptor cachedContextDescriptor : contextDescriptorObjectCache.keySet()) {
            if (cachedContextDescriptor.clazz.equals(clazz.toString())
                    && cachedContextDescriptor.fileName.equals(file.getName())) {
                contextDescriptorObjectCache.remove(cachedContextDescriptor);
            }
        }

        final Unmarshaller unmarshaller = getInstance().getJAXBContext(clazz).createUnmarshaller();
        T unmarshalledFile = (T) unmarshaller.unmarshal(file);
        contextDescriptorObjectCache.put(contextDescriptor, unmarshalledFile);
        return unmarshalledFile;
    }

    /**
     * Get the JAXBContext by class from cache.
     *
     * @param clazz
     *            The class to be bound.
     * @return The JAXBContext object.
     * @throws JAXBException
     *             Exception when creating new instance of JAXBContext
     */
    public static JAXBContext getJAXBContext(Class<?> clazz) throws JAXBException {
        if (jaxbContextCache.containsKey(clazz)) {
            return jaxbContextCache.get(clazz);
        }
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        jaxbContextCache.put(clazz, jaxbContext);
        return jaxbContext;
    }

    private static class ContextDescriptor {

        private final String clazz;

        private final long fileLastModified;

        private final String fileName;

        public boolean equals(Object potentialContextDescriptor) {
            if (potentialContextDescriptor instanceof ContextDescriptor) {
                final ContextDescriptor contextDescriptor = ((ContextDescriptor) potentialContextDescriptor);
                return clazz.equals(contextDescriptor.clazz) && this.fileName.equals(contextDescriptor.fileName)
                        && this.fileLastModified == contextDescriptor.fileLastModified;
            }
            return false;
        }

        public int hashCode() {
            return (clazz + fileName).hashCode();
        }

        ContextDescriptor(Class<?> clazz, File file) {
            this.clazz = clazz.toString();
            this.fileLastModified = file.lastModified();
            this.fileName = file.getName();
        }
    }

}
