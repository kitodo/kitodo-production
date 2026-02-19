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

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.enums.PropertyType;
import org.kitodo.production.forms.BaseTabEditView;
import org.kitodo.utils.Stopwatch;


@Named("ProcessEditViewTemplatesTab")
@ViewScoped
public class ProcessEditViewTemplatesTab extends BaseTabEditView<Process> {
    
    private Process process;
    private Property templateProperty;

    /**
     * Get list of templates for process.
     *
     * @return list of templates for process
     */
    public List<Property> getTemplates() {
        Stopwatch stopwatch = new Stopwatch(this, "getTemplates");
        return stopwatch.stop(this.process.getTemplates());
    }

    public Property getTemplateProperty() {
        Stopwatch stopwatch = new Stopwatch(this, "getTemplateProperty");
        return stopwatch.stop(this.templateProperty);
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
     * Save template property.
     */
    public void saveTemplateProperty() {
        Stopwatch stopwatch = new Stopwatch(this, "saveTemplateProperty");
        if (!this.process.getTemplates().contains(this.templateProperty)) {
            this.process.getTemplates().add(this.templateProperty);
        }
        stopwatch.stop();
    }

    /**
     * Create new template property.
     */
    public void createTemplateProperty() {
        final Stopwatch stopwatch = new Stopwatch(this, "createTemplateProperty");
        Property newProperty = new Property();
        newProperty.setDataType(PropertyType.STRING);
        this.process.getTemplates().add(newProperty);
        this.templateProperty = newProperty;
        stopwatch.stop();
    }

    /**
     * Remove template properties.
     */
    public void deleteTemplateProperty() {
        final Stopwatch stopwatch = new Stopwatch(this, "deleteTemplateProperty");
        this.templateProperty.getProcesses().clear();
        this.process.getTemplates().remove(this.templateProperty);
        stopwatch.stop();
    }

}
