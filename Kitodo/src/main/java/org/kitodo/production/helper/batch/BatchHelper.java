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

package org.kitodo.production.helper.batch;

import java.util.List;
import java.util.Objects;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;

public class BatchHelper {

    protected List<Property> properties;
    protected Property property;

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

    Process prepareProcessWithProperty(Process process, Property processProperty) {
        if (Objects.nonNull(processProperty.getTitle())) {
            boolean match = false;
            for (Property processPe : process.getProperties()) {
                if (Objects.nonNull(processPe.getTitle()) && processProperty.getTitle().equals(processPe.getTitle())) {
                    processPe.setValue(processProperty.getValue());
                    match = true;
                    break;
                }
            }
            if (!match) {
                Property newProperty = new Property();
                newProperty.setTitle(processProperty.getTitle());
                newProperty.setValue(processProperty.getValue());
                newProperty.setDataType(processProperty.getDataType());
                newProperty.getProcesses().add(process);
                process.getProperties().add(newProperty);
            }
        }
        return process;
    }

    void validateProperties(Process process) {
        List<Property> propertyList = process.getProperties();
        for (Property nextProcessProperty : propertyList) {
            if (Objects.isNull(nextProcessProperty.getTitle())) {
                process.getProperties().remove(nextProcessProperty);
            }
        }
    }
}
