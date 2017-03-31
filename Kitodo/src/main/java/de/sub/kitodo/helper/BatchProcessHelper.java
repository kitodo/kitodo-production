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

package de.sub.kitodo.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.ResponseException;
import org.kitodo.production.properties.ProcessProperty;
import org.kitodo.production.properties.PropertyParser;
import org.kitodo.services.ServiceManager;

public class BatchProcessHelper {

    private final List<Process> processes;
    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = Logger.getLogger(BatchProcessHelper.class);
    private Process currentProcess;
    private List<ProcessProperty> processPropertyList;
    private ProcessProperty processProperty;
    private Map<Integer, PropertyListObject> containers = new TreeMap<Integer, PropertyListObject>();
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

    public Process getCurrentProcess() {
        return this.currentProcess;
    }

    public void setCurrentProcess(Process currentProcess) {
        this.currentProcess = currentProcess;
    }

    public List<ProcessProperty> getProcessPropertyList() {
        return this.processPropertyList;
    }

    public void setProcessPropertyList(List<ProcessProperty> processPropertyList) {
        this.processPropertyList = processPropertyList;
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

    public List<ProcessProperty> getProcessProperties() {
        return this.processPropertyList;
    }

    private List<String> processNameList = new ArrayList<String>();

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
    public void saveCurrentProperty() throws IOException, ResponseException {
        List<ProcessProperty> ppList = getContainerProperties();
        for (ProcessProperty pp : ppList) {
            this.processProperty = pp;
            if (!this.processProperty.isValid()) {
                List<String> param = new ArrayList<String>();
                param.add(processProperty.getName());
                String value = Helper.getTranslation("propertyNotValid", param);
                Helper.setFehlerMeldung(value);
                return;
            }
            if (this.processProperty.getProzesseigenschaft() == null) {
                org.kitodo.data.database.beans.ProcessProperty pe = new org.kitodo.data.database.beans.ProcessProperty();
                pe.setProcess(this.currentProcess);
                this.processProperty.setProzesseigenschaft(pe);
                serviceManager.getProcessService().getPropertiesInitialized(this.currentProcess).add(pe);
            }
            this.processProperty.transfer();

            Process p = this.currentProcess;
            List<org.kitodo.data.database.beans.ProcessProperty> props = p.getProperties();
            for (org.kitodo.data.database.beans.ProcessProperty pe : props) {
                if (pe.getTitle() == null) {
                    serviceManager.getProcessService().getPropertiesInitialized(p).remove(pe);
                }
            }
            if (!serviceManager.getProcessService()
                    .getPropertiesInitialized(this.processProperty.getProzesseigenschaft().getProcess())
                    .contains(this.processProperty.getProzesseigenschaft())) {
                serviceManager.getProcessService()
                        .getPropertiesInitialized(this.processProperty.getProzesseigenschaft().getProcess())
                        .add(this.processProperty.getProzesseigenschaft());
            }
            try {
                serviceManager.getProcessService().save(this.currentProcess);
                Helper.setMeldung("propertySaved");
            } catch (DAOException e) {
                logger.error(e);
                Helper.setFehlerMeldung("propertyNotSaved");
            }
        }
    }

    /**
     * Save current property for all.
     */
    public void saveCurrentPropertyForAll() throws IOException, ResponseException {
        List<ProcessProperty> ppList = getContainerProperties();
        boolean error = false;
        for (ProcessProperty pp : ppList) {
            this.processProperty = pp;
            if (!this.processProperty.isValid()) {
                List<String> param = new ArrayList<>();
                param.add(processProperty.getName());
                String value = Helper.getTranslation("propertyNotValid", param);
                Helper.setFehlerMeldung(value);
                return;
            }
            if (this.processProperty.getProzesseigenschaft() == null) {
                org.kitodo.data.database.beans.ProcessProperty pe = new org.kitodo.data.database.beans.ProcessProperty();
                pe.setProcess(this.currentProcess);
                this.processProperty.setProzesseigenschaft(pe);
                serviceManager.getProcessService().getPropertiesInitialized(this.currentProcess).add(pe);
            }
            this.processProperty.transfer();

            org.kitodo.data.database.beans.ProcessProperty pe = new org.kitodo.data.database.beans.ProcessProperty();
            pe.setTitle(this.processProperty.getName());
            pe.setValue(this.processProperty.getValue());
            pe.setContainer(this.processProperty.getContainer());

            for (Process s : this.processes) {
                Process process = s;
                if (!s.equals(this.currentProcess)) {
                    if (pe.getTitle() != null) {
                        boolean match = false;
                        for (org.kitodo.data.database.beans.ProcessProperty processPe : process.getProperties()) {
                            if (processPe.getTitle() != null) {
                                if (pe.getTitle().equals(processPe.getTitle()) && pe.getContainer() == null
                                        ? processPe.getContainer() == null
                                        : pe.getContainer().equals(processPe.getContainer())) {
                                    processPe.setValue(pe.getValue());
                                    match = true;
                                    break;
                                }
                            }
                        }
                        if (!match) {
                            org.kitodo.data.database.beans.ProcessProperty p = new org.kitodo.data.database.beans.ProcessProperty();
                            p.setTitle(pe.getTitle());
                            p.setValue(pe.getValue());
                            p.setContainer(pe.getContainer());
                            p.setType(pe.getType());
                            p.setProcess(process);
                            serviceManager.getProcessService().getPropertiesInitialized(process).add(p);
                        }
                    }
                } else {
                    if (!serviceManager.getProcessService().getPropertiesInitialized(process)
                            .contains(this.processProperty.getProzesseigenschaft())) {
                        serviceManager.getProcessService().getPropertiesInitialized(process)
                                .add(this.processProperty.getProzesseigenschaft());
                    }
                }

                List<org.kitodo.data.database.beans.ProcessProperty> props = process.getProperties();
                for (org.kitodo.data.database.beans.ProcessProperty peig : props) {
                    if (peig.getTitle() == null) {
                        serviceManager.getProcessService().getPropertiesInitialized(process).remove(peig);
                    }
                }

                try {
                    serviceManager.getProcessService().save(process);
                } catch (DAOException e) {
                    error = true;
                    logger.error(e);
                    List<String> param = new ArrayList<String>();
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

    private void loadProcessProperties(Process process) {
        serviceManager.getProcessService().refresh(this.currentProcess);
        this.containers = new TreeMap<Integer, PropertyListObject>();
        this.processPropertyList = PropertyParser.getPropertiesForProcess(this.currentProcess);

        for (ProcessProperty pt : this.processPropertyList) {
            if (pt.getProzesseigenschaft() == null) {
                org.kitodo.data.database.beans.ProcessProperty pe = new org.kitodo.data.database.beans.ProcessProperty();
                pe.setProcess(process);
                pt.setProzesseigenschaft(pe);
                serviceManager.getProcessService().getPropertiesInitialized(process).add(pe);
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
            for (org.kitodo.data.database.beans.ProcessProperty pe : p.getProperties()) {
                if (!this.containers.keySet().contains(pe.getContainer())) {
                    this.containers.put(pe.getContainer(), null);
                }
            }
        }
    }

    public Map<Integer, PropertyListObject> getContainers() {
        return this.containers;
    }

    /**
     * Get containers size.
     *
     * @return size
     */
    public int getContainersSize() {
        if (this.containers == null) {
            return 0;
        }
        return this.containers.size();
    }

    /**
     * Get sorted properties.
     *
     * @return list of process properties
     */
    public List<ProcessProperty> getSortedProperties() {
        Comparator<ProcessProperty> comp = new ProcessProperty.CompareProperties();
        Collections.sort(this.processPropertyList, comp);
        return this.processPropertyList;
    }

    /**
     * Get containerless properties.
     *
     * @return list of process properties
     */
    public List<ProcessProperty> getContainerlessProperties() {
        List<ProcessProperty> answer = new ArrayList<ProcessProperty>();
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

    public List<Integer> getContainerList() {
        return new ArrayList<Integer>(this.containers.keySet());
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
        List<ProcessProperty> answer = new ArrayList<ProcessProperty>();

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

    /**
     * Duplicate container for single.
     *
     * @return String
     */
    public String duplicateContainerForSingle() throws IOException, ResponseException {
        Integer currentContainer = this.processProperty.getContainer();
        List<ProcessProperty> plist = new ArrayList<ProcessProperty>();
        // search for all properties in container
        for (ProcessProperty pt : this.processPropertyList) {
            if (pt.getContainer() == currentContainer) {
                plist.add(pt);
            }
        }
        int newContainerNumber = 0;
        if (currentContainer > 0) {
            newContainerNumber++;
            // find new unused container number
            boolean search = true;
            while (search) {
                if (!this.containers.containsKey(newContainerNumber)) {
                    search = false;
                } else {
                    newContainerNumber++;
                }
            }
        }
        // clone properties
        for (ProcessProperty pt : plist) {
            ProcessProperty newProp = pt.getClone(newContainerNumber);
            this.processPropertyList.add(newProp);
            this.processProperty = newProp;
            saveCurrentProperty();
        }
        loadProcessProperties(this.currentProcess);

        return "";
    }

    /**
     * TODO wird nur für currentStep ausgeführt.
     * 
     * @return String
     */
    public String duplicateContainerForAll() throws IOException, ResponseException {
        Integer currentContainer = this.processProperty.getContainer();
        List<ProcessProperty> plist = new ArrayList<ProcessProperty>();
        // search for all properties in container
        for (ProcessProperty pt : this.processPropertyList) {
            if (pt.getContainer() == currentContainer) {
                plist.add(pt);
            }
        }

        int newContainerNumber = 0;
        if (currentContainer > 0) {
            newContainerNumber++;
            boolean search = true;
            while (search) {
                if (!this.containers.containsKey(newContainerNumber)) {
                    search = false;
                } else {
                    newContainerNumber++;
                }
            }
        }
        // clone properties
        for (ProcessProperty pt : plist) {
            ProcessProperty newProp = pt.getClone(newContainerNumber);
            this.processPropertyList.add(newProp);
            this.processProperty = newProp;
            saveCurrentPropertyForAll();
        }
        loadProcessProperties(this.currentProcess);
        return "";
    }

}
