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
import org.kitodo.utils.Stopwatch;

@Named("ProcessEditViewWorkpiecesTab")
@ViewScoped
public class ProcessEditViewWorkpiecesTab extends BaseTabEditView<Process> {
    
    private Process process;

    private Property workpieceProperty;

    /**
     * Get list of workpieces for process.
     *
     * @return list of workpieces for process
     */
    public List<Property> getWorkpieces() {
        Stopwatch stopwatch = new Stopwatch(this, "getWorkpieces");
        return stopwatch.stop(this.process.getWorkpieces());
    }

    /**
     * Load process that is currently being edited.
     * 
     * @param process the process currently being edited
     */
    public void load(Process process) {
        this.process = process;
    }


    public Property getWorkpieceProperty() {
        Stopwatch stopwatch = new Stopwatch(this, "getWorkpieceProperty");
        return stopwatch.stop(this.workpieceProperty);
    }

    /**
     * Sets the workpiece property.
     * 
     * @param workpieceProperty
     *            workpiece property to set
     */
    public void setWorkpieceProperty(Property workpieceProperty) {
        Stopwatch stopwatch = new Stopwatch(this, "setWorkpieceProperty", "workpieceProperty", Objects.toString(
            workpieceProperty));
        this.workpieceProperty = workpieceProperty;
        stopwatch.stop();
    }

    /**
     * Remove workpiece properties.
     */
    public void deleteWorkpieceProperty() {
        final Stopwatch stopwatch = new Stopwatch(this, "deleteWorkpieceProperty");
        this.workpieceProperty.getProcesses().clear();
        this.process.getWorkpieces().remove(this.workpieceProperty);
        stopwatch.stop();
    }

    /**
     * Create new workpiece property.
     */
    public void createWorkpieceProperty() {
        final Stopwatch stopwatch = new Stopwatch(this, "createWorkpieceProperty");
        Property newProperty = new Property();
        newProperty.setDataType(PropertyType.STRING);
        this.process.getWorkpieces().add(newProperty);
        this.workpieceProperty = newProperty;
        stopwatch.stop();
    }

    /**
     * Save workpiece property.
     */
    public void saveWorkpieceProperty() {
        Stopwatch stopwatch = new Stopwatch(this, "saveWorkpieceProperty");
        if (!this.process.getWorkpieces().contains(this.workpieceProperty)) {
            this.process.getWorkpieces().add(this.workpieceProperty);
        }
        stopwatch.stop();
    }


}
