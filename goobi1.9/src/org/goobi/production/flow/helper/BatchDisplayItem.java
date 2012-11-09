package org.goobi.production.flow.helper;
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
import java.util.HashMap;
import java.util.List;

import de.sub.goobi.Beans.Schritt;
import de.sub.goobi.helper.enums.StepStatus;

public class BatchDisplayItem implements Comparable<BatchDisplayItem>{

	private String stepTitle = "";
	private Integer stepOrder = null;
	private StepStatus stepStatus = StepStatus.DONE;
	private HashMap<String, String> scripts = new HashMap<String, String>();
	private boolean exportDMS = false;

	public BatchDisplayItem(Schritt s) {
		this.stepTitle = s.getTitel();
		this.stepOrder = s.getReihenfolge();
		this.stepStatus = s.getBearbeitungsstatusEnum();
		this.scripts.putAll(s.getAllScripts());
		this.exportDMS = s.isTypExportDMS();
	}

	public String getStepTitle() {
		return this.stepTitle;
	}

	public void setStepTitle(String stepTitle) {
		this.stepTitle = stepTitle;
	}

	public Integer getStepOrder() {
		return this.stepOrder;
	}

	public void setStepOrder(Integer stepOrder) {
		this.stepOrder = stepOrder;
	}

	public StepStatus getStepStatus() {
		return this.stepStatus;
	}

	public void setStepStatus(StepStatus stepStatus) {
		this.stepStatus = stepStatus;
	}

	@Override
	public int compareTo(BatchDisplayItem o) {
	
		return this.getStepOrder().compareTo(o.getStepOrder());
	}

	public HashMap<String, String> getScripts() {
		return this.scripts;
	}

	public void setScripts(HashMap<String, String> scripts) {
		this.scripts = scripts;
	}
	
	public int getScriptSize() {
		return this.scripts.size();
	}
	
	public List<String> getScriptnames() {
		List<String> answer = new ArrayList<String>();
		answer.addAll(this.scripts.keySet());		
		return answer;
	}

	public boolean getExportDMS() {
		return this.exportDMS;
	}

	public void setExportDMS(boolean exportDMS) {
		this.exportDMS = exportDMS;
	}
}
