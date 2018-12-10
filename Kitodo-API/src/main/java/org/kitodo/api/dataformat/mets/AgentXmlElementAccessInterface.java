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

package org.kitodo.api.dataformat.mets;

public interface AgentXmlElementAccessInterface {
    /**
     * Returns the name of the entity causing the entry.
     * 
     * @return the name of the entity causing the entry
     */
    String getName();

    /**
     * Returns the text of the entry.
     * 
     * @return the text of the entry
     */
    String getNote();

    /**
     * Returns the role of the entity causing the entry.
     * 
     * @return the role of the entity causing the entry
     */
    String getRole();

    /**
     * Returns the type of the entity causing the entry.
     * 
     * @return the type of the entity causing the entry
     */
    String getType();

    /**
     * Sets the name of the entity causing the entry. This may be the name of a
     * person, an institution, the software, or similar.
     * 
     * @param name
     *            the name of the entity causing the entry
     */
    void setName(String name);

    /**
     * Sets the text of the processing note.
     * 
     * @param note
     *            the text of the processing note
     */
    void setNote(String note);

    /**
     * Sets the role of the entity causing the edit comment. If possible, it
     * should be one of the following strings that have semantics in METS: (It
     * can also be other text.)
     * <ul>
     * <li>{@code CREATOR} − a person or institution responsible for the METS
     * document
     * <li>{@code EDITOR} − a person or institution that prepares the meta-data
     * for encoding
     * <li>{@code ARCHIVIST} − a person institution responsible for the
     * document/collection
     * <li>{@code PRESERVATION} − a person or institution responsible for
     * preservation functions
     * <li>{@code DISSEMINATOR} − a person or institution responsible for
     * dissemination functions
     * <li>{@code CUSTODIAN} − a person or institution charged with the
     * oversight of a document/collection
     * <li>{@code IPOWNER} − <i>Intellectual Property Owner</i>: a person or
     * institution holding copyright, trade or service marks or other
     * intellectual property rights for the object
     * </ul>
     * 
     * @param role
     *            role to set
     */
    void setRole(String role);

    /**
     * Sets the type of the entity causing the edit comment. If possible, it
     * should be one of the following strings that have semantics in METS: (It
     * can also be other text.)
     * <ul>
     * <li>{@code INDIVIDUAL} − if an individual has served as the agent
     * <li>{@code ORGANIZATION} − if an institution, corporate body,
     * association, non-profit enterprise, government, religious body, etc. has
     * served as the agent
     * </ul>
     * 
     * @param type
     *            type to set
     */
    void setType(String type);
}
