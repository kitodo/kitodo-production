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

package org.kitodo.helper.messages;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;

abstract class CustomResourceBundle extends ResourceBundle {

    private static final Logger logger = LogManager.getLogger(CustomResourceBundle.class);

    @Override
    public Enumeration<String> getKeys() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object handleGetObject(String key) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get custom resource bundle. In case there is a custom version of translation
     * files load them, if not load the default ones.
     * 
     * @param defaultBundleName
     *            name of the default translation bundle
     * @param customBundleName
     *            name of the custom translation bundle
     * @param locale
     *            for which translation bundle should be created
     * @return available translation bundle
     */
    public static ResourceBundle getResourceBundle(String defaultBundleName, String customBundleName, Locale locale) {
        File file = new File(ConfigCore.getParameterOrDefaultValue(ParameterCore.DIR_LOCAL_MESSAGES));
        if (file.exists()) {
            try {
                final URL resourceURL = file.toURI().toURL();
                URLClassLoader urlLoader = AccessController.doPrivileged(
                    (PrivilegedAction<URLClassLoader>) () -> new URLClassLoader(new URL[] {resourceURL }));
                return ResourceBundle.getBundle(customBundleName, locale, urlLoader);
            } catch (MalformedURLException | MissingResourceException e) {
                logger.info(e.getMessage(), e);
            }
        }
        return ResourceBundle.getBundle(defaultBundleName, locale);
    }

    ResourceBundle getBaseResources(String bundleName) {
        return ResourceBundle.getBundle(bundleName, getCurrentLocale());
    }

    Object getValueFromExtensionBundles(String key, String bundleName) {
        ResourceBundle extensionResources = getExtensionResources(bundleName);
        if (Objects.nonNull(extensionResources)) {
            return extensionResources.getObject(key);
        }
        return null;
    }

    private ResourceBundle getExtensionResources(String bundleName) {
        File file = new File(ConfigCore.getParameterOrDefaultValue(ParameterCore.DIR_LOCAL_MESSAGES));
        if (file.exists()) {
            try {
                final URL resourceURL = file.toURI().toURL();
                URLClassLoader urlLoader = AccessController.doPrivileged(
                    (PrivilegedAction<URLClassLoader>) () -> new URLClassLoader(new URL[] {resourceURL }));
                return ResourceBundle.getBundle(bundleName, getCurrentLocale(), urlLoader);
            } catch (MalformedURLException | MissingResourceException e) {
                logger.info(e.getMessage(), e);
            }
        }
        return null;
    }

    private Locale getCurrentLocale() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (Objects.nonNull(facesContext)) {
            return facesContext.getViewRoot().getLocale();
        } else {
            return new Locale(ConfigCore.getParameterOrDefaultValue(ParameterCore.LANGUAGE_DEFAULT));
        }
    }
}
