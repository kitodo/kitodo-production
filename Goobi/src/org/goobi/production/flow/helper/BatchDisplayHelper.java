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

package org.goobi.production.flow.helper;

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
