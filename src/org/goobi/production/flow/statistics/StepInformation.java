/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.production.flow.statistics;
public class StepInformation {

	// step identifier in workflow
	private String title = "";
	private Double averageStepOrder = new Double(0);
	
	// information about all steps of these type
	private int numberOfTotalSteps = 0;
	private int numberOfTotalImages = 0;
	private int totalProcessCount = 0;
	
	// information about all steps of these type with status done 
	private int numberOfStepsDone = 0;
	private int numberOfImagesDone = 0;
	private int processCountDone = 0;
	
	public StepInformation(){}
	
	
	public StepInformation(String title) {
		this.title = title;
	}
	
	public StepInformation(String title, Double avgOrdner) {
		this.title = title;
		this.averageStepOrder = avgOrdner;
	}
	
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return the averageStepOrder
	 */
	public Double getAverageStepOrder() {
		return averageStepOrder;
	}
	/**
	 * @param averageStepOrder the averageStepOrder to set
	 */
	public void setAverageStepOrder(Double averageStepOrder) {
		this.averageStepOrder = averageStepOrder;
	}
	/**
	 * @return the numberOfTotalSteps
	 */
	public int getNumberOfTotalSteps() {
		return numberOfTotalSteps;
	}
	/**
	 * @param numberOfTotalSteps the numberOfTotalSteps to set
	 */
	public void setNumberOfTotalSteps(int numberOfTotalSteps) {
		this.numberOfTotalSteps = numberOfTotalSteps;
	}
	/**
	 * @return the numberOfTotalImages
	 */
	public int getNumberOfTotalImages() {
		return numberOfTotalImages;
	}
	/**
	 * @param numberOfTotalImages the numberOfTotalImages to set
	 */
	public void setNumberOfTotalImages(int numberOfTotalImages) {
		this.numberOfTotalImages = numberOfTotalImages;
	}
	/**
	 * @return the totalProcessCount
	 */
	public int getTotalProcessCount() {
		return totalProcessCount;
	}
	/**
	 * @param totalProcessCount the totalProcessCount to set
	 */
	public void setTotalProcessCount(int totalProcessCount) {
		this.totalProcessCount = totalProcessCount;
	}
	/**
	 * @return the numberOfStepsDone
	 */
	public int getNumberOfStepsDone() {
		return numberOfStepsDone;
	}
	/**
	 * @param numberOfStepsDone the numberOfStepsDone to set
	 */
	public void setNumberOfStepsDone(int numberOfStepsDone) {
		this.numberOfStepsDone = numberOfStepsDone;
	}
	/**
	 * @return the numberOfImagesDone
	 */
	public int getNumberOfImagesDone() {
		return numberOfImagesDone;
	}
	/**
	 * @param numberOfImagesDone the numberOfImagesDone to set
	 */
	public void setNumberOfImagesDone(int numberOfImagesDone) {
		this.numberOfImagesDone = numberOfImagesDone;
	}
	/**
	 * @return the processCountDone
	 */
	public int getProcessCountDone() {
		return processCountDone;
	}
	/**
	 * @param processCountDone the processCountDone to set
	 */
	public void setProcessCountDone(int processCountDone) {
		this.processCountDone = processCountDone;
	}
}
