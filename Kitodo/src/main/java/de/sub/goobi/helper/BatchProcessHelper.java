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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.enums.ObjectType;

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
     * Save current property.
     */
    public void saveCurrentProperty() {
        List<Property> ppList = getProperties();
        for (Property pp : ppList) {
            this.property = pp;

            Process currentProcess = this.currentProcess;
            List<Property> propertyList = currentProcess.getProperties();
            for (Property processProperty : propertyList) {
                if (processProperty.getTitle() == null) {
                    currentProcess.getProperties().remove(processProperty);
                }
            }
            for (Process process : this.property.getProcesses()) {
                if (!process.getProperties().contains(this.property)) {
                    process.getProperties().add(this.property);
                }
            }
            try {
                serviceManager.getProcessService().save(this.currentProcess);
                Helper.setMessage("propertySaved");
            } catch (DataException e) {
                Helper.setErrorMessage("errorSaving", new Object[] {ObjectType.PROPERTY.getTranslationSingular() },
                    logger, e);
            }
        }
    }

    /**
     * Save current property for all.
     */
    public void saveCurrentPropertyForAll() {
        List<Property> ppList = getProperties();

        for (Property pp : ppList) {
            this.property = pp;

            Property processProperty = new Property();
            processProperty.setTitle(this.property.getTitle());
            processProperty.setValue(this.property.getValue());

            for (Process process : this.processes) {
                if (!process.equals(this.currentProcess)) {
                    process = prepareProcessWithProperty(process, processProperty);
                } else {
                    if (!process.getProperties().contains(this.property)) {
                        process.getProperties().add(this.property);
                    }
                }

                List<Property> propertyList = process.getProperties();
                for (Property nextProcessProperty : propertyList) {
                    if (nextProcessProperty.getTitle() == null) {
                        process.getProperties().remove(nextProcessProperty);
                    }
                }

                try {
                    serviceManager.getProcessService().save(process);
                    Helper.setMessage("propertiesSaved");
                } catch (DataException e) {
                    List<String> param = new ArrayList<>();
                    param.add(process.getTitle());
                    String value = Helper.getTranslation("propertiesForProcessNotSaved", param);
                    Helper.setErrorMessage(value, logger, e);
                }
            }
        }
    }

    private void loadProcessProperties() {
        serviceManager.getProcessService().refresh(this.currentProcess);
        this.properties = this.currentProcess.getProperties();

        for (Process process : this.processes) {
            serviceManager.getProcessService().refresh(process);
        }
    }
}
