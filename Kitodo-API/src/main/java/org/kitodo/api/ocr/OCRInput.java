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

package org.kitodo.api.ocr;

import java.util.Collection;

/**
 * The properties of the ocr processing.
 */
public class OCRInput {

    /** The process id. */
    private Integer processId;
    /** The font type. */
    private Collection<String> fontType;
    /** The used languages. */
    private Collection<String> languages;
    /** Properties for given and returned files. */
    private Collection<FileProperty> fileProperties;
    /** The type of the result xml file. */
    private String resultType;
    /**
     * Whole result should be stored in one file, or if every input gets an
     * output file.
     */
    private Boolean singleResultFile;

    /**
     * Gets the processId.
     * 
     * @return The processId.
     */
    public Integer getProcessId() {
        return processId;
    }

    /**
     * Sets the processId.
     * 
     * @param processId
     *            The processId.
     */
    public void setProcessId(Integer processId) {
        this.processId = processId;
    }

    /**
     * Gets the fontType.
     * 
     * @return The fontType.
     */
    public Collection<String> getFontType() {
        return fontType;
    }

    /**
     * Sets the fontType.
     * 
     * @param fontType
     *            The fontType.
     */
    public void setFontType(Collection<String> fontType) {
        this.fontType = fontType;
    }

    /**
     * Gets the languages.
     * 
     * @return The languages.
     */
    public Collection<String> getLanguages() {
        return languages;
    }

    /**
     * Sets the languages.
     * 
     * @param languages
     *            The languages.
     */
    public void setLanguages(Collection<String> languages) {
        this.languages = languages;
    }

    /**
     * Gets the fileProperties.
     * 
     * @return The fileProperties.
     */
    public Collection<FileProperty> getFileProperties() {
        return fileProperties;
    }

    /**
     * Sets the fileProperties.
     * 
     * @param fileProperties
     *            The fileProperties.
     */
    public void setFileProperties(Collection<FileProperty> fileProperties) {
        this.fileProperties = fileProperties;
    }

    /**
     * Gets the resultType.
     * 
     * @return The resultType.
     */
    public String getResultType() {
        return resultType;
    }

    /**
     * Sets the resultType.
     * 
     * @param resultType
     *            The resultType.
     */
    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    /**
     * Gets the singleResultfile.
     * 
     * @return The singleResultfile.
     */
    public Boolean getSingleResultFile() {
        return singleResultFile;
    }

    /**
     * Sets the singleResultfile.
     * 
     * @param singleResultFile
     *            The singleResultfile.
     */
    public void setSingleResultFile(Boolean singleResultFile) {
        this.singleResultFile = singleResultFile;
    }
}
