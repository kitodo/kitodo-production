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

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * A possible option as a member of an enumeration type.
 */
public class Option {
    /**
     * The string value.
     */
    @XmlAttribute(required = true)
    private String value;

    /**
     * Optional labels.
     */
    @XmlElement(name = "label", namespace = "http://names.kitodo.org/ruleset/v2")
    private List<Label> labels = new LinkedList<>();

    /**
     * Return function for labels.
     * 
     * @return labels
     */
    public List<Label> getLabels() {
        return labels;
    }

    /**
     * Return function for the value.
     * 
     * @return the value
     */
    public String getValue() {
        return value;
    }

}
