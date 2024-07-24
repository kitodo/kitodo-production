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
 * Acquisition stages specify a time during the acquisition.
 */
public class AcquisitionStage {
    /**
     * The name of the acquisition stage.
     */
    @XmlAttribute(required = true)
    private String name;

    /**
     * The settings valid on this stage.
     */
    @XmlElement(name = "setting", namespace = "http://names.kitodo.org/ruleset/v2", required = true)
    private List<Setting> settings = new LinkedList<>();

    /**
     * Returns the name of the acquisition stage.
     *
     * @return the name of the acquisition stage
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the settings valid on that acquisition stage.
     *
     * @return the settings valid on this stage
     */
    public List<Setting> getSettings() {
        return settings;
    }
}
