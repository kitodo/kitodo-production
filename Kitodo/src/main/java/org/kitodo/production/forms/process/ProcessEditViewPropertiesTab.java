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

package org.kitodo.production.forms.process;

import java.util.List;
import java.util.Objects;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.enums.PropertyType;
import org.kitodo.production.forms.BaseTabEditView;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.utils.Stopwatch;


@Named("ProcessEditViewPropertiesTab")
@ViewScoped
public class ProcessEditViewPropertiesTab extends BaseTabEditView<Process> {

    private final transient ProcessService processService = ServiceManager.getProcessService();
 
    private Process process;
    private Property property;

    /**
     * Get list of properties for process.
     *
     * @return list of process properties
     */
    public List<Property> getProperties() {
        Stopwatch stopwatch = new Stopwatch(this, "getProperties");
        return stopwatch.stop(this.process.getProperties());
    }

    /**
     * Get property for process.
     *
     * @return property for process
     */
    public Property getProperty() {
        Stopwatch stopwatch = new Stopwatch(this, "getProperty");
        return stopwatch.stop(this.property);
    }

    /**
     * Load process that is currently being edited.
     * 
     * @param process the process currently being edited
     */
    public void load(Process process) {
        this.process = process;
    }

    /**
     * Set property for process.
     *
     * @param property
     *            for process as Property object
     */
    public void setProperty(Property property) {
        Stopwatch stopwatch = new Stopwatch(this, "setProperty", "property", Objects.toString(property));
        this.property = property;
        stopwatch.stop();
    }

    /**
     * Create new property.
     */
    public void createNewProperty() {
        final Stopwatch stopwatch = new Stopwatch(this, "createNewProperty");
        Property newProperty = new Property();
        newProperty.setDataType(PropertyType.STRING);
        this.process.getProperties().add(newProperty);
        this.property = newProperty;
        stopwatch.stop();
    }

    /**
     * Save current property.
     */
    public void saveCurrentProperty() {
        Stopwatch stopwatch = new Stopwatch(this, "saveCurrentProperty");
        if (!this.process.getProperties().contains(this.property)) {
            this.process.getProperties().add(this.property);
        }
        stopwatch.stop();
    }

    /**
     * Delete property.
     */
    public void deleteProperty() {
        final Stopwatch stopwatch = new Stopwatch(this, "deleteProperty");
        this.property.getProcesses().clear();
        this.process.getProperties().remove(this.property);

        List<Property> propertiesToFilterTitle = this.process.getProperties();
        processService.removePropertiesWithEmptyTitle(propertiesToFilterTitle, this.process);
        stopwatch.stop();
    }

    /**
     * Duplicate property.
     */
    public void duplicateProperty() {
        final Stopwatch stopwatch = new Stopwatch(this, "duplicateProperty");
        Property newProperty = ServiceManager.getPropertyService().transfer(this.property);
        newProperty.getProcesses().add(this.process);
        this.process.getProperties().add(newProperty);
        stopwatch.stop();
    }

}
