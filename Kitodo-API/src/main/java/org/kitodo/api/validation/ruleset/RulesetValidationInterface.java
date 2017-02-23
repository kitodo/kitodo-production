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

package org.kitodo.api.validation.ruleset;

import org.kitodo.api.validation.ValidationInterface;
import org.kitodo.api.validation.ValidationResult;

import java.io.File;
import java.nio.file.Path;

public interface RulesetValidationInterface extends ValidationInterface {

    /**
     * Validates if a rulesetfile is valid.
     *
     * @param rulesetFilePath The path to the rulesetfile, which should be validated.
     * @return A validation result.
     */
    ValidationResult validate(Path rulesetFilePath);

}
