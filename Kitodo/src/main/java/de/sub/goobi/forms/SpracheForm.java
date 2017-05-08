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

import java.text.DecimalFormatSymbols;
import java.util.*;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

/**
 * The SpracheForm class serves to switch the displayed language for the current
 * user in the running application.
 */
public class SpracheForm {

    public static final String SESSION_LOCALE_FIELD_ID = "lang";

    /**
     * The constructor of this class loads the required MessageBundle.
     */
    public SpracheForm() {
        String p = ConfigCore.getParameter("language.force-default");
        if (p != null && p.length() > 0) {
            FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(p));
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
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Locale currentDisplayLanguage = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        @SuppressWarnings("unchecked")
        // It seems we have an old Faces API, Faces 2.1’s getSupportedLocales()
        // returns Iterator<Locale>
        // TODO: Update JSF API
        Iterator<Locale> localesIterator = FacesContext.getCurrentInstance().getApplication().getSupportedLocales();
        while (localesIterator.hasNext()) {
            Locale supportedLocale = localesIterator.next();
            if (supportedLocale.getLanguage().length() > 0) {
                Map<String, Object> translation = new HashMap<String, Object>();
                translation.put("id", supportedLocale.toString());
                translation.put("displayLanguageSelf", supportedLocale.getDisplayLanguage(supportedLocale));
                translation.put("displayLanguageTranslated",
                        supportedLocale.getDisplayLanguage(currentDisplayLanguage));
                translation.put("selected", Boolean.valueOf(supportedLocale.equals(currentDisplayLanguage)));
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
    public void switchLanguage(String langCodeCombined) {
        String[] languageCode = langCodeCombined.split("_");
        Locale locale = null;
        if (languageCode.length == 2) {
            locale = new Locale(languageCode[0], languageCode[1]);
        } else {
            locale = new Locale(languageCode[0]);
        }
        FacesContext context = FacesContext.getCurrentInstance();
        context.getViewRoot().setLocale(locale);
        context.getExternalContext().getSessionMap().put(SESSION_LOCALE_FIELD_ID, locale);
    }

    /**
     * The procedure switchLanguage is called from /pages/Metadaten2oben.jsp to
     * switch the language.
     *
     * @return the empty String to point to the JSF framework to remain on the
     *         current page
     */
    public String switchLanguage() {
        String languageCodeCombined = Helper.getRequestParameter("locale");
        switchLanguage(languageCodeCombined);
        return Helper.getRequestParameter("ziel");
    }

    /**
     * Get locale.
     *
     * @return Locale object
     */
    public Locale getLocale() {
        FacesContext fac = FacesContext.getCurrentInstance();
        @SuppressWarnings("rawtypes")
        Map session = fac.getExternalContext().getSessionMap();
        UIViewRoot frame = fac.getViewRoot();
        if (session.containsKey(SESSION_LOCALE_FIELD_ID)) {
            Locale locale = (Locale) session.get(SESSION_LOCALE_FIELD_ID);
            if (frame.getLocale() != locale) {
                frame.setLocale(locale);
            }
            return locale;
        } else if (!Objects.equals(frame, null)) {
            return frame.getLocale();
        } else {
            // workaround for session object not containing 'locale' value
            return Locale.GERMAN;
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
}
