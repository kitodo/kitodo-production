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

/**
 * A person is a very special kind of meta-data. For this reason it inherits
 * from the class meta-data.
 *
 * <p>
 * A person has several meta-data and not only a single value. The person's name
 * can be split into:
 *
 * <ul>
 * <li>firstname
 * <li>lastname
 * <li>affilication (company they are working at…)
 * <li>a role, when creating or producing a book, article etc.; usually this is
 * a MetadataType.
 * </ul>
 *
 * <p>
 * This class provides methods to store and retrieve these additional
 * information.
 *
 * <p>
 * Most file formats are not able to serialize / store all information from a
 * person object.
 */
public interface PersonInterface extends MetadataInterface {

    /**
     * Returns the authority URI of the person.
     *
     * @return the authority URI
     */
    String getAuthorityURI();

    /**
     * Returns the authority value of the person.
     *
     * @return the authority value
     */
    String getAuthorityValue();

    /**
     * Returns the display name of the person.
     *
     * @return the display name
     */
    String getDisplayName();

    /**
     * Returns the first name of the person.
     *
     * @return the first name
     */
    String getFirstName();

    /**
     * Returns the last name of the person.
     *
     * @return the last name
     */
    String getLastName();

    /**
     * Returns the role of the person.
     *
     * @return the role
     */
    String getRole();

    /**
     * Sets the authority file of the person.
     *
     * @param authority
     *            the authority
     * @param authorityURI
     *            the authority URI
     * @param valueURI
     *            the value URI
     */
    void setAutorityFile(String authority, String authorityURI, String valueURI);

    /**
     * Sets the display name of the person.
     *
     * @param displayName
     *            the display name
     */
    void setDisplayName(String displayName);

    /**
     * Sets the first name of the person.
     *
     * @param firstName
     *            the first name
     */
    void setFirstName(String firstName);

    /**
     * Sets the last name of the person.
     *
     * @param lastName
     *            the last name
     */
    void setLastName(String lastName);

    /**
     * Sets the role of the person.
     *
     * @param role
     *            the role
     */
    void setRole(String role);

}
