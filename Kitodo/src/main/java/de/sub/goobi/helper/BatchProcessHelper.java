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
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.properties.ProcessProperty;
import org.goobi.production.properties.PropertyParser;
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
    private List<ProcessProperty> processPropertyList;
    private ProcessProperty processProperty;
    private Map<Integer, PropertyListObject> containers = new TreeMap<>();
    private Integer container;

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
        loadProcessProperties(this.currentProcess);
    }

    public ProcessProperty getProcessProperty() {
        return this.processProperty;
    }

    public void setProcessProperty(ProcessProperty processProperty) {
        this.processProperty = processProperty;
    }

    public int getPropertyListSize() {
        return this.processPropertyList.size();
    }

    private List<String> processNameList = new ArrayList<>();

    public List<String> getProcessNameList() {
        return this.processNameList;
    }

    public void setProcessNameList(List<String> processNameList) {
        this.processNameList = processNameList;
    }

    private String processName = "";

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
                loadProcessProperties(this.currentProcess);
                break;
            }
        }
    }

    /**
     * Save current property.
     */
    public void saveCurrentProperty() {
        List<ProcessProperty> ppList = getContainerProperties();
        for (ProcessProperty pp : ppList) {
            this.processProperty = pp;
            if (!prepareProcessPropertyForTransfer()) {
                return;
            }
            this.processProperty.transfer();

            Process p = this.currentProcess;
            List<Property> propertyList = p.getProperties();
            for (Property processProperty : propertyList) {
                if (processProperty.getTitle() == null) {
                    p.getProperties().remove(processProperty);
                }
            }
            for (Process process : this.processProperty.getProzesseigenschaft().getProcesses()) {
                if (!process.getProperties().contains(this.processProperty.getProzesseigenschaft())) {
                    process.getProperties().add(this.processProperty.getProzesseigenschaft());
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
        List<ProcessProperty> ppList = getContainerProperties();
        boolean error = false;
        for (ProcessProperty pp : ppList) {
            this.processProperty = pp;
            if (!prepareProcessPropertyForTransfer()) {
                return;
            }
            this.processProperty.transfer();

            Property processProperty = new Property();
            processProperty.setTitle(this.processProperty.getName());
            processProperty.setValue(this.processProperty.getValue());
            processProperty.setContainer(this.processProperty.getContainer());

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
                    if (!process.getProperties().contains(this.processProperty.getProzesseigenschaft())) {
                        process.getProperties().add(this.processProperty.getProzesseigenschaft());
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

    private boolean prepareProcessPropertyForTransfer() {
        if (!this.processProperty.isValid()) {
            List<String> param = new ArrayList<>();
            param.add(processProperty.getName());
            String value = Helper.getTranslation("propertyNotValid", param);
            Helper.setFehlerMeldung(value);
            return false;
        }
        if (this.processProperty.getProzesseigenschaft() == null) {
            Property processProperty = new Property();
            processProperty.getProcesses().add(this.currentProcess);
            this.processProperty.setProzesseigenschaft(processProperty);
            this.currentProcess.getProperties().add(processProperty);
        }
        return true;
    }

    private void loadProcessProperties(Process process) {
        serviceManager.getProcessService().refresh(this.currentProcess);
        this.containers = new TreeMap<>();
        this.processPropertyList = PropertyParser.getPropertiesForProcess(this.currentProcess);

        for (ProcessProperty pt : this.processPropertyList) {
            if (pt.getProzesseigenschaft() == null) {
                Property processProperty = new Property();
                processProperty.getProcesses().add(process);
                pt.setProzesseigenschaft(processProperty);
                process.getProperties().add(processProperty);
                pt.transfer();
            }
            if (!this.containers.keySet().contains(pt.getContainer())) {
                PropertyListObject plo = new PropertyListObject(pt.getContainer());
                plo.addToList(pt);
                this.containers.put(pt.getContainer(), plo);
            } else {
                PropertyListObject plo = this.containers.get(pt.getContainer());
                plo.addToList(pt);
                this.containers.put(pt.getContainer(), plo);
            }
        }
        for (Process p : this.processes) {
            for (Property processProperty : p.getProperties()) {
                if (!this.containers.keySet().contains(processProperty.getContainer())) {
                    this.containers.put(processProperty.getContainer(), null);
                }
            }
        }
    }

    public Map<Integer, PropertyListObject> getContainers() {
        return this.containers;
    }

    /**
     * Get containerless properties.
     *
     * @return list of process properties
     */
    public List<ProcessProperty> getContainerlessProperties() {
        List<ProcessProperty> answer = new ArrayList<>();
        for (ProcessProperty pp : this.processPropertyList) {
            if (pp.getContainer() == 0 && pp.getName() != null) {
                answer.add(pp);
            }
        }
        return answer;
    }

    public Integer getContainer() {
        return this.container;
    }

    /**
     * Set container.
     *
     * @param container
     *            Integer
     */
    public void setContainer(Integer container) {
        this.container = container;
        if (container != null && container > 0) {
            this.processProperty = getContainerProperties().get(0);
        }
    }

    /**
     * Get container properties.
     *
     * @return list of process properties
     */
    public List<ProcessProperty> getContainerProperties() {
        List<ProcessProperty> answer = new ArrayList<>();

        if (this.container != null && this.container > 0) {
            for (ProcessProperty pp : this.processPropertyList) {
                if (pp.getContainer() == this.container && pp.getName() != null) {
                    answer.add(pp);
                }
            }
        } else {
            answer.add(this.processProperty);
        }

        return answer;
    }


}
