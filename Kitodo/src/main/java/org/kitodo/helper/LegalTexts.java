package org.kitodo.helper;

import de.sub.goobi.helper.Helper;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.kitodo.config.Config;
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

    public String getTermsOfUseText() {
        return termsOfUseText;
    }

    public void setTermsOfUseText(String newText) {
        termsOfUseText = newText;
    }

    public String getDataPrivacyText() {
        return dataPrivacyText;
    }

    public void setDataPrivacyText(String newText) {
        dataPrivacyText = newText;
    }

    public String getImprintText() {
        return imprintText;
    }

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
        String filePath = Config.getKitodoConfigDirectory() + "legal_" + legalTextName + "_" + language + ".html";
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            return getDefaultText(legalTextName, language);
        }
    }

    private static String getDefaultText(String legalText, String language) {
        return "<h1>" + Helper.getString(Locale.forLanguageTag(language), legalText) + "</h1><br/><p>"
                + Helper.getString(Locale.forLanguageTag(language), legalText + "DefaultText") + "</p><br/><p>"
                + Helper.getString(Locale.forLanguageTag(language), "adjustSettingText") + "</p>";

    }
}
