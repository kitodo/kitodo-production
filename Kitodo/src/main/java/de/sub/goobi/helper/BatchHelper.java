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

package de.sub.goobi.helper;

import java.util.List;
import java.util.Objects;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.services.ServiceManager;

public class BatchHelper {

    protected List<Property> properties;
    protected Property property;
    protected final ServiceManager serviceManager = new ServiceManager();

    /**
     * Get property for process.
     *
     * @return property for process
     */
    public Property getProperty() {
        return this.property;
    }

    /**
     * Set property for process.
     *
     * @param property
     *            for process as Property object
     */
    public void setProperty(Property property) {
        this.property = property;
    }

    /**
     * Get list of process properties.
     *
     * @return list of process properties
     */
    public List<Property> getProperties() {
        return this.properties;
    }

    /**
     * Set list of process properties.
     *
     * @param properties
     *            for process as Property objects
     */
    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    /**
     * Get size of properties' list.
     *
     * @return size of properties' list
     */
    public int getPropertiesSize() {
        return this.properties.size();
    }

    protected Process prepareProcessWithProperty(Process process, Property processProperty) {
        if (processProperty.getTitle() != null) {
            boolean match = false;
            for (Property processPe : process.getProperties()) {
                if (Objects.nonNull(processPe.getTitle()) && (processProperty.getTitle().equals(processPe.getTitle()))) {
                    processPe.setValue(processProperty.getValue());
                    match = true;
                    break;
                }
            }
            if (!match) {
                Property property = new Property();
                property.setTitle(processProperty.getTitle());
                property.setValue(processProperty.getValue());
                property.setType(processProperty.getType());
                property.getProcesses().add(process);
                process.getProperties().add(property);
            }
        }
        return process;
    }
}
