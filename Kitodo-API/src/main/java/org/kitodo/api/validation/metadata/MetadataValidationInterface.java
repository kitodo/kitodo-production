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

package org.kitodo.api.validation.metadata;

import java.net.URI;
import java.util.List;
import java.util.Locale.LanguageRange;
import java.util.Map;

import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.api.validation.ValidationInterface;
import org.kitodo.api.validation.ValidationResult;

/** Validates a metadata file against a given ruleset. */
public interface MetadataValidationInterface extends ValidationInterface {

    /**
     * Validates if a mets file is confirm to a rulesetFile.
     *
     * @param metsFileUri
     *            The uri to the mets file which should be validated.
     * @param rulesetFileUri
     *            The uri to the ruleset file to validate against.
     * @param metadataLanguage
     *            The list of languages preferred by the requesting user to
     *            display the metadata labels
     * @param translations
     *            A map containing the validation error messages translated into
     *            the requesting user’s language. The map must contain the
     *            following entries: {@code metadataInvalidData},
     *            {@code metadataMandatoryElement}, {@code metadataMediaError},
     *            {@code metadataMediaUnassigned},
     *            {@code metadataNotEnoughElements},
     *            {@code metadataNotOneElement}, and
     *            {@code metadataStructureWithoutMedia}.
     * @return A validation result.
     */
    ValidationResult validate(URI metsFileUri, URI rulesetFileUri, List<LanguageRange> metadataLanguage,
            Map<String, String> translations);

    /**
     * Validates if a workpiece is confirm to a ruleset.
     *
     * @param workpiece
     *            The workpiece which should be validated.
     * @param ruleset
     *            The ruleset to validate against.
     * @param metadataLanguage
     *            The list of languages preferred by the requesting user to
     *            display the metadata labels
     * @param translations
     *            A map containing the validation error messages translated into
     *            the requesting user’s language. The map must contain the
     *            following entries: {@code metadataInvalidData},
     *            {@code metadataMandatoryElement}, {@code metadataMediaError},
     *            {@code metadataMediaUnassigned},
     *            {@code metadataNotEnoughElements},
     *            {@code metadataNotOneElement}, and
     *            {@code metadataStructureWithoutMedia}.
     * @param checkMedia
     *            whether to check for missing or unlinked media
     * @return A validation result.
     */
    ValidationResult validate(Workpiece workpiece, RulesetManagementInterface ruleset,
            List<LanguageRange> metadataLanguage, Map<String, String> translations, boolean checkMedia);
}
