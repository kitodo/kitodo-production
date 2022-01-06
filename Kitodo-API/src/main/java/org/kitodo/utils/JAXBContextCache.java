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

public class JAXBContextCache {

    private static JAXBContextCache instance;

    private static Map<Class, JAXBContext> jaxbContextCache;

    private JAXBContextCache() {
        // ConcurrentHashMap takes care of synchronization in a multi-threaded
        // environment
        jaxbContextCache = new ConcurrentHashMap<>();
    }

    public static JAXBContextCache getInstance() {
        if (Objects.isNull(instance)) {
            instance = new JAXBContextCache();
        }
        return instance;
    }

    public JAXBContext get(Class<?> classToBeBound) throws JAXBException {
        if (jaxbContextCache.containsKey(classToBeBound)) {
            return jaxbContextCache.get(classToBeBound);
        }

        JAXBContext jaxbContext = JAXBContext.newInstance(classToBeBound);
        jaxbContextCache.put(classToBeBound, jaxbContext);
        return jaxbContext;
    }
}
