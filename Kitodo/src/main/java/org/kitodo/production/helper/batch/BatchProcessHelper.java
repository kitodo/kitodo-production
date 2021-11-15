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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

public class BatchProcessHelper extends BatchHelper {
    private final List<Process> processes;
    private final Batch batch;
    private static final Logger logger = LogManager.getLogger(BatchProcessHelper.class);
    private Process currentProcess;

    /**
     * Constructor.
     *
     * @param batch
     *            object
     */
    public BatchProcessHelper(Batch batch) {
        this.batch = batch;
        this.processes = batch.getProcesses();
        this.currentProcess = processes.iterator().next();
        loadProcessProperties();
    }

    /**
     * Get batch.
     *
     * @return value of batch
     */
    public Batch getBatch() {
        return batch;
    }

    /**
     * Get current process.
     *
     * @return value of currentProcess
     */
    public Process getCurrentProcess() {
        return currentProcess;
    }

    /**
     * Set current process.
     *
     * @param currentProcess
     *            as Process
     */
    public void setCurrentProcess(Process currentProcess) {
        this.currentProcess = currentProcess;
    }

    /**
     * Get list of processes.
     *
     * @return list of processes
     */
    public List<Process> getProcesses() {
        return this.processes;
    }

    /**
     * Update list of properties for chosen process.
     *
     * @param processId
     *            id of chosen process
     */
    public void updatePropertyList(int processId) {
        for (Process process : this.processes) {
            if (process.getId().equals(processId)) {
                this.currentProcess = process;
                loadProcessProperties();
            }
        }
    }

    /**
     * Save property for chosen process.
     */
    public void saveForChosenProcess() {
        try {
            ServiceManager.getProcessService().save(this.currentProcess);
            Helper.setMessage("propertySaved");
        } catch (DataException e) {
            Helper.setErrorMessage("errorSaving", new Object[] {ObjectType.PROPERTY.getTranslationSingular() }, logger,
                e);
        }
    }

    /**
     * Save property for all processes belonging to the batch.
     */
    public void saveForAllProcesses() {
        for (Process process : this.processes) {
            try {
                ServiceManager.getProcessService().save(process);
                Helper.setMessage("propertiesSaved");
            } catch (DataException e) {
                String value = Helper.getTranslation("propertiesForProcessNotSaved", process.getTitle());
                Helper.setErrorMessage(value, logger, e);
            }
        }
    }

    /**
     * Edit current property for chosen process.
     */
    public void editPropertyForOneProcess() {
        List<Property> ppList = getProperties();
        for (Property pp : ppList) {
            this.property = pp;

            List<Property> propertyList = this.currentProcess.getProperties();
            for (Property processProperty : propertyList) {
                if (Objects.isNull(processProperty.getTitle())) {
                    this.currentProcess.getProperties().remove(processProperty);
                }
            }
            for (Process process : this.property.getProcesses()) {
                if (!process.getProperties().contains(this.property)) {
                    process.getProperties().add(this.property);
                }
            }
        }
    }

    /**
     * Edit current property for all processes belonging to the batch.
     */
    public void editPropertyForAllProcesses() {
        List<Property> ppList = getProperties();

        for (Property pp : ppList) {
            this.property = pp;

            Property processProperty = new Property();
            processProperty.setTitle(this.property.getTitle());
            processProperty.setValue(this.property.getValue());

            for (Process process : this.processes) {
                if (!process.equals(this.currentProcess)) {
                    process = prepareProcessWithProperty(process, processProperty);
                } else if (!process.getProperties().contains(this.property)) {
                    process.getProperties().add(this.property);
                }

                validateProperties(process);
            }
        }
    }

    /**
     * Delete property for one process.
     */
    public void deletePropertyForOneProcess() {
        this.property.getProcesses().clear();
        this.currentProcess.getProperties().remove(this.property);

        loadProcessProperties();
    }

    /**
     * Delete property for all processes belonging to the batch.
     */
    public void deletePropertyForAllProcesses() {
        this.property.getProcesses().clear();
        this.currentProcess.getProperties().remove(this.property);

        for (Process process : this.processes) {
            for (Property property : process.getProperties()) {
                if (property.getTitle().equals(this.property.getTitle())
                        && property.getValue().equals(this.property.getValue())) {
                    property.getProcesses().clear();
                    process.getProperties().remove(property);
                }
            }
        }

        loadProcessProperties();
    }

    private void loadProcessProperties() {
        this.properties = this.currentProcess.getProperties();
    }
}
