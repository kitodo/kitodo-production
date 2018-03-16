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

package org.kitodo.api.persistentidentifier.doi;

public interface DigitalObjectIdentifierInterface {

    /**
     * Generates a DOI for the given list of parameters.
     *
     * @param parameters
     *            list of String parameters for DOI generation
     * @return a valid DOI
     */
    String generate(String... parameters);
}
