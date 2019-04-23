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

package org.kitodo.api.rulesetreader;

import java.util.Collection;
import java.util.List;
import java.util.Locale.LanguageRange;

import org.kitodo.api.Metadata;

/** This data is required to get information from the ruleset. */
public class RulesetRequestData {

    /** The division to look in. */
    private String division;

    /** The already existing metadata. */
    private Collection<Metadata> exisitingMetadata;

    /** The preferred languages. */
    private List<LanguageRange> languages;

    /** The current acquisition stage. */
    private String acquisitionStage;

    /**
     * Get division.
     *
     * @return value of division
     */
    public String getDivision() {
        return division;
    }

    /**
     * Set division.
     *
     * @param division
     *            as java.lang.String
     */
    public void setDivision(String division) {
        this.division = division;
    }

    /**
     * Get exisitingMetadata.
     *
     * @return value of exisitingMetadata
     */
    public Collection<Metadata> getExisitingMetadata() {
        return exisitingMetadata;
    }

    /**
     * Set exisitingMetadata.
     *
     * @param exisitingMetadata
     *            as java.util.Collection of Metadata
     */
    public void setExisitingMetadata(Collection<Metadata> exisitingMetadata) {
        this.exisitingMetadata = exisitingMetadata;
    }

    /**
     * Get languages.
     *
     * @return value of language
     */
    public List<LanguageRange> getLanguages() {
        return languages;
    }

    /**
     * Set languages.
     *
     * @param languages
     *            as java.util.Locale.LanguageRange
     */
    public void setLanguages(List<LanguageRange> languages) {
        this.languages = languages;
    }

    /**
     * Get acquisitionStage.
     *
     * @return value of acquisitionStage
     */
    public String getAcquisitionStage() {
        return acquisitionStage;
    }

    /**
     * Set acquisitionStage.
     *
     * @param acquisitionStage
     *            as java.lang.String
     */
    public void setAcquisitionStage(String acquisitionStage) {
        this.acquisitionStage = acquisitionStage;
    }
}
