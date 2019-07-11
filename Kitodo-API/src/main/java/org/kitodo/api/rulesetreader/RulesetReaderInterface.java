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
import java.util.Map;

import org.kitodo.api.Metadata;

/** This interface is to access the ruleset and acquire information from it. */
public interface RulesetReaderInterface {

    /**
     * Get all possible divisions from the ruleset.
     *
     * @param languages
     *            the preferred languages for translation
     * @return a {@link Map} collection of all divisions that contains
     *         {@link String} as id and {@link String} as translation
     */
    Map<String, String> getAllDivisions(List<LanguageRange> languages);

    /**
     * Get all possible acquisition stages.
     *
     * @return A Collection of all acquisition stages
     */
    Collection<String> getAllAcquisitionStages();

    /**
     * Get all metadata, which can be added for a specific division.
     * 
     * @param rulesetRequestData
     *            The needed data for retrieval.
     * @return A {@link Map} collection of addable metadata that contains
     *         {@link String} as id and {@link String} as translation
     */
    Map<String, String> getAddableMetadataForDivision(RulesetRequestData rulesetRequestData);

    /**
     * Gets the sorted visible metadata.
     * 
     * @param rulesetRequestData
     *            The needed data for retrieval.
     * @return a Collection of Metadata.
     */
    Collection<Metadata> getSortedVisibleMetadataForDivision(RulesetRequestData rulesetRequestData);

    /**
     * Gets options for metadata.
     * 
     * @param metadata
     *            The Metadata to get options for.
     * @return A {@link Map} collection of options that contains {@link String}
     *         as value and {@link String} as label
     */
    Map<String, String> getOptionsForMetadata(Metadata metadata);

    /**
     * Gets a translation for metadata.
     * 
     * @param metadata
     *            the metadata to be translated.
     * @param languages
     *            the goal language for translation
     * @return A translated string.
     */
    String getTranslationForMetadata(Metadata metadata, List<LanguageRange> languages);

}
