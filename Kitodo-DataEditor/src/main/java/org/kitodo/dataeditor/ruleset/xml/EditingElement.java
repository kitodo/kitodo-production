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
import java.util.Optional;

import javax.xml.bind.annotation.XmlElement;

/**
 * A container for the properties of the XML element {@code <editing>} in the
 * ruleset file.
 */
class EditingElement {
    /**
     * The editor settings.
     */
    @XmlElement(name = "setting", namespace = "http://names.kitodo.org/ruleset/v2")
    private List<Setting> settings = new LinkedList<>();

    /**
     * The acquisition stages.
     */
    @XmlElement(name = "acquisitionStage", namespace = "http://names.kitodo.org/ruleset/v2")
    private List<AcquisitionStage> acquisitionStages = new LinkedList<>();

    /**
     * Returns an acquisition stage by name.
     *
     * @param name
     *            name of the acquisition stage
     * @return an optional object of type AcquisitionStage
     */
    Optional<AcquisitionStage> getAcquisitionStage(String name) {
        return acquisitionStages.parallelStream().filter(acquisitionStage -> name.equals(acquisitionStage.getName()))
                .findFirst();
    }

    /**
     * Returns the acquisition stages.
     *
     * @return the acquisition stages
     */
    List<AcquisitionStage> getAcquisitionStages() {
        return acquisitionStages;
    }

    /**
     * Returns the editor settings.
     *
     * @return the editor settings
     */
    List<Setting> getSettings() {
        return settings;
    }
}
