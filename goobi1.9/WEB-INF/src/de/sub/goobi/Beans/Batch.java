package de.sub.goobi.Beans;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.goobi.production.api.property.xmlbasedprovider.Status;
import org.goobi.production.flow.helper.BatchDisplayHelper;
import org.goobi.production.flow.helper.BatchDisplayItem;

import de.sub.goobi.Beans.Property.BatchProperty;
import de.sub.goobi.Beans.Property.DisplayPropertyList;
import de.sub.goobi.Beans.Property.IGoobiEntity;
import de.sub.goobi.Beans.Property.IGoobiProperty;
import de.sub.goobi.helper.enums.StepStatus;
/**
 * 
 * @author Robert Sehr
 *
 */
public class Batch implements Serializable, IGoobiEntity {

	private static final long serialVersionUID = -8503827557959453247L;
	private Integer batchId;
	private String title;
//	private List<Prozess> batchList = new ArrayList<Prozess>();
	private Set<BatchProperty> properties;
	private DisplayPropertyList displayProperties;
	private Projekt project;
	private List<BatchDisplayItem> stepList = new ArrayList<BatchDisplayItem>();
	private Set<Prozess> processes;
	private Benutzer user;
	private String stepTitle = null;
	private BatchDisplayHelper bdh = new BatchDisplayHelper();

	
//	zeitdesaf_PPN602167531_0093
//	sttb_8_cod_ms_hist_lit__48x_21
	
	public Batch() {
		this.title = "";
		this.properties = new HashSet<BatchProperty>();
		this.processes = new HashSet<Prozess>();
	}

	public BatchDisplayItem getCurrentStep() {
		for (BatchDisplayItem bdi : getStepList()) {
			if (bdi.getStepStatus().equals(StepStatus.OPEN)|| bdi.getStepStatus().equals(StepStatus.INWORK)){
				this.stepTitle = bdi.getStepTitle();
				return bdi;
			}
		}
		return null;		
	}
	
	private void generateWorkflowStatus() {
		for (Prozess p : this.processes) {
			for (Schritt s : p.getSchritteList()) {
				boolean match = false;
				for (BatchDisplayItem bdi : this.stepList) {
					if (s.getTitel().equals(bdi.getStepTitle())) {
						if (s.getReihenfolge() < bdi.getStepOrder()) {
							bdi.setStepOrder(s.getReihenfolge());
						}
						if (s.getBearbeitungsstatusEnum().getValue() < bdi.getStepStatus().getValue()) {
							bdi.setStepStatus(s.getBearbeitungsstatusEnum());
						}
						if (!s.getAllScripts().isEmpty()) {
							bdi.setScripts(s.getAllScripts());
						}
						bdi.setExportDMS(s.isTypExportDMS());
						match = true;
						break;
					}
				}
				if (!match) {
					BatchDisplayItem bdi = new BatchDisplayItem(s);
					this.stepList.add(bdi);
				}
			}
		}
		Collections.sort(this.stepList);
	}

	public void setId(Integer batchId) {
		this.batchId = batchId;
	}

	@Override
	public Integer getId() {
		return this.batchId;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return this.title;
	}

	

	public List<Prozess> getBatchList() {
		List<Prozess> temp = new ArrayList<Prozess>();
		if (this.processes != null) {
			temp.addAll(this.processes);
		}
		return temp;
		
	}

	public int getListSize() {
		return getBatchList().size();
	}
	

	@Override
	public Status getStatus() {
		return Status.getBatchStatus(this);
	}

	@Override
	public List<IGoobiProperty> getProperties() {
		List<IGoobiProperty> returnlist = new ArrayList<IGoobiProperty>();
		returnlist.addAll(getEigenschaftenList());
		return returnlist;
	}
	
	

	public List<BatchProperty> getEigenschaftenList() {
		if (this.properties == null) {
			return new ArrayList<BatchProperty>();
		} else {
			return new ArrayList<BatchProperty>(this.properties);
		}
	}

	@Override
	public void addProperty(IGoobiProperty toAdd) {
		this.properties.add((BatchProperty) toAdd);

	}

	@Override
	public void removeProperty(IGoobiProperty toRemove) {
		this.properties.remove(toRemove);
		toRemove.setOwningEntity(null);
	}

	public DisplayPropertyList getDisplayProperties() {
		if (this.displayProperties == null) {
			this.displayProperties = new DisplayPropertyList(this);
		}
		return this.displayProperties;
	}

	@Override
	public void refreshProperties() {
		this.displayProperties = null;
	}



	public void addProcessToBatch(Prozess p) {
		p.setBatch(this);
		this.processes.add(p);
	}

	public void removeProcessFromBatch(Prozess p) {
		p.setBatch(null);
		this.processes.remove(p);
	}

	public Projekt getProject() {
		if (this.project == null && this.processes.size() > 0) {
			this.project = getBatchList().get(0).getProjekt();
		}
		return this.project;
	}

	public void setProject(Projekt project) {
		this.project = project;
	}

	public List<BatchDisplayItem> getStepList() {
//		if (this.stepList.size() == 0) {
			generateWorkflowStatus();
//		}
		return this.stepList;
	}

	public void setStepList(List<BatchDisplayItem> stepList) {
		this.stepList = stepList;
	}

	
//	public static void main(String[] args) throws DAOException {
//		Batch b = new Batch();
//		ProzessDAO pd = new ProzessDAO();
//		Prozess kleiuniv = pd.get(1165);
//		b.addProcessToBatch(kleiuniv);
//		b.addProcessToBatch(pd.get(30582));
//		b.addProcessToBatch(pd.get(20945));
//		b.addProcessToBatch(pd.get(20466));
//		b.addProcessToBatch(pd.get(17999));
//		b.addProcessToBatch(pd.get(13937));
//		b.generateWorkflowStatus();
//		b.setProject(kleiuniv.getProjekt());
//		for (BatchDisplayItem bdi : b.stepList) {
//			System.out.println(bdi.getStepTitle() + "    "  + bdi.getStepOrder() + "    "  + bdi.getStepStatus().getTitle());
//		}
//		BatchDAO dao = new BatchDAO();
//		dao.save(b);
//		
//	}
	
//	public static void main(String[] args) throws DAOException {
//		BatchDAO dao = new BatchDAO();
//		Batch b = dao.get(2);
//		for (String s : b.getCurrentStep().getScriptnames())
//		 {
//			System.out.println(s);
//		}
//	}


	public Set<Prozess> getProcesses() {
		return this.processes;
	}


	public void setProcesses(Set<Prozess> processes) {
		this.processes = processes;
	}
	
	public Set<BatchProperty> getEigenschaften() {
		return this.properties;
	}

	public void setEigenschaften(Set<BatchProperty> eigenschaften) {
		this.properties = eigenschaften;
	}

	public Benutzer getUser() {
		return this.user;
	}

	public void setUser(Benutzer user) {
		this.user = user;
	}

	public String getStepTitle() {
		return this.stepTitle;
	}

	public void setStepTitle(String stepTitle) {
		this.stepTitle = stepTitle;
	}
	
	public BatchDisplayHelper getBatchDisplayHelper() {
		this.bdh.setStepList(getStepList());
		return this.bdh;
	}
	
}
