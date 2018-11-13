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

package org.kitodo.api.dataeditor.rulesetmanagement;

import java.util.Map;

/**
 * Provides an interface for the division view service. The division view
 * service provides a filtered view of a specific division. The service itself
 * is parameterized via its getter in the ruleset service.
 *
 */
public interface StructuralElementViewInterface extends ComplexMetadataViewInterface {
    /**
     * Returns which child types are allowed.
     *
     * @return which child types are allowed, as map from IDs to labels
     */
    Map<String, String> getAllowedSubstructuralElements();
}
