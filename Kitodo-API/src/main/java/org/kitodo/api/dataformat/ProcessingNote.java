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

package org.kitodo.api.dataformat;

/**
 * A processing note is a comment by an editor to communicate the status of
 * editing to other editors of the file.
 */
public class ProcessingNote {
    /**
     * The name of the entity causing the entry.
     */
    private String name;

    /**
     * The text of the processing note.
     */
    private String note;

    /**
     * The role of the entity causing the entry.
     */
    private String role;

    /**
     * The type of the entity causing the entry.
     */
    private String type;

    /**
     * Returns the name of the entity causing the entry.
     * 
     * @return the name of the entity causing the entry
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the entity causing the entry. This may be the name of a
     * person, an institution, the software, or similar.
     * 
     * @param name
     *            the name of the entity causing the entry
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the text of the entry.
     * 
     * @return the text of the entry
     */
    public String getNote() {
        return note;
    }

    /**
     * Sets the text of the processing note.
     * 
     * @param note
     *            the text of the processing note
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * Returns the role of the entity causing the entry.
     * 
     * @return the role of the entity causing the entry
     */
    public String getRole() {
        return role;
    }

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
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Returns the type of the entity causing the entry.
     * 
     * @return the type of the entity causing the entry
     */
    public String getType() {
        return type;
    }

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
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((note == null) ? 0 : note.hashCode());
        result = prime * result + ((role == null) ? 0 : role.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ProcessingNote other = (ProcessingNote) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (note == null) {
            if (other.note != null) {
                return false;
            }
        } else if (!note.equals(other.note)) {
            return false;
        }
        if (role == null) {
            if (other.role != null) {
                return false;
            }
        } else if (!role.equals(other.role)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }
}
