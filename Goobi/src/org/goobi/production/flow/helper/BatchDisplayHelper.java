package org.goobi.production.flow.helper;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.util.List;

import de.sub.goobi.helper.enums.StepStatus;

public class BatchDisplayHelper {

	private List<BatchDisplayItem> stepList = null;
	private boolean panelOpen = false;
	
	public BatchDisplayHelper() {
	}
	
	
	public boolean isPanelOpen() {
		return this.panelOpen;
	}

	public void setPanelOpen(boolean panelOpen) {
		this.panelOpen = panelOpen;
	}
	
	
	public List<BatchDisplayItem> getStepList() {
		return this.stepList;
	}
	
	public void setStepList(List<BatchDisplayItem> stepList) {
		this.stepList = stepList;
	}
	
	
	/*
	 * Auswertung des Fortschritts
	 */

	public String getFortschritt() {
		int offen = 0;
		int inBearbeitung = 0;
		int abgeschlossen = 0;

		for (BatchDisplayItem bdi : this.stepList) {
			if (bdi.getStepStatus() == StepStatus.DONE) {
				abgeschlossen++;
			} else if (bdi.getStepStatus() == StepStatus.LOCKED) {
				offen++;
			} else {
				inBearbeitung++;
			}
		}
		double offen2 = 0;
		double inBearbeitung2 = 0;
		double abgeschlossen2 = 0;

		if ((offen + inBearbeitung + abgeschlossen) == 0) {
			offen = 1;
		}

		offen2 = (offen * 100) / (double) (offen + inBearbeitung + abgeschlossen);
		inBearbeitung2 = (inBearbeitung * 100) / (double) (offen + inBearbeitung + abgeschlossen);
		abgeschlossen2 = 100 - offen2 - inBearbeitung2;
		// (abgeschlossen * 100) / (offen + inBearbeitung + abgeschlossen);
		java.text.DecimalFormat df = new java.text.DecimalFormat("#000");
		return df.format(abgeschlossen2) + df.format(inBearbeitung2) + df.format(offen2);
	}

	public int getFortschritt1() {
		int offen = 0;
		int inBearbeitung = 0;
		int abgeschlossen = 0;

		for (BatchDisplayItem bdi : this.stepList) {
			if (bdi.getStepStatus() == StepStatus.DONE) {
				abgeschlossen++;
			} else if (bdi.getStepStatus() == StepStatus.LOCKED) {
				offen++;
			} else {
				inBearbeitung++;
			}
		}
		if ((offen + inBearbeitung + abgeschlossen) == 0) {
			offen = 1;
		}
		return (offen * 100) / (offen + inBearbeitung + abgeschlossen);
	}

	public int getFortschritt2() {
		int offen = 0;
		int inBearbeitung = 0;
		int abgeschlossen = 0;

		for (BatchDisplayItem bdi : this.stepList) {
			if (bdi.getStepStatus() == StepStatus.DONE) {
				abgeschlossen++;
			} else if (bdi.getStepStatus() == StepStatus.LOCKED) {
				offen++;
			} else {
				inBearbeitung++;
			}
		}
		if ((offen + inBearbeitung + abgeschlossen) == 0) {
			offen = 1;
		}
		return (inBearbeitung * 100) / (offen + inBearbeitung + abgeschlossen);
	}

	public int getFortschritt3() {
		int offen = 0;
		int inBearbeitung = 0;
		int abgeschlossen = 0;

		for (BatchDisplayItem bdi : this.stepList) {
			if (bdi.getStepStatus() == StepStatus.DONE) {
				abgeschlossen++;
			} else if (bdi.getStepStatus() == StepStatus.LOCKED) {
				offen++;
			} else {
				inBearbeitung++;
			}
		}
		if ((offen + inBearbeitung + abgeschlossen) == 0) {
			offen = 1;
		}
		double offen2 = 0;
		double inBearbeitung2 = 0;
		double abgeschlossen2 = 0;

		offen2 = (offen * 100) / (double) (offen + inBearbeitung + abgeschlossen);
		inBearbeitung2 = (inBearbeitung * 100) / (double) (offen + inBearbeitung + abgeschlossen);
		abgeschlossen2 = 100 - offen2 - inBearbeitung2;
		return (int) abgeschlossen2;
	}
}
