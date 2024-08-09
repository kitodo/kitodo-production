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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * This class is a backing bean for the XML attribute unspecified in the
 * ruleset. With it, JAXB can map the attribute to an enum.
 */
@XmlEnum(String.class)
public enum Unspecified {
    /**
     * For unspecified elements there are no further restrictions.
     */
    @XmlEnumValue("unrestricted")
    UNRESTRICTED,

    /**
     * Unspecified elements are not allowed.
     */
    @XmlEnumValue("forbidden")
    FORBIDDEN
}
