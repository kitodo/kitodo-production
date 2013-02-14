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

package org.goobi.production.api.property.xmlbasedprovider;
import de.sub.goobi.beans.Projekt;
import de.sub.goobi.beans.Prozess;
import de.sub.goobi.beans.Schritt;
import de.sub.goobi.beans.Vorlage;
import de.sub.goobi.beans.Werkstueck;

/**
 * simple POJO class to generate PropertyTemplate for a IGoobiEntity
 * @author rsehr
 *
 */
public class Status {

	private String project;
	private String process;
	private String step;
	private String product;
	private String productionResource;

	public void setProject(String project) {
		this.project = project;
	}

	public String getProject() {
		return project;
	}

	public void setProcess(String process) {
		this.process = process;
	}

	public String getProcess() {
		return process;
	}

	public void setStep(String step) {
		this.step = step;
	}

	public String getStep() {
		return step;
	}

	public void setProduct(String digitalDocument) {
		this.product = digitalDocument;
	}

	public String getProduct() {
		return product;
	}

	public void setProductionResource(String original) {
		this.productionResource = original;
	}

	public String getProductionResource() {
		return productionResource;
	}

	public static Status getProjectStatus(Projekt inEntity) {
		Status myHappyStatus = new Status();
		myHappyStatus.setProject(inEntity.getTitel());
		return myHappyStatus;
	}

	public static Status getProcessStatus(Prozess inEntity) {
		Status myHappyStatus = new Status();
		myHappyStatus.setProject(inEntity.getProjekt().getTitel());
		myHappyStatus.setProcess(inEntity.getTitel());
		return myHappyStatus;
	}

	public static Status getStepStatus(Schritt inEntity) {
		Status myHappyStatus = new Status();
		myHappyStatus.setProject(inEntity.getProzess().getProjekt().getTitel());
		myHappyStatus.setProcess(inEntity.getProzess().getTitel());
		myHappyStatus.setStep(inEntity.getTitel());
		return myHappyStatus;
	}

	public static Status getResourceStatusFromEntity(Vorlage inEntity) {
		Status myHappyStatus = new Status();
		myHappyStatus.setProject(inEntity.getProzess().getProjekt().getTitel());
		myHappyStatus.setProcess(inEntity.getProzess().getTitel());
		myHappyStatus.setProductionResource(String.valueOf(inEntity.getId()));
		return myHappyStatus;
	}

	public static Status getProductStatusFromEntity(Werkstueck inEntity) {
		Status myHappyStatus = new Status();
		myHappyStatus.setProject(inEntity.getProzess().getProjekt().getTitel());
		myHappyStatus.setProcess(inEntity.getProzess().getTitel());
		myHappyStatus.setProduct(String.valueOf(inEntity.getId()));
		return myHappyStatus;
	}

}
