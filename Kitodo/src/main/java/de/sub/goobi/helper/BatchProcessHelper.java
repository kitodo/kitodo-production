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
import org.kitodo.services.ServiceManager;

public class BatchProcessHelper {
    private final List<Process> processes;
    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(BatchProcessHelper.class);
    private Process currentProcess;
    private List<Property> properties;
    private Property property;
    private String processName;

    /**
     * Constructor.
     *
     * @param batch
     *            object
     */
    public BatchProcessHelper(Batch batch) {
        this.processes = batch.getProcesses();
        for (Process p : processes) {
            this.processNameList.add(p.getTitle());
        }
        this.currentProcess = processes.iterator().next();
        this.processName = this.currentProcess.getTitle();
        loadProcessProperties();
    }

    public Property getProperty() {
        return this.property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    /**
     * Get properties.
     *
     * @return list of process properties
     */
    public List<Property> getProperties() {
        return this.properties;
    }

    public int getPropertiesSize() {
        return this.properties.size();
    }

    private List<String> processNameList = new ArrayList<>();

    public List<String> getProcessNameList() {
        return this.processNameList;
    }

    public void setProcessNameList(List<String> processNameList) {
        this.processNameList = processNameList;
    }

    public String getProcessName() {
        return this.processName;
    }

    /**
     * Set process name.
     *
     * @param processName
     *            String
     */
    public void setProcessName(String processName) {
        this.processName = processName;
        for (Process s : this.processes) {
            if (s.getTitle().equals(processName)) {
                this.currentProcess = s;
                loadProcessProperties();
                break;
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
                Helper.setMeldung("propertySaved");
            } catch (DataException e) {
                logger.error(e);
                Helper.setFehlerMeldung("propertyNotSaved");
            }
        }
    }

    /**
     * Save current property for all.
     */
    public void saveCurrentPropertyForAll() {
        List<Property> ppList = getProperties();
        boolean error = false;
        for (Property pp : ppList) {
            this.property = pp;

            Property processProperty = new Property();
            processProperty.setTitle(this.property.getTitle());
            processProperty.setValue(this.property.getValue());
            processProperty.setContainer(this.property.getContainer());

            for (Process process : this.processes) {
                if (!process.equals(this.currentProcess)) {
                    if (processProperty.getTitle() != null) {
                        boolean match = false;
                        for (Property processPe : process.getProperties()) {
                            if (processPe.getTitle() != null) {
                                if (processProperty.getTitle().equals(processPe.getTitle())
                                        && processProperty.getContainer() == null ? processPe.getContainer() == null
                                                : processProperty.getContainer().equals(processPe.getContainer())) {
                                    processPe.setValue(processProperty.getValue());
                                    match = true;
                                    break;
                                }
                            }
                        }
                        if (!match) {
                            Property newProcessProperty = new Property();
                            newProcessProperty.setTitle(processProperty.getTitle());
                            newProcessProperty.setValue(processProperty.getValue());
                            newProcessProperty.setContainer(processProperty.getContainer());
                            newProcessProperty.setType(processProperty.getType());
                            newProcessProperty.getProcesses().add(process);
                            process.getProperties().add(newProcessProperty);
                        }
                    }
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
                } catch (DataException e) {
                    error = true;
                    logger.error(e);
                    List<String> param = new ArrayList<>();
                    param.add(process.getTitle());
                    String value = Helper.getTranslation("propertiesForProcessNotSaved", param);
                    Helper.setFehlerMeldung(value);
                }
            }
        }
        if (!error) {
            Helper.setMeldung("propertiesSaved");
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
