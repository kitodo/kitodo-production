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

package de.sub.goobi.forms;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;

import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.LocaleUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.DefaultValues;
import org.kitodo.config.Parameters;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;

/**
 * The SpracheForm class serves to switch the displayed language for the current
 * user in the running application.
 */
@Named("SpracheForm")
@SessionScoped
public class SpracheForm implements Serializable {

    private static final String SESSION_LOCALE_FIELD_ID = "lang";
    private static final long serialVersionUID = -8766724454080390450L;
    private static final Logger logger = LogManager.getLogger(SpracheForm.class);
    private transient ServiceManager serviceManager = new ServiceManager();

    /**
     * The constructor of this class loads the required MessageBundle.
     */
    public SpracheForm() {
        setSessionLocaleFieldId();
    }

    /**
     * Set session locale field id.
     *
     *
     */
    private void setSessionLocaleFieldId() {
        String key = "";
        if (Objects.isNull(serviceManager.getUserService().getAuthenticatedUser())) {
            key = ConfigCore.getParameter(Parameters.LANGUAGE_DEFAULT, DefaultValues.LANGUAGE_DEFAULT);
        } else {
            try {
                User user = serviceManager.getUserService().getById(serviceManager.getUserService().getAuthenticatedUser().getId());
                key = user.getLanguage();
            } catch (DAOException e) {
                logger.error("Error in retrieving user : " + e.getMessage());
            }
        }
        Locale locale = new Locale.Builder().setLanguageTag(key).build();
        if (LocaleUtils.isAvailableLocale(locale)) {
            FacesContext context = FacesContext.getCurrentInstance();
            if (Objects.nonNull(context.getViewRoot())) {
                context.getViewRoot().setLocale(locale);
                context.getExternalContext().getSessionMap().put(SESSION_LOCALE_FIELD_ID, locale);
            }
        }
    }

    /**
     * The function getSupportedLocales() returns a list of maps, each
     * representing one locale configured in the faces-config.xml file. Each of
     * the maps will contain the fields. id − the locale’s ID String, e.g. “fr”
     * or “en_GB” displayLanguageSelf − the name of the language in the language
     * itself, e.g. “English”, “Deutsch”, “français”, “español”, “русский”,
     * “日本語”, …
     *
     * <p>
     * It’s a good practice to identify a language in its own spelling, since
     * this will be most likely what a speaker of that language will recognize.
     * See also: http://www.cs.tut.fi/~jkorpela/flags.html Note that
     * capitalisation is subject to the respective language. If the language is
     * unknown, the id will be returned. displayLanguageTranslated − the name of
     * the language in the currently selected language, e.g., if the current
     * language is English: “English”, “German”, “French”, …
     * </p>
     *
     * <p>
     * This is just a nice feature because the language names are provided by
     * Java; and it’s used in the mouse-over titles, so you can find out what
     * e.g. “हिंदी” means, even if you don’t have a clue of the glyphs used. If
     * no translations are available, this will fall back to English. selected −
     * whether this is the current language
     * </p>
     *
     * <p>
     * This can be used for a sophisticated layout.
     * </p>
     *
     * @return a list of maps, each with the fields “id”, “displayName” and
     *         “selected”
     */
    public List<Map<String, Object>> getSupportedLocales() {
        List<Map<String, Object>> result = new ArrayList<>();
        Locale currentDisplayLanguage = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        @SuppressWarnings("unchecked")
        // It seems we have an old Faces API, Faces 2.1’s getSupportedLocales()
        // returns Iterator<Locale>
        // TODO: Update JSF API
        Iterator<Locale> localesIterator = FacesContext.getCurrentInstance().getApplication().getSupportedLocales();
        while (localesIterator.hasNext()) {
            Locale supportedLocale = localesIterator.next();
            if (supportedLocale.getLanguage().length() > 0) {
                Map<String, Object> translation = new HashMap<>();
                translation.put("id", supportedLocale.toString());
                translation.put("displayLanguageSelf", supportedLocale.getDisplayLanguage(supportedLocale));
                translation.put("displayLanguageTranslated",
                    supportedLocale.getDisplayLanguage(currentDisplayLanguage));
                translation.put("selected", supportedLocale.equals(currentDisplayLanguage));
                translation.put("flag", "javax.faces.resource/images/" + supportedLocale.toString() + ".svg.jsf");
                result.add(translation);
            }
        }
        return result;
    }

    /**
     * The procedure switchLanguage is used to alter the application’s interface
     * language.
     *
     * @param langCodeCombined
     *            This parameter can be either of form “‹language›” or of form
     *            “‹language›_‹country›”, e.g. “en” or “en_GB” are valid values.
     */
    @SuppressWarnings("unchecked")
    public void switchLanguage(String langCodeCombined) throws IOException {
        String[] languageCode = langCodeCombined.split("_");
        Locale locale;
        if (languageCode.length == 2) {
            locale = new Locale(languageCode[0], languageCode[1]);
        } else {
            locale = new Locale(languageCode[0]);
        }
        try {
            User user = serviceManager.getUserService().getById(serviceManager.getUserService().getAuthenticatedUser().getId());
            user.setLanguage(locale.toString());
            serviceManager.getUserService().save(user);
        } catch (DataException e) {
            logger.error("Error in saving user : " + e.getMessage());
        } catch (DAOException e) {
            logger.error("Error in retrieving user" + e.getMessage());
        }
        FacesContext context = FacesContext.getCurrentInstance();
        context.getViewRoot().setLocale(locale);
        context.getExternalContext().getSessionMap().put(SESSION_LOCALE_FIELD_ID, locale);
        // Reload current page to make language change effective
        context.getExternalContext()
                .redirect(((HttpServletRequest) context.getExternalContext().getRequest()).getRequestURI());
    }

    /**
     * Get locale.
     *
     * @return Locale object
     */
    public Locale getLocale() {
        setSessionLocaleFieldId();
        FacesContext fac = FacesContext.getCurrentInstance();
        UIViewRoot frame = fac.getViewRoot();
        if (!Objects.equals(frame, null)) {
            @SuppressWarnings("rawtypes")
            Map session = fac.getExternalContext().getSessionMap();
            if (session.containsKey(SESSION_LOCALE_FIELD_ID)) {
                Locale locale = (Locale) session.get(SESSION_LOCALE_FIELD_ID);
                if (frame.getLocale() != locale) {
                    frame.setLocale(locale);
                }
                return locale;
            } else {
                return frame.getLocale();
            }
        } else {
            /*
             * When no locale is given (no Accept-Language Http Request header
             * is present) return default language
             */
            String key = ConfigCore.getParameter(Parameters.LANGUAGE_DEFAULT,
                DefaultValues.LANGUAGE_DEFAULT);
            Locale locale = new Locale.Builder().setLanguageTag(key).build();
            if (LocaleUtils.isAvailableLocale(locale)) {
                return locale;
            } else {
                throw new IllegalArgumentException("Locale code is not valid");
            }
        }
    }

    /**
     * The function getGroupingSeparator() returns the character used for
     * thousands separator for the current locale as read-only property
     * "groupingSeparator".
     *
     * @return the character used for thousands separator
     */
    public char getGroupingSeparator() {
        return DecimalFormatSymbols.getInstance(getLocale()).getGroupingSeparator();
    }

    /**
     * Set language.
     *
     * @param language
     *            String
     */
    public void setLanguage(String language) {
        try {
            switchLanguage(language);
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Get language.
     *
     * @return String language
     */
    public String getLanguage() {
        return getLocale().getLanguage();
    }
}
