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

package org.kitodo.production.helper.cache;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.faces.context.FacesContext;

/**
 * Caches arbitrary objects while processing a request.
 * 
 * <p>Stores objects in a simple hash map inside FacesContext.getAttributes(). After each request, 
 * a new FacesContext will be created, which means cached objects are not available any more.</p>
 * 
 * <p>Usually, such caching is not necessary when implementing Beans annotated with @RequestScope
 * and getters that store processed values in local variables. However, a lot of beans do not 
 * follow this pattern, which means that some common methods, e.g., getting the current locale of 
 * a user (which requires a database lookup), are re-evaluated many times during processing of 
 * a request.</p>
 * 
 * <p>Therefore, this class provides the possibility to inject a request scoped cache.</p>
 * 
 * <p>If this cache is used outside of a JSF request context, it will simply re-evaluate the supplier 
 * function upon every request.</p>
 * 
 */
public class RequestScopeCacheHelper {

    private static final String ATTRIBUTE_NAME = "RequestScopeCacheHelper";

    /**
     * Load or create a simple hash map in the FacesContext.getAttributes() map.
     * @return the cache
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> getCache() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (Objects.nonNull(ctx)) {
            if (!ctx.getAttributes().containsKey(ATTRIBUTE_NAME)) {
                ctx.getAttributes().put(ATTRIBUTE_NAME, new ConcurrentHashMap<String, Object>());
            }
            return (ConcurrentHashMap<String, Object>)ctx.getAttributes().get(ATTRIBUTE_NAME);
        }
        return null;
    }

    /**
     * Returns a cached object if it has been previously requested within the scope of a request,
     * otherwise it will evaluate the supplier function.
     * 
     * @param <T> the type of the object that is cached
     * @param key the key that is used to cache an object (needs to be unique application wide)
     * @param supplier a supplier function that is evaluated if the object is not found in cache 
     *                 and whose result is then stored in the cache
     * @param clazz the class of the object type
     * @return the cached object or the object provided by supplier if it was not cached before
     */
    public static <T> T getFromCache(String key, Supplier<T> supplier, Class<T> clazz) {
        Map<String, Object> cache = getCache();
        if (Objects.nonNull(cache)) {
            // save value if it does not yet exist
            if (!cache.containsKey(key)) {
                T value = supplier.get();
                cache.put(key, value);
            };
            return clazz.cast(cache.get(key));
        }
        // cache not available, e.g., when called outside of request scope (in a background process)
        return supplier.get();
    }
}
