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

package org.kitodo.api.ugh;

public interface PersonInterface extends MetadataInterface {

    String getAuthorityURI();

    String getAuthorityValue();

    String getDisplayname();

    String getFirstname();

    String getLastname();

    String getRole();

    /**
     * @return always {@code true}. The return value is never used.
     */
    boolean setAutorityFile(String authority, String authorityURI, String valueURI);

    void setDisplayname(String displayname);

    /**
     * @return always {@code true}. The return value is never used.
     */
    boolean setFirstname(String firstname);

    /**
     * @return always {@code true}. The return value is never used.
     */
    boolean setLastname(String lastname);

    /**
     * @return always {@code true}. The return value is never used.
     */
    boolean setRole(String role);

}
