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

package org.kitodo.production.helper;

import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.cache.RequestScopeCacheHelper;
import org.kitodo.production.security.SecurityUserDetails;
import org.kitodo.production.services.ServiceManager;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * The class LocaleHelper contains static functions for handling locales.
 */
public class LocaleHelper {

    private static final Logger logger = LogManager.getLogger(LocaleHelper.class);

    public static final String COOKIE_LANG_NAME = "kitodo_lang";

    /**
     * Private constructor to hide the implicit public one.
     */
    private LocaleHelper() {

    }

    /**
     * Get the current locale from a request scoped cache to avoid repeated 
     * queries to the database required to retrieve the current user's language 
     * setting. 
     *
     * @return the current locale
     */
    public static Locale getCurrentLocale() {
        return RequestScopeCacheHelper.getFromCache(
            "current_locale",
            () -> calculateCurrentLocale(),
            Locale.class
        );
    }

    /**
     * Get the current locale. If user is authenticated the locale is generated
     * based on the selected user language. If there is not an authenticated user,
     * Locale is generated based on the faces context of prime face. For this
     * purpose, it is checked whether a cookie is available, the browser or the
     * application locale is set. Otherwise, if there is no faces context the locale
     * of spring is used and thus the system default too. Update locale in spring
     * locale context holder.
     *
     * @return the current locale
     */
    private static Locale calculateCurrentLocale() {        
        Locale locale = getAuthenticatedUserLocale();
        if (Objects.nonNull(locale)) {
            LocaleContextHolder.setLocale(locale);
            return locale;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (Objects.nonNull(facesContext)) {
            locale = getLocaleOfFacesContext(facesContext);
            if (Objects.nonNull(locale)) {
                LocaleContextHolder.setLocale(locale); // set faces context locale as spring locale
            }
        }

        return LocaleContextHolder.getLocale(); // spring locale with system default     
    }

    /**
     * Get the authenticated user locale.
     *
     * @return the locale of the authenticated user.
     */
    public static Locale getAuthenticatedUserLocale() {
        SecurityUserDetails securityUserDetails = ServiceManager.getUserService().getAuthenticatedUser();
        if (Objects.nonNull(securityUserDetails)) {
            try {
                User user = ServiceManager.getUserService().getById(securityUserDetails.getId());
                return new Locale.Builder().setLanguageTag(user.getLanguage()).build();
            } catch (DAOException e) {
                Helper.setErrorMessage("errorLoadingOne",
                    new Object[] {ObjectType.USER.getTranslationSingular(), securityUserDetails.getId() }, logger, e);
            }

        }
        return null;
    }

    /**
     * Check if locale is supported.
     *
     * @param locale
     *            the locale to check
     * @return True or false if locale is supported
     */
    public static boolean isSupportedLocale(Locale locale) {
        Iterator<Locale> supportedLocales = FacesContext.getCurrentInstance().getApplication()
                .getSupportedLocales();
        while (supportedLocales.hasNext()) {
            Locale supportedLocale = supportedLocales.next();
            if (supportedLocale.getLanguage().equals(locale.getLanguage())) {
                return true;
            }
        }
        return false;
    }

    private static Locale getLocaleOfFacesContext(FacesContext facesContext) {
        Locale locale;
        if (facesContext.getExternalContext().getRequestCookieMap().containsKey(COOKIE_LANG_NAME)) {
            // locale from cookie
            Cookie cookie = (Cookie) facesContext.getExternalContext().getRequestCookieMap().get(COOKIE_LANG_NAME);
            locale = new Locale.Builder().setLanguageTag(cookie.getValue()).build();
        } else {
            locale = facesContext.getExternalContext().getRequestLocale(); // browser locale
        }

        if (isSupportedLocale(locale)) {
            return locale;
        }

        return facesContext.getApplication().getDefaultLocale(); // application locale
    }

}
