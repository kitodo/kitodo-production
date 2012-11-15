package de.sub.goobi.helper;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2011, intranda GmbH, Göttingen
 * 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.goobi.production.properties.ProcessProperty;
import org.goobi.production.properties.PropertyParser;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Prozesseigenschaft;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.ProzessDAO;

public class BatchProcessHelper {

	private List<Prozess> processes;
	private ProzessDAO pdao = new ProzessDAO();
	private static final Logger logger = Logger.getLogger(BatchProcessHelper.class);
	private Prozess currentProcess;
	private List<ProcessProperty> processPropertyList;
	private ProcessProperty processProperty;
	private Map<Integer, PropertyListObject> containers = new TreeMap<Integer, PropertyListObject>();
	private Integer container;

	public BatchProcessHelper(List<Prozess> processes) {
		this.processes = processes;
		for (Prozess p : processes) {

			this.processNameList.add(p.getTitel());
		}
		this.currentProcess = processes.get(0);
		this.processName = this.currentProcess.getTitel();
		loadProcessProperties(this.currentProcess);
	}

	public Prozess getCurrentProcess() {
		return this.currentProcess;
	}

	public void setCurrentProcess(Prozess currentProcess) {
		this.currentProcess = currentProcess;
	}

	public List<Prozess> getProcesses() {
		return this.processes;
	}

	public void setProcesses(List<Prozess> processes) {
		this.processes = processes;
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
				this.currentProcess.getEigenschaften().add(pe);
			}
			this.processProperty.transfer();

			Prozess p = this.currentProcess;
			List<Prozesseigenschaft> props = p.getEigenschaftenList();
			for (Prozesseigenschaft pe : props) {
				if (pe.getTitel() == null) {
					p.getEigenschaften().remove(pe);
				}
			}
			if (!this.processProperty.getProzesseigenschaft().getProzess().getEigenschaften().contains(this.processProperty.getProzesseigenschaft())) {
				this.processProperty.getProzesseigenschaft().getProzess().getEigenschaften().add(this.processProperty.getProzesseigenschaft());
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
				this.currentProcess.getEigenschaften().add(pe);
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
								if (pe.getTitel().equals(processPe.getTitel()) && pe.getContainer() == processPe.getContainer()) {
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
							process.getEigenschaften().add(p);
						}
					}
				} else {
					if (!process.getEigenschaftenList().contains(this.processProperty.getProzesseigenschaft())) {
						process.getEigenschaften().add(this.processProperty.getProzesseigenschaft());
					}
				}

				List<Prozesseigenschaft> props = process.getEigenschaftenList();
				for (Prozesseigenschaft peig : props) {
					if (peig.getTitel() == null) {
						process.getEigenschaften().remove(peig);
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
//		for (ProcessProperty pt : this.processPropertyList) {
//			if (!this.containers.keySet().contains(pt.getContainer())) {
//				this.containers.put(pt.getContainer(), 1);
//			} else {
//				this.containers.put(pt.getContainer(), this.containers.get(pt.getContainer()) + 1);
//			}
//		}
		for (Prozess p : this.processes) {
			for (Prozesseigenschaft pe : p.getEigenschaftenList()) {
				if (!this.containers.keySet().contains(pe.getContainer())) {
					this.containers.put(pe.getContainer(), null);
//				} else {
//					this.containers.put(pe.getContainer(), this.containers.get(pe.getContainer()) + 1);
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
		// int currentContainer = this.processProperty.getContainer();

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
