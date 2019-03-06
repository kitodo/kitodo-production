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

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.kitodo.config.ConfigCore;
import org.omnifaces.cdi.Eager;
import org.omnifaces.util.Ajax;
import org.omnifaces.util.Faces;

@Named("LegalTexts")
@ApplicationScoped
@Eager
public class LegalTexts implements Serializable {

    public static final String TERMS_OF_USE = "termsOfUse";
    public static final String DATA_PRIVACY = "dataPrivacy";
    public static final String IMPRINT = "imprint";

    private static String termsOfUseText = "";
    private static String dataPrivacyText = "";
    private static String imprintText = "";

    private static final String DEFAULT_LANGUAGE = Locale.GERMAN.getLanguage();

    /**
     * Get terms of use text.
     *
     * @return terms of use text
     */
    public String getTermsOfUseText() {
        return termsOfUseText;
    }

    /**
     * Set terms of use texts.
     *
     * @param newText
     *            new terms of use text
     */
    public void setTermsOfUseText(String newText) {
        termsOfUseText = newText;
    }

    /**
     * Get data privacy text.
     *
     * @return data privacy text
     */
    public String getDataPrivacyText() {
        return dataPrivacyText;
    }

    /**
     * Set data privacy text.
     *
     * @param newText
     *            new data privacy text
     */
    public void setDataPrivacyText(String newText) {
        dataPrivacyText = newText;
    }

    /**
     * Get imprint text.
     *
     * @return imprint text
     */
    public String getImprintText() {
        return imprintText;
    }

    /**
     * Set imprint text.
     *
     * @param newText
     *            new imprint text
     */
    public void setImprintText(String newText) {
        imprintText = newText;
    }

    /**
     * Initialize legal texts.
     */
    @PostConstruct
    public static void initializeTexts() {
        updateTexts(DEFAULT_LANGUAGE);
    }

    /**
     * Reload all legal texts in the given language.
     *
     * @param language
     *            language in which legal texts are loaded
     */
    public static void updateTexts(String language) {
        termsOfUseText = loadText(TERMS_OF_USE, language);
        dataPrivacyText = loadText(DATA_PRIVACY, language);
        imprintText = loadText(IMPRINT, language);
        if (Objects.nonNull(Faces.getContext())) {
            Ajax.update("imprintDialog", "dataPrivacyDialog", "termsOfUseDialog");
        }
    }

    /**
     * Load legal text file identified by given String 'legalTextName' and in the
     * given language and return its text content. If no file for the given
     * parameters can be found, a corresponding default text from the message
     * properties files will be used.
     *
     * @param legalTextName
     *            String identifying the legal text file to load
     * @param language
     *            String specifying in which language the legal text is to be loaded
     * @return the text content of the legal text
     */
    public static String loadText(String legalTextName, String language) {
        String filePath = ConfigCore.getKitodoConfigDirectory() + "legal_" + legalTextName + "_" + language + ".html";
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            return getDefaultText(legalTextName, language);
        }
    }

    private static String getDefaultText(String legalText, String language) {
        return "<p>" + Helper.getString(Locale.forLanguageTag(language), legalText + "DefaultText") + "</p><br/>"
                + "<p>" + Helper.getString(Locale.forLanguageTag(language), "adjustSettingText") + "</p>";

    }
}
