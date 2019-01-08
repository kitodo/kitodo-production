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

package org.kitodo.production.editor;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.kitodo.config.ConfigCore;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.LegalTexts;

@Named("LegalTextEditor")
@ViewScoped
public class LegalTextEditor implements Serializable {

    private static List<String> legalTextTitles;
    private String currentLegalTextTitle;
    private String currentLegalTextContent;
    private List<Locale> availableLocales = new ArrayList<>();
    private String currentLanguage;

    /**
     * Default constructor.
     */
    public LegalTextEditor() {
        legalTextTitles = new LinkedList<>();

        legalTextTitles.add(LegalTexts.TERMS_OF_USE);
        legalTextTitles.add(LegalTexts.DATA_PRIVACY);
        legalTextTitles.add(LegalTexts.IMPRINT);

        currentLegalTextTitle = legalTextTitles.get(0);

        FacesContext.getCurrentInstance().getApplication().getSupportedLocales()
                .forEachRemaining(availableLocales::add);
        if (!availableLocales.isEmpty()) {
            currentLanguage = availableLocales.get(0).getLanguage();
        }
        loadText();
    }

    /**
     * Load currently selected legal file and set it's text content to
     * 'currentLegalTextContent'.
     */
    private void loadText() {
        currentLegalTextContent = LegalTexts.loadText(this.currentLegalTextTitle, this.currentLanguage);
    }

    /**
     * Save text of currently selected legal text to file.
     */
    public void saveText() {
        String filePath = ConfigCore.getKitodoConfigDirectory() + "legal_" + this.currentLegalTextTitle + "_"
                + this.currentLanguage + ".html";
        try {
            Files.write(Paths.get(filePath), this.currentLegalTextContent.getBytes());
            LegalTexts.updateTexts(this.currentLanguage);
        } catch (IOException e) {
            Helper.setErrorMessage("ERROR: unable to save file '" + filePath + "'!");
        }
    }

    /**
     * Return list of legal texts.
     *
     * @return list of legal texts
     */
    public List<String> getLegalTextTitles() {
        return legalTextTitles;
    }

    /**
     * Return current legal text.
     *
     * @return current legal text
     */
    public String getCurrentLegalTextTitle() {
        return this.currentLegalTextTitle;
    }

    /**
     * Set current legal text.
     *
     * @param text
     *            current legal text
     */
    public void setCurrentLegalTextTitle(String text) {
        if (!Objects.equals(text, this.currentLegalTextTitle)) {
            this.currentLegalTextTitle = text;
            loadText();
        }
    }

    /**
     * Return current legal text content as String.
     *
     * @return current legal text content as String
     */
    public String getCurrentLegalTextContent() {
        return currentLegalTextContent;
    }

    /**
     * Set current legal text content to given String 'textString'.
     *
     * @param textString
     *            current legal text String
     */
    public void setCurrentLegalTextContent(String textString) {
        this.currentLegalTextContent = textString;
    }

    /**
     * Get list of available locales.
     *
     * @return list of available locales
     */
    public List<Locale> getAvailableLocales() {
        return availableLocales;
    }

    /**
     * Get language currently selected in the editor.
     *
     * @return language currently selected in the editor
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }

    /**
     * Set current language.
     *
     * @param language
     *            new current language
     */
    public void setCurrentLanguage(String language) {
        if (!Objects.equals(language, this.currentLanguage)) {
            this.currentLanguage = language;
            loadText();
        }
    }
}
