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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.goobi.production.properties.ProcessProperty;
import org.goobi.production.properties.PropertyParser;

import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Prozess;
import org.kitodo.data.database.beans.Prozesseigenschaft;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.ProzessDAO;

public class BatchProcessHelper {

	private final Set<Prozess> processes;
	private final ProzessDAO pdao = new ProzessDAO();
	private static final Logger logger = Logger.getLogger(BatchProcessHelper.class);
	private Prozess currentProcess;
	private List<ProcessProperty> processPropertyList;
	private ProcessProperty processProperty;
	private Map<Integer, PropertyListObject> containers = new TreeMap<Integer, PropertyListObject>();
	private Integer container;

	public BatchProcessHelper(Batch batch) {
		this.processes = batch.getProcesses();
		for (Prozess p : processes) {

			this.processNameList.add(p.getTitel());
		}
		this.currentProcess = processes.iterator().next();
		this.processName = this.currentProcess.getTitel();
		loadProcessProperties(this.currentProcess);
	}

	public Prozess getCurrentProcess() {
		return this.currentProcess;
	}

	public void setCurrentProcess(Prozess currentProcess) {
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

	public void setProcessName(String processName) {
		this.processName = processName;
		for (Prozess s : this.processes) {
			if (s.getTitel().equals(processName)) {
				this.currentProcess = s;
				loadProcessProperties(this.currentProcess);
				break;
			}
		}
	}

	public void saveCurrentProperty() {
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
				Prozesseigenschaft pe = new Prozesseigenschaft();
				pe.setProzess(this.currentProcess);
				this.processProperty.setProzesseigenschaft(pe);
				this.currentProcess.getEigenschaftenInitialized().add(pe);
			}
			this.processProperty.transfer();

			Prozess p = this.currentProcess;
			List<Prozesseigenschaft> props = p.getEigenschaftenList();
			for (Prozesseigenschaft pe : props) {
				if (pe.getTitel() == null) {
					p.getEigenschaftenInitialized().remove(pe);
				}
			}
			if (!this.processProperty.getProzesseigenschaft().getProzess().getEigenschaftenInitialized().contains(this.processProperty.getProzesseigenschaft())) {
				this.processProperty.getProzesseigenschaft().getProzess().getEigenschaftenInitialized().add(this.processProperty.getProzesseigenschaft());
			}
			try {
				this.pdao.save(this.currentProcess);
				Helper.setMeldung("propertySaved");
			} catch (DAOException e) {
				logger.error(e);
				Helper.setFehlerMeldung("propertyNotSaved");
			}
		}
	}

	public void saveCurrentPropertyForAll() {
		List<ProcessProperty> ppList = getContainerProperties();
		boolean error = false;
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
				Prozesseigenschaft pe = new Prozesseigenschaft();
				pe.setProzess(this.currentProcess);
				this.processProperty.setProzesseigenschaft(pe);
				this.currentProcess.getEigenschaftenInitialized().add(pe);
			}			
			this.processProperty.transfer();

			Prozesseigenschaft pe = new Prozesseigenschaft();
			pe.setTitel(this.processProperty.getName());
			pe.setWert(this.processProperty.getValue());
			pe.setContainer(this.processProperty.getContainer());

			for (Prozess s : this.processes) {
				Prozess process = s;
				if (!s.equals(this.currentProcess)) {

					if (pe.getTitel() != null) {
						boolean match = false;

						for (Prozesseigenschaft processPe : process.getEigenschaftenList()) {
							if (processPe.getTitel() != null) {
								if (pe.getTitel().equals(processPe.getTitel()) && pe.getContainer() == null ? processPe
										.getContainer() == null : pe.getContainer().equals(processPe.getContainer())) {
									processPe.setWert(pe.getWert());
									match = true;
									break;
								}
							}
						}
						if (!match) {
							Prozesseigenschaft p = new Prozesseigenschaft();
							p.setTitel(pe.getTitel());
							p.setWert(pe.getWert());
							p.setContainer(pe.getContainer());
							p.setType(pe.getType());
							p.setProzess(process);
							process.getEigenschaftenInitialized().add(p);
						}
					}
				} else {
					if (!process.getEigenschaftenList().contains(this.processProperty.getProzesseigenschaft())) {
						process.getEigenschaftenInitialized().add(this.processProperty.getProzesseigenschaft());
					}
				}

				List<Prozesseigenschaft> props = process.getEigenschaftenList();
				for (Prozesseigenschaft peig : props) {
					if (peig.getTitel() == null) {
						process.getEigenschaftenInitialized().remove(peig);
					}
				}

				try {
					this.pdao.save(process);
				} catch (DAOException e) {
					error = true;
					logger.error(e);
					List<String> param = new ArrayList<String>();
					param.add(process.getTitel());
					String value = Helper.getTranslation("propertiesForProcessNotSaved", param);
					Helper.setFehlerMeldung(value);
				}
			}
		}
		if (!error) {
			Helper.setMeldung("propertiesSaved");
		}
	}

	private void loadProcessProperties(Prozess process) {
		this.pdao.refresh(this.currentProcess);
		this.containers = new TreeMap<Integer, PropertyListObject>();
		this.processPropertyList = PropertyParser.getPropertiesForProcess(this.currentProcess);
		
		for (ProcessProperty pt : this.processPropertyList) {
		    if (pt.getProzesseigenschaft() == null) {
                Prozesseigenschaft pe = new Prozesseigenschaft();
                pe.setProzess(process);
                pt.setProzesseigenschaft(pe);
                process.getEigenschaftenInitialized().add(pe);
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
		for (Prozess p : this.processes) {
			for (Prozesseigenschaft pe : p.getEigenschaftenList()) {
				if (!this.containers.keySet().contains(pe.getContainer())) {
					this.containers.put(pe.getContainer(), null);
				}
			}
		}
		
	}

	public Map<Integer, PropertyListObject> getContainers() {
		return this.containers;
	}

	public int getContainersSize() {
		if (this.containers == null) {
			return 0;
		}
		return this.containers.size();
	}

	public List<ProcessProperty> getSortedProperties() {
		Comparator<ProcessProperty> comp = new ProcessProperty.CompareProperties();
		Collections.sort(this.processPropertyList, comp);
		return this.processPropertyList;
	}

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

	public void setContainer(Integer container) {
		this.container = container;
		if (container != null && container > 0) {
			this.processProperty = getContainerProperties().get(0);
		}
	}

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

	public String duplicateContainerForSingle() {
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

	// TODO wird nur für currentStep ausgeführt
	public String duplicateContainerForAll() {
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
