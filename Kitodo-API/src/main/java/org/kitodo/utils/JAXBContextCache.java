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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Cache for JAXBContexts. Class contains cache map of already created
 * JAXBContext of object classes.
 */
public class JAXBContextCache {

    private static JAXBContextCache instance;

    private static Map<Class, JAXBContext> jaxbContextCache;

    private JAXBContextCache() {
        // ConcurrentHashMap takes care of synchronization in a multi-threaded
        // environment
        jaxbContextCache = new ConcurrentHashMap<>();
    }

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
     * Get the JAXBContext by class from cache.
     *
     * @param clazz
     *            The class to be bound.
     * @return The JAXBContext object.
     * @throws JAXBException
     *             Exception when creating new instance of JAXBContext
     */
    public JAXBContext get(Class<?> clazz) throws JAXBException {
        if (jaxbContextCache.containsKey(clazz)) {
            return jaxbContextCache.get(clazz);
        }

        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        jaxbContextCache.put(clazz, jaxbContext);
        return jaxbContext;
    }
}
