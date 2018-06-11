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

package org.kitodo.api.persistentidentifier;

/** Handles persistent Identifiers. */
public interface PersistentIdentifierGeneratorInterface {

    /**
     * Registers the given identifier.
     *
     * @param urn
     *            the identifier to register.
     * @return true, if successful, false otherwise.
     * @throws UnsupportedOperationException
     *             if the register operation is not supported by this generator
     */
    boolean register(String urn);

}
