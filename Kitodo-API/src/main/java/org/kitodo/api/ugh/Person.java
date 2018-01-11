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

public interface Person extends Metadata {

    String getAuthorityURI();

    String getAuthorityValue();

    String getDisplayname();

    String getFirstname();

    String getLastname();

    String getRole();

    void setAutorityFile(String authority, String authorityURI, String valueURI);

    void setDisplayname(String string);

    void setFirstname(String trim);

    void setLastname(String trim);

    void setRole(String inRole);

}
