/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kitodo.dataeditor.ruleset.xml;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * A container for the properties of the XML element {@code <codomain>} in the
 * ruleset file.
 */
class CodomainElement {
    /**
     * The type of the codomain.
     */
    @XmlAttribute
    private Type type;

    /**
     * Minimum number of digits with type {@code integer}. Must be a
     * positive integer.
     */
    @XmlAttribute
    private Integer minDigits;

    /**
     * The name space for URIs.
     */
    @XmlAttribute
    private String namespace;

    /**
     * Returns the minimum number of digits for integer values.
     *
     * @return the minimum number of digits
     */
    int getMinDigits() {
        return Objects.isNull(minDigits) ? 1 : minDigits;
    }
    
    /**
     * Returns the name space for URIs.
     *
     * @return the name space for URIs
     */
    String getNamespace() {
        return namespace;
    }

    /**
     * Returns the type of the codomain.
     *
     * @return the type of the codomain
     */
    Type getType() {
        return type;
    }

    /**
     * Sets the type of the codomain.
     *
     * @param type
     *            the type of the codomain
     */
    void setType(Type type) {
        this.type = type;
    }

}
