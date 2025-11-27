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

package org.kitodo.selenium.testframework.helper;

import static org.awaitility.Awaitility.await;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.pagefactory.ElementLocator;

/**
 * Custom implementation of the default Selenium LocatingElementHandler.
 * 
 * <p>The default implementation tries to invoke a WebElement method just once. However, there can be 
 * instances where a WebElement is already stale before the method can be invoked. In contrast, this
 * implementation tries to invoke the method for some time in case there are any exceptions.</p>
 */
public class RepeatingLocatingElementHandler  implements InvocationHandler {

    private static final Object NULL_OBJECT = new Object();

    private static final Logger logger = LogManager.getLogger(RepeatingLocatingElementHandler.class);

    private final ElementLocator locator;

    public RepeatingLocatingElementHandler(ElementLocator locator) {
        this.locator = locator;
    }

    /**
     * Invokes a method on a WebElement.
     */
    @Override
    public Object invoke(Object object, Method method, Object[] objects) {
        Callable<Object> findAndInvoke = () -> {
            logger.trace("invoke method {} on WebElement with locator {}", method.getName(), locator.toString());
            WebElement element;
            try {
                element = locator.findElement();
            } catch (NoSuchElementException e) {
                if ("toString".equals(method.getName())) {
                    return "Proxy element for: " + locator;
                }
                throw e;
            }

            if ("getWrappedElement".equals(method.getName())) {
                return element;
            }

            Object result = method.invoke(element, objects);

            // replace null with custom object because awaitility doesn't accept null as result
            if (Objects.nonNull(result)) {
                return result;
            }
            return NULL_OBJECT;
        };

        Object result = await("invoke method '" + method.getName() + "'' on WebElement with locator '" + locator.toString() + "'")
            .ignoreExceptions()
            .pollInterval(100, TimeUnit.MILLISECONDS)
            .atMost(5, TimeUnit.SECONDS).ignoreExceptions()
            .until(findAndInvoke, (obj) -> {
                // allow any result as long as there was no exception, e.g. StaleElementReferenceException
                return true;
            });

        // replace NULL_OBJECT with null again
        if (result == NULL_OBJECT) {
            return null;
        }
        return result;
    }
}
